package com.pigmal.android.fourbeat.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SelectStageActivity extends FourBeatBaseActivity implements OnClickListener {

	Button[] mButtons;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_stage_activity);
		
		mButtons = new Button[4];
		mButtons[0] = (Button)findViewById(R.id.button_red);
		mButtons[0].setOnClickListener(this);
		mButtons[1] = (Button)findViewById(R.id.button_blue);
		mButtons[1].setOnClickListener(this);
		mButtons[2] = (Button)findViewById(R.id.button_yellow);
		mButtons[2].setOnClickListener(this);
		mButtons[3] = (Button)findViewById(R.id.button_green);
		mButtons[3].setOnClickListener(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_red:
			handle(0);
			break;
		case R.id.button_blue:
			handle(1);
			break;
		case R.id.button_yellow:
			handle(2);
			break;
		case R.id.button_green:
			handle(3);
			break;
		default:break;
		}
	}

	@Override
	protected void onFourBeatConnected() {
		
	}

	@Override
	protected void onFourBeatStateChange(int id, int state) {
		if (state == BUTTON_STATE_ON) {
			handle(id);
		}
	}

	void handle(int buttonId) {
		String stage = "";
		switch (buttonId) {
		case 0:
			stage = "land";
			break;
		case 1:
			stage = "sea";
			break;
		case 2:
			stage = "mountain";
			break;
		case 3:
			stage = "sky";
			break;
		default:
			break;
		}
		Intent i = new Intent(this, MainActivity.class);
		i.putExtra("stage", stage);
		startActivity(i);
	}
}
