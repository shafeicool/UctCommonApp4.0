package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.entity.GroupUser;
import com.ptyt.uct.utils.ConstantUtils;
import com.ptyt.uct.utils.ToastUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Date: 2017/5/11
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class GMemberAdapter extends BaseRecyAdapter<GroupUser> {


    private static final int COMMON_TYPE = 0, INDEX_TYPE = 1;
    private static final int USER_OFFLINE = 0, USER_ONLINE = 1;
    private static final int IS_SELECTED = 1;
    private static final int TAG_TEL = 1;
    private Context mContext;
    private Map<String, GroupUser> selectedUserMap;

    public GMemberAdapter(Context context) {
        super(context);
        this.mContext = context;
        selectedUserMap = new HashMap<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == COMMON_TYPE) {
            return new GMemberViewHolder(inflater.inflate(R.layout.member_list_item, null));
        } else {
            return new IndexViewHolder(inflater.inflate(R.layout.member_index_item, null));
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        final GroupUser groupUser = getItem(position);
        if (getItemViewType(position) == COMMON_TYPE) {
            final GMemberViewHolder viewHolder = (GMemberViewHolder) holder;
            viewHolder.tv_username.setText(groupUser.getUserName());
            viewHolder.tv_userTel.setText(groupUser.getUserTel());
            Integer userOnlineTag = groupUser.getUserOnline();
            Integer userType = groupUser.getUserType();
            if (userOnlineTag == USER_ONLINE) {
                switch (userType) {
                    case ConstantUtils.UT_TYPE_DISPATCH:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_diaodutai_online);
                        break;
                    case ConstantUtils.UT_TYPE_COMMONIN:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_shouji_online);
                        break;
                    case ConstantUtils.TYPE_NO_SCREEN_MACHINE:
                    case ConstantUtils.UT_TYPE_NOSCREEN:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_wupingji_online);
                        break;
                    case ConstantUtils.UT_TYPE_3PROOF:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_sanfangji_online);
                        break;
                    case ConstantUtils.UT_TYPE_EXDISPATCH:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_zhuanyezhongduan_online);
                        break;
                    case ConstantUtils.UT_TYPE_4GRECORDER:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_zhifajiluyi_online);
                        break;
                    case ConstantUtils.UT_TYPE_CAMERA:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_shexiangtou_online);
                        break;
                    case ConstantUtils.UT_TYPE_VEHICLE:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_wupingji_online);
                        break;
                    case ConstantUtils.UT_TYPE_CONTROLBALL:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_zhuanyezhongduan_online);
                        break;
                    default:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_zhifajiluyi_online);

                }
            } else {
                switch (userType) {
                    case ConstantUtils.UT_TYPE_DISPATCH:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_diaodutai_noline);
                        break;
                    case ConstantUtils.UT_TYPE_COMMONIN:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_shouji_noline);
                        break;
                    case ConstantUtils.TYPE_NO_SCREEN_MACHINE:
                    case ConstantUtils.UT_TYPE_NOSCREEN:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_wupingji_noline);
                        break;
                    case ConstantUtils.UT_TYPE_3PROOF:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_sanfangji_noline);
                        break;
                    case ConstantUtils.UT_TYPE_EXDISPATCH:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_zhuanyezhonngduan_noline);
                        break;
                    case ConstantUtils.UT_TYPE_4GRECORDER:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_zhifajiliyi_noline);
                        break;
                    case ConstantUtils.UT_TYPE_CAMERA:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_shexiangtou_noline);
                        break;
                    case ConstantUtils.UT_TYPE_VEHICLE:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_wupingji_noline);
                        break;
                    case ConstantUtils.UT_TYPE_CONTROLBALL:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_zhuanyezhonngduan_noline);
                        break;
                    default:
                        viewHolder.iv_avatar.setImageResource(R.mipmap.head_zhifajiliyi_noline);

                }
            }
            //勾选图标
            if (checkableState) {
                viewHolder.iv_checkState.setVisibility(View.VISIBLE);
            }
            if (selectedUserMap.containsKey(groupUser.getUserTel())) {
                viewHolder.iv_checkState.setImageResource(R.mipmap.ic_btn_check);
                viewHolder.iv_checkState.setTag(true);
            } else {
                viewHolder.iv_checkState.setImageResource(R.mipmap.chenkbox_normal);
                viewHolder.iv_checkState.setTag(false);
            }
            viewHolder.iv_checkState.setTag(R.id.iv_checkState, groupUser.getUserTel());
            //点击时勾选图标的改变
            viewHolder.iv_checkState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isSelected = (boolean) v.getTag();
                    if (!isSelected) {
                        // 如果选择登录号码，是不被允许的
                        if (v.getTag(R.id.iv_checkState) .equals(AppContext.getAppContext().getLoginNumber())) {
                            ToastUtils.getToast().showMessageLong(mContext, mContext.getString(R.string.string_main_group_list_prompt), -1);
                            return;
                        }
                        v.setTag(true);
                        viewHolder.iv_checkState.setImageResource(R.mipmap.ic_btn_check);
                        selectedUserMap.put(groupUser.getUserTel(), groupUser);
                        //通知fragment中选中的人员列表跟新
                        if (onSelectedMemberChangedListener != null) {
                            onSelectedMemberChangedListener.onSelectedMemberChanged(selectedUserMap, groupUser, true);
                        }
                    } else {
                        v.setTag(false);
                        viewHolder.iv_checkState.setImageResource(R.mipmap.chenkbox_normal);
                        selectedUserMap.remove(groupUser.getUserTel());
                        //通知fragment中选中的人员列表跟新
                        if (onSelectedMemberChangedListener != null) {
                            onSelectedMemberChangedListener.onSelectedMemberChanged(selectedUserMap, groupUser, false);
                        }
                    }

                }
            });

        } else {
            final IndexViewHolder viewHolder = (IndexViewHolder) holder;
            viewHolder.tv_index.setText(groupUser.getUserName());
        }
    }

    private boolean checkableState;

    public void setCheckableState(boolean checkableState) {
        this.checkableState = checkableState;
    }

    public class IndexViewHolder extends RecyclerView.ViewHolder {
        private final TextView tv_index;

        public IndexViewHolder(View itemView) {
            super(itemView);
            tv_index = ((TextView) itemView.findViewById(R.id.tv_index));

        }
    }

    public class GMemberViewHolder extends BaseViewHolder {
        private final TextView tv_username,tv_userTel;
        private final ImageView iv_avatar;
        private final ImageView iv_checkState;

        public GMemberViewHolder(View itemView) {
            super(itemView);
            PrintLog.i("GMemberViewHolder()");
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(layoutParams);
            tv_username = ((TextView) itemView.findViewById(R.id.tv_username));
            iv_avatar = ((ImageView) itemView.findViewById(R.id.iv_avatar));
            iv_checkState = ((ImageView) itemView.findViewById(R.id.iv_checkState));
            tv_userTel = ((TextView) itemView.findViewById(R.id.tv_userTel));
        }
    }

    @Override
    public int getItemViewType(int position) {
        GroupUser item = getItem(position);
        String userName = item.getUserName();
        if (userName.length() >= 1) {
            return COMMON_TYPE;
        }
        return INDEX_TYPE;
    }

    private OnSelectedMemberChangedListener onSelectedMemberChangedListener;

    public void setOnSelectedMemberChangedListener(OnSelectedMemberChangedListener onSelectedMemberChangedListener) {
        this.onSelectedMemberChangedListener = onSelectedMemberChangedListener;
    }

    /**
     * 人员选中时通知fragment,接口回调
     */
    public interface OnSelectedMemberChangedListener {
        void onSelectedMemberChanged(Map<String, GroupUser> groupUserMap, GroupUser groupUser, boolean isAdd);
    }


}
