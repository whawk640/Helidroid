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

    private static final double FULL_BLOCK_SIZE = 100.0;

    private static final double STREET_OFFSET = 3.0;

    private static final double SIDEWALK_OFFSET = 2.0;

    private static final double BLOCK_SIZE = FULL_BLOCK_SIZE - 2.0 * STREET_OFFSET;

    private static final double SQUARE_SIZE = BLOCK_SIZE - 2.0 * SIDEWALK_OFFSET;

    private static final double BUILDING_SPACE = (SQUARE_SIZE / 10.0);

    private static final double BUILDING_SIZE = 0.9 * BUILDING_SPACE;

    private static final double HOUSES_PER_BLOCK = 10.0;

    public static final double MAX_PACKAGE_DISTANCE = 2.0;

    private Camera camera;

    private HeliGLRenderer renderer;

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
     * @param rend
     * @throws Exception
     */
    public World(HeliGLRenderer rend) throws Exception
    {
        renderer = rend;
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

        worldState = new ArrayList<Object3D>();

        // Generate the world... TODO: Move to city blocks
        for (int row = 0; row < 10; ++row)
        {
            for (int col = 0; col < 10; ++col)
            {
                // Generate a city block
                // TODO: Move to CityBlock class
                // For now, streets are 6.0 m wide
                // and Sidewalks are 3.0 m wide
                double startX = FULL_BLOCK_SIZE * col + STREET_OFFSET;
                double endX = startX + BLOCK_SIZE;
                double startY = FULL_BLOCK_SIZE * row + STREET_OFFSET;
                double endY = startY + BLOCK_SIZE;
                // Sidewalks are 0.1 m above street
                Point3D sidewalkPos = new Point3D(startX, startY, 0.0);
                Point3D sidewalkSize = new Point3D(BLOCK_SIZE, BLOCK_SIZE, 0.1);
                Object3D sidewalk = new Object3D(sidewalkPos, sidewalkSize);
                double startZ = 0.1;
                sidewalk.setColor(0.8, 0.8, 0.8, 1.0);
                worldState.add(sidewalk);
                startX += 0.05 * BUILDING_SPACE + SIDEWALK_OFFSET;
                startY += 0.05 * BUILDING_SPACE + SIDEWALK_OFFSET;
                for (int houseIndex = 0; houseIndex < Math.round(HOUSES_PER_BLOCK); ++houseIndex)
                {
                    Object3D leftHouse = makeHouse(startX, startY + houseIndex * BUILDING_SPACE, startZ);
                    worldState.add(leftHouse);
                    Object3D rightHouse = makeHouse(startX + 9 * BUILDING_SPACE, startY + houseIndex * BUILDING_SPACE, startZ);
                    worldState.add(rightHouse);
                    if (houseIndex == 0 || houseIndex == 9)
                    {
                        continue;
                    }
                    Object3D topHouse = makeHouse(startX + houseIndex * BUILDING_SPACE, startY, startZ);
                    worldState.add(topHouse);
                    Object3D bottomHouse = makeHouse(startX  + houseIndex * BUILDING_SPACE, startY + 9 * BUILDING_SPACE, startZ);
                    worldState.add(bottomHouse);
                }
            }
        }
        Point3D locPos = new Point3D(30.0, 30.0, 0.0);
        Point3D locSize = new Point3D(40.0, 40.0, Math.random() * 20.0 + 5.0);
        double r = Math.random() * 0.5;
        double g = Math.random() * 0.5 + 0.25;
        double b = Math.random() * 0.5 + 0.5;
        double a = Math.random() * 0.25 + 0.75;
        Object3D newObject = new Object3D(locPos, locSize);
        newObject.setColor(r, g, b, a);
        worldState.add(newObject);

        camera = new Camera(sizeX/2, sizeY/2,0);
        allPackageLocs = new ArrayList<Point3D>();
        // Give the choppers somewhere to go
        setChopperWaypoints();
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

    public void updateCamera(GL10 gl, int width, int height)
    {
        //camera.tellGL(gl, width, height);
        dbg(TAG,"Updated camera with vp size (" + width + ", " + height + ")", WORLD_DBG);
    }

    public int requestNextChopperID() { return nextChopperID++; }

    public Object3D makeHouse(double posX, double posY, double posZ)
    {
        double buildingHeight = computeBuildingHeight();
        Point3D buildingPos = new Point3D(posX, posY, posZ);
        Point3D buildingSize = new Point3D(BUILDING_SIZE, BUILDING_SIZE, buildingHeight);
        Object3D worldObj = new Object3D(buildingPos, buildingSize);
        worldObj.setColor(0.6, 0.6 + 0.4 * Math.random(), 0.6 + 0.4 * Math.random(), 1.0);
        return worldObj;
    }

    public double computeBuildingHeight()
    {
        double buildingHeight = 10.0 + Math.random() * 10.0;
        double exceptChance = Math.random();
        if (exceptChance >= 0.98)
        {
            buildingHeight *= 5.0;
        }
        else if (exceptChance >= 0.9)
        {
            buildingHeight *= 2.0;
        }
        return buildingHeight;
    }

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

    /*
    public void render(GLAutoDrawable drawable, Texture texture)
    {
        // different transformations
        GL2 gl = drawable.getGL().getGL2();
        camera.tellGL(gl);
        Point3D chopperPos = gps(m_camToFollow);
        camera.chase(chopperPos, 20.0);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        for (Object3D object : worldState)
        {
            double objColor[] = object.getColor();
            Point3D objectLoc = object.getPosition();
            Point3D objectSize = object.getSize();
            float[] bufferArray = makeVertexArray(objectLoc, objectSize);
            if (bufferArray == null)
            {
                continue;
            }
            gl.glColor4dv(objColor, 0);
            drawRectangles(gl,bufferArray, true, texture);
            Point3D helipadCenter = new Point3D(objectLoc.m_x + (objectSize.m_x / 2.0), objectLoc.m_y + (objectSize.m_y / 2.0), objectLoc.m_z + objectSize.m_z);
            //drawHelipad(gl, helipadCenter.m_x, helipadCenter.m_y, helipadCenter.m_z +0.05, objectSize.m_x, texture);
        }
        gl.glBegin(gl.GL_QUADS);
        gl.glColor3d(1.0, 0.8, 0.8);
        gl.glVertex3d(497.0, 503.0,  0.0);
        gl.glVertex3d(497.0, 497.0, 0.0);
        gl.glVertex3d(503.0, 497.0, 0.0);
        gl.glVertex3d(503.0, 503.0, 0.0);
        gl.glEnd();
        drawHelipad(gl, 500.0, 495.0, 0.05, 6.0, texture);
        drawHelipad(gl, 500.0, 505.0, 0.05, 6.0, texture);
        Iterator it = myChoppers.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<Integer, ChopperAggregator> pairs = (Map.Entry)it.next();
            int id = (int) pairs.getKey();
            ChopperAggregator locData = (ChopperAggregator) pairs.getValue();
            if (locData != null)
            {
                StigChopper theChopper = locData.getChopper();
                ChopperInfo theInfo = locData.getInfo();
                double curHeading = theInfo.getHeading();
                double curTilt = theInfo.getTilt();
                double rotorPos = theInfo.getMainRotorPosition();
                double tailRotorPos = theInfo.getTailRotorPosition();
                theChopper.render(drawable, curHeading, curTilt, rotorPos, tailRotorPos);
            }
        }
    }

    public void drawHelipad(GL2 gl, double xCenter, double yCenter, double zHeight, double size, Texture texture)
    {
        if (texture != null)
        {
            double halfSize = size / 2.0;
            gl.glEnable(gl.GL_TEXTURE_2D);
            texture.enable(gl);
            texture.bind(gl);
            gl.glBegin(gl.GL_QUADS);
            gl.glColor3d(1.0, 1.0, 1.0);
            gl.glTexCoord2d(0.0, 1.0);
            gl.glVertex3d(xCenter - halfSize, yCenter + halfSize, zHeight);
            gl.glTexCoord2d(0.0, 0.0);
            gl.glVertex3d(xCenter - halfSize, yCenter - halfSize, zHeight);
            gl.glTexCoord2d(1.0, 0.0);
            gl.glVertex3d(xCenter + halfSize, yCenter - halfSize, zHeight);
            gl.glTexCoord2d(1.0, 1.0);
            gl.glVertex3d(xCenter + halfSize, yCenter + halfSize, zHeight);
            gl.glEnd();
            gl.glDisable(gl.GL_TEXTURE_2D);
        }
        else
        {
            double sizeIncrement = size / 6.0;
            // Do it the old way
            gl.glBegin(gl.GL_QUADS);
            // Background Square
            gl.glColor3d(1.0, 1.0, 1.0);
            gl.glVertex3d(xCenter - 3.0 * sizeIncrement, yCenter + 3.0 * sizeIncrement, zHeight);
            gl.glVertex3d(xCenter - 3.0 * sizeIncrement, yCenter - 3.0 * sizeIncrement, zHeight);
            gl.glVertex3d(xCenter + 3.0 * sizeIncrement, yCenter - 3.0 * sizeIncrement, zHeight);
            gl.glVertex3d(xCenter + 3.0 * sizeIncrement, yCenter + 3.0 * sizeIncrement, zHeight);
            // Left Side H
            gl.glColor3d(0.0, 0.0, 1.0);
            gl.glVertex3d(xCenter - 2.0 * sizeIncrement, yCenter + 2.0 * sizeIncrement, zHeight);
            gl.glVertex3d(xCenter - 2.0 * sizeIncrement, yCenter - 2.0 * sizeIncrement, zHeight);
            gl.glVertex3d(xCenter - 1.0 * sizeIncrement, yCenter - 2.0 * sizeIncrement, zHeight);
            gl.glVertex3d(xCenter - 1.0 * sizeIncrement, yCenter + 2.0 * sizeIncrement, zHeight);
            // Right Side H
            gl.glVertex3d(xCenter + 1.0 * sizeIncrement, yCenter + 2.0 * sizeIncrement, zHeight);
            gl.glVertex3d(xCenter + 1.0 * sizeIncrement, yCenter - 2.0 * sizeIncrement, zHeight);
            gl.glVertex3d(xCenter + 2.0 * sizeIncrement, yCenter - 2.0 * sizeIncrement, zHeight);
            gl.glVertex3d(xCenter + 2.0 * sizeIncrement, yCenter + 2.0 * sizeIncrement, zHeight);
            // Middle H
            gl.glVertex3d(xCenter - 1.0 * sizeIncrement, yCenter + 0.5 * sizeIncrement, zHeight);
            gl.glVertex3d(xCenter - 1.0 * sizeIncrement, yCenter - 0.5 * sizeIncrement, zHeight);
            gl.glVertex3d(xCenter + 1.0 * sizeIncrement, yCenter - 0.5 * sizeIncrement, zHeight);
            gl.glVertex3d(xCenter + 1.0 * sizeIncrement, yCenter + 0.5 * sizeIncrement, zHeight);
            gl.glEnd();
        }
    }

    public static void drawRectangles(GL2 gl, float[] bufferArray, boolean doLines, Texture roofTexture)
    {
        if (roofTexture != null)
        {
            gl.glEnable(gl.GL_TEXTURE_2D);
            roofTexture.enable(gl);
            roofTexture.bind(gl);
        }
        gl.glBegin(GL2.GL_QUADS);
        // Top face
        gl.glTexCoord2d(0.0, 1.0);
        gl.glVertex3fv(bufferArray,0);
        gl.glTexCoord2d(0.0, 0.0);
        gl.glVertex3fv(bufferArray,3);
        gl.glTexCoord2d(1.0, 0.0);
        gl.glVertex3fv(bufferArray,6);
        gl.glTexCoord2d(1.0, 1.0);
        gl.glVertex3fv(bufferArray,9);
        gl.glEnd();
        if (roofTexture != null)
        {
            gl.glDisable(gl.GL_TEXTURE_2D);
        }

        gl.glBegin(GL2.GL_QUADS);
        // Bottom face
        gl.glVertex3fv(bufferArray,12);
        gl.glVertex3fv(bufferArray,21);
        gl.glVertex3fv(bufferArray,18);
        gl.glVertex3fv(bufferArray,15);
        // Left face
        gl.glVertex3fv(bufferArray,0);
        gl.glVertex3fv(bufferArray,3);
        gl.glVertex3fv(bufferArray,21);
        gl.glVertex3fv(bufferArray,12);
        // Right face
        gl.glVertex3fv(bufferArray,15);
        gl.glVertex3fv(bufferArray,18);
        gl.glVertex3fv(bufferArray,6);
        gl.glVertex3fv(bufferArray,9);
        // Front face
        gl.glVertex3fv(bufferArray,0);
        gl.glVertex3fv(bufferArray,12);
        gl.glVertex3fv(bufferArray,15);
        gl.glVertex3fv(bufferArray,9);
        // Back face
        gl.glVertex3fv(bufferArray,3);
        gl.glVertex3fv(bufferArray,6);
        gl.glVertex3fv(bufferArray,18);
        gl.glVertex3fv(bufferArray,21);
        gl.glEnd();
        if (doLines)
        {
            gl.glColor3d(0.25, 0.25, 0.25);
            // Top face
            gl.glBegin(GL.GL_LINE_LOOP);
            gl.glVertex3fv(bufferArray,0);
            gl.glVertex3fv(bufferArray,3);
            gl.glVertex3fv(bufferArray,6);
            gl.glVertex3fv(bufferArray,9);
            gl.glEnd();
            // Bottom face
            gl.glBegin(GL.GL_LINE_LOOP);
            gl.glVertex3fv(bufferArray,12);
            gl.glVertex3fv(bufferArray,21);
            gl.glVertex3fv(bufferArray,18);
            gl.glVertex3fv(bufferArray,15);
            gl.glEnd();
            // Left face
            gl.glBegin(GL.GL_LINE_LOOP);
            gl.glVertex3fv(bufferArray,0);
            gl.glVertex3fv(bufferArray,3);
            gl.glVertex3fv(bufferArray,21);
            gl.glVertex3fv(bufferArray,12);
            gl.glEnd();
            // Right face
            gl.glBegin(GL.GL_LINE_LOOP);
            gl.glVertex3fv(bufferArray,15);
            gl.glVertex3fv(bufferArray,18);
            gl.glVertex3fv(bufferArray,6);
            gl.glVertex3fv(bufferArray,9);
            gl.glEnd();
            // Front face
            gl.glBegin(GL.GL_LINE_LOOP);
            gl.glVertex3fv(bufferArray,0);
            gl.glVertex3fv(bufferArray,12);
            gl.glVertex3fv(bufferArray,15);
            gl.glVertex3fv(bufferArray,9);
            gl.glEnd();
            // Back face
            gl.glBegin(GL.GL_LINE_LOOP);
            gl.glVertex3fv(bufferArray,3);
            gl.glVertex3fv(bufferArray,6);
            gl.glVertex3fv(bufferArray,18);
            gl.glVertex3fv(bufferArray,21);
            gl.glEnd();
        }
    } */

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
