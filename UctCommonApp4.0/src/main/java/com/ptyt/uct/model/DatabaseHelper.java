package com.ptyt.uct.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.gen.ConversationDao;
import com.ptyt.uct.gen.ConversationMsgDao;
import com.ptyt.uct.gen.DaoMaster;

import org.greenrobot.greendao.database.Database;

/**
 * Title: com.ptyt.uct.model
 * Description:
 * Date: 2017/6/9
 * Author: ShaFei
 * Version: V1.0
 */

public class DatabaseHelper extends DaoMaster.OpenHelper {
    public DatabaseHelper(Context context, String name) {
        super(context, name);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onCreate(Database db) {
        super.onCreate(db);
        createTrigger(db);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        for (int i = oldVersion; i < newVersion; i++) {
            switch (oldVersion) {
                case 1:
                    DaoMaster.dropAllTables(db, true);
                    DaoMaster.createAllTables(db, false);
                    break;
            }
        }
        createTrigger(db);
    }

    private void createTrigger(Database db) {
        try {
            /** 插入消息触发会话更新 */
            String sql = "CREATE TRIGGER IF NOT EXISTS im_update_conversation_on_insert AFTER INSERT ON "
                    + ConversationMsgDao.TABLENAME
                    + " BEGIN  "
                    + " UPDATE "
                    + ConversationDao.TABLENAME
                    + " SET "
                    + ConversationDao.Properties.IsNotify.columnName + "=new." + ConversationMsgDao.Properties.RecvNotify.columnName + ","
                    + ConversationDao.Properties.LastMsgType.columnName + "=new." + ConversationMsgDao.Properties.MsgType.columnName + ","
                    + ConversationDao.Properties.LastMsgContent.columnName + "=new." + ConversationMsgDao.Properties.Content.columnName + ","
                    + ConversationDao.Properties.LastMsgTime.columnName + "=new." + ConversationMsgDao.Properties.MsgTime.columnName + ","
                    + ConversationDao.Properties.LastMsgStatus.columnName + "=new." + ConversationMsgDao.Properties.MsgStatus.columnName + ","
                    + ConversationDao.Properties.LastMsgDirection.columnName + "=new." + ConversationMsgDao.Properties.MsgDirection.columnName
                    + " WHERE "
                    + ConversationDao.Properties.ID.columnName + "=new." + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + ";"

                    + " UPDATE "
                    + ConversationDao.TABLENAME
                    + " SET "
                    + ConversationDao.Properties.UnreadMsgCounts.columnName
                    + "=(SELECT COUNT(*) FROM "
                    + ConversationMsgDao.TABLENAME
                    + " WHERE "
                    + ConversationMsgDao.Properties.ReadStatus.columnName + "=" + MessageDBConstant.UNREAD_MSG
                    + " AND "
                    + ConversationMsgDao.Properties.MsgConversationId.columnName + "=new." + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + ")"
                    + " WHERE "
                    + ConversationDao.Properties.ID.columnName + "=new." + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + "; END;";
            PrintLog.e("sql = " + sql);
            db.execSQL(sql);

            /** 删除会话触发关联消息删除 */
            sql = "CREATE TRIGGER IF NOT EXISTS im_update_message_on_delete AFTER DELETE ON "
                    + ConversationDao.TABLENAME
                    + " BEGIN DELETE FROM "
                    + ConversationMsgDao.TABLENAME
                    + " WHERE "
                    + ConversationMsgDao.Properties.MsgConversationId.columnName + " = old." + ConversationDao.Properties.ID.columnName
                    + "; END;";
            db.execSQL(sql);

            /** 触发消息未读数更新 **/
            sql = "CREATE TRIGGER IF NOT EXISTS im_update_conversation_read_on_update AFTER UPDATE OF "
                    + ConversationMsgDao.Properties.ReadStatus.columnName
                    + " ON "
                    + ConversationMsgDao.TABLENAME
                    + " BEGIN"
                    + " UPDATE "
                    + ConversationDao.TABLENAME
                    + " SET "
                    + ConversationDao.Properties.UnreadMsgCounts.columnName
                    + "=(SELECT COUNT(*) FROM "
                    + ConversationMsgDao.TABLENAME
                    + " WHERE "
                    + ConversationMsgDao.Properties.ReadStatus.columnName + "=" + MessageDBConstant.UNREAD_MSG
                    + " AND "
                    + ConversationMsgDao.Properties.MsgConversationId.columnName + "=old." + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + ")"
                    + " WHERE "
                    + ConversationDao.Properties.ID.columnName + "=old." + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + "; END;";
            db.execSQL(sql);

            /** 更新最后一条消息状态，触发会话消息状态 **/
            sql = "CREATE TRIGGER IF NOT EXISTS im_update_conversation_status_on_update AFTER UPDATE OF "
                    + ConversationMsgDao.Properties.MsgStatus.columnName
                    + " ON "
                    + ConversationMsgDao.TABLENAME
                    + " BEGIN"
                    + " UPDATE "
                    + ConversationDao.TABLENAME
                    + " SET "
                    + ConversationDao.Properties.LastMsgStatus.columnName
                    + "=(SELECT "
                    + ConversationMsgDao.Properties.MsgStatus.columnName
                    + " FROM "
                    + ConversationMsgDao.TABLENAME
                    + " WHERE "
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + "=old."
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + " ORDER BY "
                    + ConversationMsgDao.Properties.MsgTime.columnName
                    + " DESC LIMIT 1) WHERE "
                    + ConversationDao.Properties.ID.columnName
                    + "=old."
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + ";"
                    + " END;";
            db.execSQL(sql);

            /** 删除消息，触发会话更新 **/
            sql = "CREATE TRIGGER IF NOT EXISTS im_update_conversation_on_delete AFTER DELETE ON "
                    + ConversationMsgDao.TABLENAME
                    + " BEGIN "
                    + " UPDATE "
                    + ConversationDao.TABLENAME
                    + " SET "
                    + ConversationDao.Properties.UnreadMsgCounts.columnName
                    + "=(SELECT COUNT(*) FROM "
                    + ConversationMsgDao.TABLENAME
                    + " WHERE "
                    + ConversationMsgDao.Properties.ReadStatus.columnName
                    + "=0 AND "
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + "=old."
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + ")"
                    + " WHERE "
                    + ConversationDao.Properties.ID.columnName
                    + "=old."
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + ";"

                    + "UPDATE "
                    + ConversationDao.TABLENAME
                    + " SET "
                    + ConversationDao.Properties.LastMsgContent.columnName
                    + "=(SELECT "
                    + ConversationMsgDao.Properties.Content.columnName
                    + " FROM "
                    + ConversationMsgDao.TABLENAME
                    + " WHERE "
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + "=old."
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + " ORDER BY "
                    + ConversationMsgDao.Properties.MsgTime.columnName
                    + " DESC LIMIT 1) WHERE "
                    + ConversationDao.Properties.ID.columnName
                    + "=old."
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + ";"

                    + "UPDATE "
                    + ConversationDao.TABLENAME
                    + " SET "
                    + ConversationDao.Properties.LastMsgTime.columnName
                    + "=(SELECT "
                    + ConversationMsgDao.Properties.MsgTime.columnName
                    + " FROM "
                    + ConversationMsgDao.TABLENAME
                    + " WHERE "
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + "=old."
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + " ORDER BY "
                    + ConversationMsgDao.Properties.MsgTime.columnName
                    + " DESC LIMIT 1) WHERE "
                    + ConversationDao.Properties.ID.columnName
                    + "=old."
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + ";"

                    + "UPDATE "
                    + ConversationDao.TABLENAME
                    + " SET "
                    + ConversationDao.Properties.LastMsgType.columnName
                    + "=(SELECT "
                    + ConversationMsgDao.Properties.MsgType.columnName
                    + " FROM "
                    + ConversationMsgDao.TABLENAME
                    + " WHERE "
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + "=old."
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + " ORDER BY "
                    + ConversationMsgDao.Properties.MsgTime.columnName
                    + " DESC LIMIT 1) WHERE "
                    + ConversationDao.Properties.ID.columnName
                    + "=old."
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + ";"

                    + "UPDATE "
                    + ConversationDao.TABLENAME
                    + " SET "
                    + ConversationDao.Properties.LastMsgStatus.columnName
                    + "=(SELECT "
                    + ConversationMsgDao.Properties.MsgStatus.columnName
                    + " FROM "
                    + ConversationMsgDao.TABLENAME
                    + " WHERE "
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + "=old."
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + " ORDER BY "
                    + ConversationMsgDao.Properties.MsgTime.columnName
                    + " DESC LIMIT 1) WHERE "
                    + ConversationDao.Properties.ID.columnName
                    + "=old."
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + ";"

                    + "UPDATE "
                    + ConversationDao.TABLENAME
                    + " SET "
                    + ConversationDao.Properties.LastMsgDirection.columnName
                    + "=(SELECT "
                    + ConversationMsgDao.Properties.MsgDirection.columnName
                    + " FROM "
                    + ConversationMsgDao.TABLENAME
                    + " WHERE "
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + "=old."
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + " ORDER BY "
                    + ConversationMsgDao.Properties.MsgTime.columnName
                    + " DESC LIMIT 1) WHERE "
                    + ConversationDao.Properties.ID.columnName
                    + "=old."
                    + ConversationMsgDao.Properties.MsgConversationId.columnName
                    + ";"
                    + " END;";
            db.execSQL(sql);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
