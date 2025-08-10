package SimpleDB;

import java.util.Iterator;

public class LogIterator implements Iterator<byte[]> {
    private FileMgr fm;
    private BlockId blk;
    private Page p;
    private int currentpos;
    private int boundary;

    public LogIterator(FileMgr fm, BlockId blk) {
        this.fm = fm;
        this.blk = blk;
        byte[] b = new byte[fm.blockSize()];
        p = new Page(b);
        moveToBlock(blk);
    }

    public boolean hasNext() {
        return currentpos < fm.blockSize() || blk.number() > 0;
    }

    public byte[] next() {
        if (currentpos == fm.blockSize()) {
            blk = new BlockId(blk.filename(), blk.number() - 1);
            moveToBlock(blk);
        }
        byte[] rec = p.getBytes(currentpos);
        currentpos += Integer.BYTES + rec.length;
        return rec;
    }

    private void moveToBlock(BlockId blk) {
        fm.read(blk, p);
        boundary = p.getInt(0);
        currentpos = boundary;
    }
}
