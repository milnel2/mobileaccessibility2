/**
 * 
 */
package android_talking_software.development.talking_tap_twice.accessible_view_extensions;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android_talking_software.development.talking_tap_twice.R;

/**
 * <h1>Class Overview</h1>
 * <p></br></br>This class is an extension of android.widget.LinearLayout that allows for the Layout to pass the parameters specified in the ParametersPasser interface.</br></br></p>
 * 
 * @author Nicole
 *
 */
public class LinearLayoutTTT extends LinearLayout implements ParametersPasser
{
	private String defaultMethod, defaultSelected, pronounciationDictionaryEntries;
	
	/**
	 *
	 * @see android.widget.LinearLayout#LinearLayout(android.content.Context)
	 */
		public LinearLayoutTTT(Context context) 
	{
		super(context);
	}

		/**
		 *
		 * @see android.widget.LinearLayout#LinearLayout(android.content.Context, android.util.AttributeSet)
		 */
public LinearLayoutTTT(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LinearLayoutTTT);
		defaultMethod = a.getString(R.styleable.LinearLayoutTTT_defaultMethod);
		defaultSelected = a.getString(R.styleable.LinearLayoutTTT_defaultSelected);
		pronounciationDictionaryEntries = a.getString(R.styleable.LinearLayoutTTT_pronounciationDictionaryEntries);
		}

	/* 
	 * @see android_talking_software.development.talking_tap_twice.talking_tap_twice.accessible_view_extensions.ParametersPasser#getDefaultMethod()
	 */
	public String getDefaultMethod() 
	{
		return defaultMethod;
	}

	/* 
	 * @see android_talking_software.development.talking_tap_twice.accessible_view_extensions.ParametersPasser#getDefaultSelected()
	 */
	public String getDefaultSelected() 
	{
		return defaultSelected;
	}

	/* 
	 * @see android_talking_software.development.talking_tap_twice.accessible_view_extensions.ParametersPasser#getPronounciationDictionaryEntries()
	 */
	public String getPronounciationDictionaryEntries() 
	{
		return pronounciationDictionaryEntries;
	}

}
