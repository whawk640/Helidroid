package heli.org.helidroid;

import android.app.*;
import android.view.*;
import android.widget.*;
import android.icu.text.*;

public class DanookPanel extends ChopperPanel
{
	protected LinearLayout stateLay = null;
	protected TextView stateLabel = null;
	protected TextView stateDisplay = null;
	protected LinearLayout tiltLay = null;
	protected TextView tiltLabel = null;
	protected TextView tiltDisplay = null;
	
	public void update(ChopperInfo info)
	{
		super.update(info);
		setTilt(info.getTilt());
	}

	public LinearLayout getMainLayout()
	{
		// This can be used to add more info by base class
		return mainLay;
	}

	public DanookPanel(LinearLayout par)
	{
		super(par);
		mainLay = getMainLayout();
		stateLay = LayoutTools.addLL(1.0f,LayoutTools.getNextViewID(),LinearLayout.HORIZONTAL,mainLay,MyApp.getContext());
		stateLabel = LayoutTools.addWidget(new TextView(MyApp.getContext()), 1.0f,stateLay);
		stateDisplay = LayoutTools.addWidget(new TextView(MyApp.getContext()), 1.0f, stateLay);
		stateLabel.setText(R.string.label_state);
		tiltLay = LayoutTools.addLL(1.0f,LayoutTools.getNextViewID(),LinearLayout.HORIZONTAL,mainLay,MyApp.getContext());
		tiltLabel = LayoutTools.addWidget(new TextView(MyApp.getContext()),1.0f,tiltLay);
		tiltDisplay = LayoutTools.addWidget(new TextView(MyApp.getContext()),1.0f,tiltLay);
		tiltLabel.setText(R.string.label_tilt);
	}

	public void setState(String newState)
	{
		stateDisplay.setText(newState);
	}
	
	public void setTilt(String newTilt)
	{
		tiltDisplay.setText(newTilt);
	}
	
	public void setTilt(double newTilt)
	{
		if (Math.abs(newTilt) < 0.05)
		{
			newTilt = 0.01; // Prevent negative 0
		}
		String tiltString = String.format("%3.1f",newTilt);
		tiltDisplay.setText(tiltString);
	}
}
