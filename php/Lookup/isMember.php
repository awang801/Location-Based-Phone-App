<?php
  function isMember($group_id, $user_id, $link)
  {
    $group_id = $link->real_escape_string($group_id);
    $user_id = $link->real_escape_string($user_id);
    $memberResult = $link->query("SELECT 1 FROM Group_Users
                            WHERE Group_Id = '$group_id' AND User_Id = '$user_id'
                            AND State = 0");

    return($memberResult->num_rows > 0);
  }
?>
