package com.example.simon.myapp.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.example.simon.myapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 圆形滑动菜单(可设置是否自动回滚对齐)
 *
 * @author simon.k
 */
public class CircleMenuLayout extends ViewGroup {
    private int mRadius;
    private int mViewWidth;
    private int mViewHeight;

    private static final float RADIO_DEFAULT_CHILD_DIMENSION = 1 / 8f;

    private static final float RADIO_PADDING_LAYOUT = 1 / 90f;

    private static final int FLINGABLE_VALUE = 300;

    private static final int NOCLICK_VALUE = 3;

    private int mFlingableValue = FLINGABLE_VALUE;

    private float mPadding;

    //can set init angle 0.
    private double mStartAngle = 180;

    private String[] mItemTexts;

    private int mMenuItemCount;

    private float mTmpAngle;

    private float mLastX;
    private float mLastY;

    private float mItemAngleDelay;

    private ValueAnimator mValueAnimator;
    /**
     * 是否自动对准模式
     */
    private boolean isAlignment = true;


    int halfItemWidth;
    private int triangleHeight = 10;
    private int triangleLong = 16;

    private Paint paint;
    private Paint trianglePaint;
    private Paint bGPaint;

    private int triangBgColor;
    private int circleLineColor;
    private int viewBgColor;
    int curPosition;
    private int circleType;//default 0

    public void setAlignment(boolean alignment) {
        isAlignment = alignment;
    }

    public boolean getIsAlignment() {
        return isAlignment;
    }


    public boolean isSingleMode() {
        return isSingleMode;
    }

    public void setSingleMode(boolean singleMode) {
        isSingleMode = singleMode;
    }

    boolean isSingleMode = true;

    public CircleMenuLayout(Context context) {
        this(context, null);
    }

    public CircleMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        setPadding(0, 0, 0, 0);
        setWillNotDraw(false);
        initPaints();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleMenuLayout);
        isAlignment = typedArray.getBoolean(R.styleable.CircleMenuLayout_children_auto_alignment, true);
        triangBgColor = typedArray.getColor(R.styleable.CircleMenuLayout_triangle_bg_color,
                getResources().getColor(R.color.triang_color));
        circleLineColor = typedArray.getColor(R.styleable.CircleMenuLayout_circle_line_color,
                getResources().getColor(android.R.color.white));
        viewBgColor = typedArray.getColor(R.styleable.CircleMenuLayout_circle_view_bg_color,
                getResources().getColor(R.color.circle_view_bg));
        isSingleMode = typedArray.getBoolean(R.styleable.CircleMenuLayout_circle_view_single_mode, true);
        typedArray.recycle();
    }

    private void initPaints() {
        paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setAntiAlias(true);
        paint.setColor(circleLineColor);
        paint.setStyle(Paint.Style.STROKE);

        bGPaint = new Paint();
        paint.setStrokeWidth(1);
        bGPaint.setAntiAlias(true);
        bGPaint.setColor(viewBgColor);
        bGPaint.setStyle(Paint.Style.FILL);

        trianglePaint = new Paint();
        trianglePaint.setAntiAlias(true);
        trianglePaint.setColor(triangBgColor);
        trianglePaint.setStyle(Paint.Style.FILL);
    }

    private void drawLineTriangle(Canvas canvas) {
        canvas.drawCircle(mRadius / 2, mRadius / 2, mRadius / 2 - (mPadding + halfItemWidth * 4), bGPaint);
        if (isSingleMode) {
            canvas.drawCircle(mRadius / 2, mRadius / 2, mRadius / 2 - (mPadding + halfItemWidth * 2), bGPaint);
        } else {
            canvas.drawCircle(mRadius / 2, mRadius / 2, mRadius / 2 - (mPadding + halfItemWidth * 2), bGPaint);
            canvas.drawCircle(mRadius / 2, mRadius / 2, mRadius / 2 - (mPadding), bGPaint);
        }

        canvas.drawCircle(mRadius / 2, mRadius / 2, mRadius / 2 - (mPadding + halfItemWidth * 3), paint);
        canvas.drawCircle(mRadius / 2, mRadius / 2, mRadius / 2 - (mPadding + halfItemWidth * 2), paint);

        Path path = new Path();
        path.moveTo(mPadding + halfItemWidth * 2, mRadius / 2 - triangleHeight);
        path.lineTo(mPadding + halfItemWidth * 2 + triangleLong, mRadius / 2);
        path.lineTo(mPadding + halfItemWidth * 2, mRadius / 2 + triangleHeight);
        path.close();
        canvas.drawPath(path, trianglePaint);

        if (!isSingleMode) {
            Path pathIn = new Path();
            pathIn.moveTo((mPadding), mRadius / 2 - triangleHeight);
            pathIn.lineTo((mPadding) + triangleLong, mRadius / 2);
            pathIn.lineTo((mPadding), mRadius / 2 + triangleHeight);
            pathIn.close();
            canvas.drawPath(pathIn, trianglePaint);
            canvas.drawCircle(mRadius / 2, mRadius / 2, mRadius / 2 - (mPadding + halfItemWidth), paint);
            canvas.drawCircle(mRadius / 2, mRadius / 2, mRadius / 2 - (mPadding), paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        mViewWidth = getMeasuredWidth();
        mViewHeight = getMeasuredHeight();
        mRadius = Math.min(mViewWidth, mViewHeight);

        final int count = getChildCount();
        int childCount = (int) (mViewHeight * RADIO_DEFAULT_CHILD_DIMENSION);
        halfItemWidth = childCount / 2;
        int childMode = MeasureSpec.EXACTLY;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            int makeMeasureSpec = MeasureSpec.makeMeasureSpec(childCount, childMode);
            child.measure(makeMeasureSpec, makeMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int childCount = getChildCount();
        int left, top;
        int cWidth = (int) (mRadius * RADIO_DEFAULT_CHILD_DIMENSION);
        mItemAngleDelay = 360 * 1.0f / getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            mStartAngle %= 360;

            int targetCenter = 0;
            if (isSingleMode) {
                targetCenter = halfItemWidth * 2;
            }

            float tmp = mRadius / 2f - cWidth / 2 - mPadding - targetCenter;
            left = (mViewWidth / 2 + (int) (Math.round(tmp * Math.cos(Math.toRadians(mStartAngle))) - 1 / 2f * cWidth));
            top = (mViewHeight / 2 + (int) (Math.round(tmp * Math.sin(Math.toRadians(mStartAngle))) - 1 / 2f * cWidth));

            child.layout(left, top, left + cWidth, top + cWidth);
            mStartAngle += mItemAngleDelay;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLineTriangle(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mTmpAngle = 0;
                if (mValueAnimator != null) {
                    mValueAnimator.cancel();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float start = getAngle(mLastX, mLastY);
                float end = getAngle(x, y);
                if (getQuadrant(x, y) == 1 || getQuadrant(x, y) == 4) {
                    mStartAngle += (end - start);
                    mTmpAngle += (end - start);
                } else {
                    mStartAngle += (start - end);
                    mTmpAngle += (start - end);
                }
                requestLayout();
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (isAlignment) {
                    autoScroll(autoStartAngle());
                }
                if (Math.abs(mTmpAngle) > NOCLICK_VALUE) {
                    return true;
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }


    private void autoScroll(float endAngle) {
        if (endAngle == mStartAngle) {
            return;
        }
        if (mValueAnimator == null) {
            mValueAnimator = new ValueAnimator();
        } else {
            cancleAnimation();
        }
        mValueAnimator.setIntValues();
        mValueAnimator.setFloatValues(((float) mStartAngle), endAngle);
        mValueAnimator.setDuration((long) Math.abs(endAngle - mStartAngle) * 5);
        mValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mStartAngle = (float) valueAnimator.getAnimatedValue();
                requestLayout();
            }
        });
        mValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isAlignment) {
                    int pos = getCurPosition() == mItemTexts.length ? 0 : getCurPosition();
                    if (curPosition != pos) {
                        curPosition = pos;
                        mOnMenuItemClickListener.autoScrollBack(pos, isSingleMode, circleType);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mValueAnimator.start();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    //  onPause clean it
    public void cancleAnimation() {
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
        }
    }

    private float autoStartAngle() {
        return Math.round((mStartAngle - 180) / mItemAngleDelay) * mItemAngleDelay + 180;
    }

    public void scrollToNext(boolean clockwise) {
        if (clockwise) {
            autoScroll(autoStartAngle() + mItemAngleDelay);
        } else {
            autoScroll(autoStartAngle() - mItemAngleDelay);
        }
    }

    public void getDelayAngle(int pos) {
        int curPosition = getCurPosition();
        float targetAngle;
        int half = mItemTexts.length / 2;
        if (pos == mItemTexts.length) {
            pos = 0;
        }
        if (curPosition == mItemTexts.length) {
            curPosition = 0;
        }
        int i = pos - curPosition;
        if (i > 0) {
            if (i < half) {
                targetAngle = -i * mItemAngleDelay;
            } else {
                targetAngle = (mItemTexts.length - i) * mItemAngleDelay;
            }
        } else {
            if (Math.abs(i) < half) {
                targetAngle = Math.abs(i) * mItemAngleDelay;
            } else {
                targetAngle = -(mItemTexts.length + i) * mItemAngleDelay;
            }
        }
        float end = autoStartAngle() + targetAngle;
        autoScroll(end);
    }

    public int getCurPosition() {
        return getChildCount() - (int) Math.round((mStartAngle - 180 + 360) % 360 / mItemAngleDelay);
    }

    private float getAngle(float xTouch, float yTouch) {
        double x = xTouch - (mViewHeight / 2d);
        double y = yTouch - (mViewHeight / 2d);
        return (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
    }

    private int getQuadrant(float x, float y) {
        int tmpX = (int) (x - mViewHeight / 2);
        int tmpY = (int) (y - mViewHeight / 2);
        if (tmpX >= 0) {
            return tmpY >= 0 ? 4 : 1;
        } else {
            return tmpY >= 0 ? 3 : 2;
        }
    }

    /**
     * @param isSingle 是否是 单环 模式
     */
    public void setMenuItemIconsAndTexts(String[] texts, boolean isSingle) {
        this.isSingleMode = isSingle;
        mItemTexts = texts;
        if (texts == null) {
            throw new IllegalArgumentException("error...CircleMenuLayout data is null,check the text data !");
        }
        mMenuItemCount = texts.length;
        addMenuItems();
    }

    private List<View> mViewList;

    private void addMenuItems() {
        if (mViewList == null) {
            mViewList = new ArrayList<View>(mMenuItemCount);
        } else {
            mViewList.clear();
            this.removeAllViews();
        }
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        for (int i = 0; i < mMenuItemCount; i++) {
            final int j = i;
            int mMenuItemLayoutId = R.layout.circle_menu_item;
            View view = mInflater.inflate(mMenuItemLayoutId, this, false);
            TextView tv = (TextView) view.findViewById(R.id.circle_layout_item_tv);
            if (tv != null) {
                tv.setVisibility(View.VISIBLE);
                tv.setText(mItemTexts[i]);
                tv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnMenuItemClickListener != null) {
                            getDelayAngle(j);
                            mOnMenuItemClickListener.itemClick(v, j, isSingleMode, circleType);
                        }
                    }
                });
            }
            mViewList.add(view);
            addView(view);
        }
        mStartAngle = 180;
        requestLayout();
    }

    public void setFlingableValue(int mFlingableValue) {
        this.mFlingableValue = mFlingableValue;
    }

    public void setPadding(float mPadding) {
        this.mPadding = mPadding;
    }

    private int getDefaultWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        assert wm != null;
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return Math.min(outMetrics.widthPixels, outMetrics.heightPixels);
    }

    public interface OnMenuItemClickListener {
        void itemClick(View view, int pos, boolean isSingle, int circleType);

        void autoScrollBack(int pos, boolean isSingle, int circleType);
    }

    private OnMenuItemClickListener mOnMenuItemClickListener;

    public void setOnMenuItemClickListener(OnMenuItemClickListener mOnMenuItemClickListener) {
        this.mOnMenuItemClickListener = mOnMenuItemClickListener;
    }
}