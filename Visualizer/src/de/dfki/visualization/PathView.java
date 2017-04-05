package de.dfki.visualization;

import de.dfki.selection.Path;

public class PathView {

  public Path path;
  public int color;
  
  public String toString() {
    return path.Id;
  }
}
