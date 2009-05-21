package FRDL;

import java.io.BufferedWriter;
import java.io.IOException;
import org.jdesktop.application.ResourceMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;


/**
 * various useful utilities
 * @author rmh
 */
public class Utilities {
    private static JFileChooser fc;
    private static ResourceMap rm = App.getResourceMap();


    /*
     * Simply checks if a directory on host system exists.
    */
    public static Boolean directoryExists(String path) {
        if (path == null||path.length()==0) {
            return false;
        } else {
            return (new File(path)).exists();
        }
    }

     // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDirectory(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }





    /*
     * Simply checks if a file on host system exists.
    */
    public static Boolean fileExists (String path) {
        return (new File(path)).exists();
    }


    /*
     * Returns a file name without the file extension
    */
    public static String getFileNameWithoutExtension(String fileName) {
        int whereDot = fileName.trim().lastIndexOf('.');
        if (whereDot > 0) {
            return fileName.trim().substring(0, whereDot);
        } else {
            return fileName.trim();
        }
    }

    /*
     * Returns file extension without the file name
    */
    public static String getFileExtension(String fileName) {
        int whereDot = fileName.trim().lastIndexOf('.');
        if (whereDot > 0) {
            return fileName.trim().substring(whereDot + 1, fileName.trim().length());
        } else {
            return fileName.trim();
        }
    }

    /*
     * Copies src file to dst file.
     * If the dst file does not exist, it is created
    */

    public static Boolean copy(File src, File dst)  {
        Boolean bo = true;
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

        } catch (IOException ex) {
            bo = false;
            //Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            MainView.addLog("ERROR: File copy error: " + ex);
        }
        return bo;
    }

    /*
     * Generates the default CIMA igc file name
     * looks in the target dir and NEVER writes duplicates
     * will fall over if it gets beyond z...
    */
    public static String makeCimaFileName (String compNo, String taskNo, String fileVersion, String loggerPriority, String pilotName) {
        //35 has got to be enough hasn't it?
        final String[] st = {"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
        String fv = st[Integer.parseInt(fileVersion)];

        return repeatString("0",3 - compNo.trim().length()) +
                compNo.trim() +
                "T" +
                repeatString("0",2 - taskNo.trim().length()) + 
                taskNo.trim() + 
                "V" + 
                fv +
                "R" + 
                loggerPriority.substring(0,1) + 
                "_" + 
                pilotName.trim().replaceAll(" ", "_");
    }

    /*
     * Repeats a character x number of times in a string
    */
    public static String repeatString(String st, int i) {
         String tst = "";
         for(int j = 0; j < i; j++) {
             tst = tst+ st;
         }
         return tst;
    }

    /*
     * Opens a championship file.
    */
    public static Boolean openChampionship() {
        fc = new JFileChooser();
        fc.addChoosableFileFilter(new frdlFileFilter(rm.getString("frdcFileExtension"),rm.getString("frdcFileDescription")));
        fc.setApproveButtonToolTipText(rm.getString("openChampMenuItem.text"));
        fc.setApproveButtonText(rm.getString("openChampMenuItem.text"));
        fc.setDialogTitle(rm.getString("openChampMenuItem.text"));
        fc.setAcceptAllFileFilterUsed(false);
        if (Utilities.directoryExists(App.pathToAllFiles)) {
            fc.setCurrentDirectory(new File(App.pathToAllFiles));
        }
        //int returnVal = 0;
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            return loadChampionshipFile(f);
        } else {
        MainView.addLog("File - Open Championship command cancelled by user.");
        return false;
        }
    }

    public static Boolean newChampionship() {
        Dialogs d = new Dialogs();
        fc = new JFileChooser();
        fc.addChoosableFileFilter(new frdlFileFilter(rm.getString("frdcFileExtension"),rm.getString("frdcFileDescription")));
        fc.setApproveButtonToolTipText(rm.getString("newChampMenuItem.text"));
        fc.setApproveButtonText(rm.getString("newChampMenuItem.text"));
        fc.setDialogTitle(rm.getString("newChampMenuItem.text"));
        fc.setAcceptAllFileFilterUsed(false);
        if (directoryExists(App.pathToAllFiles)) {
            fc.setCurrentDirectory(new File(App.pathToAllFiles));
        }
        int returnVal = fc.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            Boolean test = true;

            if (getFileNameWithoutExtension(fc.getSelectedFile().getName()).trim().length() == 0) {
                d.showErrorDialog(rm.getString("noFileNameErrorMsg"), null);
                return false;
            }

            String newFileName = fc.getSelectedFile().getParent()
                    + File.separatorChar
                    + getFileNameWithoutExtension(fc.getSelectedFile().getName())
                    + "." + rm.getString("frdcFileExtension");
            //System.out.println("new file name is: " + newFileName);

            if (fileExists(newFileName)) test = d.showQuestionDialog(rm.getString("overWriteExistingFileQuestion"));

            //now write the content
            if (test) {
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter(newFileName));
                    out.write(rm.getString("newBlankChampionship"));
                    out.close();
                    MainView.addLog("Created new championship file: " + newFileName);
                } catch (IOException e) {
                    test = false;
                    MainView.addLog("ERROR while trying to make a new championship file: " + e);
                }
            } 

            //now load the new blank championship
            if (test) {
                File f = new File(newFileName);
                if (!loadChampionshipFile(f)) return false;

                //load some default settings into the new file
                //default output path is the same one as the
                App.thisChampionship.champData.writeProperty("master.pathToFlightAnalysis", 
                        App.pathToAllFiles + File.separatorChar + "igcFiles" + File.separatorChar);
       
                DateTime dt = (new DateTime()).withZone(DateTimeZone.UTC);
                //championship & task 1 window open & close
                dt = dt.withMillisOfDay(0);
                App.thisChampionship.champData.writeProperty("championship.windowOpen", dt.toString());
                //App.thisChampionship.champData.writeProperty("task.1.windowOpen", dt.toString());
                //and championship window close to today + 10 days + 1 min less than one day
                dt = dt.plusDays(10).plusMinutes(1439);
                App.thisChampionship.champData.writeProperty("championship.windowClose", dt.toString());
                //user's local time offset - of course might not be set on user's computer correctly, but
                //we can try anyway
                int[] ltz = getTimeZoneOffset (App.userLocalTimeOffset);
                App.thisChampionship.champData.writeProperty("championship.utcOffsetH", Integer.toString(ltz[0]));
                App.thisChampionship.champData.writeProperty("championship.utcOffsetM", Integer.toString(ltz[1]));

            }
            return test;
        } else {
        MainView.addLog("File - Create new championship command cancelled by user.");
        return false;
        }

    }
    
    public static Boolean saveChampionshipAs() {
        Dialogs d = new Dialogs();
        fc = new JFileChooser();
        fc.addChoosableFileFilter(new frdlFileFilter(rm.getString("frdcFileExtension"),rm.getString("frdcFileDescription")));
        fc.setApproveButtonToolTipText(rm.getString("saveChampAsMenuItem.text"));
        fc.setApproveButtonText(rm.getString("saveChampAsMenuItem.text"));
        fc.setDialogTitle(rm.getString("saveChampAsMenuItem.text"));
        fc.setAcceptAllFileFilterUsed(false);
        if (directoryExists(App.pathToAllFiles)) {
            fc.setCurrentDirectory(new File(App.pathToAllFiles));
        }
        int returnVal = fc.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            Boolean test = true;

            if (getFileNameWithoutExtension(fc.getSelectedFile().getName()).trim().length() == 0) {
                d.showErrorDialog(rm.getString("noFileNameErrorMsg"), null);
                return false;
            }

            String newFileName = fc.getSelectedFile().getParent()
                    + File.separatorChar
                    + getFileNameWithoutExtension(fc.getSelectedFile().getName())
                    + "." + rm.getString("frdcFileExtension");
            //System.out.println("new file name is: " + newFileName);

            if (fileExists(newFileName)) test = d.showQuestionDialog(rm.getString("overWriteExistingFileQuestion"));
            
            //now create the file
            // Copies src file to dst file.
            // If the dst file does not exist, it is created
            File f = new File(newFileName);
            if (test) test = copy(App.thisChampionship.fChamp,f);
            if (test) test = loadChampionshipFile(f);
            return test;
        } else {
        MainView.addLog("File - New Championship command cancelled by user.");
        return false;
        }
    }
    
    public static Boolean loadChampionshipFile(File f) {
        try {
            App.pathToAllFiles = f.getParent();
            App.thisChampionship = new Championship(f);
            setWindowTitle(f.getName());
            App.sessionProperties.setProperty("lastChampionshipFile", f.getAbsolutePath());
            App.mapCaption = App.getResourceMap().getString("waitingForLoggerMsg");
            MainView.setMainStatus("");
            MainView.addLog("Loaded championship: " + f.getName());
            return true;
        } catch (Exception e) {
            MainView.addLog("ERROR: failed to load championship: " + f.getName() + " " + e.getMessage());
            return false;
        }
	}

    public static void setWindowTitle(String openFileName) {
        JFrame mainFrame = App.getApplication().getMainFrame();
        mainFrame.setTitle(rm.getString("Application.shortTitle") + " - " + openFileName);
    }

    /*
     * Sets up a file filter for open, new, saveAs dialogs
    */
    private static class frdlFileFilter extends FileFilter {
        private String fileExt = null;
        private String fileExtDescription = null;

        //constructor
        frdlFileFilter (String fileExt, String fileExtDescription) {
            this.fileExt = fileExt;
            this.fileExtDescription = fileExtDescription;
        }


      public boolean accept(File f) {
        if (f.isDirectory())
          return true;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1)
          if (s.substring(i + 1).toLowerCase().equals(fileExt))
            return true;

        return false;
      }

      /* returns the file extension & description to the filefilter
       *
       */
      public String getDescription() {
          return fileExtDescription + "; ." + fileExt;
      }
    }

    public static String makeTimeZoneOffset (int ih, int im, Boolean addLocalTimeTxt) {
        //nothing clever = simple string parsing
        if (addLocalTimeTxt == true && ih == 0 && im == 0) return "UTC";
        String ret = "";
        //int ih = Integer.parseInt(App.thisChampionship.champData.readValue("championship.utcOffsetH"));
        //int im = Integer.parseInt(App.thisChampionship.champData.readValue("championship.utcOffsetM"));
        ret = String.format("%02d", ih) + ":" + String.format("%02d", im);
        ret = (ih >=0) ? "+" + ret : "" + ret;

        if (addLocalTimeTxt) {
            ret = App.getResourceMap().getString("localTimeMsg") + "; UTC " + ret;
        }

        return ret;
    }

    /*
     * Give this a string eg "+01:45" or "-05:45" and
     * it returns an integer array where ar[0] = hours and ar[1] = minutes
     *
     * designed for parsing timezone offsets, particularly
     * App.userLocalTimeOffset
     */
    public static int[] getTimeZoneOffset (String st) {
        int[] x = {0,0};
        try {
            String str[] = st.split(":");
            int ofH = Integer.parseInt(str[0].substring(1));
            if (str[0].substring(0, 1).equals("-")) ofH = ofH * -1;
            int ofM = Integer.parseInt(str[1]);
            x[0] = ofH;
            x[1] = ofM;
        } catch (Exception e) {
            //nothing to do here
        }
        return x;
    }
    
    /*
     * a little bit basic, but will probably work in most cases...
     * 
    */
    public static String removeAccents(String s){
        s = s.replaceAll("[ÀÁÂÃÄÅ]","A");
        s = s.replaceAll("[àáâãäå]","a");
        s = s.replaceAll("Æ","AE");
        s = s.replaceAll("æ","ae");
        s = s.replaceAll("[ĆĈĊČÇ]","C");
        s = s.replaceAll("[ćĉċčç]","c");
        s = s.replaceAll("[ĎĐ]","D");
        s = s.replaceAll("[ďđ]","d");
        s = s.replaceAll("[ĒĔĖĘĚÈÉÊË]","E");
        s = s.replaceAll("[ēĕėęěèéêë]","e");
        s = s.replaceAll("[ĜĞĠĢ]","G");
        s = s.replaceAll("[ĝğġģ]","g");
        s = s.replaceAll("[ĤĦ]","H");
        s = s.replaceAll("[ĥħ]","h");
        s = s.replaceAll("[ÌÍÎÏĨĪĬĮİ]","I");
        s = s.replaceAll("[ĩīĭįıîïìí]","i");
        s = s.replaceAll("Ĵ","J");
        s = s.replaceAll("ĵ","j");
        s = s.replaceAll("Ķ","K");
        s = s.replaceAll("[ķĸ]","k");
        s = s.replaceAll("[ĹĻĽĿŁ]","L");
        s = s.replaceAll("[ĺļľŀł]","l");
        s = s.replaceAll("[ŃŅŇŊÑ]", "N");
        s = s.replaceAll("[ńņňŉŋňñ]", "n");
        s = s.replaceAll("[ÒÓÔÕÖŌŎŐØ]","O");
        s = s.replaceAll("[ôõöòóōŏőø]","o");
        s = s.replaceAll("Œ", "OE");
        s = s.replaceAll("œ", "oe");
        s = s.replaceAll("[ŔŖŘ]","R");
        s = s.replaceAll("[ŕŗř]","r");
        s = s.replaceAll("[ŚŜŞŠ]","S");
        s = s.replaceAll("[śŝşš]","s");
        s = s.replaceAll("ß", "ss");
        s = s.replaceAll("[ŢŤŦ]","T");
        s = s.replaceAll("[ţťŧ]","t");
        s = s.replaceAll("[ŨŪŬŮŰŲÙÚÛÜ]","U");
        s = s.replaceAll("[ûüùúũūŭůűų]","u");
        s = s.replaceAll("Ŵ", "W");
        s = s.replaceAll("ŵ", "w");
        s = s.replaceAll("[ÝŶŸ]", "Y");
        s = s.replaceAll("[ýŷ]", "y");
        s = s.replaceAll("[ŹŻŽ]", "Z");
        s = s.replaceAll("[źżž]", "z");
        return s;
    } 
   
}



