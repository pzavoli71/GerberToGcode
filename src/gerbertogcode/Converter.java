/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gerbertogcode;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author vojta3310
 */
public class Converter {

  private float penSize = 0.2f;
  private float zUp = 3;
  public static int F = 1500;
  public static float penDown = -0.2f;
  public static float minx = 0.0f;
  public static float miny = 0.0f;
  public static float maxx = 80.0f;
  public static float maxy = 80.0f;
  
  private boolean bebug = false;
  private boolean show = false;
  private boolean mirX = false;
  private boolean mirY = false;
  private int repeat=0;
  private float tolerance = 0.01f;
  private float scale = 1;
  private String drillFile = "";
  private String edgeFile = "";
  private String fileIn;
  private String fileOut ="out.gcode";
  

  public void convert() {
    Mover mo = new Mover();
    mo.setPenSize(penSize);
    mo.setScale(scale);
    mo.setTolerance(tolerance);
    mo.setzUp(zUp);
    mo.setRepeat(repeat);
    mo.setF(F);
    mo.setDrillFile(drillFile);
    
    //mo.setF
    if (mirX) mo.setMirX(-1);
    if (mirY) mo.setMirY(-1);
    Parser pa = new Parser();
    pa.setDx(minx);
    pa.setDy(miny);
    pa.setBebug(bebug);
    pa.parseFile(fileIn, mo);
    mo.saveGcode(fileOut);
    if (show) {
      JFrame fr = new JFrame("G2G - Show: "+fileIn);
      fr.setVisible(true);
      fr.setSize(1000, 800);
      JPanel p = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
          g.setColor(Color.white);
          g.fillRect(0, 0, getWidth(), getHeight());
          mo.draw(g, getHeight(), getWidth());
        }
      };
      fr.add(p);
      fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
  }
  
  public void printOption() {
    System.out.println(""
      + "Settings:\n"
      + "   penSize:    "+penSize
      + "\n   penUp:      "+zUp
      + "\n   penDown:      "+penDown
      + "\n   F:      " + F
      + "\n   dx:      " + minx
      + "\n   dy:      "+miny            
      + "\n   precision:  "+tolerance
      + "\n   scale:      "+scale
      + "\n   mirror X:   "+mirX
      + "\n   mirror Y:   "+mirY
      + "\n   repeat:     "+repeat
      + "\n   show:       "+show
      + "\n   drill:       "+drillFile
      + "\n   edge:       "+edgeFile
      + "\n   debug:      "+bebug);
  }

  public void setShow(boolean show) {
    this.show = show;
  }

  public void setPenSize(float penSize) {
    this.penSize = penSize;
  }

  public void setTolerance(float tolerance) {
    this.tolerance = tolerance;
  }

  public void setScale(float scale) {
    this.scale = scale;
  }

  public void setMirX(boolean mirX) {
    this.mirX = mirX;
  }

  public void setMirY(boolean mirY) {
    this.mirY = mirY;
  }

  public void setzUp(float zUp) {
    this.zUp = zUp;
  }

  public void setBebug(boolean bebug) {
    this.bebug = bebug;
  }

  public void setFileIn(String fileIn) {
    this.fileIn = fileIn;
  }

  public void setFileOut(String fileOut) {
    this.fileOut = fileOut;
  }
  
  public void setDrillFile(String file) {
    this.drillFile = file;
  }

  public void setEdgeFile(String file) {
    this.edgeFile = file;
  }
  
  public void setRepeat(int repeat) {
    this.repeat = repeat;
  }
  public void setF(int F) {
    this.F = F;
  }
  public void setPenDown(float pd) {
    Converter.penDown = pd;
  }
  public void setMinx(float dx) {
    this.minx = dx;
  }
  public void setMiny(float dy) {
    this.miny = dy;
  }
  public void setMaxx(float dx) {
    this.maxx = dx;
  }
  public void setMaxy(float dy) {
    this.maxy = dy;
  }
  
}
