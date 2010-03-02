package com.ps.s2ttimer;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Chronometer.OnChronometerTickListener;

public class SpeechToTextTimerActivity extends Activity {
    /** Called when the activity is first created. */
	
	private Chronometer chrono;
	
	private Button startButton;
	private Button stopButton;
	private Button resetButton;
	
	private TextView timerMsg;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        chrono = (Chronometer)findViewById(R.id.chronometer);
        chrono.setOnChronometerTickListener(new OnChronometerTickListener() {
        	@Override
        	public void onChronometerTick(Chronometer chron) {
        		timerMsg.setText("Elapsed time : " + (SystemClock.elapsedRealtime()-chrono.getBase()));
        	}
        });
        
        timerMsg = (TextView)findViewById(R.id.time_msg);
        
        startButton = (Button)findViewById(R.id.start);
        startButton.setOnClickListener(new Button.OnClickListener() {
        	@Override
        	public void onClick(View view) {
        		chrono.start();
        	}
        });
        
        stopButton = (Button)findViewById(R.id.stop);
        stopButton.setOnClickListener(new Button.OnClickListener() {
        	@Override
        	public void onClick(View view) {
        		chrono.stop();
        	}
        });
        
        resetButton = (Button)findViewById(R.id.reset);
        resetButton.setOnClickListener(new Button.OnClickListener() {
        	@Override
        	public void onClick(View view) {
        		chrono.setBase(SystemClock.elapsedRealtime());
        	}
        });
    }
    
    @Override
    public void onPause() {
    	// Save application state or clear state.   	
    }
}