package FRDL;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author rmh
 */
public class GpsLogger {
        private final String path;
        private final File fRoot;
        public ArrayList allLogFiles = new ArrayList<File>();
        public File loggerFile = null;
        public PropertiesIO loggerFileContent = null;
        public String loggerBackupDir = null;
        public long totalLoggerBytes = 0;
        private final int MODE_FULL = 1;
        private final int MODE_DOWNLOAD = 2;
        private final int MODE_PARTIAL = 3;
        
        
        //the constructor
        GpsLogger(String path, File file) {
            this.path = path;
            this.fRoot = file;
            init();
        }

        private void init() {
            File f = fRoot;
            String loggerConfigFileName = App.getResourceMap().getString("loggerConfigFileName");
            //logger.frdl MUST be in the root
            if (Utilities.fileExists(path + loggerConfigFileName)) {
                this.loggerFile = new File(path + loggerConfigFileName);
                this.loggerFileContent = new PropertiesIO(loggerFile.getAbsolutePath());
            }
            //puts every file in allLogFiles except the logger config file
            RecursiveFileListIterator it = new RecursiveFileListIterator(f);
            while (it.hasNext()) {
                f = it.next();
                //gets everything but the logger config file
                if (!f.getName().toLowerCase().equals(loggerConfigFileName)) {
                    allLogFiles.add(f);
                    totalLoggerBytes = totalLoggerBytes + f.length();
                }
            }
        }

        /*
         * checks whether the logger is properly configured
         * and returns with messages if it is not
        */
        public Boolean checkLogger() {


            Dialogs q = new Dialogs();
            if (this.loggerFile == null) {
                if (!q.showQuestionDialog(App.getResourceMap().getString("noLoggerConfigFileMsg") +
                        " " + path + "\n " +
                        App.getResourceMap().getString("createNewMsg"))) {
                    MainView.setMainStatus(App.getResourceMap().getString("youMayDisconnectMsg"));
                    MainView.setBottomStatus(App.getResourceMap().getString("youMayDisconnectMsg"));
                    return false;
                } else {
                    MainView.addLog("New logger connected in full mode");
                    App.clientFullMode = true;
                    openLoggerSettings(true);  //ie this is a NEW logger
                }
            }

            //check the hashed password matches
            if (loggerExists(MODE_FULL) && App.status.getClientState(App.thisChampionship.champData.readValue("password.master"),
                    this.loggerFileContent.readValue("password.hashed"))) {
                // we ARE ok - this is the normal mode
                // if either the master password is blank (so is an unsecured championship)
                // or passwords do match, so is a secured app and we are downloading
                // on organizer box
                //System.out.println("exists, and master & logger passwords ok - DEFINITELY FULL MODE");
                MainView.addLog("Logger connected in full mode");
                App.clientFullMode = true;
                writeLatestChampionshipSettingsToLogger();
                return true;
            } 
            
            if (!loggerExists(MODE_FULL) && App.status.getClientState(App.thisChampionship.champData.readValue("password.master"),
                    this.loggerFileContent.readValue("password.hashed"))) {
                    //password OK, but logger doesn't exist in championship file
                    // if either the master password is blank (so is an unsecured championship)
                    // or passwords do match, so is a secured app and we are downloading
                    // on organizer box
                    // - add logger to championship?
                if (!q.showQuestionDialog(
                        App.getResourceMap().getString("doYouWantToAddThisLoggerMsg.line1") + "\n\n" +
                        App.logr.loggerFileContent.readValue("championship.name") + "\n" +
                        App.logr.loggerFileContent.readValue("pilot.compNo") + " - " +
                        App.logr.loggerFileContent.readValue("pilot.name") + " - " +
                        App.logr.loggerFileContent.readValue("pilot.nation") + "\n\n" +
                        App.getResourceMap().getString("doYouWantToAddThisLoggerMsg.line2") + "\n" +
                        App.getResourceMap().getString("doYouWantToAddThisLoggerMsg.line3")
                        )) {
                    MainView.setMainStatus(App.getResourceMap().getString("youMayDisconnectMsg"));
                    MainView.setBottomStatus(App.getResourceMap().getString("youMayDisconnectMsg"));
                    return false;
                } else {
                    //add the logger to the championship file
                    MainView.addLog("Added logger in full mode");
                    App.thisChampionship.champData.writeProperty("logger." + App.logr.loggerFileContent.readValue("logger.uuid"),(new DateTime().withZone(DateTimeZone.UTC)).toString());
                    App.clientFullMode = true;
                    openLoggerSettings(false); //ie is NOT a new logger
                    return true;
                }
            }

            if (loggerExists(MODE_DOWNLOAD)) {
                //System.out.println("exists MODE_DOWNLOAD - DEFINITELY DOWNLOAD MODE");
                MainView.addLog("Logger connected in download mode");
                App.clientFullMode = false;
                writeLatestLoggerSettingsToChampionship();
                MainView.setMainStatus("");  //this will reset the active task on screen, if necessary
                return true;
            }
            
            //if still not returned, we need a catch-all what do you want to do dialog?
            //options:  
            //      set into download mode
            //      reset for this championship - 
            // a reset can / should be done manually by deleting or renaming logger.frdl
            // so in fact the only option we need is a one-time dialog to set into
            // download mode for this championship
     
            if (!q.showQuestionDialog(App.getResourceMap().getString("enterDownloadMode.line1") + "\n\n" +
                    App.getResourceMap().getString("enterDownloadMode.line2") + "\n\n" +
                    App.getResourceMap().getString("enterDownloadMode.line3") + "\n" +
                    App.getResourceMap().getString("enterDownloadMode.line4") + "\n" +
                    App.getResourceMap().getString("enterDownloadMode.line5") + "\n" +
                    App.getResourceMap().getString("enterDownloadMode.line6") + "")) {
                MainView.setMainStatus(App.getResourceMap().getString("youMayDisconnectMsg"));
                MainView.setBottomStatus(App.getResourceMap().getString("youMayDisconnectMsg"));
                return false;
            } else {
                if (!q.showQuestionDialog(App.getResourceMap().getString("areYouSureMsg.line1") + "\n\n" +
                    App.getResourceMap().getString("areYouSureMsg.line2") + "")) {
                    MainView.setMainStatus(App.getResourceMap().getString("youMayDisconnectMsg"));
                    MainView.setBottomStatus(App.getResourceMap().getString("youMayDisconnectMsg"));
                    return false;
                } else {
                    MainView.addLog("Added logger connected in download mode");
                    App.clientFullMode = false;
                    App.thisChampionship.champData.writeProperty("logger.downloadmode." + App.logr.loggerFileContent.readValue("logger.uuid"),
                            (new DateTime().withZone(DateTimeZone.UTC)).toString());
                    writeLatestLoggerSettingsToChampionship();
                    MainView.setMainStatus("");  //this will reset the active task on screen, if necessary
                    return true;
                }
            }

            /*
            if (loggerExists(MODE_FULL)) {
                //exists, but password wrong - reset password on logger...
                System.out.println("exists, and master & logger passwords mismatch - reset");
                int ctr = 0;
                Boolean pwTest = false;
                String pw = "";
                while (ctr < 3 || pwTest == false || pw != null) {
                    pw = q.showPasswordDialog("Logger password");
                    pwTest = App.status.getClientState(pw,App.thisChampionship.champData.readValue("password.hashed"));
                    ctr++;
                }
                if (pwTest) {
                    //passworrd was OK, reset the logger hashed pw
                    App.status.clientFullMode = true;
                    writeLatestFullModeSettingsToLogger();
                    //open logger info
                    openLoggerSettings();
                }
            }

            if (this.loggerFile == null) {
                System.out.println("Finally STILL false");
                return false;
            }
 */
        }

            
        /*
         * Opens the logger settings dialog
         * @param newlogger - if true, loggerSettings sees this as a new logger
         * otherwise is an existing one.
        */
        private void openLoggerSettings(Boolean newlogger) {
            GpsLoggerSettings loggerSettings;
            JFrame mainFrame = App.getApplication().getMainFrame();
            if (newlogger) {
                loggerSettings = new GpsLoggerSettings(mainFrame, path);
            } else {
                loggerSettings = new GpsLoggerSettings(mainFrame);
            }
            loggerSettings.setLocationRelativeTo(mainFrame);
            App.getApplication().show(loggerSettings);
        }


        private Boolean loggerExists(int mode) {
            //check this logger exists in championship file
            //ONLY IN FULL MODE!
            //TODO otherwise add to championship file
            ArrayList ar = null;
            String thisLogger = null;
            if (mode == MODE_FULL) {
                ar = App.thisChampionship.champData.readAllKeys("logger");
                thisLogger = "logger." + this.loggerFileContent.readValue("logger.uuid");
            } else { //MODE_DOWNLOAD
                ar = App.thisChampionship.champData.readAllKeys("logger.downloadmode");
                thisLogger = "logger.downloadmode." + this.loggerFileContent.readValue("logger.uuid");
            }

            //System.out.println("this logger: " + thisLogger);
            for (int i=0;i<ar.size();i++) {
                //System.out.println("all loggers: " + ar.get(i));
                if (ar.get(i).equals(thisLogger)) {
                    return true;
                }
            }
            return false;
        }

        /*
         * writes settings to logger ONLY if we are in client full mode
         *
        */
        private void writeLatestChampionshipSettingsToLogger () {
            if (!App.clientFullMode) return;

            //password hashed
            App.logr.loggerFileContent.writeProperty("password.hashed",App.thisChampionship.getItemAsString("password.hashed"));
            //championship name
            App.logr.loggerFileContent.writeProperty("championship.name",App.thisChampionship.getItemAsString("championship.name"));
            //championship window
            App.logr.loggerFileContent.writeProperty("championship.windowOpen",App.thisChampionship.getItemAsString("championship.windowOpen"));
            App.logr.loggerFileContent.writeProperty("championship.windowClose",App.thisChampionship.getItemAsString("championship.windowClose"));
            //read in all the task windows
            ArrayList ar = App.thisChampionship.champData.readAllKeys("task");
            for (int i=0; i<ar.size();i++) {
                App.logr.loggerFileContent.writeProperty((String) ar.get(i),App.thisChampionship.champData.readValue((String) ar.get(i)));
            }
            //the active task
            App.logr.loggerFileContent.writeProperty("championship.activeTask",App.thisChampionship.getItemAsString("championship.activeTask"));
            //offsets
            App.logr.loggerFileContent.writeProperty("championship.utcOffsetH",App.thisChampionship.getItemAsString("championship.utcOffsetH"));
            App.logr.loggerFileContent.writeProperty("championship.utcOffsetM",App.thisChampionship.getItemAsString("championship.utcOffsetM"));
            App.logr.loggerFileContent.writeProperty("championship.adjustIgcFileTimes",App.thisChampionship.getItemAsString("championship.adjustIgcFileTimes"));
        }
        
        
        /*
         * writes settings to championship ONLY if we are in client download mode
         *
        */
        private void writeLatestLoggerSettingsToChampionship () {
             //password hashed
            // //App.logr.loggerFileContent.writeProperty("password.hashed",App.thisChampionship.getItemAsString("password.hashed"));
            //championship name
            App.thisChampionship.champData.writeProperty("championship.name", App.logr.loggerFileContent.readValue("championship.name"));
            //championship window
            App.thisChampionship.champData.writeProperty("championship.windowOpen", App.logr.loggerFileContent.readValue("championship.windowOpen"));
            App.thisChampionship.champData.writeProperty("championship.windowClose", App.logr.loggerFileContent.readValue("championship.windowClose"));
            //read in all the task windows
            ArrayList ar = App.logr.loggerFileContent.readAllKeys("task");
            for (int i=0; i<ar.size();i++) {
                App.thisChampionship.champData.writeProperty((String) ar.get(i),App.logr.loggerFileContent.readValue((String) ar.get(i)));
            }
            //the active task
            App.thisChampionship.champData.writeProperty("championship.activeTask",App.logr.loggerFileContent.readValue("championship.activeTask"));
            //offsets
            App.thisChampionship.champData.writeProperty("championship.utcOffsetH", App.logr.loggerFileContent.readValue("championship.utcOffsetH"));
            App.thisChampionship.champData.writeProperty("championship.utcOffsetM", App.logr.loggerFileContent.readValue("championship.utcOffsetM"));
            App.thisChampionship.champData.writeProperty("championship.adjustIgcFileTimes", App.logr.loggerFileContent.readValue("championship.adjustIgcFileTimes"));
        }
        /*
         * this sets things up for the asynchronous task to 
         * backup all files
         * it:
         * reads the logger file
         * writes or re-writes a correct logger config file to the 
         * logger (if one needs to be written).
         * makes a backup dir on the host (if it doesn't already exist)
         * returns the log file extension (to the asynchronous task)
         *
         */
        public String prepareToBackupFiles () {
            if (loggerFile == null) return null;

            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMddHHmmss");
            
            //delete any AUTORUN.INF file or dir
            String autorunName = path + "AUTORUN.INF";
            //delete directory first as this is the most likely
            if (Utilities.directoryExists(autorunName)) {
                //delete it
                if (!Utilities.deleteDirectory(new File(autorunName))) {
                    MainView.addLog("ERROR: Failed to delete directory " + autorunName);
                }
            }
            //now delete the file - probably not there, but let's try anyway
            if (Utilities.fileExists(autorunName)) {
                //delete it
                if (!(new File(autorunName)).delete()) {
                   MainView.addLog("ERROR: Failed to delete file " + autorunName);
                }
            }
            // now add a new AUTORUN.INF directory
            if (!(new File(autorunName)).mkdir()) {
                MainView.addLog("ERROR: Failed to create directory " + autorunName);
            } else {
                //add a readme file to the directory
                String readmeContent = "The AUTORUN.INF directory and this README.TXT file\r\n" +
                    "were both placed here by FRDL (Flight Recorder DownLoader) on\r\n" +
                    new DateTime().withZone(DateTimeZone.UTC).toString() + "\r\n" +
                    "as a protection against attack by malicious software.\r\n" +
                    "See 'Protecting against malware' in FRDL help.\r\n";
                //now write it
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter(autorunName + File.separatorChar + "README.TXT"));
                    out.write(readmeContent);
                    out.close();
                } catch (IOException e) {
                    MainView.addLog("ERROR: failed to write README.TXT: " + e.getMessage());
                }
            }

            //logger configuration file (optional - only needed on some types of logger
            //like the geochron)
            //could be 'after the horse has bolted' but should configure it OK for
            //next time.
            String loggertype = loggerFileContent.readValue("logger.type");

            //always rewrite logger config file (if this type of logger needs one)
            String configFileName = App.gpsLoggersMaster.getValue(loggertype, "configFileName");
            String configFileContent = App.gpsLoggersMaster.getValue(loggertype, "configFileContent");

            System.out.println(configFileContent);
            //writes the file
            if (configFileName.trim().length() > 0) {
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter(path + configFileName));
                    out.write(configFileContent);
                    out.close();
                } catch (IOException e) {
                    MainView.addLog("ERROR: failed to write logger configuration file: " + e.getMessage());
                }
            }

            //for geochron and others where we may have to rename some files on the logger
            //don't need to do anything fancy like look inside the file to find the first
            //date - a simple now() will do.
            String regEx = App.gpsLoggersMaster.getValue(loggertype, "logFileForcedRenameRegex");
            System.out.println("regex: " + regEx);
            if (regEx.trim().length() > 0) {
                Pattern p = Pattern.compile(regEx.trim());
                for (int i=0; i<allLogFiles.size(); i++) {
                    File f = (File) allLogFiles.get(i);
                    Matcher m = p.matcher(f.getName().toLowerCase());
                    if (m.find()) {
                        //need to rename the file
                        String newFileName = f.getParent() +
                                fmt.print(new DateTime()) +
                                "_" +
                                f.getName();
                        File f2 = new File(newFileName);
                        // Rename file
                        if (f.renameTo(f2)) {
                            MainView.addLog("Renamed " + f.getName() + " to " + f2.getName());
                            allLogFiles.set(i, f2);
                        } else {
                            MainView.addLog("ERROR failed to rename " + f.getName() + " on logger.");
                        }
                    }
                }
            }

            makeBackupDir();

            //make a backup of the current logger configuration file to the backup dir
            if (loggerFile != null && Utilities.directoryExists(loggerBackupDir)) {
                String dest = Utilities.getFileNameWithoutExtension(loggerFile.getName());
                dest = loggerBackupDir +
                        File.separatorChar +
                        fmt.print(new DateTime()) +
                        "_logger.bak";
                if (!Utilities.copy(loggerFile, new File(dest))) {
                    MainView.addLog("ERROR: failed to write logger backup file");
                }
            }



            
            return App.gpsLoggersMaster.getValue(loggertype, "logFileExtension");
            //return loggerFileContent.readValue("logger.logFileExtension");
        }


        /*
         * makes a correctly named backup dir for this logger on the host
         * system
        */
        private void makeBackupDir() {
            if (loggerFile == null) return;
            //check our backup directory for log files exists
            //and make it if it doesn't
            String name =
                    Utilities.makeCimaFileName(
                        loggerFileContent.readValue("pilot.compNo"),
                        "00",
                        "1",
                        loggerFileContent.readValue("pilot.loggerPriority"),
                        loggerFileContent.readValue("pilot.name"));

            loggerBackupDir = App.pathToAllFiles + File.separatorChar + name;
            //System.out.println("backup path:" + loggerBackupDir);

            Boolean success = false;
            if (!Utilities.directoryExists(loggerBackupDir)) {
                success = (new File(loggerBackupDir)).mkdirs();
            }
            if (success) MainView.addLog("Created new backup dir: " + name);
        }

        /*
         * this sets things up for the asynchronous task to 
         * process the nmea files
         * it:
         * writes all the files in the logger backup dir which have the
         * correct file extension to the allLogFiles array.
         * Generates the correct igc file name according to CIMA spec (no duplicates)
         * Returns this file name for the asynchronous task to
         * process the nmea files and write to it.
         */
        public String prepareToProcessFiles(String logFileExtension) {
            if (loggerFile == null) return null;

            File f = new File(loggerBackupDir);
            allLogFiles.clear();
            //puts every file in the backup dir in allLogFiles
            RecursiveFileListIterator it = new RecursiveFileListIterator(f);
            while (it.hasNext()) {
                f = it.next();
                if (Utilities.getFileExtension(f.getName()).equals(logFileExtension)) {
                    allLogFiles.add(f);
                }
            }
            //create the outfileName
            //loops to get the igc file version as per CIMA spec
            //ie files are NEVER overwritten

            String igcFilesPath = App.thisChampionship.getItemAsString("master.pathToFlightAnalysis");
            if (!Utilities.directoryExists(igcFilesPath)) {
                //create it
                Boolean success = (new File(igcFilesPath)).mkdirs();
            }

            //String ct = Integer.toString(App.currentTask);
            String ct = App.thisChampionship.champData.readValue("championship.activeTask");
            int ctr = 1;
            String igcFileName = "";
            do {
                String ver = Integer.toString(ctr);
                igcFileName = igcFilesPath +
                    //File.separatorChar +
                    Utilities.makeCimaFileName(
                        loggerFileContent.readValue("pilot.compNo"),
                        ct,
                        ver,
                        loggerFileContent.readValue("pilot.loggerPriority"),
                        loggerFileContent.readValue("pilot.name")) +
                        ".igc";
                    ctr = ctr + 1;
            }   while (Utilities.fileExists(igcFileName));

            return igcFileName;
        }
}
