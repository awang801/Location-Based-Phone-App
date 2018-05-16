<?php include("Util/validationUtil.php"); ?>
<?php
//user_id, size (int)
//Result: json of all feed messages for group limited to size

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
  $user_id = $v->test_input($_GET['user_id']);
  $size = $v->test_input($_GET['size']);
}
else
{
 echo "WrongMethod";
 $link->close();
 exit;
}

if(empty($user_id))
{
  echo "InputError" . PHP_EOL;
  $link->close();
  exit;
}

$size = intval($size);

include("Lookup/userLookup.php");

$user_id = userLookup($user_id, $link);

if($user_id == false)
{
  echo "Lookup Failed - user";
  $link->close();
  exit;
}


$user_id = $link->real_escape_string($user_id);
$result = $link->query("SELECT Users.Name AS n, Feed.Report AS r, Feed.Creation AS t, Groups.Name AS gn, Feed.Feed_Id AS id
                        FROM Group_Users
                        INNER JOIN Feed
                        ON Group_Users.Group_Id = Feed.Group_Id
                        INNER JOIN Users
                        ON Feed.User_Id = Users.User_Id
                        INNER JOIN Groups
                        ON Group_Users.Group_Id= Groups.Group_Id
                        WHERE Group_Users.User_Id = '$user_id'
                        AND Group_Users.State = 0
                        ORDER BY Feed.Creation DESC");

$data = array();

if($result->num_rows > 0)
{
  $count = 0;
  while(($r = $result->fetch_assoc()) && ($count < $size))
  {
    $timeInt = strtotime($r['t']);
    if($timeInt == false)
    {
      $timeInt = 0;
    }
    $r['t'] = $timeInt;

    $data[] = $r;
    $count = $count + 1;
  }
}

echo json_encode(array('feed' => $data));

$link->close();
?>
