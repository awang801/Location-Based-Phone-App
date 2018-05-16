<?php include("Util/validationUtil.php"); ?>
<?php
//Input: id (string)
//Result: Remove user

//NOTE: type refers to how this script handles no invitations
//Default = 0 : error message is returned
// Anything else : automatically creates a request

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
}
else
{
 echo "WrongMethod";
 $link->close();
 exit;
}

if(empty($login_id))
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

$user_id = $link->real_escape_string($user_id);
$result1 = $link->query("DELETE FROM Group_Users
                        WHERE User_Id = '$user_id'");
$result2 = $link->query("DELETE FROM Feed
                        WHERE User_Id = '$user_id'");

$result3 = $link->query("DELETE FROM Users
                         WHERE  User_Id = '$user_id'");

if($result1 && $result2 && $result3)
{
  echo "Success";
}
else
{
  $errorString = mysqli_error($link);
  echo $errorString;
}



$link->close();
?>
