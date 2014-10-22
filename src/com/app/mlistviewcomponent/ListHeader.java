package com.app.mlistviewcomponent;

import com.example.listviewtest.R;

import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;
import android.content.res.Resources;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.view.animation.RotateAnimation;
import android.widget.ProgressBar;

public class ListHeader extends SVList {
	public final int SELF_HEIGHT;
	public final int MAX_HEIGHT;

	private String lastRefreshTime;
	private boolean isLastRefreshTimeShowed;
	private int rotateDuration;

	{
		Resources r = getResources();
		SELF_HEIGHT = (int) r.getDimension(R.dimen.list_header_self_height);
		MAX_HEIGHT = (int) r.getDimension(R.dimen.list_header_max_height);
		rotateDuration = r.getInteger(R.integer.header_rotate_duration);
	}

	public ListHeader(Context context) {
		super(context);
		if (context == null) {
			throw new NullPointerException(this.getClass().getName()
					+ "初始化传入的Conetxt参数不能为null!");
		}

		AbsListView.LayoutParams params = new AbsListView.LayoutParams(
				AbsListView.LayoutParams.MATCH_PARENT,
				AbsListView.LayoutParams.WRAP_CONTENT);
		setLayoutParams(params);
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 0); // 规定ListHeader初始高度为0
		contenter = LayoutInflater.from(context).inflate(R.layout.headerlayout,
				null);
		addView(contenter, params1);
	}

	@Override
	public void setVisibleHeight(int height) {
		if (height < 0) {
			height = 0;
		} else if (height > MAX_HEIGHT) {
			height = MAX_HEIGHT;
		}

		ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) contenter
				.getLayoutParams();
		params.height = height;
		contenter.setLayoutParams(params);

		if (getHeight() >= SELF_HEIGHT) {
			isReady = true;
		} else {
			isReady = false;
		}
	}

	@Override
	public void showAtNormal() {
		showAtNormal(false);
	}

	private void showAtNormal(boolean isReset) {
		if ((currentState == STATE_NORMAL) && isLastRefreshTimeShowed) {
			return;
		} else if ((currentState == STATE_NORMAL) && !isLastRefreshTimeShowed
				&& !isReset) {
			String t = lastRefreshTime != null ? lastRefreshTime : "无更新";
			TextView time = (TextView) contenter.findViewById(R.id.time);
			time.setText(getResources().getString(
					R.string.header_last_update_time)
					+ t);
			isLastRefreshTimeShowed = true;
			return;
		}
		currentState = STATE_NORMAL;

		((ProgressBar) findViewById(R.id.header_progress))
				.setVisibility(View.GONE);
		ImageView img = (ImageView) findViewById(R.id.header_arrow);
		img.setVisibility(View.VISIBLE);

		int x = img.getWidth() / 2;
		int y = img.getHeight() / 2;
		if (!isReset) {
			RotateAnimation animation = new RotateAnimation(-180, 0, x, y);
			animation.setDuration(rotateDuration);
			animation.setFillAfter(true);
			img.startAnimation(animation);
		} else {
			img.setImageResource(R.drawable.xlistview_arrow);
		}
	}

	@Override
	public void showAtNormalExtend() {
		if (currentState == STATE_NORMALEXTEND) {
			return;
		}
		currentState = STATE_NORMALEXTEND;

		((ProgressBar) findViewById(R.id.header_progress))
				.setVisibility(View.GONE);
		ImageView img = (ImageView) findViewById(R.id.header_arrow);
		img.setVisibility(View.VISIBLE);

		int x = img.getWidth() / 2;
		int y = img.getHeight() / 2;

		RotateAnimation animation = new RotateAnimation(0, -180, x, y);
		animation.setDuration(rotateDuration);
		animation.setFillAfter(true);
		img.startAnimation(animation);
	}

	@Override
	public void showAtRefresh() {
		if (currentState == STATE_REFRESH) {
			return;
		}

		Time t = new Time();
		t.setToNow();
		lastRefreshTime = t.hour + "时"
				+ (t.minute < 10 ? "0" + t.minute : t.minute) + "分"
				+ (t.second < 10 ? "0" + t.second : t.second) + "秒";

		currentState = STATE_REFRESH;

		ImageView img = (ImageView) findViewById(R.id.header_arrow);
		img.clearAnimation();// 如果没有加此句，则下方的img.setVisibility(View.GONE);将无效。
								// -->具体原因置疑
		img.setVisibility(View.GONE);

		((ProgressBar) findViewById(R.id.header_progress))
				.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean isGetReady() {
		return isReady;
	}

	@Override
	public void reset() {
		isReady = false;
		isLastRefreshTimeShowed = false;
		showAtNormal(true);
	}
}