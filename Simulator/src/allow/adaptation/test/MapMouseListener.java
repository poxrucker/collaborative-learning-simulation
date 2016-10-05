package allow.adaptation.test;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Set;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.Waypoint;

public class MapMouseListener extends MouseAdapter {

	private Set<Waypoint> waypoints;
	private JXMapViewer mapViewer;

	public MapMouseListener(JXMapViewer mapViewer, Set<Waypoint> waypoints) {
		this.mapViewer = mapViewer;
		this.waypoints = waypoints;
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		Point2D gp_pt = null;

		for (Waypoint waypoint : waypoints) {
			// convert to world bitmap
			gp_pt = mapViewer.getTileFactory().geoToPixel(
					waypoint.getPosition(), mapViewer.getZoom());

			// convert to screen
			Rectangle rect = mapViewer.getViewportBounds();
			Point converted_gp_pt = new Point((int) gp_pt.getX() - rect.x,
					(int) gp_pt.getY() - rect.y);
			// check if near the mouse
			if (converted_gp_pt.distance(me.getPoint()) < 50) {
				System.out.println("show agent details");

			} else {
				System.out
						.println("mapViewer mouse click has been dected but NOT with 10 pixels of a waypoint ");
			}
		}

	}

}
