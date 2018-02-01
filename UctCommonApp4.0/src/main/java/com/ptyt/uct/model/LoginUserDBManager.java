package com.ptyt.uct.model;

import android.content.Context;

import com.ptyt.uct.entity.LoginUser;
import com.ptyt.uct.gen.LoginUserDao;

import org.greenrobot.greendao.query.QueryBuilder;

/**
 * Title: com.ptyt.uct.model
 * Description:
 * Date: 2017/12/20
 * Author: ShaFei
 * Version: V1.0
 */

public class LoginUserDBManager {

    private Context mContext;
    private static LoginUserDBManager mInstance;
    private DBDaoHelper dbDaoHelper;

    public LoginUserDBManager(Context context) {
        this.mContext = context;
        if (dbDaoHelper == null) {
            dbDaoHelper = DBDaoHelper.getInstance(context);
        }
    }

    public static LoginUserDBManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new LoginUserDBManager(context);
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
     * @param loginUser
     * @return 对应row ID
     */
    public long insertLoginUser(LoginUser loginUser) {
        LoginUserDao loginUserDao = dbDaoHelper.getLoginUserDao();
        return loginUserDao.insert(loginUser);
    }

    public void deleteLoginUser(LoginUser loginUser) {
        LoginUserDao loginUserDao = dbDaoHelper.getLoginUserDao();
        loginUserDao.delete(loginUser);
    }

    public void updateLoginUser(LoginUser loginUser) {
        LoginUserDao loginUserDao = dbDaoHelper.getLoginUserDao();
        loginUserDao.update(loginUser);
    }

    public LoginUser queryLoginUser(Long id) {
        LoginUserDao loginUserDao = dbDaoHelper.getLoginUserDao();
        QueryBuilder<LoginUser> qb = loginUserDao.queryBuilder();
        qb.where(LoginUserDao.Properties.ID.eq(id));
        return qb.unique();
    }

}
