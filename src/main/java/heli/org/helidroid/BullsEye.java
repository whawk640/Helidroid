package heli.org.helidroid;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/** For now, an object3D is a cube-like object, though each individual dimension
 * can be varied by passing in size.
 */
public class BullsEye extends Base3D {
    private Point3D position;

    static float offColor[] = { 1.0f, 1.0f, 1.0f, 1.0f };

	private Point3D myLoc;
	private float myColor[];
    static public FloatBuffer vertexBuffer;
    static public IntBuffer drawListBuffer;
    static public FloatBuffer colBuffer;

    private Boolean overrideColors = null;

    boolean testColorOverride()
    {
        boolean useColors = useVertexColor;
        if (overrideColors != null)
        {
            useColors = overrideColors;
        }
        return useColors;
    }

	private final int NUM_RINGS = 5;
	private final double RING_RADIUS = 2.0;
	
	// Center, plus 25 around the circle (Including starting point twice)
	private final int VERTICES_PER_RING = 26;
	
	public static float circleCoords[] = null;

	public static float circleColors[] = null;
	
    public static int drawOrder[] = null;
	
	public BullsEye(Point3D loc, float[] color)
	{
		super();
		myLoc = loc.copy();
		myColor = color.clone();
		overrideColors = new Boolean(true);
		int drawListEntries = VERTICES_PER_RING * NUM_RINGS;
		int vertexEntries = COORDS_PER_VERTEX * drawListEntries;
		int colorEntries = COLORS_PER_VERTEX * drawListEntries;
		System.out.println("DrawList: " + drawListEntries + ", vertices: " + vertexEntries + ", colors: " + colorEntries);
		circleCoords = new float[vertexEntries];
		circleColors = new float[colorEntries];
		drawOrder = new int[drawListEntries];
		
		// Fill in all of our arrays
		for (int i = 0; i < NUM_RINGS; ++i)
		{
			float[] drawColor = null;
			if ((i % 2) == 1)
			{
				drawColor = offColor.clone();
			}
			else
			{
				drawColor = myColor.clone();
			}
			double ringRadius = RING_RADIUS * (i + 1);
			int vertexOffset = VERTICES_PER_RING * i;
			double deltaAngle = 360.0 / (VERTICES_PER_RING - 2);
			double curAngle = 0.0;
			for (int j = 0; j < VERTICES_PER_RING; ++j, ++vertexOffset)
			{
				if (vertexOffset < drawListEntries)
				{
					drawOrder[vertexOffset] = vertexOffset;
				}
				else
				{
					System.out.println("Bogus, i: " + i + ", j: " + j + ", vertexOffset: " + vertexOffset);
					break;
				}
				circleColors[vertexOffset * COLORS_PER_VERTEX] = drawColor[0];
				circleColors[vertexOffset * COLORS_PER_VERTEX + 1] = drawColor[1];
				circleColors[vertexOffset * COLORS_PER_VERTEX + 2] = drawColor[2];
				circleColors[vertexOffset * COLORS_PER_VERTEX + 3] = drawColor[3];
				if (j == 0) // Center
				{
					circleCoords[vertexOffset * COORDS_PER_VERTEX] = (float)myLoc.m_x;
					circleCoords[vertexOffset * COORDS_PER_VERTEX + 1] = (float)myLoc.m_y;
					circleCoords[vertexOffset * COORDS_PER_VERTEX + 2] = (float)myLoc.m_z + 0.5f;
				}
				else
				{
					circleCoords[vertexOffset * COORDS_PER_VERTEX] = (float)myLoc.m_x + (float)(ringRadius * Math.sin(curAngle));
					circleCoords[vertexOffset * COORDS_PER_VERTEX + 1] = (float)myLoc.m_y + (float)(ringRadius * Math.cos(curAngle));
					circleCoords[vertexOffset * COORDS_PER_VERTEX + 2] = (float)myLoc.m_z + 0.5f;
					curAngle += deltaAngle;
				}
			}
		}
		vertexBuffer = BufferUtils.getFB(circleCoords);
		colBuffer = BufferUtils.getFB(circleColors);
		drawListBuffer = BufferUtils.getIB(drawOrder);
	}
	
	public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR)
        {
            System.out.println("Use Program Error: " + error);
        }

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        if (mPositionHandle < 0)
        {
            System.out.println("Failed to get mPositionHandle");
        }

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Enable a handle to the cube vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the cube coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
									 GLES20.GL_FLOAT, false,
									 vertexStride, vertexBuffer);

        // get handle to vertex shader's vColor member
        if (useVertexColor)
        {
            mColorHandle = GLES20.glGetAttribLocation(mProgram, "vColor");
            if (mColorHandle < 0)
            {
                System.out.println("Failed to get vColor");
            }
            GLES20.glEnableVertexAttribArray(mColorHandle);

            GLES20.glVertexAttribPointer(mColorHandle, COLORS_PER_VERTEX,
										 GLES20.GL_FLOAT, false, colorStride, colBuffer);
        }
        else
        {
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
			GLES20.glUniform4f(mColorHandle,myColor[0],myColor[1],myColor[2],myColor[3]);
        }

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

		int circleDrawListCount = drawListBuffer.capacity() / NUM_RINGS;	
		for (int i = 0; i< NUM_RINGS; ++i)
		{
			int circleDrawListOffset = i * circleDrawListCount;
			GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, drawListBuffer.capacity() / NUM_RINGS,
								  GLES20.GL_UNSIGNED_INT, circleDrawListOffset);
								  
			int drawError = GLES20.glGetError();
			if (drawError != GLES20.GL_NO_ERROR)
			{
				System.out.println("Draw Elements Error: " + drawError);
			}
		}

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        if (useVertexColor)
        {
            GLES20.glDisableVertexAttribArray(mColorHandle);
        }
    }
	
}
