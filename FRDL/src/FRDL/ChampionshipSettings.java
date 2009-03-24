/*
 * FRDLchampionshipSettings.java
 */

package FRDL;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import org.jdesktop.application.Action;
import org.joda.time.DateTime;

//import sun.security.util.Password;
/**
 * The championship settings dialog box.
 */
public class ChampionshipSettings extends javax.swing.JDialog {

    public ChampionshipSettings(java.awt.Frame parent) {
        super(parent);
        initComponents();
        getRootPane().setDefaultButton(saveButton);
        loadChampionshipSettings();
        //System.out.println("championship settings loaded");
        //MainView.addLog("Championship settings dialog loaded");

        setTzOffsetDisplay(Integer.parseInt(App.thisChampionship.champData.readValue("championship.utcOffsetH")),
                Integer.parseInt(App.thisChampionship.champData.readValue("championship.utcOffsetM")));

    }

    @Action public void closeChampionshipSettings() {
        if (saveChampionshipSettings()) {
            loadChampionshipSettings();
            dispose();
            //MainView.addLog("Championship settings dialog closed");
        }
    }

    /*
     * loads the data from App.thisChampionship into the form
    */
    private Boolean loadChampionshipSettings() {

        if (App.thisChampionship.champData.readValue("master.pathToFlightAnalysis").trim().length() == 0) {
            App.thisChampionship.champData.writeProperty("master.pathToFlightAnalysis", App.pathToAllFiles + File.separatorChar);
        }
        pathToFlightAnalysisTextField.setText(App.thisChampionship.champData.readValue("master.pathToFlightAnalysis"));
        championshipPw.setText(App.thisChampionship.champData.readValue("password.master"));
        championshipNameField.setText(App.thisChampionship.champData.readValue("championship.name"));

        int i = App.thisChampionship.getItemAsInt("championship.utcOffsetM",0);
        //there is probably a cleverer way of doing this
        if (i <= 45) offsetMinComboBox.setSelectedIndex(3);
        if (i <= 30) offsetMinComboBox.setSelectedIndex(2);
        if (i <= 15) offsetMinComboBox.setSelectedIndex(1);
        if (i < 15) offsetMinComboBox.setSelectedIndex(0);
        offsetHrComboBox.setSelectedIndex(App.thisChampionship.getItemAsInt("championship.utcOffsetH",0) + 11);

        adjustIgcFileTimesCheckBox.setSelected(App.thisChampionship.getItemAsBoolean("championship.adjustIgcFileTimes",false));

        championshipOpenDatePicker.setDate(App.thisChampionship.getItemAsDT("championship.windowOpen").toDate());
        championshipOpenDatePicker.setFormats(App.standardDateFormat);

        championshipCloseDatePicker.setDate(App.thisChampionship.getItemAsDT("championship.windowClose").toDate());
        championshipCloseDatePicker.setFormats(App.standardDateFormat);

        if (App.thisChampionship.getItemAsDT("championship.windowOpen").equals(App.thisChampionship.getItemAsDT("championship.windowClose"))) {
            //the blank championship template has equal window open & window close
            //this is a new championship, set championship window open to today
            DateTime dt = new DateTime();
            dt = dt.withMillisOfDay(0);
            championshipOpenDatePicker.setDate(dt.toDate());
            //and championship window close to today + 10 days + 1 min less than one day
            dt = dt.plusDays(10).plusMinutes(1439);
            championshipCloseDatePicker.setDate(dt.toDate());
        }

        setFieldsState(App.clientFullMode);

        MainView.addLog("Championship settings loaded");

        return true;
    }

    /*
     * saves the data from the form into App.thisChampionship
    */
    private Boolean saveChampionshipSettings() {
        Boolean ret = true;

        //backup the existing champ file
        App.thisChampionship.makeBackup();

        //write the championship settings
        App.thisChampionship.champData.writeProperty("championship.name",championshipNameField.getText().trim());
        App.thisChampionship.champData.writeProperty("master.pathToFlightAnalysis",pathToFlightAnalysisTextField.getText().trim());
        String plainPw = new String(championshipPw.getPassword()).trim();
        App.thisChampionship.champData.writeProperty("password.master", plainPw);
        //only write the hashed pw if we are in full mode
        if (plainPw.length() > 0 && App.clientFullMode == true) {
            PasswordHasher ph = new PasswordHasher();
            App.thisChampionship.champData.writeProperty("password.hashed",ph.hashPassword(plainPw));
        }

        App.thisChampionship.champData.writeProperty("championship.utcOffsetM",Integer.toString(offsetMinComboBox.getSelectedIndex()*15));

        if (offsetHrComboBox.getSelectedIndex() == 11) App.thisChampionship.champData.writeProperty("championship.utcOffsetH", "0");
        if (offsetHrComboBox.getSelectedIndex() > 11) App.thisChampionship.champData.writeProperty("championship.utcOffsetH", Integer.toString(offsetHrComboBox.getSelectedIndex()-11));
        if (offsetHrComboBox.getSelectedIndex() < 11) App.thisChampionship.champData.writeProperty("championship.utcOffsetH", Integer.toString((11 - offsetHrComboBox.getSelectedIndex()) * -1));

        App.thisChampionship.champData.writeProperty("championship.adjustIgcFileTimes",new Boolean(adjustIgcFileTimesCheckBox.isSelected()).toString());

        DateTime wo = new DateTime(championshipOpenDatePicker.getDate());
        wo = wo.withMillisOfDay(0);
        //System.out.println("window open " + wo);

        DateTime wc = new DateTime(championshipCloseDatePicker.getDate());
        wc = wc.withMillisOfDay(0).plusMinutes(1439).plusSeconds(59);
        //System.out.println("window close " + wc);

        if (ret == true) {
            if (wc.isAfter(wo.toInstant())) {
                App.thisChampionship.champData.writeProperty("championship.windowOpen",wo.toString());
                App.thisChampionship.champData.writeProperty("championship.windowClose", wc.toString());
                //MainView.addLog("Championship settings saved");
            } else {
                ret = false;
                //System.out.println("Error 3 ");
                Toolkit.getDefaultToolkit().beep();
                statusLabel.setText(App.getResourceMap().getString("openDateAfterClose"));
            }
        }
        return ret;
    }
    /*
     * sets fields enabled or disabled on this form according to
     * the client state in App.clientState
    */
    private Boolean setFieldsState (Boolean cs) {

        championshipNameField.setEditable(cs);
        offsetMinComboBox.setEnabled(cs);
        offsetHrComboBox.setEnabled(cs);
        adjustIgcFileTimesCheckBox.setEnabled(cs);
        championshipOpenDatePicker.setEnabled(cs);
        championshipCloseDatePicker.setEnabled(cs);
        if (cs) {
            statusLabel.setText("Full mode");

        } else {
            statusLabel.setText("Download mode");
        }
        return cs;
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        saveButton = new javax.swing.JButton();
        localTimePanel = new javax.swing.JPanel();
        offsetHrComboBox = new javax.swing.JComboBox();
        hoursLabel = new javax.swing.JLabel();
        offsetMinComboBox = new javax.swing.JComboBox();
        minutesLabel = new javax.swing.JLabel();
        adjustIgcFileTimesCheckBox = new javax.swing.JCheckBox();
        championshipWindowPanel = new javax.swing.JPanel();
        fromLabel = new javax.swing.JLabel();
        toLabel = new javax.swing.JLabel();
        championshipOpenDatePicker = new org.jdesktop.swingx.JXDatePicker();
        championshipCloseDatePicker = new org.jdesktop.swingx.JXDatePicker();
        championshipNamePanel = new javax.swing.JPanel();
        championshipNameField = new javax.swing.JTextField();
        outputPathPanel = new javax.swing.JPanel();
        pathToFlightAnalysisTextField = new javax.swing.JTextField();
        folderOpenButton = new javax.swing.JButton();
        passwordPanel = new javax.swing.JPanel();
        championshipPw = new javax.swing.JPasswordField();
        jLabel1 = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(FRDL.App.class).getContext().getResourceMap(ChampionshipSettings.class);
        setTitle(resourceMap.getString("championshipSettings.title")); // NOI18N
        setModal(true);
        setName("championshipSettings"); // NOI18N
        setResizable(false);
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(FRDL.App.class).getContext().getActionMap(ChampionshipSettings.class, this);
        saveButton.setAction(actionMap.get("closeChampionshipSettings")); // NOI18N
        saveButton.setFont(resourceMap.getFont("saveButton.font")); // NOI18N
        saveButton.setText(resourceMap.getString("saveButton.text")); // NOI18N
        saveButton.setName("saveButton"); // NOI18N

        localTimePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("localTimePanel.border.title"))); // NOI18N
        localTimePanel.setToolTipText(resourceMap.getString("localTimePanel.toolTipText")); // NOI18N
        localTimePanel.setName("localTimePanel"); // NOI18N

        offsetHrComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-11", "-10", "-9", "-8", "-7", "-6", "-5", "-4", "-3", "-2", "-1", "0", "+1", "+2", "+3", "+4", "+5", "+6", "+7", "+8", "+9", "+10", "+11" }));
        offsetHrComboBox.setSelectedIndex(11);
        offsetHrComboBox.setToolTipText(resourceMap.getString("offsetHrComboBox.toolTipText")); // NOI18N
        offsetHrComboBox.setName("offsetHrComboBox"); // NOI18N
        offsetHrComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                offsetHrComboBoxActionPerformed(evt);
            }
        });

        hoursLabel.setText(resourceMap.getString("hoursLabel.text")); // NOI18N
        hoursLabel.setName("hoursLabel"); // NOI18N

        offsetMinComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "15", "30", "45" }));
        offsetMinComboBox.setName("offsetMinComboBox"); // NOI18N
        offsetMinComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                offsetMinComboBoxActionPerformed(evt);
            }
        });

        minutesLabel.setText(resourceMap.getString("minutesLabel.text")); // NOI18N
        minutesLabel.setName("minutesLabel"); // NOI18N

        adjustIgcFileTimesCheckBox.setText(resourceMap.getString("adjustIgcFileTimesCheckBox.text")); // NOI18N
        adjustIgcFileTimesCheckBox.setToolTipText(resourceMap.getString("adjustIgcFileTimesCheckBox.toolTipText")); // NOI18N
        adjustIgcFileTimesCheckBox.setName("adjustIgcFileTimesCheckBox"); // NOI18N

        org.jdesktop.layout.GroupLayout localTimePanelLayout = new org.jdesktop.layout.GroupLayout(localTimePanel);
        localTimePanel.setLayout(localTimePanelLayout);
        localTimePanelLayout.setHorizontalGroup(
            localTimePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(localTimePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(offsetHrComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(hoursLabel)
                .add(8, 8, 8)
                .add(offsetMinComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 51, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(minutesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 21, Short.MAX_VALUE)
                .add(adjustIgcFileTimesCheckBox)
                .addContainerGap())
        );
        localTimePanelLayout.setVerticalGroup(
            localTimePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, localTimePanelLayout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(localTimePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(offsetHrComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(hoursLabel)
                    .add(offsetMinComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(minutesLabel)
                    .add(adjustIgcFileTimesCheckBox))
                .addContainerGap())
        );

        championshipWindowPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Overall championship window (local time)"));
        championshipWindowPanel.setToolTipText(resourceMap.getString("championshipWindowPanel.toolTipText")); // NOI18N
        championshipWindowPanel.setName("championshipWindowPanel"); // NOI18N

        fromLabel.setText(resourceMap.getString("fromLabel.text")); // NOI18N
        fromLabel.setName("fromLabel"); // NOI18N

        toLabel.setText(resourceMap.getString("toLabel.text")); // NOI18N
        toLabel.setName("toLabel"); // NOI18N

        championshipOpenDatePicker.setName("championshipOpenDatePicker"); // NOI18N

        championshipCloseDatePicker.setName("championshipCloseDatePicker"); // NOI18N

        org.jdesktop.layout.GroupLayout championshipWindowPanelLayout = new org.jdesktop.layout.GroupLayout(championshipWindowPanel);
        championshipWindowPanel.setLayout(championshipWindowPanelLayout);
        championshipWindowPanelLayout.setHorizontalGroup(
            championshipWindowPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(championshipWindowPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(fromLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(championshipOpenDatePicker, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 160, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(50, 50, 50)
                .add(toLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(championshipCloseDatePicker, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 159, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );
        championshipWindowPanelLayout.setVerticalGroup(
            championshipWindowPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, championshipWindowPanelLayout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(championshipWindowPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fromLabel)
                    .add(toLabel)
                    .add(championshipOpenDatePicker, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(championshipCloseDatePicker, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        championshipNamePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("championshipNamePanel.border.title"))); // NOI18N
        championshipNamePanel.setName("championshipNamePanel"); // NOI18N

        championshipNameField.setToolTipText(resourceMap.getString("championshipNameField.toolTipText")); // NOI18N
        championshipNameField.setName("championshipNameField"); // NOI18N

        org.jdesktop.layout.GroupLayout championshipNamePanelLayout = new org.jdesktop.layout.GroupLayout(championshipNamePanel);
        championshipNamePanel.setLayout(championshipNamePanelLayout);
        championshipNamePanelLayout.setHorizontalGroup(
            championshipNamePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, championshipNamePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(championshipNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                .addContainerGap())
        );
        championshipNamePanelLayout.setVerticalGroup(
            championshipNamePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(championshipNamePanelLayout.createSequentialGroup()
                .add(championshipNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        outputPathPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("outputPathPanel.border.title"))); // NOI18N
        outputPathPanel.setToolTipText(resourceMap.getString("outputPathPanel.toolTipText")); // NOI18N
        outputPathPanel.setName("outputPathPanel"); // NOI18N

        pathToFlightAnalysisTextField.setText(resourceMap.getString("pathToFlightAnalysisTextField.text")); // NOI18N
        pathToFlightAnalysisTextField.setToolTipText(resourceMap.getString("pathToFlightAnalysisTextField.toolTipText")); // NOI18N
        pathToFlightAnalysisTextField.setFocusable(false);
        pathToFlightAnalysisTextField.setName("pathToFlightAnalysisTextField"); // NOI18N

        folderOpenButton.setAction(actionMap.get("setFinalOutputPath")); // NOI18N
        folderOpenButton.setFont(resourceMap.getFont("folderOpenButton.font")); // NOI18N
        folderOpenButton.setIcon(resourceMap.getIcon("folderOpenButton.icon")); // NOI18N
        folderOpenButton.setText(resourceMap.getString("folderOpenButton.text")); // NOI18N
        folderOpenButton.setIconTextGap(2);
        folderOpenButton.setName("folderOpenButton"); // NOI18N

        org.jdesktop.layout.GroupLayout outputPathPanelLayout = new org.jdesktop.layout.GroupLayout(outputPathPanel);
        outputPathPanel.setLayout(outputPathPanelLayout);
        outputPathPanelLayout.setHorizontalGroup(
            outputPathPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(outputPathPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(folderOpenButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pathToFlightAnalysisTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                .addContainerGap())
        );
        outputPathPanelLayout.setVerticalGroup(
            outputPathPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(outputPathPanelLayout.createSequentialGroup()
                .add(outputPathPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pathToFlightAnalysisTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(folderOpenButton))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        passwordPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("passwordPanel.border.title"))); // NOI18N
        passwordPanel.setToolTipText(resourceMap.getString("passwordPanel.toolTipText")); // NOI18N
        passwordPanel.setName("passwordPanel"); // NOI18N

        championshipPw.setText(resourceMap.getString("championshipPw.text")); // NOI18N
        championshipPw.setToolTipText(resourceMap.getString("championshipPw.toolTipText")); // NOI18N
        championshipPw.setName("championshipPw"); // NOI18N
        championshipPw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                championshipPwKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                championshipPwKeyTyped(evt);
            }
        });

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        org.jdesktop.layout.GroupLayout passwordPanelLayout = new org.jdesktop.layout.GroupLayout(passwordPanel);
        passwordPanel.setLayout(passwordPanelLayout);
        passwordPanelLayout.setHorizontalGroup(
            passwordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(passwordPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(championshipPw, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 118, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 269, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(54, Short.MAX_VALUE))
        );
        passwordPanelLayout.setVerticalGroup(
            passwordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(passwordPanelLayout.createSequentialGroup()
                .add(passwordPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(championshipPw, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        statusLabel.setText(resourceMap.getString("statusLabel.text")); // NOI18N
        statusLabel.setName("statusLabel"); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, outputPathPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, championshipNamePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(localTimePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, passwordPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, championshipWindowPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(statusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 357, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(championshipNamePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(passwordPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(localTimePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(championshipWindowPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(outputPathPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 62, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(statusLabel))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /*
     * this checks whether the password is correct whenever you
     * type in the box  If there's a match then you go into full mode.
     * see ClientState for a full description of mode switching
    */
    private void championshipPwKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_championshipPwKeyTyped

    }//GEN-LAST:event_championshipPwKeyTyped

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        //loadChampionshipSettings();
        //System.out.println("championship settings loaded");
    }//GEN-LAST:event_formWindowGainedFocus

    private void championshipPwKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_championshipPwKeyReleased
        //System.out.println("------start typing------");
        //String pw = new String(championshipPw.getPassword()).trim();
        //System.out.println("plain password string:" + pw);
        //App.thisChampionship.champData.writeProperty("password.master", pw);
        //App.status.setClientState();
        //setFieldsState(App.clientFullMode);
    }//GEN-LAST:event_championshipPwKeyReleased

    private void offsetHrComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_offsetHrComboBoxActionPerformed
        int h = 0;
        if (offsetHrComboBox.getSelectedIndex() > 11) h = offsetHrComboBox.getSelectedIndex()-11;
        if (offsetHrComboBox.getSelectedIndex() < 11) h = (11 - offsetHrComboBox.getSelectedIndex()) * -1;
        int m = offsetMinComboBox.getSelectedIndex()*15;
        setTzOffsetDisplay(h,m);
    }//GEN-LAST:event_offsetHrComboBoxActionPerformed

    private void offsetMinComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_offsetMinComboBoxActionPerformed
        int h = 0;
        if (offsetHrComboBox.getSelectedIndex() > 11) h = offsetHrComboBox.getSelectedIndex()-11;
        if (offsetHrComboBox.getSelectedIndex() < 11) h = (11 - offsetHrComboBox.getSelectedIndex()) * -1;
        int m = offsetMinComboBox.getSelectedIndex()*15;
        setTzOffsetDisplay(h,m);
    }//GEN-LAST:event_offsetMinComboBoxActionPerformed

    /*
     * sets the path for final igc file output
    */
    @Action
    public void setFinalOutputPath() {

        JFileChooser fc = new JFileChooser();
        fc.setApproveButtonToolTipText(App.getResourceMap().getString("selectOutputDir"));
        fc.setApproveButtonText(App.getResourceMap().getString("selectOutputDir"));
        fc.setDialogTitle(App.getResourceMap().getString("selectOutputDir"));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        if (Utilities.directoryExists(App.thisChampionship.getItemAsString("master.pathToFlightAnalysis"))) {
            fc.setCurrentDirectory(new File(App.thisChampionship.getItemAsString("master.pathToFlightAnalysis")));
        }

        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String st = null; // + fc.getSelectedFile() + File.separatorChar;
            try {
                File f = fc.getSelectedFile();
                st = f.getCanonicalPath() + File.separatorChar;
            } catch (IOException ex) {
                //Logger.getLogger(ChampionshipSettings.class.getName()).log(Level.SEVERE, null, ex);
            }
            pathToFlightAnalysisTextField.setText(st.trim());
            App.thisChampionship.champData.writeProperty("master.pathToFlightAnalysis", st.trim());
        }
    }

    /*
     * sets the xx:yy of "Overall Championship window {xx:yy)"
     * to the current offset
     *
    */
    private void setTzOffsetDisplay(int h, int m) {
        championshipWindowPanel.setBorder(BorderFactory.createTitledBorder(
        App.getResourceMap().getString("championshipWindowPanelText") +
        " ("  +
        Utilities.makeTimeZoneOffset (h, m,true) +
        ")"
        ));
    }


    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox adjustIgcFileTimesCheckBox;
    private org.jdesktop.swingx.JXDatePicker championshipCloseDatePicker;
    private javax.swing.JTextField championshipNameField;
    private javax.swing.JPanel championshipNamePanel;
    private org.jdesktop.swingx.JXDatePicker championshipOpenDatePicker;
    private javax.swing.JPasswordField championshipPw;
    private javax.swing.JPanel championshipWindowPanel;
    private javax.swing.JButton folderOpenButton;
    private javax.swing.JLabel fromLabel;
    private javax.swing.JLabel hoursLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel localTimePanel;
    private javax.swing.JLabel minutesLabel;
    private javax.swing.JComboBox offsetHrComboBox;
    private javax.swing.JComboBox offsetMinComboBox;
    private javax.swing.JPanel outputPathPanel;
    private javax.swing.JPanel passwordPanel;
    private javax.swing.JTextField pathToFlightAnalysisTextField;
    private javax.swing.JButton saveButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel toLabel;
    // End of variables declaration//GEN-END:variables
    
}
