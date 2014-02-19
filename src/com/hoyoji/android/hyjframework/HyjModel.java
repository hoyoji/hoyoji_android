package com.hoyoji.android.hyjframework;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Select;

public abstract class HyjModel extends Model  implements Cloneable {

	@Column(name = "_creatorId")
	private String m_creatorId;

	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void setCreator(HyjModel creator){
		m_creatorId = creator.getId();
	}
	
	public HyjModelEditor newModelEditor(){
		return new HyjModelEditor(this);
	}
	
	public abstract void validate(HyjModelEditor hyjModelEditor);
}
