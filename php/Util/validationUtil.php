<?php

	class Validation{
		function test_input($data) {
		  $data = trim($data);
		  $data = stripslashes($data);
		  $data = htmlspecialchars($data);
		  return $data;
		}

		function isSecure($secure, $port) {
			//Note: our webservice is set up only to allow secure HTTP access
			if ($port == 443)
			{
					return 1;
			}
			else if ( $secure != 'on' )
			{
				return 0;
			}
			return 1;
		}
	}
?>
