/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gerbertogcode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author vojta3310
 */
public class Parser {

  private int formatx;
  private int formaty;
  private final Tool[] tools = new Tool[999];
  private boolean bebug = false;

  public void setBebug(boolean bebug) {
    this.bebug = bebug;
  }

  public void parse(String s, Mover m) {
    String[] arr = getCommands(s);
    for (String c : arr) {
      parseCommand(c, m);
    }
  }

  public String[] getCommands(String fullFile) {
    String in = fullFile.replace("%", "").replace("\r\n", "").trim().toUpperCase();
    return in.split("\\*");
  }

  public float parseFloatX(String s) {
    return (float) Integer.parseInt(s) / Math.round(Math.pow(10, formatx - 10 * (int) (formatx / 10)));
  }

  public float parseFloatY(String s) {
    return (float) Integer.parseInt(s) / Math.round(Math.pow(10, formaty - 10 * (int) (formaty / 10)));
  }

  public void parseCommand(String command, Mover m) {
    if (bebug) {
      System.out.println("Parsing: " + command);
    }
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
      if (bebug) {
        System.out.println("  Tool definition!");
      }
      if (bebug) {
        System.out.println("  Tool number:" + command.substring(3, 5));
      }
      String[] a = command.substring(7).split("X");
      float d;
      if (command.substring(5, 6).equals("C") || command.substring(5, 6).equals("P")) {
        d = Float.parseFloat(a[0]);
        tools[Integer.parseInt(command.substring(3, 5)) - 10] = new Tool('C', d);
        if (bebug) {
          System.out.println("  Tool diametr:" + d);
        }
      } else if (command.substring(5, 7).equals("OC")) {
        d = Float.parseFloat(command.substring(9));
        tools[Integer.parseInt(command.substring(3, 5)) - 10] = new Tool('C', d);
        if (bebug) {
          System.out.println("  Tool diametr:" + d);
        }
      } else if (command.substring(5, 6).equals("R")) {
        tools[Integer.parseInt(command.substring(3, 5)) - 10] = new Tool('R', Float.parseFloat(a[0]), Float.parseFloat(a[1]));
        if (bebug) {
          System.out.println("  Tool size:" + Float.parseFloat(a[0]) + "x" + Float.parseFloat(a[1]));
        }
      } else if (command.substring(5, 6).equals("O")){
        tools[Integer.parseInt(command.substring(3, 5)) - 10] = new Tool('O', Float.parseFloat(a[0]), Float.parseFloat(a[1]));
        if (bebug) {
          System.out.println("  Tool size:" + Float.parseFloat(a[0]) + "x" + Float.parseFloat(a[1]));
        }
      } else {
        System.out.println("Unknown tool !!!");
      }


    } else if (command.startsWith(
      "D")) {
      if (command.charAt(1) == '0') {
        if (bebug) {
          System.out.println("  Move mode.");
        }
      } else {
        if (bebug) {
          System.out.println("  Select tool.");
        }
        if (bebug) {
          System.out.println("  Tool number:" + command.substring(1));
        }
        m.setTool(tools[Integer.parseInt(command.substring(1)) - 10]);
      }
    } else if (command.startsWith(
      "X") || command.startsWith("Y")) {
      if (bebug) {
        System.out.println("  Move.");
      }
      
      String coords[] = {"X","Y","I","J","D"};
      Float values[] = {Float.NaN, Float.NaN, 0.0f, 0.0f};
      
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
          values[i] = parseFloatX(command.substring(command.indexOf(coord) + 1, command.indexOf(next)));
        }else{
          values[i] = parseFloatY(command.substring(command.indexOf(coord) + 1, command.indexOf(next)));
        }
      }
      
      Float x = values[0]+values[2];
      Float y = values[1]+values[3];
      String d = command.substring(command.indexOf("D") + 1);
      if (bebug) {
        System.out.println("  To x:" + x + " y:" + y + " mode:" + d);
      }
      m.move(x, y, Integer.parseInt(d));
    } else if (command.startsWith(
      "G01")) {
      if (bebug) {
        System.out.println("  Linear mode!");
      }
    } else if (command.startsWith(
      "G74") || command.startsWith("G75")) {
      if (bebug) {
        System.out.println("  Quadrant mode!");
      }
    } else if (command.startsWith(
      "T")) {
      if (bebug) {
        System.out.println("  Attribute - dont care.");
      }
    } else if (command.startsWith(
      "OF")) {
      if (bebug) {
        System.out.println("  Ofset - dont care.");
      }
    } else if (command.startsWith(
      "AM")
      || command.startsWith("AB")
      || command.startsWith("LP")
      || command.startsWith("LM")
      || command.startsWith("LS")
      || command.startsWith("LR")
      || command.startsWith("SR")
      || command.startsWith("G36")
      || command.startsWith("G37")
      || command.startsWith("G02")
      || command.startsWith("G03")) {
      if (bebug) {
        System.out.println("!!I CANT DO THIS!!");
      }
    } else {
      if (bebug) {
        System.out.println("!!UNKNOWN COMMAND!!");
      }
    }
  }

  public void parseFile(String path, Mover m) {
    parse(readFile(path), m);
  }

  private static String readFile(String filePath) {
    String content = "";
    try {
      content = new String(Files.readAllBytes(Paths.get(filePath)));
    } catch (IOException e) {
    }
    return content;
  }
}
