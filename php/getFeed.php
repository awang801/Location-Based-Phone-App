<?php include("Util/validationUtil.php"); ?>
<?php
//Input: group_name (string), admin_id, size (int)
//Result: json of all feed messages for group limited to size

//TODO: at some point we need to add a check or clean up for number of items
//that is stored in a feed

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
  $user_id = $v->test_input($_GET['admin_id']);
  $size = $v->test_input($_GET['size']);
}
else
{
 echo "WrongMethod";
 $link->close();
 exit;
}

if(empty($group_name) || empty($user_id))
{
  echo "InputError" . PHP_EOL;
  $link->close();
  exit;
}

$size = intval($size);

include("Lookup/userLookup.php");

$user_id = userLookup($user_id, $link);

if($user_id == false)
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

$group_id = $link->real_escape_string($group_id);
$result = $link->query("SELECT Users.Name AS n, Feed.Report AS r, Feed.Creation AS t
                        FROM Feed
                        INNER JOIN Users ON Feed.User_Id=Users.User_Id
                        WHERE Feed.Group_Id = '$group_id'
                        ORDER BY Feed_Id DESC
                        LIMIT '$size'");

$data = array();

if($result->num_rows > 0)
{
  while($r = $result->fetch_assoc())
  {
    $data[] = $r;
  }
}
else {
  //echo "None";
}

echo json_encode(array('feed' => $data));

$link->close();
?>
