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
package org.knowm.memristor.discovery.gui.mvc.experiments.synapse;

import static org.knowm.memristor.discovery.gui.mvc.experiments.synapse.control.ControlModel.EVENT_INSTRUCTION_UPDATE;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.SwingWorker;
import org.knowm.memristor.discovery.DWFProxy;
import org.knowm.memristor.discovery.gui.mvc.experiments.Experiment;
import org.knowm.memristor.discovery.gui.mvc.experiments.ExperimentResultsPanel;
import org.knowm.memristor.discovery.gui.mvc.experiments.Model;
import org.knowm.memristor.discovery.gui.mvc.experiments.View;
import org.knowm.memristor.discovery.gui.mvc.experiments.synapse.AHaHController_21.Instruction;
import org.knowm.memristor.discovery.gui.mvc.experiments.synapse.control.ControlController;
import org.knowm.memristor.discovery.gui.mvc.experiments.synapse.control.ControlModel;
import org.knowm.memristor.discovery.gui.mvc.experiments.synapse.control.ControlPanel;
import org.knowm.memristor.discovery.gui.mvc.experiments.synapse.result.ResultController;
import org.knowm.memristor.discovery.gui.mvc.experiments.synapse.result.ResultModel;
import org.knowm.memristor.discovery.gui.mvc.experiments.synapse.result.ResultPanel;
import org.knowm.memristor.discovery.utils.gpio.MuxController;

public class SynapseExperiment extends Experiment {

  private static final float INIT_CONDUCTANCE = .0002f;

  private final MuxController muxController;
  DecimalFormat df = new DecimalFormat("0.000E0");
  private AHaHController_21 aHaHController;

  // Control and Result MVC
  private final ControlModel controlModel;
  private ControlPanel controlPanel;
  private final ResultModel resultModel;
  private ResultPanel resultPanel;
  private final ResultController resultController;

  // SwingWorkers
  private SwingWorker initSynapseWorker;
  private SwingWorker experimentCaptureWorker;
  private SwingWorker clearChartWorker;

  /**
   * Constructor
   *
   * @param dwfProxy
   * @param mainFrameContainer
   */
  public SynapseExperiment(DWFProxy dwfProxy, Container mainFrameContainer, boolean isV1Board) {

    super(dwfProxy, mainFrameContainer, isV1Board);

    controlModel = new ControlModel();
    controlPanel = new ControlPanel();
    new ControlController(controlPanel, controlModel, dwfProxy);
    resultModel = new ResultModel();
    resultPanel = new ResultPanel();
    resultController = new ResultController(resultPanel, resultModel);

    dwfProxy.setUpper8IOStates(controlModel.getInstruction().getBits());

    aHaHController = new AHaHController_21(controlModel);
    aHaHController.setdWFProxy(dwfProxy);
    muxController = new MuxController();
  }

  @Override
  public void addWorkersToButtonEvents() {

    controlPanel.clearPlotButton.addActionListener(
        new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {

            clearChartWorker = new ClearChartWorker();
            clearChartWorker.execute();
          }
        });

    controlPanel
        .getStartStopButton()
        .addActionListener(
            new ActionListener() {

              @Override
              public void actionPerformed(ActionEvent e) {

                if (!controlModel.isStartToggled()) {

                  controlModel.setStartToggled(true);
                  controlPanel.getStartStopButton().setText("Stop");

                  // start AD2 waveform 1 and start AD2 capture on channel 1 and 2
                  experimentCaptureWorker = new CaptureWorker();
                  experimentCaptureWorker.execute();
                } else {

                  controlModel.setStartToggled(false);
                  controlPanel.getStartStopButton().setText("Start");

                  // cancel the worker
                  experimentCaptureWorker.cancel(true);
                }
              }
            });

    controlPanel.initSynapseButton.addActionListener(
        new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {

            initSynapseWorker = new InitSynapseWorker();
            initSynapseWorker.execute();
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

    String propName = evt.getPropertyName();

    switch (propName) {
      case EVENT_INSTRUCTION_UPDATE:

        // System.out.println(controlModel.getInstruction());
        // dwfProxy.setUpper8IOStates(controlModel.getInstruction().getBits());

        break;

      default:
        break;
    }
  }

  @Override
  public Model getControlModel() {

    return controlModel;
  }

  @Override
  public View getControlPanel() {

    return controlPanel;
  }

  @Override
  public Model getResultModel() {
    return resultModel;
  }

  @Override
  public ExperimentResultsPanel getResultPanel() {

    return resultPanel;
  }

  private class ClearChartWorker extends SwingWorker<Boolean, Double> {

    @Override
    protected Boolean doInBackground() throws Exception {

      resultController.resetChart();
      return true;
    }
  }

  private class CaptureWorker extends SwingWorker<Boolean, Double> {

    @Override
    protected Boolean doInBackground() throws Exception {

      aHaHController.executeInstruction(controlModel.getInstruction());
      while (!isCancelled()) {

        try {
          Thread.sleep(controlModel.getSampleRate() * 1000);
        } catch (InterruptedException e) {
          // eat it. caught when interrupt is called

        }

        // set to constant pulse width for reads to help mitigate RC issues
        int pW = controlModel.getPulseWidth();
        controlModel.setPulseWidth(100_000);
        aHaHController.executeInstruction(Instruction.FFLV);
        controlModel.setPulseWidth(pW); // set it back to whatever it was

        // System.out.println("Vy=" + aHaHController.getVy());
        publish(aHaHController.getGa(), aHaHController.getGb(), aHaHController.getVy());
      }
      return true;
    }

    @Override
    protected void process(List<Double> chunks) {

      resultController.updateYChartData(chunks.get(0), chunks.get(1), chunks.get(2));
      resultController.repaintYChart();
    }
  }

  private class InitSynapseWorker extends SwingWorker<Boolean, Double> {

    @Override
    protected Boolean doInBackground() throws Exception {

      try {
        int initPulseWidth = controlModel.getPulseWidth();
        float initVoltage = controlModel.getAmplitude();

        // 1. Reset both memristors to low conducting state-->
        controlModel.setPulseWidth(500_000); // 500us.
        controlModel.setAmplitude(1.5f);

        // hard reset.
        for (int i = 0; i < 10; i++) {
          aHaHController.executeInstruction(Instruction.RLadn);
          aHaHController.executeInstruction(Instruction.RHbdn);
        }

        // drive each memristor to target conductance.
        boolean aGood = false;
        boolean bGood = false;
        for (int i = 0; i < 100; i++) { // drive pulses until conductance reaches threshold
          aHaHController.executeInstruction(Instruction.FFLV);
          if (Double.isNaN(aHaHController.getGa()) || aHaHController.getGa() < INIT_CONDUCTANCE) {
            aHaHController.executeInstruction(Instruction.RHaup);
          } else {

            // System.out.println("Here A: aHaHController.getGa()=" + aHaHController.getGa());

            aGood = true;
          }

          if (Double.isNaN(aHaHController.getGb()) || aHaHController.getGb() < INIT_CONDUCTANCE) {
            aHaHController.executeInstruction(Instruction.RLbup);
          } else {

            // System.out.println("Here B: aHaHController.getGb()=" + aHaHController.getGb());

            bGood = true;
          }

          if (aGood && bGood) {

            getControlModel()
                .swingPropertyChangeSupport
                .firePropertyChange(Model.EVENT_NEW_CONSOLE_LOG, null, "Step 1 Passed.");
            break;
          }

          getControlModel()
              .swingPropertyChangeSupport
              .firePropertyChange(
                  Model.EVENT_NEW_CONSOLE_LOG,
                  null,
                  "  A="
                      + df.format(aHaHController.getGa())
                      + "mS, B="
                      + df.format(aHaHController.getGb())
                      + "mS");
        }

        if (!aGood || !bGood) { // failed

          String failed = "";
          if (!aGood) {
            failed += "A";
          }

          if (!bGood) {
            if (failed.length() > 0) {
              failed += ", B";
            } else {
              failed += "B";
            }
          }

          getControlModel()
              .swingPropertyChangeSupport
              .firePropertyChange(
                  Model.EVENT_NEW_CONSOLE_LOG,
                  null,
                  "Step 1 Failed. Memristor ("
                      + failed
                      + ") could not reach target of "
                      + INIT_CONDUCTANCE
                      + "mS");
        }

        // zero them out.
        int stateChangedCount = 0;
        boolean s = true;
        for (int i = 0; i < 8; i++) {
          aHaHController.executeInstruction(Instruction.FFLV);

          boolean state = aHaHController.getVy() > 0;
          if (i > 0) {
            if (state != s) { // state changed
              stateChangedCount++;
            }
          }
          s = state;
          aHaHController.executeInstruction(Instruction.FF_RA);
        }

        if (stateChangedCount == 0) {
          getControlModel()
              .swingPropertyChangeSupport
              .firePropertyChange(
                  Model.EVENT_NEW_CONSOLE_LOG,
                  null,
                  "Step 2 Failed. State did not change upon application of Anti-Hebbian cycles");
        } else {

          float a = (float) (stateChangedCount / 7.0);

          getControlModel()
              .swingPropertyChangeSupport
              .firePropertyChange(
                  Model.EVENT_NEW_CONSOLE_LOG, null, "Step 2 Passed. Q=" + df.format(a));
        }

        if (aGood && bGood && stateChangedCount > 0) {
          getControlModel()
              .swingPropertyChangeSupport
              .firePropertyChange(
                  Model.EVENT_NEW_CONSOLE_LOG, null, "Synapse Initialized Sucessfully");
        }

        getControlModel()
            .swingPropertyChangeSupport
            .firePropertyChange(
                Model.EVENT_NEW_CONSOLE_LOG,
                null,
                "  A="
                    + df.format(aHaHController.getGa())
                    + "mS, B="
                    + df.format(aHaHController.getGb())
                    + "mS");

        /*
         * drive both memristors up in conductance until one or the other exceeds target. drive the remaining memristor up until it exceeds the other
         * memristors or time runs out.
         */

        controlModel.setPulseWidth(initPulseWidth);
        controlModel.setAmplitude(initVoltage);

      } catch (Exception e) {
        e.printStackTrace();
      }

      return true;
    }
  }
}
