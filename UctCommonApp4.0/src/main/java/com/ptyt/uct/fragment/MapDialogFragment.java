package com.ptyt.uct.fragment;

import android.app.Dialog;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.MainActivity;
import com.ptyt.uct.adapter.BaseRecyAdapter;
import com.ptyt.uct.adapter.MapDialogAdapter;
import com.ptyt.uct.utils.ScreenManager;
import com.ptyt.uct.widget.DividerLine;
import com.ptyt.uct.widget.mapcluster.RegionItem;

import java.util.List;

/**
 * @Description:
 * @Date: 2017/12/20
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class MapDialogFragment extends android.support.v4.app.DialogFragment implements BaseRecyAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private MapDialogAdapter adapter;

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        PrintLog.e("onCreateView()");
        ScreenManager.getInstance().prepare(getActivity());
        View view = inflater.inflate(R.layout.fragment_map_dialog, null);
        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new BitmapDrawable());
        //去阴影
        window.clearFlags( WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        initView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        /*适配oppo手机上布局宽度不受控制显示问题*/
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout((int) (getResources().getDimension(R.dimen.x494)), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void initView(View view) {
        view.findViewById(R.id.iv_dialogCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        recyclerView = ((RecyclerView) view.findViewById(R.id.recy_mapDialog));
        //固定RecyclerView的大小
        recyclerView.setHasFixedSize(true);
        //设置RecyclerView的分割线
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(2);
        dividerLine.setColor(0xFFDEE8F5);
        recyclerView.addItemDecoration(dividerLine);
        //2.设置布局管理器,默认垂直
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MapDialogAdapter(getContext());
        recyclerView.setAdapter(adapter);
        List<RegionItem> clusterItems = ((MainActivity) getActivity()).getMapFragment().getClusterItems();
        adapter.addAll(clusterItems);
        adapter.setOnItemClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onItemClick(int pos, View itemView) {
        RegionItem item = adapter.getItem(pos);
        //通知MapFragment显示
        ((MainActivity) getActivity()).getMapFragment().onMapDialogItemClick(item);
    }

    /**
     * 重写show避免闪退 解决快速多次点击按钮 java.lang.IllegalStateException: Fragment already added: XXDialogFragment
     * @param manager
     * @param tag
     */
    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            //在每个add事务前增加一个remove事务，防止连续的add
            manager.beginTransaction().remove(this).commit();
            super.show(manager, tag);
        } catch (Exception e) {
            //同一实例使用不同的tag会异常,这里捕获一下
            e.printStackTrace();
        }
    }
}
