package com.bayin.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

/****************************************
 * 功能说明:  
 *
 * Author: Created by bayin on 2017/11/13.
 ****************************************/

public class PageView extends View {

    private int mWidth;
    private int mHeight;
    private Bitmap mBackGround;
    private Paint mPaint;
    private Camera mCamera;
    //Y轴方向旋转角度
    private float degreeY;
    //不变的那一半，Y轴方向旋转角度
    private float fixDegreeY;
    //Z轴方向（平面内）旋转的角度
    private float degreeZ;
    private Bitmap mMbg;

    public PageView(Context context) {
        this(context, null);
    }

    public PageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWidth = wm.getDefaultDisplay().getWidth();
        mHeight = wm.getDefaultDisplay().getHeight();

        mBackGround = zoomImg(BitmapFactory.decodeResource(getResources(), R.mipmap.book_bg_green), 500, 500);
        mMbg = zoomImg(BitmapFactory.decodeResource(getResources(), R.mipmap.blank_bg), 500, 500);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mCamera = new Camera();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float newZ = -displayMetrics.density * 6;
        mCamera.setLocation(0, 0, newZ);
    }

    private boolean isinit = false;

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        int bitmapWidth = mBackGround.getWidth();
        int bitmapHeight = mBackGround.getHeight();
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int x = centerX - bitmapWidth / 2;
        int y = centerY - bitmapHeight / 2;

        //画变换的一半
        //先旋转，再裁切，再使用camera执行3D动效,**然后保存camera效果**,最后再旋转回来
        canvas.save();
        mCamera.save();
        canvas.translate(centerX, centerY);
        canvas.rotate(-degreeZ);
        mCamera.rotateY(degreeY);
        mCamera.applyToCanvas(canvas);
        //计算裁切参数时清注意，此时的canvas的坐标系已经移动
        canvas.clipRect(0, -centerY, centerX, centerY);
        canvas.rotate(degreeZ);
        canvas.translate(-centerX, -centerY);
        mCamera.restore();
        canvas.drawBitmap(mBackGround, x, y, mPaint);
        canvas.restore();

        //画不变换的另一半
        canvas.save();
        mCamera.save();
        canvas.translate(centerX, centerY);
        canvas.rotate(-degreeZ);
        //计算裁切参数时清注意，此时的canvas的坐标系已经移动
        canvas.clipRect(-centerX, -centerY, 0, centerY);
        //此时的canvas的坐标系已经旋转，所以这里是rotateY
        mCamera.rotateY(fixDegreeY);
        mCamera.applyToCanvas(canvas);
        canvas.rotate(degreeZ);
        canvas.translate(-centerX, -centerY);
        mCamera.restore();
        canvas.drawBitmap(mBackGround, x, y, mPaint);
        canvas.restore();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mWidth, mWidth);
    }

    public PageView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    private Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }

    /**
     * 启动动画之前调用，把参数reset到初始状态
     */
    public void reset() {
        degreeY = 0;
        fixDegreeY = 0;
        degreeZ = 0;
    }

    public void setFixDegreeY(float fixDegreeY) {
        this.fixDegreeY = fixDegreeY;
        invalidate();
    }

    public void setDegreeY(float degreeY) {
        this.degreeY = degreeY;
        invalidate();
    }

    public void setDegreeZ(float degreeZ) {
        this.degreeZ = degreeZ;
        invalidate();
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBackGround = bitmap;
        invalidate();
    }
}
