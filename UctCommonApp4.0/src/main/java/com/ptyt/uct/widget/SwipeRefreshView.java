package com.ptyt.uct.widget;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ListView;

import com.ptyt.uct.R;
import com.ptyt.uct.utils.AppUtils;

/**
 * Title: com.ptyt.uct.widget
 * Description:
 * Date: 2017/8/29
 * Author: ShaFei
 * Version: V1.0
 */

public class SwipeRefreshView extends SwipeRefreshLayout {
    private static final String TAG = SwipeRefreshView.class.getSimpleName();
    private final int mScaledTouchSlop;
    private final View mFooterView;
    private ListView mListView;
    private OnLoadMoreListener mListener;

    /**
     * 正在加载状态
     */
    private boolean isLoading;
    private RecyclerView mRecyclerView;
    private int mItemCount;
    private boolean isRecyclerView;

    public SwipeRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 填充底部加载布局
        mFooterView = View.inflate(context, R.layout.view_swipe_refresh_footer, null);

        // 表示控件移动的最小距离，手移动的距离大于这个距离才能拖动控件
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 获取ListView,设置ListView的布局位置
        if (mListView == null || mRecyclerView == null) {
            // 判断容器有多少个孩子
            if (getChildCount() > 0) {
                // 判断第一个孩子是不是ListView
                if (getChildAt(0) instanceof ListView) {
                    // 创建ListView对象
                    mListView = (ListView) getChildAt(0);
                    isRecyclerView = false;
                    // 设置ListView的滑动监听
                    setListViewOnScroll();
                } else if (getChildAt(0) instanceof RecyclerView) {
                    // 创建ListView对象
                    mRecyclerView = (RecyclerView) getChildAt(0);
                    isRecyclerView = true;
                    // 设置RecyclerView的滑动监听
                    setRecyclerViewOnScroll();
                }
            }
        }
    }


    /**
     * 在分发事件的时候处理子控件的触摸事件
     */
    private float mDownY, mUpY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 移动的起点
                mDownY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // 移动过程中判断时候能下拉加载更多
                //                if (canLoadMore()) {
                //                    // 加载数据
                //                    loadData();
                //                }

                break;
            case MotionEvent.ACTION_UP:
                // 移动的终点
                mUpY = ev.getY();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    //    /**
    //     * 判断是否满足加载更多条件
    //     */
    //    private boolean canLoadMore() {
    //        // 1. 是上拉状态
    //        if (mUpY == 0f) {
    //            return false;
    //        }
    //        boolean condition1 = (mDownY - mUpY) >= mScaledTouchSlop;
    //        if (!condition1) {
    //            return condition1;
    //        }
    //
    //        // 2. 当前页面可见的item是最后一个条目,一般最后一个条目位置需要大于第一页的数据长度
    //        boolean condition2 = false;
    //        if (isRecyclerView) {
    //            if (mRecyclerView != null && mRecyclerView.getAdapter() != null) {
    //
    //                if (mItemCount > 0) {
    //                    if (mRecyclerView.getAdapter().getItemCount() < mItemCount) {
    //                        // 第一页未满，禁止下拉
    //                        condition2 = false;
    //                    } else {
    //                        int position = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
    //                        condition2 = position == (mRecyclerView.getAdapter().getItemCount() - 1);
    //                    }
    //                } else {
    //                    // 未设置数据长度，则默认第一页数据不满时也可以上拉
    //                    condition2 = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition() == (mRecyclerView.getAdapter().getItemCount() - 1);
    //                }
    //
    //            }
    //        } else {
    //            if (mListView != null && mListView.getAdapter() != null) {
    //
    //                if (mItemCount > 0) {
    //                    if (mListView.getAdapter().getCount() < mItemCount) {
    //                        // 第一页未满，禁止下拉
    //                        condition2 = false;
    //                    } else {
    //                        condition2 = mListView.getLastVisiblePosition() == (mListView.getAdapter().getCount() - 1);
    //                    }
    //                } else {
    //                    // 未设置数据长度，则默认第一页数据不满时也可以上拉
    //                    condition2 = mListView.getLastVisiblePosition() == (mListView.getAdapter().getCount() - 1);
    //                }
    //
    //            }
    //        }
    //
    //        // 3. 正在加载状态
    //        boolean condition3 = !isLoading;
    //        return condition1 && condition2 && condition3;
    //    }

    private boolean isUpDrag() {
        // 1. 是上拉状态
        if (mUpY == 0f) {
            return false;
        }
        boolean condition1 = (mDownY - mUpY) >= mScaledTouchSlop;
        return condition1;
    }

    private boolean isLastItem() {
        // 2. 当前页面可见的item是最后一个条目,一般最后一个条目位置需要大于第一页的数据长度
        boolean condition2 = false;
        if (isRecyclerView) {
            if (mRecyclerView != null && mRecyclerView.getAdapter() != null) {

                if (mItemCount > 0) {
                    if (mRecyclerView.getAdapter().getItemCount() < mItemCount) {
                        // 第一页未满，禁止下拉
                        condition2 = false;
                    } else {
                        int position = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
                        condition2 = position == (mRecyclerView.getAdapter().getItemCount() - 1);
                    }
                } else {
                    // 未设置数据长度，则默认第一页数据不满时也可以上拉
                    condition2 = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition() == (mRecyclerView.getAdapter().getItemCount() - 1);
                }

            }
        } else {
            if (mListView != null && mListView.getAdapter() != null) {

                if (mItemCount > 0) {
                    if (mListView.getAdapter().getCount() < mItemCount) {
                        // 第一页未满，禁止下拉
                        condition2 = false;
                    } else {
                        condition2 = mListView.getLastVisiblePosition() == (mListView.getAdapter().getCount() - 1);
                    }
                } else {
                    // 未设置数据长度，则默认第一页数据不满时也可以上拉
                    condition2 = mListView.getLastVisiblePosition() == (mListView.getAdapter().getCount() - 1);
                }

            }
        }
        return condition2;
    }

    public void setItemCount(int itemCount) {
        this.mItemCount = itemCount;
    }

    /**
     * 处理加载数据的逻辑
     */
    private void loadData() {
        if (mListener != null) {
            // 设置加载状态，让布局显示出来
            mListener.onLoadMore();
        }

    }

    /**
     * 设置加载状态，是否加载传入boolean值进行判断
     *
     * @param loading
     */
    public void setLoading(boolean loading) {
        // 修改当前的状态
        isLoading = loading;
//        if (!isRecyclerView) {
//            if (isLoading) {
//                // 显示布局
//                mListView.addFooterView(mFooterView);
//            } else {
//                // 隐藏布局
//                mListView.removeFooterView(mFooterView);
//                // 重置滑动的坐标
//                mDownY = 0;
//                mUpY = 0;
//            }
//        }

    }

    private boolean isUpDrag;
    private boolean isToTop;

    /**
     * 设置ListView的滑动监听
     */
    private void setListViewOnScroll() {

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 移动过程中判断时候能下拉加载更多
                isUpDrag = isUpDrag();
                if (isUpDrag && isLastItem() && !isLoading) {
                    loadData();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }


    /**
     * 设置RecyclerView的滑动监听
     */
    private void setRecyclerViewOnScroll() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // 移动过程中判断时候能下拉加载更多
                //                if (canLoadMore()) {
                //                    // 加载数据
                //                    loadData();
                //                }
                isUpDrag = isUpDrag();
                boolean abc = isLastItem();
                if (isUpDrag && abc && !isLoading) {
                    loadData();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition = (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                if (topRowVerticalPosition == 0 && !AppUtils.isFastClick()) {
                    //                    loadData(false);
                    isToTop = true;
                }
            }
        });
    }

    public boolean isScrollToTop() {
        return !isUpDrag && isToTop;
    }

    /**
     * 上拉加载的接口回调
     */

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mListener = listener;
    }

}
