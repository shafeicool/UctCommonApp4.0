package com.ptyt.uct.viewinterface;

import com.ptyt.uct.entity.Conversation;

import java.util.List;

/**
 * Title: com.ptyt.uct.viewinterface
 * Description:
 * Date: 2017/6/9
 * Author: ShaFei
 * Version: V1.0
 */

public interface IConversationView {
    // 刷新会话界面UI
    void updateConversationAdapter(List<Conversation> conversationList);
}
