-- クラス：SK3A
-- グループ：E
-- データベースの担当者：NGUYEN THI HOAN
-- 作成日：4月30日


create database IF NOT EXISTS WhisperSystem;


use WhisperSystem; 



DROP VIEW IF EXISTS goodCntView;
DROP VIEW IF EXISTS whisperCntView;
DROP VIEW IF EXISTS followerCntView;
DROP VIEW IF EXISTS followCntView;
 
DROP TABLE IF EXISTS goodInfo;
DROP TABLE IF EXISTS whisper;
DROP TABLE IF EXISTS follow;
DROP TABLE IF EXISTS user;

create table user(
    userId varchar(30) NOT NULL, 
    userName varchar(20) NOT NULL, 
    password varchar(64) NOT NULL, 
    profile varchar(200) DEFAULT '', 
    iconpath varchar(100), 
    PRIMARY KEY (userId) 
);
CREATE TABLE follow (
    userId VARCHAR(30) NOT NULL,  
    followUserId VARCHAR(30) NOT NULL,  
    PRIMARY KEY (userId, followUserId),
    FOREIGN KEY (userId) REFERENCES user(userId),  
    FOREIGN KEY (followUserId) REFERENCES user(userId) 
);

create table whisper(
    whisperNo BIGINT NOT NULL AUTO_INCREMENT,
    userId VARCHAR(30) NOT NULL,
    postDate DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP),
    content VARCHAR(256) NOT NULL, 
    imagePath VARCHAR(100),
    PRIMARY KEY (whisperNo),
    FOREIGN KEY (userId) REFERENCES user(userId) 
);


create table goodInfo(
    userId varchar(30) NOT NULL,
    whisperNo bigint NOT NULL,
    PRIMARY KEY (userId, whisperNo),
    FOREIGN KEY (userId) REFERENCES user(userId),
    FOREIGN KEY (whisperNo) REFERENCES whisper(whisperNo)
);

CREATE OR REPLACE VIEW followCntView AS  
SELECT userId,COUNT(*) AS cnt  
FROM follow
GROUP BY userId;

CREATE OR REPLACE VIEW followerCntView AS  
SELECT followUserId , COUNT(*) AS cnt  
FROM follow
GROUP BY followUserId;

CREATE OR REPLACE VIEW whisperCntView AS
SELECT userId, COUNT(*) AS cnt  
FROM whisper
GROUP BY userId;

CREATE OR REPLACE VIEW goodCntView AS
SELECT whisperNo, COUNT(*) AS cnt  
FROM goodInfo
GROUP BY whisperNo;
 
INSERT INTO user (userId, userName, password) VALUES
    ('1', 'Test1', '123'),
    ('2', 'Test2', '123'),
    ('3', 'Test3', '123');
INSERT INTO whisper (userId, content, imagePath) VALUES
    ('1', ' system test 1', 'image1.jpg'),
    ('2', ' system test 2', 'image2.jpg'),
    ('3', ' system test 3', 'image3.jpg');
 
INSERT INTO goodInfo (userId, whisperNo) VALUES
    ('1', 2),
    ('2', 3);
INSERT INTO follow (userId, followUserId) VALUES
    ('1', 3),
    ('2', 3);    
COMMIT; 



