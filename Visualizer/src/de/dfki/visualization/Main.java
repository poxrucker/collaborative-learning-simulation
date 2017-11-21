package de.dfki.visualization;

import java.io.IOException;

import javax.swing.JFrame;

public class Main {

  public static void main(String[] args) throws IOException {
    GraphAnalysis frame = new GraphAnalysis();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.run();
  }
}
