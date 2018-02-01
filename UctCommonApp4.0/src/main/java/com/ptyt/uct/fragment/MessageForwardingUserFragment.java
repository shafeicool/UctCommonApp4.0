package com.ptyt.uct.fragment;

import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.uct.ContactCallBack;
import com.android.uct.bean.ContactUser;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.CreateGroupAndMessageForwardActivity;
import com.ptyt.uct.adapter.MessageForwardingUserAdapter;
import com.ptyt.uct.adapter.SelectUserMemberAdapter;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.entity.Contact;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.entity.MessageMyLocationEntity;
import com.ptyt.uct.model.ContactDBManager;
import com.ptyt.uct.services.MessageManager;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.KeyBoardUtils;
import com.ptyt.uct.utils.SDCardUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.widget.DividerLine;
import com.ptyt.uct.widget.SwipeRefreshView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * @Description:
 * @Date: 2017/9/11
 * @Author: ShaFei
 * @Version:V1.0
 */

public class MessageForwardingUserFragment extends BaseFragment implements
        TextWatcher,
        MessageForwardingUserAdapter.OnSelectedMemberChangedListener,
        SelectUserMemberAdapter.OnRemoveMemberChangedListener,
        View.OnClickListener,
        View.OnFocusChangeListener,
        SwipeRefreshView.OnLoadMoreListener,
        SwipeRefreshView.OnRefreshListener {

    private FragmentActivity mContext;
    private RecyclerView recyclerView;
    private TextView tv_rightButton;

    private LinearLayoutManager linearLayoutManager;
    private EditText et_search;
    private TextView tv_selectedMemberNum;
    private RecyclerView recyclerView_selectedMember;
    private MessageForwardingUserAdapter gMemberAdapter;
    private SelectUserMemberAdapter selectUserMemberAdapter;
    private EditText et_name;
    private ImageView iv_search;
    // 选择用户个数
    private int userNum = 0;
    private static final int DOWNLOAD_CONTACT = 0;
    private static final int INIT_CONTACT = 1;
    private static final int LOAD_MORE_CONTACT = 2;
    private static final int INIT_SEARCH_CONTACT = 3;
    private static final int LOAD_MORE_SEARCH_CONTACT = 4;
    private static final int LOAD_CONTACT_FROM_DATABASE = 5;

    private ConversationMsg conversationMsg;
    private SwipeRefreshView contact_srl;
    // 第一页加载多少条
    private static final int FIRST_PAGE_COUNT = 100;
    // 每次上拉加载多少条
    private static final int LOAD_COUNT = 100;
    // 最下端显示的位置
    private int currentPosition;
    // 最下端显示的搜索位置
    private int searchPosition;
    // 下载的所有的联系人列表
    private List<ContactUser> contactUserList = new ArrayList<>();
    // 当前显示的联系人列表
    private List<ContactUser> currentUserList = new ArrayList<>();
    // 搜索匹配的所有的联系人列表
    private List<ContactUser> searchAllList = new ArrayList<>();
    // 搜索匹配的当前显示的联系人列表
    private List<ContactUser> searchUserList = new ArrayList<>();
    // 用于插入数据库的所有的联系人列表
    private List<Contact> contactList = new ArrayList<>();
    private boolean isSearching = false;

    @Override
    protected int setLayoutId() {
        return R.layout.fragment_member_select;
    }

    @Override
    protected void initView(View view) {
        mContext = getActivity();
        // 如果不等于空，当前为转发页面，否则为创建对讲页面
        conversationMsg = ((CreateGroupAndMessageForwardActivity) getActivity()).getConversationMsg();
        contact_srl = (SwipeRefreshView) view.findViewById(R.id.contact_srl);
        contact_srl.setColorSchemeResources(R.color.colorBlue);
        contact_srl.setItemCount(FIRST_PAGE_COUNT);
        contact_srl.setOnLoadMoreListener(this);
        contact_srl.setOnRefreshListener(this);
        recyclerView = ((RecyclerView) view.findViewById(R.id.recyclerView));
        /* ptyt begin, 滚动列表隐藏软键盘_4128_shafei_20170912 */
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    KeyBoardUtils.closeKeybord(mContext, et_search);
                    KeyBoardUtils.closeKeybord(mContext, et_name);
                }
            }
        });
        /* ptyt end */
        iv_search = ((ImageView) view.findViewById(R.id.iv_search));
        iv_search.setOnClickListener(this);
        et_search = ((EditText) view.findViewById(R.id.et_search));
        et_search.setOnFocusChangeListener(this);
        et_name = ((EditText) view.findViewById(R.id.et_name));
        tv_selectedMemberNum = ((TextView) view.findViewById(R.id.tv_selectedMemberNum));
        tv_rightButton = ((CreateGroupAndMessageForwardActivity) getActivity()).getRightButton();
        tv_rightButton.setOnClickListener(this);
        recyclerView_selectedMember = ((RecyclerView) view.findViewById(R.id.recyclerView_selectedMember));
        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 4);
        recyclerView_selectedMember.setLayoutManager(layoutManager);
        selectUserMemberAdapter = new SelectUserMemberAdapter(mContext);
        recyclerView_selectedMember.setAdapter(selectUserMemberAdapter);
        selectUserMemberAdapter.setOnRemoveMemberChangedListener(this);
        et_search.addTextChangedListener(this);
        recyclerView.setHasFixedSize(true);
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0x99DEE8F5);
        recyclerView.addItemDecoration(dividerLine);

        linearLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(linearLayoutManager);
        gMemberAdapter = new MessageForwardingUserAdapter(mContext);
        recyclerView.setAdapter(gMemberAdapter);
        gMemberAdapter.setOnSelectedMemberChangedListener(this);
        gMemberAdapter.setCheckableState(true);
        et_name.setVisibility(View.INVISIBLE);
        iv_search.setVisibility(View.GONE);
        et_search.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams layoutParams = et_search.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected void initData() {
        setNewMyLooper(true);
        sendMsg(true, LOAD_CONTACT_FROM_DATABASE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UctClientApi.registerObserver(contactCallBack, ContactCallBack.CONTACTCALLBACK_INDEX);
    }

    @Override
    public void onDestroy() {
        UctClientApi.unregisterObserver(contactCallBack, ContactCallBack.CONTACTCALLBACK_INDEX);
        super.onDestroy();
    }

    /**
     * 通讯录信息上报
     */
    private ContactCallBack contactCallBack = new ContactCallBack() {
        /**
         * Description: 这个接口返回用户号码所在的组
         * CreateTime:  2017年8月28日 下午1:49:17
         * @author YuGuoCheng
         * @email yuguocheng@ptyt.com.cn
         * @param result 0表示返回成功 其他表示失败
         * @param number 用户号码
         * @param groupNumber 用户号码所在的组 多个组是用#隔开
         * @see com.android.uct.ContactCallBack#organizationContact(int, java.lang.String, java.lang.String)
         */
        @Override
        public void organizationContact(int result, String number, String groupNumber) {
            PrintLog.i("organizationContact [result=" + result + ",number=" + number + ",groupNumber=" + groupNumber + "]");
        }

        /**
         * Description: 返回当前登录用户所属的通讯录 如果下载的类型为0是只返回所有的用户 如果下载类型为1时返回用户和组
         * CreateTime:  2017年8月28日 下午1:51:08
         * @author YuGuoCheng
         * @email yuguocheng@ptyt.com.cn
         * @param result
         * @param contactUser 这个对象里有个sourceType属性 这个属性就表示是用户还是组 1为用户 2为组
         * @see com.android.uct.ContactCallBack#onContactCallBack(int, com.android.uct.bean.ContactUser)
         */
        @Override
        public void onContactCallBack(int result, ContactUser contactUser) {
            PrintLog.i("onContactCallBack [result=" + result + ",contactUser=" + contactUser.toString() + "]");
            if (result == 0) {
                Contact contact = new Contact();
                contact.setID(null);
                contact.setContactFileID(0);
                contact.setDesc(contactUser.getDesc());
                contact.setName(contactUser.getName());
                contact.setNumber(contactUser.getNumber());
                contact.setParentNum(contactUser.getParentNum());
                contact.setType(contactUser.getType());
                contact.setSourceType(contactUser.getSourceType());
                contactList.add(contact);
                if (!AppContext.getAppContext().getLoginNumber().equals(contactUser.getNumber())) {
                    contactUserList.add(contactUser);
                    if (contactUserList.size() == FIRST_PAGE_COUNT) {
                        PrintLog.w("联系人总数大于或等于FIRST_PAGE_COUNT");
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                contact_srl.setRefreshing(true);
                                sendMsg(true, INIT_CONTACT);
                            }
                        });
                    }
                }
            } else {
                PrintLog.i("onContactCallBack failed");
            }
        }
    };

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case DOWNLOAD_CONTACT:
                contactUserList.clear();
                contactList.clear();
                PrintLog.i("DOWNLOAD_CONTACT");
                //第一个参数是节点号节点号登录成功后在响应接口里会返回  第二个参数是下载类型 0只下载用户 1下载用户和组关系
                int isDownloadSuccess = UctClientApi.downloadContact(AppContext.getAppContext().getCurrentNodeDn(), 0);
                PrintLog.d("isDownloadSuccess = " + isDownloadSuccess);
                if (isDownloadSuccess == 0) {
                    boolean isInsertSuccess = ContactDBManager.getInstance(mContext).insertContactList(contactList);
                    if (isInsertSuccess) {
                        EventBus.getDefault().post(new EventBean(ConstantUtils.ACTION_INSERT_CONTACT));
                        PrintLog.d("插入通讯录数据库成功");
                    } else {
                        PrintLog.d("插入通讯录数据库失败");
                    }
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (contactUserList.size() < FIRST_PAGE_COUNT) {
                                PrintLog.i("联系人总数小于FIRST_PAGE_COUNT");
                                contact_srl.setRefreshing(true);
                                sendMsg(true, INIT_CONTACT);
                            }
                        }
                    });
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_contact_download_success), -1);
                } else {
                    /* ptyt begin, 解决通讯录下拉刷新下载失败时一直转圈_4018_shafei_20170906 */
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (contact_srl.isRefreshing()) {
                                contact_srl.setRefreshing(false);
                            }
                        }
                    });
                    /* ptyt end */
                    ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_contact_download_fail), -1);
                }
                break;
            case INIT_CONTACT:
                PrintLog.i("INIT_CONTACT");
                currentUserList.clear();
                for (int i = 0; i < contactUserList.size(); i++) {
                    if (i == FIRST_PAGE_COUNT) {
                        break;
                    }
                    currentUserList.add(contactUserList.get(i));
                }

                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /* ptyt begin, 在勾选联系人时，下载通讯录成功后，去勾选联系人程序崩溃，修改为下载通讯录_4078_shafei_20170907 */
                        gMemberAdapter.selectedUserMap.clear();
                        selectUserMemberAdapter.removeAll();
                        userNum = 0;
                        tv_selectedMemberNum.setText("已选组员(" + userNum + ")");
                        if (!StrUtils.isEmpty(et_search.getText().toString())) {
                            et_search.setText("");
                        }
                        /* ptyt end */
                        currentPosition = currentUserList.size();
                        gMemberAdapter.addAll(currentUserList);
                        if (contact_srl.isRefreshing()) {
                            contact_srl.setRefreshing(false);
                        }
                    }
                });
                break;
            case LOAD_MORE_CONTACT:
                PrintLog.i("LOAD_MORE_CONTACT");
                for (int i = currentPosition; i < contactUserList.size(); i++) {
                    if (i == currentPosition + LOAD_COUNT) {
                        break;
                    }
                    currentUserList.add(contactUserList.get(i));
                }
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contact_srl.setLoading(false);
                        currentPosition = currentUserList.size();
                        gMemberAdapter.addAll(currentUserList);
                        if (contact_srl.isRefreshing()) {
                            contact_srl.setRefreshing(false);
                        }
                    }
                });
                break;
            case INIT_SEARCH_CONTACT:
                PrintLog.i("INIT_SEARCH_CONTACT");
                searchAllList.clear();
                searchUserList.clear();
                for (int j = 0; j < contactUserList.size(); j++) {
                    ContactUser user = contactUserList.get(j);
                    /* ptyt begin, 解决当搜索号码时不能搜索出带有显示名的终端用户_4086_shafei_20170907 */
                    String number = user.getNumber();
                    String desc = user.getDesc();
                    if (number.toLowerCase().contains(textSearched.toLowerCase()) || desc.toLowerCase().contains(textSearched.toLowerCase())) {
                        searchAllList.add(user);
                    }
                    /* ptyt end */
                }
                for (int i = 0; i < searchAllList.size(); i++) {
                    if (i == FIRST_PAGE_COUNT) {
                        break;
                    }
                    searchUserList.add(searchAllList.get(i));
                }

                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        searchPosition = searchUserList.size();
                        gMemberAdapter.addAll(searchUserList);
                        if (contact_srl.isRefreshing()) {
                            contact_srl.setRefreshing(false);
                        }
                    }
                });
                break;
            case LOAD_MORE_SEARCH_CONTACT:
                PrintLog.i("LOAD_MORE_SEARCH_CONTACT");
                for (int i = searchPosition; i < searchAllList.size(); i++) {
                    if (i == searchPosition + LOAD_COUNT) {
                        break;
                    }
                    searchUserList.add(searchAllList.get(i));
                }
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contact_srl.setLoading(false);
                        searchPosition = searchUserList.size();
                        gMemberAdapter.addAll(searchUserList);
                        if (contact_srl.isRefreshing()) {
                            contact_srl.setRefreshing(false);
                        }
                    }
                });
                break;
            case LOAD_CONTACT_FROM_DATABASE:
                contactUserList.clear();
                List<Contact> list = ContactDBManager.getInstance(mContext).queryContactList();
                PrintLog.i("LOAD_CONTACT_FROM_DATABASE");
                if (list == null || list.size() == 0) {
                    sendMsg(true, DOWNLOAD_CONTACT);
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        Contact contact = list.get(i);
                        ContactUser contactUser = new ContactUser();
                        contactUser.setNumber(contact.getNumber());
                        contactUser.setName(contact.getName());
                        contactUser.setDesc(contact.getDesc());
                        contactUser.setParentNum(contact.getParentNum());
                        contactUser.setType(contact.getType());
                        contactUser.setSourceType(contact.getSourceType());
                        contactUserList.add(contactUser);
                        /* ptyt begin, 通讯录总用户数小于或等于FIRST_PAGE_COUNT时，显示列表_4397_shafei_20171017 */
                        if (list.size() < FIRST_PAGE_COUNT) {
                            if (contactUserList.size() == list.size()) {
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        contact_srl.setRefreshing(true);
                                        sendMsg(true, INIT_CONTACT);
                                    }
                                });
                            }
                        } else if (contactUserList.size() == FIRST_PAGE_COUNT) {
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    contact_srl.setRefreshing(true);
                                    sendMsg(true, INIT_CONTACT);
                                }
                            });
                        }
                        /* ptyt end */
                    }

                }
                break;
        }
        return false;
    }

    @Override
    public void onLoadMore() {
        if (!isSearching) {
            int size1 = currentUserList.size();
            int size2 = contactUserList.size();
            if (size1 > 0 && size2 > 0 && size1 == size2) {
                return;
            }
            /* ptyt begin, 解决将列表拉到最后，再次初始化列表时无法继续加载更多的分页_4088_shafei_20170908 */
            contact_srl.setLoading(true);
            contact_srl.setRefreshing(true);
            /* ptyt end */
            sendMsgDelayed(true, LOAD_MORE_CONTACT, 1000);
        } else {
            int size1 = searchUserList.size();
            int size2 = searchAllList.size();
            if (size1 > 0 && size2 > 0 && size1 == size2) {
                return;
            }
            /* ptyt begin, 解决将列表拉到最后，再次初始化列表时无法继续加载更多的分页_4088_shafei_20170908 */
            contact_srl.setLoading(true);
            contact_srl.setRefreshing(true);
            /* ptyt end */
            sendMsgDelayed(true, LOAD_MORE_SEARCH_CONTACT, 1000);
        }
    }

    @Override
    public void onRefresh() {
        if (contact_srl.isScrollToTop()) {
            if (!AppUtils.isDownloadContact()) {
                sendMsg(true, DOWNLOAD_CONTACT);
            } else {
                PrintLog.i("不让下载啦");
                if (contact_srl.isRefreshing()) {
                    contact_srl.setRefreshing(false);
                }
            }
        } else {
            if (contact_srl.isRefreshing()) {
                contact_srl.setRefreshing(false);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    private String textSearched;

    @Override
    public void afterTextChanged(Editable s) {
        textSearched = et_search.getText().toString();
        if (StrUtils.isEmpty(textSearched)) {
            isSearching = false;
            gMemberAdapter.addAll(currentUserList);
        } else {
            isSearching = true;
            contact_srl.setRefreshing(true);
            sendMsg(true, INIT_SEARCH_CONTACT);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.et_search:
                if (!hasFocus) {
                    et_search.setText("");
                    et_search.setVisibility(View.GONE);
                    iv_search.setVisibility(View.VISIBLE);
                }
        }
    }

    /**
     * @param
     * @return
     * @description 当勾选/去勾选成员列表时触发
     */
    @Override
    public void onSelectedMemberChanged(Map<String, ContactUser> contactUserMap, ContactUser contactUser, boolean isAdd) {
        PrintLog.d("onSelectedMemberChanged--contactUserMap = " + contactUserMap.size() + "  contactUser = " + contactUser.getNumber());
        if (isAdd) {
            selectUserMemberAdapter.addItem(contactUser);
            recyclerView_selectedMember.smoothScrollToPosition(selectUserMemberAdapter.getItemCount() - 1);
        } else {
            selectUserMemberAdapter.removeItem(contactUser);
        }
        userNum = contactUserMap.size();
        tv_selectedMemberNum.setText("已选组员(" + userNum + ")");
    }

    /**
     * @param
     * @return
     * @description 当点击已选组员列表，移除组员时触发
     */
    @Override
    public void onRemoveMemberChanged(ContactUser contactUser) {
        PrintLog.d("onRemoveMemberChanged--contactUser = " + contactUser.getNumber());
        /* ptyt begin, 解决多次同时从list中移除同一个contactUser时，数组溢出的异常_4643_shafei_20171109 */
        int pos = selectUserMemberAdapter.getItemPosition(contactUser);
        if (pos == -1) {
            return;
        }
        /* ptyt end */
        selectUserMemberAdapter.removeItem(contactUser);
        gMemberAdapter.selectedUserMap.remove(contactUser.getNumber());
        userNum = gMemberAdapter.selectedUserMap.size();
        tv_selectedMemberNum.setText("已选组员(" + userNum + ")");
        gMemberAdapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_rightButton:
                if (userNum > 0) {
                    List<ContactUser> list = selectUserMemberAdapter.getDatas();
                    for (int i = 0; i < list.size(); i++) {
                        ContactUser contactUser = list.get(i);
                        String msgSrcNo = AppContext.getAppContext().getLoginNumber();
                        String msgDstNo = contactUser.getNumber();
                        try {
                            ConversationMsg conversationMsg1 = conversationMsg.clone();
                            conversationMsg1.setID(null);
                            conversationMsg1.setMsgConversationId(null);
                            conversationMsg1.setMsgTime(StrUtils.getCurrentTimes());
                            conversationMsg1.setMsgSrcNo(msgSrcNo);
                            conversationMsg1.setMsgDstNo(msgDstNo);
                            conversationMsg1.setGroupNo(null);
                            //                                conversationMsg1.setMsgTxtSplit(MessageDBConstant.UNSEGMENTED_MSG);
                            conversationMsg1.setMsgDirection(MessageDBConstant.IMVT_TO_MSG);
                            conversationMsg1.setMsgStatus(MessageDBConstant.MSG_STATUS_WAIT_SENDING);
                            conversationMsg1.setReadStatus(MessageDBConstant.ALREAD_MSG);
                                /* ptyt begin, 转发对方消息的需要重新设置smsid等属性_4099_shafei_20170911 */
                            conversationMsg1.setRecvNotify(MessageDBConstant.BASIC_NOTIFY);
                            conversationMsg1.setRecvCfm(MessageDBConstant.NEEDLESS_CFM);
                            String smsid_new = StrUtils.getSmsId(msgDstNo, msgSrcNo);
                            String smsid_old = conversationMsg1.getMsgUctId();
                            switch (conversationMsg1.getMsgType()) {
                                case MessageDBConstant.INFO_TYPE_TEXT:
                                case MessageDBConstant.INFO_TYPE_OLD_DEVICE_TEXT:
                                    break;
                                case MessageDBConstant.INFO_TYPE_AUDIO:
                                    String second = smsid_old.substring(smsid_old.lastIndexOf("_") + 1, smsid_old.length());
                                    smsid_new = smsid_new + "_" + second;

                                    String remotePath = SDCardUtils.getChatRemotePath(msgDstNo, smsid_new, "mp3");
                                    conversationMsg1.setRemoteImgPath(remotePath);

                                    String audioContent = smsid_new + ".mp3";
                                    conversationMsg1.setContent(audioContent);

                                    try {
                                        byte[] _tmpContent = (audioContent).getBytes("UTF-8");
                                        conversationMsg1.setContentLength(_tmpContent.length);
                                    } catch (UnsupportedEncodingException e) {
                                        conversationMsg1.setContentLength(audioContent.length());
                                        e.printStackTrace();
                                    }

                                    conversationMsg1.setAudioLength(Integer.parseInt(second));
                                    conversationMsg1.setAudioPlayStatus(MessageDBConstant.AUDIO_STOP_STATUS);
                                    conversationMsg1.setAudioReadStatus(MessageDBConstant.AUDIO_ALREAD_MSG);
                                    break;
                                case MessageDBConstant.INFO_TYPE_IMAGE:
                                    conversationMsg1.setMsgThumbnail(MessageDBConstant.MSG_ORIGINAL_IMAGE);
                                case MessageDBConstant.INFO_TYPE_VIDEO:
                                case MessageDBConstant.INFO_TYPE_CAMERA_VIDEO:
                                case MessageDBConstant.INFO_TYPE_FILE:
                                    String fileSize1 = smsid_old.substring(smsid_old.lastIndexOf("_") + 1, smsid_old.length());
                                    smsid_new = smsid_new + "_" + fileSize1;

                                    String localPath1 = conversationMsg1.getLocalImgPath();
                                    String prefix1 = FileUtils.getSuffix(localPath1);
                                    String remotePath1 = SDCardUtils.getChatRemotePath(msgDstNo, smsid_new, prefix1);
                                    conversationMsg1.setRemoteImgPath(remotePath1);

                                    String msgContent = smsid_new + "." + prefix1 + FileUtils.COLON + FileUtils.getFileNameFromPath(localPath1);
                                    conversationMsg1.setContent(msgContent);

                                    try {
                                        byte[] _tmpContent = (msgContent).getBytes("UTF-8");
                                        conversationMsg1.setContentLength(_tmpContent.length);
                                    } catch (UnsupportedEncodingException e) {
                                        conversationMsg1.setContentLength(msgContent.length());
                                        e.printStackTrace();
                                    }
                                    break;
                                case MessageDBConstant.INFO_TYPE_MY_LOCATION:
                                    String fileSize2 = smsid_old.substring(smsid_old.lastIndexOf("_") + 1, smsid_old.length());
                                    smsid_new = smsid_new + "_" + fileSize2;

                                    String localPath2 = conversationMsg1.getLocalImgPath();
                                    String prefix2 = FileUtils.getSuffix(localPath2);
                                    String remotePath2 = SDCardUtils.getChatRemotePath(msgDstNo, smsid_new, prefix2);
                                    conversationMsg1.setRemoteImgPath(remotePath2);

                                    MessageMyLocationEntity entity = JSON.parseObject(conversationMsg.getContent(), MessageMyLocationEntity.class);
                                    if (entity != null) {
                                        entity.setRemotePath(remotePath2);
                                    }
                                    String msgContent2 = JSON.toJSONString(entity);
                                    conversationMsg1.setContent(msgContent2);

                                    try {
                                        byte[] _tmpContent = (msgContent2).getBytes("UTF-8");
                                        conversationMsg1.setContentLength(_tmpContent.length);
                                    } catch (UnsupportedEncodingException e) {
                                        conversationMsg1.setContentLength(msgContent2.length());
                                        e.printStackTrace();
                                    }
                                    break;
                                case MessageDBConstant.INFO_TYPE_OLD_DEVICE_COLOR_MSG:
                                    break;
                                case MessageDBConstant.INFO_TYPE_AUDIO_CALL:
                                    break;
                            }
                                /* ptyt end */
                            conversationMsg1.setMsgUctId(smsid_new);
                            MessageManager.getInstane().sendMessage(conversationMsg1);
                            ToastUtils.getToast().showMessageShort(mContext, mContext.getString(R.string.string_create_group_prompt1), -1);
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                    }
                    getActivity().finish();
                } else {
                    ToastUtils.getToast().showMessageLong(mContext, mContext.getString(R.string.string_create_group_prompt4), -1);
                }
                break;
            case R.id.iv_search:
                iv_search.setVisibility(View.GONE);
                et_search.setVisibility(View.VISIBLE);
                et_search.setFocusable(true);
                et_search.setFocusableInTouchMode(true);
                et_search.requestFocus();
                et_search.findFocus();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        PrintLog.i("onDestroyView");
        super.onDestroyView();
    }
}
