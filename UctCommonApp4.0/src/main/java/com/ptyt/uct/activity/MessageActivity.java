package com.ptyt.uct.activity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ptyt.uct.R;
import com.ptyt.uct.adapter.MessageAdapter;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.entity.MessageFileListEntity;
import com.ptyt.uct.entity.MessageMyLocationEntity;
import com.ptyt.uct.entity.MessagePhotoEntity;
import com.ptyt.uct.fragment.BaseFragment.OnEventListener;
import com.ptyt.uct.fragment.FaceFragment;
import com.ptyt.uct.model.ConversationDBManager;
import com.ptyt.uct.model.MessageDBManager;
import com.ptyt.uct.services.BaseServiceCallBack;
import com.ptyt.uct.services.MessageManager;
import com.ptyt.uct.services.MsgBinder.MsgCallBackListener;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.KeyBoardUtils;
import com.ptyt.uct.utils.SDCardUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.viewinterface.IMessageView;
import com.ptyt.uct.widget.AudioRecordButton;
import com.ptyt.uct.widget.MessageAudioPlayerManager;
import com.ptyt.uct.widget.MessageChatBottomFuncView;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

/**
 * @Description: 消息删除策略：
 * 1、如果一个会话的消息超出1000条，则删除最早的一条消息，同时删除PTYT路径下相应的文件。注：该规则在接收/发送一条消息时检查
 * 2、如果某条消息超过7天，则删除PTYT路径下相应的文件，但不删除消息。注：该规则一小时检查一次，且在切换到主界面时触发
 * @Date: 2018/1/15
 * @Author: ShaFei
 * @Version: V1.0
 */

public class MessageActivity extends BaseActionBarActivity implements
        OnClickListener,
        OnEventListener,
        TextWatcher,
        OnTouchListener,
        OnRefreshListener,
        MsgCallBackListener,
        IMessageView {

    private Context mContext;

    private Button mTxtOrAudioBtn, mKeyBoardOrFaceBtn, mSendOrFuncBtn;
    private AudioRecordButton mAudioBtn;
    private EditText mTextEt;
    private RecyclerView mChatRecyclerView;
    private MessageChatBottomFuncView mMessageChatBottomFuncView;
    private SwipeRefreshLayout mMessageChatSrl;

    //数据
    private ConversationMsg conversationMsg;
    private MessageAdapter mAdapter;
    private List<ConversationMsg> messageList = new ArrayList<>();
    // 即将发送的文件list
    private List<MessageFileListEntity> fileSendList = new ArrayList<>();
    private List<MessagePhotoEntity> photoSendList = new ArrayList<>();
    private MessageMyLocationEntity myLocationSendEntity;
    private QueryBuilder<ConversationMsg> qb = null;
    // 登录号码
    private String msgSrcNo;
    // 是否组聊天
    private boolean isGroupNo;
    // 组号码和组名称，这里无论是单人或是组聊天都要接到组号码，用于打开组成员列表
    private String msgGroupNo, msgGroupName;
    // 目的号码
    private String msgDstNo;
    // 会话id
    private Long conversationId;
    // 消息内容
    private String msgContent = "";
    // 临时消息内容，用于转换字符集编码
    private byte[] _tmpContent = null;

    // 子线程处理消息
    private static final int INIT_MESSAGE_LIST = 0;
    private static final int LOAD_MESSAGE_LIST = 1;
    private static final int SEND_TEXT_MESSAGE = 2;
    private static final int SEND_AUDIO_MESSAGE = 3;
    private static final int SEND_FILE_MESSAGE = 4;
    private static final int SEND_PHOTO_MESSAGE = 5;
    private static final int SEND_MY_LOCATION_MESSAGE = 6;
    private static final int DELAYED_TO_SCROLL_TO_POSITION = 100;

    // 底部按键功能逻辑
    private boolean isShowAudio = true;
    private boolean isShowFace = true;
    private boolean isShowFile = true;
    private boolean isShowSendBtn = false;

    // 加载消息状态
    private boolean isLoading = false;
    // 每页的大小
    private static final int PAGE_SIZE = 10;
    // 默认加载多少条
    private static final int DEFAULT_LOAD_SIZE = 10;
    // 短信总页数
    private long totalPage = 0;
    // 当前第几页
    private int currentPage = 0;
    // 数据库总的记录数
    private long totalRecord = 0;
    // 是否第一次读取
    private boolean isFirstLoad = true;
    // 是否发送压缩图
    private boolean isThumbnail = false;
    // 判断是否到底部
    private boolean isBottom = true;

    // 底部功能菜单回传数据
    public static final int PHOTO_RESULT_CODE = 10;
    public static final int FILE_RESULT_CODE = 11;
    public static final int LOCATION_RESULT_CODE = 12;
    public static final int MAP_RESULT_CODE = 13;
    public static final int GCALL_RESULT_CODE = 14;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_message_chat;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        isGroupNo = getIntent().getBooleanExtra("isGroupNo", false);
        msgGroupNo = getIntent().getStringExtra("groupId");
        msgGroupName = getIntent().getStringExtra("groupName");
        super.onCreate(savedInstanceState);
        PrintLog.i("onCreate");
    }

    @Override
    public void initWidget() {
        mContext = this;
        PrintLog.i("registerObserver BaseServiceCallBack.INDEX_IMESSAGEVIEW");
        MessageManager.getInstane().registerObserver(this, BaseServiceCallBack.INDEX_IMESSAGEVIEW);
        mTxtOrAudioBtn = (Button) findViewById(R.id.txt_or_audio_btn);
        mKeyBoardOrFaceBtn = (Button) findViewById(R.id.keyboard_or_face_btn);
        mSendOrFuncBtn = (Button) findViewById(R.id.send_or_func_btn);
        mAudioBtn = (AudioRecordButton) findViewById(R.id.audio_btn);
        mTextEt = (EditText) findViewById(R.id.text_et);
        mChatRecyclerView = (RecyclerView) findViewById(R.id.listview);
        mMessageChatBottomFuncView = (MessageChatBottomFuncView) findViewById(R.id.bottom_func_ll);
        mMessageChatSrl = (SwipeRefreshLayout) findViewById(R.id.message_chat_srl);
        mMessageChatSrl.setColorSchemeResources(R.color.colorBlue);
        mChatRecyclerView.setHasFixedSize(true);
        // 去除RecyclerView刷新闪烁的动画
        ((DefaultItemAnimator) mChatRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mTextEt.requestFocus();
        //        scrollToBottom();
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        mTxtOrAudioBtn.setOnClickListener(this);
        mKeyBoardOrFaceBtn.setOnClickListener(this);
        mSendOrFuncBtn.setOnClickListener(this);
        mTextEt.addTextChangedListener(this);
        mTextEt.setOnClickListener(this);
        mMessageChatSrl.setOnRefreshListener(this);
        mAudioBtn.setAudioFinishRecorder(this);
        mChatRecyclerView.setOnTouchListener(this);
        mChatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                //得到当前显示的最后一个item的view
                View lastChildView = recyclerView.getLayoutManager().getChildAt(recyclerView.getLayoutManager().getChildCount() - 1);
                if (lastChildView == null) {
                    return;
                }
                //得到lastChildView的bottom坐标值
                int lastChildBottom = lastChildView.getBottom();
                //得到Recyclerview的底部坐标减去底部padding值，也就是显示内容最底部的坐标
                int recyclerBottom = recyclerView.getBottom() - recyclerView.getPaddingBottom();
                //通过这个lastChildView得到这个view当前的position值
                int lastPosition = recyclerView.getLayoutManager().getPosition(lastChildView);

                //判断lastChildView的bottom值跟recyclerBottom
                //判断lastPosition是不是最后一个position
                //如果两个条件都满足则说明是真正的滑动到了底部
                if (lastChildBottom == recyclerBottom && lastPosition == recyclerView.getLayoutManager().getItemCount() - 1) {
                    isBottom = true;
                } else {
                    isBottom = false;
                }
            }

        });
    }

    //初始化要显示的数据
    protected void initData() {
        setNewMyLooper(true);
        mAdapter = new MessageAdapter(this);
        mChatRecyclerView.setAdapter(mAdapter);
        msgSrcNo = AppContext.getAppContext().getLoginNumber();
        if (isGroupNo) {
            msgDstNo = msgGroupNo;
            conversationId = ConversationDBManager.getInstance(mContext).queryConversationId(msgSrcNo, msgDstNo, msgGroupNo);
            setActionBarTitle(msgGroupName);
            setActionBarRightIcon(R.drawable.selector_message_group_member, this);
        } else {
            msgDstNo = getIntent().getStringExtra("callNumber");
            conversationId = ConversationDBManager.getInstance(mContext).queryConversationId(msgSrcNo, msgDstNo, null);
            setActionBarTitle(getIntent().getStringExtra("userName"));
        }
        PrintLog.i("msgSrcNo = " + msgSrcNo + "   msgDstNo = " + msgDstNo);
        mMessageChatBottomFuncView.initFragement(getSupportFragmentManager(), this);
        mMessageChatBottomFuncView.setSrcNo(msgSrcNo);
        mMessageChatBottomFuncView.setDstNo(msgDstNo);
        mMessageChatBottomFuncView.setConversationId(conversationId);
        mMessageChatBottomFuncView.setGroupNo(msgGroupNo);
        mMessageChatBottomFuncView.setGroupName(msgGroupName);
        mAudioBtn.setConversationId(conversationId);
        mAudioBtn.setRecordSendTel(msgDstNo);
        sendMsg(true, INIT_MESSAGE_LIST);
    }

    //    /**
    //     * @param
    //     * @return
    //     * @description 滑动到列表底部
    //     */
    //    private void scrollToBottom() {
    //        linearLayoutManager = new LinearLayoutManager(mContext);
    //        linearLayoutManager.setStackFromEnd(false);
    //        mChatRecyclerView.setLayoutManager(linearLayoutManager);
    //    }

    /**
     * @param qb
     * @return
     * @description 分页
     */
    private List<ConversationMsg> pagination(QueryBuilder<ConversationMsg> qb) {
        // 第一次加载，记录下各个数据
        if (isFirstLoad) {
            try {
                // 数据库中
                totalRecord = qb.count();
            } catch (Exception e) {
                totalRecord = 0;
            }
            //如果数据库总记录数小于默认加载的数，那么不需要分页，直接返回； 否则需要分页
            if (totalRecord <= DEFAULT_LOAD_SIZE) {
                totalPage = 0;
                currentPage = 0;
                return qb.list();
            } else {
                totalPage = ((totalRecord - DEFAULT_LOAD_SIZE) + PAGE_SIZE - 1) / PAGE_SIZE;
                currentPage = (int) totalPage;
            }
        }
        if (totalRecord <= 0) {
            return qb.list();
        }
        // 如果分页则先显示默认加载数，然后根据每次刷新逐步加载
        if (currentPage == totalPage) {
            qb.offset((int) (totalRecord - DEFAULT_LOAD_SIZE));
            qb.limit(DEFAULT_LOAD_SIZE);
        } else {
            //如果需要分页就按照分页的加载数据
            if (currentPage == 0) {
                qb.offset(0);
                qb.limit((int) totalRecord - (DEFAULT_LOAD_SIZE + ((int) totalPage - currentPage - 1) * PAGE_SIZE));
            } else {
                qb.offset((int) totalRecord - (DEFAULT_LOAD_SIZE + ((int) totalPage - currentPage) * PAGE_SIZE));
                qb.limit(PAGE_SIZE);
            }
        }
        return qb.list();
    }

    public boolean isGroupNo() {
        return isGroupNo;
    }


    /**
     * @param
     * @return 成功/失败
     * @description 检查环境
     */
    //    private boolean checkEnviroment() {
    //        boolean isSuccess=false;
    //        if(TextUtils.isEmpty(msgPcDstNumber)){
    //            isSuccess=false;
    //            ToastUtils.getToast().showMessage(MsgChatActivity.this, getString(R.string.msg_msg_call_number_empty),-1);
    //        }else if(!UctProxy.getInstance().isUserLogin()){
    //            isSuccess=false;
    //            ToastUtils.getToast().showMessage(MsgChatActivity.this, getString(R.string.msg_msg_send_error_1),-1);
    //        }else if(msgPcDstNumber.equals(loginInfo.getUserName())){
    //            isSuccess=false;
    //            ToastUtils.getToast().showMessage(MsgChatActivity.this, getString(R.string.msg_msg_call_error),-1);
    //        }else{
    //            isSuccess=true;
    //        }
    //        return isSuccess;
    //    }
    @Override
    public boolean handleMessage(final Message message) {
        super.handleMessage(message);

        switch (message.what) {
            case INIT_MESSAGE_LIST:
                // 标记所有消息为已读
                MessageDBManager.getInstance(mContext).updateReadStatus(conversationId);
                // 开始初始化加载
                isLoading = true;
                qb = MessageDBManager.getInstance(mContext).queryMessageBuilderById(conversationId);
                // 分页
                if (qb != null && qb.count() != 0) {
                    messageList = pagination(qb);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.addAll(messageList);
                        mChatRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                        // 加载结束
                        isLoading = false;
                        isFirstLoad = false;
                    }
                });
                break;
            case LOAD_MESSAGE_LIST:
                // 开始加载数据
                isLoading = true;
                // 加载数据前，当前页减一
                currentPage--;
                // 开始分页加载
                if (qb != null && qb.count() != 0) {
                    // 分页查询，查出将要加载的列表
                    messageList = pagination(qb);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 在第一个Item之前追加将要加载的列表
                            mAdapter.addMore(0, messageList);
                            isLoading = false;
                            mMessageChatSrl.setRefreshing(false);
                        }
                    });
                }
                break;
            case SEND_TEXT_MESSAGE:
                if (!UctClientApi.isUserOnline()) {
                    ToastUtils.getToast().showMessageShort(this, getString(R.string.msg_msg_send_error_3), -1);
                    msgContent = "";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mTextEt != null && mTextEt.getVisibility() == View.VISIBLE && !StrUtils.isEmpty(mTextEt.getText().toString())) {
                                mTextEt.setText("");
                            }
                        }
                    });
                    return false;
                }
                //                getConversationId();
                //                msgContent = "普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通普天宜通";
                msgContent = mTextEt.getText().toString();
                if (msgContent.length() <= 0) {
                    return false;
                }
                List<String> msgList = StrUtils.getSplitString(msgContent);
                //                final List<ConversationMsg> textList = new ArrayList<>();
                int size = msgList.size();
                int serInfo = size - 1;
                for (int i = 0; i < size; i++) {
                    String content = msgList.get(i);
                    String smsid = StrUtils.getSmsId(msgDstNo, msgSrcNo);
                    try {
                        _tmpContent = content.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    conversationMsg = new ConversationMsg();
                    // 保存Message信息到数据库中
                    //                    conversationMsg.setID(null);
                    conversationMsg.setMsgConversationId(conversationId);
                    conversationMsg.setMsgTime(StrUtils.getCurrentTimes());
                    conversationMsg.setMsgSrcNo(msgSrcNo);
                    conversationMsg.setMsgDstNo(msgDstNo);
                    //判断是不是一对多消息
                    if (isGroupNo) {
                        conversationMsg.setGroupNo(msgDstNo);
                    }
                    conversationMsg.setMsgUctId(smsid);
                    conversationMsg.setMsgType(MessageDBConstant.INFO_TYPE_TEXT);
                    conversationMsg.setMsgTxtSplit(serInfo);
                    conversationMsg.setRecvCfm(MessageDBConstant.NEEDLESS_CFM);
                    //                    conversationMsg.setRemoteMsgContent(null);
                    conversationMsg.setContent(content);
                    conversationMsg.setContentLength(_tmpContent.length);
                    conversationMsg.setMsgDirection(MessageDBConstant.IMVT_TO_MSG);
                    //                    conversationMsg.setLocalImgPath();
                    //                    conversationMsg.setRemoteImgPath();
                    conversationMsg.setMsgStatus(MessageDBConstant.MSG_STATUS_WAIT_SENDING);
                    //                    conversationMsg.setCfmType();
                    //                    conversationMsg.setResult();
                    conversationMsg.setReadStatus(MessageDBConstant.ALREAD_MSG);
                    conversationMsg.setRecvNotify(MessageDBConstant.BASIC_NOTIFY);
                    PrintLog.i("SEND_TEXT_MESSAGE--conversationId=" + conversationId + ", msgSrcNo=" + msgSrcNo + ", msgDstNo=" + msgDstNo
                            + ", groupNo=" + conversationMsg.getGroupNo() + ", smsid=" + smsid + ", msgType=" + conversationMsg.getMsgType()
                            + ", localImgPath=" + conversationMsg.getLocalImgPath() + ", msgContent=" + conversationMsg.getContent());
                    MessageManager.getInstane().sendMessage(conversationMsg);
                    //                    conversationMsg.setAudioLength();
                    //                    conversationMsg.setAudioPlayStatus();
                    //                    conversationMsg.setAudioReadStatus();
                    //                    MessageDBManager.getInstance(mContext).insertMessage(conversationMsg);
                    //                    // 发送Message请求
                    //                    int result = UctClientApi.P2PEastTone(msgDstNo,
                    //                            smsid,
                    //                            MessageDBConstant.INFO_TYPE_TEXT,
                    //                            serInfo,
                    //                            MessageDBConstant.NEEDLESS_CFM,
                    //                            MessageDBConstant.BASIC_NOTIFY,
                    //                            _tmpContent.length,
                    //                            content);
                    //                    if (result != 0) {
                    //                        ToastUtils.getToast().showMessageShort(MessageActivity.this, "发送短信失败" + result, -1);
                    //                    }
                    serInfo--;
                    //                    textList.add(conversationMsg);
                }
                msgContent = "";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mTextEt != null && mTextEt.getVisibility() == View.VISIBLE && !StrUtils.isEmpty(mTextEt.getText().toString())) {
                            mTextEt.setText("");
                        }
                    }
                });
                break;
            case SEND_AUDIO_MESSAGE:
                if (!UctClientApi.isUserOnline()) {
                    ToastUtils.getToast().showMessageShort(this, getString(R.string.msg_msg_send_error_3), -1);
                    return false;
                }
                //                getConversationId();
                String second = getStrArg(message, 0);
                String localPath = getStrArg(message, 1);
                String msgId = getStrArg(message, 2) + "_" + second;
                String remotePath = SDCardUtils.getChatRemotePath(msgDstNo, msgId, "mp3");
                msgContent = msgId + ".mp3";
                try {
                    _tmpContent = (msgContent).getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                conversationMsg = new ConversationMsg();
                //                conversationMsg.setID(null);
                conversationMsg.setMsgConversationId(conversationId);
                conversationMsg.setMsgTime(StrUtils.getCurrentTimes());
                conversationMsg.setMsgSrcNo(msgSrcNo);
                conversationMsg.setMsgDstNo(msgDstNo);
                //判断是不是一对多消息
                if (isGroupNo) {
                    conversationMsg.setGroupNo(msgDstNo);
                }
                conversationMsg.setMsgUctId(msgId);
                conversationMsg.setMsgType(MessageDBConstant.INFO_TYPE_AUDIO);
                conversationMsg.setMsgTxtSplit(MessageDBConstant.UNSEGMENTED_MSG);
                conversationMsg.setRecvCfm(MessageDBConstant.NEEDLESS_CFM);
                //                    conversationMsg.setRemoteMsgContent(null);
                conversationMsg.setContent(msgContent);
                conversationMsg.setContentLength(_tmpContent.length);
                conversationMsg.setMsgDirection(MessageDBConstant.IMVT_TO_MSG);
                conversationMsg.setLocalImgPath(localPath);
                conversationMsg.setRemoteImgPath(remotePath);
                conversationMsg.setMsgStatus(MessageDBConstant.MSG_STATUS_WAIT_SENDING);
                //                    conversationMsg.setCfmType();
                //                    conversationMsg.setResult();
                conversationMsg.setReadStatus(MessageDBConstant.ALREAD_MSG);
                conversationMsg.setRecvNotify(MessageDBConstant.BASIC_NOTIFY);
                conversationMsg.setAudioLength(Integer.parseInt(second));
                conversationMsg.setAudioPlayStatus(MessageDBConstant.AUDIO_STOP_STATUS);
                conversationMsg.setAudioReadStatus(MessageDBConstant.AUDIO_ALREAD_MSG);
                PrintLog.i("SEND_AUDIO_MESSAGE--conversationId=" + conversationId + ", msgSrcNo=" + msgSrcNo + ", msgDstNo=" + msgDstNo
                        + ", groupNo=" + conversationMsg.getGroupNo() + ", smsid=" + msgId + ", msgType=" + conversationMsg.getMsgType()
                        + ", localImgPath=" + conversationMsg.getLocalImgPath() + ", remotePath=" + conversationMsg.getRemoteImgPath()
                        + ", msgContent=" + conversationMsg.getContent());
                MessageManager.getInstane().sendMessage(conversationMsg);
                msgContent = "";
                break;
            case SEND_PHOTO_MESSAGE:
                if (!UctClientApi.isUserOnline()) {
                    ToastUtils.getToast().showMessageShort(this, getString(R.string.msg_msg_send_error_3), -1);
                    return false;
                }
                for (MessagePhotoEntity mEntry : photoSendList) {
                    conversationMsg = new ConversationMsg();
                    String smsid = "";
                    // 后缀名，不带点
                    String prefix = FileUtils.getSuffix(mEntry.getPath());
                    if (mEntry.getType() == MessageDBConstant.INFO_TYPE_IMAGE) {
                        smsid = StrUtils.getSmsId(msgDstNo, msgSrcNo) + "_" + FileUtils.getFileSizeUnit(mEntry.getSize());
                        //                        if (prefix.contains("gif")) {
                        //                            conversationMsg.setMsgThumbnail(MessageDBConstant.MSG_ORIGINAL_IMAGE);
                        //                        } else {
                        conversationMsg.setMsgThumbnail(isThumbnail ? MessageDBConstant.MSG_THUMBNAIL_IMAGE : MessageDBConstant.MSG_ORIGINAL_IMAGE);
                        //                        }
                    } else {
                        smsid = StrUtils.getSmsId(msgDstNo, msgSrcNo) + "_" + (mEntry.getDuring() / 1000);
                    }
                    //                    String prefix = FileUtils.getFilePrefix(mEntry.getLocalPath());
                    //                    String imageFileName = SDCardUtils.getChatImageFileName(msgDstNo, prefix);
                    //                    String remotePath1 = SDCardUtils.getChatImageRemotePath(msgDstNo) + imageFileName;
                    //                    msgContent = remotePath1 + ":" + mEntry.getLocalPath();
                    // 服务器路径
                    String remotePath1 = SDCardUtils.getChatRemotePath(msgDstNo, smsid, prefix);
                    // 服务器文件名称:本地文件名称
                    msgContent = smsid + "." + prefix + FileUtils.COLON + FileUtils.getFileNameFromPath(mEntry.getPath());
                    if (StrUtils.isEmpty(msgContent)) {
                        msgContent = "";
                    }
                    try {
                        _tmpContent = msgContent.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //                conversationMsg.setID(null);
                    conversationMsg.setMsgConversationId(conversationId);
                    conversationMsg.setMsgTime(StrUtils.getCurrentTimes());
                    conversationMsg.setMsgSrcNo(msgSrcNo);
                    conversationMsg.setMsgDstNo(msgDstNo);
                    //判断是不是一对多消息
                    if (isGroupNo) {
                        conversationMsg.setGroupNo(msgDstNo);
                    }
                    conversationMsg.setMsgUctId(smsid);
                    conversationMsg.setMsgType(mEntry.getType());
                    conversationMsg.setMsgTxtSplit(MessageDBConstant.UNSEGMENTED_MSG);
                    conversationMsg.setRecvCfm(MessageDBConstant.NEEDLESS_CFM);
                    //                    conversationMsg.setRemoteMsgContent(null);
                    conversationMsg.setContent(msgContent);
                    conversationMsg.setContentLength(_tmpContent.length);
                    conversationMsg.setMsgDirection(MessageDBConstant.IMVT_TO_MSG);
                    conversationMsg.setLocalImgPath(mEntry.getPath());
                    conversationMsg.setRemoteImgPath(remotePath1);
                    conversationMsg.setMsgStatus(MessageDBConstant.MSG_STATUS_WAIT_SENDING);
                    //                    conversationMsg.setCfmType();
                    //                    conversationMsg.setResult();
                    conversationMsg.setReadStatus(MessageDBConstant.ALREAD_MSG);
                    conversationMsg.setRecvNotify(MessageDBConstant.BASIC_NOTIFY);
                    //                conversationMsg.setAudioLength(Integer.parseInt(second));
                    //                conversationMsg.setAudioPlayStatus(MessageDBConstant.AUDIO_STOP_STATUS);
                    //                conversationMsg.setAudioReadStatus(MessageDBConstant.AUDIO_ALREAD_MSG);
                    PrintLog.i("SEND_PHOTO_MESSAGE--conversationId=" + conversationId + ", msgSrcNo=" + msgSrcNo + ", msgDstNo=" + msgDstNo
                            + ", groupNo=" + conversationMsg.getGroupNo() + ", smsid=" + smsid + ", msgType=" + conversationMsg.getMsgType()
                            + ", localImgPath=" + conversationMsg.getLocalImgPath() + ", remotePath=" + conversationMsg.getRemoteImgPath()
                            + ", msgContent=" + conversationMsg.getContent() + ", msgThumbnail=" + conversationMsg.getMsgThumbnail());
                    MessageManager.getInstane().sendMessage(conversationMsg);
                }
                msgContent = "";
                break;
            case SEND_FILE_MESSAGE:
                if (!UctClientApi.isUserOnline()) {
                    ToastUtils.getToast().showMessageShort(this, getString(R.string.msg_msg_send_error_3), -1);
                    return false;
                }
                for (MessageFileListEntity mEntry : fileSendList) {
                    //                    String smsid = StrUtils.getSmsId(msgDstNo, msgSrcNo) + "_" + mEntry.getSize() + ":" + mEntry.getSuffixName();
                    String smsid = StrUtils.getSmsId(msgDstNo, msgSrcNo) + "_" + FileUtils.getFileSizeUnit(mEntry.getSize());
                    String prefix = FileUtils.getSuffix(mEntry.getPath());
                    // 服务器路径
                    String remotePath1 = SDCardUtils.getChatRemotePath(msgDstNo, smsid, prefix);
                    // 服务器文件名称:本地文件名称
                    msgContent = smsid + "." + prefix + FileUtils.COLON + FileUtils.getFileNameFromPath(mEntry.getPath());
                    if (StrUtils.isEmpty(msgContent)) {
                        msgContent = "";
                    }
                    try {
                        _tmpContent = (msgContent).getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    conversationMsg = new ConversationMsg();
                    //                conversationMsg.setID(null);
                    conversationMsg.setMsgConversationId(conversationId);
                    conversationMsg.setMsgTime(StrUtils.getCurrentTimes());
                    conversationMsg.setMsgSrcNo(msgSrcNo);
                    conversationMsg.setMsgDstNo(msgDstNo);
                    //判断是不是一对多消息
                    if (isGroupNo) {
                        conversationMsg.setGroupNo(msgDstNo);
                    }
                    conversationMsg.setMsgUctId(smsid);
                    conversationMsg.setMsgType(MessageDBConstant.INFO_TYPE_FILE);
                    conversationMsg.setMsgTxtSplit(MessageDBConstant.UNSEGMENTED_MSG);
                    conversationMsg.setRecvCfm(MessageDBConstant.NEEDLESS_CFM);
                    //                    conversationMsg.setRemoteMsgContent(null);
                    conversationMsg.setContent(msgContent);
                    conversationMsg.setContentLength(_tmpContent.length);
                    conversationMsg.setMsgDirection(MessageDBConstant.IMVT_TO_MSG);
                    conversationMsg.setLocalImgPath(mEntry.getPath());
                    conversationMsg.setRemoteImgPath(remotePath1);
                    conversationMsg.setMsgStatus(MessageDBConstant.MSG_STATUS_WAIT_SENDING);
                    //                    conversationMsg.setCfmType();
                    //                    conversationMsg.setResult();
                    conversationMsg.setReadStatus(MessageDBConstant.ALREAD_MSG);
                    conversationMsg.setRecvNotify(MessageDBConstant.BASIC_NOTIFY);
                    //                conversationMsg.setAudioLength(Integer.parseInt(second));
                    //                conversationMsg.setAudioPlayStatus(MessageDBConstant.AUDIO_STOP_STATUS);
                    //                conversationMsg.setAudioReadStatus(MessageDBConstant.AUDIO_ALREAD_MSG);
                    PrintLog.i("SEND_FILE_MESSAGE--conversationId=" + conversationId + ", msgSrcNo=" + msgSrcNo + ", msgDstNo=" + msgDstNo
                            + ", groupNo=" + conversationMsg.getGroupNo() + ", smsid=" + smsid + ", msgType=" + conversationMsg.getMsgType()
                            + ", localImgPath=" + conversationMsg.getLocalImgPath() + ", remotePath=" + conversationMsg.getRemoteImgPath()
                            + ", msgContent=" + conversationMsg.getContent());
                    MessageManager.getInstane().sendMessage(conversationMsg);
                }
                msgContent = "";
                break;
            case SEND_MY_LOCATION_MESSAGE:
                if (!UctClientApi.isUserOnline()) {
                    ToastUtils.getToast().showMessageShort(this, getString(R.string.msg_msg_send_error_3), -1);
                    return false;
                }
                String smsid = StrUtils.getSmsId(msgDstNo, msgSrcNo) + "_" + myLocationSendEntity.getSize();
                //                    String prefix = FileUtils.getFilePrefix(mEntry.getLocalPath());
                //                    String imageFileName = SDCardUtils.getChatImageFileName(msgDstNo, prefix);
                //                    String remotePath1 = SDCardUtils.getChatImageRemotePath(msgDstNo) + imageFileName;
                //                    msgContent = remotePath1 + ":" + mEntry.getLocalPath();
                String prefix = FileUtils.getSuffix(myLocationSendEntity.getLocalPath());
                // 服务器路径
                String remotePath1 = SDCardUtils.getChatRemotePath(msgDstNo, smsid, prefix);
                // 服务器路径:本地路径
                //                msgContent = smsid + "." + prefix + FileUtils.COLON + myLocationSendEntity.getLocalPath();
                myLocationSendEntity.setRemotePath(remotePath1);
                msgContent = JSON.toJSONString(myLocationSendEntity);

                if (StrUtils.isEmpty(msgContent)) {
                    msgContent = "";
                }
                try {
                    _tmpContent = msgContent.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                conversationMsg = new ConversationMsg();
                //                conversationMsg.setID(null);
                conversationMsg.setMsgConversationId(conversationId);
                conversationMsg.setMsgTime(StrUtils.getCurrentTimes());
                conversationMsg.setMsgSrcNo(msgSrcNo);
                conversationMsg.setMsgDstNo(msgDstNo);
                //判断是不是一对多消息
                if (isGroupNo) {
                    conversationMsg.setGroupNo(msgDstNo);
                }
                conversationMsg.setMsgUctId(smsid);
                conversationMsg.setMsgType(myLocationSendEntity.getType());
                conversationMsg.setMsgTxtSplit(MessageDBConstant.UNSEGMENTED_MSG);
                conversationMsg.setRecvCfm(MessageDBConstant.NEEDLESS_CFM);
                //                conversationMsg.setRemoteMsgContent(myLocationSendEntity.getLatitude() +
                //                        "," + myLocationSendEntity.getLongitude() +
                //                        "," + myLocationSendEntity.getPlaceName() +
                //                        "," + myLocationSendEntity.getSpecificPlaceName() +
                //                        "," + myLocationSendEntity.getZoom());
                conversationMsg.setContent(msgContent);
                conversationMsg.setContentLength(_tmpContent.length);
                conversationMsg.setMsgDirection(MessageDBConstant.IMVT_TO_MSG);
                conversationMsg.setLocalImgPath(myLocationSendEntity.getLocalPath());
                conversationMsg.setRemoteImgPath(remotePath1);
                conversationMsg.setMsgStatus(MessageDBConstant.MSG_STATUS_WAIT_SENDING);
                //                    conversationMsg.setCfmType();
                //                    conversationMsg.setResult();
                conversationMsg.setReadStatus(MessageDBConstant.ALREAD_MSG);
                conversationMsg.setRecvNotify(MessageDBConstant.BASIC_NOTIFY);
                //                conversationMsg.setAudioLength(Integer.parseInt(second));
                //                conversationMsg.setAudioPlayStatus(MessageDBConstant.AUDIO_STOP_STATUS);
                //                conversationMsg.setAudioReadStatus(MessageDBConstant.AUDIO_ALREAD_MSG);
                PrintLog.i("SEND_MY_LOCATION_MESSAGE--conversationId=" + conversationId + ", msgSrcNo=" + msgSrcNo + ", msgDstNo=" + msgDstNo
                        + ", groupNo=" + conversationMsg.getGroupNo() + ", smsid=" + smsid + ", msgType=" + conversationMsg.getMsgType()
                        + ", localImgPath=" + conversationMsg.getLocalImgPath() + ", remotePath=" + conversationMsg.getRemoteImgPath()
                        + ", msgContent=" + conversationMsg.getContent());
                MessageManager.getInstane().sendMessage(conversationMsg);
                msgContent = "";
                break;
            case DELAYED_TO_SCROLL_TO_POSITION:
                // 防止输入法还没弹出来就滚动，导致无法滚动到底部
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChatRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                    }
                });
                break;
        }
        return false;
    }

    @Override
    public void onRefresh() {
        // 如果已经在加载，就返回
        if (isLoading) {
            mMessageChatSrl.setRefreshing(false);
            return;
        }

        // 必须超出DEFAULT_LOAD_SIZE（默认加载数）才开始加载数据
        if (currentPage > 0) {
            sendMsg(true, LOAD_MESSAGE_LIST);
        } else {
            mMessageChatSrl.setRefreshing(false);
        }
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        switch (view.getId()) {
            // 切换文本与语音按钮
            case R.id.txt_or_audio_btn:
                // 切换出语音按钮
                if (isShowAudio) {
                    //                    mTxtOrAudioBtn.setText("键盘");
                    //                    mKeyBoardOrFaceBtn.setText("表情");
                    //                    mSendOrFuncBtn.setText("显示");
                    mTxtOrAudioBtn.setBackgroundResource(R.mipmap.icon_keyboard);
                    mKeyBoardOrFaceBtn.setBackgroundResource(R.mipmap.icon_expression);
                    mSendOrFuncBtn.setBackgroundResource(R.mipmap.icon_add);
                    mSendOrFuncBtn.setText("");
                    isShowSendBtn = false;
                    KeyBoardUtils.closeKeybord(mContext, mTextEt);
                    mTextEt.setVisibility(View.GONE);
                    mAudioBtn.setVisibility(View.VISIBLE);
                    mTextEt.setFocusable(false);
                    mMessageChatBottomFuncView.switchFragement(0);
                    isShowAudio = false;
                    isShowFace = true;
                    isShowFile = true;
                    // 切换出文本编辑
                } else {
                    //                    mTxtOrAudioBtn.setText("语音");
                    //                    mKeyBoardOrFaceBtn.setText("表情");
                    mTxtOrAudioBtn.setBackgroundResource(R.mipmap.icon_voice);
                    mKeyBoardOrFaceBtn.setBackgroundResource(R.mipmap.icon_expression);
                    if (TextUtils.isEmpty(mTextEt.getText().toString())) {
                        isShowSendBtn = false;
                    } else {
                        isShowSendBtn = true;
                    }
                    if (isShowSendBtn) {
                        mSendOrFuncBtn.setBackgroundResource(R.drawable.shape_message_bottom_send);
                        mSendOrFuncBtn.setText(getString(R.string.string_message_action_send));
                    } else {
                        //                        mSendOrFuncBtn.setText("显示");
                        mSendOrFuncBtn.setBackgroundResource(R.mipmap.icon_add);
                        mSendOrFuncBtn.setText("");
                    }
                    mMessageChatBottomFuncView.switchFragement(0);
                    mAudioBtn.setVisibility(View.GONE);
                    mTextEt.setVisibility(View.VISIBLE);
                    mTextEt.setFocusable(true);
                    mTextEt.setFocusableInTouchMode(true);
                    mTextEt.requestFocus();
                    mTextEt.findFocus();
                    KeyBoardUtils.openKeybord(mContext, mTextEt);
                    isShowAudio = true;
                    isShowFace = true;
                    isShowFile = true;
                }
                //                scrollToBottom();
                sendMsgDelayed(true, DELAYED_TO_SCROLL_TO_POSITION, 100);
                break;

            // 切换键盘与表情按钮
            case R.id.keyboard_or_face_btn:
                // 切换出表情
                if (isShowFace) {
                    //                    mTxtOrAudioBtn.setText("语音");
                    //                    mKeyBoardOrFaceBtn.setText("键盘");
                    mTxtOrAudioBtn.setBackgroundResource(R.mipmap.icon_voice);
                    mKeyBoardOrFaceBtn.setBackgroundResource(R.mipmap.icon_keyboard);
                    if (TextUtils.isEmpty(mTextEt.getText().toString())) {
                        isShowSendBtn = false;
                    } else {
                        isShowSendBtn = true;
                    }
                    if (isShowSendBtn) {
                        mSendOrFuncBtn.setBackgroundResource(R.drawable.shape_message_bottom_send);
                        mSendOrFuncBtn.setText(getString(R.string.string_message_action_send));
                    } else {
                        //                        mSendOrFuncBtn.setText("显示");
                        mSendOrFuncBtn.setBackgroundResource(R.mipmap.icon_add);
                        mSendOrFuncBtn.setText("");
                    }
                    mAudioBtn.setVisibility(View.GONE);
                    mTextEt.setVisibility(View.VISIBLE);
                    KeyBoardUtils.closeKeybord(mContext, mTextEt);
                    //                    mTextEt.setFocusable(false);
                    mMessageChatBottomFuncView.switchFragement(1);
                    isShowAudio = true;
                    isShowFace = false;
                    isShowFile = true;
                    // 切换出键盘
                } else {
                    //                    mTxtOrAudioBtn.setText("语音");
                    //                    mKeyBoardOrFaceBtn.setText("表情");
                    mTxtOrAudioBtn.setBackgroundResource(R.mipmap.icon_voice);
                    mKeyBoardOrFaceBtn.setBackgroundResource(R.mipmap.icon_expression);
                    if (TextUtils.isEmpty(mTextEt.getText().toString())) {
                        isShowSendBtn = false;
                    } else {
                        isShowSendBtn = true;
                    }
                    if (isShowSendBtn) {
                        mSendOrFuncBtn.setBackgroundResource(R.drawable.shape_message_bottom_send);
                        mSendOrFuncBtn.setText(getString(R.string.string_message_action_send));
                    } else {
                        //                        mSendOrFuncBtn.setText("显示");
                        mSendOrFuncBtn.setBackgroundResource(R.mipmap.icon_add);
                        mSendOrFuncBtn.setText("");
                    }
                    mMessageChatBottomFuncView.switchFragement(0);
                    mAudioBtn.setVisibility(View.GONE);
                    mTextEt.setVisibility(View.VISIBLE);
                    mTextEt.setFocusable(true);
                    mTextEt.setFocusableInTouchMode(true);
                    mTextEt.requestFocus();
                    mTextEt.findFocus();
                    KeyBoardUtils.openKeybord(mContext, mTextEt);
                    isShowAudio = true;
                    isShowFace = true;
                    isShowFile = true;
                }
                //                scrollToBottom();
                sendMsgDelayed(true, DELAYED_TO_SCROLL_TO_POSITION, 100);
                break;

            // 打开/隐藏文件列表按钮，发送按钮
            case R.id.send_or_func_btn:
                // 切换出文件选择
                if (isShowFile && !isShowSendBtn) {
                    //                    mTxtOrAudioBtn.setText("语音");
                    //                    mKeyBoardOrFaceBtn.setText("表情");
                    //                    mSendOrFuncBtn.setText("隐藏");
                    mTxtOrAudioBtn.setBackgroundResource(R.mipmap.icon_voice);
                    mKeyBoardOrFaceBtn.setBackgroundResource(R.mipmap.icon_expression);
                    mSendOrFuncBtn.setBackgroundResource(R.mipmap.icon_add);
                    mSendOrFuncBtn.setText("");
                    mAudioBtn.setVisibility(View.GONE);
                    mTextEt.setVisibility(View.VISIBLE);
                    KeyBoardUtils.closeKeybord(mContext, mTextEt);
                    //                    mMessageChatBottomFuncView.requestFocus();
                    mTextEt.setFocusable(false);
                    mMessageChatBottomFuncView.switchFragement(2);
                    isShowAudio = true;
                    isShowFace = true;
                    isShowFile = false;
                    // 切换出键盘
                } else if (!isShowFile && !isShowSendBtn) {
                    //                    mTxtOrAudioBtn.setText("语音");
                    //                    mKeyBoardOrFaceBtn.setText("表情");
                    mTxtOrAudioBtn.setBackgroundResource(R.mipmap.icon_voice);
                    mKeyBoardOrFaceBtn.setBackgroundResource(R.mipmap.icon_expression);
                    if (TextUtils.isEmpty(mTextEt.getText().toString())) {
                        //                        mSendOrFuncBtn.setText("显示");
                        mSendOrFuncBtn.setBackgroundResource(R.mipmap.icon_add);
                        mSendOrFuncBtn.setText("");
                        isShowSendBtn = false;
                    } else {
                        mSendOrFuncBtn.setBackgroundResource(R.drawable.shape_message_bottom_send);
                        mSendOrFuncBtn.setText(getString(R.string.string_message_action_send));
                        isShowSendBtn = true;
                    }
                    mMessageChatBottomFuncView.switchFragement(0);
                    mAudioBtn.setVisibility(View.GONE);
                    mTextEt.setVisibility(View.VISIBLE);
                    mTextEt.setFocusable(true);
                    mTextEt.setFocusableInTouchMode(true);
                    mTextEt.requestFocus();
                    mTextEt.findFocus();
                    KeyBoardUtils.openKeybord(mContext, mTextEt);
                    isShowAudio = true;
                    isShowFace = true;
                    isShowFile = true;
                    // 切换出发送按钮
                } else {
                    if (mTextEt.getText().toString().trim().equals("")) {
                        mTextEt.setText("");
                        msgContent = "";
                        ToastUtils.getToast().showMessageShort(this, getString(R.string.msg_msg_send_error_8), -1);
                    }
                    sendMsg(true, SEND_TEXT_MESSAGE);
                }
                //                scrollToBottom();
                sendMsgDelayed(true, DELAYED_TO_SCROLL_TO_POSITION, 100);
                break;
            // 输入框
            case R.id.text_et:
                mKeyBoardOrFaceBtn.setBackgroundResource(R.mipmap.icon_expression);
                if (TextUtils.isEmpty(mTextEt.getText().toString())) {
                    mSendOrFuncBtn.setBackgroundResource(R.mipmap.icon_add);
                    mSendOrFuncBtn.setText("");
                    isShowSendBtn = false;
                } else {
                    mSendOrFuncBtn.setBackgroundResource(R.drawable.shape_message_bottom_send);
                    mSendOrFuncBtn.setText(getString(R.string.string_message_action_send));
                    isShowSendBtn = true;
                }
                mMessageChatBottomFuncView.switchFragement(0);
                mTextEt.setFocusable(true);
                mTextEt.setFocusableInTouchMode(true);
                mTextEt.requestFocus();
                mTextEt.findFocus();
                KeyBoardUtils.openKeybord(mContext, mTextEt);
                isShowAudio = true;
                isShowFace = true;
                isShowFile = true;
                //                scrollToBottom();
                sendMsgDelayed(true, DELAYED_TO_SCROLL_TO_POSITION, 100);
                break;
            // 进入组成员列表
            case R.id.iv_right:
                Intent intent = new Intent(MessageActivity.this, GMemberListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("groupId", msgGroupNo);
                intent.putExtra("groupName", msgGroupName);
                startActivity(intent);
                break;

            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onEvent(EventBean eventBean) {
        if (eventBean.getAction().equals(ConstantUtils.ACTION_INSERT_CONTACT)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onEvent(int what, Bundle data, Object object) {
        //插入表情
        if (what == FaceFragment.FACE_WHAT) {
            //处理删除键
            if (object.equals("del_normal")) {
                mTextEt.requestFocus();
                mTextEt.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                mTextEt.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                return;
            }
            mTextEt.getText().insert(mTextEt.getSelectionStart(), StrUtils.faceHandler(mContext, object + ""));
            PrintLog.i("msgContent" + msgContent);
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    private boolean hasReplaced = false;
    private int selectionStart;

    @Override
    public void afterTextChanged(Editable s) {
        if (mTextEt != null) {
            msgContent = mTextEt.getText().toString();
            if (TextUtils.isEmpty(msgContent)) {
                mSendOrFuncBtn.setBackgroundResource(R.mipmap.icon_add);
                mSendOrFuncBtn.setText("");
                isShowSendBtn = false;
            } else {
                if (!hasReplaced) {
                    hasReplaced = true;
                    mSendOrFuncBtn.setBackgroundResource(R.drawable.shape_message_bottom_send);
                    mSendOrFuncBtn.setText(getString(R.string.string_message_action_send));
                    isShowSendBtn = true;
                    selectionStart = mTextEt.getSelectionStart();
                    mTextEt.setText(StrUtils.faceHandler(mContext, msgContent));
                } else {
                    hasReplaced = false;
                    mTextEt.setSelection(selectionStart);
                }
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.listview:
                KeyBoardUtils.closeKeybord(mContext, mTextEt);
                mMessageChatBottomFuncView.setVisibility(View.GONE);
                break;
        }

        return false;
    }

    /**
     * Description: 录音完成
     *
     * @param seconds
     * @param filePath
     * @param msgId
     */
    @Override
    public void onRecordFinished(long seconds, String filePath, String msgId) {
        //        boolean isSuccess = checkEnviroment();
        //        if(isSuccess){
        String rang = String.format("%02d", seconds);
        sendMsg(false, SEND_AUDIO_MESSAGE, 0, "" + rang, filePath, msgId);
        //        }
    }

    @Override
    public void insertMessageAdapter(final ConversationMsg conversationMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.addItem(conversationMsg);
                if (conversationMsg.getMsgDirection() == MessageDBConstant.IMVT_TO_MSG) {
                    //                    scrollToBottom();
                    mChatRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                } else {
                    if (isBottom) {
                        //                        scrollToBottom();
                        mChatRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                    }
                }
            }
        });
    }


    //    private int index = -1;
    //
    //    @Override
    //    public synchronized void updateMessageAdapter(final ConversationMsg conversationMsg) {
    //        List<ConversationMsg> datas = mAdapter.getDatas();
    //        Log.e("shafeia", "updateMessageAdapter datas = " + datas.size());
    //        for (int i = datas.size() - 1; i < datas.size() && i >= 0; i--) {
    //            if (datas.get(i).getMsgUctId().equals(conversationMsg.getMsgUctId())) {
    //                index = i;
    //                runOnUiThread(new Runnable() {
    //                    @Override
    //                    public void run() {
    //                        mAdapter.updateItem(index, conversationMsg);
    //                        Log.e("shafeia", "index = " + index + ", conversationMsg = " + conversationMsg.getLocalImgPath());
    //                    }
    //                });
    //                break;
    //            }
    //        }
    //
    //    }

    @Override
    public void updateMessageAdapter(final ConversationMsg conversationMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<ConversationMsg> datas = mAdapter.getDatas();
                PrintLog.i("获得datas 总计 = " + datas.size());
                for (int i = datas.size() - 1; i < datas.size() && i >= 0; i--) {
                    if (datas.get(i).getMsgUctId().equals(conversationMsg.getMsgUctId())) {
                        PrintLog.i("MsgUctId = " + conversationMsg.getMsgUctId() + "，总计 = " + datas.size() + " 更新位置 = " + i);
                        mAdapter.updateItem(i, conversationMsg);
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void removeMessageAdapter(final int position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.removeItem2(position);
                PrintLog.i("移除后总计 = " + mAdapter.getDatas().size() + ",  移除位置 = " + position);
            }
        });
    }

    @Override
    public String getMsgDstNo() {
        return msgDstNo;
    }

    @Override
    public int notifyDataChangedListener(ConversationMsg mConversationMsg, int type) {
        //        switch (mConversationMsg.getMsgType()) {
        //            case MessageDBConstant.INFO_TYPE_TEXT:
        //            case MessageDBConstant.INFO_TYPE_OLD_DEVICE_TEXT:


        //        if ((isGroupNo && msgDstNo.equals(mConversationMsg.getGroupNo()))// 如果是组聊天，接收消息的组号码即是我们发送的目的号码
        //                || (!isGroupNo && msgDstNo.equals(mConversationMsg.getMsgSrcNo()))) {// 如果是一对一聊天，对方的号码即是我们发送的目的号码
        PrintLog.i("notifyDataChangedListener");
        if (mConversationMsg.getMsgConversationId().longValue() == conversationId.longValue()) {
            switch (type) {
                case MsgCallBackListener.MSG_SEND_INSERT_DB:
                    PrintLog.i("MSG_SEND_INSERT_DB");
                    if (mConversationMsg.getMsgDirection() == MessageDBConstant.IMVT_COM_MSG) {// 在聊天界面，更新接收消息为已读
                        if (mConversationMsg.getReadStatus() == null || mConversationMsg.getReadStatus() == MessageDBConstant.UNREAD_MSG) {
                            mConversationMsg.setReadStatus(MessageDBConstant.ALREAD_MSG);
                            MessageDBManager.getInstance(mContext).updateMessage(mConversationMsg);// 更新状态为已读
                        }
                    }
                    insertMessageAdapter(mConversationMsg);
                    break;
                case MsgCallBackListener.MSG_STATUS_CHANGE:
                    PrintLog.i("MSG_STATUS_CHANGE--status = " + mConversationMsg.getMsgStatus());
                    updateMessageAdapter(mConversationMsg);
                    break;
                case MsgCallBackListener.MSG_EASTONECFM:
                    PrintLog.i("MSG_EASTONECFM");
                    updateMessageAdapter(mConversationMsg);
                    break;
                case MsgCallBackListener.MSG_UPDATE_PROGRESS:
                    PrintLog.i("MSG_EASTONECFM--offsize = " + mConversationMsg.getOffSize() + ", filesize = " + mConversationMsg.getFileSize());
                    // 小于500ms不刷新
                    if (!AppUtils.isFastClick()) {
                        updateMessageAdapter(mConversationMsg);
                    }
                    break;
            }
        }
        return 0;

    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD_MR1)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        PrintLog.i("接到数据啦");
        switch (resultCode) {
            case PHOTO_RESULT_CODE:
                photoSendList.clear();
                photoSendList = data.getParcelableArrayListExtra("PhotoList");
                PrintLog.i("photoSendList.size = " + photoSendList.size());
                isThumbnail = data.getBooleanExtra("isThumbnail", false);
                sendMsg(true, SEND_PHOTO_MESSAGE);
                break;
            case FILE_RESULT_CODE:
                fileSendList.clear();
                fileSendList = (List<MessageFileListEntity>) data.getSerializableExtra("FileList");
                PrintLog.i("fileSendList.size = " + fileSendList.size());
                sendMsg(true, SEND_FILE_MESSAGE);
                break;
            case LOCATION_RESULT_CODE:
                myLocationSendEntity = data.getParcelableExtra("MyLocationEntity");
                sendMsg(true, SEND_MY_LOCATION_MESSAGE);
                break;
            case MAP_RESULT_CODE:
                break;
            case GCALL_RESULT_CODE:
                break;
            case RESULT_OK:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {// Android5.0以下(不包含)使用系统照相机拍照、录像
                    photoSendList.clear();
                    String mPath = mMessageChatBottomFuncView.getPath();
                    File file = new File(mPath);
                    if (file != null && file.exists()) {
                        FileInputStream fis;
                        try {
                            fis = new FileInputStream(file);
                            MessagePhotoEntity mEntity = new MessagePhotoEntity();
                            mEntity.setPath(mPath);
                            mEntity.setSize((long) (fis.available()));
                            String suffix = mPath.substring(mPath.lastIndexOf("."), mPath.length());
                            if (".jpg".equals(suffix)) {
                                mEntity.setType(MessageDBConstant.INFO_TYPE_IMAGE);
                            } else if (".mp4".equals(suffix)) {
                                mEntity.setType(MessageDBConstant.INFO_TYPE_CAMERA_VIDEO);
                                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                                mmr.setDataSource(mPath);
                                String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                mEntity.setDuring(Long.parseLong(duration));
                            }
                            photoSendList.add(mEntity);
                            FileUtils.scanFile(mContext, mPath);
                            sendMsg(true, SEND_PHOTO_MESSAGE);
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.getToast().showMessageShort(this, getString(R.string.string_message_file_error1), -1);
                        }
                    } else {
                        ToastUtils.getToast().showMessageShort(this, getString(R.string.string_message_file_error2), -1);
                    }
                }
                break;
            case RESULT_CANCELED:
                break;
            case RESULT_FIRST_USER:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PrintLog.i("onPause");

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        PrintLog.i("onResume");

    }

    @Override
    protected void onStart() {
        super.onStart();
        PrintLog.i("onStart");
    }

    @Override
    protected void onStop() {
        KeyBoardUtils.closeKeybord(getApplicationContext(), mTextEt);
        MessageAudioPlayerManager.getInstance(mContext, mAdapter).release();
        super.onStop();
        PrintLog.i("onStop");
    }

    @Override
    protected void onDestroy() {
        PrintLog.i("onDestroy");
        PrintLog.i("unRegisterObserver BaseServiceCallBack.INDEX_IMESSAGEVIEW");
        MessageManager.getInstane().unRegisterObserver(this, BaseServiceCallBack.INDEX_IMESSAGEVIEW);
        ImageLoader.getInstance().clearMemoryCache();
        ImageLoader.getInstance().clearDiskCache();
        super.onDestroy();
    }


}
