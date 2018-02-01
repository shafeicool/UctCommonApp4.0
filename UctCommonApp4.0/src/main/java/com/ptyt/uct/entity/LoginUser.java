package com.ptyt.uct.entity;

import com.android.uct.bean.BaseBean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Title: com.ptyt.uct.entity
 * Description:
 * Date: 2017/12/20
 * Author: ShaFei
 * Version: V1.0
 */

@Entity(nameInDb = "TB_LOGIN_USER")
public class LoginUser extends BaseBean {
    // id标识
    @Id(autoincrement = true)
    @Property(nameInDb = "id")
    private Long ID;
    @NotNull
    @Property(nameInDb = "login_number")
    private String loginNumber;
    @NotNull
    @Property(nameInDb = "login_password")
    private String loginPassword;
    @NotNull
    @Property(nameInDb = "login_ip")
    private String loginIp;
    public String getLoginIp() {
        return this.loginIp;
    }
    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }
    public String getLoginPassword() {
        return this.loginPassword;
    }
    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }
    public String getLoginNumber() {
        return this.loginNumber;
    }
    public void setLoginNumber(String loginNumber) {
        this.loginNumber = loginNumber;
    }
    public Long getID() {
        return this.ID;
    }
    public void setID(Long ID) {
        this.ID = ID;
    }
    @Generated(hash = 71959341)
    public LoginUser(Long ID, @NotNull String loginNumber,
            @NotNull String loginPassword, @NotNull String loginIp) {
        this.ID = ID;
        this.loginNumber = loginNumber;
        this.loginPassword = loginPassword;
        this.loginIp = loginIp;
    }
    @Generated(hash = 1159929338)
    public LoginUser() {
    }
}
