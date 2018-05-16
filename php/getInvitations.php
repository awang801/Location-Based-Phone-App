<?php include("Util/validationUtil.php"); ?>
<?php
//Input: id (string)
//Result: json of all groups with open invitations

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
  $login_id = $v->test_input($_GET['id']);
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
$result = $link->query("SELECT Groups.Name AS n FROM Group_Users
                        INNER JOIN Groups ON Group_Users.Group_Id=Groups.Group_ID
                        WHERE Group_Users.User_Id = '$user_id'
                              AND Group_Users.State = 1");

$data = array();
if ($result->num_rows > 0)
{
  while($r = $result->fetch_assoc())
  {
    $data[] = $r['n'];
  }
}
else
{
    //echo "NoResults";
}
echo json_encode(array('invites' => $data));

$link->close();
?>
