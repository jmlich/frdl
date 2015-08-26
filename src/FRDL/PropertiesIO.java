package FRDL;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * This class is used for Read/Write values in properties file
 * @author Manet Yim (manet.yim at gmail dot com)
 */
public class PropertiesIO {

    private String propertiesFile;
    private Properties p;

    /**
     * Initialise this class and load properties file
     * at the same time
     * @param file to load
     */
    public PropertiesIO(String file){
        this.propertiesFile = file;
        p = new Properties();
        this.loadProperties();
    }

    /**
     * @param file to load
     */

    public PropertiesIO(URL file){
        this.propertiesFile = file.toExternalForm();
        p = new Properties();
        this.loadProperties();
    }

    /**
     * @param p other instance to copy
     */

    public PropertiesIO(Properties p){
        this.p = p;
        //this.propertiesFile = file.toExternalForm();
        //p = new Properties();
        //this.loadProperties();
    }

    /**
     * @param b resource; but this does nothing
     */

    public PropertiesIO(ResourceBundle b) {
        //this.p = b.
    }
        /**
     * Initialise this class and load properties file with defaults
     * at the same time
     * IMPORTANT: Properties from the defaults
     * are NOT written out to the properties file.
     * they are only for reading if not present in the properties table.
     * @param file to load
     * @param defaultsFile to load default values
     */
    public PropertiesIO(String file, String defaultsFile){
        this.propertiesFile = file;
        try {
            Properties defaults = new Properties();
            defaults.load(new FileInputStream(defaultsFile));
            p = new Properties(defaults);
            this.loadProperties();
        } catch (FileNotFoundException e) {
            MainView.addLog("PROBLEM: PropertiesIO.PropertiesIO.DefaultFileNotFound  - loading without defaults");
            p = new Properties();
            this.loadProperties();
        } catch (IOException e) {
            MainView.addLog("ERROR: PropertiesIO.PropertiesIO.IOException " + e);
        }
    }



    /**
     * Load content of properties file into memory
     */
    public void loadProperties(){
        try {
            p.load(new FileInputStream(propertiesFile));
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            MainView.addLog("Creating new " + propertiesFile);
        } catch (IOException e) {
            //e.printStackTrace();
            MainView.addLog("ERROR: PropertiesIO.loadProperties.IOException " + e);
        }
    }

    /**
     * Read all value in properties file into ArrayList object
     * @return ArrayList object containing all values in properties file
     */
    public ArrayList readAllValues(){
        ArrayList values = new ArrayList();
        Enumeration e = p.elements();
        while (e.hasMoreElements()){
            values.add(e.nextElement());
            //System.out.println("element: " + e.nextElement());
        }
        return values;
    }
    
        /**
     * Read all values in properties file which match a subKey value
     * into ArrayList object
     * @param subKey the matched value
     * @return ArrayList object containing these values in properties file
     */
    public ArrayList readAllValues(String subKey){
        ArrayList keys = readAllKeys(subKey);
        ArrayList values = new ArrayList();
        for (int i = 0;i < keys.size();i++) {
            values.add(p.getProperty((String) keys.get(i)));
        }
        return values;
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
        Enumeration e = p.keys();
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
     * Read value that matched the key
     * @param key use to search in properties file
     * @return value that matched key
     */
    public String readValue(String key){
        return p.getProperty(key);
    }

    public String readKey(String value) {
        ArrayList keys = readAllKeys(null);
        for (int i = 0;i < keys.size();i++) {
            if (p.getProperty((String) keys.get(i)).equals(value)) {
                return (String) keys.get(i);
            }
        }
        return "";
    }

    /**
     * Write key/value pair into properties file
     * @param key represenst name of property
     * @param value represents its content
     */
    public void writeProperty(String key, String value){
        p.setProperty(key, value);
        try {
            p.store(new FileOutputStream(propertiesFile), null);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            MainView.addLog("ERROR: PropertiesIO.writeProperty.FileNotFound " + e);
        } catch (IOException e) {
            //e.printStackTrace();
            MainView.addLog("ERROR: PropertiesIO.writeProperty.IOException " + e);
        }
    }
}

