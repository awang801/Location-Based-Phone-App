<?php
  //TODO: Remove this from project
  function groupLookup($name, $admin_id, $link)
  {
    $name = $link->real_escape_string($name);
    $admin_id = $link->real_escape_string($admin_id);
    $result = $link->query("SELECT Group_Id AS id FROM Groups
                            WHERE Name = '$name' AND User_Id = '$admin_id'");

    if ($result->num_rows > 0) {
        // output data of each row
        $row = $result->fetch_assoc();
        $group_id = $row["id"];
        return $group_id;
    }
    else
    {
        return false;
    }
  }
?>
