package heli.org.helidroid;

import android.graphics.drawable.*;
import android.icu.text.*;
import android.view.View;
import android.widget.*;

public class ChopperPanel extends LinearLayout
{
	LinearLayout mainLay = null;
	LinearLayout row1Lay = null;
	TextView altLabel;
	TextView altDisplay;
	LinearLayout row2Lay = null;
	TextView headLabel;
	TextView headDisplay;
	LinearLayout row3Lay = null;
	TextView fuelLabel;
	TextView fuelDisplay;
	LinearLayout row4Lay = null;
	TextView invLabel;
	TextView invDisplay;

	public void addWidget(View v)
	{
		LayoutTools.addWidget(v,3.0f, LayoutTools.getNextViewID(),mainLay);
	}

	public void setInventory(int itemCount)
	{
		String itemString = String.format("%d packages",itemCount);
		setInventory(itemString);
	}	
	
	public void update(ChopperInfo info)
	{
		Point3D myPosition = info.getPosition();
		setAltitude(myPosition.z());
		setHeading(info.getHeading());
		double fuel = info.getFuelRemaining();
		String fuelString = String.format("%.1f kg",fuel);
		setFuel(fuelString);
	}

	public LinearLayout getMainLayout()
	{
		// This can be used to add more info by base class
		return mainLay;
	}
	
	public ChopperPanel()
	{
		super(MyApp.getContext());
		GradientDrawable border = new GradientDrawable();
		border.setColor(0xffffffff); // AARRGGBB
		border.setCornerRadius(3.0f);
		border.setStroke(2,0x80000080); // AARRGGBB
		setBackground(border);
		setOrientation(LinearLayout.VERTICAL);
		//mainLay = LayoutTools.addLLLoc(LayoutTools.getNextViewID(),LinearLayout.VERTICAL,MyApp.getActivity());
		mainLay = this;
		//this.setLayoutDirection(LinearLayout.VERTICAL);
		row1Lay = LayoutTools.addLL(1.0f,LayoutTools.getNextViewID(),LinearLayout.HORIZONTAL,mainLay,MyApp.getContext());
		altLabel = LayoutTools.addWidget(new TextView(MyApp.getContext()), 1.0f,row1Lay);
		altDisplay = LayoutTools.addWidget(new TextView(MyApp.getContext()), 2.0f, row1Lay);
		altLabel.setText(R.string.label_alt);
		altDisplay.setText("0.0 m");
		row2Lay = LayoutTools.addLL(1.0f,LayoutTools.getNextViewID(),LinearLayout.HORIZONTAL,mainLay,MyApp.getContext());
		headLabel = LayoutTools.addWidget(new TextView(MyApp.getContext()),1.0f,row2Lay);
		headDisplay = LayoutTools.addWidget(new TextView(MyApp.getContext()),2.0f,row2Lay);
		headLabel.setText(R.string.label_head);
		row3Lay = LayoutTools.addLL(1.0f,LayoutTools.getNextViewID(),LinearLayout.HORIZONTAL,mainLay,MyApp.getContext());
		fuelLabel = LayoutTools.addWidget(new TextView(MyApp.getContext()),1.0f,row3Lay);
		fuelDisplay = LayoutTools.addWidget(new TextView(MyApp.getContext()),2.0f,row3Lay);
		fuelLabel.setText(R.string.label_fuel);
		row4Lay = LayoutTools.addLL(1.0f,LayoutTools.getNextViewID(),LinearLayout.HORIZONTAL,mainLay,MyApp.getContext());
		invLabel = LayoutTools.addWidget(new TextView(MyApp.getContext()),1.0f,row4Lay);
		invDisplay = LayoutTools.addWidget(new TextView(MyApp.getContext()),2.0f,row4Lay);
		invLabel.setText(R.string.label_inv);
	}
	
	protected void setAltitude(double newAlt)
	{
		altDisplay.setText(String.format("%.2f m",newAlt));
	}
	
	protected void setAltitude(String newAlt)
	{
		altDisplay.setText(newAlt);
	}

	protected void setHeading(double newHead)
	{
		headDisplay.setText(String.format("%.2f deg",newHead));
	}
	
	protected void setHeading(String newHead)
	{
		headDisplay.setText(newHead);
	}
	
	protected void setFuel(String newFuel)
	{
		fuelDisplay.setText(newFuel);
	}

	protected void setInventory(String invString)
	{
		invDisplay.setText(invString);
	}
}
