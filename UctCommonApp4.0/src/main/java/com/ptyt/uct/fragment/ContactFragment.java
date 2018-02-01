package com.ptyt.uct.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.android.uct.ContactCallBack;
import com.android.uct.bean.ContactUser;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.CreateGroupAndMessageForwardActivity;
import com.ptyt.uct.activity.GMemberListActivity;
import com.ptyt.uct.activity.MessageActivity;
import com.ptyt.uct.activity.VideoCallActivity;
import com.ptyt.uct.adapter.BaseRecyAdapter;
import com.ptyt.uct.adapter.ContactAdapter;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.entity.Contact;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.model.ContactDBManager;
import com.ptyt.uct.utils.ActivitySkipUtils;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.KeyBoardUtils;
import com.ptyt.uct.utils.ScreenUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.widget.DividerLine;
import com.ptyt.uct.widget.SwipeRefreshView;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * @Description:
 * @Date: 2017/9/11
 * @Author: ShaFei
 * @Version:V1.0
 */

public class ContactFragment extends BaseFragment implements
        TextWatcher,
        BaseRecyAdapter.OnItemClickListener,
        View.OnClickListener,
        SwipeRefreshView.OnLoadMoreListener,
        SwipeRefreshView.OnRefreshListener {

    private ContactAdapter gMemberAdapter;
    private FragmentActivity mContext;
    private RecyclerView recyclerView;
//    private LinearLayout ll_sort;
//    private TextView tv_currentWord;

    //排序完的用户列表
//    private List<Contact> sortedUserList;
//    private HashMap<String, Integer> selector;
//    private String[] indexStr = {"#", "A", "B", "C", "D", "E", "F", "G", "H",
//            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
//            "V", "W", "X", "Y", "Z"};
//    private boolean isFirst = true;
//    private LinearLayoutManager linearLayoutManager;
//    private int indexLayoutHeight;
    private EditText et_search;
    //功能选项
    private PopupWindow ppw_itemFunction;
    private View view_parent;
    //RecyclerView距离屏幕顶部高度
    private int distanceY;
    //是否需要对数据源排序:成员列表不需要，通讯录需要
//    private boolean isNeedSort = false;
//    private View ll_selected;
//    private boolean isCheckSate = false;//创建对讲　选择成员
//    private TextView tv_selectedMemberNum;
//    private RecyclerView recyclerView_selectedMember;
//    private View view_search;
//    private ObjectAnimator anim_extend;
//    private View rl_memberList;
//    private RecyclerView recyclerView_search;
//    private GMemberAdapter gMemberSearchAdapter;
    private SwipeRefreshView contact_srl;
    private String textSearched;

    private static final int DOWNLOAD_CONTACT = 0;
    private static final int INIT_CONTACT = 1;
    private static final int LOAD_MORE_CONTACT = 2;
    private static final int INIT_SEARCH_CONTACT = 3;
    private static final int LOAD_MORE_SEARCH_CONTACT = 4;
    private static final int LOAD_CONTACT_FROM_DATABASE = 5;
    // 第一页加载多少条
    private static final int FIRST_PAGE_COUNT = 100;
    // 每次上拉加载多少条
    private static final int LOAD_COUNT = 100;
    // 当前点击Item的位置
    private int currentPos;
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
    private BitmapDrawable popBitmapDrawable;

    @Override
    protected int setLayoutId() {
        return R.layout.fragment_contact;
    }

    /**
     * 成员列表item点击事件
     *
     * @param pos      点击位置
     * @param itemView
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onItemClick(final int pos, View itemView) {
//        if (isCheckSate) {
//            return;
//        }
        currentPos = pos;
        int length = gMemberAdapter.getItem(pos).getName().trim().length();
        if (length > 1) {
            ppw_itemFunction.showAtLocation(view_parent, Gravity.TOP | Gravity.RIGHT, getActivity().getResources().getDimensionPixelOffset(R.dimen.y40), distanceY + ((int) itemView.getY()));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void initView(View view) {
        mContext = getActivity();
//        String intent = mContext.getIntent().getStringExtra("intent");
//        if (intent != null && intent.equals(ConstantUtils.INTENT_ADDRESS_BOOK)) {//通讯录
////            isNeedSort = false;
//            ((GMemberListActivity) getActivity()).setTitleName("通讯录");
//        }
//        ll_selected = view.findViewById(R.id.ll_selected);
        //右侧标题点击事件 -->进入选择页面
        ((GMemberListActivity) getActivity()).setOnRightTitleClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CreateGroupAndMessageForwardActivity.class));
            }
        });
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
                }
            }
        });
        /* ptyt end */
//        recyclerView_search = ((RecyclerView) view.findViewById(recyclerView_search));
//        recyclerView_search.setLayoutManager(new LinearLayoutManager(mContext));
//        ll_sort = ((LinearLayout) view.findViewById(ll_sort));
//        tv_currentWord = ((TextView) view.findViewById(R.id.tv_current_word));
        et_search = ((EditText) view.findViewById(R.id.et_search));
        view_parent = view.findViewById(R.id.view_parent);
//        rl_memberList = view.findViewById(rl_memberList);
//        tv_selectedMemberNum = ((TextView) view.findViewById(R.id.tv_selectedMemberNum));
//        recyclerView_selectedMember = ((RecyclerView) view.findViewById(recyclerView_selectedMember));
//        view_search = view.findViewById(view_search);
//        recyclerView_selectedMember.setLayoutManager(new GridLayoutManager(mContext, 5));
        Drawable leftDrawable = et_search.getCompoundDrawables()[0];
        if (leftDrawable != null) {
            leftDrawable.setBounds(0, 0, getActivity().getResources().getDimensionPixelOffset(R.dimen.y38), getActivity().getResources().getDimensionPixelOffset(R.dimen.y38));
            et_search.setCompoundDrawables(leftDrawable, null, null, null);//只放左边
        }
        et_search.addTextChangedListener(this);

        recyclerView.setHasFixedSize(true);
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0x99DEE8F5);
        recyclerView.addItemDecoration(dividerLine);
//        recyclerView_search.addItemDecoration(dividerLine);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        gMemberAdapter = new ContactAdapter(mContext);
        recyclerView.setAdapter(gMemberAdapter);
//        gMemberSearchAdapter = new GMemberAdapter(mContext);
//        recyclerView_search.setAdapter(gMemberSearchAdapter);
        gMemberAdapter.setOnItemClickListener(this);
//        if (isNeedSort) {
//            ll_sort.setVisibility(View.VISIBLE);
//            ll_sort.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    if (isFirst) {
//                        indexLayoutHeight = ll_sort.getMeasuredHeight() / indexStr.length;
//                        getIndexView();
//                        isFirst = false;
//                    }
//                }
//            });
//            ll_sort.setOnTouchListener(this);
//        }
        initItemFucWindow();
//        anim_extend = ObjectAnimator.ofFloat(view_search, "translationY", 100f, 0f);
//        anim_extend.setDuration(100);
//        anim_extend.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                //动画结束
//                rl_memberList.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
    }

    @Override
    protected void initData() {
        setNewMyLooper(true);
        sendMsg(true, LOAD_CONTACT_FROM_DATABASE);
    }

    /**
     * 初始化功能展开的popupWindow的view
     */
    private void initItemFucWindow() {
        View popView = View.inflate(mContext, R.layout.layout_dialog_user_fuction, null);
        popView.findViewById(R.id.iv_audio_dialog).setOnClickListener(this);
        popView.findViewById(R.id.iv_video_dialog).setOnClickListener(this);
        popView.findViewById(R.id.iv_message_dialog).setOnClickListener(this);
        popView.findViewById(R.id.iv_upload_dialog).setOnClickListener(this);
        //popView即popupWindow的布局，ture设置focusAble.
        ppw_itemFunction = new PopupWindow(popView, RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT, true);
        //必须设置BackgroundDrawable后setOutsideTouchable(true)才会有效。这里在XML中定义背景，所以这里设置为null;
        popBitmapDrawable = new BitmapDrawable();
        ppw_itemFunction.setBackgroundDrawable(popBitmapDrawable);
        //点击外部关闭。
        ppw_itemFunction.setOutsideTouchable(true);
        //设置一个动画。
        ppw_itemFunction.setAnimationStyle(android.R.style.Animation_Dialog);
        //状态栏高 + actionbar高(y96) + EditText高(y120) + 距item顶部高((110-76)/2)
        distanceY = ScreenUtils.getStatusHeight(mContext) + getActivity().getResources().getDimensionPixelOffset(R.dimen.y235);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UctClientApi.registerObserver(contactCallBack, ContactCallBack.CONTACTCALLBACK_INDEX);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ppw_itemFunction != null && ppw_itemFunction.isShowing()) {
            ppw_itemFunction.dismiss();
        }
        popBitmapDrawable = null;
        UctClientApi.unregisterObserver(contactCallBack, ContactCallBack.CONTACTCALLBACK_INDEX);
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
                        if (!StrUtils.isEmpty(et_search.getText().toString())) {
                            et_search.setText("");
                        }
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

//    /**
//     * 对数据进行加工排序
//     *
//     * @param userList
//     * @return
//     */
//    public void sortIndex(List<Contact> userList) {
//        String[] pinYinNames = new String[userList.size()];
//        for (int j = 0; j < userList.size(); j++) {
//            Contact user = userList.get(j);
//            String pinYin = StringHelper.getPinYin((user.getName()).toString());
//            userList.add(user);
//            pinYinNames[j] = pinYin;
//        }
//        TreeSet<String> set = new TreeSet();
//        // 获取初始化数据源中的首字母，添加到set中
//        for (Contact user : userList) {
//            set.add(StringHelper.getPinYinHeadChar(user.getName()).substring(0, 1));
//        }
//        // 新数组的长度为原数据加上set的大小
//        String[] names = new String[userList.size() + set.size()];
//        int i = 0;
//        for (String string : set) {
//            names[i] = string;
//            i++;
//        }
//        // 将原数据拷贝到新数据中
//        System.arraycopy(pinYinNames, 0, names, set.size(), pinYinNames.length);
//        // 自动按照首字母排序
//        Arrays.sort(names, String.CASE_INSENSITIVE_ORDER);
//        //排序
//        sortList(names, userList);
//
//        selector = new HashMap<>();
//        for (int j = 0; j < indexStr.length; j++) {
//            // 循环字母表，找出newPersons中对应字母的位置
//            for (int k = 0; k < sortedUserList.size(); k++) {
//                if (sortedUserList.get(k).getName().equals(indexStr[j])) {
//                    selector.put(indexStr[j], i);
//                }
//            }
//        }
//
//    }

//    /**
//     * 重新排序获得一个新的List集合
//     *
//     * @param allNames
//     */
//    private void sortList(String[] allNames, List<Contact> list) {
//
//        sortedUserList = new ArrayList<>();
//        for (int i = 0; i < allNames.length; i++) {
//            if (allNames[i].length() != 1) {
//                for (int j = 0; j < list.size(); j++) {
//                    Contact contact = list.get(j);
//                    if (allNames[i].equals(StringHelper.getPinYin((contact.getName()).toString()))) {
//                        sortedUserList.add(contact);
//                    }
//                }
//            } else {
//                sortedUserList.add(new Contact(allNames[i]));
//            }
//        }
//    }

//    /**
//     * 绘制索引列表
//     */
//    public void getIndexView() {
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, indexLayoutHeight);
//        for (int i = 0; i < indexStr.length; i++) {
//            final TextView tv = new TextView(mContext);
//            tv.setLayoutParams(params);
//            tv.setText(indexStr[i]);
//            tv.setPadding(10, 0, 10, 0);
//            tv.setTextColor(0xFFA3A3A3);
//            ll_sort.addView(tv);
//        }
//    }


//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        if (v.getId() == ll_sort) {
//            float y = event.getY();
//            int index = (int) (y / indexLayoutHeight);
//            if (index > -1 && index < indexStr.length) {// 防止越界
//                String key = indexStr[index];
//                if (selector != null && selector.containsKey(key)) {
//                    int pos = selector.get(key);
//                    linearLayoutManager.scrollToPositionWithOffset(pos, 0);
//                    tv_currentWord.setVisibility(View.VISIBLE);
//                    tv_currentWord.setText(indexStr[index]);
//                }
//            }
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    ll_sort.setBackgroundColor(Color.parseColor("#d7d7d7"));
//                    break;
//                case MotionEvent.ACTION_UP:
//                    ll_sort.setBackgroundColor(Color.parseColor("#00000000"));
//                    tv_currentWord.setVisibility(View.GONE);
//                    break;
//            }
//        }
//        return true;
//    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_audio_dialog://语音呼叫
                ppw_itemFunction.dismiss();
                if(UctApplication.getInstance().isInGroupCall){//当前正在组呼时，不能发起语音呼叫
                    ToastUtils.getToast().showMessageShort(mContext,getString(R.string.gcalling_cannot_audio_call),-1);
                    return;
                }
                ActivitySkipUtils.intent2CallActivity(getActivity(),VideoCallActivity.class,ConstantUtils.AUDIO_SCALL,gMemberAdapter.getItem(currentPos));
                break;
            case R.id.iv_message_dialog://信息
                ppw_itemFunction.dismiss();
                ActivitySkipUtils.intent2CallActivity(getActivity(), MessageActivity.class, -1, gMemberAdapter.getItem(currentPos));
                break;
            case R.id.iv_video_dialog://视频呼叫
                ppw_itemFunction.dismiss();
                if(UctApplication.getInstance().isInGroupCall){//当前正在组呼时，不能发起视频呼叫
                    ToastUtils.getToast().showMessageShort(mContext,getString(R.string.gcalling_cannot_video_call),-1);
                    return;
                }
                ActivitySkipUtils.intent2CallActivity(getActivity(), VideoCallActivity.class, ConstantUtils.VIDEO_SCALL, gMemberAdapter.getItem(currentPos));
                break;
            case R.id.iv_upload_dialog://上传视频
                ppw_itemFunction.dismiss();
                ActivitySkipUtils.intent2CallActivity(getActivity(), VideoCallActivity.class, ConstantUtils.UPLOAD_VIDEO, gMemberAdapter.getItem(currentPos));
                break;
        }
    }


}
