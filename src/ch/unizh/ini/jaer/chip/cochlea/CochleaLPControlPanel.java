package ch.unizh.ini.jaer.chip.cochlea;

import static java.awt.Component.TOP_ALIGNMENT;
import static java.awt.Component.LEFT_ALIGNMENT;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import ch.unizh.ini.jaer.chip.cochlea.gui.CochleaLPChannelCP;
import ch.unizh.ini.jaer.config.AbstractChipControlPanel;
import ch.unizh.ini.jaer.config.spi.SPIConfigValue;
import java.util.HashSet;
import net.sf.jaer.chip.Chip;
import net.sf.jaer.biasgen.BiasgenPanel;
import net.sf.jaer.biasgen.coarsefine.ShiftedSourceControlsCF;

/**
 * Control panel for CochleaLP
 *
 * @author Luca Longinotti, Minhao Liu, Shih-Chii Liu, Tobi Delbruck
 */
public final class CochleaLPControlPanel extends AbstractChipControlPanel {

	private static final long serialVersionUID = -7435419921722582550L;
	private JPanel onchipBiasgenPanel;
	private JPanel offchipDACPanel;
	private JPanel channelPanel;
	private JPanel scannerPanel;
	private JPanel aerPanel;
	private JPanel adcPanel;
	private JPanel chipDiagPanel;

	public CochleaLPControlPanel(final CochleaLP chip) {
		super((Chip) chip);
		initComponents();
		
		onchipBiasgenPanel.add(getBiasgen().biasForceEnable.makeGUIControl());
		onchipBiasgenPanel.add(new ShiftedSourceControlsCF(getBiasgen().ssBiases[0]));
		onchipBiasgenPanel.add(new ShiftedSourceControlsCF(getBiasgen().ssBiases[1]));

		biasgen.setPotArray(getBiasgen().ipots);
		onchipBiasgenPanel.add(new BiasgenPanel(getBiasgen()));

		onchipBiasgenPanel.add(Box.createVerticalGlue()); // push up to prevent expansion of PotPanel

		offchipDACPanel.add(getBiasgen().dacRun.makeGUIControl());

		biasgen.setPotArray(getBiasgen().vpots);
		offchipDACPanel.add(new BiasgenPanel(getBiasgen()));

		SPIConfigValue.addGUIControls(scannerPanel, getBiasgen().scannerControl);
		SPIConfigValue.addGUIControls(aerPanel, getBiasgen().aerControl);
		SPIConfigValue.addGUIControls(adcPanel, getBiasgen().adcControl);
		SPIConfigValue.addGUIControls(chipDiagPanel, getBiasgen().chipDiagChain);

		// Add cochlea channel configuration GUI.
		final int CHAN_PER_COL = 32;
		int chanCount = 0;
		// Global control for all channels
		CochleaLPChannelGroupConfig globalChanControl = new CochleaLPChannelGroupConfig("Global", 
			"Global control for all cochlea channels", getBiasgen().cochleaChannels, chip);
		final CochleaLPChannelCP gPan = new CochleaLPChannelCP((CochleaLPChannelConfig)globalChanControl);
		gPan.setAlignmentX(LEFT_ALIGNMENT);
		gPan.setAlignmentY(TOP_ALIGNMENT);
		channelPanel.add(gPan);

		JPanel chanPanChannels = new JPanel();
		chanPanChannels.setLayout(new BoxLayout(chanPanChannels, BoxLayout.X_AXIS));
		chanPanChannels.setAlignmentX(LEFT_ALIGNMENT);	// Should have the same alignment as the gPan
		chanPanChannels.setAlignmentY(TOP_ALIGNMENT);

		channelPanel.add(chanPanChannels);

		JPanel colPan = new JPanel();
		colPan.setLayout(new BoxLayout(colPan, BoxLayout.Y_AXIS));
		colPan.setAlignmentY(TOP_ALIGNMENT);
		colPan.setAlignmentX(LEFT_ALIGNMENT);

		for (final CochleaChannelConfig chan : getBiasgen().cochleaChannels) {
			// TODO add preference change or update listener to
			// synchronize when config is loaded

			// TODO add undo/redo support for channels

			final CochleaLPChannelCP cPan = new CochleaLPChannelCP((CochleaLPChannelConfig)chan);
			colPan.add(cPan);
			chanCount++;
			if ((chanCount % CHAN_PER_COL) == 0) {
				chanPanChannels.add(colPan);
				if (chanCount < getBiasgen().cochleaChannels.size()) {
					colPan = new JPanel();
					colPan.setLayout(new BoxLayout(colPan, BoxLayout.Y_AXIS));
					colPan.setAlignmentY(TOP_ALIGNMENT);
				}
			}
		}
		setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		channelPanel.setMinimumSize(channelPanel.getLayout().minimumLayoutSize(channelPanel));
		channelPanel.revalidate();
		setPreferredSize(new Dimension(800, 600));
		revalidate();

		setSelectedIndex(chip.getPrefs().getInt(prefNameSelectedTab, 0));
	}

	/**
	 * @return the biasgen
	 */
	public CochleaLP.Biasgen getBiasgen() {
		return (CochleaLP.Biasgen)biasgen;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {

		onchipBiasgenPanel = new JPanel();
		onchipBiasgenPanel.setLayout(new BoxLayout(onchipBiasgenPanel, BoxLayout.Y_AXIS));
		onchipBiasgenPanel.setAlignmentX(LEFT_ALIGNMENT);
		addTab("On-chip biases (biasgen)", (onchipBiasgenPanel));

		offchipDACPanel = new JPanel();
		offchipDACPanel.setLayout(new BoxLayout(offchipDACPanel, BoxLayout.Y_AXIS));
		addTab("Off-chip biases (DAC)", (offchipDACPanel));

		channelPanel = new JPanel();
		channelPanel.setLayout(new BoxLayout(channelPanel, BoxLayout.Y_AXIS));
		addTab("Channels", (channelPanel));

		scannerPanel = new JPanel();
		scannerPanel.setLayout(new BoxLayout(scannerPanel, BoxLayout.Y_AXIS));
		addTab("Scanner Config", (scannerPanel));

		aerPanel = new JPanel();
		aerPanel.setLayout(new BoxLayout(aerPanel, BoxLayout.Y_AXIS));
		addTab("AER Config", (aerPanel));

		adcPanel = new JPanel();
		adcPanel.setLayout(new BoxLayout(adcPanel, BoxLayout.Y_AXIS));
		addTab("ADC", (adcPanel));

		chipDiagPanel = new JPanel();
		chipDiagPanel.setAlignmentX(LEFT_ALIGNMENT);
		chipDiagPanel.setLayout(new BoxLayout(chipDiagPanel, BoxLayout.Y_AXIS));
		addTab("Chip Diag Config", (chipDiagPanel));
	}
}
