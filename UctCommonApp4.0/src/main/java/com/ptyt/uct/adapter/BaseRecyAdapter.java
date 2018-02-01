package com.ptyt.uct.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Date: 2017/5/11
 * @Author: KeChuanqi
 * @Version:V1.0
 */

public abstract class BaseRecyAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected List<T> datas;
    protected Context context;
    protected LayoutInflater inflater;
    protected RecyclerView.LayoutParams layoutParams;

    public BaseRecyAdapter(Context context) {
        this.context = context;
        datas = new ArrayList<>();
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    /**
     * 获得数据源
     *
     * @return
     */
    public List<T> getDatas() {
        return datas;
    }

    /**
     * 获得某个item数据
     *
     * @param position
     * @return
     */
    public T getItem(int position) {
        if (datas.size() > position) {
            return datas.get(position);
        }
        return null;
    }

    /**
     * 添加一个集合
     *
     * @param list
     */
    public void addAll(List<T> list) {
        datas.clear();
        datas.addAll(list);
        this.notifyDataSetChanged();
    }

    /**
     * 从某一个位置追加一个集合
     *
     * @param list
     */
    public void addMore(int positionStart, List<T> list) {
        datas.addAll(positionStart, list);
        this.notifyItemRangeInserted(positionStart, list.size());
    }

    public void updateItem(int positionStart, T bean) {
        datas.set(positionStart, bean);
//        this.notifyItemRangeChanged(positionStart, 1);
        notifyItemChanged(positionStart);
    }

    /**
     * 移除一条数据
     *
     * @param position
     */
    public void removeItem(int position) {
        datas.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * 移除一条数据
     *
     * @param position
     */
    public void removeItem2(int position) {
        datas.remove(position);
        notifyDataSetChanged();
    }

    /**
     * 移除一条数据
     *
     * @param bean
     */
    public void removeItem(T bean) {
        int pos = datas.indexOf(bean);
        removeItem(pos);
    }

    /**
     * 返回一条数据的位置
     *
     * @param bean
     */
    public int getItemPosition(T bean) {
        return datas.indexOf(bean);
    }

    /**
     * 移除所有数据
     */
    public void removeAll() {
        datas.clear();
        notifyDataSetChanged();
    }

    /**
     * 添加一条数据
     *
     * @param bean
     */
    public void addItem(T bean) {
        datas.add(bean);
        this.notifyDataSetChanged();
    }

    /**
     * 添加更多
     *
     * @param list
     */
    public void addMoreData(List<T> list) {
        this.datas.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * 清空数据源
     */
    public void clear() {
        datas.clear();
        notifyDataSetChanged();
    }

    /**
     * 设置adapter的item点击事件
     *
     * @param itemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    /**
     * 设置adapter的item长按事件
     *
     * @param itemLongClickListener
     */
    public void setOnItemLongClickListener(OnItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    /**
     * ViewHolder基类，已经设置item点击事件
     */
    public class BaseViewHolder extends RecyclerView.ViewHolder {

        public BaseViewHolder(final View itemView) {
            super(itemView);
            //layoutParams如果实例化不写在这如构造方法中，子类有时可能报错 why? ：called detach on an already detached child ViewHolder
            layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(layoutParams);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(getLayoutPosition(), itemView);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (itemLongClickListener != null) {
                        itemLongClickListener.onItemLongClick(getLayoutPosition(), itemView);
                    }
                    return true;
                }
            });
        }
    }

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        /**
         * @param pos      点击位置
         * @param itemView
         */
        void onItemClick(int pos, View itemView);
    }

    private OnItemLongClickListener itemLongClickListener;

    public interface OnItemLongClickListener {
        /**
         * @param pos      点击位置
         * @param itemView
         */
        void onItemLongClick(int pos, View itemView);
    }
}
