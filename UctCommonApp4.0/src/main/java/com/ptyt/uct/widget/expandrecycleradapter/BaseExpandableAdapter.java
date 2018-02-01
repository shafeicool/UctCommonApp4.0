package com.ptyt.uct.widget.expandrecycleradapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * this adapter is implementation of RecyclerView.Adapter
 * creater: zaihuishou
 * create time: 7/13/16.
 * author email:tanzhiqiang.cathy@gmail.com
 */
public abstract class BaseExpandableAdapter extends RecyclerView.Adapter implements AbstractExpandableAdapterItem.ParentListItemExpandCollapseListener {

    protected List<Object> mDataList;

    private Object mItemType;

    private AdapterItemUtil mUtil = new AdapterItemUtil();

    private List<RecyclerView> mRecyclerViewList;

    protected BaseExpandableAdapter(List data) {
        if (data == null) return;
        this.mDataList = data;
        mRecyclerViewList = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public void onParentListItemCollapsed(int position) {
        Object o = mDataList.get(position);
        if (o instanceof ExpandableListItem) {
            collapseParentListItem((ExpandableListItem) o, position, true);
        }
    }

    /**
     * expand parent item
     *
     * @param position The index of the item in the list being expanded
     */
    @Override
    public void onParentListItemExpanded(int position) {
        try {
            Object o = mDataList.get(position);
            if (o instanceof ExpandableListItem) {
                expandParentListItem((ExpandableListItem) o, position, true, false);
            }
        } catch (IndexOutOfBoundsException e) {
        }
    }

    /**
     * @param expandableListItem {@link ExpandableListItem}
     * @param parentIndex        item index
     */
    private void collapseParentListItem(ExpandableListItem expandableListItem, int parentIndex, boolean collapseTriggeredByListItemClick) {
        if (expandableListItem.isExpanded()) {
            List<?> childItemList = expandableListItem.getChildItemList();
            if (childItemList != null && !childItemList.isEmpty()) {
                notifyItemExpandedOrCollapsed(parentIndex, false);
                int childListItemCount = childItemList.size();
                for (int i = childListItemCount - 1; i >= 0; i--) {
                    int index = parentIndex + i + 1;
                    Object o = mDataList.get(index);
                    if (o instanceof ExpandableListItem) {
                        ExpandableListItem parentListItem;
                        try {
                            parentListItem = (ExpandableListItem) o;
                            collapseParentListItem(parentListItem, index, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    mDataList.remove(index);
                }
                notifyItemRangeRemoved(parentIndex + 1, childListItemCount);
                expandableListItem.setExpanded(false);
                notifyItemRangeChanged(parentIndex + 1, mDataList.size() - parentIndex - 1);
            }
        }
    }

    /**
     * notify item state changed
     */
    private void notifyItemExpandedOrCollapsed(int parentIndex, boolean isExpand) {
        if (mRecyclerViewList != null && !mRecyclerViewList.isEmpty()) {
            RecyclerView recyclerView = mRecyclerViewList.get(0);
            BaseAdapterViewHolder viewHolderForAdapterPosition = (BaseAdapterViewHolder) recyclerView.findViewHolderForAdapterPosition(parentIndex);
            try {
                AbstractAdapterItem<Object> item = viewHolderForAdapterPosition.getItem();
                if (item != null && item instanceof AbstractExpandableAdapterItem) {
                    AbstractExpandableAdapterItem abstractExpandableAdapterItem = (AbstractExpandableAdapterItem) item;
                    abstractExpandableAdapterItem.onExpansionToggled(isExpand);
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * expand index item (Fixed by Jason. now it just adjust triple level, no more level!!!!!!!)
     *
     * @param parentIndex The index of the parent to collapse
     */
    protected void expandParentListItem(ExpandableListItem expandableListItem, int parentIndex, boolean expansionTriggeredByListItemClick, boolean isExpandAllChildren) {
        if (!expandableListItem.isExpanded()) {
            List<?> childItemList = expandableListItem.getChildItemList();
            if (childItemList != null && !childItemList.isEmpty()) {
                expandableListItem.setExpanded(true);
                int childListItemCount = childItemList.size();
                for (int i = 0; i < childListItemCount; i++) {
                    Object o = childItemList.get(i);
                    int newIndex = parentIndex + i + 1;
                    if (isExpandAllChildren && i > 0) {
                        for (int j = 0; j < i; j++) {
                            Object childBefore = childItemList.get(j);
                            if (childBefore instanceof ExpandableListItem) {
                                newIndex += ((ExpandableListItem) childBefore).getChildItemList().size();
                            }
                        }
                    }
                    mDataList.add(newIndex, o);
                    notifyItemInserted(newIndex);
                    if (isExpandAllChildren)
                        if (o instanceof ExpandableListItem) {
                            expandParentListItem((ExpandableListItem) o, newIndex, expansionTriggeredByListItemClick, isExpandAllChildren);
                        }
                }
                int positionStart = parentIndex + childListItemCount;
                if (parentIndex != mDataList.size() - 1)
                    notifyItemRangeChanged(positionStart, mDataList.size() - positionStart);
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    /**
     * add an item
     *
     * @param position intem index
     * @param o        item
     */
    public void addItem(int position, Object o) {
        if (isDataListNotEmpty() && position >= 0) {
            mDataList.add(position, o);
            notifyItemInserted(position);
        }
    }
    private boolean isDataListNotEmpty() {
        return mDataList != null && !mDataList.isEmpty();
    }
    /**
     * instead by{@link #getItemViewType(Object)}
     *
     * @param position item index
     * @return item view type
     */
    @Deprecated
    @Override
    public int getItemViewType(int position) {
        mItemType = getItemViewType(mDataList.get(position));
        return mUtil.getIntType(mItemType);
    }

    public Object getItemViewType(Object t) {
        return -1;
    }

    @NonNull
    public abstract AbstractAdapterItem<Object> getItemView(Object type);

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BaseAdapterViewHolder(parent.getContext(), parent, getItemView(mItemType));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BaseAdapterViewHolder rcvHolder = (BaseAdapterViewHolder) holder;
        Object object = mDataList.get(position);
        if (object instanceof ExpandableListItem) {
            AbstractExpandableAdapterItem abstractParentAdapterItem = (AbstractExpandableAdapterItem) rcvHolder.getItem();
            abstractParentAdapterItem.setParentListItemExpandCollapseListener(this);
        }
        (rcvHolder).getItem().onUpdateViews(mDataList.get(position), position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerViewList.add(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerViewList.remove(recyclerView);
    }
}
