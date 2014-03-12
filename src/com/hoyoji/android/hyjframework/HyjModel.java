package com.hoyoji.android.hyjframework;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Select;
import com.hoyoji.hoyoji.models.ClientSyncRecord;

public abstract class HyjModel extends Model  implements Cloneable {

	public HyjModel(){
		super();
		if(HyjApplication.getInstance().getCurrentUser() != null){
			this.setCreatorId(HyjApplication.getInstance().getCurrentUser().getId());
		}
	}
	
	public static <T extends HyjModel> T getModel(Class<T> modelClass, String id){
		T entity = (T)Cache.getEntity(modelClass, id);
		if (entity == null) {
			entity = new Select().from(modelClass).where("id=?", id).executeSingle();
		}
		return entity;
	}	
	
	
	@Override
	protected HyjModel clone() {
		try {
			return (HyjModel)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public abstract void setId(String id);
	
	public abstract String getId();
	
	public abstract void setCreatorId(String id);
//	{
//		m_creatorId = id;
//	}
	public abstract String getCreatorId();
//	{
//		return m_creatorId;
//	}
	
	public abstract String getServerRecordHash();
//	{
//		return mServerRecordHash;
//	}

	public abstract void setServerRecordHash(String mServerRecordHash);
//	{
//		this.mServerRecordHash = mServerRecordHash;
//	}

	public abstract String getLastServerUpdateTime();
//	{
//		return mLastServerUpdateTime;
//	}

	public abstract void setLastServerUpdateTime(String mLastServerUpdateTime);
//	{
//		this.mLastServerUpdateTime = mLastServerUpdateTime;
//	}

	public abstract Long getLastClientUpdateTime();
//	{
//		return mLastClientUpdateTime;
//	}

	public abstract void setLastClientUpdateTime(Long mLastClientUpdateTime);
//	{
//		this.mLastClientUpdateTime = mLastClientUpdateTime;
//	}	
	
	public HyjModelEditor newModelEditor(){
		return new HyjModelEditor(this);
	}

	@Override
	public void save(){
		if(this.getCreatorId() == null){
			this.setCreatorId(HyjApplication.getInstance().getCurrentUser().getId());
		}
		if(!this.getSyncFromServer()){
			this.setLastClientUpdateTime(System.currentTimeMillis());
		}
		super.save();
	}
	
	public boolean isClientNew(){
		ClientSyncRecord clientSyncRecord = this.getClientSyncRecord();
		if(clientSyncRecord == null){
			return false;
		} else {
			return clientSyncRecord.getOperation().equalsIgnoreCase("Create");
		}
	}
	
	public ClientSyncRecord getClientSyncRecord(){
		return new Select().from(ClientSyncRecord.class).where("id=?", getId()).executeSingle();
	}
	
	final public void deleteFromServer(){
		this.setSyncFromServer(true);
		super.delete();
	}
	
	public abstract void validate(HyjModelEditor<? extends HyjModel> hyjModelEditor);
}
