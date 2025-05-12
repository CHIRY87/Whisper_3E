<?php
require_once 'mysqlConnect.php';
require_once 'mysqlClose.php';
require_once 'errorMsgs.php';

// リクエストデータの取得
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $postData = json_decode(file_get_contents('php://input'), true);
}

// パラメータの確認
if (empty($postData['userId'])) {
    returnError('006'); // ユーザーID未指定
    exit;
}
if (empty($postData['loginUserId'])) {
    returnError('015'); // ログインユーザーID未指定
    exit;
}

$userId = $postData["userId"];
$loginUserId = $postData["loginUserId"];



//1.ユーザ情報取得
$sql = "
SELECT
    u.userId,
    u.userName,
    u.profile,
    COALESCE(fv.cnt, 0) AS followCount,
    COALESCE(rv.cnt, 0) AS followerCount
FROM user u
LEFT JOIN followCntView fv ON u.userId = fv.userId
LEFT JOIN followerCntView rv ON u.userId = rv.followUserId
WHERE u.userId = :userId
";

try {
    $stmt = $pdo->prepare($sql);
    $stmt->bindParam(":userId", $userId, PDO::PARAM_STR);
    $stmt->execute();

    $user = $stmt->fetch(PDO::FETCH_ASSOC);

} catch (PDOException $e) {
    echo "$e";
    returnError('004'); // SQLエラー
    exit;
}

//2.フォロー中かどうか確認
$sqlFollow = "
SELECT 1
FROM follow
WHERE userId = :loginUserId AND followUserId = :userId
";

try {
    $stmtFollow = $pdo->prepare($sqlFollow);
    $stmtFollow->bindParam(':loginUserId', $loginUserId, PDO::PARAM_STR);
    $stmtFollow->bindParam(':userId', $userId, PDO::PARAM_STR);
    $stmtFollow->execute();

    $userFollowFlg = $stmtFollow->fetch() ? true : false;
} catch (PDOException $e) {
    echo "$e";
    returnError('004'); // 対象データが見つからない
    exit;
}


//3.whisperList取得
$sqlWhisper = "
SELECT
    w.whisperNo,
    w.userId,
    u.userName,
    w.postDate,
    w.content,
    CASE WHEN g.whisperNo IS NOT NULL THEN true ELSE false END AS goodFlg
FROM whisper w
JOIN user u ON w.userId = u.userId
LEFT JOIN goodInfo g ON w.whisperNo = g.whisperNo AND g.userId = :loginUserId
WHERE w.userId = :userId
ORDER BY w.postDate DESC
";

$stmtWhisper = $pdo->prepare($sqlWhisper);
$stmtWhisper->bindParam(':userId', $userId, PDO::PARAM_STR);
$stmtWhisper->bindParam(':loginUserId', $loginUserId, PDO::PARAM_STR);
$stmtWhisper->execute();

$whisperList = $stmtWhisper->fetchAll(PDO::FETCH_ASSOC);

//4.goodList取得
$sqlGood = "
SELECT
    w.whisperNo,
    w.userId,
    u.userName,
    w.postDate,
    w.content,
    CASE WHEN g2.whisperNo IS NOT NULL THEN true ELSE false END AS goodFlg
FROM goodInfo g
JOIN whisper w ON g.whisperNo = w.whisperNo
JOIN user u ON w.userId = u.userId
LEFT JOIN goodInfo g2 ON g2.whisperNo = w.whisperNo AND g2.userId = :loginUserId
WHERE g.userId = :userId
ORDER BY w.postDate DESC
";

$stmtGood = $pdo->prepare($sqlGood);
$stmtGood->bindParam(':userId', $userId, PDO::PARAM_STR);
$stmtGood->bindParam(':loginUserId', $loginUserId, PDO::PARAM_STR);
$stmtGood->execute();

$goodList = $stmtGood->fetchAll(PDO::FETCH_ASSOC);

//5.出力データ作成
$response = [
    "result" => "success",
    "userId" => $user["userId"],
    "userName" => $user["userName"],
    "profile" => $user["profile"],
    "userFollowFlg" => $userFollowFlg,
    "followCount" => (int) $user["followCount"],
    "followerCount" => (int) $user["followerCount"],
    "whisperList" => $whisperList ?: null,
    "goodList" => $goodList ?: null
];

// レスポンス送信
header('Content-Type: application/json; charset=UTF-8');
echo json_encode($response, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);

// DB切断
closeConnection($pdo);
?>