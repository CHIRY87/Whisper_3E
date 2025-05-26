<?php
require_once 'mysqlConnect.php';
require_once 'mysqlClose.php';
require_once 'errorMsgs.php';

//グローバル変数化
global $pdo;

//リクエストデータの取得
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
  $postData = json_decode(file_get_contents('php://input'), true);
  $userId = isset($postData['userId']) ? $postData['userId'] : null;
  $userName = isset($postData['userName']) ? $postData['userName'] : null;
  $password = isset($postData['password']) ? $postData['password'] : null;
  $profile = isset($postData['profile']) ? $postData['profile'] : null;
}

//ユーザーIDチェック
if (empty($userId)) {
  returnError('006');
}
if (empty($userName) && empty($password) && empty($profile)) {
  returnError('002');
}

try {
  //トランザクション処理
  $pdo->beginTransaction();

  $sql = "UPDATE user SET ";
  $params = [];

  if (!empty($userName)) {
    $sql .= "userName = :userName, ";
    $params[':userName'] = $userName;
  }
  if (!empty($password)) {
    $sql .= "password = :password, ";
    $params[':password'] = password_hash($password, PASSWORD_DEFAULT);
  }
  if(!empty($profile)) {
    $sql .= "profile = :profile, ";
    $params[':profile'] = $profile;
  }
  
  //末尾のカンマを削除
  $sql = rtrim($sql, ", ");
  $sql .= " WHERE userId = :userId";
  $params[':userId'] = $userId;

  $stmt = $pdo->prepare($sql);
  $result = $stmt->execute($params);

  if (!$result) {
    throw new Exception('SQL Execution Failed');
  }

  //コミット
  $pdo->commit();

  //成功レスポンスをセット
  $response = [
      "result" => "success",
      "userId" => $userId,
      "userName" => $userName,
      "password" => $password,
      "profile" => $profile,
  ];
    
} catch (Exception $e) {
  //ロールバック
  $pdo->rollBack();
  returnError('001');
} finally {
  //DB切断
  closeConnection($pdo);
}

echo json_encode($response, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
?>