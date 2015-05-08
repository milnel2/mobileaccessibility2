package cs.washington.mobileaccessibility.onebusaway;

import java.util.ArrayList;

import cs.washington.mobileaccessibility.onebusaway.uistates.CoState;
// import cs.washington.mobileaccessibility.onebusaway.util.Util;

import firbi.base.com.BusStop;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


/*
 * An instance of this class is given to each State by MainActivity.
 * This class manages the bookmarks or favorites (I need to choose one
 * of these terms and stick to it TODO)
 * The State object requests a BookmarkManager using CoState.getBookmarks()
 * It can then access and modify the list of bookmarks.
 * 
 * I suppose that we could have just replaced this class with an ArrayList<BusStop>
 * I didn't think of doing that, however.
 * 
 * Also, MainActivity talks to this class, by giving it a SharedPreferences object
 * in the constructor, and giving it a SharedPreferences.Editor object when it needs
 * to commit the changes.  MainActivity also instantiates this class
 */
public class BookmarkManager {
	
	private ArrayList<BusStop> bookmarks;
	
	public BookmarkManager(SharedPreferences prefs) {
		bookmarks = new ArrayList<BusStop>();
		int n = prefs.getInt("nBookmarks", 0);
		for(int i = 0; i < n; i++) {
			String sID = prefs.getString("favorite_" + i, "");
			if(sID.equals(""))
				continue;
			BusStop stop = BusStop.find(sID);
			if(stop != null)
				bookmarks.add(stop);
		}
	}
	
	// Add or remove the given stop from the bookmarks
	// Vocally announce what's happening too.
	public void toggleBookmark(BusStop stop, CoState cos) {
		String stopNum = stop.getStopNumber();
		String address = stop.getAddress();
		if(bookmarks.contains(stop)) {
			bookmarks.remove(stop);
			cos.outputText("Deleted bookmark for stop number " + stopNum +
					", at " + address);
		}
		else {
			bookmarks.add(stop);
			cos.outputText("Added bookmark for stop number " + stopNum +
					", at " + address);
		}
	}
	
	
	public BusStop [] getBookmarks() {
		return bookmarks.toArray(new BusStop[0]);
	}
	
	public boolean isBookmarked(BusStop b) {
		return bookmarks.contains(b);
	}

	// this method doesn't commit the changes,
	// but just puts them into the editor
	// because we might be using the Editor for other preference changes
	// not related to bookmarks
	public void saveSettings(Editor ed) {
		ed.putInt("nBookmarks", bookmarks.size());
		for(int i = 0; i < bookmarks.size(); i++) {
			ed.putString("favorite_" + i, bookmarks.get(i).getId());
		}
	}

}
