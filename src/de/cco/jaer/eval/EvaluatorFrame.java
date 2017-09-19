/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cco.jaer.eval;

import de.cco.jaer.eval.ResultEvaluator;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JLabel;

/**
 *
 * @author viktor
 */
public class EvaluatorFrame extends javax.swing.JFrame {
    
    ResultEvaluator reval;
    EvaluatorThreshold thresh;
    
    boolean listening;
    List<PropertyChangeSupport> pcsl;
    PropertyChangeListener filterStateListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (pce.getPropertyName().equals("filterEnabled")) {
                boolean val = (boolean) pce.getNewValue();
                if (val == false) {
                    System.out.println("Filter disabled.");
                    System.out.println("Stopping evaluation.");
                    enableCheckBox.setSelected(false);
                    enableCheckBox.setEnabled(false);
                    drawCheckBox.setSelected(false);
                    drawCheckBox.setEnabled(false);
                    visualizeLabel.setEnabled(false);
                    reval.draw(false);
                    reval.arm(false);
                }
                else if (val == true) {
                    System.out.println("Filter enabled.");
                    enableCheckBox.setEnabled(true);
                    drawCheckBox.setEnabled(true);
                    visualizeLabel.setEnabled(true);
                }
            }
        }
    };

    /**
     * Creates new form EvaluatorFrame
     */
    public EvaluatorFrame() {
        reval = ResultEvaluator.getInstance();
        pcsl = new LinkedList<>();
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        enableCheckBox = new javax.swing.JCheckBox();
        visualizeLabel = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        drawCheckBox = new javax.swing.JCheckBox();
        tabbedPane = new javax.swing.JTabbedPane();
        ratePanel = new javax.swing.JPanel();
        rateSlider = new javax.swing.JSlider();
        rateLabel = new javax.swing.JLabel();
        posPanel = new javax.swing.JPanel();
        xSpinner = new javax.swing.JSpinner();
        ySpinner = new javax.swing.JSpinner();
        xLabel = new javax.swing.JLabel();
        yLabel = new javax.swing.JLabel();
        speedPane = new javax.swing.JPanel();
        speedSlider = new javax.swing.JSlider();
        speedLabel = new javax.swing.JLabel();

        setTitle("ResultEvaluator");
        setResizable(false);

        enableCheckBox.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        enableCheckBox.setText("Evaluate Results");
        enableCheckBox.setEnabled(false);
        enableCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableCheckBoxActionPerformed(evt);
            }
        });

        visualizeLabel.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        visualizeLabel.setText("Visualize");

        jRadioButton1.setText("Option 1");
        jRadioButton1.setEnabled(false);
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        jRadioButton2.setText("Option 2");
        jRadioButton2.setEnabled(false);
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        drawCheckBox.setText("Draw");
        drawCheckBox.setEnabled(false);
        drawCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drawCheckBoxActionPerformed(evt);
            }
        });

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });

        rateSlider.setMajorTickSpacing(50);
        rateSlider.setMaximum(500);
        rateSlider.setPaintLabels(true);
        rateSlider.setPaintTicks(true);
        rateSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rateSliderStateChanged(evt);
            }
        });

        rateLabel.setText("# events / dt [µHz]");

        javax.swing.GroupLayout ratePanelLayout = new javax.swing.GroupLayout(ratePanel);
        ratePanel.setLayout(ratePanelLayout);
        ratePanelLayout.setHorizontalGroup(
            ratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ratePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(rateSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                    .addComponent(rateLabel))
                .addContainerGap())
        );
        ratePanelLayout.setVerticalGroup(
            ratePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ratePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rateSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rateLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Eventrate", ratePanel);

        xLabel.setText("X:");

        yLabel.setText("Y:");

        javax.swing.GroupLayout posPanelLayout = new javax.swing.GroupLayout(posPanel);
        posPanel.setLayout(posPanelLayout);
        posPanelLayout.setHorizontalGroup(
            posPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(posPanelLayout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addComponent(xLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(xSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 103, Short.MAX_VALUE)
                .addComponent(yLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45))
        );
        posPanelLayout.setVerticalGroup(
            posPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(posPanelLayout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addGroup(posPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(xSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xLabel)
                    .addComponent(yLabel)
                    .addComponent(ySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(39, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Position", posPanel);

        speedSlider.setMajorTickSpacing(2);
        speedSlider.setMaximum(20);
        speedSlider.setPaintLabels(true);
        speedSlider.setPaintTicks(true);
        speedSlider.setValue(4);
        speedSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                speedSliderStateChanged(evt);
            }
        });

        speedLabel.setText("(distance [px] / dt [µs]) * e-4 ");

        javax.swing.GroupLayout speedPaneLayout = new javax.swing.GroupLayout(speedPane);
        speedPane.setLayout(speedPaneLayout);
        speedPaneLayout.setHorizontalGroup(
            speedPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(speedPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(speedPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(speedSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                    .addComponent(speedLabel))
                .addContainerGap())
        );
        speedPaneLayout.setVerticalGroup(
            speedPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(speedPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(speedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(speedLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Speed", speedPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(enableCheckBox)
                    .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(visualizeLabel))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton2)
                    .addComponent(jRadioButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(drawCheckBox)
                .addGap(51, 51, 51))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(enableCheckBox)
                .addGap(18, 18, 18)
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(visualizeLabel)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jRadioButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton2))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(drawCheckBox)))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public synchronized void attachFilterStateListener(PropertyChangeSupport s) {
        s.addPropertyChangeListener(filterStateListener);
        pcsl.add(s);
        if (!listening) {
            listening = true;
        }
    }
    
    private void enableCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableCheckBoxActionPerformed
        boolean selected = enableCheckBox.isSelected();
        visualizeLabel.setEnabled(selected);
        drawCheckBox.setEnabled(selected);
        reval.arm(selected);
    }//GEN-LAST:event_enableCheckBoxActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void drawCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drawCheckBoxActionPerformed
        boolean selected = drawCheckBox.isSelected();
        reval.draw(selected);
        // jRadioButton1.setEnabled(selected);
        // jRadioButton2.setEnabled(selected);
    }//GEN-LAST:event_drawCheckBoxActionPerformed
    
    public boolean isListening() {
        return listening;
    }
    
    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
        switch (tabbedPane.getTitleAt(tabbedPane.getSelectedIndex())) {
            case "Eventrate":
            thresh = new EvaluatorThreshold(EvaluatorThreshold.Parameter.EVENTRATE, (double) rateSlider.getValue());
            reval.setThreshold(thresh);
            break;
            case "Position":
            try {
                xSpinner.commitEdit();
                ySpinner.commitEdit();
            } catch ( java.text.ParseException e ) {  }
            int x = (Integer) xSpinner.getValue();
            int y = (Integer) ySpinner.getValue();
            break;
            case "Speed":
            thresh = new EvaluatorThreshold(EvaluatorThreshold.Parameter.SPEED, (double) speedSlider.getValue() * 1e-4);
            reval.setThreshold(thresh);
            break;
            default:
            break;
        }
    }//GEN-LAST:event_tabbedPaneStateChanged

    private void speedSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_speedSliderStateChanged
        thresh.setValue((double) speedSlider.getValue() * 1e-4);
    }//GEN-LAST:event_speedSliderStateChanged
    
    public synchronized void removeFilterStateListener(PropertyChangeSupport pcs) {
        pcs.removePropertyChangeListener(filterStateListener);
        listening = false;
    }
    
    private void rateSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rateSliderStateChanged
        thresh.setValue((double) rateSlider.getValue());
    }//GEN-LAST:event_rateSliderStateChanged

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(EvaluatorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(EvaluatorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(EvaluatorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(EvaluatorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new EvaluatorFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox drawCheckBox;
    private javax.swing.JCheckBox enableCheckBox;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JPanel posPanel;
    private javax.swing.JLabel rateLabel;
    private javax.swing.JPanel ratePanel;
    private javax.swing.JSlider rateSlider;
    private javax.swing.JLabel speedLabel;
    private javax.swing.JPanel speedPane;
    private javax.swing.JSlider speedSlider;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel visualizeLabel;
    private javax.swing.JLabel xLabel;
    private javax.swing.JSpinner xSpinner;
    private javax.swing.JLabel yLabel;
    private javax.swing.JSpinner ySpinner;
    // End of variables declaration//GEN-END:variables
}
