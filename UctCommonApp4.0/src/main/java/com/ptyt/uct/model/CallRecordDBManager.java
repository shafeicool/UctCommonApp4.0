package com.ptyt.uct.model;

import android.content.Context;

import com.ptyt.uct.entity.CallRecord;
import com.ptyt.uct.gen.CallRecordDao;
import com.ptyt.uct.utils.ConstantUtils;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * Title: com.ptyt.uct.model
 * Description:
 * Date: 2017/5/16
 * Author: ShaFei
 * Version: V1.0
 */

public class CallRecordDBManager {

    private Context mContext;
    private static CallRecordDBManager mInstance;
    private DBDaoHelper dbDaoHelper;

    public CallRecordDBManager(Context context) {
        this.mContext = context;
        if (dbDaoHelper == null) {
            dbDaoHelper = DBDaoHelper.getInstance(context);
        }
    }

    public static CallRecordDBManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new CallRecordDBManager(context);
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
     * @param callRecord
     * @return 对应row ID
     */
    public long insertCallRecord(CallRecord callRecord) {
        CallRecordDao callRecordDao = dbDaoHelper.getCallRecordDao();
        return callRecordDao.insert(callRecord);
    }

    /**
     * 插入一组记录
     *
     * @param callRecords
     * @return 是否插入成功
     */
    public boolean insertCallRecordList(List<CallRecord> callRecords) {
        if (callRecords == null || callRecords.size() == 0) {
            return false;
        }
        dbDaoHelper.getDaoSession(mContext).clear();
        CallRecordDao callRecordDao = dbDaoHelper.getCallRecordDao();
        callRecordDao.insertInTx(callRecords);
        return true;
    }

    /**
     * 删除一条记录
     *
     * @param callRecord
     */
    public void deleteCallRecord(CallRecord callRecord) {
        CallRecordDao callRecordDao = dbDaoHelper.getCallRecordDao();
        callRecordDao.delete(callRecord);
    }

    /**
     * 删除一组记录
     *
     * @param callRecords
     */
    public void deleteCallRecord(List<CallRecord> callRecords) {
        CallRecordDao callRecordDao = dbDaoHelper.getCallRecordDao();
        callRecordDao.deleteInTx(callRecords);
    }

    /**
     * 更新一条记录
     *
     * @param callRecord
     */
    public void updateCallRecord(CallRecord callRecord) {
        CallRecordDao callRecordDao = dbDaoHelper.getCallRecordDao();
        callRecordDao.update(callRecord);
    }

    /**
     * 查询通讯录列表
     */
    public List<CallRecord> queryCallRecordList() {
        dbDaoHelper.getDaoSession(mContext).clear();
        CallRecordDao callRecordDao = dbDaoHelper.getCallRecordDao();
        QueryBuilder<CallRecord> qb = callRecordDao.queryBuilder();
        // 再排序最后一条时间
        qb.orderDesc(CallRecordDao.Properties.RecordTime);
        List<CallRecord> list = qb.list();
        return list;
    }

    /**
     * 查询通讯录未读数
     */
    public int queryCallRecordUnread() {
        dbDaoHelper.getDaoSession(mContext).clear();
        CallRecordDao callRecordDao = dbDaoHelper.getCallRecordDao();
        QueryBuilder<CallRecord> qb = callRecordDao.queryBuilder();
        // 再排序最后一条时间
        qb.where(CallRecordDao.Properties.IsRead.eq(ConstantUtils.CALL_RECORD_UNREAD));
        List<CallRecord> list = qb.list();
        return list.size();
    }

}
