/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gerbertogcode;

/**
 *
 * @author vojta3310
 */
public class Tool {

  private float d = 0.9f;
  private float h = 0.9f;
  private float w = 0.9f;
  private char s = 'C';
  private float f = 0.8f;  

  public Tool(char shape, float d) {
    this.d = d;
    this.s = shape;
  }

  public Tool(char shape, float w, float h) {
    this.s = shape;
    this.d = (float) Math.sqrt(h * h + w * w);
    this.h = h;
    this.w = w;
  }

  public void setForo(float foro) {
      this.f = foro;
  }
  public float getD() {
    return d;
  }

  public float getH() {
    return h;
  }

  public float getW() {
    return w;
  }

  public char getS() {
    return s;
  }

  public float getForo() {
    return f;
  }

}
