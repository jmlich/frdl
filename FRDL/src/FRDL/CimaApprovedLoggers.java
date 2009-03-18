package FRDL;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Manages the info from the CimaApprovedLoggers.properties
 * which is the master loggers configuration file
 *
 *
 * would prefer to use the PropertiesIO class, but can't find
 * a way of getting that to read from inside the jar (rather than external files)
 * so many methods here are similar but we don't need to write to this one
 *
 * @author rmh
 */
public class CimaApprovedLoggers {
    private ResourceBundle p;


    //constructor
    CimaApprovedLoggers() {
        //this loads CimaApprovedLoggers from resources
        this.p = ResourceBundle.getBundle("FRDL/resources/CimaApprovedLoggers");
        //could use this one? but it wouls also use App.properties as default
        //this.p = org.jdesktop.application.Application.getInstance(FRDL.App.class).getContext().getResourceMap(CimaApprovedLoggers.class);
    }

    /*
     * CimaLoggersMaster.frdl is laid out like
     * type.AMOD.1=AMOD 8030
     * configFileContent.AMOD.1=some content
     * configFileName.AMOD.1=some content
     * logFileExtension.AMOD.1=log
     *
     * the identifier is the value of the line beginning type.

     * so if "AMOD 8030" is entered as the identifier
     * and the key "logFileExtension" is entered
     * the value "log" will be returned
     *
     */
    public String getValue(String identifier, String key) {
        //String x = key + makeBaseKey(loggersMasterContent.readKey(identifier));
        //return loggersMasterContent.readValue(x);
        String x = key + makeBaseKey(readKey(identifier));
        return p.getString(x);
    }

    public String readKey(String value) {
        ArrayList keys = readAllKeys(null);
        for (int i = 0;i < keys.size();i++) {
            if (p.getString((String) keys.get(i)).equals(value)) {
                return (String) keys.get(i);
            }
        }
        return "";
    }

        /*
     *
     * this returns an arraylist of key names,
     * if subkey is null, returns the lot
     * if subkey has a value, for example "pilot." then
     * will return all the subkeys with "pilot." in them, eg
     * "pilot.name", "pilot.compNo" etc.
    */
    public ArrayList readAllKeys(String subKey){
        ArrayList keys = new ArrayList();
        Enumeration e = p.getKeys();
        while (e.hasMoreElements()){
            String x = (String) e.nextElement();
            if (subKey == null || x.indexOf(subKey.trim()) >= 0) {
                //add it
                //System.out.println("adding: " + x);
                keys.add(x);
            }
        }
        return keys;
    }

     /**
     * Read all values in properties file which match a subKey value
     * into ArrayList object
     * @return ArrayList object containing these values in properties file
     */
    public ArrayList readAllValues(String subKey){
        ArrayList keys = readAllKeys(subKey);
        ArrayList values = new ArrayList();
        for (int i = 0;i < keys.size();i++) {
            values.add(p.getString((String) keys.get(i)));
        }
        return values;
    }

    /*
     * convenience method
     * returns an arraylist of values of all master logger types
     * 
     *
    */
    public ArrayList getTypes() {
        return readAllValues("type");
    }


    /*
     * all keys are a dot separated string
     * of length 3
     * this will return the last 2 parts, the equivalent
     * to removing the first part.  It is used when searching
     * for subkeys
    */

     private String makeBaseKey (String in) {
        String[] key = in.split("\\.");
        if (key.length == 3) {
            return "." + key[1] + "." + key[2];
        } else {
            return "";
        }
    }

}