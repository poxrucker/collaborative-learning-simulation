package de.dfki.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.DoubleStream;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public final class Dataset {
  // Dataset matrix
  private double[][] dataSet;
  
  // Time slots
  private int[] time;
  
  // Edges
  private String[] edges;
  private Object2IntOpenHashMap<String> offsets;
    
  private Dataset(int[] time, String[] edges, double[][] dataSet) {
    this.time = time;
    this.edges = edges;
    this.dataSet = dataSet;
    
    offsets = new Object2IntOpenHashMap<>();
    
    for (int i = 0; i < edges.length; i++) {
      offsets.put(edges[i], i);
    }
  }
  
  public double getMinimumTime() {
    return time[0];
  }
  
  public double getMaximumTime() {
    return time[time.length - 1];
  }
  
  public int[] getTime() {
    return time.clone();
  }
  
  public String[] getEdges() {
    return edges.clone();
  }
  
  public double[] getDataForEdge(String edge) {
    int offset = offsets.getInt(edge);
    return dataSet[offset];
  }
  
  public double getAggregatedDataForEdge(String edge) {
    double[] data = getDataForEdge(edge);
    int count = 0;
    double total = 0;

    for (int i = 0; i < data.length; i++) {
      
        if (data[i] != 0) {
            total += data[i];
            count++;
        } 
    }

    return (count > 0) ? (total / count) : 0;
  }
  
  public double[] getAggregatedDataForEdges() {
    double[] ret = new double[edges.length];
    
    for (int i = 0; i < ret.length; i++) {      
      ret[i] = DoubleStream.of(getDataForEdge(edges[i])).sum();
    }
    return ret;
  }
  
  public double[] getDataForTime(int pointInTime) {
    
    if ((pointInTime < time[0]) && (pointInTime > time[time.length - 1]))
      return null;
    
    // Find index
    int index = 0;
    
    while (time[index] < pointInTime)
      index++;
    
    double[] ret = new double[edges.length];

    for (int i = 0; i < edges.length; i++) {
      ret[i] = dataSet[i][index];
    }
    return ret;
  }
  
  private static int getNumberOfLines(Path path) throws IOException {
    int lines = 0;

    try (BufferedReader reader = Files.newBufferedReader(path)) {
      
      while (reader.readLine() != null)
        lines++;
    }
    return lines;
  }
  
  public static Dataset loadDataset(Path path) throws IOException {
    // Get number of lines
    int lines = getNumberOfLines(path);
    System.out.println(lines);
    Map<String, String> mapping = new HashMap<>();
    
    // Parse file
    try(BufferedReader reader = Files.newBufferedReader(path)) {
      int offset = 0;
      
      // Parse MAPPING section
      String line = reader.readLine();
      
      if (!line.equals("MAPPING"))
        throw new IllegalArgumentException();
      
      offset++;
      
      while (!(line = reader.readLine()).equals("")) {
        String[] tokens = line.split(";");
        mapping.put(tokens[0], tokens[1] + ";;" + tokens[2]);
        offset++;
      }
      offset++;
      
      // Parse DATA section
      line = reader.readLine();
      
      if (!line.equals("DATA"))
        throw new IllegalArgumentException();
      
      offset++;
      
      // Header line
      line = reader.readLine();
      offset++;
      String[] timeTokens = line.split(";");
      int[] timeSlots = new int[timeTokens.length - 1];
      
      for (int i = 1; i < timeTokens.length; i++) {
        timeSlots[i - 1] = Integer.parseInt(timeTokens[i]);
      }
     
      int nRows = lines - offset;
      double[][] data = new double[nRows][timeSlots.length];
      String[] edges = new String[nRows];
      
      // Actual data
      for (int i = 0; i < nRows; i++) {
        // Read and parse line
        line = reader.readLine();
        String[] tokens = line.split(";");
        edges[i] = mapping.get(tokens[0]);
        
        if (edges[i] == null)
          System.out.println();
        
        //int dataOffset = i * timeSlots.length;
        double[] temp = data[i];
        for (int j = 1; j < tokens.length; j++) {
          temp[j - 1] = Double.parseDouble(tokens[j]);
        }
        offset++;
      }
      return new Dataset(timeSlots, edges, data);
    }

  }
  
}
