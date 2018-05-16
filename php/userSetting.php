<?php include("Util/validationUtil.php"); ?>
<?php
//Input: user_id (string), type (int), setting (int)
//Result: setting for user changed
// type is the value to be placed in db
// setting is the setting to overwrite
// 1 = Privacy
// 2 = Notify

$v = new Validation();
if($v->isSecure($_SERVER['https'], $_SERVER['SERVER_PORT']) == 0)
{
  echo "NotSecure";
  exit;
}

include("Util/connect.php");
$link = connect();

if($link == false)
{
  echo "Connection Failed";
  exit;
}

if ($_SERVER["REQUEST_METHOD"] == "POST") {
  $user_id = $v->test_input($_POST['user_id']);
  $type = $v->test_input($_POST['type']);
  $setting = $v->test_input($_POST['setting']);
}
else
{
 echo "WrongMethod";
 $link->close();
 exit;
}

if(empty($user_id))
{
  echo "InputError" . PHP_EOL;
  $link->close();
  exit;
}

//NOTE: this returns 0 on failure (i.e. default)
// This would need to be expanded if more than 2 options are present
$type = intval($type);
$setting = intval($setting);

include("Update/userSetting.php");

$result = userSettingUpdate($user_id, $type, $link, $setting);
echo $result;

$link->close();
?>
