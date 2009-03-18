/*
 * FRDLchampionshipSettings.java
 */

package FRDL;

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.jdesktop.application.Action;
import org.joda.time.DateTime;
import org.joda.time.Instant;
/**
 * The Settings - Tasks popup dialog box.
 */
public class TaskSettings extends javax.swing.JDialog {

    DefaultComboBoxModel taskDescriptionModel = null;
    //private int thisSelectedTask;
    //private String thisSelectedTaskDescription;

    public TaskSettings(java.awt.Frame parent) {
        super(parent);
        initComponents();


       addWindowListener(new WindowAdapter(){
           //capture the window closing event i.e clicking 'X'
           public void windowClosing(WindowEvent evt){
              ///do any actions that u wish to
               MainView.addLog("Task settings dialog closed");
              dispose();//dispose the JDialog
           }
        });

        getRootPane().setDefaultButton(activateButton);
        loadTask(App.thisChampionship.getItemAsInt("championship.activeTask",1));

        setTzOffsetDisplay(Integer.parseInt(App.thisChampionship.champData.readValue("championship.utcOffsetH")),
                Integer.parseInt(App.thisChampionship.champData.readValue("championship.utcOffsetM")));

        MainView.addLog("Task settings dialog opened");
    }

    @Action public void closetaskSettings() {
        if (saveTask(TaskNoComboBox.getSelectedIndex()+1)) {
            //Utilities.saveChampionshipToFile(FRDLApp.thisChampionship);
            MainView.addLog("Task settings dialog saved & closed");
            dispose();
        }
    }

    public void loadTask(int taskId) {
        if (taskId < 0 || taskId >= 30) taskId = 1;
        TaskNoComboBox.setSelectedIndex(taskId - 1);

        DateTime wo = App.thisChampionship.getItemAsDT("task." + Integer.toString(taskId) + ".windowOpen", "championship.windowOpen");
        windowOpenDatePicker.setDate(wo.toDate());
        windowOpenDatePicker.setFormats(App.standardDateFormat);
        //these combo box items are at 10 minute intervals hence the 0.01
        windowOpenTimeComboBox.setSelectedIndex((int) (wo.getMinuteOfDay() * 0.1));

        DateTime wc = App.thisChampionship.getItemAsDT("task." + Integer.toString(taskId) + ".windowClose", "championship.windowClose");
        windowCloseDatePicker.setDate(wc.toDate());
        windowCloseDatePicker.setFormats(App.standardDateFormat);
        windowCloseTimeComboBox.setSelectedIndex((int) (wc.getMinuteOfDay() * 0.1));

        setFieldsState(App.clientFullMode);

        MainView.addLog("Task id " + taskId + " loaded");
    }

    /*
     * Saves a task 
     *
     */
    public Boolean saveTask(int taskId) {
        if (App.clientFullMode) App.thisChampionship.makeBackup();

        App.thisChampionship.champData.writeProperty("championship.activeTask",Integer.toString(taskId));
        MainView.setMainStatus("");
        
        Boolean ret = true;
        //only full mode
        if (App.clientFullMode) {
            DateTime wo = makeDateTime(new DateTime(windowOpenDatePicker.getDate()),windowOpenTimeComboBox.getSelectedIndex());
            DateTime wc = makeDateTime(new DateTime(windowCloseDatePicker.getDate()),windowCloseTimeComboBox.getSelectedIndex());
            
            //check window open is before window close            
            if (wc.isAfter(wo.toInstant())) {
                App.thisChampionship.champData.writeProperty("task." + Integer.toString(taskId) + ".windowOpen", wo.toString());
                App.thisChampionship.champData.writeProperty("task." + Integer.toString(taskId) + ".windowClose", wc.toString());
            } else {
                ret = false;
                Toolkit.getDefaultToolkit().beep();
                statusLabel.setText(App.getResourceMap().getString("openDateAfterClose"));
            }
        }
        return ret;
    }

    /*
     * makes a date time out of a DateTime (from calendar selector)
     * plus the minutes from the selected position of a comboBox / 10
     * (in other words the comboBox goes from 00:00 to 12:50 in 10
     * minute increments)
    */
    private DateTime makeDateTime(DateTime dt, int minPos) {
        dt= dt.plusMinutes(minPos * 10);
        //System.out.println(whichWindow + " total time is: " + dt);
        return dt;
    }

    /*
     * Sets the fields on the page active or not
     * depending on client state (full or download)
    */
    private Boolean setFieldsState (Boolean cs) {

        //taskDescriptionTextField.setEditable(cs);
        //newTaskButton.setVisible(cs);
        //taskDescriptionComboBox.setEditable(cs);
        windowOpenDatePicker.setEnabled(cs);
        windowOpenTimeComboBox.setEnabled(cs);
        windowCloseDatePicker.setEnabled(cs);
        windowCloseTimeComboBox.setEnabled(cs);
        if (cs) {
            statusLabel.setText(App.getResourceMap().getString("fullMode"));

        } else {
            statusLabel.setText(App.getResourceMap().getString("downloadMode"));
        }
        return cs;
    }

        /*
     * sets the xx:yy of "Task window {xx:yy)"
     * to the current offset
     *
    */
    private void setTzOffsetDisplay(int h, int m) {
        taskWindowPanel.setBorder(BorderFactory.createTitledBorder(
        App.getResourceMap().getString("taskWindowPanelText") +
        " ("  +
        Utilities.makeTimeZoneOffset (h, m,true) +
        ")"
        ));
    }



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        taskPanel = new javax.swing.JPanel();
        TaskNoComboBox = new javax.swing.JComboBox();
        activateButton = new javax.swing.JButton();
        taskWindowPanel = new javax.swing.JPanel();
        windowOpenDatePicker = new org.jdesktop.swingx.JXDatePicker();
        DefaultComboBoxModel mo1 = new DefaultComboBoxModel();
        String[] nos1 = {":00",":10",":20",":30",":40",":50"};
        for (int i = 0;i<24;i++) {
            for (String item : nos1){
                mo1.addElement(Integer.toString(i)+item);
            }
        }
        windowOpenTimeComboBox = new javax.swing.JComboBox(mo1);
        fromLabel = new javax.swing.JLabel();
        windowCloseDatePicker = new org.jdesktop.swingx.JXDatePicker();
        DefaultComboBoxModel mo2 = new DefaultComboBoxModel();
        String[] nos = {":00",":10",":20",":30",":40",":50"};
        for (int i = 0;i<24;i++) {
            for (String item : nos){
                //li.add(Integer.toString(i)+item);
                mo2.addElement(Integer.toString(i)+item);
            }
        }
        windowCloseTimeComboBox = new javax.swing.JComboBox(mo2);
        toLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(FRDL.App.class).getContext().getResourceMap(TaskSettings.class);
        setTitle(resourceMap.getString("title")); // NOI18N
        setModal(true);
        setName("taskSettings"); // NOI18N
        setResizable(false);

        taskPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("taskPanel.border.title"))); // NOI18N
        taskPanel.setName("taskPanel"); // NOI18N

        TaskNoComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29" }));
        TaskNoComboBox.setName("TaskNoComboBox"); // NOI18N
        TaskNoComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TaskNoComboBoxActionPerformed(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(FRDL.App.class).getContext().getActionMap(TaskSettings.class, this);
        activateButton.setAction(actionMap.get("closetaskSettings")); // NOI18N
        activateButton.setFont(resourceMap.getFont("activateButton.font")); // NOI18N
        activateButton.setText(resourceMap.getString("activateButton.text")); // NOI18N
        activateButton.setName("activateButton"); // NOI18N

        org.jdesktop.layout.GroupLayout taskPanelLayout = new org.jdesktop.layout.GroupLayout(taskPanel);
        taskPanel.setLayout(taskPanelLayout);
        taskPanelLayout.setHorizontalGroup(
            taskPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, taskPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(TaskNoComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 134, Short.MAX_VALUE)
                .add(activateButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        taskPanelLayout.setVerticalGroup(
            taskPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(taskPanelLayout.createSequentialGroup()
                .add(taskPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(TaskNoComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(activateButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        taskWindowPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Task window (local time)"));
        taskWindowPanel.setToolTipText(resourceMap.getString("taskWindowPanel.toolTipText")); // NOI18N
        taskWindowPanel.setName("taskWindowPanel"); // NOI18N

        windowOpenDatePicker.setName("windowOpenDatePicker"); // NOI18N
        windowOpenDatePicker.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                windowOpenDatePickerActionPerformed(evt);
            }
        });

        windowOpenTimeComboBox.setName("windowOpenTimeComboBox"); // NOI18N
        windowOpenTimeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                windowOpenTimeComboBoxActionPerformed(evt);
            }
        });

        fromLabel.setText(resourceMap.getString("fromLabel.text")); // NOI18N
        fromLabel.setName("fromLabel"); // NOI18N

        windowCloseDatePicker.setFocusable(false);
        windowCloseDatePicker.setName("windowCloseDatePicker"); // NOI18N
        windowCloseDatePicker.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                windowCloseDatePickerActionPerformed(evt);
            }
        });

        windowCloseTimeComboBox.setName("windowCloseTimeComboBox"); // NOI18N
        windowCloseTimeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                windowCloseTimeComboBoxActionPerformed(evt);
            }
        });

        toLabel.setText(resourceMap.getString("toLabel.text")); // NOI18N
        toLabel.setName("toLabel"); // NOI18N

        org.jdesktop.layout.GroupLayout taskWindowPanelLayout = new org.jdesktop.layout.GroupLayout(taskWindowPanel);
        taskWindowPanel.setLayout(taskWindowPanelLayout);
        taskWindowPanelLayout.setHorizontalGroup(
            taskWindowPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(taskWindowPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(taskWindowPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(fromLabel)
                    .add(toLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(taskWindowPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, windowCloseDatePicker, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, windowOpenDatePicker, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(taskWindowPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(windowOpenTimeComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(windowCloseTimeComboBox, 0, 72, Short.MAX_VALUE))
                .addContainerGap())
        );
        taskWindowPanelLayout.setVerticalGroup(
            taskWindowPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(taskWindowPanelLayout.createSequentialGroup()
                .add(taskWindowPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(windowOpenDatePicker, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fromLabel)
                    .add(windowOpenTimeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(taskWindowPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(windowCloseDatePicker, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(windowCloseTimeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(toLabel))
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
                    .add(taskWindowPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(taskPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(statusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(24, 24, 24)
                .add(taskPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(taskWindowPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 116, Short.MAX_VALUE)
                .add(statusLabel)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void windowOpenDatePickerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_windowOpenDatePickerActionPerformed
        DateTime dt = makeDateTime(new DateTime(windowOpenDatePicker.getDate()),windowOpenTimeComboBox.getSelectedIndex());
        if (dt.isBefore(new Instant(App.thisChampionship.getItemAsDT("championship.windowOpen"))) ||
               dt.isAfter(new Instant(App.thisChampionship.getItemAsDT("championship.windowClose")))  ) {
            statusLabel.setText(App.getResourceMap().getString("taskWinNotInChampWinMsg"));
            Toolkit.getDefaultToolkit().beep();
        } else {
            statusLabel.setText("");

        }
        //saveTask(TaskNoComboBox.getSelectedIndex()+1);
}//GEN-LAST:event_windowOpenDatePickerActionPerformed

    private void windowOpenTimeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_windowOpenTimeComboBoxActionPerformed
        DateTime dt = makeDateTime(new DateTime(windowOpenDatePicker.getDate()),windowOpenTimeComboBox.getSelectedIndex());
        if (dt.isBefore(new Instant(App.thisChampionship.getItemAsDT("championship.windowOpen"))) ||
               dt.isAfter(new Instant(App.thisChampionship.getItemAsDT("championship.windowClose")))  ) {
            statusLabel.setText(App.getResourceMap().getString("taskWinNotInChampWinMsg"));
            Toolkit.getDefaultToolkit().beep();
        } else {
            statusLabel.setText("");
        }
        //saveTask(TaskNoComboBox.getSelectedIndex()+1);
    }//GEN-LAST:event_windowOpenTimeComboBoxActionPerformed

    private void windowCloseDatePickerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_windowCloseDatePickerActionPerformed
        DateTime dt = makeDateTime(new DateTime(windowCloseDatePicker.getDate()),windowCloseTimeComboBox.getSelectedIndex());
        if (dt.isBefore(new Instant(App.thisChampionship.getItemAsDT("championship.windowOpen"))) ||
               dt.isAfter(new Instant(App.thisChampionship.getItemAsDT("championship.windowClose")))  ) {
            statusLabel.setText(App.getResourceMap().getString("taskWinNotInChampWinMsg"));
            Toolkit.getDefaultToolkit().beep();
        } else {
            statusLabel.setText("");
        }
        //saveTask(TaskNoComboBox.getSelectedIndex()+1);
}//GEN-LAST:event_windowCloseDatePickerActionPerformed

    private void windowCloseTimeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_windowCloseTimeComboBoxActionPerformed
        DateTime dt = makeDateTime(new DateTime(windowCloseDatePicker.getDate()),windowCloseTimeComboBox.getSelectedIndex());
        if (dt.isBefore(new Instant(App.thisChampionship.getItemAsDT("championship.windowOpen"))) ||
               dt.isAfter(new Instant(App.thisChampionship.getItemAsDT("championship.windowClose")))  ) {
            statusLabel.setText(App.getResourceMap().getString("taskWinNotInChampWinMsg"));
            Toolkit.getDefaultToolkit().beep();
        } else {
            statusLabel.setText("");
        }
        //saveTask(TaskNoComboBox.getSelectedIndex()+1);
    }//GEN-LAST:event_windowCloseTimeComboBoxActionPerformed

    private void TaskNoComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TaskNoComboBoxActionPerformed
        JComboBox cb = (JComboBox)evt.getSource();
        //String newSelection = (String)cb.getSelectedItem();
        //thisSelectedTaskDescription = newSelection;

        //System.out.println("--------");
        //System.out.println("text in box = " + newSelection);
        //System.out.println("thisSelectedTask" + thisSelectedTask);
       
        //System.out.println("--------");

        //if (cb.getSelectedIndex() >= 0 && thisSelectedTask != cb.getSelectedIndex()) {
        if (cb.getSelectedIndex()+1 != App.thisChampionship.getItemAsInt("championship.activeTask", 1)) {
            loadTask(cb.getSelectedIndex()+1);
             //System.out.println("cb.getSelectedIndex()" + cb.getSelectedIndex());
        }
        
        //}
    }//GEN-LAST:event_TaskNoComboBoxActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox TaskNoComboBox;
    private javax.swing.JButton activateButton;
    private javax.swing.JLabel fromLabel;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel taskPanel;
    private javax.swing.JPanel taskWindowPanel;
    private javax.swing.JLabel toLabel;
    private org.jdesktop.swingx.JXDatePicker windowCloseDatePicker;
    private javax.swing.JComboBox windowCloseTimeComboBox;
    private org.jdesktop.swingx.JXDatePicker windowOpenDatePicker;
    private javax.swing.JComboBox windowOpenTimeComboBox;
    // End of variables declaration//GEN-END:variables
    
}
