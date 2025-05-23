<?php
require_once 'errorMsgs.php';
require_once 'mysqlConnect.php';

// ２．Inputパラメータの取得
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $postData = json_decode(file_get_contents('php://input'), true);
 
} 
   $userId = isset($postData['userId']) ? $postData['userId'] : null;
// ３．Inputパラメータの必須チェックを行う。
if (empty($userId)) {
    returnError('006'); // 【エラーコード：006】チェック対象：ユーザID
}


try {
    $sql = "
    SELECT 
        w.whisperNo,
        w.userId,
        u.userName,
        w.postDate,
        w.content,
        CASE WHEN g.userId IS NOT NULL THEN true ELSE false END AS goodFlg
    FROM whisper w
    INNER JOIN user u ON w.userId = u.userId
    LEFT JOIN goodInfo g ON w.whisperNo = g.whisperNo AND g.userId = :g_userId
    WHERE w.userId = :w_userId 
       OR w.userId IN (
           SELECT followUserId FROM follow WHERE userId = :f_userId
       )
    ORDER BY w.postDate DESC
";

$stmt = $pdo->prepare($sql);
$stmt->bindValue(':g_userId', $userId, PDO::PARAM_STR);
$stmt->bindValue(':w_userId', $userId, PDO::PARAM_STR);
$stmt->bindValue(':f_userId', $userId, PDO::PARAM_STR);
$stmt->execute();


// ５．データのフェッチを行う
$whisperList = [];

while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
    $whisperList[] = [
        'whisperNo' => $row['whisperNo'],
        'userId'    => $row['userId'],
        'userName'  => $row['userName'],
        'postDate'  => $row['postDate'],
        'content'   => $row['content'],
        'goodFlg'   => $row['goodFlg'] ? true : false,
    ];
}

// ６．返却値の連想配列に成功パラメータとささやきリスト連想配列のデータを格納する
$response = [
    'result' => 'OK',
    'whisperList' => $whisperList
];


} catch (PDOException $e) {
    // データベース処理が異常終了した場合
    echo json_encode($userId);

    returnError('001');
} finally {
    require_once 'mysqlClose.php';
    closeConnection($pdo);
}

// ８．返却値の連想配列をJSONにエンコードしてoutputパラメータを出力する。
header('Content-Type: application/json; charset=utf-8');
echo json_encode($response);

?>
