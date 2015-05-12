package FRDL;

import java.io.File;
import java.net.URI;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;



public class LinuxMounts {

    public static File[] listRootsAll() {
        File[] roots_orig = File.listRoots();

        if ((roots_orig.length == 1) && (roots_orig[0].toString() == "/")) {
            try {
            Process mountProcess = Runtime.getRuntime ().exec ( "mount" );
            BufferedReader mountOutput = new BufferedReader ( new InputStreamReader ( mountProcess.getInputStream () ) );
            ArrayList<File> roots = new ArrayList<File>();
            while ( true ) {

            // fetch the next line of output from the "mount" command
                String line = mountOutput.readLine ();

                if ( line == null ) {
                    break;
                }

            // the line will be formatted as "... on <filesystem> (...)"; get the substring we need
                int indexStart = line.indexOf ( " on /" )+4;
                int indexEnd = line.indexOf ( " ", indexStart);
                String m_path = line.substring ( indexStart, indexEnd);
//                System.out.println( "debug: " + m_path );
                if (m_path.length() > 0) {
                    roots.add ( new File ( m_path ) );
                }
            }
            mountOutput.close ();
            File[] roots_return = new File[ roots.size() ];
//            return roots_orig;
            roots.toArray(roots_return);
            return roots_return;
            } catch (java.io.IOException ex) {
                System.out.println( "Exception - mount not found!" );
        
               return roots_orig;
            }
        } else {

            return roots_orig;
        }
    }
}