package com.ptyt.uct.entity;

import java.io.Serializable;

/**
 * @Description:
 * @Date: 2017/10/30
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class OperationResult implements Serializable{

    public OperationResult(){
        errorCode = 0;
    }

    public String resultContent;
    public int errorCode;
    public String errorDesc;
}