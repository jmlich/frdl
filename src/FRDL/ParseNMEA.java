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
    private String alt = "00000";
    private Double dLat = 0.0;
    private Double dLon = 0.0;
    private Double dAlt = 0.0;
    private String fixValidity = "X";
    private String loggerModel = null;
    private String loggerFirmwareVer = null;
    private int lastDay = -1;
    
    private static Pattern NMEA_PATTERN = Pattern.compile("^\\$(GPRMC|GPGGA|GPGSA|GPWPL|ADVER).+$", 32);
    private LocalDateTime windowOpen = null;
    private LocalDateTime windowClose = null;
    private int utcOffsetH = 0;
    private int utcOffsetM = 0;
    private GpsLogger logger = null;
    private TreeMap track;

    //private static final int secondsBeforeDisconnect = 30;
    //

    private static final String crlf = "\r\n"; //\r\n = CFLF

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
            //System.out.println("About to check: " + f.getAbsolutePath());
            if (checkWindow(f,this.windowOpen,this.windowClose)) {
                readNMEAfile(f.getAbsolutePath());
            }
        }
        if (track.size() == 0) {
            track = null;
        } else {
            setValidEvents();
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
        if (ldt[0] != null && ldt[1] != null) {
            if (ldt[0].isBefore(windowOpen) && ldt[1].isAfter(windowOpen) && ldt[1].isBefore(windowClose)) return true;
            if (ldt[0].isBefore(windowOpen) && ldt[1].isAfter(windowClose)) return true;
            if (ldt[0].isAfter(windowOpen) && ldt[1].isBefore(windowClose)) return true;
            if (ldt[0].isAfter(windowOpen)&& ldt[0].isBefore(windowClose) && ldt[1].isAfter(windowClose) ) return true;
        } else {
            String warning = "WARNING cannot find ";
            if (ldt[0] == null) warning = warning + "starting ";
            if (ldt[0] == null && ldt[1] == null) warning = warning + "and ";
            if (ldt[1] == null) warning = warning + "ending ";
            MainView.addLog(warning + "date-time in " + f.getName() +
                    " (only significant if this is a NMEA log file.)");
        }
       return false;
    }

    private void readNMEAfile (String logFileName) {
        try {
        BufferedReader in = new BufferedReader(new FileReader(logFileName));
        String str;
        String fName = (new File(logFileName)).getName();
        int lineCtr = 0;
        while ((str = in.readLine()) != null) {
            lineCtr++;
             if (null != str && NMEA_PATTERN.matcher(str).matches())  {
                 //if (checkSumOk(str)) {
                     processNMEAline(str, lineCtr, fName);
                 //}
            } 
        }
        in.close();
        } catch (IOException e) {
            //catch here
        }
    }

    private void processNMEAline (String str, int lineNo, String fileName) {
        Boolean wayPointDetected = false;
        String pevDescr = "";
        String sb[] = str.split(",");
        int len = sb.length;
        if (sb[0].compareTo("$GPRMC") == 0 && len >= 9) {
            //NMEA RMC line has date,time,lat,lon but no alt or fix validity
            try {
                Ldate = new LocalDate(nmeaDateFormat.parseDateTime(sb[9]));
                Ltime = new LocalTime(nmeaTimeFormat.parseDateTime(sb[1].substring(0,6)));
                recordDt = new LocalDateTime(Ldate.toDateTime(Ltime).withZone(DateTimeZone.UTC));
                lat = sb[3].substring(0,4) + sb[3].substring(5, 8) + sb[4];
                lon = sb[5].substring(0,5) + sb[5].substring(6, 9) + sb[6];
                dLat = parseLat(sb[3],sb[4]);
                dLon = parseLon(sb[5],sb[6]);
            } catch (Exception e) {
                MainView.addLog("ERROR parsing RMC line " + Integer.toString(lineNo) + " in " + fileName + " ["  + str + "] " + e);
            }

        } else if (sb[0].compareTo("$GPGGA") == 0 && len >= 11) {
            //NMEA GGA line has time,lat,lon,alt but no date or fix validity
            if (Ldate != null) {
                try {
                    Ltime = new LocalTime(nmeaTimeFormat.parseDateTime(sb[1].substring(0,6)));
                    recordDt = new LocalDateTime(Ldate.toDateTime(Ltime).withZone(DateTimeZone.UTC));
                    lat = sb[2].substring(0,4) + sb[2].substring(5, 8) + sb[3];
                    lon = sb[4].substring(0,5) + sb[4].substring(6, 9) + sb[5];
                    dLat = parseLat(sb[2],sb[3]);
                    dLon = parseLon(sb[4],sb[5]);
                    dAlt = Double.parseDouble(sb[9]);
                    int iAlt = dAlt.intValue();
                    if (iAlt < 0) {
                        //negative altitude
                        iAlt = Math.abs(iAlt);
                        alt = "-" + repeatString("0",4 - Integer.toString(iAlt).length()) + Integer.toString(iAlt);
                    } else {
                        //positive altitude
                        alt = repeatString("0",5 - Integer.toString(iAlt).length()) + Integer.toString(iAlt);
                    }
                } catch (Exception e) {
                    MainView.addLog("ERROR parsing GGA line " + Integer.toString(lineNo) + " in " + fileName + " ["  + str + "] " + e);
                }
                //System.out.println("gga alt" + alt + " - " + str);
            }
        } else if (sb[0].compareTo("$GPGSA") == 0 && len >= 2) {
            //NMEA GSA line has fix validity but no date,time,lat,lon,alt
            try {
                fixValidity = "X";
                if (sb[2].equals("3")) fixValidity = "A";
                if (sb[2].equals("2")) fixValidity = "V";
                //System.out.println("GSA len: " + len);
            } catch (Exception e) {
                MainView.addLog("ERROR parsing GSA line " + Integer.toString(lineNo) + " in " + fileName + " ["  + str + "] " + e);
            }
        } else if (sb[0].compareTo("$GPWPL") == 0 && len >= 5) {
            // waypoint has time,lat,lon no date,alt,fix validity
            // IGC only needs time and we fix it to be +500 ms so it
            // is definitely included in the resulting data map
            if (recordDt != null) {
                try {
                    wayPointDetected = true;
                    recordDt = recordDt.minusMillis(500);
                    String[] q = sb[5].split("\\*");
                    pevDescr = q[0];
                } catch (Exception e) {
                    MainView.addLog("ERROR parsing WPL line " + Integer.toString(lineNo) + " in " + fileName + " ["  + str + "] " + e);
                }
            }
        } else if (sb[0].compareTo("$ADVER") == 0 && len >=3 ) {
            //proprietry header for AMOD  $ADVER,3080,2.2
            //assume this means model:3080 firmware 2.2
            try {
                loggerModel = sb[1];
                loggerFirmwareVer = sb[2];
            } catch (Exception e) {
                MainView.addLog("ERROR parsing ADVER line " + Integer.toString(lineNo) + " in " + fileName + " ["  + str + "] " + e);
            }
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

            String igcLine = "";

            //go into a new day?  for full CIMA spec need to write a 'L' record with the
            //new day.  Probably no analysis software will read it, but it's there for
            //the record and can be spotted quite easily manually....
            if (lastDay >= 0 && recordDt.getDayOfYear() != lastDay ) {
                //make a time -250ms so it definitely gets included in the resulting data map
                //at the row immediately before the day change
                LocalDateTime dtX = recordDt.minusMillis(250);
                igcLine = "LCMASDTETRACKDATE:" + nmeaDateFormat.print(dtX);
                //write it to the track
                track.put(dtX, new GpsPoint(igcLine,"L",fixValidity,dLat,dLon,dAlt,0.0));
                igcLine = "";
            }
            lastDay = recordDt.getDayOfYear();
            
            //write the normal igc line
            if (wayPointDetected) {
                // E "Event" line
                igcLine = "E" +
                    nmeaTimeFormat.print(recordDt.plusMillis(500)) +
                    "PEV" +
                    pevDescr;
                    track.put(recordDt, new GpsPoint(igcLine,"E",fixValidity,dLat,dLon,dAlt,0.0));
            } else {
                // Ordinary B fix line
                igcLine = "B" +
                    nmeaTimeFormat.print(recordDt) +
                    lat +
                    lon +
                    fixValidity +
                    //"00000" +
                    alt + //we are filling pressure alt with GPS alt
                    alt;
                track.put(recordDt,new GpsPoint(igcLine,"B",fixValidity,dLat,dLon,dAlt,0.0));
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
                LocalDateTime lastKey = (LocalDateTime) track.firstKey();
                    // For both the keys and values of a map
                for (Iterator it=track.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry entry = (Map.Entry)it.next();
                    //Object key = entry.getKey();
                    //Object value = entry.getValue();
                    // we are now NOT writing invalid gps points to igc
                    // unless forced by user
                    GpsPoint p = (GpsPoint) entry.getValue();
                    Boolean writeLine = true;
                    if (p.fixValidity.equals("X")) {
                        writeLine = App.includeInvalidFixesInIgcFile;
                        //System.out.println(writeLine + " " + p.getIgcString());
                    }
                    if (writeLine)  {

                        out.write(p.getIgcString() + crlf);
                    }
                    lastKey = (LocalDateTime) entry.getKey();
                    //out.write(((GpsPoint) entry.getValue()).getIgcString() + crlf); //\r\n = CFLF
                }
            } else {
                App.mapCaption = App.getResourceMap().getString("noTrackFoundMsg") +
                        " " +
                        printDTFormat.print(windowOpen) +
                        " & " +
                        printDTFormat.print(windowClose);
            }
            
            out.close();
            
            //always set this back to default
            App.includeInvalidFixesInIgcFile = false;
            
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
                App.logr.loggerFileContent.readValue("logger.uuid") + crlf +
                "HSDTE" + firstLogDt + crlf +
                "HSFXA010" + crlf +
                "HSPLTPILOT:" + 
                App.logr.loggerFileContent.readValue("pilot.name") + crlf +
                "HSDTM100GPSDATUM:WGS84" + crlf +
                "HSRFWFIRMWAREVERSION:" + loggerFirmwareVer + crlf +
                "HSRHWHARDWAREVERSION:" + 
                App.gpsLoggersMaster.getValue(loggertype, "make") + "," + loggerModel + crlf +
                "HSFTYFRTYPE:" + 
                App.gpsLoggersMaster.getValue(loggertype, "make") + "," + loggerModel + "," + 
                App.logr.loggerFileContent.readValue("logger.uuid") + crlf +
                "HSGPS:" + 
                App.gpsLoggersMaster.getValue(loggertype, "GPSmfg") +
                "," + 
                App.gpsLoggersMaster.getValue(loggertype, "GPSmodel") + 
                "," + 
                App.gpsLoggersMaster.getValue(loggertype, "GPSchannels") + 
                "," + 
                App.gpsLoggersMaster.getValue(loggertype, "GPSmaxAlt") + crlf +
                "HSPRS:NOTFITTED" + crlf +
                "HSDFSFILESPECIFICATION:"  + 
                "CIMA," + App.getResourceMap().getString("meetsCimaSpecificationYear") + crlf +
                "HSCIDCOMPETITIONID:" + 
                App.logr.loggerFileContent.readValue("pilot.compNo") + crlf +
                //I records here = none for CIMA spec
                "LCMASTSNDATATRANSFERSOFTWARENAME:" + 
                App.getResourceMap().getString("Application.shortTitle") + crlf +
                "LCMASTSVDATATRANSFERSOFTWAREVERSION:" + 
                App.getResourceMap().getString("Application.version") + crlf +
                "LCMASTSDDATATRANSFERDATE:" + nmeaDateFormat.print(dt) + crlf +
                "LCMASTSTDATATRANSFERTIME:" + nmeaTimeFormat.print(dt) + crlf +
                "LCMASTSKTASKNUMBER:" + 
                App.thisChampionship.champData.readValue("championship.activeTask") + crlf +
                "LCMASPRSPRESSALTFILL:GNSSALT" + crlf +
                "LCMASTZNTIMEZONEOFFSET:" + forcedTimezoneOffset + crlf +
                "LCMASDTETRACKDATE:" + firstLogDt + crlf
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

          /*
      * once the track is made then we can iterate with this over all entries in it
      * (because they are definitely now in order) PEV's were set in the map at
      * time - 500ms and there MUST be a B record with that time + 500ms (ie on the whole second).
      * Probably there is one, but if there isn't, then we insert one copied from the
      * the previous B record
     */

     private void setValidEvents () {
         GpsPoint lastPoint = (GpsPoint) track.firstEntry().getValue();
         LocalDateTime bKey;
         // For both the keys and values of a map
        for (Iterator it=track.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry)it.next();
            GpsPoint p = (GpsPoint) entry.getValue();
            if (p.getPointType().equals("B")) {
                lastPoint = p;
            }
            if (p.getPointType().equals("E")) {
                //ignore anything but E events (eg B fixes)
                bKey = ((LocalDateTime) entry.getKey()).plusMillis(500);
                if (!track.containsKey(bKey)) {
                    //then we need to put one in
                    //System.out.println("need put in key at " + bKey.toString() + " with value " + lastPoint.toString());
                    //use lastPoint, but change the time in the igcString
                    String oldIgcString = lastPoint.getIgcString();
                    String newIgcLine = "B" +
                        nmeaTimeFormat.print(bKey) +
                        oldIgcString.substring(7, oldIgcString.length());
                    lastPoint.setIgcString(newIgcLine);
                    //put it in the map
                    track.put(bKey,lastPoint);
                    //System.out.println("new igc line is: " + newIgcLine);
                }
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
