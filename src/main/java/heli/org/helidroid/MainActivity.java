package heli.org.helidroid;

import android.os.Bundle;
import android.os.Handler;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Timer timer;
    TimerTask timerTask;

    private HeliGLSurfaceView mGLView;
    private World mWorld;

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        timer = new Timer();
        // TODO: Add intents to replace argument parsing from java main

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainLayout);
        mGLView = new HeliGLSurfaceView(this);
        mGLView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(mGLView);

		/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */
        try {
            mWorld = new World(mGLView);
			Toast.makeText(this,"World Created...",Toast.LENGTH_SHORT);
        } catch(Exception e)
        {
            Toast.makeText(this, "Exception when creating world...", Toast.LENGTH_SHORT);
            // Do nothing at this time
        }

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

        // Fastest we hope for is 60 frames per second
        // We wait 1.5 seconds for world to be created etc.
        //timer.schedule(timerTask, 1500, 16);
        timer.schedule(timerTask, 1500, 100);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //timer.cancel();
    }
}
