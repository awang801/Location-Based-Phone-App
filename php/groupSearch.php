<?php include("Util/validationUtil.php"); ?>
<?php
//Input: search (string), limit
//Result: us like to find groups

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
  $search = $v->test_input($_GET['search']);
  $limit = $v->test_input($_GET['limit']);
}
else
{
 echo "WrongMethod";
 $link->close();
 exit;
}

if(empty($search))
{
  echo "InputError" . PHP_EOL;
  $link->close();
  exit;
}
$limit = intval($limit);

//NOTE: This is stupid complicated and should probably be two lines
//It takes each white space seperated string in search and replaces it with |
//Wrapping whole thing with ()+ so any terms can be repeated in query below
$search = '('.preg_replace('#' . preg_quote('/\s+/','#') . '#', '|',trim($search)).')+';

$search = $link->real_escape_string($search);
$result = $link->query("SELECT Groups.Name AS n
                        FROM Groups
                        WHERE Groups.Name RLIKE '$search'
                        AND Groups.Vis = 1");

$member = array();
if($result->num_rows > 0)
{
  while($r = $result->fetch_assoc())
  {
    $member[] = $r['n'];
  }
  //echo json_encode($member);
}
else {
  //echo "NONE";
}

echo json_encode(array('groups' => $member));

$link->close();
?>
