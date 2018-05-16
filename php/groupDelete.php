<?php include("Util/validationUtil.php"); ?>
<?php
//Input: admin_id (string), group_name (string)
//Result: Delete Group and intersections

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

include("Lookup/userLookup.php");

$admin_id = userLookup($admin_id, $link);

if($admin_id == false)
{
  echo "Lookup Failed - admin";
  $link->close();
  exit;
}

include("Lookup/groupLookupAuth.php");

$group_id = groupLookup($group_name, $admin_id, $link);

if($group_id == false)
{
  //NOTE: since this lookup requires admin privleges, this could mean
  //either group doesn't exsist or this user is not the admin
  echo "Lookup Failed - group";
  $link->close();
  exit;
}

$group_id = $link->real_escape_string($group_id);
$result1 = $link->query("DELETE FROM Group_Users
                        WHERE Group_Id = '$group_id'");
$result2 = $link->query("DELETE FROM Feed
                        WHERE Group_Id = '$group_id'");
$result3 = $link->query("DELETE FROM Att_Reports
                        WHERE Group_Id = '$group_id'");
$result4 = $link->query("DELETE FROM Groups WHERE Group_Id = '$group_id'");

if($result1 && $result2 && $result3 && $result4)
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
