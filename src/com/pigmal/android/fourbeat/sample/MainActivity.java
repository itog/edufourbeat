package com.pigmal.android.fourbeat.sample;

import java.util.ArrayList;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FourBeatBaseActivity implements OnClickListener {
	protected static final String TAG = "ServiceSample";

	int mGaugeResources[][];
	static final int[] RES_RED = {R.drawable.red0, R.drawable.red1, R.drawable.red2, R.drawable.red3, R.drawable.red4, R.drawable.red5};
	static final int[] RES_BLUE = {R.drawable.blue0, R.drawable.blue1, R.drawable.blue2, R.drawable.blue3, R.drawable.blue4, R.drawable.blue5};
	static final int[] RES_YELLOW = {R.drawable.yellow0, R.drawable.yellow1, R.drawable.yellow2, R.drawable.yellow3, R.drawable.yellow4, R.drawable.yellow5};
	static final int[] RES_GREEN = {R.drawable.green0, R.drawable.green1, R.drawable.green2, R.drawable.green3, R.drawable.green4, R.drawable.green5};

	TextView[] mTextViews; //TODO ゲージ画像に差し替え
	private QuizView mQuizView;
	int[] mPoints = {0, 0, 0, 0};
	int[] mCorrectPoints = {0, 0, 0, 0};
	private Handler mHander = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_activity);
		
		mQuizView = (QuizView) findViewById(R.id.quizview);
		
		mTextViews = new TextView[4];
		mTextViews[0] = (TextView)findViewById(R.id.gauge0);
		mTextViews[0].setOnClickListener(this);
		mTextViews[1] = (TextView)findViewById(R.id.gauge1);
		mTextViews[1].setOnClickListener(this);
		mTextViews[2] = (TextView)findViewById(R.id.gauge2);
		mTextViews[2].setOnClickListener(this);
		mTextViews[3] = (TextView)findViewById(R.id.gauge3);
		mTextViews[3].setOnClickListener(this);
		
		mGameState = GAME_STATE.IDLE;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		// テスト開始
		
		mQuizView.setListener(new QuizViewListener() {
			@Override
			public void quizFinished() {
				// 正解せずクイズ終了
				Log.d("Quiz","クイズ終了");
				mHander.postDelayed(new Runnable() {
					@Override
					public void run() {
						startNext();						
					}
				}, 5000);
			}
		});
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mQuizView.stop();
	}
	
	private void updateUi(final int id) {
		int[] reses_org = RES_RED;
		switch (id) {
		case 0:
			reses_org = RES_RED;
			break;
		case 1:
			reses_org = RES_BLUE;
			break;
		case 2:
			reses_org = RES_YELLOW;
			break;
		case 3:
			reses_org = RES_GREEN;
			break;
		default:
			break;
		}
		
		final int[] reses = reses_org; 
		
		if (mPoints[id] < 10) {
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mTextViews[id].setBackgroundResource(reses[0]);
				}
			});
		} else if (mPoints[id] == 10) {
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mTextViews[id].setBackgroundResource(reses[1]);
				}
			});
		} else if (mPoints[id] == 20) {
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mTextViews[id].setBackgroundResource(reses[2]);
				}
			});
		} else if (mPoints[id] == 30) {
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mTextViews[id].setBackgroundResource(reses[3]);
				}
			});
		} else if (mPoints[id] == 40) {
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mTextViews[id].setBackgroundResource(reses[4]);
				}
			});
		} else if (mPoints[id] > 50) {
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mTextViews[id].setBackgroundResource(reses[5]);
				}
			});
		}
	}

	@Override
	protected void onFourBeatConnected() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onFourBeatStateChange(final int id, final int state) {
		if (state == BUTTON_STATE_ON) {
			handleEvent(id);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.gauge0:
			handleEvent(0);
			break;
		case R.id.gauge1:
			handleEvent(1);
			break;
		case R.id.gauge2:
			handleEvent(2);
			break;
		case R.id.gauge3:
			handleEvent(3);
			break;
		default:
			break;
		}
	}

	int mRound = 0;
	enum GAME_STATE {IDLE, GAUGE, ANSWER};
	GAME_STATE mGameState = GAME_STATE.IDLE;
	static final int GAUGE_MAX = 51;

	void handleEvent(final int id) {
		Log.v(TAG, "handle " + id);
		switch (mGameState) {
		case IDLE:
			if (mRound >= mQuizView.getQuizSize()) {
				finish();
			} else {
				startNext();
				mGameState = GAME_STATE.GAUGE;
			}
			break;
		case GAUGE:
			mPoints[id] += 1;
			if (mPoints[id] == GAUGE_MAX) {
				mGameState = GAME_STATE.ANSWER;
				startAnswer(id);				
			} else if (mPoints[id] > GAUGE_MAX){
				//ignore
			}
			updateUi(id);
			break;
		case ANSWER:
			break;
		default:
			break;
		}
	}

	// player id が回答権取得。音声認識スタート
	void startAnswer(int id) {
		mQuizView.stop(); // ヒントの進行をpause
		callRecognizer();
	}

	static final int REQUEST_CODE = 1234;
	void callRecognizer() {
        try {
            // インテント作成
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // ACTION_WEB_SEARCH
            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    "Shout the Answer!");
            
            // インテント発行
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // このインテントに応答できるアクティビティがインストールされていない場合
            Toast.makeText(this, "ActivityNotFoundException", Toast.LENGTH_LONG).show();
        }
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.v(TAG, "result " + requestCode + ", " + resultCode);
        // 自分が投げたインテントであれば応答する
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            String resultsString = "";
            
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            
            for (int i = 0; i< results.size(); i++) {
                resultsString += results.get(i);
            }
            Toast.makeText(this, resultsString, Toast.LENGTH_LONG).show();

			if (checkAnswer(results)) {
				// TODO 正解！ポイント追加？
				mGameState = GAME_STATE.IDLE;
			} else {
				// 誤答であれば間違い画像が表示されるので、2秒後に再開
				mHander.postDelayed(new Runnable() {
					@Override
					public void run() {
						resumeGame();
					}
				},2000);
            }
        } else {
        	resumeGame();
        }        
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    // ゲームに戻る
    void resumeGame() {
    	mGameState = GAME_STATE.GAUGE;

		// ヒントの進行再開
		mQuizView.resume();
    }

	void startNext() {
		for (int i = 0; i < mPoints.length; i++) {
			mPoints[i] = 0;
			updateUi(i);
		}
		mQuizView.start(mRound);
		mRound++;
	}

    boolean checkAnswer(List<String> answers) {
    	return mQuizView.answer(answers);
    }
}
