<?php
function getErrorMessage($errCode) {
    $errorMessages = [
        '001' => 'データベース処理が異常終了しました',
        '002' => '変更内容がありません',
        '003' => 'ユーザIDまたはパスワードが違います',
        '004' => '対象データが見つかりませんでした',
        '005' => 'ささやき内容がありません',
        '006' => 'ユーザIDが指定されていません',
        '007' => 'パスワードが指定されていません',
        '008' => 'ささやき管理番号が指定されていません',
        '009' => '検索区分が指定されていません',
        '010' => '検索文字列が指定されていません',
        '011' => 'ユーザ名が指定されていません',
        '012' => 'フォロユーザIDが指定されていません',
        '013' => 'フォローフラグが指定されていません',
        '014' => 'イイねフラグが指定されていません',
        '015' => 'ログインユーザIDが指定されていません',
        '016' => '検索区分が不正です'
    ];

    if (isset($errorMessages[$errCode])) {
        return $errorMessages[$errCode];
    } else {
        return '不明なエラーが発生しました';
    }
}
function returnError($errCode) {
    $response = [
        "result" => "error",
        "errCode" => $errCode,
        "errMsg" => getErrorMessage($errCode)
    ];
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit;
}
?>
