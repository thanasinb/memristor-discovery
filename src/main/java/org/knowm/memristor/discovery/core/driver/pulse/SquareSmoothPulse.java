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
package org.knowm.memristor.discovery.core.driver.pulse;

import org.knowm.memristor.discovery.core.driver.PulseDriver;

public class SquareSmoothPulse extends PulseDriver {

  private double dYdt;
  private double riseTime;
  private double fallTime;

  public SquareSmoothPulse(
      String id, double dcOffset, double pulseWidthInNS, double dutyCycle, double amplitude) {
    super(id, dcOffset, pulseWidthInNS, dutyCycle, amplitude);

    this.riseTime = this.pulseWidth * .1;
    this.dYdt = this.amplitude / riseTime;
    this.fallTime = this.pulseWidth - this.riseTime;

    System.out.println("riseTime=" + riseTime);
    System.out.println("dYdt=" + dYdt);
    System.out.println("fallTime=" + fallTime);
    System.out.println("pulseWidth=" + pulseWidth);
  }

  @Override
  public double getSignal(double time) {
    double t = time % getPeriod();
    if (t < riseTime) {
      return dcOffset + t * dYdt;
    } else if (t > riseTime && t < fallTime) {
      return amplitude + dcOffset;
    } else if (t > fallTime && t < pulseWidth) {
      return dcOffset + amplitude - (t - fallTime) * dYdt;
    } else {
      return dcOffset;
    }
  }
}
