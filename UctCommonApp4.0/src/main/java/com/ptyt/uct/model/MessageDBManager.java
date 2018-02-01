package com.ptyt.uct.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.entity.Conversation;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.gen.ConversationMsgDao;
import com.ptyt.uct.utils.StrUtils;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Title: com.ptyt.uct.model
 * Description:
 * Date: 2017/5/16
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageDBManager {

    private static MessageDBManager mInstance;
    private DBDaoHelper dbDaoHelper;
    private Context mContext;

    private MessageDBManager(Context context) {
        mContext = context;
        if (dbDaoHelper == null) {
            dbDaoHelper = DBDaoHelper.getInstance(context);
        }
    }

    public static MessageDBManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MessageDBManager(context);
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
     * @param message
     */
    public long insertMessage(ConversationMsg message) {
        ConversationMsgDao conversationMsgDao = dbDaoHelper.getMessageDao();
        return conversationMsgDao.insert(message);
    }

    /**
     * 插入用户集合
     *
     * @param messages
     */
    public void insertMessageList(List<ConversationMsg> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        ConversationMsgDao conversationMsgDao = dbDaoHelper.getMessageDao();
        conversationMsgDao.insertInTx(messages);
    }

    /**
     * 删除一条记录
     *
     * @param message
     */
    public void deleteMessage(ConversationMsg message) {
        ConversationMsgDao conversationMsgDao = dbDaoHelper.getMessageDao();
        conversationMsgDao.delete(message);
    }

    /**
     * 删除条件等于msgConversationId的记录
     *
     * @param msgConversationId
     * @return 返回被删除的记录
     */
    public ConversationMsg deleteMessage(long msgConversationId) {
        ConversationMsgDao conversationMsgDao = dbDaoHelper.getMessageDao();
        QueryBuilder<ConversationMsg> qb = conversationMsgDao.queryBuilder();
        qb.where(ConversationMsgDao.Properties.MsgConversationId.eq(msgConversationId));
        qb.orderAsc(ConversationMsgDao.Properties.MsgTime);
        ConversationMsg message = qb.list().get(0);
        conversationMsgDao.delete(message);
        return message;
    }

    /**
     * 删除条件等于msgConversationId，总数超过limitCount的记录，超过时间limitTime
     *
     * @param limitCount
     * @param limitTime
     */
    public void deleteMessageByLimit(long limitCount, long limitTime) {
        ConversationMsgDao conversationMsgDao = dbDaoHelper.getMessageDao();
        QueryBuilder<ConversationMsg> qb = conversationMsgDao.queryBuilder();
        //        PrintLog.d("----------------搜索MsgConversationId开始----------------");
        //        qb.where(ConversationMsgDao.Properties.MsgConversationId.eq(msgConversationId));
        //        PrintLog.d("----------------搜索MsgConversationId结束----------------");

        // 超过limitCount条记录，删掉
        if (qb.count() > limitCount) { // 大于10000条
            int count = (int) (qb.count() - limitCount);
            qb.limit(count);
            PrintLog.d("----------------数目限制删除Begin----------------");
            conversationMsgDao.deleteInTx(qb.list());
            PrintLog.d("----------------数目限制删除End----------------");
        }

        // 搜索与视频属性匹配的条目
        PrintLog.d("----------------搜索消息类型为视频Begin----------------");
        qb.whereOr(ConversationMsgDao.Properties.MsgType.eq(MessageDBConstant.INFO_TYPE_VIDEO), ConversationMsgDao.Properties.MsgType.eq(MessageDBConstant.INFO_TYPE_CAMERA_VIDEO));
        PrintLog.d("----------------搜索消息类型为视频End----------------");
        // 获取游标
        Cursor cursor = qb.buildCursor().query();
        if (cursor == null || cursor.getCount() == 0) {
            return;
        }
        // 移动到第一个
        if (cursor.moveToFirst()) {
            //            int count = 0;
            do {
                // 获取条目时间
                long record = cursor.getLong(cursor.getColumnIndex(ConversationMsgDao.Properties.MsgTime.columnName));
                // 如果超过limitTime时间，则累加记录
                if (StrUtils.getCurrentTimes() - record > limitTime) {  //大于604800000L 七天
                    //                    count++;
                } else {
                    break;
                }
            } while (cursor.moveToNext());
            // 如果有超出limitTime的条目，则删除
            //            if (count != 0) {
            //                qb.limit(count);
            //                PrintLog.d("----------------时间限制删除开始----------------");
            //                conversationMsgDao.deleteInTx(qb.list());
            //                PrintLog.d("----------------时间限制删除结束----------------");
            //            }
        }
    }

    /**
     * 更新一条记录
     *
     * @param conversationMsg
     */
    public void updateMessage(ConversationMsg conversationMsg) {
        ConversationMsgDao conversationMsgDao = dbDaoHelper.getMessageDao();
        conversationMsgDao.update(conversationMsg);
    }

    /**
     * 更新信息读状态为已读
     *
     * @param msgConversationId
     */
    public void updateReadStatus(Long msgConversationId) {
        ConversationMsgDao conversationMsgDao = dbDaoHelper.getMessageDao();
        Database database = conversationMsgDao.getDatabase();
        String sql = "UPDATE "
                + ConversationMsgDao.TABLENAME
                + " SET "
                + ConversationMsgDao.Properties.ReadStatus.columnName + "=" + MessageDBConstant.ALREAD_MSG
                + " WHERE "
                + ConversationMsgDao.Properties.MsgConversationId.columnName + "=" + msgConversationId
                + " AND "
                + ConversationMsgDao.Properties.ReadStatus.columnName + "=" + MessageDBConstant.UNREAD_MSG;
        database.execSQL(sql);
    }

    /**
     * 更新音频读状态为已读
     *
     * @param conversationMsg
     */
    public void updateAudioReadStatus(ConversationMsg conversationMsg) {
        ConversationMsgDao conversationMsgDao = dbDaoHelper.getMessageDao();
        conversationMsg.setAudioReadStatus(MessageDBConstant.AUDIO_ALREAD_MSG);
        conversationMsgDao.update(conversationMsg);
    }

    /**
     * 查询信息列表
     */
    public List<ConversationMsg> queryMessageList() {
        ConversationMsgDao conversationMsgDao = dbDaoHelper.getMessageDao();
        QueryBuilder<ConversationMsg> qb = conversationMsgDao.queryBuilder();
        List<ConversationMsg> list = qb.list();
        return list;
    }

    /**
     * 查询所有消息的未读数
     */
    public String queryMessageUnreadCount() {
        ConversationMsgDao conversationMsgDao = dbDaoHelper.getMessageDao();
        QueryBuilder<ConversationMsg> qb = conversationMsgDao.queryBuilder();
        qb.where(ConversationMsgDao.Properties.ReadStatus.eq(MessageDBConstant.UNREAD_MSG));
        // 以后要考虑不同用户登录查询，需要增加登录名判断
        //        qb.where(ConversationMsgDao.Properties.LoginNo.eq(AppContext.getAppContext().getLoginNumber()));
        return qb.list().size() + "";
    }

    /**
     * 通过msgConversationId查询信息QueryBuilder
     *
     * @param msgConversationId
     */
    public QueryBuilder<ConversationMsg> queryMessageBuilderById(Long msgConversationId) {
        if (msgConversationId == null) {
            return null;
        } else {
            ConversationMsgDao conversationMsgDao = dbDaoHelper.getMessageDao();
            QueryBuilder<ConversationMsg> qb = conversationMsgDao.queryBuilder();
            qb.where(ConversationMsgDao.Properties.MsgConversationId.eq(msgConversationId));
            return qb;
        }
    }

    /**
     * 通过msgConversationId查询所有的图片和视频列表
     *
     * @param msgConversationId
     */
    public List<ConversationMsg> queryMessagePhotoById(Long msgConversationId) {
        if (msgConversationId == null) {
            return null;
        } else {
            ConversationMsgDao conversationMsgDao = dbDaoHelper.getMessageDao();
            QueryBuilder<ConversationMsg> qb = conversationMsgDao.queryBuilder();
            qb.where(ConversationMsgDao.Properties.MsgConversationId.eq(msgConversationId));
            qb.whereOr(ConversationMsgDao.Properties.MsgType.eq(MessageDBConstant.INFO_TYPE_IMAGE),
                    ConversationMsgDao.Properties.MsgType.eq(MessageDBConstant.INFO_TYPE_VIDEO),
                    ConversationMsgDao.Properties.MsgType.eq(MessageDBConstant.INFO_TYPE_CAMERA_VIDEO));
            return qb.list();
        }
    }

    public boolean querySmsIdIsExist(String chSmsId) {
        ConversationMsg conversationMsg = queryConversationMsgBySmsId(chSmsId);
        return (conversationMsg == null) ? false : true;
    }

    /**
     * 查询所有的图片、视频和文件列表
     *
     * @param
     */
    public List<ConversationMsg> queryMessageFile() {
        ConversationMsgDao conversationMsgDao = dbDaoHelper.getMessageDao();
        QueryBuilder<ConversationMsg> qb = conversationMsgDao.queryBuilder();
        qb.whereOr(ConversationMsgDao.Properties.MsgType.eq(MessageDBConstant.INFO_TYPE_AUDIO),
                ConversationMsgDao.Properties.MsgType.eq(MessageDBConstant.INFO_TYPE_IMAGE),
                ConversationMsgDao.Properties.MsgType.eq(MessageDBConstant.INFO_TYPE_VIDEO),
                ConversationMsgDao.Properties.MsgType.eq(MessageDBConstant.INFO_TYPE_CAMERA_VIDEO),
                ConversationMsgDao.Properties.MsgType.eq(MessageDBConstant.INFO_TYPE_FILE),
                ConversationMsgDao.Properties.MsgType.eq(MessageDBConstant.INFO_TYPE_MY_LOCATION));
        return qb.list();
    }

    /**
     * @param ids
     * @return 返回最后一条记录的集合
     * @description 通过会话表（tb_msg_conversation）中的ID查询信息表（tb_msginfo）中的最后一条记录，返回最后一条记录的集合
     */
    public List<Conversation> queryMessageListById(List<Long> ids) {
        if (ids == null || ids.size() == 0) {
            return null;
        }
        List<Conversation> list = new ArrayList<>();
        ConversationMsgDao conversationMsgDao = dbDaoHelper.getMessageDao();
        QueryBuilder<ConversationMsg> qb;
        for (int i = 0; i <= ids.size() - 1; i++) {
            Long id = ids.get(i);
            PrintLog.d("id2 = " + id);
            qb = conversationMsgDao.queryBuilder();
            Cursor cursor = qb.where(ConversationMsgDao.Properties.MsgConversationId.eq(id))
                    .orderAsc(ConversationMsgDao.Properties.MsgTime)
                    .buildCursor()
                    .query();
            if (cursor == null || cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToLast();
            int recvNotify = cursor.getInt(cursor.getColumnIndex(ConversationMsgDao.Properties.RecvNotify.columnName));
            String msgSrcNo = cursor.getString(cursor.getColumnIndex(ConversationMsgDao.Properties.MsgSrcNo.columnName));
            String msgDstNo = cursor.getString(cursor.getColumnIndex(ConversationMsgDao.Properties.MsgDstNo.columnName));
            String groupNo = cursor.getString(cursor.getColumnIndex(ConversationMsgDao.Properties.GroupNo.columnName));
            int msgType = cursor.getInt(cursor.getColumnIndex(ConversationMsgDao.Properties.MsgType.columnName));
            String content = cursor.getString(cursor.getColumnIndex(ConversationMsgDao.Properties.Content.columnName));
            long msgTime = cursor.getLong(cursor.getColumnIndex(ConversationMsgDao.Properties.MsgTime.columnName));
            qb = conversationMsgDao.queryBuilder();
            long unreadCount = qb.where(ConversationMsgDao.Properties.MsgConversationId.eq(id), ConversationMsgDao.Properties.ReadStatus.eq(MessageDBConstant.UNREAD_MSG))
                    .count();
            String loginNo = AppContext.getAppContext().getLoginNumber();
            PrintLog.d("recvNotify = " + recvNotify
                    + ", msgSrcNo = " + msgSrcNo
                    + ", msgDstNo = " + msgDstNo
                    + ", groupNo = " + groupNo
                    + ", msgType = " + msgType
                    + ", content = " + content
                    + ", msgTime = " + msgTime
                    + ", unreadCount = " + unreadCount
                    + ", loginNo = " + loginNo);

            Conversation conversation = new Conversation();
            conversation.setID(id);
            conversation.setIsNotify(recvNotify);
            conversation.setMsgSrcNo(msgSrcNo);
            conversation.setMsgDstNo(msgDstNo);
            if (!StrUtils.isEmpty(groupNo)) {
                // 组聊天
                conversation.setGroupNo(groupNo);
            } else {
                // 点对点聊天
                conversation.setGroupNo(null);
            }
            conversation.setLoginNo(loginNo);
            conversation.setLastMsgType(msgType);
            conversation.setLastMsgContent(content);
            conversation.setLastMsgTime(msgTime);
            conversation.setUnreadMsgCounts((int) unreadCount);

            list.add(conversation);
        }
        return list;
    }

    public ConversationMsg queryConversationMsgBySmsId2(String chSmsId) {
        if (TextUtils.isEmpty(chSmsId)) {
            PrintLog.e("chSmsId can not empty.");
            throw new IllegalStateException("chSmsId can not empty.");
        }
        ConversationMsgDao conversationMsgDao = dbDaoHelper.getMessageDao();
        QueryBuilder<ConversationMsg> qb = conversationMsgDao.queryBuilder();
        qb.where(ConversationMsgDao.Properties.MsgUctId.eq(chSmsId));
        ConversationMsg conversationMsg = qb.unique();
        return conversationMsg;
    }


    public ConversationMsg queryConversationMsgBySmsId(String chSmsId) {
        if (TextUtils.isEmpty(chSmsId)) {
            PrintLog.e("chSmsId can not be empty.");
            throw new IllegalStateException("chSmsId can not be empty.");
        }
        ConversationMsgDao conversationMsgDao = dbDaoHelper.getMessageDao();
        QueryBuilder<ConversationMsg> qb = conversationMsgDao.queryBuilder();
        qb.where(ConversationMsgDao.Properties.MsgUctId.eq(chSmsId));
        if (qb.list() == null || qb.list().size() == 0) {
            PrintLog.e("qb.list is null.");
            return null;
        }
        boolean isExist;
        for (int i = 0; i < qb.list().size(); i++) {
            ConversationMsg conversationMsg = qb.list().get(i);
            isExist = ConversationDBManager.getInstance(mContext).isConversationExist(conversationMsg.getMsgConversationId());
            if (isExist) {
                return conversationMsg;
            }
        }
        PrintLog.e("Conversation is not exists.");
        return null;
    }

    public List<ConversationMsg> queryConversationMsgByMsgStatus(Integer status1, Integer status2, Integer... status) {
        ConversationMsgDao conversationMsgDao = dbDaoHelper.getMessageDao();
        QueryBuilder<ConversationMsg> qb = conversationMsgDao.queryBuilder();
        WhereCondition[] whereConditionList = new WhereCondition[status.length];
        for (int i = 0; i < status.length; i++) {
            whereConditionList[i] = ConversationMsgDao.Properties.MsgStatus.eq(status[i]);
        }
        qb.whereOr(ConversationMsgDao.Properties.MsgStatus.eq(status1), ConversationMsgDao.Properties.MsgStatus.eq(status2), whereConditionList);
        return qb.list();
    }
}
