package android_talking_software.development.talking_tap_twice.accessible_view_extensions;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;
import android_talking_software.development.talking_tap_twice.R;
import android_talking_software.development.talking_tap_twice.Event.TouchEvent;
import android_talking_software.development.talking_tap_twice.Event.TouchListener;

/**
 * <h1>Class Overview</h1>
 * <p></br></br>This class is an extension of android.widget.TextView that allows for the use of the attributes specified in the AccessibleViewExtensions interface.</p>
* <p>These attributes, including method as specified in the AccessibleViewExtension documentation, are:
 * <list>
* <li>String label: The optional phrase to speak before speaking the contents of this TextView when tapped or selected.</br>
 * </li>
 * <li>String tapped: The optional phrase to be spoken instead of the contents of the TextView when this TextView is tapped.</br>
 * </li>
* <li>String whenSelected: The optional phrase to be spoken instead of spelling the contents of the TextView when this TextView is selected.</br>
 * </li>
 * </list>
 * </br></br></p>
 * @author Nicole Torcolini
 */

public class TextViewTTT extends TextView implements AccessibleViewExtension 
{
	private String tapped, whenSelected, method, label;
	private TouchListener listener;

	/**
	 *
	 * @see android.widget.TextView#TextView(android.content.Context)
	 */
	public TextViewTTT(Context context) 
	{
		super(context);
	}

	/**
	 *
	 * @see android.widget.TextView#TextView(android.content.Context, android.util.AttributeSet)
	 */
	public TextViewTTT(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		init(attrs);
	}

	/**
	 *
	 * @see android.widget.TextView#TextView(android.content.Context, android.util.AttributeSet, int)
	 */
	public TextViewTTT(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		init(attrs);
	}

	/**
	 * Returns the optional phrase to speak before speaking the contents of this TextView when tapped or selected. Returns null if no value has been entered.
	 * 
	 * @return The optional phrase to speak before speaking the contents of this TextView when tapped or selected.
	 */ 
	public final String getLabel() 
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
	 * @see android_talking_software.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension#getMethod()
	 */
	public final String getMethod() 
	{
		return method;
	}

	
		/*
		 * Returns the optional phrase to be spoken instead of the contents of the TextView when this TextView is tapped.  Returns null if no value has been entered.
		 *  
		 * @return The optional phrase to be spoken instead of the contents of the TextView when this TextView is tapped.  Returns null if no value has been entered.
		 */
		public final String getTapped() 
		{
			return tapped;
		}

		/*
 * Returns the optional phrase to be spoken instead of spelling the contents of the TextView when this TextView is selected. Returns null if no value has been entered.
 * 
 * @return The optional phrase to be spoken instead of spelling the contents of the TextView when this TextView is selected.
 */
			public final String getWhenSelected() 
{
			return whenSelected;
}

	private void init(AttributeSet attrs)
	{
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TextViewTTT);
		method = a.getString(R.styleable.TextViewTTT_method);
		label = a.getString(R.styleable.TextViewTTT_label);
		tapped = a.getString(R.styleable.TextViewTTT_tapped);
		whenSelected = a.getString(R.styleable.TextViewTTT_whenSelected);
	}

	/**
	 * Sets the optional phrase to speak before speaking the contents of this TextView when tapped or selected.
	 * 
	 * @param label The optional phrase to speak before speaking the contents of this TextView when tapped or selected.
	 */ 
	public void setLabel(String label) 
	{
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

/*
 * Sets the optional phrase to be spoken instead of the contents of the TextView when this TextView is tapped.
 * 
 * @param tapped The optional phrase to be spoken instead of the contents of the TextView when this TextView is tapped.
 */
public void setTapped(String tapped) 
{
this.tapped = tapped;
}

	/*
 * Sets the optional phrase to be spoken instead of spelling the contents of the TextView when this TextView is selected.
 * 
 * @param whenSelected The optional phrase to be spoken instead of spelling the contents of the TextView when this TextView is selected.
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
		e.speak = (whenSelected != null) ? whenSelected : (label != null) ?label+" "+listener.convert(getText()) : listener.convert(getText());
		e.method = method;
listener.processSelection(e);
}

	/* 
	 * @see android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension#performTap()
	 */
	public void performTap() 
		{
			TouchEvent e = new TouchEvent();
			e.speak = (tapped != null) ? tapped : (label != null) ?label+" "+listener.convert(getText()) : listener.convert(getText());
	listener.processTap(e);
		}
	
	/*
	* This method is overridden to prevent the triggering of an AccessibilityEvent and, consequently, conflict between the local and system accessibility features.
		 * 	 *  
		 * @see android.view.View#performClick()
		 */
		@Override
		public boolean performClick() 
		{
			return false;
		}

		/* 
		 * @see android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension#setFocus()
		 */
		public boolean setFocus() 
		{
			return super.requestFocus();
		}
	
		/*
	 * TextView does not implement equals, so comparisons are based only on if the two Objects are the same Object,  if the second Object is null, if the second Object is an instance of TextViewTTT, and if the four attributes defined in AccessibleViewExtension are the same; all other attributes that are specific to TextView and all superclasses are not considered.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof TextViewTTT)) return false;
		TextViewTTT other = (TextViewTTT) obj;
		if (!method.equals(other.method))
			return false;
		if (!tapped.equals(other.tapped))
			return false;
		if (!whenSelected.equals(other.whenSelected))
			return false;
		return true;
	}
	
}