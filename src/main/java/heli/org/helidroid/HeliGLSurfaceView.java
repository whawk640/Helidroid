package heli.org.helidroid;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by Daniel A. LaFuze on 5/4/2017.
 */

public class HeliGLSurfaceView extends GLSurfaceView
{
    private final HeliGLRenderer mRenderer;

	static public final int MODE_OVERVIEW = 0;
	static public final int MODE_CHASE = 1;
	
	public void setCameraMode(int newMode)
	{
		if (mRenderer != null)
		{
			mRenderer.setMode(newMode);
			requestRender();
		}
	}
	
	public void setChopper(int newChopper)
	{
		if (mRenderer != null)
		{
			mRenderer.setChopper(newChopper);
		}
		requestRender();
	}
	
    public HeliGLSurfaceView(Context context, World wrld)
	{
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new HeliGLRenderer(context, wrld);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private final double H_TOUCH_SCALE_FACTOR = 10.0;
    private final double V_TOUCH_SCALE_FACTOR = 1.0;
    private float mPreviousX;
    private float mPreviousY;

    public HeliGLRenderer getRenderer()
    {
        return mRenderer;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction())
		{
            case MotionEvent.ACTION_MOVE:
			{
                double dx = x - mPreviousX;
                double dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2)
				{
                    dx = dx * -1;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2)
				{
                    dy = dy * -1;
                }

				mRenderer.rotateCameraXY(dx * H_TOUCH_SCALE_FACTOR);
				mRenderer.adjustCameraHeight(dy * V_TOUCH_SCALE_FACTOR);
            }
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
}
