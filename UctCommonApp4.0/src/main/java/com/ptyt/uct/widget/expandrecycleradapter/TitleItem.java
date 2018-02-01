package com.ptyt.uct.widget.expandrecycleradapter;


import android.view.View;
import android.widget.TextView;

import com.ptyt.uct.R;

public class TitleItem extends AbstractAdapterItem {

    private TextView mName;

    @Override
    public int getLayoutResId() {
        return R.layout.item_offline_map_title;
    }

    @Override
    public void onBindViews(View root) {
        mName = (TextView) root.findViewById(R.id.tv_titleItem);
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(Object model, int position) {
        if (model instanceof Title) {
            Title title = (Title) model;
            mName.setText(title.name);
        }
    }
}
