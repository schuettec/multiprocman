package shelltest;

import java.awt.Color;

public class AnsiColors {
  static final Color D_Black = Color.getHSBColor(0.000f, 0.000f, 0.000f);
  static final Color D_Red = Color.getHSBColor(0.000f, 1.000f, 0.502f);
  static final Color D_Blue = Color.getHSBColor(0.667f, 1.000f, 0.502f);
  static final Color D_Magenta = Color.getHSBColor(0.833f, 1.000f, 0.502f);
  static final Color D_Green = Color.getHSBColor(0.333f, 1.000f, 0.502f);
  static final Color D_Yellow = Color.getHSBColor(0.167f, 1.000f, 0.502f);
  static final Color D_Cyan = Color.getHSBColor(0.500f, 1.000f, 0.502f);
  static final Color D_White = Color.getHSBColor(0.000f, 0.000f, 0.753f);
  static final Color B_Black = Color.getHSBColor(0.000f, 0.000f, 0.502f);
  static final Color B_Red = Color.getHSBColor(0.000f, 1.000f, 1.000f);
  static final Color B_Blue = Color.getHSBColor(0.667f, 1.000f, 1.000f);
  static final Color B_Magenta = Color.getHSBColor(0.833f, 1.000f, 1.000f);
  static final Color B_Green = Color.getHSBColor(0.333f, 1.000f, 1.000f);
  static final Color B_Yellow = Color.getHSBColor(0.167f, 1.000f, 1.000f);
  static final Color B_Cyan = Color.getHSBColor(0.500f, 1.000f, 1.000f);
  static final Color B_White = Color.getHSBColor(0.000f, 0.000f, 1.000f);

  public static Color[] getAllColors() {
    return new Color[] {
        D_Black, D_Red, D_Blue, D_Magenta, D_Green, D_Yellow, D_Cyan, D_White, B_Black, B_Red, B_Blue, B_Magenta,
        B_Green, B_Yellow, B_Cyan, B_White
    };
  }

  public static Color getANSIColor(String ANSIColor, Color resetColor) {
    if (ANSIColor.equals("\u001B[30m")) {
      return D_Black;
    } else if (ANSIColor.equals("\u001B[31m")) {
      return D_Red;
    } else if (ANSIColor.equals("\u001B[32m")) {
      return D_Green;
    } else if (ANSIColor.equals("\u001B[33m")) {
      return D_Yellow;
    } else if (ANSIColor.equals("\u001B[34m")) {
      return D_Blue;
    } else if (ANSIColor.equals("\u001B[35m")) {
      return D_Magenta;
    } else if (ANSIColor.equals("\u001B[36m")) {
      return D_Cyan;
    } else if (ANSIColor.equals("\u001B[37m")) {
      return D_White;
    } else if (ANSIColor.equals("\u001B[0;30m")) {
      return D_Black;
    } else if (ANSIColor.equals("\u001B[0;31m")) {
      return D_Red;
    } else if (ANSIColor.equals("\u001B[0;32m")) {
      return D_Green;
    } else if (ANSIColor.equals("\u001B[0;33m")) {
      return D_Yellow;
    } else if (ANSIColor.equals("\u001B[0;34m")) {
      return D_Blue;
    } else if (ANSIColor.equals("\u001B[0;35m")) {
      return D_Magenta;
    } else if (ANSIColor.equals("\u001B[0;36m")) {
      return D_Cyan;
    } else if (ANSIColor.equals("\u001B[0;37m")) {
      return D_White;
    } else if (ANSIColor.equals("\u001B[1;30m")) {
      return B_Black;
    } else if (ANSIColor.equals("\u001B[1;31m")) {
      return B_Red;
    } else if (ANSIColor.equals("\u001B[1;32m")) {
      return B_Green;
    } else if (ANSIColor.equals("\u001B[1;33m")) {
      return B_Yellow;
    } else if (ANSIColor.equals("\u001B[1;34m")) {
      return B_Blue;
    } else if (ANSIColor.equals("\u001B[1;35m")) {
      return B_Magenta;
    } else if (ANSIColor.equals("\u001B[1;36m")) {
      return B_Cyan;
    } else if (ANSIColor.equals("\u001B[1;37m")) {
      return B_White;
    } else if (ANSIColor.equals("\u001B[0m")) {
      return resetColor;
    } else {
      return B_White;
    }
  }
}
