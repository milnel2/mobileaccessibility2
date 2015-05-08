package cs.washington.mobileaccessibility.onebusaway.util;

import com.google.android.maps.GeoPoint;

import firbi.base.com.BusStop;

//import android.util.Log;

/**
 * convert things like "25th Ave NE & NE 47th St" to a form that
 * TTS will correctly read, like
 * "twenty-fifth avenue northeast & northeast fourtyseventh street"
 * 
 * Of course, it'll still butcher words like "Pend Oreille" or "Ravenna"
 * 
 * Oh, and now we also have a utility for changing stop and route numbers to more
 * common pronunciations.  For example, 75414 and 565 are usually pronounced
 * by humans as "seven five four one four" and "five sixty five", not
 * "seventy five thousand four hundred fourteen" and "five hundred sixty five,"
 * as far as I know.
 * 
 * On a completely unrelated note, there is also a function taken straight out of
 * firbi.base.util.FirbiHelper to calculate the distance between two GeoPoints
 * 
 * @author Will
 *
 */
public class Util {
	
	
	
	// Basically, we're going to run everything through a bunch
	// of regex patterns
	// Here they are, in pairs
	private final static String [] patterns = {
		"ALY", "Alley",
		"ANX", "Annex",
		"ARC", "Arcade",
		"AVE", "Avenue",
		"BCH", "Beach",
		"BND", "Bend",
		"BLF", "Bluff",
		"BLFS", "Bluffs",
		"BLVD", "Boulevard",
		"BRG", "Bridge",
		"BRK", "Brook",
		"BRKS", "Brooks",
		"BYP", "Bypass",
		"CIR", "Circle",
		"CSWY", "Causeway",
		"CTR", "Center",
		"CLF", "Cliff",
		"CLFS", "Cliffs",
		"COR", "Corner",
		"CT", "Court",
		"CTS", "Courts",
		"CV", "Cove",
		"CVS", "Coves",
		"CRK", "Creek",
		"CRES", "Crescent",
		"CRST", "Crest",
		"XING", "Crossing",
		"CURV", "Curve",
		"DR", "Drive",
		"EST", "Estate",
		"ESTS", "Estates",
		"EXPY", "Expressway",
		"EXT", "Extension",
		"FLS", "Falls",
		"FRY", "Ferry",
		"FLD", "Field",
		"FLDS", "Fields",
		"FRST", "Forest",
		"FRK", "Fork",
		"FT", "Fort",
		"FWY", "Freeway",
		"GDN", "Garden",
		"GTWY", "Gateway",
		"HBR", "Harbor",
		"HTS", "Heights",
		"HWY", "Highway",
		"HL", "Hill",
		"HLS", "Hills",
		"INLT", "Inlet",
		// "IS", "Island", // The stop number Island written on the top left corner...
		"ISS", "Islands",
		"JCT", "Junction",
		"JCTS", "Junctions",
		"LK", "Lake",
		"LKS", "Lakes",
		"LN", "Lane",
		"LCK", "Lock",
		"LCKS", "Locks",
		"LDG", "Lodge",
		"MDW", "Meadow",
		"MDWS", "Meadows",
		"MTWY", "Motorway",
		"MT", "Mount",
		"MTN", "Mountain",
		"OPAS", "Overpass",
		"PK", "Park", // unofficial
		"PKWY", "Parkway",
		"PSGE", "Passage",
		"PNE", "Pine",
		"PNES", "Pines",
		"PL", "Place",
		"PLN", "Plain",
		"PLZ", "Plaza",
		"PT", "Point",
		"PRT", "Port",
		"PR", "Prairie",
		"RNCH", "Ranch",
		"RIV", "River",
		"RD", "Road",
		"RDS", "Roads",
		"RTE", "Route",
		"SHR", "Shore",
		"SHRS", "Shores",
		"SKWY", "Skyway", // what is this?
		"SPG", "Spring",
		"SQ", "Square",
		"SQS", "Squares",
		"STA", "Station",
		// "STRA", "Stravenue", we're not in Tucson, AZ
		"STRM", "Stream",
		"ST", "Street",
		"STS", "Streets",
		"SMT", "Summit",
		"TER", "Terrace",
		"TRWY", "Throughway",
		"TRL", "Trail",
		"TUNL", "Tunnel",
		"UPAS", "Underpass",
		"UN", "Union",
		"VLY", "Valley",
		"VLYS", "Valleys",
		"VIA", "Viaduct",
		"VW", "View",
		"VWS", "Views",
		"VLG", "Village",
		"VIS", "Vista",
		"VSTA", "Vista", //unofficial
		"VST", "Vista", //unofficial
		
		// "SR", "Route", would cause confusion between SR 522 and bus route 522
		"TC", "Transit center",
		"P&R", "parcandride",
		// for some reason "park and ride" always has a huge gap after "park"
		
		"AcRd", "Access Road", // apparently unofficial
		"NE", "northeast",
		"SE", "southeast",
		"SW", "southwest",
		"NW", "northwest",
		"N", "north",
		"E", "east",
		"W", "west",
		"S", "south",
		"([0-9])([1-9][0-9])", "$1 $2",
		"([0-9]*[2-9])([4-9]th|3rd|2nd|1st)", "$10 $2",
		"([0-9])([0-9])([0-9])([0-9])([0-9])", "$1 $2 $3 $4 $5",
		"([0-9])([0-9])([0-9])([0-9])", "$1 $2 $3 $4", // hopefully there are no 6-digit stop no.s
		"1st", "first",
		"2nd", "second",
		"3rd", "third",
		"4th", "fourth",
		"5th", "fifth",
		"6th", "sixth",
		"7th", "seventh",
		"8th", "eighth",
		"9th", "ninth",
		"NOW", "now"
	};
	

	// Take a string and convert it to a better form for pronunciation
	// This gets applied to each string before it is spoken via TTS
	public static String convert(String address) {
		for(int i = 0; i < patterns.length; i+=2)
			address = address.replaceAll("(?i)\\b" + patterns[i] + "\\b",patterns[i+1]);
		
		// address = spaceDigits(address); // actually this interferes with something we already did
		// hm, it looks like I just put digit-spacing above
		address = address.replaceAll("#", "number"); // eh, this is a sketchy thing TODO
		
		return address;
	}
	
	// turns 372 into 3 7 2, and 75414 into 7 5 4 1 4.  But leaves two digit
	// number alone.  This seemed natural for bus stop numbers, but now I'm rethinking
	// the three-digit case
	// 
	// This function is not called, and maybe have been superseded by some patterns
	// in the giant array above
	public static String spaceDigits(String number) {
		number = number.replaceAll("([0-9])", "$1z");
		number = number.replaceAll("\\b([0-9])z([0-9])z\\b", "$1$2");
		number = number.replaceAll("([0-9])z", "$1 ");
		return number;
	}
	
	// TODO very preliminary
	// This gets applied to every text before it gets sent to BrailleView
	public static String brailleConvert(String text) {
		text = text.replaceAll("([0-9]+)$", "\\\\$1");
		text = text.replaceAll("([0-9]+)([^0-9])", "\\\\$1 $2");
		text = text.replaceAll("([A-Z])", "$1"); // should be "+$1", but I'm not sure we use caps for anything significant
		return text;
		
	}
	
	
	/** 
	 * *COPIED STRAIGHT OUT OF Firbi's firbi.base.util.FirbiHelper class **
	 * Returns the distance in feet or meters between two GeoPoints
	 * 
	 * @param p1: Point 1
	 * @param p2: Point 2
	 * @param meters: Whether to return distance in meters or feet
	 * @return: distance between two GeoPoints
	 */
	public static double getDistance(GeoPoint p1, GeoPoint p2, boolean meters) {
		double p1Long = (p1.getLongitudeE6()/1E6)/57.29577951;
		double p1Lat = (p1.getLatitudeE6()/1E6)/57.29577951;
		double p2Long = (p2.getLongitudeE6()/1E6)/57.29577951;
		double p2Lat = (p2.getLatitudeE6()/1E6)/57.29577951;
		
		double d = Math.acos(Math.sin(p1Lat)*Math.sin(p2Lat)+
				 Math.cos(p1Lat)*Math.cos(p2Lat)*Math.cos(p2Long-p1Long));
		if (meters) d *= 6371*1000;
		else  d *= 3963 * 5280; 
		
		return ((int)(d*100))/100.0;
	}
	
	

	/** 
	 * COPIED STRAIGHT OUT OF firbi.base.util.FirbiHelper.java
	 * Converts a BusStop's direction field to a String.
	 * 
	 * @param direction: Direction field of a BusStop
	 * @return: String representation of that direction or
	 * 			'--' if the direction is not defined. 
	 */
	public static String directionToString(Integer direction) {
		switch (direction) {
		case BusStop.DIRECTION_E: return "E";
		case BusStop.DIRECTION_N: return "N";
		case BusStop.DIRECTION_NE: return "NE";
		case BusStop.DIRECTION_NW: return "NW";
		case BusStop.DIRECTION_S: return "S";
		case BusStop.DIRECTION_SE: return "S";
		case BusStop.DIRECTION_SW: return "SW";
		case BusStop.DIRECTION_W: return "W";
		default: return "--";
		}
	}
}
