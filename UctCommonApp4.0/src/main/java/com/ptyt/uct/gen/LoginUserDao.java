package com.ptyt.uct.gen;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.ptyt.uct.entity.LoginUser;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "TB_LOGIN_USER".
*/
public class LoginUserDao extends AbstractDao<LoginUser, Long> {

    public static final String TABLENAME = "TB_LOGIN_USER";

    /**
     * Properties of entity LoginUser.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property ID = new Property(0, Long.class, "ID", true, "id");
        public final static Property LoginNumber = new Property(1, String.class, "loginNumber", false, "login_number");
        public final static Property LoginPassword = new Property(2, String.class, "loginPassword", false, "login_password");
        public final static Property LoginIp = new Property(3, String.class, "loginIp", false, "login_ip");
    };


    public LoginUserDao(DaoConfig config) {
        super(config);
    }
    
    public LoginUserDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"TB_LOGIN_USER\" (" + //
                "\"id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: ID
                "\"login_number\" TEXT NOT NULL ," + // 1: loginNumber
                "\"login_password\" TEXT NOT NULL ," + // 2: loginPassword
                "\"login_ip\" TEXT NOT NULL );"); // 3: loginIp
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"TB_LOGIN_USER\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, LoginUser entity) {
        stmt.clearBindings();
 
        Long ID = entity.getID();
        if (ID != null) {
            stmt.bindLong(1, ID);
        }
        stmt.bindString(2, entity.getLoginNumber());
        stmt.bindString(3, entity.getLoginPassword());
        stmt.bindString(4, entity.getLoginIp());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, LoginUser entity) {
        stmt.clearBindings();
 
        Long ID = entity.getID();
        if (ID != null) {
            stmt.bindLong(1, ID);
        }
        stmt.bindString(2, entity.getLoginNumber());
        stmt.bindString(3, entity.getLoginPassword());
        stmt.bindString(4, entity.getLoginIp());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public LoginUser readEntity(Cursor cursor, int offset) {
        LoginUser entity = new LoginUser( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // ID
            cursor.getString(offset + 1), // loginNumber
            cursor.getString(offset + 2), // loginPassword
            cursor.getString(offset + 3) // loginIp
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, LoginUser entity, int offset) {
        entity.setID(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setLoginNumber(cursor.getString(offset + 1));
        entity.setLoginPassword(cursor.getString(offset + 2));
        entity.setLoginIp(cursor.getString(offset + 3));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(LoginUser entity, long rowId) {
        entity.setID(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(LoginUser entity) {
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