package com.hoyoji.android.hyjframework.fragment;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.aaevent_android.R;
import com.hoyoji.hoyoji.models.Event;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.money.MoneyApportionField;

public class HyjFreeGameFormFragment extends HyjUserFormFragment {
	private MoneyApportionField mApportionFieldApportions = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.game_formfragment_freegame;
	}

	@Override
	public void onInitViewData() {
		super.onInitViewData();
		Event event;
		Project project = null;

		Intent intent = getActivity().getIntent();
		Long modelId = intent.getLongExtra("MODEL_ID", -1);
		if (modelId != -1) {
			event = new Select().from(Event.class).where("_id=?", modelId).executeSingle();
			project = event.getProject();
		} else {
			event = new Event();
			Long project_id = intent.getLongExtra("PROJECT_ID", -1);
			if(project_id != -1){
				project = Project.load(Project.class, project_id);
			} else {
				String projectId = intent.getStringExtra("PROJECTID");
				if(projectId != null) {
					project = Project.getModel(Project.class, projectId);
				}
			}
			if (project != null) {
				event.setProjectId(project.getId());
			}
		}
		
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    super.onCreateOptionsMenu(menu, inflater);
	    
	}
	
	private void fillData() {
		
	}

	private void showValidatioErrors() {
		HyjUtil.displayToast(R.string.app_validation_error);

		
	}

	@Override
	public void onSave(View v) {
		super.onSave(v);

		fillData();

		
		
	}

	protected void doSave() {
	}
//	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		}
	}
}
