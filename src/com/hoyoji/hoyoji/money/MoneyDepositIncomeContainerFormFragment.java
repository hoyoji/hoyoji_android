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
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.activity.HyjActivity.DialogCallbackListener;
import com.hoyoji.android.hyjframework.fragment.HyjTextInputFormFragment;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeField;
import com.hoyoji.android.hyjframework.view.HyjImageField;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjRemarkField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.android.hyjframework.view.HyjImageField.PictureItem;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyApportion;
import com.hoyoji.hoyoji.models.MoneyBorrow;
import com.hoyoji.hoyoji.models.MoneyDepositIncomeApportion;
import com.hoyoji.hoyoji.models.MoneyDepositIncomeContainer;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.MoneyApportionField.ApportionItem;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;


public class MoneyDepositIncomeContainerFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_APPORTION_MEMBER_ID = 4;
	private static final int GET_REMARK = 3;
	
	private int CREATE_EXCHANGE = 0;
	private int SET_EXCHANGE_RATE_FLAG = 1;
	
	private MoneyDepositIncomeContainerEditor mMoneyDepositIncomeContainerEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private MoneyApportionField mApportionFieldApportions = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericAmount = null;
	private HyjSelectorField mSelectorFieldMoneyAccount = null;
	private HyjSelectorField mSelectorFieldProject = null;
	private HyjNumericField mNumericExchangeRate = null;
	private HyjRemarkField mRemarkFieldRemark = null;
	private ImageView mImageViewRefreshRate = null;
	private View mViewSeparatorExchange = null;
	private LinearLayout mLinearLayoutExchangeRate = null;
	
	private boolean hasEditPermission = true;
	
	@Override
	public Integer useContentView() {
		return R.layout.money_formfragment_moneydepositincomecontainer;
	}
	 
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		final MoneyDepositIncomeContainer moneyDepositIncomeContainer;
		
		Intent intent = getActivity().getIntent();
		long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			moneyDepositIncomeContainer =  new Select().from(MoneyDepositIncomeContainer.class).where("_id=?", modelId).executeSingle();
			hasEditPermission = moneyDepositIncomeContainer.hasEditPermission();
		} else {
			moneyDepositIncomeContainer = new MoneyDepositIncomeContainer();
			final String moneyAccountId = intent.getStringExtra("moneyAccountId");
			if(moneyDepositIncomeContainer.get_mId() == null && moneyAccountId != null){
				moneyDepositIncomeContainer.setMoneyAccountId(moneyAccountId);
			}
		}
		
		mMoneyDepositIncomeContainerEditor = new MoneyDepositIncomeContainerEditor(moneyDepositIncomeContainer);
		
		setupDeleteButton(mMoneyDepositIncomeContainerEditor);
		
		mImageFieldPicture = (HyjImageField) getView().findViewById(R.id.moneyDepositIncomeContainerFormFragment_imageField_picture);
		mImageFieldPicture.setImages(moneyDepositIncomeContainer.getPictures());
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyDepositIncomeContainerFormFragment_textField_date);	
		if(modelId != -1){
			mDateTimeFieldDate.setText(moneyDepositIncomeContainer.getDate());
		}
		
		Project project;
		String projectId = intent.getStringExtra("projectId");//从消息导入
		if(projectId != null){
			moneyDepositIncomeContainer.setProjectId(projectId);
			project = HyjModel.getModel(Project.class, projectId);
		}else{
			project = moneyDepositIncomeContainer.getProject();
		}
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(R.id.moneyDepositIncomeContainerFormFragment_selectorField_project);
		
		if(project != null){
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getDisplayName() + "(" + project.getCurrencyId() + ")");
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyDepositIncomeContainerFormFragment.this.openActivityWithFragmentForResult(ProjectListFragment.class, R.string.projectListFragment_title_select_project, null, GET_PROJECT_ID);
			}
		});	
		
		setupApportionField(moneyDepositIncomeContainer);
		
		mNumericAmount = (HyjNumericField) getView().findViewById(R.id.moneyDepositIncomeContainerFormFragment_textField_amount);		
		double amount = intent.getDoubleExtra("amount", -1.0);//从分享消息导入的金额
		if(amount >= 0.0){
			double exchangeRate = intent.getDoubleExtra("exchangeRate", 1.0);
			mNumericAmount.setNumber(amount*exchangeRate);
			mApportionFieldApportions.setTotalAmount(amount*exchangeRate);
		}else{
			mNumericAmount.setNumber(moneyDepositIncomeContainer.getAmount());
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
		
		MoneyAccount moneyAccount = moneyDepositIncomeContainer.getMoneyAccount();
		mSelectorFieldMoneyAccount = (HyjSelectorField) getView().findViewById(R.id.moneyDepositIncomeContainerFormFragment_selectorField_moneyAccount);

		if(moneyAccount != null){
			mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
			mSelectorFieldMoneyAccount.setText(moneyAccount.getName() + "(" + moneyAccount.getCurrencyId() + ")");
		}
		mSelectorFieldMoneyAccount.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("excludeType", "Debt");
				
				MoneyDepositIncomeContainerFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, bundle, GET_MONEYACCOUNT_ID);
			}
		});	
		
		
		mNumericExchangeRate = (HyjNumericField) getView().findViewById(R.id.moneyDepositIncomeContainerFormFragment_textField_exchangeRate);		
		mNumericExchangeRate.setNumber(moneyDepositIncomeContainer.getExchangeRate());
		
		mViewSeparatorExchange = (View) getView().findViewById(R.id.moneyDepositIncomeContainerFormFragment_separatorField_exchange);
		mLinearLayoutExchangeRate = (LinearLayout) getView().findViewById(R.id.moneyDepositIncomeContainerFormFragment_linearLayout_exchangeRate);
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyDepositIncomeContainerFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyDepositIncomeContainer.getRemark());
		mRemarkFieldRemark.setEditable(false);
		mRemarkFieldRemark.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("TEXT", mRemarkFieldRemark.getText());
				bundle.putString("HINT", "请输入" + mRemarkFieldRemark.getLabelText());
				MoneyDepositIncomeContainerFormFragment.this
						.openActivityWithFragmentForResult(
								HyjTextInputFormFragment.class,
								R.string.moneyExpenseFormFragment_textView_remark,
								bundle, GET_REMARK);
			}
		});
		
		ImageView takePictureButton = (ImageView) getView().findViewById(R.id.moneyDepositIncomeContainerFormFragment_imageView_camera);	
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
		
		mImageViewRefreshRate = (ImageView) getView().findViewById(R.id.moneyDepositIncomeContainerFormFragment_imageButton_refresh_exchangeRate);	
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
						HyjUtil.displayToast(R.string.moneyDepositIncomeContainerFormFragment_toast_select_currency);
					}
				}else{
					HyjUtil.displayToast(R.string.moneyDepositIncomeContainerFormFragment_toast_select_currency);
				}
			}
		});
		
			
			getView().findViewById(R.id.moneyDepositIncomeContainerFormFragment_imageButton_apportion_add).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Bundle bundle = new Bundle();
					Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
					bundle.putLong("MODEL_ID", project.get_mId());
					openActivityWithFragmentForResult(SelectApportionMemberListFragment.class, R.string.moneyDepositIncomeContainerFormFragment_moneyApportionField_select_apportion_member, bundle, GET_APPORTION_MEMBER_ID);
				}
			});
			
			getView().findViewById(R.id.moneyDepositIncomeContainerFormFragment_imageButton_apportion_add_all).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					addAllProjectMemberIntoApportionsField(moneyDepositIncomeContainer);
				}
			});
			
			getView().findViewById(R.id.moneyDepositIncomeContainerFormFragment_imageButton_apportion_more_actions).setOnClickListener(new OnClickListener() {
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
//									MoneyDepositIncomeApportion apportion = new MoneyDepositIncomeApportion();
//									apportion.setAmount(0.0);
//									apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
//									apportion.setMoneyDepositIncomeId(moneyBorrow.getId());
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
	    if(mMoneyDepositIncomeContainerEditor!= null && mMoneyDepositIncomeContainerEditor.getModelCopy().get_mId() != null && !hasEditPermission){
	    	hideSaveAction();
	    }
	}
	
	private void addAllProjectMemberIntoApportionsField(MoneyDepositIncomeContainer moneyIncomeContainer) {
		Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
		List<ProjectShareAuthorization> projectShareAuthorizations = project.getShareAuthorizations();
		for (int i = 0; i < projectShareAuthorizations.size(); i++) {
			if(!projectShareAuthorizations.get(i).getState().equalsIgnoreCase("Accept")){
				continue;
			}
			MoneyDepositIncomeApportion apportion = new MoneyDepositIncomeApportion();
			apportion.setAmount(0.0);
			apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
			apportion.setMoneyDepositIncomeContainerId(moneyIncomeContainer.getId());

			mApportionFieldApportions.addApportion(apportion, project.getId(), ApportionItem.NEW);
		}
		mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
		
	}
	
	private void setupApportionField(MoneyDepositIncomeContainer moneyDepositIncomeContainer) {

		mApportionFieldApportions = (MoneyApportionField) getView().findViewById(R.id.moneyDepositIncomeContainerFormFragment_apportionField);
		
		List<MoneyDepositIncomeApportion> moneyApportions = null;
		
		if(mMoneyDepositIncomeContainerEditor.getModelCopy().get_mId() == null) {
			
			moneyApportions = new ArrayList<MoneyDepositIncomeApportion>();
//			if(moneyDepositIncomeContainer.getProject() != null && moneyDepositIncomeContainer.getProject().getAutoApportion()){
//				List<ProjectShareAuthorization> projectShareAuthorizations = moneyDepositIncomeContainer.getProject().getShareAuthorizations();
//				for(int i=0; i < projectShareAuthorizations.size(); i++){
//					MoneyDepositIncomeApportion apportion = new MoneyDepositIncomeApportion();
//					apportion.setAmount(0.0);
//					apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
//					apportion.setMoneyDepositIncomeContainerId(moneyDepositIncomeContainer.getId());
//					apportion.setApportionType("Share");
//					
//					moneyApportions.add(apportion);
//				}
//			} else 
//			if(moneyDepositIncomeContainer.getProject() != null) {
//				MoneyDepositIncomeApportion apportion = new MoneyDepositIncomeApportion();
//				apportion.setAmount(moneyDepositIncomeContainer.getAmount0());
//				apportion.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
//				apportion.setMoneyDepositIncomeContainerId(moneyDepositIncomeContainer.getId());
//				apportion.setApportionType("Average");
//				moneyApportions.add(apportion);
//			}

			Intent intent = getActivity().getIntent();
			String friendUserId = intent.getStringExtra("friendUserId");
			if(friendUserId != null){
				MoneyDepositIncomeApportion apportion = new MoneyDepositIncomeApportion();
				apportion.setAmount(moneyDepositIncomeContainer.getAmount0());
				apportion.setFriendUserId(friendUserId);
				apportion.setMoneyDepositIncomeContainerId(moneyDepositIncomeContainer.getId());
				apportion.setApportionType("Average");
				moneyApportions.add(apportion);
			}
			
		} else {
			moneyApportions = moneyDepositIncomeContainer.getApportions();
		}
		
		mApportionFieldApportions.init(moneyDepositIncomeContainer.getAmount0(), moneyApportions, moneyDepositIncomeContainer.getProjectId(), moneyDepositIncomeContainer.getId());
	}
	
	private void setupDeleteButton(HyjModelEditor<MoneyDepositIncomeContainer> moneyDepositIncomeContainerEditor) {

		Button buttonDelete = (Button) getView().findViewById(R.id.button_delete);
		
		final MoneyDepositIncomeContainer moneyDepositIncomeContainer = moneyDepositIncomeContainerEditor.getModelCopy();
		
		if (moneyDepositIncomeContainer.get_mId() == null) {
			buttonDelete.setVisibility(View.GONE);
		} else {
			buttonDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(moneyDepositIncomeContainer.hasDeletePermission()){
					((HyjActivity)getActivity()).displayDialog(R.string.app_action_delete_list_item, R.string.app_confirm_delete, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
							new DialogCallbackListener() {
								@Override
								public void doPositiveClick(Object object) {
									try {
										ActiveAndroid.beginTransaction();

										MoneyAccount moneyAccount = moneyDepositIncomeContainer.getMoneyAccount();
										HyjModelEditor<MoneyAccount> moneyAccountEditor = moneyAccount.newModelEditor();
										moneyAccountEditor.getModelCopy().setCurrentBalance(moneyAccount.getCurrentBalance() - moneyDepositIncomeContainer.getAmount0());

										//更新项目余额
										Project oldProject = moneyDepositIncomeContainer.getProject();
										HyjModelEditor<Project> oldProjectEditor = oldProject.newModelEditor();
										oldProjectEditor.getModelCopy().setDepositTotal(oldProject.getDepositTotal() - moneyDepositIncomeContainer.getAmount0()*moneyDepositIncomeContainer.getExchangeRate());
										oldProjectEditor.save();
										
										// 更新旧项目收入所有者的实际借入
										ProjectShareAuthorization oldProjectShareAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(oldProject.getId());
										HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
										oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalBorrow(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalBorrow() - (moneyDepositIncomeContainer.getAmount0() * moneyDepositIncomeContainer.getExchangeRate()));
										oldProjectShareAuthorizationEditor.save();
										
										//删除收入的同时删除分摊
										Iterator<MoneyDepositIncomeApportion> moneyDepositIncomeApportions = moneyDepositIncomeContainer.getApportions().iterator();
										while (moneyDepositIncomeApportions.hasNext()) {
											MoneyDepositIncomeApportion moneyDepositIncomeAportion = moneyDepositIncomeApportions.next();
										
											MoneyBorrow moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyDepositIncomeApportionId=?", moneyDepositIncomeAportion.getId()).executeSingle();
											if(moneyBorrow != null){
												if(!oldProject.isProjectMember(moneyBorrow.getLocalFriendId(), moneyBorrow.getFriendUserId())){
													MoneyAccount debtAccount = MoneyAccount.getDebtAccount(moneyBorrow.getProject().getCurrencyId(), moneyBorrow.getLocalFriendId(), moneyBorrow.getFriendUserId());
													HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
													debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + moneyBorrow.getProjectAmount());
													debtAccountEditor.save();
												}
												moneyBorrow.delete();
											} 
												
											moneyDepositIncomeAportion.delete();
										}

										moneyDepositIncomeContainer.delete();
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

		if(mMoneyDepositIncomeContainerEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			mDateTimeFieldDate.setEnabled(false);
			
			mNumericAmount.setNumber(mMoneyDepositIncomeContainerEditor.getModel().getProjectAmount());
			mNumericAmount.setEnabled(false);
			
			mSelectorFieldMoneyAccount.setEnabled(false);
			mSelectorFieldMoneyAccount.setVisibility(View.GONE);

			mSelectorFieldProject.setEnabled(false);
			
			mNumericExchangeRate.setEnabled(false);
			
			mApportionFieldApportions.setEnabled(false);

			mRemarkFieldRemark.setEnabled(false);
			
			if(this.mOptionsMenu != null){
		    	hideSaveAction();
			}
			
			getView().findViewById(R.id.moneyDepositIncomeContainerFormFragment_imageButton_apportion_add).setEnabled(false);
			getView().findViewById(R.id.moneyDepositIncomeContainerFormFragment_imageButton_apportion_add_all).setEnabled(false);
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
		MoneyDepositIncomeContainer modelCopy =  mMoneyDepositIncomeContainerEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setAmount(mNumericAmount.getNumber());
		modelCopy.setMoneyAccountId(mSelectorFieldMoneyAccount.getModelId());
		modelCopy.setProject(HyjModel.getModel(Project.class, mSelectorFieldProject.getModelId()));
		modelCopy.setExchangeRate(mNumericExchangeRate.getNumber());
		
		modelCopy.setRemark(mRemarkFieldRemark.getText().toString().trim());
		
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		mDateTimeFieldDate.setError(mMoneyDepositIncomeContainerEditor.getValidationError("datetime"));
		mNumericAmount.setError(mMoneyDepositIncomeContainerEditor.getValidationError("amount"));
		if(mMoneyDepositIncomeContainerEditor.getValidationError("amount") != null){
					mNumericAmount.showSoftKeyboard();
				}
		mSelectorFieldMoneyAccount.setError(mMoneyDepositIncomeContainerEditor.getValidationError("moneyAccount"));
		mSelectorFieldProject.setError(mMoneyDepositIncomeContainerEditor.getValidationError("project"));
		mNumericExchangeRate.setError(mMoneyDepositIncomeContainerEditor.getValidationError("exchangeRate"));
		mRemarkFieldRemark.setError(mMoneyDepositIncomeContainerEditor.getValidationError("remark"));
		mApportionFieldApportions.setError(mMoneyDepositIncomeContainerEditor.getValidationError("apportionTotalAmount"));
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		if(mMoneyDepositIncomeContainerEditor.getModelCopy().get_mId() == null && !mMoneyDepositIncomeContainerEditor.getModelCopy().hasAddNewPermission(mMoneyDepositIncomeContainerEditor.getModelCopy().getProjectId())){
			HyjUtil.displayToast(R.string.app_permission_no_addnew);
		}else if(mMoneyDepositIncomeContainerEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			HyjUtil.displayToast(R.string.app_permission_no_edit);
		}else if(mApportionFieldApportions.getCount() <= 0){
			HyjUtil.displayToast("请选择至少一个缴费成员");
		}else{
		
		mMoneyDepositIncomeContainerEditor.validate();
		
		if (mApportionFieldApportions.getCount() > 0) {
			if (mNumericAmount.getNumber() != null && !mNumericAmount.getNumber().equals(mApportionFieldApportions.getTotalAmount())) {
				mMoneyDepositIncomeContainerEditor.setValidationError("apportionTotalAmount",R.string.moneyApportionField_select_toast_apportion_totalAmount_not_equal);
			} else {
				mMoneyDepositIncomeContainerEditor.removeValidationError("apportionTotalAmount");
			}
		} else {
			mMoneyDepositIncomeContainerEditor.removeValidationError("apportionTotalAmount");
		}
		
		if(mMoneyDepositIncomeContainerEditor.hasValidationErrors()){
			showValidatioErrors();
		} else {
			try {
				ActiveAndroid.beginTransaction();
				
				savePictures();

				saveApportions();
				
				MoneyDepositIncomeContainer oldMoneyDepositIncomeContainerModel = mMoneyDepositIncomeContainerEditor.getModel();
				MoneyDepositIncomeContainer moneyDepositIncomeContainerModel = mMoneyDepositIncomeContainerEditor.getModelCopy();
				
				//设置默认项目和账户
				UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
				if(moneyDepositIncomeContainerModel.get_mId() == null && !userData.getActiveMoneyAccountId().equals(moneyDepositIncomeContainerModel.getMoneyAccountId()) || !userData.getActiveProjectId().equals(moneyDepositIncomeContainerModel.getProjectId())){
					HyjModelEditor<UserData> userDataEditor = userData.newModelEditor();
					userDataEditor.getModelCopy().setActiveMoneyAccountId(moneyDepositIncomeContainerModel.getMoneyAccountId());
					userDataEditor.getModelCopy().setActiveProjectId(moneyDepositIncomeContainerModel.getProjectId());
					userDataEditor.save();
				}
				
				//当前汇率不存在时，创建汇率
				String localCurrencyId = moneyDepositIncomeContainerModel.getMoneyAccount().getCurrencyId();
				String foreignCurrencyId = moneyDepositIncomeContainerModel.getProject().getCurrencyId();
				if(CREATE_EXCHANGE == 1){
					MoneyAccount moneyAccount = moneyDepositIncomeContainerModel.getMoneyAccount();
					Project project = moneyDepositIncomeContainerModel.getProject();
					
					Exchange newExchange = new Exchange();
					newExchange.setLocalCurrencyId(moneyAccount.getCurrencyId());
					newExchange.setForeignCurrencyId(project.getCurrencyId());
					newExchange.setRate(moneyDepositIncomeContainerModel.getExchangeRate());
					newExchange.save();
				}else {
					if(!localCurrencyId.equalsIgnoreCase(foreignCurrencyId)){
						Exchange exchange = null;
						Double exRate = null;
						Double rate = HyjUtil.toFixed2(moneyDepositIncomeContainerModel.getExchangeRate());
						exchange = Exchange.getExchange(localCurrencyId, foreignCurrencyId);
						if(exchange != null){
							exRate = exchange.getRate();
							if(!rate.equals(exRate)){
								HyjModelEditor<Exchange> exchangModelEditor = exchange.newModelEditor();
								exchangModelEditor.getModelCopy().setRate(rate);
								exchangModelEditor.save();
							}
						} else {
							exchange = Exchange.getExchange(foreignCurrencyId, localCurrencyId);
							if(exchange != null){
								exRate = HyjUtil.toFixed2(1 / exchange.getRate());
								if(!rate.equals(exRate)){
									HyjModelEditor<Exchange> exchangModelEditor = exchange.newModelEditor();
									exchangModelEditor.getModelCopy().setRate(1/rate);
									exchangModelEditor.save();
								}
							}
						}
					}
			}
				
				    MoneyAccount oldMoneyAccount = oldMoneyDepositIncomeContainerModel.getMoneyAccount();
					MoneyAccount newMoneyAccount = moneyDepositIncomeContainerModel.getMoneyAccount();
					HyjModelEditor<MoneyAccount> newMoneyAccountEditor = newMoneyAccount.newModelEditor();
					
					//更新账户余额
					if(moneyDepositIncomeContainerModel.get_mId() == null || oldMoneyAccount.getId().equals(newMoneyAccount.getId())){
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() - oldMoneyDepositIncomeContainerModel.getAmount0() + moneyDepositIncomeContainerModel.getAmount0());
					}else{
						HyjModelEditor<MoneyAccount> oldMoneyAccountEditor = oldMoneyAccount.newModelEditor();
						oldMoneyAccountEditor.getModelCopy().setCurrentBalance(oldMoneyAccount.getCurrentBalance() - oldMoneyDepositIncomeContainerModel.getAmount0());
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() + moneyDepositIncomeContainerModel.getAmount0());
						oldMoneyAccountEditor.save();
					}
					newMoneyAccountEditor.save();

					Project oldProject = oldMoneyDepositIncomeContainerModel.getProject();
					Project newProject = moneyDepositIncomeContainerModel.getProject();
					HyjModelEditor<Project> newProjectEditor = newProject.newModelEditor();
					
					//更新项目余额
					if(moneyDepositIncomeContainerModel.get_mId() == null || oldProject.getId().equals(newProject.getId())){
						newProjectEditor.getModelCopy().setDepositTotal(newProject.getDepositTotal() - oldMoneyDepositIncomeContainerModel.getAmount0()*oldMoneyDepositIncomeContainerModel.getExchangeRate() + moneyDepositIncomeContainerModel.getAmount0()*moneyDepositIncomeContainerModel.getExchangeRate());
					} else {
						HyjModelEditor<Project> oldProjectEditor = oldProject.newModelEditor();
						oldProjectEditor.getModelCopy().setDepositTotal(oldProject.getDepositTotal() - oldMoneyDepositIncomeContainerModel.getAmount0()*oldMoneyDepositIncomeContainerModel.getExchangeRate());
						newProjectEditor.getModelCopy().setDepositTotal(newProject.getDepositTotal() + moneyDepositIncomeContainerModel.getAmount0()*moneyDepositIncomeContainerModel.getExchangeRate());
						oldProjectEditor.save();
					}
					newProjectEditor.save();
					
					
					//更新支出所有者的实际借入
					ProjectShareAuthorization selfProjectAuthorization = mMoneyDepositIncomeContainerEditor.getNewSelfProjectShareAuthorization();
					HyjModelEditor<ProjectShareAuthorization> selfProjectAuthorizationEditor = selfProjectAuthorization.newModelEditor();
				    
					if(moneyDepositIncomeContainerModel.get_mId() == null || oldMoneyDepositIncomeContainerModel.getProjectId().equals(moneyDepositIncomeContainerModel.getProjectId())){
				    	selfProjectAuthorizationEditor.getModelCopy().setActualTotalBorrow(selfProjectAuthorization.getActualTotalBorrow() - oldMoneyDepositIncomeContainerModel.getAmount0()*oldMoneyDepositIncomeContainerModel.getExchangeRate() + moneyDepositIncomeContainerModel.getAmount0()*moneyDepositIncomeContainerModel.getExchangeRate());
					}else{
						selfProjectAuthorizationEditor.getModelCopy().setActualTotalBorrow(selfProjectAuthorization.getActualTotalBorrow() + moneyDepositIncomeContainerModel.getAmount0()*moneyDepositIncomeContainerModel.getExchangeRate());
						
						ProjectShareAuthorization oldSelfProjectAuthorization = mMoneyDepositIncomeContainerEditor.getOldSelfProjectShareAuthorization();
						HyjModelEditor<ProjectShareAuthorization> oldSelfProjectAuthorizationEditor = oldSelfProjectAuthorization.newModelEditor();
						oldSelfProjectAuthorizationEditor.getModelCopy().setActualTotalBorrow(oldSelfProjectAuthorization.getActualTotalBorrow() - oldMoneyDepositIncomeContainerModel.getAmount0()*oldMoneyDepositIncomeContainerModel.getExchangeRate());
						oldSelfProjectAuthorizationEditor.save();
					}
					selfProjectAuthorizationEditor.save();

					
				mMoneyDepositIncomeContainerEditor.save();
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
					newPic.setRecordId(mMoneyDepositIncomeContainerEditor.getModel().getId());
					newPic.setRecordType("MoneyDepositIncomeContainer");
					newPic.setDisplayOrder(i);
					newPic.save();
				} else if(pi.getState() == PictureItem.DELETED){
					pi.getPicture().delete();
				} else if(pi.getState() == PictureItem.CHANGED){

				}
				if (!mainPicSet && pi.getPicture() != null && pi.getState() != PictureItem.DELETED) {
					mainPicSet = true;
					mMoneyDepositIncomeContainerEditor.getModelCopy().setPicture(pi.getPicture());
				}
			}
	 }
	
	 private void saveApportions() {
		 MoneyApportionField.ImageGridAdapter adapter = mApportionFieldApportions.getAdapter();
			int count = adapter.getCount();
			int savedCount = 0;
			for (int i = 0; i < count; i++) {
				ApportionItem<MoneyApportion> api = adapter.getItem(i);
				MoneyDepositIncomeApportion apportion = (MoneyDepositIncomeApportion) api.getApportion();
	            
					if(api.getState() == ApportionItem.DELETED ){
						MoneyBorrow moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyDepositIncomeApportionId=?", apportion.getId()).executeSingle();
						if(moneyBorrow != null){
							if(!moneyBorrow.getProject().isProjectMember(moneyBorrow.getLocalFriendId(), moneyBorrow.getFriendUserId())){
								MoneyAccount debtAccount = null;
								debtAccount = MoneyAccount.getDebtAccount(moneyBorrow.getProject().getCurrencyId(), moneyBorrow.getLocalFriendId(), moneyBorrow.getFriendUserId());
								HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
								debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + moneyBorrow.getProjectAmount());
								debtAccountEditor.save(); 
							}
							moneyBorrow.delete();
						} 
						apportion.delete();
					} else {
						HyjModelEditor<MoneyDepositIncomeApportion> apportionEditor = apportion.newModelEditor();
						if(api.getState() != ApportionItem.UNCHANGED		
								|| !mMoneyDepositIncomeContainerEditor.getModelCopy().getProjectId().equals(mMoneyDepositIncomeContainerEditor.getModel().getProjectId())
								|| !mMoneyDepositIncomeContainerEditor.getModelCopy().getMoneyAccountId().equals(mMoneyDepositIncomeContainerEditor.getModel().getMoneyAccountId())) {
							api.saveToCopy(apportionEditor.getModelCopy());
						}
						
						MoneyAccount debtAccount = null;
						// 该好友是网络好友 或 该好友是本地好友
						boolean isNewProjectMember = mMoneyDepositIncomeContainerEditor.getModelCopy().getProject().isProjectMember(apportion.getLocalFriendId(), apportion.getFriendUserId());
						if(!isNewProjectMember){
							debtAccount = MoneyAccount.getDebtAccount(mMoneyDepositIncomeContainerEditor.getModelCopy().getProject().getCurrencyId(), apportion.getLocalFriendId(), apportion.getFriendUserId());
						}
						if(api.getState() == ApportionItem.NEW){
			                if(debtAccount != null){
			                	HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
			                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() - apportionEditor.getModelCopy().getAmount0()*mMoneyDepositIncomeContainerEditor.getModelCopy().getExchangeRate());
			                	debtAccountEditor.save();
			                }else if(!isNewProjectMember){
			                	MoneyAccount.createDebtAccount(apportion.getLocalFriendId(), apportion.getFriendUserId(), mMoneyDepositIncomeContainerEditor.getModelCopy().getProject().getCurrencyId(), -apportionEditor.getModelCopy().getAmount()*mMoneyDepositIncomeContainerEditor.getModelCopy().getExchangeRate());
				            }
						} else {
							MoneyAccount oldDebtAccount = null;
							if(!mMoneyDepositIncomeContainerEditor.getModel().getProject().isProjectMember(apportionEditor.getModel().getLocalFriendId(), apportionEditor.getModel().getFriendUserId())){
								oldDebtAccount = MoneyAccount.getDebtAccount(mMoneyDepositIncomeContainerEditor.getModelCopy().getProject().getCurrencyId(), apportionEditor.getModel().getLocalFriendId(), apportionEditor.getModel().getFriendUserId());
							}
							if(debtAccount == null){
								if(oldDebtAccount != null){
									HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
									oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() + apportionEditor.getModel().getAmount0()*apportionEditor.getModel().getExchangeRate());
									oldDebtAccountEditor.save();
								}
								if(!isNewProjectMember){
									MoneyAccount.createDebtAccount(apportion.getLocalFriendId(), apportion.getFriendUserId(), mMoneyDepositIncomeContainerEditor.getModelCopy().getProject().getCurrencyId(), -apportionEditor.getModelCopy().getAmount0()*mMoneyDepositIncomeContainerEditor.getModelCopy().getExchangeRate());
								}
							}else if(oldDebtAccount != null && debtAccount.getId().equals(oldDebtAccount.getId())){
									HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
									oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() + apportionEditor.getModel().getAmount0()*apportionEditor.getModel().getExchangeRate() - apportionEditor.getModelCopy().getAmount0()*mMoneyDepositIncomeContainerEditor.getModelCopy().getExchangeRate());
									oldDebtAccountEditor.save();
							}else {
								if(oldDebtAccount != null){
									HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
									oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() + apportionEditor.getModel().getAmount0()*apportionEditor.getModel().getExchangeRate());
									oldDebtAccountEditor.save();
								}
								if(!isNewProjectMember){
									HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
				                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() - apportionEditor.getModelCopy().getAmount0()*mMoneyDepositIncomeContainerEditor.getModelCopy().getExchangeRate());
				                	debtAccountEditor.save();
								}
			                }
						}
						
						MoneyBorrow moneyBorrow = null;
						if(apportion.get_mId() == null){
							moneyBorrow = new MoneyBorrow();
							moneyBorrow.setBorrowType("Deposit");
						} else {
							moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyDepositIncomeApportionId=?", apportion.getId()).executeSingle();
						}
						moneyBorrow.setMoneyDepositIncomeApportionId(apportionEditor.getModelCopy().getId());
						moneyBorrow.setAmount(apportionEditor.getModelCopy().getAmount0());
						moneyBorrow.setDate(mMoneyDepositIncomeContainerEditor.getModelCopy().getDate());
						moneyBorrow.setRemark(mMoneyDepositIncomeContainerEditor.getModelCopy().getRemark());
						moneyBorrow.setFriendUserId(apportionEditor.getModelCopy().getFriendUserId());
						if(apportionEditor.getModelCopy().getFriendUserId() == null){
							Friend friend = null;
							friend = HyjModel.getModel(Friend.class, apportionEditor.getModelCopy().getLocalFriendId());
							if(friend.getFriendUserId() != null){
								moneyBorrow.setFriendUserId(friend.getFriendUserId());
								moneyBorrow.setLocalFriendId(null);
							} else {
								moneyBorrow.setLocalFriendId(friend.getId());
							}
						}
						moneyBorrow.setExchangeRate(mMoneyDepositIncomeContainerEditor.getModelCopy().getExchangeRate());
						moneyBorrow.setGeoLat(mMoneyDepositIncomeContainerEditor.getModelCopy().getGeoLat());
						moneyBorrow.setGeoLon(mMoneyDepositIncomeContainerEditor.getModelCopy().getGeoLon());

						if(mMoneyDepositIncomeContainerEditor.getModelCopy().getMoneyAccountId() != null){
							MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mMoneyDepositIncomeContainerEditor.getModelCopy().getMoneyAccountId());
							moneyBorrow.setMoneyAccountId(mMoneyDepositIncomeContainerEditor.getModelCopy().getMoneyAccountId(), moneyAccount.getCurrencyId());
						} else {
							moneyBorrow.setMoneyAccountId(null, null);
						}

						Intent intent = getActivity().getIntent();
						String counterpartId = intent.getStringExtra("counterpartId");
						if(counterpartId != null){
							moneyBorrow.setMoneyLendId(counterpartId);
						}
						moneyBorrow.setLocation(mMoneyDepositIncomeContainerEditor.getModelCopy().getLocation());
						moneyBorrow.setAddress(mMoneyDepositIncomeContainerEditor.getModelCopy().getAddress());
						moneyBorrow.setPictureId(mMoneyDepositIncomeContainerEditor.getModelCopy().getPictureId());
						moneyBorrow.setProject(mMoneyDepositIncomeContainerEditor.getModelCopy().getProject());
						moneyBorrow.save();
						
						if(api.getState() != ApportionItem.UNCHANGED
								|| !mMoneyDepositIncomeContainerEditor.getModelCopy().getProjectId().equals(mMoneyDepositIncomeContainerEditor.getModel().getProjectId())
								|| !mMoneyDepositIncomeContainerEditor.getModelCopy().getMoneyAccountId().equals(mMoneyDepositIncomeContainerEditor.getModel().getMoneyAccountId())) {
							apportionEditor.save();
						}
						savedCount++;
					}
			}
			
			// 从隐藏掉的分摊里面删除原来的分摊
			Iterator<ApportionItem<MoneyApportion>> it = mApportionFieldApportions.getHiddenApportions().iterator();
			while (it.hasNext()) {
				// Get element
				ApportionItem<MoneyApportion> item = it.next();
				if (item.getState() != ApportionItem.NEW) {
					MoneyDepositIncomeApportion apportion = ((MoneyDepositIncomeApportion) item.getApportion());
					
						MoneyBorrow moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyDepositIncomeApportionId=?", apportion.getId()).executeSingle();
						if(moneyBorrow != null){
							if(!moneyBorrow.getProject().isProjectMember(moneyBorrow.getLocalFriendId(), moneyBorrow.getFriendUserId())){
								MoneyAccount debtAccount = null;
								debtAccount = MoneyAccount.getDebtAccount(moneyBorrow.getProject().getCurrencyId(), moneyBorrow.getLocalFriendId(), moneyBorrow.getFriendUserId());
								HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
								debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + moneyBorrow.getProjectAmount());
								debtAccountEditor.save();
							}
							moneyBorrow.delete();
						}
						apportion.delete();
				}
			}
			
			// 如果列表里一个都没有被保存，我们生成一个默认的分摊
			if (savedCount == 0) {
				MoneyDepositIncomeApportion apportion = new MoneyDepositIncomeApportion();
				apportion.setAmount(mMoneyDepositIncomeContainerEditor.getModelCopy().getAmount0());
				apportion.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
				apportion.setMoneyDepositIncomeContainerId(mMoneyDepositIncomeContainerEditor.getModelCopy().getId());
				apportion.setApportionType("Average");
				
				MoneyBorrow moneyBorrow = null;
				moneyBorrow = new MoneyBorrow();
				moneyBorrow.setBorrowType("Deposit");
				
				//自己一定是项目成员，所以不用更新借贷账户
//				
////				Friend friend = new Select().from(Friend.class).where("friendUserId = ?", apportion.getFriendUserId()).executeSingle();
//				MoneyAccount debtAccount = MoneyAccount.getDebtAccount(mMoneyDepositIncomeContainerEditor.getModelCopy().getProject().getCurrencyId(), apportion.getLocalFriendId(), apportion.getFriendUserId());
//			    
//				if(debtAccount == null){
//                	MoneyAccount.createDebtAccount(apportion.getLocalFriendId(), apportion.getFriendUserId(), mMoneyDepositIncomeContainerEditor.getModelCopy().getProject().getCurrencyId(), -apportion.getAmount0()*apportion.getExchangeRate());
//                }else{
//                	HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
//                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() - apportion.getAmount0()*apportion.getExchangeRate());
//                	debtAccountEditor.save();
//                }
				
				moneyBorrow.setMoneyDepositIncomeApportionId(apportion.getId());
				moneyBorrow.setAmount(apportion.getAmount0());
				moneyBorrow.setFriendUserId(apportion.getFriendUserId());
				moneyBorrow.setLocalFriendId(apportion.getLocalFriendId());
				moneyBorrow.setDate(mMoneyDepositIncomeContainerEditor.getModelCopy().getDate());
				moneyBorrow.setRemark(mMoneyDepositIncomeContainerEditor.getModelCopy().getRemark());
				moneyBorrow.setExchangeRate(mMoneyDepositIncomeContainerEditor.getModelCopy().getExchangeRate());
				moneyBorrow.setGeoLat(mMoneyDepositIncomeContainerEditor.getModelCopy().getGeoLat());
				moneyBorrow.setGeoLon(mMoneyDepositIncomeContainerEditor.getModelCopy().getGeoLon());

				if(mMoneyDepositIncomeContainerEditor.getModelCopy().getMoneyAccountId() != null){
					MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mMoneyDepositIncomeContainerEditor.getModelCopy().getMoneyAccountId());
					moneyBorrow.setMoneyAccountId(mMoneyDepositIncomeContainerEditor.getModelCopy().getMoneyAccountId(), moneyAccount.getCurrencyId());
				} else {
					moneyBorrow.setMoneyAccountId(null, null);
				}

				Intent intent = getActivity().getIntent();
				String counterpartId = intent.getStringExtra("counterpartId");
				if(counterpartId != null){
					moneyBorrow.setMoneyLendId(counterpartId);
				}
				moneyBorrow.setLocation(mMoneyDepositIncomeContainerEditor.getModelCopy().getLocation());
				moneyBorrow.setAddress(mMoneyDepositIncomeContainerEditor.getModelCopy().getAddress());
				moneyBorrow.setPictureId(mMoneyDepositIncomeContainerEditor.getModelCopy().getPictureId());
				moneyBorrow.setProject(mMoneyDepositIncomeContainerEditor.getModelCopy().getProject());
				moneyBorrow.save();
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
					
					if(mMoneyDepositIncomeContainerEditor.getModelCopy().get_mId() == null && !psa.getProjectShareMoneyIncomeAddNew()){
						HyjUtil.displayToast(R.string.app_permission_no_addnew);
						return;
					}else if(mMoneyDepositIncomeContainerEditor.getModelCopy().get_mId() != null && !psa.getProjectShareMoneyIncomeEdit()){
						HyjUtil.displayToast(R.string.app_permission_no_edit);
						return;
					}
	         		
	         		mSelectorFieldProject.setText(project.getDisplayName() + "(" + project.getCurrencyId() + ")");
	         		mSelectorFieldProject.setModelId(project.getId());
	         		setExchangeRate(false);
	         		mApportionFieldApportions.changeProject(project, MoneyDepositIncomeApportion.class);
					mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
	         	 }
	        	 break;

     		case GET_REMARK:
     			if (resultCode == Activity.RESULT_OK) {
     				String text = data.getStringExtra("TEXT");
     				mRemarkFieldRemark.setText(text);
     			}
     			break;
            	 
             case GET_APPORTION_MEMBER_ID:
     			if (resultCode == Activity.RESULT_OK) {
     				long _id = data.getLongExtra("MODEL_ID", -1);
    				String type = data.getStringExtra("MODEL_TYPE");
    				MoneyDepositIncomeApportion apportion = new MoneyDepositIncomeApportion();
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
    				apportion.setMoneyDepositIncomeContainerId(mMoneyDepositIncomeContainerEditor.getModel().getId());
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
		private static class MoneyDepositIncomeContainerEditor extends HyjModelEditor<MoneyDepositIncomeContainer> {
			private  ProjectShareAuthorization mOldProjectShareAuthorization;
			private  ProjectShareAuthorization mNewProjectShareAuthorization;
			
			public MoneyDepositIncomeContainerEditor(MoneyDepositIncomeContainer model) {
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
