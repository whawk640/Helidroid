package heli.org.helidroid;

import android.os.Bundle;
import android.os.Handler;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Timer timer;
    TimerTask timerTask;
    LinearLayout masterLayout = null;
    LinearLayout btnLayout = null;
    Button nextChopper = null;
    Button nextCamera = null;
    Button camUp = null;
    Button camDown = null;
    Button camIn = null;
    Button camOut = null;
	Button exit = null;

    private HeliGLSurfaceView mGLView;
    private World mWorld;

    private final Handler handler = new Handler();

    @Override
    public void onClick(View v) {
        if (v == nextChopper)
        {
            mWorld.nextChopper();
        }
        else if (v == nextCamera)
        {
            mWorld.toggleChaseCam();
        }
		else if (v == camOut)
		{
			mWorld.cameraFarther();
		}
		else if (v == camIn)
		{
			mWorld.cameraCloser();
		}
		else if (v == exit)
		{
			finishAffinity();
		}
        // implements your things
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        timer = new Timer();
        // TODO: Add intents to replace argument parsing from java main

        try {
            mWorld = new World();
			Toast.makeText(this,"World Created...",Toast.LENGTH_SHORT);
        } catch(Exception e)
        {
            Toast.makeText(this, "Exception when creating world...", Toast.LENGTH_SHORT);
            // Do nothing at this time
        }
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainLayout);
        masterLayout = LayoutTools.addLL(LayoutTools.MP,LayoutTools.MP,LayoutTools.getNextViewID(),LinearLayout.VERTICAL,layout,this);
        btnLayout = LayoutTools.addLL(LayoutTools.WC,LayoutTools.MP,LayoutTools.getNextViewID(),LinearLayout.HORIZONTAL,masterLayout,this);
        btnLayout.setGravity(Gravity.TOP);
        nextChopper = new Button(this);
        nextChopper.setId(LayoutTools.getNextViewID());
        nextChopper.setText("Next Chopper");
        nextChopper.setOnClickListener(this);
        nextCamera = new Button(this);
        nextCamera.setText("Next Camera");
        nextCamera.setId(LayoutTools.getNextViewID());
        nextCamera.setOnClickListener(this);
		camIn = new Button(this);
		camIn.setText("Camera In");
		camIn.setId(LayoutTools.getNextViewID());
		camIn.setOnClickListener(this);
		camOut = new Button(this);
		camOut.setText("Camera Out");
		camOut.setId(LayoutTools.getNextViewID());
		camOut.setOnClickListener(this);
		exit = new Button(this);
		exit.setText("Exit");
		exit.setId(LayoutTools.getNextViewID());
		exit.setOnClickListener(this);
        btnLayout.addView(nextChopper);
        btnLayout.addView(nextCamera);
		btnLayout.addView(camIn);
		btnLayout.addView(camOut);
		btnLayout.addView(exit);
        mGLView = new HeliGLSurfaceView(this, mWorld);
        mGLView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        masterLayout.addView(mGLView);
		mWorld.setSurface(mGLView);
		mGLView.requestRender();

		/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */

    }

    public void initializeTimerTask()
    {
        System.out.println("Starting Timer Task...");
        timerTask = new TimerTask() {
            public void run()
            {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            mWorld.tick();
                            Toast.makeText(getApplicationContext(),"World Time: " + mWorld.getTimestamp(), Toast.LENGTH_SHORT);
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(getApplicationContext(),"Unhandled Exception: " + e.getMessage(), Toast.LENGTH_SHORT);
                        }
                    }
                });
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // Start our timer

        startTimer();
    }

    public void startTimer()
    {
        initializeTimerTask();

        // World computes positions every 20ms of world time
		// You'll accelerate time by using a shorter delay here
        // We wait 2.5 seconds for world to be created etc.
        //timer.schedule(timerTask, 1500, 16);
        timer.schedule(timerTask, 2500, 4);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //timer.cancel();
    }
}
