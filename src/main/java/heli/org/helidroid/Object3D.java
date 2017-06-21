package heli.org.helidroid;

import android.content.*;
import android.graphics.*;
import android.opengl.*;
import java.io.*;
import java.nio.*;
import javax.microedition.khronos.opengles.*;

/** For now, an object3D is a cube-like object, though each individual dimension
 * can be varied by passing in size.
 */
public class Object3D extends Base3D {
    private Point3D position;
    private Point3D size;

    static float color[] = { 1.0f, 1.0f, 1.0f, 1.0f };

    static public FloatBuffer vertexBuffer;
    static public IntBuffer drawListBuffer;
	static public IntBuffer lineDrawListBuffer;
    static public FloatBuffer uvBuffer;
    static public FloatBuffer colBuffer;

    // TODO: Add textures for each side
    // private int[] whichTexture = {0, 0, 0, 0, 0, 0};
    static private int[] mTextureDataHandle;
    static private final int WHICH_TEXTURE = 0;

    // number of coordinates per vertex in this array

    public static float cubeCoords[] = {
            // Top
            -0.49f, 0.49f, 0.49f,  // left back
            -0.49f, -0.49f, 0.49f,  // left front
            0.49f, -0.49f, 0.49f,   // right front
            0.49f, 0.49f, 0.49f,   // right back

            // Bottom
            0.49f, 0.49f, -0.49f,  // left back
            0.49f, -0.49f, -0.49f,  // left front
            -0.49f, -0.49f, -0.49f,   // right front
            -0.49f, 0.49f, -0.49f,   // right back

            // Back
            -0.49f,  0.49f, -0.49f,  // left back
            -0.49f,  0.49f, 0.49f,  // left front
            0.49f,  0.49f, 0.49f,   // right front
            0.49f,  0.49f, -0.49f,   // right back

            // Front
            -0.49f,  -0.49f, 0.49f,  // left back
            -0.49f, -0.49f, -0.49f,  // left front
            0.49f, -0.49f, -0.49f,   // right front
            0.49f,  -0.49f, 0.49f,   // right back

            // Left
            -0.49f, 0.49f, -0.49f,  // left back
            -0.49f, -0.49f, -0.49f,  // left front
            -0.49f, -0.49f, 0.49f,   // right front
            -0.49f,  0.49f, 0.49f,   // right back

            // Right
            0.49f,  0.49f, 0.49f,  // left back
            0.49f, -0.49f, 0.49f,  // left front
            0.49f, -0.49f, -0.49f,   // right front
            0.49f,  0.49f, -0.49f    // right back
    };

    public static int drawOrder[] = {
            0,  1,  2,   0,  2,  3, // Top
            4,  5,  6,   4,  6,  7, // Bottom
            8,  9,  10,  8, 10, 11, // Back
            12, 13, 14, 12, 14, 15, // Front
            16, 17, 18, 16, 18, 19, // Left
            20, 21, 22, 20, 22, 23  // Right
    };

	public static int lineDrawOrder[] = {
		0,  1,   1,  2,  2,  3,  3,  0, // Top
		4,  5,   5,  6,  6,  7,  7,  4, // Bottom
		8,  9,   9, 10, 10, 11, 11,  8, // Back
		12, 13, 13, 14, 14, 15, 15, 12, // Front
		16, 17, 17, 18, 18, 19, 19, 16, // Left
		20, 21, 21, 22, 22, 23, 23, 20  // Right
    };
	
    public static final float uvs[] = {
            0.0f, 0.0f, // First Face
            0.0f, 0.1f,
            0.1f, 0.1f,
            0.1f, 0.0f,
            0.0f, 0.0f, // Second Face
            0.0f, 0.1f,
            0.1f, 0.1f,
            0.1f, 0.0f,
            0.0f, 0.0f, // Third Face
            0.0f, 0.1f,
            0.1f, 0.1f,
            0.1f, 0.0f,
            0.0f, 0.0f, // Fourth Face
            0.0f, 0.1f,
            0.1f, 0.1f,
            0.1f, 0.0f,
            0.0f, 0.0f, // Fifth Face
            0.0f, 0.1f,
            0.1f, 0.1f,
            0.1f, 0.0f,
            0.0f, 0.0f, // Sixth Face
            0.0f, 0.1f,
            0.1f, 0.1f,
            0.1f, 0.0f
    };

	private static final int NUM_TEXTURES = 2;
    static final int COORDS_PER_VERTEX = 3;
    static final int COLORS_PER_VERTEX = 4;
    public static final int vertexCount = cubeCoords.length / COORDS_PER_VERTEX;
    static private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per coordinate
    static private final int colorStride = COLORS_PER_VERTEX * 4; // 4 bytes per RGBA
    static private final int NUM_FRAMES = 10;
    static private final int FRAME_DELAY = 0;
    static int curFrame = 0;
    static int frameDelay = 0;

    private Boolean overrideTextures = null;

	public double getHeight()
	{
		return position.m_z + size.m_z/2.0;
	}
	
    static public void initImage(final Context context, GL10 gl, int surfaceId)
    {
        mTextureDataHandle = new int[NUM_TEXTURES];
        gl.glGenTextures(NUM_TEXTURES, mTextureDataHandle, 0);
        if (mTextureDataHandle[0] != 0) {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureDataHandle[0]);
            gl.glTexParameterf(
				GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_NEAREST);
            gl.glTexParameterf(
				GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR);
            gl.glTexParameterf(
				GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_WRAP_S,
				GL10.GL_CLAMP_TO_EDGE);
            gl.glTexParameterf(
				GL10.GL_TEXTURE_2D,
				GL10.GL_TEXTURE_WRAP_T,
				GL10.GL_CLAMP_TO_EDGE);
            gl.glTexEnvf(
				GL10.GL_TEXTURE_ENV,
				GL10.GL_TEXTURE_ENV_MODE,
				GL10.GL_REPLACE);
            int resID = R.drawable.text_atlas;
            InputStream in = context.getResources().openRawResource(resID);
            Bitmap image;
            try {
                image = BitmapFactory.decodeStream(in);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println("Couldn't decode texture stream!");
                }
            }
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, image, 0);
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            image.recycle();
        }

		 for (int i = 1; i < NUM_TEXTURES; ++i)
		 {
		 if (mTextureDataHandle[i] != 0) {
		 gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureDataHandle[i]);
		 gl.glTexParameterf(
		 GL10.GL_TEXTURE_2D,
		 GL10.GL_TEXTURE_MIN_FILTER,
		 GL10.GL_NEAREST);
		 gl.glTexParameterf(
		 GL10.GL_TEXTURE_2D,
		 GL10.GL_TEXTURE_MAG_FILTER,
		 GL10.GL_LINEAR);
		 gl.glTexParameterf(
		 GL10.GL_TEXTURE_2D,
		 GL10.GL_TEXTURE_WRAP_S,
		 GL10.GL_CLAMP_TO_EDGE);
		 gl.glTexParameterf(
		 GL10.GL_TEXTURE_2D,
		 GL10.GL_TEXTURE_WRAP_T,
		 GL10.GL_CLAMP_TO_EDGE);
		 gl.glTexEnvf(
		 GL10.GL_TEXTURE_ENV,
		 GL10.GL_TEXTURE_ENV_MODE,
		 GL10.GL_REPLACE);

		 // Set filtering
		 GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		 GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

		 int resID = R.drawable.helipad_256_a;
		 //String resourceName;
		 //try {
		 //resourceName = "masked_" + i;
		 //resID = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
		 //} catch (Exception e) {
		 //System.out.println("Failed to create resource...");
		 //}
		 InputStream in = context.getResources().openRawResource(resID);
		 Bitmap image;
		 try {
		 image = BitmapFactory.decodeStream(in);
		 } finally {
		 try {
		 in.close();
		 } catch (IOException e) {
		 System.out.println("Couldn't decode texture stream!");
		 }
		 }
		 GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, image, 0);
		 image.recycle();
		 }
		 else
		 {
		 System.out.println("Texture is 0...");
		 }
		 }
		 onSurfaceCreated(surfaceId);
    }

    boolean testTextureOverride()
    {
        boolean useText = useTextures;
        if (overrideTextures != null)
        {
            useText = overrideTextures;
        }
        return useText;
    }

    void commonConstructor(Point3D thePos, Point3D theSize)
    {
		// TODO: Add textures for each side
        position = thePos;
        size = theSize;
    }

    public Object3D(Point3D thePos, Point3D theSize, boolean overText)
    {
		super();
        commonConstructor(thePos, theSize);
        overrideTextures = new Boolean(overText);
    }

    public Object3D(Point3D thePos, Point3D theSize)
	{
		super();
        commonConstructor(thePos, theSize);
    }

	boolean testValue(int thisMin, int thisMax, int otherPos)
	{
		boolean overlaps = false;
        if ((thisMin < otherPos) && (thisMax > otherPos))
        {
            overlaps = true;
        }
		return overlaps;
	}
	
    boolean testValues(int thisMin, int thisMax, int otherMin, int otherMax)
    {
        boolean overlaps = false;
        if (((thisMin > otherMin) && (thisMin < otherMax)) ||
            ((thisMax > otherMin) && (thisMax < otherMax)) ||
            ((otherMin > thisMin) && (otherMin < thisMax)) ||
            ((otherMax > thisMin) && (otherMax < thisMax)))
        {
            overlaps = true;
        }
        return overlaps;
    }

	public boolean collidesWith(Point3D other)
	{
		boolean doesItCollide = false;
        boolean zOverlaps = testValue(position.Z() - size.Z(), position.Z() + size.Z(),
                                       other.Z());
        if (zOverlaps)
        {
            boolean yOverlaps = testValue(position.Y() - size.Y(), position.Y() + size.Y(),
										   other.Y());
            if (yOverlaps)
            {
                doesItCollide = testValue(position.X() - size.X(), position.X() + size.X(),
										   other.X());
            }
        }
		return doesItCollide;
	}
	
    public boolean collidesWith(Object3D other)
    {
        boolean doesItCollide = false;
        Point3D otherPos = other.getPosition();
        Point3D otherSize = other.getSize();
        boolean zOverlaps = testValues(position.Z() - size.Z(), position.Z() + size.Z(),
                                       otherPos.Z() - otherSize.Z(), otherPos.Z() + otherSize.Z());
        if (zOverlaps)
        {
            boolean yOverlaps = testValues(position.Y() - size.Y(), position.Y() + size.Y(),
                    otherPos.Y() - otherSize.Y(), otherPos.Y() + otherSize.Y());
            if (yOverlaps)
            {
                doesItCollide = testValues(position.X() - size.X(), position.X() + size.X(),
                        otherPos.X() - otherSize.X(), otherPos.X() + otherSize.X());
            }
        }
        return doesItCollide;
    }

    /**
     vxs - array of vertices
     ors - array of orders
	 wors - array of wireframe orders
     cls - array of colors
     iv - Position in master vertex array
     io - Position in master draw order array
	 wio - Position in wireframe draw order array
     ic - Position in master color array
     it - Position in master texture array
     */
    public void createObject(float[] vxs, int[] ors, int[] wors,float[] cls, float[] texs
            ,int iv, int io, int wio, int ic, int it)
    {
        // NOTE: Scaling could probably be done at draw time just like translation
        for (int i = 0; i < cubeCoords.length; ++i)
        {
            if (i%3 == 0) // X coordinate
            {
                vxs[iv] = (float) (cubeCoords[i] * size.m_x + position.m_x);
            }
            else if (i%3 == 1) // Y coordinate
            {
                vxs[iv] = (float) (cubeCoords[i] * size.m_y + position.m_y);
            }
            else // Z coordinate
            {
                vxs[iv] = (float) (cubeCoords[i] * size.m_z + position.m_z);
            }
            ++iv;
        }
        // Found the bug!  Draw order has 36 elements, but only need to increment by 24!
        int objOffset = io / drawOrder.length * 24;
        for(int i = 0; i<drawOrder.length; ++i)
        {
            ors[io] = drawOrder[i] + objOffset;
            ++io;
        }
        int wObjOffset = wio / lineDrawOrder.length * 24;
        for(int i = 0; i<lineDrawOrder.length; ++i)
        {
            wors[wio] = lineDrawOrder[i] + wObjOffset;
            ++wio;
        }
		
        for(int i = 0; i<vertexCount; ++i)
        {
            cls[ic++] = color[0];
            cls[ic++] = color[1];
            cls[ic++] = color[2];
            cls[ic++] = color[3];
        }

        for(int i = 0; i < uvs.length; ++i)
        {
            texs[it++] = uvs[i];
        }
		// NOTE: OpenGL Related objects must be created here after the context is created
		// Number of textures below
    }

    // TODO: Update textures for each side
    public int getTexture() { return Object3D.WHICH_TEXTURE; }

    //public void setTexture(int newTexture) { whichTexture = newTexture; }

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

    public void setX(double newX) { position.m_x = newX; }
    public void setY(double newY) { position.m_y = newY; }
    public void setZ(double newZ) { position.m_z = newZ; }

    public float[] getColor() {
        return color;
    }

    public Point3D getPosition() {
        return position;
    }

    public Point3D getSize()
    {
        return size;
    }

    static public void draw(int surfaceId, float[] mvpMatrix, int vtxBuf, int colBuf, int txtBuf, int triBuf, int triCount, int lineBuf, int lineCount)
	{ // pass in the calculated transformation matrix
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram[surfaceId]);
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR)
        {
            System.out.println("Object3d: Use Program Error: " + error + " on program " + mProgram);
        }

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram[surfaceId], "vPosition");
        if (mPositionHandle < 0)
        {
            System.out.println("Object3d: Failed to get mPositionHandle");
        }
		
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram[surfaceId], "uMVPMatrix");

        // Enable a handle to the cube vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the cube coordinate data
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,vtxBuf);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
		
        // get handle to vertex shader's vColor member
		if (useVertexColor)
		{
			mColorHandle = GLES20.glGetAttribLocation(mProgram[surfaceId], "vColor");
			if (mColorHandle < 0)
			{
				System.out.println("Object3D: Failed to get vColor");
			}
			GLES20.glEnableVertexAttribArray(mColorHandle);

			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,colBuf);
			GLES20.glVertexAttribPointer(mColorHandle, COLORS_PER_VERTEX,
										 GLES20.GL_FLOAT, false, colorStride, 0);
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
		}
		else
		{
			mColorHandle = GLES20.glGetUniformLocation(mProgram[surfaceId], "vColor");
			// We do NOT have a static color variable for now
			GLES20.glUniform4f(mColorHandle,0.1f,0.9f,0.1f,1.0f);
		}
		
		if (useTextures && (useWireframeOnly == false))
		{
            mFrameHandle = GLES20.glGetUniformLocation(mProgram[surfaceId], "fNumber");
            GLES20.glUniform1f(mFrameHandle,0.1f * (float)curFrame);
            
        	mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram[surfaceId], "u_texture");
        	if (mTextureUniformHandle < 0)
        	{
            	System.out.println("Object3d: Failed to get texture uniform");
        	}

        	mTextureCoordinateHandle  = GLES20.glGetAttribLocation(mProgram[surfaceId], "a_texCoordinate");
        	if (mTextureCoordinateHandle < 0)
        	{
            	System.out.println("Object3d: Failed to get texture coordinates.");
        	}
        	GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        	// Prepare the uv coordinate data.
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,txtBuf);
    	    GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2,
                GLES20.GL_FLOAT, false, 8, 0);
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);

	        // Set the active texture unit to texture unit 0.
        	GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        	// Bind the texture to this unit.
        	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle[Object3D.WHICH_TEXTURE]);
			
			// Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
			GLES20.glUniform1i(mTextureUniformHandle, 0);
		}
		
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

		if (useWireframeOnly)
		{
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,lineBuf);
			GLES20.glDrawElements(GLES20.GL_LINES, lineCount,
								  GLES20.GL_UNSIGNED_INT, 0);
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,0);
		}
		else
		{
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,triBuf);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, triCount,
								  GLES20.GL_UNSIGNED_INT, 0);
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,0);
		}
        int drawError = GLES20.glGetError();
        if (drawError != GLES20.GL_NO_ERROR)
        {
            System.out.println("Object3d: Draw Elements Error: " + drawError);
        }

		if (useTextures)
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
		updateFrame();
    }

	static public void updateFrame()
	{
		if (frameDelay < FRAME_DELAY)
		{
			++frameDelay;
		}
		else
		{
			frameDelay = 0;
			curFrame = (++curFrame) % NUM_FRAMES;
		}
	}
    public void drawSingle(int surfaceId, int textDataHandle, float[] mvpMatrix) { // pass in the calculated transformation matrix
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram[surfaceId]);
        boolean useText = testTextureOverride();
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR)
        {
            System.out.println("Use Program Error: " + error);
        }

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram[surfaceId], "vPosition");
        if (mPositionHandle < 0)
        {
            System.out.println("Failed to get mPositionHandle");
        }

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram[surfaceId], "uMVPMatrix");

        // Enable a handle to the cube vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the cube coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to vertex shader's vColor member
        if (useVertexColor)
        {
            mColorHandle = GLES20.glGetAttribLocation(mProgram[surfaceId], "vColor");
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
            mColorHandle = GLES20.glGetUniformLocation(mProgram[surfaceId], "vColor");
			GLES20.glUniform4f(mColorHandle,color[0],color[1],color[2],color[3]);
        }

        if (useText)
        {
            mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram[surfaceId], "u_texture");
            if (mTextureUniformHandle < 0)
            {
                System.out.println("Failed to get texture uniform");
            }

            mTextureCoordinateHandle  = GLES20.glGetAttribLocation(mProgram[surfaceId], "a_texCoordinate");
            if (mTextureCoordinateHandle < 0)
            {
                System.out.println("Failed to get texture coordinates.");
            }
            GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

            // Prepare the uv coordinate data.
            GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2,
                    GLES20.GL_FLOAT, false, 8, uvBuffer);

            // Set the active texture unit to texture unit 0.
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

            // Bind the texture to this unit.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textDataHandle);
        }

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawListBuffer.capacity(),
                GLES20.GL_UNSIGNED_INT, drawListBuffer);
        int drawError = GLES20.glGetError();
        if (drawError != GLES20.GL_NO_ERROR)
        {
            System.out.println("Draw Elements Error: " + drawError);
        }

        if (useText)
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

}
