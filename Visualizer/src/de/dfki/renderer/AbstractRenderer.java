package de.dfki.renderer;

import processing.core.PGraphics;

public abstract class AbstractRenderer<T> {

  public abstract void draw(T object, PGraphics g);
  
}
