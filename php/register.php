<?php include("Util/validationUtil.php"); ?>
<?php

//Input is: token (string)

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
  $token = $v->test_input($_POST['Token']);
}
else
{
 echo "WrongMethod";
 $link->close();
 exit;
}

if(empty($token))
{
  echo "InputError" . PHP_EOL;
  $link->close();
  exit;
}

$token = $link->real_escape_string($token);
$link->query("CREATE TABLE IF NOT EXISTS users
(
id int(20) NOT NULL AUTO_INCREMENT,
Token varchar(200)
PRIMARY KEY (id)
)");
$result = $link->query("INSERT INTO users (Token) VALUES ('$token')
                        ON DUPLICATE KEY UPDATE Token = '$token'");

if($result)
{
  echo "User Added";
}
else {
  //TODO: It might not be a great idea to pass back the error string with table info
  $errorString = mysqli_error($link);
  echo $errorString;
}

$link->close();
 ?>
