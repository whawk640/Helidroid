package heli.org.helidroid;

import android.opengl.GLES20;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

/** This class represents our chopper and its capabilities
 *  Derive fromt this class if you want special features.
 * @author Daniel LaFuze
 * Copyright 2015
 * All Rights Reserved
 *
 */
public class StigChopper
{
    private static int mTriProgram = -1;
	private static int mLineProgram = -1;
	
    // These buffers are currently designed for triangles
    public FloatBuffer triVertexBuffer;
    public IntBuffer triDrawListBuffer;
    public FloatBuffer triUvBuffer;
    public FloatBuffer triColBuffer;

	// Line Buffers	
	public FloatBuffer lineVertexBuffer;
    public IntBuffer lineDrawListBuffer;
    public FloatBuffer lineColBuffer;
	
	// NOTE: May not need separate rotor functions
	public FloatBuffer rotorVertexBuffer;
	public IntBuffer rotorDrawListBuffer;
    public FloatBuffer rotorColBuffer;
	
	public float lineCoords[] = {
		// Cab to Tail Frame
		// Left Bottom
		-1.00f,  0.00f, 0.50f,
		-0.25f, -1.50f, 1.00f,
		// Right Bottom
		+1.00f, 0.00f, 0.50f,
		0.25f, -1.50f, 1.00f
		// Left Top
		-1.00f, 0.00f, 2.50f,
		-0.25f, -1.50f, 2.00f,
		// Right Top
		1.0f, 0.00f, 2.50f,
		0.25f, -1.50f, 2.00f
		// Skids (Coming soon)
	};
    public static int lineDrawOrder[] = {
		// Frame
		0,  1,  2,  3, // Left
		4,  5,  6,  7 // Right
		
	};
	
	public float triCoords[] = {
		// Large Chopper Cube
		// Left
		-1.00f, 2.00f, 2.50f,
		-1.00f, 2.00f, 0.50f,
		-1.00f, 0.00f, 0.50f,
		-1.00f, 0.00f, 2.50f,

		// Right
		1.00f, 0.00f, 2.50f,
		1.00f, 0.00f, 0.50f,
		1.00f, 2.00f, 0.50f,
		1.00f, 2.00f, 2.50f,

		// Top
		-1.00f, 2.00f, 2.50f,
		-1.00f, 0.00f, 2.50f,
		1.00f, 0.00f, 2.50f,
		1.00f, 2.00f, 2.50f,
		
		// Bottom
		1.00f, 2.00f, 0.50f,
		1.00f, 0.00f, 0.50f,
		-1.00f, 0.00f, 0.50f,
		-1.00f, 2.00f, 0.50f,
		
		// Front
		1.00f, 2.00f, 2.50f,
		1.00f, 2.00f, 0.50f,
		-1.00f, 2.00f, 0.50f,
		-1.00f, 2.00f, 2.50f,
		
		// Back
		-1.00f, 0.00f, 2.50f,
		-1.00f, 0.00f, 0.50f,
		1.00f, 0.00f, 0.50f,
		1.00f, 0.00f, 2.50f,
		
		// Tail Rotor Mount
		// Left
		-0.25f, -1.50f, 2.00f,
		-0.25f, -1.50f, 1.00f,
		-0.25f, -2.50f, 1.00f,
		-0.25f, -2.50f, 2.00f,

		// Right
		0.25f, -2.50f, 2.00f,
		0.25f, -2.50f, 1.00f,
		0.25f, -1.50f, 1.00f,
		0.25f, -1.50f, 2.00f,
		
		// Top
		-0.25f, -1.50f, 2.00f,
		-0.25f, -2.50f, 2.00f,
		0.25f, -2.50f, 2.00f,
		0.25f, -1.50f, 2.00f,

		// Bottom
		0.25f, -1.50f, 1.00f,
		0.25f, -2.50f, 1.00f,
		-0.25f, -2.50f, 1.00f,
		-0.25f, -1.50f, 1.00f,
		
		// Front
		0.25f, -1.50f, 2.00f,
		0.25f, -1.50f, 1.00f,
		-0.25f, -1.50f, 1.00f,
		-0.25f, -1.50f, 2.00f,

		// Back
		-0.25f, -2.50f, 2.00f,
		-0.25f, -2.50f, 1.00f,
		0.25f, -2.50f, 1.00f,
		0.25f, -2.50f, 2.00f,
		
		// Rotor Cone
		// Left
		-0.5f, 1.5f, 2.5f,
		-0.5f, 0.5f, 2.5f,
		0.0f, 1.0f, 3.0f,
		
		// Back
		-0.5f, 0.5f, 2.5f,
		0.5f, 0.5f, 2.5f,
		0.0f, 1.0f, 3.0f,
		
		// Right
		0.5f, 0.5f, 2.5f,
		0.5f, 1.5f, 2.5f,
		0.0f, 1.0f, 3.0f,
		
		// Front
		0.5f, 1.5f, 2.5f,
		-0.5f, 1.5f, 2.5f,
		0.0f, 1.0f, 3.0f
	};

    public static int triDrawOrder[] = {
		// Main Cube
		0,  1,  2,   0,  2,  3, // Top
		4,  5,  6,   4,  6,  7, // Bottom
		8,  9,  10,  8, 10, 11, // Back
		12, 13, 14, 12, 14, 15, // Front
		16, 17, 18, 16, 18, 19, // Left
		20, 21, 22, 20, 22, 23, // Right
		
		// Small Tail Cube
	    24, 25, 26, 24, 26, 27, // Top
	    28, 29, 30, 28, 30, 31, // Bottom
	    32, 33, 34, 32, 34, 35, // Back
	    36, 37, 38, 36, 38, 39, // Front
	    40, 41, 42, 40, 42, 43, // Left
	    44, 45, 46, 44, 46, 47, // Right
		
		// Rotor Cube
		48, 49, 50, 51, 52, 53,
		54, 55, 56, 57, 58, 59
    };
	
	private float uvs[] = {
		// Large Chopper Cube
		0.0f, 0.0f, 
		0.0f, 1.0f,
		1.0f, 1.0f,
		1.0f, 0.0f,
		0.0f, 0.0f, 
		0.0f, 1.0f,
		1.0f, 1.0f,
		1.0f, 0.0f,
		0.0f, 0.0f, 
		0.0f, 1.0f,
		1.0f, 1.0f,
		1.0f, 0.0f,
		0.0f, 0.0f, 
		0.0f, 1.0f,
		1.0f, 1.0f,
		1.0f, 0.0f,
		0.0f, 0.0f, 
		0.0f, 1.0f,
		1.0f, 1.0f,
		1.0f, 0.0f,
		0.0f, 0.0f, 
		0.0f, 1.0f,
		1.0f, 1.0f,
		1.0f, 0.0f,
		// Small Chopper Cube
		0.0f, 0.0f, 
		0.0f, 1.0f,
		1.0f, 1.0f,
		1.0f, 0.0f,
		0.0f, 0.0f, 
		0.0f, 1.0f,
		1.0f, 1.0f,
		1.0f, 0.0f,
		0.0f, 0.0f, 
		0.0f, 1.0f,
		1.0f, 1.0f,
		1.0f, 0.0f,
		0.0f, 0.0f, 
		0.0f, 1.0f,
		1.0f, 1.0f,
		1.0f, 0.0f,
		0.0f, 0.0f, 
		0.0f, 1.0f,
		1.0f, 1.0f,
		1.0f, 0.0f,
		0.0f, 0.0f, 
		0.0f, 1.0f,
		1.0f, 1.0f,
		1.0f, 0.0f,
		// Rotor Cone
		0.0f, 0.0f,
		0.0f, 1.0f,
		1.0f, 0.5f,
		0.0f, 1.0f,
		1.0f, 1.0f,
		0.5f, 0.0f,
		1.0f, 1.0f,
		1.0f, 0.0f,
		0.0f, 0.5f,
		1.0f, 0.0f,
		0.0f, 0.0f,
		0.5f, 1.0f
	};
	
	private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mColorHandle;
    private int mTextureCoordinateHandle;
    /** Size of the texture coordinate data in elements. */
    final int mTextureCoordinateDataSize = 2;
    private int mTextureUniformHandle;
	
    final int COORDS_PER_VERTEX = 3;
    final int COLORS_PER_VERTEX = 4;
    public final int triVertexCount = triCoords.length / COORDS_PER_VERTEX;
	public final int lineVertexCount = lineCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per coordinate
    private final int colorStride = COLORS_PER_VERTEX * 4; // 4 bytes per RGBA

    float color[] = { 1.0f, 1.0f, 1.0f, 1.0f };

    static protected boolean useVertexColor = false;

    static protected boolean useTextures = false;

    protected Point3D size;

    protected World world;

    protected int id;

    protected int inventory;

    protected static final double X_SIZE = 2.0;
    protected static final double Y_SIZE = 5.0;
    protected static final double Z_SIZE = 3.0;

    protected double cargoCapacity; // kg

    protected double fuelCapacity; // kg

    protected boolean landed;

    // This is the chopper's home base.  For now, it is defined
    // as the location at which it appeared in the world.
    protected Point3D homeBase;

    protected ArrayList<Point3D> targetWaypoints;

    // Complication -- homeBase isn't known yet -- we need chopperInfo constructed first
    public StigChopper(int chopperID, World theWorld)
    {
		System.out.println("Creating StigChopper ID: " + chopperID);
        id = chopperID;
        world = theWorld;
        cargoCapacity = ChopperAggregator.TOTAL_CAPACITY / 2.0;
        inventory = (int)Math.round(cargoCapacity / ChopperAggregator.ITEM_WEIGHT);
        fuelCapacity = ChopperAggregator.TOTAL_CAPACITY / 2.0;
        size = new Point3D(X_SIZE, Y_SIZE, Z_SIZE);
        landed = true;
        homeBase = null;
        targetWaypoints = new ArrayList<Point3D>();

        if (mTriProgram < 0) {
            String vertexCode = buildVertexCode(useVertexColor, useTextures);
			System.out.println("Creating Triangle Vertex Shader: " + vertexCode);
            int vertexShader = HeliGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                    //vertexShaderCode);
                    vertexCode);
            String fragmentCode = buildFragmentCode(useVertexColor, useTextures);
			System.out.println("Creating Triangle Fragment Shader: " + fragmentCode);
            int fragmentShader = HeliGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                    //fragmentShaderCode);
                    fragmentCode);
            if (vertexShader > 0 && fragmentShader > 0) {
                // create empty OpenGL ES Program
                mTriProgram = GLES20.glCreateProgram();

                // add the vertex shader to program
                GLES20.glAttachShader(mTriProgram, vertexShader);

                // add the fragment shader to program
                GLES20.glAttachShader(mTriProgram, fragmentShader);

                // creates OpenGL ES program executables
                GLES20.glLinkProgram(mTriProgram);
                System.out.println("StigChopper Shaders created, vtx: " + vertexShader + ", fragment: " +
                        fragmentShader + ", program ID: " + mTriProgram);
            } else {
                System.out.println("StigChopper Failed to load shader program -- vertex: " + vertexShader +
                        ", fragment: " + fragmentShader);
            }

            System.out.println("StigChopper " + id + " created -- fuel capacity: " + fuelCapacity);
        }
    }

    protected String buildVertexCode(boolean vertexColor, boolean enableTextures)
    {
        String vertexString =
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 vPosition;";
        if (vertexColor)
        {
            vertexString += "attribute vec4 vColor;";
        }
        if (enableTextures)
        {
            vertexString += "attribute vec2 a_texCoordinate;";
            vertexString += "varying vec2 v_texCoordinate;";
        }
        if (vertexColor)
        {
            vertexString += "varying vec4 fColor;";
        }
        vertexString += "void main() {";
        vertexString += "  gl_Position = uMVPMatrix * vPosition;";
        if (enableTextures)
        {
            vertexString += "  v_texCoordinate = a_texCoordinate;";
        }
        if (vertexColor)
        {
            vertexString += "  fColor = vColor;";
        }
        vertexString +=	"}";
        return vertexString;
    }

    protected String buildFragmentCode(boolean vertexColor, boolean enableTextures)
    {
        String fragmentString = "precision mediump float;";
        if (vertexColor == false)
        {
            fragmentString += "uniform vec4 vColor;";
        }
        if (enableTextures)
        {
            fragmentString += "uniform sampler2D u_texture;";
            fragmentString += "varying vec2 v_texCoordinate;";

        }
        if (vertexColor)
        {
            fragmentString += "varying vec4 fColor;";
        }
        fragmentString += "void main() {";
        if (enableTextures && vertexColor)
        {
            fragmentString += "  gl_FragColor = fColor * texture2D( u_texture, v_texCoordinate);";
        }
        else if (enableTextures)
        {
            fragmentString += "  gl_FragColor = vColor * texture2D( u_texture, v_texCoordinate);";
        }
        else if (vertexColor)
        {
            fragmentString += "  gl_FragColor = fColor;";
        }
        else // Single color, no texture
        {
            fragmentString += "  gl_FragColor = vColor;";
        }
        fragmentString += "}";
        return fragmentString;
    }

    /** This method sets the chopper's waypoints.  Eventually, we will deliver
     * packages by reaching a waypoint, and notifying the world of our intent
     * to drop off a package.  Land at a waypoint to enable delivery.
     * @param newWaypoints
     */
    public void setWaypoints(ArrayList<Point3D> newWaypoints)
    {
        targetWaypoints = newWaypoints;
    }

    public double fuelCapacity()
    {
        return fuelCapacity;
    }

    public int itemCount()
    {
        return inventory;
    }

    public int getId()
    {
        return id;
    }

    public boolean deliverItem()
    {
        boolean success = false;
        if (inventory > 0)
        {
            --inventory;
            success = true;
        }
        return success;
    }

    public Point3D getSize()
    {
        Point3D result = new Point3D(X_SIZE, Y_SIZE, Z_SIZE);
        return result;
    }

	public void createBuffers()
	{
		triVertexBuffer = BufferUtils.getFB(triCoords);
        //triColBuffer = getFB(cls);
        triUvBuffer = BufferUtils.getFB(uvs);
        triDrawListBuffer = BufferUtils.getIB(triDrawOrder);
		lineVertexBuffer = BufferUtils.getFB(lineCoords);
		lineDrawListBuffer = BufferUtils.getIB(lineDrawOrder);
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
    /* public void render(GLAutoDrawable drawable, double actHeading, double actTilt, double rotorPos, double tailRotorPos)
    {
        GL2 gl = drawable.getGL().getGL2();
        Point3D myPosition = world.gps(id);
        // Capture our first position reading as our home base
        if (homeBase == null)
        {
            homeBase = myPosition;
        }
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
        // Translate the center of chopper to the origin, so rotation doesn't move chopper
        gl.glPushMatrix();
        gl.glTranslated(centerPos.m_x, centerPos.m_y, centerPos.m_z);
        Point3D transformation = world.transformations(id);
        // rotate chopper by heading
        // NOTE: I accidentally drew them facing south instead of north, rotate 180
        gl.glRotated(transformation.m_x + 180.0, 0.0, 0.0, -1.0);
        // rotate chopper by tilt
        gl.glRotated(transformation.m_y, 1.0, 0.0, 0.0);
        gl.glTranslated(-centerPos.m_x,  -centerPos.m_y, -centerPos.m_z);
        ArrayList<Object3D> chopperObjects = makeChopperObjects(myPosition, mySize);
        for (Object3D chopperObject : chopperObjects)
        {
            chopperObject.setColor(1.0 - 0.25 * id,  0.0, 0.0 + 0.25 * id, 1.0);
            double objColor[] = chopperObject.getColor();
            float[] bufferArray = World.makeVertexArray(chopperObject.getPosition(), chopperObject.getSize());
            if (bufferArray != null)
            {
                gl.glColor4dv(objColor, 0);
                Texture fakeTexture = null;
                World.drawRectangles(gl,bufferArray, true, fakeTexture);
            }
            gl.glColor4dv(objColor, 0);
        }
        drawTopRotorPyramid(gl, centerPos.copy());
        //drawWindows(gl, centerPos.copy());
        drawRearFrame(gl, centerPos.copy());
        drawTopRotor(gl, centerPos.copy(), rotorPos);
        drawTailRotor(gl, centerPos.copy(), tailRotorPos, 0.3);
        drawTailRotor(gl, centerPos.copy(), tailRotorPos, -0.3);
        gl.glPopMatrix();
    } */

    /*
    private void drawWindows(GL10 gl, Point3D centerPos)
    {
        gl.glColor4d(0.20, 0.2, 0.2, 0.2);
        gl.glBegin(gl.GL_TRIANGLES);
        // Draw Left Top Angled Window
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 2.5, centerPos.m_z + 0.5);
        gl.glVertex3d(centerPos.m_x - 1.0, centerPos.m_y - 2.0, centerPos.m_z + 0.5);
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 2.0, centerPos.m_z + 1.0);
        // Draw Right Top Angled Window
        gl.glVertex3d(centerPos.m_x + 1.0, centerPos.m_y - 2.0, centerPos.m_z + 0.5);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 2.5, centerPos.m_z + 0.5);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 2.0, centerPos.m_z + 1.0);
        gl.glEnd();
        // Draw Front Top Straight Window
        gl.glBegin(gl.GL_QUADS);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 2.5, centerPos.m_z + 0.5);
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 2.5, centerPos.m_z + 0.5);
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 2.0, centerPos.m_z + 1.0);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 2.0, centerPos.m_z + 1.0);
        gl.glEnd();
        gl.glBegin(gl.GL_TRIANGLES);
        // Draw Left Bottom Angled Window
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 2.5, centerPos.m_z - 0.5);
        gl.glVertex3d(centerPos.m_x - 1.0, centerPos.m_y - 2.0, centerPos.m_z - 0.5);
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 2.0, centerPos.m_z - 1.0);
        // Draw Right Bottom Angled Window
        gl.glVertex3d(centerPos.m_x + 1.0, centerPos.m_y - 2.0, centerPos.m_z - 0.5);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 2.5, centerPos.m_z - 0.5);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 2.0, centerPos.m_z - 1.0);
        gl.glEnd();
        // Draw Front Bottom Straight Window
        gl.glBegin(gl.GL_QUADS);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 2.0, centerPos.m_z - 1.0);
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 2.0, centerPos.m_z - 1.0);
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 2.5, centerPos.m_z - 0.5);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 2.5, centerPos.m_z - 0.5);
        // Draw Left Straight Window
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 2.5, centerPos.m_z + 0.5);
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 2.5, centerPos.m_z - 0.5);
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 2.0, centerPos.m_z - 0.5);
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 2.0, centerPos.m_z + 0.5);
        // Draw Right Straight Window
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 2.0, centerPos.m_z + 0.5);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 2.0, centerPos.m_z - 0.5);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 2.5, centerPos.m_z - 0.5);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 2.5, centerPos.m_z + 0.5);
        gl.glEnd();
    }

    private void drawTopRotorPyramid(GL2 gl, Point3D centerPos)
    {
        gl.glBegin(gl.GL_TRIANGLE_STRIP);
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 1.75, centerPos.m_z + 1.0);
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 0.75, centerPos.m_z + 1.0);
        gl.glVertex3d(centerPos.m_x, centerPos.m_y - 1.25, centerPos.m_z + 1.5);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 0.75, centerPos.m_z + 1.0);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 1.75, centerPos.m_z + 1.0);
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 1.75, centerPos.m_z + 1.0);
        gl.glEnd();
        gl.glBegin(gl.GL_LINES);
        gl.glColor3d(0.25, 0.25, 0.25);
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 1.75, centerPos.m_z + 1.0);
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 0.75, centerPos.m_z + 1.0);
        gl.glVertex3d(centerPos.m_x, centerPos.m_y - 1.25, centerPos.m_z + 1.5);
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 0.75, centerPos.m_z + 1.0);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 0.75, centerPos.m_z + 1.0);
        gl.glVertex3d(centerPos.m_x, centerPos.m_y - 1.25, centerPos.m_z + 1.5);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 0.75, centerPos.m_z + 1.0);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 1.75, centerPos.m_z + 1.0);
        gl.glVertex3d(centerPos.m_x, centerPos.m_y - 1.25, centerPos.m_z + 1.5);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y - 1.75, centerPos.m_z + 1.0);
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y - 1.75, centerPos.m_z + 1.0);
        gl.glVertex3d(centerPos.m_x, centerPos.m_y - 1.25, centerPos.m_z + 1.5);
        gl.glEnd();
    }

    private void drawRearFrame(GL2 gl, Point3D centerPos)
    {
        gl.glBegin(gl.GL_LINES);
        gl.glColor3d(0.75, 0.75, 0.75);
        // Draw 4 lines to contain the frame
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y, centerPos.m_z + 0.5);
        gl.glVertex3d(centerPos.m_x - 0.25, centerPos.m_y + 1.5, centerPos.m_z + 0.25);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y, centerPos.m_z + 0.5);
        gl.glVertex3d(centerPos.m_x + 0.25, centerPos.m_y + 1.5, centerPos.m_z + 0.25);
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y, centerPos.m_z - 0.5);
        gl.glVertex3d(centerPos.m_x - 0.25, centerPos.m_y + 1.5, centerPos.m_z - 0.25);
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y, centerPos.m_z - 0.5);
        gl.glVertex3d(centerPos.m_x + 0.25, centerPos.m_y + 1.5, centerPos.m_z - 0.25);
        // Draw left X closest to body
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y, centerPos.m_z + 0.5);
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*0.33), centerPos.m_y + 0.5, centerPos.m_z - 0.5 + (0.25*0.33));
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y, centerPos.m_z - 0.5);
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*0.33), centerPos.m_y + 0.5, centerPos.m_z + 0.5 - (0.25*0.33));
        // Draw Right X closest to body
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y, centerPos.m_z + 0.5);
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*0.33), centerPos.m_y + 0.5, centerPos.m_z - 0.5 + (0.25*0.33));
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y, centerPos.m_z - 0.5);
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*0.33), centerPos.m_y + 0.5, centerPos.m_z + 0.5 - (0.25*0.33));
        // Draw Top X closest to body
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y, centerPos.m_z + 0.5);
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*0.33), centerPos.m_y + 0.5, centerPos.m_z + 0.5 - (0.25*0.33));
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y, centerPos.m_z + 0.5);
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*0.33), centerPos.m_y + 0.5, centerPos.m_z + 0.5 - (0.25*0.33));
        // Draw Bottom X closest to body
        gl.glVertex3d(centerPos.m_x - 0.5, centerPos.m_y, centerPos.m_z - 0.5);
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*0.33), centerPos.m_y + 0.5, centerPos.m_z - 0.5 + (0.25*0.33));
        gl.glVertex3d(centerPos.m_x + 0.5, centerPos.m_y, centerPos.m_z - 0.5);
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*0.33), centerPos.m_y + 0.5, centerPos.m_z - 0.5 + (0.25*0.33));
        // Draw second left X
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*0.33), centerPos.m_y + 0.5, centerPos.m_z + 0.5 - (0.25*0.66));
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*0.66), centerPos.m_y + 1.0, centerPos.m_z - 0.5 + (0.25*0.33));
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*0.33), centerPos.m_y + 0.5, centerPos.m_z - 0.5 + (0.25*0.66));
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*0.66), centerPos.m_y + 1.0, centerPos.m_z + 0.5 - (0.25*0.33));
        // Draw second Right X
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*0.33), centerPos.m_y + 0.5, centerPos.m_z + 0.5 - (0.25*0.66));
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*0.66), centerPos.m_y + 1.0, centerPos.m_z - 0.5 + (0.25*0.33));
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*0.33), centerPos.m_y + 0.5, centerPos.m_z - 0.5 + (0.25*0.66));
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*0.66), centerPos.m_y + 1.0, centerPos.m_z + 0.5 - (0.25*0.33));
        // Draw second Top X
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*0.66), centerPos.m_y + 0.5, centerPos.m_z + 0.5 - (0.25*0.33));
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*0.33), centerPos.m_y + 1.0, centerPos.m_z + 0.5 - (0.25*0.66));
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*0.66), centerPos.m_y + 0.5, centerPos.m_z + 0.5 - (0.25*0.33));
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*0.33), centerPos.m_y + 1.0, centerPos.m_z + 0.5 - (0.25*0.66));
        // Draw second Bottom X
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*1.0), centerPos.m_y + 0.5, centerPos.m_z - 0.5 + (0.25*0.66));
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*0.66), centerPos.m_y + 1.0, centerPos.m_z - 0.5 + (0.25*1.0));
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*1.0), centerPos.m_y + 0.5, centerPos.m_z - 0.5 + (0.25*0.66));
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*0.66), centerPos.m_y + 1.0, centerPos.m_z - 0.5 + (0.25*1.0));
        // Draw third left X
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*0.66), centerPos.m_y + 1.0, centerPos.m_z + 0.5 - (0.25*1.0));
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*1.0), centerPos.m_y + 1.5, centerPos.m_z - 0.5 + (0.25*0.66));
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*0.66), centerPos.m_y + 1.0, centerPos.m_z - 0.5 + (0.25*1.0));
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*1.0), centerPos.m_y + 1.5, centerPos.m_z + 0.5 - (0.25*0.66));
        // Draw third Right X
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*0.66), centerPos.m_y + 1.0, centerPos.m_z + 0.5 - (0.25*1.0));
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*1.0), centerPos.m_y + 1.5, centerPos.m_z - 0.5 + (0.25*0.66));
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*0.66), centerPos.m_y + 1.0, centerPos.m_z - 0.5 + (0.25*1.0));
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*1.0), centerPos.m_y + 1.5, centerPos.m_z + 0.5 - (0.25*0.66));
        // Draw third Top X
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*1.0), centerPos.m_y + 1.0, centerPos.m_z + 0.5 - (0.25*0.66));
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*0.66), centerPos.m_y + 1.5, centerPos.m_z + 0.5 - (0.25*1.0));
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*1.0), centerPos.m_y + 1.0, centerPos.m_z + 0.5 - (0.25*0.66));
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*0.66), centerPos.m_y + 1.5, centerPos.m_z + 0.5 - (0.25*1.0));
        // Draw third Bottom X
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*1.0), centerPos.m_y + 1.0, centerPos.m_z - 0.5 + (0.25*0.66));
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*0.66), centerPos.m_y + 1.5, centerPos.m_z - 0.5 + (0.25*1.0));
        gl.glVertex3d(centerPos.m_x + 0.5 - (0.25*1.0), centerPos.m_y + 1.0, centerPos.m_z - 0.5 + (0.25*0.66));
        gl.glVertex3d(centerPos.m_x - 0.5 + (0.25*0.66), centerPos.m_y + 1.5, centerPos.m_z - 0.5 + (0.25*1.0));
        gl.glEnd();
    }

    private void drawTopRotor(GL2 gl, Point3D centerPos, double rotorPos)
    {
        // Move center to center of top rotor
        centerPos.m_y -= 1.25;
        centerPos.m_z += 1.50;
        gl.glPushMatrix();
        gl.glTranslated(centerPos.m_x, centerPos.m_y, centerPos.m_z);
        gl.glRotated(rotorPos, 0.0, 0.0, 1.0);
        gl.glTranslated(-centerPos.m_x, -centerPos.m_y, -centerPos.m_z);
        // Draw main rotor
        gl.glBegin(gl.GL_LINES);
        gl.glColor3d(1.0, 1.0, 0.00);
        // All 3 rotor blades start in the center
        gl.glVertex3d(centerPos.m_x, centerPos.m_y, centerPos.m_z);
        gl.glVertex3d(centerPos.m_x, centerPos.m_y + 1.5, centerPos.m_z);
        gl.glVertex3d(centerPos.m_x, centerPos.m_y, centerPos.m_z);
        gl.glVertex3d(centerPos.m_x - 1.3, centerPos.m_y - 1.0, centerPos.m_z);
        gl.glVertex3d(centerPos.m_x, centerPos.m_y, centerPos.m_z);
        gl.glVertex3d(centerPos.m_x + 1.3, centerPos.m_y - 1.0, centerPos.m_z);
        gl.glEnd();
        gl.glPopMatrix();
    }

    private void drawTailRotor(GL2 gl, Point3D centerPos, double rotorPos, double xOffset)
    {
        // Move center to center of top rotor
        centerPos.m_y += 2.00;
        centerPos.m_x += xOffset;
        gl.glPushMatrix();
        gl.glTranslated(centerPos.m_x, centerPos.m_y, centerPos.m_z);
        gl.glRotated(rotorPos, 1.0, 0.0, 0.0);
        gl.glTranslated(-centerPos.m_x, -centerPos.m_y, -centerPos.m_z);
        // Draw tail rotor
        gl.glBegin(gl.GL_LINES);
        gl.glColor3d(1.0, 1.0, 0.0);
        // Both rotor blades start in the center
        gl.glVertex3d(centerPos.m_x, centerPos.m_y, centerPos.m_z);
        gl.glVertex3d(centerPos.m_x, centerPos.m_y, centerPos.m_z + 0.5);
        gl.glVertex3d(centerPos.m_x, centerPos.m_y, centerPos.m_z);
        gl.glVertex3d(centerPos.m_x, centerPos.m_y, centerPos.m_z - 0.5);
        gl.glEnd();
        gl.glPopMatrix();
    } */
	
	public void drawTriangles(int textDataHandle, float[] mvpMatrix)
	{
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mTriProgram);
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR)
        {
            System.out.println("StigChopper: Use Tri Program Error: " + error);
        }

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mTriProgram, "vPosition");
        if (mPositionHandle < 0)
        {
            System.out.println("StigChopper: Failed to get mPositionHandle");
        }

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mTriProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Enable a handle to the cube vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the cube coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
									 GLES20.GL_FLOAT, false,
									 vertexStride, triVertexBuffer);

        // get handle to vertex shader's vColor member
        if (useVertexColor)
        {
            mColorHandle = GLES20.glGetAttribLocation(mTriProgram, "vColor");
            if (mColorHandle < 0)
            {
                System.out.println("StigChopper: Failed to get vColor");
            }
            GLES20.glEnableVertexAttribArray(mColorHandle);

            GLES20.glVertexAttribPointer(mColorHandle, COLORS_PER_VERTEX,
										 GLES20.GL_FLOAT, false, colorStride, triColBuffer);
        }
        else
        {
            mColorHandle = GLES20.glGetUniformLocation(mTriProgram, "vColor");
			GLES20.glUniform4f(mColorHandle,color[0],color[1],color[2],color[3]);
        }

        if (StigChopper.useTextures)
        {
            mTextureUniformHandle = GLES20.glGetUniformLocation(mTriProgram, "u_texture");
            if (mTextureUniformHandle < 0)
            {
                System.out.println("StigChopper: Failed to get texture uniform");
            }

            mTextureCoordinateHandle  = GLES20.glGetAttribLocation(mTriProgram, "a_texCoordinate");
            if (mTextureCoordinateHandle < 0)
            {
                System.out.println("StigChopper: Failed to get texture coordinates.");
            }
            GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

            // Prepare the uv coordinate data.
            GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2,
										 GLES20.GL_FLOAT, false, 8, triUvBuffer);

            // Set the active texture unit to texture unit 0.
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

            // Bind the texture to this unit.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textDataHandle);

			// Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
			GLES20.glUniform1i(mTextureUniformHandle, 0);
        }

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, triDrawListBuffer.capacity(),
							  GLES20.GL_UNSIGNED_INT, triDrawListBuffer);
        int drawError = GLES20.glGetError();
        if (drawError != GLES20.GL_NO_ERROR)
        {
            System.out.println("StigChopper:Triangle Draw Elements Error: " + drawError + ", color: " + useVertexColor + ", text: " + useTextures);
        }

        if (StigChopper.useTextures)
        {
            // Disable texture array
            GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        if (useVertexColor)
        {
            GLES20.glDisableVertexAttribArray(mColorHandle);
        }
		System.out.println("StigChopper: Done drawing triangles " + triDrawListBuffer.capacity() + " vertices...");
	}

	public void drawLines(float[] mvpMatrix)
	{
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mTriProgram);
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR)
        {
            System.out.println("StigChopper: Use Line Program Error: " + error);
        }

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mTriProgram, "vPosition");
        if (mPositionHandle < 0)
        {
            System.out.println("StigChopper -- lines: Failed to get mPositionHandle");
        }

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mTriProgram, "uMVPMatrix");

        // Enable a handle to the cube vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the cube coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
									 GLES20.GL_FLOAT, false,
									 vertexStride, lineVertexBuffer);

        // get handle to vertex shader's vColor member
        if (useVertexColor)
        {
            mColorHandle = GLES20.glGetAttribLocation(mTriProgram, "vColor");
            if (mColorHandle < 0)
            {
                System.out.println("StigChopper: Failed to get vColor");
            }
            GLES20.glEnableVertexAttribArray(mColorHandle);

            GLES20.glVertexAttribPointer(mColorHandle, COLORS_PER_VERTEX,
										 GLES20.GL_FLOAT, false, colorStride, triColBuffer);
        }
        else
        {
            mColorHandle = GLES20.glGetUniformLocation(mTriProgram, "vColor");
			GLES20.glUniform4f(mColorHandle,color[0],color[1]/2.0f,color[2],color[3]);
        }

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_LINES, lineDrawListBuffer.capacity(),
							  GLES20.GL_UNSIGNED_INT, lineDrawListBuffer);
        int drawError = GLES20.glGetError();
        if (drawError != GLES20.GL_NO_ERROR)
        {
            System.out.println("StigChopper: Line Draw Elements Error: " + drawError + ", color: " + useVertexColor + ", text: " + useTextures);
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        if (useVertexColor)
        {
            GLES20.glDisableVertexAttribArray(mColorHandle);
        }
		System.out.println("StigChopper: Done drawing lines " + lineDrawListBuffer.capacity() + " vertices...");
	}
	
    public void draw(int textDataHandle, float[] mvpMatrix) { // pass in the calculated transformation matrix
		// Consider cloning matrices if any transformations are needed
		//float[] triMatrix = mvpMatrix.clone();
		//float[] lineMatrix = mvpMatrix.clone();
		drawTriangles(textDataHandle, mvpMatrix);
		drawLines(mvpMatrix);
	}
	
}
