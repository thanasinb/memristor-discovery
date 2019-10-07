/**
 * Memristor-Discovery is distributed under the GNU General Public License version 3 and is also
 * available under alternative licenses negotiated directly with Knowm, Inc.
 *
 * <p>Copyright (c) 2016-2019 Knowm Inc. www.knowm.org
 *
 * <p>This package also includes various components that are not part of Memristor-Discovery itself:
 *
 * <p>* `Multibit`: Copyright 2011 multibit.org, MIT License * `SteelCheckBox`: Copyright 2012
 * Gerrit, BSD license
 *
 * <p>Knowm, Inc. holds copyright and/or sufficient licenses to all components of the
 * Memristor-Discovery package, and therefore can grant, at its sole discretion, the ability for
 * companies, individuals, or organizations to create proprietary or open source (even if not GPL)
 * modules which may be dynamically linked at runtime with the portions of Memristor-Discovery which
 * fall under our copyright/license umbrella, or are distributed under more flexible licenses than
 * GPL.
 *
 * <p>The 'Knowm' name and logos are trademarks owned by Knowm, Inc.
 *
 * <p>If you have any questions regarding our licensing policy, please contact us at
 * `contact@knowm.org`.
 */
package org.knowm.memristor.discovery.gui.mvc.experiments.program.control;

import org.knowm.memristor.discovery.gui.mvc.experiments.ControlView;
import org.knowm.memristor.discovery.gui.mvc.experiments.ExperimentPreferences.Waveform;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Hashtable;

/**
 * Provides controls for running the control
 *
 * @author timmolter
 */
public class ControlPanel extends ControlView {

  private NumberFormat formatter = new DecimalFormat("0.###E0");

  // private final JLabel appliedAmplitudeLabel;
  // private final JLabel currentLabel;
  private final JLabel energyLabel;
  // private final JLabel energyMemRistorOnlyLabel;
  // private final JCheckBox memristorVoltageCheckBox;
  private final JSlider amplitudeSlider, reverseAmplitudeSlider;
  private final JSlider pulseWidthSlider, reversePulseWidthSlider;
  private final JSlider pulseWidthSliderNs, reversePulseWidthSliderNs;
  private final JSlider pulseNumberSlider, reversePulseNumberSlider;

//  private final JSlider reversePulseWidthSlider;
//  private final JSlider reversePulseWidthSliderNs;
//  private final JSlider reversePulseNumberSlider;

  private final JSlider dutyCycleSlider, reverseDutyCycleSlider;

  private final JLabel seriesLabel;
  private final JTextField seriesTextField;
  private final JLabel sampleRateLabel;
  private final JTextField sampleRateTextField;
  private final JLabel targetLabel;
  private final JTextField targetTextField;
  private JComboBox<Waveform> waveformComboBox;
  private final JLabel debugLabel, debugMsgLabel, debugMsgLabel2;

  public final JButton startStopButton;

  /** Constructor */
  public ControlPanel() {

    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

    c.gridx = 0;

    //    appliedAmplitudeLabel = new JLabel("Applied Amplitude [V]: ");
    //    appliedAmplitudeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    //    c.gridy++;
    //    c.insets = new Insets(0, 10, 4, 0);
    //    add(appliedAmplitudeLabel, c);

    //    currentLabel = new JLabel("Current [" + PulsePreferences.CURRENT_UNIT.getLabel() + "]: ");
    //    currentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    //    c.gridy++;
    //    c.insets = new Insets(0, 10, 4, 0);
    //    add(currentLabel, c);

    energyLabel = new JLabel("Energy M+R (J): ");
    energyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    c.gridy++;
    c.insets = new Insets(0, 10, 4, 0);
    add(energyLabel, c);

    //    energyMemRistorOnlyLabel = new JLabel("Energy M [nJ]: ");
    //    energyMemRistorOnlyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    //    c.gridy++;
    //    c.insets = new Insets(0, 10, 4, 0);
    //    add(energyMemRistorOnlyLabel, c);

    this.waveformComboBox = new JComboBox<>();
    waveformComboBox.setFocusable(false);
    c.gridy++;
    c.insets = new Insets(0, 0, 4, 6);
    add(waveformComboBox, c);

    //    c.gridy++;
    //    c.insets = new Insets(0, 0, 0, 0);
    //    memristorVoltageCheckBox = new JCheckBox("Memristor Voltage Drop");
    //    add(memristorVoltageCheckBox, c);

    //  int amplitude=
    // experimentPreferences.getInteger(PulsePreferences.MAX_SLIDER_VOLTAGE_INIT_KEY,
    // PulsePreferences.MAX_SLIDER_VOLTAGE_INIT_DEFAULT_VALUE)

    amplitudeSlider = new JSlider(JSlider.HORIZONTAL, -300, 300, 0);
    amplitudeSlider.setBorder(BorderFactory.createTitledBorder("Forward Amplitude [V]"));
    amplitudeSlider.setMajorTickSpacing(100);
    amplitudeSlider.setMinorTickSpacing(5);
    amplitudeSlider.setPaintTicks(true);
    amplitudeSlider.setPaintLabels(true);
    amplitudeSlider.setSnapToTicks(true);
    Hashtable<Integer, JLabel> labelTable = new Hashtable<>();

    //    labelTable.put(-500, new JLabel("-5"));
    //    labelTable.put(-400, new JLabel("-4"));
    labelTable.put(-300, new JLabel("-3"));
    labelTable.put(-200, new JLabel("-2"));
    labelTable.put(-100, new JLabel("-1"));
    labelTable.put(0, new JLabel("0"));
    labelTable.put(100, new JLabel("1"));
    labelTable.put(200, new JLabel("2"));
    labelTable.put(300, new JLabel("3"));
    //    labelTable.put(400, new JLabel("4"));
    //    labelTable.put(500, new JLabel("5"));
    amplitudeSlider.setLabelTable(labelTable);

    c.gridy++;
    c.insets = new Insets(0, 6, 4, 6);
    amplitudeSlider.setPreferredSize(new Dimension(300, 80));
    add(amplitudeSlider, c);

    reverseAmplitudeSlider = new JSlider(JSlider.HORIZONTAL, -300, 300, 0);
    reverseAmplitudeSlider.setBorder(BorderFactory.createTitledBorder("Reverse Amplitude [V]"));
    reverseAmplitudeSlider.setMajorTickSpacing(100);
    reverseAmplitudeSlider.setMinorTickSpacing(5);
    reverseAmplitudeSlider.setPaintTicks(true);
    reverseAmplitudeSlider.setPaintLabels(true);
    reverseAmplitudeSlider.setSnapToTicks(true);
    Hashtable<Integer, JLabel> reverseLabelTable = new Hashtable<>();

    //    labelTable.put(-500, new JLabel("-5"));
    //    labelTable.put(-400, new JLabel("-4"));
    labelTable.put(-300, new JLabel("-3"));
    labelTable.put(-200, new JLabel("-2"));
    labelTable.put(-100, new JLabel("-1"));
    labelTable.put(0, new JLabel("0"));
    labelTable.put(100, new JLabel("1"));
    labelTable.put(200, new JLabel("2"));
    labelTable.put(300, new JLabel("3"));
    //    labelTable.put(400, new JLabel("4"));
    //    labelTable.put(500, new JLabel("5"));
    reverseAmplitudeSlider.setLabelTable(labelTable);

    c.gridx++;
    c.insets = new Insets(0, 6, 4, 6);
    reverseAmplitudeSlider.setPreferredSize(new Dimension(300, 80));
    add(reverseAmplitudeSlider, c);

    pulseWidthSlider = new JSlider(JSlider.HORIZONTAL, 5000, 100000, 5000);
    pulseWidthSlider.setBorder(BorderFactory.createTitledBorder("Forward Pulse Width [µs]"));
    pulseWidthSlider.setMinorTickSpacing(5000);
    pulseWidthSlider.setPaintTicks(true);
    pulseWidthSlider.setPaintLabels(true);
    pulseWidthSlider.setSnapToTicks(true);
    labelTable = new Hashtable<>();
    labelTable.put(5000, new JLabel("5"));
    labelTable.put(50000, new JLabel("50"));
    labelTable.put(100000, new JLabel("100"));
    pulseWidthSlider.setLabelTable(labelTable);
    c.gridx = 0;
    c.gridy++;
    add(pulseWidthSlider, c);

    reversePulseWidthSlider = new JSlider(JSlider.HORIZONTAL, 5000, 100000, 5000);
    reversePulseWidthSlider.setBorder(BorderFactory.createTitledBorder("Reverse Pulse Width [µs]"));
    reversePulseWidthSlider.setMinorTickSpacing(5000);
    reversePulseWidthSlider.setPaintTicks(true);
    reversePulseWidthSlider.setPaintLabels(true);
    reversePulseWidthSlider.setSnapToTicks(true);
    labelTable = new Hashtable<>();
    labelTable.put(5000, new JLabel("5"));
    labelTable.put(50000, new JLabel("50"));
    labelTable.put(100000, new JLabel("100"));
    reversePulseWidthSlider.setLabelTable(labelTable);
    c.gridx++;
    add(reversePulseWidthSlider, c);

    pulseWidthSliderNs = new JSlider(JSlider.HORIZONTAL, 100, 5000, 5000);
    pulseWidthSliderNs.setBorder(BorderFactory.createTitledBorder("Forward Pulse Width [µs]"));
    pulseWidthSliderNs.setMinorTickSpacing(100);
    pulseWidthSliderNs.setPaintTicks(true);
    pulseWidthSliderNs.setPaintLabels(true);
    pulseWidthSliderNs.setSnapToTicks(true);
    labelTable = new Hashtable<>();
    labelTable.put(100, new JLabel(".1"));
    labelTable.put(500, new JLabel(".5"));
    labelTable.put(1000, new JLabel("1"));
    labelTable.put(2000, new JLabel("2"));
    labelTable.put(3000, new JLabel("3"));
    labelTable.put(4000, new JLabel("4"));
    labelTable.put(5000, new JLabel("5"));
    pulseWidthSliderNs.setLabelTable(labelTable);
    c.gridx = 0;
    c.gridy++;
    add(pulseWidthSliderNs, c);

    reversePulseWidthSliderNs = new JSlider(JSlider.HORIZONTAL, 100, 5000, 5000);
    reversePulseWidthSliderNs.setBorder(BorderFactory.createTitledBorder("Reverse Pulse Width [µs]"));
    reversePulseWidthSliderNs.setMinorTickSpacing(100);
    reversePulseWidthSliderNs.setPaintTicks(true);
    reversePulseWidthSliderNs.setPaintLabels(true);
    reversePulseWidthSliderNs.setSnapToTicks(true);
    labelTable = new Hashtable<>();
    labelTable.put(100, new JLabel(".1"));
    labelTable.put(500, new JLabel(".5"));
    labelTable.put(1000, new JLabel("1"));
    labelTable.put(2000, new JLabel("2"));
    labelTable.put(3000, new JLabel("3"));
    labelTable.put(4000, new JLabel("4"));
    labelTable.put(5000, new JLabel("5"));
    reversePulseWidthSliderNs.setLabelTable(labelTable);
    c.gridx++;
    add(reversePulseWidthSliderNs, c);

    pulseNumberSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 1);
    pulseNumberSlider.setBorder(BorderFactory.createTitledBorder("Forward Pulse Number"));
    pulseNumberSlider.setMajorTickSpacing(25);
    pulseNumberSlider.setMinorTickSpacing(1);
    pulseNumberSlider.setPaintTicks(true);
    pulseNumberSlider.setPaintLabels(true);
    pulseNumberSlider.setSnapToTicks(true);
    labelTable = new Hashtable<>();
    labelTable.put(1, new JLabel("1"));
    labelTable.put(25, new JLabel("25"));
    labelTable.put(50, new JLabel("50"));
    labelTable.put(75, new JLabel("75"));
    labelTable.put(100, new JLabel("100"));
    pulseNumberSlider.setLabelTable(labelTable);
    c.gridx = 0;
    c.gridy++;
    add(pulseNumberSlider, c);

    reversePulseNumberSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 1);
    reversePulseNumberSlider.setBorder(BorderFactory.createTitledBorder("Reverse Pulse Number"));
    reversePulseNumberSlider.setMajorTickSpacing(25);
    reversePulseNumberSlider.setMinorTickSpacing(1);
    reversePulseNumberSlider.setPaintTicks(true);
    reversePulseNumberSlider.setPaintLabels(true);
    reversePulseNumberSlider.setSnapToTicks(true);
    labelTable = new Hashtable<>();
    labelTable.put(1, new JLabel("1"));
    labelTable.put(25, new JLabel("25"));
    labelTable.put(50, new JLabel("50"));
    labelTable.put(75, new JLabel("75"));
    labelTable.put(100, new JLabel("100"));
    reversePulseNumberSlider.setLabelTable(labelTable);
    c.gridx++;
    add(reversePulseNumberSlider, c);

    dutyCycleSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 1);
    dutyCycleSlider.setBorder(BorderFactory.createTitledBorder("Forward Duty Cycle"));
    dutyCycleSlider.setMajorTickSpacing(25);
    dutyCycleSlider.setMinorTickSpacing(1);
    dutyCycleSlider.setPaintTicks(true);
    dutyCycleSlider.setPaintLabels(true);
    dutyCycleSlider.setSnapToTicks(true);
    labelTable = new Hashtable<>();
    labelTable.put(0, new JLabel("0"));
    labelTable.put(25, new JLabel(".25"));
    labelTable.put(50, new JLabel(".5"));
    labelTable.put(75, new JLabel(".75"));
    labelTable.put(100, new JLabel("1"));
    dutyCycleSlider.setLabelTable(labelTable);
    c.gridx = 0;
    c.gridy++;
    add(dutyCycleSlider, c);

    reverseDutyCycleSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 1);
    reverseDutyCycleSlider.setBorder(BorderFactory.createTitledBorder("Reverse Duty Cycle"));
    reverseDutyCycleSlider.setMajorTickSpacing(25);
    reverseDutyCycleSlider.setMinorTickSpacing(1);
    reverseDutyCycleSlider.setPaintTicks(true);
    reverseDutyCycleSlider.setPaintLabels(true);
    reverseDutyCycleSlider.setSnapToTicks(true);
    labelTable = new Hashtable<>();
    labelTable.put(0, new JLabel("0"));
    labelTable.put(25, new JLabel(".25"));
    labelTable.put(50, new JLabel(".5"));
    labelTable.put(75, new JLabel(".75"));
    labelTable.put(100, new JLabel("1"));
    reverseDutyCycleSlider.setLabelTable(labelTable);
    c.gridx++;
    add(reverseDutyCycleSlider, c);

    seriesLabel = new JLabel("Series R [Ohm]");
    seriesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    c.gridx = 0;
    c.gridy++;
    c.insets = new Insets(0, 10, 4, 0);
    add(seriesLabel, c);

    targetLabel = new JLabel("Target R [Ohm]");
    targetLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    c.gridx++;
    c.insets = new Insets(0, 10, 4, 0);
    add(targetLabel, c);

    seriesTextField = new JTextField();
    seriesTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
    c.gridx = 0;
    c.gridy++;
    c.insets = new Insets(0, 5, 14, 5);
    add(seriesTextField, c);

    targetTextField = new JTextField();
    targetTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
    c.gridx++;
    c.insets = new Insets(0, 5, 14, 5);
    add(targetTextField, c);

    sampleRateLabel = new JLabel("Sample Period [s]");
    sampleRateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    c.gridx = 0;
    c.gridy++;
    c.insets = new Insets(0, 10, 4, 0);
    add(sampleRateLabel, c);

    debugLabel = new JLabel("Debug Message");
    sampleRateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    c.gridx++;
    c.insets = new Insets(0, 10, 4, 0);
    add(debugLabel, c);

    sampleRateTextField = new JTextField();
    sampleRateTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
    c.gridx = 0;
    c.gridy++;
    c.insets = new Insets(0, 5, 14, 5);
    add(sampleRateTextField, c);

    debugMsgLabel = new JLabel("XXX");
    debugMsgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    c.gridx++;
    c.insets = new Insets(0, 10, 4, 0);
    add(debugMsgLabel, c);

    startStopButton = new JButton("Start");
    startStopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    c.gridx = 0;
    c.gridy++;
    c.insets = new Insets(0, 0, 0, 0);
    add(startStopButton, c);

    debugMsgLabel2 = new JLabel("XXX");
    debugMsgLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
    c.gridx++;
    c.insets = new Insets(0, 10, 4, 0);
    add(debugMsgLabel2, c);

    c.gridx = 0;
    c.gridy++;
    add(logoLabel, c);
    c.gridx++;
    add(us_logo, c);
  }

  public void enableAllChildComponents(boolean enabled) {

    waveformComboBox.setEnabled(enabled);
    // memristorVoltageCheckBox.setEnabled(enabled);
    amplitudeSlider.setEnabled(enabled);
    pulseWidthSlider.setEnabled(enabled);
    pulseWidthSliderNs.setEnabled(enabled);
    pulseNumberSlider.setEnabled(enabled);
    dutyCycleSlider.setEnabled(enabled);
    reverseAmplitudeSlider.setEnabled(enabled);
    reversePulseWidthSlider.setEnabled(enabled);
    reversePulseWidthSliderNs.setEnabled(enabled);
    reversePulseNumberSlider.setEnabled(enabled);
    reverseDutyCycleSlider.setEnabled(enabled);
    seriesTextField.setEnabled(enabled);
    sampleRateTextField.setEnabled(enabled);
    targetTextField.setEnabled(enabled);
    startStopButton.setEnabled(enabled);
  }

  public void updateEnergyGUI(
      double appliedAmplitude, double appliedCurrent, double appliedEnergy) {

    // appliedAmplitudeLabel.setText("Applied Amplitude [V]: " + Util.round(appliedAmplitude, 4));
    // currentLabel.setText("Current [" + PulsePreferences.CURRENT_UNIT.getLabel() + "]: " +
    // Util.round(appliedCurrent, 3));
    energyLabel.setText("Energy M+R (J): " + formatter.format(appliedEnergy));
  }

  public void updateDebugMsg(String debugMsg){
    debugMsgLabel.setText(debugMsg);
  }

  public void updateDebugMsg2(String debugMsg2){
    debugMsgLabel2.setText(debugMsg2);
  }

  public JComboBox<Waveform> getWaveformComboBox() {

    return waveformComboBox;
  }

  public JSlider getAmplitudeSlider() {

    return amplitudeSlider;
  }

  public JSlider getPulseWidthSlider() {

    return pulseWidthSlider;
  }

  public JSlider getPulseWidthSliderNs() {

    return pulseWidthSliderNs;
  }

  public JSlider getPulseNumberSlider() {

    return pulseNumberSlider;
  }

  public JSlider getDutyCycleSlider() {

    return dutyCycleSlider;
  }

  public JSlider getReverseAmplitudeSlider() {

    return reverseAmplitudeSlider;
  }

  public JSlider getReversePulseWidthSlider() {

    return reversePulseWidthSlider;
  }

  public JSlider getReversePulseWidthSliderNs() {

    return reversePulseWidthSliderNs;
  }

  public JSlider getReversePulseNumberSlider() {

    return reversePulseNumberSlider;
  }

  public JSlider getReverseDutyCycleSlider() {

    return reverseDutyCycleSlider;
  }

  public JTextField getSeriesTextField() {

    return seriesTextField;
  }

  public JTextField getSampleRateTextField() {
    return sampleRateTextField;
  }

  //  public JCheckBox getMemristorVoltageCheckBox() {
  //
  //    return memristorVoltageCheckBox;
  //  }

  public JTextField getTargetTextField() {

    return targetTextField;
  }

  public JButton getStartStopButton() {
    return startStopButton;
  }
}
