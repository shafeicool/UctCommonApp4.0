package com.ptyt.uct.callback;

import android.content.Context;
import android.os.Message;

import com.android.uct.ContactCallBack;
import com.android.uct.DataTransportInfoListener;
import com.android.uct.bean.ContactUser;
import com.android.uct.exception.UctLibException;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.PrintLog;
import com.ptyt.uct.common.AppContext;
import com.ptyt.uct.entity.Contact;
import com.ptyt.uct.entity.EventBean;
import com.ptyt.uct.model.ContactDBManager;
import com.ptyt.uct.utils.ConstantUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * @Description:
 * @Date: 2017/5/9
 * @Author: ShaFei
 * @Version: V1.0
 */

public class DataTransportInfoCallBack extends BaseCallBack {

    private static DataTransportInfoCallBack instance = null;
    private Context mContext = null;
    private static final int DOWNLOAD_CONTACT = 0;
    private List<Contact> contactList = new ArrayList<>();

    public static synchronized DataTransportInfoCallBack getDataTransportInfoCallBack() {
        if (instance == null) {
            instance = new DataTransportInfoCallBack();
        }
        return instance;
    }

    @Override
    public void init(Context context) {
        mContext = context;
        PrintLog.i("注册DataTransportInfoCallBack");
        // 通讯录回调
        UctClientApi.registerObserver(contactCallBack, ContactCallBack.CONTACTCALLBACK_INDEX);
        // 文件传输信息上报
        UctClientApi.registerObserver(dataTransportInfoListener, DataTransportInfoListener.DATATRANSPORTINFOLISTENER_INDEX);
        setNewMyLooper(true);
    }

    @Override
    public void release() {
        PrintLog.i("反注册DataTransportInfoCallBack");
        UctClientApi.unregisterObserver(contactCallBack, ContactCallBack.CONTACTCALLBACK_INDEX);
        UctClientApi.unregisterObserver(dataTransportInfoListener, DataTransportInfoListener.DATATRANSPORTINFOLISTENER_INDEX);
        super.closeHandlerThread();
    }

    /**
     * 通讯录信息上报
     */
    private ContactCallBack contactCallBack = new ContactCallBack() {
        /**
         * Description: 这个接口返回用户号码所在的组
         * CreateTime:  2017年8月28日 下午1:49:17
         *
         * @author YuGuoCheng
         * @email yuguocheng@ptyt.com.cn
         * @param result 0表示返回成功 其他表示失败
         * @param number 用户号码
         * @param groupNumber 用户号码所在的组 多个组是用#隔开
         * @see com.android.uct.ContactCallBack#organizationContact(int, java.lang.String, java.lang.String)
         */
        @Override
        public void organizationContact(int result, String number, String groupNumber) {
            //PrintLog.i("organizationContact [result=" + result + ",number=" + number + ",groupNumber=" + groupNumber + "]");
        }

        /**
         * Description: 返回当前登录用户所属的通讯录 如果下载的类型为0是只返回所有的用户 如果下载类型为1时返回用户和组
         * CreateTime:  2017年8月28日 下午1:51:08
         * @author YuGuoCheng
         * @email yuguocheng@ptyt.com.cn
         * @param result
         * @param contactUser 这个对象里有个sourceType属性 这个属性就表示是用户还是组 1为用户 2为组
         * @see com.android.uct.ContactCallBack#onContactCallBack(int, com.android.uct.bean.ContactUser)
         */
        @Override
        public void onContactCallBack(int result, ContactUser contactUser) {
            //PrintLog.i("onContactCallBack [result=" + result + ",contactUser=" + contactUser.toString() + "]");
            if (result == 0) {
//                if (!AppContext.getAppContext().getLoginNumber().equals(contactUser.getNumber())) {
                    Contact contact = new Contact();
                    contact.setID(null);
                    contact.setContactFileID(0);
                    contact.setDesc(contactUser.getDesc());
                    contact.setName(contactUser.getName());
                    contact.setNumber(contactUser.getNumber());
                    contact.setParentNum(contactUser.getParentNum());
                    contact.setType(contactUser.getType());
                    contact.setSourceType(contactUser.getSourceType());
                    contactList.add(contact);
//                }
            } else {
                PrintLog.i("onContactCallBack failed");
            }
        }
    };

    /**
     * 文件传输IP 端口信息上报
     */
    private DataTransportInfoListener dataTransportInfoListener = new DataTransportInfoListener() {

        @Override
        public int UCT_SetDataTransportInfo(String pcSrc, String pcDst, int usType,
                                            int usMode, String ip, int port, String userName, String passWord,
                                            String path, String exInfo) throws UctLibException {
            PrintLog.i("UCT_SetDataTransportInfo [pcSrc=" + pcSrc + ", pcDst=" + pcDst + ", usType="
                    + usType + ", usMode=" + usMode + ", ip=" + ip + ", port="
                    + port + ", userName=" + userName + ", passWord=" + passWord
                    + ", path=" + path + ", exInfo=" + exInfo + "]");

            // 检查是否有升级的版本
            SoftUpgradeCallBack.getSoftUpgradeCallBack().checkNewVersion();
            sendMsgDelayed(true, DOWNLOAD_CONTACT, 500);
            return 0;
        }
    };

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case DOWNLOAD_CONTACT:
                contactList.clear();
                PrintLog.i("DOWNLOAD_CONTACT");
                PrintLog.i("AppContext.getAppContext().getCurrentNodeDn() = " + AppContext.getAppContext().getCurrentNodeDn());
                //第一个参数是节点号节点号登录成功后在响应接口里会返回  第二个参数是下载类型 0只下载用户 1下载用户和组关系
                int isDownloadSuccess = UctClientApi.downloadContact(AppContext.getAppContext().getCurrentNodeDn(), 0);
                PrintLog.d("isDownloadSuccess = " + isDownloadSuccess);
                if (isDownloadSuccess == 0) {
                    boolean isInsertSuccess = ContactDBManager.getInstance(mContext).insertContactList(contactList);
                    if (isInsertSuccess) {
                        EventBus.getDefault().post(new EventBean(ConstantUtils.ACTION_INSERT_CONTACT));
                        PrintLog.d("插入通讯录数据库成功");
                    } else {
                        PrintLog.d("插入通讯录数据库失败");
                    }
                    PrintLog.d("通讯录下载成功");
                } else {
                    PrintLog.d("通讯录下载失败");
                }
                break;
        }
        return false;
    }

}
