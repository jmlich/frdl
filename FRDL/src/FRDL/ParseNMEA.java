package FRDL;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author rmh
 */
public class ParseNMEA {
    private final DateTimeFormatter nmeaDateFormat = DateTimeFormat.forPattern("ddMMyy");
    private final DateTimeFormatter nmeaTimeFormat = DateTimeFormat.forPattern("HHmmss");
    private final DateTimeFormatter printDTFormat = DateTimeFormat.forPattern("d MMM YYYY HH:mm");
    private LocalDate Ldate = null;
    private LocalTime Ltime  = null;
    private LocalDateTime recordDt = null;
    private String lat = null;
    private String lon = null;
    private String alt = null;
    private Double dLat = 0.0;
    private Double dLon = 0.0;
    private Double dAlt = 0.0;
    private String val = "X";
    private String loggerModel = null;
    private String loggerFirmwareVer = null;
    
    private static Pattern NMEA_PATTERN = Pattern.compile("^\\$(GPRMC|GPGGA|GPGSA|GPWPL|ADVER).+$", 32);
    private LocalDateTime windowOpen = null;
    private LocalDateTime windowClose = null;
    private int utcOffsetH = 0;
    private int utcOffsetM = 0;
    private GpsLogger logger = null;
    private TreeMap track;

    //originally GPGGA|GPRMC|GPGLL|GPGSA|GPGSV|GPVTG|GPRMC|ADPMB

   
    /*
     * the constructor 
     * Entry point
     * 
     */
    public void processNMEAfiles (GpsLogger logger, String outFileName, LocalDateTime windowOpen, LocalDateTime windowClose) {
        App.track = null;
        this.track = new TreeMap<LocalDateTime, GpsPoint>();
        //track = new TreeMap<LocalDateTime, GpsPoint>();
        this.utcOffsetH = Integer.valueOf(App.thisChampionship.champData.readValue("championship.utcOffsetH"));
        this.utcOffsetM = Integer.valueOf(App.thisChampionship.champData.readValue("championship.utcOffsetM"));
        if (utcOffsetH >= 0) {
            this.windowOpen = windowOpen.minusHours(Math.abs(this.utcOffsetH)).minusMinutes(utcOffsetM);
            this.windowClose = windowClose.minusHours(this.utcOffsetH).minusMinutes(utcOffsetM);
        } else {
            this.windowOpen = windowOpen.plusHours(Math.abs(this.utcOffsetH)).plusMinutes(utcOffsetM);
            this.windowClose = windowClose.plusHours(this.utcOffsetH).plusMinutes(utcOffsetM);
        }
        //this.windowOpen = windowOpen;
        //this.windowClose = windowClose;
        this.logger = logger;

        for (int i = 0;i < logger.allLogFiles.size();i++) {
            File f = (File) logger.allLogFiles.get(i);
            if (checkWindow(f,this.windowOpen,this.windowClose)) {
                readNMEAfile(f.getAbsolutePath());
            }
        }
        if (track.size() == 0) {
            track = null;
        } else {
            setPointDistances();
            //I don't think this is a deep copy, but it seems to do the trick
            //to solve a big problem of the maps which were looking at
            //the track before it was complete and getting errors
            App.track = track;

        }
        writeIGCfile (outFileName);
    }

    /*
     * checks whether there are any points in the file within the task window
     * as the process of getting the start and finish times from a NMEA file
     * returns null if no NMEA is found, this method also has the effect of
     * rejecting any non-NMEA files
     */
    private Boolean checkWindow(File f, LocalDateTime windowOpen, LocalDateTime windowClose) {
        LocalDateTime[] ldt = CheckNMEAfile.getStartAndFinishTimes(f.getAbsolutePath());
        if (ldt[0] != null || ldt[1] != null) {
            if (ldt[0].isBefore(windowOpen) && ldt[1].isAfter(windowOpen) && ldt[1].isBefore(windowClose)) return true;
            if (ldt[0].isBefore(windowOpen) && ldt[1].isAfter(windowClose)) return true;
            if (ldt[0].isAfter(windowOpen) && ldt[1].isBefore(windowClose)) return true;
            if (ldt[0].isAfter(windowOpen)&& ldt[0].isBefore(windowClose) && ldt[1].isAfter(windowClose) ) return true;
        }
       return false;
    }

    private void readNMEAfile (String logFileName) {
        try {
        BufferedReader in = new BufferedReader(new FileReader(logFileName));
        String str;
        while ((str = in.readLine()) != null) {
             if (null != str && NMEA_PATTERN.matcher(str).matches())  {
                 //if (checkSumOk(str)) {
                     processNMEAline(str);
                 //}
            } 
        }
        in.close();
        } catch (IOException e) {
            //catch here
        }
    }

    private void processNMEAline (String str) {
        Boolean wayPointDetected = false;
        String pevDescr = "";
        String sb[] = str.split(",");
        int len = sb.length;
        if (sb[0].compareTo("$GPRMC") == 0 && len >= 9) {
            //NMEA RMC line has date,time,lat,lon but no alt or fix validity
            Ldate = new LocalDate(nmeaDateFormat.parseDateTime(sb[9]));
            Ltime = new LocalTime(nmeaTimeFormat.parseDateTime(sb[1].substring(0,6)));
            recordDt = new LocalDateTime(Ldate.toDateTime(Ltime).withZone(DateTimeZone.UTC));
            lat = sb[3].substring(0,4) + sb[3].subSequence(5, 8) + sb[4];
            lon = sb[5].substring(0,5) + sb[5].subSequence(6, 9) + sb[6];
            dLat = parseLat(sb[3],sb[4]);
            dLon = parseLon(sb[5],sb[6]);

        } else if (sb[0].compareTo("$GPGGA") == 0 && len >= 11) {
            //NMEA GGA line has time,lat,lon,alt but no date or fix validity
            if (Ldate != null) {
                Ltime = new LocalTime(nmeaTimeFormat.parseDateTime(sb[1].substring(0,6)));
                recordDt = new LocalDateTime(Ldate.toDateTime(Ltime).withZone(DateTimeZone.UTC));
                lat = sb[2].substring(0,4) + sb[2].subSequence(5, 8) + sb[3];
                lon = sb[4].substring(0,5) + sb[4].subSequence(6, 9) + sb[5];
                dLat = parseLat(sb[2],sb[3]);
                dLon = parseLon(sb[4],sb[5]);
                dAlt = Double.parseDouble(sb[9]);
                int iAlt = dAlt.intValue();
                alt = repeatString("0",5 - Integer.toString(iAlt).length()) + Integer.toString(iAlt);
                
                //System.out.println("gga alt" + alt + " - " + str);
            }
        } else if (sb[0].compareTo("$GPGSA") == 0 && len >= 2) {
            //NMEA GSA line has fix validity but no date,time,lat,lon,alt
            val = "X";
            if (sb[2].equals("3")) val = "A";
            if (sb[2].equals("2")) val = "V";
            //System.out.println("GSA len: " + len);

        } else if (sb[0].compareTo("$GPWPL") == 0 && len >= 5) {
            // waypoint has time,lat,lon no date,alt,fix validity
            // IGC only needs time and we fix it to be +500 ms so it
            // is definitely included in the resulting data map
            if (recordDt != null) {
                wayPointDetected = true;
                recordDt = recordDt.plusMillis(500);
                String[] q = sb[5].split("\\*");
                pevDescr = q[0];
            }
        } else if (sb[0].compareTo("$ADVER") == 0 && len >=3 ) {
            //proprietry header for AMOD  $ADVER,3080,2.2
            //assume this means model:3080 firmware 2.2
            loggerModel = sb[1];
            loggerFirmwareVer = sb[2];
        }
        
        if (recordDt != null && lat != null && lon != null && alt != null) {
            //window filter.  Does not write any points outside the task window
            //either to the igc file or to the track
            if (recordDt.isBefore(windowOpen)||recordDt.isAfter(windowClose)) return;

            //we can return an igc line

            //forced to adjust to local time?
            if (App.thisChampionship.getItemAsBoolean("championship.adjustIgcFileTimes", false)) {
                if (utcOffsetH >=0) {
                    recordDt = recordDt.minusHours(utcOffsetH).minusMinutes(utcOffsetM);
                } else {
                    recordDt = recordDt.plusHours(utcOffsetH).plusMinutes(utcOffsetM);
                }
            }
            //write the igc line
            String igcLine = "";
            if (wayPointDetected) {
                // E "Event" line
                igcLine = "E" +
                    nmeaTimeFormat.print(recordDt) +
                    "PEV" +
                    pevDescr;
                    track.put(recordDt, new GpsPoint(igcLine,"E",dLat,dLon,dAlt,0.0));
            } else {
                // Ordinary B fix line
                igcLine = "B" +
                    nmeaTimeFormat.print(recordDt) +
                    lat +
                    lon +
                    val +
                    //"00000" +
                    alt + //we are filling pressure alt with GPS alt
                    alt;
                track.put(recordDt,new GpsPoint(igcLine,"B",dLat,dLon,dAlt,0.0));
            }
        }
    }

    private void writeIGCfile (String fileName) {
        //START experiment various filtering exercises on the track
        //LocalDateTime start = new LocalDateTime("2009-01-15T16:02:00.000");
        //LocalDateTime end = new LocalDateTime("2009-01-15T16:19:59.000");
        //int count = App.track.subMap(start, end).size();
        //System.out.println("subset = " + count);

        //NMEA5App.track.headMap(start, false).clear();
        //map.subMap(map.firstEntry().getKey(), start).clear();
        //NMEA5App.track.tailMap(end, false).clear();

        //System.out.println("total fixes now: " + App.track.size());
        //END  experiment various filtering exercises on the track

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));

            out.write(makeIgcHeader());

            if (track != null) {
                    // For both the keys and values of a map
                for (Iterator it=track.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry entry = (Map.Entry)it.next();
                    //Object key = entry.getKey();
                    //Object value = entry.getValue();
                    out.write(((GpsPoint) entry.getValue()).getIgcString() + "\n");
                }
            } else {
                App.mapCaption = App.getResourceMap().getString("noTrackFoundMsg") +
                        " " +
                        printDTFormat.print(windowOpen) +
                        " & " +
                        printDTFormat.print(windowClose);
            }
            
            out.close();
            
            File f = new File(fileName);
            MainView.addLog("IGC file saved: " + f.getName());
            Boolean x = MainView.setMainStatus("Saved " + f.getName());
        } catch (IOException e) {
            MainView.addLog("ERROR: Failed to write the igc file: " + e);
        }
    }

    private String makeIgcHeader() {
        LocalDateTime dt = new LocalDateTime();
        String firstLogDt = "Unknown";
        if (track != null) {
            firstLogDt = nmeaDateFormat.print((LocalDateTime) track.firstKey());
        }
        
        String loggertype = App.logr.loggerFileContent.readValue("logger.type");

        String forcedTimezoneOffset = "+00:00";
                
        if (App.thisChampionship.getItemAsBoolean("championship.adjustIgcFileTimes", false)) {
            //forced adjustment of timezone?  nothing clever = simple string parsing
            //int ih = Integer.parseInt(App.thisChampionship.champData.readValue("championship.utcOffsetH"));
            //int im = Integer.parseInt(App.thisChampionship.champData.readValue("championship.utcOffsetM"));
            //forcedTimezoneOffset = String.format("%02d", ih) + ":" + String.format("%02d", im);
            //forcedTimezoneOffset = (ih >=0) ? "+" + forcedTimezoneOffset : "-" + forcedTimezoneOffset;

            forcedTimezoneOffset = Utilities.makeTimeZoneOffset (Integer.parseInt(App.thisChampionship.champData.readValue("championship.utcOffsetH")),
                    Integer.parseInt(App.thisChampionship.champData.readValue("championship.utcOffsetM")),
                    false);
        }

        //if any other items are directly extractable from a logger, add here instead of
        //directly below
        if (loggerModel == null) loggerModel = App.gpsLoggersMaster.getValue(loggertype,"model");
        if (loggerFirmwareVer == null) loggerModel = App.gpsLoggersMaster.getValue(loggertype,"firmwareVer");

        String header = "AXXXXXX" + 
                App.gpsLoggersMaster.getValue(loggertype, "make") + "," + loggerModel + "," + 
                App.logr.loggerFileContent.readValue("logger.uuid") + "\n" +
                "HSDTE" + firstLogDt + "\n" +
                "HSFXA010\n" +
                "HSPLTPILOT:" + 
                App.logr.loggerFileContent.readValue("pilot.name") + "\n" +
                "HSDTM100GPSDATUM:WGS84\n" +
                "HSRFWFIRMWAREVERSION:" + loggerFirmwareVer + "\n" +
                "HSRHWHARDWAREVERSION:" + 
                App.gpsLoggersMaster.getValue(loggertype, "make") + "," + loggerModel + "\n" +
                "HSFTYFRTYPE:" + 
                App.gpsLoggersMaster.getValue(loggertype, "make") + "," + loggerModel + "," + 
                App.logr.loggerFileContent.readValue("logger.uuid") + "\n" +
                "HSGPS:" + 
                App.gpsLoggersMaster.getValue(loggertype, "GPSmfg") +
                "," + 
                App.gpsLoggersMaster.getValue(loggertype, "GPSmodel") + 
                "," + 
                App.gpsLoggersMaster.getValue(loggertype, "GPSchannels") + 
                "," + 
                App.gpsLoggersMaster.getValue(loggertype, "GPSmaxAlt") + "\n" +
                "HSPRS:NOTFITTED\n" +
                "HSDFSFILESPECIFICATION:"  + 
                "CIMA," + App.getResourceMap().getString("meetsCimaSpecificationYear") + "\n" +
                "HSCIDCOMPETITIONID:" + 
                App.logr.loggerFileContent.readValue("pilot.compNo") + "\n" +
                //I records here = none for CIMA spec
                "LCMASTSNDATATRANSFERSOFTWARENAME:" + 
                App.getResourceMap().getString("Application.shortTitle") + "\n" +
                "LCMASTSVDATATRANSFERSOFTWAREVERSION:" + 
                App.getResourceMap().getString("Application.version") + "\n" +
                "LCMASTSDDATATRANSFERDATE:" + nmeaDateFormat.print(dt) + "\n" +
                "LCMASTSTDATATRANSFERTIME:" + nmeaTimeFormat.print(dt) + "\n" +
                "LCMASTSKTASKNUMBER:" + 
                App.thisChampionship.champData.readValue("championship.activeTask") + "\n" +
                "LCMASPRSPRESSALTFILL:GNSSALT" + "\n" +
                "LCMASTZNTIMEZONEOFFSET:" + forcedTimezoneOffset + "\n" +
                "LCMASDTETRACKDATE:" + firstLogDt + "\n"
                ;
        return header;
    }

    
    private String repeatString(String st, int i) {
         String tst = "";
         for(int j = 0; j < i; j++) {
             tst = tst+ st;
         }
         return tst;
    }

         /**
     * Parses latitude given values from GPS device.
     * @param s Latitude String from GPS device in ddmm.mm format.
     * @param d Latitude hemisphere, "N" for northern, "S" for southern.
     * @return Latitude parsed from GPS data, with appropriate sign based on hemisphere or
     * 90.0 if invalid latitude provided.
     */
     private double parseLat(String s, String d)    {
         double _lat = Double.parseDouble(s);
         if (_lat < 99999.0) {
             double lat = (double)((long)_lat / 100L); // _lat is always positive here
             lat += (_lat - (lat * 100.0)) / 60.0;
             return d.equals("S")? -lat : lat;
         } else {
             return 90.0; // invalid latitude
         }
     }

     /**
     * Parses longitude given values from GPS device.
     * @param s Longitude String from GPS device in ddmm.mm format.
     * @param d Longitude hemisphere, "E" for eastern, "W" for western.
     * @return Longitude parsed from GPS data, with appropriate sign based on hemisphere or
     * 180.0 if invalid longitude provided.
     */
     private double parseLon(String s, String d)
     {
         double _lon = Double.parseDouble(s);
         if (_lon < 99999.0) {
             double lon = (double)((long)_lon / 100L); // _lon is always positive here
             lon += (_lon - (lon * 100.0)) / 60.0;
             return d.equals("W")? -lon : lon;
         } else {
             return 180.0; // invalid longitude
         }
     }

     /*
      * once the track is made then we can iterate with this over all entries in it
      * (because they are definitely now in order) and set the dist from
      * the previous point in metres
     */

     private void setPointDistances () {
         GpsPoint lastPoint = null;
         // For both the keys and values of a map
        for (Iterator it=track.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry)it.next();
            GpsPoint p = (GpsPoint) entry.getValue();
            if (p.getPointType().equals("B")) {
                //ignore anything but B fixes (eg E events)
                p.setDist(distanceTo(p, lastPoint));
                entry.setValue(p);
                lastPoint = p;
            }
        }
     }

    /**
    * Get the distance between 2 points
    * @param GpsPoint pt1 a point
    * @param GpsPoint pt2 another point
    * @return Double value of the distance in metres
    * Uses a simple spherical model of the earth, NOT WGS84 so is
    * only approximate, but adequate for purpose
    *
    */
    public double distanceTo(GpsPoint pt1, GpsPoint pt2)
    {
        if (pt1 == null || pt2 == null) return (double)0.0;
        double lat1 = Math.toRadians(pt1.getLat());
        double lon1 = Math.toRadians(pt1.getLon());
        double lat2 = Math.toRadians(pt2.getLat());
        double lon2 = Math.toRadians(pt2.getLon());
        double radius = 6378100;			// Approx radius of the earth in meters

        // Spherical law of cosines
        double distance = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2-lon1)) * radius;

        // Temporary fix for if acos returns NaN
        if ("NaN".equals("" + distance)) { distance = 0.0; }

        //System.out.println("dist: " + distance);
        
        return distance;
    }

}