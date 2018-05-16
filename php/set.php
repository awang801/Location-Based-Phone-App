<?php include("Util/validationUtil.php"); ?>
<?php

//Input is: name (string), long (string), lat (string)
//These are all used to set value of names record

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
  $long = $v->test_input($_POST['long']);
  $lat = $v->test_input($_POST['lat']);
}
else
{
 echo "WrongMethod";
 $link->close();
 exit;
}

if(empty($name) || empty($long) || empty($lat))
{
  echo "InputError" . PHP_EOL;
  exit;
}

//$sql = "SELECT Longitude, Lat FROM Locations WHERE Name='$name'";
//$sql = "UPDATE Locations SET Longitude, Lat WHERE Name='$name'";

$long = $link->real_escape_string($long);
$lat = $link->real_escape_string($lat);
$name = $link->real_escape_string($name);
$result = $link->query("UPDATE Locations SET Longitude='$long', Lat='$lat' WHERE Name='$name'");

if($result)
{
  echo "Worked";
}
else {
  echo "Failed";
}
$link->close();
 ?>
