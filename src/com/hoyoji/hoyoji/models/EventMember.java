package com.hoyoji.hoyoji.models;

import java.util.UUID;

import android.provider.BaseColumns;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;

@Table(name = "EventMember", id = BaseColumns._ID)
public class EventMember extends HyjModel {

	@Column(name = "id", index = true, unique = true)
	private String mUUID;

	@Column(name = "eventId")
	private String mEventId;

	@Column(name = "localFriendId")
	private String mLocalFriendId;

	@Column(name = "friendUserId")
	private String mFriendUserId;
	
	@Column(name = "friendUserName")
	private String mFriendUserName;
	
	@Column(name = "state")
	private String mState;

	@Column(name = "ownerUserId")
	private String mOwnerUserId;
	
	@Column(name = "_creatorId")
	private String m_creatorId;

	@Column(name = "serverRecordHash")
	private String mServerRecordHash;

	@Column(name = "lastServerUpdateTime")
	private String mLastServerUpdateTime;

	@Column(name = "lastClientUpdateTime")
	private Long mLastClientUpdateTime;
	
	
	public EventMember(){
		super();
		mUUID = UUID.randomUUID().toString();
	}

	
	@Override
	public void validate(HyjModelEditor<?> modelEditor) {
		
	}

	public String getId() {
		return mUUID;
	}

	public void setId(String mUUID) {
		this.mUUID = mUUID;
	}
	
	public String getOwnerUserId() {
		return mOwnerUserId;
	}

	public void setOwnerUserId(String mOwnerUserId) {
		this.mOwnerUserId = mOwnerUserId;
	}

	public String getEventId() {
		return mEventId;
	}

	public void setEventId(String mEventId) {
		this.mEventId = mEventId;
	}
	
	public Event getEvent(){
		if(mEventId == null){
			return null;
		}
		return getModel(Event.class, mEventId);
	}
	
	public String getLocalFriendId() {
		return mLocalFriendId;
	}

	public void setLocalFriendId(String mLocalFriendId) {
		this.mLocalFriendId = mLocalFriendId;
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

	public void setFriendUserName(String mFriendUserName) {
		this.mFriendUserName = mFriendUserName;
	}
	
	public String getFriendDisplayName(){
		if(mFriendUserId != null){
			Friend friend = new Select().from(Friend.class).where("friendUserId=?", mFriendUserId).executeSingle();
			if(friend != null){
				return friend.getDisplayName();
			} else {
				User user = HyjModel.getModel(User.class, mFriendUserId);
				if(user != null){
					return user.getDisplayName();
				}
			}
		} else if(mLocalFriendId != null){
			Friend friend = Friend.getModel(Friend.class, mLocalFriendId);
			if(friend != null){
				return friend.getDisplayName();
			}
		}
		return this.getFriendUserName();
	}
	
	public String getState() {
		return mState;
	}

	public void setState(String mState) {
		this.mState = mState;
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
	
}
