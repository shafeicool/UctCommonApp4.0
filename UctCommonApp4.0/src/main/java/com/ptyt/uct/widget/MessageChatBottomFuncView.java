package com.ptyt.uct.widget;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.ptyt.uct.R;
import com.ptyt.uct.fragment.BaseFragment.OnEventListener;
import com.ptyt.uct.fragment.FaceFragment;
import com.ptyt.uct.fragment.MessageFunctionFragment;

/**
 * @Description:
 * @Date: 2017/5/10
 * @Author: ShaFei
 * @Version: V1.0
 */

public class MessageChatBottomFuncView extends FrameLayout {

    private Context mContext;
    private MessageFunctionFragment functionFragment;
    private FaceFragment faceFragement;
    private FragmentManager mfragementManager;

    public MessageChatBottomFuncView(Context context) {
        this(context, null, -1);
    }

    public MessageChatBottomFuncView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public MessageChatBottomFuncView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initView();
    }
    View layout;
    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View layout = inflater.inflate(R.layout.include_message_chat_bottom_func, null);
        removeAllViews();
        LayoutParams mLayoutParams  = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(layout, mLayoutParams);
    }

    public void initFragement(FragmentManager fragementManager, OnEventListener onEventListener) {
        mfragementManager = fragementManager;
        faceFragement = (FaceFragment) mfragementManager.findFragmentById(R.id.face_fragment);
        functionFragment = (MessageFunctionFragment) mfragementManager.findFragmentById(R.id.select_file_fragment);
        faceFragement.setOnEventListener(onEventListener);
    }

    public void switchFragement(int key) {
        switch (key) {
            case 0:
                setVisibility(View.GONE);
                break;
            case 1:
                setVisibility(View.VISIBLE);
                setFragement(faceFragement);
                break;

            case 2:
                setVisibility(View.VISIBLE);
                setFragement(functionFragment);
                break;
        }
    }

    private void setFragement(Fragment baseFragement) {
        FragmentTransaction transaction = mfragementManager.beginTransaction();
        if (baseFragement == faceFragement) {
            transaction.hide(functionFragment);
            transaction.show(faceFragement);
        } else {
            transaction.hide(faceFragement);
            transaction.show(functionFragment);
        }
        transaction.commit();
    }

    public void setSrcNo(String msgSrcNo) {
        functionFragment.setSrcNo(msgSrcNo);
    }

    public void setDstNo(String msgDstNo) {
        functionFragment.setDstNo(msgDstNo);
    }

    public void setConversationId(Long conversationId) {
        functionFragment.setConversationId(conversationId);
    }

    public void setGroupNo(String groupNo) {
        functionFragment.setGroupNo(groupNo);
    }

    public void setGroupName(String groupName) {
        functionFragment.setGroupName(groupName);
    }

    public String getPath() {
        return functionFragment.getPath();
    }

}
