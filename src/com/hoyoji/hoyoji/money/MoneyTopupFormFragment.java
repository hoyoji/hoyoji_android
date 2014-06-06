package com.hoyoji.hoyoji.money;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjModelEditor;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.activity.HyjActivity.DialogCallbackListener;
import com.hoyoji.android.hyjframework.fragment.HyjUserFormFragment;
import com.hoyoji.android.hyjframework.view.HyjDateTimeField;
import com.hoyoji.android.hyjframework.view.HyjImageField;
import com.hoyoji.android.hyjframework.view.HyjImageField.PictureItem;
import com.hoyoji.android.hyjframework.view.HyjNumericField;
import com.hoyoji.android.hyjframework.view.HyjRemarkField;
import com.hoyoji.android.hyjframework.view.HyjSelectorField;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyAccount;
import com.hoyoji.hoyoji.models.MoneyTransfer;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.money.moneyaccount.MoneyAccountListFragment;
import com.hoyoji.hoyoji.project.ProjectListFragment;
import com.hoyoji.hoyoji.friend.FriendListFragment;


public class MoneyTopupFormFragment extends HyjUserFormFragment {
	private final static int GET_TRANSFERIN_FRIEND_ID = 3;
	private final static int GET_TRANSFERIN_ID = 4;
	private final static int GET_PROJECT_ID = 5;
	
	private HyjModelEditor<MoneyTransfer> mMoneyTransferEditor = null;
	private HyjImageField mImageFieldPicture = null;
	private HyjDateTimeField mDateTimeFieldDate = null;
	private HyjNumericField mNumericTransferOutAmount = null;
	private HyjSelectorField mSelectorFieldTransferInFriend = null;
	private HyjSelectorField mSelectorFieldTransferIn = null;
	private HyjSelectorField mSelectorFieldProject = null;
	private HyjRemarkField mRemarkFieldRemark = null;
	
	@Override
	public Integer useContentView() {
		return R.layout.money_formfragment_moneytopup;
	}
	 
	@Override
	public void onInitViewData(){
		super.onInitViewData();
		MoneyTransfer moneyTransfer;
		
		Intent intent = getActivity().getIntent();
		long modelId = intent.getLongExtra("MODEL_ID", -1);
		if(modelId != -1){
			moneyTransfer =  new Select().from(MoneyTransfer.class).where("_id=?", modelId).executeSingle();
		} else {
			moneyTransfer = new MoneyTransfer();
			final String moneyAccountId = intent.getStringExtra("moneyAccountId");
			if(moneyAccountId != null){
				moneyTransfer.setTransferInId(moneyAccountId);
				moneyTransfer.setTransferOutId(moneyAccountId);
			} else {
				moneyTransfer.setTransferInId(null);
				moneyTransfer.setTransferOutId(null);
			}
//			String friendUserId = intent.getStringExtra("friendUserId");//从消息导入
//			if(friendUserId != null){
//				moneyTransfer.setTransferInFriendUserId(friendUserId);
//				moneyTransfer.setTransferOutFriendUserId(friendUserId);
//			} else {
//				String localFriendId = intent.getStringExtra("localFriendId");//从消息导入
//				if(localFriendId != null){
//					moneyTransfer.setTransferInLocalFriendId(localFriendId);
//					moneyTransfer.setTransferOutLocalFriendId(localFriendId);
//				}
//			}
		}
		mMoneyTransferEditor = moneyTransfer.newModelEditor();
		
		setupDeleteButton(mMoneyTransferEditor);
		
		mImageFieldPicture = (HyjImageField) getView().findViewById(R.id.moneyTopupFormFragment_imageField_picture);		
		mImageFieldPicture.setImages(moneyTransfer.getPictures());
		
		mDateTimeFieldDate = (HyjDateTimeField) getView().findViewById(R.id.moneyTopupFormFragment_textField_date);	
		if(modelId != -1){
			mDateTimeFieldDate.setText(moneyTransfer.getDate());
		}
		
		mNumericTransferOutAmount = (HyjNumericField) getView().findViewById(R.id.moneyTopupFormFragment_textField_transferOutAmount);		
		mNumericTransferOutAmount.setNumber(moneyTransfer.getTransferOutAmount());

		Friend transferInFriend = moneyTransfer.getTransferInFriend();
		mSelectorFieldTransferInFriend = (HyjSelectorField) getView().findViewById(R.id.moneyTopupFormFragment_selectorField_transferInFriend);
		if(transferInFriend != null){
			mSelectorFieldTransferInFriend.setModelId(transferInFriend.getId());
			mSelectorFieldTransferInFriend.setText(transferInFriend.getDisplayName());
		}
		mSelectorFieldTransferInFriend.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyTopupFormFragment.this
				.openActivityWithFragmentForResult(FriendListFragment.class, R.string.friendListFragment_title_select_friend_transferIn, null, GET_TRANSFERIN_FRIEND_ID);
			}
		}); 
		
		MoneyAccount transferIn = moneyTransfer.getTransferIn();
		mSelectorFieldTransferIn = (HyjSelectorField) getView().findViewById(R.id.moneyTopupFormFragment_selectorField_transferIn);

		if(transferIn != null){
			mSelectorFieldTransferIn.setModelId(transferIn.getId());
			mSelectorFieldTransferIn.setText(transferIn.getName() + "(" + transferIn.getCurrencyId() + ")");
		}
		mSelectorFieldTransferIn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("excludeType", "Debt");
				
				MoneyTopupFormFragment.this.openActivityWithFragmentForResult(MoneyAccountListFragment.class, R.string.moneyAccountListFragment_title_select_moneyAccount, bundle, GET_TRANSFERIN_ID);
			}
		});	
		
		Project project;
		String projectId = intent.getStringExtra("projectId");//从消息导入
		if(moneyTransfer.get_mId() == null && projectId != null){
			project = HyjModel.getModel(Project.class, projectId);
		}else{
			project = moneyTransfer.getProject();
		}
		mSelectorFieldProject = (HyjSelectorField) getView().findViewById(R.id.moneyTopupFormFragment_selectorField_project);
		
		if(project != null){
			mSelectorFieldProject.setModelId(project.getId());
			mSelectorFieldProject.setText(project.getDisplayName());
		}
		mSelectorFieldProject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MoneyTopupFormFragment.this.openActivityWithFragmentForResult(ProjectListFragment.class, R.string.projectListFragment_title_select_project, null, GET_PROJECT_ID);
			}
		});	
		
		
		
		mRemarkFieldRemark = (HyjRemarkField) getView().findViewById(R.id.moneyTopupFormFragment_textField_remark);
		mRemarkFieldRemark.setText(moneyTransfer.getRemark());
		
		ImageView takePictureButton = (ImageView) getView().findViewById(R.id.moneyTopupFormFragment_imageView_camera);	
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
				
				if(mSelectorFieldTransferInFriend.getModelId() == null && mSelectorFieldTransferIn.getModelId() == null){
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
				this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    super.onCreateOptionsMenu(menu, inflater);
	    if(mMoneyTransferEditor!= null 
	    		&& mMoneyTransferEditor.getModelCopy().get_mId() != null 
	    		&& mSelectorFieldTransferInFriend.getModelId() == null 
				&& mSelectorFieldTransferIn.getModelId() == null){
	    	hideSaveAction();
	    }
	}
	
	private void setPermission() {
		if(mSelectorFieldTransferInFriend.getModelId() == null 
				&& mSelectorFieldTransferIn.getModelId() == null){

//			mDateTimeFieldDate.setEnabled(false);
//			mNumericTransferOutAmount.setEnabled(false);
//			mSelectorFieldTransferInFriend.setEnabled(false);
//			mSelectorFieldTransferIn.setEnabled(false);
//			mSelectorFieldProject.setEnabled(false);
//			mRemarkFieldRemark.setEnabled(false);
			
			if(this.mOptionsMenu != null){
		    	hideSaveAction();
			}
			
		}
	}

	private void setupDeleteButton(HyjModelEditor<MoneyTransfer> moneyTransferEditor) {

		Button buttonDelete = (Button) getView().findViewById(R.id.button_delete);
		
		final MoneyTransfer moneyTransfer = moneyTransferEditor.getModelCopy();
		
		if (moneyTransfer.get_mId() != null) {
			buttonDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					((HyjActivity)getActivity()).displayDialog(R.string.app_action_delete_list_item, R.string.app_confirm_delete, R.string.alert_dialog_yes, R.string.alert_dialog_no, -1,
							new DialogCallbackListener() {
								@Override
								public void doPositiveClick(Object object) {
									try {
										ActiveAndroid.beginTransaction();

										MoneyAccount transferIn = moneyTransfer.getTransferIn();
										
										if(transferIn != null){
											HyjModelEditor<MoneyAccount> transferInEditor = transferIn.newModelEditor();
											transferInEditor.getModelCopy().setCurrentBalance(transferIn.getCurrentBalance() - moneyTransfer.getTransferInAmount());
											transferInEditor.save();
										}
										
										moneyTransfer.delete();

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
	
	
	private void fillData(){
		MoneyTransfer modelCopy = (MoneyTransfer) mMoneyTransferEditor.getModelCopy();
		modelCopy.setDate(mDateTimeFieldDate.getText());
		modelCopy.setTransferOutAmount(mNumericTransferOutAmount.getNumber());
		
		modelCopy.setTransferInId(mSelectorFieldTransferIn.getModelId());
		if(mSelectorFieldTransferInFriend.getModelId() != null){
			Friend transferInFriend = HyjModel.getModel(Friend.class, mSelectorFieldTransferInFriend.getModelId());
			modelCopy.setTransferInFriend(transferInFriend);
			modelCopy.setTransferInId(null);
		}else{
			modelCopy.setTransferInFriend(null);
		}
		
		modelCopy.setProjectId(mSelectorFieldProject.getModelId());
		
		modelCopy.setRemark(mRemarkFieldRemark.getText().toString().trim());
	}
	
	private void showValidatioErrors(){
		HyjUtil.displayToast(R.string.app_validation_error);
		mDateTimeFieldDate.setError(mMoneyTransferEditor.getValidationError("datetime"));
		mNumericTransferOutAmount.setError(mMoneyTransferEditor.getValidationError("transferOutAmount"));
		if(mMoneyTransferEditor.getValidationError("transferOutAmount") != null){
			mNumericTransferOutAmount.showSoftKeyboard();
		}
		mSelectorFieldTransferInFriend.setError(mMoneyTransferEditor.getValidationError("transferInFriend"));
		mSelectorFieldTransferIn.setError(mMoneyTransferEditor.getValidationError("transferIn"));
		mSelectorFieldProject.setError(mMoneyTransferEditor.getValidationError("project"));
		mRemarkFieldRemark.setError(mMoneyTransferEditor.getValidationError("remark"));
	}

	 @Override
	public void onSave(View v){
		super.onSave(v);
		
		fillData();
		
		mMoneyTransferEditor.validate();
		
		if(mMoneyTransferEditor.hasValidationErrors()){
			showValidatioErrors();
		} else {
			try {
				ActiveAndroid.beginTransaction();
				HyjImageField.ImageGridAdapter adapter = mImageFieldPicture.getAdapter();
				int count = adapter.getCount();
				boolean mainPicSet = false;
				for(int i = 0; i < count; i++){
					PictureItem pi = adapter.getItem(i);
					if(pi.getState() == PictureItem.NEW){
						Picture newPic = pi.getPicture();
						newPic.setRecordId(mMoneyTransferEditor.getModel().getId());
						newPic.setRecordType("MoneyTransfer");
						newPic.save();
					} else if(pi.getState() == PictureItem.DELETED){
						pi.getPicture().delete();
					} else if(pi.getState() == PictureItem.CHANGED){

					}
					if(!mainPicSet && pi.getPicture() != null){
						mainPicSet = true;
						mMoneyTransferEditor.getModelCopy().setPicture(pi.getPicture());
					}
				}
				
				MoneyTransfer oldMoneyTransferModel = mMoneyTransferEditor.getModel();
				MoneyTransfer moneyTransferModel = mMoneyTransferEditor.getModelCopy();
				MoneyAccount newTransferIn = moneyTransferModel.getTransferIn();
				
				if(moneyTransferModel.get_mId() == null){
				    if(newTransferIn != null){
						HyjModelEditor<MoneyAccount> newTransferInEditor = newTransferIn.newModelEditor();
				    	newTransferInEditor.getModelCopy().setCurrentBalance(newTransferIn.getCurrentBalance() + moneyTransferModel.getTransferInAmount0());
				    	newTransferInEditor.save();
				    }
				}else{
					MoneyAccount oldTransferIn = oldMoneyTransferModel.getTransferIn();
					
					if(oldTransferIn != null && newTransferIn != null && oldTransferIn.getId().equals(newTransferIn.getId())){
						HyjModelEditor<MoneyAccount> newTransferInEditor = newTransferIn.newModelEditor();
						newTransferInEditor.getModelCopy().setCurrentBalance(newTransferIn.getCurrentBalance() - oldMoneyTransferModel.getTransferInAmount0() + moneyTransferModel.getTransferInAmount0());
						newTransferInEditor.save();
					}else{
						if(oldTransferIn != null){
							HyjModelEditor<MoneyAccount> oldTransferInEditor = oldTransferIn.newModelEditor();
							oldTransferInEditor.getModelCopy().setCurrentBalance(oldTransferIn.getCurrentBalance() - oldMoneyTransferModel.getTransferInAmount0());
							oldTransferInEditor.save();
						}
						if(newTransferIn != null){
							HyjModelEditor<MoneyAccount> newTransferInEditor = newTransferIn.newModelEditor();
							newTransferInEditor.getModelCopy().setCurrentBalance(newTransferIn.getCurrentBalance() + moneyTransferModel.getTransferInAmount0());
							newTransferInEditor.save();
						}
					}
				}
				
				mMoneyTransferEditor.save();
				ActiveAndroid.setTransactionSuccessful();
				HyjUtil.displayToast(R.string.app_save_success);
				getActivity().finish();
			} finally {
			    ActiveAndroid.endTransaction();
			}
		}
	}	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode){
             case GET_TRANSFERIN_FRIEND_ID :
            	 if(resultCode == Activity.RESULT_OK){
             		long _id = data.getLongExtra("MODEL_ID", -1);
             		Friend friend = Friend.load(Friend.class, _id);
             		
//             		if(friend.getFriendUserId() != null && friend.getFriendUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
//             			//选择的商家好友是自己
//             			mSelectorFieldTransferInFriend.setText("");
//             			mSelectorFieldTransferInFriend.setModelId(null);
//             			
//             			MoneyAccount activeAccount = HyjModel.getModel(MoneyAccount.class, HyjApplication.getInstance().getCurrentUser().getUserData().getActiveMoneyAccountId());
//             			mSelectorFieldTransferIn.setText(activeAccount.getName() + "(" + activeAccount.getCurrencyId() + ")");
//                 		mSelectorFieldTransferIn.setModelId(activeAccount.getId());
//                 	} else {
                 		mSelectorFieldTransferInFriend.setText(friend.getDisplayName());
                 		mSelectorFieldTransferInFriend.setModelId(friend.getId());
//                 	}
             	 }
             	 break;
             case GET_TRANSFERIN_ID:
	        	 if(resultCode == Activity.RESULT_OK){
	         		long _id = data.getLongExtra("MODEL_ID", -1);
	         		MoneyAccount moneyAccount = MoneyAccount.load(MoneyAccount.class, _id);
	         		mSelectorFieldTransferIn.setText(moneyAccount.getName() + "(" + moneyAccount.getCurrencyId() + ")");
	         		mSelectorFieldTransferIn.setModelId(moneyAccount.getId());
	        	 }
	        	 break;
             case GET_PROJECT_ID:
	        	 if(resultCode == Activity.RESULT_OK){
	         		long _id = data.getLongExtra("MODEL_ID", -1);
	         		Project project = Project.load(Project.class, _id);
	         		mSelectorFieldProject.setText(project.getDisplayName());
	         		mSelectorFieldProject.setModelId(project.getId());
	        	 }
	        	 break;
          }
    }
}
