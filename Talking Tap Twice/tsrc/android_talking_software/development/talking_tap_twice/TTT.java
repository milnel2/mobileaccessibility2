package android_talking_software.development.talking_tap_twice;
import java.util.HashMap;

import unimplemented_java_classes.timer.Timer;
import unimplemented_java_classes.timer.TimerListener;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android_talking_software.development.talking_tap_twice.Event.TouchEvent;
import android_talking_software.development.talking_tap_twice.Event.TouchListener;
import android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension;
import android_talking_software.development.talking_tap_twice.accessible_view_extensions.ButtonTTT;
import android_talking_software.development.talking_tap_twice.accessible_view_extensions.CheckBoxTTT;
import android_talking_software.development.talking_tap_twice.accessible_view_extensions.LinearLayoutTTT;
import android_talking_software.development.talking_tap_twice.accessible_view_extensions.TextViewTTT;


/**
 * <h1>Class Overview</h1>
 * <p></br></br>This class allows programmers to add self voicing functionality to their applications while also defining an accessible input method that allows the user to examine the contents of the screen without activation.</p>
 * <h2>Input Method</h2>
 * <p></br></br>The input method defined by the TTT class allows the user to examine the contents of the screen without activation. Tapping any of the controls on the screen causes the label of the control to be spoken. Tapping twice anywhere on the screen will cause the last selected control to be activated.</p>
<h2>General Structure</h2>
<p></br></br>The TTT class works by using the attributes specified in the various layout XML files to associate each control with a method to be invoked when that control is selected, a phrase to be spoken when that control is selected, and a phrase to be spoken when that control is tapped. The TTT class also allows the programmer to specify a TextView as the main display of the application, for which there are methods that allow the subclass to call tap, select, read, and spell; these methods can be overridden if the programmer desires other actions. 
The TTT class also allows the programmer to control how the TextToSpeech engine pronounces symbols, words, and abbreviations by associating the correct spelling with a spelling that the programmer knows that the TextToSpeech engine will pronounce in the desired way; for example, + and plus.</br></br></p>
<h2>Designing Subclasses</h2>
<p></br></br>Subclasses of the TTT class must contain certain elements, as defined below.</p>
<h3>Invoking Methods</h3>
<p></br></br>In order to invoke the methods associated with the various controls of the subclass, the invokeFrom Object of the TTT class must point to the running instance of the subclass. Programmers need to initialize invokeFrom somewhere in the code that is executed when the application starts, before the application receives input from the controls. Using newInstance to create an instance of the subclass does not work.</p>
<h3>Required Strings</h3>
<p></br></br>Strings with the following names that serve the following functions can be passed to the TTT. To do this, replace an android.widget.LinearLayout in a layout file with an android_talking_software.development.talking_tap_twice.accessible_view_extensions.LinearLayoutTTT and specify the different attributes.
<list>
<li>pronounciation_dictionary_entries: This string contains any text which the programmer wishes to have spoken in a way other than how the TextToSpeech engine would normally render it. Entries and elements of entries are separated by ~. For example, to have "+" spoken as "plus" and "/" spoken as divided by, this string would contain "+~plus~/~divided by".</li>
<li>default_selected: This string contains an optional phrase to always be spoken proceeding the phrase for the selected item, such as "entered", "pressed", or selected".</li>
<li>default_method: This string contains an optional method name that is invoked when controls are selected. It is useful if multiple controls invoke the same method. However, having all controls invoke the same method and then delegating the decision of what to do to the subclass is discouraged as having many controls can lead to complex, time consuming conditionals. This decision making should be left to the TTT class.</li>
</list>
</p>
<h3>Layout Files</h3>
<p></br></br>All layout files must contain a TextView with the android:id of "title". This is used as a reference point to gain access to the rest of the View hierarchy.</p>
<p>Layout files may contain an optional TextView with the android:id of "display". This TextView will be read when tapped, and its contents will be spelled when selected. These behaviors are also available to the subclass and can be changed by overriding the associated methods, but these behaviors will not be changed for the subclass by setting attributes for this TextView,.</p>
<p>All controls should be those defined in the android_talking_software.accessible_view_extensions package and should have the method, tapped, when_selected, and label attributes set as appropriate as described in the android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension interface and its implementing classes.</p>
<p>All layout files should contain a namespace definition that points to the local resources and that prefixes all AccessibleViewExtension attributes.</p>
<h3>Required Files</h3>
<p></br></br>The following required files are available from <a href="http://mobileaccessibility.googlecode.com/svn/trunk/Talking%20Tap%20Twice/res/">http://mobileaccessibility.googlecode.com/svn/trunk/Talking%20Tap%20Twice/res/</a></br></br>
<list>
<li>raw/click.mp3</li>
<li>values/attrs.xml</li>
</list>
</p>
<h3>Overriding Methods</h3>
<p></br></br>The spell, speak, and expandTextViewContents methods should not be overridden.</br>
Unless a desired functionality is not achieveable, overriding of the process methods is discouraged</br>
The tappedDisplay and selectDisplay methods may be overridden if a different behavior for the display is desired.</br></br></p>
<h3>Linking Resources</h3>
<p>The res folder of the subclass should be deleted. The subclass should reference the res folder of the TTT folder and the source code of the TTT.</br></br></p>
 * @author Nicole Torcolini</br></br>
 */
public abstract class TTT extends Activity implements TouchListener
{
	/**
	 * </br>Treated as the optional main display of the application.
	 */
	protected TextViewTTT display;
	/**
	 * </br>Acts as the target for invoking methods.
	 * 
	 * It points to the running instance of the Activity subclass. 
	 * Unlike the other parameters that are initialized through information in the xml, this parameter must be initialized manually by the programmer.
	 */
	protected Object invokeFrom;
	private OnTouchListener listener;
	private HashMap<String, String> pronounciationDictionary;
	private TextToSpeech tts;
	private AccessibleViewExtension selected, holding, eventView;
	private long time, diff;
	private String defaultSelected, phrase, defaultMethod;
	private StringBuffer expander;
	private Timer timer;
	private boolean usingMathematicalPronounciations;
	private FeedBackProvider provider;

	/** 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		init();
	}

	/* (*
	* This method is overridden so that each new view may be examined and its children loaded.
	* 
	  * @see android.app.Activity#setContentView(int)
	 */
	@Override
	public void setContentView(int layoutResID) 
	{
		super.setContentView(layoutResID);
	if (findViewById(R.id.display) != null)
setSelected(display = (TextViewTTT)findViewById(R.id.display));
		View v = findViewById(R.id.title);
		if (provider == null) provider = new FeedBackProvider(v.getContext());
		do
		{
			v = (View)v.getParent();
			if (v.getClass().equals(LinearLayoutTTT.class)) 
				initParameters((LinearLayoutTTT)v);
		}
		while (!v.getParent().getClass().equals(FrameLayout.class));
		setup(v);
	}

	private void setup(View v)
	{
		if ((v.getClass().equals(LinearLayout.class)) || (v.getClass().equals(LinearLayoutTTT.class)))
		{
			for (int index = 0, children = ((LinearLayout)v).getChildCount(); index < children; index++)
				setup(((LinearLayout)v).getChildAt(index));
		}
		else if (v.getClass().equals(TableLayout.class))
		{
			for (int index = 0, children = ((TableLayout)v).getChildCount(); index < children; index++)
				setup(((TableLayout)v).getChildAt(index));
		}
		else if (v.getClass().equals(TableRow.class))
		{
			for (int index = 0, children = ((TableRow)v).getChildCount(); index < children; index++)
				setup(((TableRow)v).getChildAt(index));
		}
		else if (v.getClass().getPackage().toString().endsWith("accessible_view_extensions"))
		{
			if (v.getClass() == ButtonTTT.class) setup((AccessibleViewExtension)((ButtonTTT)v));
			else if (v.getClass() == CheckBoxTTT.class) setup((AccessibleViewExtension)((CheckBoxTTT)v));
			else if (v.getClass() == TextViewTTT.class) setup((AccessibleViewExtension)((TextViewTTT)v));
		}
	}
	
	private void setup(AccessibleViewExtension v)
	{
		((View) v).setOnTouchListener(listener);
		((AccessibleViewExtension)v).setListener(this);
		if (Button.class.isAssignableFrom(v.getClass()))
	{
			if ((v.getLabel() == null) || (v.getLabel().equals(""))) v.setLabel(((TextView)v).getText().toString());
			v.setTapped(((v.getTapped() == null) | ((v.getTapped() != null) && (v.getTapped().length() == 0))) ? v.getLabel() : (v.getTapped().charAt(0) == '+') ? v.getLabel()+" "+v.getTapped().substring(1) : v.getTapped()+" "+v.getLabel());
			if ((v.getWhenSelected() != null) && (v.getWhenSelected().equals("default"))) v.setWhenSelected(defaultSelected+" "+v.getLabel());
			else if ((v.getWhenSelected() != null) && (v.getWhenSelected().equals("/"))) v.setWhenSelected(null);
			else v.setWhenSelected(((v.getWhenSelected() == null) | ((v.getWhenSelected() != null) && (v.getWhenSelected().length() == 0))) ? v.getLabel() : (v.getWhenSelected().charAt(0) == '+') ? v.getLabel()+" "+v.getWhenSelected().substring(1) : v.getWhenSelected()+" "+v.getLabel());
	}
				if (v.getMethod() != null)
						{
						if(v.getMethod().equals("default")) v.setMethod(defaultMethod);
						else if (v.getMethod().length() == 0) v.setMethod(null);
						}
	}

	private void init()
	{
		initOther();
		initTimer();
	}
	
	private void initParameters(LinearLayoutTTT l)
	{
		initDictionary(l.getPronounciationDictionaryEntries());
		initStrings(l);
	}
	
	private void initDictionary(String entries)
	{
		pronounciationDictionary = new HashMap<String, String>();
		if (entries == null) return;
		String[] splitter = entries.split("~");
		for (int index = splitter.length-2; index >= 0; index-= 2)
			pronounciationDictionary.put(splitter[index], splitter[index+1]);
	}
	
	private void initStrings(LinearLayoutTTT l)
	{
		defaultMethod = l.getDefaultMethod();
		defaultSelected = l.getDefaultSelected();
	}

	private void initOther()
	{
		time = System.currentTimeMillis();
		diff = -1;
			listener = new OnTouchListener()
			{

				public boolean onTouch(View v, MotionEvent event) 
				{
					return onTouchAction(v, event);
				}
			};
		tts = new TextToSpeech(this, new OnInitListener()
		{
			public void onInit(int status) 
			{
				if (status == TextToSpeech.SUCCESS) speak("Ready");
	}
		});
	}

	private void initTimer()
	{
		timer = new Timer(300, new TimerListener()
		{

			public void actionPerformed() 
			{
			executeTimerTask();	
			}
		}, false);
	}
	
	private void executeTimerTask()
	{
		speak(phrase);
		clearTimer();
	}

	/**
	 *  	  This method is overridden to allow the addition of a spoken "Good-bye" message as well as the destruction of the TextToSpeech engine.
*
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() 
	{
		if (tts != null) 
		{
			speak("Good-bye");
			tts.stop();            
			tts.shutdown();        
		}
		super.onDestroy();
	}

	private boolean onTouchAction(View v, MotionEvent event)
	{
		if (v.getClass() == ButtonTTT.class) eventView = (AccessibleViewExtension)((ButtonTTT)v);
		else if (v.getClass() == CheckBoxTTT.class) eventView = (AccessibleViewExtension)((CheckBoxTTT)v);
		else if (v.getClass() == TextViewTTT.class) eventView = (AccessibleViewExtension)((TextViewTTT)v);
		if (event.getAction() == MotionEvent.ACTION_UP) up();
		else if (event.getAction() == MotionEvent.ACTION_DOWN) down();
		return false;
	}

	private void up()
	{
		time = System.currentTimeMillis();
	}

	private void down()
	{
		diff = System.currentTimeMillis()-time;
if (diff > 1000)
		{
	setSelected(eventView);
	if (eventView == display) tapDisplay();
	else
	{
					eventView.performAction();
		setTimer();
	}
		}
else if (diff <= 1000)
{
	selected.performAction();
	resetSelected(selected);
	clearTimer();
	}
}

	/**
	 * Sets the currently selected View
	 * 
	 * @param v the new View that is selected
	 */
	protected void setSelected(AccessibleViewExtension v)
	{
		selected = holding;
		holding = v;
	}

	private void clearTimer()
	{
		timer.stop();
	}

	private void setTimer()
	{
timer.start();
	}

	/**
	 * Simplifies the process of passing a phrase to the TextToSpeech engine.
	 * 
	 * This method is meant only to provide access to functionality and should not be overridden.
	 * 
	 * @param speak the phrase to be spoken
	 */
	protected void speak(String speak)
	{
		if (speak != null) tts.speak(speak, TextToSpeech.QUEUE_ADD, null);
	}

	/* 
	 * @see android_talking_software.development.talking_tap_twice.Event.TouchListener#convert(java.lang.CharSequence)
	 */
	public String convert(CharSequence text)
	{
		expander = new StringBuffer(text);
		char aChar;
		for (int pos = 0; pos < expander.length(); pos++)
		{
			aChar = expander.charAt(pos);
			if ((Character.isDigit(aChar)) || (Character.isLetter(aChar)) || (aChar == '.')) continue;
			switch (aChar)
			{
				case '-':
					if (usingMathematicalPronounciations) expander.replace(pos, pos+1, " minus ");
					break;
				case '*':
					if (usingMathematicalPronounciations) expander.replace(pos, pos+1, " times ");
					break;
				case '/':
					if (usingMathematicalPronounciations) expander.replace(pos, pos+1, " divided by ");
					break;
				case '(':
					expander.replace(pos, pos+1, " open parenthesis ");
					break;
				case ')':
					expander.replace(pos, pos+1, " close parenthesis ");
					break;
				case '^':
					if (usingMathematicalPronounciations) expander.replace(pos, pos+1, " to the power of ");
					break;
			}
		}
		if (pronounciationDictionary.isEmpty()) return expander.toString();
		for (String str : pronounciationDictionary.keySet())
		{
			for (int pos = expander.indexOf(str); pos > -1; pos = expander.indexOf(str, pos+1+pronounciationDictionary.get(str).length()))
				expander.replace(pos, pos+str.length(), pronounciationDictionary.get(str));
		}
		return expander.toString();
	}

	/* 
	 * @see android_talking_software.development.talking_tap_twice.Event.TouchListener#expandTextViewContents(java.lang.CharSequence)
	 */
	public String expandTextViewContents(CharSequence text)
	{
		return spell(convert(text));
	}

	/**
	 * 
	 * Inserts spaces into the given String so that passing the String into the TextToSpeech engine will spell the String.
	 * 
	 * This method is meant only to provide access to functionality and should not be overridden.
	 * 
	 * @param text the String into which to insert spaces
	 * @return the new String with spaces
	 */
	public String spell(String text)
	{
		expander = new StringBuffer(display.getText());
		for (int pos = 0; pos < expander.length(); pos++)
		{
			if (Character.isDigit(expander.charAt(pos)))
			{
				expander.insert(pos, " ");
				pos++;
			}
			else if (expander.charAt(pos) == '.') expander.replace(pos, pos+1, " point ");
		}
		return expander.toString();
	}

	/**
	 * Same as setSelected except does not require a second tap to become valid.
	 * @param the new View that is selected
	 */
	protected void resetSelected(AccessibleViewExtension v)
	{
		(holding = selected = v).setFocus();
	}

	/**
	 * Returns the currently selected View
	 * 
	 * @return the currently selected View
	 */
	protected AccessibleViewExtension getSelected() 
	{
		return selected;
	}

	/**
	 * Returns the name of the default method to be invoked when a control is selected.
	 * 
 * @return the name of the default method to be invoked when a control is selected
	 */
	protected String getDefaultMethod() 
	{
		return defaultMethod;
	}

	/**
	 * Returns the default phrase to be spoken when a control is selected.
	 * 
 * @return the default phrase to be spoken when a control is selected
	 */
	protected String getDefaultSelected() 
	{
		return defaultSelected;
	}

	/**
	 * Returns a boolean indicating whether or not the TextToSpeech engine speaks the names of mathematical symbols that are normally punctuation marks.
	 */
	protected boolean isUsingMathematicalPronounciations() 
	{
		return usingMathematicalPronounciations;
	}

	/**
	 * Determines whether or not the TextToSpeech engine speaks the names of mathematical symbols that are normally punctuation marks.
	 *
	 * These symbols are:
	 * <list>
	 * <li> -: mminus</li>
	 * <li>*: times</li>
	 * <li>/: divided by</li>
	 * <li>^: to the power of</li>
	 * </list>
	 * These symbols are not stored in the pronounciation dictionary. Overriding them by adding entries for them will always result in the dictionary entry having priority over the original value, regardless of whether this feature is turned on or off.
	 */
	protected void setUsingMathematicalPronounciations(
			boolean usingMathematicalPronounciations) 
	{
		this.usingMathematicalPronounciations = usingMathematicalPronounciations;
	}
	
	/**
	 * Adds an entry to the pronounciation dictionary
	 * 
	 * If the pronounciation dictionary already contains a pronounciation for the phrase, the current value is overridden without warning or confirmation.
	 * 
	 * @param phrase the original phrase to be pronounced differently
	 * @param pronounciation a phrase that will be pronounced in the desired way
	 */
	protected void addPronounciationDictionaryEntry(String phrase, String pronounciation)
	{
		pronounciationDictionary.put(phrase, pronounciation);
	}
	
	/**
	 * Removes an entry from the pronounciation dictionary.
	 * 
	 * @param phrase phrase to be removed
	 */
	protected void removePronounciationDictionaryEntry(String phrase)
	{
		pronounciationDictionary.remove(phrase);
	}
	
	/**
	 *Returns a boolean indicating whether or not the given phrase is in the pronounciation dictiionary.
	 */
	protected boolean hasPhraseInPronounciationDictionary(String phrase)
	{
		return pronounciationDictionary.containsKey(phrase);
	}
	
	/**
	 * Returns the pronounciation that is stored in the pronounciation dectionary for the given phrase.
	 *
	 * If there is not an entry in the pronounciation dictionary for the given phrase, the original phrase is returned. However, this may not be what the TextToSpeech engine actually speaks. Rather, it is what is passed into the TextToSpeech engine. Programmers using words not found in the English dictionary, unusal names or symbols should experiment on their own with speech output of such inputs.
	 * @param phrase phrase for which the pronounciation is desired
	 * @return the pronounciation associated with the phrase
	 */
	protected String getPronounciationForPhrase(String phrase)
	{
		return (pronounciationDictionary.containsKey(phrase)) ? pronounciationDictionary.get(phrase) : phrase;
	}

	/* 
	 * @see android_talking_software.development.talking_tap_twice.Event.TouchListener#processSelection(android_talking_software.development.talking_tap_twice.Event.TouchEvent)
	 */
	public void processSelection(TouchEvent e) 
	{
			speak(e.speak);
		if (e.method != null)
			try
		{
		invokeFrom.getClass().getMethod(e.method, (Class[])null).invoke(invokeFrom, (Object[])null);
		} 
		catch (Exception ex) 
		{
ex.printStackTrace();
ex.getCause().printStackTrace();
		}
	}

	/* 
	 * @see android_talking_software.development.talking_tap_twice.Event.TouchListener#processEntry()
	 */
	public void processEntry()
	{
		selected.setFocus();
		provider.feedBack();
		clearTimer();
		selected.performSelection();
	}
	
	/* 
	 * @see android_talking_software.development.talking_tap_twice.Event.TouchListener#processTap(android_talking_software.development.talking_tap_twice.Event.TouchEvent)
	 */
	public void processTap(TouchEvent e) 
	{
		provider.feedBack();
phrase = e.speak;
	}
	
	/* 
	 * @see android_talking_software.development.talking_tap_twice.Event.TouchListener#isSearching()
	 */
	public boolean isSearching()
	{
		return !timer.isRunning();
	}
	
	/**
	 * Speaks the contents of the display 
	 */
	protected void readDisplay()
	{
		speak((display.getLabel() != null) ? display.getLabel()+" "+convert(display.getText()) : convert(display.getText()));
	}
	
	/**
	 * Allows the subclass to easily tap the display. The default action is to read the display.
	 */
	protected void tapDisplay()
	{
		provider.feedBack();
		readDisplay();
	}
	
	/**
	 * Allows the subclass to select the display. The default action is to spell out the contents of the display. 
	 */
	protected void selectDisplay()
	{
		spellDisplay();
		resetSelected(display);
	}
	
	/**
	 * Allows the subclass to spell the contents of the display.
	 */
	protected void spellDisplay()
	{
		speak((display.getLabel() != null) ? display.getLabel()+" "+expandTextViewContents(display.getText()) : expandTextViewContents(display.getText()));
	}

}