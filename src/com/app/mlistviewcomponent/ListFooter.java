package com.app.mlistviewcomponent;

import com.example.listviewtest.R;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ListFooter extends SVList {
	public final int SELF_HEIGHT;
	public final int MAX_HEIGHT;

	private String normal_text;
	private String normalextend_text;
	private String loading_text;

	private AbsListView listView;

	{
		Resources r = getResources();
		SELF_HEIGHT = (int) r.getDimension(R.dimen.list_footer_self_height);
		MAX_HEIGHT = (int) r.getDimension(R.dimen.list_footer_max_height);
		normal_text = r.getString(R.string.footer_normal);
		normalextend_text = r.getString(R.string.footer_normal_extend);
		loading_text = r.getString(R.string.footer_loading);
	}

	public ListFooter(Context context, AbsListView listView) {
		super(context);
		if (context == null || listView == null) {
			throw new NullPointerException(this.getClass().getName()
					+ "初始化传入的Conetxt参数和AbsListView参数不能为null!");
		}

		this.listView = listView;

		AbsListView.LayoutParams params = new AbsListView.LayoutParams(
				AbsListView.LayoutParams.MATCH_PARENT,
				AbsListView.LayoutParams.WRAP_CONTENT);
		setLayoutParams(params);
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, SELF_HEIGHT);
		contenter = LayoutInflater.from(context).inflate(R.layout.footerlayout,
				null);
		addView(contenter, params1);// 如果没有加params1参数，则contenter不会按照自己的布局去生成
	}

	@Override
	public void setVisibleHeight(int height) {
		if (height < SELF_HEIGHT) {
			height = SELF_HEIGHT;
		} else if (height > MAX_HEIGHT) {
			height = MAX_HEIGHT;
		}
		ViewGroup.LayoutParams params = contenter.getLayoutParams();
		params.height = height;
		contenter.setLayoutParams(params);

		// 子控件的绝对坐标以父控件的左上角为原点(0,0)；
		// 此知识点可参考： http://blog.csdn.net/androiddevelop/article/details/8373782
		// listView.getHeight()-this.getTop()的值表示ListFooter的顶部与父控件ListView低部的间隔
		// （listView.getHeight()相当于子控件所处的最大的可视Y坐标；
		// this.getTop()相当于子控件ListFooter顶部的当前Y坐标，即以父控件listView顶部到ListFooter顶部（注意listView左上角为原点）的距离）

		// 如果间隔>=ListFooter默认高度，才允许增减高度或更改isReady的状态
		if (listView.getHeight() - this.getTop() >= SELF_HEIGHT) {
			isReady = true;
		} else {
			isReady = false;
		}
	}

	@Override
	public void showAtNormal() {
		if (currentState == STATE_NORMAL) {
			return;
		}
		currentState = STATE_NORMAL;

		((ProgressBar) findViewById(R.id.footer_progress))
				.setVisibility(View.GONE);
		((TextView) findViewById(R.id.content)).setText(normal_text);
	}

	@Override
	public void showAtNormalExtend() {
		if (currentState == STATE_NORMALEXTEND) {
			return;
		}
		currentState = STATE_NORMALEXTEND;

		((ProgressBar) findViewById(R.id.footer_progress))
				.setVisibility(View.GONE);
		((TextView) findViewById(R.id.content)).setText(normalextend_text);
	}

	@Override
	public void showAtRefresh() {
		if (!isReady || currentState == STATE_REFRESH) {
			return;
		}
		currentState = STATE_REFRESH;

		((TextView) findViewById(R.id.content)).setText(loading_text);
		((ProgressBar) findViewById(R.id.footer_progress))
				.setVisibility(View.VISIBLE);
	}

	public void dontReady() {
		reset();
	}

	@Override
	public boolean isGetReady() {
		return isReady;
	}

	@Override
	public void reset() {
		isReady = false;
		showAtNormal();
	}

	public void directShowRefresh() {
		isReady = true;
		showAtRefresh();
	}
}