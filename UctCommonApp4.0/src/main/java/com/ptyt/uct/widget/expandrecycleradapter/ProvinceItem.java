package com.ptyt.uct.widget.expandrecycleradapter;

import android.animation.ObjectAnimator;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ptyt.uct.R;

/**
 * @Description:
 * @Date: 2018/1/17
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class ProvinceItem extends AbstractExpandableAdapterItem {

    private TextView mName;
    private Province province;
    private ImageView mArrow;
    private View rootView;

    @Override
    public int getLayoutResId() {
        return R.layout.item_company;
    }
    @Override
    public void onBindViews(final View root) {
        /**
         * control item expand and unexpand
         */
        this.rootView = root;
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doExpandOrUnexpand();
//                Toast.makeText(root.getContext(), "click company：" +province.name,Toast.LENGTH_SHORT).show();
            }
        });
        mName = (TextView) root.findViewById(R.id.tv_name);
        mArrow = (ImageView) root.findViewById(R.id.iv_arrow);
    }

    @Override
    public void onSetViews() {

    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onExpansionToggled(boolean expanded) {
        float start, target;
        if (expanded) {
            start = 0f;
            target = 90f;
        } else {
            start = 90f;
            target = 0f;
        }
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mArrow, View.ROTATION, start, target);
        objectAnimator.setDuration(300);
        objectAnimator.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onUpdateViews(Object model, int position) {
        super.onUpdateViews(model, position);
        onSetViews();
        onExpansionToggled(getExpandableListItem().isExpanded());
        province = (Province) model;
        mName.setText(province.name);
        if("下载管理".equals(province.name)){
            rootView.setBackgroundColor(0xffd2d2d2);
        }else{
            rootView.setBackgroundColor(0xfff2f2f2);
        }
    }
}
