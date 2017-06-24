package heli.org.helidroid;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Base3D
{
	static final int MAX_SURFACES = 4;
    static protected int mProgram[] = null;
    static protected final int COORDS_PER_VERTEX = 3;
    static protected final int COLORS_PER_VERTEX = 4;
    static protected final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per coordinate
    static protected final int colorStride = COLORS_PER_VERTEX * 4; // 4 bytes per RGBA

    static protected int mPositionHandle;
    static protected int mColorHandle;
	static protected int mFrameHandle;
    static protected int mTextureCoordinateHandle;
    static protected int mTextureUniformHandle;
	// Use to access and set the view transformation
    static protected int mMVPMatrixHandle;

	static public boolean useWireframeOnly = false;
	static protected boolean useVertexColor = true;
	static protected boolean useTextures = true;
	
    private Boolean overrideTextures = null;

	public Base3D()
	{
		// Intentionally empty
	}

	static public void onSurfaceCreated(int surfaceId)
	{
		if (mProgram == null)
		{
			mProgram = new int[MAX_SURFACES];
			mProgram[0] = -1;
			mProgram[1] = -1;
			mProgram[2] = -1;
			mProgram[3] = -1;
		}
		if (mProgram[surfaceId] < 0)
		{
			boolean useVColor = useVertexColor;
			boolean useText = useTextures;
			String vertexCode = "";
			if (surfaceId == 0)
			{
				vertexCode = buildVertexCode(useVColor, useText);
			}
			else
			{
				vertexCode = RadarGLRenderer.buildVertexCode();
			}
			int vertexShader = HeliGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
					vertexCode);
			String fragmentCode = "";
			if (surfaceId == 0)
			{
				fragmentCode = buildFragmentCode(useVColor, useText);
			}
			else
			{
				fragmentCode = RadarGLRenderer.buildFragmentCode();
			}
			int fragmentShader = HeliGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
					fragmentCode);
			if (vertexShader > 0 && fragmentShader > 0)
			{
				// create empty OpenGL ES Program
				mProgram[surfaceId] = GLES20.glCreateProgram();

				// add the vertex shader to program
				GLES20.glAttachShader(mProgram[surfaceId], vertexShader);

				// add the fragment shader to program
				GLES20.glAttachShader(mProgram[surfaceId], fragmentShader);

				// creates OpenGL ES program executables
				GLES20.glLinkProgram(mProgram[surfaceId]);
				System.out.println("Base3D Shaders created, vtx: " + vertexShader + ", fragment: " +
						fragmentShader + ", program ID: " + mProgram[surfaceId]);
			}
			else
			{
				System.out.println("Base3D Failed to load shader program -- vertex: " + vertexShader +
						", fragment: " + fragmentShader);
			}
		}
	}
	/* Derived classes can build programs to respect their vertex
	and texture rules.
	*/
	static protected int buildProgram(boolean vColor, boolean useText)
	{
		int programID = -1;
		String vertexCode = buildVertexCode(vColor, useText);
		int vertexShader = HeliGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
													 vertexCode);
		String fragmentCode = buildFragmentCode(vColor, useText);
		int fragmentShader = HeliGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
													   fragmentCode);
		if (vertexShader > 0 && fragmentShader > 0) {
			// create empty OpenGL ES Program
			programID = GLES20.glCreateProgram();

			// add the vertex shader to program
			GLES20.glAttachShader(programID, vertexShader);

			// add the fragment shader to program
			GLES20.glAttachShader(programID, fragmentShader);

			// creates OpenGL ES program executables
			GLES20.glLinkProgram(programID);
			int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programID, GLES20.GL_LINK_STATUS, linkStatus, 0);
			if (linkStatus[0] != GLES20.GL_TRUE)
			{
				GLES20.glDeleteProgram(programID);
				throw new RuntimeException("Could not link program: "
						+ GLES20.glGetProgramInfoLog(programID));
			}

			System.out.println("User Shaders created, vtx: " + vertexShader + ", fragment: " +
							   fragmentShader + ", program ID: " + programID);
		}
		else
		{
			System.out.println("User Failed to load shader program -- vertex: " + vertexShader +
							   ", fragment: " + fragmentShader);
		}
		return programID;
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

	static protected String buildVertexCode(boolean vertexColor, boolean enableTextures)
	{
		String vertexString = 
			"uniform mat4 uMVPMatrix;" +
			//"uniform float fNumber;" +
			"attribute vec4 vPosition;";
		if (vertexColor)
		{
			vertexString += "attribute vec4 vColor;";  			 			  
		}
		if (enableTextures)
		{
			vertexString += "uniform float fNumber;";
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
			vertexString += "  v_texCoordinate[0] = a_texCoordinate[0] + fNumber;";
			vertexString += "  v_texCoordinate[1] = a_texCoordinate[1];";
		}
		if (vertexColor)
		{
			vertexString += "  fColor = vColor;";
		}
		vertexString +=	"}";
		return vertexString;
	}

	static protected String buildFragmentCode(boolean vertexColor, boolean enableTextures)
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
			//fragmentString += "  u_texture[0] = u_texture[0] + 0.1 * fNumber;";
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
	
}
