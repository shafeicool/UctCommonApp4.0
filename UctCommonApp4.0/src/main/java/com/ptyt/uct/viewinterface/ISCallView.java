package com.ptyt.uct.viewinterface;

/**
 * @Description:
 * @Date: 2017/5/15
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public interface ISCallView {

    int uCT_SCallMoCfm(String pcCfmDn, String pMpIp, int CallRefID,int hUserCall, int isVideo);

    void uCT_SCallRelInd(int usCause, int hUserCall);
}
