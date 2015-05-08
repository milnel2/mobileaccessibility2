package android_talking_software.development.talking_tap_twice.accessible_view_extensions;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android_talking_software.development.talking_tap_twice.R;
import android_talking_software.development.talking_tap_twice.Event.TouchEvent;
import android_talking_software.development.talking_tap_twice.Event.TouchListener;

/**
 * <h1>Class Overview</h1>
 * <p></br></br>This class is an extension of android.widget.CheckBox that allows for the use of the attributes specified in the AccessibleViewExtensions interface.</p>
* <p>These attributes, including method as specified in the AccessibleViewExtension documentation, are:
	 * <list>
* <li>String label: The phrase that is passed to the TouchListener as the label of the CheckBox.</li>
	 * <list>
	 * 	 <li>This field is particularly useful if the text of the CheckBox is an abbreviation, but the TTT should speak the expansion instead of the abbreviation.</li>
	 * <li>Leaving this field set to null will result in the text of the CheckBox being spoken.</li>
	 * </list>
	 * </li>
	 * <li>String tapped: The phrase to be spoken along with the label phrase when this CheckBox is tapped.</li>
	 * <list>
	 * 	 <li>Placing a "+" at the beginning of this field will result in the tapped phrase being spoken after the label phrase.</li>
	 * <li>Leaving this field set to null will result in only the label phrase being spoken.</li>
	 * </list>
	 * </li>
*	<li>String whenSelected: The phrase to be spoken when this CheckBox is selected along with the label phrase.</br>
	 * <list>
	 * 	 <li>Writing a "/" in this field will cause nothing to be spoken . This is useful if different phrases should be spoken based on the state of the Activity. These different phrases can be spoken in the method that invoked by this CheckBox by using the speak method of TTT.</li>
* <li>Writing "default" in the field will result in the default when selected phrase being spoken followed by the tapped phrase.</li>
	 * <li>Placing a "+" at the beginning of this field will result in the tapped phrase being spoken after the label phrase.</li>
	 * <li>Leaving this field set to null will result in only the tapped phrase being spoken.</li>
	 * </list>
	 * </li>
	 * </list>
	 * </br></br></p>
  * @author Nicole Torcolini
 *
 */
public class CheckBoxTTT extends CheckBox implements AccessibleViewExtension
{
	private String tapped, whenSelected, method, label;
	private TouchListener listener;

	/**
	 *
	 * @see android.widget.CheckBox#CheckBox(android.content.Context)
	 */
	public CheckBoxTTT(Context context) 
	{
		super(context);
	}

	/**
	 *
	 * @see android.widget.CheckBox#CheckBox(android.content.Context, android.util.AttributeSet)
	 */
	public CheckBoxTTT(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		init(attrs);
	}

	/*
	 *
	 * @see android.widget.CheckBox#CheckBox(android.content.Context, android.util.AttributeSet, int)
	 */
	public CheckBoxTTT(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		init(attrs);
	}

	private void init(AttributeSet attrs)
	{
		android.content.res.TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CheckBoxTTT);
		method = a.getString(R.styleable.CheckBoxTTT_method);
		label = a.getString(R.styleable.CheckBoxTTT_label);
		tapped = a.getString(R.styleable.CheckBoxTTT_tapped);
		whenSelected = a.getString(R.styleable.CheckBoxTTT_whenSelected);
	}

		/**
	 * Returns the phrase that is passed to the TouchListener as the label of the CheckBox. Returns null if no value has been entered.
	 * 
	 * @return The phrase that is passed to the TouchListener as the label of the CheckBox.
	 */
	public String getLabel() 
	{
		return label;
	}

	/* 
	 * @see android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension#getListener()
	 */
	public TouchListener getListener() 
	{
		return listener;
	}

	/* *
	 * 
	 * @see android_talking_software.talking_tap_twice.accessible_view_extensions.AccessibleViewExtensions#getMethod()
	 */
	public final String getMethod() 
	{
		return method;
	}

	/**
	 * Returns the phrase to be spoken along with the label phrase when this CheckBox is tapped. Returns null if no value has been entered.
	 * 
	 * @return The phrase to be spoken along with the label phrase when this CheckBox is tapped.
	 */ 
	public final String getTapped() 
	{
		return tapped;
	}

	/**
	 * Returns the phrase to be spoken along with the label phrase when this CheckBox is selected. Returns null if no value has been entered.
	 * 
	 * @return The phrase to be spoken along with the label phrase when this CheckBox is selected.
	 */ 
	public final String getWhenSelected() 
	{
		return whenSelected;
	}

	/**
	 * Sets the phrase that is passed to the TouchListener as the label of the CheckBox.
	 * 
	 * <list>
	 * 	 <li>This field is particularly useful if the text of the CheckBox is an abbreviation, but the TTT should speak the expansion instead of the abbreviation.</li>
	 * <li>Leaving this field blank will result in the text of the CheckBox being spoken.</li>
	 * </list>
	 *@param label The phrase that is passed to the TouchListener as the label of the CheckBox.
	 */
	public void setLabel(String label) 
	{
		if (this.label != null)
		{
			if ((tapped != null) && (tapped.contains(this.label))) tapped.replaceFirst(this.label, label);
			if ((whenSelected != null) && (whenSelected.contains(this.label))) whenSelected.replaceFirst(this.label, label);
		}
		this.label = label;
	}

	/* 
	 * @see android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension#setListener(android_talking_software.development.talking_tap_twice.Event.TouchListener)
	 */
	public void setListener(TouchListener listener) 
	{
		this.listener = listener;
	}

	/* 
	 * @see android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension#setMethod(java.lang.String)
	 */
	public void setMethod(String method) 
	{
		this.method = method;
	}

	/**
	 * Sets the phrase to be spoken along with the label phrase when this CheckBox is tapped.
	 * 
	 * <list>
	 * 	 <li>Placing a "+" at the beginning of this field will result in the tapped phrase being spoken after the label phrase.</li>
	 * <li>Leaving this field set to null will result in only the label phrase being spoken.</li>
	 * </list>
*@param tapped The phrase to be spoken along with the label phrase when this CheckBox is tapped.
	 */ 
	public void setTapped(String tapped) 
	{
		this.tapped = tapped;
	}

	/**
	 * Sets the phrase to be spoken along with the label phrase when this CheckBox is selected.
	 * 
	 * 		* 	 <li>Writing a "/" in this field will cause nothing to be spoken . This is useful if different phrases should be spoken based on the state of the Activity. These different phrases can be spoken in the method that invoked by this View by using the speak method of TTT.</li>
*<li>Writing "default" in the field will result in the default when selected phrase being spoken followed by the label phrase.</li>
	 * <li>Placing a "+" at the beginning of this field will result in the whenSelected phrase being spoken after the label phrase.</li>
	 * <li>Leaving this field set to null will result in only the label phrase being spoken.</li>
	 * </list>
*@param whenSelected The phrase to be spoken along with the label phrase when this CheckBox is selected.
	 */ 
	public void setWhenSelected(String whenSelected) 
	{
		this.whenSelected = whenSelected;
	}

/* 
	 * @see android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension#performAction()
	 */
	public void performAction()
	{
		if (listener.isSearching()) performTap();
		else performEntry();
	}

		/* 
	 * @see android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension#performEntry()
	 */
	public void performEntry() 
	{
		listener.processEntry();	
	}

	/*
	 * Includes toggling the state of the CheckBox.
	 *  
	 * @see android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension#performSelection()
	 */
	public void performSelection() 
	{
		toggle();
		TouchEvent e = new TouchEvent();
		e.method = method;
		e.speak = isChecked() ? whenSelected+" checked" : whenSelected+" not checked";
		listener.processSelection(e);
	}

	/* 
	 * @see android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension#performTap()
	 */
	public void performTap() 
	{
		TouchEvent e = new TouchEvent();
		e.speak = isChecked() ? tapped+" checked" : tapped+" not checked";
	}

	/*
	 * This method is overridden to prevent the triggering of an AccessibilityEvent and, consequently, conflict between the local and system accessibility features.
	 *  
	 * @see android.widget.CompoundButton#performClick()
	 */
	@Override
	public boolean performClick() 
	{
		return false;
	}

	/* (
	 * @see android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension#setFocus()
	 */
	public boolean setFocus() 
	{
		return super.requestFocus();
	}

	/*
	 * CheckBox does not implement equals, so comparisons are based only on if the two Objects are the same Object,  if the second Object is null, if the second Object is an instance of CheckBoxTTT, and if the four attributes defined in AccessibleViewExtension are the same; all other attributes that are specific to CheckBox and all superclasses are not considered.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) 
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof CheckBoxTTT)) return false;
		CheckBoxTTT other = (CheckBoxTTT) obj;
		if (!method.equals(other.method))
			return false;
		if (!tapped.equals(other.tapped))
			return false;
		if (!whenSelected.equals(other.whenSelected))
			return false;
		return true;
	}

}