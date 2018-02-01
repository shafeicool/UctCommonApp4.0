package com.ptyt.uct.fragment;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.adapter.BasePagingAdapter;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.widget.DividerLine;
import com.ptyt.uct.widget.EmptyLayout;

/**
 * @Description: 列表页基类
 * @Date: 2017/5/11
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public abstract class BaseListFragment extends BaseFragment implements BasePagingAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {


    protected RecyclerView recyclerView;
    protected Context context;
    protected BasePagingAdapter adapter;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected EmptyLayout emptyLayout;
    private LinearLayoutManager layoutManager;
    protected int currentPage = 0;//加载的当前页码
    protected boolean isRefreshMode = false;//标志当前为刷新还是加载更多
    protected boolean isBottom;//标志数据是否被加载完
    @Override
    protected int setLayoutId() {
        return R.layout.fragment_base_list;
    }

    @Override
    protected void initView(View view) {
        context = getActivity();
        recyclerView = ((RecyclerView) view.findViewById(R.id.recyclerView));
        swipeRefreshLayout = ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout));
        emptyLayout = ((EmptyLayout) view.findViewById(R.id.emptyLayout));
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_green_light);
        //固定RecyclerView的大小
        recyclerView.setHasFixedSize(true);
        //设置RecyclerView的分割线
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(2);
        dividerLine.setColor(0xDDDEE8F5);
        recyclerView.addItemDecoration(dividerLine);
        //2.设置布局管理器,默认垂直
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        adapter = getAdapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void initEvent() {
        super.initEvent();
        swipeRefreshLayout.setOnRefreshListener(this);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            public int lastVisibleItem;
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem =layoutManager.findLastVisibleItemPosition();
            }
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                /*ptyt 解决加载首页的下滑刷新时问题 !swipeRefreshLayout.isRefreshing()*/
                if (newState ==RecyclerView.SCROLL_STATE_IDLE && !swipeRefreshLayout.isRefreshing() && lastVisibleItem + 1 ==adapter.getItemCount() && isBottom == false && !isRefreshMode) {
                    currentPage++;
                    adapter.setState(ConstantUtils.STATE_LOAD_MORE);
                    loadMoreData();
                }
            }
        });
    }
    /**
     * recyclerView滑到底,列表加载更多
     */
    protected void loadMoreData() {
        isRefreshMode = false;
    }
    /**
     * @return BaseRecyAdapter实现类
     */
    protected abstract BasePagingAdapter getAdapter();

    /**
     * item点击事件 子类可决定是否重写
     * @param pos 点击位置
     */
    @Override
    public void onItemClick(int pos) {

    }

    @Override
    public void onRefresh() {
        PrintLog.i("onRefresh()");
        adapter.setState(ConstantUtils.STATE_LOAD_MORE);
        isRefreshMode = true;
    }
}
