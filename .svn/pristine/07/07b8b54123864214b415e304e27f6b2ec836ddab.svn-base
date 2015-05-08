<?php
	function add_schedule(){
		global $p_id;
		$time = $_POST['time'];
		$flag = $_POST['SMS'];
		$taskName = $_POST['taskName'];
		$result = mysql_query("SELECT * FROM `Tasks`");
		while($row = mysql_fetch_array($result)){
			if($row['taskName'] == $taskName){
				$t_id = $row['taskId'];
				break;
			}
		}
		//check validity of time
		$valid = 1;
		$length  = strlen($time);
		 
		if($length < 3){
			$valid = 0;
		}else{
			if($length == 3 ){
				$hour = (int)("0" . $time[0]);
				$min = (int) ($time[1] . $time[2]);
				//echo " hour: " . $hour;
				//echo " minute: " . $min;
				if($min > 59){
					$valid = 0;
				}
			}else{
				$hour = (int)($time[0] . $time[1]);
				$min = (int) ($time[2] . $time[3]);
				//echo " hour: " . $hour;
				//echo " minute: " . $min;
				if($hour > 23 || $min > 59){
					$valid = 0;
				}
			}				
		}
		if($valid){
			$p_id = (int)$p_id;
			$t_id = (int)$t_id; 
			$flag = (int)$flag;
			$sql = "INSERT INTO `patientTasks` (patientId, taskId, time, flag) VALUES ($p_id, $t_id, $time, $flag)";
			$sql2 = "INSERT INTO `update` (command, patientId, taskId, time, flag, status) VALUES (0, $p_id, $t_id, $time, $flag, 0)";
			mysql_query($sql);
			mysql_query($sql2);
		}else{
			echo "   invalid time";
			//$error = "Invalid time format. Please type \"hhmm\"";
		}
		$action = "patient.php?p_id=" . $p_id;
		header( 'Location:' . $action ) ;
	}
	
	function delete_schedule(){
		global $p_id;
		$delete_tasks = $_POST['delete_tasks'];
		for($i=0; $i < count($delete_tasks); $i++){
			$result = $delete_tasks[$i];
			//print_r($result);
			$param = explode(" ", $result);
			$time = (int)$param[0];
			$t_id = (int)$param[1];
			$flag = (int)$param[2];
			$stat = (int)$param[3];
			$sql = "DELETE FROM `patientTasks` WHERE patientId = $p_id AND taskId = $t_id AND time = $time AND flag = $flag AND status = $stat LIMIT 1";
			$sql2 = "INSERT INTO `update` (command, patientId, taskId, time, flag, status) VALUES (1, $p_id, $t_id, $time, $flag, 0)";
			mysql_query($sql);
			mysql_query($sql2);
		}
		$action = "patient.php?p_id=" . $p_id;
		header( 'Location:' . $action ) ;
	}
?>