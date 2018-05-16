<?php include("Util/validationUtil.php"); ?>
<?php
//Input: group_name (string), admin_id, size (int), offset (int)
//Result: json of size most recent reports starting offset from the most recent

//TODO: at some point we need to add a check or clean up for number of items
//that is stored in a feed

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
  $group_name = $v->test_input($_GET['group_name']);
  $admin_id = $v->test_input($_GET['admin_id']);
  $size = $v->test_input($_GET['size']);
  $offset = $v->test_input($_GET['offset']);
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

$size = intval($size);
$offset = intval($offset);

include("Lookup/userLookup.php");

$admin_id = userLookup($admin_id, $link);

if($admin_id == false)
{
  echo "Lookup Failed - user";
  $link->close();
  exit;
}

include("Lookup/groupLookup.php");

$group_id = groupLookup($group_name, $link);

if($group_id == false)
{
  echo "Lookup Failed - group";
  $link->close();
  exit;
}

include("Lookup/isAdmin.php");
$is_admin = isAdmin($group_id, $admin_id, $link);

if(!$is_admin)
{
  echo "Lookup Failed - Not Admin";
  $link->close();
  exit;
}

$group_id = $link->real_escape_string($group_id);
$resultRuns = $link->query("SELECT Att_Runs.Att_Run_Id AS r, Att_Runs.Period AS p, Att_Runs.Duration AS d, Att_Runs.Start AS s, Att_Runs.Lng AS lng, Att_Runs.Lat AS lat, Att_Runs.radius AS rad
                        FROM Att_Runs
                        WHERE Att_Runs.Group_Id = '$group_id'
                        ORDER BY Att_Run_Id DESC
                        LIMIT $size
                        OFFSET $offset");
$data = array();
if($resultRuns->num_rows > 0)
{
  while($r = $resultRuns->fetch_assoc())
  {
    $curRun = intval($r['r']);
    $result = $link->query("SELECT Att_Reports.Report AS r, Att_Reports.Creation AS t
                            FROM Att_Reports
                            WHERE Att_Reports.Run_Id = '$curRun'
                            ORDER BY UNIX_TIMESTAMP(Att_Reports.Creation) DESC");
    $tempData = array();
    if($result->num_rows > 0)
    {
      while($tempR = $result->fetch_assoc())
      {
        $tempData[] = array('locations' => json_decode($tempR['r'],TRUE)['locations'],'missing' => json_decode($tempR['r'],TRUE)['missing'], 'time' => $tempR['t']);
      }
    }
    $data[] = array( 'start' => $r['s'], 'period' => $r['p'], 'duration' => $r['d'], 'longitude' => $r['lng'], 'latitude' => $r['lat'], 'radius' => $r['rad'], 'results' => $tempData);
  }
}

echo json_encode(array('runs' => $data));

$link->close();
?>
