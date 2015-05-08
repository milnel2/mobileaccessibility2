package cs.washington.edu.buddies;

/* Implemented by classes that want to display several
 * toasts in a row but need to cancel those toasts if
 * the user takes an action that eliminates the need for
 * the remaining toasts to be displayed.
 */

public interface ToastDisplay {
	
	public void displayToast();

}
