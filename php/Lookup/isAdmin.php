<?php
  function isAdmin($group_id, $admin_id, $link)
  {
    $group_id = $link->real_escape_string($group_id);
    $admin_id = $link->real_escape_string($admin_id);
    $result = $link->query("SELECT 1 FROM Groups
                            WHERE Group_Id = '$group_id' AND User_Id = '$admin_id'");

    return($result->num_rows > 0);
  }
?>
