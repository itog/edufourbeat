package com.pigmal.android.fourbeat.sample;

import java.util.ArrayList;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FourBeatBaseActivity implements OnClickListener {
	protected static final String TAG = "ServiceSample";
	
	TextView[] mTextViews; //TODO ゲージ画像に差し替え
	private QuizView mQuizView;
	int[] mPoints = {0, 0, 0, 0};
	int[] mCorrectPoints = {0, 0, 0, 0};
	
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
		
		mGameState = GAME_STATE.GAUGE; // GAME_STATE.IDLE;
		mQuizView.start(0); // 0 = ステージ1の1問目
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
			}
		});
	}

	private void updateUi(int id) {
		mTextViews[id].setText(String.valueOf(mPoints[id]));
		
		//TODO ゲージ
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
	static final int GAUGE_MAX = 5;

	void handleEvent(final int id) {
		Log.v(TAG, "handle " + id);
		switch (mGameState) {
		case IDLE:
			mQuizView.start(mRound); // 0 = ステージ1の1問目
			break;
		case GAUGE:
			mPoints[id] += 1;
			if (mPoints[id] == GAUGE_MAX) {
				mGameState = GAME_STATE.ANSWER;
				startAnswer(id);
				
			} else if (mPoints[id] > GAUGE_MAX){
				//ignore
			}
			break;
		case ANSWER:
			break;
		default:
			break;
		}
		updateUi(id);
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
				mRound++;
				if (mRound > mQuizView.getStageCount()) {
					finish();
				}
			} else {
				// 誤答であれば再開
				resumeGame();
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
		mQuizView.start(mRound);
	}

    boolean checkAnswer(List<String> answers) {
    	return mQuizView.answer(answers);
    }
}
