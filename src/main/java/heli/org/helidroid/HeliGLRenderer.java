package heli.org.helidroid;

//import android.opengl.EGLConfig;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
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

/**
 * Created by Dan LaFuze on 10/16/2015.
 */
public class HeliGLRenderer implements GLSurfaceView.Renderer 
{
    private Camera mCamera;
    private double camDistance;
    private int[] mTextureDataHandle;
    private boolean surfaceCreated;
    private World theWorld = null;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private volatile float mAngle;
    private Context mContext;

    public boolean isSurfaceCreated() {
        return surfaceCreated;
    }

    float getAngle() {
        return mAngle;
    }

    public void setCamDistance(double dist)
    {
        camDistance = dist;
    }

    public HeliGLRenderer(Context context, World wrld) {
        super();
		theWorld = wrld;
        mContext = context;
        surfaceCreated = false;
        camDistance = 50.0;
        mAngle = 0.0f;
    }

    // For now, I'm rotating the camera about the Z axis
    // by the specified amount.
    public void setAngle(float angle) {
        mAngle = angle;
    }

    private static int[] initImage(final Context context, GL10 gl, int numTextures) {
        final int[] textureHandle = new int[numTextures];
        gl.glGenTextures(numTextures, textureHandle, 0);
        if (textureHandle[0] != 0) {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textureHandle[0]);
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
            int resID = R.drawable.helipad_256_a;
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

        /* Re-enable this to bind more textures
        for (int i = 1; i < numTextures; ++i)
        {
            if (textureHandle[i] != 0) {
                gl.glBindTexture(GL10.GL_TEXTURE_2D, textureHandle[i]);
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

                int resID = R.drawable.masked_1;
                String resourceName;
                try {
                    resourceName = "masked_" + i;
                    resID = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
                } catch (Exception e) {
                    System.out.println("Failed to create resource...");
                }
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
        } */

        return textureHandle;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        mCamera = new Camera();
        // TODO: adjust eye position based on world size
        mCamera.setSource(-15.0, -10.0, 2.5);
        mCamera.setTarget(0.0, 0.0, 0.0);
        mCamera.setUp(0.0, 0.0, 1.0);
        // NOTE: OpenGL Related objects must be created here after the context is created
        int cell = 0;
        // Number of textures below
        mTextureDataHandle = initImage(mContext, gl, 1);
        theWorld.createObjects();

        surfaceCreated = true;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // Set the camera position (View matrix)
        //mCamera.source.m_x += 0.016f;
		//mCamera.source.m_y += 0.001;
		//mCamera.source.m_z -= 0.002;
		mCamera.orbit(120);
		mCamera.show();
        Matrix.setLookAtM(mViewMatrix, 0,
                (float) mCamera.source.x(), (float) mCamera.source.y(), (float) mCamera.source.z(),
                (float) mCamera.target.x(), (float) mCamera.target.y(), (float) mCamera.target.z(),
                (float) mCamera.upUnit.x(), (float) mCamera.upUnit.y(), (float) mCamera.upUnit.z());

        float[] newProjectMatrix = new float[16];
        Matrix.rotateM(newProjectMatrix,0, mProjectionMatrix,0,mAngle,0.0f,0.0f,1.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, newProjectMatrix, 0, mViewMatrix, 0);

        theWorld.draw(mTextureDataHandle[0], mMVPMatrix);
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
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1.0f, 1.0f, 5.0f, 2000.0f);
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
