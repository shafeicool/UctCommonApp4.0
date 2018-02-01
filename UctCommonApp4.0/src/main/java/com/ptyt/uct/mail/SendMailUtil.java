package com.ptyt.uct.mail;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by Administrator on 2017/4/10.
 */

public class SendMailUtil {

    private static final String HOST = "smtp.exmail.qq.com";
    private static final String PORT = "25";
    private static final String FROM_ADD = "bug_system@ptyt.com.cn";
    private static final String TO_ADD = "bug_system@ptyt.com.cn";
    private static final String FROM_PSW = "Abc@123";
    private static final String SUBJECT = "普天宜通通用客户端4.0问题反馈";

    public static void send(final File file, String content) {
        final MailInfo mailInfo = creatMail(content);
        final MailSender sms = new MailSender();
        new Thread(new Runnable() {
            @Override
            public void run() {
                sms.sendFileMail(mailInfo, file);
            }
        }).start();
    }


    public static void send(String content) {
        final MailInfo mailInfo = creatMail(content);
        final MailSender sms = new MailSender();
        new Thread(new Runnable() {
            @Override
            public void run() {
                sms.sendTextMail(mailInfo);
            }
        }).start();
    }

    @NonNull
    private static MailInfo creatMail(String content) {
        final MailInfo mailInfo = new MailInfo();
        mailInfo.setMailServerHost(HOST);
        mailInfo.setMailServerPort(PORT);
        mailInfo.setValidate(true);
        mailInfo.setUserName(FROM_ADD); // 你的邮箱地址
        mailInfo.setPassword(FROM_PSW);// 您的邮箱密码
        mailInfo.setFromAddress(FROM_ADD); // 发送的邮箱
        mailInfo.setToAddress(TO_ADD); // 发到哪个邮件去
        mailInfo.setSubject(SUBJECT); // 邮件主题
        mailInfo.setContent(content); // 邮件文本
        return mailInfo;
    }

}
