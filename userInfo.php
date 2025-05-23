<?php
header('Content-Type: application/json; charset=utf-8');

// 1. エラーメッセージとデータベース接続のファイルを読み込む
require_once('errorMsgs.php');
require_once('mysqlConnect.php');

// 2. 入力データの取得
// JSON または POST フォームのデータを取得
$input = json_decode(file_get_contents("php://input"), true);
$userId = null;

// JSON から優先的に取得
if ($input && isset($input['userId'])) {
    $userId = trim($input['userId']);
} elseif (isset($_POST['userId'])) {
    $userId = trim($_POST['userId']);
}

// 3. 必須チェック
if (empty($userId)) {
    returnError('006'); // ユーザIDが指定されていません
}

try {
    // 4. SQL文の作成
    $sql = "SELECT userId, userName, profile, iconpath FROM user WHERE userId = :userId";
    $stmt = $pdo->prepare($sql);
    $stmt->bindParam(':userId', $userId);
    $stmt->execute();

    // 5. データの取得
    $user = $stmt->fetch();
    if (!$user) {
        returnError('004'); // 対象データが見つかりませんでした
    }

    // 6. 結果の返却
    $response = [
        "result" => "success",
        "userId" => $user['userId'],
        "userName" => $user['userName'],
        "profile" => $user['profile'] ?: null,
        "iconpath" => $user['iconpath'] ?: null
    ];
    echo json_encode($response, JSON_UNESCAPED_UNICODE);

} catch (PDOException $e) {
    // データベースエラーが発生した場合、エラーコード001を返す
    returnError('001');
} finally {
    // 7. データベース接続を切断
    require_once('mysqlClose.php');
}
?>
