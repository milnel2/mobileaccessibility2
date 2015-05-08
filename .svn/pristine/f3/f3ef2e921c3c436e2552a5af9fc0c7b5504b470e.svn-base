package android_talking_software.applications.level;

import java.util.HashMap;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.CompoundButton;
import android_talking_software.development.talking_tap_twice.R;
import android_talking_software.development.talking_tap_twice.TTT;
import android_talking_software.development.talking_tap_twice.accessible_view_extensions.CheckBoxTTT;

public class ATL extends TTT implements SensorEventListener 
{
	private CheckBoxTTT rotationXCheckbox, atXCheckbox, angleXCheckbox, fromXCheckbox, rotationZCheckbox, atZCheckbox, angleZCheckbox, fromZCheckbox;
	private CheckBoxTTT[] xCheckBoxes, zCheckBoxes;
	private HashMap<CheckBoxTTT, CheckBoxTTT[]> visible;
	private boolean isRotationX, isAtX, isAngleX, isFromX, isRotationZ, isAtZ, isAngleZ, isFromZ;
	private int lastX, lastZ, x, z;
	private StringBuffer output;
	private SensorManager manager;;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        showLevel();
      	System.out.println("baae");
      	initVariables();
                System.out.println("caaa");
        System.out.println("daab");
    }
    
    private void initVariables()
    {
    	invokeFrom = this;
  		output = new StringBuffer();
    	isAtX = isAtZ = isFromX = isFromZ = !(isRotationX = isRotationZ = isAngleX = isAngleZ = true);
    	lastX = lastZ = x = z = -1;
    	visible = new HashMap<CheckBoxTTT, CheckBoxTTT[]>();
    	initSensor();
}
    
    public void showLevel() 
		{
			setContentView(R.layout.level);
			speak("level");
}

		public void showOptionsDialog() 
		{
			visible.clear();
			setContentView(R.layout.options);
rotationXCheckbox = (CheckBoxTTT) findViewById(R.id.rotation_x_checkbox);
rotationXCheckbox.setChecked(isRotationX);
	   atXCheckbox = (CheckBoxTTT) findViewById(R.id.at_x_checkbox);
	   atXCheckbox.setChecked(isAtX);
	   angleXCheckbox = (CheckBoxTTT) findViewById(R.id.angle_x_checkbox);
	   angleXCheckbox.setChecked(isAngleX);
	  fromXCheckbox = (CheckBoxTTT) findViewById(R.id.from_x_checkbox);
	  fromXCheckbox.setChecked(isFromX);
	  xCheckBoxes = new CheckBoxTTT[] {angleXCheckbox, atXCheckbox, fromXCheckbox};
	  visible.put(rotationXCheckbox, xCheckBoxes);
	  rotationZCheckbox = (CheckBoxTTT) findViewById(R.id.rotation_z_checkbox);
rotationZCheckbox.setChecked(isRotationZ);
  atZCheckbox = (CheckBoxTTT) findViewById(R.id.at_z_checkbox);
  atZCheckbox.setChecked(isAtZ);
  angleZCheckbox = (CheckBoxTTT) findViewById(R.id.angle_z_checkbox);
  angleZCheckbox.setChecked(isAngleZ);
 fromZCheckbox = (CheckBoxTTT) findViewById(R.id.from_z_checkbox);
 fromZCheckbox.setChecked(isFromZ);
 zCheckBoxes = new CheckBoxTTT[] {angleZCheckbox, atZCheckbox, fromZCheckbox};
 visible.put(rotationZCheckbox, zCheckBoxes);
 speak("options dialog");
 }

		private void initSensor()
		{
    	manager = (SensorManager)getSystemService(android.content.Context.SENSOR_SERVICE);
    	manager.registerListener(this, manager.getSensorList(Sensor.TYPE_ORIENTATION).get(0), SensorManager.SENSOR_DELAY_GAME);
		}

		@Override
		protected void onDestroy() 
		{
			display.setText("Good-bye");
			System.out.println("kill");
			super.onDestroy();
		}

		public void exit()
    {
    	finish();
    }
		
		public void cancel()
		{
			showLevel();
		}
		
		public void okay()
		{
			changeSettings();
			showLevel();
}
		
		private void changeSettings()
		{
			isRotationX = rotationXCheckbox.isChecked();
		isAtX = atXCheckbox.isChecked();
isAngleX = angleXCheckbox.isChecked();
isFromX = fromXCheckbox.isChecked();
isRotationZ = rotationZCheckbox.isChecked();
isAtZ = atZCheckbox.isChecked();
isAngleZ = angleZCheckbox.isChecked();
isFromZ = fromZCheckbox.isChecked();
}
		
		public void updateDisplay()
		{
			output.delete(0, output.length());
			if (isRotationX)
			{
output.append("X: ");
				if (isAtX) output.append(at((int)lastX));
				if (((isAngleX) || (isFromX)) && (output.charAt(output.length()-1) != ' ')) output.append(", ");
				if (isAngleX) output.append((int)lastX);
				if ((isFromX) && (output.charAt(output.length()-1) != ' ')) output.append(", ");
				if (isFromX) output.append(from((int)lastX));
				if (isRotationZ) output.append(";\n");
			}
			if (isRotationZ)
			{
output.append("Z: ");
				if (isAtZ) output.append(at((int)lastZ));
				if (((isAngleZ) || (isFromZ)) && (output.charAt(output.length()-1) != ' ')) output.append(", ");
				if (isAngleZ) output.append((int)lastZ);
				if ((isFromZ) && (output.charAt(output.length()-1) != ' ')) output.append(", ");
				if (isFromZ) output.append(from((int)lastZ));
				}
			display.setText(output);
}
		
		@Override
		public void toggleCheckBox()
		{
			super.toggleCheckBox();
			if (!visible.containsKey(getSelected())) return;
			for (CheckBoxTTT box:visible.get(getSelected()))
					box.setEnabled(((CompoundButton) getSelected()).isChecked());
		}
		
		@Override
		protected void tappedDisplay() 
		{
			updateDisplay();
			super.tappedDisplay();
		}

		private String at(double value)
		{
			if (value%180 == 0) return "H yes, V no";
			if (value%90 == 0) return "H no, V yes";
			return "H no, V no";
		}
		
		private String from(double value)
		{
			if (value%180 == 0) return "H";
			else if (value%90 == 0) return "V";
			else if ((value > 0) && (value <= 45)) return "H +"+(int)value; 
			else if ((value >= 135) && (value < 180)) return "H +"+(int)(180-value);
			else if ((value >= 45) && (value < 90)) return "V -"+(int)(90-value);
			else if ((value > 90) && (value <= 135)) return "V +"+(int)(value-90);
			else if ((value > 180) && (value <= 225)) return "H -"+(int)(value-180);
			else if (value >= 315) return "H -"+(int)(360-value);
			else if ((value >= 225) && (value < 270)) return "v -"+(int)(270-value);
			else if ((value > 270) && (value <= 315)) return "V +"+(int)(315-value);
			return "";
		}

		public void onSensorChanged(SensorEvent event) 
{
		 if (event.sensor.getType() != Sensor.TYPE_ORIENTATION) return;
z = (event.values[SensorManager.DATA_Z] >= 0) ? round(event.values[SensorManager.DATA_Z]+90) : (event.values[SensorManager.DATA_Z] >= -90) ? round(90+event.values[SensorManager.DATA_Z]) : round(450+event.values[SensorManager.DATA_Z]);
	x = round((360-event.values[SensorManager.DATA_Y]))%360;
	if (x != lastX)
	{
	 lastX = x;
	}
	 if 			(z != lastZ)
	 {
lastZ = z;
	 }
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) 
		{
		}
		
		private int round(double value)
		{
			return (value-(int)value < 0.5) ? (int)value : (int)value+1;
		}

}