package com.ptyt.uct.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.media.AudioManager;

import java.util.Set;

import static android.content.Context.AUDIO_SERVICE;

/**
 * @Description:
 * @Date: 2018/1/2
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public class SoundManager {

    private static AudioManager audioManager;

    public static void setSpeakerphoneOn(Context mContext, boolean on)
    {
        if(audioManager == null){
            audioManager = (AudioManager)mContext.getSystemService(AUDIO_SERVICE);
        }
        if(on) {
            if(!checkControlIsConnected(mContext)){//耳机未插入才能设置免提
                audioManager.setSpeakerphoneOn(true);
            }else{
                audioManager.setSpeakerphoneOn(false);//关闭扬声器
            }
        } else {
            audioManager.setSpeakerphoneOn(false);//关闭扬声器
        }
    }

    public static boolean checkControlIsConnected(Context mContext) {
        //检测耳机线是否连接
        AudioManager am = (AudioManager) mContext.getSystemService(AUDIO_SERVICE);
        //这个方法已经过时，api介绍仅仅用来检测是否连接，可以注册广播接收者接受耳机插拔的广播
        if (am.isWiredHeadsetOn()) {
            //耳机插入
            return true;
        }

        //检测蓝牙设备是否连接
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        //蓝牙适配器是否存在，即是否发生了错误
        if (ba == null || !ba.isEnabled()) {
            //蓝牙不可用
            return false;
        } else if (ba.isEnabled()) {
            Set<BluetoothDevice> bondedDevices = ba.getBondedDevices();
            if (bondedDevices == null || bondedDevices.size() <= 0) {
                //当前没有设备连接
                return false;
            } else {
                for (BluetoothDevice d : bondedDevices) {
                    if (d.getBondState() == BluetoothDevice.BOND_BONDED) {
                        //当前有设备接入并处于连接状态
                        return true;
                    }
                }
            }

        }
        return false;
    }
}
