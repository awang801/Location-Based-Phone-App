<?php
  function isFeedPub($group_id, $link)
  {
    $group_id = $link->real_escape_string($group_id);
    $result = $link->query("SELECT 1 FROM Groups
                            WHERE Group_Id = '$group_id' AND Feed_Rights = 1");

    return($result->num_rows > 0);
  }
?>
