package heli.org.helidroid;

import java.util.ArrayList;

/** This class will control a Danook Helicopter
 *
 * @author Daniel
 *
 */
public class DanookController extends Thread
{
    // TODO: Implement a state machine
    public static final String TAG = "DC:";
    public static final long DC_DBG = 0x2;
    private static final int STATE_LANDED = 0;
    private static final int STOP_NOW = 1;
    private static final int FINDING_HEADING = 2;
	private static final int RADAR_CHECK = 3;
    private static final int APPROACHING = 4;
	private static final int DONE = 5;

    private static final double VERT_CONTROL_FACTOR = 2.55;
    private static final double HORZ_CONTROL_FACTOR = 0.36;

    private static final double MAX_VERT_VELOCITY = 3.16;

    private static final double MAX_HORZ_VELOCITY = 6.0;

    private static final double MAX_VERT_ACCEL = 0.65;

    private static final double MAX_HORZ_ACCEL = 1.03;

    private static final double DECEL_DISTANCE_VERT = 12.5;

    private static final double DECEL_DISTANCE_HORZ = 75.0;

    private static final double VERT_DECEL_SPEED = 0.52;

    private static final double HORZ_DECEL_SPEED = 1.25;

	private static final double DIR_ERROR_WARN = 1.0;
	
	private static final double DEADBAND_DIST = 0.75;
	
	private static final double STOPPED_CHECK = 0.0125;
	
    private Danook myChopper;
    private World myWorld;
    private int myState = STATE_LANDED;

    private double desMainRotorSpeed_RPM = 0.0;
    private double desTailRotorSpeed_RPM = 0.0;
    private double desTilt_Degrees = 0.0;
	private int sleepTime_ms = 20;
	private int sleepTime_ns = 0;

    private Point3D estimatedAcceleration = null;
    private Point3D estimatedVelocity = null;
    private Point3D actualPosition = null;
	private Point3D homeBase = null;

    public double desiredHeading;
    public double desiredAltitude;

    private Point3D currentDestination;

    synchronized public String getControlState()
    {
        String returnState = new String("Unknown");
        switch(myState)
        {
            case STATE_LANDED:
            {
                returnState = "Landed";
                break;
            }
            case STOP_NOW:
            {
                returnState = "Stopping";
                break;
            }
            case FINDING_HEADING:
            {
                returnState = "Turning";
                break;
            }
            case RADAR_CHECK:
				{
					returnState = "Radar Check";
					break;
				}
            case APPROACHING:
            {
                returnState = "Approaching";
                break;
            }
			case DONE:
			{
				returnState = "Done";
				break;
			}
            default:
            {
                returnState = "Unknown";
                break;
            }
        }
        return returnState;
    }

    public DanookController(Danook chopper, World world)
    {
        super();
        myChopper = chopper;
        myWorld = world;
        desMainRotorSpeed_RPM = 360.0;
        desTailRotorSpeed_RPM = ChopperInfo.STABLE_TAIL_ROTOR_SPEED;
        desTilt_Degrees = 0.0;
        estimatedAcceleration = new Point3D();
        estimatedVelocity = new Point3D();
        actualPosition = new Point3D();
        desiredHeading = 0.0;
        desiredAltitude = 0.0;

        currentDestination = null;
    }

    public synchronized Point3D getDestination()
    {
        if (currentDestination != null)
        {
            return currentDestination.copy();
        }
        else
        {
            return currentDestination;
        }
    }

    public synchronized Point3D getPosition()
    {
        if (actualPosition != null)
        {
            return actualPosition.copy();
        }
        else
        {
            return actualPosition;
        }
    }

	protected void pickDestination()
	{
		if (currentDestination == null)
		{
			currentDestination = findClosestDestination();
			if (currentDestination != null)
			{
				World.dbg(TAG,"Got a destination: " + currentDestination.info(),DC_DBG);
			}
			else
			{
				//currentDestination = new Point3D(myWorld.getCenter());
			}
		}
	}
	
    public synchronized Point3D getVelocity()
    {
        if (estimatedVelocity != null)
        {
            return estimatedVelocity.copy();
        }
        else
        {
            return estimatedVelocity;
        }
    }

    public synchronized Point3D getAcceleration()
    {
        if (estimatedAcceleration != null)
        {
            return estimatedAcceleration.copy();
        }
        else
        {
            return estimatedAcceleration;
        }
    }

    @Override
    public void run()
    {
        // Allow constructors to startup
        try
        {
            Thread.sleep(1);
            myWorld.requestSettings(myChopper.getId(), desMainRotorSpeed_RPM, desTilt_Degrees, desTailRotorSpeed_RPM);
        }
        catch (Exception e)
        {
            World.dbg(TAG,"Caught an exception",DC_DBG);
        }
        Point3D lastPosition = null;
        double currTime = 0.0;
        double lastTime = 0.0;
        while (true)
        {
            try
            {
                // Do smart stuff...
                Thread.sleep(sleepTime_ms,sleepTime_ns);
                synchronized(myWorld)
                {
                    actualPosition = myWorld.gps(myChopper.getId());
					if (homeBase == null && currentDestination != null)
					{
						homeBase = actualPosition.copy();
					}
                }
                currTime = actualPosition.t();
				pickDestination();
                if (lastPosition != null && lastTime < currTime)
                {
                    boolean updated = estimatePhysics(currTime, lastPosition, lastTime);
                    if (updated)
                    {
                        boolean closer = true;
                        if (currentDestination != null)
                        {
                            double oldDistance = lastPosition.distanceXY(currentDestination);
                            double newDistance = actualPosition.distanceXY(currentDestination);
                            if (newDistance > (oldDistance + 0.75)) // Allow some slow movement away
                            {
                                closer = false;
                                World.dbg(TAG,"DC: Wrong way -- now: " + newDistance + " then: " + oldDistance,DC_DBG);

                            }
                        }
                        controlTheShip(closer);
                    }
                    else
                    {
                        World.dbg(TAG,"DC: No physics estimate?",DC_DBG);
                    }
                }
                lastPosition = actualPosition.copy();
                lastTime = currTime;
            }
            catch (Exception e)
            {
                World.dbg(TAG,"Caught an exception: " + e.toString(),DC_DBG);
            }
        }
    }

	protected void adjustSleepTime(boolean sleepLonger)
	{
		double sleepTime_sec = sleepTime_ms / 1000.0 + sleepTime_ns / 1000000000.0;
		sleepTime_sec *= (sleepLonger?1.1:0.9);
		sleepTime_ms = (int)(1000.0 * sleepTime_sec);
		sleepTime_sec -= sleepTime_ms / 1000.0;
		sleepTime_ns = (int)Math.round(1000000000.0 * sleepTime_sec);
	}
	
    /** This method attempts to update estimated physics based on what we learn
     * from the world.  It requires a reasonable amount of time to have passed
     * @param currTime Time stamp of our most current position reading
     * @param lastPos Previous position reading
     * @param lastTime
     * @return
     */
    public boolean estimatePhysics(double currTime, Point3D lastPos, double lastTime)
    {
        final double EPSILON = 0.001;
        boolean updated = false;
        double deltaTime = currTime - lastTime;
        if (deltaTime < EPSILON)
        {
            return updated; // Can't update estimates
        }
        updated = true;
        Point3D oldVelocity = estimateVelocity(lastPos, deltaTime);
        if (oldVelocity != null)
        {
            Point3D oldAcceleration = estimateAcceleration(oldVelocity, deltaTime);
        }
        return updated;
    }

    /** This method attempts to estimate the velocity given all the information
     * we have.
     * @param lastPos The last known position
     * @param deltaTime The time between the readings
     * @return The previous velocity (For use in future estimates)
     */
    public Point3D estimateVelocity( Point3D lastPos, double deltaTime)
    {
        Point3D oldVelocity = estimatedVelocity.copy();
        estimatedVelocity.m_x = (actualPosition.m_x - lastPos.m_x) / deltaTime;
        estimatedVelocity.m_y = (actualPosition.m_y - lastPos.m_y) / deltaTime;
        estimatedVelocity.m_z = (actualPosition.m_z - lastPos.m_z) / deltaTime;
        return oldVelocity;
    }

    public Point3D estimateAcceleration(Point3D oldVelocity, double deltaTime)
    {
        Point3D oldAcceleration = estimatedAcceleration.copy();
        estimatedAcceleration.m_x = (estimatedVelocity.m_x - oldVelocity.m_x) / deltaTime;
        estimatedAcceleration.m_y = (estimatedVelocity.m_y - oldVelocity.m_y) / deltaTime;
        estimatedAcceleration.m_z = (estimatedVelocity.m_z - oldVelocity.m_z) / deltaTime;
        return oldAcceleration;
    }

    public void controlTheShip(boolean isCloser) throws Exception
    {
        // Immediately change state if we're trying to approach and we are
        // getting farther away
        if (myState == APPROACHING && isCloser == false)
        {
            myState = STOP_NOW;
        }
        int nextState = myState;
        switch(myState)
        {
            case STATE_LANDED:
            {
                // Nothing to do, but no exception to be thrown
                break;
            }
            case STOP_NOW:
            {
                // STOP Spinning!
                desTailRotorSpeed_RPM = ChopperInfo.STABLE_TAIL_ROTOR_SPEED;
                myWorld.requestSettings(myChopper.getId(), desMainRotorSpeed_RPM, desTilt_Degrees, desTailRotorSpeed_RPM);
                // approachTarget(true) just tries to stop!
                boolean success = approachTarget(true);
                if (success)
                {
                    nextState = FINDING_HEADING;
                }
                break;
            }
            case FINDING_HEADING:
            {
                boolean headingOK = adjustHeading(false);
                if (headingOK)
                {
                    // Ensure we run through approaching at least once
                    nextState = RADAR_CHECK;
                }
                break;
            }
			case RADAR_CHECK:
			{
				if (actualPosition.Z() > 25)
				{
					nextState = APPROACHING;
				}
				//int nearestObj = myWorld.radar(actualPosition, desiredHeading);
				//if (nearestObj > 400)
				//{
				//	nextState = APPROACHING;
				//	System.out.println(String.format("%2.1f",myWorld.getTimestamp()) + ", pos: " + actualPosition.xyzInfo(1) + " No close contacts!");
				//}
				//else
				//{
				//	System.out.println(String.format("%2.1f",myWorld.getTimestamp()) + ", pos: " + actualPosition.xyzInfo(1) + " avoiding object at distance: " + nearestObj);
				//}
				break;
			}
            case APPROACHING:
            {
                approachTarget(false);
                break;
            }
			case DONE:
			{
				desTilt_Degrees = 0.0;
				desMainRotorSpeed_RPM = 0.0;
				desTailRotorSpeed_RPM = 0.0;
				myWorld.requestSettings(myChopper.getId(),desMainRotorSpeed_RPM, desTilt_Degrees, desTailRotorSpeed_RPM);
				break;
			}
            default:
            {
                Exception e = new Exception("Hey STUPID -- your state is: " + myState + " which is not allowed.");
                throw e;
            }
        }
        myState = nextState;
        selectDesiredAltitude();
        try
        {
            myState = controlAltitude(myState);
        }
        catch (Exception e)
        {
            World.dbg(TAG,"Exception in altitude control: " + e.getMessage(),DC_DBG);
        }
    }


    /** Returns true on a successful command, however, if you ask to stop
     * it will only return true if your X/Y velocity is under 0.1
     * @param justStop
     * @return
     */
    public boolean approachTarget(boolean justStop)
    {
        boolean success = false;
        if (currentDestination == null && justStop == false)
        {
            return false;
        }

        Point3D deltaVector = new Point3D();
        if (justStop == false)
        {
            deltaVector = Point3D.diff(currentDestination,  actualPosition);
        }
        // Compute X Acceleration
        Point3D actualDestination = currentDestination.copy();
        if (justStop == true)
        {
            actualDestination = actualPosition.copy();
        }
        double targetXVelocity = 0.0;
        double targetYVelocity = 0.0;
		// The chopper gets lighter as time goes on (HACK -- improve this)
		double controlMultiplier = 1.0 - 0.00012 * myWorld.getTimestamp();
		double headingRadians = Math.toRadians(desiredHeading);
		Point3D normVel = new Point3D(Math.sin(headingRadians),Math.cos(headingRadians), 0.0);
		//Point3D normVel = estimatedVelocity.normalized2D();
		double xDecelDist = DECEL_DISTANCE_HORZ;
		double yDecelDist = DECEL_DISTANCE_HORZ;
		double xDecelSpd = HORZ_DECEL_SPEED;
		double yDecelSpd = HORZ_DECEL_SPEED;
		if (normVel.xyLength() > 0.5)
		{
			xDecelDist *= Math.abs(normVel.x());
			yDecelDist *= Math.abs(normVel.y());
			xDecelSpd *= Math.abs(normVel.x());
			yDecelSpd *= Math.abs(normVel.y());
		}
        if (justStop == false)
        {
            targetXVelocity = computeDesiredVelocity(actualPosition.m_x,actualDestination.m_x,false,xDecelDist);
        }
        // Repeat for Y
        if (justStop == false)
        {
            targetYVelocity = computeDesiredVelocity(actualPosition.m_y,actualDestination.m_y,false,yDecelDist);
        }
		if (deltaVector.xyLength() < DEADBAND_DIST && estimatedVelocity.xyLength() < STOPPED_CHECK)
		{
			desTilt_Degrees = 0.0;
			myWorld.requestSettings(myChopper.getId(), desMainRotorSpeed_RPM, desTilt_Degrees, desTailRotorSpeed_RPM);
			return true;
		}
		if (normVel.xyLength() > 0.5)
		{
			targetXVelocity *= Math.abs(normVel.x());
			targetYVelocity *= Math.abs(normVel.y());
		}
		Point3D wantVel = new Point3D(targetXVelocity,targetYVelocity,0.0);
        double targetXAcceleration = computeDesiredAcceleration(estimatedVelocity.m_x, targetXVelocity,false,xDecelSpd);
        double targetYAcceleration = computeDesiredAcceleration(estimatedVelocity.m_y, targetYVelocity,false,yDecelSpd);
		if (normVel.xyLength() > 0.5)
		{
			targetXAcceleration *= Math.abs(normVel.x());
			targetYAcceleration *= Math.abs(normVel.y());
		}
		Point3D wantAccel = new Point3D(targetXAcceleration,targetYAcceleration,0.0);
        double xMultiplier = 1.0;
        double deltaXAcceleration = targetXAcceleration - estimatedAcceleration.m_x;
        if (deltaXAcceleration > MAX_HORZ_ACCEL)
        {
            xMultiplier = MAX_HORZ_ACCEL / deltaXAcceleration;
        }
        if (deltaXAcceleration < (-MAX_HORZ_ACCEL))
        {
            xMultiplier = (-MAX_HORZ_ACCEL) / deltaXAcceleration;
        }
        double yMultiplier = 1.0;
        double deltaYAcceleration = targetYAcceleration - estimatedAcceleration.m_y;
        // Compute magnitude of acceleration
        double deltaAcceleration = Math.sqrt(deltaXAcceleration * deltaXAcceleration + deltaYAcceleration * deltaYAcceleration);
        // check heading
        double accelHeading = Math.toDegrees(Math.atan2(deltaXAcceleration,deltaYAcceleration));
        double moveHeading = Math.toDegrees(Math.atan2(deltaVector.m_x, deltaVector.m_y));
		//checkAngles(moveHeading, velHeading, accelHeading);
        double deltaAngle = Math.abs(accelHeading - moveHeading);
		boolean backwardsDetected = false;
        if (deltaAngle > 90.0) // We're going backwards
        {
            deltaAcceleration *= -1.0;
			backwardsDetected = true;
        }
		//if (myWorld.getTimestamp() - (int)myWorld.getTimestamp() < 0.1)
		//{
		//	System.out.println(String.format("%2.1f dist: %2.1f",myWorld.getTimestamp(),deltaVector.xyLength()) + ", bkg: " + backwardsDetected + ", norm: " + normVel.xyInfo(1) + ", Pos: " + deltaVector.xyInfo(1) + " Vel: " + estimatedVelocity.xyInfo(2) + ", want: " + wantVel.xyInfo(2) + ", Acc: " + estimatedAcceleration.xyInfo(2) + ", want: " + wantAccel.xyInfo(2) + String.format(", tilt: %2.2f",desTilt_Degrees));
		//}
        desTilt_Degrees += deltaAcceleration * HORZ_CONTROL_FACTOR * controlMultiplier;
        myWorld.requestSettings(myChopper.getId(), desMainRotorSpeed_RPM, desTilt_Degrees, desTailRotorSpeed_RPM);
        if (justStop == true)
        {
            World.dbg(TAG,"Trying to stop -- acc wanted (" + targetXAcceleration + ","
                    + targetYAcceleration + ") act (" + estimatedAcceleration.m_x + ","
                    + estimatedAcceleration.m_y + ") vel wanted (" + targetXVelocity + ","
                    + targetYVelocity + ") act (" + estimatedVelocity.m_x + ","
                    + estimatedVelocity.m_y + ") des tilt: " + desTilt_Degrees,DC_DBG);
            if (estimatedVelocity.xyLength() < 0.10)
            {
                success = true;
            }
        }
        else
        {
            success = true;
        }
        return success;
    }

    public int controlAltitude(int inState) throws Exception
    {
        int outState = inState;
        if (estimatedAcceleration == null || estimatedVelocity == null)
        {
            return outState; // can't continue if we don't know
        }
        int flightState = myWorld.isAirborn(myChopper.getId());
        boolean onGround = flightState == 0;
        if (onGround)
        {
            System.out.println("On Ground...");
            if (currentDestination != null)
            {
                double actDistance = currentDestination.distanceXY(actualPosition);
                System.out.println("Trying to deliver package -- distance: " + String.format("%2.1f",actDistance));
                if ( actDistance < World.MAX_PACKAGE_DISTANCE)
                {
                    boolean delivered = myWorld.deliverPackage(myChopper.getId());
                    if (delivered)
                    {
                        System.out.println("Successfully delivered package!");
                        boolean pointDeleted = myChopper.deleteWaypoint(currentDestination);
                        currentDestination = null;
                        // it shouldn't be possible, flag controller so they know
                        if (pointDeleted == false)
                        {
                            Exception e = new Exception("World and Chopper out of sync!");
                            throw e;
                        }
                        else
                        {
                            World.dbg(TAG,  "Delivered package!", DC_DBG);
                        }
                    }
                    else
                    {
                        World.dbg(TAG, "Couldn't deliver package at pos: ", DC_DBG);
                    }
                }
                else
                {
                    World.dbg(TAG,"Too far away to deliver package, was " + actDistance + ", want: " + World.MAX_PACKAGE_DISTANCE ,DC_DBG);
                }
                outState = FINDING_HEADING;
            }
            else
            {
                World.dbg(TAG, "Landed with no destination?", DC_DBG);
            }
            if (inState == APPROACHING)
            {
                outState = STATE_LANDED;
            }
            //World.dbg(TAG, "inState: " + inState + ", outState: " + outState, DC_DBG);
            return outState;
        }
        else
        {
            if (inState == STATE_LANDED)
			{
				if (currentDestination != null)
            	{
                	outState = FINDING_HEADING;
            	}
				else
				{
					outState = DONE;
				}
			}
        }
        double targetVerticalVelocity = computeDesiredVelocity(actualPosition.m_z,desiredAltitude,true, DECEL_DISTANCE_VERT);
        double deltaVelocity = targetVerticalVelocity - estimatedVelocity.m_z;
        double targetVerticalAcceleration = computeDesiredAcceleration(estimatedVelocity.m_z, targetVerticalVelocity,true,VERT_DECEL_SPEED);
        double deltaAcceleration = targetVerticalAcceleration - estimatedAcceleration.m_z;
        if (deltaAcceleration > MAX_VERT_ACCEL)
        {
            deltaAcceleration = MAX_VERT_ACCEL;
        }
        if (deltaAcceleration < (-MAX_VERT_ACCEL))
        {
            deltaAcceleration = (-MAX_VERT_ACCEL);
        }
        desMainRotorSpeed_RPM += deltaAcceleration * VERT_CONTROL_FACTOR;
        int msTime = (int)Math.floor(actualPosition.t() * 1000);
        int desHeight_mm = (int)Math.floor(desiredAltitude * 1000);
        int actHeight_mm = (int)Math.floor(actualPosition.m_z * 1000);
        int desVel = (int)Math.floor(targetVerticalVelocity * 1000);
        int actVel = (int)Math.floor(estimatedVelocity.m_z * 1000);
        int desAcc = (int)Math.floor(targetVerticalAcceleration * 1000);
        int actAcc = (int)Math.floor(estimatedAcceleration.m_z * 1000);
        myWorld.requestSettings(myChopper.getId(), desMainRotorSpeed_RPM, desTilt_Degrees, desTailRotorSpeed_RPM);
    	/* World.dbg(TAG," Time: " + msTime + ", Want mm: " + desHeight_mm + ", Alt mm: " + actHeight_mm +
    			", actVel: " + estimatedVelocity.m_z + ", deltaAcc: " +
    			deltaAcceleration + ", desired accel: " + targetVerticalAcceleration,DC_DBG); */
        return outState;
    }

    public double computeDesiredVelocity(double actAlt, double desAlt, boolean doVertical, double DECEL_DISTANCE)
    {
        double targetVelocity = doVertical?MAX_VERT_VELOCITY:MAX_HORZ_VELOCITY;
        double deltaValue = Math.abs(desAlt - actAlt);
        if (deltaValue < DECEL_DISTANCE)
        {
            targetVelocity *= (deltaValue / DECEL_DISTANCE);
        }
        if (actAlt > desAlt)
        {
            targetVelocity *= -1.0;
        }
        return targetVelocity;
    }

    public double computeDesiredAcceleration(double actVel, double desVel, boolean doVertical, double DECEL_SPEED)
    {
        double targetAccel = (doVertical?MAX_VERT_ACCEL:MAX_HORZ_ACCEL);
        double deltaValue = Math.abs(desVel - actVel);
        if (deltaValue < DECEL_SPEED)
        {
            targetAccel *= (deltaValue / DECEL_SPEED);
        }
        if (actVel > desVel)
        {
            targetAccel *= -1.0;
        }
        return targetAccel;
    }

    public boolean adjustHeading(boolean useVelocity)
    {
        boolean headingOK = false;
        if (currentDestination == null)
        {
            return headingOK;
        }
        Point3D transformation = myWorld.transformations(myChopper.getId());
        if (transformation == null)
        {
            return headingOK;
        }
        double actHeading = transformation.m_x;
        double deltaY = currentDestination.m_y - actualPosition.m_y;
        double deltaX = currentDestination.m_x - actualPosition.m_x;
        if (useVelocity) // Strike that
        {
            deltaY = estimatedVelocity.y();
            deltaX = estimatedVelocity.x();
        }
        desiredHeading = Math.toDegrees(Math.atan2(deltaX,deltaY));
        if (desiredHeading < 0.0) // NOTE, returns -180 to +180
        {
            desiredHeading += 360.0;
        }
        double deltaHeading = desiredHeading - actHeading;
        if (deltaHeading < -180.0)
        {
            deltaHeading += 360.0;
        }
        else if (deltaHeading > 180.0)
        {
            deltaHeading -= 360.0;
        }
        if (Math.abs(deltaHeading) < 0.008)
        {
            desTailRotorSpeed_RPM = ChopperInfo.STABLE_TAIL_ROTOR_SPEED;
            // TODO: Future optimization -- don't do this every tick?
            myWorld.requestSettings(myChopper.getId(), desMainRotorSpeed_RPM, desTilt_Degrees, desTailRotorSpeed_RPM);
            headingOK = true;
        }
        else
        {
            // Anything over 10 degrees off gets max rotor speed
            double deltaRotor = (deltaHeading / 10.0) * 20.0;
            if (deltaRotor > 5.5)
            {
                deltaRotor = 5.5;
            }
            else if (deltaRotor < -5.5)
            {
                deltaRotor = -5.5;
            }
            desTailRotorSpeed_RPM = ChopperInfo.STABLE_TAIL_ROTOR_SPEED + deltaRotor;
            myWorld.requestSettings(myChopper.getId(), desMainRotorSpeed_RPM, desTilt_Degrees, desTailRotorSpeed_RPM);
        }
        return headingOK;
    }

    public void selectDesiredAltitude()
    {
        if (currentDestination != null)
        {
            if (actualPosition.distanceXY(currentDestination) > 5.0)
            {
                desiredAltitude = 105.0; // High enough to clear buildings
            }
            else
            {
                desiredAltitude = 0.0;
            }
        }
        else
        {
            desiredAltitude = 2.0 + 98.0 * (Math.floor(myWorld.getTimestamp() / 100.0)%2);
        }
    }
	
    synchronized public Point3D findClosestDestination()
    {
        Point3D resultPoint = null;
        ArrayList<Point3D> targetWaypoints = myChopper.getWaypoints();
        double minDistance = 10000.0;
		if (targetWaypoints.isEmpty())
		{
			resultPoint = homeBase.copy();
		}
		else
		{
        	for(Point3D testPoint: targetWaypoints)
        	{
            	double curDistance = actualPosition.distanceXY(testPoint);
            	if (curDistance < minDistance)
            	{
                	resultPoint = testPoint;
                	minDistance = curDistance;
            	}
        	}
		}
        return resultPoint;
    }

    synchronized public void setDesiredRotorSpeed(double newSpeed)
    {
        desMainRotorSpeed_RPM = newSpeed;
        myWorld.requestSettings(myChopper.getId(),desMainRotorSpeed_RPM,desTilt_Degrees,desTailRotorSpeed_RPM);
    }

    synchronized public void setDesiredTailRotorSpeed(double newSpeed)
    {
        desTailRotorSpeed_RPM = newSpeed;
        myWorld.requestSettings(myChopper.getId(),desMainRotorSpeed_RPM,desTilt_Degrees,desTailRotorSpeed_RPM);
    }

    synchronized public void setDesiredTilt(double newTilt)
    {
        desTilt_Degrees = newTilt;
        myWorld.requestSettings(myChopper.getId(),desMainRotorSpeed_RPM,desTilt_Degrees,desTailRotorSpeed_RPM);
    }
}
