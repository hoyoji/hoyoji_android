package com.hoyoji.hoyoji.money;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.hoyoji.hoyoji.models.MoneyIncomeCategory;
import com.hoyoji.hoyoji.models.MoneyIncome;
import com.hoyoji.hoyoji.models.MoneyIncomeApportion;
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


public class MoneyIncomeFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_FRIEND_ID = 3;
	private final static int GET_APPORTION_MEMBER_ID = 4;
	private final static int GET_CATEGORY_ID = 5;
	
	private int CREATE_EXCHANGE = 0;
	private int SET_EXCHANGE_RATE_FLAG = 1;
	private int UPDATE_SELF_PROJECTSHAREAUTHORIZATION = 1;
	
	private HyjModelEditor<MoneyIncome> mMoneyIncomeEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private MoneyApportionField mApportionFieldApportions = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericAmount = null;
	private HyjSelectorField mSelectorFieldMoneyAccount = null;
	private HyjSelectorField mSelectorFieldProject = null;
	private HyjNumericField mNumericExchangeRate = null;
	private HyjSelectorField mSelectorFieldMoneyIncomeCategory = null;
	private HyjSelectorField mSelectorFieldFriend = null;
	private HyjRemarkField mRemarkFieldRemark = null;
	private ImageView mImageViewRefreshRate = null;
	private View mViewSeparatorExchange = null;
	private LinearLayout mLinearLayoutExchangeRate = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.money_formfragment_moneyincome;
	}
	 
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		final MoneyIncome moneyIncome;
		
		Intent intent = getActivity().getIntent();
		long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			moneyIncome =  new Select().from(MoneyIncome.class).where("_id=?", modelId).executeSingle();
		} else {
			moneyIncome = new MoneyIncome();
		}
		mMoneyIncomeEditor = moneyIncome.newModelEditor();
		
		setupDeleteButton(mMoneyIncomeEditor);
		
		mImageFieldPicture = (HyjImageField) getView().findViewById(R.id.moneyIncomeFormFragment_imageField_picture);
		mImageFieldPicture.setImages(moneyIncome.getPictures());
		
		setupApportionField(moneyIncome);
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_date);	
		if(modelId != -1){
			mDateTimeFieldDate.setText(moneyIncome.getDate());
		}
		
		mNumericAmount = (HyjNumericField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_amount);		
		mNumericAmount.setNumber(moneyIncome.getAmount());
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
		
		MoneyAccount moneyAccount = moneyIncome.getMoneyAccount();
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
				
				MoneyIncomeFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, bundle, GET_MONEYACCOUNT_ID);
			}
		});	
		
		Project project = moneyIncome.getProject();
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(R.id.moneyIncomeFormFragment_selectorField_project);
		
		if(project != null){
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getName() + "(" + project.getCurrencyId() + ")");
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyIncomeFormFragment.this.openActivityWithFragmentForResult(ProjectListFragment.class, R.string.projectListFragment_title_select_project, null, GET_PROJECT_ID);
			}
		});	
		
		mNumericExchangeRate = (HyjNumericField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_exchangeRate);		
		mNumericExchangeRate.setNumber(moneyIncome.getExchangeRate());
		//mNumericExchangeRate.setVisibility(View.GONE);
		
		mViewSeparatorExchange = (View) getView().findViewById(R.id.moneyIncomeFormFragment_separatorField_exchange);
		mLinearLayoutExchangeRate = (LinearLayout) getView().findViewById(R.id.moneyIncomeFormFragment_linearLayout_exchangeRate);
		
		mSelectorFieldMoneyIncomeCategory = (HyjSelectorField) getView().findViewById(
				R.id.moneyIncomeFormFragment_textField_moneyIncomeCategory);
		mSelectorFieldMoneyIncomeCategory.setText(moneyIncome
				.getMoneyIncomeCategory());
		if(moneyIncome.getMoneyIncomeCategoryMain() != null && moneyIncome.getMoneyIncomeCategoryMain().length() > 0){
			mSelectorFieldMoneyIncomeCategory.setLabel(moneyIncome.getMoneyIncomeCategoryMain());
		}
		mSelectorFieldMoneyIncomeCategory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneyIncomeFormFragment.this
						.openActivityWithFragmentForResult(
								MoneyIncomeCategoryListFragment.class,
								R.string.moneyIncomeFormFragment_editText_hint_moneyIncomeCategory,
								null, GET_CATEGORY_ID);
			}
		});
		
		Friend friend = moneyIncome.getFriend();
		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(R.id.moneyIncomeFormFragment_selectorField_friend);
		
		if(friend != null){
			mSelectorFieldFriend.setModelId(friend.getId());
			mSelectorFieldFriend.setText(friend.getNickName());
		}
		mSelectorFieldFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyIncomeFormFragment.this
				.openActivityWithFragmentForResult(FriendListFragment.class, R.string.friendListFragment_title_select_friend_payee, null, GET_FRIEND_ID);
			}
		}); 
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyIncome.getRemark());
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyIncomeFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyIncome.getRemark());
		
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
						HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks(){
							@Override
							public void finishCallback(Object object) {
								HyjUtil.stopRoateView(mImageViewRefreshRate);
								mImageViewRefreshRate.setEnabled(true);
								mNumericExchangeRate.setNumber((Double)object);
							}
							@Override
							public void errorCallback(Object object) {
								HyjUtil.stopRoateView(mImageViewRefreshRate);
								mImageViewRefreshRate.setEnabled(true);
								if(object != null){
									HyjUtil.displayToast(object.toString());
								} else {
									HyjUtil.displayToast(R.string.moneyIncomeFormFragment_toast_cannot_refresh_rate);
								}
							}
						};
						HyjHttpGetExchangeRateAsyncTask.newInstance(fromCurrency, toCurrency, serverCallbacks);
					} else {
						HyjUtil.displayToast(R.string.moneyIncomeFormFragment_toast_select_currency);
					}
				}else{
					HyjUtil.displayToast(R.string.moneyIncomeFormFragment_toast_select_currency);
				}
			}
		});
		
			setExchangeRate();
			
			getView().findViewById(R.id.moneyIncomeFormFragment_imageButton_apportion_add).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Bundle bundle = new Bundle();
					Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
					bundle.putLong("MODEL_ID", project.get_mId());
					openActivityWithFragmentForResult(MemberListFragment.class, R.string.moneyApportionField_select_apportion_member, bundle, GET_APPORTION_MEMBER_ID);
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
							if (item.getItemId() == R.id.moneyApportionField_menu_moreActions_add_all) {
								Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
								List<ProjectShareAuthorization> projectShareAuthorizations = project.getShareAuthorizations();
								for (int i = 0; i < projectShareAuthorizations.size(); i++) {
									if(!projectShareAuthorizations.get(i).getState().equalsIgnoreCase("Accept")){
										continue;
									}
									MoneyIncomeApportion apportion = new MoneyIncomeApportion();
									apportion.setAmount(0.0);
									apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
									apportion.setMoneyIncomeId(moneyIncome.getId());

									mApportionFieldApportions.addApportion(apportion, project.getId(), ApportionItem.NEW);
								}
								mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
								return true;
							} else if (item.getItemId() == R.id.moneyApportionField_menu_moreActions_clear) {
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
					popup.show();
				}
			});
			// 只在新增时才自动打开软键盘， 修改时不自动打开
			if (modelId == -1) {
				this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			}
	}
	
	private void setupApportionField(MoneyIncome moneyIncome) {

		mApportionFieldApportions = (MoneyApportionField) getView().findViewById(R.id.moneyIncomeFormFragment_apportionField);
		
		List<MoneyIncomeApportion> moneyApportions = null;
		
		if(mMoneyIncomeEditor.getModelCopy().get_mId() == null) {
			
			moneyApportions = new ArrayList<MoneyIncomeApportion>();
			if(moneyIncome.getProject() != null && moneyIncome.getProject().getAutoApportion()){
				List<ProjectShareAuthorization> projectShareAuthorizations = moneyIncome.getProject().getShareAuthorizations();
				for(int i=0; i < projectShareAuthorizations.size(); i++){
					MoneyIncomeApportion apportion = new MoneyIncomeApportion();
					apportion.setAmount(0.0);
					apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
					apportion.setMoneyIncomeId(moneyIncome.getId());
					apportion.setApportionType("Share");
					
					moneyApportions.add(apportion);
				}
			} else if(moneyIncome.getProject() != null) {
				MoneyIncomeApportion apportion = new MoneyIncomeApportion();
				apportion.setAmount(0.0);
				apportion.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
				apportion.setMoneyIncomeId(moneyIncome.getId());
				apportion.setApportionType("Average");
				moneyApportions.add(apportion);
			}
			
		} else {
			moneyApportions = moneyIncome.getApportions();
		}
		
		mApportionFieldApportions.init(moneyIncome.getAmount0(), moneyApportions, moneyIncome.getProjectId(), moneyIncome.getId());
	}
	
	private void setupDeleteButton(HyjModelEditor<MoneyIncome> moneyIncomeEditor) {

		Button buttonDelete = (Button) getView().findViewById(R.id.button_delete);
		
		final MoneyIncome moneyIncome = moneyIncomeEditor.getModelCopy();
		
		if (moneyIncome.get_mId() == null) {
			buttonDelete.setVisibility(View.GONE);
		} else {
			buttonDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					((HyjActivity)getActivity()).displayDialog(R.string.app_action_delete_list_item, R.string.app_confirm_delete, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
							new DialogCallbackListener() {
								@Override
								public void doPositiveClick(Object object) {
									try {
										ActiveAndroid.beginTransaction();

										MoneyAccount moneyAccount = moneyIncome.getMoneyAccount();
										HyjModelEditor<MoneyAccount> moneyAccountEditor = moneyAccount.newModelEditor();
										moneyAccountEditor.getModelCopy().setCurrentBalance(moneyAccount.getCurrentBalance() - moneyIncome.getAmount());
										
										int updateProjectShareAuthorizationFlag = 1;
										//删除收入的同时删除分摊
										Iterator<MoneyIncomeApportion> moneyIncomeApportions = moneyIncome.getApportions().iterator();
										while (moneyIncomeApportions.hasNext()) {
											MoneyIncomeApportion moneyIncomeAportion = moneyIncomeApportions.next();
											if(moneyIncomeAportion.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
												updateProjectShareAuthorizationFlag = 0;
												moneyIncomeAportion._delete();
											}else{
												moneyIncomeAportion.delete();
											}
										}
										
										if(updateProjectShareAuthorizationFlag == 1){
											ProjectShareAuthorization projectShareAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(moneyIncome.getProjectId());
											HyjModelEditor<ProjectShareAuthorization> projectShareAuthorizationEditor = projectShareAuthorization.newModelEditor();
											projectShareAuthorizationEditor.getModelCopy().setActualTotalIncome(projectShareAuthorization.getActualTotalIncome() - (moneyIncome.getAmount0() * moneyIncome.getExchangeRate()));
											
											projectShareAuthorizationEditor.save();
										}
										moneyIncome.delete();
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
				}
			});
		}
		
	}
	
	private void setExchangeRate(){
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
				
				Exchange exchange = Exchange.getExchange(fromCurrency, toCurrency);
					if(exchange != null){
						mNumericExchangeRate.setNumber(exchange.getRate());
						CREATE_EXCHANGE = 0;
					}else{
						mNumericExchangeRate.setText(null);
						CREATE_EXCHANGE = 1;
					}
			}
			
		}else{
			mViewSeparatorExchange.setVisibility(View.GONE);
			mLinearLayoutExchangeRate.setVisibility(View.GONE);
		}
			SET_EXCHANGE_RATE_FLAG = 0;
	}
	
	private void fillData(){
		MoneyIncome modelCopy =  mMoneyIncomeEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setAmount(mNumericAmount.getNumber());
		modelCopy.setMoneyAccountId(mSelectorFieldMoneyAccount.getModelId());
		modelCopy.setProjectId(mSelectorFieldProject.getModelId());
		modelCopy.setExchangeRate(mNumericExchangeRate.getNumber());
		modelCopy.setMoneyIncomeCategory(mSelectorFieldMoneyIncomeCategory.getText().toString().trim());
		modelCopy.setMoneyIncomeCategoryMain(mSelectorFieldMoneyIncomeCategory.getLabel());
		
		if(mSelectorFieldFriend.getModelId() != null){
			Friend friend = HyjModel.getModel(Friend.class, mSelectorFieldFriend.getModelId());
			if(friend.getFriendUserId() != null){
				modelCopy.setFriendUserId(mSelectorFieldFriend.getModelId());
			}
			else{
				modelCopy.setLocalFriendId(mSelectorFieldFriend.getModelId());
			}
		}
		
		modelCopy.setRemark(mRemarkFieldRemark.getText().toString().trim());
		
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		mDateTimeFieldDate.setError(mMoneyIncomeEditor.getValidationError("datetime"));
		mNumericAmount.setError(mMoneyIncomeEditor.getValidationError("amount"));
		if(mMoneyIncomeEditor.getValidationError("amount") != null){
					mNumericAmount.showSoftKeyboard();
				}
		mSelectorFieldMoneyAccount.setError(mMoneyIncomeEditor.getValidationError("moneyAccount"));
		mSelectorFieldProject.setError(mMoneyIncomeEditor.getValidationError("project"));
		mNumericExchangeRate.setError(mMoneyIncomeEditor.getValidationError("exchangeRate"));
		mSelectorFieldMoneyIncomeCategory.setError(mMoneyIncomeEditor.getValidationError("moneyIncomeCategory"));
		mSelectorFieldFriend.setError(mMoneyIncomeEditor.getValidationError("friend"));
		mRemarkFieldRemark.setError(mMoneyIncomeEditor.getValidationError("remark"));
		mApportionFieldApportions.setError(mMoneyIncomeEditor.getValidationError("apportionTotalAmount"));
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		mMoneyIncomeEditor.validate();
		
		if (mApportionFieldApportions.getCount() > 0) {
			if (mNumericAmount.getNumber() != null && !mNumericAmount.getNumber().equals(mApportionFieldApportions.getTotalAmount())) {
				mMoneyIncomeEditor.setValidationError("apportionTotalAmount",R.string.moneyApportionField_select_toast_apportion_totalAmount_not_equal);
			} else {
				mMoneyIncomeEditor.removeValidationError("apportionTotalAmount");
			}
		} else {
			mMoneyIncomeEditor.removeValidationError("apportionTotalAmount");
		}
		
		if(mMoneyIncomeEditor.hasValidationErrors()){
			showValidatioErrors();
		} else {
			try {
				ActiveAndroid.beginTransaction();
				
				savePictures();

				saveApportions();
				
				MoneyIncome oldMoneyIncomeModel = mMoneyIncomeEditor.getModel();
				MoneyIncome moneyIncomeModel = mMoneyIncomeEditor.getModelCopy();
				
				//设置默认项目和账户
				UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
				if(moneyIncomeModel.get_mId() == null && !userData.getActiveMoneyAccountId().equals(moneyIncomeModel.getMoneyAccountId()) || !userData.getActiveProjectId().equals(moneyIncomeModel.getProjectId())){
					HyjModelEditor<UserData> userDataEditor = userData.newModelEditor();
					userDataEditor.getModelCopy().setActiveMoneyAccountId(moneyIncomeModel.getMoneyAccountId());
					userDataEditor.getModelCopy().setActiveProjectId(moneyIncomeModel.getProjectId());
					userDataEditor.save();
				}
				
				//当前汇率不存在时，创建汇率
				if(CREATE_EXCHANGE == 1){
					MoneyAccount moneyAccount = moneyIncomeModel.getMoneyAccount();
					Project project = moneyIncomeModel.getProject();
					
					Exchange newExchange = new Exchange();
					newExchange.setLocalCurrencyId(moneyAccount.getCurrencyId());
					newExchange.setForeignCurrencyId(project.getCurrencyId());
					newExchange.setRate(moneyIncomeModel.getExchangeRate());
					newExchange.save();
				}
				
				    MoneyAccount oldMoneyAccount = oldMoneyIncomeModel.getMoneyAccount();
					MoneyAccount newMoneyAccount = moneyIncomeModel.getMoneyAccount();
					HyjModelEditor<MoneyAccount> newMoneyAccountEditor = newMoneyAccount.newModelEditor();
					
					//更新账户余额
					if(moneyIncomeModel.get_mId() == null || oldMoneyAccount.getId().equals(newMoneyAccount.getId())){
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() - oldMoneyIncomeModel.getAmount0() + moneyIncomeModel.getAmount0());
					}else{
						HyjModelEditor<MoneyAccount> oldMoneyAccountEditor = oldMoneyAccount.newModelEditor();
						oldMoneyAccountEditor.getModelCopy().setCurrentBalance(oldMoneyAccount.getCurrentBalance() - oldMoneyIncomeModel.getAmount0());
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() + moneyIncomeModel.getAmount0());
						oldMoneyAccountEditor.save();
					}
					newMoneyAccountEditor.save();

					//更新支出所有者的实际收入
					if(UPDATE_SELF_PROJECTSHAREAUTHORIZATION == 1){
						ProjectShareAuthorization selfProjectAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(moneyIncomeModel.getProjectId());
						HyjModelEditor<ProjectShareAuthorization> selfProjectAuthorizationEditor = selfProjectAuthorization.newModelEditor();
					    if(moneyIncomeModel.get_mId() == null || oldMoneyIncomeModel.getProjectId().equals(moneyIncomeModel.getProjectId())){
					    	selfProjectAuthorizationEditor.getModelCopy().setActualTotalIncome(selfProjectAuthorization.getActualTotalIncome() - oldMoneyIncomeModel.getAmount0()*oldMoneyIncomeModel.getExchangeRate() + moneyIncomeModel.getAmount0()*moneyIncomeModel.getExchangeRate());
						}else{
							ProjectShareAuthorization oldSelfProjectAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(oldMoneyIncomeModel.getProjectId());
							HyjModelEditor<ProjectShareAuthorization> oldSelfProjectAuthorizationEditor = oldSelfProjectAuthorization.newModelEditor();
							oldSelfProjectAuthorizationEditor.getModelCopy().setActualTotalIncome(oldSelfProjectAuthorization.getActualTotalIncome() - oldMoneyIncomeModel.getAmount0()*oldMoneyIncomeModel.getExchangeRate());
							selfProjectAuthorizationEditor.getModelCopy().setActualTotalIncome(selfProjectAuthorization.getActualTotalIncome() + moneyIncomeModel.getAmount0()*moneyIncomeModel.getExchangeRate());
							oldSelfProjectAuthorizationEditor.save();
						}
						 selfProjectAuthorizationEditor.save();
					}
					
					//更新分类，使之成为最近使用过的
					if(this.mSelectorFieldMoneyIncomeCategory.getModelId() != null){
						MoneyIncomeCategory category = HyjModel.getModel(MoneyIncomeCategory.class, this.mSelectorFieldMoneyIncomeCategory.getModelId());
						if(category != null){
							category.newModelEditor().save();
						}
					}
					
				mMoneyIncomeEditor.save();
				HyjUtil.displayToast(R.string.app_save_success);
				ActiveAndroid.setTransactionSuccessful();
				getActivity().finish();
			} finally {
			    ActiveAndroid.endTransaction();
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
					newPic.setRecordId(mMoneyIncomeEditor.getModel().getId());
					newPic.setRecordType("Picture");
					newPic.save();
				} else if(pi.getState() == PictureItem.DELETED){
					pi.getPicture().delete();
				} else if(pi.getState() == PictureItem.CHANGED){

				}
				if(!mainPicSet && pi.getPicture() != null){
					mainPicSet = true;
					mMoneyIncomeEditor.getModelCopy().setPicture(pi.getPicture());
				}
			}
	 }
	
	 private void saveApportions() {
		 MoneyApportionField.ImageGridAdapter adapter = mApportionFieldApportions.getAdapter();
			int count = adapter.getCount();
			int savedCount = 0;
			for (int i = 0; i < count; i++) {
				ApportionItem<MoneyApportion> pi = adapter.getItem(i);
				MoneyIncomeApportion apportion = (MoneyIncomeApportion) pi.getApportion();
				HyjModelEditor<MoneyIncomeApportion> apportionEditor = apportion.newModelEditor();
	            
				ProjectShareAuthorization projectShareAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", 
						mMoneyIncomeEditor.getModelCopy().getProjectId(), apportion.getFriendUserId()).executeSingle();
				
				HyjModelEditor<ProjectShareAuthorization> projectShareAuthorizationEditor = projectShareAuthorization.newModelEditor();
				
				Double oldRate = mMoneyIncomeEditor.getModel().getExchangeRate(); 
				Double rate = mMoneyIncomeEditor.getModelCopy().getExchangeRate();
				Double oldApportionAmount = apportionEditor.getModel().getAmount0();
				
				if (pi.getState() != ApportionItem.DELETED) {
					pi.saveToCopy(apportionEditor.getModelCopy());
					savedCount++;
					
					//更新收入所有者的实际收入
					if(projectShareAuthorization.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
						UPDATE_SELF_PROJECTSHAREAUTHORIZATION = 0;
						
						if(mMoneyIncomeEditor.getModelCopy().get_mId() == null || mMoneyIncomeEditor.getModel().getProjectId().equals(mMoneyIncomeEditor.getModelCopy().getProjectId())){
							projectShareAuthorizationEditor.getModelCopy().setActualTotalIncome(projectShareAuthorization.getActualTotalIncome() - (mMoneyIncomeEditor.getModel().getAmount0() * oldRate) + (mMoneyIncomeEditor.getModelCopy().getAmount0() * rate));
						}else{
							ProjectShareAuthorization oldSelfProjectAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(mMoneyIncomeEditor.getModel().getProjectId());
							HyjModelEditor<ProjectShareAuthorization> oldSelfProjectAuthorizationEditor = oldSelfProjectAuthorization.newModelEditor();
							oldSelfProjectAuthorizationEditor.getModelCopy().setActualTotalIncome(oldSelfProjectAuthorization.getActualTotalIncome() - (mMoneyIncomeEditor.getModel().getAmount0() * oldRate));
							projectShareAuthorizationEditor.getModelCopy().setActualTotalIncome(projectShareAuthorization.getActualTotalIncome() + (mMoneyIncomeEditor.getModelCopy().getAmount0() * rate));
							
							//修改收入时 切换项目后保存 更新原项目的分摊金额  并把原项目和新项目都存在的分摊成员的oldApportionAmount设成0，这样维护新项目分摊金额时这条旧分摊就相当于新分摊
							if(apportionEditor.getModelCopy().get_mId() != null && !mMoneyIncomeEditor.getModel().getProjectId().equals(mMoneyIncomeEditor.getModelCopy().getProjectId())){
								oldSelfProjectAuthorizationEditor.getModelCopy().setApportionedTotalIncome(oldSelfProjectAuthorization.getApportionedTotalIncome() - (oldApportionAmount * oldRate));
								oldApportionAmount = 0.0;
							}
							oldSelfProjectAuthorizationEditor.save();
						}
					}else{
						//修改收入时 切换项目后保存 更新原项目的分摊金额  并把原项目和新项目都存在的分摊成员的oldApportionAmount设成0，这样维护新项目分摊金额时这条旧分摊就相当于新分摊
						if(apportionEditor.getModelCopy().get_mId() != null && !mMoneyIncomeEditor.getModel().getProjectId().equals(mMoneyIncomeEditor.getModelCopy().getProjectId())){
							ProjectShareAuthorization oldProjectAuthorization = ProjectShareAuthorization.getSelfProjectShareAuthorization(mMoneyIncomeEditor.getModel().getProjectId());
							HyjModelEditor<ProjectShareAuthorization> oldProjectAuthorizationEditor = oldProjectAuthorization.newModelEditor();
							oldProjectAuthorizationEditor.getModelCopy().setApportionedTotalIncome(oldProjectAuthorization.getApportionedTotalIncome() - (oldApportionAmount * oldRate));
							oldProjectAuthorizationEditor.save();
							oldApportionAmount = 0.0;
						}
						//更新相关好友的借贷账户
						MoneyAccount debtAccount = MoneyAccount.getDebtAccount(mMoneyIncomeEditor.getModelCopy().getMoneyAccount().getCurrencyId(), apportionEditor.getModelCopy().getFriendUserId());
						if(pi.getState() == ApportionItem.NEW){
			                if(debtAccount == null){
			                	MoneyAccount.createDebtAccount(apportionEditor.getModelCopy().getFriendUserId(), mMoneyIncomeEditor.getModelCopy().getMoneyAccount().getCurrencyId(), -apportionEditor.getModelCopy().getAmount0());
			                }else{
			                	HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
			                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() - apportionEditor.getModelCopy().getAmount0());
			                	debtAccountEditor.save();
			                }
						} else{
							MoneyAccount oldDebtAccount = MoneyAccount.getDebtAccount(mMoneyIncomeEditor.getModel().getMoneyAccount().getCurrencyId(), apportionEditor.getModelCopy().getFriendUserId());
							HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
							if(debtAccount == null){
								oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() + apportionEditor.getModel().getAmount0());
			                	MoneyAccount.createDebtAccount(apportionEditor.getModelCopy().getFriendUserId(), mMoneyIncomeEditor.getModelCopy().getMoneyAccount().getCurrencyId(), -apportionEditor.getModelCopy().getAmount0());
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
					
					//更新项目成员的分摊金额
					projectShareAuthorizationEditor.getModelCopy().setApportionedTotalIncome(projectShareAuthorizationEditor.getModelCopy().getApportionedTotalIncome() - (oldApportionAmount * oldRate) + (apportionEditor.getModelCopy().getAmount0() * rate));
					projectShareAuthorizationEditor.save();
					
					if(pi.getState() != ApportionItem.UNCHANGED) {
						apportionEditor.save();
					}
				} else{
					if(apportionEditor.getModelCopy().get_mId() != null && !mMoneyIncomeEditor.getModel().getProjectId().equals(mMoneyIncomeEditor.getModelCopy().getProjectId()) && projectShareAuthorization.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
						apportion._delete();
					}else{
						apportion.delete();
					}
				}
			}

			// 从隐藏掉的分摊里面删除原来的分摊
			Iterator<ApportionItem<MoneyApportion>> it = mApportionFieldApportions.getHiddenApportions().iterator();
			while (it.hasNext()) {
				// Get element
				ApportionItem<MoneyApportion> item = it.next();
				if (item.getState() != ApportionItem.NEW) {
					((MoneyIncomeApportion) item.getApportion()).delete();
				}
			}
			
			// 如果列表里一个都没有被保存，我们生成一个默认的分摊
			if (savedCount == 0) {
				MoneyIncomeApportion apportion = new MoneyIncomeApportion();
				apportion.setAmount(mMoneyIncomeEditor.getModelCopy().getAmount0());
				apportion.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
				apportion.setMoneyIncomeId(mMoneyIncomeEditor.getModelCopy().getId());
				apportion.setApportionType("Average");
				
				//更新项目成员的分摊金额
				ProjectShareAuthorization projectShareAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", 
						mMoneyIncomeEditor.getModelCopy().getProjectId(), apportion.getFriendUserId()).executeSingle();
				HyjModelEditor<ProjectShareAuthorization> projectShareAuthorizationEditor = projectShareAuthorization.newModelEditor();
				projectShareAuthorizationEditor.getModelCopy().setApportionedTotalIncome(projectShareAuthorizationEditor.getModelCopy().getApportionedTotalIncome() + (apportion.getAmount0() * mMoneyIncomeEditor.getModelCopy().getExchangeRate()));
				
				//更新收入所有者的实际收入
				if(projectShareAuthorization.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
					UPDATE_SELF_PROJECTSHAREAUTHORIZATION = 0;
					Double oldMoneyIncomeAmount = mMoneyIncomeEditor.getModel().getAmount0() * mMoneyIncomeEditor.getModel().getExchangeRate();
					if(!mMoneyIncomeEditor.getModel().getProjectId().equals(mMoneyIncomeEditor.getModelCopy().getProjectId()))
					{
						oldMoneyIncomeAmount = 0.0;
					}
						projectShareAuthorizationEditor.getModelCopy().setActualTotalIncome(projectShareAuthorizationEditor.getModelCopy().getActualTotalIncome() - oldMoneyIncomeAmount + (mMoneyIncomeEditor.getModelCopy().getAmount0() * mMoneyIncomeEditor.getModelCopy().getExchangeRate()));
					
				}
				
				projectShareAuthorizationEditor.save();
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
	         		setExchangeRate();
	         	 }
	        	 break;
             case GET_PROJECT_ID:
	        	 if(resultCode == Activity.RESULT_OK){
	         		long _id = data.getLongExtra("MODEL_ID", -1);
	         		Project project = Project.load(Project.class, _id);
	         		mSelectorFieldProject.setText(project.getName() + "(" + project.getCurrencyId() + ")");
	         		mSelectorFieldProject.setModelId(project.getId());
	         		setExchangeRate();
	         		mApportionFieldApportions.changeProject(project, MoneyIncomeApportion.class);
					mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
	         	 }
	        	 break;
	        	 
             case GET_FRIEND_ID:
            	 if(resultCode == Activity.RESULT_OK){
            		long _id = data.getLongExtra("MODEL_ID", -1);
            		Friend friend = Friend.load(Friend.class, _id);
            		mSelectorFieldFriend.setText(friend.getNickName());
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
     				ProjectShareAuthorization psa = ProjectShareAuthorization.load(ProjectShareAuthorization.class, _id);
     				if(!psa.getState().equalsIgnoreCase("Accept")){
     					HyjUtil.displayToast(R.string.moneyApportionField_select_toast_apportion_user_not_accepted);
     					break;
     				}
     				MoneyIncomeApportion apportion = new MoneyIncomeApportion();
     				apportion.setAmount(0.0);
     				apportion.setFriendUserId(psa.getFriendUserId());
     				apportion.setMoneyIncomeId(mMoneyIncomeEditor.getModel().getId());
     				if (mApportionFieldApportions.addApportion(apportion,mSelectorFieldProject.getModelId(), ApportionItem.NEW)) {
     					mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
     				} else {
     					HyjUtil.displayToast(R.string.moneyApportionField_select_toast_apportion_user_already_exists);
     				}
     			}
     			break;
          }
    }
}
