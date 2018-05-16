<?php

	class Messaging{
      public static function getInstancesFromGroup($group_id, $link)
      {
        $query = "SELECT Instance_Id
                  FROM Group_Users
                  INNER JOIN User_Device
                  ON Group_Users.User_Id = User_Device.User_Id
                  WHERE Group_Id = '$group_id'
                  AND State = 0";

        $result = $link->query($query);

        $instances = array();

        if($result->num_rows > 0)
        {
          while($r = $result->fetch_assoc())
          {
            $instances[] = $r["Instance_Id"];
          }
        }

        return $instances;
      }

			public static function refreshTokenIfNeeded($messageResult,$instance,$user_id, $link)
			{
				$messageJson = json_decode($messageResult,TRUE);

				//echo "got to refresh: ".$messageResult;
				if(!empty($messageJson))
				{
					//echo var_dump($messageJson);
					$results = $messageJson['results'];
					if(!empty($results))
					{
						//echo var_dump($results);
						foreach ($results as $key => $value)
						{
							if($value["error"] == 'NotRegistered' || $value["error"] == 'InvalidRegistration')
							{
								$link->query("UPDATE User_Device SET Instance_Id = '$instance' WHERE User_Id = '$user_id'");
								return TRUE;
							}
						}
					}
				}
				return FALSE;
			}

			public static function getInstanceForUser($user_id, $link)
      {
        $query = "SELECT Instance_Id
                  FROM User_Device
                  WHERE User_Id = '$user_id'";

        $result = $link->query($query);

        $instances = array();

        if($result->num_rows > 0)
        {
          while($r = $result->fetch_assoc())
          {
            $instances[] = $r["Instance_Id"];
          }
        }

        return $instances;
      }

      public static function sendNotification($tokens, $message)
      {
        $url = 'https://fcm.googleapis.com/fcm/send';
    		$fields = array(
    			 'registration_ids' => $tokens,
    			 'data' => $message
    			);
    		$headers = array(
    			'Authorization:key = AAAAF1EvEgI:APA91bH-sLFHKRZVZ-tsjLl4LFjWFdvVRgRAjLxcWp5cEzJXByAkPutetcsGFzas-YIKsoiEj_7_N2u6VT6bRXnbJtMLfV39jUq-pozK4ce3wy0ndfjQqEgYdTqwLOn__NJfMnme_kfU',
    			'Content-Type: application/json'
    			);

    	     $ch = curl_init();
           curl_setopt($ch, CURLOPT_URL, $url);
           curl_setopt($ch, CURLOPT_POST, true);
           curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
           curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
           curl_setopt ($ch, CURLOPT_SSL_VERIFYHOST, 0);
           curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
           curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
           $result = curl_exec($ch);
           if ($result === FALSE) {
               die('Curl failed: ' . curl_error($ch));
           }
           curl_close($ch);
           return $result;
    	}

      public static function cleanUp($messageResult)
      {
        //TODO:
      }
	}
?>
