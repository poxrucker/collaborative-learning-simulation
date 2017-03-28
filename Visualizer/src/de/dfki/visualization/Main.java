package de.dfki.visualization;

import processing.core.PApplet;

public class Main {

  public static void main(String[] args) {
    //GraphVisualization temp = new GraphVisualization(null);
    //create your JFrame
    //JFrame frame = new JFrame("JFrame Test");
    //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //create your sketch
    //GraphVisualization pt = new GraphVisualization();
    //frame.getContentPane().add(pt);
    //make your JFrame visible
    //frame.setSize(800, 600);
    //pt.resize(800, 600);
    //pt.init();
    //frame.pack();
    //frame.setVisible(true);

    //start your sketch
    // PApplet.runSketch(new String[] { "/Users/Andi/Documents/DFKI/VW simulation/data/world/trento_merged.world" }, pt);
    PApplet.main("de.dfki.visualization.GraphVisualization", new String[] { "/Users/Andi/Documents/DFKI/VW simulation/data/world/trento_merged.world" });

  }
}
