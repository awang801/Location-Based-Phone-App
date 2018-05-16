<?php include("Util/validationUtil.php"); ?>
<?php
//Input: user_id
//Result: json of the groups settings

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

if ($_SERVER["REQUEST_METHOD"] == "GET") {
  $user_id = $v->test_input($_GET['user_id']);
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

include("Lookup/userLookup.php");

$user_id = userLookup($user_id, $link);

if($user_id == false)
{
  echo "Lookup Failed - user";
  $link->close();
  exit;
}

$result = $link->query("SELECT Name AS name, Email AS email, Privacy AS priv, Notify AS notify
                        FROM Users
                        WHERE User_Id = '$user_id'");

if($result->num_rows > 0)
{
  echo json_encode($result->fetch_assoc());
}
else {
  echo "Failure";
}

$link->close();
?>
