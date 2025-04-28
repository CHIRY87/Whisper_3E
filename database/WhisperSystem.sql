-- クラス：SK3A
-- グループ：E
-- データベースの担当者：NGUYEN THI HOAN
-- 作成日：4月28日

--WhisperSystemデータベース採用文
create database IF NOT EXISTS WhisperSystem;

-- dbuser ユーザに whisperystem の権限を付与
-- GRANT ALL ON whisperSystem.* TO dbuser;
use WhisperSystem; -- システムのデータベースを触りたい時の文


-- 既存オブジェクトの削除
DROP VIEW IF EXISTS goodCntView;
DROP VIEW IF EXISTS whisperCntView;
DROP VIEW IF EXISTS followerCntView;
DROP VIEW IF EXISTS followCntView;
 
DROP TABLE IF EXISTS goodInfo;
DROP TABLE IF EXISTS whisper;
DROP TABLE IF EXISTS follow;
DROP TABLE IF EXISTS user;
--
create table user(--ユーザ情報表
    userId varchar(30) NOT NULL, --ユーザID
    userName varchar(20) NOT NULL, --ユーザ芽名
    password varchar(64) NOT NULL, --パスワード
    profile varchar(200) DEFAULT '', --プロフィール
    iconpath varchar(100), --アイコン
    PRIMARY KEY (userId) 
);
CREATE TABLE follow (
    userId VARCHAR(30) NOT NULL,
    followUserId VARCHAR(30) NOT NULL,
    PRIMARY KEY (userId, followUserId),
    FOREIGN KEY (userId) REFERENCES users(userId), --ユーザ情報表にユーザIDを参照する
    FOREIGN KEY (followUserId) REFERENCES users(userId) --ユーザ情報表にユーザIDを参照する
);
create table whisper(--  ささやき管理表
    whisperNo bigint NOT NULL ,　--管理番号
    userId varchar(30) NOT NULL,　-- ユーザID
    postDate DATE NOT NULL DEFAULT (current_date), -- 投稿日付
    content varchar(256) NOT NULL,  -- 内容    
    imagePath varchar(100),　-- 画像
    PRIMARY KEY (whisperNo),
    foreign key (userId) references user(userId) --ユーザ情報表にユーザIDを参照する
);
-- いいね情報テーブル
create table goodInfo(
    userId varchar(30) NOT NULL,　　--ユーザID
    whisperNo bigint NOT NULL,  --ささやき管理番号
    PRIMARY KEY (userId, whisperNo), 　-- (userId, whisperNo) primary key の設定
    FOREIGN KEY (userId) REFERENCES user(userId), --ユーザ情報表にユーザIDを参照する
    FOREIGN KEY (whisperNo) REFERENCES whisper(whisperNo) -- ささやき管理テーブルから管理番号を参加する。
);

--フォロー件数ビュー
CREATE OR REPLACE VIEW followCntView AS  --OR REPLACE は　ビューがなかったら新しい作る、あったら上書き（更新）する、また、この文のASは SELECT文の内容をビューに割り当てる ということです。
SELECT userId,COUNT(*) AS cnt -- COUNT(*) は、follow テーブル内の各 userId に対してレコード（行）の数を自動的にカウントします！
FROM follow
GROUP BY userId;

-- フォロワー件数ビュー
CREATE OR REPLACE VIEW followerCntView AS  
SELECT followUserId , COUNT(*) AS cnt  --followテーブルからfollowUserIdごとに件数を数え、cntという名前で表示します。
FROM follow
GROUP BY followUserId;

-- ささやき件数ビュー
CREATE OR REPLACE VIEW whisperCntView AS
SELECT userId, COUNT(*) AS cnt  --whisperテーブルからuserIdごとに、ささやきの件数（レコード数）をカウントして、その結果をcntという名前で表示します。
FROM whisper
GROUP BY userId;

-- いいね件数ビュー
CREATE OR REPLACE VIEW goodCntView AS
SELECT whisperNo, COUNT(*) AS cnt  --goodInfoテーブルから、whisperNoごとに、いいねの件数（レコード数）をカウントして、その結果をcntという名前で表示します。
FROM goodInfo
GROUP BY whisperNo;
 
-- サンプルデータ挿入
INSERT INTO user (userId, userName, password) VALUES
    ('1', 'Test1', '123'),
    ('2', 'Test2', '123'),
    ('3', 'Test3', '123');
 
INSERT INTO whisper (userId, content, imagePath) VALUES
    ('1', ' system test 1', 'image1.jpg'),
    ('2', ' system test 2', 'image2.jpg'),
    ('3', ' system test 3', 'image3.jpg');
 
-- いいね情報を挿入
INSERT INTO goodInfo (userId, whisperNo) VALUES
    ('1', 2),
    ('2', 3);
 
COMMIT; 



