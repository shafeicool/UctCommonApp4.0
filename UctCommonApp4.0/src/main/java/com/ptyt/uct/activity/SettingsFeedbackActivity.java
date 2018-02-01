package com.ptyt.uct.activity;

import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ptyt.uct.R;
import com.ptyt.uct.mail.SendMailUtil;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SettingsFeedbackActivity extends BaseActionBarActivity implements View.OnClickListener {

    private EditText et_text;
    private TextView tv_submit;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_settings_feedback;
    }

    @Override
    protected void initWidget() {
        et_text = (EditText) findViewById(R.id.et_text);
        tv_submit = (TextView) findViewById(R.id.tv_submit);
        tv_actionBarTitle.setText(getResources().getString(R.string.string_settings_about_feedback));
        tv_submit.setOnClickListener(this);
    }

    public void senTextMail() {
        SendMailUtil.send(et_text.getText().toString());
    }

    public void sendFileMail() {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "test.txt");
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            String str = "hello world";
            byte[] data = str.getBytes();
            os.write(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null)
                    os.close();
            } catch (IOException e) {
            }
        }
        SendMailUtil.send(file, et_text.getText().toString());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_submit:
                String content = et_text.getText().toString();
                if (StrUtils.isEmpty(content)) {
                    ToastUtils.getToast().showMessageShort(this, getString(R.string.string_settings_about_feedback_info), -1);
                    return;
                }
                senTextMail();
                finish();
                break;
        }
    }
}
