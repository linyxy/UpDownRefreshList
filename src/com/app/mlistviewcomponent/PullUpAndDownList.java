package com.app.mlistviewcomponent;

import android.widget.ListView;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.Scroller;
import android.view.animation.LinearInterpolator;
import android.os.Handler;

import java.lang.Runnable;

import com.example.listviewtest.R;

public class PullUpAndDownList extends ListView {
	private Context mContext;

	/*
	 * ������Scroller��Ŀ�����ڣ����б�����٣�����2���
	 * ListHeader��ListFooterͬʱ�ɼ�������£��������ƶ���ָ����ListFooter�߶ȣ�
	 * �������ƶ���ָ��ListHeader�ɼ����� �ﵽListHeader��ListFooterͬʱ�ص���Ч��
	 */
	private ListHeader mHeader;
	private ListFooter mFooter;

	private float lastY = -1;
	private final float OFFSET_RADIO = 1.8f;
	private Scroller mScroller_Head; // ���ڿ���ListHeader�Ļص�
	private Scroller mScroller_Foot; // ���ڿ���ListFooter�Ļص�
	private boolean isMoved = false; // �����ж��ڴ����¼�ACTION_UP֮ǰ�Ƿ���й�ACTION_MOVE�Ĳ�����
										// ����У����ж�ListHeader��ListFooter����Ӧ�������û������������;

	public interface OnDataRequestListener {
		public void onLoadMore();

		public void onRefresh();
	}

	private OnDataRequestListener listener;

	private int header_handler_post_delayed;
	private int footer_handler_post_delayed;
	private int header_scroll_duration;
	private int footer_scroll_duration;

	{
		Resources r = getResources();
		header_handler_post_delayed = r
				.getInteger(R.integer.header_handler_post_delayed);
		footer_handler_post_delayed = r
				.getInteger(R.integer.footer_handler_post_delayed);
		header_scroll_duration = r.getInteger(R.integer.header_scroll_duration);
		footer_scroll_duration = r.getInteger(R.integer.footer_scroll_duration);
	}

	public PullUpAndDownList(Context context) {
		super(context);
		init(context);
	}

	public PullUpAndDownList(Context context, AttributeSet set) {
		super(context, set);
		init(context);
	}

	private void init(Context context) {
		if (context == null) {
			throw new NullPointerException(this.getClass().getName()
					+ "��ʼ�������Conetxt��������Ϊnull!");
		}
		mContext = context;
		initHeaderAndFooter();
	}

	private void initHeaderAndFooter() {
		mHeader = new ListHeader(mContext);
		mFooter = new ListFooter(mContext, this);
		mFooter.setOnClickListener(new View.OnClickListener() {
			private boolean canLoad = true;

			@Override
			public void onClick(View source) {
				if (!canLoad) {
					return;
				}
				canLoad = false;
				mFooter.directShowRefresh();
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						if (listener != null) {
							listener.onLoadMore();
						}
						mFooter.reset();
						canLoad = true;
					}
				}, footer_handler_post_delayed);
			}
		});
	}

	public void setOnDataRequestListener(OnDataRequestListener listener) {
		this.listener = listener;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		addHeaderView(mHeader);
		addFooterView(mFooter);

		super.setAdapter(adapter);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			lastY = event.getRawY();
			break;

		case MotionEvent.ACTION_MOVE:
			isMoved = true;

			int hasMoved = (int) ((event.getRawY() - lastY) / OFFSET_RADIO);

			if (getFirstVisiblePosition() == 0
					&& (mHeader.getHeight() > 0 || hasMoved > 0)) {
				changeHeaderAtMove(hasMoved);

				if (mHeader.getHeight() > 0) {
					setSelection(0);
				}
				// ListView.setSelection(int position)��������Ҫ��
				// ������ֹ��Ӧposition���б���������ϻ������ƶ���
				// ���û�м�����䷽�����ᷢ��ListHeader��ListFooter������ָ�ƶ����ӻ���ٸ߶ȵ�ͬʱ����Ӧ�����ϻ������ƶ����ƶ������˲��ָ߶ȵļӼ��������¸߶�û�дﵽԤ�ڵı仯��
			}
			// -------------------------------

			if (getLastVisiblePosition() == getCount() - 1
					&& mHeader.getHeight() <= 0) {
				changeFooterAtMove(hasMoved);

				if (mFooter.getHeight() > mFooter.SELF_HEIGHT && hasMoved > 0) {
					setSelection(getCount() - 1); // ListFooter����С�߶ȹ涨ΪmFooter.SELF_HEIGHT����Ϊ0
				}
			}
			// -------------------------------

			lastY = event.getRawY();
			break;

		case MotionEvent.ACTION_UP:
			if (!isMoved) {
				break;
			}

			if (changeHeaderAtUp() && listener != null) {
				headerStartRefresh();
			}
			if (changeFooterAtUp() && listener != null) {
				footerStartLoadMore();
			}

			isMoved = false;
			break;
		}

		return super.onTouchEvent(event);
	}

	private void changeHeaderAtMove(int hasMoved) {
		mHeader.setVisibleHeight(mHeader.getHeight() + hasMoved);
		if (mHeader.getHeight() > 0
				&& getLastVisiblePosition() == getCount() - 1) {
			mFooter.dontReady(); // ���ListHeader��ListFooterͬʱ�ɼ��������б�����ٵ�����£��������ƶ���ָ����ListFooter�߶ȣ�
									// �������ƶ���ָ��ListHeader�ɼ�����
									// ��֪ͨmFooter����ΪshowAtNormal()��ʾ�����ֳ�ʼ״̬���߶Ȳ����ã���Ȼ���ⲿ�����߿��ƣ���
									// ���ҽ�mFooter�ڲ���isReady��ʶΪfalse����ζ�Ų����и��µ�׼��
		}

		if (mHeader.getHeight() <= mHeader.SELF_HEIGHT) {
			mHeader.showAtNormal();
		} else {
			mHeader.showAtNormalExtend();
		}
	}

	private void changeFooterAtMove(int hasMoved) {
		mFooter.setVisibleHeight(mFooter.getHeight() + (-hasMoved));

		if (mFooter.getHeight() <= mFooter.SELF_HEIGHT) {
			mFooter.showAtNormal();
		} else {
			mFooter.showAtNormalExtend();
		}
	}

	private boolean changeHeaderAtUp() {
		int height = mHeader.getHeight();
		if (height <= 0) {
			return false;
		}
		boolean isReady = mHeader.isGetReady();
		if (isReady) {
			mHeader.showAtRefresh();
		}
		// -------------------------
		if (height < mHeader.SELF_HEIGHT) {
			headerStartScroll(0, height, 0, -height);
		} else if (height > mHeader.SELF_HEIGHT) {
			// ����߶ȴ���mHeader.SELF_HEIGHT�����Ȼص���mHeader.SELF_HEIGHT���������Ƿ���Ҫ�������ݵ�boolean��ʶ��true��ʾ���£�false��ʾ�����£���
			// ��������������Ӧҵ����֮����ListHeader�ٴλص������߶�Ϊ0��״̬
			headerStartScroll(0, height, 0, -(height - mHeader.SELF_HEIGHT));
		}
		return isReady;
	}

	private boolean changeFooterAtUp() {
		if (getLastVisiblePosition() != getCount() - 1) {
			return false;
		}

		boolean isReady = mFooter.isGetReady();
		if (isReady) {
			mFooter.showAtRefresh();
		}
		int height = mFooter.getHeight();
		if (height > mFooter.SELF_HEIGHT) {
			footerStartScroll(0, height, 0, -(height - mFooter.SELF_HEIGHT));
		}
		return isReady;
	}

	private void headerStartRefresh() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (listener != null) {
					listener.onRefresh();
				}
				headerStartScroll(0, mHeader.getHeight(), 0,
						-mHeader.getHeight()); // �ص����߶�Ϊ0
				mHeader.reset();
			}
		}, header_handler_post_delayed);
	}

	private void footerStartLoadMore() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (listener != null) {
					listener.onLoadMore();
				}
				footerStartScroll(0, mFooter.getHeight(), 0,
						-mFooter.getHeight());
				mFooter.reset();
			}
		}, footer_handler_post_delayed);
	}

	private void headerStartScroll(int startX, int startY, int dx, int dy) {
		mScroller_Head = new Scroller(mContext, new LinearInterpolator());
		mScroller_Head.startScroll(startX, startY, dx, dy,
				header_scroll_duration); // �ĵ�˵deltaYΪ����ʱ�����ƶ������˶δ���ȴ�Ǹ���ʱ�����ƶ�
		// -->����

		invalidate();
	}

	private void footerStartScroll(int startX, int startY, int dx, int dy) {
		mScroller_Foot = new Scroller(mContext, new LinearInterpolator());
		mScroller_Foot.startScroll(startX, startY, dx, dy,
				footer_scroll_duration);
		invalidate();
	}

	// Ϊ�����ڿ��ƻ������ƣ�Android����ṩ�� computeScroll()����ȥ����������̡��ڻ���Viewʱ������draw()���̵��ø�
	// ��������ˣ� �����ʹ��Scrollerʵ�������ǾͿ��Ի�õ�ǰӦ�õ�ƫ�����꣬�ֶ�ʹView/ViewGroupƫ�����ô���
	// ������λ�ת�ԣ�http://www.cnblogs.com/wanqieddy/archive/2012/05/05/2484534.html

	// AbsListView������ʾ����δ����ʱ�ͻᴥ�����¼�������ж�Scroller�Ƿ�Ϊnull�ͺܱ�Ҫ����ֹListView����ʱ����computeScroll()-->Scroller.computeScrollOffset���׳���ָ���쳣��
	@Override
	public void computeScroll() {
		if (mScroller_Head != null) {
			if (mScroller_Head.computeScrollOffset()) {
				// Ӧ��ʹ�ø߶�������ֵ��̬���ٵķ��������ʹ��ScrollTo�ķ������б����ƶ����������ָ���ĳһ�ʵ����ȴ������ĳ�����ĸ����ã�ԭ������������ListView�ڲ�ά�����б�λ��״̬��ȻΪ����ǰ��״̬��
				mHeader.setVisibleHeight(mScroller_Head.getCurrY());

				// ������ø÷���������һ���ܿ�������Ч��
				invalidate();
			} else {
				mScroller_Head = null; // ����Scrollerռ�õ��ڴ档��ΪScrollʵ��ʵ��ҵ������֮�󣬲�û�м��������ڴ�ı�Ҫ
			}
		}
		// ------------------------------------
		if (mScroller_Foot != null) {
			if (mScroller_Foot.computeScrollOffset()) {
				mFooter.setVisibleHeight(mScroller_Foot.getCurrY());
				invalidate();
			} else {
				mScroller_Foot = null;
			}
		}
		super.computeScroll();
	}
}