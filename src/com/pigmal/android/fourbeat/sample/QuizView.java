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
	
	private String[] stage1 = {
				"android_1.gif",
				"android_2.jpg",
				"android_3.jpg",
	};
	
	private String[] answers = {
			"犬",
			"猫"
	};
	
	private MediaPlayer mediaPlayer;
	
	private String currentAnswer = "馬";
	
	private List<Bitmap> bitmapList = new ArrayList<Bitmap>();
	
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
		for(String filename:stage1){
			Log.d("Quiz","ファイル読み込み " + filename );
			String extension = null;
			int point = filename.lastIndexOf(".");
		    if (point != -1) {
		        extension = filename.substring(point + 1);
		    }
		    
		    if ("gif".equals(extension) || "jpg".equals(extension)){
		    	InputStream is;
				try {
					is = getResources().getAssets().open(filename);
			        Bitmap bm = BitmapFactory.decodeStream(is);
			        bitmapList.add(bm);
				} catch (IOException e) {
					Log.e("Quiz", "ファイル読み込み失敗", e);
					e.printStackTrace();
				}
		    }
		}
		
		// Sounds
		try {
			AssetFileDescriptor afd = getContext().getAssets().openFd("hourse.mp3");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Start Quiz
	 * @param stage
	 */
	public void start(String stage){
		initStage(stage);
		currentPanelQuestion = 0;
		currentBitmap = bitmapList.get(0);
		
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
				
				if (currentPanelQuestion >= bitmapList.size()){
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
		currentBitmap = bitmapList.get(currentPanelQuestion);
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {	
		super.onDraw(canvas);
		
		if (currentBitmap != null){
			int w = currentBitmap.getWidth();
			int h = currentBitmap.getHeight();
			Rect src = new Rect(0, 0, currentBitmap.getWidth(), currentBitmap.getHeight());
			Rect dst = new Rect(0, 200, (int)(w*0.5), 200 + (int)(h*0.5));
			canvas.drawBitmap(currentBitmap, src, dst, null);
		}

	}
	public QuizViewListener getListener() {
		return listener;
	}

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

	
	private void playSound(){
		
	}
}
