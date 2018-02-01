package com.ptyt.uct.viewinterface;

import com.ptyt.uct.entity.ConversationMsg;

public interface IMessageView extends IBaseView {
    // 语音录制完成
    void onRecordFinished(long seconds, String filePath, String msgId);

    // 刷新聊天界面UI
    void insertMessageAdapter(ConversationMsg conversationMsg);

    void updateMessageAdapter(ConversationMsg conversationMsg);

    void removeMessageAdapter(int position);

    // 获取与之聊天的用户号码
    String getMsgDstNo();
}
