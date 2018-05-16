<?php
  function groupLookup($name, $link)
  {
    $name = $link->real_escape_string($name);
    $result = $link->query("SELECT Group_Id AS id FROM Groups WHERE Name = '$name'");

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
