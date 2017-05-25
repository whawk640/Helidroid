package heli.org.helidroid;

import android.widget.*;
import java.util.*;

public class Danook extends StigChopper
{
    public static final String TAG = "Danook";
    public static final long D_DBG = 0x1;

    public static final int PANEL_RATE = 10;
    public int panelUpdater;

	private LinearLayout[] rowLayouts = null;
	private TextView[] rowLabels = null;
	private TextView[] rowValues = null;
	private static final int ROW_COUNT = 3;
	
    private DanookController myThread;

    public Danook(int id, World world)
    {
        super(id,world);
        myThread = new DanookController(this,world);
        myThread.start();
        panelUpdater = 0;
    }

	public void setupPanel()
	{
		if (myPanel != null)
		{
			LinearLayout topLay = myPanel.getMainLayout();
			for (int i = 0; i < ROW_COUNT; ++i)
			{
				// Need Activity, Sasha said that sucks... now what?
				//rowLayouts[i] = LayoutTools.addLL(1.0f,LayoutTools.getNextViewID(),LinearLayout.HORIZONTAL,topLay);
			}
		}
	}
    /* public void createPanel()
    {
        m_info.setLayout(new BoxLayout(m_info, BoxLayout.PAGE_AXIS));
        destLabel = new JLabel("Dest: (NULL)");
        m_info.add(destLabel);
        pcLabel = new JLabel("Packages: 10");
        m_info.add(pcLabel);
        posLabel = new JLabel("Position: (NULL)");
        m_info.add(posLabel);
        velLabel = new JLabel("Velocity: (NULL)");
        m_info.add(velLabel);
        accLabel = new JLabel("Acceleration: (NULL)");
        m_info.add(accLabel);
        stateLabel = new JLabel("State: UNKNOWN");
        m_info.add(stateLabel);
        fuelRemaining = new DanookHUD(fuelCapacity);
        m_info.add(fuelRemaining);
    } */

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

    /*
    @Override
    public void render(GLAutoDrawable drawable, double actHeading, double actTilt, double rotorPos, double tailRotorPos)
    {
        super.render(drawable, actHeading, actTilt, rotorPos, tailRotorPos);
        GL2 gl = drawable.getGL().getGL2();
        Point3D myTarget = myThread.getDestination();
        if (myTarget != null)
        {
            gl.glBegin(gl.GL_TRIANGLE_STRIP);
            gl.glColor4d(1.0, 0.25, 0.25, 0.5);
            gl.glVertex3d(myTarget.m_x - 5.0, myTarget.m_y - 5.0, myTarget.m_z);
            gl.glVertex3d(myTarget.m_x - 5.0, myTarget.m_y + 5.0, myTarget.m_z);
            gl.glVertex3d(myTarget.m_x, myTarget.m_y, myTarget.m_z + 75.0);
            gl.glVertex3d(myTarget.m_x + 5.0, myTarget.m_y + 5.0, myTarget.m_z);
            gl.glVertex3d(myTarget.m_x + 5.0, myTarget.m_y - 5.0, myTarget.m_z);
            gl.glVertex3d(myTarget.m_x - 5.0, myTarget.m_y - 5.0, myTarget.m_z);
            gl.glEnd();
        }
        if (++panelUpdater > PANEL_RATE)
        {
            updatePanel();
            panelUpdater = 0;
        }
    } */

    public void updatePanel()
    {
        Point3D dest = myThread.getDestination();
        Point3D pos = myThread.getPosition();
        Point3D vel = myThread.getVelocity();
        Point3D acc = myThread.getAcceleration();
        int packageCount = targetWaypoints.size();
        String controlState = myThread.getControlState();
        //stateLabel.setText("State: " + controlState);
        if (dest == null)
        {
            //destLabel.setText("Dest: None");
        }
        else
        {
            //destLabel.setText("Dest: " + dest.xyzInfo());
        }
        //pcLabel.setText("Packages: " + packageCount);
        if (pos == null)
        {
            //posLabel.setText("Position: Unknown");
        }
        else
        {
            //posLabel.setText("Position: " + pos.xyzInfo());
        }
        if (vel == null)
        {
            //velLabel.setText("Velocity: Unknown");
        }
        else
        {
            //velLabel.setText("Velocity: " + vel.xyzInfo());
        }
        if (acc == null)
        {
            //accLabel.setText("Acceleration: Unknown");
        }
        else
        {
            //accLabel.setText("Acceleration: " + acc.xyzInfo());
        }
        //fuelRemaining.setFuelLevel(world.getFuelRemaining(id));
    }
}
