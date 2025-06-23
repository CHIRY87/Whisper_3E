
<?php
require_once 'errorMsgs.php';      // エラー返却処理

header('Content-Type: application/json; charset=UTF-8');

// Inputパラメータの取得
$input = json_decode(file_get_contents('php://input'), true);
$userId = isset($input['userId']) ? trim($input['userId']) : null;
$content = isset($input['content']) ? trim($input['content']) : null;


// 必須チェック
if (empty($userId)) {
    returnError('006'); // ユーザID未指定
}

if (empty($content)) {
    returnError('005'); // ささやき内容未指定
}

// DB接続
require_once 'mysqlConnect.php';

try {
    // トランザクション開始
    $pdo->beginTransaction();

    // INSERT処理
    $sql = "
        INSERT INTO whisper (userId, content, postDate)
        VALUES (:userId, :content, NOW())
    ";

    $stmt = $pdo->prepare($sql);
    $stmt->bindParam(':userId', $userId, PDO::PARAM_STR);
    $stmt->bindParam(':content', $content, PDO::PARAM_STR);

    if (!$stmt->execute()) {
        throw new Exception('SQL実行エラー'); // 手動で例外スロー
    }

    // コミット
    $pdo->commit();

    // 成功レスポンス生成
    $response = [
        "result" => "success",
        "message" => "ささやきが登録されました。"
    ];
    echo json_encode($response, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);

} catch (Exception $e) {
    // ロールバック
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }

    returnError('001'); // SQL実行エラー
} finally {
    // DB切断
    require_once 'mysqlClose.php';     // DB切断
    closeConnection($pdo);
}
?>
