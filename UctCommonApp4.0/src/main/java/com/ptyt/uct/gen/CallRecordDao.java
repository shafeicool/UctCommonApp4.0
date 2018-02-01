package com.ptyt.uct.gen;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.ptyt.uct.entity.CallRecord;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "TB_CALL_RECORD".
*/
public class CallRecordDao extends AbstractDao<CallRecord, Long> {

    public static final String TABLENAME = "TB_CALL_RECORD";

    /**
     * Properties of entity CallRecord.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property ID = new Property(0, Long.class, "ID", true, "id");
        public final static Property Name = new Property(1, String.class, "name", false, "name");
        public final static Property Number = new Property(2, String.class, "number", false, "number");
        public final static Property CallTime = new Property(3, String.class, "callTime", false, "call_time");
        public final static Property RecordTime = new Property(4, Long.class, "recordTime", false, "record_time");
        public final static Property Type = new Property(5, Integer.class, "type", false, "type");
        public final static Property IsRead = new Property(6, Integer.class, "isRead", false, "is_read");
    };


    public CallRecordDao(DaoConfig config) {
        super(config);
    }
    
    public CallRecordDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"TB_CALL_RECORD\" (" + //
                "\"id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: ID
                "\"name\" TEXT," + // 1: name
                "\"number\" TEXT NOT NULL ," + // 2: number
                "\"call_time\" TEXT," + // 3: callTime
                "\"record_time\" INTEGER," + // 4: recordTime
                "\"type\" INTEGER," + // 5: type
                "\"is_read\" INTEGER);"); // 6: isRead
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"TB_CALL_RECORD\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, CallRecord entity) {
        stmt.clearBindings();
 
        Long ID = entity.getID();
        if (ID != null) {
            stmt.bindLong(1, ID);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(2, name);
        }
        stmt.bindString(3, entity.getNumber());
 
        String callTime = entity.getCallTime();
        if (callTime != null) {
            stmt.bindString(4, callTime);
        }
 
        Long recordTime = entity.getRecordTime();
        if (recordTime != null) {
            stmt.bindLong(5, recordTime);
        }
 
        Integer type = entity.getType();
        if (type != null) {
            stmt.bindLong(6, type);
        }
 
        Integer isRead = entity.getIsRead();
        if (isRead != null) {
            stmt.bindLong(7, isRead);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, CallRecord entity) {
        stmt.clearBindings();
 
        Long ID = entity.getID();
        if (ID != null) {
            stmt.bindLong(1, ID);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(2, name);
        }
        stmt.bindString(3, entity.getNumber());
 
        String callTime = entity.getCallTime();
        if (callTime != null) {
            stmt.bindString(4, callTime);
        }
 
        Long recordTime = entity.getRecordTime();
        if (recordTime != null) {
            stmt.bindLong(5, recordTime);
        }
 
        Integer type = entity.getType();
        if (type != null) {
            stmt.bindLong(6, type);
        }
 
        Integer isRead = entity.getIsRead();
        if (isRead != null) {
            stmt.bindLong(7, isRead);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public CallRecord readEntity(Cursor cursor, int offset) {
        CallRecord entity = new CallRecord( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // ID
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // name
            cursor.getString(offset + 2), // number
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // callTime
            cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4), // recordTime
            cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5), // type
            cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6) // isRead
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, CallRecord entity, int offset) {
        entity.setID(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setNumber(cursor.getString(offset + 2));
        entity.setCallTime(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setRecordTime(cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4));
        entity.setType(cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5));
        entity.setIsRead(cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(CallRecord entity, long rowId) {
        entity.setID(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(CallRecord entity) {
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
