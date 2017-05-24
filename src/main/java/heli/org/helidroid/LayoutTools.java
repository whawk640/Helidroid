package heli.org.helidroid;

import android.widget.*;
import android.view.*;
import android.content.*;
import android.util.*;
import android.app.*;

/** Copyright 2017 Sasha Industries
 *
 */
public class LayoutTools
{
  static public final int APT = RelativeLayout.ALIGN_PARENT_TOP;
  static public final int APR = RelativeLayout.ALIGN_PARENT_RIGHT;
  static public final int APL = RelativeLayout.ALIGN_PARENT_LEFT;
  static public final int APB = RelativeLayout.ALIGN_PARENT_BOTTOM;

  static public final int RLT = RelativeLayout.TRUE;
  static public final int WC = RelativeLayout.LayoutParams.WRAP_CONTENT;
  static public final int MP = RelativeLayout.LayoutParams.MATCH_PARENT;
  static public final int LMP = LL_Pars.MATCH_PARENT;
  static public int nextViewID = 0x1000;

  static public class RL_Pars extends RelativeLayout.LayoutParams
  {
    public RL_Pars(Context arg0, AttributeSet arg1)
    {
      super(arg0, arg1);
    }

    public RL_Pars(int wc, int wc2)
    {
      super(wc,wc2);
    }

    public void setAlign(int hor, int ver)
    {
      addRule(hor,RLT);
      addRule(ver,RLT);
    }

    public void setAlign(int hor, int ver, int hor2)
    {
      setAlign(hor,ver);
      addRule(hor2,RLT);
    }

  };

  static public class LL_Pars extends LinearLayout.LayoutParams
  {

    public float wieght;

    public LL_Pars(int hor, int ver)
    {
      super(hor, ver);
    }
  }

  static public FrameLayout addFL(float weight, int height, int width, int ma, int id, ViewGroup par,Activity act)
  {
    FrameLayout res = null;
    //res = (FrameLayout)act.findViewById(id);
    if(res == null)
    {
        res = new FrameLayout(act);
    }
    LL_Pars pars = new LL_Pars(LMP,LMP);
    pars.weight = weight;
    pars.height = height;
    pars.width = width;
    pars.setMargins(ma,ma,ma,ma);
    //RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(LMP,LMP);
    //p.addRule(RelativeLayout.CENTER_IN_PARENT);
    par.addView(res,pars);
    res.setId(id);
    return res;
  }
  
  static public LinearLayout addLL(int height, int width, int id, int dir, ViewGroup par, Activity act)
  {
    LinearLayout res = null;
    //res = (LinearLayout)act.findViewById(id);
    if(res == null)
    {
        res = new LinearLayout(act);
    }
    res.setOrientation(dir);
    LL_Pars pars = new LL_Pars(LMP,LMP);
    pars.height = height;
    pars.width = width;
    //act.addContentView(res,pars);
    res.setId(id);
    if (par == null) {
        act.setContentView(res, pars);
    }
    else
    {
        par.addView(res,pars);
    }
    return res;
  }

  static public LinearLayout addLL(int id, int dir, Activity act)
  {
    return addLL(LMP,LMP,id,dir,null,act);
  }

  static public LinearLayout addLLLoc(int height, int width, int id, int dir, Activity act)
  {
    LinearLayout res = null;
    //res = (LinearLayout)act.findViewById(id);
    if(res == null)
    {
        res = new LinearLayout(act);
    }
    res.setOrientation(dir);
    LL_Pars pars = new LL_Pars(LMP,LMP);
    pars.height = height;
    pars.width = width;
    res.setId(id);
    return res;
  }

  static public LinearLayout addLLLoc(int id, int dir, Activity act)
  {
    return addLLLoc(LMP,LMP,id,dir,act);
  }

  static public LinearLayout addLL(float weight, int height, int width, int id, int dir, LinearLayout par,Activity act)
  {
    LinearLayout res = null;
    //res = (LinearLayout)act.findViewById(id);
    if(res == null)
    {
        res = new LinearLayout(act);
    }
    res.setOrientation(dir);
    res.setId(id);
    LL_Pars pars = new LL_Pars(LMP,LMP);
    pars.weight = weight;
    pars.height = height;
    pars.width = width;
    pars.setMargins(1,1,1,1);
    par.addView(res,pars);
    return res;
  }

  static public LinearLayout addLL(float weight, int id, int dir, LinearLayout par,Activity act)
  {
    boolean o = par.getOrientation() == LinearLayout.HORIZONTAL;
    int w = o?0:LMP;
    int h = o?LMP:0;
    return addLL(weight,h,w,id,dir,par,act);
  }

  public static <T extends View> T addWidget(T res, float we, int h, int w, int id, LinearLayout par)
  {
	if (id == 0)
	{
		res.setId(getNextViewID());
	}
	else
	{
		res.setId(id);
	}
    LL_Pars p = new LL_Pars(LMP,LMP);
    p.weight = we;
    p.height = h;
    p.width = w;
    par.addView(res,p);
    return res;
  }

  public static <T extends View> T addWidget(T res, float we, int id, LinearLayout par)
  {
    boolean o = par.getOrientation() == LinearLayout.HORIZONTAL;
    int w = o?0:LMP;
    int h = o?LMP:0;
    return addWidget(res,we,h,w,id,par);
  }

  public static void update(int id, boolean o, Activity act)
  {
    update(act.findViewById(id),o);
  }

  public static int getNextViewID()
  {
      return ++nextViewID;
  }

    public static void update(View v, boolean o)
  {
    try
    {
      v.getLayoutParams().height = o?0:LMP;
      v.getLayoutParams().width = o?LMP:0;
    }
    catch(Exception e)
    {
      System.out.println("Unable to update layout "+e.toString());
    }
  }
}
