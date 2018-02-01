package com.ptyt.uct.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.uct.bean.GroupData;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.MainActivity;
import com.ptyt.uct.activity.MessageActivity;
import com.ptyt.uct.adapter.BaseRecyAdapter.OnItemClickListener;
import com.ptyt.uct.adapter.BaseRecyAdapter.OnItemLongClickListener;
import com.ptyt.uct.adapter.ConversationAdapter;
import com.ptyt.uct.callback.GroupInfoCallback;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.entity.Conversation;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.model.ConversationDBManager;
import com.ptyt.uct.model.MessageDBManager;
import com.ptyt.uct.services.BaseServiceCallBack;
import com.ptyt.uct.services.MessageManager;
import com.ptyt.uct.services.MsgBinder.MsgCallBackListener;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.SDCardUtils;
import com.ptyt.uct.utils.ScreenUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.viewinterface.IConversationView;

import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * @Description:
 * @Date: 2017/8/9
 * @Author: ShaFei
 * @Version:V1.0
 */

public class ConversationFragment extends BaseFragment implements
        IConversationView,
        OnItemClickListener,
        OnItemLongClickListener,
        OnClickListener,
        MsgCallBackListener {

    private Context mContext;

    private View view_parent;
    private RecyclerView mConversationRecyclerView;
    private TextView mNoFindListTv;
    private TextView stick_tv;
    private PopupWindow mItemPw;
    private int position;

    private ConversationAdapter mAdapter;
    private static final int LOAD_CONVERSATION_LIST = 0;
    private static final int DELETE_CONVERSATION = 1;
    private static final int STICK_CONVERSATION = 2;
    private static final int UNSTICK_CONVERSATION = 3;
    private static final int DELETE_FILE_IN_LIMITED_TIME = 4;
    private List<GroupData> groupDatas;

    // 所有会话未读数
    private int count = 0;
    // 数据库超出10000条就删除
    private Long limitCount = 10000L;
    // 数据库超过7天就删除
    private Long limitTime = 604800000L;
    private boolean isSticked;
    private BitmapDrawable popBitmapDrawable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrintLog.e("onCreate");
        PrintLog.e("registerObserver BaseServiceCallBack.INDEX_IMESSAGEVIEW");
        MessageManager.getInstane().registerObserver(this, BaseServiceCallBack.INDEX_IMESSAGEVIEW);
        EventBus.getDefault().register(this);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.fragment_conversation_list;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void initView(View view) {
        mContext = getActivity();
        view_parent = view.findViewById(R.id.view_parent);
        mNoFindListTv = (TextView) view.findViewById(R.id.no_find_list_tv);
        mConversationRecyclerView = (RecyclerView) view.findViewById(R.id.listview);
        mConversationRecyclerView.setHasFixedSize(true);
        mConversationRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        initConversationItemWindow();
    }

    @Override
    protected void initData() {
        setNewMyLooper(true);
        mAdapter = new ConversationAdapter(mContext);
        mConversationRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);
        /* ptyt begin, 优先显示组名称，如果没有则显示组号码_4094_shafei_20170908 */
        groupDatas = GroupInfoCallback.getInstance().getmGroupList();
        mAdapter.setGroupDatas(groupDatas);
        /* ptyt end */
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void initConversationItemWindow() {
        View view = View.inflate(mContext, R.layout.dialog_conversation_item_option, null);
        stick_tv = (TextView) view.findViewById(R.id.stick_tv);
        stick_tv.setOnClickListener(this);
        view.findViewById(R.id.delete_tv).setOnClickListener(this);
        mItemPw = new PopupWindow(view, RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT, true);
        popBitmapDrawable = new BitmapDrawable();
        mItemPw.setBackgroundDrawable(popBitmapDrawable);
        mItemPw.setOutsideTouchable(true);
        mItemPw.setAnimationStyle(android.R.style.Animation_Dialog);
    }

    @Override
    public boolean handleMessage(final Message message) {
        super.handleMessage(message);
        switch (message.what) {
            // 展示会话列表
            case LOAD_CONVERSATION_LIST:
                PrintLog.d("----------------查询会话数据库所有记录Begin----------------");
                List<Conversation> load_list = ConversationDBManager.getInstance(super.mContext).queryConversationList();
                PrintLog.d("----------------查询会话数据库所有记录End----------------");
                updateConversationAdapter(load_list);
                break;
            // 删除会话后刷新会话列表
            case DELETE_CONVERSATION:
                Conversation conversation1 = mAdapter.getItem(position);
                PrintLog.d("----------------删除会话数据库记录Begin----------------");
                ConversationDBManager.getInstance(super.mContext).deleteConversation(conversation1);
                PrintLog.d("----------------删除会话数据库记录End----------------");
                List<Conversation> delete_list = ConversationDBManager.getInstance(super.mContext).queryConversationList();
                updateConversationAdapter(delete_list);
                Long id = conversation1.getID();
                SDCardUtils.deleteConversationFile(mContext, id);
                break;
            // 置顶会话后刷新会话列表
            case STICK_CONVERSATION:
                Conversation conversation2 = mAdapter.getItem(position);
                conversation2.setStickTime(StrUtils.getCurrentTimes());
                PrintLog.d("----------------置顶会话数据库记录Begin----------------");
                ConversationDBManager.getInstance(super.mContext).updateConversation(conversation2);
                PrintLog.d("----------------置顶会话数据库记录End----------------");
                List<Conversation> stick_list = ConversationDBManager.getInstance(super.mContext).queryConversationList();
                updateConversationAdapter(stick_list);
                break;
            // 取消置顶会话后刷新会话列表
            /* ptyt begin, 增加取消置顶_4085_shafei_20170908 */
            case UNSTICK_CONVERSATION:
                Conversation conversation3 = mAdapter.getItem(position);
                conversation3.setStickTime(null);
                PrintLog.d("----------------取消置顶会话数据库记录Begin----------------");
                ConversationDBManager.getInstance(super.mContext).updateConversation(conversation3);
                PrintLog.d("----------------取消置顶会话数据库记录End----------------");
                List<Conversation> unstick_list = ConversationDBManager.getInstance(super.mContext).queryConversationList();
                updateConversationAdapter(unstick_list);
                break;
            /* ptyt end */
            case DELETE_FILE_IN_LIMITED_TIME:
                // 搜出所有的语音、图片、视频、文件等
                List<ConversationMsg> msgList = MessageDBManager.getInstance(super.mContext).queryMessageFile();
                if (msgList != null && msgList.size() > 0) {
                    for (int i = 0; i < msgList.size(); i++) {
                        ConversationMsg conversationMsg = msgList.get(i);
                        if (StrUtils.getCurrentTimes() - conversationMsg.getMsgTime() >= limitTime) {
                            if (SDCardUtils.isFileInFolder(conversationMsg.getLocalImgPath())) {
                                PrintLog.i("DELETE_FILE_IN_LIMITED_TIME currentTime = " + StrUtils.getCurrentTimes() + ", msgTime = " + conversationMsg.getMsgTime()
                                + ", localImgPath = " + conversationMsg.getLocalImgPath());
                                FileUtils.deleteFile(mContext, conversationMsg.getLocalImgPath());
                            }
                        }
                    }
                }
                break;
        }
        return false;
    }

    @Override
    public void onItemClick(int pos, View itemView) {
        String msgSrcNo = mAdapter.getItem(pos).getMsgSrcNo();
        String msgDstNo = mAdapter.getItem(pos).getMsgDstNo();
        String groupNo = mAdapter.getItem(pos).getGroupNo();
        Intent intent = new Intent(mContext, MessageActivity.class);
        if (!StrUtils.isEmpty(groupNo)) {
            intent.putExtra("groupName", ((TextView) itemView.findViewById(R.id.name_tv)).getText().toString());
            intent.putExtra("groupId", groupNo);
            intent.putExtra("isGroupNo", true);
        } else {
            if (msgDstNo.equals(AppContext.getAppContext().getLoginNumber())) {
                intent.putExtra("userName", ((TextView) itemView.findViewById(R.id.name_tv)).getText().toString());
                intent.putExtra("callNumber", msgSrcNo);
            } else {
                intent.putExtra("userName", ((TextView) itemView.findViewById(R.id.name_tv)).getText().toString());
                intent.putExtra("callNumber", msgDstNo);
            }
            intent.putExtra("isGroupNo", false);
        }
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onItemLongClick(int pos, View itemView) {
        position = pos;
        /* ptyt begin, 增加取消置顶_4085_shafei_20170908 */
        isSticked = ConversationDBManager.getInstance(super.mContext).isConversationSticked(mAdapter.getItem(pos));
        if (isSticked) {
            stick_tv.setText(getResources().getString(R.string.string_conversation_unstick));
        } else {
            stick_tv.setText(getResources().getString(R.string.string_conversation_stick));
        }
        /* ptyt end */
        if (((MainActivity)getActivity()).getOfflineView() == View.VISIBLE) {
            // TODO: 2017/12/13
            mItemPw.showAtLocation(view_parent, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, ScreenUtils.getDimensionPixelSize(mContext, R.dimen.y220) + itemView.getHeight() / 2 + (int) itemView.getY());
        } else {
            mItemPw.showAtLocation(view_parent, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, ScreenUtils.getDimensionPixelSize(mContext, R.dimen.y100) + itemView.getHeight() / 2 + (int) itemView.getY());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stick_tv:
                if (mItemPw.isShowing()) {
                    mItemPw.dismiss();
                }
                /* ptyt begin, 增加取消置顶_4085_shafei_20170908 */
                if (isSticked) {
                    sendMsg(true, UNSTICK_CONVERSATION);
                } else {
                    sendMsg(true, STICK_CONVERSATION);
                }
                /* ptyt end */
                break;
            case R.id.delete_tv:
                if (mItemPw.isShowing()) {
                    mItemPw.dismiss();
                }
                sendMsg(true, DELETE_CONVERSATION);
                break;
        }

    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEvent(EventBean eventBean) {
        if (eventBean.getAction().equals(ConstantUtils.ACTION_GROUP_INFO_REFRESH)) {
            groupDatas = GroupInfoCallback.getInstance().getmGroupList();
            mAdapter.setGroupDatas(groupDatas);
            mAdapter.notifyDataSetChanged();
        } else if (eventBean.getAction().equals(ConstantUtils.ACTION_INSERT_CONTACT)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateConversationAdapter(final List<Conversation> conversationList) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                count = 0;
                if (conversationList != null && conversationList.size() > 0) {
                    mConversationRecyclerView.setVisibility(View.VISIBLE);
                    mAdapter.addAll(conversationList);
                    for (Conversation conversation : conversationList) {
                        count += conversation.getUnreadMsgCounts();
                    }
                } else {
                    /* ptyt begin, 解决当删除最后一条会话时，conversationList已经为空，但是界面未刷新问题_4096_shafei_20170908 */
//                    mAdapter.addAll(conversationList);
                    mConversationRecyclerView.setVisibility(View.GONE);
                    mAdapter.removeAll();
                    /* ptyt end */
                }
                if(getActivity() == null){
                    return;
                }
                ((MainActivity) getActivity()).setMessageUnreadCount(count);
            }
        });
    }

    @Override
    public int notifyDataChangedListener(ConversationMsg mConversationMsg, int type) {
        switch (type) {
            case MsgCallBackListener.MSG_SEND_INSERT_DB:
                if (mConversationMsg.getMsgDirection() == MessageDBConstant.IMVT_COM_MSG) {
                    List<Conversation> list = ConversationDBManager.getInstance(super.mContext).queryConversationList();
                    updateConversationAdapter(list);
                }
                break;
            case MsgCallBackListener.MSG_STATUS_CHANGE:
                List<Conversation> list = ConversationDBManager.getInstance(super.mContext).queryConversationList();
                updateConversationAdapter(list);
                break;
            case MsgCallBackListener.MSG_EASTONECFM:
                break;
            case MsgCallBackListener.MSG_UPDATE_PROGRESS:
                break;
        }
        return 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        sendMsg(true, LOAD_CONVERSATION_LIST);
        if (!AppUtils.isFastClick3()) {
            sendMsg(true, DELETE_FILE_IN_LIMITED_TIME);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        PrintLog.i("onDestroy");
        popBitmapDrawable = null;
        EventBus.getDefault().unregister(this);
        PrintLog.e("unRegisterObserver BaseServiceCallBack.INDEX_IMESSAGEVIEW");
        MessageManager.getInstane().unRegisterObserver(this, BaseServiceCallBack.INDEX_IMESSAGEVIEW);
        super.onDestroy();
    }

    /**
     * 用户登录状态改变
     */
    public void notifyLoginStatusChanged() {
        if (mItemPw != null && mItemPw.isShowing()) {
            mItemPw.dismiss();
        }
    }
}
