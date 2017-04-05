package de.dfki.renderer;

import de.fhpotsdam.unfolding.UnfoldingMap;
import processing.core.PGraphics;

public final class MapRenderer extends AbstractRenderer<UnfoldingMap> {

  @Override
  public void draw(UnfoldingMap map, PGraphics g) {
    map.draw();
  }

}
