package heli.org.helidroid;

import android.app.*;
import android.content.*;

public class MyApp extends Application
{
	
	private static MyApp instance = null;
	
	public MyApp()
	{
		instance = this;
	}
	
	public static Activity getActivity()
	{
		return instance.getActivity();
	}
	
	public static Context getContext()
	{
		return instance;
	}
}
