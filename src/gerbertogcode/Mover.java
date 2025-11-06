/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gerbertogcode;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vojta3310
 */
public class Mover {

  private float penSize = 0.2f;
  private float tolerance = 0.01f;
  private float scale = 1;
  private float mirX = 1;
  private float mirY = 1;
  private float zUp = 3;
  private int repeat = 0;
  private int F = 1500;
  private String drillFile = "";

  private final LinkedList<Move> moves = new LinkedList();
  private float x = 0;
  private float y = 0;
  private boolean up = false;
  private Tool tool = new Tool('C', .3f);
  private float minX = Float.MAX_VALUE;
  private float maxX = 0;
  private float minY = Float.MAX_VALUE;
  private float maxY = 0;
  private float ofX = 0;
  private float ofY = 0;

  public void setTool(Tool t) {
    this.tool = t;
  }

  public void move(float x, float y, int how, float dhole) {
    if (Float.isNaN(x)) {
      x = this.x;
    }
    if (Float.isNaN(y)) {
      x = this.y;
    }
    switch (how) {
      case 1:
        if (up) {
          add(new Move(Converter.penDown));
          up = false;
        }
        lineTo(x, y);
        break;
      case 2:
        if (!up) {
          add(new Move(zUp));
          up = true;
        }
        add(new Move(x, y));
        break;
      case 3:
        if (!up) {
          add(new Move(zUp));
          up = true;
        }
        // QUi devo vedere se esiste un hole in corrispondenza di questo pad e passarlo alla drawShape
        drawShape(x, y, dhole);
        break;
    }
    this.x = x;
    this.y = y;
  }

  public StringBuilder toGcode() {
    StringBuilder a = new StringBuilder();
    a.append("G21 (metric ftw)\n");
    a.append("G90 (absolute mode)\n");
    //a.append("G92 X0 Y0 Z0 (you are here)\n");
    for (int i = 0; i < repeat + 1; ++i) {
      moves.forEach((Move m) -> {
        m.scale(scale);
        m.ofset(ofX*mirX, ofY*mirY);
        a.append(m.toGcode());
      });
      a.append("G1 Z").append(zUp * scale).append("\n");
      a.append("G1 X0.0 Y0.0\n");
    }
    // Disegno il bordo della scheda
      a.append(";").append("\n");      
      a.append("; Stampo il bordo della scheda").append("\n");      
      a.append("G1 Z").append(0).append("\n");      
      a.append("G1 X");
      a.append(Float.toString(Converter.maxx)).append("\n");      
      a.append("G1 Y");
      a.append(Float.toString(Converter.maxy)).append("\n");      
      a.append("G1 X");
      a.append(Float.toString(0.0f)).append("\n");      
      a.append("G1 Y");
      a.append(Float.toString(0.0f)).append("\n");      

      a.append("G1 X0.0 Y0.0\n");
    
    //a.append("M18\n");
    return a;
  }

  public void saveGcode(String path) {
    File file = new File(path);
    try (FileWriter fileWriter = new FileWriter(file)) {
      fileWriter.write(toGcode().toString());
      fileWriter.flush();
    } catch (IOException ex) {
      Logger.getLogger(Mover.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void draw(Graphics g2d, int h, int w) {
    h -= 10;
    w -= 10;
    float scaleGr = Math.min(w / Math.abs(maxX - minX), h / Math.abs(maxY - minY));
    float xl = 0, yl = 0, zl = 0;
    float xa, ya, za;
    boolean drawing = false;
    for (Move m : moves) {
      m.scale(scaleGr);
      m.ofset(ofX*mirX, ofY*mirY);
      xa = m.getToX();
      ya = m.getToY();
      za = m.getToZ();
      if (!Float.isNaN(za)) {
        drawing = (za < zl);
      } else {
        if (drawing) {
          g2d.setColor(Color.black);
        } else {
          g2d.setColor(Color.lightGray);
        }
        g2d.drawLine(5 + Math.round(xl), 5 + Math.round(yl), 5 + Math.round(xa), 5 + Math.round(ya));
      }
      if (!Float.isNaN(xa)) {
        xl = xa;
      }
      if (!Float.isNaN(ya)) {
        yl = ya;
      }
      if (!Float.isNaN(za)) {
        zl = za;
      }
    }
  }

  private void lineTo(float x, float y) {
    if (up) {
      add(new Move(0));
      up = false;
    }
    //line to xy
    add(new Move(x, y));

    float tx = x - this.x;
    float ty = y - this.y;
    float nx = -ty / (float) Math.sqrt(tx * tx + ty * ty);
    float ny = tx / (float) Math.sqrt(tx * tx + ty * ty);
    float txn = tx / (float) Math.sqrt(tx * tx + ty * ty);
    float tyn = ty / (float) Math.sqrt(tx * tx + ty * ty);

    float i = tool.getD() / (penSize * 2) - 0.5f;
    for (; i > 0; i--) {
      //line to side
      add(new Move(x + nx * penSize * i, y + ny * penSize * i));
      //line back around
      add(new Move(this.x + nx * penSize * i, this.y + ny * penSize * i));
      //round around - circle vith center in this.x this.y and r = i*penSize
      for (float ii = 0; ii <= i * penSize * 2; ii += tolerance) {
        float xr = i * penSize - ii;
        float yr = (float) Math.sqrt(Math.pow(i * penSize, 2) - Math.pow(xr, 2));
        float px = nx * xr - txn * yr + this.x;
        float py = ny * xr - tyn * yr + this.y;
        add(new Move(px, py));
      }
      add(new Move(this.x - nx * penSize * i, this.y - ny * penSize * i));
      //line to around
      add(new Move(x - nx * penSize * i, y - ny * penSize * i));
      //round around
      for (float ii = 0; ii <= i * penSize * 2; ii += tolerance) {
        float xr = i * penSize - ii;
        float yr = (float) Math.sqrt(Math.pow(i * penSize, 2) - Math.pow(xr, 2));
        float px = -nx * xr + txn * yr + x;
        float py = -ny * xr + tyn * yr + y;
        add(new Move(px, py));
      }
    }
    //back to center
    add(new Move(x, y));
  }
  
  private void drawSingleCircle(float x, float y, float rhole) {
    float xv = -rhole;
    boolean first = true;
    while ( xv <= rhole) { // xstart is negative, so xv varies from -xstart to xstart
        float yv = (float) Math.sqrt(Math.pow(rhole,2) - Math.pow(xv, 2));
        add(new Move(x+xv, y+yv));
        if ( first && up) {
            add(new Move(0));
            up = false;
            first = false;
        }
        xv += tolerance;
    }
    // Now I go from right to left in x and draw the bottom arc of circle
    xv = rhole;
    while ( xv >= -rhole) { // xstart is negative, so xv varies from -xstart to xstart
        float yv = -(float) Math.sqrt(Math.pow(rhole,2) - Math.pow(xv, 2));
        add(new Move(x+xv, y+yv));
        xv -= tolerance;
    }      
  }
  
  // Quando il Parser incontra una piazzola (Comando che termina con D03) inizia 
  // la creaazione della serie mi Moves che servono per disegnare la piazzole
  // che vengono aggiunte all'arraylist del disegno completo.
  // All'arrivo su questa funzione la testina è in up, quindi bisogno ricordarsi di mandarla a zero dopo il primo movimento.
  private void drawShape(float x, float y, float dhole) {
    float rhole = dhole / 2.0f;         // Radius of hole
    float rtool = tool.getD() / 2.0f;   // Radius of toolpad
    float rpensize = penSize / 2.0f;    // Radius of pen
    if (tool.getS() == 'C' || (tool.getS() == 'O' && (tool.getH() == tool.getW()))) {
      boolean first = true;
      if (tool.getS() == 'O' && (tool.getH() == tool.getW()))
          rtool = tool.getH() / 2.0f;
      float xstart = -rtool + rpensize;
      while (xstart <= -rhole - rpensize) { // Comunque faccio almeno un giro
          drawSingleCircle(x, y, -xstart);
          // Now I increment xstart to draw another circle pensize/2 right of the previous
          xstart += penSize;
      }
      if (rhole > 0) {
        // Draw the outer circle of the hole
        drawSingleCircle(x, y, rhole + rpensize);
      }
    } else if (tool.getS() == 'R' || tool.getS() == 'O') {
      float h = tool.getH();
      float w = tool.getW();
      if (tool.getS() == 'O' ) {
         if ( w > h) 
             w -= h;
         else if ( h > w)
             h -= w;
      } 
      boolean first = true;
      add(new Move(x - w / 2 + penSize / 2, y - h / 2 + penSize / 2));

        float xstart = -w / 2 + penSize / 2;
        float ystart = -h / 2 + penSize / 2;
        while ( xstart < 0 || first) {
            //ystart = xstart;
            if ( first && up ) {
                add(new Move(0));
                up = false;
            }
            float xv = xstart, yv = ystart;
            if ( first) {
                add(new Move(x + xv, y + yv));
                xv = -xstart;
                add(new Move(x + xv, y + yv));
                yv = -ystart;
                add(new Move(x + xv, y + yv));
                xv = xstart;
                add(new Move(x + xv, y + yv));
                yv = ystart;
                add(new Move(x + xv, y + yv));                
                first = false;
            } else {
                // down
                // Control if the vertical line touch the hole circle 
                float r = (float) Math.sqrt(Math.pow(xstart,2) + Math.pow(ystart,2));
                if ( r  > rhole + rpensize) {
                    add(new Move(x + xv, y + yv));
                    if ( xv + rpensize > -rhole - rpensize) {
                        float yh = (float) Math.sqrt(Math.pow(rhole + rpensize,2) - Math.pow(xv + rpensize,2));
                        add(new Move(x + xv, y - yh));
                        add(new Move(zUp));
                        add(new Move(x + xv, y + yh));
                        add(new Move(0));
                    } 
                    yv = -ystart;
                    add(new Move(x + xv, y + yv));                    
                    // right
                    xv = -xstart;
                    if ( yv - rpensize < rhole + rpensize) {
                        float xh = (float) Math.sqrt(Math.pow(rhole + rpensize,2) - Math.pow(yv - rpensize,2));
                        add(new Move(x - xh, y + yv));
                        add(new Move(zUp));
                        add(new Move(x + xh, y + yv));
                        add(new Move(0));
                    } 
                    add(new Move(x + xv, y + yv));            
                    // up
                    yv = ystart;
                    if ( xv - rpensize < rhole + rpensize) {
                        float yh = (float) Math.sqrt(Math.pow(rhole + rpensize,2) - Math.pow(xv - rpensize,2));
                        add(new Move(x + xv, y + yh));
                        add(new Move(zUp));
                        add(new Move(x + xv, y - yh));
                        add(new Move(0));
                    }
                    add(new Move(x + xv, y + yv));                    
                    xv = xstart;
                    if ( yv + rpensize > -rhole - rpensize) {
                        float xh = (float) Math.sqrt(Math.pow(rhole + rpensize,2) - Math.pow(yv + rpensize,2));
                        add(new Move(x + xh, y + yv));
                        add(new Move(zUp));
                        add(new Move(x - xh, y + yv));
                        add(new Move(0));
                    }
                    add(new Move(x + xv, y + yv));            
                } // end of r > rhole + rpensize
            }        
            if ( w > h) {
                xstart += penSize; ystart += penSize * h / w;
            } else {
                ystart += penSize; xstart += penSize * w / h;                
            }
        } // end while xstart > 0
        if (rhole > 0) {
            // Draw the outer circle of the hole
            drawSingleCircle(x, y, rhole - rpensize);
        }
        if (tool.getS() == 'O') {
            // draw the semicircular shape at the border
            h = tool.getH();
            w = tool.getW();
            if ( w > h) {
                // horizontal
                w -= h;
                add(new Move(zUp));
                float sign = -1;
                for ( int i = 0; i <=1; i++) {
                    ystart = -h / 2 + rpensize;
                    first = true;
                    while (ystart < 0) {
                        float yv = ystart;
                        while (yv < -ystart) {
                            float xh = sign * ((float) Math.sqrt(Math.pow(ystart,2) - Math.pow(yv,2)));
                            add(new Move(x + w / 2 * sign + xh, y + yv));
                            if ( first ) {
                                add(new Move(0));
                                first = false;
                            }
                            yv += tolerance;
                        }
                        ystart += penSize;
                    }
                    sign = sign * -1;
                    add(new Move(zUp));
                }
            } else if ( h > w) {
                // vertical
                h -= w;
                float sign = -1;
                add(new Move(zUp));
                for ( int i = 0; i <=1; i++) {
                    xstart = -w / 2 + rpensize;
                    first = true;
                    while (xstart < 0) {
                        float xv = xstart;
                        while (xv < -xstart) {
                            float yh = sign * ((float) Math.sqrt(Math.pow(xstart,2) - Math.pow(xv,2)));
                            add(new Move(x + xv, y + h / 2 * sign + yh));
                            if ( first ) {
                                add(new Move(0));
                                first = false;
                            }
                            xv += tolerance;
                        }
                        xstart += penSize;
                    }
                    sign = sign * -1;
                    add(new Move(zUp));
                }                
            }
        }
    } 

    //add(new Move(x, y));
  }

  private void add(Move m) {
    m.mirror(mirX, mirY);
    moves.add(m);
    if (m.getToX() < minX) {
      minX = m.getToX();
    }
    if (m.getToY() < minY) {
      minY = m.getToY();
    }
    if (m.getToY() > maxY) {
      maxY = m.getToY();
    }
    if (m.getToX() > maxX) {
      maxX = m.getToX();
    }
    if ( Converter.minx != 0 )
        ofX = -Converter.minx;
    else
        ofX = -minX;
    if ( Converter.miny != 0 )
        ofY = -Converter.miny;
    else
        ofY = -minY;
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

  public void setMirX(float mirX) {
    this.mirX = mirX;
  }

  public void setMirY(float mirY) {
    this.mirY = mirY;
  }
  

  public void setzUp(float zUp) {
    this.zUp = zUp;
  }

  public void setRepeat(int repeat) {
    this.repeat = repeat;
  }
  public void setF(int F) {
    this.F = F;
  }
  public void setDrillFile(String file) {
    this.drillFile = file;
  }
  
  public String getDrillFile() {
      return drillFile;
  }

}
