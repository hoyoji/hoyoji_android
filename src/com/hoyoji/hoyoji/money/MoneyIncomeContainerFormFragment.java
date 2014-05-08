package com.hoyoji.hoyoji.money;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjHttpGetExchangeRateAsyncTask;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.activity.HyjActivity.DialogCallbackListener;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeField;
import com.hoyoji.android.hyjframework.view.HyjImageField;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjRemarkField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.android.hyjframework.view.HyjImageField.PictureItem;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyApportion;
import com.hoyoji.hoyoji.models.MoneyBorrow;
import com.hoyoji.hoyoji.models.MoneyExpense;
import com.hoyoji.hoyoji.models.MoneyIncome;
import com.hoyoji.hoyoji.models.MoneyIncomeApportion;
import com.hoyoji.hoyoji.models.MoneyIncomeCategory;
import com.hoyoji.hoyoji.models.MoneyIncomeContainer;
import com.hoyoji.hoyoji.models.MoneyLend;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.MoneyApportionField.ApportionItem;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.money.moneycategory.MoneyIncomeCategoryListFragment;
import com.hoyoji.hoyoji.project.MemberListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;


public class MoneyIncomeContainerFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_FRIEND_ID = 3;
	private final static int GET_APPORTION_MEMBER_ID = 4;
	private final static int GET_CATEGORY_ID = 5;
	
	private int CREATE_EXCHANGE = 0;
	private int SET_EXCHANGE_RATE_FLAG = 1;
	
	private MoneyIncomeContainerEditor mMoneyIncomeContainerEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private MoneyApportionField mApportionFieldApportions = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericAmount = null;
	private HyjSelectorField mSelectorFieldMoneyAccount = null;
	private HyjSelectorField mSelectorFieldProject = null;
	private HyjNumericField mNumericExchangeRate = null;
	private HyjSelectorField mSelectorFieldMoneyIncomeCategory = null;
	private HyjSelectorField mSelectorFieldFriend = null;
	private ImageView mImageViewClearFriend = null;
	private HyjRemarkField mRemarkFieldRemark = null;
	private ImageView mImageViewRefreshRate = null;
	private View mViewSeparatorExchange = null;
	private LinearLayout mLinearLayoutExchangeRate = null;
	
	private boolean hasEditPermission = true;
	
	@Override
	public Integer useContentView() {
		return R.layout.money_formfragment_moneyincome;
	}
	 
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		final MoneyIncomeContainer moneyIncomeContainer;
		
		Intent intent = getActivity().getIntent();
		long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			moneyIncomeContainer =  new Select().from(MoneyIncomeContainer.class).where("_id=?", modelId).executeSingle();
			hasEditPermission = moneyIncomeContainer.hasEditPermission();
		} else {
			moneyIncomeContainer = new MoneyIncomeContainer();
			if(intent.getStringExtra("counterpartId") != null){
				moneyIncomeContainer.setMoneyExpenseId(intent.getStringExtra("counterpartId"));
			}
		}
//		mMoneyIncomeEditor = moneyIncome.newModelEditor();
		mMoneyIncomeContainerEditor = new MoneyIncomeContainerEditor(moneyIncomeContainer);
		
		setupDeleteButton(mMoneyIncomeContainerEditor);
		
		mImageFieldPicture = (HyjImageField) getView().findViewById(R.id.moneyIncomeFormFragment_imageField_picture);
		mImageFieldPicture.setImages(moneyIncomeContainer.getPictures());
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_date);	
		if(modelId != -1){
			mDateTimeFieldDate.setText(moneyIncomeContainer.getDate());
		}

		setupApportionField(moneyIncomeContainer);
		
		mNumericAmount = (HyjNumericField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_amount);		
		double amount = intent.getDoubleExtra("amount", -1.0);//从分享消息导入的金额
		if(amount >= 0.0){
			mNumericAmount.setNumber(amount);
			mApportionFieldApportions.setTotalAmount(amount);
		}else{
			mNumericAmount.setNumber(moneyIncomeContainer.getAmount());
		}
		if(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor() != null){
			mNumericAmount.getEditText().setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
			mNumericAmount.getEditText().setHintTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getIncomeColor()));
		}
		
		mNumericAmount.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				mApportionFieldApportions.setTotalAmount(mNumericAmount
						.getNumber());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
		
		MoneyAccount moneyAccount = moneyIncomeContainer.getMoneyAccount();
		mSelectorFieldMoneyAccount = (HyjSelectorField) getView().findViewById(R.id.moneyIncomeFormFragment_selectorField_moneyAccount);
		
		if(moneyAccount != null){
			mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
			mSelectorFieldMoneyAccount.setText(moneyAccount.getName() + "(" + moneyAccount.getCurrencyId() + ")");
		}
		mSelectorFieldMoneyAccount.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("excludeType", "Debt");
				
				MoneyIncomeContainerFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, bundle, GET_MONEYACCOUNT_ID);
			}
		});	
		
		Project project;
		String projectId = intent.getStringExtra("projectId");//从消息导入
		if(projectId != null){
			project = HyjModel.getModel(Project.class, projectId);
		}else{
			project = moneyIncomeContainer.getProject();
		}
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(R.id.moneyIncomeFormFragment_selectorField_project);
		
		if(project != null){
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getDisplayName() + "(" + project.getCurrencyId() + ")");
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyIncomeContainerFormFragment.this.openActivityWithFragmentForResult(ProjectListFragment.class, R.string.projectListFragment_title_select_project, null, GET_PROJECT_ID);
			}
		});	
		
		mNumericExchangeRate = (HyjNumericField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_exchangeRate);		
		mNumericExchangeRate.setNumber(moneyIncomeContainer.getExchangeRate());
		//mNumericExchangeRate.setVisibility(View.GONE);
		
		mViewSeparatorExchange = (View) getView().findViewById(R.id.moneyIncomeFormFragment_separatorField_exchange);
		mLinearLayoutExchangeRate = (LinearLayout) getView().findViewById(R.id.moneyIncomeFormFragment_linearLayout_exchangeRate);
		
		mSelectorFieldMoneyIncomeCategory = (HyjSelectorField) getView().findViewById(
				R.id.moneyIncomeFormFragment_textField_moneyIncomeCategory);
		mSelectorFieldMoneyIncomeCategory.setText(moneyIncomeContainer
				.getMoneyIncomeCategory());
		if(moneyIncomeContainer.getMoneyIncomeCategoryMain() != null && moneyIncomeContainer.getMoneyIncomeCategoryMain().length() > 0){
			mSelectorFieldMoneyIncomeCategory.setLabel(moneyIncomeContainer.getMoneyIncomeCategoryMain());
		}
		mSelectorFieldMoneyIncomeCategory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneyIncomeContainerFormFragment.this
						.openActivityWithFragmentForResult(
								MoneyIncomeCategoryListFragment.class,
								R.string.moneyIncomeFormFragment_editText_hint_moneyIncomeCategory,
								null, GET_CATEGORY_ID);
			}
		});
		
		Friend friend;
		String friendUserId = intent.getStringExtra("friendUserId");//从消息导入
		if(friendUserId != null){
			friend = new Select().from(Friend.class).where("friendUserId=?",friendUserId).executeSingle();
		}else{
			friend = moneyIncomeContainer.getFriend();
		}
		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(R.id.moneyIncomeFormFragment_selectorField_friend);
		
		if(friend != null){
			mSelectorFieldFriend.setModelId(friend.getId());
			mSelectorFieldFriend.setText(friend.getDisplayName());
		}
		mSelectorFieldFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyIncomeContainerFormFragment.this
				.openActivityWithFragmentForResult(FriendListFragment.class, R.string.friendListFragment_title_select_friend_payee, null, GET_FRIEND_ID);
			}
		}); 
		
		mImageViewClearFriend = (ImageView) getView().findViewById(
				R.id.moneyIncomeFormFragment_imageView_clear_friend);
		mImageViewClearFriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSelectorFieldFriend.setModelId(null);
				mSelectorFieldFriend.setText("");
			}
		});
		
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyIncomeContainer.getRemark());
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyIncomeContainer.getRemark());
		
		ImageView takePictureButton = (ImageView) getView().findViewById(R.id.moneyIncomeFormFragment_imageView_camera);	
		takePictureButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				PopupMenu popup = new PopupMenu(getActivity(), v);
				MenuInflater inflater = popup.getMenuInflater();
				inflater.inflate(R.menu.picture_get_picture, popup.getMenu());
				popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						if (item.getItemId() == R.id.picture_take_picture) {
							mImageFieldPicture.takePictureFromCamera();
							return true;
						} else {
							mImageFieldPicture.pickPictureFromGallery();
							return true;
						}
						// return false;
					}
				});
				
				if(!hasEditPermission){
					for(int i = 0; i<popup.getMenu().size();i++){
						popup.getMenu().setGroupEnabled(i, false);
					}
				}
				
				popup.show();	
			}
		});
		
		mImageViewRefreshRate = (ImageView) getView().findViewById(R.id.moneyIncomeFormFragment_imageButton_refresh_exchangeRate);	
		mImageViewRefreshRate.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mSelectorFieldMoneyAccount.getModelId() != null && mSelectorFieldProject.getModelId() != null){
					MoneyAccount moneyAccount = (MoneyAccount)HyjModel.getModel(MoneyAccount.class, mSelectorFieldMoneyAccount.getModelId());
					Project project = HyjModel.getModel(Project.class, mSelectorFieldProject.getModelId());
					
					String fromCurrency = moneyAccount.getCurrencyId();
					String toCurrency = project.getCurrencyId();
					if(fromCurrency != null && toCurrency != null){
						HyjUtil.startRoateView(mImageViewRefreshRate);
						mImageViewRefreshRate.setEnabled(false);
						HyjUtil.updateExchangeRate(fromCurrency, toCurrency, mImageViewRefreshRate, mNumericExchangeRate);
					} else {
						HyjUtil.displayToast(R.string.moneyIncomeFormFragment_toast_select_currency);
					}
				}else{
					HyjUtil.displayToast(R.string.moneyIncomeFormFragment_toast_select_currency);
				}
			}
		});
		
			
			getView().findViewById(R.id.moneyIncomeFormFragment_imageButton_apportion_add).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Bundle bundle = new Bundle();
					Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
					bundle.putLong("MODEL_ID", project.get_mId());
					openActivityWithFragmentForResult(SelectApportionMemberListFragment.class, R.string.moneyApportionField_select_apportion_member, bundle, GET_APPORTION_MEMBER_ID);
				}
			});
			
			getView().findViewById(R.id.moneyIncomeFormFragment_imageButton_apportion_add_all).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					addAllProjectMemberIntoApportionsField(moneyIncomeContainer);
				}
			});
			
			getView().findViewById(R.id.moneyIncomeFormFragment_imageButton_apportion_more_actions).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					PopupMenu popup = new PopupMenu(getActivity(), v);
					MenuInflater inflater = popup.getMenuInflater();
					inflater.inflate(R.menu.money_apportionfield_more_actions, popup.getMenu());
					popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
//							if (item.getItemId() == R.id.moneyApportionField_menu_moreActions_add_non_project_member) {
//								Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
//								List<ProjectShareAuthorization> projectShareAuthorizations = project.getShareAuthorizations();
//								for (int i = 0; i < projectShareAuthorizations.size(); i++) {
//									if(!projectShareAuthorizations.get(i).getState().equalsIgnoreCase("Accept")){
//										continue;
//									}
//									MoneyIncomeApportion apportion = new MoneyIncomeApportion();
//									apportion.setAmount(0.0);
//									apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
//									apportion.setMoneyIncomeId(moneyIncome.getId());
//
//									mApportionFieldApportions.addApportion(apportion, project.getId(), ApportionItem.NEW);
//								}
//								mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
//								return true;
//							} else 
								if (item.getItemId() == R.id.moneyApportionField_menu_moreActions_clear) {
								mApportionFieldApportions.clearAll();
								mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
								return true;
							} else if (item.getItemId() == R.id.moneyApportionField_menu_moreActions_all_average) {
								mApportionFieldApportions.setAllApportionAverage();
								mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
								return true;
							} else if (item.getItemId() == R.id.moneyApportionField_menu_moreActions_all_share) {
								mApportionFieldApportions.setAllApportionShare();
								mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
								return true;
							}
							return false;
						}
					});
					
					if(!hasEditPermission){
						for(int i = 0; i<popup.getMenu().size();i++){
							popup.getMenu().setGroupEnabled(i, false);
						}
					}
					
					popup.show();
				}
			});
			// 只在新增时才自动打开软键盘， 修改时不自动打开
			if (modelId == -1) {
				setExchangeRate(false);
				this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			}else{
				setExchangeRate(true);
			}
			setPermission();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    super.onCreateOptionsMenu(menu, inflater);
	    if(mMoneyIncomeContainerEditor!= null && mMoneyIncomeContainerEditor.getModelCopy().get_mId() != null && !hasEditPermission){
	    	hideSaveAction();
	    }
	}
	
	private void addAllProjectMemberIntoApportionsField(MoneyIncomeContainer moneyIncomeContainer) {
		Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
		List<ProjectShareAuthorization> projectShareAuthorizations = project.getShareAuthorizations();
		for (int i = 0; i < projectShareAuthorizations.size(); i++) {
			if(!projectShareAuthorizations.get(i).getState().equalsIgnoreCase("Accept")){
				continue;
			}
			MoneyIncomeApportion apportion = new MoneyIncomeApportion();
			apportion.setAmount(0.0);
			apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
			apportion.setMoneyIncomeContainerId(moneyIncomeContainer.getId());

			mApportionFieldApportions.addApportion(apportion, project.getId(), ApportionItem.NEW);
		}
		mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
		
	}
	
	private void setupApportionField(MoneyIncomeContainer moneyIncomeContainer) {

		mApportionFieldApportions = (MoneyApportionField) getView().findViewById(R.id.moneyIncomeFormFragment_apportionField);
		
		List<MoneyIncomeApportion> moneyApportions = null;
		
		if(mMoneyIncomeContainerEditor.getModelCopy().get_mId() == null) {
			
			moneyApportions = new ArrayList<MoneyIncomeApportion>();
			if(moneyIncomeContainer.getProject() != null && moneyIncomeContainer.getProject().getAutoApportion()){
				List<ProjectShareAuthorization> projectShareAuthorizations = moneyIncomeContainer.getProject().getShareAuthorizations();
				for(int i=0; i < projectShareAuthorizations.size(); i++){
					MoneyIncomeApportion apportion = new MoneyIncomeApportion();
					apportion.setAmount(0.0);
					apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
					apportion.setMoneyIncomeContainerId(moneyIncomeContainer.getId());
					apportion.setApportionType("Share");
					
					moneyApportions.add(apportion);
				}
			} else if(moneyIncomeContainer.getProject() != null) {
				MoneyIncomeApportion apportion = new MoneyIncomeApportion();
				apportion.setAmount(moneyIncomeContainer.getAmount0());
				apportion.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
				apportion.setMoneyIncomeContainerId(moneyIncomeContainer.getId());
				apportion.setApportionType("Average");
				moneyApportions.add(apportion);
			}
			
		} else {
			moneyApportions = moneyIncomeContainer.getApportions();
		}
		
		mApportionFieldApportions.init(moneyIncomeContainer.getAmount0(), moneyApportions, moneyIncomeContainer.getProjectId(), moneyIncomeContainer.getId());
	}
	
	private void setupDeleteButton(HyjModelEditor<MoneyIncomeContainer> moneyIncomeContainerEditor) {

		Button buttonDelete = (Button) getView().findViewById(R.id.button_delete);
		
		final MoneyIncomeContainer moneyIncomeContainer = moneyIncomeContainerEditor.getModelCopy();
		
		if (moneyIncomeContainer.get_mId() == null) {
			buttonDelete.setVisibility(View.GONE);
		} else {
			buttonDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(moneyIncomeContainer.hasDeletePermission()){
					((HyjActivity)getActivity()).displayDialog(R.string.app_action_delete_list_item, R.string.app_confirm_delete, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
							new DialogCallbackListener() {
								@Override
								public void doPositiveClick(Object object) {
									try {
										ActiveAndroid.beginTransaction();

										MoneyAccount moneyAccount = moneyIncomeContainer.getMoneyAccount();
										HyjModelEditor<MoneyAccount> moneyAccountEditor = moneyAccount.newModelEditor();
										moneyAccountEditor.getModelCopy().setCurrentBalance(moneyAccount.getCurrentBalance() - moneyIncomeContainer.getAmount());
										MoneyIncomeContainerEditor moneyIncomeContainerEditor = new MoneyIncomeContainerEditor(moneyIncomeContainer);
										
										//删除收入的同时删除分摊
										Iterator<MoneyIncomeApportion> moneyIncomeApportions = moneyIncomeContainer.getApportions().iterator();
										while (moneyIncomeApportions.hasNext()) {
											MoneyIncomeApportion moneyIncomeAportion = moneyIncomeApportions.next();
											ProjectShareAuthorization oldProjectShareAuthorization;
											
											// 非项目好友不用更新项目分摊
											if(moneyIncomeAportion.getFriendUserId() != null){
												if(moneyIncomeAportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
													// 更新旧项目的分摊收入
													oldProjectShareAuthorization = moneyIncomeContainerEditor.getOldSelfProjectShareAuthorization();
												} else {
													// 更新旧项目分摊收入
													oldProjectShareAuthorization = moneyIncomeAportion.getProjectShareAuthorization();
												}
												
												HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
												oldProjectShareAuthorizationEditor.getModelCopy().setApportionedTotalIncome(oldProjectShareAuthorizationEditor.getModelCopy().getApportionedTotalIncome() - (moneyIncomeAportion.getAmount0() * moneyIncomeAportion.getMoneyIncomeContainer().getExchangeRate()));
												oldProjectShareAuthorizationEditor.save();
											}
											
											moneyIncomeAportion.delete();
										}
										
										//更新支出所有者的实际支出
										MoneyIncomeContainer oldMoneyIncomeContainerModel = moneyIncomeContainerEditor.getModelCopy();
										ProjectShareAuthorization oldSelfProjectAuthorization = moneyIncomeContainerEditor.getOldSelfProjectShareAuthorization();
										HyjModelEditor<ProjectShareAuthorization> oldSelfProjectAuthorizationEditor = oldSelfProjectAuthorization.newModelEditor();
										oldSelfProjectAuthorizationEditor.getModelCopy().setActualTotalIncome(oldSelfProjectAuthorization.getActualTotalIncome() - oldMoneyIncomeContainerModel.getAmount0()*oldMoneyIncomeContainerModel.getExchangeRate());
										oldSelfProjectAuthorizationEditor.save();
										
										moneyIncomeContainer.delete();
										moneyAccountEditor.save();

										HyjUtil.displayToast(R.string.app_delete_success);
										ActiveAndroid.setTransactionSuccessful();
										ActiveAndroid.endTransaction();
										getActivity().finish();
									} catch (Exception e) {
										ActiveAndroid.endTransaction();
										HyjUtil.displayToast(R.string.app_delete_failed);
									} 
								}
							});
					}else{
						HyjUtil.displayToast(R.string.app_permission_no_delete);
					}
				}
			});
		}
		
	}
	
	private void setPermission(){

		if(mMoneyIncomeContainerEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			mDateTimeFieldDate.setEnabled(false);
			
			mNumericAmount.setNumber(mMoneyIncomeContainerEditor.getModel().getLocalAmount());
			mNumericAmount.setEnabled(false);
			
			mSelectorFieldMoneyIncomeCategory.setEnabled(false);

			mSelectorFieldFriend.setEnabled(false);
			
			mSelectorFieldMoneyAccount.setEnabled(false);
			mSelectorFieldMoneyAccount.setVisibility(View.GONE);

			mSelectorFieldProject.setEnabled(false);
			
			mNumericExchangeRate.setEnabled(false);
			
			mApportionFieldApportions.setEnabled(false);

			mRemarkFieldRemark.setEnabled(false);
			
			if(this.mOptionsMenu != null){
		    	hideSaveAction();
			}
			
			getView().findViewById(R.id.moneyIncomeFormFragment_imageButton_apportion_add).setEnabled(false);
			getView().findViewById(R.id.moneyIncomeFormFragment_imageButton_apportion_add_all).setEnabled(false);
//			getView().findViewById(R.id.button_save).setEnabled(false);	
			getView().findViewById(R.id.button_delete).setEnabled(false);
			getView().findViewById(R.id.button_delete).setVisibility(View.GONE);
		}
	}
	
	private void setExchangeRate(Boolean editInit){
		if(mSelectorFieldMoneyAccount.getModelId() != null && mSelectorFieldProject.getModelId()!= null){
			MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class,mSelectorFieldMoneyAccount.getModelId());
			Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
			
			String fromCurrency = moneyAccount.getCurrencyId();
			String toCurrency = project.getCurrencyId();
			
			if(fromCurrency.equals(toCurrency)){
				if(SET_EXCHANGE_RATE_FLAG != 1){//新增或修改打开时不做setNumber
					mNumericExchangeRate.setNumber(1.00);
					CREATE_EXCHANGE = 0;
				}
				mLinearLayoutExchangeRate.setVisibility(View.GONE);
				mViewSeparatorExchange.setVisibility(View.GONE);
			}else{
				mLinearLayoutExchangeRate.setVisibility(View.VISIBLE);
				mViewSeparatorExchange.setVisibility(View.VISIBLE);
				
				if(!editInit){//修改时init不需要set Rate
					Double rate = Exchange.getExchangeRate(fromCurrency, toCurrency);
					if(rate != null){
						mNumericExchangeRate.setNumber(rate);
						CREATE_EXCHANGE = 0;
					}else{
						mNumericExchangeRate.setText(null);
						CREATE_EXCHANGE = 1;
					}
				}
			}
			
		}else{
			mViewSeparatorExchange.setVisibility(View.GONE);
			mLinearLayoutExchangeRate.setVisibility(View.GONE);
		}
			SET_EXCHANGE_RATE_FLAG = 0;
	}
	
	private void fillData(){
		MoneyIncomeContainer modelCopy =  mMoneyIncomeContainerEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setAmount(mNumericAmount.getNumber());
		modelCopy.setMoneyAccountId(mSelectorFieldMoneyAccount.getModelId());
		modelCopy.setProjectId(mSelectorFieldProject.getModelId());
		modelCopy.setExchangeRate(mNumericExchangeRate.getNumber());
		modelCopy.setMoneyIncomeCategory(mSelectorFieldMoneyIncomeCategory.getText());
		modelCopy.setMoneyIncomeCategoryMain(mSelectorFieldMoneyIncomeCategory.getLabel());
		
		if(mSelectorFieldFriend.getModelId() != null){
			Friend friend = HyjModel.getModel(Friend.class, mSelectorFieldFriend.getModelId());
			modelCopy.setFriend(friend);
		}else{
			modelCopy.setFriend(null);
		}
		
		modelCopy.setRemark(mRemarkFieldRemark.getText().toString().trim());
		
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		mDateTimeFieldDate.setError(mMoneyIncomeContainerEditor.getValidationError("datetime"));
		mNumericAmount.setError(mMoneyIncomeContainerEditor.getValidationError("amount"));
		if(mMoneyIncomeContainerEditor.getValidationError("amount") != null){
					mNumericAmount.showSoftKeyboard();
				}
		mSelectorFieldMoneyAccount.setError(mMoneyIncomeContainerEditor.getValidationError("moneyAccount"));
		mSelectorFieldProject.setError(mMoneyIncomeContainerEditor.getValidationError("project"));
		mNumericExchangeRate.setError(mMoneyIncomeContainerEditor.getValidationError("exchangeRate"));
		mSelectorFieldMoneyIncomeCategory.setError(mMoneyIncomeContainerEditor.getValidationError("moneyIncomeCategory"));
		mSelectorFieldFriend.setError(mMoneyIncomeContainerEditor.getValidationError("friend"));
		mRemarkFieldRemark.setError(mMoneyIncomeContainerEditor.getValidationError("remark"));
		mApportionFieldApportions.setError(mMoneyIncomeContainerEditor.getValidationError("apportionTotalAmount"));
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		if(mMoneyIncomeContainerEditor.getModelCopy().get_mId() == null && !mMoneyIncomeContainerEditor.getModelCopy().hasAddNewPermission(mMoneyIncomeContainerEditor.getModelCopy().getProjectId())){
			HyjUtil.displayToast(R.string.app_permission_no_addnew);
		}else if(mMoneyIncomeContainerEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			HyjUtil.displayToast(R.string.app_permission_no_edit);
		}else{
		
		mMoneyIncomeContainerEditor.validate();
		
		if (mApportionFieldApportions.getCount() > 0) {
			if (mNumericAmount.getNumber() != null && !mNumericAmount.getNumber().equals(mApportionFieldApportions.getTotalAmount())) {
				mMoneyIncomeContainerEditor.setValidationError("apportionTotalAmount",R.string.moneyApportionField_select_toast_apportion_totalAmount_not_equal);
			} else {
				mMoneyIncomeContainerEditor.removeValidationError("apportionTotalAmount");
			}
		} else {
			mMoneyIncomeContainerEditor.removeValidationError("apportionTotalAmount");
		}
		
		if(mMoneyIncomeContainerEditor.hasValidationErrors()){
			showValidatioErrors();
		} else {
			try {
				ActiveAndroid.beginTransaction();
				
				savePictures();

				saveApportions();
				
				MoneyIncomeContainer oldMoneyIncomeContainerModel = mMoneyIncomeContainerEditor.getModel();
				MoneyIncomeContainer moneyIncomeContainerModel = mMoneyIncomeContainerEditor.getModelCopy();
				
				//设置默认项目和账户
				UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
				if(moneyIncomeContainerModel.get_mId() == null && !userData.getActiveMoneyAccountId().equals(moneyIncomeContainerModel.getMoneyAccountId()) || !userData.getActiveProjectId().equals(moneyIncomeContainerModel.getProjectId())){
					HyjModelEditor<UserData> userDataEditor = userData.newModelEditor();
					userDataEditor.getModelCopy().setActiveMoneyAccountId(moneyIncomeContainerModel.getMoneyAccountId());
					userDataEditor.getModelCopy().setActiveProjectId(moneyIncomeContainerModel.getProjectId());
					userDataEditor.save();
				}
				
				// 更新项目的默认分类
				if(moneyIncomeContainerModel.get_mId() == null){
					HyjModelEditor<Project> projectEditor = moneyIncomeContainerModel.getProject().newModelEditor();
					projectEditor.getModelCopy().setDefaultIncomeCategory(moneyIncomeContainerModel.getMoneyIncomeCategory());
					projectEditor.getModelCopy().setDefaultIncomeCategoryMain(moneyIncomeContainerModel.getMoneyIncomeCategoryMain());
					projectEditor.save();
				}
				
				//当前汇率不存在时，创建汇率
				String localCurrencyId = moneyIncomeContainerModel.getMoneyAccount().getCurrencyId();
				String foreignCurrencyId = moneyIncomeContainerModel.getProject().getCurrencyId();
				if(CREATE_EXCHANGE == 1){
					MoneyAccount moneyAccount = moneyIncomeContainerModel.getMoneyAccount();
					Project project = moneyIncomeContainerModel.getProject();
					
					Exchange newExchange = new Exchange();
					newExchange.setLocalCurrencyId(moneyAccount.getCurrencyId());
					newExchange.setForeignCurrencyId(project.getCurrencyId());
					newExchange.setRate(moneyIncomeContainerModel.getExchangeRate());
					newExchange.save();
				}else if(!localCurrencyId.equalsIgnoreCase(foreignCurrencyId)){
					Exchange exchange = Exchange.getExchange(localCurrencyId, foreignCurrencyId);
					Double rate = HyjUtil.toFixed2(moneyIncomeContainerModel.getExchangeRate());
					if(exchange != null){
						if(exchange.getRate() != rate){
							HyjModelEditor<Exchange> exchangModelEditor = exchange.newModelEditor();
							exchangModelEditor.getModelCopy().setRate(rate);
							exchangModelEditor.save();
						}
					}
				}
				
				    MoneyAccount oldMoneyAccount = oldMoneyIncomeContainerModel.getMoneyAccount();
					MoneyAccount newMoneyAccount = moneyIncomeContainerModel.getMoneyAccount();
					HyjModelEditor<MoneyAccount> newMoneyAccountEditor = newMoneyAccount.newModelEditor();
					
					//更新账户余额
					if(moneyIncomeContainerModel.get_mId() == null || oldMoneyAccount.getId().equals(newMoneyAccount.getId())){
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() - oldMoneyIncomeContainerModel.getAmount0() + moneyIncomeContainerModel.getAmount0());
					}else{
						HyjModelEditor<MoneyAccount> oldMoneyAccountEditor = oldMoneyAccount.newModelEditor();
						oldMoneyAccountEditor.getModelCopy().setCurrentBalance(oldMoneyAccount.getCurrentBalance() - oldMoneyIncomeContainerModel.getAmount0());
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() + moneyIncomeContainerModel.getAmount0());
						oldMoneyAccountEditor.save();
					}
					newMoneyAccountEditor.save();

					//更新支出所有者的实际收入
					ProjectShareAuthorization selfProjectAuthorization = mMoneyIncomeContainerEditor.getNewSelfProjectShareAuthorization();
					HyjModelEditor<ProjectShareAuthorization> selfProjectAuthorizationEditor = selfProjectAuthorization.newModelEditor();
				    
					if(moneyIncomeContainerModel.get_mId() == null || oldMoneyIncomeContainerModel.getProjectId().equals(moneyIncomeContainerModel.getProjectId())){
				    	selfProjectAuthorizationEditor.getModelCopy().setActualTotalIncome(selfProjectAuthorization.getActualTotalIncome() - oldMoneyIncomeContainerModel.getAmount0()*oldMoneyIncomeContainerModel.getExchangeRate() + moneyIncomeContainerModel.getAmount0()*moneyIncomeContainerModel.getExchangeRate());
					}else{
						selfProjectAuthorizationEditor.getModelCopy().setActualTotalIncome(selfProjectAuthorization.getActualTotalIncome() + moneyIncomeContainerModel.getAmount0()*moneyIncomeContainerModel.getExchangeRate());
						
						ProjectShareAuthorization oldSelfProjectAuthorization = mMoneyIncomeContainerEditor.getOldSelfProjectShareAuthorization();
						HyjModelEditor<ProjectShareAuthorization> oldSelfProjectAuthorizationEditor = oldSelfProjectAuthorization.newModelEditor();
						oldSelfProjectAuthorizationEditor.getModelCopy().setActualTotalIncome(oldSelfProjectAuthorization.getActualTotalIncome() - oldMoneyIncomeContainerModel.getAmount0()*oldMoneyIncomeContainerModel.getExchangeRate());
						oldSelfProjectAuthorizationEditor.save();
					}
					selfProjectAuthorizationEditor.save();
			
					//更新分类，使之成为最近使用过的
					if(this.mSelectorFieldMoneyIncomeCategory.getModelId() != null){
						MoneyIncomeCategory category = HyjModel.getModel(MoneyIncomeCategory.class, this.mSelectorFieldMoneyIncomeCategory.getModelId());
						if(category != null){
							category.newModelEditor().save();
						}
					}
					
				mMoneyIncomeContainerEditor.save();
				HyjUtil.displayToast(R.string.app_save_success);
				ActiveAndroid.setTransactionSuccessful();
				this.getActivity().setResult(Activity.RESULT_OK);
				getActivity().finish();
			} finally {
			    ActiveAndroid.endTransaction();
			}
		 }
	  }
	}	

	private void savePictures(){
		 HyjImageField.ImageGridAdapter adapter = mImageFieldPicture.getAdapter();
			int count = adapter.getCount();
			boolean mainPicSet = false;
			for(int i = 0; i < count; i++){
				PictureItem pi = adapter.getItem(i);
				if(pi.getState() == PictureItem.NEW){
					Picture newPic = pi.getPicture();
					newPic.setRecordId(mMoneyIncomeContainerEditor.getModel().getId());
					newPic.setRecordType("MoneyIncomeContainer");
					newPic.save();
				} else if(pi.getState() == PictureItem.DELETED){
					pi.getPicture().delete();
				} else if(pi.getState() == PictureItem.CHANGED){

				}
				if(!mainPicSet && pi.getPicture() != null){
					mainPicSet = true;
					mMoneyIncomeContainerEditor.getModelCopy().setPicture(pi.getPicture());
				}
			}
	 }
	
	 private void saveApportions() {
		 MoneyApportionField.ImageGridAdapter adapter = mApportionFieldApportions.getAdapter();
			int count = adapter.getCount();
			int savedCount = 0;
			for (int i = 0; i < count; i++) {
				ApportionItem<MoneyApportion> api = adapter.getItem(i);
				MoneyIncomeApportion apportion = (MoneyIncomeApportion) api.getApportion();
				HyjModelEditor<MoneyIncomeApportion> apportionEditor = apportion.newModelEditor();
	            
				if(apportion.getFriendUserId() == null) {
					// 该好友不是项目成员
					if(api.getState() == ApportionItem.DELETED ){
						MoneyBorrow moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyIncomeApportionId=?", apportion.getId()).executeSingle();
						if(moneyBorrow != null){
							moneyBorrow.delete();
						} 
						apportion.delete();
					} else {
						if(api.getState() != ApportionItem.UNCHANGED) {
							api.saveToCopy(apportionEditor.getModelCopy());
						}
						
						// 该好友是网络好友 或 该好友是本地好友
						Friend friend = HyjModel.getModel(Friend.class, apportion.getLocalFriendId());
						MoneyAccount debtAccount = MoneyAccount.getDebtAccount(mMoneyIncomeContainerEditor.getModelCopy().getMoneyAccount().getCurrencyId(), friend);
						if(api.getState() == ApportionItem.NEW){
			                if(debtAccount == null){
			                	MoneyAccount.createDebtAccount(friend, mMoneyIncomeContainerEditor.getModelCopy().getMoneyAccount().getCurrencyId(), -apportionEditor.getModelCopy().getAmount0());
			                }else{
			                	HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
			                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() - apportionEditor.getModelCopy().getAmount0());
			                	debtAccountEditor.save();
			                }
						} else{
							MoneyAccount oldDebtAccount = MoneyAccount.getDebtAccount(mMoneyIncomeContainerEditor.getModel().getMoneyAccount().getCurrencyId(), friend);
							HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
							if(debtAccount == null){
								oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() + apportionEditor.getModel().getAmount0());
			                	MoneyAccount.createDebtAccount(friend, mMoneyIncomeContainerEditor.getModelCopy().getMoneyAccount().getCurrencyId(), -apportionEditor.getModelCopy().getAmount0());
			                }else if(debtAccount.getId().equals(oldDebtAccount.getId())){
			                	oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() + apportionEditor.getModel().getAmount0() - apportionEditor.getModelCopy().getAmount0());
			                }else{
			                	HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
			                	oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() + apportionEditor.getModel().getAmount0());
			                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() - apportionEditor.getModelCopy().getAmount0());
			                	debtAccountEditor.save();
			                }
							oldDebtAccountEditor.save();
						}
						
						MoneyBorrow moneyBorrow = null;
						if(apportion.get_mId() == null){
							moneyBorrow = new MoneyBorrow();
						} else {
							moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyIncomeApportionId=?", apportion.getId()).executeSingle();
						}
						moneyBorrow.setMoneyIncomeApportionId(apportionEditor.getModelCopy().getId());
						moneyBorrow.setAmount(apportionEditor.getModelCopy().getAmount0());
						moneyBorrow.setDate(mMoneyIncomeContainerEditor.getModelCopy().getDate());
						moneyBorrow.setRemark(mMoneyIncomeContainerEditor.getModelCopy().getRemark());
						moneyBorrow.setFriendAccountId(mMoneyIncomeContainerEditor.getModelCopy().getFriendAccountId());
						moneyBorrow.setFriendUserId(apportionEditor.getModelCopy().getFriendUserId());
						moneyBorrow.setExchangeRate(mMoneyIncomeContainerEditor.getModelCopy().getExchangeRate());
						moneyBorrow.setGeoLat(mMoneyIncomeContainerEditor.getModelCopy().getGeoLat());
						moneyBorrow.setGeoLon(mMoneyIncomeContainerEditor.getModelCopy().getGeoLon());
						moneyBorrow.setLocalFriendId(apportionEditor.getModelCopy().getLocalFriendId());
						moneyBorrow.setMoneyAccountId(mMoneyIncomeContainerEditor.getModelCopy().getMoneyAccountId());
						moneyBorrow.setLocation(mMoneyIncomeContainerEditor.getModelCopy().getLocation());
						moneyBorrow.setAddress(mMoneyIncomeContainerEditor.getModelCopy().getAddress());
						moneyBorrow.setPictureId(mMoneyIncomeContainerEditor.getModelCopy().getPictureId());
						moneyBorrow.setProjectId(mMoneyIncomeContainerEditor.getModelCopy().getProjectId());
						moneyBorrow.save();
						
						if(api.getState() != ApportionItem.UNCHANGED) {
							apportionEditor.save();
						}
						savedCount++;
					}
				}else {
					if(api.getState() == ApportionItem.DELETED ){
						
						ProjectShareAuthorization oldProjectShareAuthorization;
						
						if(apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
							// 更新旧项目的分摊收入
							oldProjectShareAuthorization = mMoneyIncomeContainerEditor.getOldSelfProjectShareAuthorization();
						} else {
							// 更新旧项目分摊收入
							oldProjectShareAuthorization = apportion.getProjectShareAuthorization();
						}
						
						HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
						oldProjectShareAuthorizationEditor.getModelCopy().setApportionedTotalIncome(oldProjectShareAuthorizationEditor.getModelCopy().getApportionedTotalIncome() - (apportion.getAmount0() * apportion.getMoneyIncomeContainer().getExchangeRate()));
						oldProjectShareAuthorizationEditor.save();
						
						if(apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
							MoneyIncome moneyIncome = new Select().from(MoneyIncome.class).where("moneyIncomeApportionId", apportion.getId()).executeSingle();
							if(moneyIncome != null){
								moneyIncome.delete();
							}
						} else {
							MoneyBorrow moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyIncomeApportionId", apportion.getId()).executeSingle();
							if(moneyBorrow != null){
								moneyBorrow.delete();
							} 
						}
						
						apportion.delete();
						
					} else {
						 if(api.getState() != ApportionItem.UNCHANGED) {
								api.saveToCopy(apportionEditor.getModelCopy());
							 }
						Double oldRate = mMoneyIncomeContainerEditor.getModel().getExchangeRate(); 
						Double rate = mMoneyIncomeContainerEditor.getModelCopy().getExchangeRate();
						Double oldApportionAmount = apportionEditor.getModel().getAmount0();
						
						ProjectShareAuthorization projectShareAuthorization;
							//维护项目成员金额
						if(apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
							projectShareAuthorization = mMoneyIncomeContainerEditor.getNewSelfProjectShareAuthorization();
						} else {
							projectShareAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", 
									mMoneyIncomeContainerEditor.getModelCopy().getProjectId(), apportion.getFriendUserId()).executeSingle();
						}
							HyjModelEditor<ProjectShareAuthorization> projectShareAuthorizationEditor = projectShareAuthorization.newModelEditor();
							
							
							if(mMoneyIncomeContainerEditor.getModelCopy().get_mId() == null || 
									mMoneyIncomeContainerEditor.getModel().getProjectId().equals(mMoneyIncomeContainerEditor.getModelCopy().getProjectId())){
								 // 无旧项目可更新
								projectShareAuthorizationEditor.getModelCopy().setApportionedTotalIncome(projectShareAuthorization.getApportionedTotalIncome() - (oldApportionAmount * oldRate) + (apportionEditor.getModelCopy().getAmount0() * rate));
							}else{
								//更新新项目分摊支出
								projectShareAuthorizationEditor.getModelCopy().setApportionedTotalIncome(projectShareAuthorization.getApportionedTotalIncome() + (apportionEditor.getModelCopy().getAmount0() * rate));

								//更新老项目分摊支出
								ProjectShareAuthorization oldProjectAuthorization;

								if(apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
									oldProjectAuthorization = mMoneyIncomeContainerEditor.getOldSelfProjectShareAuthorization();
								} else {
									oldProjectAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", 
											mMoneyIncomeContainerEditor.getModel().getProjectId(), apportion.getFriendUserId()).executeSingle();
									
								}
								if(oldProjectAuthorization != null){
									HyjModelEditor<ProjectShareAuthorization> oldProjectAuthorizationEditor = oldProjectAuthorization.newModelEditor();
									oldProjectAuthorizationEditor.getModelCopy().setApportionedTotalIncome(oldProjectAuthorization.getApportionedTotalIncome() - (oldApportionAmount * oldRate));
									oldProjectAuthorizationEditor.save();
								}
							}
							projectShareAuthorizationEditor.save();
							
							//更新相关好友的借贷账户
							if(!apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
								MoneyAccount debtAccount = MoneyAccount.getDebtAccount(mMoneyIncomeContainerEditor.getModelCopy().getMoneyAccount().getCurrencyId(), apportionEditor.getModelCopy().getFriendUserId());
								if(api.getState() == ApportionItem.NEW){
					                if(debtAccount == null){
					                	MoneyAccount.createDebtAccount(apportionEditor.getModelCopy().getFriendUserId(), mMoneyIncomeContainerEditor.getModelCopy().getMoneyAccount().getCurrencyId(), -apportionEditor.getModelCopy().getAmount0());
					                }else{
					                	HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
					                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() - apportionEditor.getModelCopy().getAmount0());
					                	debtAccountEditor.save();
					                }
								} else{
									MoneyAccount oldDebtAccount = MoneyAccount.getDebtAccount(mMoneyIncomeContainerEditor.getModel().getMoneyAccount().getCurrencyId(), apportionEditor.getModelCopy().getFriendUserId());
									HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
									if(debtAccount == null){
										oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() + apportionEditor.getModel().getAmount0());
					                	MoneyAccount.createDebtAccount(apportionEditor.getModelCopy().getFriendUserId(), mMoneyIncomeContainerEditor.getModelCopy().getMoneyAccount().getCurrencyId(), -apportionEditor.getModelCopy().getAmount0());
					                }else if(debtAccount.getId().equals(oldDebtAccount.getId())){
					                	oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() + apportionEditor.getModel().getAmount0() - apportionEditor.getModelCopy().getAmount0());
					                }else{
					                	HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
					                	oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() + apportionEditor.getModel().getAmount0());
					                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() - apportionEditor.getModelCopy().getAmount0());
					                	debtAccountEditor.save();
					                }
									oldDebtAccountEditor.save();
							    }
						    }
							
							if(apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
								MoneyIncome moneyIncome = null;
								if(apportion.get_mId() == null){
									moneyIncome = new MoneyIncome();
								} else {
									moneyIncome = new Select().from(MoneyIncome.class).where("moneyIncomeApportionId=?", apportion.getId()).executeSingle();
								}
								moneyIncome.setMoneyIncomeApportionId(apportionEditor.getModelCopy().getId());
								moneyIncome.setAmount(apportionEditor.getModelCopy().getAmount0());
								moneyIncome.setDate(mMoneyIncomeContainerEditor.getModelCopy().getDate());
								moneyIncome.setRemark(mMoneyIncomeContainerEditor.getModelCopy().getRemark());
								moneyIncome.setFriendAccountId(mMoneyIncomeContainerEditor.getModelCopy().getFriendAccountId());
								moneyIncome.setFriendUserId(mMoneyIncomeContainerEditor.getModelCopy().getFriendUserId());
								moneyIncome.setExchangeRate(mMoneyIncomeContainerEditor.getModelCopy().getExchangeRate());
								moneyIncome.setGeoLat(mMoneyIncomeContainerEditor.getModelCopy().getGeoLat());
								moneyIncome.setGeoLon(mMoneyIncomeContainerEditor.getModelCopy().getGeoLon());
								moneyIncome.setLocalFriendId(mMoneyIncomeContainerEditor.getModelCopy().getLocalFriendId());
								moneyIncome.setMoneyAccountId(mMoneyIncomeContainerEditor.getModelCopy().getMoneyAccountId());
								moneyIncome.setLocation(mMoneyIncomeContainerEditor.getModelCopy().getLocation());
								moneyIncome.setAddress(mMoneyIncomeContainerEditor.getModelCopy().getAddress());
								moneyIncome.setMoneyIncomeCategory(mMoneyIncomeContainerEditor.getModelCopy().getMoneyIncomeCategory());
								moneyIncome.setMoneyIncomeCategoryMain(mMoneyIncomeContainerEditor.getModelCopy().getMoneyIncomeCategoryMain());
								moneyIncome.setPictureId(mMoneyIncomeContainerEditor.getModelCopy().getPictureId());
								moneyIncome.setProjectId(mMoneyIncomeContainerEditor.getModelCopy().getProjectId());
								moneyIncome.save();
							} else {
								MoneyBorrow moneyBorrow = null;
								if(apportion.get_mId() == null){
									moneyBorrow = new MoneyBorrow();
								} else {
									moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyBorrowApportionId", apportion.getId()).executeSingle();
								}
								moneyBorrow.setMoneyIncomeApportionId(apportionEditor.getModelCopy().getId());
								moneyBorrow.setAmount(apportionEditor.getModelCopy().getAmount0());
								moneyBorrow.setDate(mMoneyIncomeContainerEditor.getModelCopy().getDate());
								moneyBorrow.setRemark(mMoneyIncomeContainerEditor.getModelCopy().getRemark());
								moneyBorrow.setFriendAccountId(mMoneyIncomeContainerEditor.getModelCopy().getFriendAccountId());
								moneyBorrow.setFriendUserId(apportionEditor.getModelCopy().getFriendUserId());
								moneyBorrow.setExchangeRate(mMoneyIncomeContainerEditor.getModelCopy().getExchangeRate());
								moneyBorrow.setGeoLat(mMoneyIncomeContainerEditor.getModelCopy().getGeoLat());
								moneyBorrow.setGeoLon(mMoneyIncomeContainerEditor.getModelCopy().getGeoLon());
								moneyBorrow.setLocalFriendId(apportionEditor.getModelCopy().getLocalFriendId());
								moneyBorrow.setMoneyAccountId(mMoneyIncomeContainerEditor.getModelCopy().getMoneyAccountId());
								moneyBorrow.setLocation(mMoneyIncomeContainerEditor.getModelCopy().getLocation());
								moneyBorrow.setAddress(mMoneyIncomeContainerEditor.getModelCopy().getAddress());
								moneyBorrow.setPictureId(mMoneyIncomeContainerEditor.getModelCopy().getPictureId());
								moneyBorrow.setProjectId(mMoneyIncomeContainerEditor.getModelCopy().getProjectId());
								
								moneyBorrow.save();
							}
							
							
							 if(api.getState() != ApportionItem.UNCHANGED) {
									apportionEditor.save();
								 }
								savedCount++;
						
					}
				}
			}
			// 从隐藏掉的分摊里面删除原来的分摊
			Iterator<ApportionItem<MoneyApportion>> it = mApportionFieldApportions.getHiddenApportions().iterator();
			while (it.hasNext()) {
				// Get element
				ApportionItem<MoneyApportion> item = it.next();
				if (item.getState() != ApportionItem.NEW) {
					MoneyIncomeApportion apportion = ((MoneyIncomeApportion) item.getApportion());
					
					ProjectShareAuthorization oldProjectShareAuthorization;
					
					if(apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
						// 更新旧项目的分摊支出
						oldProjectShareAuthorization = mMoneyIncomeContainerEditor.getOldSelfProjectShareAuthorization();
					} else {
						// 更新旧项目分摊支出
						oldProjectShareAuthorization = apportion.getProjectShareAuthorization();
					}
					
					HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
					oldProjectShareAuthorizationEditor.getModelCopy().setApportionedTotalIncome(oldProjectShareAuthorizationEditor.getModelCopy().getApportionedTotalIncome() - (apportion.getAmount0() * apportion.getMoneyIncomeContainer().getExchangeRate()));
					oldProjectShareAuthorizationEditor.save();

					if(apportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
						MoneyIncome moneyIncome = new Select().from(MoneyIncome.class).where("moneyIncomeApportionId=?", apportion.getId()).executeSingle();
						if(moneyIncome != null){
							moneyIncome.delete();
						}
					} else {
						MoneyBorrow moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyIncomeApportionId=?", apportion.getId()).executeSingle();
						if(moneyBorrow != null){
							moneyBorrow.delete();
						} 
					}
					
					apportion.delete();
				}
			}
			
			// 如果列表里一个都没有被保存，我们生成一个默认的分摊
			if (savedCount == 0) {
				MoneyIncomeApportion apportion = new MoneyIncomeApportion();
				apportion.setAmount(mMoneyIncomeContainerEditor.getModelCopy().getAmount0());
				apportion.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
				apportion.setMoneyIncomeContainerId(mMoneyIncomeContainerEditor.getModelCopy().getId());
				apportion.setApportionType("Average");
				
				//更新项目成员的分摊金额
				ProjectShareAuthorization projectShareAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", 
						mMoneyIncomeContainerEditor.getModelCopy().getProjectId(), apportion.getFriendUserId()).executeSingle();
				HyjModelEditor<ProjectShareAuthorization> projectShareAuthorizationEditor = projectShareAuthorization.newModelEditor();
				projectShareAuthorizationEditor.getModelCopy().setApportionedTotalIncome(projectShareAuthorizationEditor.getModelCopy().getApportionedTotalIncome() + (apportion.getAmount0() * mMoneyIncomeContainerEditor.getModelCopy().getExchangeRate()));
				
				projectShareAuthorizationEditor.save();
				
				MoneyIncome moneyIncome = null;
				moneyIncome = new MoneyIncome();
				
				moneyIncome.setMoneyIncomeApportionId(apportion.getId());
				moneyIncome.setAmount(apportion.getAmount0());
				moneyIncome.setDate(mMoneyIncomeContainerEditor.getModelCopy().getDate());
				moneyIncome.setRemark(mMoneyIncomeContainerEditor.getModelCopy().getRemark());
				moneyIncome.setFriendAccountId(mMoneyIncomeContainerEditor.getModelCopy().getFriendAccountId());
				moneyIncome.setFriendUserId(mMoneyIncomeContainerEditor.getModelCopy().getFriendUserId());
				moneyIncome.setExchangeRate(mMoneyIncomeContainerEditor.getModelCopy().getExchangeRate());
				moneyIncome.setGeoLat(mMoneyIncomeContainerEditor.getModelCopy().getGeoLat());
				moneyIncome.setGeoLon(mMoneyIncomeContainerEditor.getModelCopy().getGeoLon());
				moneyIncome.setLocalFriendId(mMoneyIncomeContainerEditor.getModelCopy().getLocalFriendId());
				moneyIncome.setMoneyAccountId(mMoneyIncomeContainerEditor.getModelCopy().getMoneyAccountId());
				moneyIncome.setLocation(mMoneyIncomeContainerEditor.getModelCopy().getLocation());
				moneyIncome.setAddress(mMoneyIncomeContainerEditor.getModelCopy().getAddress());
				moneyIncome.setMoneyIncomeCategory(mMoneyIncomeContainerEditor.getModelCopy().getMoneyIncomeCategory());
				moneyIncome.setMoneyIncomeCategoryMain(mMoneyIncomeContainerEditor.getModelCopy().getMoneyIncomeCategoryMain());
				moneyIncome.setPictureId(mMoneyIncomeContainerEditor.getModelCopy().getPictureId());
				moneyIncome.setProjectId(mMoneyIncomeContainerEditor.getModelCopy().getProjectId());
				moneyIncome.save();
				apportion.save();
			}
	}
	 
	 @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode){
             case GET_MONEYACCOUNT_ID:
	        	 if(resultCode == Activity.RESULT_OK){
	         		long _id = data.getLongExtra("MODEL_ID", -1);
	         		MoneyAccount moneyAccount = MoneyAccount.load(MoneyAccount.class, _id);
	         		mSelectorFieldMoneyAccount.setText(moneyAccount.getName() + "(" + moneyAccount.getCurrencyId() + ")");
	         		mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
	         		setExchangeRate(false);
	         	 }
	        	 break;
             case GET_PROJECT_ID:
	        	 if(resultCode == Activity.RESULT_OK){
	         		long _id = data.getLongExtra("MODEL_ID", -1);
	         		Project project = Project.load(Project.class, _id);
	         		
	         		ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("projectId = ? AND friendUserId=?", project.getId(), HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
					
					if(mMoneyIncomeContainerEditor.getModelCopy().get_mId() == null && !psa.getProjectShareMoneyIncomeAddNew()){
						HyjUtil.displayToast(R.string.app_permission_no_addnew);
						return;
					}else if(mMoneyIncomeContainerEditor.getModelCopy().get_mId() != null && !psa.getProjectShareMoneyIncomeEdit()){
						HyjUtil.displayToast(R.string.app_permission_no_edit);
						return;
					}
	         		
	         		mSelectorFieldProject.setText(project.getDisplayName() + "(" + project.getCurrencyId() + ")");
	         		mSelectorFieldProject.setModelId(project.getId());
	         		setExchangeRate(false);
	         		mApportionFieldApportions.changeProject(project, MoneyIncomeApportion.class);
					mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
	         	 }
	        	 break;
	        	 
             case GET_FRIEND_ID:
            	 if(resultCode == Activity.RESULT_OK){
            		long _id = data.getLongExtra("MODEL_ID", -1);
            		Friend friend = Friend.load(Friend.class, _id);
            		
            		if(friend.getFriendUserId() != null && friend.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
    					HyjUtil.displayToast(R.string.moneyIncomeFormFragment_editText_error_friend);
    					return;
    				}
            		
            		mSelectorFieldFriend.setText(friend.getDisplayName());
            		mSelectorFieldFriend.setModelId(friend.getId());
            	 }
            	 break;
            	 
             case GET_CATEGORY_ID:
     			if (resultCode == Activity.RESULT_OK) {
     				long _id = data.getLongExtra("MODEL_ID", -1);
     				MoneyIncomeCategory category = MoneyIncomeCategory.load(MoneyIncomeCategory.class, _id);
     				mSelectorFieldMoneyIncomeCategory.setText(category.getName());
     				mSelectorFieldMoneyIncomeCategory.setModelId(category.getId());
     				if(category.getParentIncomeCategory() != null){
     					mSelectorFieldMoneyIncomeCategory.setLabel(category.getParentIncomeCategory().getName());
     				} else {
     					mSelectorFieldMoneyIncomeCategory.setLabel(null);
     				}
     			}
     			break;
            	 
             case GET_APPORTION_MEMBER_ID:
     			if (resultCode == Activity.RESULT_OK) {
     				long _id = data.getLongExtra("MODEL_ID", -1);
    				String type = data.getStringExtra("MODEL_TYPE");
    				MoneyIncomeApportion apportion = new MoneyIncomeApportion();
    				if("ProjectShareAuthorization".equalsIgnoreCase(type)){
    					ProjectShareAuthorization psa = ProjectShareAuthorization.load(ProjectShareAuthorization.class, _id);
    					if(!psa.getState().equalsIgnoreCase("Accept")){
    						HyjUtil.displayToast(R.string.moneyApportionField_select_toast_apportion_user_not_accepted);
    						break;
    					}
    					apportion.setFriendUserId(psa.getFriendUserId());
    				} else {
    					Friend friend = Friend.load(Friend.class, _id);
    					if(friend.getFriendUserId() != null){
    						//看一下该好友是不是项目成员, 如果是，作为项目成员添加
    						ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("friendUserId=? AND projectId=?", friend.getFriendUserId(), mSelectorFieldProject.getModelId()).executeSingle();
    						if(psa != null){
    							if(!psa.getState().equalsIgnoreCase("Accept")){
    								HyjUtil.displayToast(R.string.moneyApportionField_select_toast_apportion_user_not_accepted);
    								break;
    							} else {
    								apportion.setFriendUserId(psa.getFriendUserId());
    							}
    						} else {
    							apportion.setLocalFriendId(friend.getId());
    							apportion.setApportionType("Average");
    						}
    					} else {
    						apportion.setLocalFriendId(friend.getId());
    						apportion.setApportionType("Average");
    					}
    				}
    				apportion.setAmount(0.0);
    				apportion.setMoneyIncomeContainerId(mMoneyIncomeContainerEditor.getModel().getId());
    				if (mApportionFieldApportions.addApportion(apportion,mSelectorFieldProject.getModelId(), ApportionItem.NEW)) {
    					mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
    				} else {
    					HyjUtil.displayToast(R.string.moneyApportionField_select_toast_apportion_user_already_exists);
    				}
    			
    			}
     			break;
          }
    }
		// inner class
		private static class MoneyIncomeContainerEditor extends HyjModelEditor<MoneyIncomeContainer> {
			private  ProjectShareAuthorization mOldProjectShareAuthorization;
			private  ProjectShareAuthorization mNewProjectShareAuthorization;
			
			public MoneyIncomeContainerEditor(MoneyIncomeContainer model) {
				super(model);
			}
			
			public ProjectShareAuthorization getOldSelfProjectShareAuthorization(){
				if(mOldProjectShareAuthorization == null){
					return new Select().from(ProjectShareAuthorization.class).where("friendUserId=? AND projectId=?", this.getModel().getOwnerUserId(), this.getModel().getProjectId()).executeSingle();
				}
				return mOldProjectShareAuthorization;
			}
			
			public ProjectShareAuthorization getNewSelfProjectShareAuthorization(){
				if(mNewProjectShareAuthorization == null){
					return new Select().from(ProjectShareAuthorization.class).where("friendUserId=? AND projectId=?", HyjApplication.getInstance().getCurrentUser().getId(), this.getModelCopy().getProjectId()).executeSingle();
				}
				return mNewProjectShareAuthorization;
			}
			
			
			
		}
	}
