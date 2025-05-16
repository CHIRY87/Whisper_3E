-- クラス：SK3A
-- チーム：E
-- データベースの担当者：NGUYEN THI HOAN
-- 作成日：5月16日

<?php
// 1. エラー返却処理を読み込む
require_once 'errorMsgs.php';

// 2. 入力パラメータ取得
$input = json_decode(file_get_contents("php://input"), true);

// 3. 入力チェック
if (!isset($input['section'])) {
    returnError('009'); // 検索区分が指定されていません
}
if (!isset($input['string']) || trim($input['string']) === '') {
    returnError('010'); // 検索文字列が指定されていません
}

$section = $input['section'];
$searchString = $input['string'];

// 4. 検索区分の整合性チェック
if ($section !== '1' && $section !== '2') {
    returnError('016'); // 検索区分が不正です
}

// 5. DB接続
require_once 'mysqlConnect.php'; // ここで $pdo が利用可能になる

header('Content-Type: application/json; charset=utf-8');

try {
    if ($section === '1') {
        // ユーザ検索
        $sql = "
            SELECT 
                u.userId AS userId,
                u.userName AS userName,
                IFNULL(w.cnt, 0) AS whisperCount,
                IFNULL(f.cnt, 0) AS followCount,
                IFNULL(fr.cnt, 0) AS followerCount
            FROM user u
            LEFT JOIN whisperCntView w ON u.userId = w.userId
            LEFT JOIN followCntView f ON u.userId = f.userId
            LEFT JOIN followerCntView fr ON u.userId = fr.followUserId
            WHERE u.userId LIKE :search1 OR u.userName LIKE :search2
        ";
        $stmt = $pdo->prepare($sql);
        $stmt->bindValue(':search1', '%' . $searchString . '%');
        $stmt->bindValue(':search2', '%' . $searchString . '%');
        $stmt->execute();
        $userList = $stmt->fetchAll();
        echo json_encode([
            'result' => 'success',
            'userList' => $userList
        ], JSON_UNESCAPED_UNICODE);
    } elseif ($section === '2') {
        // ささやき検索
        $sql = "
            SELECT 
                w.whisperNo AS whisperNo,
                u.userId AS userId,
                u.userName AS userName,
                w.postDate AS postDate,
                w.content AS content,
                IFNULL(l.cnt, 0) AS goodCount
            FROM whisper w
            INNER JOIN user u ON w.userId = u.userId
            LEFT JOIN goodCntView l ON w.whisperNo = l.whisperNo
            WHERE w.content LIKE :search
        ";
        $stmt = $pdo->prepare($sql);
        $stmt->bindValue(':search', '%' . $searchString . '%');
        $stmt->execute();
        $whisperList = $stmt->fetchAll();
        echo json_encode([
            'result' => 'success',
            'whisperList' => $whisperList
        ], JSON_UNESCAPED_UNICODE);
    }
} catch (PDOException $e) {
    echo $e->getMessage(); // ログ出力などに活用できます
    returnError('001'); // データベース処理が異常終了しました
}

// 6. DB切断（明示的に閉じる必要はないが、ファイルとして記述するなら）
require_once 'mysqlClose.php';
?>