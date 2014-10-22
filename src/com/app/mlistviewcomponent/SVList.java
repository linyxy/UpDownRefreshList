package com.app.mlistviewcomponent;

import android.view.View;
import android.widget.LinearLayout;
import android.content.Context;

//ListHeader和ListFooter对应的抽象类。类名随便起的，不要在意
public abstract class SVList extends LinearLayout {
	protected final static int STATE_NORMAL = 0;
	protected final static int STATE_NORMALEXTEND = 1;
	protected final static int STATE_REFRESH = 2;
	protected int currentState = STATE_NORMAL;

	protected View contenter;
	protected boolean isReady = false; // 标识是否准备好更新。isReady仅与自身高度相关

	protected SVList(Context context) {
		super(context);
	}

	public abstract void setVisibleHeight(int height);

	public abstract void showAtNormal();

	public abstract void showAtNormalExtend();

	public abstract void showAtRefresh();

	public abstract boolean isGetReady();

	public abstract void reset(); // 高度不重置，依然由外部调用者控制
}
