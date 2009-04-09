package FRDL;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import javax.swing.JPanel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * puts a GPS track on screen
 * @author rmh
 * with MUCH help from UTiGPX source code (even though it has a scaling error)
 */
public class MapView extends JPanel {
    
    private double centreLat;
    private double centreLon;
    private double max_lat;
    private double max_lat2;
    private double max_lon;
    private double min_lat;
    private double min_lat2;
    private double min_lon;
    private double scale;
    private Boolean maxMinDone = false;
    private double totalDist = 0;
    Graphics2D      g2d;

    public static Color            LEGEND_COLOUR  = Color.BLACK;
    public static Color            WAYPOINT_COLOUR  = Color.BLACK;
    public static Color            POINT_COLOUR     = Color.BLUE;
    private static final Color TRACK_COLOUR_A     = new Color(0,0,153); //Color.BLUE;
    private static final Color TRACK_COLOUR_V     = new Color(255,0,0); //red
    private static final Color TRACK_COLOUR_X     = new Color(153,153,153); //gray

    public static final int        WAYPOINT_SIZE   = 8;
    public static final int        POINT_SIZE      = 3;
    public static final int        LINE_SIZE       = 2;
    public static final int        FONT_SIZE       = 11;
    public static final int        CLICK_THRESHOLD = 5;
    public static final int        PADDING         = 20;

    public static final double     MAX_SCALE       = 500000.0;
    public static final double     MIN_SCALE       = 20.0;
    
    private static final long GAP_BETWEEN_TRACKS = 30; //threshold in sec before a new line is generated
    private ArrayList <TrackPath> trackPaths = null;

      /*
       * test: this just draws random lines on the map panel
       *
      */
      public GeneralPath testDrawTrack () {
          GeneralPath gp1;
          Random generator = new Random();

          gp1 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
          gp1.moveTo(50, 40);
          for (int i = 1;i<100;i++) {
              gp1.lineTo(generator.nextInt(this.getWidth()) + 1,generator.nextInt(this.getHeight()) + 1);
          }
          return gp1;
    }

    /*
     * This is the method which actually draws the track
     * is fired by a window resize or represh()
    */

    @Override
    public void paintComponent(Graphics g) {
        g2d = (Graphics2D) g;
        super.paintComponent(g2d);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        //g2d.setPaint(TRACK_COLOUR_A);
        Stroke stroke = new BasicStroke(LINE_SIZE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(stroke);
        g2d.setFont(new Font("Arial", 0, FONT_SIZE));

        if (App.track != null) {
            if (getHeight() > PADDING * 3) {
                // only draw if there's some space to draw in
                setScale();
                //g2d.draw(drawTrack());  //not used, see drawTracks()
                if (drawTracks()) {
                    for (TrackPath tp : trackPaths) {
                        g2d.setPaint(tp.getColour());
                        g2d.draw(tp.getGp());
                    }
                }
                drawStartAndFinish();
                drawScale();
                drawStats();
                //System.out.println("DONE DRAWING");
            }
        } else {
           // clear?
            //g2d.setColor(Color.WHITE);
            //g2d.fill(getVisibleRect());
            g2d.setPaint(Color.BLACK);
            g2d.setFont(new Font("Arial", 0, 20));
            FontMetrics fm = g2d.getFontMetrics();
            Rectangle2D area = fm.getStringBounds(App.mapCaption, g2d);
            g2d.drawString(App.mapCaption, (int)(getWidth() - area.getWidth())/2,(int)(getHeight() + area.getHeight())/2);
        }
    }

    /*
     * this draws the track
     * NOT USED see drawTracks()
     */
    private GeneralPath drawTrack () {
        GeneralPath gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        totalDist = 0;
        GpsPoint p = (FRDL.GpsPoint) App.track.firstEntry().getValue();
        Point2D p2 = projectMapToScreen(p.getLon(),p.getLat());
        gp.moveTo(p2.getX(),p2.getY()); //track start point
        for (Iterator it=App.track.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry)it.next();
            p = (GpsPoint) entry.getValue();
            totalDist += p.getDist();
            p2 = projectMapToScreen(p.getLon(),p.getLat());
            gp.lineTo(p2.getX(),p2.getY()); //all further points
        }
        return gp;
    }
    
     /*
     * creates possibly multiple lines to draw on the screen
     */
    private Boolean drawTracks () {
        trackPaths = new ArrayList <TrackPath> ();
        GeneralPath gp = null;
        GpsPoint p = null;
        Point2D p2 = null;
        totalDist = 0;
        DateTime start_time = ((LocalDateTime) App.track.firstEntry().getKey()).toDateTime(DateTimeZone.UTC);
        DateTime t = start_time;
        long elapsed = new Duration(start_time,t).getStandardSeconds();
        long lastElapsed = elapsed;
        String fixValidity = "X";
        Color colour = TRACK_COLOUR_A;

        for (Iterator it=App.track.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry)it.next();
            t = ((LocalDateTime) entry.getKey()).toDateTime(DateTimeZone.UTC);
            elapsed = new Duration(start_time,t).getStandardSeconds();
            p = (GpsPoint) entry.getValue();
            p2 = projectMapToScreen(p.getLon(),p.getLat());

            if (gp == null) {
                //new generalpath
                gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
                fixValidity = ((GpsPoint) entry.getValue()).getFixValidity();
                gp.moveTo(p2.getX(),p2.getY()); //track start point
            }
            if (elapsed - lastElapsed < GAP_BETWEEN_TRACKS &&
                    ((GpsPoint) entry.getValue()).getFixValidity().equals(fixValidity)) {
                //continue this generalpath
                gp.lineTo(p2.getX(),p2.getY());
                totalDist += p.getDist();
            } else {
                gp.lineTo(p2.getX(),p2.getY());
                //end this generalpath
                if (fixValidity.equals("V")) colour = TRACK_COLOUR_V;
                if (fixValidity.equals("X")) colour = TRACK_COLOUR_X;
                trackPaths.add(new TrackPath(gp,colour));
                //set the colour back to normal
                colour = TRACK_COLOUR_A;
                //and start a new one
                gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
                fixValidity = ((GpsPoint) entry.getValue()).getFixValidity();
                gp.moveTo(p2.getX(),p2.getY()); //track start point
            }
            lastElapsed = elapsed;
        }
        //add the last one
        if (fixValidity.equals("V")) colour = TRACK_COLOUR_V;
        if (fixValidity.equals("X")) colour = TRACK_COLOUR_X;
        if (gp != null) trackPaths.add(new TrackPath(gp,colour));

        if (trackPaths == null) return false;

        return true;
    }

        /* container for paths and their colour
     *
    */
    private class TrackPath {
        private GeneralPath gp = null;
        private Color colour = null;
        //constructor
        public TrackPath(GeneralPath _gp, Color _colour) {
            gp = _gp;
            colour = _colour;
        }

        public GeneralPath getGp() {
            return this.gp;
        }

        public Color getColour() {
            return this.colour;
        }
    }
      
    /*
     * returns a screen coordinate from a world coordinate,
     * by applying the Mercator map projection, & scale
     * 
    */
    private Point2D projectMapToScreen(double lon, double lat)
    {
        // http://en.wikipedia.org/wiki/Mercator_projection
        double x = lon - this.centreLon;
        double y = Math.log(Math.tan(Math.PI*(0.25 + lat/360))) -
                   Math.log(Math.tan(Math.PI*(0.25 + this.centreLat/360)));

        y  = Math.toDegrees(y);

        x *= scale;
        y *= scale;

        x += getWidth() /2.0;
        y -= getHeight()/2.0;

        return new Point2D.Double(x, -y);

         /*
        //this variation starts on the left side
        //the one above starts in the middle and is better.
        
        double x = ((lon - min_lon)* scale) + PADDING;
        double y = Math.log(Math.tan(Math.PI*(0.25 + lat/360))) -
                   Math.log(Math.tan(Math.PI*(0.25 + this.min_lat/360)));
        y  = Math.toDegrees(y);
        y = (getHeight() + (((y) * scale) * -1)) - PADDING;

        return new Point2D.Double(x, y);
        */
    }

    /*
     * sets the scale so you can always see all the track
    */
    private void setScale() {
        if (App.track == null) return;
        //commented out - so it properly recalculates every map
        //if (!maxMinDone) {
             //get the max and min lat & lon values of all Points
             //initialize max & min
             GpsPoint p = (FRDL.GpsPoint) App.track.firstEntry().getValue();
             max_lon = p.getLon();
             max_lat = p.getLat();
             min_lon = max_lon;
             min_lat = max_lat;
             //now iterate over the lot
            for (Iterator it=App.track.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry)it.next();
                max_lat = Math.max(max_lat, ((GpsPoint) entry.getValue()).getLat());
                max_lon = Math.max(max_lon, ((GpsPoint) entry.getValue()).getLon());
                min_lat = Math.min(min_lat, ((GpsPoint) entry.getValue()).getLat());
                min_lon = Math.min(min_lon, ((GpsPoint) entry.getValue()).getLon());
            }
            //TODO check the calcs below work over 0 deg E/W and 180 deg E/W
            //and 0 deg N/S and 90 deg N/S

            //System.out.println("lon; max:" + max_lon + " min:" + min_lon);
            //System.out.println("lat; max:" + max_lat + " min:" + min_lat);

            //this sets min_lat and max_lat to the mercator projection we're using.
            //This is what UTiGPX was missing so they had tried to fudge it
            //with a constant in an attempt to keep the initial view of the track all
            //on screen, but of course it is entirely dependent on the actual latitude...
            //lon doesn't need converting
            min_lat2 = Math.toDegrees(Math.log(Math.tan(Math.PI*(0.25 + min_lat/360))));
            max_lat2 = Math.toDegrees(Math.log(Math.tan(Math.PI*(0.25 + max_lat/360))));

            //System.out.println("lat2; max:" + max_lat + " min:" + min_lat);

            //the centre point
            centreLon = (max_lon + min_lon) / 2;
            centreLat = (max_lat + min_lat) / 2;
            maxMinDone = true;
        //}

         double s1 = (getHeight()-(PADDING * 2)) / Math.abs(max_lat2 - min_lat2);
         double s2 = (getWidth()-(PADDING * 2)) / Math.abs(max_lon - min_lon);

        scale = Math.min(s1,s2);
    }

    /*
     * creates the scale you see on screen
     *
    */
    private void drawScale()    {
        int left = 0;
        int right= getWidth();

        Point2D topleft     = projectScreenToMap(left,  getHeight()/2.0);
        Point2D topright    = projectScreenToMap(right, getHeight()/2.0);

        double lat1 = Math.toRadians(topleft.getY());
        double lon1 = Math.toRadians(topleft.getX());
        double lon2 = Math.toRadians(topright.getX());
        double radius = 6371000;			// Approx radius of the earth in meters

        // Spherical law of cosines
        double dist1    = Math.acos(Math.sin(lat1) * Math.sin(lat1)
                        + Math.cos(lat1) * Math.cos(lat1) * Math.cos(lon2-lon1)) * radius;

        double dist2    = Math.pow(10, (int)Math.log10(dist1/1.5));

        g2d.setColor(LEGEND_COLOUR);

        int ex = right - 10;
        int sx = ex - (int)((dist2/dist1)*(right-left));
        int sy = 10;

        //System.out.printf("%d %d %d\n", sx,ex,sy);

        g2d.drawLine(sx, sy, ex, sy);

        g2d.drawLine(sx, sy-5, sx, sy+5);
        g2d.drawLine(ex, sy-5, ex, sy+5);

        for (double i=sx;i<ex;i+=(ex-sx)/10.0)
        {
            g2d.drawLine((int)i, sy, (int)i, sy-5);
        }

        String s = "m";
        if (dist2 >= 1000)
        {
            dist2 /= 1000;
            s = "Km";
        }
        g2d.drawString(String.format("%.0f%s", dist2, s), sx+2, sy+10);
    }

    /*
     * puts start and finish points on screen with their time
     *
    */
     private void drawStartAndFinish() {
        //DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss");
        //DateTime t = ((LocalDateTime) App.track.firstEntry().getKey()).toDateTime(DateTimeZone.UTC);
    
        GpsPoint p = (FRDL.GpsPoint) App.track.firstEntry().getValue();
        //drawPoint(projectMapToScreen(p.getLon(),p.getLat()),Color.GREEN,"Start " + t.toString(fmt));
        drawPoint(projectMapToScreen(p.getLon(),p.getLat()),Color.GREEN,"");

        //t = ((LocalDateTime) App.track.lastEntry().getKey()).toDateTime(DateTimeZone.UTC);
        p = (FRDL.GpsPoint) App.track.lastEntry().getValue();
        //drawPoint(projectMapToScreen(p.getLon(),p.getLat()),Color.RED,"Finish " + t.toString(fmt));
        drawPoint(projectMapToScreen(p.getLon(),p.getLat()),Color.RED,"");
    }


    /*
     * draws a circular point
     * with some text if txt length > 0
     *
    */
    private void drawPoint(Point2D p, Color c, String txt) {
        g2d.setStroke (new BasicStroke (1));
        Ellipse2D e = new Ellipse2D.Double(p.getX() - WAYPOINT_SIZE/2.0,
                                            p.getY() - WAYPOINT_SIZE/2.0,
                                            WAYPOINT_SIZE,
                                            WAYPOINT_SIZE);
        g2d.setPaint(c);
        g2d.fill(e);
        g2d.setPaint (LEGEND_COLOUR);
        g2d.draw (e);
        if (txt.trim().length() > 0) {
            g2d.setFont(new Font("Arial", 0, FONT_SIZE));
            g2d.drawString(txt,(float) p.getX()+ WAYPOINT_SIZE,(float) p.getY());
        }

    }

    /*
     * writes various interisting stats about the track to screen
     */
    private void drawStats () {
        g2d.setPaint (LEGEND_COLOUR);
        g2d.setFont(new Font("Arial", 0, FONT_SIZE));
        String st = "Track: " + App.track.size() + " Fixes" + ", " +
                toDist(totalDist);

        String timeType = (App.thisChampionship.getItemAsBoolean("championship.adjustIgcFileTimes", false)) ? " Local." : " UTC.";

        DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss");
        DateTimeFormatter fmt2 = DateTimeFormat.forPattern("d MMM yyyy HH:mm:ss");
        DateTime start = ((LocalDateTime) App.track.firstEntry().getKey()).toDateTime(DateTimeZone.UTC);
        DateTime end = ((LocalDateTime) App.track.lastEntry().getKey()).toDateTime(DateTimeZone.UTC);
        if (end.getDayOfYear() > start.getDayOfYear()) {
            st = st + ", Start: " + start.toString(fmt2) + timeType + " Finish: " + end.toString(fmt2) + timeType;
        } else {
            st = st + ", Start: " + start.toString(fmt) + timeType + " Finish: " + end.toString(fmt) + timeType;
        }
        g2d.drawString(st,5,getHeight()-5);
    }

    // returns a world coordinate from a screen coordinate
    private Point2D projectScreenToMap(double x, double y)
    {
        double lon = x;
        double lat =-y;

        lon -= getWidth() /2.0;
        lat += getHeight()/2.0;

        lon /= scale;
        lat /= scale;

        lon = lon + this.centreLon;

        lat = Math.toRadians(lat);
        lat+= Math.log(Math.tan(Math.PI*(0.25 + this.centreLat/360)));
        lat = Math.atan(Math.sinh(lat));

        lat = Math.toDegrees(lat);

        return new Point2D.Double(lon, lat);
    }


    private Point2D projectScreenToMap(Point2D p)
    {
        return projectScreenToMap(p.getX(), p.getY());
    }

    /*
     * generates a raw distance in metres to formatted m or Km
    */
    private String toDist (double d) {
        int i = (int) d;
        if (d > 1000) {
            return String.format("%.3f", d * 0.001) + " Km";
        } else {
            return String.format("%.0f", d) + "m";
        }
    }


}
