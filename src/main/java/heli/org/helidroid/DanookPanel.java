package heli.org.helidroid;

import android.app.*;
import android.view.*;
import android.widget.*;
import android.icu.text.*;

public class DanookPanel extends ChopperPanel
{
	public void update(ChopperInfo info)
	{
		super.update(info);
	}

	public LinearLayout getMainLayout()
	{
		// This can be used to add more info by base class
		return mainLay;
	}

	public DanookPanel(LinearLayout par)
	{
		super(par);
		mainLay = par;
		row1Lay = LayoutTools.addLL(1.0f,LayoutTools.getNextViewID(),LinearLayout.HORIZONTAL,mainLay,MyApp.getContext());
		altLabel = LayoutTools.addWidget(new TextView(MyApp.getContext()), 1.0f,row1Lay);
		altDisplay = LayoutTools.addWidget(new TextView(MyApp.getContext()), 1.0f, row1Lay);
		altLabel.setText(R.string.label_alt);
		altDisplay.setText("0.0 m");
		row2Lay = LayoutTools.addLL(1.0f,LayoutTools.getNextViewID(),LinearLayout.HORIZONTAL,mainLay,MyApp.getContext());
		headLabel = LayoutTools.addWidget(new TextView(MyApp.getContext()),1.0f,row2Lay);
		headDisplay = LayoutTools.addWidget(new TextView(MyApp.getContext()),1.0f,row2Lay);
		headLabel.setText(R.string.label_head);
		row3Lay = LayoutTools.addLL(1.0f,LayoutTools.getNextViewID(),LinearLayout.HORIZONTAL,mainLay,MyApp.getContext());
		fuelLabel = LayoutTools.addWidget(new TextView(MyApp.getContext()),1.0f,row3Lay);
		fuelDisplay = LayoutTools.addWidget(new TextView(MyApp.getContext()),1.0f,row3Lay);
		fuelLabel.setText(R.string.label_fuel);
	}

	public void setAltitude(String newAlt)
	{
		altDisplay.setText(newAlt);
	}

	public void setHeading(String newHead)
	{
		headDisplay.setText(newHead);
	}

	public void setFuel(String newFuel)
	{
		fuelDisplay.setText(newFuel);
	}

}
