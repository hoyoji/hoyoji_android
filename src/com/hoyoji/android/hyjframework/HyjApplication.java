package com.hoyoji.android.hyjframework;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;
import com.baidu.frontia.Frontia;
import com.baidu.frontia.FrontiaApplication;
import com.hoyoji.hoyoji.AppConstants;
import com.hoyoji.hoyoji.LoginActivity;
import com.hoyoji.hoyoji.PictureUploadService;
import com.hoyoji.hoyoji.message.MessageDownloadService;
import com.hoyoji.hoyoji.models.QQLogin;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.models.UserData;
import com.tencent.android.tpush.XGPushConfig;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.support.v4.app.Fragment;

public class HyjApplication extends FrontiaApplication {
	public final static String TAG = "HyjApplication";
	private static HyjApplication sInstance;
	private Boolean mIsSyncing = false;
	private User currentUser = null;
	private HashMap<String, Class<? extends Fragment>> fragmentClassMap = new HashMap<String, Class<? extends Fragment>>();
	private static boolean mIsDebuggable;
	
	public static boolean getIsDebuggable(){
		return mIsDebuggable;
	}
	
	public static String getServerUrl(){
		if(mIsDebuggable){
			return "http://hoyojitest.duapp.com/";
		} else {
			return "http://hoyoji.duapp.com/";
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
		mIsDebuggable =  ( 0 != ( HyjApplication.getInstance().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) );
		XGPushConfig.enableDebug(getApplicationContext(), mIsDebuggable);
		
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

		Frontia.init(this.getApplicationContext(), AppConstants.BAIDU_APP_KEY);

		Intent startPictureUploadService = new Intent(this, PictureUploadService.class);
		startPictureUploadService.putExtra("init", true);
		startService(startPictureUploadService);

		Intent startMessageDownloadService = new Intent(this, MessageDownloadService.class);
		startService(startMessageDownloadService);
		
//		FrontiaRole mRole = new FrontiaRole("admin");//假定同个id的角色尚未保存到Frontia云
//        FrontiaUser curUser = (FrontiaUser)Frontia.getCurrentAccount();//当前的授权用户。假定已经调用过Frontia的授权模块认证了一个用户，并通过Frontia.setCurrentAccount(..)设置到了Frontia里。
//
//        FrontiaUser nullUser = new FrontiaUser("null");//可以构造用户，但这类用户没有任何信息，需要开发者自己维护。
//        mRole.addMember(nullUser);
//        
//        mRole.create(new FrontiaRole.CommonOperationListener() {//把角色保存到Frontia云
//
//            @Override
//            public void onSuccess() {
//                    Log.d(TAG, "role saved");
//            }
//
//            @Override
//            public void onFailure(int errCode, String errMsg) {
//                    Log.d(TAG, "errCode:" + errCode
//                            + ", errMsg:" + errMsg);
//            }
//        });
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

	public User getCurrentUser() {
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
									.setDatabaseName(userId).create();
		ActiveAndroid.initialize(config, mIsDebuggable);
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
				ActiveAndroid.initialize(config, mIsDebuggable);
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
		ActiveAndroid.initialize(config, mIsDebuggable);
		initContentProvider();
		
		currentUser = HyjModel.getModel(User.class, userId);
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
				ActiveAndroid.initialize(config, mIsDebuggable);
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
		ActiveAndroid.initialize(config, mIsDebuggable);
		initContentProvider();
		
		User user = HyjModel.getModel(User.class, userId);
		UserData userData;
		String oldNickName = "";
		if(user != null){
			oldNickName = user.getNickName();
			if(oldNickName == null){
				oldNickName = "";
			}
		}
		try {
			ActiveAndroid.beginTransaction();
			if(user == null){
				user = new User();
				userData = new UserData();
				userData.setLastSyncTime(null);
			} else {
				userData = user.getUserData();
			}
		
			user.loadFromJSON(jsonObject.getJSONObject("user"), true);
			userData.loadFromJSON(jsonObject.getJSONObject("userData"), true);
			
			user.setUserData(userData);
			userData.setUser(user);
			userData.setPassword(password);
			
			user.save();
			userData.save();
			ActiveAndroid.setTransactionSuccessful();

			user = HyjModel.getModel(User.class, user.getId());
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
		    ActiveAndroid.endTransaction();
		}

		currentUser = user;
		if(currentUser != null){

			// 设置用户的昵称拼音, 并同步回服务器
			if(!oldNickName.equals(user.getNickName())){
				user.setNickName(user.getNickName());
				user.save();
			}
			
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
				ActiveAndroid.initialize(config, mIsDebuggable);
				initContentProvider();
			}
			return false;
		}
	}
	
	public boolean loginWBFirstTime(String userId, String password, JSONObject jsonObject) {
		User curUser = HyjApplication.getInstance().getCurrentUser();
		logout();
		assert(currentUser == null);
		Configuration config = new Configuration.Builder(HyjApplication.getInstance())
									.setDatabaseName(userId)
									.create(); 
		ActiveAndroid.initialize(config, mIsDebuggable);
		initContentProvider();
		
		User user = HyjModel.getModel(User.class, userId);
		UserData userData;
		String oldNickName = "";
		if(user != null){
			oldNickName = user.getNickName();
			if(oldNickName == null){
				oldNickName = "";
			}
		}
		try {
			ActiveAndroid.beginTransaction();
			if(user == null){
				user = new User();
				userData = new UserData();
				userData.setLastSyncTime(null);
			} else {
				userData = user.getUserData();
			}
		
			user.loadFromJSON(jsonObject.getJSONObject("user"), true);
			userData.loadFromJSON(jsonObject.getJSONObject("userData"), true);
			
			user.setUserData(userData);
			userData.setUser(user);
			userData.setPassword(password);
			
			user.save();
			userData.save();
			ActiveAndroid.setTransactionSuccessful();

			user = HyjModel.getModel(User.class, user.getId());
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
		    ActiveAndroid.endTransaction();
		}

		currentUser = user;
		if(currentUser != null){

			// 设置用户的昵称拼音, 并同步回服务器
			if(!oldNickName.equals(user.getNickName())){
				user.setNickName(user.getNickName());
				user.save();
			}
			
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
				ActiveAndroid.initialize(config, mIsDebuggable);
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
		ActiveAndroid.initialize(config, mIsDebuggable);
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
				ActiveAndroid.initialize(config, mIsDebuggable);
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