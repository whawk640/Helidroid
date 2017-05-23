package heli.org.helidroid;

import java.util.Date;

public class Apachi extends StigChopper
{
    public static final String TAG = "Apachi";
    public static final long DBG = 0x20;
    private ApachiAlt m_alt;
    private ApachiHeading m_heading;
    private ApachiSpeed m_speed;
    private ApachiGL m_agl = new ApachiGL();

    private double m_rotSpeedR = 0.0;
    private double m_tiltR = 0.0;
    private double m_stabSpeedR = 0.0;

    private double m_curSpeed = 0.0;
    private double m_curHead = 0.0;
    private double m_curAlt = 0.0;

    private double m_sumActSpeed = 0.0;

    private double m_actSpeedCnt = 0.0;

    private long m_rotStamp = -1;

    public Apachi(int id, World world)
    {
        super(id,world);
        World.dbg(TAG,"### CONSTRUCTOR ###",DBG);
        m_alt = new ApachiAlt(this,world);
        World.dbg(TAG,"Starting alt loop",DBG);
        m_alt.start();

        m_heading = new ApachiHeading(this, world);
        m_heading.setTarget(0.0);
        m_heading.start();

        m_speed = new ApachiSpeed(this, world);
        m_speed.setTarget(0.0,-1.0);
        m_speed.start();

        hover(30);
        maintainHeading(315);
        inventory = 16;

    }
	
	public void draw(int textDataHandle, float[] myMatrix) { // pass in the calculated transformation matrix
		int slow = 70;
        double wts = world.getTimestamp();
        if(wts > 30 && wts < slow)
        {
            maintainAlt(90);
            maintainSpeed(5.0,-1.0);
        }

        if(wts > slow && m_curSpeed > 0.05)
        {
            maintainSpeed(0.0,-1.0);
        }

        if(wts > slow && m_curSpeed < 0.049)
        {
            hover(-1.0);
        }
        //m_altJL.setText("Alt: " + Apachi.f(m_alt.m_lastAlt));
		
		super.draw(myMatrix);
	}
    /** This method renders a chopper.  We'll get the position from the world.
     * We need to get information about the chopper's orientation from the
     * world object that is in charge of the choppers Orientation.
     * @param drawable Access to OpenGL pipeline
     * @param actHeading Direction in degrees, so we can rotate appropriately
     * @param actTilt Tilt in degrees so we can rotate accordingly
     * @param rotorPos Rotation of the rotor (0 - 360) so we can draw it
     * @param tailRotorPos Rotation of the rotor (0 - 360) so we can draw it
     */

    /*
    public void render(GLAutoDrawable drawable, double actHeading, double actTilt, double rotorPos, double tailRotorPos)
    {
        int slow = 70;
        double wts = world.getTimestamp();
        if(wts > 30 && wts < slow)
        {
            maintainAlt(90);
            maintainSpeed(5.0,-1.0);
        }

        if(wts > slow && m_curSpeed > 0.05)
        {
            maintainSpeed(0.0,-1.0);
        }

        if(wts > slow && m_curSpeed < 0.049)
        {
            hover(-1.0);
        }
        m_altJL.setText("Alt: " + Apachi.f(m_alt.m_lastAlt));
        super.render(drawable, actHeading, actTilt, rotorPos, tailRotorPos); */
        /*
        GL2 gl = drawable.getGL().getGL2();
        Point3D myPosition = world.gps(id);
        // This method returns the bottom center of our chopper, first, get center
        Point3D centerPos = new Point3D(myPosition.m_x, myPosition.m_y, myPosition.m_z);
        // For now, we need our center point for an axis of rotation (Pitch and heading)
        // When we start rendering a more realistic chopper, we'll have to do that in addition
        // to rotating the rotors
        centerPos.m_z += Z_SIZE / 2.0;
        // Next, get bounding rectangular prism
        myPosition.m_x -= X_SIZE / 2.0;
        myPosition.m_y -= Y_SIZE / 2.0;
        Point3D mySize = new Point3D(X_SIZE, Y_SIZE, Z_SIZE);
        Object3D chopperObject = new Object3D(myPosition, mySize);
        chopperObject.setColor(new Point3D(0.7,0.0,1.0),0.98);
        // Translate the center of chopper to the origin, so rotation doesn't move chopper
        gl.glPushMatrix();
        gl.glTranslated(centerPos.m_x, centerPos.m_y, centerPos.m_z);
        Point3D transformation = world.transformations(id);
        // rotate chopper by heading
        gl.glRotated(transformation.m_x, 0.0, 0.0, -1.0);
        // rotate chopper by tilt
        gl.glRotated(transformation.m_y, -1.0, 0.0, 0.0);
        gl.glTranslated(-centerPos.m_x,  -centerPos.m_y, -centerPos.m_z);
        m_agl.setMaterial(gl,new Point3D(0.7,0.0,1.0),new Point3D(0.3,0.3,0.3),0.98f,1.0f,1.0f);
        m_agl.box(gl,0.98f,myPosition,new Point3D(0.0,0.0,0.0),X_SIZE);
        gl.glEnd();
        gl.glPopMatrix();
        */
    //}


    synchronized public double getCurrentRotorSpeed()
    {
        return m_rotSpeedR;
    }

    synchronized public double setDesiredRotorSpeed(double newSpeed)
    {
        m_rotSpeedR = newSpeed;
        if(m_rotSpeedR > 0.0 && m_rotStamp < 0)
        {
            m_rotStamp = new Date().getTime();
        }
        m_sumActSpeed += m_rotSpeedR;
        m_actSpeedCnt += 1.0;
        world.requestSettings(id,m_rotSpeedR,m_tiltR,m_stabSpeedR);
        return m_rotSpeedR;
    }

    synchronized public double getStabilizerSpeed()
    {
        return m_stabSpeedR;
    }

    synchronized public void setDesiredStabilizerSpeed(double newSpeed)
    {
        m_stabSpeedR = newSpeed;
        world.requestSettings(id,m_rotSpeedR,m_tiltR,m_stabSpeedR);
    }

    synchronized public double getCurrentTilt()
    {
        return world.transformations(id).m_y;
    }

    synchronized public void setDesiredTilt(double newTilt)
    {
        m_tiltR = newTilt;
        world.requestSettings(id,m_rotSpeedR,m_tiltR,m_stabSpeedR);
    }

    synchronized public void setCurrentSpeed(double speed)
    {
        m_curSpeed = speed;
    }

    synchronized public void setCurrentHeading(double head)
    {
        m_curHead = head;
    }

    synchronized public void setCurrentAlt(double alt)
    {
        m_curAlt = alt;
    }

    synchronized public double estHoverSpeed(double revs)
    {
        double cf = 1.0;//0.9 / eT_min;
        if(cf < 0.0) cf = 1.0;
        double burnt = cf * revs * (1.0 / 60.0);
        double fuel = fuelCapacity - burnt;

        World.dbg(TAG,"RPM: "
                //+ f(avgR)
                + ",revs " + f(revs)
                + ", cor: " + f(cf)
                + ", burnt: " + f(burnt)
                // + ", min: " + f(eT_min)
                + ", rem: " + f(fuel),0);

        double wt = ChopperAggregator.ITEM_WEIGHT * itemCount() + ChopperAggregator.BASE_MASS + fuel;
        double res = wt * ChopperInfo.EARTH_ACCELERATION / ChopperInfo.THRUST_PER_RPM;
        World.dbg(TAG,"Total weight: " + f(wt) + " kg, desired: " + f(res) + " rpm",DBG);
        return res;
    }

    public void hover(double alt)
    {
        setDesiredStabilizerSpeed(ChopperInfo.STABLE_TAIL_ROTOR_SPEED);
        maintainAlt(alt);
        m_speed.setTarget(0.0,-1.0);
    }

    public void maintainAlt(double alt)
    {
        m_alt.setTarget(alt);
    }

    public void maintainHeading(double head_deg)
    {
        m_heading.setTarget(head_deg);
    }

    public void maintainSpeed(double spd_ms, double time_sec)
    {
        m_speed.setTarget(spd_ms,time_sec);
    }

    synchronized public void turnIntoDOT(double dot)
    {
        maintainHeading(dot);
    }

    static public String f(double n)
    {
        return String.format("%.4f",n);
    }
}
