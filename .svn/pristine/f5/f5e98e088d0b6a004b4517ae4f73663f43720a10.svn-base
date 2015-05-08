package android_talking_software.applications.calculator;

import android_talking_software.development.talking_tap_twice.R;
import android.os.Bundle;
import android.widget.Button;
import android_talking_software.development.talking_tap_twice.TTT;

public class ATC extends TTT
{
	private StringBuffer input;
	private boolean cleared, result;

	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calculator);
			initVariables();
	}

	private void initVariables()
	{
		invokeFrom = this;
		cleared = true;
		input = new StringBuffer("0");
		setUsingMathematicalPronounciations(true);
	}

	@Override
	protected void onDestroy() 
	{
		display.setText("Good-bye");
		super.onDestroy();
	}

	public void updateDisplay()
	{
		if (result) clearDisplaySilent();
		if (!cleared) 
			input.append(((Button)getSelected()).getText());
		else
			input.replace(0, 1, ((Button)getSelected()).getText().toString());
		cleared = false;
		display.setText(input);
	}

	public void processBackspace()
	{
		if (cleared)
			speak("Nothing entered");
		else
		{
			speak("erasing "+input.charAt(input.length()-1));
			input.deleteCharAt(input.length()-1);
			if (input.length() == 0)
				clearDisplayAndRead();
			else display.setText(input);
		}
	}

	public void processClear()
	{
		clearDisplayAndRead();
	}

	private void clearDisplayAndRead()
	{
		clearDisplaySilent();
		readDisplay();			
	}
	
	private void clearDisplaySilent()
	{
		input.delete(0, input.length());
		result = !(cleared = true);
		input.append(0);
		display.setText(input);
	}

	public void evaluate()
	{
		result = true;
		Calculator.calculate(input);
		display.setText(input);
		speak("result: "+input.toString());
		resetSelected(display);
	}

}