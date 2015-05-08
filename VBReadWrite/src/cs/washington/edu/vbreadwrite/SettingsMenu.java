package cs.washington.edu.vbreadwrite;

import android.content.Intent;
import android.os.Bundle;

public class SettingsMenu extends AccessibleMenu {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onFinishing()
	{
		// do nothing (don't kill TTS)
	}
	
}
