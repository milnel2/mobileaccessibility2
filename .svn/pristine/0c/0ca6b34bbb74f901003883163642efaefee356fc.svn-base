package cs.washington.edu.buddies;

import android.content.Context;
import android.content.SharedPreferences;

/* Static class used for managing bank account balance.
 * Also contains global variables for amounts that can
 * be earned for correct answers in games.
 */
public class BankAccount {

	// amounts that can be earned
	public static final int PER_SYMBOL = 5;
	public static final int PER_EASY_WORD = 10;
	public static final int PER_INTERMED_WORD = 15;
	public static final int PER_HARD_WORD = 20;
	
	// setting key names and default values
	private static final String OPT_BALANCE = "balance";
	private static final int OPT_BALANCE_DEF = 20;
	
	// Bank account file name
	public static final String ACCOUNT_FILENM = "BBBankAccount";

	// Get the current balance
	public static int getBalance(Context context) {
		SharedPreferences account = context.getSharedPreferences(ACCOUNT_FILENM, 0);
		return account.getInt(OPT_BALANCE, OPT_BALANCE_DEF);
	}

	// Set the current balance
	protected static boolean setBalance(Context context, int amount) {
		SharedPreferences account = context.getSharedPreferences(ACCOUNT_FILENM, 0);
		SharedPreferences.Editor ed = account.edit();
		boolean result = false;
		// Don't allow negative balances
		if (amount < 0)
			amount = 0;
		ed.putInt(OPT_BALANCE, amount);
		result = ed.commit();
		return result;
	}
	
	// Add to current balance (or subtract from current balance by passing in 
	// a negative number.
	protected static boolean addToBalance(Context context, int amount) {
		boolean result = false;
		result = BankAccount.setBalance(context, BankAccount.getBalance(context) + amount);
		return result;
	}
	
	// Check whether there are enough funds in the account to cover the
	// amount passed in as the argument.
	protected static boolean enoughFunds(Context context, int amount) {
		return (BankAccount.getBalance(context) - amount >= 0);
	}

}
