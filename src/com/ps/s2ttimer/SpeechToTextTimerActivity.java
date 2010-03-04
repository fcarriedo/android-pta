package com.ps.s2ttimer;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Chronometer.OnChronometerTickListener;

public class SpeechToTextTimerActivity extends Activity {
    
	private static final String TAG = SpeechToTextTimerActivity.class.getSimpleName();
	
	private static final int MAX_ATTEMPTS = 3;
	
	private Chronometer chrono;
	
	private Button startButton;
	private Button stopButton;
	private Button resetButton;
	
	private TextView timerMsg;
	private TextView speechMsg;
	private TextView remainingTime;
	
	private TextToSpeech tts;
	private boolean readyToSpeak;
	
	private long cutofTime = 20;
	
	private int attempts;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        chrono = (Chronometer)findViewById(R.id.chronometer);
        
        timerMsg = (TextView)findViewById(R.id.time_msg);
        speechMsg = (TextView)findViewById(R.id.speech_recog_msg);
        
        startButton = (Button)findViewById(R.id.start);
        startButton.setOnClickListener(new Button.OnClickListener() {
        	@Override
        	public void onClick(View view) {
        		chrono.start();
        		addChronoTickListener();
        	}
        });
        
        stopButton = (Button)findViewById(R.id.stop);
        stopButton.setOnClickListener(new Button.OnClickListener() {
        	@Override
        	public void onClick(View view) {
        		chrono.stop();
        		clearChronoTickListener();
        	}
        });
        
        resetButton = (Button)findViewById(R.id.reset);
        resetButton.setOnClickListener(new Button.OnClickListener() {
        	@Override
        	public void onClick(View view) {
        		chrono.setBase(SystemClock.elapsedRealtime());
        		clearChronoTickListener();
        	}
        });
        
        remainingTime = (TextView)findViewById(R.id.remaining_time);
        remainingTime.setText("Remaining time : ");
        
        
        tts = new TextToSpeech(getApplicationContext(), new OnInitListener() {
        	@Override
        	public void onInit(int status) {
        		if(status == TextToSpeech.SUCCESS) {
        			readyToSpeak = true;
        		}
        	}
        });
    }
    
    public void speakAndHear(String textToSpeak, long sleepTime, boolean startSpeechRecognition) {
    	if(readyToSpeak) {
    		clearChronoTickListener();
    		Toast.makeText(this, "Speaking...", 5000).show();
    		tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
    		if (startSpeechRecognition) {
				sleep(4500);
				startVoiceRecognition();
			}
    	} else {
    		Toast.makeText(this, "Speech2Text not ready", 3000);
    	}
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	try {
			if(tts != null) {
				tts.shutdown();
				tts = null;
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
    }
    
    public void clearChronoTickListener() {
    	chrono.setOnChronometerTickListener(null);
    }
    
    public void addChronoTickListener() {
        chrono.setOnChronometerTickListener(new OnChronometerTickListener() {
        	long elapsedSeconds = 0;
        	
        	@Override
        	public void onChronometerTick(Chronometer chron) {
        		elapsedSeconds = (SystemClock.elapsedRealtime() - chrono.getBase()) / 1000;
				timerMsg.setText("Elapsed time : " + elapsedSeconds);
				remainingTime.setText("Remaining time : " + (cutofTime-elapsedSeconds) + " seconds.");
				if (elapsedSeconds % cutofTime == 0) {
					// Say something on text to speech or play little mp3/wav
					speakAndHear("Timer done. Do you need more time?", 2500, true);					
        		}
        	}
        });
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	// Save application state or clear state.   	
    }
    
    public void startVoiceRecognition() {
    	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    	startActivityForResult(intent, 0);	
    }
    
    @Override 
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	boolean isTimeSet = false;
    	boolean isUnitsSet = false;
    	String units = "minutes";
    	int multiplier = 0;
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 || resultCode == Activity.RESULT_OK) {
        	List<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        	for (String str : text) {
        		speechMsg.setText("You said : '" + str + "'");
				String[] parts = str.split(" ");
				for (String part : parts) {
					if (part.startsWith("yes")) {
						speakAndHear("How much time?", 2300, true);
						attempts = 0;
					} else if (part.startsWith("no")) {
						finishActivity();
					} else {
						try {
							cutofTime = Integer.parseInt(part);
							isTimeSet = true;
						} catch (NumberFormatException nfe) {
							if (part.startsWith("hour")) {
								isUnitsSet = true;
								multiplier = 60 * 60;
								units = "hours";
							} else if (part.startsWith("min")) {
								isUnitsSet = true;
								multiplier = 60;
								units = "minutes";
							} else if (part.startsWith("sec")) {
								isUnitsSet = true;
								multiplier = 1;
								units = "seconds";
							}
						}

						if (isTimeSet && isUnitsSet) {
							attempts = 0;
							break;
						}
					}
				}
			}
        	
        	if(isTimeSet && isUnitsSet) {
        		speakAndHear("Snoozing for " + cutofTime + " more " + units, 0, false);
        		cutofTime = cutofTime*multiplier;
        		addChronoTickListener();
        	} else {
        		if(attempts < MAX_ATTEMPTS) {
        			speakAndHear("Couldn't hear you. Can you tell me again?", 3000, true);
        		} else {
        			finishActivity();
        		}
        		attempts++;
        	}
        }
    }
    
    private void finishActivity() {
    	clearChronoTickListener();
    	speakAndHear("Finishing activity.", 0, false);
    }
    
    private void sleep(long millis) {
    	try {
			Thread.sleep(millis);
		} catch (Exception e) {
			// Do nothing.
		}
    }
}