<?php include("Util/validationUtil.php"); ?>
<?php
//Input: user_id (string), group_name (string), text (string)
//Result: Insert a new feed message

//TODO: at some point we need to put a limit on size! I will just cut it ocilogoff
// on my end

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
  $group_name = $v->test_input($_POST['group_name']);
  $user_id = $v->test_input($_POST['user_id']);
  $text = $v->test_input($_POST['text']);
}
else
{
 echo "WrongMethod";
 $link->close();
 exit;
}

if(empty($group_name) || empty($user_id) || empty($text))
{
  echo "InputError" . PHP_EOL;
  $link->close();
  exit;
}

include("Lookup/userLookup.php");

$user_id = userLookup($user_id, $link);

if($user_id == false)
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
$is_admin = isAdmin($group_id, $user_id, $link);

if(!$is_admin)
{
  include("Lookup/isMember.php");
  include("Lookup/isFeedPub.php");

  $is_member = isMember($group_id, $user_id, $link);
  $is_pub = isFeedPub($group_id, $link);
  if(!$is_member || !$is_pub)
  {
    echo "Lookup Failed - Admin / User with privleges";
    $link->close();
    exit;
  }
}

$user_id = $link->real_escape_string($user_id);
$group_id = $link->real_escape_string($group_id);
$text = $link->real_escape_string($text);
date_default_timezone_set("UTC");
$datetime = date('Y-m-d H:i:s', time());
$result = $link->query("INSERT INTO Feed (Group_Id, Creation, Report, User_Id)
                        VALUES ('$group_id', '$datetime', '$text', '$user_id')");

if($result)
{
  echo "Success";
}
else
{
  $errorString = mysqli_error($link);
  echo $errorString;
}

$link->close();
?>
