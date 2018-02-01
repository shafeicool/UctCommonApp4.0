package com.ptyt.uct.model;

import android.content.Context;

import com.ptyt.uct.gen.CallRecordDao;
import com.ptyt.uct.gen.ContactDao;
import com.ptyt.uct.gen.ContactFileDao;
import com.ptyt.uct.gen.ConversationDao;
import com.ptyt.uct.gen.ConversationMsgDao;
import com.ptyt.uct.gen.DaoMaster;
import com.ptyt.uct.gen.DaoSession;
import com.ptyt.uct.gen.GroupDao;
import com.ptyt.uct.gen.GroupUserDao;
import com.ptyt.uct.gen.LoginUserDao;


/**
 * xfcui
 * <p>
 * 2017/5/11.
 */

public class DBDaoHelper {
    private static DBDaoHelper instanse;
    private Context mContext;
    private DaoMaster daoMaster;
    private DaoSession daoSession;

    private static final String dbName = "ptyt.db";


    /**
     * Description:
     * <p>
     * param:
     * <p>
     * return：
     */
    private DBDaoHelper(Context context) {
        mContext = context;
    }

    public static DBDaoHelper getInstance(Context context) {
        if (instanse == null) {
            synchronized (DBDaoHelper.class) {
                if (instanse == null) {
                    instanse = new DBDaoHelper(context);
                }
            }
        }
        return instanse;
    }


    /**
     * 取得DaoMaster
     *
     * @param context
     * @return DaoMaster
     */
    public DaoMaster getDaoMaster(Context context) {
        if (daoMaster == null) {
            //            DaoMaster.DevOpenHelper helper=new DaoMaster.DevOpenHelper(context,dbName);
            DatabaseHelper helper = new DatabaseHelper(context, dbName, null);
            daoMaster = new DaoMaster(helper.getWritableDb());
        }
        return daoMaster;
    }

    /**
     * 取得DaoSession
     *
     * @param context
     * @return DaoSession
     */
    public DaoSession getDaoSession(Context context) {
        if (daoSession == null) {
            if (daoMaster == null) {
                daoMaster = getDaoMaster(context);
            }
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }

    public LoginUserDao getLoginUserDao() {
        return getDaoSession(mContext).getLoginUserDao();
    }

    public ContactDao getContactDao() {
        return getDaoSession(mContext).getContactDao();
    }

    public ContactFileDao getContactFileDao() {
        return getDaoSession(mContext).getContactFileDao();
    }

    public ConversationDao getConversationDao() {
        return getDaoSession(mContext).getConversationDao();
    }

    public GroupDao getGroupDao() {
        return getDaoSession(mContext).getGroupDao();
    }

    public GroupUserDao getGroupUserDao() {
        return getDaoSession(mContext).getGroupUserDao();
    }

    public ConversationMsgDao getMessageDao() {
        return getDaoSession(mContext).getConversationMsgDao();
    }

    public CallRecordDao getCallRecordDao() {
        return getDaoSession(mContext).getCallRecordDao();
    }
}
