package com.hoyoji.hoyoji.setting;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjFragment;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.UserData;

public class BindPhoneFragment extends HyjFragment {
	private HyjTextField mTextViewPhone = null;
	private HyjTextField mTextViewAuthCode = null;
	private Button mButtonSendAuthCode = null;
	private String mAuthCodeFromServer = null;
	private String mPhoneText = null;
	private String mAuthCodeText = null;

	@Override
	public Integer useContentView() {
		return R.layout.setting_fragment_bindphone;
	}

	@Override
	public void onInitViewData() {
		Intent intent = getActivity().getIntent();
		String clickType = intent.getStringExtra("clickType");
		
		mTextViewPhone = (HyjTextField) getView().findViewById(R.id.bindPhoneFragment_textField_phone);
		if(clickType != null && clickType.equalsIgnoreCase("unBindPhone")){
			mTextViewPhone.setText(HyjApplication.getInstance().getCurrentUser().getUserData().getPhone());
			mTextViewPhone.setEnabled(false);
		}else{
			 SIMCardInfo siminfo = new SIMCardInfo(this.getActivity());
			 mTextViewPhone.setText(siminfo.getNativePhoneNumber());
		}
		
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

		
		
		if(clickType != null && clickType.equalsIgnoreCase("unBindPhone")){
			((Button) getView().findViewById(R.id.bindPhoneFragment_button_submit)).setText("解绑");
			getView().findViewById(R.id.bindPhoneFragment_button_submit).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					unBindPhone_submit(v);
				}
			});
		}else{
			getView().findViewById(R.id.bindPhoneFragment_button_submit).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					bindPhone_submit(v);
				}
			});
		}
		
		

		this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}

	protected void sendAuthCodeToPhone() {
		mAuthCodeFromServer = Double.toString((Math.random() + 0.1)*10000).substring(0, 4);
		SmsManager smsManager = SmsManager.getDefault(); 
		smsManager.sendTextMessage(mTextViewPhone.getText().toString().trim(), null, mAuthCodeFromServer, null, null); 
	}

	private void fillData() {
		mPhoneText = mTextViewPhone.getText();
		mAuthCodeText = mTextViewAuthCode.getText();
	}
	
	public boolean validateData(){
		boolean valiatePass = true;
		fillData();
		if(mPhoneText.length() == 0){
			mTextViewPhone.setError(getString(R.string.bindPhoneFragment_editText_hint_phone));
	   		valiatePass = false;
		}else {
			mTextViewPhone.setError(null);
	   	}
		
		if(mAuthCodeText.length() != 4 || !mAuthCodeText.equalsIgnoreCase(mAuthCodeFromServer)){
			mTextViewAuthCode.setError(getString(R.string.bindPhoneFragment_editText_validate_error_authCode));
	   		valiatePass = false;
		}else {
			mTextViewAuthCode.setError(null);
	   	}
		return valiatePass;
	}

    private void bindPhone_submit(View v) {
       if(!validateData()){
    	   HyjUtil.displayToast(R.string.app_validation_error);
       }else{
    	   UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
    	   HyjModelEditor<UserData> userDataEditor = userData.newModelEditor();
    	   userDataEditor.getModelCopy().setPhone(mTextViewPhone.getText().toString().trim());
    	   userDataEditor.save();
    	   getActivity().finish();
       }
	}

    private void unBindPhone_submit(View v) {
		// TODO Auto-generated method stub
    	if(!validateData()){
     	   HyjUtil.displayToast(R.string.app_validation_error);
        }else{
	       UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
	 	   HyjModelEditor<UserData> userDataEditor = userData.newModelEditor();
	 	   userDataEditor.getModelCopy().setPhone(null);
	 	   userDataEditor.save();
	 	   getActivity().finish();
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

    /**

     * class name：SIMCardInfo<BR>
     * class description：读取Sim卡信息<BR>
     * PS： 必须在加入各种权限 <BR>
     * Date:2012-3-12<BR>
     * @version 1.00
     * @author CODYY)peijiangping
     */
    
    public class SIMCardInfo {
        /**
         * TelephonyManager提供设备上获取通讯服务信息的入口。 应用程序可以使用这个类方法确定的电信服务商和国家 以及某些类型的用户访问信息。
         * 应用程序也可以注册一个监听器到电话收状态的变化。不需要直接实例化这个类
         * 使用Context.getSystemService(Context.TELEPHONY_SERVICE)来获取这个类的实例。
         */
        private TelephonyManager telephonyManager;
        /**
         * 国际移动用户识别码
         */
        private String IMSI;
        
        public SIMCardInfo(Context context) {
            telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
    
        }
    
        /**
    
         * Role:获取当前设置的电话号码
    
         * <BR>Date:2012-3-12
    
         * <BR>@author CODYY)peijiangping
    
         */
    
        public String getNativePhoneNumber() {
            String NativePhoneNumber=null;
            NativePhoneNumber=telephonyManager.getLine1Number();
            return NativePhoneNumber;
    
        }
    
        /**
         * Role:Telecom service providers获取手机服务商信息 <BR>
         * 需要加入权限<uses-permission
         * android:name="android.permission.READ_PHONE_STATE"/> <BR>
         * Date:2012-3-12 <BR>
         *
         * @author CODYY)peijiangping
    
         */
    
        public String getProvidersName() {
    
            String ProvidersName = null;
            // 返回唯一的用户ID;就是这张卡的编号神马的
            IMSI = telephonyManager.getSubscriberId();
            // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
            System.out.println(IMSI);
            if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
                ProvidersName = "中国移动";
    
            } else if (IMSI.startsWith("46001")) {
                ProvidersName = "中国联通";
            } else if (IMSI.startsWith("46003")) {
                ProvidersName = "中国电信";
            }
            return ProvidersName;
        }
    }
}


