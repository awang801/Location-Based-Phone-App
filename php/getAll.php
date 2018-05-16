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


if ($_SERVER["REQUEST_METHOD"] != "GET") {
 echo "WrongMethod";
 $link->close();
 exit;
}

//$sql = "SELECT Longitude, Lat FROM Locations WHERE Name='$name'";
//$sql = "UPDATE Locations SET Longitude, Lat WHERE Name='$name'";

$result = $link->query("SELECT Name AS n, Longitude AS lg, Lat AS lt FROM Locations");
$rows = array();

if ($result->num_rows > 0) {
  while($r = $result->fetch_assoc()) {
    $rows[] = $r;
  }

  echo json_encode($rows);
}
else
{
    echo "NoResults";
}
$link->close();
 ?>
