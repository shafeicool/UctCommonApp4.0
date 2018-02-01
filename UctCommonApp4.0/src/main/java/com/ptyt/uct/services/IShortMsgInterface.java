package com.ptyt.uct.services;

import com.ptyt.uct.entity.ConversationMsg;

/**
 * 短信相关接口
 */
interface IShortMsgInterface {
    /**
     * 发送短信
     * @param conversationMsg 短消息对象
     * @return 0表示调用成功 -1表示调用失败
     */
    int sendMsg(ConversationMsg conversationMsg);
    int sendTextMsg(ConversationMsg conversationMsg);
    /**
     * 短信重发
     * @param conversationMsg 短信息对象
     * @return 0表示调用成功 -1表示调用失败
     */
    int reSendMsg(ConversationMsg conversationMsg);

    /**
     * 下载短信
     * @param conversationMsg 短信息对象
     * @return 0表示调用成功 -1表示调用失败
     */
    int downloadMsg(ConversationMsg conversationMsg);
    /**
     * 取消短息发送，主要是发送图片 发送文件过程中取消的发送，会删除数据库数据和SD卡数据
     * @param conversationMsg 短信息对象
     * @return 0表示调用成功 -1表示调用失败
     */
    int cancelMsg(ConversationMsg conversationMsg);

    /**
     * 删除短信
     * @param conversationMsg 短信息对象
     * @return 0表示调用成功 -1表示调用失败
     */
    int deleteMsg(ConversationMsg conversationMsg);
}
