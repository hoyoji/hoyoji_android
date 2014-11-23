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

import com.activeandroid.ActiveAndroid;
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
import com.hoyoji.hoyoji.models.MoneyDepositIncomeApportion;
import com.hoyoji.hoyoji.models.MoneyDepositIncomeContainer;
import com.hoyoji.hoyoji.models.MoneyDepositIncomeContainer.MoneyDepositIncomeContainerEditor;
import com.hoyoji.hoyoji.models.MoneyDepositReturnApportion;
import com.hoyoji.hoyoji.models.MoneyDepositReturnContainer;
import com.hoyoji.hoyoji.models.MoneyDepositReturnContainer.MoneyDepositReturnContainerEditor;
import com.hoyoji.hoyoji.models.MoneyExpenseApportion;
import com.hoyoji.hoyoji.models.MoneyExpenseContainer;
import com.hoyoji.hoyoji.models.MoneyExpenseContainer.MoneyExpenseContainerEditor;
import com.hoyoji.hoyoji.models.MoneyIncomeApportion;
import com.hoyoji.hoyoji.models.MoneyIncomeContainer;
import com.hoyoji.hoyoji.models.MoneyIncomeContainer.MoneyIncomeContainerEditor;
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
				bundle.putString("LOCAL_FRIENDID", projectShareAuthorization.getLocalFriendId());
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
		if(mApportionFieldApportions.getAdapter().getCount() == 0){
			HyjUtil.displayToast("请选择拆分成员");
			return;
		}

		((HyjActivity) MemberTBDFormFragment.this.getActivity())
		.displayProgressDialog(
				R.string.memberTBDFormFragment_title_split,
				R.string.memberTBDFormFragment_progress_splitting);
	
		ActiveAndroid.beginTransaction();
		try {
			
			doSplitExpenseContainers();
			doSplitIncomeContainers();
			doSplitDepositIncomeContainers();
			doSplitDepositReturnContainers();

			
			ActiveAndroid.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ActiveAndroid.endTransaction();
		
		
		((HyjActivity) MemberTBDFormFragment.this.getActivity()).dismissProgressDialog();
		HyjUtil.displayToast(R.string.memberTBDFormFragment_toast_split_success);
	}	
	 
	 private void doSplitDepositReturnContainers() {
			List<MoneyDepositReturnContainer> moneyDepositReturnContainers = new Select("container.*").from(MoneyDepositReturnContainer.class).as("container")
									.join(MoneyDepositReturnApportion.class).as("apportion").on("container.id = apportion.moneyDepositReturnContainerId").
									where("apportion.localFriendId=?", projectShareAuthorization.getLocalFriendId()).execute();
			
			for(MoneyDepositReturnContainer moneyDepositReturnContainer : moneyDepositReturnContainers){
				List apportions = moneyDepositReturnContainer.getApportions();
				List<ApportionItem> apportionItems = makeMoneyApportionItems(MoneyDepositReturnApportion.class, apportions, moneyDepositReturnContainer.getId());
				MoneyDepositReturnContainer.saveApportions(apportionItems, new MoneyDepositReturnContainerEditor(moneyDepositReturnContainer));
			}
		}
	 
	 private void doSplitDepositIncomeContainers() {
			List<MoneyDepositIncomeContainer> moneyDepositIncomeContainers = new Select("container.*").from(MoneyDepositIncomeContainer.class).as("container")
									.join(MoneyDepositIncomeApportion.class).as("apportion").on("container.id = apportion.moneyDepositIncomeContainerId").
									where("apportion.localFriendId=?", projectShareAuthorization.getLocalFriendId()).execute();
			
			for(MoneyDepositIncomeContainer moneyDepositIncomeContainer : moneyDepositIncomeContainers){
				List apportions = moneyDepositIncomeContainer.getApportions();
				List<ApportionItem> apportionItems = makeMoneyApportionItems(MoneyDepositIncomeApportion.class, apportions, moneyDepositIncomeContainer.getId());
				MoneyDepositIncomeContainer.saveApportions(apportionItems, new MoneyDepositIncomeContainerEditor(moneyDepositIncomeContainer));
			}
		}
	 
	 private void doSplitIncomeContainers() {
		List<MoneyIncomeContainer> moneyIncomeContainers = new Select("container.*").from(MoneyIncomeContainer.class).as("container")
								.join(MoneyIncomeApportion.class).as("apportion").on("container.id = apportion.moneyIncomeContainerId").
								where("apportion.localFriendId=?", projectShareAuthorization.getLocalFriendId()).execute();
		
		for(MoneyIncomeContainer moneyIncomeContainer : moneyIncomeContainers){
			List apportions = moneyIncomeContainer.getApportions();
			List<ApportionItem> apportionItems = makeMoneyApportionItems(MoneyIncomeApportion.class, apportions, moneyIncomeContainer.getId());
			MoneyIncomeContainer.saveApportions(apportionItems, new MoneyIncomeContainerEditor(moneyIncomeContainer));
		}
	}
	 
	private void doSplitExpenseContainers() {
		List<MoneyExpenseContainer> moneyExpenseContainers = new Select("container.*").from(MoneyExpenseContainer.class).as("container")
								.join(MoneyExpenseApportion.class).as("apportion").on("container.id = apportion.moneyExpenseContainerId").
								where("apportion.localFriendId=?", projectShareAuthorization.getLocalFriendId()).execute();
		
		for(MoneyExpenseContainer moneyExpenseContainer : moneyExpenseContainers){
			List apportions = moneyExpenseContainer.getApportions();//new Select().from(MoneyExpenseApportion.class).where("moneyExpenseContainerId=?", moneyExpenseContainer.getId()).execute(); 
			List<ApportionItem> apportionItems = makeMoneyApportionItems(MoneyExpenseApportion.class, apportions, moneyExpenseContainer.getId());
			
			MoneyExpenseContainer.saveApportions(apportionItems, new MoneyExpenseContainerEditor(moneyExpenseContainer));
		}
	}
	
	private List<ApportionItem> makeMoneyApportionItems(Class<? extends MoneyApportion> modelClass, List<MoneyApportion> apportions, String apportionContainerId){
		Set<String> apportionsSet = new HashSet<String>();
		List<ApportionItem> apportionItems = new ArrayList<ApportionItem>();
		MoneyApportion apportionTBD = null;
		double amountTBD = 0.0;
		ApportionItem apiTBD = null;
		
		for(int i = 0; i < apportions.size(); i++){
			MoneyApportion apportion = apportions.get(i);
			if(!projectShareAuthorization.getLocalFriendId().equals(apportion.getLocalFriendId())){
				apportionsSet.add(HyjUtil.ifNull(apportion.getFriendUserId(), apportion.getLocalFriendId()));
			} else {
				apportionTBD = apportion;
				amountTBD = apportionTBD.getAmount();
			}
		}
		
		for(int i = 0; i < mApportionFieldApportions.getAdapter().getCount(); i++){
			ApportionItem<MoneyApportion> api = mApportionFieldApportions.getAdapter().getItem(i);
			MoneyApportion apiApportion = api.getApportion();
			
			if(!apportionsSet.contains(HyjUtil.ifNull(apiApportion.getFriendUserId(), apiApportion.getLocalFriendId()))){
				MoneyApportion apportion;
				try {
					if(projectShareAuthorization.getLocalFriendId().equals(apiApportion.getLocalFriendId())){
						apiTBD = api;
						apportion = apportionTBD;
					} else {
						apportion = (MoneyApportion) modelClass.newInstance();
						apportion.setFriendUserId(apiApportion.getFriendUserId());
						apportion.setLocalFriendId(apiApportion.getLocalFriendId());
						apportion.setMoneyId(apportionContainerId);
					}
					
					apportion.setApportionType(api.getApportionType());
					
					ApportionItem apportionItem = new ApportionItem(apportion, projectShareAuthorization.getProject().getId(), ApportionItem.NEW);
					apportionItems.add(apportionItem);
					
//					apportionsSet.add(HyjUtil.ifNull(apiApportion.getFriendUserId(), apiApportion.getLocalFriendId()));
					
					
				} catch (java.lang.InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		
		// 如果拆分里没有待定成员，我们把原来的待定成员移除
		if(apiTBD == null && (amountTBD == 0.0 || apportionItems.size() > 0)){
			ApportionItem apportionItemTBD = new ApportionItem(apportionTBD, projectShareAuthorization.getProject().getId(), ApportionItem.DELETED);
			apportionItems.add(apportionItemTBD);
		}
		
		if(apportionItems.size() > 0){
			setApportionsAmount(apportionItems, amountTBD);
		}
		return apportionItems;
	}
	
	private void setApportionsAmount(List<ApportionItem> mergedApportions, Double totalAmount){
		double fixedTotal = 0.0;
		double sharePercentageTotal = 0.0;
		
		double averageAmount = 0.0;
		double shareTotal = 0.0;
		int numOfAverage = 0;
		
		for(int i = 0; i < mergedApportions.size(); i++){
			ApportionItem<MoneyApportion> api = (ApportionItem<MoneyApportion>) mergedApportions.get(i);
			if(api.getState() != ApportionItem.DELETED) {
				if(api.getApportionType().equalsIgnoreCase("Average")){
					numOfAverage++;
					sharePercentageTotal += api.getSharePercentage();
				} else if(api.getApportionType().equalsIgnoreCase("Share")){
//					api.setAmount(api.getAmount());
					sharePercentageTotal += api.getSharePercentage();
				} else {
					fixedTotal += api.getAmount();
				}
			}
		}
		
		
		// 占股分摊=（总金额-定额分摊）*占股/（分摊人所占股数）
		shareTotal = totalAmount - fixedTotal;
		for(int i = 0; i < mergedApportions.size(); i++){
			ApportionItem<MoneyApportion> api = (ApportionItem<MoneyApportion>) mergedApportions.get(i);
			if(api.getState() != ApportionItem.DELETED) {
				if(api.getApportionType().equalsIgnoreCase("Share")){
					Double shareAmount = shareTotal * api.getSharePercentage() / sharePercentageTotal;
					api.setAmount(shareAmount);
					fixedTotal += api.getAmount();
				}
			}
		}
		
		// 平均分摊 = （总金额-定额分摊-占股分摊） / 平均分摊人数
		averageAmount = (totalAmount - fixedTotal) / numOfAverage;
		ApportionItem firstNonDeletedItem = null;
		for(int i = 0; i < mergedApportions.size(); i++){
			ApportionItem<MoneyApportion> api = (ApportionItem<MoneyApportion>) mergedApportions.get(i);
			if(api.getState() != ApportionItem.DELETED) {
				if(api.getApportionType().equalsIgnoreCase("Average")){
					api.setAmount(averageAmount);
					fixedTotal += api.getAmount();
				}
				if(firstNonDeletedItem == null){
					firstNonDeletedItem = api;
				}
			}
		}
		if(mergedApportions.size() > 0){
			if(fixedTotal != totalAmount && firstNonDeletedItem != null){
				double adjustedAmount = firstNonDeletedItem.getAmount() + (totalAmount - fixedTotal);
				firstNonDeletedItem.setAmount(adjustedAmount);
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode){
         case GET_APPORTION_MEMBER_ID:
 			if (resultCode == Activity.RESULT_OK) {
 				String type = data.getStringExtra("MODEL_TYPE");
 				long _id = data.getLongExtra("MODEL_ID", -1);
 				if(_id != -1){
 					AddApportionMember(type, _id);
 				} else {
 					long[] _ids = data.getLongArrayExtra("MODEL_IDS");
 					if(_ids != null){
 						for(int i=0; i<_ids.length; i++){
 							AddApportionMember(type, _ids[i]);
 						}
 					}
 				}
 				
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

	private void AddApportionMember(String type, long _id) {
			ProjectShareAuthorization psa = null;
			if("ProjectShareAuthorization".equalsIgnoreCase(type)){
				psa = ProjectShareAuthorization.load(ProjectShareAuthorization.class, _id);
			} else {
				final Friend friend = Friend.load(Friend.class, _id);
				//看一下该好友是不是账本成员
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
					return;
				}
			}
			addAsProjectMember(psa);
		
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
