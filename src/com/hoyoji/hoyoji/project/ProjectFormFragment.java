package com.hoyoji.hoyoji.project;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjListField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.android.hyjframework.view.HyjTextField;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Currency;
import com.hoyoji.hoyoji.models.ParentProject;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.money.currency.CurrencyListFragment;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountFormFragment;
import com.hoyoji.hoyoji.project.ProjectFormFragment.ParentProjectListItem;


public class ProjectFormFragment extends HyjUserFormFragment {
	private final static int GET_PARENT_PROJECT_ID = 1;
	private final static int GET_CURRENCY_ID = 2;
	
	private HyjModelEditor mProjectEditor = null;
	private HyjTextField mTextFieldProjectName = null;
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
		
		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			project =  new Select().from(Project.class).where("_id=?", modelId).executeSingle();
		} else {
			project = new Project();
		}
		mProjectEditor = project.newModelEditor();
		
		mTextFieldProjectName = (HyjTextField) getView().findViewById(R.id.projectFormFragment_textField_projectName);
		mTextFieldProjectName.setText(project.getName());
		
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

		ArrayList<ParentProjectListItem> parentProjectList = new ArrayList<ParentProjectListItem>();
		for(ParentProject pp : project.getParentProjects()){
			parentProjectList.add(new ParentProjectListItem(pp));
		}
		mParentProjectListAdapter = new PrentProjectListAdapter(this.getActivity(), R.layout.project_formfragment_parentproject_listitem, R.id.list_item_title, parentProjectList);
		mListFieldParentProject.setListAdapter(mParentProjectListAdapter);
		
		this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
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
				mProjectEditor.save();
				HyjUtil.displayToast(R.string.app_save_success);
				ActiveAndroid.setTransactionSuccessful();
				getActivity().finish();
			} finally {
			    ActiveAndroid.endTransaction();
			}
		}
	}	
	
	 @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode){
             case GET_PARENT_PROJECT_ID:
            	 if(resultCode == Activity.RESULT_OK){
            		 Project project = new Select().from(Project.class).where("_id=?",data.getLongExtra("MODEL_ID", -1)).executeSingle();
            		 ParentProject parentProject = new ParentProject();
            		 parentProject.setParentProjectId(project.getId());
            		 parentProject.setSubProjectId(mProjectEditor.getModel().getId());
            		 mParentProjectListAdapter.add(new ParentProjectListItem(parentProject, ParentProjectListItem.NEW));
            	 }
            	 break;
             case GET_CURRENCY_ID:
            	 if(resultCode == Activity.RESULT_OK){
            		 HyjUtil.displayToast(String.valueOf(data.getLongExtra("MODEL_ID", -1)));
            		 long _id = data.getLongExtra("MODEL_ID", -1);
 	         		 Currency currency = Currency.load(Currency.class, _id);
 	         		 mSelectorFieldProjectCurrency.setText(currency.getName());
 	         		 mSelectorFieldProjectCurrency.setModelId(currency.getId());
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
			return mParentProject.getParentProject().getName();
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
