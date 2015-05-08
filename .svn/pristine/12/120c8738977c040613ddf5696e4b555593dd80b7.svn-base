<?php

# connect to LinkUp database
# $db = mysql_connect('vergil.u.washington.edu:21988', 'shenama', 'amanda.shen0');
# mysql_select_db('LinkUpDB');

// connect and select a database 
$con = mysql_connect("vergil.u.washington.edu:21988", "root", "capstone");
    if (!$con){
        die('Could not connect: ' . mysql_error());
    }
$db = "LinkUpDB";
mysql_select_db($db) or die("Could not select the database '" . $db . "'.  Are you sure it exists?");




if ($_SERVER["REQUEST_METHOD"] == "GET") {
	# process a GET request
	# execute a SQL query to grab everything (for now) from the database)
	# Store it to $results variable

	$numOfLocations = mysql_query("SELECT COUNT( 1 ) AS 'Num' FROM LinkUpFavLocs") or die(mysql_error());
	$qResults = mysql_query("SELECT ID, Latitude, Longitude, Address, Sound_File_Loc, Category_Name, Address_Type
	FROM LinkUpFavLocs l
	JOIN Categories c ON l.Cat_ID = c.Cat_ID
	JOIN Address_Type a ON l.Address_Type_ID = a.Address_Type_ID") or die(mysql_error());

	# Create the output string starting with the number of results
	$resultString = "";

	# this would be better handled as a XML or JSON file, but we're gonna hack this for now	
	while ($row = mysql_fetch_array($numOfLocations)) {
		$resultString .= $row["Num"] . "\n";
	}
	
	# parse the mysql array and print as a string
	while ($row = mysql_fetch_array($qResults)) {
		#$resultString = "123";
		$resultString .= $row["ID"] . ";" . $row["Latitude"] . ";" . $row["Longitude"] . ";" . 
					$row["Address"] . ";" . $row["Sound_File_Loc"] . ";" . $row["Category_Name"] . ";" .
					$row["Address_Type"] . "\n";
	}

	#	echo $resultString;
	print($resultString);
	  
} elseif ($_SERVER["REQUEST_METHOD"] == "POST") {
	# process a POST request
	
	# Grab the parameters that we want to enter into the database from the post
	$latitude = $_REQUEST["latitude"];
	$longitude = $_REQUEST["longitude"];
	$address = $_REQUEST["address"];
	$sound = $_REQUEST["sound"];
	$cat = $_REQUEST["cat"];
	$addresstype = $_REQUEST["addresstype"];

	# set up switch cases for category for all it's options later
	if ($cat == "Unknown") {
		$cat = 0;
	} 
	
	# set up switch cases for address for all it's options later
	if ($addresstype == "Exact") {
		$addresstype = 1;
	}
	
	# Construct the string to print
	$insertString = $latitude . ", " . $longitude . ", '" . $address . "', '" . $sound . "', " . $cat . ", " . $addresstype;

	# Run the query... not sure what I'm supposed to receive
	$qResults = mysql_query("
		INSERT INTO LinkUpFavLocs (Latitude, Longitude, Address, Sound_File_Loc, Cat_ID, Address_Type_ID)
		VALUES (" . $insertString . ")") 
		or die(mysql_error());
							
	# Return that it was either a success or fail.
	print($qResults);
}

?>