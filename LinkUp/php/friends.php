<?php

# connect to LinkUp database
$con = mysql_connect("vergil.u.washington.edu:21988", "root", "capstone");
    if (!$con){
        die('Could not connect: ' . mysql_error());
    }
$db = "LinkUpDB";
mysql_select_db($db) or die("Could not select the database '" . $db . "'.  Are you sure it exists?");

if ($_SERVER["REQUEST_METHOD"] == "GET") {

	# We need the current longitude and latitude and limitation
	# assume these are always provided
	$mLatitude = $_REQUEST["latitude"];
	$mLongitude = $_REQUEST["longitude"];
	#$withinRad = $_REQUEST["withinRad"];

	#print("Latitude: " . $mLatitude . "<BR>");
	#print("Longitude: " . $mLongitude . "<BR>");
	#print("Radius: " . $withinRad . "<BR>");
	# Generate a list of friends and distances from current location


	# Query database for all necessary information
	$friends = mysql_query("SELECT * FROM FriendsCurLoc");
	$numOfRows = mysql_num_rows($friends);
	# print("numOfRows: " . $numOfRows . " ");
	
	# for each row of the result, calculate a distance and create a row
	# 		how should this be stored?
	# using json files?

	# parameters:
	# $lat1
	# $long1
	# $lat2
	# $long2

	# Returns a distance in miles


	#$R = 6371000.0; # Radius of earth in meters
	#$R = 20 925 524.9 # Radius of earth in feet
	$R = 3963.1676; # Radius of earth in miles
	
	#print("R: " . $R . "<BR>");
	# formated result with direction
	$resultString = $numOfRows . "\n";
								
	# parse the mysql array and print as a string
	# Known columns for this results:
	#	ID, Alias, Longitude, Latitude, PhoneNum
	while ($row = mysql_fetch_array($friends)) {
		$dLat = deg2rad($row["Latitude"] - $mLatitude);
		$dLon = deg2rad($row["Longitude"] - $mLongitude);
		
		#print("dLat: " . $dLat . "<BR>");
		#print("dLon: " . $dLon . "<BR>");
	
		#print("sin dLat / 2: " . sin($dLat/2) . "<BR>");
		$a = sin($dLat/2) * 
			sin($dLat/2) +
			cos(deg2rad($mLatitude)) * 
			cos(deg2rad($row["Latitude"])) *
			sin($dLon/2) *
			sin($dLon/2);
	
		# print("a: " . $a . "<BR>");
		$c = 2 * atan2(sqrt($a), sqrt(1-$a));
		$d = $R * $c;
	
		
		#if ($d < $withinRad) {
				#print("d is less than within: ");		
				$resultString .= $row["Alias"] . ";" . $row["PhoneNumber"] . ";". $d  . "\n ";
		#}
	}

	print($resultString);
} elseif ($_SERVER["REQUEST_METHOD"] == "POST") {


	# Grab the parameters that we want to enter into the database from the post
	$alias = $_REQUEST["alias"];
	$latitude = $_REQUEST["latitude"];
	$longitude = $_REQUEST["longitude"];
	$phonenumber = $_REQUEST["phonenumber"];
	
	$insertString = "'" . $alias . "', " . $latitude . ", " . $longitude . ", '" . $phonenumber . "'";
	
	$exist = mysql_query("SELECT * FROM  `FriendsCurLoc` WHERE PhoneNumber =" . $phonenumber);
	$numRows = mysql_num_rows($exist);
	print($numRows);
	print($phonenumber);
	
	if ($numRows > 0) { 
		$result = mysql_query("UPDATE FriendsCurLoc SET latitude=" . $latitude . ", longitude=" . $longitude . " WHERE phonenumber='" . $phonenumber . "'") or die(mysql_error());
	} else {
		$result = mysql_query("INSERT INTO FriendsCurLoc (Alias, Latitude, Longitude, PhoneNumber) 
		VALUES (" . $insertString . ")") or die(mysql_error());
	
	}
}
?>