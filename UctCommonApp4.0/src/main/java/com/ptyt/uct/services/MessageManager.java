package com.ptyt.uct.services;

import android.text.TextUtils;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.callback.MessageCallBack;
import com.ptyt.uct.entity.ConversationMsg;
import com.ptyt.uct.common.MessageDBConstant;

/**
 * Title: com.ptyt.uct.common
 * Description:短信相关的管理类 这里
 * Date: 2017/6/21
 * Author: ShaFei
 * Version: V1.0
 */

public class MessageManager {

    private static MessageManager mInstance;

    private MessageManager(){
    }

    public static synchronized MessageManager getInstane() {
        if (mInstance == null) {
            mInstance = new MessageManager();
        }
        return mInstance;
    }

    /**
     * 下载视频短信 文件短信
     * @param conversationMsg 组装好的短信
     * @return 0表示调用成功 1表示调用失败
     */
    public int downloadMessage(ConversationMsg conversationMsg){
        //校验是否合法
        boolean isOk = checkDownloadMsg(conversationMsg);
        if(!isOk){
            return -1;
        }
        ServiceBinder serviceBinder = MessageCallBack.getMessageCallBack().getServiceBinder();
        if(serviceBinder!=null){
            serviceBinder.downloadMsg(conversationMsg);
        }else {
            return -1;
        }
        return 0;
    }

    private boolean checkDownloadMsg(ConversationMsg conversationMsg){
        boolean isOk = checkMsg(conversationMsg);
        if(!isOk){
            return false;
        }
        int status = conversationMsg.getMsgStatus();
        if(MessageDBConstant.FILE_STATUS_NOT_DOWNLOAD == status || MessageDBConstant.MSG_STATUS_FAIL == status){
            return true;
        }else{
            return false;
        }
    }

    public int deleteMsg(ConversationMsg conversationMsg){
        PrintLog.i("deleteMsg {conversationMsg="+conversationMsg.toString()+"}");
        //校验是否合法
        boolean isOk = checkMsg(conversationMsg);
        if(!isOk){
            return -1;
        }
        ServiceBinder serviceBinder = MessageCallBack.getMessageCallBack().getServiceBinder();
        if(serviceBinder!=null){
            serviceBinder.deleteMsg(conversationMsg);
        }else {
            return -1;
        }
        return 0;
    }

    public int cancelMsg(ConversationMsg conversationMsg){
        //校验是否合法
        boolean isOk = checkMsg(conversationMsg);
        if(!isOk){
            return -1;
        }
        ServiceBinder serviceBinder = MessageCallBack.getMessageCallBack().getServiceBinder();
        if(serviceBinder!=null){
            serviceBinder.cancelMsg(conversationMsg);
        }else {
            return -1;
        }
        return 0;
    }

    /**
     * 发送短信接口
     * @param conversationMsg 组装好的短信
     * @return 0表示调用成功 1表示调用失败
     */
    public int sendMessage(ConversationMsg conversationMsg) {
        boolean isOk = checkMsg(conversationMsg);
        if(!isOk){
            return -1;
        }
        ServiceBinder serviceBinder = MessageCallBack.getMessageCallBack().getServiceBinder();
        if(serviceBinder!=null){
            serviceBinder.sendMsg(conversationMsg);
        }else {
            return -1;
        }
        return 0;
    }

    /**
     * 发送短信这些是必填项 校验短信发送的包是否正确
     * @param conversationMsg 短信数据包
     * @return true表示合法 false表示不合法
     */
    private boolean checkMsg(ConversationMsg conversationMsg){
        String dstNo = conversationMsg.getMsgDstNo();
        if(TextUtils.isEmpty(dstNo)){
            PrintLog.e("目的号码不能为空");
            return false;
        }
        String srcNo = conversationMsg.getMsgSrcNo();
        if((!TextUtils.isEmpty(dstNo)&&!TextUtils.isEmpty(srcNo)) && dstNo.equals(srcNo)){
            PrintLog.e("目的号码不能和源号码相同");
            return false;
        }
        if(TextUtils.isEmpty(conversationMsg.getMsgUctId())){
            PrintLog.e("MsgId不能为空");
            return false;
        }
        if(conversationMsg.getMsgType() < 0){
            PrintLog.e("短信类型错误");
            return false;
        }
         int recvCfm = conversationMsg.getRecvCfm();
        if( recvCfm < 0 || recvCfm> 2){
            PrintLog.e("短信通知码错误");
            return false;
        }
        String content = conversationMsg.getContent();
        if(TextUtils.isEmpty(content)){
            PrintLog.e("短信内容不能为空");
            return false;
        }
        int contentLength = conversationMsg.getContentLength();
        if(contentLength <= 0){
            PrintLog.e("短信长度不能<0");
            return false;
        }
        return true;
    }

    public int reSendMsg(ConversationMsg conversationMsg) {
        boolean isOk = checkMsg(conversationMsg);
        if(!isOk){
            return -1;
        }
        ServiceBinder serviceBinder = MessageCallBack.getMessageCallBack().getServiceBinder();
        if(serviceBinder!=null){
            serviceBinder.reSendMsg(conversationMsg);
        }else {
            return -1;
        }
        return 0;
    }

    public void registerObserver(BaseServiceCallBack observer, int index) {
        ServiceBinder serviceBinder = MessageCallBack.getMessageCallBack().getServiceBinder();
        if(serviceBinder!=null){
            serviceBinder.registerObserver(observer,index);
        }

    }

    public void unRegisterObserver(BaseServiceCallBack observer, int index) {
        ServiceBinder serviceBinder = MessageCallBack.getMessageCallBack().getServiceBinder();
        if(serviceBinder!=null){
            serviceBinder.unRegisterObserver(observer,index);
        }
    }

}
