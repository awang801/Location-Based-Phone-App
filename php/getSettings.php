<?php include("Util/validationUtil.php"); ?>
<?php
//Input: group_name (string), admin_id
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
  $group_name = $v->test_input($_GET['group_name']);
  $admin_id = $v->test_input($_GET['admin_id']);
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
  echo "Lookup Failed - user";
  $link->close();
  exit;
}

include("Lookup/groupLookup.php");

$group_id = groupLookup($group_name, $link);

if($group_id == false)
{
  echo "Lookup Failed - group";
  $link->close();
  exit;
}

include("Lookup/isAdmin.php");
$is_admin = isAdmin($group_id, $admin_id, $link);

if(!$is_admin)
{
  echo "Lookup Failed - Not Admin";
  $link->close();
  exit;
}

//TODO: You idiot, you can do the check for admin in sql if you want
// If no results then they aren't admin!
$group_id = $link->real_escape_string($group_id);
$result = $link->query("SELECT Name AS name, Description AS description, Att_Type AS type, Att_Period AS period, Att_Duration AS duration, Att_Vis AS avis, Att_Start AS start, Att_Stop AS stop, Vis AS vis, Feed_Rights AS fr, Lng AS lng, Lat AS lat, radius AS r, Img_Id AS img
                        FROM Groups
                        WHERE Group_Id = '$group_id'");

if($result->num_rows > 0)
{
  echo json_encode($result->fetch_assoc());
}
else {
  echo "Failure";
}

$link->close();
?>
