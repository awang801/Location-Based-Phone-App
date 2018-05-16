<?php include("Util/validationUtil.php"); ?>
<?php
//Input: admin_id (string), group_name (string), user_name (string), type (int)
//Result: Request admittance to group

//NOTE: type refers to how this script handles no invitations
//Default = 0 : error message is returned
// Anything else : automatically creates a request

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
  $admin_id = $v->test_input($_POST['admin_id']);
  $user_name = $v->test_input($_POST['user_name']);
  $type = $v->test_input($_POST['type']);
}
else
{
 echo "WrongMethod";
 $link->close();
 exit;
}

if(empty($group_name) || empty($admin_id) || empty($user_name))
{
  echo "InputError" . PHP_EOL;
  $link->close();
  exit;
}

//NOTE: this returns 0 on failure (i.e. default)
// This would need to be expanded if more than 2 options are present
$type = intval($type);

include("Lookup/userLookup.php");

$admin_id = userLookup($admin_id, $link);

if($admin_id == false)
{
  echo "Lookup Failed - admin";
  $link->close();
  exit;
}

include("Lookup/userNameLookup.php");

$user_id = userNameLookup($user_name, $link);

if($user_id == false)
{
  echo "Lookup Failed - user name";
  $link->close();
  exit;
}

include("Lookup/groupLookupAuth.php");

$group_id = groupLookup($group_name, $admin_id, $link);

if($group_id == false)
{
  //NOTE: since this lookup requires admin privleges, this could mean
  //either group doesn't exsist or this user is not the admin
  echo "Lookup Failed - group";
  $link->close();
  exit;
}

$user_id = $link->real_escape_string($user_id);
$group_id = $link->real_escape_string($group_id);
$result = $link->query("SELECT State AS s FROM Group_Users WHERE User_Id = '$user_id' AND Group_Id = '$group_id'");

if($result->num_rows > 1) //There are more than 1 intersections!!
{
  //There is an error in the database
  //TODO: handle this error with a delete?
}
if($result->num_rows > 0)
{
  $row = $result->fetch_assoc();
  $state = $row["s"];

  if($state == 0) //Indicates user is already member
  {
    echo "Member";
  }
  else if($state == 1) //Indicates user has already been invited
  {
    echo "Invitation: already invited";
  }
  else if($state == 2)
  {
    $result2 = $link->query("UPDATE Group_Users SET Sync = TRUE, State = 0 WHERE User_Id = '$user_id' AND Group_Id = '$group_id'");
    if($result2)
    {
      echo "Requested: Added as member";
    }
    else
    {
      echo "Requested: Couldn't be added as member";
    }
  }
  else //if the state value is unknown, table may be corrupt or this wasn't updated
  {
    echo "Unknown Result";
  }
  $link->close();
  exit;
}
else //No intersections, insert a new one :)
{
  if($type == 0)
  {
    echo "No Invitation Present";
  }
  else
  {
    $result3 = $link->query("INSERT INTO Group_Users (User_id, Group_id, State) VALUES ('$user_id', '$group_id', 1)");
    if($result3)
    {
      echo "Invited";
    }
    else
    {
      echo "Failed to Invite";
    }
  }
}

$link->close();
?>
