package cs.washington.mobileaccessibility.onebusaway.uistates;

import com.google.marvin.shell.TouchGestureControlOverlay.Gesture;

import cs.washington.mobileaccessibility.onebusaway.SettingsManager;
import cs.washington.mobileaccessibility.onebusaway.SettingsManager.Setting;

/**
 * This is the page/state that allows the user to change the program's settings
 * 
 * It interacts closely with SettingsManager.java
 * 
 * @author Will
 *
 */
public class SettingsState implements State {
	// the index of which setting is currently displayed
	private int currentIndex;
	private static Setting[] settings;
	
	
	public SettingsState() {
		currentIndex = 0;
		settings = SettingsManager.getSettings();
	}
	
	
	// if down is +1, scroll down.  if down is -1, scroll up
	private void scroll(int down, CoState cos) {
		int nextIndex = currentIndex;
		do {
		nextIndex += down;
		if(nextIndex < 0 || nextIndex >= settings.length)
			return;
		} while(!settings[nextIndex].enabled());
		currentIndex = nextIndex;
		cos.vibrateForScroll();
		updateText(cos,true);
	}
	
	
	private void updateText(CoState cos, boolean speak) {
		String settingName = settings[currentIndex].getName();
		String settingValue = settings[currentIndex].getNextPrev(0);
		cos.displayText("SETTINGS\n" + settingName + ": " + settingValue);
		if(speak)
			cos.outputText(settingName + " current value: " + settingValue);
	}
	
	
	public String tentativeGesture(Gesture g, CoState cos) {
		if(g == Gesture.LEFT)
			return settings[currentIndex].getNextPrev(-1);
		if(g == Gesture.RIGHT)
			return settings[currentIndex].getNextPrev(+1);
		if(g == Gesture.UP) {
			int tempIndex = currentIndex;
			do {
				tempIndex--;
				if(tempIndex < 0)
					return "";
			} while(!settings[currentIndex].enabled());
			return "Scroll up";
		}
		if(g == Gesture.DOWN || g == Gesture.DOUBLEDOWN) {
			int tempIndex = currentIndex;
			do {
				tempIndex++;
				if(tempIndex >= settings.length)
					return "";
			} while(!settings[currentIndex].enabled());
			return "Scroll down";
		}
		return "";
	}
	
	public boolean wantsTrackballScrolling() {
		return true;
	}
	
	public boolean wantsZeroGesture() {
		return true;
	}
	
	public boolean wantsSidebar() {
		return true;  // there'll always be some!
	}
	
	public boolean accelerateScroll() {
		return true;
	}
	
	public boolean delayNumberAnnounce() {
		return false;
	}
	
	
	
	
	
	
	public void onGestureFinish(Gesture g, CoState cos) {
		switch(g) {
		case UP:
		case UPRIGHT:
		case UPLEFT:
			scroll(-1, cos);
			break;
		case DOWN:
		case DOWNRIGHT:
		case DOWNLEFT:
			scroll(+1, cos);
			break;
		case LEFT:
			doNextPrev(cos, -1);
			break;
		case RIGHT:
			doNextPrev(cos, +1);
			break;
		}
	}
	
	private void doNextPrev(CoState cos, int delta) {
		Setting s = settings[currentIndex];
		String newVal = s.getNextPrev(delta);
		if(newVal.equals(""))
			return;
		cos.outputText("Set " + s.getName() + " to " + newVal);
		s.doNextPrev(delta);
		cos.vibrateForScroll();
		updateText(cos, false);
	}
	
	public void onTrackballDown(CoState cos) {
		doNextPrev(cos, +1);
	}
	
	public boolean onKeyDown(int keyCode, CoState cos) {
		return false; // nothing to do
	}

	public void onSidebar(double fraction, CoState cos) {
		int totalEnabled = 0;
		for(int i = 0; i < settings.length; i++)
			if(settings[i].enabled())
				totalEnabled++;
		int protoNextIndex = (int) (totalEnabled * fraction);
		if(protoNextIndex == totalEnabled)
			protoNextIndex--;
		int nextIndex = 0;
		while(nextIndex < settings.length) {
			if(!settings[nextIndex].enabled())
				protoNextIndex++;
			if(nextIndex == protoNextIndex)
				break;
			nextIndex++;
		}
		
		if(nextIndex < settings.length) {
			if(nextIndex != currentIndex) {
				currentIndex = nextIndex;
				cos.vibrateForScroll();
				updateText(cos,true);
			}
		}
	}

	
	public void longDescribe(CoState cos) {
		// TODO fix up these instructions
		cos.outputText("Choose setting.  Current selection: ");
		updateText(cos, true);
	}

	public void shortDescribe(CoState cos) {
		updateText(cos, true);
	}
	


	public void onResume(CoState cos) {
		// nop
	}
}
