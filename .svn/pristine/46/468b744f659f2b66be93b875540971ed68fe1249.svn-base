<?php
	$con = mysql_connect("vergil.u.washington.edu:21988", "root", "capstone");
	if (!$con){
		die('Could not connect: ' . mysql_error());
	}
	mysql_select_db("DTT", $con);
	
	$string = "1,20,Take a shower;!";
	echo $string . "        ";
	$array = explode(";", $string);
	print_r($array);
	echo "        ";
	$i = 0; 
	while($array[$i] != "!"){
		$params = explode(",", $array[$i]);
		print_r($params);
		echo "        ";
		
		
		
		
		
		
		$i = $i + 1;
	}
?>
