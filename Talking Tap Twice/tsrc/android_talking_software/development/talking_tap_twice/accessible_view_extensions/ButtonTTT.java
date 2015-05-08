package android_talking_software.development.talking_tap_twice.accessible_view_extensions;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.Button;
import android_talking_software.development.talking_tap_twice.R;
import android_talking_software.development.talking_tap_twice.Event.TouchEvent;
import android_talking_software.development.talking_tap_twice.Event.TouchListener;

/**
* <h1>Class Overview</h1>
 * <p></br></br>This class is an extension of android.widget.Button that allows for the use of the attributes specified in the AccessibleViewExtensions interface.</p>
 * 	 * <p>These attributes, including method as specified in the AccessibleViewExtension documentation, are:
 	 * <list>
* <li>String label: The phrase that is passed to the TouchListener as the label of the Button.
	 * <list>
	 * 	 <li>This field is particularly useful if the text of the button is an abbreviation, but the TTT should speak the expansion instead of the abbreviation.</li>
	 * <li>Leaving this field set to null will result in the text of the Button being spoken.</li>
	 * </list>
	 * </li>
	 * <li>String tapped: The phrase to be spoken along with the label phrase when this Button is tapped.
	 * <list>
	 * 	 <li>Placing a "+" at the beginning of this field will result in the tapped phrase being spoken after the label phrase.</li>
	 * <li>Leaving this field set to null will result in only the label phrase being spoken.</li>
	 * </list>
	 * </li>
	* <li>String whenSelected: The phrase to be spoken when this Button is selected along with the label phrase.</br>
	 * <list>
	 * 	 <li>Writing a "/" in this field will cause nothing to be spoken . This is useful if different phrases should be spoken based on the state of the Activity. These different phrases can be spoken in the method that invoked by this Button by using the speak method of TTT.</li>
* <li>Writing "default" in the field will result in the default when selected phrase being spoken followed by the tapped phrase.</li>
	 * <li>Placing a "+" at the beginning of this field will result in the tapped phrase being spoken after the label phrase.</li>
	 * <li>Leaving this field set to null will result in only the tapped phrase being spoken.</li>
	 * </list>
	 * </li>
	 * </list>
	 * </br></br></p>
	 * 
 * @author Nicole
 */
public class ButtonTTT extends Button implements AccessibleViewExtension 
{
	private String tapped, whenSelected, method, label;
	private TouchListener listener;

	/**
	 *
	 * @see android.widget.Button#Button(android.content.Context)
	 */
	public ButtonTTT(Context context) 
	{
		super(context);
	}

	/**
	 *
	 * @see android.widget.Button#Button(android.content.Context, android.util.AttributeSet)
	 */
	public ButtonTTT(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		init(attrs);
	}

	/**
	 *
	 * @see android.widget.Button#Button(android.content.Context, android.util.AttributeSet, int)
	 */
	public ButtonTTT(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		init(attrs);
	}

	private void init(AttributeSet attrs)
	{
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ButtonTTT);
		method = a.getString(R.styleable.ButtonTTT_method);
		label = a.getString(R.styleable.ButtonTTT_label);
		tapped = a.getString(R.styleable.ButtonTTT_tapped);
		whenSelected = a.getString(R.styleable.ButtonTTT_whenSelected);
	}

	/**
	 * Returns the phrase that is passed to the TouchListener as the label of the Button. Returns null if no value has been entered.
	 * 
	 * @return The phrase that is passed to the TouchListener as the label of the Button.
	 */
public String getLabel() 
{
	return label;
}

	/*
	 *  
	 * @see android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension#getListener()
	 */
	public TouchListener getListener() 
	{
		return listener;
	}

	/*
	 *  
	 * @see android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension#getMethod()
	 */
	public final String getMethod() 
	{
		return method;
	}

	/**
	 * Returns the phrase to be spoken along with the label phrase when this Button is tapped. Returns null if no value has been entered.
	 * 
	 * @return The phrase to be spoken along with the label phrase when this Button is tapped.
	 */ 
		public final String getTapped() 
		{
			return tapped;
		}

		/**
		 * Returns the phrase to be spoken along with the label phrase when this Button is selected. Returns null if no value has been entered.
		 * 
		 * @return The phrase to be spoken along with the label phrase when this Button is selected.
		 */ 
public final String getWhenSelected() 
		{
			return whenSelected;
		}

/**
 * Set the phrase that is passed to the TouchListener as the label of the Button.
 * 
 * <list>
 * 	 <li>This field is particularly useful if the text of the button is an abbreviation, but the TTT should speak the expansion instead of the abbreviation.</li>
 * <li>Leaving this field set to null will result in the text of the Button being spoken.</li>
 * </list>
 *@param label The phrase that is passed to the TouchListener as the label of the Button.
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
 * Sets the phrase to be spoken along with the label phrase when this Button is tapped.
 * 
 	 * <list>
	 * 	 <li>Placing a "+" at the beginning of this field will result in the tapped phrase being spoken after the label phrase.</li>
	 * <li>Leaving this field set to null will result in only the label phrase being spoken.</li>
	 * </list>
* @param tapped The phrase to be spoken along with the label phrase when this Button is tapped.
 */ 
	public void setTapped(String tapped) 
{
	this.tapped = tapped;
}

	/**
	 * Sets the phrase to be spoken along with the label phrase when this Button is selected.
	 * 
		 * 	 <li>Writing a "/" in this field will cause nothing to be spoken . This is useful if different phrases should be spoken based on the state of the Activity. These different phrases can be spoken in the method that invoked by this View by using the speak method of TTT.</li>
*<li>Writing "default" in the field will result in the default when selected phrase being spoken followed by the label phrase.</li>
	 * <li>Placing a "+" at the beginning of this field will result in the whenSelected phrase being spoken after the label phrase.</li>
	 * <li>Leaving this field set to null will result in only the label phrase being spoken.</li>
	 * </list>
 * @param whenSelected The phrase to be spoken along with the label phrase when this Button is selected.
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
	 * @see android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension#performSelection()
	 */
	public void performSelection() 
{
		TouchEvent e = new TouchEvent();
e.speak = whenSelected;
e.method = method;
listener.processSelection(e);
}

	/* 
	 * @see android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension#performTap()
	 */
	public void performTap() 
		{
			TouchEvent e = new TouchEvent();
			e.speak = tapped;
			listener.processTap(e);
		}

	/* 
	 * This method is overridden to prevent the triggering of an AccessibilityEvent and, consequently, conflict between the local and system accessibility features.
	 * 
	 * @see android.view.View#performClick()
	 */
	public boolean performClick() 
	{
		return false;
	}

	/* 
	 * @see android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension#setFocus()
	 */
	public final boolean setFocus()
{
return 	super.requestFocus();
}

/*
	 * Button does not implement equals, so comparisons are based only on if the two Objects are the same Object,  if the second Object is null, if the second Object is an instance of ButtonTTT, and if the four attributes defined in AccessibleViewExtension are the same; all other attributes that are specific to Button and all superclasses are not considered.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof ButtonTTT)) return false;
		ButtonTTT other = (ButtonTTT) obj;
		if (!method.equals(other.method))
			return false;
		if (!tapped.equals(other.tapped))
			return false;
		if (!whenSelected.equals(other.whenSelected))
			return false;
		return true;
	}
	
}