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
	private int mCurrentQuizNo = 0;
	
	/**
	 * ステージ情報
	 * jpg gif mp3が設定可能
	 */
	private String[][] mStage =
		{
			{
				"uma1.jpg",
				"uma2.jpg",
				"uma3.jpg",
				"uma4.jpg",
				"uma.mp3",
				"uma5.jpg",
			},
			{
				"inu1.jpg",
				"inu2.jpg",
				"inu3.jpg",
				"inu4.jpg",
				"inu.mp3",
				"inu5.jpg",
			},
			{
				"elephant1.jpg",
				"elephant2.jpg",
				"elephant3.jpg",
				"elephant4.jpg",
				"zou.mp3",
				"elephant5.jpg",
			},
			{
				"neko1.jpg",
				"neko2.jpg",
				"neko3.jpg",
				"neko4.jpg",
				"cat.mp3",
				"neko5.jpg",
			},
	};
	
	private String[] mAnswers = {
			"馬",
			"犬",
			"象",
			"猫",
	};
	
	private String mCurrentAnswer = "馬";
	private Bitmap mCurrentBitmap = null;
	private Bitmap mCorrectBitmap = null;
	private Bitmap mIncorrectBitmap = null;
	private Bitmap mTimeoutBitmap = null;
	private Runnable mCurrentTask;
	private int mCurrentPanelQuestion = 0;
	private QuizViewListener mListener = null;
	private boolean mIsCorrect = false;
	private boolean mIsMistake = false;
	private boolean mIsTimeout = false;
	
	private static final int DELAY_MS = 3000;

	public QuizView(Context context, AttributeSet attrs) {
		super(context, attrs);
				
		try {
			InputStream is = getResources().getAssets().open("correct.png");
			mCorrectBitmap = BitmapFactory.decodeStream(is);
			
			is = getResources().getAssets().open("incorrect.png");
			mIncorrectBitmap = BitmapFactory.decodeStream(is);
			
			is = getResources().getAssets().open("timeout.png");
			mTimeoutBitmap = BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			Log.e("Quiz","正解画像読み込み失敗");
			e.printStackTrace();
		}
	}

	/**
	 * init stage
	 * assetからファイル読み込み
	 * @param stage
	 */
	public void initStage(String stage) {
		mCurrentQuizNo = 0;
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
		        mCurrentBitmap = BitmapFactory.decodeStream(is);
		        Log.d("Quiz","画像設定 "+filename);
			} catch (IOException e) {
				Log.e("Quiz", "ファイル読み込み失敗", e);
				e.printStackTrace();
			}
	    }else if ("mp3".equals(extension)){
	    	playSound(filename);
	    }
	}
	
	/**
	 * 音声ファイルを再生する。
	 * @param filename
	 */
	private void playSound(String filename){
		try {
			Log.d("Quiz","音声再生 "+filename);
			if (filename.equals("uma.mp3")){
				MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.uma);
				mp.start();
				return;
			}else if (filename.equals("zou.mp3")){
				MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.zou);
				mp.start();
				return;
			}else if (filename.equals("inu.mp3")){
				MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.inu);
				mp.start();
				return;
			}else if (filename.equals("cat.mp3")){
				MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.cat);
				mp.start();
				return;
			}
			AssetFileDescriptor afd = getContext().getAssets().openFd(filename);
	    	MediaPlayer mp = new MediaPlayer();
	    	mp.setDataSource(afd.getFileDescriptor());
	    	mp.prepare();
	    	mp.start();
	    	
	
		} catch (IOException e) {
			Log.e("Quiz", "音声ファイル読み込み失敗",e);
			e.printStackTrace();
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
		if (quizNo >= mStage.length){
			Log.d("Quiz","存在しないステージが指定されました。");
		}
		Log.d("Quiz","QuizView.start");
		initStage(null);
		mCurrentQuizNo = quizNo;
		mCurrentPanelQuestion = 0; // 問題の一枚目の画像にセット
		mCurrentAnswer = mAnswers[quizNo];
		mIsTimeout = false;
		mIsCorrect = false;
		playHint(getCurrentFilename());
		
		refreash();
		setNextHintTimer();
	}
	
	/**
	 * Stop Quiz
	 */
	public void stop(){
		Log.d("Quiz","QuizView.stop");
		if (mCurrentTask != null){
			mHander.removeCallbacks(mCurrentTask);
		}
	}
	
	/**
	 * Resume Quiz
	 */
	public void resume(){
		Log.d("Quiz","QuizView.resume");
		if (mIsMistake){
			mIsMistake = false;
			invalidateByUIThread();
		}

		if (mCurrentTask != null){
			mHander.postDelayed(mCurrentTask, DELAY_MS);
		}
	}
	
	private void setNextHintTimer(){
		if (mCurrentTask != null){
			mHander.removeCallbacks(mCurrentTask);
		}
		
		mCurrentTask = new Runnable() {
			@Override
			public void run() {
				mCurrentPanelQuestion++;
				
				if (mCurrentPanelQuestion >= mStage[mCurrentQuizNo].length){
					mCurrentPanelQuestion = 0;
					if (mListener != null){
						mListener.quizFinished();
					}
					mIsTimeout = true;
					invalidateByUIThread();
				}else{
					refreash();
					setNextHintTimer();
				}
			}
		};
		mHander.postDelayed(mCurrentTask, DELAY_MS);
	}
	
	private void refreash(){
		
		String currentFilename = getCurrentFilename();
		playHint(currentFilename);
		
		invalidateByUIThread();
		
	}
	
	private String getCurrentFilename(){
		return mStage[mCurrentQuizNo][mCurrentPanelQuestion];
	}
	
	@Override
	protected void onDraw(Canvas canvas) {	
		super.onDraw(canvas);
		Log.d("Quiz","再描画 start");
		
		if (mCurrentBitmap != null){
			int w = mCurrentBitmap.getWidth();
			int h = mCurrentBitmap.getHeight();
			Rect src = new Rect(0, 0, w, h);
			Rect dst = new Rect(0, 0,getWidth(),getHeight());
			canvas.drawBitmap(mCurrentBitmap, src, dst, null);
		}
		
		if (mIsCorrect){
			Log.d("Quiz", "正解描画");
			Rect src = new Rect(0, 0, mCorrectBitmap.getWidth(), mCorrectBitmap.getHeight());
			Rect dst = new Rect(0, 0,getWidth(),getHeight());
			canvas.drawBitmap(mCorrectBitmap, src, dst, null);
		}
		
		if (mIsMistake){
			Log.d("Quiz", "不正解描画");
			Rect src = new Rect(0, 0, mIncorrectBitmap.getWidth(), mIncorrectBitmap.getHeight());
			Rect dst = new Rect(0, 0,getWidth(),getHeight());
			canvas.drawBitmap(mIncorrectBitmap, src, dst, null);
		}
		
		if (mIsTimeout){
			Log.d("Quiz", "タイムアウト");
			Rect src = new Rect(0, 0, mTimeoutBitmap.getWidth(), mTimeoutBitmap.getHeight());
			Rect dst = new Rect(0, 0,getWidth(),getHeight());
			canvas.drawBitmap(mTimeoutBitmap, src, dst, null);
		}
		
		Log.d("Quiz","再描画 end");
	}
	public QuizViewListener getListener() {
		return mListener;
	}
	
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		/*
		if(visibility == View.INVISIBLE){
			if (mCurrentTask != null){
				mHander.removeCallbacks(mCurrentTask);
			}
		}*/
		Log.d("Quiz","onVisivilityChanged visivility="+visibility);
	}

	/**
	 * 正解かどうか
	 * @param answerList
	 * @return
	 */
	public boolean answer(List<String> answerList){
		for(String ans:answerList){
			if (ans.contains(mCurrentAnswer)){
				Log.d("Quiz","正解 正解="+mCurrentAnswer+" 入力="+ans);
				mIsCorrect = true;
				invalidateByUIThread();
				 
				return true;
			}
		}
		mIsMistake = true;
		invalidateByUIThread();
		return false;
	}
	
	private void invalidateByUIThread(){
		mHander.post(new Runnable() {
			@Override
			public void run() {
				invalidate();
			}
		});
	}

	public void setListener(QuizViewListener listener) {
		this.mListener = listener;
	}
	/**
	 * 現在のステージが何問あるかを返す。
	 * @return
	 */
	public int getQuizSize(){
		return mStage.length;
	}
}
