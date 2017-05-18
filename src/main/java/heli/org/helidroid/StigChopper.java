package heli.org.helidroid;

import android.opengl.*;
import java.nio.*;
import java.util.*;

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
	
	public FloatBuffer mainRotorVertexBuffer;
	public IntBuffer mainRotorDrawListBuffer;
    public FloatBuffer mainRotorColBuffer;
	
	public FloatBuffer tailRotorVertexBuffer;
	public IntBuffer tailRotorDrawListBuffer;
    public FloatBuffer tailRotorColBuffer;

	public float tailRotorCoords[] = {
		// Left Rotor
		-0.30f, -2.0f, 2.5f,
		-0.30f, -2.0f, 0.5f,
		-0.30f, -1.0f, 1.5f,
		-0.30f, -3.0f, 1.5f,
		// Right Rotor
		0.30f, -2.0f, 2.5f,
		0.30f, -2.0f, 0.5f,
		0.30f, -1.0f, 1.5f,
		0.30f, -3.0f, 1.5f,
		
	};
	
	public int tailRotorDrawOrder[] = {
		0,1,2,3,4,5,6,7
	};
	
	public float mainRotorCoords[] = {
		-1.50f, 1.00f, 3.00f, // 0
		1.50f, 1.00f, 3.00f, // 1
		0.00f, 2.50f, 3.00f, // 2
		0.00f, -0.50f, 3.00f // 3
	};
	
	public int mainRotorDrawOrder[] = {
		0, 1, 2, 3
	};
	
	public float lineCoords[] = {
		// Cab to Tail Frame
		// Left Bottom
		-1.00f,  0.00f, 0.50f, // 0
		-0.25f, -1.50f, 1.00f, // 1
		// Right Bottom
		+1.00f, 0.00f, 0.50f, // 2
		0.25f, -1.50f, 1.00f, // 3
		// Left Top
		-1.00f, 0.00f, 2.50f,  // 4
		-0.25f, -1.50f, 2.00f, // 5
		// Right Top
		1.0f, 0.00f, 2.50f,   // 6
		0.25f, -1.50f, 2.00f, // 7
		// Skids
		// Left
		// Front bar
		-1.00f, 1.50f, 0.50f, // 8
		-1.00f, 1.50f, 0.00f, // 9
		// Back Bar
		-1.00f, 0.50f, 0.50f, // 10
		-1.00f, 0.50f, 0.00f, // 11
		// Bottom
		-1.00f, 2.00f, 0.00f, // 12
		-1.00f, 0.00f, 0.00f,  // 13
		// Front Lip
		-1.00f, 2.50f, 0.50f, // 14
		// Back Lip
		-1.00f, -0.50f, 0.50f, // 15
		// Right
		// Front bar
		1.00f, 1.50f, 0.50f, // 16
		1.00f, 1.50f, 0.00f, // 17
		// Back Bar
		1.00f, 0.50f, 0.50f, // 18
		1.00f, 0.50f, 0.00f, // 19
		// Bottom
		1.00f, 2.00f, 0.00f, // 20
		1.00f, 0.00f, 0.00f,  // 21
		// Front Lip
		1.00f, 2.50f, 0.50f, // 22
		// Back Lip
		1.00f, -0.50f, 0.50f, // 23
		// Reinforcing Bar midpoints (for X's)
		-0.625f, -0.75f, 0.75f, // 24 Down Left
		0.625f, -0.75f, 0.75f, // 25 Down right
		-0.625f, -0.75f, 2.25f, // 26 Up Left
		0.625f, -0.75f, 2.25f  // 27 Up Right
	};
	
    public static int lineDrawOrder[] = {
		 // Frame
		 0,  1,  2,  3, // Left
		 4,  5,  6,  7, // Right
		 // Left Skid
		 8,  9, 10, 11, // Down Bars
		12, 13, // Bottom Bar
		// Lips use ends of bottom bar
		12, 14, 13, 15,
	     // Left Skid
	    16, 17, 18, 19, // Down Bars
	    20, 21, // Bottom Bar
	    // Lips use ends of bottom bar
	    20, 22, 21, 23,
		0, 26, 1, 26,
		2, 24, 3, 24,
		0, 25, 1, 25,
		2, 27, 3, 27,
		4, 27, 5, 27,
		6, 26, 7, 26,
		4, 25, 5, 25,
		6, 24, 7, 24
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
	private float lineWidth = 3.0f;
	
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

    public void setColor(float red, float green, float blue, float alpha)
    {
        color[0] = red;
        color[1] = green;
        color[2] = blue;
        color[3] = alpha;
    }

    public void setColor(Point3D rgb, double alpha)
    {
        color[3] = (float)alpha;
        color[0] = (float)rgb.m_x;
        color[1] = (float)rgb.m_y;
        color[2] = (float)rgb.m_z;
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
		mainRotorVertexBuffer = BufferUtils.getFB(mainRotorCoords);
		mainRotorDrawListBuffer = BufferUtils.getIB(mainRotorDrawOrder);
		tailRotorVertexBuffer = BufferUtils.getFB(tailRotorCoords);
		tailRotorDrawListBuffer = BufferUtils.getIB(tailRotorDrawOrder);
	}
	
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
			GLES20.glUniform4f(mColorHandle,color[0],color[1],color[2],color[3]);
        }

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

		GLES20.glLineWidth(lineWidth);
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
	}

	public void drawMainRotor(float[] mvpMatrix)
	{
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mTriProgram);
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR)
        {
            System.out.println("StigChopper -- rotors: Use Program Error: " + error);
        }

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mTriProgram, "vPosition");
        if (mPositionHandle < 0)
        {
            System.out.println("StigChopper -- rotors: Failed to get mPositionHandle");
        }

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mTriProgram, "uMVPMatrix");

        // Enable a handle to the cube vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the cube coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
									 GLES20.GL_FLOAT, false,
									 vertexStride, mainRotorVertexBuffer);

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
			GLES20.glUniform4f(mColorHandle,1.0f,1.0f,0.0f,0.7f);
        }

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

		GLES20.glLineWidth(lineWidth);
        GLES20.glDrawElements(GLES20.GL_LINES, mainRotorDrawListBuffer.capacity(),
							  GLES20.GL_UNSIGNED_INT, mainRotorDrawListBuffer);
        int drawError = GLES20.glGetError();
        if (drawError != GLES20.GL_NO_ERROR)
        {
            System.out.println("StigChopper: Rotor Draw Elements Error: " + drawError + ", color: " + useVertexColor + ", text: " + useTextures);
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        if (useVertexColor)
        {
            GLES20.glDisableVertexAttribArray(mColorHandle);
        }
	}
	
	public void drawTailRotor(float[] mvpMatrix)
	{
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mTriProgram);
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR)
        {
            System.out.println("StigChopper -- rotors: Use Program Error: " + error);
        }

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mTriProgram, "vPosition");
        if (mPositionHandle < 0)
        {
            System.out.println("StigChopper -- rotors: Failed to get mPositionHandle");
        }

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mTriProgram, "uMVPMatrix");

        // Enable a handle to the cube vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the cube coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
									 GLES20.GL_FLOAT, false,
									 vertexStride, tailRotorVertexBuffer);

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
			GLES20.glUniform4f(mColorHandle,1.0f,1.0f,0.0f,0.6f);
        }

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

		GLES20.glLineWidth(lineWidth);
        GLES20.glDrawElements(GLES20.GL_LINES, tailRotorDrawListBuffer.capacity(),
							  GLES20.GL_UNSIGNED_INT, tailRotorDrawListBuffer);
        int drawError = GLES20.glGetError();
        if (drawError != GLES20.GL_NO_ERROR)
        {
            System.out.println("StigChopper: Rotor Draw Elements Error: " + drawError + ", color: " + useVertexColor + ", text: " + useTextures);
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        if (useVertexColor)
        {
            GLES20.glDisableVertexAttribArray(mColorHandle);
        }
	}

    public void draw(int textDataHandle, float[] myMatrix) { // pass in the calculated transformation matrix
		ChopperInfo myInfo = world.getChopInfo(id);
		Point3D myPos = myInfo.getPosition();
		double headingDeg = myInfo.getHeading();
		double tiltDeg = myInfo.getTilt();
		double mainRotorDeg = myInfo.getMainRotorPosition();
		double tailRotorDeg = myInfo.getTailRotorPosition();
		float[] transMatrix = new float[16];
		Matrix.setIdentityM(transMatrix,0);
		Matrix.translateM(transMatrix,0,(float)myPos.m_x, (float)myPos.m_y, (float)myPos.m_z);
		Matrix.rotateM(transMatrix, 0, (float)headingDeg, 0.0f, 0.0f, -1.0f);
		Matrix.rotateM(transMatrix, 0, (float)tiltDeg, 1.0f, 0.0f, 1.0f);
		// Save a copies of myMatrix for rotors before messing with it
		float[] mainRotorMatrix = myMatrix.clone();
		float[] tailRotorMatrix = myMatrix.clone();
		Matrix.multiplyMM(myMatrix,0,myMatrix,0,transMatrix,0);
		drawTriangles(textDataHandle, myMatrix);
		drawLines(myMatrix);
		float[] mainRotorTransMatrix = new float[16];
		Matrix.setIdentityM(mainRotorTransMatrix,0);
		// OK, first, move the main rotor with the chopper
		Matrix.translateM(mainRotorTransMatrix,0,(float)myPos.m_x, (float)myPos.m_y,(float)myPos.m_z);
		Matrix.rotateM(mainRotorTransMatrix, 0, (float)headingDeg, 0.0f, 0.0f, -1.0f);
		Matrix.rotateM(mainRotorTransMatrix, 0, (float)tiltDeg, 1.0f, 0.0f, 1.0f);
		// Now move the origin to the position of the center of the rotor
		Matrix.translateM(mainRotorTransMatrix,0,0.0f, 1.0f,0.0f);
		Matrix.rotateM(mainRotorTransMatrix,0,(float)mainRotorDeg, 0.0f, 0.0f, -1.0f);
		Matrix.translateM(mainRotorTransMatrix,0,0.0f, -1.0f,0.0f);
		Matrix.multiplyMM(mainRotorMatrix,0,mainRotorMatrix,0,mainRotorTransMatrix,0);
		drawMainRotor(mainRotorMatrix);
		// Move center to center of top rotor
		float[] tailRotorTransMatrix = new float[16];
		Matrix.setIdentityM(tailRotorTransMatrix,0);
		// OK, first, move the tail rotor with the chopper
		Matrix.translateM(tailRotorTransMatrix,0,(float)myPos.m_x, (float)myPos.m_y,(float)myPos.m_z);
		Matrix.rotateM(tailRotorTransMatrix, 0, (float)headingDeg, 0.0f, 0.0f, -1.0f);
		Matrix.rotateM(tailRotorTransMatrix, 0, (float)tiltDeg, 1.0f, 0.0f, 1.0f);
		// Now move the origin to the position of the center of the tail rotor
		Matrix.translateM(tailRotorTransMatrix,0,0.0f, -2.0f,1.5f);
		Matrix.rotateM(tailRotorTransMatrix,0,(float)tailRotorDeg, 1.0f, 0.0f, 0.0f);
		Matrix.translateM(tailRotorTransMatrix,0,0.0f, 2.0f,-1.5f);
		Matrix.multiplyMM(tailRotorMatrix,0,tailRotorMatrix,0,tailRotorTransMatrix,0);
		drawTailRotor(tailRotorMatrix);
	}
	
}
