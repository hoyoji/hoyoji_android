package com.hoyoji.hoyoji.project;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.activity.HyjActivity.DialogCallbackListener;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjHttpGetExchangeRateAsyncTask;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjListField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Currency;
import com.hoyoji.hoyoji.models.Exchange;
import com.hoyoji.hoyoji.models.ParentProject;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectRemark;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.money.currency.CurrencyListFragment;
import com.hoyoji.hoyoji.money.currency.ExchangeFormFragment;

public class ProjectFormFragment extends HyjUserFormFragment {
	private final static int GET_PARENT_PROJECT_ID = 1;
	private final static int GET_CURRENCY_ID = 2;
	private final static int FETCH_PROJECT_TO_LOCAL_EXCHANGE = 3;
	
	private HyjModelEditor<Project> mProjectEditor = null;
	private HyjTextField mTextFieldProjectName = null;
	private HyjTextField mTextFieldProjectRemarkName = null;
	private HyjListField mListFieldParentProject = null;
	private HyjSelectorField mSelectorFieldProjectCurrency = null;
	private CheckBox mCheckBoxAutoApportion = null;
	private PrentProjectListAdapter mParentProjectListAdapter = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.project_formfragment_project;
	}
	
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		Project project;
		Boolean editPermission = true;
		
		Intent intent = getActivity().getIntent();
		final Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			project =  new Select().from(Project.class).where("_id=?", modelId).executeSingle();
			if(!project.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
				editPermission = false;
			}
		} else {
			project = new Project();
		}
		mProjectEditor = project.newModelEditor();
		
		View buttonMember = getView().findViewById(R.id.projectFormFragment_button_member);
		if(modelId == -1){
			buttonMember.setVisibility(View.GONE);
		} else {
			buttonMember.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Bundle bundle = new Bundle();
					bundle.putLong("MODEL_ID", modelId);
					openActivityWithFragment(MemberListFragment.class, R.string.memberListFragment_title, bundle);
				}
			});
		}
		
		mTextFieldProjectName = (HyjTextField) getView().findViewById(R.id.projectFormFragment_textField_projectName);
		mTextFieldProjectName.setText(project.getName());
		mTextFieldProjectName.setEnabled(editPermission);
		
		mTextFieldProjectRemarkName = (HyjTextField) getView().findViewById(R.id.projectFormFragment_textField_projectRemarkName);
		if(modelId != -1 && !project.getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
			mTextFieldProjectRemarkName.setText(project.getRemarkName());
		} else {
			mTextFieldProjectRemarkName.setVisibility(View.GONE);
			getView().findViewById(R.id.projectFormFragment_textField_projectRemarkName_field_separator).setVisibility(View.GONE);
		}
		
		
		mListFieldParentProject = (HyjListField) getView().findViewById(R.id.projectFormFragment_listField_parentProject);
		mListFieldParentProject.setOnAddItemListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ProjectFormFragment.this
				.openActivityWithFragmentForResult(ProjectListFragment.class, R.string.projectListFragment_title_select_parent_project, null, GET_PARENT_PROJECT_ID);
			}
		});
		
		Currency currency = project.getCurrency();
		mSelectorFieldProjectCurrency = (HyjSelectorField) getView().findViewById(R.id.projectFormFragment_selectorField_projectCurrency);
		if(modelId != -1){
			mSelectorFieldProjectCurrency.setEnabled(false);
		}
		if(currency != null){
			mSelectorFieldProjectCurrency.setModelId(currency.getId());
			mSelectorFieldProjectCurrency.setText(currency.getName());
		}
		mSelectorFieldProjectCurrency.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ProjectFormFragment.this.openActivityWithFragmentForResult(CurrencyListFragment.class, R.string.currencyListFragment_title_select_currency, null, GET_CURRENCY_ID);
			}
		});	
		
		mCheckBoxAutoApportion = (CheckBox)getView().findViewById(R.id.projectFormFragment_checkBox_autoApportion);
		mCheckBoxAutoApportion.setChecked(project.getAutoApportion());
		mCheckBoxAutoApportion.setEnabled(editPermission);

		ArrayList<ParentProjectListItem> parentProjectList = new ArrayList<ParentProjectListItem>();
		for(ParentProject pp : project.getParentProjects()){
			parentProjectList.add(new ParentProjectListItem(pp));
		}
		mParentProjectListAdapter = new PrentProjectListAdapter(this.getActivity(), R.layout.project_formfragment_parentproject_listitem, R.id.list_item_title, parentProjectList);
		mListFieldParentProject.setListAdapter(mParentProjectListAdapter);
		
		if (modelId == -1){
			this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}
	}
	
	private void fillData() {
		Project modelCopy = (Project) mProjectEditor.getModelCopy();
		modelCopy.setName(mTextFieldProjectName.getText().toString().trim());
		modelCopy.setCurrencyId(mSelectorFieldProjectCurrency.getModelId());
		modelCopy.setAutoApportion(mCheckBoxAutoApportion.isChecked());
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		
		mTextFieldProjectName.setError(mProjectEditor.getValidationError("name"));
		mSelectorFieldProjectCurrency.setError(mProjectEditor.getValidationError("currency"));
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();

		mProjectEditor.validate();
		
		if(mProjectEditor.hasValidationErrors()){
			showValidatioErrors();
		} else {
			// 检查汇率存不存在
			final String projectCurrencyId = mProjectEditor.getModelCopy().getCurrencyId();
			
			if(projectCurrencyId.equalsIgnoreCase(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId())){
				// 币种是一样的，不用新增汇率
				doSave();
			} else {
				((HyjActivity)ProjectFormFragment.this.getActivity()).displayProgressDialog(R.string.projectMessageFormFragment_addShare_fetch_exchange, R.string.projectMessageFormFragment_addShare_fetching_exchange);
				Exchange exchange = new Select().from(Exchange.class).where("foreignCurrencyId=? AND localCurrencyId=?", projectCurrencyId, HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId()).executeSingle();
				if(exchange != null){
					// 汇率已经存在，直接保存新项目
					doSave();
					return;
				}
				// 尝试到网上获取汇率
				HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
					@Override
					public void finishCallback(Object object) {
						// 到网上获取汇率成功，新建汇率然后保存
						((HyjActivity)ProjectFormFragment.this.getActivity()).dismissProgressDialog();
						Double exchangeRate = (Double) object;
						Exchange newExchange = new Exchange();
						newExchange.setForeignCurrencyId(projectCurrencyId);
						newExchange.setLocalCurrencyId(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId());
						newExchange.setRate(exchangeRate);
						newExchange.save();
						doSave();
					}

					@Override
					public void errorCallback(Object object) {
						((HyjActivity)ProjectFormFragment.this.getActivity()).dismissProgressDialog();
						if (object != null) {
							HyjUtil.displayToast(object.toString());
						} else {
							HyjUtil.displayToast(R.string.moneyExpenseFormFragment_toast_cannot_refresh_rate);
						}

						// 到网上获取汇率失败，问用户是否要手工添加该汇率
						((HyjActivity)ProjectFormFragment.this.getActivity()).displayDialog(-1, R.string.projectMessageFormFragment_addShare_cannot_fetch_exchange, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1, new DialogCallbackListener(){
							@Override
							public void doPositiveClick(Object object){
								Bundle bundle = new Bundle();
								bundle.putString("localCurrencyId", HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId());
								bundle.putString("foreignCurrencyId", projectCurrencyId);
								openActivityWithFragmentForResult(ExchangeFormFragment.class, R.string.exchangeFormFragment_title_addnew, bundle, FETCH_PROJECT_TO_LOCAL_EXCHANGE);
							}
							@Override
							public void doNegativeClick(){
								HyjUtil.displayToast("未能获取项目币种到本币的汇率");
							}
							
						});
					}
				};
				HyjHttpGetExchangeRateAsyncTask.newInstance(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId(),projectCurrencyId,
						serverCallbacks);
			}
		}
	}	
	
	 
	 private void doSave(){
			try {
				ActiveAndroid.beginTransaction();
				int count = mParentProjectListAdapter.getCount();
				for(int i = 0; i < count; i++){
					ParentProjectListItem pp = mParentProjectListAdapter.getItem(i);
					if(pp.getState() == ParentProjectListItem.NEW){
						pp.getParentProject().save();
					} else if(pp.getState() == ParentProjectListItem.DELETED){
						pp.getParentProject().delete();
					}
				}
				
				if(mProjectEditor.getModelCopy().get_mId() == null){
					ProjectShareAuthorization newProjectShareAuthorization = new ProjectShareAuthorization();
					newProjectShareAuthorization.setProjectId(mProjectEditor.getModelCopy().getId());
					newProjectShareAuthorization.setState("Accept");
					newProjectShareAuthorization.setFriendUserId(HyjApplication.getInstance().getCurrentUser().getId());
					newProjectShareAuthorization.setSharePercentage(100.0);
					newProjectShareAuthorization.setSharePercentageType("Average");
					newProjectShareAuthorization.setShareAllSubProjects(false);
					newProjectShareAuthorization.setOwnerUserId(HyjApplication.getInstance().getCurrentUser().getId());
					
					newProjectShareAuthorization.save();
				}
				
				if(mProjectEditor.getModelCopy().get_mId() != null && 
						!mProjectEditor.getModelCopy().getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
					
					String projectRemarkName = mTextFieldProjectRemarkName.getText();
					ProjectRemark projectRemark = new Select().from(ProjectRemark.class).where("projectId=?", mProjectEditor.getModelCopy().getId()).executeSingle();
					if(projectRemarkName != null && projectRemarkName.trim().length() > 0){
						if(projectRemark == null){
							projectRemark = new ProjectRemark();
							projectRemark.setRemark(projectRemarkName);
							projectRemark.setProjectIdId(mProjectEditor.getModelCopy().getId());
							projectRemark.save();
						} else {
							HyjModelEditor<ProjectRemark> projectRemarkEditor = projectRemark.newModelEditor();
							projectRemarkEditor.getModelCopy().setRemark(projectRemarkName);
							projectRemarkEditor.save();
						}
					} else {
						if(projectRemark != null){
							projectRemark.delete();
						}
					}
				}
				
				mProjectEditor.save();
				HyjUtil.displayToast(R.string.app_save_success);
				ActiveAndroid.setTransactionSuccessful();
				getActivity().finish();
			} finally {
			    ActiveAndroid.endTransaction();
			}
	 }
	 
//	 public void createExchange(){
//		 Currency activeCurrency = HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrency();
//		 Exchange exchange = Exchange.getExchange(activeCurrency.getId(), mProjectEditor.getModelCopy().getCurrencyId());
//		 if(exchange == null){
//			 HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
//					@Override
//					public void finishCallback(Object object) {
//						Exchange newExchange = new Exchange();
//						newExchange.setLocalCurrencyId(HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId());
//						newExchange.setForeignCurrencyId(mProjectEditor.getModelCopy().getCurrencyId());
//						newExchange.setRate((Double) object);
//						newExchange.save();
//					}
//
//					@Override
//					public void errorCallback(Object object) {
//						if (object != null) {
//							HyjUtil.displayToast(object.toString());
//						} else {
//							HyjUtil.displayToast("无法获取汇率");
//						}
//					}
//				};
//			 
//			 HyjHttpGetExchangeRateAsyncTask.newInstance(activeCurrency.getId(), mProjectEditor.getModelCopy().getCurrencyId(), serverCallbacks);
//		 }
//	 }

	 @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode){
             case GET_PARENT_PROJECT_ID:
            	 if(resultCode == Activity.RESULT_OK){
            		 Project project = new Select().from(Project.class).where("_id=?",data.getLongExtra("MODEL_ID", -1)).executeSingle();
            		 if(project.getId().equals(mProjectEditor.getModel().getId())){
            			 HyjUtil.displayToast("不能添加自己为上级项目");
                 		 break;
            		 }
            		 //检查要增加的上级项目是不是已经是当前项目的下级项目
            		 ParentProject pp = new Select().from(ParentProject.class).as("main").where("parentProjectId = ? AND (subProjectId = ? OR EXISTS (SELECT id FROM ParentProject WHERE parentProjectId = main.subProjectId AND subProjectId = ?))", mProjectEditor.getModel().getId(), project.getId(), project.getId()).executeSingle();
            		 if(pp != null){
            			 HyjUtil.displayToast("该项目已经是当前项目的下级项目，不能添加！");
            		 } else {
            			 for(int i=0; i < mParentProjectListAdapter.getCount(); i++){
            				 ParentProjectListItem ppi = mParentProjectListAdapter.getItem(i);
            				 if(ppi.getParentProject().getParentProjectId().equals(project.getId())){
                    			 HyjUtil.displayToast("该项目已经是上级项目，不能重复添加！");
                    			 return;
            				 } 
//            				 else {
//            					 ParentProject pp1 = new Select().from(ParentProject.class).as("main").where("parentProjectId = ? AND (subProjectId = ? OR EXISTS (SELECT id FROM ParentProject WHERE parentProjectId = main.subProjectId AND subProjectId = ?))", project.getId(), mProjectEditor.getModel().getId(), mProjectEditor.getModel().getId()).executeSingle();
//            					 if(pp1 != null){
//                        			 HyjUtil.displayToast("该项目已经是上级项目，不能重复添加！");
//                        			 break;
//                        		 } 
//            				 }
            			 }
            			 
	            		 ParentProject parentProject = new ParentProject();
	            		 parentProject.setParentProjectId(project.getId());
	            		 parentProject.setSubProjectId(mProjectEditor.getModel().getId());
	            		 mParentProjectListAdapter.add(new ParentProjectListItem(parentProject, ParentProjectListItem.NEW));
            	 	 }
            	 }
            	 break;
             case GET_CURRENCY_ID:
            	 if(resultCode == Activity.RESULT_OK){
            		 long _id = data.getLongExtra("MODEL_ID", -1);
 	         		 Currency currency = Currency.load(Currency.class, _id);
 	         		 mSelectorFieldProjectCurrency.setText(currency.getName());
 	         		 mSelectorFieldProjectCurrency.setModelId(currency.getId());
            	 }
            	 break;
             case FETCH_PROJECT_TO_LOCAL_EXCHANGE:
	        	 if(resultCode == Activity.RESULT_OK){
						//检查该汇率是否添加成功，如果是保存
		        		Exchange exchange = new Select().from(Exchange.class).where("foreignCurrencyId=? AND localCurrencyId=?", mProjectEditor.getModelCopy().getCurrencyId(), HyjApplication.getInstance().getCurrentUser().getUserData().getActiveCurrencyId()).executeSingle();
						if(exchange != null){
							doSave();
						} else {
							HyjUtil.displayToast("未能获取项目币种到本币的汇率");
						}
	         	 }
	        	 break;
          }
    }
	 
	static class ParentProjectListItem {
		public static final int UNCHANGED = 0;
		public static final int NEW = 1;
		public static final int DELETED = 2;
		
		private int mState = 0;
		private ParentProject mParentProject;
		
		ParentProjectListItem(ParentProject parentProject){
			mParentProject = parentProject;
		}
		
		ParentProjectListItem(ParentProject parentProject, int state){
			mParentProject = parentProject;
			mState = state;
		}
		
		public void setState(int state){
			mState = state;
		}
		
		public int getState(){
			return mState;
		}
		
		public ParentProject getParentProject() {
			return mParentProject;
		}
		
		public String toString(){
			Project parentProject = mParentProject.getParentProject();
			if(parentProject != null){
				return parentProject.getName();
			} else {
				return HyjApplication.getInstance().getApplicationContext().getString(R.string.projectFormFragment_parentProject_topProject);
			}
		}
	}
	
	static class PrentProjectListAdapter extends ArrayAdapter<ParentProjectListItem> {
	    private LayoutInflater mInflater;
	    private int mTextViewResourceId;
	    private int mResource;

	    public PrentProjectListAdapter(Context context, int resource,
				int textViewResourceId, List<ParentProjectListItem> objects) {
			super(context, resource, textViewResourceId, objects);
			
			mTextViewResourceId = textViewResourceId;
			mResource = resource;
	        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public PrentProjectListAdapter(Context context, int resource,
				int textViewResourceId) {
			super(context, resource, textViewResourceId);

			mTextViewResourceId = textViewResourceId;
			mResource = resource;
	        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		static class ViewHolder{
			public TextView text;
			public ImageButton button;
		}
		
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(mResource, null);
				holder.text = (TextView) convertView
						.findViewById(mTextViewResourceId);
				holder.button = (ImageButton) convertView
						.findViewById(R.id.list_item_delete);
				holder.button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						int position = (Integer) view.getTag();
						ParentProjectListItem item = PrentProjectListAdapter.this
								.getItem(position);
						TextView tv = (TextView) ((ViewGroup) view.getParent())
								.findViewById(mTextViewResourceId);
						if (item.getState() == ParentProjectListItem.NEW) {
							PrentProjectListAdapter.this
									.remove(PrentProjectListAdapter.this
											.getItem(position));
						} else if (item.getState() == ParentProjectListItem.UNCHANGED) {
							item.setState(ParentProjectListItem.DELETED);
							((ImageButton) view)
									.setImageResource(R.drawable.ic_action_undo);
							tv.setPaintFlags(tv.getPaintFlags()
									| Paint.STRIKE_THRU_TEXT_FLAG);
						} else if (item.getState() == ParentProjectListItem.DELETED) {
							item.setState(ParentProjectListItem.UNCHANGED);
							((ImageButton) view)
									.setImageResource(R.drawable.ic_action_remove);
							tv.setPaintFlags(tv.getPaintFlags()
									& (~Paint.STRIKE_THRU_TEXT_FLAG));
						}
					}
				});

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.text.setText(PrentProjectListAdapter.this.getItem(position)
					.toString());
			holder.button.setTag(position);

			return convertView;
		}
	}
}
