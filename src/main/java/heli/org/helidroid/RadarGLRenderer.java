package heli.org.helidroid;

import android.content.*;
import android.opengl.*;
import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by Dan LaFuze on 10/16/2015.
 * This class will display radar data for the chopper.
 * It is envisioned that this file will move with the chopper and will collect data about
 * nearby objects.
 */
public class RadarGLRenderer implements GLSurfaceView.Renderer
{
    private Camera mCamera = null;
    private boolean surfaceCreated;
    private World theWorld = null;
	private int chopper = 0;
    private int surfaceId;

    private static int nextSurfaceID = 1;
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private static final double RADAR_DISTANCE = 1024.0;

    private volatile float mAngle;
    private Context mContext;

	static protected String buildVertexCode()
	{
		String vertexString = 
			"uniform mat4 uMVPMatrix;" +
			"attribute vec4 vPosition;" +
			//"varying float fogFactor;" +
			"void main() {" +
			"  gl_Position = uMVPMatrix * vPosition;" +
			//"  vVertex = vec3(gl_ModelViewMatrix * gl_Vertex);" +
			//"  gl_FogFragCoord = length(vVertex);" +
			//"  fogFactor = gl_Fog.density * gl_FogFragCoord;" +
			//"  fogFactor = clamp(fogFactor,0.0,1.0);" +
			"}";
		return vertexString;
	}

	static protected String buildFragmentCode()
	{
		String fragmentString = "precision mediump float;" +
			"lowp vec4 vColor = vec4(1.0);" +
			//"varying float fogFactor;" +
			"void main() {" +
			"  vec4 finalColor = vColor * 1.0;" +
			//"  gl_FragColor = mix(gl_Fog.color, finalColor, fogFactor);" +
			"  gl_FragColor = finalColor;" +
			"}";
		return fragmentString;
	}		
	
	public void setChopper(int which)
	{
		chopper = which;
	}

    public boolean isSurfaceCreated()
	{
        return surfaceCreated;
    }

    float getAngle()
	{
        return mAngle;
    }

	public void rotateCameraXY(double angleDeg)
	{
		mCamera.rotateXY(angleDeg);
	}

    public RadarGLRenderer(Context context, World wrld, int chopperID)
	{
        super();
        surfaceId = nextSurfaceID++;
		theWorld = wrld;
        mContext = context;
        surfaceCreated = false;
        mAngle = 0.0f;
        chopper = chopperID;
    }

    // For now, I'm rotating the camera about the Z axis
    // by the specified amount.
    public void setAngle(float angle)
	{
        mAngle = angle;
    }

	public void moveCamera(Point3D deltaCam)
	{
		if (mCamera != null)
		{
			Point3D newSrc = mCamera.source.add(deltaCam);
			mCamera.setSource(newSrc);
		}
	}

    public void setCamera(Point3D newCam)
    {
        if (mCamera != null)
        {
            Point3D newSrc = mCamera.source = newCam.copy();
            mCamera.setSource(newSrc);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		gl.glEnable(gl.GL_FOG);
		float[] fogColor = {0.0f, 0.0f, 0.0f, 1.0f};
		gl.glFogfv(gl.GL_FOG_COLOR,fogColor,0);
        mCamera = new Camera();
        // TODO: adjust eye position as soon as we know chopper location
        mCamera.setSource(100.0, 100.0, 150.0);
        mCamera.setTarget(500.0, 500.0, 0.0);
        mCamera.setUp(0.0, 0.0, 1.0);
        surfaceCreated = true;
        theWorld.setupObjects(mContext,gl,config,surfaceId);
    }

    @Override
    public void onDrawFrame(GL10 gl)
	{
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // Set the camera position (View matrix)
        //mCamera.source.m_x += 0.016f;
		//mCamera.source.m_y += 0.001;
		//mCamera.source.m_z -= 0.002;
		Point3D chopPosition = theWorld.gps(chopper);
        chopPosition.m_z += 1.5; // Half the chopper height
        ChopperInfo chopInfo = theWorld.getChopInfo(chopper);
        double heading = chopInfo.getHeading();
        mCamera.setSource(chopPosition);
        Point3D targetPosition = chopPosition.copy();
        double angleRadians = Math.toRadians(heading);
        targetPosition.m_x += (RADAR_DISTANCE * Math.sin(angleRadians));
        targetPosition.m_y += (RADAR_DISTANCE * Math.cos(angleRadians));
        targetPosition.m_z = chopPosition.m_z;

        mCamera.setTarget(targetPosition);
        Matrix.setLookAtM(mViewMatrix, 0,
                (float) mCamera.source.x(), (float) mCamera.source.y(), (float) mCamera.source.z(),
                (float) mCamera.target.x(), (float) mCamera.target.y(), (float) mCamera.target.z(),
                (float) mCamera.upUnit.x(), (float) mCamera.upUnit.y(), (float) mCamera.upUnit.z());

        float[] newProjectMatrix = new float[16];
        Matrix.rotateM(newProjectMatrix,0, mProjectionMatrix,0,mAngle,0.0f,0.0f,1.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, newProjectMatrix, 0, mViewMatrix, 0);

        theWorld.draw(mMVPMatrix, mContext,surfaceId);
    }

    public void orbitCamera(double ticks)
    {
        mCamera.orbit(ticks);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        mCamera.setSource(mCamera.source.m_x, mCamera.source.m_y, mCamera.source.m_z);

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method

        // Reminder -- matrix, offset, low x, high x, low y, high y, near, far
        Matrix.frustumM(mProjectionMatrix, 0, -ratio/10, ratio/10, -0.1f, 0.1f, 3.0f, (float)RADAR_DISTANCE);
    }

    static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

}
