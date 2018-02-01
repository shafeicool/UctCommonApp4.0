package com.ptyt.uct.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;
import com.ptyt.uct.adapter.BaseRecyAdapter;
import com.ptyt.uct.adapter.MessageFileListAdapter;
import com.ptyt.uct.entity.MessageFileListEntity;
import com.ptyt.uct.utils.FileUtils;
import com.ptyt.uct.utils.KeyBoardUtils;
import com.ptyt.uct.utils.StrUtils;
import com.ptyt.uct.utils.ToastUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MessageFileListActivity extends BaseActionBarActivity implements View.OnClickListener, BaseRecyAdapter.OnItemClickListener, TextWatcher {

    private TextView mNoFileTv;
    private EditText mSearchEt;
    private ProgressBar mProgressView;
    private RecyclerView mRecyclerView;

    private LinearLayoutManager layoutManager;
    private MessageFileListAdapter mAdapter;
    // 所有的文件列表
    private List<MessageFileListEntity> allList = new ArrayList<>();
    // 已选择的文件列表
    private List<MessageFileListEntity> selectedList = new ArrayList<>();
    // 搜索的文件列表
    private List<MessageFileListEntity> searchAllList = new ArrayList<>();
    // 选择文件的数目
    private int counts = 0;
    // 限制发送文件数目
    private static final int LIMIT_COUNTS = 10;
    private String textSearched;
    private String[] suffixName = null;

    private static final int INIT_FILE_LIST = 0;
    private static final int INIT_SEARCH_FILE_LIST = 1;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_message_file_list;
    }

    @Override
    protected void initWidget() {
        mProgressView = (ProgressBar) findViewById(R.id.progress_message_pbar);
        mNoFileTv = (TextView) findViewById(R.id.no_file_tv);

        mSearchEt = (EditText) findViewById(R.id.search_et);
        Drawable leftDrawable = mSearchEt.getCompoundDrawables()[0];
        if (leftDrawable != null) {
            leftDrawable.setBounds(0, 0, getResources().getDimensionPixelOffset(R.dimen.y38), getResources().getDimensionPixelOffset(R.dimen.y38));
            mSearchEt.setCompoundDrawables(leftDrawable, null, null, null);//只放左边
        }
        mSearchEt.addTextChangedListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.file_list_rv);
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    KeyBoardUtils.closeKeybord(mContext, mSearchEt);
                }
            }
        });

        tv_actionBarTitle.setText(getResources().getString(R.string.string_file_list_title));
        tv_actionBarRight.setVisibility(View.VISIBLE);
        tv_actionBarRight.setText(getResources().getString(R.string.string_file_list_send));
        tv_actionBarRight.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextView_13));
        tv_actionBarRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.y28));
        tv_actionBarRight2.setBackgroundResource(R.drawable.shape_message_file_list_send);
        tv_actionBarRight2.setGravity(Gravity.CENTER);
        mProgressView.setVisibility(View.VISIBLE);
        rl_layout.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        mSearchEt.setVisibility(View.GONE);

        rl_relative.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setNewMyLooper(true);
        mAdapter = new MessageFileListAdapter(mContext);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        suffixName = new String[]{".xla", ".xlc", ".xll", ".xlm", ".xls", ".xlt", ".xlw", ".xlsx", // EXCEL
                ".doc", ".docx", ".dot", // WORD
                ".bz2", ".gtar", ".gtar", ".jar", ".lzh", ".nar", ".rar", ".uu", ".uue", ".x-gzip", ".z", ".zip", // ZIP
                ".txt", ".awb", ".m4a", ".mid", ".midi", ".mp2", ".mp3", ".mp4", ".ogg", ".wav"}; // OTHER
        sendMsg(true, INIT_FILE_LIST);
    }

    @Override
    public boolean handleMessage(final Message message) {
        super.handleMessage(message);
        switch (message.what) {
            case INIT_FILE_LIST:
                // 搜索指定后缀名文件
                allList = FileUtils.getInstance(mContext).getSpecificTypeOfFile(mContext, suffixName);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressView.setVisibility(View.GONE);
                        rl_layout.setVisibility(View.VISIBLE);
                        if (allList != null && allList.size() > 0) {
                            mNoFileTv.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mSearchEt.setVisibility(View.VISIBLE);
                            mAdapter.addAll(allList);
                        } else {
                            // 没有搜索到任何文件
                            mRecyclerView.setVisibility(View.GONE);
                            mSearchEt.setVisibility(View.GONE);
                            mNoFileTv.setVisibility(View.VISIBLE);
                        }
                    }
                });
                break;
            case INIT_SEARCH_FILE_LIST:
                searchAllList.clear();
                for (int j = 0; j < allList.size(); j++) {
                    MessageFileListEntity entity = allList.get(j);
                    String displayName = entity.getDisplayName();
                    if (displayName.toLowerCase().contains(textSearched.toLowerCase())) {
                        searchAllList.add(entity);
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.addAll(searchAllList);
                    }
                });
                break;
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        textSearched = mSearchEt.getText().toString();
        if (StrUtils.isEmpty(textSearched)) {
            mAdapter.addAll(allList);
        } else {
            sendMsg(true, INIT_SEARCH_FILE_LIST);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_relative:
                if (counts > 0) {
                    PrintLog.i("发送数据啦");
                    Intent intent = new Intent();
                    intent.putExtra("FileList", (Serializable) selectedList);
                    setResult(MessageActivity.FILE_RESULT_CODE, intent);
                    finish();
                }
                break;
        }
    }

    @Override
    public void onItemClick(int pos, View itemView) {
        if (mAdapter.getItem(pos).getChecked()) {
            mAdapter.getItem(pos).setChecked(false);
            counts--;
            PrintLog.i("remove name = " + mAdapter.getItem(pos).getDisplayName());
            selectedList.remove(mAdapter.getItem(pos));
        } else {
            if (counts >= LIMIT_COUNTS) {
                ToastUtils.getToast().showMessageShort(mContext, String.format(getResources().getString(R.string.string_file_list_prompt), LIMIT_COUNTS), -1);
                return;
            }
            mAdapter.getItem(pos).setChecked(true);
            counts++;
            PrintLog.i("add name = " + mAdapter.getItem(pos).getDisplayName());
            selectedList.add(mAdapter.getItem(pos));
        }
        if (counts > 0) {
            tv_actionBarRight.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextView_12));
            tv_actionBarRight2.setVisibility(View.VISIBLE);
            tv_actionBarRight2.setText(counts + "");
        } else {
            tv_actionBarRight.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextView_13));
            tv_actionBarRight2.setVisibility(View.GONE);
        }
        mAdapter.updateItem(pos, mAdapter.getItem(pos));
    }

    @Override
    protected void onDestroy() {
        FileUtils.getInstance(mContext).release();
        super.onDestroy();
    }
}
