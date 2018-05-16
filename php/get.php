<?php include("Util/validationUtil.php"); ?>
<?php

//Input: name
//Output: "Longitude Latitude" (one space)

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
  $name = $v->test_input($_GET['name']);
}
else
{
	echo "WrongMethod";
	exit;
}

if(empty($name))
{
  echo "InputError";
  exit;
}

//echo "name: " . $name;
$name = $link->real_escape_string($name);
$result = $link->query("SELECT Longitude, Lat FROM Locations WHERE Name ='$name'");

if ($result->num_rows > 0) {
    // output data of each row
    $returnString = "";
    $row = $result->fetch_assoc();
    $returnString = $row["Longitude"] . " " . $row["Lat"];
    echo $returnString;
}
else
{
    echo "NoResults";
}
$link->close();
 ?>
