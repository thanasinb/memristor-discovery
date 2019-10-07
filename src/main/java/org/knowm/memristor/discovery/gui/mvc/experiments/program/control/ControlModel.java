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

import org.knowm.memristor.discovery.core.Util;
import org.knowm.memristor.discovery.core.driver.Driver;
import org.knowm.memristor.discovery.core.driver.pulse.*;
import org.knowm.memristor.discovery.core.driver.waveform.Sawtooth;
import org.knowm.memristor.discovery.core.rc_engine.RC_ResistanceComputer;
import org.knowm.memristor.discovery.gui.mvc.experiments.ExperimentPreferences;
import org.knowm.memristor.discovery.gui.mvc.experiments.Model;
import org.knowm.memristor.discovery.gui.mvc.experiments.program.ProgramPreferences;

import java.text.DecimalFormat;

public class ControlModel extends Model {

  private final DecimalFormat ohmFormatter = new DecimalFormat("#,### Ω");
  private final double[] waveformTimeData = new double[ProgramPreferences.CAPTURE_BUFFER_SIZE];
  private final double[] waveformAmplitudeData = new double[ProgramPreferences.CAPTURE_BUFFER_SIZE];
  private final double[] reverseWaveformTimeData = new double[ProgramPreferences.CAPTURE_BUFFER_SIZE];
  private final double[] reverseWaveformAmplitudeData = new double[ProgramPreferences.CAPTURE_BUFFER_SIZE];

  public ProgramPreferences.Waveform waveform;
  private boolean isMemristorVoltageDropSelected = false;
  private float amplitude, reverseAmplitude;
  private int pulseWidth, reversePulseWidth; // model store pulse width in nanoseconds
  private double dutyCycle, reverseDutyCycle; // 0 to 1.
  private int pulseNumber, reversePulseNumber;
//  private String debugMsg;
  // private double appliedAmplitude;

  private double appliedCurrent;
  private double appliedEnergy;
  //  private double appliedMemristorEnergy;
  private double lastG;
  private int sampleRate;

  private boolean isStartToggled = false;

  private double readPulseWidth = 25E-6;
  private double readPulseAmplitude = .1;
  private double parasiticReadCapacitance = 140E-12;

  // used to compute resistance give read pulse voltage and takes into account parasitic capacitance
  // by using a board circuit model
  private RC_ResistanceComputer rcComputer;

  private int boardVersion;

  /** Constructor */
  public ControlModel(int boardVersion) {
    this.boardVersion = boardVersion;
    if (boardVersion == 2) {
      readPulseAmplitude = -readPulseAmplitude;
    }
  }

  @Override
  public void doLoadModelFromPrefs(ExperimentPreferences experimentPreferences) {

    waveform =
        ProgramPreferences.Waveform.valueOf(
            experimentPreferences.getString(
                ProgramPreferences.WAVEFORM_INIT_STRING_KEY,
                ProgramPreferences.WAVEFORM_INIT_STRING_DEFAULT_VALUE));
    seriesResistance =
        experimentPreferences.getInteger(
            ProgramPreferences.SERIES_R_INIT_KEY, ProgramPreferences.SERIES_R_INIT_DEFAULT_VALUE);
    targetResistance =
            experimentPreferences.getInteger(
                    ProgramPreferences.TARGET_R_INIT_KEY, ProgramPreferences.TARGET_R_INIT_DEFAULT_VALUE);
    upperResistance = (int) (targetResistance*1.05);
    lowerResistance = (int) (targetResistance*0.95);

    amplitude =
        experimentPreferences.getFloat(
            ProgramPreferences.AMPLITUDE_INIT_FLOAT_KEY,
            ProgramPreferences.AMPLITUDE_INIT_FLOAT_DEFAULT_VALUE);
    // appliedAmplitude = amplitude;
    pulseWidth =
        experimentPreferences.getInteger(
            ProgramPreferences.PULSE_WIDTH_INIT_KEY, ProgramPreferences.PULSE_WIDTH_INIT_DEFAULT_VALUE);
    pulseNumber =
        experimentPreferences.getInteger(
            ProgramPreferences.NUM_PULSES_INIT_KEY, ProgramPreferences.NUM_PULSES_INIT_DEFAULT_VALUE);
    dutyCycle =
        experimentPreferences.getFloat(
            ProgramPreferences.PULSE_DUTY_CYCLE_KEY, ProgramPreferences.PULSE_DUTY_CYCLE_DEFAULT_VALUE);

    sampleRate =
            experimentPreferences.getInteger(
                    ProgramPreferences.SAMPLE_RATE_INIT_KEY, ProgramPreferences.SAMPLE_RATE_INIT_DEFAULT_VALUE);

    reverseAmplitude =
            experimentPreferences.getFloat(
                    ProgramPreferences.REVERSE_AMPLITUDE_INIT_FLOAT_KEY,
                    ProgramPreferences.REVERSE_AMPLITUDE_INIT_FLOAT_DEFAULT_VALUE);
    reversePulseWidth =
            experimentPreferences.getInteger(
                    ProgramPreferences.REVERSE_PULSE_WIDTH_INIT_KEY, ProgramPreferences.REVERSE_PULSE_WIDTH_INIT_DEFAULT_VALUE);
    reversePulseNumber =
            experimentPreferences.getInteger(
                    ProgramPreferences.REVERSE_NUM_PULSES_INIT_KEY, ProgramPreferences.REVERSE_NUM_PULSES_INIT_DEFAULT_VALUE);
    reverseDutyCycle =
            experimentPreferences.getFloat(
                    ProgramPreferences.REVERSE_PULSE_DUTY_CYCLE_KEY, ProgramPreferences.REVERSE_PULSE_DUTY_CYCLE_DEFAULT_VALUE);

    updateWaveformChartData();

    rcComputer =
        new RC_ResistanceComputer(
            boardVersion,
            readPulseAmplitude,
            readPulseWidth,
            seriesResistance,
            parasiticReadCapacitance);
  }

  /** Given the state of the model, update the waveform x and y axis data arrays. */
  void updateWaveformChartData() {

    Driver driver, reverseDriver;
    switch (waveform) {
      case Sawtooth:
        driver =
            new Sawtooth("Sawtooth", amplitude / 2, 0, amplitude / 2, getCalculatedFrequency());
        reverseDriver =
                new Sawtooth("Sawtooth", reverseAmplitude / 2, 0, reverseAmplitude / 2, getReverseCalculatedFrequency());
        break;
      case QuarterSine:
        driver = new QuarterSinePulse("QuarterSine", 0, pulseWidth, dutyCycle, amplitude);
        reverseDriver = new QuarterSinePulse("QuarterSine", 0, reversePulseWidth, reverseDutyCycle, reverseAmplitude);
        break;
      case Triangle:
        driver = new TrianglePulse("Triangle", 0, pulseWidth, dutyCycle, amplitude);
        reverseDriver = new TrianglePulse("Triangle", 0, reversePulseWidth, reverseDutyCycle, reverseAmplitude);
        break;
      case Square:
        driver = new SquarePulse("Square", 0, pulseWidth, dutyCycle, amplitude);
        reverseDriver = new SquarePulse("Square", 0, reversePulseWidth, reverseDutyCycle, reverseAmplitude);
        break;
      case SquareSmooth:
        driver = new SquareSmoothPulse("SquareSmooth", 0, pulseWidth, dutyCycle, amplitude);
        reverseDriver = new SquareSmoothPulse("SquareSmooth", 0, reversePulseWidth, reverseDutyCycle, reverseAmplitude);
        break;
      case SquareDecay:
        driver = new SquareDecayPulse("SquareDecay", 0, pulseWidth, dutyCycle, amplitude);
        reverseDriver = new SquareDecayPulse("SquareDecay", 0, reversePulseWidth, reverseDutyCycle, reverseAmplitude);
        break;
      case SquareLongDecay:
        driver = new SquareLongDecayPulse("SquareLongDecay", 0, pulseWidth, dutyCycle, amplitude);
        reverseDriver = new SquareLongDecayPulse("SquareLongDecay", 0, reversePulseWidth, reverseDutyCycle, reverseAmplitude);
        break;
      default:
        driver = new HalfSinePulse("HalfSine", 0, pulseWidth, dutyCycle, amplitude);
        reverseDriver = new HalfSinePulse("HalfSine", 0, reversePulseWidth, reverseDutyCycle, reverseAmplitude);
        break;
    }

    double stopTime = driver.getPeriod() * pulseNumber;
    double timeStep = driver.getPeriod() / ProgramPreferences.CAPTURE_BUFFER_SIZE * pulseNumber;

    //    System.out.println("driver.getPeriod()=" + driver.getPeriod());
    //    System.out.println("stopTime=" + stopTime);
    //    System.out.println("timeStep=" + timeStep);

    int counter = 0;
    for (double i = 0.0; i < stopTime; i = i + timeStep) {
      if (counter >= ProgramPreferences.CAPTURE_BUFFER_SIZE) {
        break;
      }
      waveformTimeData[counter] = i * ProgramPreferences.TIME_UNIT.getDivisor();
      waveformAmplitudeData[counter++] = driver.getSignal(i);
    }

    double reverseStopTime = reverseDriver.getPeriod() * reversePulseNumber;
    double reverseTimeStep = reverseDriver.getPeriod() / ProgramPreferences.CAPTURE_BUFFER_SIZE * reversePulseNumber;

    //    System.out.println("driver.getPeriod()=" + driver.getPeriod());
    //    System.out.println("stopTime=" + stopTime);
    //    System.out.println("timeStep=" + timeStep);

    counter = 0;
    for (double i = 0.0; i < reverseStopTime; i = i + reverseTimeStep) {
      if (counter >= ProgramPreferences.CAPTURE_BUFFER_SIZE) {
        break;
      }
      reverseWaveformTimeData[counter] = i * ProgramPreferences.TIME_UNIT.getDivisor();
      reverseWaveformAmplitudeData[counter++] = reverseDriver.getSignal(i);
    }
  }

  /////////////////////////////////////////////////////////////
  // GETTERS AND SETTERS //////////////////////////////////////
  /////////////////////////////////////////////////////////////

  public void setSeriesResistance(int seriesResistance) {

    this.seriesResistance = seriesResistance;
    rcComputer =
        new RC_ResistanceComputer(
            boardVersion,
            readPulseAmplitude,
            readPulseWidth,
            seriesResistance,
            parasiticReadCapacitance);
  }

  public float getAmplitude() {

    return amplitude;
  }

  public void setAmplitude(float amplitude) {

    this.amplitude = amplitude;
    swingPropertyChangeSupport.firePropertyChange(Model.EVENT_WAVEFORM_UPDATE, true, false);
  }

  public float getReverseAmplitude() {

    return reverseAmplitude;
  }

  public void setReverseAmplitude(float reverseAmplitude) {

    this.reverseAmplitude = reverseAmplitude;
    swingPropertyChangeSupport.firePropertyChange(Model.EVENT_WAVEFORM_UPDATE, true, false);
  }

  public int getPulseWidth() {

    return pulseWidth;
  }

  public void setPulseWidth(int pulseWidth) {

    this.pulseWidth = pulseWidth;
    swingPropertyChangeSupport.firePropertyChange(Model.EVENT_WAVEFORM_UPDATE, true, false);
  }

  public int getReversePulseWidth() {

    return reversePulseWidth;
  }

  public void setReversePulseWidth(int reversePulseWidth) {

    this.reversePulseWidth = reversePulseWidth;
    swingPropertyChangeSupport.firePropertyChange(Model.EVENT_WAVEFORM_UPDATE, true, false);
  }

//  public void setDebugMsg(String debugMsg) {
//
//    this.debugMsg = debugMsg;
//    swingPropertyChangeSupport.firePropertyChange(Model.EVENT_WAVEFORM_UPDATE, true, false);
//  }

  public double getCalculatedFrequency() {
    return (1.0 / (pulseWidth / dutyCycle) * 1_000_000_000); // 50% duty cycle
  }
  public double getReverseCalculatedFrequency() {
    return (1.0 / (reversePulseWidth / reverseDutyCycle) * 1_000_000_000); // 50% duty cycle
  }

  public double[] getWaveformTimeData() {

    return waveformTimeData;
  }

  public double[] getReverseWaveformTimeData() {

    return reverseWaveformTimeData;
  }

  public double[] getWaveformAmplitudeData() {

    return waveformAmplitudeData;
  }

  public double[] getReverseWaveformAmplitudeData() {

    return reverseWaveformAmplitudeData;
  }

  public int getPulseNumber() {

    return pulseNumber;
  }

  public void setPulseNumber(int pulseNumber) {

    this.pulseNumber = pulseNumber;
    swingPropertyChangeSupport.firePropertyChange(Model.EVENT_WAVEFORM_UPDATE, true, false);
  }

  public int getReversePulseNumber() {

    return reversePulseNumber;
  }

  public void setReversePulseNumber(int reversePulseNumber) {

    this.reversePulseNumber = reversePulseNumber;
    swingPropertyChangeSupport.firePropertyChange(Model.EVENT_WAVEFORM_UPDATE, true, false);
  }

  public boolean isMemristorVoltageDropSelected() {

    return isMemristorVoltageDropSelected;
  }

  public void setMemristorVoltageDropSelected(boolean memristorVoltageDropSelected) {

    isMemristorVoltageDropSelected = memristorVoltageDropSelected;
    swingPropertyChangeSupport.firePropertyChange(Model.EVENT_WAVEFORM_UPDATE, true, false);
  }

  //  public double getAppliedAmplitude() {
  //
  //    return appliedAmplitude;
  //  }

  public ProgramPreferences.Waveform getWaveform() {

    return waveform;
  }

  public void setWaveform(ProgramPreferences.Waveform waveform) {

    this.waveform = waveform;
    swingPropertyChangeSupport.firePropertyChange(Model.EVENT_WAVEFORM_UPDATE, true, false);
  }

  public void setWaveform(String text) {

    waveform = Enum.valueOf(ProgramPreferences.Waveform.class, text);
    swingPropertyChangeSupport.firePropertyChange(Model.EVENT_WAVEFORM_UPDATE, true, false);
  }

  public int getSampleRate() {
    return sampleRate;
  }

  public void setSampleRate(int sampleRate) {
    this.sampleRate = sampleRate;
  }

  public double getLastG() {

    return lastG;
  }

  public void setLastG(double lastG) {

    this.lastG = lastG;
  }

  public double getLastR() {

    return 1.0 / lastG * ProgramPreferences.CONDUCTANCE_UNIT.getDivisor();
  }

  public double getAppliedCurrent() {

    return appliedCurrent;
  }

  public double getAppliedEnergy() {

    return appliedEnergy;
  }

  //  public double getAppliedMemristorEnergy() {
  //
  //    return appliedMemristorEnergy;
  //  }

  public String getLastRAsString() {

    return ohmFormatter.format(getLastR());
  }

  public String getLastGAsString() {

    return String.valueOf(getLastG()* ProgramPreferences.CONDUCTANCE_UNIT.getDivisor()).split("\\.")[0]+" uS";
  }

  public String getUpperRAsString() {
    return ohmFormatter.format(getUpperResistance());
  }

  public String getLowerRAsString() {
    return ohmFormatter.format(getLowerResistance());
  }

  public void updateEnergyData() {

    // calculate applied voltage
    // if (lastG > 0.0) {
    // this.appliedAmplitude = amplitude;

    this.appliedCurrent =
        amplitude
            / (getLastR() + seriesResistance + Util.getSwitchesSeriesResistance())
            * ProgramPreferences.CURRENT_UNIT.getDivisor();
    this.appliedEnergy =
        amplitude
            * amplitude
            / (getLastR() + seriesResistance + Util.getSwitchesSeriesResistance())
            * pulseNumber
            * pulseWidth
            / 1E9;
  }

  public boolean isStartToggled() {

    return isStartToggled;
  }

  public void setStartToggled(boolean isStartToggled) {

    this.isStartToggled = isStartToggled;
  }

  public double getDutyCycle() {
    return dutyCycle;
  }

  public void setDutyCycle(double dutyCycle) {
    this.dutyCycle = dutyCycle;
    swingPropertyChangeSupport.firePropertyChange(Model.EVENT_WAVEFORM_UPDATE, true, false);
  }

  public double getReverseDutyCycle() {
    return reverseDutyCycle;
  }

  public void setReverseDutyCycle(double reverseDutyCycle) {
    this.reverseDutyCycle = reverseDutyCycle;
    swingPropertyChangeSupport.firePropertyChange(Model.EVENT_WAVEFORM_UPDATE, true, false);
  }

  public double getReadPulseWidth() {
    return readPulseWidth;
  }

  public void setReadPulseWidth(double readPulseWidth) {
    this.readPulseWidth = readPulseWidth;
  }

  public double getReadPulseAmplitude() {
    return readPulseAmplitude;
  }

  public void setReadPulseAmplitude(double readPulseAmplitude) {
    this.readPulseAmplitude = readPulseAmplitude;
  }

  public double getParasiticReadCapacitance() {
    return parasiticReadCapacitance;
  }

  public void setParasiticReadCapacitance(double parasiticReadCapacitance) {
    this.parasiticReadCapacitance = parasiticReadCapacitance;
  }

  public RC_ResistanceComputer getRcComputer() {
    return rcComputer;
  }
}
