<?php include("Util/validationUtil.php"); ?>
<?php
//Input: id (string) syncAll (int)
//Result: json of all groups needed to sync

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
  $sync_all = $v->test_input($_GET['syncAll']);
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

//NOTE: Defaults to 0
$sync_all = intval($sync_all);

include("Lookup/userLookup.php");

$user_id = userLookup($login_id, $link);

if($user_id == false)
{
  echo "Lookup Failed - user";
  $link->close();
  exit;
}


$query = "SELECT Groups.Name AS n,
                         Groups.Att_Period AS p,
                         Groups.Att_Duration AS d,
                         Groups.Att_Start AS s,
                         Groups.Att_Stop AS t,
                         Groups.Lng AS lng,
                         Groups.Lat AS lat,
                         Groups.radius AS r
                         FROM Group_Users
                         INNER JOIN Groups ON Group_Users.Group_Id=Groups.Group_Id
                         WHERE Group_Users.User_id = '$user_id'
                         AND State = 0";
if($sync_all == 0)
{
  $query = $query . " AND Sync = TRUE";
}

$result = $link->query($query);

date_default_timezone_set("UTC");
$datetime = date('Y-m-d H:i:s', time());

$diff = array();
if($result->num_rows > 0)
{
  while($r = $result->fetch_assoc())
  {
    $startInt = strtotime($r['s']);
    if($startInt == false)
    {
      $startInt = 0;
    }
    $stopInt = strtotime($r['t']);
    if($stopInt == false)
    {
      $stopInt = 0;
    }
    $r['s'] = $startInt;
    $r['t'] = $stopInt;
    $diff[] = $r;
  }
}
else {
  //echo "None";
}

$data = array('datetime' => $datetime, 'groups' => $diff);
echo json_encode($data, JSON_NUMERIC_CHECK);

$link->close();
?>
