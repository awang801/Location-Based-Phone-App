<?php include("Util/validationUtil.php"); ?>
<?php
//Input: id (string), group_name (string), type (int)
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
  $login_id = $v->test_input($_POST['id']);
  $type = $v->test_input($_POST['type']);
}
else
{
 echo "WrongMethod";
 $link->close();
 exit;
}

if(empty($group_name) || empty($login_id))
{
  echo "InputError" . PHP_EOL;
  $link->close();
  exit;
}

//NOTE: this returns 0 on failure (i.e. default)
// This would need to be expanded if more than 2 options are present
$type = intval($type);

include("Lookup/userLookup.php");

$user_id = userLookup($login_id, $link);

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

$user_id = $link->real_escape_string($user_id);
$group_id = $link->real_escape_string($group_id);
$result = $link->query("SELECT Group_Users.State AS s, Groups.Vis AS v
                        FROM Group_Users
                        INNER JOIN Groups
                        ON Group_Users.Group_Id = Groups.Group_Id
                        WHERE Group_Users.User_Id = '$user_id' AND Group_Users.Group_Id = '$group_id'");

$visCheck = $link->query("SELECT Vis AS v
                          FROM Groups
                          WHERE Group_Id = '$group_id'");

if($result->num_rows > 1) //There are more than 1 intersections!!
{
  //There is an error in the database
  //TODO: handle this error?
}
else if($result->num_rows > 0) //An intersection already exists
{

  $row = $result->fetch_assoc();

  $state = $row["s"];
  if($state == 0) //Indicates user is already member
  {
    echo "Already a member";
  }
  else if($state == 1) //Indicates user has already been invited
  {
    $result2 = $link->query("UPDATE Group_Users SET Sync = TRUE, State = 0 WHERE User_Id = '$user_id' AND Group_Id = '$group_id'");
    if($result2)
    {
      echo "Invited: invitation accepted";
    }
    else
    {
      echo "Invited: invitation couldn't be accepted";
    }
  }
  else if($state == 2)
  {
    echo "Requested: membership is requested";
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
  //Check if public first though
  $row = $visCheck->fetch_assoc();
  if(intval($row['v']) == 0)
  {
    echo "Private Group";
    $link->close();
    exit;
  }

  if($type == 0)
  {
    echo "No Invitation Present";
  }
  else
  {
    $result3 = $link->query("INSERT INTO Group_Users (User_id, Group_id, State) VALUES ('$user_id', '$group_id', 2)");
    if($result3)
    {
      echo "Requested";
    }
    else
    {
      echo "Failed to Request";
    }
  }
}

$link->close();
?>
