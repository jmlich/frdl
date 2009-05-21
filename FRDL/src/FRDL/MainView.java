package FRDL;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskService;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import javax.swing.DefaultListModel;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.jdesktop.swingworker.SwingWorker;
import org.joda.time.LocalDateTime;

/**
 * The application's main frame.
 * All gui stuff starts from this page.
 */
public class MainView extends FrameView {
    public static DefaultListModel logListModel = new DefaultListModel();
    private DriveScanTask driveScan = null;
    ResourceMap rm = App.getResourceMap();
    private JDialog championshipSettings;
    private JDialog taskSettings;
    private JDialog loggerSettings;
    private JDialog loggersInfo;
    private boolean loggerIsConnected = false;
    private boolean stopRepaint = false;

    public MainView(SingleFrameApplication main_app) {
        super(main_app);

        //initialize all the GUI bits
        initComponents();

        //a quick link to the last championship file which was opened.
        setQuickAccessMenuItem();

        //TODO can't get this to work....
        //URL imgURL = getClass().getResource("resources/jet16.png");
        //Image icon = Toolkit.getDefaultToolkit().getImage(imgURL);
        //App.getApplication().getMainFrame().setIconImage(icon);

        //check with flymicro.com to see if this is the latest version
        CheckLatestVersionTask t = new CheckLatestVersionTask();
        t.execute();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = App.getApplication().getMainFrame();
            aboutBox = new AboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        App.getApplication().show(aboutBox);
    }

    @Action(enabledProperty = "champFileIsOpen")
    public void showChampionshipSettings() {
        JFrame mainFrame = App.getApplication().getMainFrame();
        championshipSettings = new ChampionshipSettings(mainFrame);
        championshipSettings.setLocationRelativeTo(mainFrame);
        App.getApplication().show(championshipSettings);
    }

        @Action(enabledProperty = "champFileIsOpen")
    public void showTaskSettings() {
        int ActiveTaskBefore = App.thisChampionship.getItemAsInt("championship.activeTask", 1);

        JFrame mainFrame = App.getApplication().getMainFrame();
        taskSettings = new TaskSettings(mainFrame);
        taskSettings.setLocationRelativeTo(mainFrame);
        App.getApplication().show(taskSettings);

        if (loggerIsConnected && ActiveTaskBefore != App.thisChampionship.getItemAsInt("championship.activeTask", 1)) {
            runBackupAndProcessTask();
        }
    }

    @Action(enabledProperty = "loggerIsConnected")
    public void showLoggerSettings() {
        JFrame mainFrame = App.getApplication().getMainFrame();
        loggerSettings = new GpsLoggerSettings(mainFrame);
        loggerSettings.setLocationRelativeTo(mainFrame);
        App.getApplication().show(loggerSettings);
        //after dialog is closed...
        System.out.println("is this after the dialog has closed?");
    }

    @Action(enabledProperty = "champFileIsOpen")
    public void showLoggersInfo() {
        JFrame mainFrame = App.getApplication().getMainFrame();
        loggersInfo = new GpsLoggersInfo(mainFrame);
        loggersInfo.setLocationRelativeTo(mainFrame);
        App.getApplication().show(loggersInfo);
    }


    /* Utility which writes to the log pane
     * only allows 100 lines
     * syntax addLog(String);
    */
    public static Boolean addLog(String str) {
        logListModel.add(0,str);
        int pos = logListModel.getSize()-1;
        if (pos >= 100) {
            logListModel.remove(pos);
        }
        return true;
    }

        @Action
    public void setUserLanguage(String lang) {
        Dialogs d = new Dialogs();
        App.sessionProperties.setProperty("nextStartLanguage",lang);
        //System.out.println("next lanf" + FRDLApp.sessionProperties.getProperty("nextStartLanguage"));
        MainView.addLog("Language will be "+lang+" on next startup");
        d.showInfoDialog(rm.getString("changeLangRestartMessage"));
    }
        
    private void setQuickAccessMenuItem () {
        quickOpenMenuItem.setVisible(false);
        quickOpenSeparator.setVisible(false);
        if (App.sessionProperties.getProperty("lastChampionshipFile") != null) {
            File f = new File(App.sessionProperties.getProperty("lastChampionshipFile"));
            if (Utilities.fileExists(f.getAbsolutePath())) {
                quickOpenMenuItem.setText(f.getName());
                quickOpenMenuItem.setVisible(!isChampFileIsOpen());
                quickOpenSeparator.setVisible(!isChampFileIsOpen());
            }
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        outerSplitPane = new javax.swing.JSplitPane();
        innerSplitPane = new javax.swing.JSplitPane();
        mapPanel = new FRDL.MapView();
        altPanel = new FRDL.AltitudeView();
        logPane = new javax.swing.JScrollPane();
        logList = new javax.swing.JList(logListModel);
        topPanel = new javax.swing.JPanel();
        mainStatusLabel = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        quickOpenMenuItem = new javax.swing.JMenuItem();
        quickOpenSeparator = new javax.swing.JSeparator();
        openChampMenuItem = new javax.swing.JMenuItem();
        newChampMenuItem = new javax.swing.JMenuItem();
        saveChampAsMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        settingsMenu = new javax.swing.JMenu();
        championshipSettingsMenuItem = new javax.swing.JMenuItem();
        TaskSettingsMenuItem = new javax.swing.JMenuItem();
        loggerMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        forceDownloadMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        LanguageMenu = new javax.swing.JMenu();
        languageMenuItem_EN = new javax.swing.JMenuItem();
        languageMenuItem_CZ = new javax.swing.JMenuItem();
        languageMenuItem_FR = new javax.swing.JMenuItem();
        languageMenuItem_ES = new javax.swing.JMenuItem();
        infoMenu = new javax.swing.JMenu();
        loggerInfoMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        helpMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        FormListener formListener = new FormListener();

        mainPanel.setName("mainPanel"); // NOI18N

        outerSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        outerSplitPane.setResizeWeight(1.0);
        outerSplitPane.setMinimumSize(new java.awt.Dimension(3, 5));
        outerSplitPane.setName("outerSplitPane"); // NOI18N

        innerSplitPane.setDividerSize(8);
        innerSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        innerSplitPane.setResizeWeight(0.4);
        innerSplitPane.setMinimumSize(new java.awt.Dimension(0, 0));
        innerSplitPane.setName("innerSplitPane"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(FRDL.App.class).getContext().getResourceMap(MainView.class);
        mapPanel.setBackground(resourceMap.getColor("mapPanel.background")); // NOI18N
        mapPanel.setMinimumSize(new java.awt.Dimension(400, 200));
        mapPanel.setName("mapPanel"); // NOI18N

        org.jdesktop.layout.GroupLayout mapPanelLayout = new org.jdesktop.layout.GroupLayout(mapPanel);
        mapPanel.setLayout(mapPanelLayout);
        mapPanelLayout.setHorizontalGroup(
            mapPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 451, Short.MAX_VALUE)
        );
        mapPanelLayout.setVerticalGroup(
            mapPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 201, Short.MAX_VALUE)
        );

        innerSplitPane.setLeftComponent(mapPanel);

        altPanel.setBackground(resourceMap.getColor("altPanel.background")); // NOI18N
        altPanel.setMinimumSize(new java.awt.Dimension(400, 100));
        altPanel.setName("altPanel"); // NOI18N

        org.jdesktop.layout.GroupLayout altPanelLayout = new org.jdesktop.layout.GroupLayout(altPanel);
        altPanel.setLayout(altPanelLayout);
        altPanelLayout.setHorizontalGroup(
            altPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 451, Short.MAX_VALUE)
        );
        altPanelLayout.setVerticalGroup(
            altPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 278, Short.MAX_VALUE)
        );

        innerSplitPane.setRightComponent(altPanel);

        outerSplitPane.setTopComponent(innerSplitPane);

        logPane.setMinimumSize(new java.awt.Dimension(0, 0));
        logPane.setName("logPane"); // NOI18N
        logPane.setOpaque(false);
        logPane.setPreferredSize(new java.awt.Dimension(260, 0));

        logList.setName("logList"); // NOI18N
        logPane.setViewportView(logList);

        outerSplitPane.setRightComponent(logPane);

        topPanel.setName("topPanel"); // NOI18N

        mainStatusLabel.setFont(new java.awt.Font("Tahoma", 0, 18));
        mainStatusLabel.setForeground(resourceMap.getColor("mainStatusLabel.foreground")); // NOI18N
        mainStatusLabel.setName("mainStatusLabel"); // NOI18N

        org.jdesktop.layout.GroupLayout topPanelLayout = new org.jdesktop.layout.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(topPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mainStatusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
                .addContainerGap())
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, mainStatusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(topPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(outerSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .add(topPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(outerSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        quickOpenMenuItem.setText(resourceMap.getString("quickOpenMenuItem.text")); // NOI18N
        quickOpenMenuItem.setHideActionText(true);
        quickOpenMenuItem.setName("quickOpenMenuItem"); // NOI18N
        quickOpenMenuItem.addActionListener(formListener);
        fileMenu.add(quickOpenMenuItem);

        quickOpenSeparator.setName("quickOpenSeparator"); // NOI18N
        fileMenu.add(quickOpenSeparator);

        openChampMenuItem.setText(resourceMap.getString("openChampMenuItem.text")); // NOI18N
        openChampMenuItem.setName("openChampMenuItem"); // NOI18N
        openChampMenuItem.addActionListener(formListener);
        fileMenu.add(openChampMenuItem);

        newChampMenuItem.setText(resourceMap.getString("newChampMenuItem.text")); // NOI18N
        newChampMenuItem.setName("newChampMenuItem"); // NOI18N
        newChampMenuItem.addActionListener(formListener);
        fileMenu.add(newChampMenuItem);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(FRDL.App.class).getContext().getActionMap(MainView.class, this);
        saveChampAsMenuItem.setAction(actionMap.get("saveChampionshipAs")); // NOI18N
        saveChampAsMenuItem.setText(resourceMap.getString("saveChampAsMenuItem.text")); // NOI18N
        saveChampAsMenuItem.setName("saveChampAsMenuItem"); // NOI18N
        saveChampAsMenuItem.addActionListener(formListener);
        fileMenu.add(saveChampAsMenuItem);

        jSeparator1.setName("jSeparator1"); // NOI18N
        fileMenu.add(jSeparator1);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        settingsMenu.setText(resourceMap.getString("settingsMenu.text")); // NOI18N
        settingsMenu.setName("settingsMenu"); // NOI18N

        championshipSettingsMenuItem.setAction(actionMap.get("showChampionshipSettings")); // NOI18N
        championshipSettingsMenuItem.setText(resourceMap.getString("championshipSettingsMenuItem.text")); // NOI18N
        championshipSettingsMenuItem.setName("championshipSettingsMenuItem"); // NOI18N
        settingsMenu.add(championshipSettingsMenuItem);

        TaskSettingsMenuItem.setAction(actionMap.get("showTaskSettings")); // NOI18N
        TaskSettingsMenuItem.setText(resourceMap.getString("TaskSettingsMenuItem.text")); // NOI18N
        TaskSettingsMenuItem.setName("TaskSettingsMenuItem"); // NOI18N
        settingsMenu.add(TaskSettingsMenuItem);

        loggerMenuItem.setAction(actionMap.get("showLoggerSettings")); // NOI18N
        loggerMenuItem.setText(resourceMap.getString("loggerMenuItem.text")); // NOI18N
        loggerMenuItem.setName("loggerMenuItem"); // NOI18N
        settingsMenu.add(loggerMenuItem);

        jSeparator2.setName("jSeparator2"); // NOI18N
        settingsMenu.add(jSeparator2);

        forceDownloadMenuItem.setAction(actionMap.get("forceDownload")); // NOI18N
        forceDownloadMenuItem.setText(resourceMap.getString("forceDownloadMenuItem.text")); // NOI18N
        forceDownloadMenuItem.setName("forceDownloadMenuItem"); // NOI18N
        settingsMenu.add(forceDownloadMenuItem);

        jSeparator3.setName("jSeparator3"); // NOI18N
        settingsMenu.add(jSeparator3);

        LanguageMenu.setText(resourceMap.getString("LanguageMenu.text")); // NOI18N
        LanguageMenu.setName("LanguageMenu"); // NOI18N

        languageMenuItem_EN.setText(resourceMap.getString("languageMenuItem_EN.text")); // NOI18N
        languageMenuItem_EN.setName("languageMenuItem_EN"); // NOI18N
        languageMenuItem_EN.addActionListener(formListener);
        LanguageMenu.add(languageMenuItem_EN);

        languageMenuItem_CZ.setText(resourceMap.getString("languageMenuItem_CZ.text")); // NOI18N
        languageMenuItem_CZ.setName("languageMenuItem_CZ"); // NOI18N
        languageMenuItem_CZ.addActionListener(formListener);
        LanguageMenu.add(languageMenuItem_CZ);

        languageMenuItem_FR.setText(resourceMap.getString("languageMenuItem_FR.text")); // NOI18N
        languageMenuItem_FR.setName("languageMenuItem_FR"); // NOI18N
        languageMenuItem_FR.addActionListener(formListener);
        LanguageMenu.add(languageMenuItem_FR);

        languageMenuItem_ES.setText(resourceMap.getString("languageMenuItem_ES.text")); // NOI18N
        languageMenuItem_ES.setName("languageMenuItem_ES"); // NOI18N
        languageMenuItem_ES.addActionListener(formListener);
        LanguageMenu.add(languageMenuItem_ES);

        settingsMenu.add(LanguageMenu);

        menuBar.add(settingsMenu);

        infoMenu.setText(resourceMap.getString("infoMenu.text")); // NOI18N
        infoMenu.setName("infoMenu"); // NOI18N

        loggerInfoMenuItem.setAction(actionMap.get("showLoggersInfo")); // NOI18N
        loggerInfoMenuItem.setText(resourceMap.getString("loggerInfoMenuItem.text")); // NOI18N
        loggerInfoMenuItem.setName("loggerInfoMenuItem"); // NOI18N
        infoMenu.add(loggerInfoMenuItem);

        menuBar.add(infoMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        helpMenuItem.setAction(actionMap.get("openHelp")); // NOI18N
        helpMenuItem.setText(resourceMap.getString("helpMenuItem.text")); // NOI18N
        helpMenuItem.setName("helpMenuItem"); // NOI18N
        helpMenu.add(helpMenuItem);

        jSeparator4.setName("jSeparator4"); // NOI18N
        helpMenu.add(jSeparator4);

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)
            .add(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(statusMessageLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 285, Short.MAX_VALUE)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelLayout.createSequentialGroup()
                .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(statusMessageLabel)
                    .add(statusAnimationLabel)
                    .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == openChampMenuItem) {
                MainView.this.openChampMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == newChampMenuItem) {
                MainView.this.newChampMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == saveChampAsMenuItem) {
                MainView.this.saveChampAsMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == languageMenuItem_EN) {
                MainView.this.languageMenuItem_ENActionPerformed(evt);
            }
            else if (evt.getSource() == languageMenuItem_FR) {
                MainView.this.languageMenuItem_FRActionPerformed(evt);
            }
            else if (evt.getSource() == quickOpenMenuItem) {
                MainView.this.quickOpenMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == languageMenuItem_CZ) {
                MainView.this.languageMenuItem_CZActionPerformed(evt);
            }
            else if (evt.getSource() == languageMenuItem_ES) {
                MainView.this.languageMenuItem_ESActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void openChampMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openChampMenuItemActionPerformed
        setChampFileIsOpen(Utilities.openChampionship());
}//GEN-LAST:event_openChampMenuItemActionPerformed

    private void newChampMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newChampMenuItemActionPerformed
        setChampFileIsOpen(Utilities.newChampionship());
        if (App.champFileIsOpen) {
            showChampionshipSettings();
           if (App.thisChampionship.getItemAsInt("championship.activeTask", -1) <= 0) showTaskSettings(); 
        }
        
}//GEN-LAST:event_newChampMenuItemActionPerformed

    private void saveChampAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveChampAsMenuItemActionPerformed
        // NOT USED see saveChampionshipAs()
    }//GEN-LAST:event_saveChampAsMenuItemActionPerformed

    private void languageMenuItem_ENActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_languageMenuItem_ENActionPerformed
        setUserLanguage("en");
    }//GEN-LAST:event_languageMenuItem_ENActionPerformed

    private void languageMenuItem_FRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_languageMenuItem_FRActionPerformed
        //setUserLanguage("fr");
        Dialogs d = new Dialogs();
        d.showInfoDialog("There is no French translation at the moment,\nbut with your help there could be....\n\nPlease contact the developer.");
    }//GEN-LAST:event_languageMenuItem_FRActionPerformed

    private void quickOpenMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quickOpenMenuItemActionPerformed
        File f = new File(App.sessionProperties.getProperty("lastChampionshipFile"));
        setChampFileIsOpen(Utilities.loadChampionshipFile(f));
    }//GEN-LAST:event_quickOpenMenuItemActionPerformed

    private void languageMenuItem_CZActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_languageMenuItem_CZActionPerformed
        //setUserLanguage("cz");
        Dialogs d = new Dialogs();
        d.showInfoDialog("There is no Czech translation at the moment,\nbut with your help there could be....\n\nPlease contact the developer.");
    }//GEN-LAST:event_languageMenuItem_CZActionPerformed

    private void languageMenuItem_ESActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_languageMenuItem_ESActionPerformed
        //setUserLanguage("es");
        Dialogs d = new Dialogs();
        d.showInfoDialog("There is no Spanish translation at the moment,\nbut with your help there could be....\n\nPlease contact the developer.");
    }//GEN-LAST:event_languageMenuItem_ESActionPerformed
    
    // Begin swingWorker
    /* Starts the task which (almost) continuously scans
     * for added or removed root drives
     *
     */
    public void startDriveScanTask() {
        if (driveScan == null) {
            (driveScan = new DriveScanTask()).execute();
            repaintMaps();
        } else {
            addLog("Drive scan already running!");
        }
    }
    /* Stops the task which scans for added or removed root drives
     *
     */
    public void stopDriveScanTask() {
        if (driveScan != null) {
            try {
                driveScan.cancel(true);
            } catch (CancellationException ex) {
                //dunno why, but this always throws an error;
            }
            driveScan = null;
            addLog("Drive scan stopped");
            App.mapCaption = "";
            repaintMaps();
        } else {
            addLog("no Drive scan to stop!");
        }
    }
    
    public void repaintMaps () {
        //there's some sort of threading issue where
        //the maps are repainting during the asynchronous processing?
        if (!stopRepaint) {
             mapPanel.repaint();
             altPanel.repaint();
        }
    }

    public void stopRepaint(Boolean stop) {
        this.stopRepaint = stop;
        if (stop == false) repaintMaps ();
    }

    /*
     * Sets the big status message
     *
     */
    public static Boolean setMainStatus(String st) {
        String stx = "";
        //if (App.thisChampionship != null && App.thisChampionship.getItemAsInt("championship.activeTask", -1) > 0) {
        if (App.status.canProcessLogger()) {
           stx = App.getResourceMap().getString("thisTask") + " " + App.thisChampionship.getItemAsString("championship.activeTask") + ": ";
        }
        mainStatusLabel.setText(stx + st);
        return true;
    }

        /*
     * Sets the bottom status message
     *
     */
    public static void setBottomStatus(String st) {
        statusMessageLabel.setText(st);
        //App.mapCaption = st;
    }


// START check for latest version task (swingWorker)

    /*
     * this takes a look at
     * http://www.flymicro.com/frdl/checkLatestVersion.cfm
     * if no contact ot current version is OK, do nothing
     * otherwise dialog with reminder
     *
     */
    private class CheckLatestVersionTask extends SwingWorker<Boolean , Void> {
        String latestVersion = null;
        
        //the constructor
        public CheckLatestVersionTask() {
            
        }
        /*
         * this is happening quietly in a separate thread
         * although this seems to work, will it on a crappy connection?
         * seems no ability to set a timeout
         * perhaps it doesn't matter - there's no dialog or anything
         * if it fails.  
         * Did I neet to go to all the trouble to put 
         * it in a separate thread though?
         * 
         * */
        @Override  protected Boolean doInBackground() {
            try {

                URL flyMicro = new URL("http://www.flymicro.com/frdl/checkLatestVersion.cfm");
                BufferedReader in = new BufferedReader(new InputStreamReader(flyMicro.openStream()));
                String pageContent = "";
                String thisLine;
                while ((thisLine = in.readLine()) != null) {
                    pageContent = pageContent + thisLine;
                }
                in.close();
                latestVersion = pageContent.trim();
                if (App.getResourceMap().getString("Application.version").trim().equals(pageContent.trim())) {
                    return true;
                } else {
                    return false;
                }
            } catch (IOException ex) {
                return false;
            }
        }

        /* Back on the EDT
         * runs when the task ends
         */
        @Override public void done() {
            try {
                Boolean result = get();
                if (result) {
                    addLog("This is the latest version of FRDL (v " + latestVersion + ")");
                } else {
                    if (latestVersion != null) {
                        addLog("Need to upgrade to FRDL v " + latestVersion);
                        Dialogs d = new Dialogs();
                        String msg = "(" + 
                                App.getResourceMap().getString("Application.name") + " " + 
                                App.getResourceMap().getString("version.line1") + " " +
                                App.getResourceMap().getString("Application.version") + ")\n\n" +
                                App.getResourceMap().getString("Application.name") + " " +
                                App.getResourceMap().getString("version.line1") + " " +
                                latestVersion + " " +
                                App.getResourceMap().getString("version.line2") + "\n" +
                                App.getResourceMap().getString("Application.homepage");
                        d.showInfoDialog(msg);
                    } else {
                        addLog("Unable to check flymicro.com for latest FRDL version.");
                    }
                }
                //System.out.println("result: " + latestVersion);
                
            } catch (InterruptedException ex) {
                //do nothing
            } catch (ExecutionException ex) {
                //do nothing
            }
        }
    }
// END check for latest version task (swingWorker)

// START drivescan asynchronous task (swingWorker)
    
    /*
     * this is a small class for holding the result of
     * the continuous scan on drives
     *
     */
    private static class driveData {
        private final String msg;
        private final String path;
        private final File file;

        driveData(String msg, String path, File file) {
            this.msg = msg;
            this.path = path;
            this.file = file;
        }
        public String getMsg() {
            return this.msg;
        }
        public String getPath() {
            return this.path;
        }
        public File getFile() {
            return this.file;
        }
    }

    /*
     * this scans for addition or removal of a drive from
     * the box in a separate thread.
     */
    private class DriveScanTask extends SwingWorker<driveData , Void> {
        File[] startupRoots = null;

        //the constructor
        public DriveScanTask() {
            this.startupRoots = App.startupRoots;
        }

        @Override  protected driveData doInBackground() {
            //this is happening quietly in a separate thread
            File[] currentRoots = File.listRoots();
            //loops continuously looking for any change in
            //the drive roots.  If there is a change, then it
            //breaks out.
            while (Arrays.equals(startupRoots,currentRoots)) {
                try {
                    Thread.sleep(1000);
                    //System.out.println("orig:" + App.startupRoots.length + " now:" + f.length);
                    currentRoots = File.listRoots();
                } catch (InterruptedException ex) {
                    return null;
                } 
            }

            String msg = "No drive change";  //if this shows up in the log there was some sort of error...
            String path = null;
            File f = null;
            //if a drive has been added
            if (currentRoots.length > startupRoots.length) {
                //find the new drive
                for (int i1 = 0; i1 < currentRoots.length;i1++) {
                    Boolean found = false;
                    for (int i2 = 0;i2<startupRoots.length;i2++) {
                        if (currentRoots[i1].equals(startupRoots[i2])) {
                            found = true;
                            break;
                        }
                    }
                    if (found == false) {
                        //this is the new one
                        path = currentRoots[i1].getAbsolutePath();
                        f = currentRoots[i1];
                        break;
                    }
                }
                msg = App.getResourceMap().getString("driveAddedMsg") + " " + path;
            // if a drive has been removed
            } else if (currentRoots.length < startupRoots.length) {
                 //find the removed drive
                for (int i1 = 0; i1 < startupRoots.length;i1++) {
                    Boolean found = false;
                    for (int i2 = 0;i2<currentRoots.length;i2++) {
                        if (startupRoots[i1].equals(currentRoots[i2])) {
                            found = true;
                            break;
                        }
                    }
                    if (found == false) {
                        //this is the removed one
                        path = startupRoots[i1].getAbsolutePath();
                        break;
                    }
                }
                msg = App.getResourceMap().getString("driveRemovedMsg") + " (" + path + ")";
            }

            return new driveData(msg, path, f);
        }

        /* Back on the EDT
         * runs when the task ends
         */
        @Override public void done() {
            driveData scanResult = null;
            App.startupRoots = File.listRoots();

            try {
                scanResult = get();
                addLog(scanResult.getMsg());
                
                if (scanResult.getFile() != null) {
                    //in other words a drive has been added
                    //System.out.println("inspect drive now...");
                    App.mapCaption = App.getResourceMap().getString("doNotDisconnectMsg");
                    setMainStatus(App.getResourceMap().getString("doNotDisconnectMsg"));
                    setBottomStatus(App.getResourceMap().getString("doNotDisconnectMsg"));
                    setLoggerIsConnected(true);
                    App.track = null;
                    repaintMaps();

                    App.logr = new GpsLogger(scanResult.getPath(),scanResult.getFile());

                    if (App.status.canProcessLogger() && App.logr.checkLogger()) {
                         runBackupAndProcessTask();
                    } else {
                         driveScan = null;
                         startDriveScanTask();
                    }

                } else {
                    // a drive has been removed
                    App.track = null;
                    App.logr = null;
                    setMainStatus("");
                    setBottomStatus(App.getResourceMap().getString("waitingForLoggerMsg"));
                    App.mapCaption = App.getResourceMap().getString("waitingForLoggerMsg");
                    setLoggerIsConnected(false);
                    repaintMaps();

                    driveScan = null;
                    startDriveScanTask();
                }
            } catch (InterruptedException ex) {
                //Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                //Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
// END drivescan asynchronous task (swingWorker)
    
   
// START main FRDL asynchronous task
    
    /*
     * BackupAndProcessTask is the asynchronous task which actually does the
     * main FRDL business
     * runBackupAndProcessTask() is the method to use to set it off
     * programmatically
    */
    public void runBackupAndProcessTask () {
        stopRepaint(true);
        stopDriveScanTask();
        App.track = null;
        //this stuff sets off the task
        Task mT = null;
        mT = new BackupAndProcessTask(getApplication());
        ApplicationContext appC = Application.getInstance().getContext();
        //TaskMonitor tM = appC.getTaskMonitor();
        TaskService tS = appC.getTaskService();
        tS.execute(mT);
        //tM.setForegroundTask(mT);


    }

    /*
     * this is the asynchronous task which actually does the
     * main FRDL business.  It uses the progress bar and status line
     *
     */
    private class BackupAndProcessTask extends org.jdesktop.application.Task<Object, Void> {
        private double totalBytes = 0;
        private double doneBytes = 0;
        private String activetask;
        private LocalDateTime winOpen;
        private LocalDateTime winClose;
        //private GpsLogger logger;

        
        BackupAndProcessTask(org.jdesktop.application.Application app) {
            // Constructor:  Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to BackupTask fields, here.
            super(app);
            this.totalBytes = App.logr.totalLoggerBytes;
            this.activetask = App.thisChampionship.getItemAsString("championship.activeTask");
            this.winOpen = App.thisChampionship.getItemAsLocalDT("task." + activetask + ".windowOpen");
            this.winClose = App.thisChampionship.getItemAsLocalDT("task." + activetask + ".windowClose");
            //this.logger = App.logr;
        }

        @Override protected Object doInBackground() {
                // Your Task's code here.  This method runs
                // on a background thread, so don't reference
                // the Swing GUI from here.
        // You can affect the progress bar and message area like this:
        //setMessage("This will show up on the status bar");
        // setProgress(soFar, lowvalue, highvalue);
            
            String logFileExtension = App.logr.prepareToBackupFiles();

            if (logFileExtension == null) return null;

            setMessage(App.getResourceMap().getString("backingUpMsg"));
            int ctr = 0;
            //System.out.println("total: " + totalBytes);
            for (int i = 0;i < App.logr.allLogFiles.size();i++ ) {
                File srcFile = (File) App.logr.allLogFiles.get(i);
                //System.out.println("file: " + srcFile.getAbsolutePath());
                doneBytes = doneBytes + srcFile.length();

                if (Utilities.getFileExtension(srcFile.getName().trim().toLowerCase()).equals(logFileExtension.trim().toLowerCase())) {
                    File destFile = new File(App.logr.loggerBackupDir + File.separatorChar + srcFile.getName());
                    //System.out.println("backup: " + srcFile.getAbsolutePath() + " to: " + destFile.getAbsolutePath());
                    if (!Utilities.fileExists(destFile.getAbsolutePath())) {
                        Utilities.copy(srcFile, destFile);
                        ctr = ctr + 1;
                    }
                    
                }
                double done1 = ((doneBytes / totalBytes)*100);
                int done = (int) done1;
                //System.out.println("Progress: " + done);
                setProgress(done);
                //System.out.println("name: " + Utilities.getFileNameWithoutExtension(srcFile.getName()) + " ext: " + Utilities.getFileExtension(srcFile.getName()));
            }
            setMessage(ctr + " " + App.getResourceMap().getString("loggerFilesBackedUp"));
            MainView.addLog(ctr + " " + App.getResourceMap().getString("loggerFilesBackedUp"));
            
            String igcFileName = App.logr.prepareToProcessFiles(logFileExtension);
            File fx = new File(igcFileName);

            if (igcFileName == null) return null;

            //System.out.println("win open: " + winOpen.toString());
            //System.out.println("win close: " + winClose.toString());

            ParseNMEA p = new ParseNMEA();
            p.processNMEAfiles(App.logr, igcFileName , winOpen, winClose);

            return fx.getName();  // return your result
        }
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            if (result != null) {
                //MainView.addLog("Backed up " + ctr + " files");
                if (!Utilities.directoryExists(App.thisChampionship.getItemAsString("master.pathToFlightAnalysis"))) {
                    //show warning that igc filse dir is not available
                    //and will save to championship files dir instead
                    addLog("ERROR: Cannot find the igc files folder.");
                    Dialogs d = new Dialogs();
                    d.showErrorDialog(App.getResourceMap().getString("cannotFindIgcFilesDirErr.line1") + "\n"  +
                            App.getResourceMap().getString("cannotFindIgcFilesDirErr.line2") + "\n\n" +
                            App.getResourceMap().getString("cannotFindIgcFilesDirErr.line3") + "\n" +
                            App.getResourceMap().getString("cannotFindIgcFilesDirErr.line4")  + "\n\n" +
                            App.getResourceMap().getString("cannotFindIgcFilesDirErr.line5") + "\n" +
                            App.getResourceMap().getString("cannotFindIgcFilesDirErr.line6"));
                }
                setMainStatus(App.getResourceMap().getString("savedMsg") + " " + result.toString());
                setBottomStatus(App.getResourceMap().getString("mayDisconnect"));
                repaintMaps();
            }
        }
       
        @Override protected void finished()  {
        // when the task is finished, enter your code here. this method is even called, when
        // a task was cancelled
        //super.finished();
        stopRepaint(false);
        driveScan = null;


        startDriveScanTask();

        }
    }

// END main FRDL asynchronous task


    @Action(enabledProperty = "champFileIsOpen")
    public void saveChampionshipAs() {
        Utilities.saveChampionshipAs();
    }


    
    public boolean isChampFileIsOpen() {
        //return champFileIsOpen;
        return App.champFileIsOpen;
    }

    public void setChampFileIsOpen(boolean b) {
        boolean old = isChampFileIsOpen();
        App.champFileIsOpen = b;
        firePropertyChange("champFileIsOpen", old, isChampFileIsOpen());
        if (b) {
            startDriveScanTask();
        } else {
            stopDriveScanTask();
        }
        setQuickAccessMenuItem();
    }

    public boolean isLoggerIsConnected() {
        return loggerIsConnected;
    }

    public void setLoggerIsConnected(boolean b) {
        boolean old = isLoggerIsConnected();
        this.loggerIsConnected = b;
        firePropertyChange("loggerIsConnected", old, isLoggerIsConnected());
    }

    @Action(enabledProperty = "loggerIsConnected")
    public void forceDownload() {
        runBackupAndProcessTask();
    }

    @Action
    public void openHelp() {
        Help.HelpView.startHelp();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu LanguageMenu;
    private javax.swing.JMenuItem TaskSettingsMenuItem;
    private javax.swing.JPanel altPanel;
    private javax.swing.JMenuItem championshipSettingsMenuItem;
    private javax.swing.JMenuItem forceDownloadMenuItem;
    private javax.swing.JMenuItem helpMenuItem;
    private javax.swing.JMenu infoMenu;
    private javax.swing.JSplitPane innerSplitPane;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JMenuItem languageMenuItem_CZ;
    private javax.swing.JMenuItem languageMenuItem_EN;
    private javax.swing.JMenuItem languageMenuItem_ES;
    private javax.swing.JMenuItem languageMenuItem_FR;
    private static javax.swing.JList logList;
    private javax.swing.JScrollPane logPane;
    private javax.swing.JMenuItem loggerInfoMenuItem;
    private javax.swing.JMenuItem loggerMenuItem;
    private javax.swing.JPanel mainPanel;
    private static javax.swing.JLabel mainStatusLabel;
    private javax.swing.JPanel mapPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newChampMenuItem;
    private javax.swing.JMenuItem openChampMenuItem;
    private javax.swing.JSplitPane outerSplitPane;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JMenuItem quickOpenMenuItem;
    private javax.swing.JSeparator quickOpenSeparator;
    private javax.swing.JMenuItem saveChampAsMenuItem;
    private javax.swing.JMenu settingsMenu;
    private javax.swing.JLabel statusAnimationLabel;
    private static javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
