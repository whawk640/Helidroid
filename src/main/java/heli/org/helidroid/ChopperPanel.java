package heli.org.helidroid;

import android.app.*;
import android.view.*;
import android.widget.*;

public class ChopperPanel
{
	Activity mainAct = null;
	LinearLayout mainLay = null;
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
		mainLay = par;
		mainAct = act;
		row1Lay = LayoutTools.addLL(LayoutTools.WC, LayoutTools.MP, LayoutTools.getNextViewID(), LinearLayout.HORIZONTAL,mainLay,mainAct);
		altLabel = LayoutTools.addWidget(new TextView(mainAct.getApplicationContext()),0.5f, LayoutTools.getNextViewID(),row1Lay);
		altLabel.setText(R.string.label_alt);
		altDisplay = LayoutTools.addWidget(new TextView(mainAct.getApplicationContext()),1.0f,LayoutTools.getNextViewID(),row1Lay);
		altDisplay.setText("0.0 m");
	}
	
	public void setAltitude(String newAlt)
	{
		altDisplay.setText(newAlt);
	}
	
}
