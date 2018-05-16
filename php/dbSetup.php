<?php

include("Util/connect.php");
$link = connect();

if($link == false)
{
  echo "Connection Failed";
  exit;
}

if ($_SERVER["REQUEST_METHOD"] != "GET")
{
  echo "WrongMethod";
  exit;
}

//NOTE: login_id is currently UNIQUE, however as we add more login apis
// We may need to change this and begin using the Acc_type during lookups
$link->query("CREATE TABLE IF NOT EXISTS Users
(
User_Id BIGINT NOT NULL AUTO_INCREMENT,
Email varchar(255) NOT NULL,
Name varchar(255) NOT NULL,
Privacy TINYINT DEFAULT 0 NOT NULL,
Notify TINYINT DEFAULT 0 NOT NULL,
Login_Id varchar(255) UNIQUE,
PRIMARY KEY (User_Id)
)");

//NOTE: Though called User_Id in reference to User table it is actually admin
//Vis = 1 is public searchable and can be Requested
//Vis = 0 is not public searchable and request will be tossed if made
$link->query("CREATE TABLE IF NOT EXISTS Groups
(
Group_Id BIGINT NOT NULL AUTO_INCREMENT,
User_Id BIGINT NOT NULL,
Img_Id BIGINT NOT NULL,
Name varchar(63) NOT NULL UNIQUE,
Description varchar(255),
Att_Type TINYINT DEFAULT 0 NOT NULL,
Att_Period INT DEFAULT 5,
Att_Duration INT DEFAULT 60,
Att_Vis TINYINT DEFAULT 0 NOT NULL,
Att_Start DATETIME DEFAULT 0 NOT NULL,
Att_Stop DATETIME DEFAULT 0 NOT NULL,
Vis TINYINT DEFAULT 0 NOT NULL,
Feed_Rights TINYINT DEFAULT 0 NOT NULL,
Lng varchar(255) DEFAULT '0.0' NOT NULL,
Lat varchar(255) DEFAULT '0.0' NOT NULL,
radius INT DEFAULT 0 NOT NULL,
PRIMARY KEY (Group_Id),
FOREIGN KEY (User_Id) REFERENCES Users(User_Id)
)");

$link->query("CREATE TABLE IF NOT EXISTS Att_Reports
(
Att_Id BIGINT NOT NULL AUTO_INCREMENT,
Run_Id BIGINT NOT NULL,
Creation DATETIME NOT NULL,
Report LONGTEXT NOT NULL,
PRIMARY KEY (Att_Id),
FOREIGN KEY (Run_Id) REFERENCES Groups(Att_Runs)
)");

$link->query("CREATE TABLE IF NOT EXISTS Att_Runs
(
Att_Run_Id BIGINT NOT NULL AUTO_INCREMENT,
Group_Id BIGINT NOT NULL,
Period INT DEFAULT 0,
Duration INT DEFAULT 0,
Start DATETIME DEFAULT 0 NOT NULL,
Lng varchar(255) DEFAULT '0.0' NOT NULL,
Lat varchar(255) DEFAULT '0.0' NOT NULL,
radius INT DEFAULT 0 NOT NULL,
PRIMARY KEY (Att_Run_Id),
FOREIGN KEY (Group_Id) REFERENCES Groups(Group_Id)
)");

$link->query("CREATE TABLE IF NOT EXISTS Feed
(
Feed_Id BIGINT NOT NULL AUTO_INCREMENT,
Group_Id BIGINT NOT NULL,
User_Id BIGINT NOT NULL,
Creation DATETIME NOT NULL,
Report LONGTEXT NOT NULL,
PRIMARY KEY (Feed_Id),
FOREIGN KEY (Group_Id) REFERENCES Groups(Group_Id),
FOREIGN KEY (User_Id) REFERENCES Users(User_Id)
)");

//NOTE: State is as follows
// 0 = member
// 1 = invited
// 2 = requesting

//NOTE: The SQL here can allow there to be multiple entries with same u_id and g_id
// Our system should not allow this. There should be an occasional check for this leak

$link->query("CREATE TABLE IF NOT EXISTS Group_Users
(
GroupUsers_Id BIGINT NOT NULL AUTO_INCREMENT,
User_Id BIGINT NOT NULL,
Group_Id BIGINT NOT NULL,
State TINYINT NOT NULL,
Sync TINYINT(1) DEFAULT false NOT NULL,
Sync_Change DATETIME,
Lng varchar(255),
Lat varchar(255),
Last_Update DATETIME NOT NULL DEFAULT '0000-00-00',
PRIMARY KEY (GroupUsers_Id),
FOREIGN KEY (User_Id) REFERENCES Users(User_Id),
FOREIGN KEY (Group_Id) REFERENCES Groups(Group_Id)
)");

$link->query("CREATE TABLE IF NOT EXISTS User_Device
(
UserDevice_Id BIGINT NOT NULL AUTO_INCREMENT,
User_Id BIGINT NOT NULL UNIQUE,
Instance_Id varchar(200),
PRIMARY KEY (UserDevice_ID),
FOREIGN KEY (User_Id) REFERENCES Users(User_Id)
)");

$link->close();

echo "Setup Complete";
 ?>
