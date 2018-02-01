package com.ptyt.uct.model;

import android.content.Context;

import com.ptyt.uct.entity.GroupUser;
import com.ptyt.uct.gen.GroupUserDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * @Description:
 * @Date: 2017/12/19
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class GroupUserDBManager {
    private Context mContext;
    private static GroupUserDBManager mInstance;
    private DBDaoHelper dbDaoHelper;

    public GroupUserDBManager(Context context) {
        this.mContext = context;
        if (dbDaoHelper == null) {
            dbDaoHelper = DBDaoHelper.getInstance(context);
        }
    }

    public static GroupUserDBManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new GroupUserDBManager(context);
        }
        return mInstance;
    }

    public void release() {
        if (mInstance != null) {
            mInstance = null;
        }
    }

    public void deleteAll() {
        GroupUserDao groupUserDao = dbDaoHelper.getGroupUserDao();
        groupUserDao.deleteAll();
    }

    /**
     * 插入一条记录
     *
     * @param user
     * @return 对应row ID
     */
    public long insertItem(GroupUser user) {
        GroupUserDao groupUserDao = dbDaoHelper.getGroupUserDao();
        return groupUserDao.insert(user);
    }

    /**
     * 删除一条记录
     *
     * @param user
     */
    public void deleteItem(GroupUser user) {
        GroupUserDao groupUserDao = dbDaoHelper.getGroupUserDao();
        groupUserDao.delete(user);
    }



    /**
     * 更新一条记录
     *
     * @param groupUser
     */
    public void updateUser(GroupUser groupUser) {
        GroupUserDao groupUserDao = dbDaoHelper.getGroupUserDao();
        groupUserDao.update(groupUser);
    }

    /**
     * 查询用户列表
     */
    public List<GroupUser> queryUserList() {
        GroupUserDao groupUserDao = dbDaoHelper.getGroupUserDao();
        QueryBuilder<GroupUser> qb = groupUserDao.queryBuilder();
        List<GroupUser> list = qb.list();
        return list;
    }

    /**
     * 根据用户id查询某用户
     */
    public GroupUser queryUserByID(String userId) {
        //dbDaoHelper.getDaoSession(mContext).clear();
        GroupUserDao groupUserDao = dbDaoHelper.getGroupUserDao();
        GroupUser groupUser = groupUserDao.queryBuilder()
                .where(GroupUserDao.Properties.UserTel.eq(userId))
                .unique();
        return groupUser;
    }



    /**
     * 插入一组记录
     *
     * @param groupId
     * @param newGroupUsers
     * @return 是否插入成功
     */
    public boolean insertUserList(String groupId,List<GroupUser> newGroupUsers) {
        if (newGroupUsers == null || newGroupUsers.size() == 0) {
            return false;
        }
        dbDaoHelper.getDaoSession(mContext).clear();
        GroupUserDao groupUserDao = dbDaoHelper.getGroupUserDao();
        List<GroupUser> oldGroupUsers = groupUserDao.queryBuilder()
                .where(GroupUserDao.Properties.GroupId.eq(groupId))
                .list();
        groupUserDao.deleteInTx(oldGroupUsers);
        groupUserDao.insertInTx(newGroupUsers);
        return true;
    }

    /**
     * 删除一组用户
     */
    public void deleteUserListByGroupID(String groupId) {
        dbDaoHelper.getDaoSession(mContext).clear();
        GroupUserDao groupUserDao = dbDaoHelper.getGroupUserDao();
        List<GroupUser> groupUsers = groupUserDao.queryBuilder()
                .where(GroupUserDao.Properties.GroupId.eq(groupId))
                .list();
        groupUserDao.deleteInTx(groupUsers);
    }

    /**
     * 根据组id查询某用户列表
     */
    public List<GroupUser> queryUserListByGroupID(String groupId) {
        dbDaoHelper.getDaoSession(mContext).clear();
        GroupUserDao groupUserDao = dbDaoHelper.getGroupUserDao();
        List<GroupUser> groupUsers = groupUserDao.queryBuilder()
                .where(GroupUserDao.Properties.GroupId.eq(groupId))
                .list();
        return groupUsers;
    }
}
