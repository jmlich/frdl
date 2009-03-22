package FRDL;

import java.io.*;
import java.util.*;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/*
 * modified from http://mattfleming.com/node/11
 *
 * The main purpose of this is to quickly get the first and last
 * date/times from the RMC line in a NMEA file
 *
 * see getStartAndFinishTimes (String fileName)
 *
 *
*/

public class CheckNMEAfile  {
    private static final DateTimeFormatter nmeaDateFormat = DateTimeFormat.forPattern("ddMMyy");
    private static final DateTimeFormatter nmeaTimeFormat = DateTimeFormat.forPattern("HHmmss");

    static public class BackwardsFileInputStream extends InputStream
    {
        public BackwardsFileInputStream(File file) throws IOException
        {
            assert (file != null) && file.exists() && file.isFile() && file.canRead();

            raf = new RandomAccessFile(file, "r");
            currentPositionInFile = raf.length();
            currentPositionInBuffer = 0;
        }

        public int read() throws IOException
        {
            if (currentPositionInFile <= 0)
                return -1;
            if (--currentPositionInBuffer < 0)
            {
                currentPositionInBuffer = buffer.length;
                long startOfBlock = currentPositionInFile - buffer.length;
                if (startOfBlock < 0)
                {
                    currentPositionInBuffer = buffer.length + (int)startOfBlock;
                    startOfBlock = 0;
                }
                raf.seek(startOfBlock);
                raf.readFully(buffer, 0, currentPositionInBuffer);
                return read();
            }
            currentPositionInFile--;
            return buffer[currentPositionInBuffer];
        }

        public void close() throws IOException
        {
            raf.close();
        }

        private final byte[] buffer = new byte[4096];
        private final RandomAccessFile raf;
        private long currentPositionInFile;
        private int currentPositionInBuffer;
    }

    public static List<String> head(File file, int numberOfLinesToRead) throws IOException
    {
        return head(file, "ISO-8859-1" , numberOfLinesToRead);
    }

    public static List<String> head(File file, String encoding, int numberOfLinesToRead) throws IOException
    {
        assert (file != null) && file.exists() && file.isFile() && file.canRead();
        assert numberOfLinesToRead > 0;
        assert encoding != null;

        LinkedList<String> lines = new LinkedList<String>();
        BufferedReader reader= new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
        for (String line = null; (numberOfLinesToRead-- > 0) && (line = reader.readLine()) != null;)
        {
            lines.addLast(line);
        }
        reader.close();
        return lines;
    }

    /*
     * this is customized specially to look at a NMEA file and return
     * the first LocalDateTime found in the file (from the RMC line)
     *
     */
    public static LocalDateTime head(File file) throws IOException    {
        assert (file != null) && file.exists() && file.isFile() && file.canRead();

        LocalDateTime ldt = null;
        BufferedReader reader= new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));
        String line = null;
        while (ldt == null && (line = reader.readLine()) != null) {
            ldt = parseRMC(reader.readLine());
        }
        reader.close();
        return ldt;
    }

    /*
     * this parses out the date and time from a NMEA RMC line
     *
    */
    private static LocalDateTime parseRMC (String str) {
        String sb[] = str.split(",");
        if (sb[0].compareTo("$GPRMC") == 0 && sb.length >= 9) {
            LocalDate Ldate = new LocalDate(nmeaDateFormat.parseDateTime(sb[9]));
            LocalTime Ltime = new LocalTime(nmeaTimeFormat.parseDateTime(sb[1].substring(0,6)));
            return new LocalDateTime(Ldate.toDateTime(Ltime));
        } else return null;
    }


    public static List<String> tail(File file, int numberOfLinesToRead) throws IOException
    {
        return tail(file, "ISO-8859-1" , numberOfLinesToRead);
    }

    public static List<String> tail(File file, String encoding, int numberOfLinesToRead) throws IOException
    {
        assert (file != null) && file.exists() && file.isFile() && file.canRead();
        assert numberOfLinesToRead > 0;
        assert (encoding != null) && encoding.matches("(?i)(iso-8859|ascii|us-ascii).*");

        LinkedList<String> lines = new LinkedList<String>();
        BufferedReader reader= new BufferedReader(new InputStreamReader(new BackwardsFileInputStream(file), encoding));
        for (String line = null; (numberOfLinesToRead-- > 0) && (line = reader.readLine()) != null;)
        {
            // Reverse the order of the characters in the string
            char[] chars = line.toCharArray();
            for (int j = 0, k = chars.length - 1; j < k ; j++, k--)
            {
                char temp = chars[j];
                chars[j] = chars[k];
                chars[k]= temp;
            }
            //was addFirst AddLast outputs lines in correct reverse order.
            lines.addLast(new String(chars));
        }
        reader.close();
        return lines;
    }

    /*
     * this is customized specially to look at a NMEA file and return
     * the last LocalDateTime found in the file (from the RMC line)
     *
     */
    public static LocalDateTime tail(File file) throws IOException {
        assert (file != null) && file.exists() && file.isFile() && file.canRead();
        //assert numberOfLinesToRead > 0;
        //assert (encoding != null) && encoding.matches("(?i)(iso-8859|ascii|us-ascii).*");

        //LinkedList<String> lines = new LinkedList<String>();
        LocalDateTime ldt = null;
        BufferedReader reader= new BufferedReader(new InputStreamReader(new BackwardsFileInputStream(file), "ISO-8859-1"));
        String line = null;
        while (ldt == null && (line = reader.readLine()) != null) {
            //line = reader.readLine();
            // Reverse the order of the characters in the string
            char[] chars = line.toCharArray();
            for (int j = 0, k = chars.length - 1; j < k ; j++, k--)
            {
                char temp = chars[j];
                chars[j] = chars[k];
                chars[k]= temp;
            }
            //System.err.println("thisLine: " + new String(chars));
            ldt = parseRMC(new String(chars));
        }
        reader.close();
        return ldt;
    }

    public static void test (String fileName)    {
        try
        {
            File file = new File(fileName);
            int n = 20;

            {
                System.out.println("Head of " + file);
                int index = 0;
                for (String line : head(file, n))
                {
                    System.out.println(++index + "\t[" + line + "]");
                }
            }
            {
                System.out.println("Tail of " + file);
                int index = 0;
                for (String line : tail(file, n))
                {
                    System.out.println(++index + "\t[" + line + "]");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

        public static void test2 (String fileName)    {
        try
        {
            File file = new File(fileName);
            int n = 20;

            {
                System.out.println("Head of " + file);
                LocalDateTime dt = head(file);
                System.out.println("firstDT: " + dt.toString());
            }
            {
                System.out.println("Tail of " + file);
                LocalDateTime dt = tail(file);
                System.out.println("lastDT: " + dt.toString());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*
     * returns a 2 part array of LocalDateTimes which are the
     * first date/time from a NMEA file (from the RMC line) and
     * the last date/time from a NMEA file (from the RMC line)
     *
     * if one or other was not found, they return null.
     *
    */
    public static LocalDateTime[] getStartAndFinishTimes (String fileName)    {
        //Object[] obj = null;
        LocalDateTime[] ldt = new LocalDateTime[2];
        ldt[0] = null;
        ldt[1] = null;

        try
        {
            File file = new File(fileName);
            ldt[0] = head(file);
            ldt[1] = tail(file);
        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }
        return ldt;
    }
}

