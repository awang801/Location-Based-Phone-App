<?php
  function userNameLookup($name, $link)
  {
    $name = $link->real_escape_string($name);
    $result = $link->query("SELECT User_id AS id FROM Users WHERE Name = '$name'");

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
