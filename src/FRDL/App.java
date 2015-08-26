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
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * The main class of the application.
 */
public class App extends SingleFrameApplication {


    public static SimpleDateFormat standardDateFormat;
    public static Properties sessionProperties = null;
    private static final String sessionPropertiesFileName = System.getProperty("user.home") +
                    File.separatorChar +
                    "FRDL_session_Properties";
    public static File[] startupRoots = LinuxMounts.listRootsAll(); //File.listRoots();

    
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
    public static String userLocalTimeOffset = "+00:00"; //used to pre-fill the offset of a new championship

    public static Boolean includeInvalidFixesInIgcFile = false;

        @Override
    /*
    @param args command line arguments
    */
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
     * @return the instance of App
     */
    public static App getApplication() {
        return Application.getInstance(App.class);
    }

    /**
     * Main method launching the application.
     * @param args command line arguments (they are passed to launch method)
     */
    public static void main(String[] args) {
        DateTime dt = new DateTime();
        //test only...
        //dt = dt.withZone(DateTimeZone.forID("Asia/Katmandu"));
        DateTimeFormatter fmt = DateTimeFormat.forPattern("ZZ");
        userLocalTimeOffset = fmt.print(dt);
        MainView.addLog("User local time offset: " + userLocalTimeOffset);

        // VERY important we are calculating in UTC, so set defaults
        // for joda time and for java.util.TimeZone
        DateTimeZone.setDefault(DateTimeZone.UTC);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.setProperty("user.timezone", "UTC");
        //MainView.addLog("Application timezone: " + new DateTime().getZone().getID());
        MainView.addLog("Application timezone offset: " + fmt.print(new DateTime()));

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
        if (pathToAllFiles != null) {
            sessionProperties.setProperty("lastPathToAllFiles", pathToAllFiles) ;
        }
        writeSessionProperties();
        System.out.println("about to shut down");
    }

     /*
     * reads session properties - kept in the System.user.home dir
     * in the properties file: FRDL_session_Properties
    */
    private void readSessionProperties() {
        if (!Utilities.fileExists(sessionPropertiesFileName)) {
            writeSessionProperties();
        }
        try {
            // now load properties from last invocation
            FileInputStream in = new FileInputStream(sessionPropertiesFileName);
            sessionProperties.load(in);
            in.close();
        } catch (IOException ex) {
            MainView.addLog("ERROR: failed to read session properties.");
        }
    }
    /*
     * writes session properties to
     * the properties file: FRDL_session_Properties
     * kept in the System.user.home dir
    */
    private void writeSessionProperties() {
        try {
            FileOutputStream out = new FileOutputStream(sessionPropertiesFileName);
            sessionProperties.store(out, "---FRDL properties - DO NOT MANUALLY EDIT---");
            out.close();
        } catch (IOException ex) {
            MainView.addLog("ERROR: failed to write session properties " + ex);
        }
    }

    /*
     * returns the stuff in App.properties
     * very important since most strings for messages Etc are
     * kept in this properties file and it is the one which
     * is internationalized
     *
    */
    public static ResourceMap getResourceMap () {
        return org.jdesktop.application.Application.getInstance(FRDL.App.class).getContext().getResourceMap(App.class);
    }


}
