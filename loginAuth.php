<?php
require_once 'mysqlConnect.php';  
require_once 'mysqlClose.php';
require_once 'errorMsgs.php';     

// 入力パラメータの取得
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
$postData = json_decode(file_get_contents('php://input'), true);
$userId = isset($postData['userId']) ? $postData['userId'] : null;
$password = isset($postData['password']) ? $postData['password'] : null;
}


if (empty($userId)) {
    returnError('006');  // ユーザIDが指定されていません
}

if (empty($password)) {
    returnError('007');  // パスワードが指定されていません
}

try {
    // ユーザIDとパスワードを一致させるSQL文
    $stmt = $pdo->prepare("SELECT COUNT(*) AS count FROM user WHERE userId = :userId AND password = :password");
    $stmt->bindParam(':userId', $userId, PDO::PARAM_STR);
    $stmt->bindParam(':password', $password, PDO::PARAM_STR);
    $stmt->execute();
    
    // データの取得
    $result = $stmt->fetch(PDO::FETCH_ASSOC);
    
    // ユーザIDとパスワードが一致するデータが1件以外の場合、エラーメッセージを返す
    if ($result['count'] != 1) {
        returnError('003');  // ユーザIDまたはパスワードが違います
    }

    // 認証成功
    $response = [
        "result" => "success"
    ];

    // JSON形式で返却
    echo json_encode($response);

} catch (PDOException $e) {
    // データベース接続エラー
    returnError('001');  // データベース処理が異常終了しました
} finally {
    // データベース接続を閉じる
    closeConnection($pdo);
}
?>
