package com.freedev.rotationview;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class RotateGestureDetector implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private static final String TAG = "RotateGestureDetector";

    private float mLastDegree;
    private PointF mCenterPoint;                //中心点坐标
    private OnRotateGestureListener mListener;
    private GestureDetectorCompat mDetector;
    /**
     * 判断是否正在自动滚动
     */
    private boolean mIsFling, mIsScrolling;
    private Handler mHandler;
    private AutoFlingRunnable mAutoFlingRunnable;

    public RotateGestureDetector(Context context, OnRotateGestureListener listener) {
        mListener = listener;
        mDetector = new GestureDetectorCompat(context, this);
        mDetector.setOnDoubleTapListener(this);
        mHandler = new Handler();
    }

    public void setCenterPoint(PointF centerPoint) {
        mCenterPoint = centerPoint;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = mDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP && mIsScrolling && !mIsFling) {
            mIsScrolling = false;
            if (mListener != null) {
                mListener.onRotateEnd();
            }
        }
        return ret;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d(TAG, "onSingleTapConfirmed: " + e.toString());
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.d(TAG, "onDoubleTap: " + e.toString());
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        Log.d(TAG, "onDoubleTapEvent: " + e.toString());
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d(TAG, "onDown: " + e.getX() + " " + e.getY());
        mLastDegree = 0;
        mIsScrolling = false;
        stopFlingAndRotateEnd();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.d(TAG, "onShowPress: " + e.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d(TAG, "onSingleTapUp: " + e.toString());
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float degree = getRotationDegreesDelta(e2.getX(), e2.getY(), e1.getX(), e1.getY());
        Log.d(TAG, "onScroll: " + e1.getX() + " " + e1.getY() + " " + e2.getX() + " " + e2.getY() + ", degree: " + degree);
        mIsScrolling = true;
        callOnRotation(degree);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float degree = getRotationDegreesDelta(e2.getX(), e2.getY(), e1.getX(), e1.getY());
        Log.d(TAG, "onFling: " + e1.getX() + " " + e1.getY() + " " + e2.getX() + " " + e2.getY() + ", degree: " + degree);
        mIsScrolling = false;
        stopFling();
        startFling(degree, (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY));
        return true;
    }

    private void callOnRotation(float degree) {
        float delta = degree - mLastDegree;
        if (delta > 180.0f) {
            delta -= 360;
        } else if (delta < -180.0f) {
            delta += 360;
        }
        if (mListener != null) {
            mListener.onRotated(delta);
        }
        mLastDegree = degree;
    }

    private float getRotationDegreesDelta(float x1, float y1, float x2, float y2) {
        double diffRadians;
        if (mCenterPoint != null) {
            diffRadians = Math.atan2(y1 - mCenterPoint.y, x1 - mCenterPoint.y) - Math.atan2(y2 - mCenterPoint.y, x2 - mCenterPoint.y);
        } else {
            diffRadians = Math.atan2(y1, x1) - Math.atan2(y2, x2);
        }
        return (float) (diffRadians * 180 / Math.PI);
    }

    public boolean isFling() {
        return mIsFling;
    }

    public void startFling(float degree, float v) {
        mIsFling = true;
        mAutoFlingRunnable = new AutoFlingRunnable(degree, v);
        mHandler.postDelayed(mAutoFlingRunnable, 30);
    }

    public void stopFling() {
        mIsFling = false;
        if (mAutoFlingRunnable != null) {
            mHandler.removeCallbacks(mAutoFlingRunnable);
        }
    }

    public void stopFlingAndRotateEnd() {
        if (mIsFling) {
            if (mListener != null) {
                mListener.onRotateEnd();
            }
        }
        stopFling();
    }

    /**
     * 自动滚动的任务
     *
     * @author zhy
     */
    private class AutoFlingRunnable implements Runnable {

        private float mVelocity;
        private float mDegree;

        public AutoFlingRunnable(float degree, float velocity) {
            mDegree = degree;
            if (mDegree > 180.0f) {
                mDegree -= 360;
            } else if (mDegree < -180.0f) {
                mDegree += 360;
            }
            mVelocity = velocity;
            //如果初速度过低,就不进行fling了
            if (mVelocity < 500) {
                mDegree = 0;
            }
        }

        public void run() {
            // 如果小于20,则停止
            if ((int) Math.abs(mVelocity) < 100 || mDegree == 0) {
                stopFlingAndRotateEnd();
                return;
            }
            if (mIsFling) {
                if (mDegree > 0) {
                    mDegree += (mVelocity / 200);
                } else {
                    mDegree -= (mVelocity / 200);
                }
                // 逐渐减小这个值
                mVelocity /= 1.0666F;
                callOnRotation(mDegree);
                mHandler.postDelayed(this, 30);
            }
        }
    }

    interface OnRotateGestureListener {
        void onRotated(float delta);

        void onRotateEnd();
    }
}
