package com.ptyt.uct.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.uct.bean.GroupData;
import com.android.uct.bean.GroupOrganizationBean;
import com.android.uct.bean.User;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.activity.MainActivity;
import com.ptyt.uct.activity.MessageActivity;
import com.ptyt.uct.activity.VideoCallActivity;
import com.ptyt.uct.adapter.BaseRecyAdapter;
import com.ptyt.uct.adapter.GMemberAdapter;
import com.ptyt.uct.callback.GMemberListCallBack;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.entity.GroupUser;
import com.ptyt.uct.helper.StringHelper;
import com.ptyt.uct.model.GroupUserDBManager;
import com.ptyt.uct.utils.ActivitySkipUtils;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.NetUtils;
import com.ptyt.uct.utils.ScreenUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;
import com.ptyt.uct.viewinterface.IGMemberView;
import com.ptyt.uct.widget.DividerLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import de.greenrobot.event.EventBus;


/**
 * @Description: 组成员
 * @Date: 2017/5/11
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class GMemberListFragment extends BaseFragment implements IGMemberView, View.OnTouchListener, TextWatcher, BaseRecyAdapter.OnItemClickListener, View.OnClickListener {

    private GMemberAdapter gMemberAdapter;
    private List<GroupUser> newUserList = new ArrayList<>();
    private List<GroupUser> searchUserList = new ArrayList<>();
    private FragmentActivity mContext;
    private RecyclerView recyclerView;
    private LinearLayout ll_sort;
    private TextView tv_currentWord;

    //排序完的用户列表
    private List<GroupUser> sortedUserList;
    private HashMap<String, Integer> selector;
    private String[] indexStr = { "#", "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z" };
    private boolean isFirst = true;
    private LinearLayoutManager linearLayoutManager;
    private int indexLayoutHeight;
    private EditText et_searchEnter;
    //功能选项
    private PopupWindow ppw_itemFunction;
    private int currentPosition;
    private View view_parent;
    //RecyclerView距离屏幕顶部高度
    private int distanceY;
    //是否需要对数据源排序:成员列表不需要，通讯录需要
    private boolean isNeedSort = false;
    private View ll_selected;
    private boolean isCheckSate = false;//创建对讲　选择成员
    private TextView tv_selectedMemberNum;
    private RecyclerView recyclerView_selectedMember;
    private View view_search;
    private ObjectAnimator anim_extend;
    private View rl_memberList;
    private RecyclerView recyclerView_search;
    private GMemberAdapter gMemberSearchAdapter;
    private TextView tv_online;
    private String groupId;

    @Override
    protected int setLayoutId() {
        return R.layout.fragment_member_list;
    }

    /**
     * 成员列表item点击事件
     * @param pos 点击位置
     * @param itemView
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onItemClick(final int pos, View itemView) {
        if(isCheckSate){
            return;
        }
        currentPosition = pos;
        GroupUser item = gMemberAdapter.getItem(pos);
        int length = item.getUserName().trim().length();
        if(length >= 1){
            /*ptyt start 组成员列表中点击自己会弹出功能框，但不能与自己通话，建议去掉功能框_4910_kechuanqi_20171214*/
            if(item.getUserTel().equals(AppContext.getAppContext().getLoginNumber())){
                ToastUtils.getToast().showMessageShort(mContext,getString(R.string.user_self),-1);
                return;
            }
            /*ptyt end*/
            ppw_itemFunction.showAtLocation(view_parent, Gravity.TOP|Gravity.RIGHT, getActivity().getResources().getDimensionPixelOffset(R.dimen.y40),distanceY + ((int) itemView.getY()));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void initView(View view) {
        mContext = getActivity();
        ll_selected = view.findViewById(R.id.ll_selected);
        recyclerView = ((RecyclerView) view.findViewById(R.id.recyclerView));
        recyclerView_search = ((RecyclerView) view.findViewById(R.id.recyclerView_search));
        recyclerView_search.setLayoutManager(new LinearLayoutManager(mContext));
        ll_sort = ((LinearLayout) view.findViewById(R.id.ll_sort));
        tv_currentWord = ((TextView) view.findViewById(R.id.tv_current_word));
        et_searchEnter = ((EditText) view.findViewById(R.id.et_search));
        view_parent = view.findViewById(R.id.view_parent);
        rl_memberList = view.findViewById(R.id.rl_memberList);
        tv_selectedMemberNum = ((TextView) view.findViewById(R.id.tv_selectedMemberNum));
        recyclerView_selectedMember = ((RecyclerView) view.findViewById(R.id.recyclerView_selectedMember));
        view_search = view.findViewById(R.id.view_search);
        tv_online = (TextView) view.findViewById(R.id.tv_online);
        EditText et_search = (EditText) view.findViewById(R.id.et_search);
        Drawable leftDrawable1 = et_search.getCompoundDrawables()[0];
        if(leftDrawable1!=null){
            leftDrawable1.setBounds(0, 0, getActivity().getResources().getDimensionPixelOffset(R.dimen.y38), getActivity().getResources().getDimensionPixelOffset(R.dimen.y38));
            et_search.setCompoundDrawables(leftDrawable1, null, null, null);//只放左边
        }
        recyclerView_selectedMember.setLayoutManager(new GridLayoutManager(mContext,5));
        Drawable leftDrawable = et_searchEnter.getCompoundDrawables()[0];
        if(leftDrawable!=null){
            leftDrawable.setBounds(0, 0, getActivity().getResources().getDimensionPixelOffset(R.dimen.y38), getActivity().getResources().getDimensionPixelOffset(R.dimen.y38));
            et_searchEnter.setCompoundDrawables(leftDrawable, null, null, null);//只放左边
        }
        et_searchEnter.addTextChangedListener(this);
        recyclerView.setHasFixedSize(true);
        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
        dividerLine.setSize(1);
        dividerLine.setColor(0x99DEE8F5);
        recyclerView.addItemDecoration(dividerLine);
        recyclerView_search.addItemDecoration(dividerLine);

        linearLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(linearLayoutManager);
        gMemberAdapter = new GMemberAdapter(mContext);
        recyclerView.setAdapter(gMemberAdapter);
        gMemberSearchAdapter = new GMemberAdapter(mContext);
        recyclerView_search.setAdapter(gMemberSearchAdapter);
        gMemberAdapter.setOnItemClickListener(this);
        if(isNeedSort){
            ll_sort.setVisibility(View.VISIBLE);
            ll_sort.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (isFirst) {
                        indexLayoutHeight = ll_sort.getMeasuredHeight() / indexStr.length;
                        getIndexView();
                        isFirst = false;
                    }
                }
            });
            ll_sort.setOnTouchListener(this);
        }
       //组成员信息绑定回调
       GMemberListCallBack.getInstance().bindView(this);

        initItemFucWindow();
        anim_extend = ObjectAnimator.ofFloat(view_search, "translationY", 100f, 0f);
        anim_extend.setDuration(100);
        anim_extend.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //动画结束
                rl_memberList.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
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
        popView.findViewById(R.id.iv_location_dialog).setOnClickListener(this);
        //popView即popupWindow的布局，ture设置focusAble.
        ppw_itemFunction = new PopupWindow(popView, RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT, true);
        //必须设置BackgroundDrawable后setOutsideTouchable(true)才会有效。这里在XML中定义背景，所以这里设置为null;
        ppw_itemFunction.setBackgroundDrawable(new BitmapDrawable());
        //点击外部关闭。
        ppw_itemFunction.setOutsideTouchable(true);
        //设置一个动画。
        ppw_itemFunction.setAnimationStyle(android.R.style.Animation_Dialog);
        //状态栏高 + actionbar高(y96) + EditText高(y120) + 距item顶部高((110-68)/2) + 在线人数高(y60)
        distanceY =ScreenUtils.getStatusHeight(mContext)+ getActivity().getResources().getDimensionPixelOffset(R.dimen.y303);
    }

    @Override
    protected void initData() {
        super.initData();
        groupId = getArguments().getString("groupId");
        //1.先从数据库读取并显示
        List<GroupUser> groupUsers = GroupUserDBManager.getInstance(mContext).queryUserListByGroupID(groupId);
        if(groupUsers!=null && groupUsers.size() > 0){
            gMemberAdapter.addAll(groupUsers);
        }
        //2.网络获取
        //进度圈提醒
        UctClientApi.QueryGListFormIdx(groupId);
    }

    @Override
    public void onLoadSucceed(List<GroupData> groupList, List<User> userList) {
        PrintLog.i("userList="+userList +"  userList.size()="+userList.size());
//        String groupName = getActivity().getIntent().getStringExtra("groupName");
        int userOnlineNumber = 0;
        if(isNeedSort){
            sortIndex(userList);
            gMemberAdapter.addAll(sortedUserList);
        }else {
            newUserList.clear();
            for (int j = 0; j < userList.size(); j++) {
                User user = userList.get(j);
                PrintLog.i(user.getUserName()+":  getUserType="+user.getUserType());
                long longGroupId = 0;
                try {
                    longGroupId = Long.valueOf(groupId).longValue();
                }catch (Exception e){
                     e.printStackTrace();
                }
                newUserList.add(new GroupUser(longGroupId,user.getUserTel(),user.getUserName(),user.getUserIcon(),user.getUserType(),user.getUserStatus(),user.getUserBlink(),user.getUserOnline(),""));
                if (user.getUserOnline() != null && user.getUserOnline() == 1) {
                    userOnlineNumber++;
                }
            }
            gMemberAdapter.addAll(newUserList);
            /*ptyt start 保存用户至数据库*/
            GroupUserDBManager.getInstance(mContext).insertUserList(GMemberListFragment.this.groupId,newUserList);
            /*ptyt end*/
        }
        tv_online.setText(String.format(getResources().getString(R.string.string_main_group_member_list_online), userOnlineNumber, userList.size()));
//        ((GMemberListActivity) getActivity()).setTitleName(groupName+" ("+userOnlineNumber+"/"+userList.size()+")");
    }

    @Override
    public void onAllUserLoadSucceed(List<GroupOrganizationBean> list, ArrayList<String> arrayList) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //组成员信息绑定回调
        if(ppw_itemFunction!=null && ppw_itemFunction.isShowing()){
            ppw_itemFunction.dismiss();
        }
        UctClientApi.CancelQueryGListFormIdx();
        GMemberListCallBack.getInstance().unbindView(this);
    }
    /**
     * 对数据进行加工排序
     *
     * @param userList
     * @return
     */
    public void sortIndex(List<User> userList) {
        String[] pinYinNames = new String[userList.size()];
        List<GroupUser> newUserList = new ArrayList<>();
        for (int j = 0; j < userList.size(); j++) {
            User user = userList.get(j);
            String pinYin = StringHelper.getPinYin((user.getUserName()).toString());
            newUserList.add(new GroupUser(user.getUserTel(),user.getUserName(),user.getUserIcon(),user.getUserType(),user.getUserStatus(),user.getUserBlink(),user.getUserOnline(),pinYin));
            pinYinNames[j] = pinYin;
        }
        TreeSet<String> set = new TreeSet();
        // 获取初始化数据源中的首字母，添加到set中
        for (User user : userList) {
            set.add(StringHelper.getPinYinHeadChar(user.getUserName()).substring(0, 1));
        }
        // 新数组的长度为原数据加上set的大小
        String[] names = new String[userList.size() + set.size()];
        int i = 0;
        for (String string : set) {
            names[i] = string;
            i++;
        }
        // 将原数据拷贝到新数据中
        System.arraycopy(pinYinNames, 0, names, set.size(), pinYinNames.length);
        // 自动按照首字母排序
        Arrays.sort(names, String.CASE_INSENSITIVE_ORDER);
        //排序
        sortList(names, newUserList);

        selector = new HashMap<>();
        for (int j = 0; j < indexStr.length; j++) {
            // 循环字母表，找出newPersons中对应字母的位置
            for (int k = 0; k < sortedUserList.size(); k++) {
                if (sortedUserList.get(k).getUserName().equals(indexStr[j])) {
                    selector.put(indexStr[j], i);
                }
            }
        }

    }

    /**
     * 重新排序获得一个新的List集合
     *
     * @param allNames
     */
    private void sortList(String[] allNames,List<GroupUser> list) {

        sortedUserList = new ArrayList<>();
        for (int i = 0; i < allNames.length; i++) {
            if (allNames[i].length() != 1) {
                for (int j = 0; j < list.size(); j++) {
                    GroupUser groupUser = list.get(j);
                    if (allNames[i].equals(groupUser.getUserNamePinYin())) {
                        sortedUserList.add(groupUser);
                    }
                }
            } else {
                sortedUserList.add(new GroupUser(allNames[i]));
            }
        }
    }

    /**
     * 绘制索引列表
     */
    public void getIndexView() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, indexLayoutHeight);
        for (int i = 0; i < indexStr.length; i++) {
            final TextView tv = new TextView(mContext);
            tv.setLayoutParams(params);
            tv.setText(indexStr[i]);
            tv.setPadding(10, 0, 10, 0);
            tv.setTextColor(0xFFA3A3A3);
            ll_sort.addView(tv);
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.ll_sort){
            float y = event.getY();
            int index = (int) (y / indexLayoutHeight);
            if (index > -1 && index < indexStr.length) {// 防止越界
                String key = indexStr[index];
                if (selector != null  && selector.containsKey(key)) {
                    int pos = selector.get(key);
                    linearLayoutManager.scrollToPositionWithOffset(pos,0);
                    tv_currentWord.setVisibility(View.VISIBLE);
                    tv_currentWord.setText(indexStr[index]);
                }
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ll_sort.setBackgroundColor(Color.parseColor("#d7d7d7"));
                    break;
                case MotionEvent.ACTION_UP:
                    ll_sort.setBackgroundColor(Color.parseColor("#00000000"));
                    tv_currentWord.setVisibility(View.GONE);
                    break;
            }
        }
        return true;
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        PrintLog.i("onTextChanged()  s="+s);
        if(s.length() > 0){
            et_searchEnter.setBackgroundResource(R.drawable.shape_round_corner_stroke_blue);
            //数据库查找含该文字的名称

        }else {
            et_searchEnter.setBackgroundResource(R.drawable.shape_round_corner_stroke);
        }
    }

    private String textSearched;
    @Override
    public void afterTextChanged(Editable s) {
        textSearched = et_searchEnter.getText().toString();
        if (StrUtils.isEmpty(textSearched)) {
            gMemberAdapter.addAll(newUserList);
        } else {
            searchUserList.clear();
            for (int i = 0; i < newUserList.size(); i++) {
                GroupUser groupUser = newUserList.get(i);
                String tel = groupUser.getUserTel();
                String name = groupUser.getUserName();
                if (tel.toLowerCase().contains(textSearched.toLowerCase()) || name.toLowerCase().contains(textSearched.toLowerCase())) {
                    searchUserList.add(groupUser);
                }
            }
            gMemberAdapter.addAll(searchUserList);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onClick(View v) {
        if(!NetUtils.isNetworkAvailable(mContext)){
            ToastUtils.getToast().showMessageShort(mContext,getString(R.string.net_error),-1);
            if(ppw_itemFunction.isShowing()){
                ppw_itemFunction.dismiss();
            }
            return;
        }
        switch (v.getId()){
            case R.id.iv_audio_dialog://语音呼叫
                ppw_itemFunction.dismiss();
                ActivitySkipUtils.intent2CallActivity(getActivity(),VideoCallActivity.class,ConstantUtils.AUDIO_SCALL,gMemberAdapter.getItem(currentPosition));
                break;
            case R.id.iv_message_dialog://信息
                ActivitySkipUtils.intent2CallActivity(getActivity(),MessageActivity.class,-1,gMemberAdapter.getItem(currentPosition));
                ppw_itemFunction.dismiss();
                break;
            case R.id.iv_video_dialog://视频呼叫
                ppw_itemFunction.dismiss();
                ActivitySkipUtils.intent2CallActivity(getActivity(),VideoCallActivity.class,ConstantUtils.VIDEO_SCALL,gMemberAdapter.getItem(currentPosition));
                break;
            case R.id.iv_upload_dialog://上传视频
                ppw_itemFunction.dismiss();
                ActivitySkipUtils.intent2CallActivity(getActivity(),VideoCallActivity.class,ConstantUtils.UPLOAD_VIDEO,gMemberAdapter.getItem(currentPosition));
                break;
            case R.id.et_search:
                break;
            case R.id.iv_location_dialog://显示成员位置
                EventBus.getDefault().post(new EventBean(ConstantUtils.ACTION_TO_MAP_SHOW_USER));
                mContext.startActivity(new Intent(mContext, MainActivity.class));
                break;
        }
    }
}
