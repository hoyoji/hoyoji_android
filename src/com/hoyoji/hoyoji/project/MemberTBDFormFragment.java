package com.hoyoji.hoyoji.project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.activeandroid.Model;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.activity.HyjActivity.DialogCallbackListener;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyApportion;
import com.hoyoji.hoyoji.models.MoneyExpenseApportion;
import com.hoyoji.hoyoji.models.MoneyExpenseContainer;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.money.MoneyApportionField;
import com.hoyoji.hoyoji.money.SelectApportionMemberListFragment;
import com.hoyoji.hoyoji.money.MoneyApportionField.ApportionItem;


public class MemberTBDFormFragment extends HyjUserFormFragment {

	protected static final int GET_APPORTION_MEMBER_ID = 0;
	private static final int ADD_AS_PROJECT_MEMBER = 1;

	ProjectShareAuthorization projectShareAuthorization;
	private HyjTextField mTextFieldProjectName = null;

	private MoneyApportionField mApportionFieldApportions;
	
	@Override
	public Integer useContentView() {
		return R.layout.project_formfragment_member_tbd;
	}
	
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		final Project project;
		
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		projectShareAuthorization =  ProjectShareAuthorization.load(ProjectShareAuthorization.class, modelId);
		project = projectShareAuthorization.getProject();
		
		boolean _canNotEdit = false;
		if(!projectShareAuthorization.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
			_canNotEdit = true;
		}
		final boolean canNotEdit = _canNotEdit;
		
//		mProjectShareAuthorizations = new Select().from(ProjectShareAuthorization.class).where("projectId = ? AND state <> ? AND id <> ?", project.getId(), "Delete", projectShareAuthorization.getId()).execute();
//		mProjectShareAuthorizations.add(projectShareAuthorization);
		
		mTextFieldProjectName = (HyjTextField) getView().findViewById(R.id.memberTBDFormFragment_textField_projectName);
		mTextFieldProjectName.setText(project.getDisplayName());
		mTextFieldProjectName.setEnabled(false);
		
		mApportionFieldApportions = (MoneyApportionField) getView().findViewById(R.id.memberTBDFormFragment_apportionField);
		getView().findViewById(R.id.memberTBDFormFragment_imageButton_apportion_add).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				Project project = projectShareAuthorization.getProject();
				bundle.putLong("MODEL_ID", project.get_mId());
				openActivityWithFragmentForResult(SelectApportionMemberListFragment.class, R.string.moneyApportionField_select_apportion_member, bundle, GET_APPORTION_MEMBER_ID);
			}
		});

		getView().findViewById(R.id.memberTBDFormFragment_imageButton_apportion_add_all).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addAllProjectMemberIntoApportionsField(projectShareAuthorization.getProject());
			}
		});
		
		getView().findViewById(R.id.memberTBDFormFragment_imageButton_apportion_more_actions).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						PopupMenu popup = new PopupMenu(getActivity(), v);
						MenuInflater inflater = popup.getMenuInflater();
						inflater.inflate(R.menu.money_apportionfield_more_actions, popup.getMenu());
						popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem item) {
								if (item.getItemId() == R.id.moneyApportionField_menu_moreActions_clear) {
									mApportionFieldApportions.clearAll();
									mApportionFieldApportions.setTotalAmount(0.0);
									return true;
								} else if (item.getItemId() == R.id.moneyApportionField_menu_moreActions_all_average) {
									mApportionFieldApportions.setAllApportionAverage();
									mApportionFieldApportions.setTotalAmount(0.0);
									return true;
								} else if (item.getItemId() == R.id.moneyApportionField_menu_moreActions_all_share) {
									mApportionFieldApportions.setAllApportionShare();
									mApportionFieldApportions.setTotalAmount(0.0);
									return true;
								}
								return false;
							}
		
		
						});
						
						if(canNotEdit){
							for(int i = 0; i<popup.getMenu().size();i++){
								popup.getMenu().setGroupEnabled(i, false);
							}
						}
						
						popup.show();
					}
				});

		
		getView().findViewById(R.id.memberTBDFormFragment_button_transactions).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putLong("PROJECT_ID", project.get_mId());
				openActivityWithFragment(ProjectMoneyTBDListFragment.class, R.string.memberTBDFormFragment_title_transactions, bundle);
			}
		});
		
		if(canNotEdit){
			getView().findViewById(R.id.button_save).setVisibility(View.GONE);
			if(this.mOptionsMenu != null){
				hideSaveAction();
			}
		}
	}
	
	private void addAllProjectMemberIntoApportionsField(Project project) {
		List<ProjectShareAuthorization> projectShareAuthorizations = project.getShareAuthorizations();
		for (int i = 0; i < projectShareAuthorizations.size(); i++) {
			if(projectShareAuthorizations.get(i).getState().equalsIgnoreCase("Delete")){
				continue;
			}
			MoneyExpenseApportion apportion = new MoneyExpenseApportion();
			apportion.setAmount(0.0);
			apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
			apportion.setLocalFriendId(projectShareAuthorizations.get(i).getLocalFriendId());
			if(projectShareAuthorizations.get(i).getSharePercentageType() != null && projectShareAuthorizations.get(i).getSharePercentageType().equals("Average")){
				apportion.setApportionType("Average");
			} else {
				apportion.setApportionType("Share");
			}
			mApportionFieldApportions.addApportion(apportion, project.getId(), ApportionItem.NEW);
			mApportionFieldApportions.setTotalAmount(0.0);
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if(projectShareAuthorization != null && projectShareAuthorization.get_mId() != null){
				boolean canNotEdit = false;
				if(!projectShareAuthorization.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
					canNotEdit = true;
				} 
				if(canNotEdit){
					hideSaveAction();
				}
		}
	}
	
	
	private void fillData() {

	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		if(!projectShareAuthorization.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
			return;
		}

		((HyjActivity) MemberTBDFormFragment.this.getActivity())
		.displayProgressDialog(
				R.string.memberTBDFormFragment_title_split,
				R.string.memberTBDFormFragment_progress_splitting);
	
		doSplitExpenseContainers();
		
		((HyjActivity) MemberTBDFormFragment.this.getActivity()).dismissProgressDialog();
		HyjUtil.displayToast(R.string.memberTBDFormFragment_toast_split_success);
	}	

	
	private void doSplitExpenseContainers() {
		List<MoneyExpenseContainer> moneyExpenseContainers = new Select().from(MoneyExpenseContainer.class).as("container")
								.join(MoneyExpenseApportion.class).as("apportion").on("container.id = apportion.moneyExpenseContainerId").
								where("apportion.localFriendId=?", projectShareAuthorization.getLocalFriendId()).execute();
		
		for(MoneyExpenseContainer moneyExpenseContainer : moneyExpenseContainers){
			List mergedApportions = new Select().from(MoneyExpenseApportion.class).where("moneyExpenseContainerId=?", moneyExpenseContainer.getId()).execute(); 
			mergeApportions(MoneyExpenseApportion.class, mergedApportions);
			setApportionsAmount(mergedApportions, moneyExpenseContainer.getAmount0());
			moneyExpenseContainer.saveApportions(mergedApportions);
		}
	}
	
	private void mergeApportions(Class<? extends MoneyApportion> modelClass, List<MoneyApportion> mergedApportions){
		Set<String> apportionsSet = new HashSet<String>();
		int positionOfTBD = -1;
		
		for(int i = 0; i < mergedApportions.size(); i++){
			MoneyApportion apportion = mergedApportions.get(i);
			apportionsSet.add(HyjUtil.ifNull(apportion.getFriendUserId(), apportion.getLocalFriendId()));
		}
		if(positionOfTBD > -1){
			mergedApportions.remove(positionOfTBD);
		}
		
		for(int i = 0; i < mApportionFieldApportions.getAdapter().getCount(); i++){
			ApportionItem<MoneyApportion> api = mApportionFieldApportions.getAdapter().getItem(i);
			MoneyApportion apiApportion = api.getApportion();
			
			if(apportionsSet.contains(HyjUtil.ifNull(apiApportion.getFriendUserId(), apiApportion.getLocalFriendId()))){
				MoneyApportion apportion;
				try {
					apportion = (MoneyApportion) modelClass.newInstance();
					
					apportion.setApportionType(apiApportion.getApportionType());
					apportion.setFriendUserId(apiApportion.getFriendUserId());
					apportion.setLocalFriendId(apiApportion.getLocalFriendId());
					
					mergedApportions.add(apportion);
					apportionsSet.add(HyjUtil.ifNull(apiApportion.getFriendUserId(), apiApportion.getLocalFriendId()));
					
				} catch (java.lang.InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void setApportionsAmount(List<MoneyApportion> mergedApportions, Double totalAmount){
		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode){
         case GET_APPORTION_MEMBER_ID:
 			if (resultCode == Activity.RESULT_OK) {
 				long _id = data.getLongExtra("MODEL_ID", -1);
 				String type = data.getStringExtra("MODEL_TYPE");
 				ProjectShareAuthorization psa = null;
 				if("ProjectShareAuthorization".equalsIgnoreCase(type)){
 					psa = ProjectShareAuthorization.load(ProjectShareAuthorization.class, _id);
 				} else {
 					final Friend friend = Friend.load(Friend.class, _id);
 					//看一下该好友是不是项目成员
 					if(friend.getFriendUserId() != null){
 						psa = new Select().from(ProjectShareAuthorization.class).where("friendUserId=? AND projectId=? AND state <> 'Delete'", friend.getFriendUserId(), projectShareAuthorization.getProjectId()).executeSingle();
 					} else {
 						psa = new Select().from(ProjectShareAuthorization.class).where("localFriendId=? AND projectId=? AND state <> 'Delete'", friend.getId(), projectShareAuthorization.getProjectId()).executeSingle();
 					}
 					
 					if(psa == null){
 						((HyjActivity)getActivity()).displayDialog(R.string.moneyApportionField_select_toast_apportion_user_not_member, R.string.moneyApportionField_select_confirm_apportion_add_as_member, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
 								new DialogCallbackListener() {
 									@Override
 									public void doPositiveClick(Object object) {
 										Bundle bundle = new Bundle();
 										bundle.putString("PROJECTID", projectShareAuthorization.getProjectId());
 										if(friend.getFriendUserId() != null){
 											bundle.putString("FRIEND_USERID", friend.getFriendUserId());
 										} else {
 											bundle.putString("LOCAL_FRIENDID", friend.getId());
 										}
 										openActivityWithFragmentForResult(MemberFormFragment.class, R.string.memberFormFragment_title_addnew, bundle, ADD_AS_PROJECT_MEMBER);
 									}
 			
 									@Override
 									public void doNegativeClick() {
 										HyjUtil.displayToast(R.string.moneyApportionField_select_toast_apportion_user_not_member);
 									}
 								});
 						
 	//					HyjUtil.displayToast(R.string.moneyApportionField_select_toast_apportion_user_not_member);
 						break;
 					}
 				}
 				addAsProjectMember(psa);
 			}
 			break;
 		case ADD_AS_PROJECT_MEMBER:
 			if (resultCode == Activity.RESULT_OK) {
 				String id = data.getStringExtra("MODELID");
 				ProjectShareAuthorization psa = HyjModel.getModel(ProjectShareAuthorization.class, id);
 				if(psa != null){
 					addAsProjectMember(psa);
 				} else {
 					HyjUtil.displayToast(R.string.moneyApportionField_select_toast_apportion_user_not_member);
 				}
 			} else {
 				HyjUtil.displayToast(R.string.moneyApportionField_select_toast_apportion_user_not_member);
 				
 			}
 			break;
          }
    }

	private void addAsProjectMember(ProjectShareAuthorization psa){
		MoneyExpenseApportion apportion = new MoneyExpenseApportion();
		apportion.setFriendUserId(psa.getFriendUserId());
		apportion.setLocalFriendId(psa.getLocalFriendId());
		apportion.setAmount(0.0);
		if(psa.getSharePercentageType() != null && psa.getSharePercentageType().equals("Average")){
			apportion.setApportionType("Average");
		} else {
			apportion.setApportionType("Share");
		}
		if (mApportionFieldApportions.addApportion(apportion,projectShareAuthorization.getProjectId(), ApportionItem.NEW)) {
			mApportionFieldApportions.setTotalAmount(0.0);
		} else {
			HyjUtil.displayToast(R.string.moneyApportionField_select_toast_apportion_user_already_exists);
		}
	}

	private void displayError(Object object){
		((HyjActivity) this.getActivity())
		.dismissProgressDialog();
		JSONObject json = (JSONObject) object;
		HyjUtil.displayToast(json.optJSONObject("__summary").optString(
				"msg"));
	}
 
}
