package com.hoyoji.hoyoji.models;

import java.util.UUID;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.hoyoji_android.R;

import android.provider.BaseColumns;
import android.widget.TextView;

@Table(name = "Friend", id = BaseColumns._ID)
public class Friend extends HyjModel {
	
	@Column(name = "id", index = true, unique = true)
	private String mUUID;
	
	@Column(name = "nickName")
	private String mNickName;
	
	@Column(name = "nickName_pinYin")
	private String mNickName_pinYin;
	
	@Column(name = "friendUserId")
	private String mFriendUserId;

	@Column(name = "friendUserName")
	private String mFriendUserName;
	
	@Column(name = "friendCategoryId")
	private String mFriendCategoryId;

	@Column(name = "lastSyncTime")
	private String mLastSyncTime;

	@Column(name = "ownerUserId")
	private String mOwnerUserId;

	@Column(name = "location")
	private String mLocation;
	
	@Column(name = "geoLon")
	private String mGeoLon;
	
	@Column(name = "geoLat")
	private String mGeoLat;
	
	@Column(name = "address")
	private String mAddress;

	@Column(name = "_creatorId")
	private String m_creatorId;

	@Column(name = "serverRecordHash")
	private String mServerRecordHash;

	@Column(name = "lastServerUpdateTime")
	private String mLastServerUpdateTime;

	@Column(name = "lastClientUpdateTime")
	private Long mLastClientUpdateTime;
	
	
	public Friend(){
		super();
		mUUID = UUID.randomUUID().toString();
		mFriendCategoryId = HyjApplication.getInstance().getCurrentUser().getUserData().getDefaultFriendCategoryId();
	}

	public User getFriendUser(){
		if(mFriendUserId == null){
			return null;
		}
		return (User) getModel(User.class, mFriendUserId);
	}
	
	public void setFriendUser(User user){
		mFriendUserId = user.getId();
	}

	@Override
	public String getId() {
		return mUUID;
	}

	public void setId(String mUUID) {
		this.mUUID = mUUID;
	}

	public String getDisplayName(){
		if(this.getNickName() != null && this.getNickName().length() > 0){
			return this.getNickName();
		} else {
			User friendUser = this.getFriendUser();
			if(friendUser != null){
				return friendUser.getDisplayName();
			} else {
				return this.getFriendUserName();
			}
		}
	}
	
	public String getNickName() {
		return mNickName;
	}

	public void setNickName(String nickName) {
		if(nickName == null){
			this.mNickName_pinYin = "";
		} else if(this.mNickName == null || !this.mNickName.equals(nickName) || this.mNickName_pinYin == null){
			this.mNickName_pinYin = HyjUtil.convertToPinYin(nickName);
		}

		this.mNickName = nickName;
	}
	
	public String getFriendUserId() {
		return mFriendUserId;
	}

	public void setFriendUserId(String mFriendUserId) {
		this.mFriendUserId = mFriendUserId;
	}

	public String getFriendUserName() {
		return mFriendUserName;
	}

//	public void setFriendUserName1(String mFriendUserName) {
//		this.mFriendUserName = mFriendUserName;
//	}
	
	public FriendCategory getFriendCategory() {
		if(mFriendCategoryId == null){
			return null;
		}
		return (FriendCategory) getModel(FriendCategory.class, mFriendCategoryId);
	}

	public void setFriendCategory(FriendCategory mFriendCategory) {
		this.mFriendCategoryId = mFriendCategory.getId();
	}
	
	public String getFriendCategoryId() {
		return mFriendCategoryId;
	}

	public void setFriendCategoryId(String mFriendCategoryId) {
		this.mFriendCategoryId = mFriendCategoryId;
	}

	public String getLastSyncTime() {
		return mLastSyncTime;
	}

	public void setLastSyncTime(String mLastSyncTime) {
		this.mLastSyncTime = mLastSyncTime;
	}

	public String getOwnerUserId() {
		return mOwnerUserId;
	}

	public void setOwnerUserId(String mOwnerUserId) {
		this.mOwnerUserId = mOwnerUserId;
	}

	public String getLocation() {
		return mLocation;
	}

	public void setLocation(String mLocation) {
		this.mLocation = mLocation;
	}

	public String getGeoLon() {
		return mGeoLon;
	}

	public void setGeoLon(String mGeoLon) {
		this.mGeoLon = mGeoLon;
	}

	public String getGeoLat() {
		return mGeoLat;
	}

	public void setGeoLat(String mGeoLat) {
		this.mGeoLat = mGeoLat;
	}

	public String getAddress() {
		return mAddress;
	}

	public void setAddress(String mAddress) {
		this.mAddress = mAddress;
	}
	
	@Override
	public void validate(HyjModelEditor modelEditor) {
		if(this.getFriendCategoryId() == null){
			modelEditor.setValidationError("friendCategory", R.string.friendFormFragment_editText_hint_friend_category);
		} else {
			modelEditor.removeValidationError("friendCategory");
		}
		if(this.getFriendUserId() == null){
			if(this.getNickName().length() == 0){
				modelEditor.setValidationError("nickName", R.string.friendFormFragment_editText_hint_friendName);
			} else {
				modelEditor.removeValidationError("nickName");
			}		
		}
	}

	@Override
	public void save(){
		if(this.getOwnerUserId() == null){
			this.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
		}
		super.save();
	}	

	public void setCreatorId(String id){
		m_creatorId = id;
	}
	
	public String getCreatorId(){
		return m_creatorId;
	}
	
	public String getServerRecordHash(){
		return mServerRecordHash;
	}

	public void setServerRecordHash(String mServerRecordHash){
		this.mServerRecordHash = mServerRecordHash;
	}

	public String getLastServerUpdateTime(){
		return mLastServerUpdateTime;
	}

	public void setLastServerUpdateTime(String mLastServerUpdateTime){
		this.mLastServerUpdateTime = mLastServerUpdateTime;
	}

	public Long getLastClientUpdateTime(){
		return mLastClientUpdateTime;
	}

	public void setLastClientUpdateTime(Long mLastClientUpdateTime){
		this.mLastClientUpdateTime = mLastClientUpdateTime;
	}

	public static String getFriendUserDisplayName(String ownerUserId) {
		if(ownerUserId.equalsIgnoreCase(HyjApplication.getInstance().getCurrentUser().getId())){
			return "";
		}else{
			Friend friend = new Select().from(Friend.class).where("friendUserId=?",ownerUserId).executeSingle();
			if(friend != null){
				return friend.getDisplayName();
			} else {
				User user = HyjModel.getModel(User.class, ownerUserId);
				if(user != null){
					return user.getDisplayName();
				} else {
					return "";
				}
			}
		}
	}	
	public static String getFriendUserDisplayName1(String ownerUserId) {
		if(ownerUserId.equalsIgnoreCase(HyjApplication.getInstance().getCurrentUser().getId())){
			return "自己";
		}else{
			Friend friend = new Select().from(Friend.class).where("friendUserId=?",ownerUserId).executeSingle();
			if(friend != null){
				return friend.getDisplayName();
			} else {
				User user = HyjModel.getModel(User.class, ownerUserId);
				if(user != null){
					return user.getDisplayName();
				} else {
					return "";
				}
			}
		}
	}	
	
}
