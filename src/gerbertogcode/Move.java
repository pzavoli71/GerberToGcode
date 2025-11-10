/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gerbertogcode;

import static java.lang.Float.NaN;

/**
 *
 * @author vojta3310
 */
public class Move {

  private float toX = NaN;
  private float toY = NaN;
  private float ofX = 0;
  private float ofY = 0;
  private float toZ = NaN;
  private float scale = 1;
  private float mirX = 1;
  private float mirY = 1;
  private int F = 1500;
  
  public Move(float toX, float toY) {
    this.toX = toX;
    this.toY = toY;
  }

  public Move(float toZ) {
    this.toZ = toZ;
  }

  public void scale(float s) {
    scale = s;
  }

  public void mirror(float x, float y) {
    mirX = x;
    mirY = y;
  }

  public void ofset(float x, float y) {
    ofX = x;
    ofY = y;
  }

  StringBuilder toGcode() {
    StringBuilder a = new StringBuilder("G1");
    if (!Float.isNaN(toZ)) {
      a.append(" Z");
      a.append(Float.toString(getToZ()));
      a.append(" F300.00\n");
    } else {
      a.append(" X");
      a.append(Float.toString(getToX()));
      a.append(" Y");
      a.append(Float.toString(getToY()));
      a.append(" F" + Converter.F + ".00\n");
    }
    return a;
  }

  public float getToX() {
      if ( mirX == -1 && Converter.maxx != -Float.MAX_VALUE)
          return (toX + ofX) * scale * mirX + Converter.maxx - Converter.minx;
    return (toX + ofX) * scale * mirX;
  }

  public float getToY() {
    return (toY + ofY) * scale * mirY;
  }

  public float getToZ() {
    return toZ * scale;
  }
}
