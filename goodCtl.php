<?php
require_once 'errorMsgs.php';      // エラー返却処理

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
  $postData = json_decode(file_get_contents('php://input'), true);
  $userId = isset($postData['userId']) ? trim($postData['userId']) : null;
  $whisperNo = isset($postData['whisperNo']) ? trim($postData['whisperNo']) : null;
  $goodFlg = isset($postData['goodFlg']) ? $postData['goodFlg'] : null;
}

// エラーチェック
if (empty($userId)) {
    returnError('006');
}
if (empty($whisperNo)) {
    returnError('008');
}
if (!isset($goodFlg)) {
    returnError('014');
}

// DB接続
require_once 'mysqlConnect.php';

try {
  // トランザクション開始
  $pdo->beginTransaction();

  // イイね or イイね外しの処理
  if ($goodFlg == true) {
    // INSERT
    $sql = "
      INSERT INTO goodInfo (userId, whisperNo)
      VALUES (:userId, :whisperNo)
    ";
  } else {
    // DELETE
    $sql = "
      DELETE FROM goodInfo
      WHERE userId = :userId AND whisperNo = :whisperNo
    ";
  }

  $stmt = $pdo->prepare($sql);
  $stmt->bindParam(':userId', $userId, PDO::PARAM_STR);
  $stmt->bindParam(':whisperNo', $whisperNo, PDO::PARAM_STR);

  if (!$stmt->execute()) {
    throw new Exception('SQL実行エラー');
  }

  // コミット
  $pdo->commit();

  // 成功レスポンス
  $response = [
    "result" => "success",
    "userId" => $userId,
    "whisperNo" => $whisperNo
  ];

} catch (Exception $e) {
  // ロールバック
  $pdo->rollBack();
  returnError('001');
  exit;

} finally {
  // DB切断
  require_once 'mysqlClose.php';
  closeConnection($pdo);
}

// 出力
echo json_encode($response, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
?>
