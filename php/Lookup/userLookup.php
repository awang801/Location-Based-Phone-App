<?php
  function userLookup($login_id, $link)
  {
    $login_id = $link->real_escape_string($login_id);
    $result = $link->query("SELECT User_id AS id FROM Users WHERE Login_Id = '$login_id'");

    if ($result->num_rows > 0) {
        // output data of each row
        $row = $result->fetch_assoc();
        $user_id = $row["id"];
        return $user_id;
    }
    else
    {
        return false;
    }
  }
?>
