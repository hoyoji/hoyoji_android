package com.hoyoji.hoyoji.money;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

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
import com.hoyoji.android.hyjframework.activity.HyjUserActivity;
import com.hoyoji.android.hyjframework.activity.HyjActivity.DialogCallbackListener;
import com.hoyoji.android.hyjframework.fragment.HyjCalculatorFormFragment;
import com.hoyoji.android.hyjframework.fragment.HyjFragment;
import com.hoyoji.android.hyjframework.fragment.HyjTextInputFormFragment;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.fragment.HyjUserFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeField;
import com.hoyoji.android.hyjframework.view.HyjImageField;
import com.hoyoji.android.hyjframework.view.HyjImageField.PictureItem;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjRemarkField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.LoginActivity;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyApportion;
import com.hoyoji.hoyoji.models.MoneyBorrow;
import com.hoyoji.hoyoji.models.MoneyExpense;
import com.hoyoji.hoyoji.models.MoneyExpenseApportion;
import com.hoyoji.hoyoji.models.MoneyExpenseCategory;
import com.hoyoji.hoyoji.models.MoneyExpenseContainer;
import com.hoyoji.hoyoji.models.MoneyExpenseContainer.MoneyExpenseContainerEditor;
import com.hoyoji.hoyoji.models.MoneyLend;
import com.hoyoji.hoyoji.models.MoneyTemplate;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.UserData;
import com.hoyoji.hoyoji.money.MoneyApportionField.ApportionItem;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.money.moneycategory.MoneyExpenseCategoryListFragment;
import com.hoyoji.hoyoji.project.ExplainFinancialOwnerFragment;
import com.hoyoji.hoyoji.project.MemberFormFragment;
import com.hoyoji.hoyoji.project.MemberListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;

public class MoneyExpenseContainerFormFragment extends HyjUserFormFragment {
	private final static int GET_MONEYACCOUNT_ID = 1;
	private final static int GET_PROJECT_ID = 2;
	private final static int GET_FRIEND_ID = 3;
	private final static int GET_APPORTION_MEMBER_ID = 4;
	private final static int GET_CATEGORY_ID = 5;
	private final static int GET_REMARK = 6;
	private final static int GET_AMOUNT = 8;
	private static final int ADD_AS_PROJECT_MEMBER = 0;
	protected static final int GET_FINANCIALOWNER_ID = 7;
	
	private int CREATE_EXCHANGE = 0;
	private int SET_EXCHANGE_RATE_FLAG = 1;

	private MoneyExpenseContainerEditor mMoneyExpenseContainerEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private MoneyApportionField mApportionFieldApportions = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericAmount = null;
	private HyjSelectorField mSelectorFieldMoneyAccount = null;
	private HyjSelectorField mSelectorFieldProject = null;
	private HyjNumericField mNumericExchangeRate = null;
	private HyjSelectorField mSelectorFieldMoneyExpenseCategory = null;
	private HyjSelectorField mSelectorFieldFriend = null;
//	private ImageView mImageViewClearFriend = null;
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
	
	private ImageButton calculatorTextView = null;
	
	private Button app_action_save_template = null;

	@Override
	public Integer useContentView() {
		return R.layout.money_formfragment_moneyexpensecontainer;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		final MoneyExpenseContainer moneyExpenseContainer;
		JSONObject temPlateJso = null;

		Intent intent = getActivity().getIntent();
		final long modelId = intent.getLongExtra("MODEL_ID", -1);
		
		if (modelId != -1) {
			moneyExpenseContainer = new Select().from(MoneyExpenseContainer.class).where("_id=?", modelId).executeSingle();
			hasEditPermission = moneyExpenseContainer.hasEditPermission();
		} else {
			moneyExpenseContainer = new MoneyExpenseContainer();
			String temPlateData = intent.getStringExtra("MONEYTEMPLATE_DATA");
			if (temPlateData != null) {
				try {
					temPlateJso = new JSONObject(temPlateData);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				temPlateJso.remove("id");
				temPlateJso.remove("date");
				temPlateJso.remove("amount");
				moneyExpenseContainer.loadFromJSON(temPlateJso,false);
				
			} else {
				final String moneyAccountId = intent.getStringExtra("moneyAccountId");
				if(moneyAccountId != null){
					moneyExpenseContainer.setMoneyAccountId(moneyAccountId);
				}
				if(intent.getStringExtra("counterpartId") != null){
					moneyExpenseContainer.setIsImported(true);
				}
			}
		}
				
		mMoneyExpenseContainerEditor = new MoneyExpenseContainerEditor(moneyExpenseContainer);

		setupDeleteButton(mMoneyExpenseContainerEditor);

		mImageFieldPicture = (HyjImageField) getView().findViewById(R.id.moneyExpenseContainerFormFragment_imageField_picture);
		mImageFieldPicture.setImages(moneyExpenseContainer.getPictures());
				
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyExpenseContainerFormFragment_textField_date);
		if (modelId != -1) {
			mDateTimeFieldDate.setText(moneyExpenseContainer.getDate());
		} else {
			long dateInMillisec = intent.getLongExtra("DATE_IN_MILLISEC", -1);
			if(dateInMillisec != -1){
				Date date = new Date(dateInMillisec);
				mDateTimeFieldDate.setDate(date);
				mDateTimeFieldDate.setTextColor(Color.RED);
			}
		}
		
		Project project;
		String projectId = intent.getStringExtra("projectId");//从消息导入
		if(moneyExpenseContainer.get_mId() == null && projectId != null){
			moneyExpenseContainer.setProjectId(projectId);
			project = HyjModel.getModel(Project.class, projectId);
		}else{
			project = moneyExpenseContainer.getProject();
		}
		
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(R.id.moneyExpenseContainerFormFragment_selectorField_project);

		if (project != null) {
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getDisplayName() + "("+ project.getCurrencyId() + ")");
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneyExpenseContainerFormFragment.this.openActivityWithFragmentForResult(
								ProjectListFragment.class,
								R.string.projectListFragment_title_select_project,
								null, GET_PROJECT_ID);
			}
		});
		
		setupApportionField(moneyExpenseContainer);
		
		mNumericAmount = (HyjNumericField) getView().findViewById(R.id.moneyExpenseContainerFormFragment_textField_amount);
		mNumericAmount.getEditText().setTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
		mNumericAmount.getEditText().setHintTextColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
		if (modelId == -1) {
			double amount = 0.0;
			double exchangeRate = 1.0;
			if(temPlateJso != null){
				amount = temPlateJso.optDouble("amount", 0.0);
				amount = temPlateJso.optDouble("exchangeRate", 1.0);
			} else {
				amount = intent.getDoubleExtra("amount", 0.0);//从分享消息导入的金额
				exchangeRate = intent.getDoubleExtra("exchangeRate", 1.0);
			}
			mNumericAmount.setNumber(amount*exchangeRate);
			mApportionFieldApportions.setTotalAmount(amount*exchangeRate);
		} else {
			mNumericAmount.setNumber(moneyExpenseContainer.getAmount());
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
		
		MoneyAccount moneyAccount = moneyExpenseContainer.getMoneyAccount();
		mSelectorFieldMoneyAccount = (HyjSelectorField) getView().findViewById(R.id.moneyExpenseContainerFormFragment_selectorField_moneyAccount);

		if (moneyAccount != null) {
			mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
			mSelectorFieldMoneyAccount.setText(moneyAccount.getName() + "(" + moneyAccount.getCurrencyId() + ")");
		}
		
		
		mSelectorFieldMoneyAccount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("excludeType", "Debt");
				
				MoneyExpenseContainerFormFragment.this
						.openActivityWithFragmentForResult(
								MoneyAccountListFragment.class,
								R.string.moneyAccountListFragment_title_select_moneyAccount,
								bundle, GET_MONEYACCOUNT_ID);
			}
		});


		mNumericExchangeRate = (HyjNumericField) getView().findViewById(R.id.moneyExpenseContainerFormFragment_textField_exchangeRate);
		mNumericExchangeRate.setNumber(moneyExpenseContainer.getExchangeRate());

		mViewSeparatorExchange = (View) getView().findViewById(R.id.moneyExpenseContainerFormFragment_separatorField_exchange);
		mLinearLayoutExchangeRate = (LinearLayout) getView().findViewById(R.id.moneyExpenseContainerFormFragment_linearLayout_exchangeRate);

		mSelectorFieldMoneyExpenseCategory = (HyjSelectorField) getView().findViewById(R.id.moneyExpenseContainerFormFragment_textField_moneyExpenseCategory);
		mSelectorFieldMoneyExpenseCategory.setText(moneyExpenseContainer.getMoneyExpenseCategory());
		if(moneyExpenseContainer.getMoneyExpenseCategoryMain() != null && moneyExpenseContainer.getMoneyExpenseCategoryMain().length() > 0){
			mSelectorFieldMoneyExpenseCategory.setLabel(moneyExpenseContainer.getMoneyExpenseCategoryMain());
		}
		mSelectorFieldMoneyExpenseCategory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneyExpenseContainerFormFragment.this
						.openActivityWithFragmentForResult(
								MoneyExpenseCategoryListFragment.class,
								R.string.moneyExpenseFormFragment_editText_hint_moneyExpenseCategory,
								null, GET_CATEGORY_ID);
			}
		});

		String friendUserId, localFriendId;
		if(moneyExpenseContainer.get_mId() == null){
			friendUserId = intent.getStringExtra("friendUserId");//从消息导入
			localFriendId = intent.getStringExtra("localFriendId");//从消息导入
		}else{
			friendUserId = moneyExpenseContainer.getFriendUserId();
			localFriendId = moneyExpenseContainer.getLocalFriendId();
		}
		mSelectorFieldFriend = (HyjSelectorField) getView().findViewById(R.id.moneyExpenseContainerFormFragment_selectorField_friend);

		mSelectorFieldFriend.setText(Friend.getFriendUserDisplayName(localFriendId, friendUserId, projectId));
		if (friendUserId != null) {
			mSelectorFieldFriend.setModelId(friendUserId);
		} else {
			mSelectorFieldFriend.setModelId(localFriendId);
		}
		
		mSelectorFieldFriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("NULL_ITEM", (String) mSelectorFieldFriend.getHint());
				MoneyExpenseContainerFormFragment.this
						.openActivityWithFragmentForResult(
								FriendListFragment.class,
								R.string.friendListFragment_title_select_friend_payee,
								bundle, GET_FRIEND_ID);
			}
		});
		
//		mImageViewClearFriend = (ImageView) getView().findViewById(
//				R.id.moneyExpenseContainerFormFragment_imageView_clear_friend);
//		mImageViewClearFriend.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				mSelectorFieldFriend.setModelId(null);
//				mSelectorFieldFriend.setText("");
//			}
//		});
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyExpenseContainerFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyExpenseContainer.getRemark());
		mRemarkFieldRemark.setEditable(false);
		mRemarkFieldRemark.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("TEXT", mRemarkFieldRemark.getText());
				bundle.putString("HINT", "请输入" + mRemarkFieldRemark.getLabelText());
				MoneyExpenseContainerFormFragment.this
						.openActivityWithFragmentForResult(
								HyjTextInputFormFragment.class,
								R.string.moneyExpenseFormFragment_textView_remark,
								bundle, GET_REMARK);
			}
		});
		
		calculatorTextView = (ImageButton) getView().findViewById(R.id.calculator);
		calculatorTextView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putDouble("AMOUNT", mNumericAmount.getNumber()!=null?mNumericAmount.getNumber():0.00);
				MoneyExpenseContainerFormFragment.this.openActivityWithFragmentForResult(
								HyjCalculatorFormFragment.class,
								R.string.hyjCalculatorFormFragment_title,
								bundle, GET_AMOUNT);
			}
		});
		
		app_action_save_template = (Button) getView().findViewById(R.id.button_save_template);
		app_action_save_template.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				fillData();
				if(validate()){
					MoneyTemplate moneyTemplate = new MoneyTemplate();
					moneyTemplate.setDate(mMoneyExpenseContainerEditor.getModelCopy().getDate());
					moneyTemplate.setType("MoneyExpense");
					moneyTemplate.setData(mMoneyExpenseContainerEditor.getModelCopy().toJSON().toString());
					moneyTemplate.save();
					HyjUtil.displayToast(R.string.app_save_template_success);
				}
			}
		});

		mSelectorFieldFinancialOwner = (HyjSelectorField) getView().findViewById(R.id.projectFormFragment_selectorField_financialOwner);
		mSelectorFieldFinancialOwner.setEnabled(hasEditPermission);
//		mSelectorFieldFinancialOwner.showHelpButton().setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				MoneyExpenseContainerFormFragment.this.openActivityWithFragment(ExplainFinancialOwnerFragment.class, R.string.explainFinancialOwnerFragment_title, null);
//			}
//		});
		if(modelId == -1){
			if(project.getFinancialOwnerUserId() != null){
				mSelectorFieldFinancialOwner.setModelId(project.getFinancialOwnerUserId());
				mSelectorFieldFinancialOwner.setText(Friend.getFriendUserDisplayName(project.getFinancialOwnerUserId()));
			}
		} else if(moneyExpenseContainer.getFinancialOwnerUserId() != null){
				mSelectorFieldFinancialOwner.setModelId(moneyExpenseContainer.getFinancialOwnerUserId());
				mSelectorFieldFinancialOwner.setText(Friend.getFriendUserDisplayName(moneyExpenseContainer.getFinancialOwnerUserId()));
		}
		
		mSelectorFieldFinancialOwner.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mSelectorFieldProject.getModelId() == null){
					HyjUtil.displayToast("请先选择一个账本。");
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
				MoneyExpenseContainerFormFragment.this.openActivityWithFragment(ExplainFinancialOwnerFragment.class, R.string.explainFinancialOwnerFragment_title, null);
			}
		});
		
		ImageView takePictureButton = (ImageView) getView().findViewById(R.id.moneyExpenseContainerFormFragment_imageView_camera);
		takePictureButton.setBackgroundColor(Color.parseColor(HyjApplication.getInstance().getCurrentUser().getUserData().getExpenseColor()));
		takePictureButton.setOnClickListener(new OnClickListener() {
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

		mImageViewRefreshRate = (ImageView) getView().findViewById(R.id.moneyExpenseContainerFormFragment_imageButton_refresh_exchangeRate);
		mImageViewRefreshRate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mSelectorFieldMoneyAccount.getModelId() != null && mSelectorFieldProject.getModelId() != null) {
					MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mSelectorFieldMoneyAccount.getModelId());
					Project project = HyjModel.getModel(Project.class, mSelectorFieldProject.getModelId());

					String fromCurrency = moneyAccount.getCurrencyId();
					String toCurrency = project.getCurrencyId();
					if (fromCurrency != null && toCurrency != null) {
						HyjUtil.startRoateView(mImageViewRefreshRate);
						mImageViewRefreshRate.setEnabled(false);
						HyjUtil.updateExchangeRate(fromCurrency, toCurrency, mImageViewRefreshRate, mNumericExchangeRate);
					} else {
						HyjUtil.displayToast(R.string.moneyExpenseFormFragment_toast_select_currency);
					}
				} else {
					HyjUtil.displayToast(R.string.moneyExpenseFormFragment_toast_select_currency);
				}
			}
		});

		mLinearLayoutExpandMore = (LinearLayout)getView().findViewById(R.id.moneyExpenseContainerFormFragment_expandMore);
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
		
		getView().findViewById(R.id.moneyExpenseContainerFormFragment_imageButton_apportion_add).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Bundle bundle = new Bundle();
						Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
						bundle.putLong("MODEL_ID", project.get_mId());
						openActivityWithFragmentForResult(SelectApportionMemberListFragment.class, R.string.moneyApportionField_select_apportion_member, bundle, GET_APPORTION_MEMBER_ID);
					}
				});

		getView().findViewById(R.id.moneyExpenseContainerFormFragment_imageButton_apportion_add_all).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addAllProjectMemberIntoApportionsField(moneyExpenseContainer);
			}
		});
		
		getView().findViewById(R.id.moneyExpenseContainerFormFragment_imageButton_apportion_more_actions).setOnClickListener(new OnClickListener() {
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

		setPermission();
		
		// 只在新增时才自动打开软键盘， 修改时不自动打开
		if (modelId == -1) {
			setExchangeRate(false);
			this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}else{
			if(!moneyExpenseContainer.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())) {
				app_action_save_template.setVisibility(View.GONE);
			}
			setExchangeRate(true);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    super.onCreateOptionsMenu(menu, inflater);
	    if(mMoneyExpenseContainerEditor!= null && mMoneyExpenseContainerEditor.getModelCopy().get_mId() != null && !hasEditPermission){
	    	hideSaveAction();
	    }
	}
	
	private void addAllProjectMemberIntoApportionsField(MoneyExpenseContainer moneyExpenseContainer) {
		Project project = HyjModel.getModel(Project.class,mSelectorFieldProject.getModelId());
		List<ProjectShareAuthorization> projectShareAuthorizations = project.getShareAuthorizations();
		for (int i = 0; i < projectShareAuthorizations.size(); i++) {
			if(projectShareAuthorizations.get(i).getState().equalsIgnoreCase("Delete")){
				continue;
			}
			MoneyExpenseApportion apportion = new MoneyExpenseApportion();
			apportion.setAmount(0.0);
			apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
			apportion.setLocalFriendId(projectShareAuthorizations.get(i).getLocalFriendId());
			apportion.setMoneyExpenseContainerId(moneyExpenseContainer.getId());
			if(projectShareAuthorizations.get(i).getSharePercentageType() != null && projectShareAuthorizations.get(i).getSharePercentageType().equals("Average")){
				apportion.setApportionType("Average");
			} else {
				apportion.setApportionType("Share");
			}
			mApportionFieldApportions.addApportion(apportion, project.getId(), ApportionItem.NEW);
		}
		mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
		
	}
	
	private void setupApportionField(MoneyExpenseContainer moneyExpenseContainer) {

		mApportionFieldApportions = (MoneyApportionField) getView().findViewById(R.id.moneyExpenseContainerFormFragment_apportionField);
		mTextViewApportionFieldTitle = (TextView) getView().findViewById(R.id.moneyExpenseContainerFormFragment_apportionField_title);
		mApportionCountObserver = new DataSetObserver(){
	        @Override
	        public void onChanged() {
	    		mTextViewApportionFieldTitle.setText(getString(R.string.moneyApportionField_title)+"("+mApportionFieldApportions.getApportionCount()+")");
	        }
		};
		mApportionFieldApportions.getAdapter().registerDataSetObserver(mApportionCountObserver);
		
		List<MoneyExpenseApportion> moneyApportions = null;
		
		if(mMoneyExpenseContainerEditor.getModelCopy().get_mId() == null) {
			
			moneyApportions = new ArrayList<MoneyExpenseApportion>();
			if(moneyExpenseContainer.getProject() != null && moneyExpenseContainer.getProject().getAutoApportion() && !moneyExpenseContainer.getIsImported()){
				List<ProjectShareAuthorization> projectShareAuthorizations = moneyExpenseContainer.getProject().getShareAuthorizations();
				for(int i=0; i < projectShareAuthorizations.size(); i++){
					MoneyExpenseApportion apportion = new MoneyExpenseApportion();
					apportion.setAmount(0.0);
					apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
					apportion.setLocalFriendId(projectShareAuthorizations.get(i).getLocalFriendId());
					apportion.setMoneyExpenseContainerId(moneyExpenseContainer.getId());
					if(projectShareAuthorizations.get(i).getSharePercentageType() != null && projectShareAuthorizations.get(i).getSharePercentageType().equals("Average")){
						apportion.setApportionType("Average");
					} else {
						apportion.setApportionType("Share");
					}
					moneyApportions.add(apportion);
				}
			} else if(moneyExpenseContainer.getProject() != null) {
				MoneyExpenseApportion apportion = new MoneyExpenseApportion();
				apportion.setAmount(moneyExpenseContainer.getAmount0());
				apportion.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
				apportion.setMoneyExpenseContainerId(moneyExpenseContainer.getId());
				ProjectShareAuthorization projectShareAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", moneyExpenseContainer.getProjectId(), apportion.getFriendUserId()).executeSingle();
				if(projectShareAuthorization.getSharePercentageType() != null && projectShareAuthorization.getSharePercentageType().equals("Average")){
					apportion.setApportionType("Average");
				} else {
					apportion.setApportionType("Share");
				}
				moneyApportions.add(apportion);
			}
			
		} else {
			moneyApportions = moneyExpenseContainer.getApportions();
		}
		
		mApportionFieldApportions.init(moneyExpenseContainer.getAmount0(), moneyApportions, moneyExpenseContainer.getProjectId(), moneyExpenseContainer.getId());
	}

	private void setupDeleteButton(MoneyExpenseContainerEditor moneyExpenseContainerEditor) {

		Button buttonDelete = (Button) getView().findViewById(R.id.button_delete);
		
		final MoneyExpenseContainer moneyExpenseContainer = moneyExpenseContainerEditor.getModelCopy();
		
		if (moneyExpenseContainer.get_mId() == null) {
			buttonDelete.setVisibility(View.GONE);
		} else {
			buttonDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(moneyExpenseContainer.hasDeletePermission()){
					((HyjActivity)getActivity()).displayDialog(R.string.app_action_delete_list_item, R.string.app_confirm_delete, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
							new DialogCallbackListener() {
								@Override
								public void doPositiveClick(Object object) {
									try {
										ActiveAndroid.beginTransaction();

										MoneyAccount moneyAccount = moneyExpenseContainer.getMoneyAccount();
										HyjModelEditor<MoneyAccount> moneyAccountEditor = moneyAccount.newModelEditor();
										moneyAccountEditor.getModelCopy().setCurrentBalance(moneyAccount.getCurrentBalance() + moneyExpenseContainer.getAmount());
										MoneyExpenseContainerEditor moneyExpenseContainerEditor = new MoneyExpenseContainerEditor(moneyExpenseContainer);
										
										//更新账本余额
										Project newProject = moneyExpenseContainer.getProject();
										HyjModelEditor<Project> newProjectEditor = newProject.newModelEditor();
										newProjectEditor.getModelCopy().setExpenseTotal(newProject.getExpenseTotal() - moneyExpenseContainer.getAmount0()*moneyExpenseContainer.getExchangeRate());
										newProjectEditor.save();
										
										//删除支出的同时删除分摊
										Iterator<MoneyExpenseApportion> moneyExpenseApportions = moneyExpenseContainer.getApportions().iterator();
										while (moneyExpenseApportions.hasNext()) {
											MoneyExpenseApportion moneyExpenseApportion = moneyExpenseApportions.next();
											MoneyExpenseContainer.deleteApportion(moneyExpenseApportion, moneyExpenseContainerEditor);
//											ProjectShareAuthorization oldProjectShareAuthorization;
//
//											if(HyjApplication.getInstance().getCurrentUser().getId().equals(moneyExpenseApportion.getFriendUserId())){
//												// 更新旧账本的分摊支出
//												oldProjectShareAuthorization = moneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
//												HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
//												oldProjectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getApportionedTotalExpense() - (moneyExpenseApportion.getAmount0() * moneyExpenseApportion.getMoneyExpenseContainer().getExchangeRate()));
//												oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalExpense() - (moneyExpenseApportion.getAmount0() * moneyExpenseApportion.getMoneyExpenseContainer().getExchangeRate()));
//												oldProjectShareAuthorizationEditor.save();
//												
//												MoneyExpense moneyExpense = new Select().from(MoneyExpense.class).where("moneyExpenseApportionId=?", moneyExpenseApportion.getId()).executeSingle();
//												if(moneyExpense != null){
//													moneyExpense.delete();
//												}
//											} else {
//												// 更新旧账本分摊支出
//												oldProjectShareAuthorization = moneyExpenseApportion.getProjectShareAuthorization();
//												HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
//												
//												oldProjectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getApportionedTotalExpense() - (moneyExpenseApportion.getAmount0() * moneyExpenseApportion.getMoneyExpenseContainer().getExchangeRate()));
//												oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalExpense() - (moneyExpenseApportion.getAmount0() * moneyExpenseApportion.getMoneyExpenseContainer().getExchangeRate()));
//												MoneyExpense moneyExpense = new Select().from(MoneyExpense.class).where("moneyExpenseApportionId=?", moneyExpenseApportion.getId()).executeSingle();
//												if(moneyExpense != null){
//													moneyExpense.delete();
//												} 
//											
//												oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalBorrow(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalBorrow() - (moneyExpenseApportion.getAmount0() * moneyExpenseApportion.getMoneyExpenseContainer().getExchangeRate()));
//												List<MoneyBorrow> moneyBorrows = new Select().from(MoneyBorrow.class).where("moneyExpenseApportionId=?", moneyExpenseApportion.getId()).execute();
//												for(MoneyBorrow moneyBorrow : moneyBorrows){
//													moneyBorrow.delete();
//												} 
//												oldProjectShareAuthorizationEditor.save();
//												
//												oldProjectShareAuthorization = moneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
//												oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
//												oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalLend(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalLend() - (moneyExpenseApportion.getAmount0() * moneyExpenseApportion.getMoneyExpenseContainer().getExchangeRate()));
//												oldProjectShareAuthorizationEditor.save();
//												List<MoneyLend> moneyLends = new Select().from(MoneyLend.class).where("moneyExpenseApportionId=?", moneyExpenseApportion.getId()).execute();
//												for(MoneyLend moneyLend : moneyLends){
//													moneyLend.delete();
//												}
//											}
//																					
//											moneyExpenseApportion.delete();
										}
										
										moneyExpenseContainer.delete();
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

		if(mMoneyExpenseContainerEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			mDateTimeFieldDate.setEnabled(false);
			
//			mNumericAmount.setNumber(mMoneyExpenseContainerEditor.getModel().getProjectAmount());
			mNumericAmount.setEnabled(false);
			
			mSelectorFieldMoneyExpenseCategory.setEnabled(false);
			mSelectorFieldFriend.setEnabled(false);
			
			mSelectorFieldMoneyAccount.setEnabled(false);
			mSelectorFieldMoneyAccount.setVisibility(View.GONE);
			getView().findViewById(R.id.moneyExpenseContainerFormFragment_separatorField_moneyAccount).setVisibility(View.GONE);

			mSelectorFieldProject.setEnabled(false);
			
			mNumericExchangeRate.setEnabled(false);
			
			mApportionFieldApportions.setEnabled(false);

			mRemarkFieldRemark.setEnabled(false);

			if(this.mOptionsMenu != null) {
		    	hideSaveAction();
			}
			
			getView().findViewById(R.id.moneyExpenseContainerFormFragment_imageButton_apportion_add).setEnabled(false);
			getView().findViewById(R.id.moneyExpenseContainerFormFragment_imageButton_apportion_add_all).setEnabled(false);
//			getView().findViewById(R.id.button_save).setEnabled(false);	
			getView().findViewById(R.id.button_delete).setEnabled(false);	
			getView().findViewById(R.id.button_delete).setVisibility(View.GONE);
		}
	}

	private void setExchangeRate(Boolean editInit) {
		if (mSelectorFieldMoneyAccount.getModelId() != null
				&& mSelectorFieldProject.getModelId() != null) {
			MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class,
					mSelectorFieldMoneyAccount.getModelId());
			Project project = HyjModel.getModel(Project.class,
					mSelectorFieldProject.getModelId());

			String fromCurrency = moneyAccount.getCurrencyId();
			String toCurrency = project.getCurrencyId();

			if (fromCurrency.equals(toCurrency)) {
				if (SET_EXCHANGE_RATE_FLAG != 1) {// 新增或修改打开时不做setNumber
					mNumericExchangeRate.setNumber(1.00);
					CREATE_EXCHANGE = 0;
				}
				mViewSeparatorExchange.setVisibility(View.GONE);
				mLinearLayoutExchangeRate.setVisibility(View.GONE);
			} else {
				mViewSeparatorExchange.setVisibility(View.VISIBLE);
				mLinearLayoutExchangeRate.setVisibility(View.VISIBLE);

				if(!editInit){//修改时init不需要set Rate
					Double rate = Exchange.getExchangeRate(fromCurrency,
							toCurrency);
					if (rate != null) {
						mNumericExchangeRate.setNumber(rate);
						CREATE_EXCHANGE = 0;
					} else {
						mNumericExchangeRate.setNumber(null);
						CREATE_EXCHANGE = 1;
					}
				}
			}

		} else {
			mViewSeparatorExchange.setVisibility(View.GONE);
			mLinearLayoutExchangeRate.setVisibility(View.GONE);
		}
		SET_EXCHANGE_RATE_FLAG = 0;
	}

	private void fillData() {
		MoneyExpenseContainer modelCopy = (MoneyExpenseContainer) mMoneyExpenseContainerEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setAmount(mNumericAmount.getNumber());
		modelCopy.setMoneyAccountId(mSelectorFieldMoneyAccount.getModelId());
		modelCopy.setProject(HyjModel.getModel(Project.class, mSelectorFieldProject.getModelId()));
		modelCopy.setExchangeRate(mNumericExchangeRate.getNumber());
		modelCopy.setMoneyExpenseCategory(mSelectorFieldMoneyExpenseCategory.getText());
		modelCopy.setMoneyExpenseCategoryMain(mSelectorFieldMoneyExpenseCategory.getLabel());
		modelCopy.setFinancialOwnerUserId(mSelectorFieldFinancialOwner.getModelId());
		
		if(mSelectorFieldFriend.getModelId() != null){
			Friend friend = HyjModel.getModel(Friend.class, mSelectorFieldFriend.getModelId());
			modelCopy.setFriend(friend);
		}else{
			modelCopy.setFriend(null);
		}

		modelCopy.setRemark(mRemarkFieldRemark.getText().toString().trim());
	}

	private void showValidatioErrors() {
		HyjUtil.displayToast(R.string.app_validation_error);
		mDateTimeFieldDate.setError(mMoneyExpenseContainerEditor
				.getValidationError("datetime"));
		mNumericAmount.setError(mMoneyExpenseContainerEditor
				.getValidationError("amount"));
		if(mMoneyExpenseContainerEditor
		.getValidationError("amount") != null){
			mNumericAmount.showSoftKeyboard();
		}
		mSelectorFieldMoneyAccount.setError(mMoneyExpenseContainerEditor
				.getValidationError("moneyAccount"));
		mSelectorFieldProject.setError(mMoneyExpenseContainerEditor
				.getValidationError("project"));
		mNumericExchangeRate.setError(mMoneyExpenseContainerEditor
				.getValidationError("exchangeRate"));
		mSelectorFieldMoneyExpenseCategory.setError(mMoneyExpenseContainerEditor
				.getValidationError("moneyExpenseCategory"));
		mSelectorFieldFriend.setError(mMoneyExpenseContainerEditor
				.getValidationError("friend"));
		mRemarkFieldRemark.setError(mMoneyExpenseContainerEditor
				.getValidationError("remark"));
		mApportionFieldApportions.setError(mMoneyExpenseContainerEditor
				.getValidationError("apportionTotalAmount"));
	}

	private boolean validate(){
		if(mMoneyExpenseContainerEditor.getModelCopy().get_mId() == null 
				&& !mMoneyExpenseContainerEditor.getModelCopy().hasAddNewPermission(mMoneyExpenseContainerEditor.getModelCopy().getProjectId())){
			HyjUtil.displayToast(R.string.app_permission_no_addnew);
		}else if(mMoneyExpenseContainerEditor.getModelCopy().get_mId() != null && !hasEditPermission){
			HyjUtil.displayToast(R.string.app_permission_no_edit);
		}else{
			mMoneyExpenseContainerEditor.validate();
			
			if (mApportionFieldApportions.getCount() > 0) {
				if (mNumericAmount.getNumber() != null && !mNumericAmount.getNumber().equals(
						mApportionFieldApportions.getTotalAmount())) {
					mMoneyExpenseContainerEditor.setValidationError("apportionTotalAmount",R.string.moneyApportionField_select_toast_apportion_totalAmount_not_equal);
				} else {
					mMoneyExpenseContainerEditor.removeValidationError("apportionTotalAmount");
				}
			} else {
				mMoneyExpenseContainerEditor.removeValidationError("apportionTotalAmount");
			}

			if (mMoneyExpenseContainerEditor.hasValidationErrors()) {
				showValidatioErrors();
			} else {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void onSave(View v) {
		super.onSave(v);

		fillData();
		if(!validate()){
			return;
		}
			try {
				ActiveAndroid.beginTransaction();

				savePictures();

				saveApportions();

				MoneyExpenseContainer oldMoneyExpenseContainerModel = mMoneyExpenseContainerEditor.getModel();
				MoneyExpenseContainer moneyExpenseContainerModel = mMoneyExpenseContainerEditor.getModelCopy();
				
				//设置默认账本和账户
				UserData userData = HyjApplication.getInstance().getCurrentUser().getUserData();
				if(moneyExpenseContainerModel.get_mId() == null 
						&& !userData.getActiveMoneyAccountId().equals(moneyExpenseContainerModel.getMoneyAccountId()) 
						|| !userData.getActiveProjectId().equals(moneyExpenseContainerModel.getProjectId())){
					HyjModelEditor<UserData> userDataEditor = userData.newModelEditor();
					userDataEditor.getModelCopy().setActiveMoneyAccountId(moneyExpenseContainerModel.getMoneyAccountId());
					userDataEditor.getModelCopy().setActiveProjectId(moneyExpenseContainerModel.getProjectId());
					userDataEditor.save();
				}
				
				// 更新账本的默认分类
				if(moneyExpenseContainerModel.get_mId() == null){
					HyjModelEditor<Project> projectEditor = moneyExpenseContainerModel.getProject().newModelEditor();
					projectEditor.getModelCopy().setDefaultExpenseCategory(moneyExpenseContainerModel.getMoneyExpenseCategory());
					projectEditor.getModelCopy().setDefaultExpenseCategoryMain(moneyExpenseContainerModel.getMoneyExpenseCategoryMain());
					projectEditor.save();
				}
				
				//当前汇率不存在时，创建汇率
				String localCurrencyId = moneyExpenseContainerModel.getMoneyAccount().getCurrencyId();
				String foreignCurrencyId = moneyExpenseContainerModel.getProject().getCurrencyId();
				if(CREATE_EXCHANGE == 1){
					MoneyAccount moneyAccount = moneyExpenseContainerModel.getMoneyAccount();
					Project project = moneyExpenseContainerModel.getProject();
					
					Exchange newExchange = new Exchange();
					newExchange.setLocalCurrencyId(moneyAccount.getCurrencyId());
					newExchange.setForeignCurrencyId(project.getCurrencyId());
					newExchange.setRate(moneyExpenseContainerModel.getExchangeRate());
					newExchange.save();
				}else {
					if(!localCurrencyId.equalsIgnoreCase(foreignCurrencyId)){
						Exchange exchange = null;
						Double exRate = null;
						Double rate = HyjUtil.toFixed2(moneyExpenseContainerModel.getExchangeRate());
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
				
			    MoneyAccount oldMoneyAccount = oldMoneyExpenseContainerModel.getMoneyAccount();
				MoneyAccount newMoneyAccount = moneyExpenseContainerModel.getMoneyAccount();
				HyjModelEditor<MoneyAccount> newMoneyAccountEditor = newMoneyAccount.newModelEditor();
				
				//更新账户余额
				if(moneyExpenseContainerModel.get_mId() == null || oldMoneyAccount.getId().equals(newMoneyAccount.getId())){
					newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() + oldMoneyExpenseContainerModel.getAmount0() - moneyExpenseContainerModel.getAmount0());
				} else {
					HyjModelEditor<MoneyAccount> oldMoneyAccountEditor = oldMoneyAccount.newModelEditor();
					oldMoneyAccountEditor.getModelCopy().setCurrentBalance(oldMoneyAccount.getCurrentBalance() + oldMoneyExpenseContainerModel.getAmount0());
					newMoneyAccountEditor.getModelCopy().setCurrentBalance(newMoneyAccount.getCurrentBalance() - moneyExpenseContainerModel.getAmount0());
					oldMoneyAccountEditor.save();
				}
				newMoneyAccountEditor.save();
			
			    Project oldProject = oldMoneyExpenseContainerModel.getProject();
				Project newProject = moneyExpenseContainerModel.getProject();
				HyjModelEditor<Project> newProjectEditor = newProject.newModelEditor();
				
				//更新账本余额
				if(moneyExpenseContainerModel.get_mId() == null || oldProject.getId().equals(newProject.getId())){
					newProjectEditor.getModelCopy().setExpenseTotal(newProject.getExpenseTotal() - oldMoneyExpenseContainerModel.getAmount0()*oldMoneyExpenseContainerModel.getExchangeRate() + moneyExpenseContainerModel.getAmount0()*moneyExpenseContainerModel.getExchangeRate());
				} else {
					HyjModelEditor<Project> oldProjectEditor = oldProject.newModelEditor();
					oldProjectEditor.getModelCopy().setExpenseTotal(oldProject.getExpenseTotal() - oldMoneyExpenseContainerModel.getAmount0()*oldMoneyExpenseContainerModel.getExchangeRate());
					newProjectEditor.getModelCopy().setExpenseTotal(newProject.getExpenseTotal() + moneyExpenseContainerModel.getAmount0()*moneyExpenseContainerModel.getExchangeRate());
					oldProjectEditor.save();
				}
				newProjectEditor.save();
				
				/*
				//更新支出所有者的实际支出
				ProjectShareAuthorization selfProjectAuthorization = mMoneyExpenseContainerEditor.getNewSelfProjectShareAuthorization();
				HyjModelEditor<ProjectShareAuthorization> selfProjectAuthorizationEditor = selfProjectAuthorization.newModelEditor();
			    
				if(moneyExpenseContainerModel.get_mId() == null || oldMoneyExpenseContainerModel.getProjectId().equals(moneyExpenseContainerModel.getProjectId())){
				    // 无旧账本可更新
					selfProjectAuthorizationEditor.getModelCopy().setActualTotalExpense(selfProjectAuthorization.getActualTotalExpense() - oldMoneyExpenseContainerModel.getAmount0()*oldMoneyExpenseContainerModel.getExchangeRate() + moneyExpenseContainerModel.getAmount0()*moneyExpenseContainerModel.getExchangeRate());
					
				} else {
					selfProjectAuthorizationEditor.getModelCopy().setActualTotalExpense(selfProjectAuthorization.getActualTotalExpense() + moneyExpenseContainerModel.getAmount0()*moneyExpenseContainerModel.getExchangeRate());
						
					ProjectShareAuthorization oldSelfProjectAuthorization = mMoneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
					HyjModelEditor<ProjectShareAuthorization> oldSelfProjectAuthorizationEditor = oldSelfProjectAuthorization.newModelEditor();
					oldSelfProjectAuthorizationEditor.getModelCopy().setActualTotalExpense(oldSelfProjectAuthorization.getActualTotalExpense() - oldMoneyExpenseContainerModel.getAmount0()*oldMoneyExpenseContainerModel.getExchangeRate());
					oldSelfProjectAuthorizationEditor.save();
				
				}
				selfProjectAuthorizationEditor.save();
				*/
				
				//更新分类，使之成为最近使用过的
				if(this.mSelectorFieldMoneyExpenseCategory.getModelId() != null){
					MoneyExpenseCategory category = HyjModel.getModel(MoneyExpenseCategory.class, this.mSelectorFieldMoneyExpenseCategory.getModelId());
					if(category != null){
						category.newModelEditor().save();
					}
				}
				
				
				
				mMoneyExpenseContainerEditor.save();
				
				Intent intent = getActivity().getIntent();
				String temPlateID = intent .getStringExtra("MONEYTEMPLATE_ID");
				if(temPlateID != null){
					MoneyTemplate moneyTemplate = HyjModel.getModel(MoneyTemplate.class, temPlateID);
					moneyTemplate.setDate(HyjUtil.formatDateToIOS(new Date()));
					moneyTemplate.save();
				}
				
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

	private void savePictures() {
		HyjImageField.ImageGridAdapter adapter = mImageFieldPicture.getAdapter();
		int count = adapter.getCount();
		boolean mainPicSet = false;
		for (int i = 0; i < count; i++) {
			PictureItem pi = adapter.getItem(i);
			if (pi.getState() == PictureItem.NEW) {
				Picture newPic = pi.getPicture();
				newPic.setRecordId(mMoneyExpenseContainerEditor.getModel().getId());
				newPic.setRecordType("MoneyExpenseContainer");
				newPic.setDisplayOrder(i);
				newPic.save();
			} else if (pi.getState() == PictureItem.DELETED) {
				pi.getPicture().delete();
			} else if (pi.getState() == PictureItem.CHANGED) {

			}
			if (!mainPicSet && pi.getPicture() != null && pi.getState() != PictureItem.DELETED) {
				mainPicSet = true;
				mMoneyExpenseContainerEditor.getModelCopy().setPicture(pi.getPicture());
			}
		}
	}

	private void saveApportions() {
		MoneyApportionField.ImageGridAdapter adapter = mApportionFieldApportions.getAdapter();
		int count = adapter.getCount();
		int savedCount = 0;
		List<ApportionItem> apportionItems = new ArrayList<ApportionItem>();
		for (int i = 0; i < count; i++) {
			apportionItems.add(adapter.getItem(i));
		}
		savedCount = MoneyExpenseContainer.saveApportions(apportionItems, mMoneyExpenseContainerEditor);
		
//		for (int i = 0; i < count; i++) {
//			ApportionItem<MoneyApportion> api = adapter.getItem(i);
//			MoneyExpenseApportion apportion = (MoneyExpenseApportion) api.getApportion();
//			HyjModelEditor<MoneyExpenseApportion> apportionEditor = apportion.newModelEditor();
//
//					// 分摊好友是账本成员
//					if(api.getState() == ApportionItem.DELETED ){
//						ProjectShareAuthorization oldProjectShareAuthorization;
//						if(HyjApplication.getInstance().getCurrentUser().getId().equals(apportion.getFriendUserId())) {
//							// 把自己从分摊成员中移除：
//							// 1、要更新自己在旧账本中的分摊总额和支出总额，
//							// 2、要删除自己对应的分摊支出
//							// 更新旧账本的分摊支出
//							oldProjectShareAuthorization = mMoneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
//							HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
//							oldProjectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getApportionedTotalExpense() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
//							oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalExpense() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
//							oldProjectShareAuthorizationEditor.save();
//							
//							MoneyExpense moneyExpense = new Select().from(MoneyExpense.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
//							if(moneyExpense != null){
//								moneyExpense.delete();
//							}
//						} else {
//							// 把别人从分摊成员中移除：
//							// 1、要更新别人在旧账本中的分摊总额、支出总额、借入总额，
//							// 2、要删除别人对应的分摊支出和借入，
//							// 3、要删除自己对应的分摊借出，
//							// 4、要更新自己在该账本中的借出总额
//							// 更新旧账本分摊支出
//							oldProjectShareAuthorization = apportion.getProjectShareAuthorization();
//							HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
//							
//							oldProjectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getApportionedTotalExpense() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
//							oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalExpense() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
//							MoneyExpense moneyExpense = new Select().from(MoneyExpense.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
//							if(moneyExpense != null){
//								moneyExpense.delete();
//							} 
//						
//							oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalBorrow(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalBorrow() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
//							List<MoneyBorrow> moneyBorrows = new Select().from(MoneyBorrow.class).where("moneyExpenseApportionId=?", apportion.getId()).execute();
//							for(MoneyBorrow moneyBorrow : moneyBorrows){
//								moneyBorrow.delete();
//							} 
//							oldProjectShareAuthorizationEditor.save();
//							
//							oldProjectShareAuthorization = mMoneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
//							oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
//							oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalLend(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalLend() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
//							oldProjectShareAuthorizationEditor.save();
//							List<MoneyLend> moneyLends = new Select().from(MoneyLend.class).where("moneyExpenseApportionId=?", apportion.getId()).execute();
//							for(MoneyLend moneyLend : moneyLends){
//								moneyLend.delete();
//							} 
//						}
//						apportion.delete();
//						
//					} else {
//						 	if(api.getState() != ApportionItem.UNCHANGED
//									|| !mMoneyExpenseContainerEditor.getModelCopy().getProjectId().equals(mMoneyExpenseContainerEditor.getModel().getProjectId())
//									|| !mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId().equals(mMoneyExpenseContainerEditor.getModel().getMoneyAccountId())) {
//								api.saveToCopy(apportionEditor.getModelCopy());
//							}
//							Double oldRate = mMoneyExpenseContainerEditor.getModel().getExchangeRate(); 
//							Double rate = mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate();
//							Double oldApportionAmount = apportionEditor.getModel().getAmount0();
//							
//							ProjectShareAuthorization projectShareAuthorization;
//							//维护账本成员金额
//							if(HyjApplication.getInstance().getCurrentUser().getId().equals(apportion.getFriendUserId())){
//								projectShareAuthorization = mMoneyExpenseContainerEditor.getNewSelfProjectShareAuthorization();
//							} else if(apportion.getFriendUserId() != null){
//								projectShareAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", 
//										mMoneyExpenseContainerEditor.getModelCopy().getProjectId(), apportion.getFriendUserId()).executeSingle();
//							} else if(apportion.getLocalFriendId() != null){
//								projectShareAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND localFriendId=?", 
//										mMoneyExpenseContainerEditor.getModelCopy().getProjectId(), apportion.getLocalFriendId()).executeSingle();
//							} else {
//								projectShareAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND localFriendId IS NULL AND friendUserId IS NULL", 
//										mMoneyExpenseContainerEditor.getModelCopy().getProjectId()).executeSingle();
//							}
//							HyjModelEditor<ProjectShareAuthorization> projectShareAuthorizationEditor = projectShareAuthorization.newModelEditor();
//							
//							if(mMoneyExpenseContainerEditor.getModelCopy().get_mId() == null || 
//									mMoneyExpenseContainerEditor.getModel().getProjectId().equals(mMoneyExpenseContainerEditor.getModelCopy().getProjectId())){
//								 // 该支出是新的，或者该支出的账本没有改变：无旧账本需要更新，只需更新新账本的projectShareAuthorization
//								projectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(projectShareAuthorization.getApportionedTotalExpense() - (oldApportionAmount * oldRate) + (apportionEditor.getModelCopy().getAmount0() * rate));
//								projectShareAuthorizationEditor.getModelCopy().setActualTotalExpense(projectShareAuthorization.getActualTotalExpense() - (oldApportionAmount * oldRate) + (apportionEditor.getModelCopy().getAmount0() * rate));
//								if(!HyjApplication.getInstance().getCurrentUser().getId().equals(apportion.getFriendUserId())){
//									projectShareAuthorizationEditor.getModelCopy().setActualTotalBorrow(projectShareAuthorization.getActualTotalBorrow() - (oldApportionAmount * oldRate) + (apportionEditor.getModelCopy().getAmount0() * rate));
//									projectShareAuthorizationEditor.save();
//									
//									ProjectShareAuthorization selfProjectShareAuthorization = mMoneyExpenseContainerEditor.getNewSelfProjectShareAuthorization();
//									projectShareAuthorizationEditor = selfProjectShareAuthorization.newModelEditor();
//									projectShareAuthorizationEditor.getModelCopy().setActualTotalLend(selfProjectShareAuthorization.getActualTotalLend() - (oldApportionAmount * oldRate) + (apportionEditor.getModelCopy().getAmount0() * rate));
//									projectShareAuthorizationEditor.save();
//								} else {
//									projectShareAuthorizationEditor.save();
//								}
//							} else {
//								//更新新账本分摊支出
//								projectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(projectShareAuthorization.getApportionedTotalExpense() + (apportionEditor.getModelCopy().getAmount0() * rate));
//								projectShareAuthorizationEditor.getModelCopy().setActualTotalExpense(projectShareAuthorization.getActualTotalExpense() + (apportionEditor.getModelCopy().getAmount0() * rate));
//								if(!HyjApplication.getInstance().getCurrentUser().getId().equals(apportion.getFriendUserId())){
//									projectShareAuthorizationEditor.getModelCopy().setActualTotalBorrow(projectShareAuthorization.getActualTotalBorrow() + (apportionEditor.getModelCopy().getAmount0() * rate));
//									projectShareAuthorizationEditor.save();
//										
//									ProjectShareAuthorization selfProjectShareAuthorization = mMoneyExpenseContainerEditor.getNewSelfProjectShareAuthorization();
//									projectShareAuthorizationEditor = selfProjectShareAuthorization.newModelEditor();
//									projectShareAuthorizationEditor.getModelCopy().setActualTotalLend(selfProjectShareAuthorization.getActualTotalLend() + (apportionEditor.getModelCopy().getAmount0() * rate));
//									projectShareAuthorizationEditor.save();
//								} else {
//									projectShareAuthorizationEditor.save();
//								}
//								
//								//更新老账本分摊支出
//								ProjectShareAuthorization oldProjectAuthorization;
//								if(HyjApplication.getInstance().getCurrentUser().getId().equals(apportion.getFriendUserId())){
//									oldProjectAuthorization = mMoneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
//								} else if(apportion.getFriendUserId() != null){
//									oldProjectAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", 
//											mMoneyExpenseContainerEditor.getModel().getProjectId(), apportion.getFriendUserId()).executeSingle();
//								} else {
//									oldProjectAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND localFriendId=?", 
//											mMoneyExpenseContainerEditor.getModel().getProjectId(), apportion.getLocalFriendId()).executeSingle();
//								}
//								if(oldProjectAuthorization != null){
//									HyjModelEditor<ProjectShareAuthorization> oldProjectAuthorizationEditor = oldProjectAuthorization.newModelEditor();
//									oldProjectAuthorizationEditor.getModelCopy().setApportionedTotalExpense(oldProjectAuthorization.getApportionedTotalExpense() - (oldApportionAmount * oldRate));
//									oldProjectAuthorizationEditor.getModelCopy().setActualTotalExpense(oldProjectAuthorization.getActualTotalExpense() - (oldApportionAmount * oldRate));
//									if(!HyjApplication.getInstance().getCurrentUser().getId().equals(apportion.getFriendUserId())){
//										oldProjectAuthorizationEditor.getModelCopy().setActualTotalBorrow(oldProjectAuthorization.getActualTotalBorrow() - (oldApportionAmount * oldRate));
//										oldProjectAuthorizationEditor.save();
//
//										ProjectShareAuthorization oldSelfProjectAuthorization = mMoneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
//										oldProjectAuthorizationEditor = oldSelfProjectAuthorization.newModelEditor();
//										oldProjectAuthorizationEditor.getModelCopy().setActualTotalLend(oldSelfProjectAuthorization.getActualTotalLend() - (oldApportionAmount * oldRate));
//										oldProjectAuthorizationEditor.save();
//									} else {
//										oldProjectAuthorizationEditor.save();
//									}
//								}
//							}
//							
//							//更新相关好友的借贷账户
//							if(!HyjApplication.getInstance().getCurrentUser().getId().equals(apportion.getFriendUserId())){
//								MoneyAccount debtAccount = null;
//								if(mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId() != null){
//									if(!mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
//										debtAccount = MoneyAccount.getDebtAccount(mMoneyExpenseContainerEditor.getModelCopy().getProject().getCurrencyId(), null, mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId());
//									} else {
//										debtAccount = MoneyAccount.getDebtAccount(mMoneyExpenseContainerEditor.getModelCopy().getProject().getCurrencyId(), apportionEditor.getModelCopy().getLocalFriendId(), apportionEditor.getModelCopy().getFriendUserId());
//									}
//								} else {
//									debtAccount = MoneyAccount.getDebtAccount(mMoneyExpenseContainerEditor.getModelCopy().getProject().getCurrencyId(), apportionEditor.getModelCopy().getLocalFriendId(), apportionEditor.getModelCopy().getFriendUserId());
//								}
//								if(api.getState() == ApportionItem.NEW) {
//									// 该分摊是新增加的，我们更新借贷账户的余额即可
//					                if(debtAccount != null) {
//					                	HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
//					                	debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + apportionEditor.getModelCopy().getAmount0()*mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
//					                	debtAccountEditor.save();
//					                } else {
//					                	// 创建新的借贷账户
//					                	if(mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId() != null){
//					                		if(!mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
//												MoneyAccount.createDebtAccount(null, null, mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId(), mMoneyExpenseContainerEditor.getModelCopy().getProject().getCurrencyId(), apportionEditor.getModelCopy().getAmount0()*mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
//					                		} else {
//						                		MoneyAccount.createDebtAccount(projectShareAuthorization.getFriendUserName(), apportionEditor.getModelCopy().getLocalFriendId(), apportionEditor.getModelCopy().getFriendUserId(), mMoneyExpenseContainerEditor.getModelCopy().getProject().getCurrencyId(), apportionEditor.getModelCopy().getAmount0()*mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
//							                }
//					                	} else {
//					                		MoneyAccount.createDebtAccount(projectShareAuthorization.getFriendUserName(), apportionEditor.getModelCopy().getLocalFriendId(), apportionEditor.getModelCopy().getFriendUserId(), mMoneyExpenseContainerEditor.getModelCopy().getProject().getCurrencyId(), apportionEditor.getModelCopy().getAmount0()*mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
//						                }
//							        }
//								} else {
//									// 该分摊是旧的，我们要更新其对应的旧借贷账户
//									MoneyAccount oldDebtAccount = null;
//									if(mMoneyExpenseContainerEditor.getModel().getFinancialOwnerUserId() != null) {
//										if(!mMoneyExpenseContainerEditor.getModel().getFinancialOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
//											oldDebtAccount = MoneyAccount.getDebtAccount(mMoneyExpenseContainerEditor.getModel().getProject().getCurrencyId(), null, mMoneyExpenseContainerEditor.getModel().getFinancialOwnerUserId());
//										} else {
//											oldDebtAccount = MoneyAccount.getDebtAccount(mMoneyExpenseContainerEditor.getModel().getProject().getCurrencyId(), apportionEditor.getModel().getLocalFriendId(), apportionEditor.getModel().getFriendUserId());
//										}
//									} else {
//										oldDebtAccount = MoneyAccount.getDebtAccount(mMoneyExpenseContainerEditor.getModel().getProject().getCurrencyId(), apportionEditor.getModel().getLocalFriendId(), apportionEditor.getModel().getFriendUserId());
//									}
//									if(debtAccount == null) {
//										// 新账户不存在：更新老账户，创建新账户
//										if(oldDebtAccount != null){
//											HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
//											oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - apportionEditor.getModel().getAmount0()*apportionEditor.getModel().getExchangeRate());
//											oldDebtAccountEditor.save();
//										}
//										// 创建新的借贷账户
//					                	if(mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId() != null){
//					                		if(!mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
//												MoneyAccount.createDebtAccount(null, null, mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId(), mMoneyExpenseContainerEditor.getModelCopy().getProject().getCurrencyId(), apportionEditor.getModelCopy().getAmount0()*mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
//					                		} else {
//						                		MoneyAccount.createDebtAccount(projectShareAuthorization.getFriendUserName(), apportionEditor.getModelCopy().getLocalFriendId(), apportionEditor.getModelCopy().getFriendUserId(), mMoneyExpenseContainerEditor.getModelCopy().getProject().getCurrencyId(), apportionEditor.getModelCopy().getAmount0()*mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
//						                	}
//					                	} else {
//					                		MoneyAccount.createDebtAccount(projectShareAuthorization.getFriendUserName(), apportionEditor.getModelCopy().getLocalFriendId(), apportionEditor.getModelCopy().getFriendUserId(), mMoneyExpenseContainerEditor.getModelCopy().getProject().getCurrencyId(), apportionEditor.getModelCopy().getAmount0()*mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
//					                	}
//									}else if(oldDebtAccount != null && debtAccount.getId().equals(oldDebtAccount.getId())){
//										// 新老借贷账户一样
//					                	HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
//										oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - apportionEditor.getModel().getAmount0()*apportionEditor.getModel().getExchangeRate() + apportionEditor.getModelCopy().getAmount0()*mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
//										oldDebtAccountEditor.save();
//									}else{
//										// 新账户存在，更新新账户
//										if(oldDebtAccount != null){
//											HyjModelEditor<MoneyAccount> oldDebtAccountEditor = oldDebtAccount.newModelEditor();
//											oldDebtAccountEditor.getModelCopy().setCurrentBalance(oldDebtAccount.getCurrentBalance() - apportionEditor.getModel().getAmount0()*apportionEditor.getModel().getExchangeRate());
//											oldDebtAccountEditor.save();
//										}	
//
//										HyjModelEditor<MoneyAccount> debtAccountEditor = debtAccount.newModelEditor();
//										debtAccountEditor.getModelCopy().setCurrentBalance(debtAccount.getCurrentBalance() + apportionEditor.getModelCopy().getAmount0()*mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
//										debtAccountEditor.save();
//					                }
//								}
//						    }
//							
//							if(HyjApplication.getInstance().getCurrentUser().getId().equals(apportion.getFriendUserId())){
//								MoneyExpense moneyExpense = null;
//								if(apportion.get_mId() == null){
//									moneyExpense = new MoneyExpense();
//								} else {
//									moneyExpense = new Select().from(MoneyExpense.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
//								}
//								moneyExpense.setMoneyExpenseApportionId(apportionEditor.getModelCopy().getId());
//								moneyExpense.setAmount(apportionEditor.getModelCopy().getAmount0());
//								moneyExpense.setDate(mMoneyExpenseContainerEditor.getModelCopy().getDate());
//								moneyExpense.setRemark(mMoneyExpenseContainerEditor.getModelCopy().getRemark());
//								moneyExpense.setFriendAccountId(mMoneyExpenseContainerEditor.getModelCopy().getFriendAccountId());
//								moneyExpense.setFriendUserId(mMoneyExpenseContainerEditor.getModelCopy().getFriendUserId());
//								moneyExpense.setLocalFriendId(mMoneyExpenseContainerEditor.getModelCopy().getLocalFriendId());
//								moneyExpense.setExchangeRate(mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
//								moneyExpense.setGeoLat(mMoneyExpenseContainerEditor.getModelCopy().getGeoLat());
//								moneyExpense.setGeoLon(mMoneyExpenseContainerEditor.getModelCopy().getGeoLon());
//
//								if(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId() != null){
//									MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId());
//									moneyExpense.setMoneyAccountId(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId(), moneyAccount.getCurrencyId());
//								} else {
//									moneyExpense.setMoneyAccountId(null, null);
//								}
//								moneyExpense.setMoneyExpenseCategory(mMoneyExpenseContainerEditor.getModelCopy().getMoneyExpenseCategory());
//								moneyExpense.setMoneyExpenseCategoryMain(mMoneyExpenseContainerEditor.getModelCopy().getMoneyExpenseCategoryMain());
//								moneyExpense.setLocation(mMoneyExpenseContainerEditor.getModelCopy().getLocation());
//								moneyExpense.setAddress(mMoneyExpenseContainerEditor.getModelCopy().getAddress());
//								moneyExpense.setPictureId(mMoneyExpenseContainerEditor.getModelCopy().getPictureId());
//								moneyExpense.setProject(mMoneyExpenseContainerEditor.getModelCopy().getProject());
//								moneyExpense.save();
//							} else {
//								MoneyBorrow moneyBorrow = null;
//								MoneyLend moneyLendOfFinancialOwner = null;
//								
//								MoneyBorrow moneyBorrowOfFinancialOwner = null;
//								MoneyLend moneyLend = null;
//								if(apportion.get_mId() == null){
//									moneyLend = new MoneyLend();
//									moneyBorrowOfFinancialOwner = new MoneyBorrow();
//									
//									moneyBorrow = new MoneyBorrow();
//									moneyLendOfFinancialOwner = new MoneyLend();
//								} else {
//									if(mMoneyExpenseContainerEditor.getModel().getFinancialOwnerUserId() == null
//											|| mMoneyExpenseContainerEditor.getModel().getFinancialOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
//										if(apportionEditor.getModel().getFriendUserId() != null){
//											moneyLend = new Select().from(MoneyLend.class).where("moneyExpenseApportionId=? AND ownerUserId=? AND friendUserId = ?", apportion.getId(), HyjApplication.getInstance().getCurrentUser().getId(), apportionEditor.getModel().getFriendUserId()).executeSingle();
//											moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyExpenseApportionId=? AND ownerUserId=?", apportion.getId(), apportionEditor.getModel().getFriendUserId()).executeSingle();
//										} else {
//											moneyLend = new Select().from(MoneyLend.class).where("moneyExpenseApportionId=? AND ownerUserId=? AND localFriendId = ?", apportion.getId(), HyjApplication.getInstance().getCurrentUser().getId(), apportionEditor.getModel().getLocalFriendId()).executeSingle();
//											moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyExpenseApportionId=? AND ownerFriendId=?", apportion.getId(), apportionEditor.getModel().getLocalFriendId()).executeSingle();
//										}
//
//										moneyLendOfFinancialOwner = new MoneyLend();
//										moneyBorrowOfFinancialOwner = new MoneyBorrow();
//									} else {
//										moneyLend = new Select().from(MoneyLend.class).where("moneyExpenseApportionId=? AND ownerUserId=? AND friendUserId = ?", apportion.getId(), HyjApplication.getInstance().getCurrentUser().getId(), mMoneyExpenseContainerEditor.getModel().getFinancialOwnerUserId()).executeSingle();
//										moneyBorrow = new Select().from(MoneyBorrow.class).where("moneyExpenseApportionId=? AND ownerUserId=?", mMoneyExpenseContainerEditor.getModel().getFinancialOwnerUserId()).executeSingle();
//										
//										moneyBorrowOfFinancialOwner = new Select().from(MoneyBorrow.class).where("moneyExpenseApportionId=? AND ownerUserId=?", apportion.getId(), mMoneyExpenseContainerEditor.getModel().getFinancialOwnerUserId()).executeSingle();
//										if(apportionEditor.getModel().getFriendUserId() != null){
//											moneyLendOfFinancialOwner = new Select().from(MoneyLend.class).where("moneyExpenseApportionId=? AND ownerUserId=? AND friendUserId = ?", apportion.getId(), mMoneyExpenseContainerEditor.getModel().getFinancialOwnerUserId(), apportionEditor.getModel().getFriendUserId()).executeSingle();
//										} else {
//											moneyLendOfFinancialOwner = new Select().from(MoneyLend.class).where("moneyExpenseApportionId=? AND ownerUserId=? AND localFriendId = ?", apportion.getId(), mMoneyExpenseContainerEditor.getModel().getFinancialOwnerUserId(), apportionEditor.getModel().getLocalFriendId()).executeSingle();
//										}
//									}
//									
//									String previousFinancialOwnerUserId = HyjUtil.ifNull(mMoneyExpenseContainerEditor.getModel().getFinancialOwnerUserId() , "");
//									String currentFinancialOwnerUserId = HyjUtil.ifNull(mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId() , "");
//									if(moneyLendOfFinancialOwner != null && !previousFinancialOwnerUserId.equals(currentFinancialOwnerUserId)){
//										moneyLendOfFinancialOwner.delete();
//										moneyLendOfFinancialOwner = new MoneyLend();
//									}
//									if(moneyLendOfFinancialOwner == null) {
//										moneyLendOfFinancialOwner = new MoneyLend();
//									}
//									
//									if(moneyLend != null && !previousFinancialOwnerUserId.equals(currentFinancialOwnerUserId)){
//										moneyLend.delete();
//										moneyLend = new MoneyLend();
//									}
//									if(moneyLend == null){
//										moneyLend = new MoneyLend();
//									}
//
//									if(moneyBorrow != null && !previousFinancialOwnerUserId.equals(currentFinancialOwnerUserId)){
//										moneyBorrow.delete();
//										moneyBorrow = new MoneyBorrow();
//									}
//									if(moneyBorrow == null){
//										moneyBorrow = new MoneyBorrow();
//									}
//									
//									if(moneyBorrowOfFinancialOwner != null && !previousFinancialOwnerUserId.equals(currentFinancialOwnerUserId)){
//										moneyBorrowOfFinancialOwner.delete();
//										moneyBorrowOfFinancialOwner = new MoneyBorrow();
//									}
//									if(moneyBorrowOfFinancialOwner == null) {
//										moneyBorrowOfFinancialOwner = new MoneyBorrow();
//									}
//								}
//								
//								if(mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId() == null
//										|| mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
//									moneyLend.setMoneyExpenseApportionId(apportionEditor.getModelCopy().getId());
//									moneyLend.setAmount(apportionEditor.getModelCopy().getAmount0());
//									moneyLend.setDate(mMoneyExpenseContainerEditor.getModelCopy().getDate());
//									moneyLend.setRemark(mMoneyExpenseContainerEditor.getModelCopy().getRemark());
//									moneyLend.setFriendAccountId(mMoneyExpenseContainerEditor.getModelCopy().getFriendAccountId());
//									moneyLend.setFriendUserId(apportionEditor.getModelCopy().getFriendUserId());
//									moneyLend.setLocalFriendId(apportionEditor.getModelCopy().getLocalFriendId());
//									moneyLend.setExchangeRate(mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
//									moneyLend.setGeoLat(mMoneyExpenseContainerEditor.getModelCopy().getGeoLat());
//									moneyLend.setGeoLon(mMoneyExpenseContainerEditor.getModelCopy().getGeoLon());
//									
//									if(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId() != null){
//										MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId());
//										moneyLend.setMoneyAccountId(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId(), moneyAccount.getCurrencyId());
//									} else {
//										moneyLend.setMoneyAccountId(null, null);
//									}
//
//									moneyLend.setLocation(mMoneyExpenseContainerEditor.getModelCopy().getLocation());
//									moneyLend.setAddress(mMoneyExpenseContainerEditor.getModelCopy().getAddress());
//									moneyLend.setPictureId(mMoneyExpenseContainerEditor.getModelCopy().getPictureId());
//									moneyLend.setProject(mMoneyExpenseContainerEditor.getModelCopy().getProject());
//									moneyLend.save();
//								} else if(!mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
//									moneyLend.setMoneyExpenseApportionId(apportionEditor.getModelCopy().getId());
//									moneyLend.setAmount(apportionEditor.getModelCopy().getAmount0());
//									moneyLend.setDate(mMoneyExpenseContainerEditor.getModelCopy().getDate());
//									moneyLend.setRemark(mMoneyExpenseContainerEditor.getModelCopy().getRemark());
//									moneyLend.setFriendAccountId(mMoneyExpenseContainerEditor.getModelCopy().getFriendAccountId());
//									moneyLend.setFriendUserId(mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId());
//									moneyLend.setLocalFriendId(null);
//									moneyLend.setExchangeRate(mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
//									moneyLend.setGeoLat(mMoneyExpenseContainerEditor.getModelCopy().getGeoLat());
//									moneyLend.setGeoLon(mMoneyExpenseContainerEditor.getModelCopy().getGeoLon());
//									
//									if(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId() != null){
//										MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId());
//										moneyLend.setMoneyAccountId(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId(), moneyAccount.getCurrencyId());
//									} else {
//										moneyLend.setMoneyAccountId(null, null);
//									}
//
//									moneyLend.setLocation(mMoneyExpenseContainerEditor.getModelCopy().getLocation());
//									moneyLend.setAddress(mMoneyExpenseContainerEditor.getModelCopy().getAddress());
//									moneyLend.setPictureId(mMoneyExpenseContainerEditor.getModelCopy().getPictureId());
//									moneyLend.setProject(mMoneyExpenseContainerEditor.getModelCopy().getProject());
//									moneyLend.save();
//
//									//===================================================================================================
//									
//									moneyBorrowOfFinancialOwner.setMoneyExpenseApportionId(apportionEditor.getModelCopy().getId());
//									moneyBorrowOfFinancialOwner.setAmount(apportionEditor.getModelCopy().getAmount0());
//									moneyBorrowOfFinancialOwner.setOwnerUserId(mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId());
//									moneyBorrowOfFinancialOwner.setOwnerFriendId(null);
//									moneyBorrowOfFinancialOwner.setDate(mMoneyExpenseContainerEditor.getModelCopy().getDate());
//									moneyBorrowOfFinancialOwner.setRemark(mMoneyExpenseContainerEditor.getModelCopy().getRemark());
//									moneyBorrowOfFinancialOwner.setFriendAccountId(mMoneyExpenseContainerEditor.getModelCopy().getFriendAccountId());
//									moneyBorrowOfFinancialOwner.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
//									moneyBorrowOfFinancialOwner.setLocalFriendId(null);
//									moneyBorrowOfFinancialOwner.setExchangeRate(mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
//									moneyBorrowOfFinancialOwner.setGeoLat(mMoneyExpenseContainerEditor.getModelCopy().getGeoLat());
//									moneyBorrowOfFinancialOwner.setGeoLon(mMoneyExpenseContainerEditor.getModelCopy().getGeoLon());
//
//									if(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId() != null){
//										MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId());
//										moneyBorrowOfFinancialOwner.setMoneyAccountId(null, moneyAccount.getCurrencyId());
//									} else {
//										moneyBorrowOfFinancialOwner.setMoneyAccountId(null, null);
//									}
//									
//									moneyBorrowOfFinancialOwner.setLocation(mMoneyExpenseContainerEditor.getModelCopy().getLocation());
//									moneyBorrowOfFinancialOwner.setAddress(mMoneyExpenseContainerEditor.getModelCopy().getAddress());
//									moneyBorrowOfFinancialOwner.setPictureId(mMoneyExpenseContainerEditor.getModelCopy().getPictureId());
//									moneyBorrowOfFinancialOwner.setProject(mMoneyExpenseContainerEditor.getModelCopy().getProject());
//									moneyBorrowOfFinancialOwner.save();
//								}
//								
//
//								
//								
//								if(mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId() == null
//										|| mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
//									moneyBorrow.setMoneyExpenseApportionId(apportionEditor.getModelCopy().getId());
//									moneyBorrow.setAmount(apportionEditor.getModelCopy().getAmount0());
//									if(apportionEditor.getModelCopy().getFriendUserId() != null){
//										moneyBorrow.setOwnerUserId(apportionEditor.getModelCopy().getFriendUserId());
//										moneyBorrow.setOwnerFriendId(null);
//									} else {
//										moneyBorrow.setOwnerUserId(""); // 设为"",使他不会自动使用当前的用户id
//										moneyBorrow.setOwnerFriendId(apportionEditor.getModelCopy().getLocalFriendId());
//									}
//									moneyBorrow.setDate(mMoneyExpenseContainerEditor.getModelCopy().getDate());
//									moneyBorrow.setRemark(mMoneyExpenseContainerEditor.getModelCopy().getRemark());
//									moneyBorrow.setFriendAccountId(mMoneyExpenseContainerEditor.getModelCopy().getFriendAccountId());
//									moneyBorrow.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
//									moneyBorrow.setLocalFriendId(null);
//									moneyBorrow.setExchangeRate(mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
//									moneyBorrow.setGeoLat(mMoneyExpenseContainerEditor.getModelCopy().getGeoLat());
//									moneyBorrow.setGeoLon(mMoneyExpenseContainerEditor.getModelCopy().getGeoLon());
//	
//									if(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId() != null){
//										MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId());
//										moneyBorrow.setMoneyAccountId(null, moneyAccount.getCurrencyId());
//									} else {
//										moneyBorrow.setMoneyAccountId(null, null);
//									}
//									
//									moneyBorrow.setLocation(mMoneyExpenseContainerEditor.getModelCopy().getLocation());
//									moneyBorrow.setAddress(mMoneyExpenseContainerEditor.getModelCopy().getAddress());
//									moneyBorrow.setPictureId(mMoneyExpenseContainerEditor.getModelCopy().getPictureId());
//									moneyBorrow.setProject(mMoneyExpenseContainerEditor.getModelCopy().getProject());
//									moneyBorrow.save();
//								} else if(!mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId().equals(apportionEditor.getModelCopy().getFriendUserId())){
//									moneyBorrow.setMoneyExpenseApportionId(apportionEditor.getModelCopy().getId());
//									moneyBorrow.setAmount(apportionEditor.getModelCopy().getAmount0());
//									if(apportionEditor.getModelCopy().getFriendUserId() != null){
//										moneyBorrow.setOwnerUserId(apportionEditor.getModelCopy().getFriendUserId());
//										moneyBorrow.setOwnerFriendId(null);
//									} else {
//										moneyBorrow.setOwnerUserId("");  // 设为"",使他不会自动使用当前的用户id
//										moneyBorrow.setOwnerFriendId(apportionEditor.getModelCopy().getLocalFriendId());
//									}
//									moneyBorrow.setDate(mMoneyExpenseContainerEditor.getModelCopy().getDate());
//									moneyBorrow.setRemark(mMoneyExpenseContainerEditor.getModelCopy().getRemark());
//									moneyBorrow.setFriendAccountId(mMoneyExpenseContainerEditor.getModelCopy().getFriendAccountId());
//									moneyBorrow.setFriendUserId(mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId());
//									moneyBorrow.setLocalFriendId(null);
//									moneyBorrow.setExchangeRate(mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
//									moneyBorrow.setGeoLat(mMoneyExpenseContainerEditor.getModelCopy().getGeoLat());
//									moneyBorrow.setGeoLon(mMoneyExpenseContainerEditor.getModelCopy().getGeoLon());
//	
//									if(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId() != null){
//										MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId());
//										moneyBorrow.setMoneyAccountId(null, moneyAccount.getCurrencyId());
//									} else {
//										moneyBorrow.setMoneyAccountId(null, null);
//									}
//									
//									moneyBorrow.setLocation(mMoneyExpenseContainerEditor.getModelCopy().getLocation());
//									moneyBorrow.setAddress(mMoneyExpenseContainerEditor.getModelCopy().getAddress());
//									moneyBorrow.setPictureId(mMoneyExpenseContainerEditor.getModelCopy().getPictureId());
//									moneyBorrow.setProject(mMoneyExpenseContainerEditor.getModelCopy().getProject());
//									moneyBorrow.save();
//									
//									//===================================================================================================
//									
//									moneyLendOfFinancialOwner.setMoneyExpenseApportionId(apportionEditor.getModelCopy().getId());
//									moneyLendOfFinancialOwner.setAmount(apportionEditor.getModelCopy().getAmount0());
//									moneyLendOfFinancialOwner.setDate(mMoneyExpenseContainerEditor.getModelCopy().getDate());
//									moneyLendOfFinancialOwner.setRemark(mMoneyExpenseContainerEditor.getModelCopy().getRemark());
//									moneyLendOfFinancialOwner.setFriendAccountId(mMoneyExpenseContainerEditor.getModelCopy().getFriendAccountId());
//									moneyLendOfFinancialOwner.setOwnerUserId(mMoneyExpenseContainerEditor.getModelCopy().getFinancialOwnerUserId());
//									moneyLendOfFinancialOwner.setOwnerFriendId(null);
//									if(apportionEditor.getModelCopy().getFriendUserId() != null){
//										moneyLendOfFinancialOwner.setFriendUserId(apportionEditor.getModelCopy().getFriendUserId());
//										moneyLendOfFinancialOwner.setLocalFriendId(null);
//									} else {
//										moneyLendOfFinancialOwner.setFriendUserId(null);
//										moneyLendOfFinancialOwner.setLocalFriendId(apportionEditor.getModelCopy().getLocalFriendId());
//									}
//									moneyLendOfFinancialOwner.setExchangeRate(mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
//									moneyLendOfFinancialOwner.setGeoLat(mMoneyExpenseContainerEditor.getModelCopy().getGeoLat());
//									moneyLendOfFinancialOwner.setGeoLon(mMoneyExpenseContainerEditor.getModelCopy().getGeoLon());
//									
//									if(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId() != null){
//										MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId());
//										moneyLendOfFinancialOwner.setMoneyAccountId(null, moneyAccount.getCurrencyId());
//									} else {
//										moneyLendOfFinancialOwner.setMoneyAccountId(null, null);
//									}
//
//									moneyLendOfFinancialOwner.setLocation(mMoneyExpenseContainerEditor.getModelCopy().getLocation());
//									moneyLendOfFinancialOwner.setAddress(mMoneyExpenseContainerEditor.getModelCopy().getAddress());
//									moneyLendOfFinancialOwner.setPictureId(mMoneyExpenseContainerEditor.getModelCopy().getPictureId());
//									moneyLendOfFinancialOwner.setProject(mMoneyExpenseContainerEditor.getModelCopy().getProject());
//									moneyLendOfFinancialOwner.save();
//
//								}
//								
//								
//								MoneyExpense moneyExpense = null;
//								if(apportion.get_mId() == null){
//									moneyExpense = new MoneyExpense();
//								} else {
//									moneyExpense = new Select().from(MoneyExpense.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
//								}
//								moneyExpense.setMoneyExpenseApportionId(apportionEditor.getModelCopy().getId());
//								moneyExpense.setAmount(apportionEditor.getModelCopy().getAmount0());
//								if(apportionEditor.getModelCopy().getFriendUserId() != null){
//									moneyExpense.setOwnerUserId(apportionEditor.getModelCopy().getFriendUserId());
//									moneyExpense.setOwnerFriendId(null);
//								} else {
//									moneyExpense.setOwnerUserId("");  // 设为"",使他不会自动使用当前的用户id
//									moneyExpense.setOwnerFriendId(apportionEditor.getModelCopy().getLocalFriendId());
//								}
//								moneyExpense.setDate(mMoneyExpenseContainerEditor.getModelCopy().getDate());
//								moneyExpense.setRemark(mMoneyExpenseContainerEditor.getModelCopy().getRemark());
//								moneyExpense.setFriendAccountId(mMoneyExpenseContainerEditor.getModelCopy().getFriendAccountId());
//								moneyExpense.setFriendUserId(mMoneyExpenseContainerEditor.getModelCopy().getFriendUserId());
//								moneyExpense.setLocalFriendId(null);
//								moneyExpense.setExchangeRate(mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
//								moneyExpense.setGeoLat(mMoneyExpenseContainerEditor.getModelCopy().getGeoLat());
//								moneyExpense.setGeoLon(mMoneyExpenseContainerEditor.getModelCopy().getGeoLon());
//
//								if(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId() != null){
//									MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId());
//									moneyExpense.setMoneyAccountId(null, moneyAccount.getCurrencyId());
//								} else {
//									moneyExpense.setMoneyAccountId(null, null);
//								}
//								moneyExpense.setLocation(mMoneyExpenseContainerEditor.getModelCopy().getLocation());
//								moneyExpense.setAddress(mMoneyExpenseContainerEditor.getModelCopy().getAddress());
//								moneyExpense.setMoneyExpenseCategory(mMoneyExpenseContainerEditor.getModelCopy().getMoneyExpenseCategory());
//								moneyExpense.setMoneyExpenseCategoryMain(mMoneyExpenseContainerEditor.getModelCopy().getMoneyExpenseCategoryMain());
//								moneyExpense.setPictureId(mMoneyExpenseContainerEditor.getModelCopy().getPictureId());
//								moneyExpense.setProject(mMoneyExpenseContainerEditor.getModelCopy().getProject());
//								moneyExpense.save();
//							}		
//							
////							if(api.getState() != ApportionItem.UNCHANGED
////										|| !mMoneyExpenseContainerEditor.getModelCopy().getProjectId().equals(mMoneyExpenseContainerEditor.getModel().getProjectId())
////										|| !mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId().equals(mMoneyExpenseContainerEditor.getModel().getMoneyAccountId())) {
//								apportionEditor.save();
////							}
//							savedCount++;
//				}
//			}
////		}
		
		// 从隐藏掉的分摊里面删除原来的分摊
		Iterator<ApportionItem<MoneyApportion>> it = mApportionFieldApportions.getHiddenApportions().iterator();
		while (it.hasNext()) {
			// Get element
			ApportionItem<MoneyApportion> item = it.next();
			if (item.getState() != ApportionItem.NEW) {
				MoneyExpenseApportion apportion = ((MoneyExpenseApportion) item.getApportion());
				MoneyExpenseContainer.deleteApportion(apportion, mMoneyExpenseContainerEditor);
//					ProjectShareAuthorization oldProjectShareAuthorization;
////					if(apportion.getFriendUserId() != null){
//						if(HyjApplication.getInstance().getCurrentUser().getId().equals(apportion.getFriendUserId())){
//							// 更新旧账本的分摊支出
//							oldProjectShareAuthorization = mMoneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
//						} else {
//							// 更新旧账本分摊支出
//							oldProjectShareAuthorization = apportion.getProjectShareAuthorization();
//						}
//						
//						HyjModelEditor<ProjectShareAuthorization> oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
//						oldProjectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getApportionedTotalExpense() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
//						oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalExpense(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalExpense() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
//						if(!HyjApplication.getInstance().getCurrentUser().getId().equals(apportion.getFriendUserId())){
//							oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalBorrow(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalBorrow() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
//							oldProjectShareAuthorizationEditor.save();
//					
//							oldProjectShareAuthorization = mMoneyExpenseContainerEditor.getOldSelfProjectShareAuthorization();
//							oldProjectShareAuthorizationEditor = oldProjectShareAuthorization.newModelEditor();
//							oldProjectShareAuthorizationEditor.getModelCopy().setActualTotalLend(oldProjectShareAuthorizationEditor.getModelCopy().getActualTotalLend() - (apportion.getAmount0() * apportion.getMoneyExpenseContainer().getExchangeRate()));
//							oldProjectShareAuthorizationEditor.save();
//						} else {
//							oldProjectShareAuthorizationEditor.save();
//						}
////					}
//					
//					if(HyjApplication.getInstance().getCurrentUser().getId().equals(apportion.getFriendUserId())){
//						MoneyExpense moneyExpense = new Select().from(MoneyExpense.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
//						if(moneyExpense != null){
//							moneyExpense.delete();
//						}
//					} else {
//						List<MoneyLend> moneyLends = new Select().from(MoneyLend.class).where("moneyExpenseApportionId=?", apportion.getId()).execute();
//						for(MoneyLend moneyLend : moneyLends){
//							moneyLend.delete();
//						} 
//						//if(apportion.getFriendUserId() != null){
//							List<MoneyBorrow> moneyBorrows = new Select().from(MoneyBorrow.class).where("moneyExpenseApportionId=?", apportion.getId()).execute();
//							for(MoneyBorrow moneyBorrow : moneyBorrows){
//								moneyBorrow.delete();
//							} 
//							MoneyExpense moneyExpense = new Select().from(MoneyExpense.class).where("moneyExpenseApportionId=?", apportion.getId()).executeSingle();
//							if(moneyExpense != null){
//								moneyExpense.delete();
//							} 
////						}
//					}
//				apportion.delete();
			}
		}
		
		// 如果列表里一个都没有被保存，我们生成一个默认的分摊
		if (savedCount == 0) {
			MoneyExpenseApportion apportion = new MoneyExpenseApportion();
			apportion.setAmount(mMoneyExpenseContainerEditor.getModelCopy().getAmount0());
			apportion.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
			apportion.setMoneyExpenseContainerId(mMoneyExpenseContainerEditor.getModelCopy().getId());
			apportion.setApportionType("Average");
			
			//更新账本成员的分摊金额
			ProjectShareAuthorization projectShareAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", 
					mMoneyExpenseContainerEditor.getModelCopy().getProjectId(), apportion.getFriendUserId()).executeSingle();
			HyjModelEditor<ProjectShareAuthorization> projectShareAuthorizationEditor = projectShareAuthorization.newModelEditor();
			projectShareAuthorizationEditor.getModelCopy().setApportionedTotalExpense(projectShareAuthorizationEditor.getModelCopy().getApportionedTotalExpense() + (apportion.getAmount0() * mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate()));
			projectShareAuthorizationEditor.getModelCopy().setActualTotalExpense(projectShareAuthorizationEditor.getModelCopy().getActualTotalExpense() + (apportion.getAmount0() * mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate()));
			projectShareAuthorizationEditor.save();
			
			MoneyExpense moneyExpense = null;
			moneyExpense = new MoneyExpense();
			
			moneyExpense.setMoneyExpenseApportionId(apportion.getId());
			moneyExpense.setAmount(apportion.getAmount0());
			moneyExpense.setDate(mMoneyExpenseContainerEditor.getModelCopy().getDate());
			moneyExpense.setRemark(mMoneyExpenseContainerEditor.getModelCopy().getRemark());
			moneyExpense.setFriendAccountId(mMoneyExpenseContainerEditor.getModelCopy().getFriendAccountId());
			moneyExpense.setFriendUserId(mMoneyExpenseContainerEditor.getModelCopy().getFriendUserId());
			moneyExpense.setLocalFriendId(mMoneyExpenseContainerEditor.getModelCopy().getLocalFriendId());
			moneyExpense.setExchangeRate(mMoneyExpenseContainerEditor.getModelCopy().getExchangeRate());
			moneyExpense.setGeoLat(mMoneyExpenseContainerEditor.getModelCopy().getGeoLat());
			moneyExpense.setGeoLon(mMoneyExpenseContainerEditor.getModelCopy().getGeoLon());

			if(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId() != null){
				MoneyAccount moneyAccount = HyjModel.getModel(MoneyAccount.class, mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId());
				moneyExpense.setMoneyAccountId(mMoneyExpenseContainerEditor.getModelCopy().getMoneyAccountId(), moneyAccount.getCurrencyId());
			} else {
				moneyExpense.setMoneyAccountId(null, null);
			}
			moneyExpense.setLocation(mMoneyExpenseContainerEditor.getModelCopy().getLocation());
			moneyExpense.setAddress(mMoneyExpenseContainerEditor.getModelCopy().getAddress());
			moneyExpense.setMoneyExpenseCategory(mMoneyExpenseContainerEditor.getModelCopy().getMoneyExpenseCategory());
			moneyExpense.setMoneyExpenseCategoryMain(mMoneyExpenseContainerEditor.getModelCopy().getMoneyExpenseCategoryMain());
			moneyExpense.setPictureId(mMoneyExpenseContainerEditor.getModelCopy().getPictureId());
			moneyExpense.setProject(mMoneyExpenseContainerEditor.getModelCopy().getProject());
			
			moneyExpense.save();
			apportion.save();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case GET_MONEYACCOUNT_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				MoneyAccount moneyAccount = MoneyAccount.load(
						MoneyAccount.class, _id);
				mSelectorFieldMoneyAccount.setText(moneyAccount.getName() + "("
						+ moneyAccount.getCurrencyId() + ")");
				mSelectorFieldMoneyAccount.setModelId(moneyAccount.getId());
				setExchangeRate(false);
			}
			break;
		case GET_PROJECT_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				Project project = Project.load(Project.class, _id);
				ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("projectId = ? AND friendUserId=?", project.getId(), HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
				
				if(mMoneyExpenseContainerEditor.getModelCopy().get_mId() == null && !psa.getProjectShareMoneyExpenseAddNew()){
					HyjUtil.displayToast(R.string.app_permission_no_addnew);
					return;
				}else if(mMoneyExpenseContainerEditor.getModelCopy().get_mId() != null && !psa.getProjectShareMoneyExpenseEdit()){
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
				mApportionFieldApportions.changeProject(project, MoneyExpenseApportion.class);
				mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
			}
			break;

		case GET_FRIEND_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);

 	   	       	if(_id == -1){
 	   	       		mSelectorFieldFriend.setText(null);
 	   	       		mSelectorFieldFriend.setModelId(null);
 	       		} else {
					Friend friend = Friend.load(Friend.class, _id);
					
					if(HyjApplication.getInstance().getCurrentUser().getId().equals(friend.getFriendUserId())){
						HyjUtil.displayToast(R.string.moneyExpenseFormFragment_editText_error_friend);
						return;
					}
					mSelectorFieldFriend.setText(friend.getDisplayName());
					mSelectorFieldFriend.setModelId(friend.getId());
 	       		}
			}
			break;
			
		case GET_REMARK:
			if (resultCode == Activity.RESULT_OK) {
				String text = data.getStringExtra("TEXT");
				mRemarkFieldRemark.setText(text);
			}
			break;
		case GET_AMOUNT:
			if (resultCode == Activity.RESULT_OK) {
				String calculatorAmount = data.getStringExtra("calculatorAmount");
				if (calculatorAmount != null){
					mNumericAmount.setNumber(Double.parseDouble(calculatorAmount));
				}
			}
			break;
		case GET_CATEGORY_ID:
			if (resultCode == Activity.RESULT_OK) {
				long _id = data.getLongExtra("MODEL_ID", -1);
				MoneyExpenseCategory category = MoneyExpenseCategory.load(MoneyExpenseCategory.class, _id);
				mSelectorFieldMoneyExpenseCategory.setText(category.getName());
				mSelectorFieldMoneyExpenseCategory.setModelId(category.getId());
				if(category.getParentExpenseCategory() != null){
					mSelectorFieldMoneyExpenseCategory.setLabel(category.getParentExpenseCategory().getName());
				} else {
					mSelectorFieldMoneyExpenseCategory.setLabel(null);
				}
			}
			break;

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
	
	private void AddApportionMember(String type, long _id) {
		ProjectShareAuthorization psa = null;
		if("ProjectShareAuthorization".equalsIgnoreCase(type)){
			psa = ProjectShareAuthorization.load(ProjectShareAuthorization.class, _id);
		} else {
			final Friend friend = Friend.load(Friend.class, _id);
			//看一下该好友是不是账本成员
			if(friend.getFriendUserId() != null){
				psa = new Select().from(ProjectShareAuthorization.class).where("friendUserId=? AND projectId=? AND state <> 'Delete'", friend.getFriendUserId(), mSelectorFieldProject.getModelId()).executeSingle();
			} else {
				psa = new Select().from(ProjectShareAuthorization.class).where("localFriendId=? AND projectId=? AND state <> 'Delete'", friend.getId(), mSelectorFieldProject.getModelId()).executeSingle();
			}
			
			if(psa == null){
				((HyjActivity)getActivity()).displayDialog(R.string.moneyApportionField_select_toast_apportion_user_not_member, R.string.moneyApportionField_select_confirm_apportion_add_as_member, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
						new DialogCallbackListener() {
							@Override
							public void doPositiveClick(Object object) {
								Bundle bundle = new Bundle();
								bundle.putString("PROJECTID", mSelectorFieldProject.getModelId());
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
		apportion.setMoneyExpenseContainerId(mMoneyExpenseContainerEditor.getModel().getId());
		if (mApportionFieldApportions.addApportion(apportion,mSelectorFieldProject.getModelId(), ApportionItem.NEW)) {
			mApportionFieldApportions.setTotalAmount(mNumericAmount.getNumber());
		} else {
			HyjUtil.displayToast(R.string.moneyApportionField_select_toast_apportion_user_already_exists);
		}
	}
	

	@Override
	public void onDestroy(){
		super.onDestroy();
		mApportionFieldApportions.getAdapter().unregisterDataSetObserver(mApportionCountObserver);
	}
}
