package com.ptyt.uct.services;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.android.uct.EastoneResultListener;
import com.android.uct.IEastoneCfmListener;
import com.android.uct.IUCTShortMsgListener;
import com.android.uct.exception.UctLibException;
import com.android.uct.service.Observable;
import com.android.uct.service.TFileManager;
import com.android.uct.service.TFileSdk;
import com.android.uct.service.UctClientApi;
import com.android.uct.utils.FileUtils;
import com.android.uct.utils.PrintLog;
import com.android.uct.utils.UctLibStringUtils;
import com.android.uct.utils.UctLibUtils;
import com.facebook.stetho.Stetho;
import com.ptyt.uct.activity.MessageActivity;
import com.ptyt.uct.common.AppManager;
import com.ptyt.uct.common.MessageDBConstant;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.entity.MessageMyLocationEntity;
import com.ptyt.uct.model.ConversationDBManager;
import com.ptyt.uct.model.MessageDBManager;
import com.ptyt.uct.utils.AppUtils;
import com.ptyt.uct.utils.SDCardUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.viewinterface.IMessageView;

import net.bither.util.NativeUtil;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.android.uct.utils.FileUtils.getFileName;
import static com.ptyt.uct.common.MessageDBConstant.FILE_STATUS_NOT_DOWNLOAD;
import static com.ptyt.uct.common.MessageDBConstant.FILE_STATUS_RECEIVE_SUCCESS;
import static com.ptyt.uct.common.MessageDBConstant.FILE_STATUS_RECEIVING;
import static com.ptyt.uct.common.MessageDBConstant.FILE_STATUS_WAIT_RECEIVING;
import static com.ptyt.uct.common.MessageDBConstant.IMVT_COM_MSG;
import static com.ptyt.uct.common.MessageDBConstant.INFO_TYPE_AUDIO;
import static com.ptyt.uct.common.MessageDBConstant.INFO_TYPE_CAMERA_VIDEO;
import static com.ptyt.uct.common.MessageDBConstant.INFO_TYPE_FILE;
import static com.ptyt.uct.common.MessageDBConstant.INFO_TYPE_IMAGE;
import static com.ptyt.uct.common.MessageDBConstant.INFO_TYPE_MY_LOCATION;
import static com.ptyt.uct.common.MessageDBConstant.INFO_TYPE_OLD_DEVICE_TEXT;
import static com.ptyt.uct.common.MessageDBConstant.INFO_TYPE_PTT_AUDIO;
import static com.ptyt.uct.common.MessageDBConstant.INFO_TYPE_TEXT;
import static com.ptyt.uct.common.MessageDBConstant.INFO_TYPE_VIDEO;
import static com.ptyt.uct.common.MessageDBConstant.MESSAGE_LIMIT_COUNT;
import static com.ptyt.uct.common.MessageDBConstant.MSG_STATUS_FAIL;
import static com.ptyt.uct.common.MessageDBConstant.MSG_STATUS_SENDING;
import static com.ptyt.uct.common.MessageDBConstant.MSG_STATUS_SEND_SUCCESS;
import static com.ptyt.uct.common.MessageDBConstant.MSG_STATUS_WAIT_SENDING;
import static com.ptyt.uct.common.MessageDBConstant.UNREAD_MSG;


/**
 * 短信相关的
 */
public class MsgBinder extends BaseBinder implements IShortMsgInterface {
    /**
     * 短信分割符号，图片的内容服务器路径和源路径中间用:分开
     */
    private static final String SPLIT_MARK = ":";
    /**
     * 同时运行线程数
     */
    private static final int THREAD_COUNTS = 3;
    /**
     * 每次执行限定个数个任务的线程池
     */
    private ExecutorService mConversationThreadPool = null;
    private ExecutorService addBuffer;
    private Context mContext;
    /**
     * 通知界面的接口回调
     */
    private final Observable<MsgCallBackListener> iMessageViewList = new Observable<>();
    /**
     * 当前上传的任务
     */
    private final Map<String, Callable> currentUploadTask = new HashMap<>();

    MsgBinder(Context context) {
        this.mContext = context;
        UctClientApi.registerObserver(eastoneResultListener, EastoneResultListener.EASTONERESULTLISTENER_INDEX);
        UctClientApi.registerObserver(iEastoneCfmListener, IEastoneCfmListener.IEASTONECFMLISTENER_INDEX);
        UctClientApi.registerObserver(shortMsgListener, IUCTShortMsgListener.IUCTSHORTMSGLISTENER_INDEX);
        mConversationThreadPool = Executors.newFixedThreadPool(THREAD_COUNTS);// 限制线程池大小为count的线程池
        addBuffer = Executors.newFixedThreadPool(THREAD_COUNTS);
        addBuffer.execute(updateMsgStatus);
    }

    /**
     * App启动后查询数据库是否有正在发送的状态如果有将这些状态统一修改成失败状态
     */
    private Runnable updateMsgStatus = new Runnable() {
        @Override
        public void run() {
            //查询未下载状态 正在发送状态 发送中 等待发送的数据，然后统统修改成发送失败的状态
            List<ConversationMsg> conversationMsgsList = MessageDBManager.getInstance(mContext).queryConversationMsgByMsgStatus(FILE_STATUS_NOT_DOWNLOAD, FILE_STATUS_RECEIVING, MSG_STATUS_SENDING, MSG_STATUS_WAIT_SENDING);
            if (conversationMsgsList != null && !conversationMsgsList.isEmpty()) {
                PrintLog.i("queryConversationMsgByMsgStatus[size=" + conversationMsgsList.size() + "]");
                for (int i = 0; i < conversationMsgsList.size(); i++) {
                    ConversationMsg conversationMsg = conversationMsgsList.get(i);
                    conversationMsg.setMsgStatus(MSG_STATUS_FAIL);
                    //更新数据库
                    try {
                        updateConversationBySmsId(conversationMsg.getMsgUctId(), MSG_STATUS_FAIL);
                        //通知界面
                        notifyDataChangedListener(conversationMsg, MsgCallBackListener.MSG_STATUS_CHANGE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        PrintLog.e("updateMsgStatus fail[exception=" + e + "]");
                    }
                }

            }
        }
    };

    @Override
    public void registerObserver(BaseServiceCallBack observer, int index) {
        switch (index) {
            case BaseServiceCallBack.INDEX_IMESSAGEVIEW:
                if (!(observer instanceof MsgCallBackListener)) {
                    throw new ClassCastException("Class conversion exception. " + observer.getClass().getName());
                }
                this.iMessageViewList.registerObserver((MsgCallBackListener) observer);
                break;
        }
    }

    @Override
    public void unRegisterObserver(BaseServiceCallBack observer, int index) {
        switch (index) {
            case BaseServiceCallBack.INDEX_IMESSAGEVIEW:
                if (!(observer instanceof MsgCallBackListener)) {
                    throw new ClassCastException("Class conversion exception. " + observer.getClass().getName());
                }
                this.iMessageViewList.unregisterObserver((MsgCallBackListener) observer);
                break;
        }
    }

    /**
     * 服务退出的时候调用 这里释放资源
     */
    @Override
    public void serviceDestory() {
        UctClientApi.unregisterObserver(eastoneResultListener, EastoneResultListener.EASTONERESULTLISTENER_INDEX);
        UctClientApi.unregisterObserver(iEastoneCfmListener, IEastoneCfmListener.IEASTONECFMLISTENER_INDEX);
        UctClientApi.unregisterObserver(shortMsgListener, IUCTShortMsgListener.IUCTSHORTMSGLISTENER_INDEX);
        iMessageViewList.unregisterAll();
        mConversationThreadPool.shutdown();
        addBuffer.shutdownNow();
        Stetho.initializeWithDefaults(mContext);

    }

    @Override
    public int sendMsg(ConversationMsg conversationMsg) {
        addSendQueue(conversationMsg);
        return 0;
    }

    @Override
    public int downloadMsg(ConversationMsg conversationMsg) {
        addReceiverQueue(conversationMsg, true);
        return 0;
    }

    /**
     * 文本短信比较特殊，需要分割后才能发送
     *
     * @param conversationMsg 短息数据对象
     * @return 0表示成功 -1表示失败
     */
    @Override
    public int sendTextMsg(ConversationMsg conversationMsg) {
        try {
            //这里截取字符串 一条短信最大支持900个字节
            ArrayList<String> msgContent = StrUtils.getSplitString(conversationMsg.getContent());
            int size = msgContent.size();
            //短消息分割ID号
            int serInfo = size - 1;
            for (int i = 0; i < size; i++) {
                //重新组装一个对象 只改变短信内容 msgId 分割ID号 如果没有分割的话分割号iD填写0
                ConversationMsg currentConversationMsg = null;
                try {
                    currentConversationMsg = (ConversationMsg) conversationMsg.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                    PrintLog.e("sendTextMsg error:{" + e.getMessage() + "}");
                }
                //分割后的内容
                String content = msgContent.get(i);
                if (TextUtils.isEmpty(content) || currentConversationMsg == null) {
                    throw new IllegalStateException("content or currentConversationMsg can not empty.{currentConversationMsg=" + currentConversationMsg + ",content=" + content + "}");
                }
                //设置当前的短信内容
                currentConversationMsg.setContent(content);
                byte[] _tmpContent = new byte[0];
                try {
                    _tmpContent = content.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    PrintLog.e("sendTextMsg error:{" + e.getMessage() + "}");
                }
                //设置短信的长度这里指的是UTF-8下的字节数
                currentConversationMsg.setContentLength(_tmpContent.length);
                //生成msgId 这个是唯一的
                String msgId = StrUtils.getSmsId(currentConversationMsg.getMsgDstNo(), currentConversationMsg.getMsgSrcNo());
                currentConversationMsg.setMsgUctId(msgId);
                currentConversationMsg.setMsgTxtSplit(serInfo);
                //设置为等待...
                currentConversationMsg.setMsgStatus(MSG_STATUS_WAIT_SENDING);
                serInfo--;
                ConversationMsg conversation = insertDb(currentConversationMsg);
                //插入到数据库并且添加到发送队列里
                int sendResult = sendMsgCommand(conversation);
                //打印出新对象内容
                PrintLog.i(currentConversationMsg.toString() + ",sendResult=" + sendResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    /**
     * 重新发送的短信不需要插入数据库 直接更新数据库
     *
     * @param conversationMsg 数据库对象
     * @return 0表示成功 非0表示失败
     */
    @Override
    public int reSendMsg(ConversationMsg conversationMsg) {
        PrintLog.i("reSendMsg = " + conversationMsg.toString());
        return addSendQueue(conversationMsg);
    }

    /**
     * 保存数据库并且添加到发送队列里
     *
     * @param conversationMsg 发送短信的数据包
     * @return 返回发送的状态 0为调用成功 -1表示调用失败
     */
    private int addSendQueue(ConversationMsg conversationMsg) {
        try {
            int status = conversationMsg.getMsgStatus();
            //如果不是等待发送的状态表示此短信已经取消发送
            if (status != MSG_STATUS_WAIT_SENDING) {
                PrintLog.w("this message already cancel.");
                return 0;
            }
            MsgSendThread msgSendThread = new MsgSendThread(conversationMsg);
            Future<Integer> msgResult = mConversationThreadPool.submit(msgSendThread);
            Integer result = msgResult.get();
            PrintLog.i("send Msg result{" + result + "}");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            PrintLog.e("addSendQueue error:{ " + e.getMessage() + " }");
            throw new RuntimeException("addSendQueue error:{ " + e.getMessage() + " }");
        }
    }

    @Override
    public int cancelMsg(ConversationMsg conversationMsg) {
        String msgUctId = conversationMsg.getMsgUctId();
        int msgDirection = conversationMsg.getMsgDirection();
        PrintLog.i("{cancelMsg. conversationMsg=" + conversationMsg.toString() + "}");
        try {
            conversationMsg.setMsgStatus(MSG_STATUS_FAIL);
            //更新数据库状态信息为发送失败
            updateConversationBySmsId(conversationMsg.getMsgUctId(), MSG_STATUS_FAIL);
            //通知界面状态
            notifyDataChangedListener(conversationMsg, MsgCallBackListener.MSG_STATUS_CHANGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //如果是自己发出去的短信取消的是上传
        if (msgDirection == MessageDBConstant.IMVT_TO_MSG) {
            MsgSendThread msgSendThread = (MsgSendThread) currentUploadTask.get(msgUctId);
            if (msgSendThread != null) {
                msgSendThread.cancelCurrentUpload(conversationMsg);
                return 0;
            }
        } else {
            MsgReceiverThread msgReceiverThread = (MsgReceiverThread) currentUploadTask.get(msgUctId);
            if (msgReceiverThread != null) {
                msgReceiverThread.cancelDownloadMsg(conversationMsg);
                return 0;
            }
        }
        return -1;
    }

    @Override
    public int deleteMsg(ConversationMsg conversationMsg) {
        int isCancel = cancelMsg(conversationMsg);
        try {
            int isDeleteFile = deleteConversationBySmsId(conversationMsg, true);
            //            notifyDataChangedListener(conversationMsg,MsgCallBackListener.MSG_STATUS_CHANGE);
            PrintLog.i("deleteMsg {isCancel=" + isCancel + ",isDeleteFile=" + isDeleteFile + "}");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("" + e);
        }
        return 0;
    }

    /**
     * 插入数据库并下载文件
     *
     * @param conversationMsg 短信包的对象
     * @return 0表示成功 其他表示失败
     */
    private int addReceiverQueue(ConversationMsg conversationMsg, boolean isdownload) {
        try {
            PrintLog.i("isdownload{" + isdownload + "}");
            if (!isdownload) {
                return 0;
            }
            int status = conversationMsg.getMsgStatus();
            if (status == FILE_STATUS_NOT_DOWNLOAD || status == FILE_STATUS_WAIT_RECEIVING) {
                Future<Integer> msgResult = mConversationThreadPool.submit(new MsgReceiverThread(conversationMsg));
                Integer result = msgResult.get();
                PrintLog.i("receiver Msg result{" + result + "}");
            } else {
                PrintLog.w("this message already cancel.");
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            PrintLog.e("addReceiverQueue error:{ " + e.getMessage() + " }");
            throw new RuntimeException("addReceiverQueue error:{ " + e.getMessage() + " }");
        }
        return 0;
    }

    /**
     * 插入短信到数据库
     *
     * @param conversationMsg 短信对象
     * @return 返回新的短信对象
     */
    protected ConversationMsg insertDb(ConversationMsg conversationMsg) {
        try {
            long conversationId = queryConversationId(conversationMsg);
            long id = MessageDBManager.getInstance(mContext).insertMessage(conversationMsg);
            QueryBuilder<ConversationMsg> qb = MessageDBManager.getInstance(mContext).queryMessageBuilderById(conversationId);
            int size = qb.list().size();
            if (size > MESSAGE_LIMIT_COUNT) {
                //如果该会话的消息超出MESSAGE_LIMIT_COUNT数目，则删除最早的一条记录
                ConversationMsg deleteMessage = MessageDBManager.getInstance(mContext).deleteMessage(conversationId);
                String localPath = deleteMessage.getLocalImgPath();
                if (!TextUtils.isEmpty(localPath) && SDCardUtils.isFileInFolder(localPath)) {
                    FileUtils.deleteAll(new File(localPath));
                }
            }
            if (id > 0) {
                //插入数据库后修改ID号
                conversationMsg.setID(id);
                notifyDataChangedListener(conversationMsg, MsgCallBackListener.MSG_SEND_INSERT_DB);
                return conversationMsg;
            } else {
                PrintLog.e("insert conversationMsg fail. id={ " + id + " }");
                throw new RuntimeException("insert db fail. id{" + id + "}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            PrintLog.e("insertDb error:{ " + e.getMessage() + " }");
            throw new RuntimeException("insertDb error:{ " + e.getMessage() + " }");
        }
    }

    /**
     * 获取会话ID 如果会话ID为空会自动创建一个会话ID
     *
     * @return 返回当前会话ID
     * @para 会话ID
     */
    private long queryConversationId(ConversationMsg conversationMsg) {
        Long onversationId = conversationMsg.getMsgConversationId();
        //判断当前会话是否存在 如果不存在首先创建一个会话
        if (onversationId == null || onversationId < 0) {
            onversationId = ConversationDBManager.getInstance(mContext).queryConversationId(conversationMsg.getMsgSrcNo(), conversationMsg.getMsgDstNo(), conversationMsg.getGroupNo());
            PrintLog.i("This is a new conversation. onversationId{ " + onversationId + " }");
            if (onversationId == null || onversationId < 0) {
                throw new RuntimeException("onversationId can not empty. onversationId{" + onversationId + "}");
            }
            conversationMsg.setMsgConversationId(onversationId);
        }
        return onversationId;
    }

    /**
     * 根据msgId更新状态
     *
     * @param smsId  短信ID
     * @param status 短信的状态
     */
    protected ConversationMsg updateConversationBySmsId(String smsId, int status) throws Exception {
        ConversationMsg dbConversationMsg = MessageDBManager.getInstance(mContext).queryConversationMsgBySmsId(smsId);
        if (dbConversationMsg == null) {
            throw new RuntimeException("dbConversationMsg can not empty.dbConversationMsg={null}");
        }
        dbConversationMsg.setMsgStatus(status);
        MessageDBManager.getInstance(mContext).updateMessage(dbConversationMsg);
        return dbConversationMsg;
    }

    /**
     * 根据msgId更新状态
     *
     * @param smsId  短信ID
     * @param status 短信的状态
     */
    private ConversationMsg updateConversationBySmsId(String smsId, int status, int result) throws Exception {
        ConversationMsg dbConversationMsg = MessageDBManager.getInstance(mContext).queryConversationMsgBySmsId(smsId);
        if (dbConversationMsg == null) {
            throw new RuntimeException("dbConversationMsg can not empty.dbConversationMsg={null}");
        }
        dbConversationMsg.setMsgStatus(status);
        dbConversationMsg.setResult(result);
        MessageDBManager.getInstance(mContext).updateMessage(dbConversationMsg);
        return dbConversationMsg;
    }

    /**
     * 删除短信
     *
     * @param conversationMsg 短信对象
     * @param isDeleteSdData  是否删除短信所对应的资源
     * @return 是否删除成功
     * @throws Exception 删除过程中出现的异常
     */
    private int deleteConversationBySmsId(ConversationMsg conversationMsg, boolean isDeleteSdData) throws Exception {
        MessageDBManager.getInstance(mContext).deleteMessage(conversationMsg);
        if (isDeleteSdData) {
            String localPath = conversationMsg.getLocalImgPath();
            PrintLog.i("{deleteFile filePath=[" + localPath + "]}");
            if (!TextUtils.isEmpty(localPath) && SDCardUtils.isFileInFolder(localPath)) {
                FileUtils.deleteAll(new File(localPath));
            }
        }
        return 0;
    }

    /**
     * 根据msgId更新状态
     *
     * @param smsId    短信ID
     * @param fileSize 文件的大小
     * @param offset   已上传的大小
     */
    private ConversationMsg updateConversationBySmsId(String smsId, long fileSize, long offset) {
        ConversationMsg dbConversationMsg = MessageDBManager.getInstance(mContext).queryConversationMsgBySmsId(smsId);
        if (dbConversationMsg == null) {
            throw new RuntimeException("dbConversationMsg can not empty.dbConversationMsg={null}");
        }
        dbConversationMsg.setFileSize(fileSize);
        dbConversationMsg.setOffSize(offset);
        MessageDBManager.getInstance(mContext).updateMessage(dbConversationMsg);
        return dbConversationMsg;
    }

    /**
     * 发送短信
     */
    private class MsgSendThread implements Callable<Integer> {
        //当前发送短信的短信包信息
        private ConversationMsg mConversationMsg;
        //线程之间通信信息
        private Exchanger<Integer> msgExchanger = null;
        /**
         * 短信发送超时时间
         */
        private static final int SEND_MSG_TIME_OUT = 5000;
        private long lastSendUpdateTime = 0;
        /**
         * 是否停止当前任务
         */
        private boolean isCancelCurrentTask = false;
        /**
         * 当前上传的索引ID
         */
        private int uploadIndex = -1;

        MsgSendThread(ConversationMsg conversationMsg) {
            this.mConversationMsg = conversationMsg;
            String msgId = mConversationMsg.getMsgUctId();
            PrintLog.i("add currentBuffer msgId=" + msgId);
            if (!currentUploadTask.containsKey(msgId)) {
                currentUploadTask.put(msgId, this);
            } else {
                //这里需要判断是否已经在发送中 防止重复发送
                throw new RuntimeException("{msgId=" + msgId + " already exist}");
            }
            msgExchanger = new Exchanger<>();
            try {
                //等待发送...
                mConversationMsg.setMsgStatus(MSG_STATUS_SENDING);
                //更新数据库状态信息
                updateConversationBySmsId(mConversationMsg.getMsgUctId(), MSG_STATUS_SENDING);
                //通知界面状态
                notifyDataChangedListener(conversationMsg, MsgCallBackListener.MSG_STATUS_CHANGE);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("" + e.getMessage());
            }
        }

        private void cancelCurrentUpload(ConversationMsg conversationMsg) {
            isCancelCurrentTask = true;
            String msgId = conversationMsg.getMsgUctId();
            if (currentUploadTask.containsKey(msgId)) {
                PrintLog.i("remove currentBuffer msgId=" + msgId);
                currentUploadTask.remove(msgId);
            }
        }

        /**
         * 此函数是线程池里回调过来的，在子线程中运行
         *
         * @return 返回发送成功或失败的结果
         */
        @Override
        public Integer call() throws Exception {
            int sendResult = -1;
            Integer msgType = mConversationMsg.getMsgType();
            if (msgType == null) {
                throw new RuntimeException("SMS type cannot be empty");
            }
            //发送中...
            mConversationMsg.setMsgStatus(MSG_STATUS_SENDING);
            try {
                //更新数据库
                updateConversationBySmsId(mConversationMsg.getMsgUctId(), MSG_STATUS_SENDING);
                //通知界面
                notifyDataChangedListener(mConversationMsg, MsgCallBackListener.MSG_STATUS_CHANGE);
            } catch (Exception e) {
                throw new RuntimeException("" + e.getMessage());
            }
            PrintLog.i("msgType msgType=｛ " + msgType + " ｝");
            switch (msgType) {
                case INFO_TYPE_TEXT:
                case INFO_TYPE_OLD_DEVICE_TEXT:
                    sendResult = sendMsgCommand(mConversationMsg);
                    break;

                //发送语音短信 图片短信 小视频短信 视频短信 文件短信处理方式一样这里处理相同，后期有区别再分开
                //语音短信
                case INFO_TYPE_AUDIO:
                    //小视频短信
                case INFO_TYPE_VIDEO:
                    //视频短信
                case INFO_TYPE_CAMERA_VIDEO:
                    //文件短信
                case INFO_TYPE_FILE:
                    //位置短信
                case INFO_TYPE_MY_LOCATION:
                    sendResult = sendFileMsg(mConversationMsg, null);
                    break;
                //图片短信
                case INFO_TYPE_IMAGE:
                    String thumbnailImage = "";
                    if (mConversationMsg.getMsgThumbnail() == 0) {// 压缩图片
                        int quality = 50;
                        String tempFileName = FileUtils.getFileName(mConversationMsg.getLocalImgPath());
                        thumbnailImage = UctClientApi.getCacheDir().getAbsolutePath() + File.separator + tempFileName;
                        boolean isCompressSuccessed = NativeUtil.getNativeUtil().compressBitmap(mConversationMsg.getLocalImgPath(), quality, thumbnailImage, true, mContext);
                        if (!isCompressSuccessed) {
                            thumbnailImage = "";
                        }
                    }
                    sendResult = sendFileMsg(mConversationMsg, thumbnailImage);
                    break;

                default:
                    if (UctClientApi.isDebugMode()) {
                        throw new RuntimeException("SMS type does not exist");
                    }

            }
            String msgId = mConversationMsg.getMsgUctId();
            if (currentUploadTask.containsKey(msgId)) {
                PrintLog.i("remove currentBuffer msgId=" + msgId);
                currentUploadTask.remove(msgId);
            }
            return sendResult;
        }

        private int sendFileMsg(final ConversationMsg conversationMsg, String thumbnailImage) throws Exception {
            int sendResult;
            //计算下划线的个数 msgId协议:2017072110351900_523_536_1234564
            int underlineCount = UctLibStringUtils.appearNumber(conversationMsg.getMsgUctId(), UctLibStringUtils.SPLIT_MARK_UNDERLINE);
            //如果下划线个数不是三个表示msgId的协议错误
            if (underlineCount != 3) {
                throw new IllegalArgumentException("Invalid string .chSmsId={ " + conversationMsg.getMsgUctId() + " }");
            }
            String remotePath = mConversationMsg.getRemoteImgPath();
            String filePath = FileUtils.getFilePath(remotePath);
            String localFileName = mConversationMsg.getLocalImgPath();
            File localFile = new File(localFileName);
            if (!localFile.isFile()) {
                throw new RuntimeException("Not file .localFileName=:{" + localFileName + "}");
            }
            PrintLog.e("localFile.canRead() = " + localFile.canRead() + "  localFile.canWrite() = " + localFile.canWrite());
            String remoteFileName = getFileName(remotePath);
            String sendPath = "";
            if (TextUtils.isEmpty(thumbnailImage)) {
                sendPath = localFileName;
            } else {
                sendPath = thumbnailImage;
            }
            //首先上传语音文件
            int upload_trans_index = TFileManager.getFileManager().uploadFile(sendPath, remoteFileName, filePath, SEND_MSG_TIME_OUT, new TFileSdk.OnTransListener() {
                @Override
                public void onTransNotify(TFileSdk.TransNotifyData transNotifyData) {
                    try {
                        int isError = transNotifyData.getError();
                        PrintLog.i("send finish 0 tFileResult={ " + isError + " }");
                        //回调首先要判断是否错误 大于0表示传输文件是出错了
                        if (TFileSdk.TFileSdkApi.TRANS_NOTIFY_TYPE_PROGRESS == isError) {
                            TFileSdk.TransNotifyData.progress pro = transNotifyData.getPro();
                            long fileSize = pro.getFile_size();
                            long offSet = pro.getOffset();
                            PrintLog.i("uploda file{ fileSize=" + fileSize + ",offSet=" + offSet + "}");
                            conversationMsg.setOffSize(offSet);
                            conversationMsg.setFileSize(fileSize);
                            long currentTime = System.currentTimeMillis();
                            long range = Math.abs(currentTime - lastSendUpdateTime);
                            //1秒更新一次
                            if (range >= 1000) {
                                lastSendUpdateTime = currentTime;
                                //根据msgId更新文件大小和上传进度
                                updateConversationBySmsId(mConversationMsg.getMsgUctId(), fileSize, offSet);
                            }
                            //通知界面更新进度
                            notifyDataChangedListener(conversationMsg, MsgCallBackListener.MSG_UPDATE_PROGRESS);
                            if (fileSize == offSet) {
                                PrintLog.i("send finish 1");
                                //根据msgId更新文件大小和上传进度
                                updateConversationBySmsId(mConversationMsg.getMsgUctId(), fileSize, offSet);
                                msgExchanger.exchange(isError);
                                PrintLog.i("send finish 2");
                            } else {
                                //如果当前任务未上传完成 并且状态是停止状态 这里关闭停止
                                if (isCancelCurrentTask) {
                                    PrintLog.i("close current upload begin . uploadIndex=" + uploadIndex);
                                    TFileManager.getFileManager().close(uploadIndex);
                                    msgExchanger.exchange(-1);
                                    isCancelCurrentTask = false;
                                    PrintLog.i("close current upload end. uploadIndex=" + uploadIndex);
                                }
                            }
                        } else {
                            PrintLog.i("send fail 1");
                            //根据msgId更新文件大小和上传进度
                            updateConversationBySmsId(mConversationMsg.getMsgUctId(), 0L, 0L);
                            msgExchanger.exchange(isError);
                            PrintLog.i("send fail 2");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        PrintLog.i("Exception:{" + e.getMessage() + "}");
                        throw new RuntimeException("Exception:{" + e.getMessage() + "}");
                    }
                }
            });
            PrintLog.i("send wiat 1");
            int result = msgExchanger.exchange(null);
            if (!TextUtils.isEmpty(thumbnailImage)) {
                File file = new File(thumbnailImage);
                PrintLog.i("delete image path=" + thumbnailImage + ",fileSize=" + file.length());
                FileUtils.deleteAll(file);
            }
            PrintLog.i("close_trans");
            TFileSdk.getInstance().close_trans(upload_trans_index);
            PrintLog.i("send stop 2 result=" + result);
            sendResult = result;
            //上传文件结束后 如果失败直接返回 如果成功更新
            if (result == 0) {
                PrintLog.i("upload file success");
                //表示上传成功 发送指令
                sendResult = sendMsgCommand(conversationMsg);
            } else {
                //表示上传失败
                conversationMsg.setMsgStatus(MSG_STATUS_FAIL);
                //更新数据库成功状态
                updateConversationBySmsId(conversationMsg.getMsgUctId(), conversationMsg.getMsgStatus());
                //通知界面发送成功
                notifyDataChangedListener(conversationMsg, MsgCallBackListener.MSG_STATUS_CHANGE);
                PrintLog.i("send Msg fail");
            }
            return sendResult;
        }
    }

    /**
     * 发送短信指令并返回发送结果
     *
     * @return 0表示成功 其他表示失败
     */
    private int sendMsgCommand(ConversationMsg mConversationMsg) {
        int sendResult;
        try {
            String groupNumber = mConversationMsg.getGroupNo();
            //组号码为空认为是一对一 组号码不为空认为是组消息
            if (TextUtils.isEmpty(groupNumber)) {
                PrintLog.i("send P2P msg");
                sendResult = UctClientApi.P2PEastTone(
                        mConversationMsg.getMsgDstNo(),
                        mConversationMsg.getMsgUctId(),
                        mConversationMsg.getMsgType(),
                        mConversationMsg.getMsgTxtSplit(),
                        mConversationMsg.getRecvCfm(),
                        mConversationMsg.getRecvNotify(),
                        mConversationMsg.getContentLength(),
                        mConversationMsg.getContent()
                );
            } else {
                //一对多短信
                PrintLog.i("send P2MP msg");
                sendResult = UctClientApi.P2MPEastTone(
                        mConversationMsg.getMsgDstNo(),
                        mConversationMsg.getMsgUctId(),
                        mConversationMsg.getMsgType(),
                        mConversationMsg.getMsgTxtSplit(),
                        mConversationMsg.getRecvCfm(),
                        mConversationMsg.getRecvNotify(),
                        mConversationMsg.getContentLength(),
                        mConversationMsg.getContent()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResult = -1;
            PrintLog.e("sendMsgCommand error{ " + e.getMessage() + " }");
        }
        //如果返回-1为失败刷新数据库通知失败
        if (sendResult == 0) {
            mConversationMsg.setMsgStatus(MSG_STATUS_SEND_SUCCESS);
        } else {
            //更新状态为失败
            mConversationMsg.setMsgStatus(MSG_STATUS_FAIL);
        }
        try {
            //更新状态
            updateConversationBySmsId(mConversationMsg.getMsgUctId(), mConversationMsg.getMsgStatus());
            //通知界面
            notifyDataChangedListener(mConversationMsg, MsgCallBackListener.MSG_STATUS_CHANGE);
        } catch (Exception e) {
            throw new RuntimeException("" + e.getMessage());
        }
        //成功的状态收到服务器确认是才更新数据库
        return sendResult;
    }

    /**
     * 接受短信
     */
    private class MsgReceiverThread implements Callable<Integer> {
        //当前接受短信的短信包信息
        private ConversationMsg mConversationMsg;
        private Exchanger<Integer> msgExchanger;
        /**
         * 短信下载超时时间
         */
        private static final int SEND_MSG_TIME_OUT = 5000;
        /**
         * 上次更新的时间
         */
        private long lastReceiverUpdateTime;
        /**
         * 是否停止当前任务
         */
        private boolean isCancelCurrentTask = false;
        /**
         * 当前下载的索引ID
         */
        private int downloadIndex = -1;

        MsgReceiverThread(ConversationMsg conversationMsg) {
            this.mConversationMsg = conversationMsg;
            String msgId = mConversationMsg.getMsgUctId();
            PrintLog.i("add currentBuffer downloadmsgId=" + msgId);
            if (!currentUploadTask.containsKey(msgId)) {
                currentUploadTask.put(msgId, this);
            } else {
                //这里如果收到同样的短信说明同一个短信有两条消息
                throw new RuntimeException("{downloadmsgId = " + msgId + " already exist}");
            }
            msgExchanger = new Exchanger<>();
            try {
                //等待下载文件...
                mConversationMsg.setMsgStatus(FILE_STATUS_WAIT_RECEIVING);
                //更新数据库状态信息
                updateConversationBySmsId(mConversationMsg.getMsgUctId(), FILE_STATUS_WAIT_RECEIVING);
                //通知界面状态
                notifyDataChangedListener(conversationMsg, MsgCallBackListener.MSG_STATUS_CHANGE);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("" + e.getMessage());
            }
        }

        /**
         * 此函数是线程池里回调过来的，在子线程中运行
         *
         * @return 返回短信下载成功或失败的结果
         */
        @Override
        public Integer call() throws Exception {
            int sendResult = -1;
            Integer msgType = mConversationMsg.getMsgType();
            if (msgType == null) {
                throw new RuntimeException("SMS type cannot be empty");
            }
            //短信下载中
            mConversationMsg.setMsgStatus(FILE_STATUS_RECEIVING);
            try {
                //更新数据库
                updateConversationBySmsId(mConversationMsg.getMsgUctId(), FILE_STATUS_RECEIVING);
                //通知界面
                notifyDataChangedListener(mConversationMsg, MsgCallBackListener.MSG_STATUS_CHANGE);
            } catch (Exception e) {
                throw new RuntimeException("" + e.getMessage());
            }
            PrintLog.i("msgType msgType=｛ " + msgType + " ｝");
            switch (msgType) {
                case INFO_TYPE_TEXT:
                case INFO_TYPE_OLD_DEVICE_TEXT:
                    //文本短信不需要下载
                    sendResult = 0;
                    break;

                //发送语音短信 图片短信 小视频短信 视频短信 文件短信处理方式一样这里处理相同，后期有区别再分开
                //语音短信
                case INFO_TYPE_AUDIO:
                    //图片短信
                case INFO_TYPE_IMAGE:
                    //小视频短信
                case INFO_TYPE_VIDEO:
                    //视频短信
                case INFO_TYPE_CAMERA_VIDEO:
                    //文件短信
                case INFO_TYPE_FILE:
                    //位置短信
                case INFO_TYPE_MY_LOCATION:
                    sendResult = receiverFileMsg(mConversationMsg);
                    break;

                default:
                    if (UctClientApi.isDebugMode()) {
                        throw new RuntimeException("SMS type does not exist");
                    }

            }
            //删除缓存里的信息
            String msgId = mConversationMsg.getMsgUctId();
            if (currentUploadTask.containsKey(msgId)) {
                PrintLog.i("remove currentBuffer msgId=" + msgId);
                currentUploadTask.remove(msgId);
            }
            return sendResult;
        }

        private int receiverFileMsg(final ConversationMsg conversationMsg) throws Exception {
            int sendResult;
            String remotePath = mConversationMsg.getRemoteImgPath();
            String localImgPath = mConversationMsg.getLocalImgPath();
            String sdFilePath = FileUtils.getFilePath(localImgPath);

            String threadName = Thread.currentThread().getName();
            PrintLog.i("currentName=" + threadName);
            //首先下载语音文件
            downloadIndex = TFileManager.getFileManager().downloadFile(remotePath, sdFilePath, SEND_MSG_TIME_OUT, new TFileSdk.OnTransListener() {
                @Override
                public void onTransNotify(TFileSdk.TransNotifyData transNotifyData) {
                    try {
                        String threadName = Thread.currentThread().getName();
                        int isError = transNotifyData.getError();
                        PrintLog.i("currentName=" + threadName);
                        PrintLog.i("receiver File callback 0 tFileResult={ " + isError + " }");
                        //回调首先要判断是否错误 大于0表示传输文件是出错了
                        if (TFileSdk.TFileSdkApi.TRANS_NOTIFY_TYPE_PROGRESS == isError) {
                            TFileSdk.TransNotifyData.progress pro = transNotifyData.getPro();
                            long fileSize = pro.getFile_size();
                            long offSet = pro.getOffset();
                            PrintLog.i("receiver file{ fileSize=" + fileSize + ",offSet=" + offSet + "}");
                            conversationMsg.setOffSize(offSet);
                            conversationMsg.setFileSize(fileSize);
                            long currentTime = System.currentTimeMillis();
                            long range = Math.abs(currentTime - lastReceiverUpdateTime);
                            //1秒更新一次
                            if (range >= 1000) {
                                lastReceiverUpdateTime = currentTime;
                                //更具msgId更新文件大小和上传进度
                                updateConversationBySmsId(mConversationMsg.getMsgUctId(), fileSize, offSet);
                            }
                            //通知界面更新进度
                            notifyDataChangedListener(conversationMsg, MsgCallBackListener.MSG_UPDATE_PROGRESS);
                            if (fileSize == offSet) {
                                //上传完成后再次 更新根据msgId更新文件大小和上传进度
                                updateConversationBySmsId(mConversationMsg.getMsgUctId(), fileSize, offSet);
                                msgExchanger.exchange(isError);
                                PrintLog.i("receiver file finish 1");
                            } else {
                                //如果当前任务未下载完成 并且状态是停止状态 这里关闭停止
                                if (isCancelCurrentTask) {
                                    PrintLog.i("close current download begin . uploadIndex=" + downloadIndex);
                                    TFileManager.getFileManager().close(downloadIndex);
                                    msgExchanger.exchange(-1);
                                    isCancelCurrentTask = false;
                                    PrintLog.i("close current upload end. uploadIndex=" + downloadIndex);
                                }
                            }
                        } else {
                            //上传完成后再次 更新根据msgId更新文件大小和上传进度
                            updateConversationBySmsId(mConversationMsg.getMsgUctId(), 0L, 0L);
                            PrintLog.i("receiver file fail 1");
                            msgExchanger.exchange(isError);
                            PrintLog.i("receiver file fail 2");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        PrintLog.i("Exception:{" + e.getMessage() + "}");
                        throw new RuntimeException("Exception:{" + e.getMessage() + "}");
                    }
                }
            });
            PrintLog.i("receiver wiat 1");
            int result = msgExchanger.exchange(null);
            PrintLog.i("{ close_trans_download_trans_index=" + downloadIndex + "}");
            isCancelCurrentTask = false;
            TFileSdk.getInstance().close_trans(downloadIndex);
            sendResult = result;
            lastReceiverUpdateTime = 0;
            //下载文件结束后 如果失败直接返回 如果成功更新
            if (result == 0) {
                PrintLog.i("download file success");
                String remoteFileName = FileUtils.getFileName(conversationMsg.getRemoteImgPath());
                String newFileName = conversationMsg.getLocalImgPath();
                String localFilePath = FileUtils.getFilePath(newFileName);
                File reNameFile = new File(localFilePath + remoteFileName);
                boolean isRenameSuccess = reNameFile.renameTo(new File(newFileName));
                PrintLog.i("reName file. oldFileName=" + (localFilePath + remoteFileName) + ", newFileName=" + newFileName + ",isRenameSuccess=" + isRenameSuccess);
                conversationMsg.setMsgStatus(FILE_STATUS_RECEIVE_SUCCESS);
            } else {
                //表示上传失败
                conversationMsg.setMsgStatus(MSG_STATUS_FAIL);
                PrintLog.i("receiver Msg fail result=" + result);
            }
            //更新数据库
            updateConversationBySmsId(conversationMsg.getMsgUctId(), conversationMsg.getMsgStatus());
            //通知界面更新进度
            notifyDataChangedListener(conversationMsg, MsgCallBackListener.MSG_STATUS_CHANGE);
            return sendResult;
        }

        private void cancelDownloadMsg(ConversationMsg conversationMsg) {
            isCancelCurrentTask = true;
            String msgId = conversationMsg.getMsgUctId();
            if (currentUploadTask.containsKey(msgId)) {
                PrintLog.i("remove currentBuffer downloadmsgId=" + msgId);
                currentUploadTask.remove(msgId);
            }
        }
    }

    /**
     * 短信发送是否成功的判断
     */
    private EastoneResultListener eastoneResultListener = new EastoneResultListener() {

        @Override
        public int UCT_MSG_EastoneResult(int result, String chSmsId) throws UctLibException {
            if (TextUtils.isEmpty(chSmsId)) {
                throw new RuntimeException("chSmsId can not empty.chSmsId={ " + chSmsId + " }");
            }
            if (result == 0) {
                try {
                    //更新状态
                    ConversationMsg conversationMsg = updateConversationBySmsId(chSmsId, MSG_STATUS_SEND_SUCCESS, result);
                    //通知界面
                    notifyDataChangedListener(conversationMsg, MsgCallBackListener.MSG_STATUS_CHANGE);
                } catch (Exception e) {
                    throw new RuntimeException("" + e.getMessage());
                }
            } else {
                try {
                    //更新状态
                    ConversationMsg conversationMsg = updateConversationBySmsId(chSmsId, MSG_STATUS_FAIL, result);
                    //通知界面
                    notifyDataChangedListener(conversationMsg, MsgCallBackListener.MSG_STATUS_CHANGE);
                } catch (Exception e) {
                    throw new RuntimeException("" + e.getMessage());
                }
            }
            return 0;
        }
    };

    /**
     * 短信对端发过来
     */
    private IUCTShortMsgListener shortMsgListener = new IUCTShortMsgListener() {
        //        private ConversationMsg dbConversationMsg;

        /**
         * 一对一短信
         */
        @Override
        public int UCT_P2P_Eastone(String pcSrc, String pcDst, String chSmsId, int InfoType, int InfoSeq, int RecvCfm, int RecvNotify, int ContentLength, String chMsgContent) throws UctLibException {
            //被叫短信一定要注意，这里有些短信会重复发过来 原因就是由于网络丢包问题对端提示发送失败了其实这端已经收到，对端会重发送消息这边又收到了
            //这里收到消息后首先查询下数据库这个msgId的数据是否存在 如果不存在才插入到数据库 如果存在这条短信要扔掉
            final ConversationMsg dbConversationMsgIsEmpty = MessageDBManager.getInstance(mContext).queryConversationMsgBySmsId(chSmsId);
            if (dbConversationMsgIsEmpty != null) {
                String msgAlradyExist = "This message already exists. UCT_P2P_Eastone [pcSrc=" + pcSrc + ", pcDst=" + pcDst + ", chSmsId="
                        + chSmsId + ", InfoType=" + InfoType + ", InfoSeq=" + InfoSeq
                        + ", RecvCfm=" + RecvCfm + ", RecvNotify=" + RecvNotify
                        + ", ContentLength=" + ContentLength + ", chMsgContent="
                        + chMsgContent + "] dbConversationMsgIsEmpty = " + dbConversationMsgIsEmpty.toString();
                if (UctClientApi.isDebugMode()) {
                    throw new AssertionError(msgAlradyExist);
                } else {
                    PrintLog.e(msgAlradyExist);
                    return -1;
                }
            } else {
                //如果在同一会话聊天，则不播放响铃
                if (!isInMessageActivity(pcSrc)) {
                    AppUtils.playMessageRingtone();
                }
                //如果数据库不存在数据组装一个对象 插入到数据库通知界面
                //-------------------------公共数据开始--------------------------------//
                final ConversationMsg dbConversationMsg = new ConversationMsg();
                dbConversationMsg.setMsgSrcNo(pcSrc);
                dbConversationMsg.setMsgDstNo(pcDst);
                dbConversationMsg.setMsgUctId(chSmsId);
                if (InfoType == INFO_TYPE_PTT_AUDIO) {
                    InfoType = INFO_TYPE_AUDIO;
                }
                dbConversationMsg.setMsgType(InfoType);
                dbConversationMsg.setMsgTxtSplit(InfoSeq);
                dbConversationMsg.setRecvCfm(RecvCfm);
                //表示这是对端发过来的数据
                dbConversationMsg.setMsgDirection(IMVT_COM_MSG);
                //短信内容的长度
                dbConversationMsg.setContentLength(ContentLength);
                //短信设置为未读
                dbConversationMsg.setReadStatus(UNREAD_MSG);
                //纯文本短信的内容
                dbConversationMsg.setContent(chMsgContent);
                //-------------------------公共数据结束--------------------------------//
                //以下不同的短信类型解析也不同
                switch (InfoType) {
                    //文本短信
                    case INFO_TYPE_TEXT:
                    case INFO_TYPE_OLD_DEVICE_TEXT:
                        //计算下划线的个数 msgId协议:2017072110351900_523_536
                        int underlineCount = UctLibStringUtils.appearNumber(chSmsId, UctLibStringUtils.SPLIT_MARK_UNDERLINE);
                        //如果下划线个数不是三个表示msgId的协议错误 这里需要兼容以前的版本 以前的版本这里没有长度所以underlineCount>0或underlineCount<3都是合法的
                        if (underlineCount <= 0) {
                            throw new IllegalArgumentException("Invalid string .chSmsId={ " + chSmsId + " }");
                        }
                        //解析msgId
                        String[] msgIdInfo = UctLibStringUtils.spiltMsgId(chSmsId);
                        //获取短信发送时间
                        String msgTime = msgIdInfo[0];
                        msgTime = msgTime.substring(0, 14);
                        //从MSGID里获取到时间再转换为long
                        Date msgDate = UctLibUtils.strMsgToDate(msgTime);
                        //设置收到短信是的时间
                        dbConversationMsg.setMsgTime(msgDate.getTime());
                        //短信状态
                        dbConversationMsg.setMsgStatus(FILE_STATUS_RECEIVE_SUCCESS);
                        insertDb(dbConversationMsg);
                        break;

                    //语音短信 图片短信 视频短信文件比较小默认自动下载下来
                    //语音短信
                    case INFO_TYPE_AUDIO:
                        //这里组装一个对象
                        assembleMsgObject(dbConversationMsg, true, chSmsId, chMsgContent);
                        final ConversationMsg conversation1 = insertDb(dbConversationMsg);
                        addBuffer.execute(new Runnable() {
                            @Override
                            public void run() {
                                //插入到数据库并下载附件
                                addReceiverQueue(conversation1, true);
                            }
                        });
                        break;
                    //图片短信
                    case INFO_TYPE_IMAGE:
                        //这里组装一个对象
                        assembleMsgObject(dbConversationMsg, false, chSmsId, chMsgContent);
                        final ConversationMsg conversation2 = insertDb(dbConversationMsg);
                        addBuffer.execute(new Runnable() {
                            @Override
                            public void run() {
                                //插入到数据库并下载附件
                                addReceiverQueue(conversation2, true);
                            }
                        });
                        break;

                    //视频短信 文件短信 小视频短信
                    //注意了这里不会自动下载，文件比较大用户点击时才请求下载
                    case INFO_TYPE_FILE:
                        //视频短信
                    case INFO_TYPE_VIDEO:
                        //小视频短信
                    case INFO_TYPE_CAMERA_VIDEO:
                        assembleMsgObject(dbConversationMsg, false, chSmsId, chMsgContent);
                        final ConversationMsg conversation3 = insertDb(dbConversationMsg);
                        addBuffer.execute(new Runnable() {
                            @Override
                            public void run() {
                                //插入到数据库不下载附件
                                addReceiverQueue(conversation3, false);
                            }
                        });
                        break;
                    //位置短信
                    case INFO_TYPE_MY_LOCATION:
                        assembleLocationMsgObject(dbConversationMsg, chSmsId, chMsgContent);
                        final ConversationMsg conversation4 = insertDb(dbConversationMsg);
                        addBuffer.execute(new Runnable() {
                            @Override
                            public void run() {
                                //插入到数据库不下载附件
                                addReceiverQueue(conversation4, true);
                            }
                        });
                        break;
                    default:
                        if (UctClientApi.isDebugMode()) {
                            throw new RuntimeException("SMS type does not exist! UCT_P2P_Eastone [pcSrc=" + pcSrc + ", pcDst=" + pcDst + ", chSmsId="
                                    + chSmsId + ", InfoType=" + InfoType + ", InfoSeq=" + InfoSeq
                                    + ", RecvCfm=" + RecvCfm + ", RecvNotify=" + RecvNotify
                                    + ", ContentLength=" + ContentLength + ", chMsgContent="
                                    + chMsgContent + "]");
                        }


                }
            }
            return 0;
        }

        /**
         * 一对多短信 也就是群聊天
         */
        @Override
        public int UCT_P2MP_Eastone(String pcSrc, String pcDst, String chSmsId, String groupNo, int InfoType, int InfoSeq, int RecvCfm, int RecvNotify, int ContentLength, String chMsgContent) throws UctLibException {
            //被叫短信一定要注意，这里有些短信会重复发过来 原因就是由于网络丢包问题对端提示发送失败了其实这端已经收到，对端会重发送消息这边又收到了
            //这里收到消息后首先查询下数据库这个msgId的数据是否存在 如果不存在才插入到数据库 如果存在这条短信要扔掉
            ConversationMsg dbConversationMsgIsEmpty = MessageDBManager.getInstance(mContext).queryConversationMsgBySmsId(chSmsId);
            if (dbConversationMsgIsEmpty != null) {
                String msgAlradyExist = "This message already exists. UCT_P2MP_Eastone [pcSrc=" + pcSrc + ", pcDst=" + pcDst + ", chSmsId="
                        + chSmsId + ", InfoType=" + InfoType + ", InfoSeq=" + InfoSeq
                        + ", RecvCfm=" + RecvCfm + ", RecvNotify=" + RecvNotify
                        + ", ContentLength=" + ContentLength + ", chMsgContent="
                        + chMsgContent + "] dbConversationMsgIsEmpty = " + dbConversationMsgIsEmpty.toString();
                if (UctClientApi.isDebugMode()) {
                    throw new AssertionError(msgAlradyExist);
                } else {
                    PrintLog.e(msgAlradyExist);
                    return -1;
                }
            } else {
                //如果在同一会话聊天，则不播放响铃
                if (!isInMessageActivity(groupNo)) {
                    AppUtils.playMessageRingtone();
                }
                //如果数据库不存在数据组装一个对象 插入到数据库通知界面
                //-------------------------公共数据开始--------------------------------//
                final ConversationMsg dbConversationMsg = new ConversationMsg();
                dbConversationMsg.setMsgSrcNo(pcSrc);
                dbConversationMsg.setGroupNo(groupNo);
                dbConversationMsg.setMsgDstNo(pcDst);
                dbConversationMsg.setMsgUctId(chSmsId);
                dbConversationMsg.setMsgType(InfoType);
                dbConversationMsg.setMsgTxtSplit(InfoSeq);
                dbConversationMsg.setRecvCfm(RecvCfm);
                //表示这是对端发过来的数据
                dbConversationMsg.setMsgDirection(IMVT_COM_MSG);
                //短信内容的长度
                dbConversationMsg.setContentLength(ContentLength);
                //短信设置为未读
                dbConversationMsg.setReadStatus(UNREAD_MSG);
                //纯文本短信的内容
                dbConversationMsg.setContent(chMsgContent);
                //-------------------------公共数据结束--------------------------------//
                //以下不同的短信类型解析也不同
                switch (InfoType) {
                    //文本短信
                    case INFO_TYPE_TEXT:
                    case INFO_TYPE_OLD_DEVICE_TEXT:
                        //计算下划线的个数 msgId协议:2017072110351900_523_536
                        int underlineCount = UctLibStringUtils.appearNumber(chSmsId, UctLibStringUtils.SPLIT_MARK_UNDERLINE);
                        //如果下划线个数不是三个表示msgId的协议错误 这里需要兼容以前的版本 以前的版本这里没有长度所以underlineCount>0或underlineCount<3都是合法的
                        if (underlineCount <= 0) {
                            throw new IllegalArgumentException("Invalid string .chSmsId={ " + chSmsId + " }");
                        }
                        //解析msgId
                        String[] msgIdInfo = UctLibStringUtils.spiltMsgId(chSmsId);
                        //获取短信发送时间
                        String msgTime = msgIdInfo[0];
                        msgTime = msgTime.substring(0, 14);
                        //从MSGID里获取到时间再转换为long
                        Date msgDate = UctLibUtils.strMsgToDate(msgTime);
                        //设置收到短信是的时间
                        dbConversationMsg.setMsgTime(msgDate.getTime());
                        //短信状态
                        dbConversationMsg.setMsgStatus(FILE_STATUS_RECEIVE_SUCCESS);
                        insertDb(dbConversationMsg);
                        break;
                    //组呼录音 按照语音短信的类型处理
                    case INFO_TYPE_PTT_AUDIO:
                        dbConversationMsg.setMsgType(INFO_TYPE_AUDIO);
                        //这里组装一个对象
                        assembleMsgObject(dbConversationMsg, true, chSmsId, chMsgContent);
                        final ConversationMsg conversation5 = insertDb(dbConversationMsg);
                        addBuffer.execute(new Runnable() {
                            @Override
                            public void run() {
                                //插入到数据库并下载附件
                                addReceiverQueue(conversation5, true);
                            }
                        });
                        break;

                    //语音短信 图片短信 视频短信文件比较小默认自动下载下来
                    //语音短信
                    case INFO_TYPE_AUDIO:
                        //这里组装一个对象
                        assembleMsgObject(dbConversationMsg, true, chSmsId, chMsgContent);
                        final ConversationMsg conversation1 = insertDb(dbConversationMsg);
                        addBuffer.execute(new Runnable() {
                            @Override
                            public void run() {
                                //插入到数据库并下载附件
                                addReceiverQueue(conversation1, true);
                            }
                        });
                        break;
                    //图片短信
                    case INFO_TYPE_IMAGE:
                        //这里组装一个对象
                        assembleMsgObject(dbConversationMsg, false, chSmsId, chMsgContent);
                        final ConversationMsg conversation2 = insertDb(dbConversationMsg);
                        addBuffer.execute(new Runnable() {
                            @Override
                            public void run() {
                                //插入到数据库并下载附件
                                addReceiverQueue(conversation2, true);
                            }
                        });
                        break;

                    //视频短信 文件短信 小视频短信不会自动下载
                    //注意了这里不会自动下载，文件比较大用户点击时才请求下载
                    case INFO_TYPE_FILE:
                        //视频短信
                    case INFO_TYPE_VIDEO:
                        //小视频短信
                    case INFO_TYPE_CAMERA_VIDEO:
                        assembleMsgObject(dbConversationMsg, false, chSmsId, chMsgContent);
                        final ConversationMsg conversation3 = insertDb(dbConversationMsg);
                        addBuffer.execute(new Runnable() {
                            @Override
                            public void run() {
                                //插入到数据库不下载附件
                                addReceiverQueue(conversation3, false);
                            }
                        });
                        break;
                    //位置短信
                    case INFO_TYPE_MY_LOCATION:
                        assembleLocationMsgObject(dbConversationMsg, chSmsId, chMsgContent);
                        final ConversationMsg conversation4 = insertDb(dbConversationMsg);
                        addBuffer.execute(new Runnable() {
                            @Override
                            public void run() {
                                //插入到数据库不下载附件
                                addReceiverQueue(conversation4, true);
                            }
                        });
                        break;
                    default:
                        if (UctClientApi.isDebugMode()) {
                            throw new RuntimeException("SMS type does not exist! UCT_P2MP_Eastone [pcSrc=" + pcSrc + ", pcDst=" + pcDst + ", chSmsId="
                                    + chSmsId + ", InfoType=" + InfoType + ", InfoSeq=" + InfoSeq
                                    + ", RecvCfm=" + RecvCfm + ", RecvNotify=" + RecvNotify
                                    + ", ContentLength=" + ContentLength + ", chMsgContent="
                                    + chMsgContent + "]");
                        }

                }
            }
            return 0;
        }

        /**
         * @description  判断来消息时是否在同一会话的微信页面内
         * @param        number 接到消息的号码，点对点消息是源号码，点对多是组号码
         * @return
         */
        private boolean isInMessageActivity(String number) {
            String actClsName = AppManager.getAppManager().currentActivity().getComponentName().getClassName();
            if (!StrUtils.isEmpty(actClsName) && actClsName.equals(MessageActivity.class.getName())) {
                IMessageView iMessageView = (IMessageView) AppManager.getAppManager().currentActivity();
                if (iMessageView != null && !TextUtils.isEmpty(number) && number.equals(iMessageView.getMsgDstNo())) {
                    PrintLog.i("is in MessageActivity number = " + number);
                    return true;
                }
            }
            PrintLog.i("is not in MessageActivity number = " + number);
            return false;
        }

        public String parseStrDate(String strDate) throws Exception {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
            Date date = sdf.parse(strDate);
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
            return sdf1.format(date);
        }

        /**
         * 组装短信对象
         * @param chSmsId 短信Id
         * @param chMsgContent 短信内容
         */
        private void assembleMsgObject(ConversationMsg dbConversationMsg, boolean isAudioMsg, String chSmsId, String chMsgContent) {
            //判断内容是否合法如果不包含:说明不合法
            if (!chMsgContent.contains(SPLIT_MARK)) {
                //这里兼容老的4.0版本 4.0版本语音短信不带“：”也不带源路径 这里自动生成一个
                PrintLog.w("Invalid string .chMsgContent={" + chMsgContent + "}");
                String prefix = "." + FileUtils.getFilePrefix(chMsgContent);
                chMsgContent = chMsgContent + ":" + chSmsId + prefix;
                //                throw new IllegalArgumentException("Invalid string .chMsgContent={"+chMsgContent+"}");
            }
            //计算下划线的个数 msgId协议:2017072110351900_523_536_1234564
            int underlineCount = UctLibStringUtils.appearNumber(chSmsId, UctLibStringUtils.SPLIT_MARK_UNDERLINE);
            //如果下划线个数不是三个表示msgId的协议错误 这里需要兼容以前的版本 以前的版本这里没有长度所以underlineCount>0或underlineCount<3都是合法的
            if (underlineCount < 0 || underlineCount > 3) {
                throw new IllegalArgumentException("Invalid string .chSmsId={ " + chSmsId + " }");
            }
            //解析msgId
            String[] msgIdInfo = UctLibStringUtils.spiltMsgId(chSmsId);
            //获取短信发送时间
            String msgTime = msgIdInfo[0];
            String fileName = msgTime;
            //从msgId里截取时间，时间包括年月日时分秒，共14位 例如：2017-07-21 10：35：19
            msgTime = msgTime.substring(0, 14);
            //获取短信目的号码
            String msgDstNo = msgIdInfo[1];
            //获取短信源号码
            String msgSrcNo = msgIdInfo[2];
            String fileSize = "0";
            if (msgIdInfo.length == 4) {
                //获取文件的大小
                fileSize = msgIdInfo[3];
            }
            PrintLog.i("parseChSmsId {chSmsId=" + chSmsId + ",msgTime=" + msgTime + ",msgDstNo=" + msgDstNo + ",msgSrcNo=" + msgSrcNo + ",fileSize=" + fileSize + "}");
            //            从消息内容里解析远程图片的路径
            String fileNameList[] = UctLibStringUtils.spiltMsgFileName(chMsgContent);
            if (fileNameList.length <= 0 || fileNameList.length > 2) {
                throw new IllegalArgumentException("Invalid string .chMsgContent={" + chMsgContent + "}");
            }
            String tempMsgTime = "";
            try {
                tempMsgTime = parseStrDate(msgTime);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid Date .msgTime={" + msgTime + "}");
            }
            //远程图片的路径
            String remotePath = fileNameList[0];
            //图片的源文件名称
            String srcFileName = fileNameList[1];
            String prefix = "." + FileUtils.getFilePrefix(remotePath);
            //有源文件名称时，保存的文件名：接收时间+源文件名称 如果源文件名称为空 直接用smsId
            if (!TextUtils.isEmpty(srcFileName)) {
                String tempSrcFileName = com.ptyt.uct.utils.FileUtils.subFileName(srcFileName);
                fileName = tempSrcFileName + "_" + fileName + prefix;
            } else {
                fileName = chSmsId + prefix;
            }
            //兼容以前的老版本
            String tempRemotePath = remotePath;
            if (!remotePath.contains("/") && !remotePath.contains("\\")) {
                remotePath = tempMsgTime + File.separator + msgDstNo + File.separator + tempRemotePath;
            }
            PrintLog.i("parseMsgContent {remotePath=" + remotePath + " ,srcFileName=" + srcFileName + ",fileName=" + fileName + "}");
            //设置文件的大小
            if (!StrUtils.isMatchs(fileSize)) {
                fileSize = "0";
                PrintLog.w("Invalid Integer .fileSize={" + fileSize + "}");
            }
            if (isAudioMsg) {
                dbConversationMsg.setAudioPlayStatus(MessageDBConstant.AUDIO_STOP_STATUS);
                dbConversationMsg.setAudioReadStatus(MessageDBConstant.AUDIO_UNREAD_MSG);
                dbConversationMsg.setAudioLength(Integer.parseInt(fileSize));
            } else {
                dbConversationMsg.setFileSize(Long.parseLong(fileSize));
            }
            //从MSGID里获取到时间再转换为long
            Date msgDate = UctLibUtils.strMsgToDate(msgTime);
            //设置收到短信是的时间
            dbConversationMsg.setMsgTime(msgDate.getTime());
            //设置图片的服务器路径
            dbConversationMsg.setRemoteImgPath(remotePath);
            //获取会话ID
            long conversationId = queryConversationId(dbConversationMsg);
            //组装本地图片的绝对路径
            String localMsgPath = SDCardUtils.getChatRecordPath(conversationId, dbConversationMsg.getMsgSrcNo() + "_" + dbConversationMsg.getMsgDstNo()) + fileName;
            dbConversationMsg.setLocalImgPath(localMsgPath);
            dbConversationMsg.setMsgStatus(FILE_STATUS_NOT_DOWNLOAD);
        }

        /**
         * 组装位置短信对象
         * @param chSmsId 短信Id
         * @param chMsgContent 短信内容
         */
        private void assembleLocationMsgObject(ConversationMsg dbConversationMsg, String chSmsId, String chMsgContent) {
            //计算下划线的个数 msgId协议:2017072110351900_523_536_1234564
            int underlineCount = UctLibStringUtils.appearNumber(chSmsId, UctLibStringUtils.SPLIT_MARK_UNDERLINE);
            //如果下划线个数不是三个表示msgId的协议错误 这里需要兼容以前的版本 以前的版本这里没有长度所以underlineCount>0或underlineCount<3都是合法的
            if (underlineCount < 0 || underlineCount > 3) {
                throw new IllegalArgumentException("Invalid string .chSmsId={ " + chSmsId + " }");
            }
            //解析msgId
            String[] msgIdInfo = UctLibStringUtils.spiltMsgId(chSmsId);
            //获取短信发送时间
            String msgTime = msgIdInfo[0];
            //截取的字符串
            msgTime = msgTime.substring(0, 14);
            //获取短信目的号码
            String msgDstNo = msgIdInfo[1];
            //获取短信源号码
            String msgSrcNo = msgIdInfo[2];
            String fileSize = "0";
            if (msgIdInfo.length == 4) {
                //获取文件的大小
                fileSize = msgIdInfo[3];
            }
            //设置文件的大小
            if (!StrUtils.isMatchs(fileSize)) {
                fileSize = "0";
                PrintLog.w("Invalid Integer .fileSize={" + fileSize + "}");
            }
            dbConversationMsg.setFileSize(Long.parseLong(fileSize));
            PrintLog.i("parseChSmsId {chSmsId=" + chSmsId + ",msgTime=" + msgTime + ",msgDstNo=" + msgDstNo + ",msgSrcNo=" + msgSrcNo + ",fileSize=" + fileSize + "}");
            MessageMyLocationEntity entity = JSON.parseObject(chMsgContent, MessageMyLocationEntity.class);
            //从MSGID里获取到时间再转换为long
            Date msgDate = UctLibUtils.strMsgToDate(msgTime);
            //设置收到短信是的时间
            dbConversationMsg.setMsgTime(msgDate.getTime());
            //设置图片的服务器路径
            dbConversationMsg.setRemoteImgPath(entity.getRemotePath());
            //获取会话ID
            long conversationId = queryConversationId(dbConversationMsg);
            String prefix = "." + FileUtils.getFilePrefix(entity.getRemotePath());
            //组装本地图片的绝对路径
            String localMsgPath = SDCardUtils.getChatRecordPath(conversationId, dbConversationMsg.getMsgSrcNo() + "_" + dbConversationMsg.getMsgDstNo()) + chSmsId + prefix;
            dbConversationMsg.setLocalImgPath(localMsgPath);
            dbConversationMsg.setMsgStatus(FILE_STATUS_NOT_DOWNLOAD);
        }

    };

    private IEastoneCfmListener iEastoneCfmListener = new IEastoneCfmListener() {

        @Override
        public int UCT_P2P_EastoneCfm(String pcSrc, String pcDst, String smsId, int infoSeq, int cfmType, int result) throws UctLibException {
            try {
                ConversationMsg conversationMsg = updateConversationBySmsId(smsId, MsgCallBackListener.MSG_EASTONECFM, infoSeq, cfmType, result);
                //通知界面
                notifyDataChangedListener(conversationMsg, MsgCallBackListener.MSG_EASTONECFM);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("UCT_P2P_EastoneCfm error:{" + e.getMessage() + "}");
            }
            return 0;
        }

        @Override
        public int UCT_P2MP_EastoneCfm(String pcSrc, String pcDst, String smsId, int infoSeq, int cfmType, int result) throws UctLibException {
            try {
                ConversationMsg conversationMsg = updateConversationBySmsId(smsId, MsgCallBackListener.MSG_EASTONECFM, infoSeq, cfmType, result);
                //通知界面
                notifyDataChangedListener(conversationMsg, MsgCallBackListener.MSG_EASTONECFM);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("UCT_P2P_EastoneCfm error:{" + e.getMessage() + "}");
            }
            return 0;
        }
    };

    /**
     * 短信确认回调函数
     *
     * @param smsId   msgId
     * @param status  短信状态
     * @param infoSeq 分割ID
     * @param cfmType 0-	服务器确认 1-	接收方自动确认 2-	接收方手动确认
     * @param result  结果
     * @return 返回短信的ID
     */
    private ConversationMsg updateConversationBySmsId(String smsId, int status, int infoSeq, int cfmType, int result) throws Exception {
        ConversationMsg dbConversationMsg = MessageDBManager.getInstance(mContext).queryConversationMsgBySmsId(smsId);
        if (dbConversationMsg == null) {
            throw new RuntimeException("dbConversationMsg can not empty.dbConversationMsg={ null }");
        }
        dbConversationMsg.setMsgTxtSplit(infoSeq);
        dbConversationMsg.setMsgStatus(status);
        dbConversationMsg.setCfmType(cfmType);
        dbConversationMsg.setResult(result);
        MessageDBManager.getInstance(mContext).updateMessage(dbConversationMsg);
        return dbConversationMsg;
    }


    /**
     * 短信改变通知
     *
     * @param mConversationMsg 当前改变后的对象
     * @param type             通知的类型
     */
    protected void notifyDataChangedListener(ConversationMsg mConversationMsg, int type) {
        int size = this.iMessageViewList.getList().size();
        PrintLog.i("msgChangeNotify [mConversationMsg=" + mConversationMsg.toString() + ", type=" + type + ",size= " + size + "]");
        for (int i = this.iMessageViewList.getList().size() - 1; i >= 0; --i) {
            MsgCallBackListener msgCallBackListener = this.iMessageViewList.getList().get(i);
            int result = msgCallBackListener.notifyDataChangedListener(mConversationMsg, type);
            if (result != 0) {
                PrintLog.e("result=[" + result + "]");
            }
        }
    }

    public interface MsgCallBackListener extends BaseServiceCallBack {
        /**
         * 短信发送并且插入到数据库
         */
        int MSG_SEND_INSERT_DB = 1;
        /**
         * 短信状态改变是通知
         */
        int MSG_STATUS_CHANGE = 2;
        /**
         * 短信确认回调
         */
        int MSG_EASTONECFM = 3;
        /**
         * 文件传输进度
         */
        int MSG_UPDATE_PROGRESS = 4;

        /**
         * 短信改变通知
         *
         * @param mConversationMsg 当前改变后的对象
         * @param type             通知的类型 更新状态还是插入数据
         */
        int notifyDataChangedListener(ConversationMsg mConversationMsg, int type);
    }
}
