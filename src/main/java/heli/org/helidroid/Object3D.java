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
public class Object3D {
    private static int mProgram = -1;
    private Point3D position;
    private Point3D size;

    static float color[] = { 1.0f, 1.0f, 1.0f, 1.0f };

    static public FloatBuffer vertexBuffer;
    static public IntBuffer drawListBuffer;
    static public FloatBuffer uvBuffer;
    static public FloatBuffer colBuffer;

	static protected boolean useVertexColor = true;
	
	static protected boolean useTextures = true;
	
    // TODO: Add textures for each side
    // private int[] whichTexture = {0, 0, 0, 0, 0, 0};
    private int whichTexture;

    /** Example Shader Code to draw textures and per vertex color.
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 a_texCoordinate;" + // Texture coordinates
                    "attribute vec4 vColor;" +
                    "varying vec2 v_texCoordinate;" + // Texture coordinates
                    "varying vec4 fColor;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  v_texCoordinate = a_texCoordinate;" +
                    "  fColor = vColor;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D u_texture;" +
                    "varying vec2 v_texCoordinate;" +
                    "varying vec4 fColor;" +
                    "void main() {" +
                    "  gl_FragColor = fColor * texture2D( u_texture, v_texCoordinate);" +
                    "}";
    */

    // Use to access and set the view transformation
    static private int mMVPMatrixHandle;

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

    static private int mPositionHandle;
    static private int mColorHandle;
    static private int mTextureCoordinateHandle;
    /** Size of the texture coordinate data in elements. */
    private final int mTextureCoordinateDataSize = 2;
    static private int mTextureUniformHandle;

    public static final float uvs[] = {
            0.0f, 0.0f, // First Face
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f, // Second Face
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f, // Third Face
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f, // Fourth Face
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f, // Fifth Face
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f, // Sixth Face
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };

    static final int COORDS_PER_VERTEX = 3;
    static final int COLORS_PER_VERTEX = 4;
    public static final int vertexCount = cubeCoords.length / COORDS_PER_VERTEX;
    static private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per coordinate
    static private final int colorStride = COLORS_PER_VERTEX * 4; // 4 bytes per RGBA

    private Boolean overrideTextures = null;

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
        if (mProgram < 0)
        {
            boolean useVColor = useVertexColor;
            boolean useText = testTextureOverride();
            String vertexCode = buildVertexCode(useVColor, useText);
            int vertexShader = HeliGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                    vertexCode);
            String fragmentCode = buildFragmentCode(useVColor, useText);
            int fragmentShader = HeliGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                    fragmentCode);
            if (vertexShader > 0 && fragmentShader > 0) {
                // create empty OpenGL ES Program
                mProgram = GLES20.glCreateProgram();

                // add the vertex shader to program
                GLES20.glAttachShader(mProgram, vertexShader);

                // add the fragment shader to program
                GLES20.glAttachShader(mProgram, fragmentShader);

                // creates OpenGL ES program executables
                GLES20.glLinkProgram(mProgram);
                System.out.println("Object3D Shaders created, vtx: " + vertexShader + ", fragment: " +
                        fragmentShader + ", program ID: " + mProgram);
            }
            else
            {
                System.out.println("Object3D Failed to load shader program -- vertex: " + vertexShader +
                        ", fragment: " + fragmentShader);
            }

        }
    }

    public Object3D(Point3D thePos, Point3D theSize, boolean overText)
    {
        commonConstructor(thePos, theSize);
        overrideTextures = new Boolean(overText);
    }

    public Object3D(Point3D thePos, Point3D theSize) {
        commonConstructor(thePos, theSize);
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
     cls - array of colors
     iv - Position in master vertex array
     io - Position in master draw order array
     ic - Position in master color array
     it - Position in master texture array
     */
    public void createObject(float[] vxs, int[] ors, float[] cls, float[] texs
            ,int iv, int io, int ic, int it)
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
    }


    // TODO: Update textures for each side
    public int getTexture() { return whichTexture; }

    public void setTexture(int newTexture) { whichTexture = newTexture; }

    // TODO: Switch to doubles
    public void setColor(float red, float green, float blue, float alpha)
    {
        color[0] = red;
        color[1] = green;
        color[2] = blue;
        color[3] = alpha;
    }

    // TODO: Switch to doubles
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

    public Point3D getSize() {
        return size;
    }

    static public void draw(int textDataHandle, float[] mvpMatrix) { // pass in the calculated transformation matrix
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
		}
		
		if (useTextures)
		{
        	mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_texture");
        	if (mTextureUniformHandle < 0)
        	{
            	System.out.println("Failed to get texture uniform");
        	}

        	mTextureCoordinateHandle  = GLES20.glGetAttribLocation(mProgram, "a_texCoordinate");
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
    }

    public void drawSingle(int textDataHandle, float[] mvpMatrix) { // pass in the calculated transformation matrix
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);
        boolean useText = testTextureOverride();
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
        }

        if (useText)
        {
            mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_texture");
            if (mTextureUniformHandle < 0)
            {
                System.out.println("Failed to get texture uniform");
            }

            mTextureCoordinateHandle  = GLES20.glGetAttribLocation(mProgram, "a_texCoordinate");
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
