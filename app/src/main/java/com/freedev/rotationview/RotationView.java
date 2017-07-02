package com.freedev.rotationview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;


/**
 * RotationView:
 * 支持手势旋转的圆
 * 支持单指旋转以及fling操作
 * 进一步的,需支持拨动时的齿轮阻尼效果(音效+震动)
 * <p>
 * author:caca
 * 2017-6-19
 */
public class RotationView extends ImageView {

    private static final String TAG = "RotationView";
    private static final double ANGLE_ONE = 2 * Math.PI / 100;

    private float mTotalDegree = Integer.MAX_VALUE, mCurDegree;
    private RotateGestureDetector mRotateDetector;
    private OnRotateListener mRotateListener;
    protected Matrix mCurrentMatrix = new Matrix();
    private final PointF mCenterPoint = new PointF();
    private RectF mDrawRect, mViewRect;
    private Paint mLinePaint;
    private int mPadding, mHalf, mRadius;

    public RotationView(Context context) {
        super(context);
        init();
    }

    public RotationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RotationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setScaleType(ScaleType.MATRIX);
        mRotateDetector = new RotateGestureDetector(getContext(), new RotateGestureDetector.OnRotateGestureListener() {
            @Override
            public void onRotated(float delta) {
                handleOnRotated(delta);
            }

            @Override
            public void onRotateEnd() {
                if (mRotateListener != null) {
                    mRotateListener.onRotateEnd(mCurDegree);
                }
            }
        });

        mDrawRect = new RectF();
        mViewRect = new RectF();

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.GRAY);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setFilterBitmap(true);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeWidth(2);
    }

    public void setTotalDegree(float totalDegree) {
        mTotalDegree = totalDegree;
    }

    public void setCurDegree(float curDegree) {
        mCurDegree = curDegree;
    }

    private void handleOnRotated(float delta) {
        Log.d(TAG, "onRotated: " + delta);
        boolean isRotatedEnd = false;
        if (mCurDegree >= mTotalDegree && delta > 0) {
            Log.d(TAG, "too big: total degree " + mTotalDegree + ", curDegree " + mCurDegree + ", delta " + delta);
            return;
        }
        if (mCurDegree <= 0 && delta < 0) {
            Log.d(TAG, "too small: total degree " + mTotalDegree + ", curDegree " + mCurDegree + ", delta " + delta);
            return;
        }
        mCurDegree += delta;
        if (mCurDegree > mTotalDegree) {
            delta -= mCurDegree - mTotalDegree;
            mCurDegree = mTotalDegree;
            isRotatedEnd = mRotateDetector.isFling();
            mRotateDetector.stopFling();
        } else if (mCurDegree < 0) {
            delta -= mCurDegree;
            mCurDegree = 0;
            isRotatedEnd = mRotateDetector.isFling();
            mRotateDetector.stopFling();
        }

        Log.d(TAG, "onRotated curDegree: " + mCurDegree);
        postRotate(delta);
        if (mRotateListener != null) {
            mRotateListener.onRotated(mCurDegree);
        }

        if (isRotatedEnd && mRotateListener != null) {
            mRotateListener.onRotateEnd(mCurDegree);
        }
    }

    @Override
    public void setImageMatrix(Matrix matrix) {
        super.setImageMatrix(matrix);
        mCurrentMatrix.set(matrix);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHalf == 0) {
            mPadding = getPaddingLeft();
            mHalf = getMeasuredWidth() / 2;
            mRadius = mHalf - mPadding;
            mCenterPoint.set(mRadius, mRadius);
            mRotateDetector.setCenterPoint(mCenterPoint);
            mDrawRect.set(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
            mViewRect.set(0, 0, mRadius * 2, mRadius * 2);
            mCurrentMatrix.setRectToRect(mDrawRect, mViewRect, Matrix.ScaleToFit.CENTER);
            setImageMatrix(mCurrentMatrix);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mRotateDetector.onTouchEvent(event);
    }

    public void autoRotate(float deltaAngle) {
        Log.d(TAG, "autoRotate: curDegree " + mCurDegree + ", delta " + deltaAngle);
        mCurDegree += deltaAngle;
        postRotate(deltaAngle);
    }

    protected void postRotate(float deltaAngle) {
        if (deltaAngle != 0) {
            mCurrentMatrix.postRotate(deltaAngle, mCenterPoint.x, mCenterPoint.y);
            setImageMatrix(mCurrentMatrix);
        }
    }

    public void setOnRotateListener(OnRotateListener listener) {
        mRotateListener = listener;
    }

    public interface OnRotateListener {
        void onRotated(float curDegree);

        void onRotateEnd(float curDegree);
    }
}
