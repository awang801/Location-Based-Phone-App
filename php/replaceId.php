<?php include("Util/validationUtil.php"); ?>
<?php include("Util/Messaging.php");?>
<?php

//Input is: id, instance

$v = new Validation();
if($v->isSecure($_SERVER['https'], $_SERVER['SERVER_PORT']) == 0)
{
  echo "NotSecure";
  exit;
}

if ($_SERVER["REQUEST_METHOD"] == "POST") {
  $user_id = $v->test_input($_POST['id']);
  $instance = $v->test_input($_POST['instance']);
}
else
{
 echo "WrongMethod";
 exit;
}

if(empty($user_id) || empty($instance))
{
  echo "InputError" . PHP_EOL;
  exit;
}

include("Util/connect.php");
$link = connect();

if($link == false)
{
  echo "Connection Failed";
  exit;
}


//Must use firebaseId for Lookup
include("Lookup/userLookup.php");

$user_id = userLookup($user_id, $link);
if($user_id == false)
{
  echo "User Not Available";
  $link->close();
  exit;
}

$instance = $link->real_escape_string($instance);
$user_id = $link->real_escape_string($user_id);
$dupeCheck = $link->query("SELECT Instance_Id FROM User_Device
                          WHERE User_Id = '$user_id'");
if($dupeCheck->num_rows > 0)
{
  $r = $dupeCheck->fetch_assoc();
  $currentInst = $r["Instance_Id"];
  if($currentInst == $instance)
  {
    echo "Success";
  }
  else {
    $m = new Messaging();

    //TODO: This code runs under assumption 1 user_device per user!!
    //TODO: haven't actually tested this on multiple devices
    $curInstance = $m->getInstanceForUser($user_id,$link);
    $message = array("clean" => "Ignore");
    $messageResult = $m->sendNotification($curInstance,$message);

    //echo $messageResult;
    if($m->refreshTokenIfNeeded($messageResult,$instance, $user_id,$link) == TRUE)
    {
      echo "Success";
    }
    else
    {
      echo "Failed";
    }
  }
}

$link->close();
exit;
?>
