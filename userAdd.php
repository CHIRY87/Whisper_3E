<?php
require_once 'errorMsgs.php';
require_once 'mysqlConnect.php';

// 2. Input取得
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $postData = json_decode(file_get_contents('php://input'), true);
 
} 
$userId = $postData['userId'] ?? null;
$userName = $postData['userName'] ?? null;
$password = $postData['password'] ?? null;

// 3. 必須チェック
if (!$userId) returnError("006");
if (!$password) returnError("007");
if (!$userName) returnError("011");

try {
    // 5. トランザクション開始
    $pdo->beginTransaction();

    // 6. INSERT 実行
    $sql = "INSERT INTO user (userId, userName, password) VALUES (:userId, :userName, :password)";
    $stmt = $pdo->prepare($sql);
    $stmt->bindValue(":userId", $userId);
    $stmt->bindValue(":userName", $userName);
    $stmt->bindValue(":password", $password);
    $stmt->execute();

    // 7. コミット
    $pdo->commit();

    // 8. 成功返却値
    $response = [
        "result" => "success"
    ];

} catch (PDOException $e) {
    // 6-1. ロールバック
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    returnError("001");
} finally {
    // 9. DB切断
    require_once 'mysqlClose.php';
    closeConnection($pdo);
}

// 10. JSON 出力
header('Content-Type: application/json; charset=utf-8');
echo json_encode($response);
?>