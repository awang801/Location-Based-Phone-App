<?php include("Util/validationUtil.php"); ?>
<?php
//Input: group_name (string), admin_id, state (int)
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
  $state = $v->test_input($_GET['state']);
}
else
{
 echo "WrongMethod";
 $link->close();
 exit;
}

$state = intval($state);

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

$group_id = $link->real_escape_string($group_id);
$result = $link->query("SELECT Users.name AS n, Group_Users.GroupUsers_Id AS id
                        FROM Group_Users
                        INNER JOIN Users
                          ON Group_Users.User_Id = Users.User_Id
                        WHERE Group_Users.Group_Id = '$group_id'
                        AND Group_Users.State = '$state'");

$data = array();
while($r = $result->fetch_assoc())
{
  $data[] = $r;
}

echo json_encode($data);

$link->close();
?>
