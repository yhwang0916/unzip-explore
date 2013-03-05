package com.fmw.unzip.service;

import com.adsmogo.adview.AdsMogoLayout;
import com.box.unzip.R;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

/***
 * 自定义拖拉ListView
 * 
 */
public class DragListView extends ListView implements OnScrollListener,
		OnClickListener {
	private Context context;
	// 下拉ListView枚举所有状态
	private enum DListViewState {
		LV_NORMAL, // 普通状态
		LV_PULL_REFRESH, // 下拉状态（为超过mHeadViewHeight）

	}
	
	// 点击加载更多枚举所有状态
	private enum DListViewLoadingMore {
		LV_NORMAL, // 普通状态
		LV_PULL_REFRESH, // 上拉状态（为超过mHeadViewHeight）
	}

	private View mHeadView, mFootView;// 头部headView

	private int mHeadViewWidth; // headView的宽（mHeadView）
	private int mHeadViewHeight;// headView的高（mHeadView）

	private int mFirstItemIndex = -1;// 当前视图能看到的第一个项的索引

	private int mLastItemIndex = -1;// 当前视图中是否是最后一项.

	// 用于保证startY的值在一个完整的touch事件中只被记录一次
	private boolean mIsRecord = false;// 针对下拉

	private boolean mIsRecord_B = false;// 针对上拉

	private int mStartY, mMoveY;// 按下是的y坐标,move时的y坐标

	private DListViewState mlistViewState = DListViewState.LV_NORMAL;// 拖拉状态.(自定义枚举)

	private DListViewLoadingMore loadingMoreState = DListViewLoadingMore.LV_NORMAL;// 加载更多默认状态.

	private final static int RATIO = 2;// 手势下拉距离比.

	private boolean isScroller = true;// 是否屏蔽ListView滑动。

	private MyAsynTask myAsynTask;// 任务
	private final static int DRAG_UP = 1, DRAG_DOWN = 2;

	public DragListView(Context context) {
		
		super(context, null);
		initDragListView(context);
		this.context = context;
	}

	public DragListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initDragListView(context);
	}

	/***
	 * 初始化ListView
	 */
	public void initDragListView(Context context) {

		initHeadView(context);// 初始化该head.

		initFooterView(context);// 初始化footer

		setOnScrollListener(this);// ListView滚动监听
	}

	/***
	 * 初始话头部HeadView
	 * 
	 * @param context
	 *            上下文
	 * @param time
	 *            上次更新时间
	 */
	public void initHeadView(Context context) {
		mHeadView = LayoutInflater.from(context).inflate(R.layout.listview_header2, null);
		//View mFooterView = LayoutInflater.from(context).inflate(R.layout.listview_header, null);
		measureView(mHeadView);
		// 获取宽和高
		mHeadViewHeight = mHeadView.getMeasuredHeight();
		
		addHeaderView(mHeadView, null, false);// 将初始好的ListView add进拖拽ListView
		//addFooterView(mFooterView, null, false);// 将初始好的ListView add进拖拽ListView
		//mFooterView.setPadding(0,0, 0, 0);
		mHeadViewHeight = 0;
		// 在这里我们要将此headView设置到顶部不显示位置.
		mHeadView.setPadding(0, -1 * mHeadViewHeight, 0, 0);

	}

	/***
	 * 初始化底部加载更多控件
	 */
	private void initFooterView(Context context) {
		
		mFootView = LayoutInflater.from(context).inflate(R.layout.listview_header2, null);
		LinearLayout container =(LinearLayout)(mFootView.findViewById(R.id.AdLinearLayout)); 
		//添加广告
		//new AdView(context,container).DisplayAd();
		// 构造方法，设置快速模式
		AdsMogoLayout adsMogoLayoutCode = new AdsMogoLayout((Activity) context,"2129b29b3a1840db9aaa2616ee54992b", false);
		container.addView(adsMogoLayoutCode);
		addFooterView(mFootView, null, false);// 将初始好的ListView add进拖拽ListView
		// 在这里我们要将此FooterView设置到底部不显示位置.
		mFootView.setPadding(0,0, 0,  0);
	}

	/***
	 * 作用：测量 headView的宽和高.
	 * 
	 * @param child
	 */
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	/***
	 * touch 事件监听
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		switch (ev.getAction()) {
		// 按下
		case MotionEvent.ACTION_DOWN:
			doActionDown_B(ev);
			doActionDown(ev);
			break;
		// 移动
		case MotionEvent.ACTION_MOVE:
				doActionMove_B(ev);
				doActionMove(ev);
			
			break;
		// 抬起
		case MotionEvent.ACTION_UP:
			doActionUp_B(ev);
			doActionUp(ev);
			break;
		default:
			break;
		}

		/***
		 * 如果是ListView本身的拉动，那么返回true，这样ListView不可以拖动.
		 * 如果不是ListView的拉动，那么调用父类方法，这样就可以上拉执行.
		 */
		if (isScroller) {
			return super.onTouchEvent(ev);
		} else {
			return true;
		}

	}

	/***
	 * 摁下操作
	 * 
	 * 作用：获取摁下是的y坐标
	 * 
	 * @param event
	 */
	void doActionDown(MotionEvent event) {
		// 如果是第一项且是一次touch
		if (mIsRecord == false && mFirstItemIndex == 0) {
			mStartY = (int) event.getY();
			mIsRecord = true;
		}
	}

	/***
	 * 摁下操作 底部
	 * 
	 * 作用：获取摁下是的y坐标
	 */
	void doActionDown_B(MotionEvent event) {
		// 如果是第一项且是一次touch
		if (mIsRecord_B == false && mLastItemIndex == getCount()) {
			mStartY = (int) event.getY();
			mIsRecord_B = true;
		}
	}

	/***
	 * 拖拽移动操作
	 * 
	 * @param event
	 */
	void doActionMove(MotionEvent event) {

		// 判断是否是第一项，若不是直接返回
		mMoveY = (int) event.getY();// 获取实时滑动y坐标

		// 检测是否是一次touch事件.
		if (mIsRecord == false && mFirstItemIndex == 0) {
			mStartY = (int) event.getY();
			mIsRecord = true;
		}
		// 直接返回说明不是第一项
		if (mIsRecord == false)
			return;

		// 向下啦headview移动距离为y移动的一半.（比较友好）
		int offset = (mMoveY - mStartY) / RATIO;

		switch (mlistViewState) {
		// 普通状态
		case LV_NORMAL: {
			// 说明下拉
			if (offset > 0) {
				// 设置headView的padding属性.
				mHeadView.setPadding(0, offset - mHeadViewHeight, 0, 0);
				mlistViewState = DListViewState.LV_PULL_REFRESH;// 下拉状态
			}
		}
			break;
		// 下拉状态
		case LV_PULL_REFRESH: {
			setSelection(0);// 时时保持在顶部.
			// 设置headView的padding属性.
			mHeadView.setPadding(0, offset - mHeadViewHeight, 0, 0);
			if (offset < 0) {
				/***
				 * 要明白为什么isScroller = false;
				 */
				isScroller = false;
				mlistViewState = DListViewState.LV_NORMAL;
			}
		}
			break;
		default:
			return;
		}
	}

	void doActionMove_B(MotionEvent event) {
		mMoveY = (int) event.getY();// 获取实时滑动y坐标
		// 检测是否是一次touch事件.(若mFirstItemIndex为0则要初始化mStartY)
		if (mIsRecord_B == false && mLastItemIndex == getCount()) {
			mStartY = (int) event.getY();
			mIsRecord_B = true;
		}
		// 直接返回说明不是最后一项
		if (mIsRecord_B == false)
			return;

		// 向下啦headview移动距离为y移动的一半.（比较友好）
		int offset = (mMoveY - mStartY) / RATIO;

		switch (loadingMoreState) {
		// 普通状态
		case LV_NORMAL: {
			// 说明上拉
			if (offset < 0) {
				int distance = Math.abs(offset);
				// 设置headView的padding属性.
				mFootView.setPadding(0, distance - mHeadViewHeight, 0, 0);
				loadingMoreState = DListViewLoadingMore.LV_PULL_REFRESH;// 下拉状态
			}
		}
			break;
		// 上拉状态
		case LV_PULL_REFRESH: {
			setSelection(getCount() - 1);// 时时保持最底部
			// 设置headView的padding属性.
			int distance = Math.abs(offset);
			mFootView.setPadding(0, 0, 0, distance - mHeadViewHeight);
			// 说明下滑
			if (offset > 0) {
				
				/***
				 * 要明白为什么isScroller = false;
				 */
				isScroller = false;
				loadingMoreState = DListViewLoadingMore.LV_NORMAL;
			}
		}
			break;
		default:
			return;
		}
	}

	
	/***
	 * 手势抬起操作
	 * 
	 * @param event
	 */
	public void doActionUp(MotionEvent event) {
		mIsRecord = false;// 此时的touch事件完毕，要关闭。
		mIsRecord_B = false; // 此时的touch事件完毕，要关闭。
		isScroller = true;// ListView可以Scrooler滑动.
		mlistViewState = DListViewState.LV_NORMAL;// 状态也回归最初状态

		// 执行相应动画.
		myAsynTask = new MyAsynTask();
		myAsynTask.execute(DRAG_UP);

	}

	private void doActionUp_B(MotionEvent event) {
		mIsRecord = false;// 此时的touch事件完毕，要关闭。
		isScroller = true;// ListView可以Scrooler滑动.

		loadingMoreState = DListViewLoadingMore.LV_NORMAL;// 状态也回归最初状态

		// 执行相应动画.
		myAsynTask = new MyAsynTask();
		myAsynTask.execute(DRAG_DOWN);
	}

	/***
	 * ListView 滑动监听
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		scrollState = 0;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		mFirstItemIndex = firstVisibleItem;
		mLastItemIndex = firstVisibleItem + visibleItemCount;

	}

	@Override
	public void onClick(View v) {

	}

	@Override
	public void setOnItemClickListener(OnItemClickListener listener) {
		// TODO Auto-generated method stub
		super.setOnItemClickListener(listener);
	}

	@Override
	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		// TODO Auto-generated method stub
		super.setOnItemLongClickListener(listener);
	}

	/***
	 * 用于产生动画
	 * 
	 * @author zhangjia
	 * 
	 */
	private class MyAsynTask extends AsyncTask<Integer, Integer, Void> {
		private final static int STEP = 30;// 步伐
		private final static int TIME = 5;// 休眠时间
		private int distance;// 距离（该距离指的是：mHeadView的PaddingTop+mHeadView的高度，及默认位置状态.）
		private int number;// 循环执行次数.
		private int disPadding;// 时时padding距离.
		private int DRAG;

		@Override
		protected Void doInBackground(Integer... params) {
			try {
				this.DRAG = params[0];
				if (params[0] == DRAG_UP) {
					// 获取距离.
					distance = mHeadView.getPaddingTop()
							+ Math.abs(mHeadViewHeight);
				} else {
					// 获取距离.
					distance = mFootView.getPaddingBottom()
							+ Math.abs(mHeadViewHeight);
				}

				// 获取循环次数.
				if (distance % STEP == 0) {
					number = distance / STEP;
				} else {
					number = distance / STEP + 1;
				}
				// 进行循环.
				for (int i = 0; i < number; i++) {
					Thread.sleep(TIME);
					publishProgress(STEP);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);

			switch (DRAG) {
			case DRAG_UP:
				disPadding = Math.max(mHeadView.getPaddingTop() - STEP, -1
						* mHeadViewHeight);
				
				mHeadView.setPadding(0, disPadding, 0, 0);// 回归.
				break;
			case DRAG_DOWN:
				disPadding = Math.max(mFootView.getPaddingBottom() - STEP, -1
							* mHeadViewHeight);
				
				mFootView.setPadding(0, 0, 0, disPadding);// 回归.
				break;
			default:
				break;
			}

		}

	}

}
