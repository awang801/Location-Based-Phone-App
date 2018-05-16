<?php include("Util/validationUtil.php"); ?>
<?php
//Input: id (string), datetime (string)
//Result: response telling backend what syncs to clear

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
  $login_id = $v->test_input($_POST['id']);
  $datetime = $v->test_input($_POST['datetime']);
}
else
{
 echo "WrongMethod";
 $link->close();
 exit;
}

if(empty($login_id) || empty($datetime))
{
  echo "InputError" . PHP_EOL;
  $link->close();
  exit;
}

include("Lookup/userLookup.php");

$user_id = userLookup($login_id, $link);

if($user_id == false)
{
  echo "Lookup Failed - user";
  $link->close();
  exit;
}

$result = $link->query("UPDATE Group_Users
                        SET Sync = FALSE
                        WHERE Sync = TRUE
                        AND State = 0
                        AND Sync_Change <= '$datetime'");

if($result)
{
  echo "Success";
}
else {
  echo "Failed";
}

$link->close();
?>
