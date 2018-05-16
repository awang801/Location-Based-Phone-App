<?php include("Util/validationUtil.php");?>
<?php include("Util/Report.php");?>
<?php
//Input: admin_id (string), group_name (string)
//Result: Start Attendence by saving timestamp and request loc updates

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

date_default_timezone_set("UTC");
$datetime = date('Y-m-d H:i:s', time());
$unixTime = time();

$result = $link->query("SELECT Att_Type, Att_Period, Att_Duration, Att_Start FROM Groups
                        WHERE Group_Id = '$group_id'
                        AND Att_Stop >= '$datetime'");
if($result->num_rows == 0)
{
  echo "No Event";
  $link->close();
  exit;
}

$r = $result->fetch_assoc();
$type = intval($r['Att_Type']);
$period = intval($r['Att_Period']);
$duration = intval($r['Att_Duration']);
echo "string start: " . $r['Att_Start'] . " ";
$start = strtotime($r['Att_Start']);

$count = 0;
$currentStart = $start;

echo " time: " . $unixTime . " ";
echo " currentStart: " . $currentStart . " | ";

$delay = $period * 60;
while($unixTime > $currentStart + $delay && $count < $duration)
{
  $currentStart = $currentStart + $delay;
  echo $currentStart . " ";
  $count = $count + 1;
}

echo Report::getJSON($admin_id, $group_id, $link, date('Y-m-d H:i:s', $currentStart));

$link->close();
exit;
?>
