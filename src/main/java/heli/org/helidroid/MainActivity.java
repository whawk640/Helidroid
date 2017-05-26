package heli.org.helidroid;

import android.os.*;
import android.support.v7.app.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Timer timer;
    TimerTask timerTask;
	// TODO: Consider screen rotation and/or phones
    LinearLayout masterLayout = null; // Vertical
    LinearLayout btnLayout = null; // Horizonta
	LinearLayout glLayout = null; // Horizontal
	LinearLayout panelLayout = null; // Vertical
    Button nextChopper = null;
    Button nextCamera = null;
	Button pauseResume = null;
    Button camUp = null;
    Button camDown = null;
    Button camIn = null;
    Button camOut = null;
	Button wireFrame = null;
	Button exit = null;
	boolean worldPaused = false;
	boolean panelsCreated = false;

    private HeliGLSurfaceView mainGLView = null;
	private HeliGLSurfaceView apGLView = null;
	private HeliGLSurfaceView danGLView = null;
	
    private World mWorld = null;
	
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
		else if (v == pauseResume)
		{
			if (worldPaused == false)
			{
				((Button)v).setText(R.string.btn_resume);
				mWorld.pause();
				worldPaused = true;
			}
			else
			{
				((Button)v).setText(R.string.btn_pause);
				mWorld.resume();
				worldPaused = false;
			}
		}
		else if (v == camOut)
		{
			mWorld.cameraFarther();
		}
		else if (v == camIn)
		{
			mWorld.cameraCloser();
		}
		else if (v == camUp)
		{
			mWorld.cameraUp();
		}
		else if (v == camDown)
		{
			mWorld.cameraDown();
		}
		else if (v == wireFrame)
		{
			mWorld.toggleWireFrame();
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
		worldPaused = false;
       // setContentView(R.layout.content_main);
        // TODO: Add intents to replace argument parsing from java main

        //RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainLayout);
        masterLayout = LayoutTools.addLL(LayoutTools.getNextViewID(),LinearLayout.VERTICAL,this);
		setContentView(masterLayout);
        btnLayout = LayoutTools.addLL(1.0f,LayoutTools.getNextViewID(),LinearLayout.HORIZONTAL,masterLayout,this);
		glLayout = LayoutTools.addLL(10.0f,LayoutTools.getNextViewID(), LinearLayout.HORIZONTAL,masterLayout,this);
        btnLayout.setGravity(Gravity.TOP);
		nextChopper = LayoutTools.addWidget(new Button(this),1.0f,LayoutTools.getNextViewID(),btnLayout);
        nextChopper.setText(R.string.btn_nxtChop);
        nextChopper.setOnClickListener(this);
		nextCamera = LayoutTools.addWidget(new Button(this),1.0f, LayoutTools.getNextViewID(),btnLayout);
        nextCamera.setText(R.string.btn_nxtCam);
        nextCamera.setOnClickListener(this);
		pauseResume = LayoutTools.addWidget(new Button(this), 1.0f, LayoutTools.getNextViewID(),btnLayout);
		pauseResume.setText(R.string.btn_pause);
		pauseResume.setOnClickListener(this);
		/* 
		camUp = LayoutTools.addWidget(new Button(this), 1.0f, LayoutTools.MP, LayoutTools.WC,0,btnLayout);
		camUp.setText(R.string.btn_camUp);
		camUp.setOnClickListener(this);
		camDown = LayoutTools.addWidget(new Button(this), 1.0f, LayoutTools.MP, LayoutTools.WC,0,btnLayout);
		camDown.setText(R.string.btn_camDown);
		camDown.setOnClickListener(this);
		camIn = LayoutTools.addWidget(new Button(this), 1.0f, LayoutTools.MP, LayoutTools.WC,0,btnLayout);
		camIn.setText(R.string.btn_camIn);
		camIn.setOnClickListener(this);
		camOut = LayoutTools.addWidget(new Button(this), 1.0f, LayoutTools.MP, LayoutTools.WC,0,btnLayout);
		camOut.setText(R.string.btn_camOut);
		camOut.setOnClickListener(this); */
		wireFrame = LayoutTools.addWidget(new Button(this), 1.0f, LayoutTools.getNextViewID(),btnLayout);
		wireFrame.setText(R.string.btn_togWF);
		wireFrame.setOnClickListener(this);
		exit = LayoutTools.addWidget(new Button(this), 1.0f, LayoutTools.getNextViewID(),btnLayout);
		exit.setText(R.string.btn_exit);
		exit.setOnClickListener(this);
        try
		{
            mWorld = new World();
			Toast.makeText(this,"World Created...",Toast.LENGTH_SHORT);
        } catch(Exception e)
        {
            Toast.makeText(this, "Exception when creating world...", Toast.LENGTH_SHORT);
            // Do nothing at this time
        }
		
		mainGLView = LayoutTools.addWidget(new HeliGLSurfaceView(this, mWorld), 4.0f,LayoutTools.getNextViewID(),glLayout);
		panelLayout = LayoutTools.addLL(1.0f,LayoutTools.getNextViewID(),LinearLayout.VERTICAL,glLayout,this);
		apGLView = LayoutTools.addWidget(new HeliGLSurfaceView(this, mWorld), 1.0f, LayoutTools.getNextViewID(),panelLayout);
		danGLView = LayoutTools.addWidget(new HeliGLSurfaceView(this, mWorld), 1.0f, LayoutTools.getNextViewID(),panelLayout);
		
		mWorld.setPanelLayout(panelLayout);		
		mWorld.addSurface(mainGLView);
		apGLView.setCameraMode(HeliGLSurfaceView.MODE_CHASE);
		danGLView.setCameraMode(HeliGLSurfaceView.MODE_CHASE);
		apGLView.setChopper(0);
		danGLView.setChopper(1);
		mWorld.addSurface(apGLView);
		mWorld.addSurface(danGLView);
		timer = new Timer();
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
							/* if (panelsCreated == false)
							{
								mWorld.addPanels();
								panelsCreated = true;
							} */
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
        timer.schedule(timerTask, 5000, 4);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //timer.cancel();
    }
}
