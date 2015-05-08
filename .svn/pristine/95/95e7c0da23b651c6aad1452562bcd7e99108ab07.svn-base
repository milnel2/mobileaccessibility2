<?php

# connect to LinkUp database
/*$db = mysql_connect('cubist.cs.washington.edu', 'malikb2', '2bo3fZJo');
mysql_select_db('malikb2_LinkUpDB');
*/

$con = mysql_connect("vergil.u.washington.edu:21988", "root", "capstone");
    if (!$con){
        die('Could not connect: ' . mysql_error());
    }
$db = "LinkUpDB";
mysql_select_db($db) or die("Could not select the database '" . $db . "'.  Are you sure it exists?");


# process a POST request
	
	# Grab the parameters that we want to enter into the database from the post
	$latitude = $_REQUEST["latitude"];
	$longitude = $_REQUEST["longitude"];
	$address = $_REQUEST["address"];
	$sound = $_REQUEST["sound"];
	$cat = $_REQUEST["cat_id"];
	$addresstype = $_REQUEST["addresstype_id"];

	# Construct the string to print
	$insertString = $latitude . ", " . $longitude . ", '" . $address . "', '" . $sound . "', " . $cat . ", " . $addresstype;

	# Run the query... not sure what I'm supposed to receive
	$qResults = mysql_query("
		INSERT INTO LinkUpFavLocs (Latitude, Longitude, Address, Sound_File_Loc, Cat_ID, Address_Type_ID)
		VALUES (" . $insertString . ")") 
		or die(mysql_error());
							
	# Return that it was either a success or fail.
	print($qResults);
?>