/*
 * NMEA5App.java
 */

package FRDL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.joda.time.DateTime;

/**
 * The main class of the application.
 */
public class App extends SingleFrameApplication {


    public static SimpleDateFormat standardDateFormat;
    public static Properties sessionProperties = null;
    public static File[] startupRoots = File.listRoots();
    
    public static String pathToAllFiles = null;
    public static Championship thisChampionship = null;
    public static GpsLogger logr = null;
    public static TreeMap track = null;
    
    public static AppStatus status = new AppStatus();
    public static CimaApprovedLoggers gpsLoggersMaster = new CimaApprovedLoggers();

    public static Boolean clientFullMode = true;
    public static String mapCaption = "";
    public static boolean champFileIsOpen = false;

    public static String userLanguage = "en";


        @Override
    protected void initialize(String[] args) {

        // create and load default properties
        sessionProperties = new Properties();
        //create some default properties
        sessionProperties.setProperty("nextStartLanguage","en");
        //add more here as necessary
        readSessionProperties();
        //set language NOTE country is always GB - maintains consistency of number formats Etc.
        Locale.setDefault(new Locale(sessionProperties.getProperty("nextStartLanguage"),"GB"));
        try {
            String lastPathToAllFiles = sessionProperties.getProperty("lastPathToAllFiles");
            if (Utilities.directoryExists(lastPathToAllFiles)) {
                pathToAllFiles = lastPathToAllFiles;
            }
        } catch (Exception e) {
            // lastPathToAllFiles not in sessionProperties
        }
        MainView.addLog("Application locale: " + Locale.getDefault());

    }
    
    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        show(new MainView(this));
        //selected = new Selection(this);
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of NMEA5App
     */
    public static App getApplication() {
        return Application.getInstance(App.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        // VERY important we are calculating in UTC
        System.setProperty("user.timezone", "UTC");
        MainView.addLog("Application timezone: " + new DateTime().getZone().getID());

        // also VERY important, set timezone for standard date format to UTC
        TimeZone tz = TimeZone.getTimeZone("GMT");
        standardDateFormat = new SimpleDateFormat("EEE d MMM yyyy");
        standardDateFormat.setTimeZone(tz);

        //launch the app
        launch(App.class, args);
    }

            /*
     * this runs immnediately before the app is closed
    */
    @Override
    protected void shutdown() {
        MainView.addLog("Application state shutting down");
        // The default shutdown saves session window state.
        super.shutdown();  //remove this to not preserve session state
        // Now perform any other shutdown tasks you need.
        sessionProperties.setProperty("lastPathToAllFiles", pathToAllFiles) ;
        writeSessionProperties();
        System.out.println("about to shut down");
    }

            /*
     * reads session properties - this is guaranteed to fail on first run
     * but should be OK after that...
    */
    private void readSessionProperties() {
        try {
            // now load properties from last invocation
            FileInputStream in = new FileInputStream("sessionProperties");
            sessionProperties.load(in);
            in.close();
        } catch (IOException ex) {
            MainView.addLog("ERROR: failed to read session properties.  This is expected if it is the first time you are using FRDL");
        }
    }
    /*
     * writes session properties to a .properties file called sessionProperties
     * location is set by the system, but it should be consistent....
    */
    private void writeSessionProperties() {
        try {
            FileOutputStream out = new FileOutputStream("sessionProperties");
            sessionProperties.store(out, "---FRDL properties - DO NOT MANUALLY EDIT---");
            out.close();
        } catch (IOException ex) {
            MainView.addLog("ERROR: failed to write session properties " + ex);
        }
    }

    public static ResourceMap getResourceMap () {
        return org.jdesktop.application.Application.getInstance(FRDL.App.class).getContext().getResourceMap(App.class);
    }


}