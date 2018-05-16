<?php include("Util/validationUtil.php"); ?>
<?php

//Input is: instance (string), user_id (string)
//result: isntance pair is removed from db

$v = new Validation();
if($v->isSecure($_SERVER['https'], $_SERVER['SERVER_PORT']) == 0)
{
  echo "NotSecure";
  exit;
}

if ($_SERVER["REQUEST_METHOD"] == "POST") {
  $instance = $v->test_input($_POST['instance']);
  $user_id = $v->test_input($_POST['user_id']);
}
else
{
 echo "WrongMethod";
 exit;
}

if(empty($user_id) || empty($instance))
{
  echo "InputError" . PHP_EOL;
  exit;
}

include("Util/connect.php");
$link = connect();

if($link == false)
{
  echo "Connection Failed";
  exit;
}

include("Lookup/userLookup.php");

$user_id = userLookup($user_id, $link);

if($user_id == false)
{
  echo "User Not Available";
  $link->close();
  exit;
}


//TODO: Decide if we should care about instance!
$user_id = $link->real_escape_string($user_id);
$result = $link->query("DELETE FROM User_Device
                        WHERE User_Id = '$user_id'");
//NOTE: this extra sync shouldn't be required, but it will make sure everything
// Gets pulled on the first actual change after signing out/back in
date_default_timezone_set("UTC");
$datetime = date('Y-m-d H:i:s', time());
$datetime = $link->real_escape_string($datetime);
$result1 = $link->query("UPDATE Group_Users SET Sync = TRUE, Sync_Change = '$datetime'
                         WHERE User_Id = '$user_id'
                         AND State = 0");
if($result && $result1)
{
  echo "Success";
}
else {
  echo "Error";
}

$link->close();
?>
