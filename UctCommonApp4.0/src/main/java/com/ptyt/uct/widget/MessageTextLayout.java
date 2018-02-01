package com.ptyt.uct.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ptyt.uct.R;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.utils.StrUtils;

/**
 * Title: com.ptyt.uct.widget
 * Description:
 * Date: 2017/5/22
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageTextLayout extends MessageBaseLayout implements View.OnLongClickListener {

    private int maxHeight;
    private RelativeLayout rl_relative;
    private TextView tv_content;
    private Context mContext;

    private ConversationMsg conversationMsg;
    private int position;
    private int msgDirection;
    private String msgContent;
    // 第一次添加该Layout
    private boolean isFirstEnter = true;

    public MessageTextLayout(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public MessageTextLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.MaxWidth);
        maxHeight = (int) typedArray.getDimension(R.styleable.MaxWidth_maxWidth, 0);
    }

    public void show(ConversationMsg conversationMsg, int position) {
        this.conversationMsg = conversationMsg;
        this.position = position;
        msgDirection = conversationMsg.getMsgDirection();
        initWindow(mContext);
    }

    @Override
    protected int setLayoutId() {
        if (msgDirection == MessageDBConstant.IMVT_COM_MSG) {
            return R.layout.view_message_text_content_left;
        } else {
            return R.layout.view_message_text_content_right;
        }
    }

    @Override
    protected void initView(View view) {
        rl_relative = (RelativeLayout) view.findViewById(R.id.rl_relative);
        tv_content = (TextView) view.findViewById(R.id.tv_content);
        msgContent = conversationMsg.getContent();
        tv_content.setText(StrUtils.faceHandler(mContext, msgContent));

        rl_relative.setOnLongClickListener(this);
    }

    @Override
    protected void initData() {

    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.rl_relative:
                new MessageOptionDialogManager(mContext, conversationMsg, position);
                break;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
