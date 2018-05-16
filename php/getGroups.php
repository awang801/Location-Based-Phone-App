<?php include("Util/validationUtil.php"); ?>
<?php
//Input: id (string)
//Result: json of all groups member/owner

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

$data = array();

$user_id = $link->real_escape_string($user_id);
$result = $link->query("SELECT Groups.Name AS n, Groups.Description AS d, Groups.Img_Id AS img
                        FROM Group_Users
                        INNER JOIN Groups ON Group_Users.Group_Id=Groups.Group_Id
                        WHERE Group_Users.User_Id = '$user_id'
                              AND Group_Users.State = 0");

$member = array();
if($result->num_rows > 0)
{
  while($r = $result->fetch_assoc())
  {
    $member[] = $r;
  }
  //echo json_encode($member);
}
else {
  //echo "no membership";
}

$result2 = $link->query("SELECT Name AS n, Description AS d, Img_Id AS img
                         FROM Groups WHERE User_id = '$user_id'");

$owner = array();
if($result2->num_rows > 0)
{
  while($r = $result2->fetch_assoc())
  {
    $owner[] = $r;
  }
  //echo json_encode($owner);
}
else {
  //echo "no ownership";
}

//Invites are 1 & requests are 2
$result3 = $link->query("SELECT Groups.Name AS n, Groups.Description AS d, Group_Users.GroupUsers_Id AS id, Group_Users.State AS s, Groups.Img_Id AS img
                         FROM Group_Users
                         INNER JOIN Groups
                         ON Group_Users.Group_Id = Groups.Group_Id
                         WHERE Group_Users.User_id = '$user_id'
                         AND Group_Users.State = 1
                         ORDER BY Group_Users.State");

$reqInv = array();

if($result3->num_rows > 0)
{
  while($r = $result3->fetch_assoc())
  {
    $reqInv[] = $r;
  }
}
else {
}

echo json_encode(array('member' => $member, 'owner' => $owner, 'ri' => $reqInv));

$link->close();
?>
