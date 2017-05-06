package heli.org.helidroid;

//-*-java-*-
// *************************************************************************
// *                           MODULE SOURCE FILE                          *
// *************************************************************************
//
//           CONFIDENTIAL AND PROPRIETARY INFORMATION   (UNPUBLISHED)
//
//  All Rights Reserved.
//
//  This document  contains confidential and  proprietary  information of
//  Sasha Industries Inc.  and contains patent rights or pending,  trade
//  secrets and or  copyright protected or  pending data  and shall not be
//  reproduced or electronically reproduced or transmitted or disclosed in
//  whole or in part or used for any design or manufacture except when the
//  user possess direct written authorization from Sasha Industries Inc.
//  Its  receipt or possession  does not convey any  rights to  reproduce,
//  disclose its contents,  or to manufacture, use or sell anything it may
//  describe.
//
//  File Name:      World.java
//
//
//  Module Name:
//
//  Creation:       Mar 1, 2015 1:55:04 PM
//
//  Document/Part #:
//
//  Description:
//
//
//

import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

/** World Class, for StigChoppers.  Defines the world.
 * Copyright 2015, Daniel A. LaFuze
 * @author dlafuze
 *
 */
public class World
{
    static public final String TAG = "World";
    static public long m_dbgMask = 0;
    static public final long WORLD_DBG = 0x10000000;
    static public int m_camToFollow = 0;
    private int nextChopperID = 0;
    private double m_rtToRndRatio = 1.0;
    private int sizeX;
    private int sizeY;
    private int sizeZ;
    private double[] chop1Color;
    private double[] chop2Color;
    private double curTimeStamp = 0.0;
    private static final double TICK_TIME = 1.0 / 50.0;

    public static final double MAX_PACKAGE_DISTANCE = 2.0;

    private HeliGLSurfaceView glSurface;

    private double maxTime = 10000.0;

    private ArrayList<Object3D> worldState;

    private Map<Integer, ChopperAggregator> myChoppers;

    /** With this array, the world will attempt to maintain a list of all
     * addresses given to the delivery choppers so it can validate
     * attempted deliveries.
     */
    private ArrayList<Point3D> allPackageLocs;

    static public String mName()
    {
        try
        {
            return Thread.currentThread().getStackTrace()[2].getMethodName() + ": ";
        }
        catch(Exception e)
        {
            return "unk. method: ";
        }
    }
    static public String mName(int depth)
    {
        try
        {
            return Thread.currentThread().getStackTrace()[depth].getMethodName() + ": ";
        }
        catch(Exception e)
        {
            return "unk. method: ";
        }
    }

    static public void dbg(String tag, String msg, long bit)
    {
        if((m_dbgMask & bit) != 0)
        {
            System.out.println(tag + ":" + mName(3) + msg);
        }
    }

    public void insertChopper(StigChopper chap)
    {
        int chopperID = chap.getId();
        Point3D startPos = getStartingPosition(chopperID);
        ChopperInfo chopInfo = new ChopperInfo(this, chap, chopperID, startPos, 0.0);
        ChopperAggregator myAggregator = new ChopperAggregator(chap, chopInfo);
        myChoppers.put(chopperID, myAggregator);
    }

    synchronized double timeRatio()
    {
        return m_rtToRndRatio;
    }

    /** This method gives the choppers some random locations to deliver
     * packages to.  For now, I'm selecting easy to reach places within
     * the center of blocks
     */
    private void setChopperWaypoints()
    {
        Iterator it = myChoppers.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<Integer, ChopperAggregator> pairs = (Map.Entry)it.next();
            int id = pairs.getKey();
            ChopperAggregator locData = pairs.getValue();
            if (locData != null)
            {
                StigChopper theChopper = locData.getChopper();
                ArrayList targetPoints = new ArrayList<Point3D>();
                for (int i = 0; i < theChopper.itemCount(); ++i)
                {
                    long whichRow = Math.round(Math.floor(Math.random() * 10.0));
                    long whichCol = Math.round(Math.floor(Math.random() * 10.0));
                    // Open space mid block extends from about 20 to 80
                    double inBlockX = 20.0 + Math.random() * 60.0;
                    double inBlockY = 20.0 + Math.random() * 60.0;
                    double targetX = 100.0 * whichCol + inBlockX;
                    double targetY = 100.0 * whichRow + inBlockY;
                    double targetZ = 0.1; // There's a curb height
                    Point3D targetPoint = new Point3D(targetX, targetY, targetZ);
                    targetPoints.add(targetPoint);
                }
                theChopper.setWaypoints(targetPoints);
                allPackageLocs.addAll(targetPoints);
            }
        }
    }

    public int isAirborn(int id)
    {
        int retVal = 1;
        ChopperAggregator ca = myChoppers.get(id);
        ChopperInfo info = ca.getInfo();
        // TODO: Implement crashed
        if (info.onGround() == true)
        {
            retVal = 0;
        }
        return retVal;
    }

    public double getFuelRemaining(int id)
    {
        ChopperAggregator ca = myChoppers.get(id);
        double fuelLeft = 0.0;
        if (ca != null)
        {
            ChopperInfo info = ca.getInfo();
            fuelLeft = info.getFuelRemaining();
        }
        return fuelLeft;
    }

    public boolean deliverPackage(int id)
    {
        boolean success = false;
        ChopperAggregator ca = myChoppers.get(id);
        Point3D myPos = gps(id);
        if (ca != null)
        {
            ChopperInfo info = ca.getInfo();
            StigChopper chop = ca.getChopper();
            if (info.onGround())
            {
                // OK, check position
                // NOTE: I believe the hashCode function is used to determine
                // if the container has the object.  That only includes X,Y,Z
                // which is what I think we want.
                for (Point3D object : allPackageLocs)
                {
                    if (object.distanceXY(myPos) < 5.0)
                    {
                        allPackageLocs.remove(object);
                        success = true;
                        break;
                    }
                }
                if (success == false)
                {
                    dbg(TAG,"Couldn't find package to deliver at  (" + myPos.info() + ")", WORLD_DBG);
                }
            }
        }
        return success;
    }

    // TODO: Replace args functionality
    /**
     * @param surf
     * @throws Exception
     */
    public World(HeliGLSurfaceView surf) throws Exception
    {
        glSurface = surf;
        HeliGLRenderer theRenderer = glSurface.getRenderer();
        sizeX = 1000;
        sizeY = 1000;
        sizeZ = 200;

        /*
        for (String thisArg: args)
        {
            // I want my arguments to be lower case
            String lowerArg = thisArg.toLowerCase();
            // Strip dashes in case they do it the standard way, I don't want to worry about this yet
            // TODO: Worry about this later
            String strippedArg = lowerArg.replace("-", "");
            String[] splits = lowerArg.split(":");
            if (splits.length != 2)
            {
                if (!lowerArg.equals("h"))
                {
                    System.out.println("Ignoring improperly formatted argument!");
                    continue;
                }
            }
            // TODO: Add sanity checking on all arguments etc.
            switch(splits[0].charAt(0))
            {
                case 'x':
                {
                    sizeX = Integer.parseInt(splits[1]);
                    break;
                }
                case 'y':
                {
                    sizeY = Integer.parseInt(splits[1]);
                    break;
                }
                case 'z':
                {
                    sizeZ = Integer.parseInt(splits[1]);
                    break;
                }
                case 'd':
                {
                    m_dbgMask = Integer.parseInt(splits[1].replaceAll("0x",""),16);
                    break;
                }
                case 'c':
                {
                    m_camToFollow = Integer.parseInt(splits[1]);
                    break;
                }
                case 'f':
                {
                    m_rtToRndRatio = Double.parseDouble(splits[1]);
                    break;
                }
                case 'h':
                {
                    System.out.println("Command Line Arguments:");
                    System.out.println("-----------------------");
                    System.out.println("x:Number (X World Size   -- default 1000)");
                    System.out.println("y:Number (Y World Size   -- default 1000)");
                    System.out.println("z:Number (z World Size   -- default 1000)");
                    System.out.println("h        (This Help Message");
                    System.out.println("d:msk debug mask 0xF - dan, 0xF0 sasha, 0xF000000 world");
                    System.out.println("c:idx index of chopper for camera to follow");
                    System.out.println("f:rat - ratio of world to real time 1 - for real-time 10 - 10x faster");
                    break;
                }
                default:
                {
                    System.out.println("Unhandled command line argument '" + thisArg + "'");
                    break;
                }
            }
        } */
        myChoppers = new HashMap<Integer, ChopperAggregator>();

        //inserting choppers
        Apachi apChop = new Apachi(requestNextChopperID(),this);
        insertChopper(apChop);

        Danook myChopper = new Danook(requestNextChopperID(), this);
        insertChopper(myChopper);


        //worldState = new ArrayList<Object3D>();

        allPackageLocs = new ArrayList<Point3D>();
        // Give the choppers somewhere to go
        setChopperWaypoints();

        theRenderer.setWorld(this);
    }

    public ArrayList<Object3D> getObjects()
    {
        return worldState;
    }

    /** This method returns the number of seconds that have passed since
     * time started.
     * @return
     */
    public double getTimestamp()
    {
        return curTimeStamp;
    }

    // TODO: Provide for random starting positions, but for now, start them
    // on main street
    public Point3D getStartingPosition(int chopperID)
    {
        Point3D startPos = new Point3D(500.0, 495.0 + 10.0 * chopperID, 0.0);
        return startPos;
    }

    /** Return the chopper with the specified ID
     * or null if that chopper doesn't exist
     * @param chopperID ID of the desired chopper
     * @return
     */
    public StigChopper getChopper(int chopperID) {
        ChopperAggregator resAggregator = null;
        StigChopper resChopper = null;
        if (myChoppers.containsKey(chopperID))
        {
            resAggregator = myChoppers.get(chopperID);
            resChopper = resAggregator.getChopper();
        }
        return resChopper;
    }

    /** Return the chopper info with the specified ID
     * or null if that chopper doesn't exist
     * @param chopperID ID of the desired chopper
     * @return
     */
    public ChopperInfo getChopInfo(int chopperID)
    {
        ChopperAggregator resAggregator = null;
        ChopperInfo resInfo = null;
        if (myChoppers.containsKey(chopperID))
        {
            resAggregator = myChoppers.get(chopperID);
            resInfo = resAggregator.getInfo();
        }
        return resInfo;
    }

    public void requestSettings(int chopperID, double mainRotorSpeed, double tiltAngle, double tailRotorSpeed)
    {
        ChopperAggregator resAggregator = null;
        ChopperInfo resInfo = null;
        if (myChoppers.containsKey(chopperID))
        {
            resAggregator = myChoppers.get(chopperID);
            resInfo = resAggregator.getInfo();
            if (resInfo != null)
            {
                resInfo.requestMainRotorSpeed(mainRotorSpeed);
                resInfo.requestTailRotorSpeed(tailRotorSpeed);
                resInfo.requestTiltLevel(tiltAngle);
                resAggregator.setInfo(resInfo);
                myChoppers.put(chopperID, resAggregator);
            }
        }
    }

    public int requestNextChopperID() { return nextChopperID++; }

    public void tick() throws Exception
    {
         synchronized(this)
         {
             Iterator it = myChoppers.entrySet().iterator();
             while (it.hasNext())
             {
                 Map.Entry<Integer, ChopperAggregator> pairs = (Map.Entry)it.next();
                 int id = pairs.getKey();
                 ChopperAggregator locData = pairs.getValue();
                 if (locData != null)
                 {
                     ChopperInfo chopInfo = locData.getInfo();
                     if (chopInfo != null)
                     {
                         chopInfo.fly(curTimeStamp, TICK_TIME);
                         locData.setInfo(chopInfo);
                         myChoppers.put(id, locData);
                     }
                 }
             }
         }
         HeliGLRenderer renderer = glSurface.getRenderer();
         // TODO: Remove hard-coding etc.
         //renderer.orbitCamera(5.0);
		 glSurface.requestRender();
         curTimeStamp += TICK_TIME;
    }

    public static float[] makeVertexArray(Point3D inPoint, Point3D boxSize)
    {
        float[] resultArray = null;
        if (inPoint == null)
        {
            return resultArray;
        }
        // 8 vertexes, 3 coordinates each (Add one for center at end)
        resultArray = new float[27];
        float xStart = (float) inPoint.x();
        float yStart = (float) inPoint.y();
        float zStart = (float) inPoint.z();

        float xSize = (float) boxSize.x();
        float ySize = (float) boxSize.y();
        float zSize = (float) boxSize.z();
// Vertex 1
        resultArray[0] = xStart;
        resultArray[1] = yStart + ySize;
        resultArray[2] = zStart + zSize;
        // Vertex 2
        resultArray[3] = xStart;
        resultArray[4] = yStart;
        resultArray[5] = zStart + zSize;
        // Vertex 3
        resultArray[6] = xStart + xSize;
        resultArray[7] = yStart;
        resultArray[8] = zStart + zSize;
        // Vertex 4
        resultArray[9] = xStart + xSize;
        resultArray[10] = yStart + ySize;
        resultArray[11] = zStart + zSize;
        // Vertex 5
        resultArray[12] = xStart;
        resultArray[13] = yStart + ySize;
        resultArray[14] = zStart;
        // Vertex 6
        resultArray[15] = xStart + xSize;
        resultArray[16] = yStart + ySize;
        resultArray[17] = zStart;
        // Vertex 7
        resultArray[18] = xStart + xSize;
        resultArray[19] = yStart;
        resultArray[20] = zStart;
        // Vertex 8
        resultArray[21] = xStart;
        resultArray[22] = yStart;
        resultArray[23] = zStart;
        // Vertex 9 (Extra -- at center)
        resultArray[24] = xStart + xSize / 2.0f;
        resultArray[25] = yStart + ySize / 2.0f;
        resultArray[26] = zStart + zSize / 2.0f;
        return resultArray;
    }

    synchronized public Point3D gps(int chopperID)
    {
        ChopperAggregator thisAg = null;
        Point3D retPosition = null;
        if (myChoppers.containsKey(chopperID))
        {
            thisAg = myChoppers.get(chopperID);
            ChopperInfo thisInfo = thisAg.getInfo();
            retPosition = thisInfo.getPosition();
        }
        return retPosition;
    }

    /** This method returns heading, tilt, and zero in a single vector
     *  They're returned in degrees
     * @param chopperID
     * @return
     */
    public Point3D transformations(int chopperID)
    {
        Point3D resultVector = new Point3D();
        ChopperAggregator thisAg = null;
        Point3D actPosition = null;
        if (myChoppers.containsKey(chopperID))
        {
            thisAg = myChoppers.get(chopperID);
            ChopperInfo thisInfo = thisAg.getInfo();
            resultVector.m_x = thisInfo.getHeading();
            resultVector.m_y = thisInfo.getTilt();
        }
        return resultVector;
    }

    public void addPanels()
    {
        //for all choppers
        Iterator<Map.Entry<Integer, ChopperAggregator>> it = myChoppers.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<Integer, ChopperAggregator> pairs = it.next();
            ChopperAggregator locData = pairs.getValue();
            if (locData != null)
            {
                StigChopper theChopper = locData.getChopper();
                //m_chopperInfoPanel.add(theChopper.m_info);
            }
        }
    }
}
