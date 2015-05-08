package cs.washington.edu.buddies;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

/* Static class used for managing a player's purchases.
 * Relies on store data to know what types of purchases
 * can exist -- that is, the item names from products.xml
 * are the keys to this data.
 */
public class Purchases {

	// Bank account file name
	public static final String PURCHASES_FILENM = "BBPurchases";

	/** See whether player has item on hand (more than zero) */
	public static boolean purchased(Context context, String item) {
		SharedPreferences purchases = context.getSharedPreferences(PURCHASES_FILENM, 0);
		return (Purchases.getItemCount(context, item) > 0);
	}
	
	/** Return how many of a specific item have already been purchased */
	public static int getItemCount(Context context, String item) {
		SharedPreferences purchases = context.getSharedPreferences(PURCHASES_FILENM, 0);
		return purchases.getInt(item, 0);
	}

	/** Add one to the purchase count for an item */
	protected static boolean buyItem(Context context, String item) {
		SharedPreferences purchases = context.getSharedPreferences(PURCHASES_FILENM, 0);
		SharedPreferences.Editor ed = purchases.edit();
		boolean result = false;
		ed.putInt(item, Purchases.getItemCount(context, item) + 1);
		result = ed.commit();
		return result;
	}
	
	/** Subtract one to the purchase count for an item */
	protected static boolean discardItem(Context context, String item) {
		SharedPreferences purchases = context.getSharedPreferences(PURCHASES_FILENM, 0);
		SharedPreferences.Editor ed = purchases.edit();
		boolean result = false;
		ed.putInt(item, Purchases.getItemCount(context, item) - 1);
		if (Purchases.getItemCount(context, item) < 0)
			ed.putInt(item, 0);
		result = ed.commit();
		return result;
	}
	
	/** Get a map of all purchases and their counts */
	protected static Map<String, ?> getAllPurchases(Context context, int amount) {
		SharedPreferences purchases = context.getSharedPreferences(PURCHASES_FILENM, 0);
		return purchases.getAll();
	}

}
