package SimpleDB;

import java.nio.Buffer;

public class BufferTest {
    public static void main(String[] args) {
        SimpleDB db = new SimpleDB("buffertest", 400, 3);
        BufferMgr bm = db.bufferMgr();

        Buffer buff1 = bm.pin(new BlockId("testfile", 1));
        Page p = buffer1.contents();
        int n = p.getInt(80);
        p.setInt(80, n + 1);
        buffer1.setModified(1, 0);
        System.out.println("the new value is " + (n + 1));
        bm.unpin(buff1);

        Buffer buff2 = bm.pin(new BlockId("testfile", 2));
        Buffer buff3 = bm.pin(new BlockId("testfile", 3));
        Buffer buff4 = bm.pin(new BlockId("testfile", 4));

        bm.unpin(buff2);
        buff2 = bm.pin(new BlockId("testfile", 1));
        Page p2 = buff2.contents();
        p2.setInt(80, 9999);
        buff2.setModified(1, 0);
        bm.unpin(buff2);
    }
}
