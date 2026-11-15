# SimpleDB

Edward Sciore『Database Design and Implementation』で教材として使われるデータベースエンジンSimpleDBを、本のコードを書き写しながらJavaで実装している途中のリポジトリ。ファイル管理からレコード管理までの層を書いたところで止まっており、現時点では全体のコンパイルが通らない。

## できること

単独でコンパイルできるのはファイル層とログ層（`Page` / `BlockId` / `FileMgr` / `LogMgr` / `LogIterator`）まで。それより上の層は本のコードを書き写した段階で、未定義のクラスや綴りの誤りが残っている。

- ファイル層: DBのファイルを固定長ブロック単位で読み書きする `FileMgr`、ブロック内のint・文字列・バイト列を読み書きする `Page`、ファイル名とブロック番号の組 `BlockId`
- ログ層: ログレコードをブロックに追記する `LogMgr` と、新しい順に読み出す `LogIterator`
- バッファ層（書きかけ）: バッファプールでpin/unpinを管理する `Buffer` / `BufferMgr`。空きバッファが無いときは最大10秒待つ
- トランザクション層（書きかけ）: `Transaction`、undoログでロールバック・復旧する `RecoveryMgr`、共有ロック・排他ロックを扱う `ConcurrecyMgr` / `LockTable`、ピン留めしたバッファを覚える `BufferList`
- レコード層（書きかけ）: `Schema`、フィールドのオフセットを決める `Layout`、スロット単位でレコードを扱う `RecordPage`、テーブルを先頭から走査する `TableScan`、レコードの位置 `RID`

## 動かし方

ビルドツールは使っておらず、IntelliJ IDEAのプロジェクト（`SimpleDB.iml`、JDK 23指定）として作っている。コンパイルが通る範囲は次のコマンドで確かめた（JDK 17）。コメントが日本語なので `-encoding UTF-8` が要る。

```sh
javac -encoding UTF-8 -d out \
  src/SimpleDB/Page.java src/SimpleDB/BlockId.java src/SimpleDB/FileMgr.java \
  src/SimpleDB/LogMgr.java src/SimpleDB/LogIterator.java
```

`LogTest` などの確認用プログラム（`main` を持つ `*Test.java`）は、本にあるエンジン本体のクラス `SimpleDB` がまだ無いため動かせない。

## 構成

| パス | 役割 |
|---|---|
| `src/SimpleDB/Page.java` / `BlockId.java` / `FileMgr.java` | ファイル層 |
| `src/SimpleDB/LogMgr.java` / `LogIterator.java` | ログ層 |
| `src/SimpleDB/Buffer.java` / `BufferMgr.java` | バッファ層 |
| `src/SimpleDB/Transaction.java` / `RecoverMgr.java` / `LogRecord.java` / `SetStringRecord.java` | トランザクションと復旧 |
| `src/SimpleDB/ConcurrecyMgr.java` / `LockTable.java` / `BufferList.java` | ロックとトランザクション単位のバッファ管理 |
| `src/SimpleDB/Schema.java` / `Layout.java` / `RecordPage.java` / `TableScan.java` / `src/RID.java` | レコード層 |
| `src/SimpleDB/*Test.java` | 本の確認用プログラムを写したもの（JUnitではない） |

## 設計・実装のポイント

ここに書くのは本の設計で、このリポジトリのコードではまだ動かして確かめていない。

- 層を下から積み上げる構成。ファイル → ログ → バッファ → トランザクション → レコードの順に作り、各層は1つ下の層だけを使う
- `Page` は `ByteBuffer` の上で、文字列やバイト列を「4バイトの長さ＋本体」の形で置く。文字列が最大で何バイトになるかは `Page.maxLength`（文字数×1文字の最大バイト数＋4）で求め、レコードの固定長スロットの大きさに使う
- `Buffer.flush` は、変更したページを書き出す前に、そのページに対応するログを `LogMgr.flush` でディスクに書く（WAL）
- `RecordPage` はブロックを固定長のスロットに区切り、スロット先頭の4バイトを空き・使用中のフラグにする。`format` はログを残さずに書き込む（`okToLog = false`）

## 学んだこと

- DBの下回りをファイル・ログ・バッファ・トランザクション・レコードの層に分けて組み立てる構成
- 変更をディスクに書く前にログを書くという順序と、undoログを新しい順にたどってロールバックする流れ
- 固定長スロットでレコードを並べ、空き・使用中のフラグで挿入位置を探すレコード管理

## 参考

- Edward Sciore『Database Design and Implementation』
