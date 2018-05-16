<?php include("Util/validationUtil.php"); ?>
<?php
//Input: name (string), desc (string), id (string), type (int->), period, duration, vis, rights, img, avis
//result: add new group entry with id as admin

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
  $name = $v->test_input($_POST['name']);
  $login_id = $v->test_input($_POST['id']);
  $type = $v->test_input($_POST['type']);
  $period = $v->test_input($_POST['period']);
  $duration = $v->test_input($_POST['duration']);
  $vis = $v->test_input($_POST['vis']);
  $rights = $v->test_input($_POST['rights']);
  $desc = $v->test_input($_POST['desc']);
  $img = $v->test_input($_POST['img']);
  $attVis = $v->test_input($_POST['avis']);
}
else
{
 echo "WrongMethod";
 $link->close();
 exit;
}


if(empty($name) || empty($login_id) || empty($desc))
{
  echo "InputError" . PHP_EOL;
  $link->close();
  exit;
}

$type = intval($type);
$period = intval($period);
$duration = intval($duration);
$vis = intval($vis);
$rights = intval($rights);
$img = intval($img);
$attVis = intval($attVis);


//TODO: NOTE:Manually set defaults, this don't even line up with db defaults lol
if($period == 0 || $period > 60)
{
  $period = 5;
}

if($duration == 0 || $duration > 100)
{
  $duration = 3;
}

if(strlen($name) > 63)
{
  echo "name is too long";
  $link->close();
  exit;
}

if(strlen($desc) > 255)
{
  echo "description is too long";
  $link->close();
  exit;
}


include("Lookup/userLookup.php");

$admin_id = userLookup($login_id, $link);

if($admin_id == false)
{
  echo "Lookup Failed";
  $link->close();
  exit;
}


$admin_id = $link->real_escape_string($admin_id);
$desc = $link->real_escape_string($desc);
$name = $link->real_escape_string($name);

$result = $link->query("INSERT INTO Groups (User_id, Name, Description, Att_Type, Att_Period, Att_Duration, Att_Vis, Vis, Feed_Rights, Img_Id) VALUES ('$admin_id', '$name', '$desc', '$type', '$period', '$duration', '$attVis', '$vis', '$rights', '$img')");

if($result)
{
  echo "Complete";
}
else {
  //$errorString = mysqli_error($link);
  //echo $errorString;
  echo "Backend Error";
}

$link->close();
 ?>
