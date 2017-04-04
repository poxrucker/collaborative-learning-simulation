package de.dfki.util;

public class RGBColor {

  public static final int getColor(int r, int g, int b) {
    return ((r & 0x0ff) << 16) | ((g & 0x0ff) << 8) | (b & 0x0ff);
  }
}
