package FRDL;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JPanel;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;

/**
 * creates an altitude against time graph
 * @author rmh
 * with MUCH help from UTiGPX source code (even though it has a scaling error)
 */
public class AltitudeView extends JPanel {

    private static final Color LEGEND_COLOUR  = Color.BLACK;
    private static final Color TRACK_COLOUR_A     = new Color(0,0,153); //Color.BLUE;
    private static final Color TRACK_COLOUR_V     = new Color(255,0,0); //red 
    private static final Color TRACK_COLOUR_X     = new Color(153,153,153); //gray
    private static final int        LINE_SIZE       = 2;
    private static final int        FONT_SIZE       = 11;
    private static final int        PADDING = 20;

    private Graphics2D g2d;
    private boolean maxMinDone = false;
    private double max_alt;
    private double min_alt;
    private DateTime start_time;
    private DateTime end_time;
    private long trackDurationSec = 0;
    private double vScale;
    private double hScale;

    private static final long GAP_BETWEEN_TRACKS = 30; //threshold in sec before a new line is generated
    private ArrayList <TrackPath> trackPaths = null;

    /* this is the inherited method to actually draw in the component
     *
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
                //g2d.draw(drawTrack());  //one single track; not used
                if (drawTracks()) {
                    for (TrackPath tp : trackPaths) {
                        g2d.setPaint(tp.getColour());
                        g2d.draw(tp.getGp());
                    }
                }
                drawStats();
            } 

            //System.out.println("DONE ALT DRAWING");
        } else {
           // clear?
            //g2d.setColor(Color.WHITE);
            //g2d.fill(getVisibleRect());
            g2d.setPaint(Color.BLACK);
            g2d.setFont(new Font("Arial", 0, 20));
            FontMetrics fm = g2d.getFontMetrics();
            String str = App.mapCaption;
            Rectangle2D area = fm.getStringBounds(str, g2d);
            g2d.drawString(str, (int)(getWidth() - area.getWidth())/2,(int)(getHeight() + area.getHeight())/2);
        }
    }

    /*
     * creates a single line to draw on the screen
     * NOT USED see drawTracks()
     */
    private GeneralPath drawTrack () {
        GeneralPath gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        Double alt = ((GpsPoint) App.track.firstEntry().getValue()).getAlt();
        DateTime t = ((LocalDateTime) App.track.firstEntry().getKey()).toDateTime(DateTimeZone.UTC);
        long elapsed = new Duration(start_time,t).getStandardSeconds();

        Point2D p2 = projectMapToScreen(elapsed,alt);
        gp.moveTo(p2.getX(),p2.getY()); //track start point
        for (Iterator it=App.track.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry)it.next();
            alt = ((GpsPoint) entry.getValue()).getAlt();
            t = ((LocalDateTime) entry.getKey()).toDateTime(DateTimeZone.UTC);
            elapsed = new Duration(start_time,t).getStandardSeconds();
            
            p2 = projectMapToScreen(elapsed,alt);
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
        Double alt = null;
        Point2D p2 = null;
        String fixValidity = "X";
        
        DateTime t = ((LocalDateTime) App.track.firstEntry().getKey()).toDateTime(DateTimeZone.UTC);
        long elapsed = new Duration(start_time,t).getStandardSeconds();
        long lastElapsed = elapsed;
        Color colour = TRACK_COLOUR_A;

        for (Iterator it=App.track.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry)it.next();
            alt = ((GpsPoint) entry.getValue()).getAlt();
            t = ((LocalDateTime) entry.getKey()).toDateTime(DateTimeZone.UTC);
            elapsed = new Duration(start_time,t).getStandardSeconds();
            p2 = projectMapToScreen(elapsed,alt);
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
            } else {
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
     * converts points converted to screen scale
     */
    private Point2D projectMapToScreen(long elapsed, double alt)    {
        double x = (elapsed  * hScale) + PADDING;
        double y = (getHeight() + (((alt - min_alt) * vScale) * -1)) - PADDING;
        return new Point2D.Double(x, y);
    }

    /*
     * draws various interisting stats on the screen
    */
    private void drawStats() {
        //altitude min & max at top of frame
        g2d.setPaint (LEGEND_COLOUR);
        g2d.setFont(new Font("Arial", 0, FONT_SIZE));
        String st = "Altitude: Min " + metersToFeet(min_alt) + " ft, Max " + metersToFeet(max_alt) + " ft" ;
        g2d.drawString(st,5,15);

        //and pilot info at the bottom of the frame
        st = "Pilot: " +
                Utilities.repeatString("0", 3 - App.logr.loggerFileContent.readValue("pilot.compNo").length()) +
                App.logr.loggerFileContent.readValue("pilot.compNo") + ", " +
                App.logr.loggerFileContent.readValue("pilot.name") + " [" +
                App.logr.loggerFileContent.readValue("pilot.nation") + "]" +
                " logger " + App.logr.loggerFileContent.readValue("pilot.loggerPriority");
        g2d.drawString(st,5,getHeight()-5);
    }

    /*
     * sets the scale so the entire track is always visible
    */
    private void setScale() {
        if (App.track == null) return;
        try {
        //commented out so it properly recalculates every map
        //if (!maxMinDone) {
             //get the max and min lat & lon values of all Points
             //initialize max & min
             GpsPoint p = (FRDL.GpsPoint) App.track.firstEntry().getValue();
             max_alt = p.getAlt();
             min_alt = max_alt;
             start_time = ((LocalDateTime) App.track.firstEntry().getKey()).toDateTime(DateTimeZone.UTC);
             end_time = ((LocalDateTime) App.track.lastEntry().getKey()).toDateTime(DateTimeZone.UTC);
            trackDurationSec = new Duration(start_time,end_time).getStandardSeconds();
             

             //now iterate over the lot
            for (Iterator it=App.track.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry)it.next();
                max_alt = Math.max(max_alt, ((GpsPoint) entry.getValue()).getAlt());
                min_alt = Math.min(min_alt, ((GpsPoint) entry.getValue()).getAlt());
            }
             maxMinDone = true;
        //}
        vScale = 0.9999 * (getHeight()-(PADDING * 2)) / Math.abs(max_alt - min_alt);
        hScale = 0.9999 * (getWidth()-(PADDING * 2)) / trackDurationSec;
        } catch (Exception e) {
            
        }
        //System.out.println("height: " + getHeight() + " minAlt: " + min_alt + " maxAlt: " + max_alt +  " = vScale: "+ vScale);
        //System.out.println("width: " + getWidth() + " durationSec: " + trackDurationSec + " = hScale: "+ hScale);
    }

    /*
     * converts meters to feet
    */
    private int metersToFeet(double m) {
        return (int) ((int) m * 3.2808399);
    }
}
