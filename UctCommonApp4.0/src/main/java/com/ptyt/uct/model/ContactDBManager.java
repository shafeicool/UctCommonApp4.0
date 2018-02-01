package com.ptyt.uct.model;

import android.content.Context;
import android.text.TextUtils;

import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.entity.Contact;
import com.ptyt.uct.gen.ContactDao;
import com.ptyt.uct.utils.StrUtils;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * Title: com.ptyt.uct.model
 * Description:
 * Date: 2017/5/16
 * Author: ShaFei
 * Version: V1.0
 */

public class ContactDBManager {

    private Context mContext;
    private static ContactDBManager mInstance;
    private DBDaoHelper dbDaoHelper;

    public ContactDBManager(Context context) {
        this.mContext = context;
        if (dbDaoHelper == null) {
            dbDaoHelper = DBDaoHelper.getInstance(context);
        }
    }

    public static ContactDBManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ContactDBManager(context);
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
     * @param contact
     * @return 对应row ID
     */
    public long insertContact(Contact contact) {
        ContactDao contactDao = dbDaoHelper.getContactDao();
        return contactDao.insert(contact);
    }

    /**
     * 插入一组记录
     *
     * @param contacts
     * @return 是否插入成功
     */
    public boolean insertContactList(List<Contact> contacts) {
        if (contacts == null || contacts.size() == 0) {
            return false;
        }
        dbDaoHelper.getDaoSession(mContext).clear();
        ContactDao contactDao = dbDaoHelper.getContactDao();
        contactDao.deleteAll();
        Database database = contactDao.getDatabase();
        String sql = "UPDATE sqlite_sequence SET seq = 0 WHERE name = '" + ContactDao.TABLENAME + "'"; //删除所有数据后，id清零
        database.execSQL(sql);
        contactDao.insertInTx(contacts);
        return true;
    }

    /**
     * 删除一条记录
     *
     * @param contact
     */
    public void deleteContact(Contact contact) {
        ContactDao contactDao = dbDaoHelper.getContactDao();
        contactDao.delete(contact);
    }

    /**
     * 删除所有记录
     *
     * @param
     */
    public void deleteContacts() {
        dbDaoHelper.getDaoSession(mContext).clear();
        ContactDao contactDao = dbDaoHelper.getContactDao();
        contactDao.deleteAll();
        Database database = contactDao.getDatabase();
        String sql = "UPDATE sqlite_sequence SET seq = 0 WHERE name = '" + ContactDao.TABLENAME + "'"; //删除所有数据后，id清零
        database.execSQL(sql);
    }

    /**
     * 更新一条记录
     *
     * @param contact
     */
    public void updateContact(Contact contact) {
        ContactDao contactDao = dbDaoHelper.getContactDao();
        contactDao.update(contact);
    }

    /**
     * 查询通讯录列表
     */
    public List<Contact> queryContactList() {
        dbDaoHelper.getDaoSession(mContext).clear();
        ContactDao contactDao = dbDaoHelper.getContactDao();
        QueryBuilder<Contact> qb = contactDao.queryBuilder();
        qb.where(ContactDao.Properties.Number.notEq(AppContext.getAppContext().getLoginNumber()));
        List<Contact> list = qb.list();
        return list;
    }

    /**
     * 查询通讯录
     */
    public String queryContactName(String number) {
        if (TextUtils.isEmpty(number)) {
            return "";
        }
        dbDaoHelper.getDaoSession(mContext).clear();
        ContactDao contactDao = dbDaoHelper.getContactDao();
        QueryBuilder<Contact> qb = contactDao.queryBuilder();
        Contact contact = qb.where(ContactDao.Properties.Number.eq(number)).unique();
        if (contact == null) {
            return number;
        } else {
            if (StrUtils.isEmpty(contact.getDesc())) {
                return contact.getNumber();
            }
            return contact.getDesc();
        }
    }
}
