/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FRDL;

/**
 *
 * @author rmh
 */
public class GpsPoint {
    public String igcString;
    public String pointType;
    public double lat;
    public double lon;
    public double alt;

    //added by Nathan
    // This is so my elevation view can project on this dimension,
    // which is distance from track start.
    // If you can think of a slicker way to do this, by all means be my guest.
    public double dist;

    /**
     * Constructor with pre-existing information
     * @param _name String name
     * @param _desc String description
     * @param _lat	double latitude
     * @param _lon	double longitude
     * @param _ele  double elevation
     * @param _time long time
     */
    public GpsPoint(String _igcString,
                    String _pointType,
                    double _lat,
                    double _lon,
                    double _alt,
                    double _dist)    {
        igcString    = _igcString;
        pointType   = _pointType;
        lat     = _lat;
        lon     = _lon;
        alt     = _alt;
        dist    = _dist;
    }


    /**
     * Returns name parameter
     * @return name
     */
    public String getIgcString() {
        return igcString;
    }

    /**
     * Set Waypoint name
     * @param String x
     */
    public void setIgcString(String x) {
        igcString = x;
    }


    /**
     * Returns Latitude parameter
     * @return Latitude
     */
    public double getLat() {
        return lat;
    }

    /**
     * Set Latitude
     * @param double x
     */
    public void setLat(double x) {
        lat = x;
    }

    /**
     * Returns Longitude parameter
     * @return longitude
     */
    public double getLon() {
        return lon;
    }

    /**
     * Set longitude
     * @param double x
     */
    public void setLon(double x) {
        lon = x;
    }

    /**
     * Returns elevation parameter
     * @return elevation
     */
    public double getAlt() {
        return alt;
    }

    /**
     * Set elevation
     * @param double x
     */
    public void setAlt(double x) {
        alt = x;
    }
    
    public Double getDist() {
        return dist;
    }

    public void setDist(double x) {
        dist = x;
    }

    public String getPointType() {
        return pointType;
    }

    public void setPointType(String x) {
        pointType = x;
    }

    @Override
    public String toString() {
        return getIgcString() + "," +
                getPointType() + "," +
                getLat() + "," +
                getLon() + "," +
                getAlt() + "," +
                getDist();

    }

}
