package com.ptyt.uct.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.android.uct.utils.PrintLog;
import com.ptyt.uct.R;


/**
 * @Description:
 * @Date: 2018/1/18
 * @Author: KeChuanqi
 * @Version:V1.0
 */
public class LineProgressView extends View {
    private final float lineStroke;
    private Context mContext = null;
    private Paint sidePaint;
    private Paint srcPaint;
    private int textColor;
    private float progress;
    private float totalNum = 100;
    private Paint textPaint;
    private float lineY;


    public LineProgressView(Context context) {
        this(context, null);
    }

    public LineProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        lineY = context.getResources().getDimension(R.dimen.y30);
        lineStroke = context.getResources().getDimension(R.dimen.y5);
        initAttrs(context,attrs);
        initPaint();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LineProgressView);
        textColor = ta.getColor(R.styleable.LineProgressView_bgLineColor,0xffff3c32);
        ta.recycle();
    }

    private void initPaint() {
        sidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sidePaint.setStyle(Paint.Style.STROKE);
        sidePaint.setStrokeWidth(lineStroke);
        sidePaint.setColor(0x40D2DCE8);
        srcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        srcPaint.setStrokeWidth(lineStroke);
        srcPaint.setColor(0xaa0077DA);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStrokeWidth(2);
        textPaint.setColor(0xaa0077DA);
        int lineY2 = (int) this.lineY;
        PrintLog.e("lineY2="+lineY2 + "  lineY="+lineY + "  lineStroke="+lineStroke);
        textPaint.setTextSize(28);
        textPaint.setTextAlign(Paint.Align.RIGHT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0,lineY,getWidth(),lineY,sidePaint);
        canvas.drawLine(0,lineY,(progress/totalNum)*getWidth(),lineY,srcPaint);
        canvas.drawText(progress+"%",(progress/totalNum)*getWidth(),lineY-8,textPaint);
    }

    public void onUpdate(float progress,int totalNum){
        this.progress = progress;
        this.totalNum = totalNum;
        invalidate();
    }

}