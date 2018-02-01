package com.ptyt.uct.gen;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.ptyt.uct.entity.Conversation;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "TB_CONVERSATION".
*/
public class ConversationDao extends AbstractDao<Conversation, Long> {

    public static final String TABLENAME = "TB_CONVERSATION";

    /**
     * Properties of entity Conversation.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property ID = new Property(0, Long.class, "ID", true, "id");
        public final static Property IsNotify = new Property(1, Integer.class, "isNotify", false, "is_notify");
        public final static Property MsgSrcNo = new Property(2, String.class, "msgSrcNo", false, "msg_src_no");
        public final static Property MsgDstNo = new Property(3, String.class, "msgDstNo", false, "msg_dst_no");
        public final static Property GroupNo = new Property(4, String.class, "groupNo", false, "group_no");
        public final static Property LoginNo = new Property(5, String.class, "loginNo", false, "login_no");
        public final static Property LastMsgType = new Property(6, Integer.class, "lastMsgType", false, "last_msg_type");
        public final static Property LastMsgContent = new Property(7, String.class, "lastMsgContent", false, "last_msg_content");
        public final static Property LastMsgTime = new Property(8, Long.class, "lastMsgTime", false, "last_msg_time");
        public final static Property LastMsgStatus = new Property(9, Integer.class, "lastMsgStatus", false, "last_msg_status");
        public final static Property LastMsgDirection = new Property(10, Integer.class, "lastMsgDirection", false, "last_msg_direction");
        public final static Property UnreadMsgCounts = new Property(11, Integer.class, "unreadMsgCounts", false, "unread_msg_counts");
        public final static Property StickTime = new Property(12, Long.class, "stickTime", false, "stick_time");
    };


    public ConversationDao(DaoConfig config) {
        super(config);
    }
    
    public ConversationDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"TB_CONVERSATION\" (" + //
                "\"id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: ID
                "\"is_notify\" INTEGER," + // 1: isNotify
                "\"msg_src_no\" TEXT NOT NULL ," + // 2: msgSrcNo
                "\"msg_dst_no\" TEXT NOT NULL ," + // 3: msgDstNo
                "\"group_no\" TEXT," + // 4: groupNo
                "\"login_no\" TEXT," + // 5: loginNo
                "\"last_msg_type\" INTEGER," + // 6: lastMsgType
                "\"last_msg_content\" TEXT," + // 7: lastMsgContent
                "\"last_msg_time\" INTEGER," + // 8: lastMsgTime
                "\"last_msg_status\" INTEGER," + // 9: lastMsgStatus
                "\"last_msg_direction\" INTEGER," + // 10: lastMsgDirection
                "\"unread_msg_counts\" INTEGER," + // 11: unreadMsgCounts
                "\"stick_time\" INTEGER);"); // 12: stickTime
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"TB_CONVERSATION\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Conversation entity) {
        stmt.clearBindings();
 
        Long ID = entity.getID();
        if (ID != null) {
            stmt.bindLong(1, ID);
        }
 
        Integer isNotify = entity.getIsNotify();
        if (isNotify != null) {
            stmt.bindLong(2, isNotify);
        }
        stmt.bindString(3, entity.getMsgSrcNo());
        stmt.bindString(4, entity.getMsgDstNo());
 
        String groupNo = entity.getGroupNo();
        if (groupNo != null) {
            stmt.bindString(5, groupNo);
        }
 
        String loginNo = entity.getLoginNo();
        if (loginNo != null) {
            stmt.bindString(6, loginNo);
        }
 
        Integer lastMsgType = entity.getLastMsgType();
        if (lastMsgType != null) {
            stmt.bindLong(7, lastMsgType);
        }
 
        String lastMsgContent = entity.getLastMsgContent();
        if (lastMsgContent != null) {
            stmt.bindString(8, lastMsgContent);
        }
 
        Long lastMsgTime = entity.getLastMsgTime();
        if (lastMsgTime != null) {
            stmt.bindLong(9, lastMsgTime);
        }
 
        Integer lastMsgStatus = entity.getLastMsgStatus();
        if (lastMsgStatus != null) {
            stmt.bindLong(10, lastMsgStatus);
        }
 
        Integer lastMsgDirection = entity.getLastMsgDirection();
        if (lastMsgDirection != null) {
            stmt.bindLong(11, lastMsgDirection);
        }
 
        Integer unreadMsgCounts = entity.getUnreadMsgCounts();
        if (unreadMsgCounts != null) {
            stmt.bindLong(12, unreadMsgCounts);
        }
 
        Long stickTime = entity.getStickTime();
        if (stickTime != null) {
            stmt.bindLong(13, stickTime);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Conversation entity) {
        stmt.clearBindings();
 
        Long ID = entity.getID();
        if (ID != null) {
            stmt.bindLong(1, ID);
        }
 
        Integer isNotify = entity.getIsNotify();
        if (isNotify != null) {
            stmt.bindLong(2, isNotify);
        }
        stmt.bindString(3, entity.getMsgSrcNo());
        stmt.bindString(4, entity.getMsgDstNo());
 
        String groupNo = entity.getGroupNo();
        if (groupNo != null) {
            stmt.bindString(5, groupNo);
        }
 
        String loginNo = entity.getLoginNo();
        if (loginNo != null) {
            stmt.bindString(6, loginNo);
        }
 
        Integer lastMsgType = entity.getLastMsgType();
        if (lastMsgType != null) {
            stmt.bindLong(7, lastMsgType);
        }
 
        String lastMsgContent = entity.getLastMsgContent();
        if (lastMsgContent != null) {
            stmt.bindString(8, lastMsgContent);
        }
 
        Long lastMsgTime = entity.getLastMsgTime();
        if (lastMsgTime != null) {
            stmt.bindLong(9, lastMsgTime);
        }
 
        Integer lastMsgStatus = entity.getLastMsgStatus();
        if (lastMsgStatus != null) {
            stmt.bindLong(10, lastMsgStatus);
        }
 
        Integer lastMsgDirection = entity.getLastMsgDirection();
        if (lastMsgDirection != null) {
            stmt.bindLong(11, lastMsgDirection);
        }
 
        Integer unreadMsgCounts = entity.getUnreadMsgCounts();
        if (unreadMsgCounts != null) {
            stmt.bindLong(12, unreadMsgCounts);
        }
 
        Long stickTime = entity.getStickTime();
        if (stickTime != null) {
            stmt.bindLong(13, stickTime);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Conversation readEntity(Cursor cursor, int offset) {
        Conversation entity = new Conversation( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // ID
            cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1), // isNotify
            cursor.getString(offset + 2), // msgSrcNo
            cursor.getString(offset + 3), // msgDstNo
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // groupNo
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // loginNo
            cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6), // lastMsgType
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // lastMsgContent
            cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8), // lastMsgTime
            cursor.isNull(offset + 9) ? null : cursor.getInt(offset + 9), // lastMsgStatus
            cursor.isNull(offset + 10) ? null : cursor.getInt(offset + 10), // lastMsgDirection
            cursor.isNull(offset + 11) ? null : cursor.getInt(offset + 11), // unreadMsgCounts
            cursor.isNull(offset + 12) ? null : cursor.getLong(offset + 12) // stickTime
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Conversation entity, int offset) {
        entity.setID(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setIsNotify(cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1));
        entity.setMsgSrcNo(cursor.getString(offset + 2));
        entity.setMsgDstNo(cursor.getString(offset + 3));
        entity.setGroupNo(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setLoginNo(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setLastMsgType(cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6));
        entity.setLastMsgContent(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setLastMsgTime(cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8));
        entity.setLastMsgStatus(cursor.isNull(offset + 9) ? null : cursor.getInt(offset + 9));
        entity.setLastMsgDirection(cursor.isNull(offset + 10) ? null : cursor.getInt(offset + 10));
        entity.setUnreadMsgCounts(cursor.isNull(offset + 11) ? null : cursor.getInt(offset + 11));
        entity.setStickTime(cursor.isNull(offset + 12) ? null : cursor.getLong(offset + 12));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Conversation entity, long rowId) {
        entity.setID(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Conversation entity) {
        if(entity != null) {
            return entity.getID();
        } else {
            return null;
        }
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
