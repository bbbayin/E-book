package com.bayin.myapplication.good.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bayin.myapplication.R;
import com.bayin.myapplication.good.Config;


/**
 * Created by Administrator on 2016/7/26 0026.
 */
public class SettingDialog extends Dialog implements View.OnClickListener {

    TextView tv_dark;
    TextView tv_bright;
    TextView tv_subtract;
    TextView tv_size;
    TextView tv_add;
    TextView tv_qihei;
    TextView tv_default;
    TextView tv_size_default;
    TextView tv_fzkatong;
    TextView tv_bysong;


    private Config config;
    private Boolean isSystem;
    private SettingListener mSettingListener;
    private int FONT_SIZE_MIN;
    private int FONT_SIZE_MAX;
    private int currentFontSize;

    private SettingDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    public SettingDialog(Context context) {
        this(context, R.style.setting_dialog);
    }

    public SettingDialog(Context context, int themeResId) {
        super(context, themeResId);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setGravity(Gravity.BOTTOM);
        setContentView(R.layout.dialog_setting);


        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = d.getWidth();
        getWindow().setAttributes(p);

        FONT_SIZE_MIN = (int) getContext().getResources().getDimension(R.dimen.reading_min_text_size);
        FONT_SIZE_MAX = (int) getContext().getResources().getDimension(R.dimen.reading_max_text_size);

        config = Config.getInstance();

        //点击事件
        tv_subtract = (TextView) findViewById(R.id.tv_subtract);
        tv_subtract.setOnClickListener(this);//+A
        tv_add = (TextView) findViewById(R.id.tv_add);
        tv_add.setOnClickListener(this);//-A
        tv_size_default = (TextView) findViewById(R.id.tv_size_default);
        tv_size_default.setOnClickListener(this);//默认字号
        tv_default = (TextView) findViewById(R.id.tv_default);
        tv_default.setOnClickListener(this);
        tv_qihei= (TextView) findViewById(R.id.tv_qihei);
        tv_qihei.setOnClickListener(this);
        tv_fzkatong = (TextView) findViewById(R.id.tv_fzkatong);
        tv_fzkatong.setOnClickListener(this);
        tv_bysong = (TextView) findViewById(R.id.tv_bysong);
        tv_bysong.setOnClickListener(this);
        tv_size = (TextView) findViewById(R.id.tv_size);

        //初始化字体大小
        currentFontSize = (int) config.getFontSize();
        tv_size.setText(currentFontSize + "");

        //初始化字体
        tv_default.setTypeface(config.getTypeface(Config.FONTTYPE_DEFAULT));
        tv_qihei.setTypeface(config.getTypeface(Config.FONTTYPE_QIHEI));
        tv_fzkatong.setTypeface(config.getTypeface(Config.FONTTYPE_FZKATONG));
        tv_bysong.setTypeface(config.getTypeface(Config.FONTTYPE_BYSONG));
        selectTypeface(config.getTypefacePath());



    }

    //设置字体
    public void setBookBg(int type) {
        config.setBookBg(type);
        if (mSettingListener != null) {
            mSettingListener.changeBookBg(type);
        }
    }

    //选择字体
    private void selectTypeface(String typeface) {
        if (typeface.equals(Config.FONTTYPE_DEFAULT)) {
            setTextViewSelect(tv_default, true);
            setTextViewSelect(tv_qihei, false);
            setTextViewSelect(tv_fzkatong, false);
            setTextViewSelect(tv_bysong, false);
//            setTextViewSelect(tv_xinshou, false);
//            setTextViewSelect(tv_wawa, false);
        } else if (typeface.equals(Config.FONTTYPE_QIHEI)) {
            setTextViewSelect(tv_default, false);
            setTextViewSelect(tv_qihei, true);
            setTextViewSelect(tv_fzkatong, false);
            setTextViewSelect(tv_bysong, false);
//            setTextViewSelect(tv_xinshou, false);
//            setTextViewSelect(tv_wawa, false);
        } else if (typeface.equals(Config.FONTTYPE_FZXINGHEI)) {
            setTextViewSelect(tv_default, false);
            setTextViewSelect(tv_qihei, false);
            setTextViewSelect(tv_fzkatong, false);
            setTextViewSelect(tv_bysong, false);
//            setTextViewSelect(tv_xinshou, true);
//            setTextViewSelect(tv_wawa, false);
        } else if (typeface.equals(Config.FONTTYPE_FZKATONG)) {
            setTextViewSelect(tv_default, false);
            setTextViewSelect(tv_qihei, false);
            setTextViewSelect(tv_fzkatong, true);
            setTextViewSelect(tv_bysong, false);
//            setTextViewSelect(tv_xinshou, false);
//            setTextViewSelect(tv_wawa, true);
        } else if (typeface.equals(Config.FONTTYPE_BYSONG)) {
            setTextViewSelect(tv_default, false);
            setTextViewSelect(tv_qihei, false);
            setTextViewSelect(tv_fzkatong, false);
            setTextViewSelect(tv_bysong, true);
//            setTextViewSelect(tv_xinshou, false);
//            setTextViewSelect(tv_wawa, true);
        }
    }

    //设置字体
    public void setTypeface(String typeface) {
        config.setTypeface(typeface);
        Typeface tface = config.getTypeface(typeface);
        if (mSettingListener != null) {
            mSettingListener.changeTypeFace(tface);
        }
    }

    //设置按钮选择的背景
    private void setTextViewSelect(TextView textView, Boolean isSelect) {
        if (isSelect) {
            textView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_select_bg));
            textView.setTextColor(getContext().getResources().getColor(R.color.read_dialog_button_select));
        } else {
            textView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_bg));
            textView.setTextColor(getContext().getResources().getColor(R.color.white));
        }
    }

    private void applyCompat() {
        if (Build.VERSION.SDK_INT < 19) {
            return;
        }
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
    }

    public Boolean isShow() {
        return isShowing();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_dark:
                break;
            case R.id.tv_bright:
                break;
            case R.id.tv_subtract:
                subtractFontSize();
                break;
            case R.id.tv_add:
                addFontSize();
                break;
            case R.id.tv_size_default:
                defaultFontSize();
                break;
            case R.id.tv_qihei:
                selectTypeface(Config.FONTTYPE_QIHEI);
                setTypeface(Config.FONTTYPE_QIHEI);
                break;
            case R.id.tv_fzkatong:
                selectTypeface(Config.FONTTYPE_FZKATONG);
                setTypeface(Config.FONTTYPE_FZKATONG);
                break;
            case R.id.tv_bysong:
                selectTypeface(Config.FONTTYPE_BYSONG);
                setTypeface(Config.FONTTYPE_BYSONG);
                break;
            case R.id.tv_default:
                selectTypeface(Config.FONTTYPE_DEFAULT);
                setTypeface(Config.FONTTYPE_DEFAULT);
                break;
        }
    }

    //变大书本字体
    private void addFontSize() {
        if (currentFontSize < FONT_SIZE_MAX) {
            currentFontSize += 1;
            tv_size.setText(currentFontSize + "");
            config.setFontSize(currentFontSize);
            if (mSettingListener != null) {
                mSettingListener.changeFontSize(currentFontSize);
            }
        }
    }

    private void defaultFontSize() {
        currentFontSize = (int) getContext().getResources().getDimension(R.dimen.reading_default_text_size);
        tv_size.setText(currentFontSize + "");
        config.setFontSize(currentFontSize);
        if (mSettingListener != null) {
            mSettingListener.changeFontSize(currentFontSize);
        }
    }

    //变小书本字体
    private void subtractFontSize() {
        if (currentFontSize > FONT_SIZE_MIN) {
            currentFontSize -= 1;
            tv_size.setText(currentFontSize + "");
            config.setFontSize(currentFontSize);
            if (mSettingListener != null) {
                mSettingListener.changeFontSize(currentFontSize);
            }
        }
    }


    public void setSettingListener(SettingListener settingListener) {
        this.mSettingListener = settingListener;
    }

    public interface SettingListener {
        void changeSystemBright(Boolean isSystem, float brightness);

        void changeFontSize(int fontSize);

        void changeTypeFace(Typeface typeface);

        void changeBookBg(int type);
    }

}