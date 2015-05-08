package android_talking_software.development.talking_tap_twice.accessible_view_extensions;

import android_talking_software.development.talking_tap_twice.Event.TouchListener;

/**
 * <h1>Class Overview</h1>
* <p></br></br>This interface defines methods for retrieving and setting the attributes of all implementing classes that are used by the TTT class to control the subclass program and provide verbal output.</br>
 * The only attribute that serves the same purpose over all implementing classes is:
 * <list>
 * <li>String method: the name of the method to be invoked when this View is selected.</br>
 * <list><li>Leaving this field set to null will cause no method to be invoked.</li>
 * <li>Writing "default" in this field will cause the default method to be invoked.</li>
* </list>
 * </li>
 * </list>
 * See documentation for each individual implementing class for information about the other attributes.
 * </br></br></p>
 * 
 * @author Nicole Torcolini
 *
 */
public interface AccessibleViewExtension 
{
	/**
	 * Returns the name of the method that this View invokes when selected. Returns null if no value has been entered.
	 * 
	 * @return The name of the method to be invoked when this View is selected.
	 */
	public String getMethod();
	/**
	*Sets the name of the method that this View invokes when selected.
*
* <list><li>Leaving this field set to null will cause no method to be invoked.</li>
 * <li>Writing "default" in this field will cause the default method to be invoked.</li>
* </list>
* @param method The name of the method to be invoked when this View is selected.
	*/
	public void setMethod(String method);
	/**
	 * See documentation for each individual implementing class.
	 */
	public String getWhenSelected();
	/**
	 * See documentation for each individual implementing class.
	 */
	public void setWhenSelected(String whenSelected);
	/**
	 * See documentation for each individual implementing class.
	 */
		public String getTapped();
		/**
		 * See documentation for each individual implementing class.
		 */
			public void setTapped(String tapped);
			
			/**
			 * See documentation for each individual implementing class.
			 */
			public String getLabel();
			/**
			 * See documentation for each individual implementing class.
			 */
			public void setLabel(String label);
/**
 * Called by the TouchListener to notify the View that it has been touched. Based on the time interval since the last touch, the View then decides how to respond.
 */
public void performAction();
/**
 * Notifies the TouchListener that this View has been selected, passing the TouchListener a phrase to speak and a method to invoke.
 */
public void performSelection();
/**
 * Invoked when the time interval since the last touch is short enough to trigger a selection. Notifies the TouchListener to call performSelection on the View that the TouchListener has stored as selected.
 */
public void performEntry();
/**
 * Notifies the TouchListener that this View has been tapped, passing the TouchListener a phrase to speak.
 */
public void performTap();
	/**
	 * Returns the TouchListener for this View.
	 * 
	 * @return The TouchListener for this View.
	 */
	public TouchListener getListener();
	/**
	 * Sets the TouchListener for this View.
	 * 
	 * @param listener The TouchListener for this View.
	 */
	public void setListener(TouchListener listener);
	/* (
	 * @see android.view.View#requestFocus()
	 */
	public boolean setFocus();
}
