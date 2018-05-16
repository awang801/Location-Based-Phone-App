<?php include("Util/validationUtil.php"); ?>
<?php include("Util/Messaging.php");?>
<?php

//Input is: acc_id (JWT Token), instance

$v = new Validation();
if($v->isSecure($_SERVER['https'], $_SERVER['SERVER_PORT']) == 0)
{
  echo "9";
  exit;
}

if ($_SERVER["REQUEST_METHOD"] == "POST") {
  $instance = $v->test_input($_POST['instance']);
  $acc_id = $v->test_input($_POST['acc_id']);
}
else
{
 echo "8";
 exit;
}

if(empty($acc_id) || empty($instance))
{
  echo "7";
  exit;
}

include("Verification/googleVer.php");
$googleJsonString = googleVer();
$keys = json_decode($googleJsonString, TRUE);

include("JWTDecode.php");
$decoded = decode($acc_id, $keys, array('RS256'));
if($decoded == NULL)
{
  //echo "Token Verification Error";
  //NOTE: If this error persists you may use the prints in JWTDecode
  echo "6";
  exit;
}

$array = (array)$decoded;
$user_firebase_id = $array["user_id"];
$name = $array["name"];
$email = $array["email"];

if(empty($user_firebase_id) || empty($name) || empty($email))
{
  //echo "UID/Info Missing";
  echo "5";
  exit;
}

include("Util/connect.php");
$link = connect();

if($link == false)
{
  echo "4";
  exit;
}

$user_firebase_id = $link->real_escape_string($user_firebase_id);
$instance = $link->real_escape_string($instance);
$name = $link->real_escape_string($name);
$email = $link->real_escape_string($email);

$result = $link->query("INSERT INTO Users (Email, Name, Login_Id) VALUES ('$email', '$name', '$user_firebase_id')");
$errorString1 = mysqli_error($link);

include("Lookup/userLookup.php");

$user_id = userLookup($user_firebase_id, $link);
if($user_id == false)
{
  //horrible failure!
  //if this fail we might be stuck in bad state
  echo "3";
  $link->close();
  exit;
}


$dupeCheck = $link->query("SELECT Instance_Id FROM User_Device
                          WHERE User_Id = '$user_id'");
if($dupeCheck->num_rows > 0)
{
  $r = $dupeCheck->fetch_assoc();
  $currentInst = $r["Instance_Id"];
  if($currentInst == $instance)
  {
    //echo "Failed (Same Device)";
    echo "1";
  }
  else {
    $m = new Messaging();

    //TODO: This code runs under assumption 1 user_device per user!!
    //NOTE: This function gets instance again, but wraps it up special for firebase json
    $curInstance = $m->getInstanceForUser($user_id,$link);
    $message = array("clean" => "Ignore");
    $messageResult = $m->sendNotification($curInstance,$message);
    if($m->refreshTokenIfNeeded($messageResult,$instance, $user_id,$link) == TRUE)
    {
      //echo "Replaced out of date id";
    }
    else
    {
      //echo "Failed (General)";
      echo "2";
    }
  }
  $link->close();
  exit;
}

$result2 = $link->query("INSERT INTO User_Device (User_Id, Instance_Id) VALUES ('$user_id', '$instance')");
$errorString2 = mysqli_error($link);

if($result && $result2)
{
  //echo "User Added";
}
else {
  //echo $errorString1 . " , " . $errorString2;
}

echo "-Complete-";
$link->close();
 ?>
