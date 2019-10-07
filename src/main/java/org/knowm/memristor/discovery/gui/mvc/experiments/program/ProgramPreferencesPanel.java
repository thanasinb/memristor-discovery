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
package org.knowm.memristor.discovery.gui.mvc.experiments.program;

import org.knowm.memristor.discovery.gui.mvc.experiments.ExperimentPreferences;
import org.knowm.memristor.discovery.gui.mvc.experiments.ExperimentPreferencesPanel;

import javax.swing.*;
import java.awt.*;

public class ProgramPreferencesPanel extends ExperimentPreferencesPanel {

  private JLabel seriesResistorLabel;
  private JTextField seriesResistorTextField;

  private JLabel targetResistorLabel;
  private JTextField targetResistorTextField;

  private JLabel amplitudeLabel, reverseAmplitudeLabel;
  private JTextField amplitudeTextField, reverseAmplitudeTextField;

  private JLabel pulseWidthLabel, reversePulseWidthLabel;
  private JTextField pulseWidthTextField, reversePulseWidthTextField;

  private JLabel sampleRateLabel;
  private JTextField sampleRateTextField;

  private JLabel numPulsesLabel, reverseNumPulsesLabel;
  private JTextField numPulsesTextField, reverseNumPulsesTextField;

  public ProgramPreferencesPanel(JFrame owner, String experimentName) {

    super(owner, experimentName);
  }

  @Override
  public void doCreateAndShowGUI(JPanel preferencesPanel) {

    GridBagConstraints gc = new GridBagConstraints();
    gc.fill = GridBagConstraints.HORIZONTAL;
    gc.insets = new Insets(10, 10, 10, 10);

    gc.gridy = 0;
    gc.gridx = 0;
    this.seriesResistorLabel = new JLabel("Series Resistor [Ohm]:");
    preferencesPanel.add(seriesResistorLabel, gc);

    gc.gridx = 1;
    this.seriesResistorTextField = new JTextField(12);
    this.seriesResistorTextField.setText(
        String.valueOf(
            experimentPreferences.getInteger(
                ProgramPreferences.SERIES_R_INIT_KEY, ProgramPreferences.SERIES_R_INIT_DEFAULT_VALUE)));
    preferencesPanel.add(seriesResistorTextField, gc);

    gc.gridx++;
    this.targetResistorLabel = new JLabel("Target Resistance [Ohm]:");
    preferencesPanel.add(targetResistorLabel, gc);

    gc.gridx++;
    this.targetResistorTextField = new JTextField(12);
    this.targetResistorTextField.setText(
            String.valueOf(
                    experimentPreferences.getInteger(
                            ProgramPreferences.TARGET_R_INIT_KEY, ProgramPreferences.TARGET_R_INIT_DEFAULT_VALUE)));
    preferencesPanel.add(targetResistorTextField, gc);

    gc.gridy++;
    gc.gridx = 0;
    this.amplitudeLabel = new JLabel("Forward Amplitude [V]:");
    preferencesPanel.add(amplitudeLabel, gc);

    gc.gridx = 1;
    this.amplitudeTextField = new JTextField(12);
    this.amplitudeTextField.setText(
        String.valueOf(
            experimentPreferences.getFloat(
                ProgramPreferences.AMPLITUDE_INIT_FLOAT_KEY,
                ProgramPreferences.AMPLITUDE_INIT_FLOAT_DEFAULT_VALUE)));
    preferencesPanel.add(amplitudeTextField, gc);

    gc.gridx++;
    this.reverseAmplitudeLabel = new JLabel("Reverse Amplitude [V]:");
    preferencesPanel.add(reverseAmplitudeLabel, gc);

    gc.gridx++;
    this.reverseAmplitudeTextField = new JTextField(12);
    this.reverseAmplitudeTextField.setText(
            String.valueOf(
                    experimentPreferences.getFloat(
                            ProgramPreferences.REVERSE_AMPLITUDE_INIT_FLOAT_KEY,
                            ProgramPreferences.REVERSE_AMPLITUDE_INIT_FLOAT_DEFAULT_VALUE)));
    preferencesPanel.add(reverseAmplitudeTextField, gc);

    gc.gridy++;
    gc.gridx = 0;
    this.pulseWidthLabel = new JLabel("Forward Pulse Width [ns]:");
    preferencesPanel.add(pulseWidthLabel, gc);

    gc.gridx = 1;
    this.pulseWidthTextField = new JTextField(12);
    this.pulseWidthTextField.setText(
        String.valueOf(
            experimentPreferences.getInteger(
                ProgramPreferences.PULSE_WIDTH_INIT_KEY,
                ProgramPreferences.PULSE_WIDTH_INIT_DEFAULT_VALUE)));
    preferencesPanel.add(pulseWidthTextField, gc);

    gc.gridx++;
    this.reversePulseWidthLabel = new JLabel("Reverse Pulse Width [ns]:");
    preferencesPanel.add(reversePulseWidthLabel, gc);

    gc.gridx++;
    this.reversePulseWidthTextField = new JTextField(12);
    this.reversePulseWidthTextField.setText(
            String.valueOf(
                    experimentPreferences.getInteger(
                            ProgramPreferences.REVERSE_PULSE_WIDTH_INIT_KEY,
                            ProgramPreferences.REVERSE_PULSE_WIDTH_INIT_DEFAULT_VALUE)));
    preferencesPanel.add(reversePulseWidthTextField, gc);

    gc.gridy++;
    gc.gridx = 0;
    this.numPulsesLabel = new JLabel("Forward Number Pulses:");
    preferencesPanel.add(numPulsesLabel, gc);

    gc.gridx = 1;
    this.numPulsesTextField = new JTextField(12);
    this.numPulsesTextField.setText(
        String.valueOf(
            experimentPreferences.getInteger(
                ProgramPreferences.NUM_PULSES_INIT_KEY,
                ProgramPreferences.NUM_PULSES_INIT_DEFAULT_VALUE)));
    preferencesPanel.add(numPulsesTextField, gc);

    gc.gridx++;
    this.reverseNumPulsesLabel = new JLabel("Reverse Number Pulses:");
    preferencesPanel.add(reverseNumPulsesLabel, gc);

    gc.gridx++;
    this.reverseNumPulsesTextField = new JTextField(12);
    this.reverseNumPulsesTextField.setText(
            String.valueOf(
                    experimentPreferences.getInteger(
                            ProgramPreferences.REVERSE_NUM_PULSES_INIT_KEY,
                            ProgramPreferences.REVERSE_NUM_PULSES_INIT_DEFAULT_VALUE)));
    preferencesPanel.add(reverseNumPulsesTextField, gc);

    gc.gridy++;
    gc.gridx = 0;
    this.sampleRateLabel = new JLabel("Sample Rate [s]:");
    preferencesPanel.add(sampleRateLabel, gc);

    gc.gridx = 1;
    this.sampleRateTextField = new JTextField(12);
    this.sampleRateTextField.setText(
            String.valueOf(
                    experimentPreferences.getInteger(
                            ProgramPreferences.SAMPLE_RATE_INIT_KEY,
                            ProgramPreferences.SAMPLE_RATE_INIT_DEFAULT_VALUE)));
    preferencesPanel.add(sampleRateTextField, gc);

  }

  @Override
  public void doSavePreferences() {

    experimentPreferences.setInteger(
        ProgramPreferences.NUM_PULSES_INIT_KEY, Integer.parseInt(numPulsesTextField.getText()));
    experimentPreferences.setInteger(
        ProgramPreferences.SERIES_R_INIT_KEY, Integer.parseInt(seriesResistorTextField.getText()));
    experimentPreferences.setInteger(
        ProgramPreferences.TARGET_R_INIT_KEY, Integer.parseInt(targetResistorTextField.getText()));
    experimentPreferences.setFloat(
        ProgramPreferences.AMPLITUDE_INIT_FLOAT_KEY, Float.parseFloat(amplitudeTextField.getText()));
    experimentPreferences.setInteger(
        ProgramPreferences.PULSE_WIDTH_INIT_KEY, Integer.parseInt(pulseWidthTextField.getText()));
    experimentPreferences.setInteger(
        ProgramPreferences.SAMPLE_RATE_INIT_KEY, Integer.parseInt(sampleRateTextField.getText()));
    experimentPreferences.setFloat(
            ProgramPreferences.REVERSE_AMPLITUDE_INIT_FLOAT_KEY, Float.parseFloat(amplitudeTextField.getText()));
    experimentPreferences.setInteger(
            ProgramPreferences.REVERSE_PULSE_WIDTH_INIT_KEY, Integer.parseInt(pulseWidthTextField.getText()));
    experimentPreferences.setInteger(
            ProgramPreferences.REVERSE_NUM_PULSES_INIT_KEY, Integer.parseInt(numPulsesTextField.getText()));
  }

  @Override
  public ExperimentPreferences initAppPreferences() {

    return new ProgramPreferences();
  }
}
