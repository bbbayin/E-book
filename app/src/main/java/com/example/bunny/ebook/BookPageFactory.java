package com.example.bunny.ebook;

/**
 * Created by Administrator on 2017/11/10.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Vector;

public class BookPageFactory {

    private File book_file = null;
    private MappedByteBuffer m_mbBuf = null;
    private int m_mbBufLen = 0;
    private int m_mbBufBegin = 0;
    private int m_mbBufEnd = 0;
    private String m_strCharsetName = "GBK";
    private Bitmap m_book_bg = null;
    private int mWidth;
    private int mHeight;

    private Vector<String> m_lines = new Vector<String>();

    private int m_fontSize = 25;
    private int textSpace = 8;
    private int m_textColor = Color.BLACK;
    private int m_backColor = 0xffff9e85; // 背景颜色
    private int marginWidth = 50; // 左右与边缘的距离
    private int marginHeight = 30; // 上下与边缘的距离
    private int mLineCount; // 每页可以显示的行数
    private float mVisibleHeight; // 绘制内容的宽
    private boolean m_isfirstPage, m_islastPage;

    private Paint mPaint;
    private final Paint linePaint;
    private final float mLineWidth;

    public BookPageFactory(int w, int h) {
        mWidth = w;
        mHeight = h;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextAlign(Align.LEFT);//设置绘制文字的对齐方向
        mPaint.setTextSize(m_fontSize);
        mPaint.setColor(m_textColor);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(1);
        linePaint.setColor(Color.BLACK);

        mLineWidth = (mWidth - marginWidth * 4) / 2;
        mVisibleHeight = mHeight - marginHeight * 2;
        mLineCount = (int) (mVisibleHeight / (textSpace + m_fontSize)) * 2; // 可显示的行数
        MyLog.d("行数:" + mLineCount);
    }

    /**
     * 设置字体格式
     *
     * @param typeFace
     */
    public void setTypeFace(Typeface typeFace) {
        mPaint.setTypeface(typeFace);
    }

    public void openbook(String strFilePath) throws IOException {
        book_file = new File(strFilePath);
        long lLen = book_file.length();
        m_mbBufLen = (int) lLen;

        /*
         * 内存映射文件能让你创建和修改那些因为太大而无法放入内存的文件。有了内存映射文件，你就可以认为文件已经全部读进了内存，
         * 然后把它当成一个非常大的数组来访问。这种解决办法能大大简化修改文件的代码。
         *
        * fileChannel.map(FileChannel.MapMode mode, long position, long size)将此通道的文件区域直接映射到内存中。但是，你必
        * 须指明，它是从文件的哪个位置开始映射的，映射的范围又有多大
        */
        FileChannel fc = new RandomAccessFile(book_file, "r").getChannel();

        //文件通道的可读可写要建立在文件流本身可读写的基础之上
        m_mbBuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, lLen);
    }

    public void setTextSize(int textSize,Canvas canvas) {
        m_fontSize = textSize;
        mPaint.setTextSize(m_fontSize);
        mLineCount = (int) (mVisibleHeight / (textSpace + m_fontSize)) * 2; // 可显示的行数
    }


    protected byte[] readParagraphBack(int nFromPos) {
        int nEnd = nFromPos;
        int i;
        byte b0, b1;
        if (m_strCharsetName.equals("UTF-16LE")) {
            i = nEnd - 2;
            while (i > 0) {
                b0 = m_mbBuf.get(i);
                b1 = m_mbBuf.get(i + 1);
                if (b0 == 0x0a && b1 == 0x00 && i != nEnd - 2) {
                    i += 2;
                    break;
                }
                i--;
            }

        } else if (m_strCharsetName.equals("UTF-16BE")) {
            i = nEnd - 2;
            while (i > 0) {
                b0 = m_mbBuf.get(i);
                b1 = m_mbBuf.get(i + 1);
                if (b0 == 0x00 && b1 == 0x0a && i != nEnd - 2) {
                    i += 2;
                    break;
                }
                i--;
            }
        } else {
            i = nEnd - 1;
            while (i > 0) {
                b0 = m_mbBuf.get(i);
                if (b0 == 0x0a && i != nEnd - 1) {
                    i++;
                    break;
                }
                i--;
            }
        }
        if (i < 0)
            i = 0;
        int nParaSize = nEnd - i;
        int j;
        byte[] buf = new byte[nParaSize];
        for (j = 0; j < nParaSize; j++) {
            buf[j] = m_mbBuf.get(i + j);
        }
        return buf;
    }


    //读取上一段落
    protected byte[] readParagraphForward(int nFromPos) {
        int nStart = nFromPos;
        int i = nStart;
        byte b0, b1;
        // 根据编码格式判断换行
        if (m_strCharsetName.equals("UTF-16LE")) {
            while (i < m_mbBufLen - 1) {
                b0 = m_mbBuf.get(i++);
                b1 = m_mbBuf.get(i++);
                if (b0 == 0x0a && b1 == 0x00) {
                    break;
                }
            }
        } else if (m_strCharsetName.equals("UTF-16BE")) {
            while (i < m_mbBufLen - 1) {
                b0 = m_mbBuf.get(i++);
                b1 = m_mbBuf.get(i++);
                if (b0 == 0x00 && b1 == 0x0a) {
                    break;
                }
            }
        } else {
            while (i < m_mbBufLen) {
                b0 = m_mbBuf.get(i++);
                if (b0 == 0x0a) {
                    break;
                }
            }
        }
        //共读取了多少字符
        int nParaSize = i - nStart;
        byte[] buf = new byte[nParaSize];
        for (i = 0; i < nParaSize; i++) {
            //将已读取的字符放入数组
            buf[i] = m_mbBuf.get(nFromPos + i);
        }
        return buf;
    }

    protected Vector<String> pageDown() {
        synchronized (BookPageFactory.class){
            String strParagraph = "";
            Vector<String> lines = new Vector<String>();
            while (lines.size() < mLineCount && m_mbBufEnd < m_mbBufLen) {
                byte[] paraBuf = readParagraphForward(m_mbBufEnd); // 读取一个段落
                m_mbBufEnd += paraBuf.length;//结束位置后移paraBuf.length
                try {
                    strParagraph = new String(paraBuf, m_strCharsetName);//通过decode指定的编码格式将byte[]转换为字符串
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String strReturn = "";

                //去除将字符串中的特殊字符
                if (strParagraph.indexOf("\r\n") != -1) {
                    strReturn = "\r\n";
                    strParagraph = strParagraph.replaceAll("\r\n", "");
                } else if (strParagraph.indexOf("\n") != -1) {
                    strReturn = "\n";
                    strParagraph = strParagraph.replaceAll("\n", "");
                }

                if (strParagraph.length() == 0) {
                    lines.add(strParagraph);
                }
                //读取完毕的原始数据
                MyLog.d("strParagraph = " + strParagraph);
                while (strParagraph.length() > 0) {
                    //计算每行可以显示多少个字符
                    //获益匪浅
                    int nSize = mPaint.breakText(strParagraph, true, mLineWidth, null);
//                int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth / 2, null);
                    lines.add(strParagraph.substring(0, nSize));
                    strParagraph = strParagraph.substring(nSize);//截取从nSize开始的字符串
                    if (lines.size() >= mLineCount) {
                        break;
                    }
                }
                MyLog.d("截取结束文字行数：" + lines.size());
                //当前页没显示完
                if (strParagraph.length() != 0) {
                    try {
                        m_mbBufEnd -= (strParagraph + strReturn)
                                .getBytes(m_strCharsetName).length;
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return lines;
        }
    }

    protected void pageUp() {
        synchronized (BookPageFactory.class){
            if (m_mbBufBegin < 0)
                m_mbBufBegin = 0;
            Vector<String> lines = new Vector<String>();
            String strParagraph = "";
            while (lines.size() < mLineCount && m_mbBufBegin > 0) {
                Vector<String> paraLines = new Vector<String>();
                byte[] paraBuf = readParagraphBack(m_mbBufBegin);
                m_mbBufBegin -= paraBuf.length;
                try {
                    strParagraph = new String(paraBuf, m_strCharsetName);
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                strParagraph = strParagraph.replaceAll("\r\n", "");
                strParagraph = strParagraph.replaceAll("\n", "");

                if (strParagraph.length() == 0) {
                    paraLines.add(strParagraph);
                }
                while (strParagraph.length() > 0) {
                    // TODO: 2017/11/10 修改了
                    int nSize = mPaint.breakText(strParagraph, true, mLineWidth, null);
                    paraLines.add(strParagraph.substring(0, nSize));
                    strParagraph = strParagraph.substring(nSize);
                }
                lines.addAll(0, paraLines);
            }
            while (lines.size() > mLineCount) {
                try {
                    m_mbBufBegin += lines.get(0).getBytes(m_strCharsetName).length;
                    lines.remove(0);
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            m_mbBufEnd = m_mbBufBegin;
            return;
        }
    }

    protected void prePage() throws IOException {
        if (m_mbBufBegin <= 0) {
            //第一页
            m_mbBufBegin = 0;
            m_isfirstPage = true;
            return;
        } else m_isfirstPage = false;
        m_lines.clear();//Removes all elements from this vector, leaving it empty.
        pageUp();
        m_lines = pageDown();
    }

    public void nextPage() throws IOException {
        if (m_mbBufEnd >= m_mbBufLen) {
            m_islastPage = true;
            return;
        } else m_islastPage = false;
        m_lines.clear();
        m_mbBufBegin = m_mbBufEnd;
        m_lines = pageDown();
    }

    public void onDraw(Canvas c) {
        if (m_lines.size() == 0)
            m_lines = pageDown();
        if (m_lines.size() > 0) {
            if (m_book_bg == null)
                c.drawColor(m_backColor);
            else
                c.drawBitmap(m_book_bg, 0, 0, null);
            //分割线
            c.drawLine(mWidth / 2, 0, mWidth / 2, mHeight, linePaint);

            //画字
            int y_left = marginHeight;
            int y_right = marginHeight;
            for (int i = 0; i < m_lines.size(); i++) {
                if (i < mLineCount / 2) {
                    String strLine = m_lines.elementAt(i);
                    y_left += (m_fontSize + textSpace);
                    //从（x,y）坐标将文字绘于手机屏幕
                    c.drawText(strLine, 0, strLine.length(), marginWidth, y_left, mPaint);
                } else {
                    String strLine = m_lines.elementAt(i);
                    y_right += (m_fontSize + textSpace);
                    //从（x,y）坐标将文字绘于手机屏幕
                    c.drawText(strLine, 0, strLine.length(), mWidth / 2 + marginWidth, y_right, mPaint);
                }
            }

        }
//        //计算百分比（不包括当前页）并格式化
//        float fPercent = (float) (m_mbBufBegin * 1.0 / m_mbBufLen);
//        DecimalFormat df = new DecimalFormat("#0.0");
//        String strPercent = df.format(fPercent * 100) + "%";
//
//        //计算999.9%所占的像素宽度
//        int nPercentWidth = (int) mPaint.measureText("999.9%") + 1;
//        c.drawText(strPercent, mWidth - nPercentWidth, mHeight - 5, mPaint);
    }

    public void setBgBitmap(Bitmap BG) {
        m_book_bg = zoomImg(BG, mWidth, mHeight);
    }

    public boolean isfirstPage() {
        return m_isfirstPage;
    }

    public boolean islastPage() {
        return m_islastPage;
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
        // 得到新的图片   www.2cto.com
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }
}
