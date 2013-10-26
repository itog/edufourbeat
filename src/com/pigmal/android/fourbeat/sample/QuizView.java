package com.pigmal.android.fourbeat.sample;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient.CustomViewCallback;

public class QuizView extends View{

	private Handler mHander = new Handler();
	
	private int currentQuizNo = 0;
	
	private String[][] stage =
		{
			{
				"uma_sample.jpg",
				"android_1.gif",
				"android_2.jpg",
				"horse.mp3",
				"android_3.jpg",
			}
	};
	
	private String[] answers = {
			"馬",
			"象",
			"猫",
			"犬"
	};
	
	private String currentAnswer = "馬";
	
	private Bitmap currentBitmap = null;
	private Runnable currentTask;
	private int currentPanelQuestion = 0;
	private QuizViewListener listener = null;

	private static final int DELAY_MS = 3000;
	
	public QuizView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * init stage
	 * assetからファイル読み込み
	 * @param stage
	 */
	public void initStage(String stage) {
		currentQuizNo = 0;
	}
	
	private void playHint(String filename){
		String extension = null;
		int point = filename.lastIndexOf(".");
	    if (point != -1) {
	        extension = filename.substring(point + 1);
	    }
	    
	    if ("gif".equals(extension) || "jpg".equals(extension)){
	    	InputStream is;
			try {
				is = getResources().getAssets().open(filename);
		        currentBitmap = BitmapFactory.decodeStream(is);
		        Log.d("Quiz","画像設定 "+filename);
			} catch (IOException e) {
				Log.e("Quiz", "ファイル読み込み失敗", e);
				e.printStackTrace();
			}
	    }else if ("mp3".equals(extension)){
	    	try {
				AssetFileDescriptor afd = getContext().getAssets().openFd(filename);
		    	MediaPlayer mp = new MediaPlayer();
		    	mp.setDataSource(afd.getFileDescriptor());
		    	mp.prepare();
		    	mp.start();
		    	Log.d("Quiz","音声再生 "+filename);
			} catch (IOException e) {
				Log.e("Quiz", "音声ファイル読み込み失敗");
				e.printStackTrace();
			}
	    }
	}

	/**
	 * クイズ開始
	 * 何問目かをパラメータで渡す。
	 * ステージ(海とか陸とか)は固定
	 * 
	 * @param quizNo ステージの何問目か
	 */
	public void start(int quizNo){
		initStage(null);
		currentQuizNo = quizNo;
		currentPanelQuestion = 0; // 問題の一枚目の画像にセット
		currentAnswer = answers[quizNo];
		playHint(getCurrentFilename());
		
		refreash();
		setNextHintTimer();
	}
	
	/**
	 * Stop Quiz
	 */
	public void stop(){
		if (currentTask != null){
			mHander.removeCallbacks(currentTask);
		}
	}
	
	/**
	 * Resume Quiz
	 */
	public void resume(){
		if (currentTask != null){
			mHander.postDelayed(currentTask, DELAY_MS);
		}
	}
	
	private void setNextHintTimer(){
		currentTask = new Runnable() {
			@Override
			public void run() {
				currentPanelQuestion++;
				
				if (currentPanelQuestion >= stage[currentQuizNo].length){
					currentPanelQuestion = 0;
					if (listener != null){
						listener.quizFinished();
					}
				}else{
					refreash();
					setNextHintTimer();
				}
			}
		};
		mHander.postDelayed(currentTask, DELAY_MS);
	}
	
	private void refreash(){
		
		String currentFilename = getCurrentFilename();
		playHint(currentFilename);
		
		invalidate();
	}
	
	private String getCurrentFilename(){
		return stage[currentQuizNo][currentPanelQuestion];
	}
	
	@Override
	protected void onDraw(Canvas canvas) {	
		super.onDraw(canvas);
		
		if (currentBitmap != null){
			int w = currentBitmap.getWidth();
			int h = currentBitmap.getHeight();
			Rect src = new Rect(0, 0, currentBitmap.getWidth(), currentBitmap.getHeight());
			Rect dst = new Rect(0, 0,getWidth(),getHeight());
			canvas.drawBitmap(currentBitmap, src, dst, null);
		}

	}
	public QuizViewListener getListener() {
		return listener;
	}

	/**
	 * 正解かどうか
	 * @param answerList
	 * @return
	 */
	public boolean answer(List<String> answerList){
		for(String ans:answerList){
			if (ans.contains(currentAnswer)){
				return true;
			}
		}
		return false;
	}

	public void setListener(QuizViewListener listener) {
		this.listener = listener;
	}
}
