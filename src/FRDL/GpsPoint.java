package FRDL;

/**
 * A gpsPoint is just that.
 * is filled with the igcString and other more
 * detailed info useful for drawing the two track views
 * @author rmh
 */
public class GpsPoint {
    public String igcString;
    public String pointType;
    public String fixValidity;
    public double lat;
    public double lon;
    public double alt;
    public double dist;

    /**
     * Constructor with pre-existing information
     * @param _igcString String the complete igc string
     * @param _pointType String igc line type, will be B or L or E
     * @param _fixValidity String : A = 3-D fix, V = 2-D fix and X = unknown.
     * @param _lat	double latitude
     * @param _lon	double longitude
     * @param _alt  double altitude
     * @param _dist double dist from previous fix in m
     */
    public GpsPoint(String _igcString,
                    String _pointType,
                    String _fixValidity,
                    double _lat,
                    double _lon,
                    double _alt,
                    double _dist)    {
        igcString    = _igcString;
        pointType   = _pointType;
        fixValidity = _fixValidity;
        lat     = _lat;
        lon     = _lon;
        alt     = _alt;
        dist    = _dist;
    }


    /**
     * Returns igcString parameter
     * @return igcString
     */
    public String getIgcString() {
        return igcString;
    }

    /**
     * Set igcString
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
     * Returns altitude parameter
     * @return alt
     */
    public double getAlt() {
        return alt;
    }

    /**
     * Set altitude
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

    public String getFixValidity() {
        return fixValidity;
    }

    public void setFixValidity(String x) {
        fixValidity = x;
    }

    @Override
    public String toString() {
        return getIgcString() + "," +
                getPointType() + "," +
                getFixValidity() + "," + 
                getLat() + "," +
                getLon() + "," +
                getAlt() + "," +
                getDist();
    }

}
