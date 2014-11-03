package com.hoyoji.hoyoji.money;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.hoyoji.hoyoji.models.MoneyReturn;
import com.hoyoji.hoyoji.models.MoneyDepositIncomeApportion;
import com.hoyoji.hoyoji.models.MoneyPayback;
import com.hoyoji.hoyoji.models.MoneyPayback;
import com.hoyoji.hoyoji.models.MoneyReturn;
import com.hoyoji.hoyoji.models.MoneyDepositReturnApportion;
import com.hoyoji.hoyoji.models.MoneyDepositReturnContainer;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.MoneyApportionField.ApportionItem;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.project.ExplainFinancialOwnerFragment;
import com.hoyoji.hoyoji.project.MemberListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;


public class MoneyDepositReturnContainerFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_APPORTION_MEMBER_ID = 4;
	private static final int GET_REMARK = 5;
	protected static final int GET_FINANCIALOWNER_ID = 0;
	
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
	private TextView mTextViewApportionFieldTitle;
	private DataSetObserver mApportionCountObserver;
	private HyjSelectorField mSelectorFieldFinancialOwner;
	private TextView mTextViewFinancialOwner;
	private ImageButton mButtonExpandMore;
	private LinearLayout mLinearLayoutExpandMore;
	
	@Override
	public Integer useContentView() {
		return R.layout.money_formfragment_moneydepositreturncontainer;
	}
	 
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		final MoneyDepositReturnContainer moneyDepositReturnContainer;
		
		Intent intent = getActivity().getIntent();
		final long modelId = intent.getLongExtra("MODEL_ID", -1);
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
			double exchangeRate = intent.getDoubleExtra("exchangeRate", 1.0);
			mNumericAmount.setNumber(amount*exchangeRate);
			mApportionFieldApportions.setTotalAmount(amount*exchangeRate);
		}else{
			mNumericAmount.setNumber(moneyDepositReturnContainer.getAmount());
		}
//		if(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor() != null){
			mNumericAmount.getEditText().setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
			mNumericAmount.getEditText().setHintTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
//		}
		
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

		mSelectorFieldFinancialOwner = (HyjSelectorField) getView().findViewById(R.id.projectFormFragment_selectorField_financialOwner);
		mSelectorFieldFinancialOwner.setEnabled(hasEditPermission);
		if(modelId == -1){
			if(project.getFinancialOwnerUserId() != null){
				mSelectorFieldFinancialOwner.setModelId(project.getFinancialOwnerUserId());
				mSelectorFieldFinancialOwner.setText(Friend.getFriendUserDisplayName(project.getFinancialOwnerUserId()));
			}
		} else if(moneyDepositReturnContainer.getFinancialOwnerUserId() != null){
				mSelectorFieldFinancialOwner.setModelId(moneyDepositReturnContainer.getFinancialOwnerUserId());
				mSelectorFieldFinancialOwner.setText(Friend.getFriendUserDisplayName(moneyDepositReturnContainer.getFinancialOwnerUserId()));
		}
		
		mSelectorFieldFinancialOwner.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mSelectorFieldProject.getModelId() == null){
					HyjUtil.displayToast("请先选择一个项目。");
				} else {
					Bundle bundle = new Bundle();
					Project project = HyjModel.getModel(Project.class, mSelectorFieldProject.getModelId());
					bundle.putLong("MODEL_ID", project.get_mId());
					bundle.putString("NULL_ITEM", (String)mSelectorFieldFinancialOwner.getHint());
					openActivityWithFragmentForResult(MemberListFragment.class, R.string.friendListFragment_title_select_friend_creditor, bundle, GET_FINANCIALOWNER_ID);
				}
			}
		});
		
		mTextViewFinancialOwner = (TextView) getView().findViewById(R.id.projectFormFragment_textView_hint_financialOwner);
		mTextViewFinancialOwner.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneyDepositReturnContainerFormFragment.this.openActivityWithFragment(ExplainFinancialOwnerFragment.class, R.string.explainFinancialOwnerFragment_title, null);
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
					openActivityWithFragmentForResult(MemberListFragment.class, R.string.moneyDepositReturnContainerFormFragment_moneyApportionField_select_apportion_member, bundle, GET_APPORTION_MEMBER_ID);
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
			
			mLinearLayoutExpandMore = (LinearLayout)getView().findViewById(R.id.moneyExpenseFormFragment_expandMore);
			mButtonExpandMore = (ImageButton)getView().findViewById(R.id.expand_more);
			mButtonExpandMore.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mLinearLayoutExpandMore.getVisibility() == View.GONE){
						mButtonExpandMore.setImageResource(R.drawable.ic_action_collapse);
						mLinearLayoutExpandMore.setVisibility(View.VISIBLE);
					} else {
						mButtonExpandMore.setImageResource(R.drawable.ic_action_expand);
						mLinearLayoutExpandMore.setVisibility(View.GONE);
					}
				}
			});

			// 在修改模式下自动展开
			if(modelId != -1){
				mButtonExpandMore.setImageResource(R.drawable.ic_action_collapse);
				mLinearLayoutExpandMore.setVisibility(View.VISIBLE);
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
			if(projectShareAuthorizations.get(i).getState().equalsIgnoreCase("Delete")){
				continue;
			}
			MoneyDepositReturnApportion apportion = new MoneyDepositReturnApportion();
			apportion.setAmount(0.0);
			apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
			apportion.setLocalFriendId(projectShareAuthorizations.get(i).getLocalFriendId());
			apportion.setMoneyDepositReturnContainerId(moneyIncomeContainer.getId());
			if(projectShareAuthorizations.get(i).getSharePercentageType() != null && projectShareAuthorizations.get(i).getSharePercentageType().equals("Average")){
				apportion.setApportionType("Average");
			} else {
				apportion.setApportionType("Share");
			}
			mApportionFieldApportions.addApportion(apportion, project.getId(), ApportionItem.NEW);
		}
		mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
		
	}
	
	private void setupApportionField(MoneyDepositReturnContainer moneyDepositReturnContainer) {

		mApportionFieldApportions = (MoneyApportionField) getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_apportionField);
		mTextViewApportionFieldTitle = (TextView) getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_apportionField_title);
		mApportionCountObserver = new DataSetObserver(){
	        @Override
	        public void onChanged() {
	    		mTextViewApportionFieldTitle.setText(getString(R.string.moneyDepositReturnContainerFormFragment_moneyApportionField_title)+"("+mApportionFieldApportions.getApportionCount()+")");
	        }
		};
		mApportionFieldApportions.getAdapter().registerDataSetObserver(mApportionCountObserver);
		
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
				ProjectShareAuthorization projectShareAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", moneyDepositReturnContainer.getProjectId(), friendUserId).executeSingle();
				if(projectShareAuthorization.getSharePercentageType() != null && projectShareAuthorization.getSharePercentageType().equals("Average")){
					apportion.setApportionType("Average");
				} else {
					apportion.setApportionType("Share");
				}
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
										Iterator<MoneyDepositReturnApportion> moneyDepositReturnApportions = moneyDepositReturnContainer.getApportions().iterator();
										while (moneyDepositReturnApportions.hasNext()) {
											MoneyDepositReturnApportion moneyDepositReturnAportion = moneyDepositReturnApportions.next();
											// 维护缴款人的 ProjectShareAuthorization
											ProjectShareAuthorization psa = moneyDepositReturnAportion.getProjectShareAuthorization();
											HyjModelEditor<ProjectShareAuthorization> psaEditor = psa.newModelEditor();
											psaEditor.getModelCopy().setActualTotalPayback(psa.getActualTotalPayback() - moneyDepositReturnAportion.getAmount0()*mMoneyDepositReturnContainerEditor.getModel().getExchangeRate());
											psaEditor.save();
											
											List<MoneyReturn> moneyReturns = new Select().from(MoneyReturn.class).where("moneyDepositReturnApportionId=?", moneyDepositReturnAportion.getId()).execute();
											for(MoneyReturn moneyReturn : moneyReturns){
												if(HyjApplication.getInstance().getCurrentUser().getId().equals(moneyReturn.getOwnerUserId())){
													MoneyAccount debtAccount;
													if(mMoneyDepositReturnContainerEditor.getModel().getFinancialOwnerUserId() != null){
														debtAccount = MoneyAccount.getDebtAccount(moneyReturn.getProject().getCurrencyId(), null, mMoneyDepositReturnContainerEditor.getModel().getFinancialOwnerUserId());
													} else {
														debtAccount = MoneyAccount.getDebtAccount(moneyReturn.getProject().getCurrencyId(), moneyReturn.getLocalFriendId(), moneyReturn.getFriendUserId());
													}
													HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
													debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() - moneyReturn.getProjectAmount());
													debtAccountEditor.save();
												}
												moneyReturn.delete();
											} 

											List<MoneyPayback> moneyPaybacks = new Select().from(MoneyPayback.class).where("moneyDepositReturnApportionId=?", moneyDepositReturnAportion.getId()).execute();
											for(MoneyPayback moneyPayback : moneyPaybacks){
												moneyPayback.delete();
											}
												
											moneyDepositReturnAportion.delete();
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
			getView().findViewById(R.id.moneyDepositReturnContainerFormFragment_separatorField_moneyAccount).setVisibility(View.GONE);

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
						mNumericExchangeRate.setNumber(null);
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
		modelCopy.setFinancialOwnerUserId(mSelectorFieldFinancialOwner.getModelId());
		
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
				MoneyDepositReturnContainer newMoneyDepositReturnContainerModel = mMoneyDepositReturnContainerEditor.getModelCopy();
				
				//设置默认项目和账户
				UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
				if(newMoneyDepositReturnContainerModel.get_mId() == null && !userData.getActiveMoneyAccountId().equals(newMoneyDepositReturnContainerModel.getMoneyAccountId()) || !userData.getActiveProjectId().equals(newMoneyDepositReturnContainerModel.getProjectId())){
					HyjModelEditor<UserData> userDataEditor = userData.newModelEditor();
					userDataEditor.getModelCopy().setActiveMoneyAccountId(newMoneyDepositReturnContainerModel.getMoneyAccountId());
					userDataEditor.getModelCopy().setActiveProjectId(newMoneyDepositReturnContainerModel.getProjectId());
					userDataEditor.save();
				}
				
				//当前汇率不存在时，创建汇率
				String localCurrencyId = newMoneyDepositReturnContainerModel.getMoneyAccount().getCurrencyId();
				String foreignCurrencyId = newMoneyDepositReturnContainerModel.getProject().getCurrencyId();
				if(CREATE_EXCHANGE == 1){
					MoneyAccount moneyAccount = newMoneyDepositReturnContainerModel.getMoneyAccount();
					Project project = newMoneyDepositReturnContainerModel.getProject();
					
					Exchange newExchange = new Exchange();
					newExchange.setLocalCurrencyId(moneyAccount.getCurrencyId());
					newExchange.setForeignCurrencyId(project.getCurrencyId());
					newExchange.setRate(newMoneyDepositReturnContainerModel.getExchangeRate());
					newExchange.save();
				}else {
					if(!localCurrencyId.equalsIgnoreCase(foreignCurrencyId)){
						Exchange exchange = null;
						Double exRate = null;
						Double rate = HyjUtil.toFixed2(newMoneyDepositReturnContainerModel.getExchangeRate());
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
				
				    MoneyAccount oldMoneyAccount = oldMoneyDepositReturnContainerModel.getMoneyAccount();
					MoneyAccount newMoneyAccount = newMoneyDepositReturnContainerModel.getMoneyAccount();
					HyjModelEditor<MoneyAccount> newMoneyAccountEditor = newMoneyAccount.newModelEditor();
					
					//更新账户余额
					if(newMoneyDepositReturnContainerModel.get_mId() == null || oldMoneyAccount.getId().equals(newMoneyAccount.getId())){
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() + oldMoneyDepositReturnContainerModel.getAmount0() - newMoneyDepositReturnContainerModel.getAmount0());
					}else{
						HyjModelEditor<MoneyAccount> oldMoneyAccountEditor = oldMoneyAccount.newModelEditor();
						oldMoneyAccountEditor.getModelCopy().setCurrentBalance(oldMoneyAccount.getCurrentBalance() + oldMoneyDepositReturnContainerModel.getAmount0());
						newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() - newMoneyDepositReturnContainerModel.getAmount0());
						oldMoneyAccountEditor.save();
					}
					newMoneyAccountEditor.save();

					Project oldProject = oldMoneyDepositReturnContainerModel.getProject();
					Project newProject = newMoneyDepositReturnContainerModel.getProject();
					HyjModelEditor<Project> newProjectEditor = newProject.newModelEditor();
					
					//更新项目余额
					if(newMoneyDepositReturnContainerModel.get_mId() == null || oldProject.getId().equals(newProject.getId())){
						newProjectEditor.getModelCopy().setDepositTotal(newProject.getDepositTotal() + oldMoneyDepositReturnContainerModel.getAmount0()*oldMoneyDepositReturnContainerModel.getExchangeRate() - newMoneyDepositReturnContainerModel.getAmount0()*newMoneyDepositReturnContainerModel.getExchangeRate());
					} else {
						HyjModelEditor<Project> oldProjectEditor = oldProject.newModelEditor();
						oldProjectEditor.getModelCopy().setDepositTotal(oldProject.getDepositTotal() + oldMoneyDepositReturnContainerModel.getAmount0()*oldMoneyDepositReturnContainerModel.getExchangeRate());
						newProjectEditor.getModelCopy().setDepositTotal(newProject.getDepositTotal() - newMoneyDepositReturnContainerModel.getAmount0()*newMoneyDepositReturnContainerModel.getExchangeRate());
						oldProjectEditor.save();
					}
					newProjectEditor.save();
					
					
					//更新支出所有者的实际借入
					ProjectShareAuthorization selfProjectAuthorization = mMoneyDepositReturnContainerEditor.getNewSelfProjectShareAuthorization();
					HyjModelEditor<ProjectShareAuthorization> selfProjectAuthorizationEditor = selfProjectAuthorization.newModelEditor();
				    
					if(newMoneyDepositReturnContainerModel.get_mId() == null || oldMoneyDepositReturnContainerModel.getProjectId().equals(newMoneyDepositReturnContainerModel.getProjectId())){
				    	selfProjectAuthorizationEditor.getModelCopy().setActualTotalReturn(selfProjectAuthorization.getActualTotalReturn() - oldMoneyDepositReturnContainerModel.getAmount0()*oldMoneyDepositReturnContainerModel.getExchangeRate() + newMoneyDepositReturnContainerModel.getAmount0()*newMoneyDepositReturnContainerModel.getExchangeRate());
					}else{
						selfProjectAuthorizationEditor.getModelCopy().setActualTotalReturn(selfProjectAuthorization.getActualTotalReturn() + newMoneyDepositReturnContainerModel.getAmount0()*newMoneyDepositReturnContainerModel.getExchangeRate());
						
						ProjectShareAuthorization oldSelfProjectAuthorization = mMoneyDepositReturnContainerEditor.getOldSelfProjectShareAuthorization();
						HyjModelEditor<ProjectShareAuthorization> oldSelfProjectAuthorizationEditor = oldSelfProjectAuthorization.newModelEditor();
						oldSelfProjectAuthorizationEditor.getModelCopy().setActualTotalReturn(oldSelfProjectAuthorization.getActualTotalReturn() - oldMoneyDepositReturnContainerModel.getAmount0()*oldMoneyDepositReturnContainerModel.getExchangeRate());
						oldSelfProjectAuthorizationEditor.save();
					}
					selfProjectAuthorizationEditor.save();

					
				mMoneyDepositReturnContainerEditor.save();
				ActiveAndroid.setTransactionSuccessful();
				if(getActivity().getCallingActivity() != null){
					getActivity().setResult(Activity.RESULT_OK);
				} else {
					HyjUtil.displayToast(R.string.app_save_success);
				}
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
						
						// 维护缴款人的 ProjectShareAuthorization
						ProjectShareAuthorization psa = api.getProjectShareAuthorization();
						HyjModelEditor<ProjectShareAuthorization> psaEditor = psa.newModelEditor();
						psaEditor.getModelCopy().setActualTotalReturn(psa.getActualTotalReturn() - apportion.getAmount0()*mMoneyDepositReturnContainerEditor.getModel().getExchangeRate());
						psaEditor.save();

						List<MoneyReturn> moneyReturns = new Select().from(MoneyReturn.class).where("moneyDepositReturnApportionId=?", apportion.getId()).execute();
						for(MoneyReturn moneyReturn : moneyReturns){
							if(HyjApplication.getInstance().getCurrentUser().getId().equals(moneyReturn.getOwnerUserId())) {
								MoneyAccount debtAccount = null;
								if(mMoneyDepositReturnContainerEditor.getModel().getFinancialOwnerUserId() != null){
									debtAccount = MoneyAccount.getDebtAccount(moneyReturn.getProject().getCurrencyId(), null, mMoneyDepositReturnContainerEditor.getModel().getFinancialOwnerUserId());
								} else {
									debtAccount = MoneyAccount.getDebtAccount(moneyReturn.getProject().getCurrencyId(), moneyReturn.getLocalFriendId(), moneyReturn.getFriendUserId());
								}
								HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
								debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() - moneyReturn.getProjectAmount());
								debtAccountEditor.save();
								moneyReturn.delete();
							}
						}
						List<MoneyPayback> moneyPaybacks = new Select().from(MoneyPayback.class).where("moneyDepositReturnApportionId=?", apportion.getId()).execute();
						for(MoneyPayback moneyPayback : moneyPaybacks){
							moneyPayback.delete();
						}
						apportion.delete();
					} else {
						HyjModelEditor<MoneyDepositReturnApportion> apportionEditor = apportion.newModelEditor();
						if(api.getState() != ApportionItem.UNCHANGED		
								|| !mMoneyDepositReturnContainerEditor.getModelCopy().getProjectId().equals(mMoneyDepositReturnContainerEditor.getModel().getProjectId())
								|| !mMoneyDepositReturnContainerEditor.getModelCopy().getMoneyAccountId().equals(mMoneyDepositReturnContainerEditor.getModel().getMoneyAccountId())) {
							api.saveToCopy(apportionEditor.getModelCopy());
						}
						
						MoneyAccount debtAccount = null;
						// 该好友是网络好友 或 该好友是本地好友
						if(mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId() != null){
							if(mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
								debtAccount = MoneyAccount.getDebtAccount(mMoneyDepositReturnContainerEditor.getModelCopy().getProject().getCurrencyId(), apportion.getLocalFriendId(), apportion.getFriendUserId());
							} else {
								debtAccount = MoneyAccount.getDebtAccount(mMoneyDepositReturnContainerEditor.getModelCopy().getProject().getCurrencyId(), null, mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId());
							}
						} else {
							debtAccount = MoneyAccount.getDebtAccount(mMoneyDepositReturnContainerEditor.getModelCopy().getProject().getCurrencyId(), apportion.getLocalFriendId(), apportion.getFriendUserId());
						}
						if(api.getState() == ApportionItem.NEW){
			                if(debtAccount != null){
			                	HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
			                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + apportionEditor.getModelCopy().getAmount0()*mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
			                	debtAccountEditor.save();
			                }else{
			                	if(mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId() != null){
									if(mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
										MoneyAccount.createDebtAccount(apportion.getFriendDisplayName(mMoneyDepositReturnContainerEditor.getModelCopy().getProject().getId()), apportion.getLocalFriendId(), apportion.getFriendUserId(), mMoneyDepositReturnContainerEditor.getModelCopy().getProject().getCurrencyId(), apportionEditor.getModelCopy().getAmount0()*mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
									} else {
				                		MoneyAccount.createDebtAccount(apportion.getFriendDisplayName(mMoneyDepositReturnContainerEditor.getModelCopy().getProjectId()), null, mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId(), mMoneyDepositReturnContainerEditor.getModelCopy().getProject().getCurrencyId(), apportionEditor.getModelCopy().getAmount()*mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
				                	}
			                	} else {
			                		MoneyAccount.createDebtAccount(apportion.getFriendDisplayName(mMoneyDepositReturnContainerEditor.getModelCopy().getProjectId()), apportion.getLocalFriendId(), apportion.getFriendUserId(), mMoneyDepositReturnContainerEditor.getModelCopy().getProject().getCurrencyId(), apportionEditor.getModelCopy().getAmount()*mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
					            }
							}
						} else{
							MoneyAccount oldDebtAccount = null;
							if(mMoneyDepositReturnContainerEditor.getModel().getFinancialOwnerUserId() != null){
								if(mMoneyDepositReturnContainerEditor.getModel().getFinancialOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
									oldDebtAccount = MoneyAccount.getDebtAccount(mMoneyDepositReturnContainerEditor.getModel().getProject().getCurrencyId(), apportionEditor.getModel().getLocalFriendId(), apportionEditor.getModel().getFriendUserId());
								} else {
									oldDebtAccount = MoneyAccount.getDebtAccount(mMoneyDepositReturnContainerEditor.getModel().getProject().getCurrencyId(), null, mMoneyDepositReturnContainerEditor.getModel().getFinancialOwnerUserId());
								}
							} else {
								oldDebtAccount = MoneyAccount.getDebtAccount(mMoneyDepositReturnContainerEditor.getModel().getProject().getCurrencyId(), apportionEditor.getModel().getLocalFriendId(), apportionEditor.getModel().getFriendUserId());
							}
							
							if(debtAccount == null){
								if(oldDebtAccount != null){
									HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
									oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - apportionEditor.getModel().getAmount0()*apportionEditor.getModel().getExchangeRate());
									oldDebtAccountEditor.save();
								}
								if(mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId() != null){
									if(mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
										MoneyAccount.createDebtAccount(apportion.getFriendDisplayName(mMoneyDepositReturnContainerEditor.getModelCopy().getProject().getId()), apportion.getLocalFriendId(), apportion.getFriendUserId(), mMoneyDepositReturnContainerEditor.getModelCopy().getProject().getCurrencyId(), apportionEditor.getModelCopy().getAmount0()*mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
									} else {
										MoneyAccount.createDebtAccount(apportion.getFriendDisplayName(mMoneyDepositReturnContainerEditor.getModelCopy().getProjectId()), null, mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId(), mMoneyDepositReturnContainerEditor.getModelCopy().getProject().getCurrencyId(), apportionEditor.getModelCopy().getAmount0()*mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
									}
								} else {
									MoneyAccount.createDebtAccount(apportion.getFriendDisplayName(mMoneyDepositReturnContainerEditor.getModelCopy().getProject().getId()), apportion.getLocalFriendId(), apportion.getFriendUserId(), mMoneyDepositReturnContainerEditor.getModelCopy().getProject().getCurrencyId(), apportionEditor.getModelCopy().getAmount0()*mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
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
								HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
				               	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + apportionEditor.getModelCopy().getAmount0()*mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
				               	debtAccountEditor.save();
							}
						}
						

						// 维护缴款人的 ProjectShareAuthorization
						ProjectShareAuthorization newPsa = api.getProjectShareAuthorization();
						HyjModelEditor<ProjectShareAuthorization> newPsaEditor = newPsa.newModelEditor();
						if(apportion.get_mId() == null) {
							newPsaEditor.getModelCopy().setActualTotalPayback(newPsa.getActualTotalPayback() + apportionEditor.getModelCopy().getAmount0()*mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
						} else if(mMoneyDepositReturnContainerEditor.getModelCopy().getProjectId().equals(mMoneyDepositReturnContainerEditor.getModel().getProjectId())){
							newPsaEditor.getModelCopy().setActualTotalPayback(newPsa.getActualTotalPayback() - apportionEditor.getModel().getAmount0()*mMoneyDepositReturnContainerEditor.getModel().getExchangeRate() + apportionEditor.getModelCopy().getAmount0()*mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
						} else {
							ProjectShareAuthorization oldPsa;
							if(apportion.getFriendUserId() != null){
								oldPsa = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId AND state <> 'Delete'", mMoneyDepositReturnContainerEditor.getModel().getProjectId(), apportion.getFriendUserId()).executeSingle();
							} else {
								oldPsa = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND localFriendId AND state <> 'Delete'", mMoneyDepositReturnContainerEditor.getModel().getProjectId(), apportion.getLocalFriendId()).executeSingle();
							}
							HyjModelEditor<ProjectShareAuthorization> oldPsaEditor = oldPsa.newModelEditor();
							oldPsaEditor.getModelCopy().setActualTotalPayback(oldPsa.getActualTotalPayback() - apportionEditor.getModel().getAmount0()*mMoneyDepositReturnContainerEditor.getModel().getExchangeRate());
							newPsaEditor.getModelCopy().setActualTotalPayback(newPsa.getActualTotalPayback() + apportionEditor.getModelCopy().getAmount0()*mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
						}
						newPsaEditor.save();
						
						MoneyReturn moneyReturn = null;
						MoneyPayback moneyPayback = null;
						MoneyReturn moneyReturnOfFinancialOwner = null;
						MoneyPayback moneyPaybackOfFinancialOwner = null;
						if(apportion.get_mId() != null){
							moneyReturn = new Select().from(MoneyReturn.class).where("moneyDepositReturnApportionId=? AND ownerUserId=?", apportionEditor.getModel().getId(), HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
							if(apportionEditor.getModel().getLocalFriendId() != null){
								moneyPayback = new Select().from(MoneyPayback.class).where("moneyDepositReturnApportionId=? AND ownerFriendId=?", apportionEditor.getModel().getId(), apportionEditor.getModel().getLocalFriendId()).executeSingle();
							} else {
								moneyPayback = new Select().from(MoneyPayback.class).where("moneyDepositReturnApportionId=? AND ownerUserId=?", apportionEditor.getModel().getId(), apportionEditor.getModel().getFriendUserId()).executeSingle();
							}
							if(mMoneyDepositReturnContainerEditor.getModel().getFinancialOwnerUserId() != null &&
									!mMoneyDepositReturnContainerEditor.getModel().getFinancialOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
								moneyReturnOfFinancialOwner = new Select().from(MoneyReturn.class).where("moneyDepositReturnApportionId=? AND ownerUserId=?", apportionEditor.getModel().getId(), mMoneyDepositReturnContainerEditor.getModel().getFinancialOwnerUserId()).executeSingle();
								moneyPaybackOfFinancialOwner = new Select().from(MoneyPayback.class).where("moneyDepositReturnApportionId=? AND ownerUserId=?", apportionEditor.getModel().getId(), mMoneyDepositReturnContainerEditor.getModel().getFinancialOwnerUserId()).executeSingle();
							}
							String previousFinancialOwnerUserId = HyjUtil.ifNull(mMoneyDepositReturnContainerEditor.getModel().getFinancialOwnerUserId() , "");
							String currentFinancialOwnerUserId = HyjUtil.ifNull(mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId() , "");
							if(!previousFinancialOwnerUserId.equals(currentFinancialOwnerUserId)){
								if(moneyReturn != null){
									moneyReturn.delete();
									moneyReturn = new MoneyReturn();
								}
								if(moneyPayback != null){
									moneyPayback.delete();
									moneyPayback = new MoneyPayback();
								}
								if(moneyReturnOfFinancialOwner != null){
									moneyReturnOfFinancialOwner.delete();
									moneyReturnOfFinancialOwner = new MoneyReturn();
								}
								if(moneyPaybackOfFinancialOwner != null){
									moneyPaybackOfFinancialOwner.delete();
									moneyPaybackOfFinancialOwner = new MoneyPayback();
								}
							}
						}
						if(moneyReturn == null){
							moneyReturn = new MoneyReturn();
						}
						if(moneyPayback == null){
							moneyPayback = new MoneyPayback();
						}
						if(moneyReturnOfFinancialOwner == null){
							moneyReturnOfFinancialOwner = new MoneyReturn();
						}
						if(moneyPaybackOfFinancialOwner == null){
							moneyPaybackOfFinancialOwner = new MoneyPayback();
						}
						
						moneyReturn.setMoneyDepositReturnApportionId(apportionEditor.getModelCopy().getId());
						moneyReturn.setAmount(apportionEditor.getModelCopy().getAmount0());
						moneyReturn.setDate(mMoneyDepositReturnContainerEditor.getModelCopy().getDate());
						moneyReturn.setRemark(mMoneyDepositReturnContainerEditor.getModelCopy().getRemark());
						if(mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId() == null
								|| HyjApplication.getInstance().getCurrentUser().getId().equals(mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId())){
							moneyReturn.setFriendUserId(apportionEditor.getModelCopy().getFriendUserId());
							moneyReturn.setLocalFriendId(apportionEditor.getModelCopy().getLocalFriendId());
						} else {
							moneyReturn.setFriendUserId(mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId());
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

//						Intent intent = getActivity().getIntent();
//						String counterpartId = intent.getStringExtra("counterpartId");
//						if(counterpartId != null){
//							moneyReturn.setmoneyPaybackId(counterpartId);
//						}
						moneyReturn.setLocation(mMoneyDepositReturnContainerEditor.getModelCopy().getLocation());
						moneyReturn.setAddress(mMoneyDepositReturnContainerEditor.getModelCopy().getAddress());
						moneyReturn.setPictureId(mMoneyDepositReturnContainerEditor.getModelCopy().getPictureId());
						moneyReturn.setProject(mMoneyDepositReturnContainerEditor.getModelCopy().getProject());
						moneyReturn.save();
						
						if(apportionEditor.getModelCopy().getLocalFriendId() != null){
							moneyPayback.setMoneyDepositReturnApportionId(apportionEditor.getModelCopy().getId());
//							moneyPayback.setmoneyReturnId(moneyReturn.getId());
							moneyPayback.setAmount(apportionEditor.getModelCopy().getAmount0());
							moneyPayback.setDate(mMoneyDepositReturnContainerEditor.getModelCopy().getDate());
							moneyPayback.setRemark(mMoneyDepositReturnContainerEditor.getModelCopy().getRemark());
							if(mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId() == null
									|| HyjApplication.getInstance().getCurrentUser().getId().equals(mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId())){
								moneyPayback.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
							} else {
								moneyPayback.setFriendUserId(mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId());
							}
							moneyPayback.setLocalFriendId(null);
							moneyPayback.setExchangeRate(mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
							moneyPayback.setGeoLat(mMoneyDepositReturnContainerEditor.getModelCopy().getGeoLat());
							moneyPayback.setGeoLon(mMoneyDepositReturnContainerEditor.getModelCopy().getGeoLon());
							moneyPayback.setMoneyAccountId(null, moneyReturn.getCurrencyId1());
							moneyPayback.setLocation(mMoneyDepositReturnContainerEditor.getModelCopy().getLocation());
							moneyPayback.setAddress(mMoneyDepositReturnContainerEditor.getModelCopy().getAddress());
							moneyPayback.setPictureId(mMoneyDepositReturnContainerEditor.getModelCopy().getPictureId());
							moneyPayback.setProject(mMoneyDepositReturnContainerEditor.getModelCopy().getProject());
							moneyPayback.setOwnerFriendId(apportionEditor.getModel().getLocalFriendId());
							moneyPayback.setOwnerUserId("");
							moneyPayback.save();
						}
						
						if(mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId() != null
								&& !HyjApplication.getInstance().getCurrentUser().getId().equals(mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId())){
							moneyReturnOfFinancialOwner.setMoneyDepositReturnApportionId(apportionEditor.getModelCopy().getId());
							moneyReturnOfFinancialOwner.setAmount(apportionEditor.getModelCopy().getAmount0());
							moneyReturnOfFinancialOwner.setDate(mMoneyDepositReturnContainerEditor.getModelCopy().getDate());
							moneyReturnOfFinancialOwner.setRemark(mMoneyDepositReturnContainerEditor.getModelCopy().getRemark());
							moneyReturnOfFinancialOwner.setFriendUserId(apportionEditor.getModelCopy().getFriendUserId());
							moneyReturnOfFinancialOwner.setLocalFriendId(apportionEditor.getModelCopy().getLocalFriendId());
							moneyReturnOfFinancialOwner.setExchangeRate(mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
							moneyReturnOfFinancialOwner.setGeoLat(mMoneyDepositReturnContainerEditor.getModelCopy().getGeoLat());
							moneyReturnOfFinancialOwner.setGeoLon(mMoneyDepositReturnContainerEditor.getModelCopy().getGeoLon());
							moneyReturnOfFinancialOwner.setOwnerUserId(mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId());
							
							if(mMoneyDepositReturnContainerEditor.getModelCopy().getMoneyAccountId() != null){
								MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mMoneyDepositReturnContainerEditor.getModelCopy().getMoneyAccountId());
								moneyReturnOfFinancialOwner.setMoneyAccountId(mMoneyDepositReturnContainerEditor.getModelCopy().getMoneyAccountId(), moneyAccount.getCurrencyId());
							} else {
								moneyReturnOfFinancialOwner.setMoneyAccountId(null, null);
							}
							moneyReturnOfFinancialOwner.setLocation(mMoneyDepositReturnContainerEditor.getModelCopy().getLocation());
							moneyReturnOfFinancialOwner.setAddress(mMoneyDepositReturnContainerEditor.getModelCopy().getAddress());
							moneyReturnOfFinancialOwner.setPictureId(mMoneyDepositReturnContainerEditor.getModelCopy().getPictureId());
							moneyReturnOfFinancialOwner.setProject(mMoneyDepositReturnContainerEditor.getModelCopy().getProject());
							moneyReturnOfFinancialOwner.save();
							
							moneyPaybackOfFinancialOwner.setMoneyDepositReturnApportionId(apportionEditor.getModelCopy().getId());
//							moneyPaybackOfFinancialOwner.setmoneyReturnId(moneyReturn.getId());
							moneyPaybackOfFinancialOwner.setAmount(apportionEditor.getModelCopy().getAmount0());
							moneyPaybackOfFinancialOwner.setDate(mMoneyDepositReturnContainerEditor.getModelCopy().getDate());
							moneyPaybackOfFinancialOwner.setRemark(mMoneyDepositReturnContainerEditor.getModelCopy().getRemark());
							moneyPaybackOfFinancialOwner.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
							moneyPaybackOfFinancialOwner.setLocalFriendId(null);
							moneyPaybackOfFinancialOwner.setExchangeRate(mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
							moneyPaybackOfFinancialOwner.setGeoLat(mMoneyDepositReturnContainerEditor.getModelCopy().getGeoLat());
							moneyPaybackOfFinancialOwner.setGeoLon(mMoneyDepositReturnContainerEditor.getModelCopy().getGeoLon());
							moneyPaybackOfFinancialOwner.setMoneyAccountId(null, moneyReturn.getCurrencyId1());
							moneyPaybackOfFinancialOwner.setLocation(mMoneyDepositReturnContainerEditor.getModelCopy().getLocation());
							moneyPaybackOfFinancialOwner.setAddress(mMoneyDepositReturnContainerEditor.getModelCopy().getAddress());
							moneyPaybackOfFinancialOwner.setPictureId(mMoneyDepositReturnContainerEditor.getModelCopy().getPictureId());
							moneyPaybackOfFinancialOwner.setProject(mMoneyDepositReturnContainerEditor.getModelCopy().getProject());
							moneyPaybackOfFinancialOwner.setOwnerUserId(mMoneyDepositReturnContainerEditor.getModelCopy().getFinancialOwnerUserId());
							moneyPaybackOfFinancialOwner.save();
						}
						
//						if(api.getState() != ApportionItem.UNCHANGED
//								|| !mMoneyDepositReturnContainerEditor.getModelCopy().getProjectId().equals(mMoneyDepositReturnContainerEditor.getModel().getProjectId())
//								|| !mMoneyDepositReturnContainerEditor.getModelCopy().getMoneyAccountId().equals(mMoneyDepositReturnContainerEditor.getModel().getMoneyAccountId())) {
							apportionEditor.save();
//						}
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
					
					// 维护缴款人的 ProjectShareAuthorization
					ProjectShareAuthorization psa = item.getProjectShareAuthorization();
					HyjModelEditor<ProjectShareAuthorization> psaEditor = psa.newModelEditor();
					psaEditor.getModelCopy().setActualTotalPayback(psa.getActualTotalPayback() - apportion.getAmount0()*mMoneyDepositReturnContainerEditor.getModel().getExchangeRate());
					psaEditor.save();
					
					List<MoneyReturn> moneyReturns = new Select().from(MoneyReturn.class).where("moneyDepositReturnApportionId=?", apportion.getId()).execute();
					for(MoneyReturn moneyReturn : moneyReturns){
						if(HyjApplication.getInstance().getCurrentUser().getId().equals(moneyReturn.getOwnerUserId())){
							MoneyAccount debtAccount;
							if(mMoneyDepositReturnContainerEditor.getModel().getFinancialOwnerUserId() != null){
								debtAccount = MoneyAccount.getDebtAccount(moneyReturn.getProject().getCurrencyId(), null, mMoneyDepositReturnContainerEditor.getModel().getFinancialOwnerUserId());
							} else {
								debtAccount = MoneyAccount.getDebtAccount(moneyReturn.getProject().getCurrencyId(), moneyReturn.getLocalFriendId(), moneyReturn.getFriendUserId());
							}
							HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
							debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() - moneyReturn.getProjectAmount());
							debtAccountEditor.save();
						}
						moneyReturn.delete();
					}
					
					List<MoneyPayback> moneyPaybacks = new Select().from(MoneyPayback.class).where("moneyDepositReturnApportionId=?", apportion.getId()).execute();
					for(MoneyPayback moneyPayback : moneyPaybacks){
						moneyPayback.delete();
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

				// 维护缴款人的 ProjectShareAuthorization
				ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=? AND state <> 'Delete'", mMoneyDepositReturnContainerEditor.getModelCopy().getProjectId(), apportion.getFriendUserId()).executeSingle();
				HyjModelEditor<ProjectShareAuthorization> psaEditor = psa.newModelEditor();
				psaEditor.getModelCopy().setActualTotalPayback(psa.getActualTotalPayback() + apportion.getAmount0()*mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
				psaEditor.save();
				
				MoneyReturn moneyReturn = null;
				moneyReturn = new MoneyReturn();
//				moneyReturn.setReturnType("Deposit");
				
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
				

				if(moneyReturn.getLocalFriendId() != null){
					MoneyPayback moneyPayback = null;
					if(apportion.get_mId() == null){
						moneyPayback = new MoneyPayback();
//						moneyPayback.setPaybackType("Deposit");
					} else {
						moneyPayback = new Select().from(MoneyPayback.class).where("moneyReturnId=? AND ownerFriendId=?", moneyReturn.getId(), moneyReturn.getLocalFriendId()).executeSingle();
					}
					moneyPayback.setMoneyReturnId(moneyReturn.getId());
					moneyPayback.setAmount(apportion.getAmount0());
					moneyPayback.setDate(mMoneyDepositReturnContainerEditor.getModelCopy().getDate());
					moneyPayback.setRemark(mMoneyDepositReturnContainerEditor.getModelCopy().getRemark());
					moneyPayback.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
					moneyPayback.setLocalFriendId(null);
					moneyPayback.setExchangeRate(mMoneyDepositReturnContainerEditor.getModelCopy().getExchangeRate());
					moneyPayback.setGeoLat(mMoneyDepositReturnContainerEditor.getModelCopy().getGeoLat());
					moneyPayback.setGeoLon(mMoneyDepositReturnContainerEditor.getModelCopy().getGeoLon());
					moneyPayback.setMoneyAccountId(null, moneyReturn.getCurrencyId1());
					moneyPayback.setLocation(mMoneyDepositReturnContainerEditor.getModelCopy().getLocation());
					moneyPayback.setAddress(mMoneyDepositReturnContainerEditor.getModelCopy().getAddress());
					moneyPayback.setPictureId(mMoneyDepositReturnContainerEditor.getModelCopy().getPictureId());
					moneyPayback.setProject(mMoneyDepositReturnContainerEditor.getModelCopy().getProject());
					moneyPayback.setOwnerUserId("");
					moneyPayback.setOwnerFriendId(apportion.getLocalFriendId());
					moneyPayback.save();
				}
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
	         		
	         		ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("projectId = ? AND friendUserId=? AND state <> 'Delete'", project.getId(), HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
					
					if(mMoneyDepositReturnContainerEditor.getModelCopy().get_mId() == null && !psa.getProjectShareMoneyExpenseAddNew()){
						HyjUtil.displayToast(R.string.app_permission_no_addnew);
						return;
					}else if(mMoneyDepositReturnContainerEditor.getModelCopy().get_mId() != null && !psa.getProjectShareMoneyExpenseEdit()){
						HyjUtil.displayToast(R.string.app_permission_no_edit);
						return;
					}

					if(project.getFinancialOwnerUserId() != null){
						mSelectorFieldFinancialOwner.setModelId(project.getFinancialOwnerUserId());
						mSelectorFieldFinancialOwner.setText(Friend.getFriendUserDisplayName(project.getFinancialOwnerUserId()));
					} else {
						mSelectorFieldFinancialOwner.setModelId(null);
						mSelectorFieldFinancialOwner.setText(null);
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
    					apportion.setFriendUserId(psa.getFriendUserId());
    					apportion.setLocalFriendId(psa.getLocalFriendId());
        				if(psa.getSharePercentageType() != null && psa.getSharePercentageType().equals("Average")){
        					apportion.setApportionType("Average");
        				} else {
        					apportion.setApportionType("Share");
        				}
    				} 
//    				else {
//    					Friend friend = Friend.load(Friend.class, _id);
//    					if(friend.getFriendUserId() != null){
//    						//看一下该好友是不是项目成员, 如果是，作为项目成员添加
//    						ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("friendUserId=? AND projectId=? AND state <> 'Delete'", friend.getFriendUserId(), mSelectorFieldProject.getModelId()).executeSingle();
//    						if(psa != null){
//								apportion.setFriendUserId(psa.getFriendUserId());
//			    				if(psa.getSharePercentageType() != null && psa.getSharePercentageType().equals("Average")){
//			    					apportion.setApportionType("Average");
//			    				} else {
//			    					apportion.setApportionType("Share");
//			    				}
//    						} else {
//    							apportion.setLocalFriendId(friend.getId());
//    							apportion.setApportionType("Average");
//    						}
//    					} else {
//    						apportion.setLocalFriendId(friend.getId());
//    						apportion.setApportionType("Average");
//    					}
//    				}
    				apportion.setAmount(0.0);
    				apportion.setMoneyDepositReturnContainerId(mMoneyDepositReturnContainerEditor.getModel().getId());
    				if (mApportionFieldApportions.addApportion(apportion,mSelectorFieldProject.getModelId(), ApportionItem.NEW)) {
    					mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
    				} else {
    					HyjUtil.displayToast(R.string.moneyApportionField_select_toast_apportion_user_already_exists);
    				}
    			}
     			break;
             case GET_FINANCIALOWNER_ID:
    	       	 if(resultCode == Activity.RESULT_OK){
    	       		long _id = data.getLongExtra("MODEL_ID", -1);
    	       		if(_id == -1){
    		       		mSelectorFieldFinancialOwner.setText(null);
    		       		mSelectorFieldFinancialOwner.setModelId(null);
    	       		} else {
	    	       		ProjectShareAuthorization psa = HyjModel.load(ProjectShareAuthorization.class, _id);
	
	    	       		if(psa == null){
	    					HyjUtil.displayToast(R.string.projectFormFragment_editText_error_financialOwner_must_be_member);
	    					return;
	    	       		} else if(psa.getFriendUserId() == null){
	    					HyjUtil.displayToast(R.string.projectFormFragment_editText_error_financialOwner_cannot_local);
	    					return;
	    	       		} else if(!psa.getState().equalsIgnoreCase("Accept")){
	    					HyjUtil.displayToast(R.string.projectFormFragment_editText_error_financialOwner_must_be_accepted_member);
	    					return;
	    	       		} else if(psa.getProjectShareMoneyExpenseOwnerDataOnly() == true){
	    					HyjUtil.displayToast(R.string.projectFormFragment_editText_error_financialOwner_must_has_all_auth);
	    					return;
	    	       		}
	    	       		
	    	       		mSelectorFieldFinancialOwner.setText(psa.getFriendDisplayName());
	    	       		mSelectorFieldFinancialOwner.setModelId(psa.getFriendUserId());
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

		@Override
		public void onDestroy(){
			super.onDestroy();
			mApportionFieldApportions.getAdapter().unregisterDataSetObserver(mApportionCountObserver);
		}
	}
