<?php
  //updates group setting $setting to value $type
  function groupSettingUpdate($group_name, $admin_id, $type, $link, $setting)
  {
    include("Lookup/userLookup.php");

    $admin_id = userLookup($admin_id, $link);

    if($admin_id == false)
    {
      return "Lookup Failed - admin";
    }

    include("Lookup/groupLookupAuth.php");

    $group_id = groupLookup($group_name, $admin_id, $link);

    if($group_id == false)
    {
      //NOTE: since this lookup requires admin privleges, this could mean
      //either group doesn't exsist or this user is not the admin
      return "Lookup Failed - group";
    }

    //Whitelist column types
    $col = "";
    else if($setting == 1)
    {
      $col = "Feed_Rights";
    }
    else if($setting == 2)
    {
      $col = "Att_Period";
    }
    else if($setting == 3)
    {
      $col = "Att_Duration";
    }
    else if($setting == 4)
    {
      $col = "Att_Type";
    }
    else if($setting == 5)
    {
      $col = "Att_Vis";
    }
    else if($setting == 6)
    {
      $col = "Att_Start";
    }
    else if($setting == 7)
    {
      $col = "Att_Stop";
    }
    else if($setting == 8)
    {
      $col = "Vis";
    }
    else if($setting == 9)
    {
      $col = "Lng";
    }
    else if($setting == 10)
    {
      $col = "Lat";
    }
    else if($setting == 11)
    {
      $col = "radius";
    }
    else
    {
      return "Unauthorized Query";
    }

    $type = $link->real_escape_string($type);
    $col = $link->real_escape_string($col);
    $result = $link->query("UPDATE Groups SET $col = '$type' WHERE Group_Id = '$group_id'");

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
