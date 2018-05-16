<?php include("Util/validationUtil.php"); ?>
<?php
//Input: admin_id (string), group_name (string), type (int), setting (int)
//Result: setting for a group changed
// type is the value to be placed in db
// setting is the setting to overwrite
//Check groupSetting for list of int to column mapping

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
  $group_name = $v->test_input($_POST['group_name']);
  $admin_id = $v->test_input($_POST['admin_id']);
  $type = $v->test_input($_POST['type']);
  $setting = $v->test_input($_POST['setting']);
}
else
{
 echo "WrongMethod";
 $link->close();
 exit;
}

if(empty($group_name) || empty($admin_id))
{
  echo "InputError" . PHP_EOL;
  $link->close();
  exit;
}

//NOTE: this returns 0 on failure (i.e. default)
// This would need to be expanded if more than 2 options are present
$type = intval($type);
$setting = intval($setting);

include("Update/groupSetting.php");

$result = groupSettingUpdate($group_name, $admin_id, $type, $link, $setting);
echo $result;

$link->close();
?>
