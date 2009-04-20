/*
 * FRDLchampionshipSettings.java
 */

package FRDL;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.jdesktop.application.Action;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
/**
 * The settings-devices popup dialog box.
 */
public class GpsLoggerSettings extends javax.swing.JDialog {
    private String newLoggerPath = null;
    
    //the standard constructor
    public GpsLoggerSettings(java.awt.Frame parent) {
        super(parent);
        initComponents();
        getRootPane().setDefaultButton(saveButton);

        loadSettings();
    }

    //constructor if we are coming in from an unrecognized logger
    public GpsLoggerSettings(java.awt.Frame parent, String newLoggerPath) {
        super(parent);
        initComponents();
        getRootPane().setDefaultButton(saveButton);
        this.newLoggerPath = newLoggerPath;

        loadSettings();
    }


    @Action public void closeLoggerSettings() {
        //only close if all fields are filled
        if (loggerTypeComboBox.getSelectedIndex() > 0 &&
                pilotNameTextField.getText().trim().length() > 0 &&
                pilotNationTextField.getText().trim().length() > 0 &&
                pilotCompNoTextField.getText().trim().length() > 0) {
            saveSettings();
            dispose();
        } else {
            statusLabel.setText(App.getResourceMap().getString("notCompleteMsg"));
        }
        
    }

    /* loads up the form
     *
     */
    private void loadSettings() {
        DefaultComboBoxModel mo = new DefaultComboBoxModel();
        mo.addElement(App.getResourceMap().getString("pleaseSelectLoggerType"));
        ArrayList ar = App.gpsLoggersMaster.getTypes();
        for (int i=0;i<ar.size();i++) {
            mo.addElement(ar.get(i));
        }
        loggerTypeComboBox.setModel(mo);

        if (newLoggerPath == null) {
            //this is an existing logger
            
            String loggerType = App.logr.loggerFileContent.readValue("logger.type");
            for (int i = 0;i<mo.getSize();i++) {
                if (mo.getElementAt(i).equals(loggerType)) {
                    loggerTypeComboBox.setSelectedIndex(i);
                    break;
                }
            }
            loggerInfoLabel.setText("<HTML>" + App.gpsLoggersMaster.getValue(loggerType, "usernote") + "</HTML>");
            cimaStatusLabel.setText(App.gpsLoggersMaster.getValue(loggerType, "cimaApproval"));

            loggerUuidLabel.setText(App.logr.loggerFileContent.readValue("logger.uuid"));
            pilotNameTextField.setText(App.logr.loggerFileContent.readValue("pilot.name"));
            pilotNationTextField.setText(App.logr.loggerFileContent.readValue("pilot.nation"));
            pilotCompNoTextField.setText(App.logr.loggerFileContent.readValue("pilot.compNo"));
            loggerPriorityComboBox.setSelectedIndex(Integer.parseInt(App.logr.loggerFileContent.readValue("pilot.loggerPriority"))-1);
        } else {
            this.setTitle(App.getResourceMap().getString("newOrUnrecognizedTitle") + " " + this.getTitle());
            loggerUuidLabel.setText(UUID.randomUUID().toString());
        }

        setFieldsState(App.clientFullMode);
    }
    
    private void saveSettings() {
        if (newLoggerPath != null) {
            //this is a new logger
            App.logr.loggerFile = new File(newLoggerPath + App.getResourceMap().getString("loggerConfigFileName"));
            App.logr.loggerFileContent = new PropertiesIO(App.logr.loggerFile.getAbsolutePath());
            MainView.addLog("Created new " + App.getResourceMap().getString("loggerConfigFileName"));
            DateTime now = new DateTime().withZone(DateTimeZone.UTC);
            //logger.uuid to champData
            App.thisChampionship.champData.writeProperty("logger." + loggerUuidLabel.getText().trim(), now.toString());
            //password hashed
            App.logr.loggerFileContent.writeProperty("password.hashed",App.thisChampionship.getItemAsString("password.hashed"));
        }

        App.logr.loggerFileContent.writeProperty("logger.uuid", loggerUuidLabel.getText().trim());

        App.logr.loggerFileContent.writeProperty("logger.type",(String) loggerTypeComboBox.getSelectedItem());

        
        App.logr.loggerFileContent.writeProperty("pilot.name", Utilities.removeAccents(pilotNameTextField.getText().trim()));
        App.logr.loggerFileContent.writeProperty("pilot.nation", pilotNationTextField.getText().trim());
        App.logr.loggerFileContent.writeProperty("pilot.compNo", pilotCompNoTextField.getText().trim());
        App.logr.loggerFileContent.writeProperty("pilot.loggerPriority", Integer.toString(loggerPriorityComboBox.getSelectedIndex() + 1));
    }

    /*
     * sets fields enabled or disabled on this form according to
     * the client state in App.status.clientFullMode
    */
    private Boolean setFieldsState (Boolean cs) {
        loggerTypeComboBox.setEnabled(cs);
        pilotNameTextField.setEditable(cs);
        pilotNationTextField.setEditable(cs);
        pilotCompNoTextField.setEditable(cs);
        loggerPriorityComboBox.setEnabled(cs);
        if (cs) {
            statusLabel.setText(App.getResourceMap().getString("fullMode"));

        } else {
            statusLabel.setText(App.getResourceMap().getString("downloadMode"));
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
        loggerPanel = new javax.swing.JPanel();
        loggerUuidLabel = new javax.swing.JLabel();
        loggerTypeComboBox = new javax.swing.JComboBox();
        loggerInfoLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        cimaStatusTxt = new javax.swing.JLabel();
        cimaStatusLabel = new javax.swing.JLabel();
        pilotPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nationLabel = new javax.swing.JLabel();
        compNoLabel = new javax.swing.JLabel();
        loggerPriorityLabel = new javax.swing.JLabel();
        loggerPriorityComboBox = new javax.swing.JComboBox();
        pilotNameTextField = new javax.swing.JTextField();
        pilotNationTextField = new javax.swing.JTextField();
        pilotCompNoTextField = new javax.swing.JTextField();
        statusLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(FRDL.App.class).getContext().getResourceMap(GpsLoggerSettings.class);
        setTitle(resourceMap.getString("LoggerSettings.title")); // NOI18N
        setModal(true);
        setName("deviceSettings"); // NOI18N
        setResizable(false);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(FRDL.App.class).getContext().getActionMap(GpsLoggerSettings.class, this);
        saveButton.setAction(actionMap.get("closeLoggerSettings")); // NOI18N
        saveButton.setFont(resourceMap.getFont("saveButton.font")); // NOI18N
        saveButton.setText(resourceMap.getString("saveButton.text")); // NOI18N
        saveButton.setName("saveButton"); // NOI18N

        loggerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("loggerPanel.border.title"))); // NOI18N
        loggerPanel.setName("loggerPanel"); // NOI18N

        loggerUuidLabel.setFont(resourceMap.getFont("loggerUuidLabel.font")); // NOI18N
        loggerUuidLabel.setText(resourceMap.getString("loggerUuidLabel.text")); // NOI18N
        loggerUuidLabel.setName("loggerUuidLabel"); // NOI18N

        loggerTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Select a device type", "Item 2", "Item 3", "Item 4" }));
        loggerTypeComboBox.setName("loggerTypeComboBox"); // NOI18N
        loggerTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loggerTypeComboBoxActionPerformed(evt);
            }
        });

        loggerInfoLabel.setForeground(resourceMap.getColor("loggerInfoLabel.foreground")); // NOI18N
        loggerInfoLabel.setText(resourceMap.getString("loggerInfoLabel.text")); // NOI18N
        loggerInfoLabel.setName("loggerInfoLabel"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        cimaStatusTxt.setText(resourceMap.getString("cimaStatusTxt.text")); // NOI18N
        cimaStatusTxt.setName("cimaStatusTxt"); // NOI18N

        cimaStatusLabel.setFont(resourceMap.getFont("cimaStatusLabel.font")); // NOI18N
        cimaStatusLabel.setText(resourceMap.getString("cimaStatusLabel.text")); // NOI18N
        cimaStatusLabel.setName("cimaStatusLabel"); // NOI18N

        org.jdesktop.layout.GroupLayout loggerPanelLayout = new org.jdesktop.layout.GroupLayout(loggerPanel);
        loggerPanel.setLayout(loggerPanelLayout);
        loggerPanelLayout.setHorizontalGroup(
            loggerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, loggerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(loggerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, loggerInfoLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, loggerTypeComboBox, 0, 510, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, loggerPanelLayout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(loggerUuidLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, loggerPanelLayout.createSequentialGroup()
                        .add(cimaStatusTxt)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cimaStatusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)))
                .addContainerGap())
        );
        loggerPanelLayout.setVerticalGroup(
            loggerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(loggerPanelLayout.createSequentialGroup()
                .add(loggerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(loggerUuidLabel)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(loggerTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(loggerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cimaStatusTxt)
                    .add(cimaStatusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(loggerInfoLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pilotPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pilotPanel.border.title"))); // NOI18N
        pilotPanel.setName("pilotPanel"); // NOI18N

        nameLabel.setText(resourceMap.getString("nameLabel.text")); // NOI18N
        nameLabel.setName("nameLabel"); // NOI18N

        nationLabel.setText(resourceMap.getString("nationLabel.text")); // NOI18N
        nationLabel.setName("nationLabel"); // NOI18N

        compNoLabel.setText(resourceMap.getString("compNoLabel.text")); // NOI18N
        compNoLabel.setName("compNoLabel"); // NOI18N

        loggerPriorityLabel.setText(resourceMap.getString("loggerPriorityLabel.text")); // NOI18N
        loggerPriorityLabel.setName("loggerPriorityLabel"); // NOI18N

        loggerPriorityComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" }));
        loggerPriorityComboBox.setName("loggerPriorityComboBox"); // NOI18N

        pilotNameTextField.setText(resourceMap.getString("pilotNameTextField.text")); // NOI18N
        pilotNameTextField.setName("pilotNameTextField"); // NOI18N

        pilotNationTextField.setText(resourceMap.getString("pilotNationTextField.text")); // NOI18N
        pilotNationTextField.setName("pilotNationTextField"); // NOI18N

        pilotCompNoTextField.setText(resourceMap.getString("pilotCompNoTextField.text")); // NOI18N
        pilotCompNoTextField.setName("pilotCompNoTextField"); // NOI18N

        org.jdesktop.layout.GroupLayout pilotPanelLayout = new org.jdesktop.layout.GroupLayout(pilotPanel);
        pilotPanel.setLayout(pilotPanelLayout);
        pilotPanelLayout.setHorizontalGroup(
            pilotPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pilotPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(pilotPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(loggerPriorityLabel)
                    .add(nameLabel)
                    .add(nationLabel)
                    .add(compNoLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pilotPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pilotPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(pilotPanelLayout.createSequentialGroup()
                            .add(loggerPriorityComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(326, 326, 326))
                        .add(pilotPanelLayout.createSequentialGroup()
                            .add(pilotNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                            .addContainerGap()))
                    .add(pilotPanelLayout.createSequentialGroup()
                        .add(pilotPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, pilotCompNoTextField)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, pilotNationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        pilotPanelLayout.setVerticalGroup(
            pilotPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pilotPanelLayout.createSequentialGroup()
                .add(pilotPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pilotNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(nameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pilotPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nationLabel)
                    .add(pilotNationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(9, 9, 9)
                .add(pilotPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(compNoLabel)
                    .add(pilotCompNoTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pilotPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(loggerPriorityLabel)
                    .add(loggerPriorityComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        statusLabel.setText(resourceMap.getString("statusLabel.text")); // NOI18N
        statusLabel.setName("statusLabel"); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, loggerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(statusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 428, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 28, Short.MAX_VALUE)
                        .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, pilotPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(loggerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pilotPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(statusLabel))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void loggerTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loggerTypeComboBoxActionPerformed
        JComboBox cb = (JComboBox)evt.getSource();
        String newSelection = (String)cb.getSelectedItem();
        //makeBaseKey(CimaLoggersMaster.readKey(newSelection));
        try {
        loggerInfoLabel.setText("<HTML>" + App.gpsLoggersMaster.getValue(newSelection, "usernote") + "</HTML>");
        cimaStatusLabel.setText(App.gpsLoggersMaster.getValue(newSelection, "cimaApproval"));
        } catch (Exception e) {
            //probably selected "please select a logger"
        }
    }//GEN-LAST:event_loggerTypeComboBoxActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cimaStatusLabel;
    private javax.swing.JLabel cimaStatusTxt;
    private javax.swing.JLabel compNoLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel loggerInfoLabel;
    private javax.swing.JPanel loggerPanel;
    private javax.swing.JComboBox loggerPriorityComboBox;
    private javax.swing.JLabel loggerPriorityLabel;
    private javax.swing.JComboBox loggerTypeComboBox;
    private javax.swing.JLabel loggerUuidLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel nationLabel;
    private javax.swing.JTextField pilotCompNoTextField;
    private javax.swing.JTextField pilotNameTextField;
    private javax.swing.JTextField pilotNationTextField;
    private javax.swing.JPanel pilotPanel;
    private javax.swing.JButton saveButton;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables
    
}
