# ChestShop-3

看板とチェストでショップを作れる Bukkit / Spigot / Paper 用プラグイン(2011 年から続く)。
**フォーク元**: [ChestShop-authors/ChestShop-3](https://github.com/ChestShop-authors/ChestShop-3)(作者 Acrobot ほか)
リポジトリ: https://github.com/gorogoro-space/ChestShop-3(LGPL-2.1、公開リポジトリ)

## 作業の進め方(必ず守ること)

- **設計が確定するまで実装しない。** 機能追加や仕様変更は、まず設計案(何を・なぜ・どう変えるか、変更ファイルと影響範囲)を提示し、承認を得てからコードを書く。
- 判断が必要な点は、選択肢を示して質問する。勝手に決めない。
- やり取りは日本語で行う。
- 変更は必要最小限にする。頼まれていないリファクタリングや機能追加はしない(フォークなので、元のコードとの差分を小さく保つ意味もある)。
- 作業後は、変更・追加・削除したファイルの一覧と変更内容を報告する。
- 仕様を変えたら README.md と CLAUDE.md も合わせて更新する。
- `git push` の前には必ず確認を取る。コミットは意味のある単位で分ける。コミットメッセージは日本語でよい。
- リポジトリへの初回の `git push` の前には、「GitHub で Watch 設定(Custom → Issues と Pull requests)はしましたか?」と日本語で確認する。自動 Watch は廃止されており、設定しないと PR の通知が届かない(このリポジトリは Issues が無効)。
- コミットメッセージや PR に `Co-Authored-By: Claude` などの署名を付けない。`.claude/` は Git に入れない(`.git/info/exclude` で除外する)。
- このリポジトリはフォークなので、`gh pr create` の既定の送り先は元の ChestShop-authors になる。PR は必ず `--repo gorogoro-space/ChestShop-3 --base master` を指定して、ごろ鯖のリポジトリに作る。元の ChestShop-authors へ PR を出すのは頼まれたときだけ。
- 実装中に設計の抜けや穴に気づいたら、黙って対処せず報告して相談する。
- プルリクエストをチェックするときは、次の観点で確認して報告する。
  - 変更概要(何を・なぜ変えているか)
  - 脆弱性(権限チェックの漏れ、入力値の検証、SQL・パスの扱いなど)
  - 安全面(データの破損・消失、DB の移行、他プラグインの妨げ、古いバージョンのサーバーでの互換性など)
  - 性能(TPS など)の低下(高頻度イベントでの重い処理、メインスレッドでの同期 I/O、定期タスクの追加など。「最重要の設計方針」に沿っているか)

## フォークとしての注意

- 既存コードのコメント・ログ・識別子・メッセージは英語。既存の英語コメントは書き換えない。追加するコメントは日本語でよい。
- パッケージ名 `com.Acrobot.ChestShop` / `com.Acrobot.Breeze` は変えない(他のプラグインが API として使っている)。
- ライセンスは LGPL-2.1。元の著作権表示を消さない。
- **元の更新を取り込む(`git fetch upstream` のあと merge する)ときは、ビルドの設定が二重管理であることに注意する**(下の「ビルド」を参照)。

## 環境

- 対象サーバー: Bukkit / Spigot / Paper。コンパイルは Java 8 向け(`--release 8`)。Spigot API は 1.13.2 に対してコンパイルし、新しい版の差は `adapter/` のモジュールで吸収する
- **ビルドは JDK 21 で実行する**(`JAVA_HOME` を JDK 21 にする。Gradle 9 は Java 17 以上が必要で、最新の JDK だと Java 8 向けのビルドができないことがある)
- モジュール構成
  - `plugin/`: 本体(`com.Acrobot.ChestShop`、`com.Acrobot.Breeze`)
  - `adapter/spigot/<版>`、`adapter/paper/<版>`: Minecraft の版ごとの差を吸収するアダプタ(7 つ)
  - `assemble/`: 全モジュールと取り込むライブラリを 1 つの jar にまとめる
  - `repo/`: Maven のリポジトリにない依存(`install_dependency_to_repo.sh` で入れる)

### ビルド

Gradle と Maven の**両方**でビルドできる。Maven の `pom.xml` は元のプロジェクトのものを残してある。

| | Gradle(推奨) | Maven |
|---|---|---|
| コマンド | `gradlew.bat clean build`(Linux / Mac は `./gradlew clean build`) | `mvn clean install` |
| 完成品 | `build/libs/ChestShop.jar` | `target/ChestShop.jar` |
| テスト | 一緒に実行される | 一緒に実行される(`-DskipTests` で省略) |

- 「ビルドして」と言われたら、常にクリーンビルドする
- **二重管理になっているもの**(片方を変えたら、もう片方にも同じ変更をする)
  - 依存ライブラリとその版: `plugin/pom.xml` と `plugin/build.gradle.kts`、アダプタの `pom.xml` と `build.gradle.kts`
  - バージョン: 各 `pom.xml` と `gradle.properties` の `chestshopVersion`(`update_version.sh` は Maven だけを更新する)
  - 取り込むライブラリとパッケージ名の付け替え(relocation): `assemble/pom.xml` と `assemble/build.gradle.kts`
  - `plugin.yml` の `${bukkit.plugin.version}` は、ビルド時に `版 (compiled at 時刻)` に置き換わる(Gradle は `assemble/build.gradle.kts`)
- 提供側プラグイン(WorldGuard など)は、Gradle では間接依存を取らない設定にしている(Maven は欠けた間接依存を警告で済ませるが、Gradle は失敗するため)。コンパイルに必要な間接依存が増えたら `plugin/build.gradle.kts` に足す
- バージョンは `3.13-SNAPSHOT` のように、開発中は `-SNAPSHOT` を付ける(`create_release_files.sh` は `-SNAPSHOT` のときは配布用の jar を作らない)
- Claude の確認はビルドまで。テストサーバーは起動しない。実機での動作確認はユーザーが行う

## 最重要の設計方針

### TPS に影響させない
- 高頻度イベント(`InventoryMoveItemEvent`、`PlayerInteractEvent`、`BlockPhysicsEvent` など)は、安い判定を先に行い、対象外なら即座に抜ける
- メインスレッドで DB やファイルの同期 I/O をしない(ただし、起動時に一度だけ行うものは除く)。DB の書き込みは非同期にする(`ChestShop` が持つ `ExecutorService` を使う)
- 定期タスクは最小限にし、追加するときは頻度と理由を設計案に書く
- 設定は起動時に一度だけ解析して保持する

### データの保存
- DB は SQLite(ORMLite)。`accounts`(名前・UUID・メッセージの ON/OFF)と `items`(アイテムのコードの対応表)の 2 つ。表を足す・変えるときは、`Migrations` で既存のデータを壊さずに移行する
- 取引の記録は、ログ(`ChestShop Shops`、コンソールと任意のファイル)に 1 行ずつ出るだけで、DB には保存していない
- プラグインのフォルダ以外には何も書き込まない

### 他プラグインとの関係
- `plugin.yml` の `softdepend` に、Vault、WorldGuard、LWC、PlotSquared など多数がある。どれが無くても動くこと
- 他プラグインの API は、compileOnly の依存として使い、jar に取り込まない

## ファイル構成(plugin/src/main/java/com/Acrobot/)

- `ChestShop/ChestShop.java`: メインクラス。起動処理、コマンドとリスナーの登録、ログの設定
- `ChestShop/Commands/`: コマンド(`/iteminfo`、`/shopinfo`、`/cstoggle`、`/csaccess`、`/csGive` など)
- `ChestShop/Listeners/`: イベント処理。取引は `PreTransaction`(検証)→ `PostTransaction`(実行・記録)の順
- `ChestShop/Database/`: `Account`、`Item`、`Migrations`、DAO の作成
- `ChestShop/UUIDs/NameManager.java`: アカウント(名前と UUID)の管理
- `ChestShop/Configuration/`: `Properties`(config.yml)、`Messages`、`Permission`
- `ChestShop/Signs/`: 看板の解析・判定
- `ChestShop/Plugins/`: 保護プラグインとの連携(WorldGuard、PlotSquared など)
- `Breeze/`: ユーティリティ(設定・DB・インベントリ・UUID など)
- `plugin/src/main/resources/`: `plugin.yml`、`languages/`(言語ファイル)
