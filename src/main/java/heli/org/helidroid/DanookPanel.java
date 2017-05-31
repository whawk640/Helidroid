package heli.org.helidroid;

import android.app.*;
import android.view.*;
import android.widget.*;
import android.icu.text.*;

public class DanookPanel extends ChopperPanel
{
	protected LinearLayout rowLay = null;
	protected TextView stateLabel = null;
	protected TextView stateDisplay = null;
	
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
		mainLay = getMainLayout();
		rowLay = LayoutTools.addLL(1.0f,LayoutTools.getNextViewID(),LinearLayout.HORIZONTAL,mainLay,MyApp.getContext());
		stateLabel = LayoutTools.addWidget(new TextView(MyApp.getContext()), 1.0f,rowLay);
		stateDisplay = LayoutTools.addWidget(new TextView(MyApp.getContext()), 1.0f, rowLay);
		stateLabel.setText(R.string.label_state);
	}

	public void setState(String newState)
	{
		stateDisplay.setText(newState);
	}
}
