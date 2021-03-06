package heli.org.helidroid;

import android.opengl.GLES20;

import javax.microedition.khronos.opengles.GL10;

/** This class defines a camera in three space.
 * Future Optimizations: Animations, such as chase mode
 * Copyright 2015
 * @author Daniel LaFuze
 *
 */
public class Camera {
    /** This defines where the camera is located.
     *
     */
    Point3D source;

    double orbitAltitude;

    double orbitRadius;

    /** This defines where the camera is looking.  By default, it's facing the
     * origin.
     */
    Point3D target;

    /** This defines which direction is up.  It is intended to be a
     * unit vector, though OpenGL isn't picky about the magnitude.
     */
    Point3D upUnit;

    /** This specifies the field of view of the camera
     * By default, it's 60 degrees.
     */
    double fovDegrees;

    /** This specifies the near clipping distance, by default it's 1 unit.
     *
     */
    double nearClip;

    int sceneWidth;

    int sceneHeight;

    /** This specifies the far clipping distance, by default it's 100 units.
     *
     */
    double farClip;

    /** This is used for rotating (sines/cosines) -- in radians
     *
     */
    double curAngle;

    public Camera(int trgX, int trgY, int trgZ) {
        // TODO: Compute source position such that default field of view
        // shows the whole thing... By default, camera source is up along
        // z axis
        orbitAltitude = 200.0;
        orbitRadius = trgX;
        source = new Point3D((double)trgX,(double)(-trgY/2),orbitAltitude);
        target = new Point3D((double)trgX, (double)trgY, (double)trgZ);
        upUnit = new Point3D(0.0,0.0,1.0);
        fovDegrees = 60.0;
        nearClip = 5.0;
        farClip = 1500.0;
        //System.out.println("Camera at " + source.info() + " looking at " + target.info());
    }

    /** This constructor for a camera sets the defaults
     *
     */
    public Camera() {
        source = new Point3D(0.0,0.0,5.0);
        target = new Point3D();
        upUnit = new Point3D(0.0,0.0,1.0);
        fovDegrees = 60.0;
        nearClip = 1.0;
        farClip = 100.0;
        sceneWidth = 100; // default, override with tellGL on resize
        sceneHeight = 100;
        curAngle = 0.0;
    }

    public void setUp(Point3D newUp) {
        upUnit = newUp;
    }

    public void setUp(double x, double y, double z) {
        upUnit.m_x = x;
        upUnit.m_y = y;
        upUnit.m_z = z;
    }

    public void setTarget(Point3D inPoint)
    {
        target = inPoint;
    }

    public void setTarget(double x, double y, double z) {
        target.m_x = x;
        target.m_y = y;
        target.m_z = z;
    }

    public void setSource(Point3D viewPoint) { source = viewPoint; }

    public void setSource(double x, double y, double z) {
        source.m_x = x;
        source.m_y = y;
        source.m_z = z;
    }
    /**
     * This method allows the camera to move randomly
     * @param radius Distance camera can wobble
     */
    public void wobble(double radius) {
        double deltaX = radius * Math.random() - 1.0;
        double deltaY = radius * Math.random() - 1.0;
        double deltaZ = radius * Math.random() - 1.0;
        source = new Point3D(source.x() + deltaX, source.y() + deltaY, source.z() + deltaZ);
    }

    public void chase(Point3D newTarget, double minDistance)
    {
        if (minDistance < nearClip)
        {
            minDistance = nearClip;
        }
        target = newTarget;
        // Don't look up -- always look down :)
        if (source.m_z < (target.m_z + 5.0))
        {
            source.m_z = target.m_z + 5.0;
        }
        double actDistance = eyeDistance();
        if (actDistance > minDistance * 1.1)
        {
            approach(0.25 * (actDistance / minDistance));
        }
    }

    public void chase(Point3D newTarget)
    {
        target = newTarget;
        approach(1.0);
    }

    double eyeDistance()
    {
        double deltaZ = source.z() - target.z();
        double deltaY = source.y() - target.y();
        double deltaX = source.x() - target.x();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public void approach(double approachPercent)
	{
        double deltaZ = approachPercent / 100.0 * (source.z() - target.z());
        double deltaY = approachPercent / 100.0 * (source.y() - target.y());
        double deltaX = approachPercent / 100.0 * (source.x() - target.x());
        source.m_x = source.m_x - deltaX;
        source.m_y = source.m_y - deltaY;
        source.m_z = source.m_z - deltaZ;
    }

	public void adjustHeight(double deltaZ)
	{
		source.m_z += deltaZ;
	}
	
	public void rotateXY(double angleDeg)
	{
		double angleRadians = Math.toRadians(angleDeg);
		double orbitRad = computeCamDistance();
		double deltaX = orbitRad * Math.sin(angleRadians);
		double deltaY = orbitRad * Math.cos(angleRadians);
		source = new Point3D(target.x() + deltaX, target.y() + deltaY, source.m_z);
	}
	
    public void orbit(double ticksPerRevolution)
	{
        if (ticksPerRevolution < 60.0)
        {
            ticksPerRevolution = 60.0;
        }
        curAngle += Math.PI /ticksPerRevolution;
		double orbitRad = computeCamDistance();
        double deltaX = orbitRad * Math.sin(curAngle);
        double deltaY = orbitRad * Math.cos(curAngle);
        source = new Point3D(target.x() + deltaX, target.y() + deltaY, source.m_z);
    }

	public double computeCamDistance()
	{
		double deltaX = (source.m_x - target.m_x);
		double deltaY = (source.m_y - target.m_y);
		return Math.sqrt(deltaX * deltaX +
		                               deltaY * deltaY);
	}
	
    public void show()
    {
        System.out.println("Camera at: " + source.info());
        System.out.println("Camera Looking at: " + target.info());
    }
}
