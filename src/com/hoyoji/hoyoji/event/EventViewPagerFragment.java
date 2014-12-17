package com.hoyoji.hoyoji.event;

import java.util.Date;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.hoyoji.android.hyjframework.HyjApplication;
import com.hoyoji.android.hyjframework.HyjAsyncTaskCallbacks;
import com.hoyoji.android.hyjframework.HyjModel;
import com.hoyoji.android.hyjframework.HyjUtil;
import com.hoyoji.android.hyjframework.activity.HyjActivity;
import com.hoyoji.android.hyjframework.fragment.HyjUserFragment;
import com.hoyoji.android.hyjframework.server.HyjHttpPostAsyncTask;
import com.hoyoji.android.hyjframework.view.HyjTabStrip;
import com.hoyoji.android.hyjframework.view.HyjViewPager;
import com.hoyoji.android.hyjframework.view.HyjTabStrip.OnTabSelectedListener;
import com.hoyoji.android.hyjframework.view.HyjViewPager.OnOverScrollListener;
import com.hoyoji.hoyoji_android.R;
import com.hoyoji.hoyoji.message.EventMessageFormFragment;
import com.hoyoji.hoyoji.models.Event;
import com.hoyoji.hoyoji.models.EventMember;
import com.hoyoji.hoyoji.models.ProjectShareAuthorization;
import com.hoyoji.hoyoji.money.MoneySearchListFragment;

public class EventViewPagerFragment extends HyjUserFragment {
	
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	public ViewPager mViewPager;

	protected boolean isClosingActivity = false;

	private HyjTabStrip mTabStrip;

	private DisplayMetrics mDisplayMetrics;

	private Button mBtnSignUpEvent;

	
	@Override
	public Integer useContentView() {
		return R.layout.event_viewpager_tabstrip;
	}
	
	@Override
	public void onInitViewData() {
		mDisplayMetrics = getResources().getDisplayMetrics();
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) getView().findViewById(R.id.viewpager);
		//.setBackgroundColor(Color.LTGRAY);
//		mViewPager.setPageTransformer(true, new DepthPageTransformer());
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(2);
		((HyjViewPager)mViewPager).setOnOverScrollListener(new OnOverScrollListener(){
			@Override
			public void onOverScroll(float mOverscroll) {
//				Log.i("mOverscroll", "" + mOverscroll);
				if(mOverscroll / mDisplayMetrics.density < -150){
					if(!isClosingActivity ){
						isClosingActivity = true;
						((HyjViewPager)mViewPager).setStopBounceBack(true);
						getActivity().finish();
					}
				}
			}
		});
		
		mTabStrip = (HyjTabStrip) getView().findViewById(R.id.tabstrip);
		mTabStrip.initTabLine(mSectionsPagerAdapter.getCount());
		for(int i = 0; i < mSectionsPagerAdapter.getCount(); i ++){
			CharSequence title = mSectionsPagerAdapter.getPageTitle(i);
			mTabStrip.addTab(title.toString());
		}
		
		mViewPager.setOnPageChangeListener(new OnPageChangeListener()
		{
			@Override
			public void onPageSelected(int position) {
				((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("活动"+mSectionsPagerAdapter.getPageTitle(position));
				mTabStrip.setTabSelected(position);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				mTabStrip.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		mTabStrip.setOnTabSelectedListener(new OnTabSelectedListener(){
			@Override
			public void onTabSelected(int tag) {
				mViewPager.setCurrentItem(tag);
			}
		});
		mViewPager.setCurrentItem(1);
		
		mBtnSignUpEvent = (Button)getView().findViewById(R.id.eventviewpager_signup_event);
		
		String subTitle = null;
		long model_id = this.getActivity().getIntent().getLongExtra("MODEL_ID", -1);
		if(model_id != -1){
			final Event event = HyjModel.load(Event.class, model_id);
			if(event != null){
				subTitle = event.getName();
				
				final EventMember eventMember = new Select().from(EventMember.class).where("eventId = ? AND friendUserId = ?", event.getId(), HyjApplication.getInstance().getCurrentUser().getId()).executeSingle();
				if(eventMember == null || eventMember.getState().equalsIgnoreCase("UnSignUp")){
					mBtnSignUpEvent.setVisibility(View.VISIBLE);
					mBtnSignUpEvent.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							if(event.getProject().getOwnerUserId().equals(HyjApplication.getInstance().getCurrentUser().getId())){
								eventMember.setState("SignUp");
								eventMember.save();
								mBtnSignUpEvent.setVisibility(View.GONE);
								mViewPager.setPadding(mTabStrip.getPaddingLeft(), (int) (35*mDisplayMetrics.density), mViewPager.getPaddingRight(), mViewPager.getPaddingBottom());
								HyjUtil.displayToast("报名成功");
							} else {
								ProjectShareAuthorization psa = new Select().from(ProjectShareAuthorization.class).where("friendUserId = ? AND state <> ?", HyjApplication.getInstance().getCurrentUser().getId(), "Delete").executeSingle();
								
								sendAcceptMessageToServer(event, eventMember, psa);
								
							}
							
//							if(eventMember == null){
//								
//							} else {
//								
//							}
//							
//							
//							
//							mBtnSignUpEvent.setVisibility(View.GONE);
//							mViewPager.setPadding(mTabStrip.getPaddingLeft(), (int) (35*mDisplayMetrics.density), mViewPager.getPaddingRight(), mViewPager.getPaddingBottom());
//							HyjUtil.displayToast("报名成功");
						}
					});
					mViewPager.setPadding(mTabStrip.getPaddingLeft(), (int) (103*mDisplayMetrics.density), mViewPager.getPaddingRight(), mViewPager.getPaddingBottom());
				} else if(eventMember != null && eventMember.getState().equalsIgnoreCase("SignUp") && event.getStartDate() < (new Date()).getTime()){
					mBtnSignUpEvent.setVisibility(View.VISIBLE);
					mBtnSignUpEvent.setText("我要签到");
					mViewPager.setPadding(mTabStrip.getPaddingLeft(), (int) (103*mDisplayMetrics.density), mViewPager.getPaddingRight(), mViewPager.getPaddingBottom());
				}
				
			}
			if(subTitle != null){
				((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(subTitle);
			}
		}
	}
	
	private void sendAcceptMessageToServer(final Event event, EventMember em, ProjectShareAuthorization psa) {
		try {
			HyjAsyncTaskCallbacks serverCallbacks = new HyjAsyncTaskCallbacks() {
				@Override
				public void finishCallback(Object object) {
					loadEventMembers(object);
				}
	
				@Override
				public void errorCallback(Object object) {
					((HyjActivity) EventViewPagerFragment.this.getActivity()).dismissProgressDialog();
					JSONObject json = (JSONObject) object;
					HyjUtil.displayToast(json.optJSONObject("__summary").optString("msg"));
				}
			};
	
			JSONObject msg = new JSONObject();
			msg.put("__dataType", "Message");
			msg.put("id", UUID.randomUUID().toString());
			msg.put("toUserId", event.getOwnerUserId());
			msg.put("fromUserId", HyjApplication.getInstance().getCurrentUser().getId());
			msg.put("type", "Event.Member.SignUp");
			msg.put("messageState", "new");
			msg.put("messageTitle", "活动报名");
			msg.put("date", (new Date()).getTime());
			msg.put("detail", "用户"+ HyjApplication.getInstance().getCurrentUser().getDisplayName() + "报名参加活动: "+ event.getName());
			msg.put("messageBoxId", event.getOwnerUser().getMessageBoxId1());
			msg.put("ownerUserId", event.getOwnerUserId());
	
			JSONObject msgData = new JSONObject();
			if(psa != null) {
				msgData.put("projectShareAuthorizationId", psa.getId());
			}
			msgData.put("fromUserDisplayName", HyjApplication.getInstance().getCurrentUser().getDisplayName());
			msgData.put("projectIds", new JSONArray("[" + event.getProjectId()  + "]"));
			msgData.put("eventId", event.getId());
			if(em == null){
				msgData.put("eventMemberId", null);
			} else {
				msgData.put("eventMemberId", em.getId());
			}
			msg.put("messageData", msgData.toString());
	
			HyjHttpPostAsyncTask.newInstance(serverCallbacks,"[" + msg.toString() + "]", "eventMemberSignUp");
			((HyjActivity) this.getActivity()).displayProgressDialog(
							R.string.eventListFragment_title_acceptShare,
							R.string.eventListFragment_acceptShare_progress_adding);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	protected void loadEventMembers(Object object) {
		try {
			JSONArray jsonObjects = (JSONArray) object;
			ActiveAndroid.beginTransaction();
				for (int j = 0; j < jsonObjects.length(); j++) {
					if (jsonObjects.optJSONObject(j).optString("__dataType").equals("EventMember")) {
						String id = jsonObjects.optJSONObject(j).optString("id");
						EventMember newEventMember = HyjModel.getModel(EventMember.class, id);
						if(newEventMember == null){
							newEventMember = new EventMember();
						}
						newEventMember.loadFromJSON(jsonObjects.optJSONObject(j), true);
						newEventMember.save();
					}
				}

			ActiveAndroid.setTransactionSuccessful();
//			getActivity().finish();
			mBtnSignUpEvent.setVisibility(View.GONE);
			mViewPager.setPadding(mTabStrip.getPaddingLeft(), (int) (35*mDisplayMetrics.density), mViewPager.getPaddingRight(), mViewPager.getPaddingBottom());
			HyjUtil.displayToast("报名成功");
		} finally {
			ActiveAndroid.endTransaction();
		}
		((HyjActivity) EventViewPagerFragment.this.getActivity()).dismissProgressDialog();
	}
	
	
//	@Override
//	public boolean handleBackPressed() {
//		boolean backPressedHandled = false; //super.handleBackPressed();
////		if(mViewPager.getCurrentItem() > 0){
////			mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
////			backPressedHandled = true;
////		}
//		return backPressedHandled;
//	}
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public static class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch(position){
			case 0 :
				return new EventMemberListFragment();
			case 1 :
				return new MoneySearchListFragment();
			case 2:
				return new EventFormFragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch(position){
			case 0 :
				return "成员";
			case 1 :
				return "流水";
			case 2:
				return "资料";
			}
			return null;
		}
	}

}
