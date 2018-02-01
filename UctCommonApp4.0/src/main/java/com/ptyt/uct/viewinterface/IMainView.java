package com.ptyt.uct.viewinterface;

import android.support.v4.app.Fragment;

/**
 * Title: com.ptyt.uct.viewinterface
 * Description:
 * Date: 2017/6/12
 * Author: ShaFei
 * Version: V1.0
 */

public interface IMainView {
    Fragment getFragment(int position);
    void setPtytLoginStatus(String prompt);
}
