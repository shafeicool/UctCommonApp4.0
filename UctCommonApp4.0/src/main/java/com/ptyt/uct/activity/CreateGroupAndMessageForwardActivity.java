package com.ptyt.uct.activity;

import android.view.View;
import android.widget.TextView;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.fragment.CreateTempGroupFragment;
import com.ptyt.uct.fragment.MessageForwardingGroupFragment;
import com.ptyt.uct.fragment.MessageForwardingUserFragment;

/**
 * @Description:
 * @Date: 2017/9/11
 * @Author: ShaFei
 * @Version:V1.0
 */

public class CreateGroupAndMessageForwardActivity extends BaseActionBarActivity {
    private ConversationMsg conversationMsg;
    private boolean isGroup = false;

    @Override
    protected int setLayoutId() {
        return 0;
    }

    @Override
    protected void initWidget() {
        conversationMsg = (ConversationMsg) getIntent().getSerializableExtra("conversationMsg");
        isGroup = getIntent().getBooleanExtra("isGroup", false);
        tv_rightButton.setVisibility(View.VISIBLE);
        tv_cancel.setVisibility(View.VISIBLE);
        iv_back.setVisibility(View.GONE);
        // 如果不等于空，当前为转发页面，否则为创建对讲页面
        if (conversationMsg == null) {
            tv_actionBarTitle.setText(getResources().getString(R.string.string_create_group_title1));
            tv_rightButton.setText(getResources().getString(R.string.string_create_group_confim1));
            addFragment(new CreateTempGroupFragment());
        } else {
            tv_actionBarTitle.setText(getResources().getString(R.string.string_create_group_title2));
            tv_rightButton.setText(getResources().getString(R.string.string_create_group_confim2));
            if (isGroup) {
                addFragment(new MessageForwardingGroupFragment());
            } else {
                addFragment(new MessageForwardingUserFragment());
            }
        }
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public TextView getRightButton() {
        return tv_rightButton;
    }

    public ConversationMsg getConversationMsg() {
        return conversationMsg;
    }

    @Override
    protected void onDestroy() {
        PrintLog.i("onDestroy");
        super.onDestroy();
    }
}
