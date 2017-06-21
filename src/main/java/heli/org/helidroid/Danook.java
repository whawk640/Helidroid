package heli.org.helidroid;

import android.view.View;
import android.widget.*;
import java.util.*;

public class Danook extends StigChopper
{
    public static final String TAG = "Danook";
    public static final long D_DBG = 0x1;

    public static final int PANEL_RATE = 10;
    public int panelUpdater;
    private RadarGLSurfaceView myRadar = null;

    private DanookController myThread;

    public Danook(int id, World world)
    {
        super(id,world);
        myThread = new DanookController(this,world);
        myThread.start();
        panelUpdater = 0;
    }

    synchronized public double getCurrentTilt_Degrees()
    {
        return world.transformations(id).m_y;
    }

    /** Provide our controller access to our waypoints.
     *
     * @return the list of waypoints
     */
    synchronized public ArrayList<Point3D> getWaypoints()
    {
        return targetWaypoints;
    }

    synchronized public boolean deleteWaypoint(Point3D thePoint)
    {
        boolean pointFound = false;
        if (targetWaypoints.contains(thePoint))
        {
            targetWaypoints.remove(thePoint);
            pointFound = true;
        }
        return pointFound;
    }

	public void createPanel(LinearLayout par, float stretch)
	{
		myPanel = LayoutTools.addWidget(new DanookPanel(), stretch, LayoutTools.getNextViewID(),par);
        System.out.println("Adding Radar Surface, chopper ID: " + id);
        myRadar = new RadarGLSurfaceView(MyApp.getContext(),world, id);
        world.addSurface(myRadar);
        myPanel.addWidget((View)myRadar);
	}
	
    public void updatePanel(ChopperInfo inf)
    {
		super.updatePanel(inf);
        Point3D dest = myThread.getDestination();
        Point3D pos = myThread.getPosition();
        Point3D vel = myThread.getVelocity();
        Point3D acc = myThread.getAcceleration();
        String controlState = myThread.getControlState();
		if (myPanel != null)
		{
			DanookPanel danPanel = (DanookPanel)myPanel;
			danPanel.setState(controlState);
			danPanel.setPos(pos);
			danPanel.setVel(vel);
			danPanel.setAcc(acc);
			danPanel.setDest(dest);
		}
    }
}
