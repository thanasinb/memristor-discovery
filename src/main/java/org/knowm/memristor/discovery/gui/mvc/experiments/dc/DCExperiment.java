/**
 * Memristor-Discovery is distributed under the GNU General Public License version 3
 * and is also available under alternative licenses negotiated directly
 * with Knowm, Inc.
 *
 * Copyright (c) 2016-2017 Knowm Inc. www.knowm.org
 *
 * This package also includes various components that are not part of
 * Memristor-Discovery itself:
 *
 * * `Multibit`: Copyright 2011 multibit.org, MIT License
 * * `SteelCheckBox`: Copyright 2012 Gerrit, BSD license
 *
 * Knowm, Inc. holds copyright
 * and/or sufficient licenses to all components of the Memristor-Discovery
 * package, and therefore can grant, at its sole discretion, the ability
 * for companies, individuals, or organizations to create proprietary or
 * open source (even if not GPL) modules which may be dynamically linked at
 * runtime with the portions of Memristor-Discovery which fall under our
 * copyright/license umbrella, or are distributed under more flexible
 * licenses than GPL.
 *
 * The 'Knowm' name and logos are trademarks owned by Knowm, Inc.
 *
 * If you have any questions regarding our licensing policy, please
 * contact us at `contact@knowm.org`.
 */
package org.knowm.memristor.discovery.gui.mvc.experiments.dc;

import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.util.List;

import javax.swing.SwingWorker;

import org.knowm.memristor.discovery.DWFProxy;
import org.knowm.memristor.discovery.gui.mvc.experiments.Experiment;
import org.knowm.memristor.discovery.gui.mvc.experiments.ExperimentControlModel;
import org.knowm.memristor.discovery.gui.mvc.experiments.ExperimentControlPanel;
import org.knowm.memristor.discovery.gui.mvc.experiments.ExperimentPlotPanel;
import org.knowm.memristor.discovery.gui.mvc.experiments.dc.control.ControlController;
import org.knowm.memristor.discovery.gui.mvc.experiments.dc.control.ControlModel;
import org.knowm.memristor.discovery.gui.mvc.experiments.dc.control.ControlPanel;
import org.knowm.memristor.discovery.gui.mvc.experiments.dc.plot.PlotControlModel;
import org.knowm.memristor.discovery.gui.mvc.experiments.dc.plot.PlotController;
import org.knowm.memristor.discovery.gui.mvc.experiments.dc.plot.PlotPanel;
import org.knowm.memristor.discovery.utils.PostProcessDataUtils;
import org.knowm.memristor.discovery.utils.WaveformUtils;
import org.knowm.waveforms4j.DWF;

public class DCExperiment extends Experiment {

  private final ControlModel controlModel = new ControlModel();
  private ControlPanel controlPanel;

  private PlotPanel plotPanel;
  private final PlotControlModel plotModel = new PlotControlModel();
  private final PlotController plotController;

  /**
   * Constructor
   *
   * @param dwfProxy
   * @param mainFrameContainer
   */
  public DCExperiment(DWFProxy dwfProxy, Container mainFrameContainer) {

    super(dwfProxy, mainFrameContainer);

    controlPanel = new ControlPanel();
    plotPanel = new PlotPanel();
    plotController = new PlotController(plotPanel, plotModel);
    new ControlController(controlPanel, plotPanel, controlModel, dwfProxy);
  }

  @Override
  public void doCreateAndShowGUI() {

  }

  private class CaptureWorker extends SwingWorker<Boolean, double[][]> {

    @Override
    protected Boolean doInBackground() throws Exception {

      //////////////////////////////////
      // Analog In /////////////////
      //////////////////////////////////

      int freqMultTest = 1;
      int sampleFrequencyMultiplier = 200; // adjust this down if you want to capture more pulses as the buffer size is limited.
      double sampleFrequency = controlModel.getCalculatedFrequency() * freqMultTest * sampleFrequencyMultiplier; // adjust this down if you want to capture more pulses as the buffer size is limited.
      dwfProxy.getDwf().startAnalogCaptureBothChannelsLevelTrigger(sampleFrequency, 0.02 * (controlModel.getAmplitude() > 0 ? 1 : -1));
      // dwfProxy.getDwf().startAnalogCaptureBothChannelsLevelTrigger(sampleFrequency, .3);
      // Thread.sleep(200); // Attempt to allow Analog In to get fired up for the next set of pulses
      // System.out.println("sampleFrequency = " + sampleFrequency);

      long startTime = System.currentTimeMillis();
      while (true) {
        byte status = dwfProxy.getDwf().FDwfAnalogInStatus(true);
        // System.out.println("status: " + status);
        if (status == 1) { // armed
          // System.out.println("armed.");
          break;
        }
      }
      // System.out.println("time = " + (System.currentTimeMillis() - startTime));

      //////////////////////////////////
      // Pulse Out /////////////////
      //////////////////////////////////

      double[] customWaveform = WaveformUtils.generateCustomWaveform(controlModel.getWaveform(), controlModel.getAmplitude(), controlModel.getCalculatedFrequency() * freqMultTest);
      dwfProxy.getDwf().startCustomPulseTrain(DWF.WAVEFORM_CHANNEL_1, controlModel.getCalculatedFrequency() * freqMultTest, 0, controlModel.getPulseNumber(), customWaveform);

      //////////////////////////////////
      //////////////////////////////////

      // Read In Data
      boolean success = capturePulseData();
      if (!success) {
        controlPanel.getStartStopButton().doClick();
        return false;
      }

      // Get Raw Data from Oscilloscope
      int validSamples = dwfProxy.getDwf().FDwfAnalogInStatusSamplesValid();
      double[] v1 = dwfProxy.getDwf().FDwfAnalogInStatusData(DWF.OSCILLOSCOPE_CHANNEL_1, validSamples);
      double[] v2 = dwfProxy.getDwf().FDwfAnalogInStatusData(DWF.OSCILLOSCOPE_CHANNEL_2, validSamples);

      // TODO add this to bail code too?
      // Stop Analog In and Out
      dwfProxy.getDwf().stopWave(DWF.WAVEFORM_CHANNEL_1);
      dwfProxy.getDwf().stopAnalogCaptureBothChannels();

      ///////////////////////////
      // Create Chart Data //////
      ///////////////////////////

      double[][] trimmedRawData = PostProcessDataUtils.trimIdleData(v1, v2, 0.02, 10);
      double[] V1Trimmed = trimmedRawData[0];
      double[] V2Trimmed = trimmedRawData[1];
      double[] V2MinusV1 = PostProcessDataUtils.getV1MinusV2(V1Trimmed, V2Trimmed);

      int bufferLength = V1Trimmed.length;

      // create time data
      double[] timeData = new double[bufferLength];
      double timeStep = 1 / sampleFrequency * DCPreferences.TIME_UNIT.getDivisor();
      for (int i = 0; i < bufferLength; i++) {
        timeData[i] = i * timeStep;
      }

      // create current data
      double[] current = new double[bufferLength];
      for (int i = 0; i < bufferLength; i++) {
        current[i] = V2Trimmed[i] / controlModel.getSeriesResistance() * DCPreferences.CURRENT_UNIT.getDivisor();
      }

      // create conductance data
      double[] conductance = new double[bufferLength];
      for (int i = 0; i < bufferLength; i++) {

        double I = V2Trimmed[i] / controlModel.getSeriesResistance();
        double G = I / (V1Trimmed[i] - V2Trimmed[i]) * DCPreferences.CONDUCTANCE_UNIT.getDivisor();
        G = G < 0 ? 0 : G;
        conductance[i] = G;
      }

      publish(new double[][]{timeData, V1Trimmed, V2Trimmed, V2MinusV1, current, conductance});

      return true;
    }

    @Override
    protected void process(List<double[][]> chunks) {

      double[][] newestChunk = chunks.get(chunks.size() - 1);

      plotController.updateCaptureChartData(newestChunk[0], newestChunk[1], newestChunk[2], newestChunk[3], controlModel.getPeriod(), controlModel.getAmplitude());
      plotController.updateIVChartData(newestChunk[1], newestChunk[4], controlModel.getPeriod(), controlModel.getAmplitude());
      plotController.updateGVChartData(newestChunk[1], newestChunk[5], controlModel.getPeriod(), controlModel.getAmplitude());

      if (plotPanel.getCaptureButton().isSelected()) {
        plotController.repaintCaptureChart();
        plotPanel.switch2CaptureChart();
      }
      else if (plotPanel.getIVButton().isSelected()) {
        plotController.repaintItChart();
        plotPanel.switch2IVChart();
      }
      else {
        plotController.repaintRtChart();
        plotPanel.switch2GVChart();
      }
      controlPanel.getStartStopButton().doClick();
    }
  }

  /**
   * These property change events are triggered in the controlModel in the case where the underlying controlModel is updated. Here, the controller can respond to those events and make sure the corresponding GUI
   * components get updated.
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {

    switch (evt.getPropertyName()) {

      case ExperimentControlModel.EVENT_WAVEFORM_UPDATE:

        if (!controlModel.isStartToggled()) {

          plotPanel.switch2WaveformChart();
          plotController.updateWaveformChart(controlModel.getWaveformTimeData(), controlModel.getWaveformAmplitudeData(), controlModel.getAmplitude(), controlModel.getPeriod());
        }
        break;

      default:
        break;
    }
  }

  public ExperimentControlModel getControlModel() {

    return controlModel;
  }

  @Override
  public ExperimentControlPanel getControlPanel() {

    return controlPanel;
  }

  @Override
  public ExperimentPlotPanel getPlotPanel() {

    return plotPanel;
  }

  @Override
  public SwingWorker getCaptureWorker() {

    return new CaptureWorker();
  }
}
