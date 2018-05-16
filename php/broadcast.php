<?php
  function send_notification($tokens, $message)
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

  include("Util/connect.php");
  $link = connect();

  if($link == false)
  {
    echo "Connection Failed";
    exit;
  }

	$sql = "SELECT Token From users";
	$result =$link->query($sql);

	$tokens = array();
	if(mysqli_num_rows($result) > 0 ){
		while ($row = mysqli_fetch_assoc($result)) {
			$tokens[] = $row["Token"];
		}
	}

  $link->close();
	$message = array("message" => " FCM PUSH NOTIFICATION TEST MESSAGE");
	$message_status = send_notification($tokens, $message);
	echo $message_status;
  //echo "test";
 ?>
