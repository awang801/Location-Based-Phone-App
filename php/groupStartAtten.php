<?php include("Util/validationUtil.php");?>
<?php include("Util/Messaging.php");?>
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

//Get important attendence taking information about group
$result = $link->query("SELECT Att_Type, Att_Period, Att_Duration, Att_Start, Lng, Lat, radius FROM Groups
                        WHERE Group_Id = '$group_id'");
$r = $result->fetch_assoc();
$type = intval($r['Att_Type']);
$period = intval($r['Att_Period']);
$duration = intval($r['Att_Duration']);
$lng = $r['Lng'];
$lat = $r['Lat'];
$radius = intval($r['radius']);

//TODO: Here we should have some serious clean up of possible values for period
// and duration!!
if($type == 0)
{
  $duration = 1;
}

$group_id = $link->real_escape_string($group_id);
date_default_timezone_set("UTC");
$datetime = time();
$start = date('Y-m-d H:i:s',$datetime);
$stop = date('Y-m-d H:i:s',$datetime + $duration*$period*60);

$link->query("UPDATE Groups SET Att_Start = '$start', Att_Stop = '$stop'
                         WHERE Group_Id = '$group_id' AND Att_Stop < '$start'");
$isNotTakingAtten = mysqli_affected_rows($link);

if($isNotTakingAtten > 0)
{
  //Set affected users to sync
  $link->query("UPDATE Group_Users SET Group_Users.Sync = TRUE, Group_Users.Sync_Change = '$start'
                           WHERE Group_Users.Group_Id = '$group_id'
                           AND State = 0");

  $instances = Messaging::getInstancesFromGroup($group_id,$link);
  $message = array("sync" => " Sync RIGHT NOW");
  $messageResult = Messaging::sendNotification($instances,$message);
  //echo $messageResult;
  //TODO: Clean out the message Result for bad stuff, maybe at end??

  $sql = "INSERT INTO Att_Runs (Group_Id, Period, Duration, Start, Lng, Lat, radius)
          VALUES ('$group_id', '$period', '$duration', '$start', '$lng', '$lat', '$radius')";

  //If this doesn't get set then it will be garbage (error in sql most likely)
  $last_id = -1;
  if ($link->query($sql) === TRUE) {
      $last_id = $link->insert_id;
      //echo "last id: ".$last_id;
  } else {
      echo "Error: " . $sql . "<br>" . $conn->error;
      $link->close();
      exit;
  }

}
else
{
  //NOTE: This could be missing group or already taking
  echo "Failed to Start";
  $link->close();
  exit;
}

$delay = $period * 60;
$start = $datetime;
for($i = 1; $i <= $duration; $i++)
{
  time_sleep_until($start + $delay);
  //NOTE: addition of delay is a little silly, but it is critical That
  //time passed to genReport is the beginning of interval
  //it checks if an update was made to location in last period
  Report::insertAndMessage($admin_id, $group_id, $link, date('Y-m-d H:i:s', $start), $last_id, $group_name);
  $start += $delay;
}

echo "Success";
$link->close();
exit;

?>
