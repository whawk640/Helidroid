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
	protected LinearLayout posLay = null;
	protected TextView posLabel = null;
	protected TextView posDisplay = null;
	protected LinearLayout destLay = null;
	protected TextView destLabel = null;
	protected TextView destDisplay = null;
	
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
		posLay = LayoutTools.addLL(1.0f,LayoutTools.getNextViewID(),LinearLayout.HORIZONTAL,mainLay,MyApp.getContext());
		posLabel = LayoutTools.addWidget(new TextView(MyApp.getContext()),1.0f,posLay);
		posDisplay = LayoutTools.addWidget(new TextView(MyApp.getContext()),1.0f,posLay);
		posLabel.setText(R.string.label_pos);
		destLay = LayoutTools.addLL(1.0f,LayoutTools.getNextViewID(),LinearLayout.HORIZONTAL,mainLay,MyApp.getContext());
		destLabel = LayoutTools.addWidget(new TextView(MyApp.getContext()),1.0f,destLay);
		destDisplay = LayoutTools.addWidget(new TextView(MyApp.getContext()),1.0f,destLay);
		destLabel.setText(R.string.label_dest);
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
	
	public void setPos(String newPos)
	{
		posDisplay.setText(newPos);
	}
	
	public void setPos(Point3D newPos)
	{
		if (newPos == null)
		{
			posDisplay.setText("Unknown");
		}
		else
		{
			posDisplay.setText(newPos.xyInfo(1));
		}
	}

	public void setDest(String newDest)
	{
		destDisplay.setText(newDest);
	}

	public void setDest(Point3D newDest)
	{
		if (newDest == null)
		{
			destDisplay.setText("No Dest");
		}
		else
		{
			destDisplay.setText(newDest.xyInfo(1));
		}
	}
}
