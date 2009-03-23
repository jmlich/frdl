package Help;

/**
 * This application requires additional html files:
 * Adapted from
 * http://java.sun.com/docs/books/tutorial/uiswing/examples/components/index.html#TreeDemo
 */
import FRDL.App;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import java.net.URL;
import java.io.IOException;
import java.awt.Dimension;
import java.awt.GridLayout;

public class HelpView extends JPanel implements TreeSelectionListener {
    private JEditorPane htmlPane;
    private JTree tree;
    private URL helpURL;
    private static boolean DEBUG = false;

    //Optionally play with line styles.  Possible values are
    //"Angled" (the default), "Horizontal", and "None".
    private static boolean playWithLineStyle = false;
    private static String lineStyle = "Horizontal";
    
    //Optionally set the look and feel.
    private static boolean useSystemLookAndFeel = false;

    //constructor
    public HelpView() {
        super(new GridLayout(1,0));
        init();
    }

    /*
     * 
     * starts help off
     * originally public static void main(String[] args) {
     */
    public static void startHelp() {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            createAndShowGUI();
            }
        });
    }

    /*
     * builds the tree of help items
     *
    */
    private void createNodes(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode category = null;
        DefaultMutableTreeNode book = null;

        //home item in root
        //book = new DefaultMutableTreeNode(new BookInfo("Home","1_01_firstPage.html"));
        //top.add(book);


        //MAIN FOLDER
        category = new DefaultMutableTreeNode("Program help");
        top.add(category);

        //folder items
        book = new DefaultMutableTreeNode(new BookInfo("FRDL overview","2_01_Overview.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("Installation","2_02_Installation.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("Starting FRDL","2_03_Startup.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("New Championship","2_04_New_championship.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("Open Championship","2_05_Open_championship.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("Championship settings","2_06_Championship_settings.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("Task settings","2_07_Task_settings.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("Connecting a Logger","2_08_Connecting_a_logger.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("Logger settings","2_09_Logger_settings.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("Protecting against malware","2_10_Protecting_against_malware.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("Modes of operation","2_11_Modes_of_operation.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("Disconnecting a logger","2_12_Disconnecting_a_logger.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("Language","2_13_Language.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("Errors","2_14_Errors.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("Release notes","2_15_Release_notes.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("History","2_98_History.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("Licence","2_99_licence.html"));
        category.add(book);


        //MAIN FOLDER
        category = new DefaultMutableTreeNode("Loggers");
        top.add(category);

        //folder items
        book = new DefaultMutableTreeNode(new BookInfo("AMOD 3080","3_02_AMOD_3080.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("Geochron","3_03_GeoChron.html"));
        category.add(book);

        book = new DefaultMutableTreeNode(new BookInfo("The CIMA Specification","3_99_CIMA_spec.html"));
        category.add(book);

    }

    /*
     * this shows the first page when help is started
     *
    */
    private void showFirstPage() {
        String s = "2_01_overview.html";
        helpURL = getClass().getResource(s);
        if (helpURL == null) {
            System.err.println("Couldn't open help file: " + s);
        } else if (DEBUG) {
            System.out.println("Help URL is " + helpURL);
        }

        displayURL(helpURL);
    }

    /*
     * sets up all the components in the frame
     * 
    */
    public void init() {
        //Create the nodes.
        DefaultMutableTreeNode top =
            new DefaultMutableTreeNode(App.getResourceMap().getString("Application.shortTitle") + " Help");
        createNodes(top);

        //Create a tree that allows one selection at a time.
        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //this sets the Program help folder initially
        //expanded - need to change if items are added or removed
        //from above it!
        tree.expandRow(1);

        //Listen for when the selection changes.
        tree.addTreeSelectionListener(this);

        if (playWithLineStyle) {
            System.out.println("line style = " + lineStyle);
            tree.putClientProperty("JTree.lineStyle", lineStyle);
        }

        //Create the scroll pane and add the tree to it.
        JScrollPane treeView = new JScrollPane(tree);

        //Create the HTML viewing pane.
        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
        showFirstPage();
        JScrollPane htmlView = new JScrollPane(htmlPane);

        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(htmlView);

        Dimension minimumSize = new Dimension(100, 50);
        htmlView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(200);
        splitPane.setPreferredSize(new Dimension(600, 400));

        //Add the split pane to this panel.
        add(splitPane);
    }


    /** 
     * 
     * Required by TreeSelectionListener interface.
     */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                           tree.getLastSelectedPathComponent();

        if (node == null) return;

        Object nodeInfo = node.getUserObject();
        if (node.isLeaf()) {
            BookInfo book = (BookInfo)nodeInfo;
            displayURL(book.bookURL);
            if (DEBUG) {
                System.out.print(book.bookURL + ":  \n    ");
            }
        } else {
            displayURL(helpURL); 
        }
        if (DEBUG) {
            System.out.println(nodeInfo.toString());
        }
    }


    private class BookInfo {
        public String bookName;
        public URL bookURL;

        public BookInfo(String book, String filename) {
            bookName = book;
            bookURL = getClass().getResource(filename);
            if (bookURL == null) {
                System.err.println("Couldn't find file: " + filename);
            }
        }

        @Override
        public String toString() {
            return bookName;
        }
    }



    /*
     * actually displays the help page
     *
    */
    private void displayURL(URL url) {
        try {
            if (url != null) {
                htmlPane.setPage(url);
            } else { //null url
		htmlPane.setText("File Not Found");
                if (DEBUG) {
                    System.out.println("Attempted to display a null URL.");
                }
            }
        } catch (IOException e) {
            System.err.println("Attempted to read a bad URL: " + url);
        }
    }

      
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        if (useSystemLookAndFeel) {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Couldn't use system look and feel.");
            }
        }

        //Create and set up the window.
        JFrame frame = new JFrame(App.getResourceMap().getString("Application.shortTitle") + " Help");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new HelpView());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }


}
