package com.app.mlistviewcomponent;

import android.view.View;
import android.widget.LinearLayout;
import android.content.Context;

//ListHeader��ListFooter��Ӧ�ĳ����ࡣ���������ģ���Ҫ����
public abstract class SVList extends LinearLayout {
	protected final static int STATE_NORMAL = 0;
	protected final static int STATE_NORMALEXTEND = 1;
	protected final static int STATE_REFRESH = 2;
	protected int currentState = STATE_NORMAL;

	protected View contenter;
	protected boolean isReady = false; // ��ʶ�Ƿ�׼���ø��¡�isReady��������߶����

	protected SVList(Context context) {
		super(context);
	}

	public abstract void setVisibleHeight(int height);

	public abstract void showAtNormal();

	public abstract void showAtNormalExtend();

	public abstract void showAtRefresh();

	public abstract boolean isGetReady();

	public abstract void reset(); // �߶Ȳ����ã���Ȼ���ⲿ�����߿���
}
