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
package org.knowm.memristor.discovery.gui.mvc.experiments;

import java.beans.PropertyChangeListener;
import javax.swing.event.SwingPropertyChangeSupport;

public abstract class Model {

  // Events
  public static final String EVENT_WAVEFORM_UPDATE = "EVENT_WAVEFORM_UPDATE";
  public static final String EVENT_FREQUENCY_UPDATE = "EVENT_FREQUENCY_UPDATE";
  public static final String EVENT_PREFERENCES_UPDATE = "EVENT_PREFERENCES_UPDATE";
  public static final String EVENT_NEW_CONSOLE_LOG = "EVENT_NEW_CONSOLE_LOG";

  /** runtime variables */
  public int seriesResistance, targetResistance;
  public int upperResistance, lowerResistance;

  public SwingPropertyChangeSupport swingPropertyChangeSupport;

  /** Constructor */
  public Model() {

    swingPropertyChangeSupport = new SwingPropertyChangeSupport(this);
  }

  public abstract void doLoadModelFromPrefs(ExperimentPreferences experimentPreferences);

  public void loadModelFromPrefs(ExperimentPreferences experimentPreferences) {

    doLoadModelFromPrefs(experimentPreferences);

    // this will communicate to the controllers that a preferences change has occurred
    swingPropertyChangeSupport.firePropertyChange(Model.EVENT_PREFERENCES_UPDATE, true, false);
  }

  public int getSeriesResistance() {

    return seriesResistance;
  }

  public void setSeriesResistance(int seriesResistance) {

    this.seriesResistance = seriesResistance;
  }

  public int getTargetResistance() {

    return targetResistance;
  }

  public int getUpperResistance() {

    return upperResistance;
  }

  public int getLowerResistance() {

    return lowerResistance;
  }

  public void setTargetResistance(int targetResistance) {

    this.targetResistance = targetResistance;
    this.upperResistance = (int) (targetResistance*1.05);
    this.lowerResistance = (int) (targetResistance*0.95);
  }


  /**
   * Here is where the Controller registers itself as a listener to model changes.
   *
   * @param listener
   */
  public void addListener(PropertyChangeListener listener) {

    swingPropertyChangeSupport.addPropertyChangeListener(listener);
  }
}
