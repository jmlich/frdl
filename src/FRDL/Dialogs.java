package FRDL;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import org.jdesktop.application.ResourceMap;

/**
 * various popup dialogs
 * @author rmh
 */
public class Dialogs {
    private ResourceMap rm = App.getResourceMap();

    public void showErrorDialog(String message, Exception ex) {
	String title = rm.getString("Application.shortTitle") + " " + rm.getString("errorTitle");
	int type = JOptionPane.ERROR_MESSAGE;
	message = rm.getString("errorTitle") + ": " + message + "\n" + ex.getMessage();
	JOptionPane.showMessageDialog(App.getApplication().getMainFrame(), message, title, type);
    }

    public void showErrorDialog(String message) {
	String title = rm.getString("Application.shortTitle") + " " + rm.getString("errorTitle");
	int type = JOptionPane.ERROR_MESSAGE;
	message = rm.getString("errorTitle") + ": " + message + "\n";
	JOptionPane.showMessageDialog(App.getApplication().getMainFrame(), message, title, type);
    }

    public void showInfoDialog(String message) {
	String title = rm.getString("Application.shortTitle") + " " + rm.getString("infoTitle");
	int type = JOptionPane.INFORMATION_MESSAGE;
	JOptionPane.showMessageDialog(App.getApplication().getMainFrame(), message, title, type);
    }

    public Boolean showQuestionDialogOkCancel(String message) {
	String title = rm.getString("Application.shortTitle") + " " + rm.getString("infoTitle");
	int type = JOptionPane.OK_CANCEL_OPTION;
	int answer = JOptionPane.showConfirmDialog(App.getApplication().getMainFrame(), message, title, type);
        if (answer == JOptionPane.YES_OPTION) {
            // User clicked YES.
            return true;
        } else {
            // User clicked NO.
            return false;
        }
    }

    public Boolean showQuestionDialogYesNo(String message,Boolean yesIsDefault) {
        String title = rm.getString("Application.shortTitle") + " " + rm.getString("infoTitle");
        int type = JOptionPane.YES_NO_OPTION;
        int icon = JOptionPane.QUESTION_MESSAGE;
        Object[] options = {rm.getString("yesMsg"),rm.getString("noMsg")};
        //if yesIsDefault = true then Yes is the default button, false and No is the default button
        Object def = options[0];
        if(!yesIsDefault) {
            def = options[1];
        }
        int answer = JOptionPane.showOptionDialog(App.getApplication().getMainFrame(),
            message, title, type, icon, null,  options, def);
        if (answer == JOptionPane.YES_OPTION) {
            // User clicked YES.
            return true;
        } else {
            // User clicked NO.
            return false;
        }
    }

    public String showInputDialog(String message) {
        String title = rm.getString("Application.shortTitle") + " " + rm.getString("infoTitle");
        int type = JOptionPane.OK_CANCEL_OPTION;
        String text = JOptionPane.showInputDialog(App.getApplication().getMainFrame(), message, title, type);
        return text; //if (text == null) User clicked cancel
    }
    
    
    public String showPasswordDialog(String message) {
        String title = rm.getString("Application.shortTitle") + " " + rm.getString("infoTitle");
        int type = JOptionPane.OK_CANCEL_OPTION;
        JPasswordField jpf = new JPasswordField();
        int answer = JOptionPane.showConfirmDialog(App.getApplication().getMainFrame(), new Object[]{message, jpf}, title, type);
        if (answer == JOptionPane.YES_OPTION) {
            // User clicked YES.
            return new String(jpf.getPassword()).trim();
        } else {
            // User clicked NO.
            return null;
        }
    }





}
