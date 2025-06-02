<?php
header('Content-Type: application/json; charset=utf-8');

// １．エラー処理とデータベース接続ファイルを読み込む
require_once('errorMsgs.php');


// 2. 入力パラメータを取得する

$input = json_decode(file_get_contents("php://input"), true);
$userId = null;

if ($input && isset($input['userId'])) {
    $userId = trim($input['userId']);
} elseif (isset($_POST['userId'])) {
    $userId = trim($_POST['userId']);
}

// 3. 必須項目をチェックする
if (empty($userId)) {
    returnError('006'); // ユーザIDが指定されていません
}
//データ接続
require_once('mysqlConnect.php');
try {
    // 4. フォローリスト（userIdがフォローしているユーザー）を取得する

    $sql = "
        SELECT 
            u.userId,
            u.userName,
            u.iconpath,
            IFNULL(w.cnt, 0) AS whisperCount,
            IFNULL(fv.cnt, 0) AS followCount,
            IFNULL(frv.cnt, 0) AS followerCount
        FROM follow f
        JOIN user u ON f.followUserId = u.userId
        LEFT JOIN whisperCntView w ON u.userId = w.userId
        LEFT JOIN followCntView fv ON u.userId = fv.userId
        LEFT JOIN followerCntView frv ON u.userId = frv.followUserId
        WHERE f.userId = :userId
    ";
    $stmt = $pdo->prepare($sql);
    $stmt->bindParam(':userId', $userId);
    $stmt->execute();
    $followList = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // 5. フォロワーリスト（userIdをフォローしているユーザー）を取得する
    $sql = "
        SELECT 
            u.userId,
            u.userName,
            u.iconpath,
            IFNULL(w.cnt, 0) AS whisperCount,
            IFNULL(fv.cnt, 0) AS followCount,
            IFNULL(frv.cnt, 0) AS followerCount
        FROM follow f
        JOIN user u ON f.userId = u.userId
        LEFT JOIN whisperCntView w ON u.userId = w.userId
        LEFT JOIN followCntView fv ON u.userId = fv.userId
        LEFT JOIN followerCntView frv ON u.userId = frv.followUserId
        WHERE f.followUserId = :userId
    ";
    $stmt = $pdo->prepare($sql);
    $stmt->bindParam(':userId', $userId);
    $stmt->execute();
    $followerList = $stmt->fetchAll(PDO::FETCH_ASSOC);

    //6. 結果を返却する
    $response = [
        "result" => "success",
        "followList" => $followList ?: null,
        "followerList" => $followerList ?: null
    ];
    echo json_encode($response, JSON_UNESCAPED_UNICODE);

} catch (PDOException $e) {
    returnError('001'); // DBエラー
} finally {
    require_once('mysqlClose.php');
}
