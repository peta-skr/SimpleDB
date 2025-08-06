package SimpleDB;

import java.util.Iterator;

public class LogMgr {
    private FileMgr fm;
    private String logfile;
    private Page logpage;
    private BlockId currentblk;
    private int latestLSN = 0;
    private int lastSavedLSN = 0;

    public LogMgr(FileMgr fm, String logfile) {
        this.fm = fm;
        this.logfile = logfile;
        byte[] b = new byte[fm.blockSize()];
        logpage = new Page(b);
        int logSize = fm.length(logfile);
        if (logSize == 0)
            currentblk = appendNewBlock();
        else {
            currentblk = new BlockId(logfile, logSize - 1);
            fm.read(currentblk, logpage);
        }
    }

    public void flush(int lsn) {
        if (lsn >= lastSavedLSN)
            flush();
    }

    public Iterator<byte[]> iterator() {
        flush();
        return new LogIterator(fm, currentblk);
    }

    public synchronized int append(byte[] logrec) {
        int boundary = logpage.getInt(0);
        int recsize = logrec.length;
        int bytesneeded = recsize + Integer.BYTES;
        if (boundary - bytesneeded < Integer.BYTES) { // It doesn't fit
            flush();    // so move to the next block
            currentblk = appendNewBlock();
            boundary = logpage.getInt(0);
        }
        int recpos = boundary - bytesneeded;
        logpage.setBytes(recpos, logrec);
        logpage.setInt(0, fm.blockSize()); // the new boundary
        lastSavedLSN += 1;
        return lastSavedLSN;
    }

    private BlockId appendNewBlock() {
        BlockId blk = fm.append(logfile);
        logpage.setInt(0, fm.blockSize());
        fm.write(blk, logpage);
        return blk;
    }

    private void flush() {
        fm.write(currentblk, logpage);
        lastSavedLSN = latestLSN;
    }
}
