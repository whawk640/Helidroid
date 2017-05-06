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
public class HeliGLRenderer implements GLSurfaceView.Renderer {

    private Camera mCamera;
    private double camDistance;
    private int[] mTextureDataHandle;
    private boolean surfaceCreated;
    private World theWorld = null;

    private static final double FULL_BLOCK_SIZE = 100.0;

    private static final double STREET_OFFSET = 3.0;

    private static final double SIDEWALK_OFFSET = 2.0;

    private static final double BLOCK_SIZE = FULL_BLOCK_SIZE - 2.0 * STREET_OFFSET;

    private static final double SQUARE_SIZE = BLOCK_SIZE - 2.0 * SIDEWALK_OFFSET;

    private static final double BUILDING_SPACE = (SQUARE_SIZE / 10.0);

    private static final double BUILDING_SIZE = 0.9 * BUILDING_SPACE;

    private static final double HOUSES_PER_BLOCK = 10.0;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private volatile float mAngle;
    private Context mContext;

    private ArrayList<Object3D> worldState = null;
    public void createObjects() {
        if (worldState == null)
        {
            worldState = new ArrayList<Object3D>();
        }
        // TODO: Prevent double creation
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
                sidewalk.setColor(0.8f, 0.8f, 0.8f, 1.0f);
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
        newObject.setColor((float)r, (float)g, (float)b, (float)a);
        worldState.add(newObject);
    }

    public Object3D makeHouse(double posX, double posY, double posZ)
    {
        double buildingHeight = computeBuildingHeight();
        Point3D buildingPos = new Point3D(posX, posY, posZ);
        Point3D buildingSize = new Point3D(BUILDING_SIZE, BUILDING_SIZE, buildingHeight);
        Object3D worldObj = new Object3D(buildingPos, buildingSize);
        worldObj.setColor(0.6f, 0.6f + 0.4f * (float)Math.random(), 0.6f + 0.4f * (float)Math.random(), 1.0f);
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

    public boolean isSurfaceCreated() {
        return surfaceCreated;
    }

    float getAngle() {
        return mAngle;
    }

    public void setWorld(World newWorld)
    {
        if (theWorld == null) // For now, there can be only one world
        {
            theWorld = newWorld;
        }
    }

    public void setCamDistance(double dist)
    {
        camDistance = dist;
    }

    public HeliGLRenderer(Context context) {
        super();
        mContext = context;
        surfaceCreated = false;
        camDistance = 100.0;
    }

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
            int resID = R.drawable.helipad_256;
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
        mCamera = new Camera();
        // TODO: adjust eye position based on puzzle size
        mCamera.setSource(0.0, -500.0, 1.0 * camDistance);
        mCamera.setTarget(500.0, 500.0, 5.0);
        mCamera.setUp(0.0, 0.0, 1.0);
        // NOTE: OpenGL Related objects must be created here after the context is created
        int cell = 0;
        // Number of textures below
        mTextureDataHandle = initImage(mContext, gl, 1);
        createObjects();

        surfaceCreated = true;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0,
                (float) mCamera.source.x(), (float) mCamera.source.y(), (float) mCamera.source.z(),
                (float) mCamera.target.x(), (float) mCamera.target.y(), (float) mCamera.target.z(),
                (float) mCamera.upUnit.x(), (float) mCamera.upUnit.y(), (float) mCamera.upUnit.z());

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        for (Object3D thisObject : worldState)
        {
            float[] scratch = thisObject.buildTransformation(mMVPMatrix, mAngle, 0, -1.0f, 0.0f, 1.0f, 1.0f, 1.0f);
            int locTexture = thisObject.getTexture();
            thisObject.draw(mTextureDataHandle[locTexture], scratch);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        mCamera.setSource(mCamera.source.m_x, mCamera.source.m_y, mCamera.source.m_z + 2.0);

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method

        // Reminder -- matrix, offset, low x, high x, low y, high y, near, far
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1.0f, 1.0f, 2.0f, 2000.0f);
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
