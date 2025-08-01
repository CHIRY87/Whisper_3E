<?php
require_once 'errorMsgs.php';
require_once 'mysqlConnect.php';

// 2. Input取得
$postData = json_decode(file_get_contents("php://input"), true);
$userId = $postData['userId'] ?? null;
$followUserId = $postData['followUserId'] ?? null;
$followFlg = $postData['followFlg'] ?? null;

// 3. 必須チェック
if (!$userId) returnError("006");
if (!$followUserId) returnError("012");
if (!isset($followFlg)) returnError("013");

try {
    $pdo->beginTransaction();

    if (filter_var($followFlg, FILTER_VALIDATE_BOOLEAN)) {
        // follow 登録
        $sql = "INSERT INTO follow (userId, followUserId) VALUES (:userId, :followUserId)";
        $stmt = $pdo->prepare($sql);
        $stmt->bindValue(":userId", $userId);
        $stmt->bindValue(":followUserId", $followUserId);
        $stmt->execute();
    } else {
        // follow 削除
        $sql = "DELETE FROM follow WHERE userId = :userId AND followUserId = :followUserId";
        $stmt = $pdo->prepare($sql);
        $stmt->bindValue(":userId", $userId);
        $stmt->bindValue(":followUserId", $followUserId);
        $stmt->execute();
    }

    $pdo->commit();
    $response = ["result" => "success"];

} catch (PDOException $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    returnError("001");
} finally {
    require_once 'mysqlClose.php';
    closeConnection($pdo);
}

header('Content-Type: application/json; charset=utf-8');
echo json_encode($response);
?>