package com.bayin.myapplication;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bayin.myapplication.good.Config;
import com.bayin.myapplication.good.PageFactory;
import com.bayin.myapplication.good.PageWidget;
import com.bayin.myapplication.good.db.BookList;
import com.bayin.myapplication.good.dialog.SettingDialog;
import com.bayin.myapplication.good.util.BrightnessUtil;

import java.io.IOException;

public class MainActivity extends Activity {

    private Handler handler = new Handler();
    private ObjectAnimator mAnimator1;
    private PageWidget pagewidget;
    private PageFactory pageFactory;
    private Config config;
    private final static String EXTRA_BOOK = "bookList";
    private SettingDialog mSettingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pagewidget = (PageWidget) findViewById(R.id.pagewidget);
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 19) {
            pagewidget.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        Config.createConfig(this);
        PageFactory.createPageFactory(this);

        config = Config.getInstance();
        pageFactory = PageFactory.getInstance();
        pageFactory.setBgBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.blank_bg));

        mSettingDialog = new SettingDialog(this);

        mSettingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                hideSystemUI();
            }
        });

        mSettingDialog.setSettingListener(new SettingDialog.SettingListener() {
            @Override
            public void changeSystemBright(Boolean isSystem, float brightness) {
                if (!isSystem) {
                    BrightnessUtil.setBrightness(MainActivity.this, brightness);
                } else {
                    int bh = BrightnessUtil.getScreenBrightness(MainActivity.this);
                    BrightnessUtil.setBrightness(MainActivity.this, bh);
                }
            }

            @Override
            public void changeFontSize(int fontSize) {
                pageFactory.changeFontSize(fontSize);
            }

            @Override
            public void changeTypeFace(Typeface typeface) {
                pageFactory.changeTypeface(typeface);
            }

            @Override
            public void changeBookBg(int type) {
                pageFactory.changeBookBg(type);
            }
        });

        //获取屏幕宽高
        WindowManager manage = getWindowManager();
        Display display = manage.getDefaultDisplay();
        Point displaysize = new Point();
        display.getSize(displaysize);
        int screenWidth = displaysize.x;
        int screenHeight = displaysize.y;

        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //隐藏
        hideSystemUI();
        //获取intent中的携带的信息
        Intent intent = getIntent();
        BookList bookList = (BookList) intent.getSerializableExtra(EXTRA_BOOK);

        pagewidget.setPageMode(config.getPageMode());
        pageFactory.setPageWidget(pagewidget);


        try {
            pageFactory.openBook(bookList);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "打开电子书失败", Toast.LENGTH_SHORT).show();
        }
        initListener();
    }

    protected void initListener() {


        pagewidget.setTouchListener(new PageWidget.TouchListener() {
            @Override
            public void center() {
                if (isShow) {
                    mSettingDialog.dismiss();
                } else mSettingDialog.show();
            }

            @Override
            public Boolean prePage() {

                pageFactory.prePage();
                if (pageFactory.isfirstPage()) {
                    return false;
                }

                return true;
            }

            @Override
            public Boolean nextPage() {
                Log.e("setTouchListener", "nextPage");

                pageFactory.nextPage();
                if (pageFactory.islastPage()) {
                    return false;
                }
                return true;
            }

            @Override
            public void cancel() {
                pageFactory.cancelPage();
            }
        });

    }

    public static boolean openBook(final BookList bookList, Activity context) {
        if (bookList == null) {
            throw new NullPointerException("BookList can not be null");
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_BOOK, bookList);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        context.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        context.startActivity(intent);
        return true;
    }

    private boolean isShow = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isShow) {
                return true;
            }
            if (mSettingDialog.isShowing()) {
                mSettingDialog.hide();
                return true;
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 隐藏菜单。沉浸式阅读
     */
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }
}
