package com.ptyt.uct.model;

import android.content.Context;

import com.ptyt.uct.common.UctApplication;
import com.ptyt.uct.entity.Group;
import com.ptyt.uct.gen.GroupDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * Title: com.ptyt.uct.model
 * Description:
 * Date: 2017/5/16
 * Author: ShaFei
 * Version: V1.0
 */

public class GroupDBManager {

    private Context mContext;
    private static GroupDBManager mInstance;
    private DBDaoHelper dbDaoHelper;

    public GroupDBManager(Context context) {
        this.mContext = context;
        if (dbDaoHelper == null) {
            dbDaoHelper = DBDaoHelper.getInstance(context);
        }
    }

    public static GroupDBManager getInstance() {
        if (mInstance == null) {
            mInstance = new GroupDBManager(UctApplication.getInstance());
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
     * @return 对应row ID
     */
    public void deleteAll() {
        GroupDao groupDao = dbDaoHelper.getGroupDao();
        groupDao.deleteAll();
    }

    /**
     * 插入一条记录
     *
     * @param group
     * @return 对应row ID
     */
    public long insertGroup(Group group) {
        GroupDao groupDao = dbDaoHelper.getGroupDao();
        return groupDao.insert(group);
    }

    /**
     * 插入一组记录
     *
     * @param groups
     * @return 是否插入成功
     */
    public boolean insertGroupList(List<Group> groups) {
        if (groups == null || groups.size() == 0) {
            return false;
        }
        dbDaoHelper.getDaoSession(mContext).clear();
        GroupDao groupDao = dbDaoHelper.getGroupDao();
        groupDao.insertInTx(groups);
        return true;
    }

    /**
     * 删除一条记录
     *
     * @param group
     */
    public void deleteGroup(Group group) {
        GroupDao groupDao = dbDaoHelper.getGroupDao();
        groupDao.delete(group);
    }

    /**
     * 删除一组记录
     *
     * @param groups
     */
    public void deleteGroup(List<Group> groups) {
        GroupDao groupDao = dbDaoHelper.getGroupDao();
        groupDao.deleteInTx(groups);
    }

    /**
     * 更新一条记录
     *
     * @param group
     */
    public void updateGroup(Group group) {
        GroupDao groupDao = dbDaoHelper.getGroupDao();
        groupDao.update(group);
    }

    /**
     * 查询组列表
     */
    public List<Group> queryGroupList() {
        //dbDaoHelper.getDaoSession(mContext).clear();
        GroupDao groupDao = dbDaoHelper.getGroupDao();
        QueryBuilder<Group> qb = groupDao.queryBuilder();
        // 再排序最后一条时间
        //qb.orderDesc(GroupDao.Properties.RecordTime);
        List<Group> list = qb.list();
        return list;
    }

    /**
     * 根据组id查询某组
     */
    public Group queryGroupByID(String groupId) {
        //dbDaoHelper.getDaoSession(mContext).clear();
        GroupDao groupDao = dbDaoHelper.getGroupDao();
        Group group = groupDao.queryBuilder()
                .where(GroupDao.Properties.GrouTel.eq(groupId))
                .unique();
        return group;
    }
}
