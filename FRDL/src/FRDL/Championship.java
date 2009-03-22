/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FRDL;

import java.io.File;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author rmh
 */
public class Championship {
    public File fChamp = null;
    public PropertiesIO champData = null;
    private LocalDateTime defaultLocalDT = new LocalDateTime();

    //the constructor
    Championship (File f) {
        this.fChamp = f;
        this.champData = new PropertiesIO(f.getAbsolutePath());

    }

    public String getItemAsString(String key) {
        return (champData.readValue(key) == null) ? "" : champData.readValue(key);
    }

    public String getItemAsString(String key, String defaultValue) {
        return (champData.readValue(key) == null) ? defaultValue : champData.readValue(key);
    }

    public int getItemAsInt(String key, int defaultValue) {
        return (champData.readValue(key) == null) ? defaultValue : Integer.parseInt(champData.readValue(key));
    }

    public Boolean getItemAsBoolean(String key, Boolean defaultValue) {
        return (champData.readValue(key) == null) ? defaultValue : Boolean.valueOf(champData.readValue(key)).booleanValue();
        //return Boolean.valueOf(champData.readValue(key)).booleanValue();
    }

    public LocalDateTime getItemAsLocalDT(String key) {
        String sDt = (champData.readValue(key) == null) ? "" : champData.readValue(key).trim();
        if (sDt.length() > 0) {
            DateTime dt = new DateTime(sDt);
            return new LocalDateTime(dt);
        } else {
            return defaultLocalDT;
        }
    }

    public DateTime getItemAsDT(String key) {
        String sDt = (champData.readValue(key) == null) ? "" : champData.readValue(key).trim();
        if (sDt.length() > 0) {
            return new DateTime(sDt).withZone(DateTimeZone.UTC);
        } else {
            return defaultLocalDT.toDateTime(DateTimeZone.UTC);
        }
    }

    public DateTime getItemAsDT(String key, String defaultKey) {
        String sDt = (champData.readValue(key) == null) ? "" : champData.readValue(key).trim();
        if (sDt.length() > 0) {
            return new DateTime(sDt).withZone(DateTimeZone.UTC);
        } else {
            return getItemAsDT(defaultKey);
        }
    }

    public Boolean makeBackup() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMddHHmmss");

        //generate backup file name
        String backupFileName=this.fChamp.getParent() +
                File.separatorChar +
                fmt.print(new DateTime()) +
                "_" +
                Utilities.getFileNameWithoutExtension(this.fChamp.getName()) +
                ".bak";
        //System.out.println("backupFileName: " + backupFileName);
        return Utilities.copy(this.fChamp, new File(backupFileName));
    }



        /*
     see http://java.sun.com/developer/onlineTraining/tools/netbeans_part1/#menus
     * byte - Byte.parseByte(aString)
        short - Short.parseShort(aString)
        int - Integer.parseInt(aString)
        long - Long.parseLong(aString)
        float - Float.parseFloat(aString)
        double - Double.parseDouble(aString)
        boolean - Boolean.valueOf(aString).booleanValue();
     *
     * */



}
