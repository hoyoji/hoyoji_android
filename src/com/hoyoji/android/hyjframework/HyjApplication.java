package com.hoyoji.android.hyjframework;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;
import com.hoyoji.hoyoji.models.User;
import com.hoyoji.hoyoji.models.UserData;

import android.app.Application;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.support.v4.app.Fragment;

public class HyjApplication extends Application {
	public final static String TAG = "HyjApplication";
	private final static String SERVER_URL = "http://money.app100697798.twsapp.com/";
	private static HyjApplication sInstance;
	private User currentUser = null;
	private HashMap<String, Class<? extends Fragment>> fragmentClassMap = new HashMap<String, Class<? extends Fragment>>();
	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		logout();
	}
	
	public static HyjApplication getInstance() {
		return sInstance;
	}
	
	public static String getServerUrl(){
		return SERVER_URL;
	}
	
	public User getCurrentUser() {
		return currentUser;
	}

	public boolean isLoggedIn() {
		return currentUser != null;
	}
	
	public void initContentProvider(){
		ContentResolver resolver = getApplicationContext().getContentResolver();
		ContentProviderClient client = resolver.acquireContentProviderClient("com.hoyoji.hoyoji");
		ContentProvider provider = (ContentProvider) client.getLocalContentProvider();
		provider.initialize();
	}
	public boolean login(String userId, String password){
		assert(currentUser == null);
		Configuration config = new Configuration.Builder(HyjApplication.getInstance())
									.setDatabaseName(userId)
									.create();
		ActiveAndroid.initialize(config);
		initContentProvider();
		
		currentUser = authenticateUser(userId, password);
		if(currentUser != null){
			return true;
		} else {
			ActiveAndroid.dispose();
			return false;
		}
	}
	
	
	public boolean login(String userId, String password, JSONObject jsonObject) throws JSONException {
		assert(currentUser == null);
		Configuration config = new Configuration.Builder(HyjApplication.getInstance())
									.setDatabaseName(userId)
									.create();
		ActiveAndroid.initialize(config);
		initContentProvider();
		
		User user = new Select().from(User.class).where("id=?", userId).executeSingle();
		UserData userData;
		if(user == null){
			user = new User();
			userData = new UserData();
			
			user.loadFromJSON(jsonObject.getJSONObject("user"));
			userData.loadFromJSON(jsonObject.getJSONObject("userData"));
			
			user.setUserData(userData);
			userData.setUser(user);
			
		} else {
			userData = user.getUserData();
		}
		userData.setPassword(password);
		
		try {
			ActiveAndroid.beginTransaction();
			user.save();
			userData.save();
			ActiveAndroid.setTransactionSuccessful();
		} finally {
		    ActiveAndroid.endTransaction();
		}
		
		currentUser = authenticateUser(userId, password);
		if(currentUser != null){
			return true;
		} else {
			ActiveAndroid.dispose();
			return false;
		}
	}
	
	public void logout(){
		if(isLoggedIn()){
			currentUser = null;
			ActiveAndroid.dispose();
		}
	}
	
	private User authenticateUser(String userId, String password){
		return new Select().from(User.class).join(UserData.class).on("User.id = UserData.userId").where("User.id=? AND UserData.password=?", new Object[]{userId, password}).executeSingle();
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