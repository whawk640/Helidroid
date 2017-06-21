package heli.org.helidroid;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by Daniel A. LaFuze on 6/20/2017.
 */

public class RadarGLSurfaceView extends GLSurfaceView
{
    private final RadarGLRenderer mRenderer;

	public void setChopper(int newChopper)
	{
		if (mRenderer != null)
		{
			mRenderer.setChopper(newChopper);
		}
		requestRender();
	}

    public RadarGLSurfaceView(Context context, World wrld, int chopperID)
	{
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new RadarGLRenderer(context, wrld, chopperID);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private float mPreviousX;
    private float mPreviousY;

    public RadarGLRenderer getRenderer()
    {
        return mRenderer;
    }

}
