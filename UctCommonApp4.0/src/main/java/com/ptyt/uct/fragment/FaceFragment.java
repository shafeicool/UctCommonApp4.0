package com.ptyt.uct.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Date: 2017/5/10
 * @Author: ShaFei
 * @Version: V1.0
 */
public class FaceFragment extends BaseFragment implements OnItemClickListener, ViewPager.OnPageChangeListener {

    private Activity currentActivity;
    private ViewPager mViewPage;
    private ViewGroup mPage;
    private ArrayList<GridView> grids = new ArrayList<>();
    public static final int FACE_WHAT = 122;
    // 下方的点点
    private ImageView[] tips;
    // 列数
    private int vertical = 3;
    // 行数
    private int horizantal = 7;
    // 一页容纳多少个表情
    private int onePageNum = vertical * horizantal;
    // 总页数
    private int pageNum;

    // 图片集合
    // 注意：1、expressionImgs集合必须与expressionImgNames数目相等  2、最后一张必须是删除功能图片
    public static int[] expressionImgs = new int[]{
            R.mipmap.icon_01, R.mipmap.icon_02, R.mipmap.icon_03, R.mipmap.icon_04,
            R.mipmap.icon_05, R.mipmap.icon_06, R.mipmap.icon_07, R.mipmap.icon_08,
            R.mipmap.icon_09, R.mipmap.icon_10, R.mipmap.icon_11, R.mipmap.icon_12,
            R.mipmap.icon_13, R.mipmap.icon_14, R.mipmap.icon_15, R.mipmap.icon_16,
            R.mipmap.icon_17, R.mipmap.icon_18, R.mipmap.icon_19, R.mipmap.icon_20,
            R.mipmap.icon_21, R.mipmap.icon_22, R.mipmap.icon_23, R.mipmap.icon_24,
            R.mipmap.icon_25, R.mipmap.icon_26, R.mipmap.icon_27, R.mipmap.icon_28,
            R.mipmap.icon_29, R.mipmap.icon_30, R.mipmap.icon_31, R.mipmap.icon_32,
            R.mipmap.icon_33, R.mipmap.icon_34, R.mipmap.icon_35, R.mipmap.icon_36,
            R.mipmap.icon_37, R.mipmap.icon_38, R.mipmap.icon_del
    };

    // 表达式集合
    // 注意：1、expressionImgs集合必须与ex`pressionImgNames数目相等  2、最后一张必须是删除功能表达式
    public static String[] expressionImgNames = new String[]{
            "\\01", "\\02", "\\03", "\\04", "\\05", "\\06", "\\07",
            "\\08", "\\09", "\\10", "\\11", "\\12", "\\13", "\\14", "\\15",
            "\\16", "\\17", "\\18", "\\19", "\\20", "\\21", "\\22", "\\23",
            "\\24", "\\25", "\\26", "\\27", "\\28", "\\29", "\\30", "\\31",
            "\\32", "\\33", "\\34", "\\35", "\\36", "\\37", "\\38", "del_normal"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentActivity = getActivity();
        if (currentActivity instanceof OnEventListener) {
            setOnEventListener((OnEventListener) currentActivity);
        }
    }

    @Override
    protected int setLayoutId() {
        return R.layout.fragment_message_chat_bottom_face;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        mViewPage = (ViewPager) view.findViewById(R.id.face_vp);
        mPage = (ViewGroup) view.findViewById(R.id.page_ll);
        LayoutInflater inflater1 = currentActivity.getLayoutInflater();
        // 分页加载Begin
        int tmp = (expressionImgs.length - 1) % (onePageNum - 1);
        pageNum = (expressionImgs.length - 1) / (onePageNum - 1);
        if (tmp != 0) {
            pageNum = pageNum + 1;
        }
        for (int i = 0; i < pageNum; i++) {
            GridView gridView = (GridView) inflater1.inflate(R.layout.view_face_gridview, null);
            gridView.setNumColumns(horizantal);
            initViewPage(expressionImgs, gridView, i);
            grids.add(gridView);
            gridView.setOnItemClickListener(this);
        }
        // 分页加载End
        mViewPage.setAdapter(mPagerAdapter);
        mViewPage.addOnPageChangeListener(this);
        scrollPager();
    }

    /**
     * @param expressionImgs
     * @param gridView
     * @param currentPageNum 当前页数
     * @return GridView
     * @description 分页加载显示图片
     */
    private GridView initViewPage(int expressionImgs[], GridView gridView, int currentPageNum) {
        List<Map<String, Object>> listItems = new ArrayList<>();
        int start = currentPageNum * (onePageNum - 1);
        int lastPageNum = expressionImgs.length - start;
        if (lastPageNum < onePageNum) { // 最后一页的显示
            for (int i = 0; i < lastPageNum; i++) {
                Map<String, Object> listItem = new HashMap<>();
                if (i == lastPageNum - 1) {
                    listItem.put("image", expressionImgs[expressionImgs.length - 1]);
                } else {
                    listItem.put("image", expressionImgs[start + i]);
                }
                listItems.add(listItem);
            }
        } else { // 前几页的显示
            for (int i = 0; i < onePageNum; i++) {
                Map<String, Object> listItem = new HashMap<>();
                if (i == onePageNum - 1) {
                    listItem.put("image", expressionImgs[expressionImgs.length - 1]);
                } else {
                    listItem.put("image", expressionImgs[start + i]);
                }
                listItems.add(listItem);
            }
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(currentActivity, listItems,
                R.layout.singleexpression, new String[]{"image"}, new int[]{R.id.image});
        gridView.setAdapter(simpleAdapter);
        return gridView;
    }

    /**
     * @param
     * @return
     * @description 显示下方的点点
     */
    private void scrollPager() {
        tips = new ImageView[pageNum];
        for (int i = 0; i < tips.length; i++) {
            ImageView imageView = new ImageView(currentActivity);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ScreenUtils.getDimensionPixelSize(currentActivity, R.dimen.y10), ScreenUtils.getDimensionPixelSize(currentActivity, R.dimen.y10));
            imageView.setLayoutParams(params);
            tips[i] = imageView;
            if (i == 0) {
                tips[i].setBackgroundResource(R.drawable.shape_message_face_page_focused);
            } else {
                tips[i].setBackgroundResource(R.drawable.shape_message_face_page_unfocused);
            }
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            layoutParams.leftMargin = ScreenUtils.getDimensionPixelSize(currentActivity, R.dimen.x10);
            layoutParams.rightMargin = ScreenUtils.getDimensionPixelSize(currentActivity, R.dimen.x10);
            mPage.addView(imageView, layoutParams);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (onEventListener == null) {
            return;
        }
        int currentItem = mViewPage.getCurrentItem();
        String content;
        if (pageNum == currentItem) { // 最后一页的显示
            content = expressionImgNames[currentItem * (onePageNum - 1) + position];
        } else {
            if (position == (onePageNum - 1)) { // 前几页的显示
                content = expressionImgNames[expressionImgNames.length - 1];
            } else {
                content = expressionImgNames[currentItem * (onePageNum - 1) + position];
            }
        }
        PrintLog.d("content==" + content);
        onEventListener.onEvent(FACE_WHAT, null, content);
    }

    PagerAdapter mPagerAdapter = new PagerAdapter() {
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getCount() {
            return grids.size();
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            ((ViewPager) container).removeView(grids.get(position));
        }

        @Override
        public Object instantiateItem(View container, int position) {
            ((ViewPager) container).addView(grids.get(position));
            return grids.get(position);
        }
    };

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < tips.length; i++) {
            if (i == position % tips.length) {
                tips[i].setBackgroundResource(R.drawable.shape_message_face_page_focused);
            } else {
                tips[i].setBackgroundResource(R.drawable.shape_message_face_page_unfocused);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
