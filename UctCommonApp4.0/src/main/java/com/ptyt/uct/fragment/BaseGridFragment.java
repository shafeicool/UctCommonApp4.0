package com.ptyt.uct.fragment;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ptyt.uct.R;
import com.ptyt.uct.adapter.BaseRecyAdapter;

/**
 * @Description: 网格列表基础类
 * @Date:        2017/5/25
 * @Author:      ShaFei
 * @Version:     V1.0
 */

public abstract class BaseGridFragment extends BaseFragment implements BaseRecyAdapter.OnItemClickListener {


    protected RecyclerView recyclerView;
    protected Context context;
    protected BaseRecyAdapter adapter;
    protected SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected int setLayoutId() {
        return R.layout.fragment_base_list;
    }

    @Override
    protected void initView(View view) {
        context = getActivity();
        swipeRefreshLayout = ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout));
        recyclerView = ((RecyclerView) view.findViewById(R.id.recyclerView));
        recyclerView.setHasFixedSize(true);
//        recyclerView.getHeight();
        GridLayoutManager layoutManager = new GridLayoutManager(context, 4);
        recyclerView.setLayoutManager(layoutManager);
        adapter = getAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    /**
     * @return BaseRecyAdapter实现类
     */
    protected abstract BaseRecyAdapter getAdapter();

    /**
     * item点击事件 子类可决定是否重写
     * @param pos 点击位置
     * @param itemView
     */
    @Override
    public void onItemClick(int pos, View itemView) {

    }
}
