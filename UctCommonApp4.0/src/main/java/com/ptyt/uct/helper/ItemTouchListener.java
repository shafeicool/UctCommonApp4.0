package com.ptyt.uct.helper;

/**
 * @Description:
 * @Date: 2017/5/13
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public interface ItemTouchListener {
    void onItemMove(int adapterPosition, int adapterPosition1);

    void onItemDismiss(int adapterPosition);
}
