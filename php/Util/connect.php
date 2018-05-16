<?php
  function connect()
  {
    $link = mysqli_connect("mysql.eecs.ku.edu", "gmagnuso", "123Password!", "gmagnuso");

    if (!$link) {
        /*echo "Error: Unable to connect to MySQL." . PHP_EOL;
        echo "Debugging errno: " . mysqli_connect_errno() . PHP_EOL;
        echo "Debugging error: " . mysqli_connect_error() . PHP_EOL;*/
        //echo "0";
        return false;
    }

    //echo "Success: A proper connection to MySQL was made! The my_db database is great." . PHP_EOL;
    //echo "Host information: " . mysqli_get_host_info($link) . PHP_EOL;

    return $link;
  }
  //mysqli_close($link);
?>
