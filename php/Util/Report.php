<?php

	class Report{
      public static function genReport($admin_id, $group_id, $link, $time)
      {
        $group_id = $link->real_escape_string($group_id);
        $confirmRes = $link->query("SELECT 1 FROM Groups
                                    WHERE Group_Id = '$group_id'");
        if($confirmRes->num_rows > 0)
        {
          //echo "Time = " . $time;
          $LocResult = $link->query("SELECT Users.Name AS n, Group_Users.Lng AS l, Group_Users.Lat AS lt
                                     FROM Group_Users
                                     INNER JOIN Users ON Group_Users.User_Id=Users.User_Id
                                     WHERE Group_Users.Group_Id = '$group_id'
                                        AND Group_Users.State = 0
                                        AND Group_Users.Last_Update > '$time'");

          $Missing = $link->query("SELECT Users.Name AS n
                                     FROM Group_Users
                                     INNER JOIN Users ON Group_Users.User_Id=Users.User_Id
                                     WHERE Group_Users.Group_Id = '$group_id'
                                        AND Group_Users.State = 0
                                        AND Group_Users.Last_Update <= '$time'");



          $rows = array();

          while($r = $LocResult->fetch_assoc())
          {
            $rows[] = $r;
          }

          $rowsMissing = array();
          while($r = $Missing->fetch_assoc())
          {
            $rowsMissing[] = $r;
          }

          $report = json_encode(array('locations' => $rows, 'missing' => $rowsMissing));
          return $report;
        }
        else
        {
          return "false";
        }
      }

        public static function getJSON($admin_id, $group_id, $link, $time)
        {
          $report = Report::genReport($admin_id, $group_id, $link, $time);

          if ($report == "false")
          {
            return "Failure";
          }

          $report = json_decode($report, TRUE);
          $report['time'] = $time;

          return json_encode($report);
        }

        public static function insertAndMessage($admin_id, $group_id, $link, $time, $id, $group_name)
        {
          date_default_timezone_set("UTC");
          $datetime = date('Y-m-d H:i:s', time());

          $report = Report::genReport($admin_id, $group_id, $link, $time);

          if ($report == "false")
          {
            return "false";
          }

          $updateRes = $link->query("INSERT INTO Att_Reports (Run_Id, Creation, Report)
                                     VALUES ('$id', '$datetime', '$report')");

          if($updateRes)
          {
            //Send message to Admin
            $instances = Messaging::getInstanceForUser($admin_id,$link);
            $message = array("report" => "$group_name");
            $messageResult = Messaging::sendNotification($instances,$message);
            echo $messageResult;
            //TODO: hopefully at some point we can handle reseult in general sense to clean up failures

            return "true";
          }
          else
          {
            return "false";
          }
       }
    }
?>
