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
import java.nio.*;

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

    private static final double HALF_BLOCK_OFFSET = 50.0;

    private static final double STREET_OFFSET = 3.0;

    private static final double SIDEWALK_OFFSET = 2.0;

    private static final double BLOCK_SIZE = FULL_BLOCK_SIZE - 2.0 * STREET_OFFSET;

    private static final double SQUARE_SIZE = BLOCK_SIZE - 2.0 * SIDEWALK_OFFSET;

    private static final double BUILDING_SPACE = (SQUARE_SIZE / 10.0);

    private static final double HALF_BUILDING_OFFSET = BUILDING_SPACE * 0.5;

    private static final double BUILDING_SIZE = 0.9 * BUILDING_SPACE;

    private static final double HOUSES_PER_BLOCK = 10.0;
	
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private volatile float mAngle;
    private Context mContext;

    private ArrayList<Object3D> worldState = null;
	
	static protected final int BLOCK_ROWS = 10;
	static protected final int BLOCK_COLS = 10;
	static protected final int OBJECTS_PER_BLOCK = 37;
    
	float[] vxs = new float[BLOCK_ROWS * BLOCK_COLS * OBJECTS_PER_BLOCK * Object3D.cubeCoords.length];
	float[] cls = new float[BLOCK_ROWS * BLOCK_COLS * OBJECTS_PER_BLOCK * (Object3D.cubeCoords.length / Object3D.COORDS_PER_VERTEX) * Object3D.COLORS_PER_VERTEX];
	int[] drawOrder = new int[BLOCK_ROWS * BLOCK_COLS * OBJECTS_PER_BLOCK * Object3D.drawOrder.length];
	float[] texs = new float[BLOCK_ROWS * BLOCK_COLS * OBJECTS_PER_BLOCK * Object3D.uvs.length];
	
	public void createObjects() 
	{
        if (worldState == null)
        {
            worldState = new ArrayList<Object3D>();
        }
        // TODO: Prevent double creation
        // Generate the world... TODO: Move to city blocks
		int idx = 0;
        for (int row = 0; row < BLOCK_ROWS; ++row)
        {
            for (int col = 0; col < BLOCK_COLS; ++col)
            {
                // Generate a city block
                // TODO: Move to CityBlock class
                // For now, streets are 6.0 m wide
                // and Sidewalks are 3.0 m wide
                double startX = FULL_BLOCK_SIZE * col + STREET_OFFSET;
                double startY = FULL_BLOCK_SIZE * row + STREET_OFFSET;
                // Sidewalks are 0.1 m above street
                Point3D sidewalkPos = new Point3D(startX + HALF_BLOCK_OFFSET, startY + HALF_BLOCK_OFFSET, 0.0);
                Point3D sidewalkSize = new Point3D(BLOCK_SIZE, BLOCK_SIZE, 0.1);
                Object3D sidewalk = new Object3D(sidewalkPos, sidewalkSize);
				sidewalk.createObject(vxs,drawOrder,cls,texs,
				idx * Object3D.cubeCoords.length, idx * Object3D.drawOrder.length,
				idx * 4 * Object3D.cubeCoords.length / 3, idx * Object3D.uvs.length); 
				++idx;
				
                sidewalk.setColor(0.8f, 0.8f, 0.8f, 1.0f);
                worldState.add(sidewalk);
                double startZ = 0.1;
                startX += 0.05 * BUILDING_SPACE + SIDEWALK_OFFSET;
                startY += 0.05 * BUILDING_SPACE + SIDEWALK_OFFSET;
                for (int houseIndex = 0; houseIndex < Math.round(HOUSES_PER_BLOCK); ++houseIndex)
                {
                    Object3D leftHouse = makeHouse(startX, startY + houseIndex * BUILDING_SPACE, startZ);
					leftHouse.createObject(vxs,drawOrder,cls,texs,
										   idx * Object3D.cubeCoords.length, idx * Object3D.drawOrder.length,
										   idx * 4 * Object3D.cubeCoords.length / 3, idx * Object3D.uvs.length); 
					++idx;
                    worldState.add(leftHouse);
                    Object3D rightHouse = makeHouse(startX + 9 * BUILDING_SPACE, startY + houseIndex * BUILDING_SPACE, startZ);
                    rightHouse.createObject(vxs,drawOrder,cls,texs,
											idx * Object3D.cubeCoords.length, idx * Object3D.drawOrder.length,
											idx * 4 * Object3D.cubeCoords.length / 3, idx * Object3D.uvs.length); 
					++idx;
					
					worldState.add(rightHouse);
                    if (houseIndex == 0 || houseIndex == 9)
                    {
                        continue;
                    }
                    Object3D topHouse = makeHouse(startX + houseIndex * BUILDING_SPACE, startY, startZ);
                    topHouse.createObject(vxs,drawOrder,cls,texs,
										  idx * Object3D.cubeCoords.length, idx * Object3D.drawOrder.length,
										  idx * 4 * Object3D.cubeCoords.length / 3, idx * Object3D.uvs.length); 
					++idx;
					worldState.add(topHouse);
                    Object3D bottomHouse = makeHouse(startX  + houseIndex * BUILDING_SPACE, startY + 9 * BUILDING_SPACE, startZ);
                    bottomHouse.createObject(vxs,drawOrder,cls,texs,
											 idx * Object3D.cubeCoords.length, idx * Object3D.drawOrder.length,
											 idx * 4 * Object3D.cubeCoords.length / 3, idx * Object3D.uvs.length); 
					++idx;
					worldState.add(bottomHouse);
                }
            }
        }
		Object3D.vertexBuffer = getFB(vxs);
		Object3D.colBuffer = getFB(cls);
		Object3D.uvBuffer = getFB(texs);
		Object3D.drawListBuffer = getIB(drawOrder);
    }

    public Object3D makeHouse(double posX, double posY, double posZ)
    {
        double buildingHeight = computeBuildingHeight();
        posZ += buildingHeight / 2.0;
        // The offset is to ensure the position is at the center
        Point3D buildingPos = new Point3D(posX + HALF_BUILDING_OFFSET, posY + HALF_BUILDING_OFFSET, posZ);
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
        mCamera.setSource(0, 500.0, 80);
        mCamera.setTarget(500.0, 500.0, 0.0);
        mCamera.setUp(0.0, 0.0, 1.0);
        // NOTE: OpenGL Related objects must be created here after the context is created
        // Number of textures below
        mTextureDataHandle = initImage(mContext, gl, 1);
        // TODO: Get this back into the world... Perhaps we create the world first
        // Then this class.
        createObjects();

        surfaceCreated = true;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // TODO: Remove hardcoding etc.
        mCamera.source.m_x += 2.0;
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0,
                (float) mCamera.source.x(), (float) mCamera.source.y(), (float) mCamera.source.z(),
                (float) mCamera.target.x(), (float) mCamera.target.y(), (float) mCamera.target.z(),
                (float) mCamera.upUnit.x(), (float) mCamera.upUnit.y(), (float) mCamera.upUnit.z());

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

		Object3D.draw(0,mMVPMatrix);
		/*
        for (Object3D thisObject : worldState)
        {
            float[] scratch = thisObject.buildTransformation(mMVPMatrix, mAngle, 0, -1.0f, 0.0f, 1.0f, 1.0f, 1.0f);
            int locTexture = thisObject.getTexture();
            thisObject.draw(mTextureDataHandle[locTexture], scratch);
        }
		*/
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
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1.0f, 1.0f, 5.0f, 1000.0f);
    }

    static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
		String cmpStat = GLES20.glGetShaderInfoLog(shader);
		System.out.println("Shader info '" + cmpStat +"'");

        return shader;
    }
	

	public static ByteBuffer getBB(byte[] src)
	{
		ByteBuffer idxs = ByteBuffer.allocateDirect(src.length);
		idxs.order(ByteOrder.nativeOrder());
		idxs.put(src);
		idxs.position(0);
		return idxs;
	}

	public static FloatBuffer getFB(float[] src)
	{
		ByteBuffer idxs = ByteBuffer.allocateDirect(src.length * 4);
		idxs.order(ByteOrder.nativeOrder());
		FloatBuffer fb = idxs.asFloatBuffer();
		fb.put(src);
		fb.position(0);
		return fb;
	}

	public static IntBuffer getIB(int[] src)
	{
		ByteBuffer idxs = ByteBuffer.allocateDirect(src.length * 4);
		idxs.order(ByteOrder.nativeOrder());
		IntBuffer fb = idxs.asIntBuffer();
		fb.put(src);
		fb.position(0);
		return fb;
	}
	
}
