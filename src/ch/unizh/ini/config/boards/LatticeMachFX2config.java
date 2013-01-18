/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.unizh.ini.config.boards;

import ch.unizh.ini.config.AbstractConfigValue;
import ch.unizh.ini.config.HasPreference;
import ch.unizh.ini.config.cpld.CPLDConfigValue;
import ch.unizh.ini.config.cpld.CPLDShiftRegister;
import ch.unizh.ini.config.fx2.PortBit;
import ch.unizh.ini.config.fx2.TriStateablePortBit;
import ch.unizh.ini.config.onchip.ChipConfigChain;
import ch.unizh.ini.config.onchip.OnchipConfigBit;
import ch.unizh.ini.config.onchip.OutputMux;
import eu.seebetter.ini.chips.sbret10.SBret10;
import eu.seebetter.ini.chips.sbret10.SBret10config;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;
import net.sf.jaer.biasgen.*;
import net.sf.jaer.biasgen.VDAC.VPot;
import net.sf.jaer.biasgen.coarsefine.AddressedIPotCF;
import net.sf.jaer.biasgen.coarsefine.ShiftedSourceBiasCF;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.chip.Chip;
import net.sf.jaer.hardwareinterface.HardwareInterfaceException;
import net.sf.jaer.hardwareinterface.usb.cypressfx2.CypressFX2;

/**
 *
 * @author Christian
 */
public class LatticeMachFX2config extends Biasgen implements HasPreference{

    public AEChip chip;      
    protected ShiftedSourceBiasCF[] ssBiases = new ShiftedSourceBiasCF[2];
    protected ChipConfigChain chipConfigChain = null;
    protected ArrayList<Biasgen.HasPreference> hasPreferencesList = new ArrayList<Biasgen.HasPreference>();
    
    public LatticeMachFX2config(Chip chip) {
        super(chip);
        this.chip = (AEChip) chip;
    }
    
    /***************************  FX2  *************************/
    
    /** Vendor request command understood by the firmware in connection with  VENDOR_REQUEST_SEND_BIAS_BYTES */
    public final ConfigCmd CMD_IPOT = new ConfigCmd(1, "IPOT"),
            CMD_AIPOT = new ConfigCmd(2, "AIPOT"),
            CMD_SCANNER = new ConfigCmd(3, "SCANNER"),
            CMD_CHIP_CONFIG = new ConfigCmd(4, "CHIP"),
            CMD_SETBIT = new ConfigCmd(5, "SETBIT"),
            CMD_CPLD_CONFIG = new ConfigCmd(8, "CPLD");
    public final String[] CMD_NAMES = {"IPOT", "AIPOT", "SCANNER", "CHIP", "SET_BIT", "CPLD_CONFIG"};
    final byte[] emptyByteArray = new byte[0];
    
    /** Command sent to firmware by vendor request */
    public class ConfigCmd {

        short code;
        String name;

        public ConfigCmd(int code, String name) {
            this.code = (short) code;
            this.name = name;
        }

        @Override
        public String toString() {
            return "ConfigCmd{" + "code=" + code + ", name=" + name + '}';
        }
    }
    
    /** List of direct port bits
     * 
     */
    protected ArrayList<PortBit> portBits = new ArrayList();
    
    
    
    
    /***************************  CPLD  *************************/
    
    
    /** Active container for CPLD configuration, which know how to format the data for the CPLD shift register.
     * 
     */
    protected CPLDShiftRegister cpldConfig = new CPLDShiftRegister();
    /** List of configuration values
     * 
     */
    protected ArrayList<AbstractConfigValue> configValues = new ArrayList<AbstractConfigValue>();
    
    /** List of CPLD configuration values
     * 
     */
    protected ArrayList<CPLDConfigValue> cpldConfigValues = new ArrayList();

    /** Adds a value, adding it to the appropriate internal containers, and adding this as an observer of the value.
     * 
     * @param value some configuration value
     */
    public void addConfigValue(AbstractConfigValue value) {
        if (value == null) {
            return;
        }
        configValues.add(value);
        if (value instanceof CPLDConfigValue) {
            cpldConfig.add((CPLDConfigValue) value);
        } else if (value instanceof PortBit) {
            portBits.add((PortBit) value);
        }
        value.addObserver(this);
        log.info("Added " + value);
    }

    /** Clears all lists of configuration values.
     * @see AbstractConfigValue
     * 
     */
    public void clearConfigValues() {
        cpldConfig.clear();
        configValues.clear();
        portBits.clear();
        cpldConfigValues.clear();
    }
    
    
    /** Sends complete configuration to hardware by calling several updates with objects
    * 
    * @param biasgen this object
    * @throws HardwareInterfaceException on some error
    */
    @Override
    public void sendConfiguration(Biasgen biasgen) throws HardwareInterfaceException {

        if (isBatchEditOccurring()) {
            log.info("batch edit occurring, not sending configuration yet");
            return;
        }

        log.info("sending full configuration");
        if (!sendOnchipConfig()) {
            return;
        }

        sendCPLDConfig();
        for (PortBit b : portBits) {
            update(b, null);
        }
    }
    
    /** Quick addConfigValue of an addressed pot from a string description, comma delimited
    * 
    * @param s , e.g. "Amp,n,normal,DVS ON threshold"; separate tokens for name,sex,type,tooltip\nsex=n|p, type=normal|cascode
    * @throws ParseException Error
    */
    protected void addAIPot(String s) throws ParseException {
        try {
            String d = ",";
            StringTokenizer t = new StringTokenizer(s, d);
            if (t.countTokens() != 4) {
                throw new Error("only " + t.countTokens() + " tokens in pot " + s + "; use , to separate tokens for name,sex,type,tooltip\nsex=n|p, type=normal|cascode");
            }
            String name = t.nextToken();
            String a;
            a = t.nextToken();
            Pot.Sex sex = null;
            if (a.equalsIgnoreCase("n")) {
                sex = Pot.Sex.N;
            } else if (a.equalsIgnoreCase("p")) {
                sex = Pot.Sex.P;
            } else {
                throw new ParseException(s, s.lastIndexOf(a));
            }

            a = t.nextToken();

            Pot.Type type = null;
            if (a.equalsIgnoreCase("normal")) {
                type = Pot.Type.NORMAL;
            } else if (a.equalsIgnoreCase("cascode")) {
                type = Pot.Type.CASCODE;
            } else {
                throw new ParseException(s, s.lastIndexOf(a));
            }

            String tip = t.nextToken();

            /*     public ConfigurableIPot32(SeeBetterConfig biasgen, String name, int shiftRegisterNumber,
            Type type, Sex sex, boolean lowCurrentModeEnabled, boolean enabled,
            int bitValue, int bufferBitValue, int displayPosition, String tooltipString) {
                */

            int address = getPotArray().getNumPots();
            getPotArray().addPot(new AddressedIPotCF(this, name, address++,
                    type, sex, false, true,
                    AddressedIPotCF.maxCoarseBitValue / 2, AddressedIPotCF.maxFineBitValue, address, tip));
        } catch (Exception e) {
            throw new Error(e.toString());
        }
    }
    
    protected boolean sendAIPot(AddressedIPot pot) throws HardwareInterfaceException{            
        byte[] bytes = pot.getBinaryRepresentation();
        if (bytes == null) {
            return false; // not ready yet, called by super
        }
        String hex = String.format("%02X%02X%02X",bytes[2],bytes[1],bytes[0]);
        //log.info("Send AIPot for "+pot.getName()+" with value "+hex);
        sendConfig(CMD_AIPOT, 0, bytes); // the usual packing of ipots with other such as shifted sources, on-chip voltage dac, and diagnotic mux output and extra configuration
        return true;
    }

    protected void sendCPLDConfig() throws HardwareInterfaceException {
        byte[] bytes = cpldConfig.getBytes();

        log.info("Send CPLD Config: "+cpldConfig.toString());
        sendConfig(CMD_CPLD_CONFIG, 0, bytes);
    }

    public static final byte VR_WRITE_CONFIG = (byte) 0xB8;
    
    /** convenience method for sending configuration to hardware. Sends vendor request VR_WRITE_CONFIG with subcommand cmd, index index and bytes bytes.
        * 
        * @param cmd the subcommand to set particular configuration, e.g. CMD_CPLD_CONFIG
        * @param index unused
        * @param bytes the payload
        * @throws HardwareInterfaceException 
        */
    protected void sendConfig(ConfigCmd cmd, int index, byte[] bytes) throws HardwareInterfaceException {

//            StringBuilder sb = new StringBuilder(String.format("sending config cmd=0x%X (%s) index=0x%X with %d bytes", cmd.code, cmd.name, index, bytes.length));
        if (bytes == null || bytes.length == 0) {
        } else {
            int max = 50;
            if (bytes.length < max) {
                max = bytes.length;
            }
//                sb.append(" = ");
//                for (int i = 0; i < max; i++) {
//                    sb.append(String.format("%X, ", bytes[i]));
//                }
//                log.info(sb.toString());
        } // end debug

        if (bytes == null) {
            bytes = emptyByteArray;
        }
//            log.info(String.format("sending command vendor request cmd=%d, index=%d, and %d bytes", cmd, index, bytes.length));
        if (getHardwareInterface() != null && getHardwareInterface() instanceof CypressFX2) {
            ((CypressFX2) getHardwareInterface()).sendVendorRequest(VR_WRITE_CONFIG, (short) (0xffff & cmd.code), (short) (0xffff & index), bytes); // & to prevent sign extension for negative shorts
        }
    }

    /** 
        * Convenience method for sending configuration to hardware. Sends vendor request VENDOR_REQUEST_SEND_BIAS_BYTES with subcommand cmd, index index and empty byte array.
        * 
        * @param cmd the subcommand
        * @param index data
        * @throws HardwareInterfaceException 
        */
    protected void sendConfig(ConfigCmd cmd, int index) throws HardwareInterfaceException {
        sendConfig(cmd, index, emptyByteArray);
    }
    
    
    /** Sends everything on the on-chip shift register 
    * 
    * @throws HardwareInterfaceException 
    * @return false if not sent because bytes are not yet initialized
    */
    protected boolean sendOnchipConfig() throws HardwareInterfaceException {
        log.info("Send whole OnChip Config");

        //biases
        if(getPotArray() == null){
            return false;
        }
        AddressedIPotArray ipots = (AddressedIPotArray) potArray;
        Iterator i = ipots.getShiftRegisterIterator();
        while(i.hasNext()){
            AddressedIPot iPot = (AddressedIPot) i.next();
            if(!sendAIPot(iPot))return false;
        }

        //shifted sources
        for (ShiftedSourceBiasCF ss : ssBiases) {
            if(!sendAIPot(ss))return false;
        }   

        //diagnose SR
        sendChipConfig();
        return true;
    }

    public boolean sendChipConfig() throws HardwareInterfaceException{

        String onChipConfigBits = chipConfigChain.getBitString();
        byte[] onChipConfigBytes = bitString2Bytes(onChipConfigBits);
        if(onChipConfigBits == null){
            return false;
        } else {
            BigInteger bi = new BigInteger(onChipConfigBits);
            //System.out.println("Send on chip config (length "+onChipConfigBits.length+" bytes): "+String.format("%0"+(onChipConfigBits.length<<1)+"X", bi));
            log.info("Send on chip config: "+onChipConfigBits);
            sendConfig(CMD_CHIP_CONFIG, 0, onChipConfigBytes);
            return true;
        }
    }
    
    @Override
    public void loadPreference() {
        super.loadPreferences();

        if (hasPreferencesList != null) {
            for (HasPreference hp : hasPreferencesList) {
                hp.loadPreference();
            }
        }

        if (ssBiases != null) {
            for (ShiftedSourceBiasCF ss : ssBiases) {
                ss.loadPreferences();
            }
        }
    }

    @Override
    public void storePreference() {
        super.storePreferences();
        for (HasPreference hp : hasPreferencesList) {
            hp.storePreference();
        }
        if (ssBiases != null) {
            for (ShiftedSourceBiasCF ss : ssBiases) {
                ss.storePreferences();
            }
        }
    }
    
}
