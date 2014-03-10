package com.hoyoji.hoyoji.money;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.android.hyjframework.view.HyjNumericView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyApportion;
import com.hoyoji.hoyoji.models.MoneyExpenseApportion;
import com.hoyoji.hoyoji.models.Picture;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.models.User;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class MoneyApportionField extends GridView {
	private ImageGridAdapter mImageGridAdapter;
	private	Resources r = getResources();
	private Set<ApportionItem<MoneyApportion>> mHiddenApportionItems = new HashSet<ApportionItem<MoneyApportion>>();
	private double mTotalAmount = 0.0;
	private String mMoneyTransactionId = null;
	
	public MoneyApportionField(Context context, AttributeSet attrs) {
		super(context, attrs);
//		int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 110, r.getDisplayMetrics());
//		this.setColumnWidth(px);
		this.setNumColumns(AUTO_FIT);
		this.setGravity(Gravity.CENTER);
		int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
		this.setVerticalSpacing(px);
		this.setHorizontalSpacing(px);
		//this.setStretchMode(STRETCH_COLUMN_WIDTH);
		this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		
		mImageGridAdapter = new ImageGridAdapter(context, 0);
		this.setAdapter(mImageGridAdapter);
	}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
	
	public void init(Double totalAmount, List<? extends MoneyApportion> apportions, String projectId, String moneyTransactionId){
		mMoneyTransactionId = moneyTransactionId;
		mTotalAmount = totalAmount;
		//List<PictureItem> pis = new ArrayList<PictureItem>();
		for(int i=0; i < apportions.size(); i++){
			ApportionItem<MoneyApportion> pi = new ApportionItem<MoneyApportion>(apportions.get(i), projectId, apportions.get(i).get_mId() == null ? ApportionItem.NEW : ApportionItem.UNCHANGED);
			mImageGridAdapter.add(pi);
			//pis.add(pi);
		}
		//mImageGridAdapter.addAll(pis);
	}
	
	public ImageGridAdapter getAdapter(){
		return mImageGridAdapter;
	}
	
	public boolean addApportion(MoneyApportion apportion, String projectId, int state){
		for(int i = 0; i < mImageGridAdapter.getCount(); i++){
			ApportionItem<MoneyApportion> api = (ApportionItem<MoneyApportion>) mImageGridAdapter.getItem(i);
			if(api.getApportion().getFriendUserId().equalsIgnoreCase(apportion.getFriendUserId())){
				return false;
			}
		}
		ApportionItem<MoneyApportion> pi = new ApportionItem<MoneyApportion>(apportion, projectId, state);
		mImageGridAdapter.add(pi);
		return true;
	}
	
	public void setAllApportionShare(){
		for(int i = 0; i < mImageGridAdapter.getCount(); i++){
			ApportionItem<MoneyApportion> api = (ApportionItem<MoneyApportion>) mImageGridAdapter.getItem(i);
			if(api.getState() != ApportionItem.DELETED) {
				api.setApportionType("Share");
			}
		}
	}
	
	public void setAllApportionAverage(){
		for(int i = 0; i < mImageGridAdapter.getCount(); i++){
			ApportionItem<MoneyApportion> api = (ApportionItem<MoneyApportion>) mImageGridAdapter.getItem(i);
			if(api.getState() != ApportionItem.DELETED) {
				api.setApportionType("Average");
			}
		}
	}
	
	public Double getTotalAmount(){
		double total = 0.0;
		for(int i = 0; i < mImageGridAdapter.getCount(); i++){
			ApportionItem<MoneyApportion> api = (ApportionItem<MoneyApportion>) mImageGridAdapter.getItem(i);
			if(api.getState() != ApportionItem.DELETED) {
				total += api.getAmount();
			}
		}
		return HyjUtil.toFixed2(total);
	}
	
	public int getCount(){
		int count = 0;
		for(int i = 0; i < mImageGridAdapter.getCount(); i++){
			ApportionItem<MoneyApportion> api = (ApportionItem<MoneyApportion>) mImageGridAdapter.getItem(i);
			if(api.getState() != ApportionItem.DELETED) {
				count++;
			}
		}
		return count;
	}
	
	
	public void setTotalAmount(Double totalAmount){
		double fixedTotal = 0.0;
		double sharePercentageTotal = 0.0;
		
		double averageAmount = 0.0;
		double shareTotal = 0.0;
		int numOfAverage = 0;
		if(totalAmount == null){
			totalAmount = mTotalAmount;
		} else {
			mTotalAmount = HyjUtil.toFixed2(totalAmount);
		}
		
		for(int i = 0; i < mImageGridAdapter.getCount(); i++){
			ApportionItem<MoneyApportion> api = (ApportionItem<MoneyApportion>) mImageGridAdapter.getItem(i);
			if(api.getState() != ApportionItem.DELETED) {
				if(api.getApportionType().equalsIgnoreCase("Average")){
					numOfAverage++;
					sharePercentageTotal += api.getSharePercentage();
				} else if(api.getApportionType().equalsIgnoreCase("Share")){
//					api.setAmount(api.getAmount());
					sharePercentageTotal += api.getSharePercentage();
				} else {
					fixedTotal += api.getAmount();
				}
			}
		}
		
		
		// 占股分摊=（总金额-定额分摊）*占股/（分摊人所占股数）
		shareTotal = totalAmount - fixedTotal;
		for(int i = 0; i < mImageGridAdapter.getCount(); i++){
			ApportionItem<MoneyApportion> api = (ApportionItem<MoneyApportion>) mImageGridAdapter.getItem(i);
			if(api.getState() != ApportionItem.DELETED) {
				if(api.getApportionType().equalsIgnoreCase("Share")){
					Double shareAmount = shareTotal * api.getSharePercentage() / sharePercentageTotal;
					api.setAmount(shareAmount);
					fixedTotal += api.getAmount();
				}
			}
		}
		
		// 平均分摊 = （总金额-定额分摊-占股分摊） / 平均分摊人数
		averageAmount = (totalAmount - fixedTotal) / numOfAverage;
		for(int i = 0; i < mImageGridAdapter.getCount(); i++){
			ApportionItem<MoneyApportion> api = (ApportionItem<MoneyApportion>) mImageGridAdapter.getItem(i);
			if(api.getState() != ApportionItem.DELETED) {
				if(api.getApportionType().equalsIgnoreCase("Average")){
					api.setAmount(averageAmount);
					fixedTotal += api.getAmount();
				}
			}
		}
		if(mImageGridAdapter.getCount() > 0){
			if(fixedTotal != totalAmount){
				double adjustedAmount = mImageGridAdapter.getItem(0).getAmount() + (totalAmount - fixedTotal);
				mImageGridAdapter.getItem(0).setAmount(adjustedAmount);
			}
		}
		mImageGridAdapter.notifyDataSetChanged();
	}
	
	public Set<ApportionItem<MoneyApportion>> getHiddenApportions(){
		return mHiddenApportionItems;
	}
	
	public void clearAll(){
		int i = 0; 
		while(mImageGridAdapter.getCount() > 0 && i < mImageGridAdapter.getCount()){
			ApportionItem<MoneyApportion> api = (ApportionItem<MoneyApportion>) mImageGridAdapter.getItem(i);
				if(api.getState() == ApportionItem.NEW){
					mImageGridAdapter.remove(api);
				} else if(api.getState() != ApportionItem.DELETED){
					api.delete();
					i++;
				} else {
					i++;
				}
		}
	}
	
	public void changeProject(Project project, Class<? extends MoneyApportion> type){
		List<ProjectShareAuthorization> projectShareAuthorizations = project.getShareAuthorizations();
		Set<String> friendUserSet = new HashSet<String>();
		Set<String> gridUserSet = new HashSet<String>();
		
		for(int i=0; i < projectShareAuthorizations.size(); i++){
			if(projectShareAuthorizations.get(i).getState().equals("Accept")) {
				friendUserSet.add(projectShareAuthorizations.get(i).getFriendUserId());
			}
		}	

		// 把不属于当前项目用户分摊隐藏掉
		int i = 0; 
		while(mImageGridAdapter.getCount() > 0 && i < mImageGridAdapter.getCount()){
			ApportionItem<MoneyApportion> api = (ApportionItem<MoneyApportion>) mImageGridAdapter.getItem(i);
			if(!friendUserSet.contains(api.getApportion().getFriendUserId())){
					mHiddenApportionItems.add(api);
					mImageGridAdapter.remove(api);
			} else {
				gridUserSet.add(api.getApportion().getFriendUserId());
				api.changeProject(project.getId());
				i++;
			}
		}

		// 把隐藏掉的分摊添加回去
	    Iterator<ApportionItem<MoneyApportion>> it = mHiddenApportionItems.iterator();
	    while (it.hasNext()) {
	        // Get element
	        ApportionItem<MoneyApportion> item = it.next();
	        if(friendUserSet.contains(item.getApportion().getFriendUserId())){
				gridUserSet.add(item.getApportion().getFriendUserId());
	        	mImageGridAdapter.add(item);
	        	item.changeProject(project.getId());
	        	it.remove();
	        }
	    }
	    if(project.getAutoApportion()){
		    int count = projectShareAuthorizations.size();
			for(i = 0; i < count; i++){
				if(!projectShareAuthorizations.get(i).getState().equals("Accept")) {
					continue;
				}
				if(!gridUserSet.contains(projectShareAuthorizations.get(i).getFriendUserId())){
					try {
						MoneyApportion apportion;
						apportion = type.newInstance();
						apportion.setAmount(0.0);
						apportion.setApportionType("Share");
						apportion.setMoneyId(mMoneyTransactionId);
						apportion.setFriendUserId(projectShareAuthorizations.get(i).getFriendUserId());
						//this.addApportion(apportion, project.getId(), ApportionItem.NEW);
						ApportionItem<MoneyApportion> pi = new ApportionItem<MoneyApportion>(apportion, project.getId(), ApportionItem.NEW);
						mImageGridAdapter.add(pi);
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
	    }
	}
	
	public void setError(String errMsg){
		if(errMsg != null){
			HyjUtil.displayToast(errMsg);
		}
	}
	
	public static class ImageGridAdapter extends ArrayAdapter<ApportionItem<MoneyApportion>> {
		LayoutInflater inflater;
		public ImageGridAdapter(Context context, int resource) {
			super(context, resource);
			inflater = (LayoutInflater)
		       context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		static class ViewHolder{
			public HyjImageView imageViewPicture;
			public TextView textViewPercentage;
			public TextView textViewApportionType;
			public TextView textViewFriendName;
			public HyjNumericView textViewAmount;
			public ApportionItem<?> apportionItem;
			public ViewHolder(){}
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View iv;
			ViewHolder vh;
			final MoneyApportionField self = (MoneyApportionField) parent;
			if (convertView != null) {
				iv = convertView;
				vh = (ViewHolder) convertView.getTag();
			} else {
				iv = inflater.inflate(R.layout.money_listitem_moneyapportion, null);
				vh = new ViewHolder();
				vh.imageViewPicture = (HyjImageView) iv.findViewById(R.id.moneyApportionListItem_picture);
				vh.textViewAmount = (HyjNumericView) iv.findViewById(R.id.moneyApportionListItem_amount);
				vh.textViewPercentage = (TextView) iv.findViewById(R.id.moneyApportionListItem_percentage);
				vh.textViewFriendName = (TextView) iv.findViewById(R.id.moneyApportionListItem_friendName);
				vh.textViewApportionType = (TextView) iv.findViewById(R.id.moneyApportionListItem_apportionType);
				iv.setTag(vh);
					
//				float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, self.r.getDisplayMetrics());
//				iv.setLayoutParams(new LayoutParams((int)px, (int)px));
				iv.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						final ApportionItem<MoneyApportion> apportionItem = (ApportionItem<MoneyApportion>) ((ViewHolder)v.getTag()).apportionItem;
						if(apportionItem.getState() == ApportionItem.DELETED){
							apportionItem.undelete();
							self.setTotalAmount(null);
						} else {
							final HyjActivity activity = (HyjActivity) getContext();
							
							activity.mDialogCallback = new HyjActivity.DialogCallbackListener() {
								@Override
								public void doPositiveClick(Object bundle) {
//									if(bundle != null){
									Bundle b = (Bundle)bundle;
							    	final String apportionType = b.getString("apportionType");
							    	Double apportionAmount = b.getDouble("apportionAmount");
									apportionItem.setAmount(apportionAmount);
									apportionItem.setApportionType(apportionType);
									self.setTotalAmount(null);

//									} else {
//										MoneyApportionEditDialogFragment f = (MoneyApportionEditDialogFragment)activity.mDialogFragment;
//										apportionItem.setApportionType(f.getApportionType());
//										self.setTotalAmount(null);
//										f.setApportionAmount(apportionItem.getAmount());
//									}
								}
								@Override
								public void doNegativeClick() {
									if(apportionItem.getState() == ApportionItem.NEW){
										self.mImageGridAdapter.remove(apportionItem);
									} else if(apportionItem.getState() != ApportionItem.DELETED){
										apportionItem.delete();
									} 
									self.setTotalAmount(null);
								}
							};
							
							activity.mDialogFragment = MoneyApportionEditDialogFragment.newInstance(apportionItem.getAmount(), apportionItem.getApportionType());
							activity.mDialogFragment.show(activity.getSupportFragmentManager(), "dialog");
						}
					}
				});
			}
			if(vh.apportionItem != getItem(position)){
				vh.apportionItem = getItem(position);
				
				if(vh.apportionItem.getApportion().getFriendUser() != null){
					vh.imageViewPicture.setImage(vh.apportionItem.getApportion().getFriendUser().getPictureId());
				} else {
					vh.imageViewPicture.setImage((Picture)null);
				}
//				if(vh.apportionItem.getFriend() != null){
//					vh.textViewFriendName.setText(vh.apportionItem.getFriend().getDisplayName());
//				} else {
//					vh.textViewFriendName.setText(vh.apportionItem.getApportion().getFriendUser().getDisplayName());
//				}
				vh.textViewFriendName.setText(vh.apportionItem.getFriendDisplayName());
			}
			
			if(vh.apportionItem.getState() == ApportionItem.DELETED){
				vh.textViewAmount.setPrefix(R.string.moneyListItem_apportion_to_be_removed);
				vh.textViewAmount.setNumber(null);
				vh.textViewAmount.setTextColor(Color.parseColor("#FF0000"));
				vh.textViewPercentage.setPaintFlags(vh.textViewPercentage.getPaintFlags()
						| Paint.STRIKE_THRU_TEXT_FLAG);
				vh.textViewFriendName.setPaintFlags(vh.textViewFriendName.getPaintFlags()
						| Paint.STRIKE_THRU_TEXT_FLAG);
				vh.textViewApportionType.setPaintFlags(vh.textViewApportionType.getPaintFlags()
						| Paint.STRIKE_THRU_TEXT_FLAG);
			} else {
				vh.textViewAmount.setPrefix(null);
				vh.textViewAmount.setNumber(vh.apportionItem.getAmount());
				vh.textViewAmount.setTextColor(Color.parseColor("#000000"));
				vh.textViewPercentage.setPaintFlags(vh.textViewPercentage.getPaintFlags()
						& (~Paint.STRIKE_THRU_TEXT_FLAG));
				vh.textViewFriendName.setPaintFlags(vh.textViewFriendName.getPaintFlags()
						& (~Paint.STRIKE_THRU_TEXT_FLAG));
				vh.textViewApportionType.setPaintFlags(vh.textViewApportionType.getPaintFlags()
						& (~Paint.STRIKE_THRU_TEXT_FLAG));
			}
			
			vh.textViewPercentage.setText(self.getResources().getString(R.string.moneyListItem_apportion_share) + vh.apportionItem.getSharePercentage() + "%");

			if(vh.apportionItem.getApportionType().equalsIgnoreCase("Average")){
				vh.textViewApportionType.setText(R.string.moneyListItem_apportion_average_apport);
			} else if(vh.apportionItem.getApportionType().equalsIgnoreCase("Fixed")){
				vh.textViewApportionType.setText(R.string.moneyListItem_apportion_fixed_apport);
			} else {
				vh.textViewApportionType.setText(R.string.moneyListItem_apportion_share_apport);
			}
			return iv;
		}

	}
	
	public static class ApportionItem<T extends MoneyApportion> {
		public static final int UNCHANGED = 0;
		public static final int NEW = 1;
		public static final int DELETED = 2;
		public static final int CHANGED = 3;
		
		private int mState = UNCHANGED;
		private T mApportion;
		private String mProjectId;
		private ProjectShareAuthorization mProjectShareAuthorization = null;
		private Friend mFriend = null;
		private Double mAmount;
		private String mApportionType;
//		
//		ApportionItem(T apportion, String projectId){
//			mApportion = apportion;
//			mProjectId = projectId;
//		}
//		
		ApportionItem(T apportion, String projectId, int state){
			mApportion = apportion;
			mState = state;
			mProjectId = projectId;
			mAmount = apportion.getAmount();
			mApportionType = apportion.getApportionType();
		}
		
		public double getSharePercentage() {
			if(this.getProjectShareAuthorization() != null){
				this.getProjectShareAuthorization().getSharePercentage();
			} 
			return 0.0;
		}

		public void changeProject(String projectId){
			mProjectId = projectId;
			mProjectShareAuthorization = null;
		}
		
		public ProjectShareAuthorization getProjectShareAuthorization(){
			if(mProjectShareAuthorization == null) {
				mProjectShareAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=? AND state=?", 
					mProjectId, mApportion.getFriendUserId(), "Accept").executeSingle();
			} 
			return mProjectShareAuthorization;
		}
		
		public void setAmount(Double amount){
			mAmount = HyjUtil.toFixed2(amount);
		}
		
		public Double getAmount(){
			return mAmount;
		}
		
		public Friend getFriend(){
			if(mFriend == null){
				mFriend = new Select().from(Friend.class).where("friendUserId=?",mApportion.getFriendUserId()).executeSingle();
			}
			return mFriend;
		}
		
		public String getFriendDisplayName(){
			if(this.getFriend() != null){
				return this.getFriend().getDisplayName();
			} else {
				User user = HyjModel.getModel(User.class, mApportion.getFriendUserId());
				if(user != null){
					return user.getDisplayName();
				}
			}
			return "NO NAME";
		}
		
//		public void setState(int state){
//			mState = state;
//		}
		
		public void delete(){
			mState = DELETED;
		}
		
		public void undelete(){
			mState = UNCHANGED;
		}
		
		public int getState(){
			if(mState == UNCHANGED){
				if(mApportion.getAmount().compareTo(mAmount) != 0
						|| !mApportion.getApportionType().equalsIgnoreCase(mApportionType)){
					return CHANGED;
				}
			}
			return mState;
		}

		public void setApportionType(String apportionType){
			mApportionType = apportionType;
		}
		
		public String getApportionType(){
			return mApportionType;
		}
		
		public T getApportion() {
			return mApportion;
		}
		
		public void saveToCopy(MoneyApportion apportion){
			apportion.setAmount(mAmount);
			apportion.setApportionType(mApportionType);
		}
		
	}

	
}
