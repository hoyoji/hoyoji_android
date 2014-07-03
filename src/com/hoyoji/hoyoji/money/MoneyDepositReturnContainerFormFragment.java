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
import com.hoyoji.hoyoji.models.MoneyDepositIncomeApportion;
import com.hoyoji.hoyoji.models.MoneyReturn;
import com.hoyoji.hoyoji.models.MoneyDepositReturnApportion;
import com.hoyoji.hoyoji.models.MoneyDepositReturnContainer;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.MoneyApportionField.ApportionItem;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;


public class MoneyDepositReturnContainerFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_APPORTION_MEMBER_ID = 4;
	private static final int GET_REMARK = 5;
	
	private int CREATE_EXCHANGE = 0;
	private int SET_EXCHANGE_RATE_FLAG = 1;
	
	private MoneyDepositReturnContainerEditor mMoneyDepositReturnContainerEditor = null;
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
		return R.layout.money_formfragment_moneydepositreturncontainer;
	}
	 
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		final MoneyDepositReturnContainer moneyDepositReturnContainer;
		
		Intent intent = getActivity().getIntent();
		long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			moneyDepositReturnContainer =  new Select().from(MoneyDepositReturnContainer.class).where("_id=?", modelId).executeSingle();
			hasEditPermission = moneyDepositReturnContainer.hasEditPermission();
		} else {
			moneyDepositReturnContainer = new MoneyDepositReturnContainer();
			final String moneyAccountId = intent.getStringExtra("moneyAccountId");
			if(moneyDepositReturnContainer.get_mId() == null && moneyAccountId != null){
				moneyDepositReturnContainer.setMoneyAccountId(moneyAccountId);
			}
		}
		
		mMoneyDepositReturnContainerEditor = new MoneyDepositReturnContainerEditor(moneyDepositReturnContainer);
		
		setupDeleteButton(mMoneyDepositReturnContainerEditor);
		
		mImageFieldPicture = (HyjImageField) getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_imageField_picture);
		mImageFieldPicture.setImages(moneyDepositReturnContainer.getPictures());
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_textField_date);	
		if(modelId != -1){
			mDateTimeFieldDate.setText(moneyDepositReturnContainer.getDate());
		}
		
		Project project;
		String projectId = intent.getStringExtra("projectId");//从消息导入
		if(projectId != null){
			moneyDepositReturnContainer.setProjectId(projectId);
			project = HyjModel.getModel(Project.class, projectId);
		}else{
			project = moneyDepositReturnContainer.getProject();
		}
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_selectorField_project);
		
		if(project != null){
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getDisplayName() + "(" + project.getCurrencyId() + ")");
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyDepositReturnContainerFormFragment.this.openActivityWithFragmentForResult(ProjectListFragment.class, R.string.projectListFragment_title_select_project, null, GET_PROJECT_ID);
			}
		});	
		
		setupApportionField(moneyDepositReturnContainer);
		
		mNumericAmount = (HyjNumericField) getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_textField_amount);		
		double amount = intent.getDoubleExtra("amount", -1.0);//从分享消息导入的金额
		if(amount >= 0.0){
			mNumericAmount.setNumber(amount);
			mApportionFieldApportions.setTotalAmount(amount);
		}else{
			mNumericAmount.setNumber(moneyDepositReturnContainer.getAmount());
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
		
		MoneyAccount moneyAccount = moneyDepositReturnContainer.getMoneyAccount();
		mSelectorFieldMoneyAccount = (HyjSelectorField) getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_selectorField_moneyAccount);

		if(moneyAccount != null){
			mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
			mSelectorFieldMoneyAccount.setText(moneyAccount.getName() + "(" + moneyAccount.getCurrencyId() + ")");
		}
		mSelectorFieldMoneyAccount.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("excludeType", "Debt");
				
				MoneyDepositReturnContainerFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, bundle, GET_MONEYACCOUNT_ID);
			}
		});	
		

		mNumericExchangeRate = (HyjNumericField) getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_textField_exchangeRate);		
		mNumericExchangeRate.setNumber(moneyDepositReturnContainer.getExchangeRate());
		
		mViewSeparatorExchange = (View) getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_separatorField_exchange);
		mLinearLayoutExchangeRate = (LinearLayout) getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_linearLayout_exchangeRate);
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyDepositReturnContainer.getRemark());
		mRemarkFieldRemark.setEditable(false);
		mRemarkFieldRemark.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("TEXT", mRemarkFieldRemark.getText());
				bundle.putString("HINT", "请输入" + mRemarkFieldRemark.getLabelText());
				MoneyDepositReturnContainerFormFragment.this
						.openActivityWithFragmentForResult(
								HyjTextInputFormFragment.class,
								R.string.moneyExpenseFormFragment_textView_remark,
								bundle, GET_REMARK);
			}
		});
		
		ImageView takePictureButton = (ImageView) getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_imageView_camera);	
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
		
		mImageViewRefreshRate = (ImageView) getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_imageButton_refresh_exchangeRate);	
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
						HyjUtil.displayToast(R.string.moneyDepositReturnContainerFormFragment_toast_select_currency);
					}
				}else{
					HyjUtil.displayToast(R.string.moneyDepositReturnContainerFormFragment_toast_select_currency);
				}
			}
		});
		
			
			getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_imageButton_apportion_add).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Bundle bundle = new Bundle();
					Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
					bundle.putLong("MODEL_ID", project.get_mId());
					openActivityWithFragmentForResult(SelectApportionMemberListFragment.class, R.string.moneyDepositReturnContainerFormFragment_moneyApportionField_select_apportion_member, bundle, GET_APPORTION_MEMBER_ID);
				}
			});
			
			getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_imageButton_apportion_add_all).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					addAllProjectMemberIntoApportionsField(moneyDepositReturnContainer);
				}
			});
			
			getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_imageButton_apportion_more_actions).setOnClickListener(new OnClickListener() {
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
	    if(mMoneyDepositReturnContainerEditor!= null && mMoneyDepositReturnContainerEditor.getModelCopy().get_mId() != null && !hasEditPermission){
	    	hideSaveAction();
	    }
	}
	
	private void addAllProjectMemberIntoApportionsField(MoneyDepositReturnContainer moneyIncomeContainer) {
		Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
		List<ProjectShareAuthorization> projectShareAuthorizations = project.getShareAuthorizations();
		for (int i = 0; i < projectShareAuthorizations.size(); i++) {
			if(!projectShareAuthorizations.get(i).getState().equalsIgnoreCase("Accept")){
				continue;
			}
			MoneyDepositReturnApportion apportion = new MoneyDepositReturnApportion();
			apportion.setAmount(0.0);
			apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
			apportion.setMoneyDepositReturnContainerId(moneyIncomeContainer.getId());

			mApportionFieldApportions.addApportion(apportion, project.getId(), ApportionItem.NEW);
		}
		mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
		
	}
	
	private void setupApportionField(MoneyDepositReturnContainer moneyDepositReturnContainer) {

		mApportionFieldApportions = (MoneyApportionField) getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_apportionField);
		
		List<MoneyDepositReturnApportion> moneyApportions = null;
		
		if(mMoneyDepositReturnContainerEditor.getModelCopy().get_mId() == null) {
			
			moneyApportions = new ArrayList<MoneyDepositReturnApportion>();
//			if(moneyDepositReturnContainer.getProject() != null && moneyDepositReturnContainer.getProject().getAutoApportion()){
//				List<ProjectShareAuthorization> projectShareAuthorizations = moneyDepositReturnContainer.getProject().getShareAuthorizations();
//				for(int i=0; i < projectShareAuthorizations.size(); i++){
//					MoneyDepositReturnApportion apportion = new MoneyDepositReturnApportion();
//					apportion.setAmount(0.0);
//					apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
//					apportion.setMoneyDepositReturnContainerId(moneyDepositReturnContainer.getId());
//					apportion.setApportionType("Share");
//					
//					moneyApportions.add(apportion);
//				}
//			} else 
//			if(moneyDepositReturnContainer.getProject() != null) {
//				MoneyDepositReturnApportion apportion = new MoneyDepositReturnApportion();
//				apportion.setAmount(moneyDepositReturnContainer.getAmount0());
//				apportion.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
//				apportion.setMoneyDepositReturnContainerId(moneyDepositReturnContainer.getId());
//				apportion.setApportionType("Average");
//				moneyApportions.add(apportion);
//			}

			Intent intent = getActivity().getIntent();
			String friendUserId = intent.getStringExtra("friendUserId");
			if(friendUserId != null){
				MoneyDepositReturnApportion apportion = new MoneyDepositReturnApportion();
				apportion.setAmount(moneyDepositReturnContainer.getAmount0());
				apportion.setFriendUserId(friendUserId);
				apportion.setMoneyDepositReturnContainerId(moneyDepositReturnContainer.getId());
				apportion.setApportionType("Average");
				moneyApportions.add(apportion);
			}
		} else {
			moneyApportions = moneyDepositReturnContainer.getApportions();
		}
		
		mApportionFieldApportions.init(moneyDepositReturnContainer.getAmount0(), moneyApportions, moneyDepositReturnContainer.getProjectId(), moneyDepositReturnContainer.getId());
	}
	
	private void setupDeleteButton(HyjModelEditor<MoneyDepositReturnContainer> moneyDepositIncomeContainerEditor) {

		Button buttonDelete = (Button) getView().findViewById(R.id.button_delete);
		
		final MoneyDepositReturnContainer moneyDepositReturnContainer = moneyDepositIncomeContainerEditor.getModelCopy();
		
		if (moneyDepositReturnContainer.get_mId() == null) {
			buttonDelete.setVisibility(View.GONE);
		} else {
			buttonDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(moneyDepositReturnContainer.hasDeletePermission()){
					((HyjActivity)getActivity()).displayDialog(R.string.app_action_delete_list_item, R.string.app_confirm_delete, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
							new DialogCallbackListener() {
								@Override
								public void doPositiveClick(Object object) {
									try {
										ActiveAndroid.beginTransaction();

										MoneyAccount moneyAccount = moneyDepositReturnContainer.getMoneyAccount();
										HyjModelEditor<MoneyAccount> moneyAccountEditor = moneyAccount.newModelEditor();
										moneyAccountEditor.getModelCopy().setCurrentBalance(moneyAccount.getCurrentBalance() + moneyDepositReturnContainer.getAmount0());

										//更新项目余额
										Project oldProject = moneyDepositReturnContainer.getProject();
										HyjModelEditor<Project> oldProjectEditor = oldProject.newModelEditor();
										oldProjectEditor.getModelCopy().setDepositTotal(oldProject.getDepositTotal() + moneyDepositReturnContainer.getAmount0()*moneyDepositReturnContainer.getExchangeRate());
										oldProjectEditor.save();
										
										// 更新旧项目收入所有者的实际借入
										ProjectShareAuthorization oldProjectShareAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(oldProject.getId());
										HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
										oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalReturn(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalReturn() - (moneyDepositReturnContainer.getAmount0() * moneyDepositReturnContainer.getExchangeRate()));
										oldProjectShareAuthorizationEditor.save();
										
										//删除收入的同时删除分摊
										Iterator<MoneyDepositReturnApportion> moneyDepositIncomeApportions = moneyDepositReturnContainer.getApportions().iterator();
										while (moneyDepositIncomeApportions.hasNext()) {
											MoneyDepositReturnApportion moneyDepositIncomeAportion = moneyDepositIncomeApportions.next();
										
											MoneyReturn moneyReturn = new Select().from(MoneyReturn.class).where("moneyDepositReturnApportionId=?", moneyDepositIncomeAportion.getId()).executeSingle();
											if(moneyReturn != null){
												if(!oldProject.isProjectMember(moneyReturn.getLocalFriendId(), moneyReturn.getFriendUserId())){
													MoneyAccount debtAccount = MoneyAccount.getDebtAccount(moneyReturn.getProject().getCurrencyId(), moneyReturn.getLocalFriendId(), moneyReturn.getFriendUserId());
													HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
													debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() - moneyReturn.getProjectAmount());
													debtAccountEditor.save();
												}
												moneyReturn.delete();
											} 
												
											moneyDepositIncomeAportion.delete();
										}

										moneyDepositReturnContainer.delete();
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

		if(mMoneyDepositReturnContainerEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			mDateTimeFieldDate.setEnabled(false);
			
			mNumericAmount.setNumber(mMoneyDepositReturnContainerEditor.getModel().getProjectAmount());
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
			
			getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_imageButton_apportion_add).setEnabled(false);
			getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_imageButton_apportion_add_all).setEnabled(false);
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
		MoneyDepositReturnContainer modelCopy =  mMoneyDepositReturnContainerEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setAmount(mNumericAmount.getNumber());
		modelCopy.setMoneyAccountId(mSelectorFieldMoneyAccount.getModelId());
		modelCopy.setProject(HyjModel.getModel(Project.class, mSelectorFieldProject.getModelId()));
		modelCopy.setExchangeRate(mNumericExchangeRate.getNumber());
		
		modelCopy.setRemark(mRemarkFieldRemark.getText().toString().trim());
		
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		mDateTimeFieldDate.setError(mMoneyDepositReturnContainerEditor.getValidationError("datetime"));
		mNumericAmount.setError(mMoneyDepositReturnContainerEditor.getValidationError("amount"));
		if(mMoneyDepositReturnContainerEditor.getValidationError("amount") != null){
			mNumericAmount.showSoftKeyboard();
		}
		mSelectorFieldMoneyAccount.setError(mMoneyDepositReturnContainerEditor.getValidationError("moneyAccount"));
		mSelectorFieldProject.setError(mMoneyDepositReturnContainerEditor.getValidationError("project"));
		mNumericExchangeRate.setError(mMoneyDepositReturnContainerEditor.getValidationError("exchangeRate"));
		mRemarkFieldRemark.setError(mMoneyDepositReturnContainerEditor.getValidationError("remark"));
		mApportionFieldApportions.setError(mMoneyDepositReturnContainerEditor.getValidationError("apportionTotalAmount"));
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		if(mMoneyDepositReturnContainerEditor.getModelCopy().get_mId() == null && !mMoneyDepositReturnContainerEditor.getModelCopy().hasAddNewPermission(mMoneyDepositReturnContainerEditor.getModelCopy().getProjectId())){
			HyjUtil.displayToast(R.string.app_permission_no_addnew);
		}else if(mMoneyDepositReturnContainerEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			HyjUtil.displayToast(R.string.app_permission_no_edit);
		}else if(mApportionFieldApportions.getCount() <= 0){
			HyjUtil.displayToast("请选择至少一个收款成员");
		}else{
		
		mMoneyDepositReturnContainerEditor.validate();
		
		if (mApportionFieldApportions.getCount() > 0) {
			if (mNumericAmount.getNumber() != null && !mNumericAmount.getNumber().equals(mApportionFieldApportions.getTotalAmount())) {
				mMoneyDepositReturnContainerEditor.setValidationError("apportionTotalAmount",R.string.moneyApportionField_select_toast_apportion_totalAmount_not_equal);
			} else {
				mMoneyDepositReturnContainerEditor.removeValidationError("apportionTotalAmount");
			}
		} else {
			mMoneyDepositReturnContainerEditor.removeValidationError("apportionTotalAmount");
		}
		
		if(mMoneyDepositReturnContainerEditor.hasValidationErrors()){
			showValidatioErrors();
		} else {
			try {
				ActiveAndroid.beginTransaction();
				
				savePictures();

				saveApportions();
				
				MoneyDepositReturnContainer oldMoneyDepositReturnContainerModel = mMoneyDepositReturnContainerEditor.getModel();
				MoneyDepositReturnContainer moneyIncomeContainerModel = mMoneyDepositReturnContainerEditor.getModelCopy();
				
				//设置默认项目和账户
				UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
				if(moneyIncomeContainerModel.get_mId() == null && !userData.getActiveMoneyAccountId().equals(moneyIncomeContainerModel.getMoneyAccountId()) || !userData.getActiveProjectId().equals(moneyIncomeContainerModel.getProjectId())){
					HyjModelEditor<UserData> userDataEditor = userData.newModelEditor();
					userDataEditor.getModelCopy().setActiveMoneyAccountId(moneyIncomeContainerModel.getMoneyAccountId());
					userDataEditor.getModelCopy().setActiveProjectId(moneyIncomeContainerModel.getProjectId());
					userDataEditor.save();
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
				}else {
					Exchange exchange = null;
					Double exRate = null;
					Double rate = HyjUtil.toFixed2(moneyIncomeContainerModel.getExchangeRate());
					if(!localCurrencyId.equalsIgnoreCase(foreignCurrencyId)){
						exchange = Exchange.getExchange(localCurrencyId, foreignCurrencyId);
						if(exchange != null){
							exRate = exchange.getRate();
							if(!rate.equals(exRate)){
								HyjModelEditor<Exchange> exchangModelEditor = exchange.newModelEditor();
								exchangModelEditor.getModelCopy().setRate(rate);
								exchangModelEditor.save();
							}
						}
					} else {
						exchange = Exchange.getExchange(foreignCurrencyId, localCurrencyId);
						if(exchange != null){
							exRate = HyjUtil.toFixed2(1 / exchange.getRate());
							if(!rate.equals(exRate)){
								HyjModelEditor<Exchange> exchangModelEditor = exchange.newModelEditor();
								exchangModelEditor.getModelCopy().setRate(rate);
								exchangModelEditor.save();
							}
						}
					}
			}
				
				    MoneyAccount oldMoneyAccount = oldMoneyDepositReturnContainerModel.getMoneyAccount();
					MoneyAccount newMoneyAccount = moneyIncomeContainerModel.getMoneyAccount();
					HyjModelEditor<MoneyAccount> newMoneyAccountEditor = newMoneyAccount.newModelEditor();
					
					//更新账户余额
					if(moneyIncomeContainerModel.get_mId() == null || oldMoneyAccount.getId().equals(newMoneyAccount.getId())){
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() + oldMoneyDepositReturnContainerModel.getAmount0() - moneyIncomeContainerModel.getAmount0());
					}else{
						HyjModelEditor<MoneyAccount> oldMoneyAccountEditor = oldMoneyAccount.newModelEditor();
						oldMoneyAccountEditor.getModelCopy().setCurrentBalance(oldMoneyAccount.getCurrentBalance() + oldMoneyDepositReturnContainerModel.getAmount0());
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() - moneyIncomeContainerModel.getAmount0());
						oldMoneyAccountEditor.save();
					}
					newMoneyAccountEditor.save();

					Project oldProject = oldMoneyDepositReturnContainerModel.getProject();
					Project newProject = moneyIncomeContainerModel.getProject();
					HyjModelEditor<Project> newProjectEditor = newProject.newModelEditor();
					
					//更新项目余额
					if(moneyIncomeContainerModel.get_mId() == null || oldProject.getId().equals(newProject.getId())){
						newProjectEditor.getModelCopy().setDepositTotal(newProject.getDepositTotal() + oldMoneyDepositReturnContainerModel.getAmount0()*oldMoneyDepositReturnContainerModel.getExchangeRate() - moneyIncomeContainerModel.getAmount0()*moneyIncomeContainerModel.getExchangeRate());
					} else {
						HyjModelEditor<Project> oldProjectEditor = oldProject.newModelEditor();
						oldProjectEditor.getModelCopy().setDepositTotal(oldProject.getDepositTotal() + oldMoneyDepositReturnContainerModel.getAmount0()*oldMoneyDepositReturnContainerModel.getExchangeRate());
						newProjectEditor.getModelCopy().setDepositTotal(newProject.getDepositTotal() - moneyIncomeContainerModel.getAmount0()*moneyIncomeContainerModel.getExchangeRate());
						oldProjectEditor.save();
					}
					newProjectEditor.save();
					
					
					//更新支出所有者的实际借入
					ProjectShareAuthorization selfProjectAuthorization = mMoneyDepositReturnContainerEditor.getNewSelfProjectShareAuthorization();
					HyjModelEditor<ProjectShareAuthorization> selfProjectAuthorizationEditor = selfProjectAuthorization.newModelEditor();
				    
					if(moneyIncomeContainerModel.get_mId() == null || oldMoneyDepositReturnContainerModel.getProjectId().equals(moneyIncomeContainerModel.getProjectId())){
				    	selfProjectAuthorizationEditor.getModelCopy().setActualTotalReturn(selfProjectAuthorization.getActualTotalReturn() - oldMoneyDepositReturnContainerModel.getAmount0()*oldMoneyDepositReturnContainerModel.getExchangeRate() + moneyIncomeContainerModel.getAmount0()*moneyIncomeContainerModel.getExchangeRate());
					}else{
						selfProjectAuthorizationEditor.getModelCopy().setActualTotalReturn(selfProjectAuthorization.getActualTotalReturn() + moneyIncomeContainerModel.getAmount0()*moneyIncomeContainerModel.getExchangeRate());
						
						ProjectShareAuthorization oldSelfProjectAuthorization = mMoneyDepositReturnContainerEditor.getOldSelfProjectShareAuthorization();
						HyjModelEditor<ProjectShareAuthorization> oldSelfProjectAuthorizationEditor = oldSelfProjectAuthorization.newModelEditor();
						oldSelfProjectAuthorizationEditor.getModelCopy().setActualTotalReturn(oldSelfProjectAuthorization.getActualTotalReturn() - oldMoneyDepositReturnContainerModel.getAmount0()*oldMoneyDepositReturnContainerModel.getExchangeRate());
						oldSelfProjectAuthorizationEditor.save();
					}
					selfProjectAuthorizationEditor.save();

					
				mMoneyDepositReturnContainerEditor.save();
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
					newPic.setRecordId(mMoneyDepositReturnContainerEditor.getModel().getId());
					newPic.setRecordType("MoneyDepositReturnContainer");
					newPic.setDisplayOrder(i);
					newPic.save();
				} else if(pi.getState() == PictureItem.DELETED){
					pi.getPicture().delete();
				} else if(pi.getState() == PictureItem.CHANGED){

				}
				if (!mainPicSet && pi.getPicture() != null && pi.getState() != PictureItem.DELETED) {
					mainPicSet = true;
					mMoneyDepositReturnContainerEditor.getModelCopy().setPicture(pi.getPicture());
				}
			}
	 }
	
	 private void saveApportions() {
		 MoneyApportionField.ImageGridAdapter adapter = mApportionFieldApportions.getAdapter();
			int count = adapter.getCount();
			int savedCount = 0;
			for (int i = 0; i < count; i++) {
				ApportionItem<MoneyApportion> api = adapter.getItem(i);
				MoneyDepositReturnApportion apportion = (MoneyDepositReturnApportion) api.getApportion();
	            
					if(api.getState() == ApportionItem.DELETED ){
						MoneyReturn moneyReturn = new Select().from(MoneyReturn.class).where("moneyDepositReturnApportionId=?", apportion.getId()).executeSingle();
						if(moneyReturn != null){
							if(!moneyReturn.getProject().isProjectMember(moneyReturn.getLocalFriendId(), moneyReturn.getFriendUserId())){
								MoneyAccount debtAccount = MoneyAccount.getDebtAccount(moneyReturn.getProject().getCurrencyId(), moneyReturn.getLocalFriendId(), moneyReturn.getFriendUserId());
								HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
								debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() - moneyReturn.getProjectAmount());
								debtAccountEditor.save();
							}
							moneyReturn.delete();
						} 
						apportion.delete();
					} else {
						HyjModelEditor<MoneyDepositReturnApportion> apportionEditor = apportion.newModelEditor();
						if(api.getState() != ApportionItem.UNCHANGED		
								|| !mMoneyDepositReturnContainerEditor.getModelCopy().getProjectId().equals(mMoneyDepositReturnContainerEditor.getModel().getProjectId())
								|| !mMoneyDepositReturnContainerEditor.getModelCopy().getMoneyAccountId().equals(mMoneyDepositReturnContainerEditor.getModel().getMoneyAccountId())) {
							api.saveToCopy(apportionEditor.getModelCopy());
						}
						
//						Friend friend = null;
//						// 该好友是网络好友 或 该好友是本地好友
//						if(apportion.getFriendUserId() == null) {
//							friend = HyjModel.getModel(Friend.class, apportion.getLocalFriendId());
//						} else {
//							friend = new Select().from(Friend.class).where("friendUserId = ?", apportion.getFriendUserId()).executeSingle();
//						}
						MoneyAccount debtAccount = null;
						// 该好友是网络好友 或 该好友是本地好友
						boolean isNewProjectMember = mMoneyDepositReturnContainerEditor.getModelCopy().getProject().isProjectMember(apportion.getLocalFriendId(), apportion.getFriendUserId());
						if(!isNewProjectMember){
							debtAccount = MoneyAccount.getDebtAccount(mMoneyDepositReturnContainerEditor.getModelCopy().getProject().getCurrencyId(), apportion.getLocalFriendId(), apportion.getFriendUserId());
						}
						if(api.getState() == ApportionItem.NEW){
			                if(debtAccount != null){
			                	HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
			                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + apportionEditor.getModelCopy().getAmount0()*mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
			                	debtAccountEditor.save();
			                }else if(!isNewProjectMember){
			                	MoneyAccount.createDebtAccount(apportion.getLocalFriendId(), apportion.getFriendUserId(), mMoneyDepositReturnContainerEditor.getModelCopy().getProject().getCurrencyId(), apportionEditor.getModelCopy().getAmount0()*mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
				            }
						} else{
							MoneyAccount oldDebtAccount = null;
							if(!mMoneyDepositReturnContainerEditor.getModel().getProject().isProjectMember(apportionEditor.getModel().getLocalFriendId(), apportionEditor.getModel().getFriendUserId())){
								oldDebtAccount = MoneyAccount.getDebtAccount(mMoneyDepositReturnContainerEditor.getModel().getProject().getCurrencyId(), apportionEditor.getModel().getLocalFriendId(), apportionEditor.getModel().getFriendUserId());
							}
							if(debtAccount == null){
								if(oldDebtAccount != null){
									HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
									oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - apportionEditor.getModel().getAmount0()*apportionEditor.getModel().getExchangeRate());
									oldDebtAccountEditor.save();
								}
								if(!isNewProjectMember){
									MoneyAccount.createDebtAccount(apportion.getLocalFriendId(), apportion.getFriendUserId(), mMoneyDepositReturnContainerEditor.getModelCopy().getProject().getCurrencyId(), apportionEditor.getModelCopy().getAmount0()*mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
								}
							}else if(oldDebtAccount != null && debtAccount.getId().equals(oldDebtAccount.getId())){
								HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
			                	oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - apportionEditor.getModel().getAmount0()*apportionEditor.getModel().getExchangeRate() + apportionEditor.getModelCopy().getAmount0()*mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
								oldDebtAccountEditor.save();
							}else{
								if(oldDebtAccount != null){
									HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
				                	oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - apportionEditor.getModel().getAmount0()*apportionEditor.getModel().getExchangeRate());
									oldDebtAccountEditor.save();
								}
								if(!isNewProjectMember){
									HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
				                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + apportionEditor.getModelCopy().getAmount0()*mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
				                	debtAccountEditor.save();
								}
			                }
						}
						
						MoneyReturn moneyReturn = null;
						if(apportion.get_mId() == null){
							moneyReturn = new MoneyReturn();
							moneyReturn.setReturnType("Deposit");
						} else {
							moneyReturn = new Select().from(MoneyReturn.class).where("moneyDepositReturnApportionId=?", apportion.getId()).executeSingle();
						}
						moneyReturn.setMoneyDepositReturnApportionId(apportionEditor.getModelCopy().getId());
						moneyReturn.setAmount(apportionEditor.getModelCopy().getAmount0());
						moneyReturn.setDate(mMoneyDepositReturnContainerEditor.getModelCopy().getDate());
						moneyReturn.setRemark(mMoneyDepositReturnContainerEditor.getModelCopy().getRemark());
						moneyReturn.setFriendUserId(apportionEditor.getModelCopy().getFriendUserId());
						if(apportionEditor.getModelCopy().getFriendUserId() == null){
							Friend friend = null;
							friend = HyjModel.getModel(Friend.class, apportionEditor.getModelCopy().getLocalFriendId());
							if(friend.getFriendUserId() != null){
								moneyReturn.setFriendUserId(friend.getFriendUserId());
								moneyReturn.setLocalFriendId(null);
							} else {
								moneyReturn.setLocalFriendId(friend.getId());
							}
						}
						moneyReturn.setExchangeRate(mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
						moneyReturn.setGeoLat(mMoneyDepositReturnContainerEditor.getModelCopy().getGeoLat());
						moneyReturn.setGeoLon(mMoneyDepositReturnContainerEditor.getModelCopy().getGeoLon());

						if(mMoneyDepositReturnContainerEditor.getModelCopy().getMoneyAccountId() != null){
							MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mMoneyDepositReturnContainerEditor.getModelCopy().getMoneyAccountId());
							moneyReturn.setMoneyAccountId(mMoneyDepositReturnContainerEditor.getModelCopy().getMoneyAccountId(), moneyAccount.getCurrencyId());
						} else {
							moneyReturn.setMoneyAccountId(null, null);
						}

						Intent intent = getActivity().getIntent();
						String counterpartId = intent.getStringExtra("counterpartId");
						if(counterpartId != null){
							moneyReturn.setMoneyPaybackId(counterpartId);
						}
						
						moneyReturn.setLocation(mMoneyDepositReturnContainerEditor.getModelCopy().getLocation());
						moneyReturn.setAddress(mMoneyDepositReturnContainerEditor.getModelCopy().getAddress());
						moneyReturn.setPictureId(mMoneyDepositReturnContainerEditor.getModelCopy().getPictureId());
						moneyReturn.setProject(mMoneyDepositReturnContainerEditor.getModelCopy().getProject());
						moneyReturn.save();
						
						if(api.getState() != ApportionItem.UNCHANGED
								|| !mMoneyDepositReturnContainerEditor.getModelCopy().getProjectId().equals(mMoneyDepositReturnContainerEditor.getModel().getProjectId())
								|| !mMoneyDepositReturnContainerEditor.getModelCopy().getMoneyAccountId().equals(mMoneyDepositReturnContainerEditor.getModel().getMoneyAccountId())) {
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
					MoneyDepositReturnApportion apportion = ((MoneyDepositReturnApportion) item.getApportion());
					
						MoneyReturn moneyReturn = new Select().from(MoneyReturn.class).where("moneyDepositReturnApportionId=?", apportion.getId()).executeSingle();
						if(moneyReturn != null){
							if(!moneyReturn.getProject().isProjectMember(moneyReturn.getLocalFriendId(), moneyReturn.getFriendUserId())){
								MoneyAccount debtAccount = MoneyAccount.getDebtAccount(moneyReturn.getProject().getCurrencyId(), moneyReturn.getLocalFriendId(), moneyReturn.getFriendUserId());
								HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
								debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() - moneyReturn.getProjectAmount());
								debtAccountEditor.save();
							}
							
							moneyReturn.delete();
						}
						apportion.delete();
				}
			}
			
			// 如果列表里一个都没有被保存，我们生成一个默认的分摊
			if (savedCount == 0) {
				MoneyDepositReturnApportion apportion = new MoneyDepositReturnApportion();
				apportion.setAmount(mMoneyDepositReturnContainerEditor.getModelCopy().getAmount0());
				apportion.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
				apportion.setMoneyDepositReturnContainerId(mMoneyDepositReturnContainerEditor.getModelCopy().getId());
				apportion.setApportionType("Average");
				
				MoneyReturn moneyReturn = null;
				moneyReturn = new MoneyReturn();
				moneyReturn.setReturnType("Deposit");
				
////				Friend friend = new Select().from(Friend.class).where("friendUserId = ?", apportion.getFriendUserId()).executeSingle();
//				MoneyAccount debtAccount = MoneyAccount.getDebtAccount(mMoneyDepositReturnContainerEditor.getModelCopy().getProject().getCurrencyId(), apportion.getLocalFriendId(), apportion.getFriendUserId());
//			    if(debtAccount == null){
//                	MoneyAccount.createDebtAccount(apportion.getLocalFriendId(), apportion.getFriendUserId(), mMoneyDepositReturnContainerEditor.getModelCopy().getProject().getCurrencyId(), apportion.getAmount0()*apportion.getExchangeRate());
//                }else{
//                	HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
//                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + apportion.getAmount0()*apportion.getExchangeRate());
//                	debtAccountEditor.save();
//                }
				
				moneyReturn.setMoneyDepositReturnApportionId(apportion.getId());
				moneyReturn.setAmount(apportion.getAmount0());
				moneyReturn.setFriendUserId(apportion.getFriendUserId());
				moneyReturn.setLocalFriendId(apportion.getLocalFriendId());
				moneyReturn.setDate(mMoneyDepositReturnContainerEditor.getModelCopy().getDate());
				moneyReturn.setRemark(mMoneyDepositReturnContainerEditor.getModelCopy().getRemark());
				moneyReturn.setExchangeRate(mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
				moneyReturn.setGeoLat(mMoneyDepositReturnContainerEditor.getModelCopy().getGeoLat());
				moneyReturn.setGeoLon(mMoneyDepositReturnContainerEditor.getModelCopy().getGeoLon());

				if(mMoneyDepositReturnContainerEditor.getModelCopy().getMoneyAccountId() != null){
					MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mMoneyDepositReturnContainerEditor.getModelCopy().getMoneyAccountId());
					moneyReturn.setMoneyAccountId(mMoneyDepositReturnContainerEditor.getModelCopy().getMoneyAccountId(), moneyAccount.getCurrencyId());
				} else {
					moneyReturn.setMoneyAccountId(null, null);
				}

				Intent intent = getActivity().getIntent();
				String counterpartId = intent.getStringExtra("counterpartId");
				if(counterpartId != null){
					moneyReturn.setMoneyPaybackId(counterpartId);
				}
				
				moneyReturn.setLocation(mMoneyDepositReturnContainerEditor.getModelCopy().getLocation());
				moneyReturn.setAddress(mMoneyDepositReturnContainerEditor.getModelCopy().getAddress());
				moneyReturn.setPictureId(mMoneyDepositReturnContainerEditor.getModelCopy().getPictureId());
				moneyReturn.setProject(mMoneyDepositReturnContainerEditor.getModelCopy().getProject());
				moneyReturn.save();
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
					
					if(mMoneyDepositReturnContainerEditor.getModelCopy().get_mId() == null && !psa.getProjectShareMoneyIncomeAddNew()){
						HyjUtil.displayToast(R.string.app_permission_no_addnew);
						return;
					}else if(mMoneyDepositReturnContainerEditor.getModelCopy().get_mId() != null && !psa.getProjectShareMoneyIncomeEdit()){
						HyjUtil.displayToast(R.string.app_permission_no_edit);
						return;
					}
	         		
	         		mSelectorFieldProject.setText(project.getDisplayName() + "(" + project.getCurrencyId() + ")");
	         		mSelectorFieldProject.setModelId(project.getId());
	         		setExchangeRate(false);
	         		mApportionFieldApportions.changeProject(project, MoneyDepositReturnApportion.class);
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
    				MoneyDepositReturnApportion apportion = new MoneyDepositReturnApportion();
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
    				apportion.setMoneyDepositReturnContainerId(mMoneyDepositReturnContainerEditor.getModel().getId());
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
		private static class MoneyDepositReturnContainerEditor extends HyjModelEditor<MoneyDepositReturnContainer> {
			private  ProjectShareAuthorization mOldProjectShareAuthorization;
			private  ProjectShareAuthorization mNewProjectShareAuthorization;
			
			public MoneyDepositReturnContainerEditor(MoneyDepositReturnContainer model) {
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
