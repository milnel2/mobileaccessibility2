<?php
//Recveiver file, when it receives a url command from phone, run this code.
//determine if the comman is a schedule/task command.
$type = $_GET['type']; // schedule = 0; task = 1;
$update = $_GET['update']; // update type: android=0; server=1;
$string = $_GET['string'];
$p_id = $_GET['p_id'];

$con = mysql_connect("vergil.u.washington.edu:21988", "root", "capstone");
	if (!$con){
		die('Could not connect: ' . mysql_error());
	}
	mysql_select_db("DTT", $con);
	
if($type){
	//rest of the string to modify_task_db function
	modify_task_db($string, $p_id);
}else{
	if($update){
		//modify server
		//if the caretaker adds/deletes something from the schedule on the phone,
		//I need to also add it to my schedule
		update_server($string, $p_id);
		
	}else if (!$update){
		//modify android
		//android will send full schedule and website will compare and send back differences.
		update_android($p_id);
	}
}



function modify_task_db($string, $p_id){
	//string = command, taskid, taskname;
	$array = explode(";", $string);
	$i = 0; 
	while($array[$i] != "!"){
	$params = explode(",", $array[$i]);
		if(count($params) != 3){
			print("error,incorrect number of parameters");
			break;
		}else{
				$t_id = (int)$params[1];
				$t_name = $params[2];
			if((int)$params[0] == 0){
				$sql2 = "DELETE FROM `Tasks` WHERE taskId = $t_id AND patientId = $p_id LIMIT 1";
				echo $sql2;
				echo "add task, successful";
				$sql = "INSERT INTO `Tasks` (taskId, taskName, patientId) VALUES ($t_id, '$t_name', $p_id)";
				mysql_query($sql2);
				mysql_query($sql);
			}else if((int)$params[0] == 1){
			// if delete task, need to delete all related tasks in the schedule too.
				
				echo "delete task, sucessful";
				$sql = "DELETE FROM `Tasks` WHERE taskId = $t_id";
				$sql1 = "DELETE FROM `patientTasks` WHERE taskId = $t_id";
				mysql_query($sql);
				mysql_query($sql1);
			}else{
				print("error, command unknown");
				break;
			}
		}
		$i = $i + 1;
	}
}

function update_server($string, $p_id){
	//			0		1	2	  3		4
	// string: command,t_id,time,flag,status;........;!
	// command 0: add, 1: delete, 2: update status
	$array = explode(";", $string);
	$i = 0; 
	while($array[$i] != "!"){
	$params = explode(",", $array[$i]);
		if(count($params) != 5){
			print("error,incorrect number of parameters");
			break;
		}else{
				$command = (int)$params[0];
				$t_id = (int)$params[1];
				$t_time = (string)$params[2];
				$t_flag = (int)$params[3];
				$t_status = (int)$params[4];
			if($command == 0){
				echo "add to schedule, successful";
				$sql = "INSERT INTO `patientTasks` 
				(patientId, taskId, time, flag, status) 
				VALUES ($p_id, $t_id, $t_time, $t_flag, $t_status)";
				mysql_query($sql);
			}else if($command == 1){
				echo "delete from schedule, successful";
				$sql = "DELETE FROM `patientTasks` 
						WHERE patientId = $p_id
						AND taskId = $t_id
						AND time = $t_time
						AND flag = $t_flag
						AND status = $t_status
						LIMIT 1";
				mysql_query($sql);
			}else if($command == 2){
				echo "update";
				$sql = "UPDATE `patientTasks` 
						SET `status` = $t_status 
						WHERE `patientId` = $p_id 
						AND `taskId` = $t_id
						AND `time` = $t_time
						AND `flag` = $t_flag
						LIMIT 1";
				mysql_query($sql);
			}else{
				print("error, command unknown");
				break;
			}
		}
		$i = $i + 1;
	}
	
}

function update_android($p_id){
	//string: 	task_id,time,flag,status;........;!
	//			command,taskid,time,flag,status;
	$sql = "SELECT * FROM `update` WHERE patientId = $p_id ORDER BY `index` ASC";
	$updates = mysql_query($sql);
	$result = "";

	while($row = mysql_fetch_array($updates)){
		$t_comm = $row['command'];
		$t_id = $row['taskId'];
		$t_time = $row['time'];
		$t_flag = $row['flag'];
		$t_stat = $row['status'];
		$result = $result . $t_comm . "," . $t_id . "," . $t_time . "," . $t_flag . "," . $t_stat . ";";  
	}
	$result =  $result . "\n";
	$flush = "DELETE FROM `update`";
	mysql_query($flush);
	print($result) ; 
}
?>