/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gerbertogcode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vojta3310
 */
public class GerberToGcode {

  private static void printHelp() {
    System.out.println(""
      + "Usage:\n"
      + "   G2G [options] <fileIn> <fileOut>\n"
      + "   <fileIn>        Path to input file.\n"
      + "   <fileOut>       Path to output file.\n"
      + "\n"
      + "Options:\n"
      + "   -penSize=<w>    Set line width to <w>\n"
      + "   -penUp=<z>      Set z coords for pen up to <z>.\n"
      + "   -penDown=<z>    Set z coords for pen down to <z>.\n"
      + "   -F=             F speed.\n"            
      + "   -dx=            Larghezza in mm del PCB.\n"            
      + "   -dy=            altezza in mm del PCB.\n"    
      + "   -bitSize=       Dimensione in mm della punta per tagliare il bordo.\n"                
      + "   -precision=<p>  Set min step to <p>.\n"
      + "   -scale=<s>      Set scale of drawing to <s>.\n"
      + "   -mirrorX        Mirror in X.\n"
      + "   -mirrorY        Mirror in Y.\n"
      + "   -repeat=<t>     SET how mmany times encycle wires.\n"
      + "   -drill=         SET File name of drill.\n"
      + "   -edge=          SET File name of edge cuts.\n"
            
      + "   -show           Show final drawing in window.\n"
      + "   -debug          Write bebug messages.\n"
      + "\n"
      + "Gerber To Gecode (G2G) version 0.0.2 (c)Vojta3310, 2018\n"
      + "Aviable from: https://github.com/Vojta3310/GerberToGcode\n");
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    Converter co = new Converter();
    String fileIn = "";
    String fileOut = "";
    String drillFile = "";
    String edgeFile = "";
    // DImensione della punta per il taglio del bordo
    float bitSize = 3.175f;
    for (String arg : args) {
      if (arg.startsWith("-")) {
        if (arg.equals("-show")) {
          co.setShow(true);
        } else if (arg.equals("-debug")) {
          co.setBebug(true);
        } else if (arg.startsWith("-penSize=")) {
          co.setPenSize(Float.parseFloat(arg.substring(9)));
        } else if (arg.startsWith("-penUp=")) {
          co.setzUp(Float.parseFloat(arg.substring(7)));
        } else if (arg.startsWith("-precision=")) {
          co.setTolerance(Float.parseFloat(arg.substring(11)));
        } else if (arg.equals("-mirrorX")) {
          co.setMirX(true);
        } else if (arg.equals("-mirrorY")) {
          co.setMirY(true);
        } else if (arg.startsWith("-scale=")) {
          co.setScale(Float.parseFloat(arg.substring(7)));
        } else if (arg.startsWith("-repeat=")) {
          co.setRepeat(Integer.parseInt(arg.substring(8)));
        } else if (arg.startsWith("-F=")) {
          co.setF(Integer.parseInt(arg.substring(3)));
        } else if (arg.startsWith("-penDown=")) {
          co.setPenDown(Float.parseFloat(arg.substring(9)));          
        } else if (arg.startsWith("-dx=")) {
          co.setMinx(Float.parseFloat(arg.substring(4)));          
        } else if (arg.startsWith("-dy=")) {
          co.setMiny(Float.parseFloat(arg.substring(4)));          
        } else if (arg.startsWith("-bitSize=")) {
          bitSize = Float.parseFloat(arg.substring(9));          
        } else if (arg.startsWith("-drill=")) {
          drillFile = arg.substring(7);          
        } else if (arg.startsWith("-edge=")) {
          edgeFile = arg.substring(6);          
        } else {
          printHelp();
          return;
        }
      } else if (fileIn.equals("")) {
        fileIn = arg;
      } else if (fileOut.equals("")) {
        fileOut = arg;
      } else {
        printHelp();
        return;
      }
    }
    if (fileIn.equals("") || fileOut.equals("")) {
      printHelp();
      return;
    }
    File fI = new File(fileIn);
    File fO = new File(fileOut);
    if (!fI.exists()) {
      System.out.println("File " + fileIn + " doesnt exist!");
      return;
    }
    if (fO.isDirectory() || fI.isDirectory()) {
      System.out.println("Not a file!");
      return;
    }
    co.setFileIn(fileIn);
    co.setFileOut(fileOut);
    co.setDrillFile(drillFile);
    co.setEdgeFile(edgeFile);
    if (edgeFile != null && edgeFile.length() > 0) {
        try {
            readEdgeFile(co, edgeFile);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error reading edge cut file!");
            return;
        }
    }
    co.convert();
    
    // Scrivo il gcode del bordo
    String path = fO.getPath();
    File fBorder = new File("border.gcode");
    try (FileWriter fileWriter = new FileWriter(fBorder)) {
      fileWriter.write("G21 (metric ftw)\n");
      fileWriter.write("G90 (absolute mode)\n");
      fileWriter.write("G1 Z4.0\n");
      fileWriter.write("G1 X0 Y0 F 1500\n");
      fileWriter.write("G1 Z0\n");
      fileWriter.write("G1 X0 Y" + GerberToGcode.formatFloatForGcode(Converter.miny + bitSize) + "\n");
      fileWriter.write("G1 X" + GerberToGcode.formatFloatForGcode(Converter.minx + bitSize) +  " Y" + GerberToGcode.formatFloatForGcode(Converter.miny + bitSize) + "\n");
      fileWriter.write("G1 X" + GerberToGcode.formatFloatForGcode(Converter.minx + bitSize) +  " Y0\n");
      fileWriter.write("G1 X0 Y0\n");
      fileWriter.write("G1 Z4.0\n");
      fileWriter.write("G1 X0 Y0\n");
      fileWriter.flush();
    } catch (IOException ex) {
      Logger.getLogger(Mover.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    System.out.println("Succesfully converted with this setting.");
    co.printOption();
  }
  
  private static String formatFloatForGcode(float f) {
      NumberFormat nf = NumberFormat.getInstance();
      nf.setGroupingUsed(false);
      nf.setMinimumFractionDigits(6);
      return nf.format(f);
  }
  
  private static void readEdgeFile(Converter co, String edgeFile) throws Exception {
    List<String> righe = Files.readAllLines(Paths.get(edgeFile));
    boolean metric = false;
    int formatx = 0, formaty = 0;
    boolean bebug = false;
    float minx = Float.MAX_VALUE;
    float miny = Float.MAX_VALUE;
    float maxx = Float.MIN_VALUE;
    float maxy = Float.MIN_VALUE;
    
    for (String command: righe ) {
        if (command.startsWith("G04")) {
          if (bebug) {
            System.out.println("  Only comment.");
          }
        } else if (command.startsWith("FS")) {
          if (bebug) {
            System.out.println("  Format specification!");
          }
          formatx = Integer.parseInt(command.substring(command.indexOf("Y") - 2, command.indexOf("Y")));
          formaty = Integer.parseInt(command.substring(command.indexOf("Y") + 1, command.indexOf("Y") + 3));
          if (bebug) {
            System.out.println("  Format x:" + formatx + " y:" + formaty);
          }      
        } else if (command.startsWith("MO")) {
          if (bebug) {
            System.out.println("  Units settings!");
          }
        } else if (command.startsWith("AD")) {

        } else if (command.startsWith("D")) {

        } else if (command.startsWith("X") || command.startsWith("Y")) {
          if (bebug) {
            System.out.println("  Move.");
          }

          String coords[] = {"X","Y","I","J","D"};
          Float values[] = {Float.NaN, Float.NaN, 0.0f, 0.0f};
          String sx = "", sy = "";
          for (int i = 0; i < coords.length-1; i++) {
            String coord = coords[i];
            if(!command.contains(coord)){
              continue;
            }
            int n = 1;
            String next=coords[i+n];
            while(!command.contains(next) && i + n < coords.length){
              n++;
              next=coords[i+n];
            }
            if(i%2==0){
                sx = command.substring(command.indexOf(coord) + 1, command.indexOf(next));
              values[i] = Parser.parseFloat(sx, formatx);

            } else {
                sy = command.substring(command.indexOf(coord) + 1, command.indexOf(next));
              values[i] = Parser.parseFloat(sy, formaty);
            }
          }

          Float dx = values[0]+values[2];
          Float dy = values[1]+values[3];
          if ( dx < minx)
              minx = dx;
          if ( dy < miny)
              miny = dy;
          if ( dx > maxx)
              maxx = dx;
          if (dy > maxy)
              maxy =dy;
        }
      }
        co.setMinx(minx);
        co.setMiny(miny);
        co.setMaxx(minx);
        co.setMaxy(miny);
        
    
  }
}
