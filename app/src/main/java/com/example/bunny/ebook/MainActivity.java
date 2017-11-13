package com.example.bunny.ebook;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
//78:54  1.44

public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
//    private PageWidget mPageWidget;
    Bitmap mCurPageBitmap, mNextPageBitmap;
    Canvas mCurPageCanvas, mNextPageCanvas;
    BookPageFactory pagefactory;
    private int width = 1280, height = 720;
    private PageWidget mPageWidget;
    private LinearLayout rootView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.book_layout);

        //获取上个页面传递来的path
        String path = getIntent().getStringExtra("path");
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(this, "路径为空，请传递合法文件路径", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        width = (int) (wm.getDefaultDisplay().getWidth() * 0.8);
        height = wm.getDefaultDisplay().getHeight();

        rootView = (LinearLayout) findViewById(R.id.rootview);
        mPageWidget = new PageWidget(this, width, height);
        rootView.addView(mPageWidget);
        mCurPageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mNextPageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        mCurPageCanvas = new Canvas(mCurPageBitmap);
        mNextPageCanvas = new Canvas(mNextPageBitmap);
        pagefactory = new BookPageFactory(width, height);//设置分辨率

        //设置字体
        Typeface typeface = Typeface.createFromAsset(getAssets(), "cj.ttf");
        pagefactory.setTypeFace(typeface);
        pagefactory.setTextSize(40, mCurPageCanvas);

        pagefactory.setBgBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.mipmap.book_bg_green));//设置背景图片
        try {
            pagefactory.openbook(path);//打开文件
//            pagefactory.openbook("/mnt/shared/Other/book.txt");//打开文件
            pagefactory.onDraw(mCurPageCanvas);//将文字绘于手机屏幕
        } catch (IOException e1) {
            e1.printStackTrace();
            Toast.makeText(this, "电子书不存在,请将《test.txt》放在SD卡根目录下",
                    Toast.LENGTH_SHORT).show();
        }

        mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);

        mPageWidget.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                boolean ret = false;
                if (v == mPageWidget) {


                    if (e.getAction() == MotionEvent.ACTION_DOWN) {

                        //如果正在执行翻页动画，则中断点击事件
                        if (mPageWidget.isPlaying()) return false;

                        //停止动画。与forceFinished(boolean)相反，Scroller滚动到最终x与y位置时中止动画。
                        mPageWidget.abortAnimation();

                        //计算拖拽点对应的拖拽角
                        mPageWidget.calcCornerXY(e.getX(), e.getY());

                        //将文字绘于当前页
                        pagefactory.onDraw(mCurPageCanvas);


                        if (mPageWidget.DragToRight()) {
                            //是否从左边翻向右边
                            try {
                                //true，显示上一页
                                pagefactory.prePage();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            if (pagefactory.isfirstPage()) return false;
                            //绘制下面一页内容
                            pagefactory.onDraw(mNextPageCanvas);
                        } else {
                            try {
                                //false，显示下一页
                                pagefactory.nextPage();
                            } catch (IOException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                            if (pagefactory.islastPage()) return false;
                            //绘制下面一页内容
                            pagefactory.onDraw(mNextPageCanvas);
                        }

                        mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
                    }

                    ret = mPageWidget.doTouchEvent(e);
                    return ret;
                }
                return false;
            }

        });
    }
}
