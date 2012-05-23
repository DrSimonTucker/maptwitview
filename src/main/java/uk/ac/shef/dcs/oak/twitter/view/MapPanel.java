package uk.ac.shef.dcs.oak.twitter.view;

import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;
import org.openstreetmap.gui.jmapviewer.interfaces.MapRectangle;

import uk.ac.shef.dcs.oak.twitter.SocialPost;
import uk.ac.shef.dcs.oak.twitter.model.ModelListener;
import uk.ac.shef.dcs.oak.twitter.model.TwitMapModel;

/**
 * Generic panel for displaying a map
 * 
 * @author sat
 * 
 */
public class MapPanel extends JMapViewer implements ModelListener
{
   @Override
   public void modelUpdated(TwitMapModel newModel)
   {
      model = newModel;
      zoom();
   }

   TwitMapModel model;

   public MapPanel()
   {
      this.addMouseListener(new MouseAdapter()
      {
         @Override
         public void mouseClicked(MouseEvent e)
         {
            System.out.println(e.getX() + " and " + e.getY());
            System.out.println(getPosition(e.getX(), e.getY()));
         }
      });
   }

   private void zoom()
   {
      if (model != null)
      {
         // Add a marker on my house
         List<MapMarker> markers = new LinkedList<MapMarker>();
         for (SocialPost post : model.getGeoPosts())
         {
            System.out.println(post.getLat() + " and " + post.getLon());
            MapMarker m = new MapMarkerDot(post.getLat(), post.getLon());
            markers.add(m);
         }
         this.setMapMarkerList(markers);
         this.setDisplayToFitMapMarkers();
         System.out.println(this.getMapMarkerList());
      }
   }

   public static void main(String[] args)
   {
      JFrame framer = new JFrame();
      framer.setLayout(new GridLayout(1, 2));
      MapPanel panel = new MapPanel();
      framer.add(panel, 0);
      TweetList list = new TweetList();
      framer.add(list, 1);
      framer.setSize(500, 500);
      framer.setLocationRelativeTo(null);
      framer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      framer.setVisible(true);

      panel.zoom();

      TwitMapModel model = new TwitMapModel();
      model.addListener(panel);
      model.addListener(list);
   }

   /**
    * Sets the displayed map pane and zoom level so that all chosen map elements
    * are visible.
    */
   @Override
   public void setDisplayToFitMapElements(boolean markers, boolean rectangles, boolean polygons)
   {
      int nbElemToCheck = 0;
      if (markers && mapMarkerList != null)
         nbElemToCheck += mapMarkerList.size();
      if (rectangles && mapRectangleList != null)
         nbElemToCheck += mapRectangleList.size();
      if (polygons && mapPolygonList != null)
         nbElemToCheck += mapPolygonList.size();
      if (nbElemToCheck == 0)
         return;

      int x_min = Integer.MAX_VALUE;
      int y_min = Integer.MAX_VALUE;
      int x_max = Integer.MIN_VALUE;
      int y_max = Integer.MIN_VALUE;
      int mapZoomMax = tileController.getTileSource().getMaxZoom();

      if (markers)
      {
         for (MapMarker marker : mapMarkerList)
         {
            int x = OsmMercator.LonToX(marker.getLon(), mapZoomMax);
            int y = OsmMercator.LatToY(marker.getLat(), mapZoomMax);
            x_max = Math.max(x_max, x);
            y_max = Math.max(y_max, y);
            x_min = Math.min(x_min, x);
            y_min = Math.min(y_min, y);
         }
      }

      if (rectangles)
      {
         for (MapRectangle rectangle : mapRectangleList)
         {
            x_max = Math.max(x_max,
                  OsmMercator.LonToX(rectangle.getBottomRight().getLon(), mapZoomMax));
            y_max = Math
                  .max(y_max, OsmMercator.LatToY(rectangle.getTopLeft().getLat(), mapZoomMax));
            x_min = Math
                  .min(x_min, OsmMercator.LonToX(rectangle.getTopLeft().getLon(), mapZoomMax));
            y_min = Math.min(y_min,
                  OsmMercator.LatToY(rectangle.getBottomRight().getLat(), mapZoomMax));
         }
      }

      if (polygons)
      {
         for (MapPolygon polygon : mapPolygonList)
         {
            for (Coordinate c : polygon.getPoints())
            {
               int x = OsmMercator.LonToX(c.getLon(), mapZoomMax);
               int y = OsmMercator.LatToY(c.getLat(), mapZoomMax);
               x_max = Math.max(x_max, x);
               y_max = Math.max(y_max, y);
               x_min = Math.min(x_min, x);
               y_min = Math.min(y_min, y);
            }
         }
      }

      int height = Math.max(0, getHeight());
      int width = Math.max(0, getWidth());
      int newZoom = mapZoomMax;
      System.out.println(x_max + " and " + x_min + " given " + height);
      int x = x_max - x_min;
      int y = y_max - y_min;
      System.out.println(x);
      while (x > width || y > height)
      {
         newZoom--;
         x >>= 1;
         y >>= 1;

         System.out.println(x);
      }
      x = x_min + (x_max - x_min) / 2;
      y = y_min + (y_max - y_min) / 2;
      int z = 1 << (mapZoomMax - newZoom);
      x /= z;
      y /= z;
      setDisplayPosition(x, y, newZoom);
   }
}
