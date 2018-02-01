package com.ptyt.uct.entity;

import com.android.uct.bean.BaseBean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 描述：消息会话的详细信息
 * <p>
 * 创建时间 2017/5/11.
 * <p>
 * 创建者 崔晓锋
 * <p>
 * 版本：V1.0
 */
@Entity(nameInDb = "TB_CONVERSATION_MSG")
public class ConversationMsg extends BaseBean implements Cloneable {

    //id标识
    @Id(autoincrement = true)
    @Property(nameInDb = "id")
    private Long ID;

    //会话id
    @NotNull
    @Property(nameInDb = "msg_conversation_id")
    private Long msgConversationId;

    //消息时间
    @Property(nameInDb = "msg_time")
    private Long msgTime;
    //消息源号码

    @NotNull
    @Property(nameInDb = "msg_src_no")
    private String msgSrcNo;
    //消息目的号码

    @NotNull
    @Property(nameInDb = "msg_dst_no")
    private String msgDstNo;

    //组号码
    @Property(nameInDb = "group_no")
    private String groupNo;

    //信息id
    @Property(nameInDb = "msg_uct_id")
    private String msgUctId;

    //消息类型
    @Property(nameInDb = "msg_type")
    private Integer msgType;

    //文本短信分割
    @Property(nameInDb = "msg_txt_split")
    private Integer msgTxtSplit;

    //接收用户的确认要求
    @Property(nameInDb = "recv_cfm")
    private Integer recvCfm;

    //接收特殊短信通知
    @Property(nameInDb = "recv_notify")
    private Integer recvNotify;

    //远程存储字段
    @Property(nameInDb = "remote_msg_content")
    private String remoteMsgContent;

    //文本内容
    @Property(nameInDb = "msg_content")
    private String content;

    //内容长度
    @Property(nameInDb = "content_length")
    private Integer contentLength;

    //消息方向
    @Property(nameInDb = "msg_direction")
    private Integer msgDirection;

    //本地图片路径
    @Property(nameInDb = "local_img_path")
    private String localImgPath;

    //远程图片路径
    @Property(nameInDb = "remote_img_path")
    private String remoteImgPath;

    //消息状态
    @Property(nameInDb = "msg_status")
    private Integer msgStatus;

    //确认类型
    @Property(nameInDb = "cfm_type")
    private Integer cfmType;

    //结果
    @Property(nameInDb = "result")
    private Integer result;

    //消息已读状态
    @Property(nameInDb = "read_status")
    private Integer readStatus;

    //音频长度
    @Property(nameInDb = "audio_length")
    private Integer audioLength;

    //音频播放状态
    @Property(nameInDb = "audio_play_status")
    private Integer audioPlayStatus;

    //音频已读状态
    @Property(nameInDb = "audio_read_status")
    private Integer audioReadStatus;

    //文件大小
    @Property(nameInDb = "file_size")
    private Long fileSize;

    //文件传输偏移量
    @Property(nameInDb = "off_size")
    private Long offSize;

    //图片压缩，只针对图片
    @Keep @Transient
    private Integer msgThumbnail = 0;

    @Generated(hash = 449846917)
    public ConversationMsg(Long ID, @NotNull Long msgConversationId, Long msgTime,
            @NotNull String msgSrcNo, @NotNull String msgDstNo, String groupNo,
            String msgUctId, Integer msgType, Integer msgTxtSplit, Integer recvCfm,
            Integer recvNotify, String remoteMsgContent, String content,
            Integer contentLength, Integer msgDirection, String localImgPath,
            String remoteImgPath, Integer msgStatus, Integer cfmType,
            Integer result, Integer readStatus, Integer audioLength,
            Integer audioPlayStatus, Integer audioReadStatus, Long fileSize,
            Long offSize) {
        this.ID = ID;
        this.msgConversationId = msgConversationId;
        this.msgTime = msgTime;
        this.msgSrcNo = msgSrcNo;
        this.msgDstNo = msgDstNo;
        this.groupNo = groupNo;
        this.msgUctId = msgUctId;
        this.msgType = msgType;
        this.msgTxtSplit = msgTxtSplit;
        this.recvCfm = recvCfm;
        this.recvNotify = recvNotify;
        this.remoteMsgContent = remoteMsgContent;
        this.content = content;
        this.contentLength = contentLength;
        this.msgDirection = msgDirection;
        this.localImgPath = localImgPath;
        this.remoteImgPath = remoteImgPath;
        this.msgStatus = msgStatus;
        this.cfmType = cfmType;
        this.result = result;
        this.readStatus = readStatus;
        this.audioLength = audioLength;
        this.audioPlayStatus = audioPlayStatus;
        this.audioReadStatus = audioReadStatus;
        this.fileSize = fileSize;
        this.offSize = offSize;
    }

    @Generated(hash = 1732484312)
    public ConversationMsg() {
    }

    @Override
    @Keep
    public ConversationMsg clone() throws CloneNotSupportedException {
        return (ConversationMsg) super.clone();
    }

    @Override
    @Keep
    /**
     * 这个对象方法打印是需要
     */
    public String toString() {
        return "ConversationMsg{" +
                "ID=" + ID +
                ", msgConversationId=" + msgConversationId +
                ", msgTime=" + msgTime +
                ", msgSrcNo='" + msgSrcNo + '\'' +
                ", msgDstNo='" + msgDstNo + '\'' +
                ", groupNo='" + groupNo + '\'' +
                ", msgUctId='" + msgUctId + '\'' +
                ", msgType=" + msgType +
                ", msgTxtSplit=" + msgTxtSplit +
                ", recvCfm=" + recvCfm +
                ", recvNotify=" + recvNotify +
                ", remoteMsgContent='" + remoteMsgContent + '\'' +
                ", content='" + content + '\'' +
                ", contentLength=" + contentLength +
                ", msgDirection=" + msgDirection +
                ", localImgPath='" + localImgPath + '\'' +
                ", remoteImgPath='" + remoteImgPath + '\'' +
                ", msgStatus=" + msgStatus +
                ", cfmType=" + cfmType +
                ", result=" + result +
                ", readStatus=" + readStatus +
                ", audioLength=" + audioLength +
                ", audioPlayStatus=" + audioPlayStatus +
                ", audioReadStatus=" + audioReadStatus +
                ", fileSize=" + fileSize +
                ", offSize=" + offSize +
                ", msgThumbnail=" + msgThumbnail +
                '}';
    }

    @Keep
    public Integer getMsgThumbnail() {
        return msgThumbnail;
    }

    @Keep
    public void setMsgThumbnail(Integer msgThumbnail) {
        this.msgThumbnail = msgThumbnail;
    }

    public Long getOffSize() {
        return this.offSize;
    }

    public void setOffSize(Long offSize) {
        this.offSize = offSize;
    }

    public Long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getAudioReadStatus() {
        return this.audioReadStatus;
    }

    public void setAudioReadStatus(Integer audioReadStatus) {
        this.audioReadStatus = audioReadStatus;
    }

    public Integer getAudioPlayStatus() {
        return this.audioPlayStatus;
    }

    public void setAudioPlayStatus(Integer audioPlayStatus) {
        this.audioPlayStatus = audioPlayStatus;
    }

    public Integer getAudioLength() {
        return this.audioLength;
    }

    public void setAudioLength(Integer audioLength) {
        this.audioLength = audioLength;
    }

    public Integer getReadStatus() {
        return this.readStatus;
    }

    public void setReadStatus(Integer readStatus) {
        this.readStatus = readStatus;
    }

    public Integer getResult() {
        return this.result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public Integer getCfmType() {
        return this.cfmType;
    }

    public void setCfmType(Integer cfmType) {
        this.cfmType = cfmType;
    }

    public Integer getMsgStatus() {
        return this.msgStatus;
    }

    public void setMsgStatus(Integer msgStatus) {
        this.msgStatus = msgStatus;
    }

    public String getRemoteImgPath() {
        return this.remoteImgPath;
    }

    public void setRemoteImgPath(String remoteImgPath) {
        this.remoteImgPath = remoteImgPath;
    }

    public String getLocalImgPath() {
        return this.localImgPath;
    }

    public void setLocalImgPath(String localImgPath) {
        this.localImgPath = localImgPath;
    }

    public Integer getMsgDirection() {
        return this.msgDirection;
    }

    public void setMsgDirection(Integer msgDirection) {
        this.msgDirection = msgDirection;
    }

    public Integer getContentLength() {
        return this.contentLength;
    }

    public void setContentLength(Integer contentLength) {
        this.contentLength = contentLength;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRemoteMsgContent() {
        return this.remoteMsgContent;
    }

    public void setRemoteMsgContent(String remoteMsgContent) {
        this.remoteMsgContent = remoteMsgContent;
    }

    public Integer getRecvNotify() {
        return this.recvNotify;
    }

    public void setRecvNotify(Integer recvNotify) {
        this.recvNotify = recvNotify;
    }

    public Integer getRecvCfm() {
        return this.recvCfm;
    }

    public void setRecvCfm(Integer recvCfm) {
        this.recvCfm = recvCfm;
    }

    public Integer getMsgTxtSplit() {
        return this.msgTxtSplit;
    }

    public void setMsgTxtSplit(Integer msgTxtSplit) {
        this.msgTxtSplit = msgTxtSplit;
    }

    public Integer getMsgType() {
        return this.msgType;
    }

    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }

    public String getMsgUctId() {
        return this.msgUctId;
    }

    public void setMsgUctId(String msgUctId) {
        this.msgUctId = msgUctId;
    }

    public String getGroupNo() {
        return this.groupNo;
    }

    public void setGroupNo(String groupNo) {
        this.groupNo = groupNo;
    }

    public String getMsgDstNo() {
        return this.msgDstNo;
    }

    public void setMsgDstNo(String msgDstNo) {
        this.msgDstNo = msgDstNo;
    }

    public String getMsgSrcNo() {
        return this.msgSrcNo;
    }

    public void setMsgSrcNo(String msgSrcNo) {
        this.msgSrcNo = msgSrcNo;
    }

    public Long getMsgTime() {
        return this.msgTime;
    }

    public void setMsgTime(Long msgTime) {
        this.msgTime = msgTime;
    }

    public Long getMsgConversationId() {
        return this.msgConversationId;
    }

    public void setMsgConversationId(Long msgConversationId) {
        this.msgConversationId = msgConversationId;
    }

    public Long getID() {
        return this.ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

}
