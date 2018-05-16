<?php

  //updates user setting $setting to value $type
  function userSettingUpdate($user_id, $type, $link, $setting)
  {
    include("Lookup/userLookup.php");

    $user_id = userLookup($user_id, $link);

    if($user_id == false)
    {
      return "Lookup Failed - user";
    }

    //Whitelist column types
    $col = "";
    if($setting == 1)
    {
      $col = "Privacy";
    }
    else if($setting == 2)
    {
      $col = "Notify";
    }
    else
    {
      return "Unauthorized Query";
    }

    $type = $link->real_escape_string($type);
    $col = $link->real_escape_string($col);
    $result = $link->query("UPDATE Users SET $col = '$type' WHERE User_Id = '$user_id'");

    if($result)
    {
      return "Success";
    }
    else {
      $errorString = mysqli_error($link);
      return $errorString;
    }
  }
?>
