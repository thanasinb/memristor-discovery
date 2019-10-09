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

import org.knowm.memristor.discovery.DWFProxy;
import org.knowm.memristor.discovery.core.PostProcessDataUtils;
import org.knowm.memristor.discovery.core.WaveformUtils;
import org.knowm.memristor.discovery.gui.mvc.experiments.ControlView;
import org.knowm.memristor.discovery.gui.mvc.experiments.Experiment;
import org.knowm.memristor.discovery.gui.mvc.experiments.ExperimentPreferences;
import org.knowm.memristor.discovery.gui.mvc.experiments.ExperimentPreferences.Waveform;
import org.knowm.memristor.discovery.gui.mvc.experiments.Model;
import org.knowm.memristor.discovery.gui.mvc.experiments.conductance.ConductancePreferences;
import org.knowm.memristor.discovery.gui.mvc.experiments.program.control.ControlController;
import org.knowm.memristor.discovery.gui.mvc.experiments.program.control.ControlModel;
import org.knowm.memristor.discovery.gui.mvc.experiments.program.control.ControlPanel;
import org.knowm.memristor.discovery.gui.mvc.experiments.program.result.ResultController;
import org.knowm.memristor.discovery.gui.mvc.experiments.program.result.ResultModel;
import org.knowm.memristor.discovery.gui.mvc.experiments.program.result.ResultPanel;
import org.knowm.waveforms4j.DWF;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProgramExperiment extends Experiment {

  // Control and Result MVC
  private final ControlModel controlModel;
  private final ResultModel resultModel;
  private final ResultController resultController;
  private boolean initialPulseTrainCaptured = false;
  private ControlPanel controlPanel;
  private ResultPanel resultPanel;
  private double lastR;
  private int count=0;
  private String timeStamp, filepath, filename;
  // SwingWorkers
  private SwingWorker experimentCaptureWorker;

  // private static float READ_PULSE_AMPLITUDE = .07f;//move this to preferences eventually...

  /**
   * Constructor
   *
   * @param dwfProxy
   * @param mainFrameContainer
   */
  public ProgramExperiment(DWFProxy dwfProxy, Container mainFrameContainer, int boardVersion) {

    super(dwfProxy, mainFrameContainer, boardVersion);

    controlModel = new ControlModel(boardVersion);
    controlPanel = new ControlPanel();
    resultModel = new ResultModel();
    resultPanel = new ResultPanel();

    refreshModelsFromPreferences();
    new ControlController(controlPanel, controlModel, dwfProxy);
    resultController = new ResultController(resultPanel, resultModel);
  }

  @Override
  public void doCreateAndShowGUI() {

    //     trigger waveform update event
    PropertyChangeEvent evt =
        new PropertyChangeEvent(this, Model.EVENT_WAVEFORM_UPDATE, true, false);
    propertyChange(evt);

    // when the control panel is manipulated, we need to communicate the changes to the results
    // panel
    getControlModel().addListener(this);
  }

  @Override
  public void addWorkersToButtonEvents() {

    controlPanel
        .getStartStopButton()
        .addActionListener(
            new ActionListener() {

              @Override
              public void actionPerformed(ActionEvent e) {

                if (!controlModel.isStartToggled()) {

                  controlModel.setStartToggled(true);
                  controlPanel.getStartStopButton().setText("Stop");
                  controlPanel.enableCheckBoxes(false);

                  // start AD2 waveform 1 and start AD2 capture on channel 1 and 2
                  experimentCaptureWorker = new CaptureWorker();
                  experimentCaptureWorker.execute();
                } else {

                  controlModel.setStartToggled(false);
                  controlPanel.getStartStopButton().setText("Start");
                  controlPanel.enableCheckBoxes(true);
                  if(controlModel.isSave()){
                    try {
                      Thread.sleep(50);
                    } catch (InterruptedException ex) {
                      ex.printStackTrace();
                    }
                    resultController.saveGChart(filename);
                  }
                  // cancel the worker
                  experimentCaptureWorker.cancel(true);
                }
              }
            });
  }

  @Override
  public Model getControlModel() {

    return controlModel;
  }

  @Override
  public ControlView getControlPanel() {

    return controlPanel;
  }

  @Override
  public Model getResultModel() {
    return resultModel;
  }

  @Override
  public JPanel getResultPanel() {

    return resultPanel;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {

    String propName = evt.getPropertyName();

    switch (propName) {
      case Model.EVENT_WAVEFORM_UPDATE:
        resultPanel.switch2WaveformChart();
        resultController.updateWaveformChart(
            controlModel.getWaveformTimeData(),
            controlModel.getWaveformAmplitudeData(),
            controlModel.getAmplitude(),
            controlModel.getPulseWidth());
        resultController.updateReverseWaveformChart(
                controlModel.getReverseWaveformTimeData(),
                controlModel.getReverseWaveformAmplitudeData(),
                controlModel.getReverseAmplitude(),
                controlModel.getReversePulseWidth());

        break;

      default:
        break;
    }
  }

  @Override
  public ExperimentPreferences initAppPreferences() {

    return new ProgramPreferences();
  }

  private class CaptureWorker extends SwingWorker<Boolean, double[][]> {

    @Override
    protected Boolean doInBackground() throws Exception {
      if(controlModel.isSave()){
        timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        filepath = System.getProperty("user.dir") + "\\" + timeStamp;
        File file = new File(filepath);
        if (!file.exists()) {
          if (!file.mkdir()) {
            return false;
          }
        }
        count = 0;
      }

      while (controlModel.isStartToggled()) {
        if (!readCycle())
          return false;
        else {
          lastR = controlModel.getLastR();
//          System.out.println(String.valueOf(lastR));
          initialPulseTrainCaptured = false;
        }
        if(!controlModel.isReadOnly()){
          if (lastR < controlModel.getLowerResistance()) {
            if (!reverseWrite()) // negative pulse
              return false;
            else
              initialPulseTrainCaptured = false;
          } else if (lastR > controlModel.getUpperResistance()) {
            if (!forwardWrite())
              return false;
            else
              initialPulseTrainCaptured = false;
          } else {
            controlPanel.getStartStopButton().setText("Done!");
            break;
          }
        }
        if (controlModel.isOneShot()) {
          controlPanel.getStartStopButton().setText("Done!");
          break;
        }
      }
      return true;
    }

    private Boolean forwardWrite(){
      // ////////////////////////////////
      // Analog In /////////////////
      // ////////////////////////////////

      int samplesPerPulse = 200;
      double sampleFrequency = controlModel.getCalculatedFrequency() * samplesPerPulse;
      boolean isScale2V = Math.abs(controlModel.getAmplitude()) <= 2.5;
      int bufferSize = samplesPerPulse * controlModel.getPulseNumber() + samplesPerPulse;

      dwfProxy
              .getDwf()
              .startAnalogCaptureBothChannelsTriggerOnWaveformGenerator(
                      DWF.WAVEFORM_CHANNEL_1, sampleFrequency, bufferSize, isScale2V);

      dwfProxy.waitUntilArmed();

      // ////////////////////////////////
      // Pulse Out /////////////////
      // ////////////////////////////////

      double[] customWaveform;
      if (boardVersion == 2) {
        customWaveform =
                WaveformUtils.generateCustomPulse(
                        controlModel.getWaveform(),
                        -controlModel.getAmplitude(),
                        controlModel.getPulseWidth(),
                        controlModel.getDutyCycle());
      } else {
        customWaveform =
                WaveformUtils.generateCustomPulse(
                        controlModel.getWaveform(),
                        controlModel.getAmplitude(),
                        controlModel.getPulseWidth(),
                        controlModel.getDutyCycle());
      }

      dwfProxy
              .getDwf()
              .startCustomPulseTrain(
                      DWF.WAVEFORM_CHANNEL_1,
                      controlModel.getCalculatedFrequency(),
                      0,
                      controlModel.getPulseNumber(),
                      customWaveform);

      // ////////////////////////////////

      // Read In Data
      boolean success =
              dwfProxy.capturePulseData(
                      controlModel.getCalculatedFrequency(), controlModel.getPulseNumber());
      if (!success) {
        // Stop Analog In and Out
        dwfProxy.getDwf().stopWave(DWF.WAVEFORM_CHANNEL_1);
        dwfProxy.getDwf().stopAnalogCaptureBothChannels();
        controlPanel.getStartStopButton().doClick();
        return false;
      }

      // Get Raw Data from Oscilloscope
      int validSamples = dwfProxy.getDwf().FDwfAnalogInStatusSamplesValid();
      double[] v1 =
              dwfProxy.getDwf().FDwfAnalogInStatusData(DWF.OSCILLOSCOPE_CHANNEL_1, validSamples);
      double[] v2 =
              dwfProxy.getDwf().FDwfAnalogInStatusData(DWF.OSCILLOSCOPE_CHANNEL_2, validSamples);

      // Stop Analog In and Out
      dwfProxy.getDwf().stopWave(DWF.WAVEFORM_CHANNEL_1);
      dwfProxy.getDwf().stopAnalogCaptureBothChannels();

      // /////////////////////////
      // Create Chart Data //////
      // /////////////////////////

      double[][] trimmedRawData = PostProcessDataUtils.trimIdleData(v1, v2, 0.05, 10);
      double[] V1Trimmed = trimmedRawData[0];
      double[] V2Trimmed = trimmedRawData[1];
      double[] VMemristor = PostProcessDataUtils.getV1MinusV2(V1Trimmed, V2Trimmed);
      double[] timeData;
      int bufferLength;
      double timeStep;

      if (boardVersion == 2) {
        bufferLength = V1Trimmed.length;

        VMemristor = PostProcessDataUtils.invert(V1Trimmed);

        // create time data
        timeData = new double[bufferLength];
        timeStep = 1.0 / sampleFrequency * ProgramPreferences.TIME_UNIT.getDivisor();
        for (int i = 0; i < bufferLength; i++) {
          timeData[i] = i * timeStep;
        }

        // create current data
        double[] current = new double[bufferLength];
        for (int i = 0; i < bufferLength; i++) {
          current[i] =
                  (V1Trimmed[i] - V2Trimmed[i])
                          / controlModel.getSeriesResistance()
                          * ProgramPreferences.CURRENT_UNIT.getDivisor();
        }

        // create conductance data
        double[] conductance = new double[bufferLength];
        for (int i = 0; i < bufferLength; i++) {

          double I = (V1Trimmed[i] - V2Trimmed[i]) / controlModel.getSeriesResistance();
          double G = I / VMemristor[i] * ProgramPreferences.CONDUCTANCE_UNIT.getDivisor();
          G = G < 0 ? 0 : G;
          conductance[i] = G;
        }
        publish(
                new double[][] {
                        timeData, V1Trimmed, V2Trimmed, VMemristor, current, conductance, null
                });
      } else {
        bufferLength = V1Trimmed.length;

        // create time data
        timeData = new double[bufferLength];
        timeStep = 1.0 / sampleFrequency * ProgramPreferences.TIME_UNIT.getDivisor();
        for (int i = 0; i < bufferLength; i++) {
          timeData[i] = i * timeStep;
        }

        // create current data
        double[] current = new double[bufferLength];
        for (int i = 0; i < bufferLength; i++) {
          current[i] =
                  V2Trimmed[i]
                          / controlModel.getSeriesResistance()
                          * ProgramPreferences.CURRENT_UNIT.getDivisor();
        }

        // create conductance data
        double[] conductance = new double[bufferLength];
        for (int i = 0; i < bufferLength; i++) {

          double I = V2Trimmed[i] / controlModel.getSeriesResistance();
          double G =
                  I / (V1Trimmed[i] - V2Trimmed[i]) * ProgramPreferences.CONDUCTANCE_UNIT.getDivisor();
          G = G < 0 ? 0 : G;
          conductance[i] = G;
        }

        if(controlModel.isSave()){
          filename = filepath + "\\" + String.valueOf(count++) + "-forward-";
        }

        publish(
                new double[][] {
                        timeData, V1Trimmed, V2Trimmed, VMemristor, current, conductance, null
                });
      }

      while (!initialPulseTrainCaptured) {
        // System.out.println("Waiting...");
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          return false;
        }
      }
      return true;
    }

    private Boolean reverseWrite(){
      // ////////////////////////////////
      // Analog In /////////////////
      // ////////////////////////////////

      int samplesPerPulse = 200;
      double sampleFrequency = controlModel.getReverseCalculatedFrequency() * samplesPerPulse;
      boolean isScale2V = Math.abs(controlModel.getReverseAmplitude()) <= 2.5;
      int bufferSize = samplesPerPulse * controlModel.getReversePulseNumber() + samplesPerPulse;

      dwfProxy
              .getDwf()
              .startAnalogCaptureBothChannelsTriggerOnWaveformGenerator(
                      DWF.WAVEFORM_CHANNEL_1, sampleFrequency, bufferSize, isScale2V);

      dwfProxy.waitUntilArmed();

      // ////////////////////////////////
      // Pulse Out /////////////////
      // ////////////////////////////////

      double[] customWaveform;
      if (boardVersion == 2) {
        customWaveform =
                WaveformUtils.generateCustomPulse(
                        controlModel.getWaveform(),
                        -controlModel.getReverseAmplitude(),
                        controlModel.getReversePulseWidth(),
                        controlModel.getReverseDutyCycle());
      } else {
        customWaveform =
                WaveformUtils.generateCustomPulse(
                        controlModel.getWaveform(),
                        controlModel.getReverseAmplitude(),
                        controlModel.getReversePulseWidth(),
                        controlModel.getReverseDutyCycle());
      }

      dwfProxy
              .getDwf()
              .startCustomPulseTrain(
                      DWF.WAVEFORM_CHANNEL_1,
                      controlModel.getReverseCalculatedFrequency(),
                      0,
                      controlModel.getReversePulseNumber(),
                      customWaveform);

      // ////////////////////////////////

      // Read In Data
      boolean success =
              dwfProxy.capturePulseData(
                      controlModel.getReverseCalculatedFrequency(), controlModel.getReversePulseNumber());
      if (!success) {
        // Stop Analog In and Out
        dwfProxy.getDwf().stopWave(DWF.WAVEFORM_CHANNEL_1);
        dwfProxy.getDwf().stopAnalogCaptureBothChannels();
        controlPanel.getStartStopButton().doClick();
        return false;
      }

      // Get Raw Data from Oscilloscope
      int validSamples = dwfProxy.getDwf().FDwfAnalogInStatusSamplesValid();
      double[] v1 =
              dwfProxy.getDwf().FDwfAnalogInStatusData(DWF.OSCILLOSCOPE_CHANNEL_1, validSamples);
      double[] v2 =
              dwfProxy.getDwf().FDwfAnalogInStatusData(DWF.OSCILLOSCOPE_CHANNEL_2, validSamples);

      // Stop Analog In and Out
      dwfProxy.getDwf().stopWave(DWF.WAVEFORM_CHANNEL_1);
      dwfProxy.getDwf().stopAnalogCaptureBothChannels();

      // /////////////////////////
      // Create Chart Data //////
      // /////////////////////////

      double[][] trimmedRawData = PostProcessDataUtils.trimIdleData(v1, v2, 0.05, 10);
      double[] V1Trimmed = trimmedRawData[0];
      double[] V2Trimmed = trimmedRawData[1];
      double[] VMemristor = PostProcessDataUtils.getV1MinusV2(V1Trimmed, V2Trimmed);
      double[] timeData;
      int bufferLength;
      double timeStep;

      if (boardVersion == 2) {
        bufferLength = V1Trimmed.length;

        VMemristor = PostProcessDataUtils.invert(V1Trimmed);

        // create time data
        timeData = new double[bufferLength];
        timeStep = 1.0 / sampleFrequency * ProgramPreferences.TIME_UNIT.getDivisor();
        for (int i = 0; i < bufferLength; i++) {
          timeData[i] = i * timeStep;
        }

        // create current data
        double[] current = new double[bufferLength];
        for (int i = 0; i < bufferLength; i++) {
          current[i] =
                  (V1Trimmed[i] - V2Trimmed[i])
                          / controlModel.getSeriesResistance()
                          * ProgramPreferences.CURRENT_UNIT.getDivisor();
        }

        // create conductance data
        double[] conductance = new double[bufferLength];
        for (int i = 0; i < bufferLength; i++) {

          double I = (V1Trimmed[i] - V2Trimmed[i]) / controlModel.getSeriesResistance();
          double G = I / VMemristor[i] * ProgramPreferences.CONDUCTANCE_UNIT.getDivisor();
          G = G < 0 ? 0 : G;
          conductance[i] = G;
        }
        publish(
                new double[][] {
                        timeData, V1Trimmed, V2Trimmed, VMemristor, current, conductance, null
                });
      } else {
        bufferLength = V1Trimmed.length;

        // create time data
        timeData = new double[bufferLength];
        timeStep = 1.0 / sampleFrequency * ProgramPreferences.TIME_UNIT.getDivisor();
        for (int i = 0; i < bufferLength; i++) {
          timeData[i] = i * timeStep;
        }

        // create current data
        double[] current = new double[bufferLength];
        for (int i = 0; i < bufferLength; i++) {
          current[i] =
                  V2Trimmed[i]
                          / controlModel.getSeriesResistance()
                          * ProgramPreferences.CURRENT_UNIT.getDivisor();
        }

        // create conductance data
        double[] conductance = new double[bufferLength];
        for (int i = 0; i < bufferLength; i++) {

          double I = V2Trimmed[i] / controlModel.getSeriesResistance();
          double G =
                  I / (V1Trimmed[i] - V2Trimmed[i]) * ProgramPreferences.CONDUCTANCE_UNIT.getDivisor();
          G = G < 0 ? 0 : G;
          conductance[i] = G;
        }

        if(controlModel.isSave()){
          filename = filepath + "\\" + String.valueOf(count++) + "-reverse-";
        }

        publish(
                new double[][] {
                        timeData, V1Trimmed, V2Trimmed, VMemristor, current, conductance, null
                });
      }

      while (!initialPulseTrainCaptured) {
        // System.out.println("Waiting...");
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          return false;
        }
      }

      return true;
    }

    private Boolean readCycle() {
      // ////////////////////////////////
      // READ PULSES /////////////////
      // ////////////////////////////////
//      while (!isCancelled()) {

        try {
          Thread.sleep(controlModel.getSampleRate() * 1000);
        } catch (InterruptedException e) {
          // eat it. caught when interrupt is called
          dwfProxy.getDwf().stopWave(DWF.WAVEFORM_CHANNEL_1);
          dwfProxy.getDwf().stopAnalogCaptureBothChannels();
        }

        // ////////////////////////////////
        // Analog In /////////////////
        // ////////////////////////////////

        // trigger on 20% the rising .1 V read pulse
        int samplesPerPulse = 300;
        double f = 1 / (controlModel.getReadPulseWidth() * 2);
        double sampleFrequency = f * samplesPerPulse;
        dwfProxy
                .getDwf()
                .startAnalogCaptureBothChannelsTriggerOnWaveformGenerator(
                        DWF.WAVEFORM_CHANNEL_1, sampleFrequency, samplesPerPulse, true);
        dwfProxy.waitUntilArmed();

        //////////////////////////////////
        // Pulse Out /////////////////
        //////////////////////////////////

        // read pulse: 0.1 V, 5 us pulse width

        double[] customWaveform =
                WaveformUtils.generateCustomWaveform(
                        Waveform.Square, controlModel.getReadPulseAmplitude(), f);

        dwfProxy.getDwf().startCustomPulseTrain(DWF.WAVEFORM_CHANNEL_1, f, 0, 1, customWaveform);

        // Read In Data
        boolean success = dwfProxy.capturePulseData(f, 1);
        if (!success) {
          // Stop Analog In and Out
          dwfProxy.getDwf().stopWave(DWF.WAVEFORM_CHANNEL_1);
          dwfProxy.getDwf().stopAnalogCaptureBothChannels();
          controlPanel.getStartStopButton().doClick();
          return false;
        } else {

          // Get Raw Data from Oscilloscope
          int validSamples = dwfProxy.getDwf().FDwfAnalogInStatusSamplesValid();
          double[] v1 = dwfProxy.getDwf().FDwfAnalogInStatusData(DWF.OSCILLOSCOPE_CHANNEL_1, validSamples);
          double[] v2 = dwfProxy.getDwf().FDwfAnalogInStatusData(DWF.OSCILLOSCOPE_CHANNEL_2, validSamples);

          // /////////////////////////
          // Create Chart Data //////
          // /////////////////////////

          double[][] trimmedRawData = PostProcessDataUtils.trimIdleData(v1, v2, 0, 10);
          double[] V1Trimmed = trimmedRawData[0];
          double[] V2Trimmed = trimmedRawData[1];
          double[] VMemristor;

          if (boardVersion == 2) {
            VMemristor = PostProcessDataUtils.invert(V1Trimmed);
          } else {
            VMemristor = PostProcessDataUtils.getV1MinusV2(V1Trimmed, V2Trimmed);
          }

          int bufferLength = V1Trimmed.length;

          // create time data
          double[] timeData = new double[bufferLength];
          double timeStep = 1.0 / sampleFrequency * ProgramPreferences.TIME_UNIT.getDivisor();
          for (int i = 0; i < bufferLength; i++) {
            timeData[i] = i * timeStep;
          }

          /*
           * get the voltage of V2 right before pulse falling/rising edge. This is given to the RC Computer to get the resistance.
           */

          double resistance;

          if (boardVersion == 2) {

            double vRead = V1Trimmed[V1Trimmed.length / 3]; // best guess
            for (int i = 50; i < V1Trimmed.length; i++) {
              double pD = (V2Trimmed[i] - V2Trimmed[i - 1]) / V2Trimmed[i];

              if (pD > .05) {
                vRead = V1Trimmed[i - 5];
                //                System.out.println("vRead=" + vRead);
                //                System.out.println(" time=" + timeData[i - 5]);
                break;
              }
            }

            resistance = controlModel.getRcComputer().getRFromV(vRead);

          } else {
            double vRead = V2Trimmed[V2Trimmed.length / 3]; // best guess
            for (int i = 50; i < V1Trimmed.length; i++) {
              double pD = (V1Trimmed[i] - V1Trimmed[i - 1]) / V1Trimmed[i];
              if (pD < -.05) {
                vRead = V2Trimmed[i - 5];
                break;
              }
            }
            resistance = controlModel.getRcComputer().getRFromV(vRead);
          }

          if(controlModel.isSave()){
            filename = filepath + "\\" + String.valueOf(count++) + "-read-";
          }

          double[] conductanceAve =
                  new double[] {
                          (1 / resistance) * ConductancePreferences.CONDUCTANCE_UNIT.getDivisor()
                  };

          if (boardVersion == 2) {
            publish(
                    new double[][] {
                            timeData, V1Trimmed, V2Trimmed, VMemristor, null, null, conductanceAve
                    });
          } else {
            publish(
                    new double[][] {
                            timeData, V1Trimmed, V2Trimmed, VMemristor, null, null, conductanceAve
                    });
          }
        }
        // Stop Analog In and Out
        dwfProxy.getDwf().stopWave(DWF.WAVEFORM_CHANNEL_1);
        dwfProxy.getDwf().stopAnalogCaptureBothChannels();

        while (!initialPulseTrainCaptured) {
          // System.out.println("Waiting...");
          try {
            Thread.sleep(50);
          } catch (InterruptedException e) {
            return false;
          }
        }
//        System.out.println("read");
      return true;
    }

    private String[] double2string (double[] input){
        int size = input.length;
        String[] str = new String[size];
        for(int i=0; i<size; i++) {
            str[i] = String.valueOf(input[i]);
        }
        return str;
    }

    @Override
    protected void process(List<double[][]> chunks) {

      double[][] newestChunk = chunks.get(chunks.size() - 1);

      if (newestChunk[6] == null) { // writing cycle
        initialPulseTrainCaptured = true;

        if(controlModel.isSave()){
          resultController.updateCaptureChartData(
                  newestChunk[0],
                  newestChunk[1],
                  newestChunk[2],
                  newestChunk[3],
                  controlModel.getPulseWidth(),
                  controlModel.getAmplitude(),
                  filename);
          resultController.updateIVChartData(
                  newestChunk[0],
                  newestChunk[4],
                  controlModel.getPulseWidth(),
                  controlModel.getAmplitude(),
                  filename);
        }else {
          resultController.updateCaptureChartData(
                  newestChunk[0],
                  newestChunk[1],
                  newestChunk[2],
                  newestChunk[3],
                  controlModel.getPulseWidth(),
                  controlModel.getAmplitude());
          resultController.updateIVChartData(
                  newestChunk[0],
                  newestChunk[4],
                  controlModel.getPulseWidth(),
                  controlModel.getAmplitude());
        }

        if (resultPanel.getCaptureButton().isSelected()) {
          resultPanel.switch2CaptureChart();
          resultController.repaintVtChart();
        } else if (resultPanel.getIVButton().isSelected()) {
          resultPanel.switch2IVChart();
          resultController.repaintItChart();
        } else {
          resultPanel.switchReadPulseCaptureChart();
          resultController.repaintReadPulseCaptureChart();
        }

      } else { // reading cycle
        // update read pulse capture chart....
        initialPulseTrainCaptured = true;

        // update G chart
        controlModel.setLastG(newestChunk[6][0]);
        resultController.updateGChartData(controlModel.getLastG(), controlModel.getLastRAsString());
        resultController.repaintGChart();

        if(controlModel.isSave()){
          resultController.updateReadPulseCaptureChartData(
                  newestChunk[0],
                  newestChunk[1],
                  newestChunk[2],
                  newestChunk[3],
                  controlModel.getPulseWidth(),
                  controlModel.getAmplitude(),
                  filename);
        }else{
          resultController.updateReadPulseCaptureChartData(
                  newestChunk[0],
                  newestChunk[1],
                  newestChunk[2],
                  newestChunk[3],
                  controlModel.getPulseWidth(),
                  controlModel.getAmplitude());
        }
        resultController.repaintReadPulseCaptureChart();

        controlModel.updateEnergyData();
        controlPanel.updateEnergyGUI(
            controlModel.getAmplitude(),
            controlModel.getAppliedCurrent(),
            controlModel.getAppliedEnergy());
        controlPanel.updateDebugMsg("G=" + controlModel.getLastGAsString() + ", R=" + controlModel.getLastRAsString());
        controlPanel.updateDebugMsg2("UpperR=" + controlModel.getUpperRAsString() + ", LowerR=" + controlModel.getLowerRAsString());
      }
    }
  }
}
