package com.hoyoji.hoyoji.models;
import com.hoyoji.android.hyjframework.HyjModel;

public interface MoneyApportion {
	public String getId();
	public Double getAmount();
	public Project getProject();
	public ProjectShareAuthorization getProjectShareAuthorization();
	public String getFriendUserId();
	public User getFriendUser();
	public String getApportionType();
}
