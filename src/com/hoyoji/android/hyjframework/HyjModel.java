package com.hoyoji.android.hyjframework;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Select;
import com.hoyoji.hoyoji.models.ClientSyncRecord;

public abstract class HyjModel extends Model  implements Cloneable {

	@Column(name = "_creatorId")
	private String m_creatorId;

	@Column(name = "serverRecordHash")
	private String mServerRecordHash;

	@Column(name = "lastServerUpdateTime")
	private String mLastServerUpdateTime;

	@Column(name = "lastClientUpdateTime")
	private Long mLastClientUpdateTime;
	
	public HyjModel(){
		super();
		if(HyjApplication.getInstance().getCurrentUser() != null){
			m_creatorId = HyjApplication.getInstance().getCurrentUser().getId();
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
	
	public void setCreator(HyjModel creator){
		m_creatorId = creator.getId();
	}
	
	public String getServerRecordHash() {
		return mServerRecordHash;
	}

	public void setServerRecordHash(String mServerRecordHash) {
		this.mServerRecordHash = mServerRecordHash;
	}

	public String getLastServerUpdateTime() {
		return mLastServerUpdateTime;
	}

	public void setLastServerUpdateTime(String mLastServerUpdateTime) {
		this.mLastServerUpdateTime = mLastServerUpdateTime;
	}

	public Long getLastClientUpdateTime() {
		return mLastClientUpdateTime;
	}

	public void setLastClientUpdateTime(Long mLastClientUpdateTime) {
		this.mLastClientUpdateTime = mLastClientUpdateTime;
	}	
	
	public HyjModelEditor newModelEditor(){
		return new HyjModelEditor(this);
	}

	@Override
	public void save(){
		if(m_creatorId == null){
			m_creatorId = this.getId();
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
		return HyjModel.getModel(ClientSyncRecord.class, getId());
	}
	
	public abstract void validate(HyjModelEditor<? extends HyjModel> hyjModelEditor);
}
