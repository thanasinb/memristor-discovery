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
package org.knowm.memristor.discovery.gui.mvc.experiments.conductance.result;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.knowm.memristor.discovery.gui.mvc.experiments.conductance.ConductancePreferences;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.markers.SeriesMarkers;

public class ResultPanel extends JPanel {

  private final JPanel radioPanel;
  private final ButtonGroup radioButtonGroup;
  private final JRadioButton captureButton;
  private final JRadioButton ivButton;
  private final JRadioButton gvButton;
  private final JCheckBox freezeYAxisCheckBoxIV;
  private final JPanel gvChartControlPanel;
  private final JCheckBox freezeYAxisCheckBoxGV;
  XYChart waveformChart;
  XChartPanel<XYChart> waveformChartPanel;
  XYChart captureChart;
  XChartPanel<XYChart> captureChartPanel;
  XYChart ivChart;
  XChartPanel<XYChart> ivChartPanel;
  XYChart gvChart;
  XChartPanel<XYChart> gvChartPanel;

  /** Constructor */
  public ResultPanel() {

    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

    // ///////////////////////////////////////////////////////////
    // Waveform Chart ///////////////////////////////////////////
    // ///////////////////////////////////////////////////////////

    waveformChart =
        new XYChartBuilder()
            .width(400)
            .height(400)
            .title("Waveform")
            .yAxisTitle("Voltage [V]")
            .xAxisTitle("Time [µs]")
            .build();
    waveformChart.getStyler().setLegendVisible(false);
    XYSeries series = waveformChart.addSeries("waveform", new double[] {0}, new double[] {0});
    series.setMarker(
        SeriesMarkers
            .NONE); // waveformChart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
    waveformChartPanel = new XChartPanel<>(waveformChart);
    add(waveformChartPanel, BorderLayout.CENTER);

    // ///////////////////////////////////////////////////////////
    // Capture Chart ////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////

    captureChart =
        new XYChartBuilder()
            .width(600)
            .title("Capture")
            .height(400)
            .yAxisTitle("Voltage [V]")
            .xAxisTitle("Time [µs]")
            .build();
    captureChart.getStyler().setLegendPosition(LegendPosition.InsideNE);

    series = captureChart.addSeries("V1", new double[] {0}, new double[] {0});
    series.setMarker(SeriesMarkers.NONE);

    series = captureChart.addSeries("V2", new double[] {0}, new double[] {0});
    series.setMarker(SeriesMarkers.NONE);

    captureChartPanel = new XChartPanel<>(captureChart);

    // ///////////////////////////////////////////////////////////
    // I-T Chart ////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////

    ivChart =
        new XYChartBuilder()
            .width(600)
            .title("I-T")
            .height(400)
            .yAxisTitle("Current [" + ConductancePreferences.CURRENT_UNIT.getLabel() + "]")
            .xAxisTitle("Voltage [V]")
            .build();
    ivChart.getStyler().setLegendVisible(false);
    ivChart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);

    series = ivChart.addSeries("iv", new double[] {0}, new double[] {0});

    ivChartPanel = new XChartPanel<>(ivChart);

    // ///////////////////////////////////////////////////////////
    // G-T Chart ////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////

    gvChart =
        new XYChartBuilder()
            .width(100)
            .title("G-T")
            .height(100)
            .yAxisTitle("Conductance [" + ConductancePreferences.CONDUCTANCE_UNIT.getLabel() + "]")
            .xAxisTitle("Voltage [V]")
            .build();
    gvChart.getStyler().setLegendVisible(false);
    gvChart.getStyler().setYAxisMin(0.0);

    // rvChart.getStyler().setYAxisMax(200.0);

    gvChart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);

    series = gvChart.addSeries("gv", new double[] {0}, new double[] {0});

    gvChartPanel = new XChartPanel<>(gvChart);

    // ///////////////////////////////////////////////////////////
    // Radio Buttons ////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////

    radioPanel = new JPanel();
    captureButton = new JRadioButton("Capture");
    ivButton = new JRadioButton("I-V");
    gvButton = new JRadioButton("G-V");
    radioButtonGroup = new ButtonGroup();
    addRadioButtons();

    // ///////////////////////////////////////////////////////////
    // Check Box ////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////

    freezeYAxisCheckBoxIV = new JCheckBox("Freeze Y-Axis");

    gvChartControlPanel = new JPanel();
    freezeYAxisCheckBoxGV = new JCheckBox("Freeze Y-Axis");

    gvChartControlPanel.add(freezeYAxisCheckBoxGV);
  }

  private void addRadioButtons() {

    radioButtonGroup.add(captureButton);
    radioButtonGroup.add(ivButton);
    radioButtonGroup.add(gvButton);
    radioPanel.add(captureButton);
    radioPanel.add(ivButton);
    radioPanel.add(gvButton);
    add(radioPanel, BorderLayout.SOUTH);
  }

  private void addYAxisFreezeCheckBoxIV() {

    add(freezeYAxisCheckBoxIV, BorderLayout.NORTH);
  }

  private void addChartControlGV() {

    // add(freezeYAxisCheckBoxGV, BorderLayout.NORTH);
    add(gvChartControlPanel, BorderLayout.NORTH);
  }

  public void switch2WaveformChart() {

    if (!waveformChartPanel.isShowing()) {
      // System.out.println("switch2WaveformChart");
      removeAll();
      add(waveformChartPanel, BorderLayout.CENTER);
      addRadioButtons();
      revalidate();
      repaint();
    }
  }

  public void switch2CaptureChart() {

    if (!captureChartPanel.isShowing()) {
      // System.out.println("switch2CaptureChart");
      removeAll();
      add(captureChartPanel, BorderLayout.CENTER);
      addRadioButtons();
      revalidate();
      repaint();
    }
  }

  public void switch2IVChart() {

    if (!ivChartPanel.isShowing()) {
      // System.out.println("switch2IVChart");
      removeAll();
      add(ivChartPanel, BorderLayout.CENTER);
      addRadioButtons();
      addYAxisFreezeCheckBoxIV();
      revalidate();
      repaint();
    }
  }

  public void switch2GVChart() {

    if (!gvChartPanel.isShowing()) {
      // System.out.println("switch2GVChart");
      removeAll();
      add(gvChartPanel, BorderLayout.CENTER);
      addRadioButtons();
      addChartControlGV();
      captureButton.setSelected(false);
      ivButton.setSelected(false);
      gvButton.setSelected(true);
      revalidate();
      repaint();
    }
  }

  public JRadioButton getCaptureButton() {

    return captureButton;
  }

  public JRadioButton getIVButton() {

    return ivButton;
  }

  public JRadioButton getGVButton() {

    return gvButton;
  }

  public JCheckBox getFreezeYAxisCheckBoxIV() {

    return freezeYAxisCheckBoxIV;
  }

  public JCheckBox getFreezeYAxisCheckBoxGV() {

    return freezeYAxisCheckBoxGV;
  }

  public double getIVChartMax() {

    return ivChart.getSeriesMap().get("iv").getYMax();
  }

  public double getIVChartMin() {

    return ivChart.getSeriesMap().get("iv").getYMin();
  }

  public double getGVChartMax() {

    return gvChart.getSeriesMap().get("gv").getYMax();
  }

  public double getGVChartMin() {

    return gvChart.getSeriesMap().get("gv").getYMin();
  }

  public JPanel getRadioPanel() {

    return radioPanel;
  }

  public XYChart getWaveformChart() {

    return waveformChart;
  }

  public XChartPanel<XYChart> getWaveformChartPanel() {

    return waveformChartPanel;
  }

  public XYChart getCaptureChart() {

    return captureChart;
  }

  public XChartPanel<XYChart> getCaptureChartPanel() {

    return captureChartPanel;
  }

  public XYChart getIvChart() {

    return ivChart;
  }

  public XChartPanel<XYChart> getIvChartPanel() {

    return ivChartPanel;
  }

  public XYChart getGvChart() {

    return gvChart;
  }

  public XChartPanel<XYChart> getGvChartPanel() {

    return gvChartPanel;
  }
}
