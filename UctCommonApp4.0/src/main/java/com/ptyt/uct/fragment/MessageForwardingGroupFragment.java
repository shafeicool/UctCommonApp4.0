package com.ptyt.uct.fragment;

import android.os.Build;
import android.os.Message;
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
import com.android.uct.bean.GroupData;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.CreateGroupAndMessageForwardActivity;
import com.ptyt.uct.adapter.MessageForwardingGroupAdapter;
import com.ptyt.uct.adapter.SelectGroupMemberAdapter;
import com.ptyt.uct.callback.GroupInfoCallback;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.entity.MessageMyLocationEntity;
import com.ptyt.uct.services.MessageManager;
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

/**
 * @Description:
 * @Date: 2017/9/11
 * @Author: ShaFei
 * @Version:V1.0
 */

public class MessageForwardingGroupFragment extends BaseFragment implements
        TextWatcher,
        MessageForwardingGroupAdapter.OnSelectedMemberChangedListener,
        SelectGroupMemberAdapter.OnRemoveMemberChangedListener,
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
    private MessageForwardingGroupAdapter gMemberAdapter;
    private SelectGroupMemberAdapter selectGroupMemberAdapter;
    private EditText et_name;
    private ImageView iv_search;
    // 选择用户个数
    private int groupNum = 0;
    private static final int DOWNLOAD_GROUP = 0;
    private static final int INIT_GROUP_DATA = 1;
    private static final int LOAD_MORE_GROUP = 2;
    private static final int INIT_SEARCH_GROUP = 3;
    private static final int LOAD_MORE_SEARCH_GROUP = 4;

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
    private List<GroupData> groupDataList = new ArrayList<>();
    // 当前显示的联系人列表
    private List<GroupData> currentGroupList = new ArrayList<>();
    // 搜索匹配的所有的联系人列表
    private List<GroupData> searchAllList = new ArrayList<>();
    // 搜索匹配的当前显示的联系人列表
    private List<GroupData> searchGroupList = new ArrayList<>();
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
        selectGroupMemberAdapter = new SelectGroupMemberAdapter(mContext);
        recyclerView_selectedMember.setAdapter(selectGroupMemberAdapter);
        selectGroupMemberAdapter.setOnRemoveMemberChangedListener(this);
        et_search.addTextChangedListener(this);
        recyclerView.setHasFixedSize(true);
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0x99DEE8F5);
        recyclerView.addItemDecoration(dividerLine);

        linearLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(linearLayoutManager);
        gMemberAdapter = new MessageForwardingGroupAdapter(mContext);
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
        sendMsg(true, INIT_GROUP_DATA);
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case INIT_GROUP_DATA:
                PrintLog.i("INIT_GROUP_DATA");
                currentGroupList.clear();
                groupDataList.clear();
                groupDataList = GroupInfoCallback.getInstance().getmGroupList();
                for (int i = 0; i < groupDataList.size(); i++) {
                    if (i == FIRST_PAGE_COUNT) {
                        break;
                    }
                    currentGroupList.add(groupDataList.get(i));
                }

                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /* ptyt begin, 在勾选联系人时，下载通讯录成功后，去勾选联系人程序崩溃，修改为下载通讯录_4078_shafei_20170907 */
                        gMemberAdapter.selectedGroupMap.clear();
                        selectGroupMemberAdapter.removeAll();
                        groupNum = 0;
                        tv_selectedMemberNum.setText("已选组(" + groupNum + ")");
                        if (!StrUtils.isEmpty(et_search.getText().toString())) {
                            et_search.setText("");
                        }
                        /* ptyt end */
                        currentPosition = currentGroupList.size();
                        gMemberAdapter.addAll(currentGroupList);
                        if (contact_srl.isRefreshing()) {
                            contact_srl.setRefreshing(false);
                        }
                    }
                });
                break;
            case LOAD_MORE_GROUP:
                PrintLog.i("LOAD_MORE_GROUP");
                for (int i = currentPosition; i < groupDataList.size(); i++) {
                    if (i == currentPosition + LOAD_COUNT) {
                        break;
                    }
                    currentGroupList.add(groupDataList.get(i));
                }
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contact_srl.setLoading(false);
                        currentPosition = currentGroupList.size();
                        gMemberAdapter.addAll(currentGroupList);
                        if (contact_srl.isRefreshing()) {
                            contact_srl.setRefreshing(false);
                        }
                    }
                });
                break;
            case INIT_SEARCH_GROUP:
                PrintLog.i("INIT_SEARCH_GROUP");
                searchAllList.clear();
                searchGroupList.clear();
                for (int j = 0; j < groupDataList.size(); j++) {
                    GroupData groupData = groupDataList.get(j);
                    /* ptyt begin, 解决当搜索号码时不能搜索出带有显示名的终端用户_4086_shafei_20170907 */
                    String number = groupData.groupId;
                    String desc = groupData.groupName;
                    if (number.toLowerCase().contains(textSearched.toLowerCase()) || desc.toLowerCase().contains(textSearched.toLowerCase())) {
                        searchAllList.add(groupData);
                    }
                    /* ptyt end */
                }
                for (int i = 0; i < searchAllList.size(); i++) {
                    if (i == FIRST_PAGE_COUNT) {
                        break;
                    }
                    searchGroupList.add(searchAllList.get(i));
                }

                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        searchPosition = searchGroupList.size();
                        gMemberAdapter.addAll(searchGroupList);
                        if (contact_srl.isRefreshing()) {
                            contact_srl.setRefreshing(false);
                        }
                    }
                });
                break;
            case LOAD_MORE_SEARCH_GROUP:
                PrintLog.i("LOAD_MORE_SEARCH_GROUP");
                for (int i = searchPosition; i < searchAllList.size(); i++) {
                    if (i == searchPosition + LOAD_COUNT) {
                        break;
                    }
                    searchGroupList.add(searchAllList.get(i));
                }
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contact_srl.setLoading(false);
                        searchPosition = searchGroupList.size();
                        gMemberAdapter.addAll(searchGroupList);
                        if (contact_srl.isRefreshing()) {
                            contact_srl.setRefreshing(false);
                        }
                    }
                });
                break;
        }
        return false;
    }

    @Override
    public void onLoadMore() {
        if (!isSearching) {
            int size1 = currentGroupList.size();
            int size2 = groupDataList.size();
            if (size1 > 0 && size2 > 0 && size1 == size2) {
                return;
            }
            /* ptyt begin, 解决将列表拉到最后，再次初始化列表时无法继续加载更多的分页_4088_shafei_20170908 */
            contact_srl.setLoading(true);
            contact_srl.setRefreshing(true);
            /* ptyt end */
            sendMsgDelayed(true, LOAD_MORE_GROUP, 1000);
        } else {
            int size1 = searchGroupList.size();
            int size2 = searchAllList.size();
            if (size1 > 0 && size2 > 0 && size1 == size2) {
                return;
            }
            /* ptyt begin, 解决将列表拉到最后，再次初始化列表时无法继续加载更多的分页_4088_shafei_20170908 */
            contact_srl.setLoading(true);
            contact_srl.setRefreshing(true);
            /* ptyt end */
            sendMsgDelayed(true, LOAD_MORE_SEARCH_GROUP, 1000);
        }
    }

    @Override
    public void onRefresh() {
        if (contact_srl.isScrollToTop()) {
            contact_srl.setEnabled(false);
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
            gMemberAdapter.addAll(currentGroupList);
        } else {
            isSearching = true;
            contact_srl.setRefreshing(true);
            sendMsg(true, INIT_SEARCH_GROUP);
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
    public void onSelectedMemberChanged(Map<String, GroupData> groupDataMap, GroupData groupData, boolean isAdd) {
        PrintLog.d("onSelectedMemberChanged--groupDataMap = " + groupDataMap.size() + "  groupData = " + groupData.groupId);
        if (isAdd) {
            selectGroupMemberAdapter.addItem(groupData);
            recyclerView_selectedMember.smoothScrollToPosition(selectGroupMemberAdapter.getItemCount() - 1);
        } else {
            selectGroupMemberAdapter.removeItem(groupData);
        }
        groupNum = groupDataMap.size();
        tv_selectedMemberNum.setText("已选组(" + groupNum + ")");
    }

    /**
     * @param
     * @return
     * @description 当点击已选组列表，移除组时触发
     */
    @Override
    public void onRemoveMemberChanged(GroupData groupData) {
        PrintLog.d("onRemoveMemberChanged--groupData = " + groupData.groupId);
        /* ptyt begin, 解决多次同时从list中移除同一个groupData时，数组溢出的异常_4643_shafei_20171109 */
        int pos = selectGroupMemberAdapter.getItemPosition(groupData);
        if (pos == -1) {
            return;
        }
        /* ptyt end */
        selectGroupMemberAdapter.removeItem(groupData);
        gMemberAdapter.selectedGroupMap.remove(groupData.groupId);
        groupNum = gMemberAdapter.selectedGroupMap.size();
        tv_selectedMemberNum.setText("已选组(" + groupNum + ")");
        gMemberAdapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_rightButton:
                if (groupNum > 0) {
                    List<GroupData> list = selectGroupMemberAdapter.getDatas();
                    for (int i = 0; i < list.size(); i++) {
                        GroupData groupData = list.get(i);
                        String msgSrcNo = AppContext.getAppContext().getLoginNumber();
                        String msgDstNo = groupData.groupId;
                        try {
                            ConversationMsg conversationMsg1 = conversationMsg.clone();
                            conversationMsg1.setID(null);
                            conversationMsg1.setMsgConversationId(null);
                            conversationMsg1.setMsgTime(StrUtils.getCurrentTimes());
                            conversationMsg1.setMsgSrcNo(msgSrcNo);
                            conversationMsg1.setMsgDstNo(msgDstNo);
                            conversationMsg1.setGroupNo(msgDstNo);
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
