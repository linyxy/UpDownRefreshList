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
	 * 用两个Scroller的目的在于，当列表项很少（比如2项），
	 * ListHeader和ListFooter同时可见的情况下（先往上移动手指增加ListFooter高度，
	 * 再往下移动手指让ListHeader可见）， 达到ListHeader和ListFooter同时回弹的效果
	 */
	private ListHeader mHeader;
	private ListFooter mFooter;

	private float lastY = -1;
	private final float OFFSET_RADIO = 1.8f;
	private Scroller mScroller_Head; // 用于控制ListHeader的回弹
	private Scroller mScroller_Foot; // 用于控制ListFooter的回弹
	private boolean isMoved = false; // 用于判断在触摸事件ACTION_UP之前是否进行过ACTION_MOVE的操作，
										// 如果有，进行对ListHeader或ListFooter的相应处理；如果没有则跳过处理;

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
					+ "初始化传入的Conetxt参数不能为null!");
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
				// ListView.setSelection(int position)方法很重要，
				// 用于阻止对应position的列表项继续往上或往下移动。
				// 如果没有加上这句方法，会发生ListHeader或ListFooter在随手指移动增加或减少高度的同时，相应地往上或往下移动（移动抵消了部分高度的加减），导致高度没有达到预期的变化。
			}
			// -------------------------------

			if (getLastVisiblePosition() == getCount() - 1
					&& mHeader.getHeight() <= 0) {
				changeFooterAtMove(hasMoved);

				if (mFooter.getHeight() > mFooter.SELF_HEIGHT && hasMoved > 0) {
					setSelection(getCount() - 1); // ListFooter的最小高度规定为mFooter.SELF_HEIGHT，不为0
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
			mFooter.dontReady(); // 如果ListHeader和ListFooter同时可见（如在列表项很少的情况下，先往上移动手指增加ListFooter高度，
									// 再往下移动手指让ListHeader可见），
									// 则通知mFooter重置为showAtNormal()显示的那种初始状态（高度不重置，依然由外部调用者控制），
									// 并且将mFooter内部的isReady标识为false，意味着不进行更新的准备
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
			// 如果高度大于mHeader.SELF_HEIGHT，则先回弹至mHeader.SELF_HEIGHT，并返回是否需要更新数据的boolean标识（true表示更新，false表示不更新），
			// 方法调用者在相应业务处理之后让ListHeader再次回弹，至高度为0的状态
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
						-mHeader.getHeight()); // 回弹至高度为0
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
				header_scroll_duration); // 文档说deltaY为正数时向上移动，而此段代码却是负数时向上移动
		// -->置疑

		invalidate();
	}

	private void footerStartScroll(int startX, int startY, int dx, int dy) {
		mScroller_Foot = new Scroller(mContext, new LinearInterpolator());
		mScroller_Foot.startScroll(startX, startY, dx, dy,
				footer_scroll_duration);
		invalidate();
	}

	// 为了易于控制滑屏控制，Android框架提供了 computeScroll()方法去控制这个流程。在绘制View时，会在draw()过程调用该
	// 方法。因此， 再配合使用Scroller实例，我们就可以获得当前应该的偏移坐标，手动使View/ViewGroup偏移至该处。
	// 以上这段话转自：http://www.cnblogs.com/wanqieddy/archive/2012/05/05/2484534.html

	// AbsListView初次显示并且未滚动时就会触发此事件，因此判断Scroller是否为null就很必要（防止ListView启动时触发computeScroll()-->Scroller.computeScrollOffset，抛出空指针异常）
	@Override
	public void computeScroll() {
		if (mScroller_Head != null) {
			if (mScroller_Head.computeScrollOffset()) {
				// 应该使用高度随坐标值动态减少的方法；如果使用ScrollTo的方法让列表项移动，会产生手指点击某一项，实际上却是另外某项被点击的副作用（原因不明，可能是ListView内部维护的列表位置状态依然为滑动前的状态）
				mHeader.setVisibleHeight(mScroller_Head.getCurrY());

				// 必须调用该方法，否则不一定能看到滚动效果
				invalidate();
			} else {
				mScroller_Head = null; // 回收Scroller占用的内存。因为Scroll实例实现业务流程之后，并没有继续存在内存的必要
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