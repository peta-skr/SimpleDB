package SimpleDB;

import java.util.HashMap;
import java.util.Map;

public class ConcurrecyMgr {
    private static LockTable locktbl = new LockTable();
    private Map<Block, String> locks = new HashMap<Block, String>();

    public void sLock(Block blk) {
        if (locks.get(blk) == null) {
            locktbl.sLock(blk);
            locks.put(blk, "S");
        }
    }

    public void xLock(Block blk) {
        if (!hasXLock(blk)) {
            sLock(blk);
            locktbl.xLock(blk);
            locks.put(blk, "X");
        }
    }

    public void release() {
        for (Block blk : locks.KeySet())
            locktbl.unlock(blk);
        locks.clear();
    }

    private boolean hasXLock(Block blk) {
        String locktype = locks.get(blk);
        return locktype != null && locktype.equals("X");
    }
}
