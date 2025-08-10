package SimpleDB;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Page クラス
 * データベースの1ページ（ブロック）に対応するクラス。
 * 主にバイト配列に対して整数・文字列などの値を読み書きするためのインターフェースを提供する。
 */
public class Page {
    // バイト配列を操作するための ByteBuffer
    private ByteBuffer bb;

    // 文字列をバイトに変換する際に使う文字セット（ここでは ASCII）
    public static final Charset CHARASET = StandardCharsets.US_ASCII;

    public Page(int blocksize) {
        bb = ByteBuffer.allocateDirect(blocksize);
    }

    /**
     * コンストラクタ
     * バイト配列を ByteBuffer でラップする
     * @param b 対象のバイト配列
     */
    public Page(byte[] b) {
        bb = ByteBuffer.wrap(b);
    }

    /**
     * 指定したオフセットから4バイト読み取り、int値として返す
     * @param offset 読み取り開始位置
     * @return int値
     */
    public int getInt(int offset) {
        return bb.getInt(offset);
    }

    /**
     * 指定したオフセットにint値を書き込む（4バイト）
     * @param offset 書き込み位置
     * @param n 書き込むint値
     */
    public void setInt(int offset, int n) {
        bb.putInt(offset, n);
    }

    /**
     * 指定したオフセットからバイト配列を取得する
     * 最初に長さ（int）を読み取り、その分だけバイトを読み込む
     * @param offset 開始位置
     * @return 読み取ったバイト配列
     */
    public byte[] getBytes(int offset) {
        bb.position(offset);         // バッファ位置をセット
        int length = bb.getInt();   // 最初の4バイトは長さ情報
        byte[] b = new byte[length];
        bb.get(b);                  // 指定長さ分を読み取る
        return b;
    }

    /**
     * 指定したオフセットにバイト配列を書き込む
     * 最初に配列の長さ（int）を書き込み、続いてバイト配列本体を格納する
     * @param offset 書き込み位置
     * @param b 書き込むバイト配列
     */
    public void setBytes(int offset, byte[] b) {
        bb.position(offset);
        bb.putInt(b.length); // 配列の長さを書き込む
        bb.put(b);           // 配列本体を書き込む
    }

    /**
     * 指定オフセットから文字列を読み取る
     * 実体はバイト配列として格納されており、ASCIIでデコードして文字列に変換
     * @param offset 読み取り開始位置
     * @return 読み取った文字列
     */
    public String getString(int offset) {
        byte[] b = getBytes(offset);
        return new String(b, CHARASET);
    }

    /**
     * 指定オフセットに文字列を書き込む
     * ASCIIエンコーディングでバイト配列に変換し、バイト配列として保存する
     * @param offset 書き込み位置
     * @param s 書き込む文字列
     */
    public void setString(int offset, String s) {
        byte[] b = s.getBytes(CHARASET);
        setBytes(offset, b);
    }

    /**
     * 文字列を格納するために必要な最大バイト数を計算する
     * バイト数 = 文字列長 × 1文字あたりの最大バイト数 + 配列長用のint分（4バイト）
     * @param strlen 文字列の最大長（文字数）
     * @return 必要な最大バイト数
     */
    public static int maxLength(int strlen) {
        float bytesPerChar = CHARASET.newEncoder().maxBytesPerChar(); // 通常は1.0（ASCII）
        return Integer.BYTES + (strlen * (int)bytesPerChar); // 配列長(int) + 本文
    }

    /**
     * FileMgrなどがバッファの中身全体にアクセスできるようにする内部用メソッド
     * 呼び出し元が読み書き開始位置を制御できるように、position(0)にリセットする
     * @return バッファの参照
     */
    ByteBuffer contents() {
        bb.position(0);
        return bb;
    }
}
