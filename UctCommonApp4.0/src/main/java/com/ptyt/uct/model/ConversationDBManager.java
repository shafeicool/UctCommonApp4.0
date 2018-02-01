package com.ptyt.uct.model;

import android.content.Context;
import android.database.Cursor;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.entity.Conversation;
import com.ptyt.uct.gen.ConversationDao;
import com.ptyt.uct.gen.ConversationMsgDao;
import com.ptyt.uct.utils.SDCardUtils;
import com.ptyt.uct.utils.StrUtils;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Title: com.ptyt.uct.model
 * Description:
 * Date: 2017/5/16
 * Author: ShaFei
 * Version: V1.0
 */

public class ConversationDBManager {

    private Context mContext;
    private static ConversationDBManager mInstance;
    private DBDaoHelper dbDaoHelper;

    public ConversationDBManager(Context context) {
        this.mContext = context;
        if (dbDaoHelper == null) {
            dbDaoHelper = DBDaoHelper.getInstance(context);
        }
    }

    public static ConversationDBManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ConversationDBManager(context);
        }
        return mInstance;
    }

    public void release() {
        if (mInstance != null) {
            mInstance = null;
        }
    }

    /**
     * 插入一条记录
     *
     * @param conversation
     * @return 对应row ID
     */
    public long insertConversation(Conversation conversation) {
        ConversationDao conversationDao = dbDaoHelper.getConversationDao();
        return conversationDao.insert(conversation);
    }

    /**
     * 插入用户集合
     *
     * @param conversations
     */
    public void insertConversationList(List<Conversation> conversations) {
        if (conversations == null || conversations.isEmpty()) {
            return;
        }
        ConversationDao conversationDao = dbDaoHelper.getConversationDao();
        conversationDao.insertInTx(conversations);
    }

    /**
     * 删除一条记录
     *
     * @param conversation
     */
    public void deleteConversation(Conversation conversation) {
        ConversationDao conversationDao = dbDaoHelper.getConversationDao();
        conversationDao.delete(conversation);
    }

    /**
     * 删除该登录号码的所有聊天记录
     */
    public void deleteAllConversation() {
        ConversationDao conversationDao = dbDaoHelper.getConversationDao();
        QueryBuilder<Conversation> qb = conversationDao.queryBuilder();
        qb.where(ConversationDao.Properties.LoginNo.eq(AppContext.getAppContext().getLoginNumber()));
        List<Conversation> list = qb.list();
        for (int i = 0; i < list.size(); i++) {
            conversationDao.delete(list.get(i));
            Long id = list.get(i).getID();
            SDCardUtils.deleteConversationFile(mContext, id);
        }
    }

    /**
     * 更新一条记录
     *
     * @param conversation
     */
    public void updateConversation(Conversation conversation) {
        ConversationDao conversationDao = dbDaoHelper.getConversationDao();
        conversationDao.update(conversation);
    }

    /**
     * 更新一个集合
     *
     * @param list
     */
    public void updateConversationList(List<Conversation> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        ConversationDao conversationDao = dbDaoHelper.getConversationDao();
        for (int i = 0; i <= list.size() - 1; i++) {
            conversationDao.update(list.get(i));
        }
    }

    /**
     * 查询会话列表
     */
    public List<Conversation> queryConversationList() {
        List<Conversation> list = new ArrayList<>();
        dbDaoHelper.getDaoSession(mContext).clear();
        ConversationDao conversationDao = dbDaoHelper.getConversationDao();
        QueryBuilder<Conversation> qb = conversationDao.queryBuilder();
        if (StrUtils.isEmpty(AppContext.getAppContext().getLoginNumber())) {
            return list;
        }
        // 当某一条会话中有消息时，才显示在会话列表中
        qb.where(ConversationDao.Properties.LastMsgContent.isNotNull());
        // 当前登录号码的数据查询
        qb.where(ConversationDao.Properties.LoginNo.eq(AppContext.getAppContext().getLoginNumber()));
        // 先排序置顶时间
        qb.orderDesc(ConversationDao.Properties.StickTime);
        // 再排序最后一条时间
        qb.orderDesc(ConversationDao.Properties.LastMsgTime);
        list = qb.list();
        return list;
    }

    /**
     * 查询当前会话是否置顶
     */
    public boolean isConversationSticked(Conversation conversation) {
        ConversationDao conversationDao = dbDaoHelper.getConversationDao();
        QueryBuilder<Conversation> qb = conversationDao.queryBuilder();
        qb.where(ConversationDao.Properties.ID.eq(conversation.getID()));
        Long time = qb.unique().getStickTime();
        if (time != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param number
     * @return 返回组号码对应的ID
     * @description 通过号码查询组row ID
     */
    public Long getGroupId(String number) {
        PrintLog.d("getGroupId");
        ConversationDao conversationDao = dbDaoHelper.getConversationDao();
        QueryBuilder<Conversation> qb = conversationDao.queryBuilder();
        qb.where(ConversationDao.Properties.GroupNo.eq(number));
        qb.where(ConversationDao.Properties.LoginNo.eq(AppContext.getAppContext().getLoginNumber()));
        List<Conversation> list = qb.list();
        if (list != null && list.size() > 0) {
            return list.get(0).getID();
        } else {
            return null;
        }
    }

    /**
     * @param
     * @return 返回ID的集合
     * @description 查询表中所有的ID，返回ID的集合
     */
    public List<Long> queryConversationAllIds() {
        ConversationDao conversationDao = dbDaoHelper.getConversationDao();
        QueryBuilder<Conversation> qb = conversationDao.queryBuilder();
        Cursor cursor = qb.buildCursor().query();
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        }
        List<Long> ids = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Long id = cursor.getLong(cursor.getColumnIndex(ConversationMsgDao.Properties.ID.columnName));
                PrintLog.d("id = " + id);
                ids.add(id);
            } while (cursor.moveToNext());
        }
        return ids;
    }

    /**
     * @param msgSrcNo
     * @param msgDstNo
     * @return 返回个人号码对应的ID
     * @description 通过号码查询个人row ID
     */
    public Long getPersonId(String msgSrcNo, String msgDstNo) {
        PrintLog.d("getPersonId");
        ConversationDao conversationDao = dbDaoHelper.getConversationDao();
        QueryBuilder<Conversation> qb = conversationDao.queryBuilder();
        qb.where(ConversationDao.Properties.GroupNo.isNull());
        qb.where(ConversationDao.Properties.LoginNo.eq(AppContext.getAppContext().getLoginNumber()));
        qb.whereOr(ConversationDao.Properties.MsgSrcNo.eq(msgSrcNo), ConversationDao.Properties.MsgSrcNo.eq(msgDstNo));
        qb.whereOr(ConversationDao.Properties.MsgDstNo.eq(msgSrcNo), ConversationDao.Properties.MsgDstNo.eq(msgDstNo));
        List<Conversation> list = qb.list();
        if (list != null && list.size() > 0) {
            return list.get(0).getID();
        } else {
            return null;
        }
    }

    /**
     * 通过id查询会话QueryBuilder
     *
     * @param id
     */
    public QueryBuilder<Conversation> queryConversationBuilder(Long id) {
        ConversationDao conversationDao = dbDaoHelper.getConversationDao();
        QueryBuilder<Conversation> qb = conversationDao.queryBuilder();
        qb.where(ConversationDao.Properties.ID.eq(id));
        return qb;
    }

    /**
     * 通过id查询会话列表
     *
     * @param id
     */
    public List<Conversation> queryConversationList(Long id) {
        ConversationDao conversationDao = dbDaoHelper.getConversationDao();
        QueryBuilder<Conversation> qb = conversationDao.queryBuilder();
        qb.where(ConversationDao.Properties.ID.eq(id));
        List<Conversation> list = qb.list();
        return list;
    }

    /**
     * @param
     * @return
     * @description 获得ConversationId
     */
    public Long queryConversationId(String msgSrcNo, String msgDstNo, String groupNo) {
        PrintLog.d("msgSrcNo = " + msgSrcNo + "  msgDstNo = " + msgDstNo + "  groupNo = " + groupNo);
        Conversation conversation;
        Long conversationId;

        // 如果没有则去会话数据库（Conversation）查询
        if (!StrUtils.isEmpty(groupNo)) {
            conversationId = ConversationDBManager.getInstance(mContext).getGroupId(groupNo);
        } else {
            conversationId = ConversationDBManager.getInstance(mContext).getPersonId(msgSrcNo, msgDstNo);
        }

        // 如果查不到则先插入一条数据到会话数据库（Conversation），再获得conversationId
        if (conversationId == null) {
            conversation = new Conversation();
            conversation.setLoginNo(AppContext.getAppContext().getLoginNumber());
            conversation.setMsgSrcNo(msgSrcNo);
            if (!StrUtils.isEmpty(groupNo)) {
                conversation.setMsgDstNo(groupNo);
                conversation.setGroupNo(groupNo);
            } else {
                conversation.setMsgDstNo(msgDstNo);
                conversation.setGroupNo(null);
            }
            conversationId = ConversationDBManager.getInstance(mContext).insertConversation(conversation);
        }
        return conversationId;
    }

    public boolean isConversationExist(Long id) {
        if (id == null) {
            return false;
        }
        ConversationDao conversationDao = dbDaoHelper.getConversationDao();
        QueryBuilder<Conversation> qb = conversationDao.queryBuilder();
        qb.where(ConversationDao.Properties.ID.eq(id));
        qb.where(ConversationDao.Properties.LoginNo.eq(AppContext.getAppContext().getLoginNumber()));
        Conversation conversation = qb.unique();
        if (conversation == null) {
            return false;
        } else {
            return true;
        }
    }
}
