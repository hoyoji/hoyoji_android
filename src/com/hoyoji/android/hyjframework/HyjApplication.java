package com.hoyoji.android.hyjframework;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;
import com.hoyoji.hoyoji.LoginActivity;
import com.hoyoji.hoyoji.models.QQLogin;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.models.UserData;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

import android.app.Application;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

public class HyjApplication extends Application {
	public final static String TAG = "HyjApplication";
	private final static String SERVER_URL = "http://hoyoji.duapp.com/";
	private static HyjApplication sInstance;
	private Boolean mIsSyncing = false;
	private User currentUser = null;
	private HashMap<String, Class<? extends Fragment>> fragmentClassMap = new HashMap<String, Class<? extends Fragment>>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;

		XGPushConfig.enableDebug(getApplicationContext(), true);
//		XGPushManager.registerPush(getApplicationContext(), HyjApplication.getInstance().getCurrentUser().getId());
//		XGPushManager.unregisterPush(getApplicationContext());
//		XGPushManager.registerPush(getApplicationContext());
		
//        SharedPreferences userInfo = getSharedPreferences("current_user_info", 0);  
//        final String userId = userInfo.getString("userId", "");  
//        final String password = userInfo.getString("password", "");  
//        if(userId.length() > 0 && password.length() > 0){
////        	Thread thread = new Thread(new Runnable(){
////				@Override
////				public void run() {
////					login(userId, password);
////				}
////        	});
////        	thread.start();
//        }
	}

	@Override
	public void onTerminate() {
		logout();
		super.onTerminate();
	}
	
	public Boolean getIsSyncing(){
		return mIsSyncing;
	}
	
	public void setIsSyncing(Boolean b){
		mIsSyncing = b;
	}
	
	public static HyjApplication getInstance() {
		return sInstance;
	}
	
	public static String getServerUrl(){
		return SERVER_URL;
	}
	
	public synchronized User getCurrentUser() {
		return currentUser;
	}

	public boolean isLoggedIn() {
		return currentUser != null;
	}
	
	public void initContentProvider(){
		
		ContentResolver resolver = getApplicationContext().getContentResolver();
		ContentProviderClient client = resolver.acquireContentProviderClient("com.hoyoji.hoyoji_android");
		ContentProvider provider = (ContentProvider) client.getLocalContentProvider();
		provider.initialize();
		
	}
	
	public boolean login(String userId, String password){
		User curUser = HyjApplication.getInstance().getCurrentUser();
		logout();
		assert(currentUser == null);
		Configuration config = new Configuration.Builder(HyjApplication.getInstance())
									.setDatabaseName(userId)
									.create();
		ActiveAndroid.initialize(config);
		initContentProvider();
		
		currentUser = authenticateUser(userId, password);
		if(currentUser != null){
            SharedPreferences userInfo = getSharedPreferences("current_user_info", 0);  
            userInfo.edit().putString("userId", currentUser.getId()).commit();  
            userInfo.edit().putString("password", currentUser.getUserData().getPassword()).commit(); 
//            if(curUser != null && !curUser.getId().equals(currentUser.getId())){
//    			XGPushManager.unregisterPush(getApplicationContext(), curUser.getId(),new XGIOperateCallback() {
//					public void onFail(Object arg0, int arg1, String arg2) {
//						XGPushManager.unregisterPush(getApplicationContext());
//						XGPushManager.registerPush(getApplicationContext(), currentUser.getId());
//					}
//					@Override
//					public void onSuccess(Object arg0, int arg1) {
//						XGPushManager.registerPush(getApplicationContext(), currentUser.getId());
//					}});
//            } else {
//				XGPushManager.registerPush(getApplicationContext(), currentUser.getId());
//            }
			return true;
		} else {
			ActiveAndroid.dispose();
			currentUser = curUser;
			if(curUser != null){
				config = new Configuration.Builder(HyjApplication.getInstance())
				.setDatabaseName(curUser.getId())
				.create(); 
				ActiveAndroid.initialize(config);
				initContentProvider();
			}
			return false;
		}
	}
	
	// 该QQ用户已经存在
	public boolean loginQQ(String userId, JSONObject jsonObject) {
		User curUser = HyjApplication.getInstance().getCurrentUser();
		logout();
		assert(currentUser == null);
		Configuration config = new Configuration.Builder(HyjApplication.getInstance()).setDatabaseName(userId).create(); 
		ActiveAndroid.initialize(config);
		initContentProvider();
		
		currentUser = new Select().from(User.class).where("id=?", userId).executeSingle();
		if(currentUser != null){
			QQLogin qqLogin = new Select().from(QQLogin.class).where("userId=?", userId).executeSingle();
			qqLogin.loadFromJSON(jsonObject, false);
			qqLogin.save();

            SharedPreferences userInfo = getSharedPreferences("current_user_info", 0);  
            userInfo.edit().putString("userId", currentUser.getId()).commit();  
            userInfo.edit().putString("password", currentUser.getUserData().getPassword()).commit(); 
//            if(curUser != null && !curUser.getId().equals(currentUser.getId())){
//    			XGPushManager.unregisterPush(getApplicationContext(), curUser.getId(),new XGIOperateCallback() {
//					public void onFail(Object arg0, int arg1, String arg2) {
//						XGPushManager.unregisterPush(getApplicationContext());
//						XGPushManager.registerPush(getApplicationContext(), currentUser.getId());
//					}
//					@Override
//					public void onSuccess(Object arg0, int arg1) {
//						XGPushManager.registerPush(getApplicationContext(), currentUser.getId());
//					}});
//            } else {
//				XGPushManager.registerPush(getApplicationContext(), currentUser.getId());
//            }
			return true;
		} else {
			ActiveAndroid.dispose();
			currentUser = curUser;
			if(curUser != null){
				config = new Configuration.Builder(HyjApplication.getInstance()).setDatabaseName(curUser.getId()).create(); 
				ActiveAndroid.initialize(config);
				initContentProvider();
			}
			return false;
		}
	}
	
	public boolean loginQQFirstTime(String userId, String password, JSONObject jsonObject) {
		User curUser = HyjApplication.getInstance().getCurrentUser();
		logout();
		assert(currentUser == null);
		Configuration config = new Configuration.Builder(HyjApplication.getInstance())
									.setDatabaseName(userId)
									.create(); 
		ActiveAndroid.initialize(config);
		initContentProvider();
		
		User user = new Select().from(User.class).where("id=?", userId).executeSingle();
		UserData userData;
		try {
			ActiveAndroid.beginTransaction();
			if(user == null){
				user = new User();
				userData = new UserData();
				
				user.loadFromJSON(jsonObject.getJSONObject("user"), true);
				userData.loadFromJSON(jsonObject.getJSONObject("userData"), true);
				
				userData.setLastSyncTime(null);
				user.setUserData(userData);
				userData.setUser(user);
				userData.setPassword(password);
				
				user.save();
			} else {
				userData = user.getUserData();
			}
			
			userData.setSyncFromServer(true);
			userData.save();
			ActiveAndroid.setTransactionSuccessful();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		    ActiveAndroid.endTransaction();
		}
		
		currentUser = user;
		if(currentUser != null){
            SharedPreferences userInfo = getSharedPreferences("current_user_info", 0);  
            userInfo.edit().putString("userId", currentUser.getId()).commit();  
            userInfo.edit().putString("password", currentUser.getUserData().getPassword()).commit();  
//            if(curUser != null && !curUser.getId().equals(currentUser.getId())){
//    			XGPushManager.unregisterPush(getApplicationContext(), curUser.getId(),new XGIOperateCallback() {
//					public void onFail(Object arg0, int arg1, String arg2) {
//						XGPushManager.unregisterPush(getApplicationContext());
//						XGPushManager.registerPush(getApplicationContext(), currentUser.getId());
//					}
//					@Override
//					public void onSuccess(Object arg0, int arg1) {
//						XGPushManager.registerPush(getApplicationContext(), currentUser.getId());
//					}});
//            } else {
//				XGPushManager.registerPush(getApplicationContext(), currentUser.getId());
//            }
			return true;
		} else {
			ActiveAndroid.dispose();
			currentUser = curUser;
			if(curUser != null){
				config = new Configuration.Builder(HyjApplication.getInstance())
				.setDatabaseName(curUser.getId())
				.create(); 
				ActiveAndroid.initialize(config);
				initContentProvider();
			}
			return false;
		}
	}
	
	public boolean login(String userId, String password, JSONObject jsonObject) {
		User curUser = HyjApplication.getInstance().getCurrentUser();
		logout();
		assert(currentUser == null);
		Configuration config = new Configuration.Builder(HyjApplication.getInstance())
									.setDatabaseName(userId)
									.create(); 
		ActiveAndroid.initialize(config);
		initContentProvider();
		
		User user = new Select().from(User.class).where("id=?", userId).executeSingle();
		UserData userData;
		try {
			ActiveAndroid.beginTransaction();
			if(user == null){
				user = new User();
				userData = new UserData();
				
				user.loadFromJSON(jsonObject.getJSONObject("user"), true);
				userData.loadFromJSON(jsonObject.getJSONObject("userData"), true);
				
				userData.setLastSyncTime(null);
				user.setUserData(userData);
				userData.setUser(user);
	
				user.save();
			} else {
				userData = user.getUserData();
			}
			
			userData.setPassword(password);
			userData.setSyncFromServer(true);
			userData.save();
			ActiveAndroid.setTransactionSuccessful();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
		    ActiveAndroid.endTransaction();
		}
		
		currentUser = authenticateUser(userId, password);
		if(currentUser != null){
            SharedPreferences userInfo = getSharedPreferences("current_user_info", 0);  
            userInfo.edit().putString("userId", currentUser.getId()).commit();  
            userInfo.edit().putString("password", currentUser.getUserData().getPassword()).commit();  
//            if(curUser != null && !curUser.getId().equals(currentUser.getId())){
//    			XGPushManager.unregisterPush(getApplicationContext(), curUser.getId(),new XGIOperateCallback() {
//					public void onFail(Object arg0, int arg1, String arg2) {
//						XGPushManager.unregisterPush(getApplicationContext());
//						XGPushManager.registerPush(getApplicationContext(), currentUser.getId());
//					}
//					@Override
//					public void onSuccess(Object arg0, int arg1) {
//						XGPushManager.registerPush(getApplicationContext(), currentUser.getId());
//					}});
//            } else {
//				XGPushManager.registerPush(getApplicationContext(), currentUser.getId());
//            }
			return true;
		} else {
			ActiveAndroid.dispose();
			currentUser = curUser;
			if(curUser != null){
				config = new Configuration.Builder(HyjApplication.getInstance())
				.setDatabaseName(curUser.getId())
				.create(); 
				ActiveAndroid.initialize(config);
				initContentProvider();
			}
			return false;
		}
	}
	
	public void logout(){
		if(isLoggedIn()){
			currentUser = null;
			ActiveAndroid.dispose();
		}
	}
	
	public void switchUser(){

		Intent intent = new Intent(
				this.getApplicationContext(),
				LoginActivity.class);

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
		startActivity(intent);
	}
	
	private User authenticateUser(String userId, String password){
		return new Select("User.*").from(User.class).join(UserData.class).on("User.id = UserData.userId").where("User.id=? AND UserData.password=?", new Object[]{userId, password}).executeSingle();
	}

	public void addFragmentClassMap(String className,
			Class<? extends Fragment> fragmentClass) {
		if(!fragmentClassMap.containsKey(className)){
			fragmentClassMap.put(className, fragmentClass);
		}
	}

	public Class<? extends Fragment> getFragmentClassMap(
			String fragmentClassName) {
		return fragmentClassMap.get(fragmentClassName);
	}
}