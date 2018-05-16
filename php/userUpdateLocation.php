<?php include("Util/validationUtil.php"); ?>
<?php include("Util/Messaging.php");?>
<?php
//Input: id (string), lat (string), long (string), groups (json)
//Result: add new location to db

function getDistance( $latitude1, $longitude1, $latitude2, $longitude2 ) {
    $earth_radius = 6371000;

    $dLat = deg2rad( $latitude2 - $latitude1 );
    $dLon = deg2rad( $longitude2 - $longitude1 );

    $a = sin($dLat/2) * sin($dLat/2) + cos(deg2rad($latitude1)) * cos(deg2rad($latitude2)) * sin($dLon/2) * sin($dLon/2);
    $c = 2 * asin(sqrt($a));
    $d = $earth_radius * $c;

    return $d;
}

function sendMessage($id,$link,$group_names)
{
  $instances = Messaging::getInstanceForUser($id,$link);
  $message = array("out" => "$group_names");
  $messageResult = Messaging::sendNotification($instances,$message);
  //echo $messageResult;
}

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
  $login_id = $v->test_input($_POST['id']);
  $lat = $v->test_input($_POST['lat']);
  $long = $v->test_input($_POST['long']);
  $groups = $_POST['groups'];
}
else
{
 echo "WrongMethod";
 $link->close();
 exit;
}

if(empty($login_id) || empty($lat) || empty($long) || empty($groups))
{
  echo "InputError" . PHP_EOL;
  $link->close();
  exit;
}

$latf = floatval($lat);
$longf = floatval($long);

include("Lookup/userLookup.php");

$user_id = userLookup($login_id, $link);

if($user_id == false)
{
  echo "Lookup Failed - user";
  $link->close();
  exit;
}

//TODO: fail gracefully on error here??
$jsonGroups = json_decode($groups, TRUE);

date_default_timezone_set("UTC");
$datetime = date('Y-m-d H:i:s', time());

$user_id = $link->real_escape_string($user_id);
$lang = $link->real_escape_string($lang);
$lat = $link->real_escape_string($lat);

include("Lookup/groupLookup.php");

$outOfRangeGroups = array();

foreach ($jsonGroups as $key => $value) {
  //echo $value["name"]."    ";
  $curName = $link->real_escape_string($value["name"]);

  $group_id = groupLookup($curName, $link);

  $select = $link->query("SELECT Lng, Lat, radius, Name FROM Groups
                          WHERE Group_Id = '$group_id'");
  //echo "nr: ".$select->num_rows;
  if($select->num_rows > 0)
  {
    $r = $select->fetch_assoc();
    $groupLat = floatval($r["Lat"]);
    $groupLng = floatval($r["Lng"]);
    $groupRad = floatval($r["radius"].".0");
    //echo "lat: ".$groupLat." long: ".$groupLng." rad: ".$groupRad;

    $dist = getDistance($groupLat, $groupLng, $latf, $longf);
    //echo " dist: ".$dist;
    if($dist >= $groupRad)
    {
      $outOfRangeGroups[] = array("name" => $r["Name"]);
    }
  }

  $result = $link->query("UPDATE Group_Users
                          Set Lng = '$long', Lat = '$lat', Last_Update = '$datetime'
                          WHERE User_Id = '$user_id'
                          AND State = 0
                          AND Group_Id = '$group_id'");
}

if(sizeof($outOfRangeGroups) > 0)
{
  //echo json_encode($outOfRangeGroups);
  sendMessage($user_id, $link, json_encode($outOfRangeGroups));
}

//NOTE: others may fail but if there is serious issue this should grab it
if($result)
{
  echo "Success";
}
else
{
  //$errorString = mysqli_error($link);
  echo "Failure";
}

$link->close();
?>
