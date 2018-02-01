package com.ptyt.uct.activity;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.ptyt.uct.R;
import com.ptyt.uct.callback.MapCallBack;

import static com.ptyt.uct.R.id.iv_clear;
import static com.ptyt.uct.R.id.tv_searchCancel;


/**
 * @Description:
 * @Date: 2017/8/4
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class MapSearchActivity extends BaseActivity implements  View.OnClickListener {

    private TextView tv_myAddress;
    private EditText et_search;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_map_search);
        tv_myAddress = ((TextView) findViewById(R.id.tv_myAddress));
        et_search = ((EditText) findViewById(R.id.et_search));
        findViewById(tv_searchCancel).setOnClickListener(this);
        findViewById(iv_clear).setOnClickListener(this);
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String s2 = s.toString().trim();
                keyChangedListener.onChanged(s2);
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        AMapLocation aMapLocation = MapCallBack.getMapCallBack().aMapLocation;
        if(aMapLocation!=null && !TextUtils.isEmpty(aMapLocation.getAddress())){
            tv_myAddress.setText(aMapLocation.getAddress());
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case tv_searchCancel://取消

                finish();
                break;
            case iv_clear://清空
                et_search.setText("");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    OnSearchKeyChangedListener keyChangedListener;

    public void setKeyChangedListener(OnSearchKeyChangedListener keyChangedListener) {
        this.keyChangedListener = keyChangedListener;
    }

    public interface OnSearchKeyChangedListener{
        void onChanged(String keyWord);
    }
}
