package com.hoyoji.hoyoji.setting;

import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.fragment.HyjFragment;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.UserData;

public class BindPhoneFragment extends HyjFragment {
	private HyjTextField mTextViewPhone = null;
	private HyjTextField mTextViewAuthCode = null;
	private Button mButtonSendAuthCode = null;

	@Override
	public Integer useContentView() {
		return R.layout.setting_fragment_bindphone;
	}

	@Override
	public void onInitViewData() {
		mTextViewPhone = (HyjTextField) getView().findViewById(R.id.bindPhoneFragment_textField_phone);
		mTextViewAuthCode = (HyjTextField) getView().findViewById(R.id.bindPhoneFragment_textField_authCode);
		mTextViewAuthCode.setEnabled(false);
		
		mButtonSendAuthCode = (Button) getView().findViewById(R.id.bindPhoneFragment_button_sendAuthCode);
		final TimeCount time = new TimeCount(60000, 1000);
		mButtonSendAuthCode.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mTextViewAuthCode.setEnabled(true);
						sendAuthCodeToPhone();
						time.start();
					}
				});

		getView().findViewById(R.id.bindPhoneFragment_button_submit).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						bindPhone_submit(v);
					}
				});

		this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}

	protected void sendAuthCodeToPhone() {
		// TODO Auto-generated method stub
		
	}

	private void fillData() {

	}

    private void bindPhone_submit(View v) {
       if(mTextViewPhone.getText() != null || mTextViewPhone.getText().length() != 0){
    	   UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
    	   HyjModelEditor<UserData> userDataEditor = userData.newModelEditor();
    	   userDataEditor.getModelCopy().setPhone(mTextViewPhone.getText().toString().trim());
    	   userDataEditor.save();
       }
	}

    private class TimeCount extends CountDownTimer{

    	public TimeCount(long millisInFuture, long countDownInterval) {
    		super(millisInFuture, countDownInterval);//参数为总时长，和计时的时间间隔
    		// TODO Auto-generated constructor stub
    	}

    	@Override
    	public void onFinish() {
    		// TODO Auto-generated method stub
    		mButtonSendAuthCode.setText("重新发送验证码");
    		mButtonSendAuthCode.setClickable(true);
    	}

    	@Override
    	public void onTick(long millisUntilFinished) {
    		// TODO Auto-generated method stub
    		mButtonSendAuthCode.setClickable(false);
    		mButtonSendAuthCode.setText(millisUntilFinished/1000 + "秒后可重新发送");
    	}

    }

    
}


