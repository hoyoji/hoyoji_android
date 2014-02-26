package com.hoyoji.hoyoji.money;

import java.util.List;

import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.view.HyjImageView;
import com.hoyoji.hoyoji.R;
import com.hoyoji.hoyoji.models.Friend;
import com.hoyoji.hoyoji.models.MoneyApportion;
import com.hoyoji.hoyoji.models.Project;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Interpolator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class MoneyApportionField extends GridView {
	private ImageGridAdapter mImageGridAdapter;
	private	Resources r = getResources();
	
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
	
	public void setApportions(List<? extends MoneyApportion> apportions, String projectId){
		//List<PictureItem> pis = new ArrayList<PictureItem>();
		for(int i=0; i < apportions.size(); i++){
			ApportionItem<MoneyApportion> pi = new ApportionItem<MoneyApportion>(apportions.get(i), projectId, ApportionItem.UNCHANGED);
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
	
	public void setApportionAmount(Double totalAmount){
		double fixedTotal = 0.0;
		double averageAmount = 0.0;
		int numOfAverage = 0;
		if(totalAmount == null){
			totalAmount = 0.0;
		}
		for(int i = 0; i < mImageGridAdapter.getCount(); i++){
			ApportionItem<MoneyApportion> api = (ApportionItem<MoneyApportion>) mImageGridAdapter.getItem(i);
			if(api.getApportionType().equalsIgnoreCase("Average")){
				numOfAverage++;
			} else {
				Double shareAmount = totalAmount * api.getProjectShareAuthorization().getSharePercentage();
				api.setAmount(shareAmount);
				fixedTotal+=shareAmount;
			}
		}
		averageAmount = (totalAmount - fixedTotal) / numOfAverage;
		for(int i = 0; i < mImageGridAdapter.getCount(); i++){
			ApportionItem<MoneyApportion> api = (ApportionItem<MoneyApportion>) mImageGridAdapter.getItem(i);
			if(api.getApportionType().equalsIgnoreCase("Average")){
				api.setAmount(averageAmount);
			}
		}
		mImageGridAdapter.notifyDataSetChanged();
	}
	
	public void setProjectApportions(Project project){
//		List<ProjectShareAuthorization> projectShareAuthorizations = project.getProjectShareAuthorizations();
//		for(int i=0; i < apportions.size(); i++){
//			ApportionItem<T> pi = new ApportionItem<T>(apportions.get(i));
//			mImageGridAdapter.add(pi);
//		}
	}
	
	public void setError(String errMsg){
		
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
			public TextView textViewAmount;
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
				iv = inflater.inflate(R.layout.money_listitem_money_apportion, null);
				vh = new ViewHolder();
				vh.imageViewPicture = (HyjImageView) iv.findViewById(R.id.moneyApportionListItem_picture);
				vh.textViewAmount = (TextView) iv.findViewById(R.id.moneyApportionListItem_amount);
				vh.textViewPercentage = (TextView) iv.findViewById(R.id.moneyApportionListItem_percentage);
				vh.textViewFriendName = (TextView) iv.findViewById(R.id.moneyApportionListItem_friendName);
				vh.textViewApportionType = (TextView) iv.findViewById(R.id.moneyApportionListItem_apportionType);
				iv.setTag(vh);
					
//				float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, self.r.getDisplayMetrics());
//				iv.setLayoutParams(new LayoutParams((int)px, (int)px));
				iv.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						ApportionItem<MoneyApportion> apportionItem = (ApportionItem<MoneyApportion>) ((ViewHolder)v.getTag()).apportionItem;
						HyjUtil.displayToast("Edit Apportion " + apportionItem.getApportion().getId());
					}
				});
			}
			vh.apportionItem = getItem(position);
			MoneyApportion apportion = vh.apportionItem.getApportion();
			vh.imageViewPicture.setImage(apportion.getFriendUser().getPictureId());
			
			if(vh.apportionItem.getFriend() != null){
				vh.textViewFriendName.setText(vh.apportionItem.getFriend().getDisplayName());
			} else {
				vh.textViewFriendName.setText(apportion.getFriendUser().getDisplayName());
			}
			vh.textViewAmount.setText(vh.apportionItem.getAmount().toString());
			
			vh.textViewPercentage.setText("占股"+vh.apportionItem.getProjectShareAuthorization().getSharePercentage() + "%");
			
			if(apportion.getApportionType().equalsIgnoreCase("Average")){
				vh.textViewApportionType.setText("平均分摊");
			} else {
				vh.textViewApportionType.setText("固定分摊");
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
		
		public ProjectShareAuthorization getProjectShareAuthorization(){
			if(mProjectShareAuthorization == null) {
				mProjectShareAuthorization = new Select().from(ProjectShareAuthorization.class).where("projectId=? AND friendUserId=?", 
					mProjectId, mApportion.getFriendUserId()).executeSingle();
			} 
			return mProjectShareAuthorization;
		}
		
		public void setAmount(Double amount){
			mAmount = amount;
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
//		
//		public void setState(int state){
//			mState = state;
//		}
		
		public int getState(){
			if(mState == UNCHANGED){
				if(mApportion.getAmount() != mAmount
						|| mApportion.getApportionType().equalsIgnoreCase(mApportionType)){
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
