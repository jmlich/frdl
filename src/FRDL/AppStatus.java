package FRDL;

/**
 *
 * @author rmh
 */
public class AppStatus {
    //public static Boolean clientFullMode;
    //public static String mapCaption;
    //public static boolean champFileIsOpen;
    //the constructor
    AppStatus () {
        //clientFullMode = true;
        //mapCaption = "";
        //champFileIsOpen = false;

    }

/**
     * sets App.clientState true for 'full' mode and 'false' for 'download' mode.
     * There are two client states, "Full" and "Download"
     * This is evaluated on the basis of the comparison of two passwords
     * <p>
     * The "ChampionshipPassword" lives only on the client<br>
     * The "Password" is a hash of the ChampionshipPassword and it is copied to
     * logger files.
     * <p>
     * Scenarios:<br>
     * <b>"New championship file"</b>:
     * <br>Initially both passwords are null.  User has full access
     * to all settings.
     * <p>
     * <b>"Logger inserted"</b><br>
     * = If the logger has no logger.frdl file then user has full write
     * privileges on logger.<br>
     * = If the logger has a null Password, then user has full write privileges on logger.<br>
     * = If a hash of the ChampionshipPassword matches the Password, then user has full
     * write privileges on logger.  (this is the typical organizer situation).<br>
     * = If a hash of the ChampionshipPassword does NOT match the Password then FRDL uses the
     * championship &amp; task settings on the logger and sets itself to 'download' mode where
     * most settings are read-only.  (this is thge typical team leader or pilot situation
     * and means he will be using the exact same download settings as the organizer used.)
      * <p>
     * getClientState returns true for 'full' mode and 'false' for 'download' mode.
     *
     * @author rmh
     */
    
    public void setClientState() {
        App.clientFullMode = getClientState (App.thisChampionship.champData.readValue("password.master"),
                App.thisChampionship.champData.readValue("password.hashed"));
    }
    

    public Boolean getClientState (String plainPw, String hashedPw) {

        //System.out.println("getClientState plain pw:" + plainPw + ": length:" + plainPw.length());
        //System.out.println("getClientState hashed pw:" + hashedPw + ": length:" + hashedPw.length());

        Boolean clientState = false;

        if ((hashedPw == null||hashedPw.length()==0) && (plainPw == null||plainPw.length()==0)) {
            // both null - initial startup
            //System.out.println("plain pw and hashedpw both null - initial startup");
            clientState = true;
        }
        else if ((hashedPw == null||hashedPw.length()==0) && (plainPw != null||plainPw.length()>0)) {
            // next save will write hashed pw to Password -
            //dialog warning it can't be changed after that!
            //System.out.println("hashedpw null - plainPw has some length ONE TIME ONLY");
            clientState = true; //temporary only!
        }
        else if (hashedPw != null && plainPw != null) {
            // both not null, so check hash
            PasswordHasher ph = new PasswordHasher();
            clientState =ph.verifyPassword(plainPw, hashedPw);

            //System.out.println("2 plain pw hashed:" + PasswordHasher.hashPassword(plainPw));
            //System.out.println("2 logger pw:" + FRDLApp.thisChampionship.getPassword() + ": length:" + FRDLApp.thisChampionship.getPassword().length());
            //System.out.println("match verified:" + clientState);

        }
        MainView.addLog("Client full mode: " + clientState);
        return clientState;
    }


    public Boolean canProcessLogger() {
        if (App.thisChampionship == null) return false;

        if (App.clientFullMode) {
            if (App.thisChampionship.getItemAsString("championship.windowOpen","").trim().length() == 0 ||
                        App.thisChampionship.getItemAsString("championship.windowClose","").trim().length() == 0) {
                    MainView.setBottomStatus("Championship windowmust be set");
                    return false;
            }
            if (App.thisChampionship.getItemAsInt("championship.activeTask", -1) <= 0) {
                    MainView.setBottomStatus("Set Active task must be set");
                    return false;
            }

        }
        MainView.setBottomStatus("");
        return true;
    }
}
