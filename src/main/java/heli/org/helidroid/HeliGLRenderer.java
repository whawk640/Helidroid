package heli.org.helidroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

/**
 * Created by Dan LaFuze on 10/16/2015.
 */
public class HeliGLRenderer implements GLSurfaceView.Renderer 
{
    private Camera mCamera = null;
    private double camDistance;
	private static boolean firstSurfaceCreated = false;
    private boolean surfaceCreated;
    private World theWorld = null;
	private int camMode = HeliGLSurfaceView.MODE_OVERVIEW;
	private int chopper = 0;
	
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private volatile float mAngle;
    private Context mContext;

	public void setChopper(int which)
	{
		chopper = which;
	}
	
	public void setMode(int cameraMode)
	{
		camMode = cameraMode;
		
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
	
	public void adjustCameraHeight(double deltaZ)
	{
		mCamera.adjustHeight(deltaZ);
	}
	
    public void setCamDistance(double dist)
    {
        camDistance = dist;
    }

    public HeliGLRenderer(Context context, World wrld)
	{
        super();
		theWorld = wrld;
        mContext = context;
        surfaceCreated = false;
        camDistance = 50.0;
        mAngle = 0.0f;
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
	
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        mCamera = new Camera();
        // TODO: adjust eye position based on world size
        mCamera.setSource(100.0, 100.0, 150.0);
        mCamera.setTarget(500.0, 500.0, 0.0);
        mCamera.setUp(0.0, 0.0, 1.0);
		if (!firstSurfaceCreated)
		{
			theWorld.createObjects(mContext,gl,config);
			firstSurfaceCreated = true;
		}

        surfaceCreated = true;
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
        if (camMode == HeliGLSurfaceView.MODE_CHASE)
		{
            mCamera.chase(chopPosition, theWorld.getCamDistance());
        }
        else
        {
			// TODO: Respect camera distance change
            mCamera.setTarget(theWorld.getCenter());
            mCamera.orbit(120);
        }
        Matrix.setLookAtM(mViewMatrix, 0,
                (float) mCamera.source.x(), (float) mCamera.source.y(), (float) mCamera.source.z(),
                (float) mCamera.target.x(), (float) mCamera.target.y(), (float) mCamera.target.z(),
                (float) mCamera.upUnit.x(), (float) mCamera.upUnit.y(), (float) mCamera.upUnit.z());

        float[] newProjectMatrix = new float[16];
        Matrix.rotateM(newProjectMatrix,0, mProjectionMatrix,0,mAngle,0.0f,0.0f,1.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, newProjectMatrix, 0, mViewMatrix, 0);

        theWorld.draw(mMVPMatrix);
    }

    public void orbitCamera(double ticks)
    {
        mCamera.orbit(ticks);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        mCamera.setSource(mCamera.source.m_x, mCamera.source.m_y, mCamera.source.m_z + 2.0);

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method

        // Reminder -- matrix, offset, low x, high x, low y, high y, near, far
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1.0f, 1.0f, 5.0f, 4000.0f);
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
