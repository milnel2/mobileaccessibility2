package android_talking_software.development.talking_tap_twice.Event;

/**
 * <h1>Class Overview</h1>
 * <p></br></br>The listener interface for receiving touch events fired by android_talking_software.development.talking_tap_twice.accessible_view_extensions.AccessibleViewExtension. 
 * The class that is interested in processing an touch event implements this interface, and the object created with 
 * that class is registered with an 
 * AccessibleViewExtension, using the setListener method of the AccessibleViewExtension. When the touch event occurs, the associated process method of the Object is invoked.</br></br></p>
 * 
 * @author Nicole
 */
public interface TouchListener 
{
/**
 * Notifies the TouchListener that an AccessibleViewExtension has been tapped.
 * 
 * @param e A TouchEvent that contains the phrase for the TouchListener to speak.
 */
public void processTap(TouchEvent e);
/**
 * Notifies the TouchListener that an AccessibleViewExtension has been double tapped and that the TouchListener is to invoke the performSelection method of the AccessibleViewExtension that it has stored as selected.
 */
public void processEntry();
/**
 * Notifies the TouchListener that an AccessibleViewExtension has had its performSelection method invoked.
 * 
 * @param e A TouchEvent that contains the phrase for the TouchListener to speak and the method, if any, for the TouchListener to invoke.
 */
public void processSelection(TouchEvent e);
/**
 * Indicates if the user is still searching for the correct control based on the time interval sense the last touch.
 * 
 * @return If the user is still searching for the correct control based on the time interval sense the last touch.
 */
public boolean isSearching();
/**
 * Performs any replacements in a phrase so that a TextToSpeech object will speak it as desired.
 * 
 * @param text The phrase to be converted.
 * @return
 */
public String convert(CharSequence text);
/**
 * Returns a String of text that a TextToSpeech will read out letter by letter.
 *
 * One possible implementation for this method is to insert spaces between the individual letters.
 * @param text The phrase to be spelled.
 * @return The converted phrase.
 */
public String expandTextViewContents(CharSequence text);
}