package SimpleDB;

import java.util.Iterator;

public class LogTest {
    private static LogMgr lm;

    public static void main(String[] args) {
        SimpleDB db = new SimpleDB("logtest", 400, 8);
        lm = db.logMgr();
        createRecords(1, 35);
        printLogRecords("The log file now has these records:");
        createRecords(36, 70);
        lm.flush(65);
        printLogRecords("The log file now has these records:");
    }

    private static void printLogRecords(String msg) {
        System.out.println(msg);
        Iterator<byte[]> iter = lm.iterator();
        while(iter.hasNext()) {
            byte[] rec = iter.next();
            Page p = new Page(rec);
            String s = p.getString(0);
            int npos = Page.maxLength(s.length());
            int val = p.getInt(npos);
            System.out.println("[" + s + ", " + val + "]");
        }
        System.out.println();
    }

    private static void createRecords(int start, int end) {
        System.out.println("Creating records: ");
        for (int i = start; i <= end; i++) {
            byte[] rec = createLogRecord("records" + i, i + 100);
             int lsn = lm.append(rec);
            System.out.println(lsn + " ");
        }
        System.out.println();
    }

    private static byte[] createLogRecord(String s, int n) {
        int npos = Page.maxLength(s.length());
        byte[] b = new byte[npos + Integer.BYTES];
        Page p = new Page(b);
        p.setString(0, s);
        p.setInt(npos, n);
        return b;
    }
}
