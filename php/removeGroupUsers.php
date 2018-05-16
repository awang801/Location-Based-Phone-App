<?php include("Util/validationUtil.php"); ?>
<?php
//Input: id (string), row (int), state (int)
//NOTE: row is an easy way to figure out who to delete if admin (can be member too)
// state defines what state the phone thinks the entry should be
//Result: Leave a group

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
  $row = $v->test_input($_POST['row']);
  $state = $v->test_input($_POST['state']);
}
else
{
 echo "WrongMethod";
 $link->close();
 exit;
}

if(empty($login_id) || empty($row))
{
  echo "InputError" . PHP_EOL;
  $link->close();
  exit;
}

$row = intval($row);
$state = intval($state);

include("Lookup/userLookup.php");

$user_id = userLookup($login_id, $link);

if($user_id == false)
{
  echo "Lookup Failed - user";
  $link->close();
  exit;
}

include("Lookup/groupLookup.php");

$user_id = $link->real_escape_string($user_id);
$result = $link->query("DELETE Group_Users FROM Group_Users
                        INNER JOIN Groups
                          ON Group_Users.Group_Id = Groups.Group_Id
                        WHERE Group_Users.GroupUsers_Id = '$row'
                          AND Group_Users.State = '$state'
                          AND (Group_Users.User_Id = '$user_id'
                              OR Groups.User_Id = '$user_id')");

if($result)
{
  echo $link->affected_rows;
}
else {
  //$errorString = mysqli_error($link);
  echo "-1";
}

$link->close();
?>
