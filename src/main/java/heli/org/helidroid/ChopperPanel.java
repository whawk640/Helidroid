package heli.org.helidroid;

import android.app.*;
import android.view.*;
import android.widget.*;

public class ChopperPanel extends View
{
	//Activity mainAct = null;
	//LinearLayout mainLay = null;
	LinearLayout row1Lay = null;
	TextView altLabel;
	TextView altDisplay;
	
	public void update(ChopperInfo info)
	{
		Point3D myPosition = info.getPosition();
		String altitudeString = myPosition.Z() + " m";
		altDisplay.setText(altitudeString);
	}
	
	public ChopperPanel(Activity act, LinearLayout par)
	{
		super(act.getApplicationContext());
		//mainLay = par;
		//mainAct = act;
		row1Lay = LayoutTools.addLL(1.0f,LayoutTools.getNextViewID(), LinearLayout.HORIZONTAL,par,act);
		altLabel = LayoutTools.addWidget(new TextView(act),0.5f, LayoutTools.getNextViewID(),row1Lay);
		altLabel.setText(R.string.label_alt);
		altDisplay = LayoutTools.addWidget(new TextView(act),1.0f,LayoutTools.getNextViewID(),row1Lay);
		altDisplay.setText("0.0 m");
	}
	
	public void setAltitude(String newAlt)
	{
		altDisplay.setText(newAlt);
	}
	
}
