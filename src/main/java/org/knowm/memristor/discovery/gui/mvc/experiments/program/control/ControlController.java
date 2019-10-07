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

import org.knowm.memristor.discovery.DWFProxy;
import org.knowm.memristor.discovery.gui.mvc.experiments.Controller;
import org.knowm.memristor.discovery.gui.mvc.experiments.ExperimentPreferences.Waveform;
import org.knowm.memristor.discovery.gui.mvc.experiments.Model;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;

public class ControlController extends Controller {

  private final ControlPanel controlPanel;
  private final ControlModel controlModel;

  /**
   * Constructor
   *
   * @param controlPanel
   * @param controlModel
   * @param dwf
   */
  public ControlController(ControlPanel controlPanel, ControlModel controlModel, DWFProxy dwf) {

    super(controlPanel, controlModel);

    this.controlPanel = controlPanel;
    this.controlModel = controlModel;
    dwf.addListener(this);

    initGUIComponents();
    setUpViewEvents();

    // register the controller as the listener of the controlModel
    controlModel.addListener(this);
  }

  private void initGUIComponents() {

    initGUIComponentsFromModel();
  }

  private void initGUIComponentsFromModel() {

    controlPanel.getWaveformComboBox().setSelectedItem(controlModel.getWaveform());
    controlPanel
        .getWaveformComboBox()
        .setModel(
            new DefaultComboBoxModel<>(
                new Waveform[] {
                  Waveform.Square,
                  Waveform.SquareSmooth,
                  Waveform.SquareDecay,
                  Waveform.SquareLongDecay,
                  Waveform.Triangle,
                  Waveform.QuarterSine,
                  Waveform.HalfSine
                }));
    controlPanel.getReadPulseAmplitudeSlider().setValue((int) (controlModel.getReadPulseAmplitude()));
    controlPanel
          .getReadPulseAmplitudeSlider()
          .setBorder(
                  BorderFactory.createTitledBorder("Read Pulse Amplitude [V] = " + controlModel.getReadPulseAmplitude()));
    controlPanel.getReadPulseAmplitudeSlider().setValue((int) (100 * controlModel.getAmplitude()));
    controlPanel
        .getAmplitudeSlider()
        .setBorder(
            BorderFactory.createTitledBorder("Forward Amplitude [V] = " + controlModel.getAmplitude()));
    if (controlModel.getPulseWidth() >= 5000) {
      controlPanel.getPulseWidthSlider().setValue((int) (controlModel.getPulseWidth()));
      controlPanel.getPulseWidthSliderNs().setValue(0);
      controlPanel
          .getPulseWidthSlider()
          .setBorder(
              BorderFactory.createTitledBorder(
                  "Forward Pulse Width [µs] = " + controlModel.getPulseWidth() / 1000));
      controlPanel
          .getPulseWidthSliderNs()
          .setBorder(BorderFactory.createTitledBorder("Forward Pulse Width [µs]"));
    } else {
      controlPanel.getPulseWidthSlider().setValue(0);
      controlPanel.getPulseWidthSliderNs().setValue(controlModel.getPulseWidth());
      controlPanel
          .getPulseWidthSlider()
          .setBorder(BorderFactory.createTitledBorder("Forward Pulse Width [µs]"));
      controlPanel
          .getPulseWidthSliderNs()
          .setBorder(
              BorderFactory.createTitledBorder(
                  "Forward Pulse Width [µs] = " + controlModel.getPulseWidth() / 1000));
    }
    controlPanel
        .getPulseNumberSlider()
        .setBorder(
            BorderFactory.createTitledBorder("Forward Pulse Number = " + controlModel.getPulseNumber()));
    controlPanel.getPulseNumberSlider().setValue(controlModel.getPulseNumber());

    controlPanel.getReverseAmplitudeSlider().setValue((int) (controlModel.getReverseAmplitude() * 100));
    controlPanel
            .getReverseAmplitudeSlider()
            .setBorder(
                    BorderFactory.createTitledBorder("Reverse Amplitude [V] = " + controlModel.getReverseAmplitude()));

      if (controlModel.getReversePulseWidth() >= 5000) {
          controlPanel.getReversePulseWidthSlider().setValue((int) (controlModel.getReversePulseWidth()));
          controlPanel.getReversePulseWidthSliderNs().setValue(0);
          controlPanel
                  .getReversePulseWidthSlider()
                  .setBorder(
                          BorderFactory.createTitledBorder(
                                  "Reverse Pulse Width [µs] = " + controlModel.getReversePulseWidth() / 1000));
          controlPanel
                  .getReversePulseWidthSliderNs()
                  .setBorder(BorderFactory.createTitledBorder("Reverse Pulse Width [µs]"));
      } else {
          controlPanel.getReversePulseWidthSlider().setValue(0);
          controlPanel.getReversePulseWidthSliderNs().setValue(controlModel.getReversePulseWidth());
          controlPanel
                  .getReversePulseWidthSlider()
                  .setBorder(BorderFactory.createTitledBorder("Reverse Pulse Width [µs]"));
          controlPanel
                  .getReversePulseWidthSliderNs()
                  .setBorder(
                          BorderFactory.createTitledBorder(
                                  "Reverse Pulse Width [µs] = " + controlModel.getReversePulseWidth() / 1000));
      }
      controlPanel
              .getReversePulseNumberSlider()
              .setBorder(
                      BorderFactory.createTitledBorder("Reverse Pulse Number = " + controlModel.getReversePulseNumber()));
      controlPanel.getReversePulseNumberSlider().setValue(controlModel.getReversePulseNumber());

    controlPanel
        .getDutyCycleSlider()
        .setBorder(BorderFactory.createTitledBorder("Forward Duty Cycle = " + controlModel.getDutyCycle()));
    controlPanel.getDutyCycleSlider().setValue((int) (100 * controlModel.getDutyCycle()));

      controlPanel
              .getReverseDutyCycleSlider()
              .setBorder(BorderFactory.createTitledBorder("Reverse Duty Cycle = " + controlModel.getReverseDutyCycle()));
      controlPanel.getReverseDutyCycleSlider().setValue((int) (100 * controlModel.getReverseDutyCycle()));

    controlPanel.getSeriesTextField().setText("" + controlModel.getSeriesResistance());
    controlPanel.getTargetTextField().setText("" + controlModel.getTargetResistance());
    controlPanel.getSampleRateTextField().setText("" + controlModel.getSampleRate());
  }

  /** Here, all the action listeners are attached to the GUI components */
  public void doSetUpViewEvents() {

    controlPanel
        .getWaveformComboBox()
        .addActionListener(
            new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {

                controlModel.setWaveform(
                    controlPanel.getWaveformComboBox().getSelectedItem().toString());
              }
            });

      controlPanel
              .getReadPulseAmplitudeSlider()
              .addChangeListener(
                      new ChangeListener() {

                          @Override
                          public void stateChanged(ChangeEvent e) {

                              JSlider source = (JSlider) e.getSource();
                              if (!(source.getValueIsAdjusting())) {
                                  controlModel.setReadPulseAmplitude(source.getValue() / (double) 1000);
                                  controlPanel
                                          .getReadPulseAmplitudeSlider()
                                          .setBorder(
                                                  BorderFactory.createTitledBorder(
                                                          "Read Pulse Amplitude [V] = " + controlModel.getReadPulseAmplitude()));
                              }
                          }
                      });

    controlPanel
        .getAmplitudeSlider()
        .addChangeListener(
            new ChangeListener() {

              @Override
              public void stateChanged(ChangeEvent e) {

                JSlider source = (JSlider) e.getSource();
                if (!(source.getValueIsAdjusting())) {
                  controlModel.setAmplitude(source.getValue() / (float) 100);
                  controlPanel
                      .getAmplitudeSlider()
                      .setBorder(
                          BorderFactory.createTitledBorder(
                              "Forward Amplitude [V] = " + controlModel.getAmplitude()));
                }
              }
            });

    controlPanel
        .getPulseWidthSlider()
        .addChangeListener(
            new ChangeListener() {

              @Override
              public void stateChanged(ChangeEvent e) {

                JSlider source = (JSlider) e.getSource();
                if (!(source.getValueIsAdjusting())) {
                  controlModel.setPulseWidth(source.getValue());
                  controlPanel
                      .getPulseWidthSlider()
                      .setBorder(
                          BorderFactory.createTitledBorder(
                              "Forward Pulse Width [µs] = "
                                  + (double) controlModel.getPulseWidth() / 1000));
                  controlPanel
                      .getPulseWidthSliderNs()
                      .setBorder(BorderFactory.createTitledBorder("Forward Pulse Width [µs]"));
                }
              }
            });

    controlPanel
        .getPulseWidthSliderNs()
        .addChangeListener(
            new ChangeListener() {

              @Override
              public void stateChanged(ChangeEvent e) {

                JSlider source = (JSlider) e.getSource();
                if (!(source.getValueIsAdjusting())) {
                  controlModel.setPulseWidth(source.getValue());
                  controlPanel
                      .getPulseWidthSlider()
                      .setBorder(BorderFactory.createTitledBorder("Forward Pulse Width [µs]"));
                  controlPanel
                      .getPulseWidthSliderNs()
                      .setBorder(
                          BorderFactory.createTitledBorder(
                              "Forward Pulse Width [µs] = "
                                  + (double) controlModel.getPulseWidth() / 1000));
                }
              }
            });

    controlPanel
        .getPulseNumberSlider()
        .addChangeListener(
            new ChangeListener() {

              @Override
              public void stateChanged(ChangeEvent e) {

                JSlider source = (JSlider) e.getSource();
                if (!(source.getValueIsAdjusting())) {
                  controlModel.setPulseNumber(source.getValue());
                  controlPanel
                      .getPulseNumberSlider()
                      .setBorder(
                          BorderFactory.createTitledBorder(
                              "Forward Pulse Number = " + controlModel.getPulseNumber()));
                }
              }
            });

    controlPanel
        .getDutyCycleSlider()
        .addChangeListener(
            new ChangeListener() {

              @Override
              public void stateChanged(ChangeEvent e) {

                JSlider source = (JSlider) e.getSource();
                if (!(source.getValueIsAdjusting())) {

                  controlModel.setDutyCycle((double) source.getValue() / 100.0);
                  controlPanel
                      .getDutyCycleSlider()
                      .setBorder(
                          BorderFactory.createTitledBorder(
                              "Forward Duty Cycle = " + controlModel.getDutyCycle()));
                }
              }
            });

      controlPanel
              .getReverseAmplitudeSlider()
              .addChangeListener(
                      new ChangeListener() {

                          @Override
                          public void stateChanged(ChangeEvent e) {

                              JSlider source = (JSlider) e.getSource();
                              if (!(source.getValueIsAdjusting())) {
                                  controlModel.setReverseAmplitude(source.getValue() / (float) 100);
                                  controlPanel
                                          .getReverseAmplitudeSlider()
                                          .setBorder(
                                                  BorderFactory.createTitledBorder(
                                                          "Reverse Amplitude [V] = " + controlModel.getReverseAmplitude()));
                              }
                          }
                      });

      controlPanel
              .getReversePulseWidthSlider()
              .addChangeListener(
                      new ChangeListener() {

                          @Override
                          public void stateChanged(ChangeEvent e) {

                              JSlider source = (JSlider) e.getSource();
                              if (!(source.getValueIsAdjusting())) {
                                  controlModel.setReversePulseWidth(source.getValue());
                                  controlPanel
                                          .getReversePulseWidthSlider()
                                          .setBorder(
                                                  BorderFactory.createTitledBorder(
                                                          "Reverse Pulse Width [µs] = "
                                                                  + (double) controlModel.getReversePulseWidth() / 1000));
                                  controlPanel
                                          .getReversePulseWidthSliderNs()
                                          .setBorder(BorderFactory.createTitledBorder("Reverse Pulse Width [µs]"));
                              }
                          }
                      });

      controlPanel
              .getReversePulseWidthSliderNs()
              .addChangeListener(
                      new ChangeListener() {

                          @Override
                          public void stateChanged(ChangeEvent e) {

                              JSlider source = (JSlider) e.getSource();
                              if (!(source.getValueIsAdjusting())) {
                                  controlModel.setReversePulseWidth(source.getValue());
                                  controlPanel
                                          .getReversePulseWidthSlider()
                                          .setBorder(BorderFactory.createTitledBorder("Reverse Pulse Width [µs]"));
                                  controlPanel
                                          .getReversePulseWidthSliderNs()
                                          .setBorder(
                                                  BorderFactory.createTitledBorder(
                                                          "Reverse Pulse Width [µs] = "
                                                                  + (double) controlModel.getReversePulseWidth() / 1000));
                              }
                          }
                      });

      controlPanel
              .getReversePulseNumberSlider()
              .addChangeListener(
                      new ChangeListener() {

                          @Override
                          public void stateChanged(ChangeEvent e) {

                              JSlider source = (JSlider) e.getSource();
                              if (!(source.getValueIsAdjusting())) {
                                  controlModel.setReversePulseNumber(source.getValue());
                                  controlPanel
                                          .getReversePulseNumberSlider()
                                          .setBorder(
                                                  BorderFactory.createTitledBorder(
                                                          "Reverse Pulse Number = " + controlModel.getReversePulseNumber()));
                              }
                          }
                      });

      controlPanel
              .getReverseDutyCycleSlider()
              .addChangeListener(
                      new ChangeListener() {

                          @Override
                          public void stateChanged(ChangeEvent e) {

                              JSlider source = (JSlider) e.getSource();
                              if (!(source.getValueIsAdjusting())) {

                                  controlModel.setReverseDutyCycle((double) source.getValue() / 100.0);
                                  controlPanel
                                          .getReverseDutyCycleSlider()
                                          .setBorder(
                                                  BorderFactory.createTitledBorder(
                                                          "Reverse Duty Cycle = " + controlModel.getReverseDutyCycle()));
                              }
                          }
                      });

    controlPanel
        .getSeriesTextField()
        .addKeyListener(
            new KeyAdapter() {

              @Override
              public void keyReleased(KeyEvent e) {

                JTextField textField = (JTextField) e.getSource();
                String text = textField.getText();

                try {
                  int newSeriesValue = Integer.parseInt(text);
                  controlModel.setSeriesResistance(newSeriesValue);
                } catch (Exception ex) {
                  // parsing error, default back to previous value
                  textField.setText(Integer.toString(controlModel.getSeriesResistance()));
                }
              }
            });

    controlPanel
          .getTargetTextField()
          .addKeyListener(
                  new KeyAdapter() {

                      @Override
                      public void keyReleased(KeyEvent e) {

                          JTextField textField = (JTextField) e.getSource();
                          String text = textField.getText();

                          try {
                              int newTargetValue = Integer.parseInt(text);
                              controlModel.setTargetResistance(newTargetValue);
                          } catch (Exception ex) {
                              // parsing error, default back to previous value
                              textField.setText(Integer.toString(controlModel.getTargetResistance()));
                          }
                      }
                  });

    controlPanel
        .getSampleRateTextField()
        .addKeyListener(
            new KeyAdapter() {

              @Override
              public void keyReleased(KeyEvent e) {

                JTextField textField = (JTextField) e.getSource();
                String text = textField.getText();

                try {
                  int newValue = Integer.parseInt(text);
                  controlModel.setSampleRate(newValue);
                } catch (Exception ex) {
                  // parsing error, default back to previous value
                  textField.setText(Integer.toString(controlModel.getSampleRate()));
                }
              }
            });

    //    controlPanel
    //        .getMemristorVoltageCheckBox()
    //        .addActionListener(
    //            new ActionListener() {
    //
    //              @Override
    //              public void actionPerformed(ActionEvent actionEvent) {
    //
    //                AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
    //                boolean selected = abstractButton.getModel().isSelected();
    //                // System.out.println("selected = " + selected);
    //                controlModel.setMemristorVoltageDropSelected(selected);
    //              }
    //            });

    controlView
        .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke("S"), "startstop");
    controlView
        .getActionMap()
        .put(
            "startstop",
            new AbstractAction() {

              @Override
              public void actionPerformed(ActionEvent e) {

                controlPanel.getStartStopButton().doClick();
              }
            });
  }

  /**
   * These property change events are triggered in the controlModel in the case where the underlying
   * controlModel is updated. Here, the controller can respond to those events and make sure the
   * corresponding GUI components get updated.
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {

    switch (evt.getPropertyName()) {
      case DWFProxy.AD2_STARTUP_CHANGE:
        controlPanel.enableAllChildComponents((Boolean) evt.getNewValue());
        break;

      case Model.EVENT_PREFERENCES_UPDATE:
        initGUIComponentsFromModel();
        break;

      case Model.EVENT_WAVEFORM_UPDATE:
        controlModel.updateWaveformChartData();
        controlModel.updateEnergyData();
        controlPanel.updateEnergyGUI(
            controlModel.getAmplitude(),
            controlModel.getAppliedCurrent(),
            controlModel.getAppliedEnergy());
        controlPanel.updateDebugMsg("G=" + controlModel.getLastGAsString() + ", R=" + controlModel.getLastRAsString());
        controlPanel.updateDebugMsg2("UpperR=" + controlModel.getUpperRAsString() + ", LowerR=" + controlModel.getLowerRAsString());
        break;

      default:
        break;
    }
  }
}
