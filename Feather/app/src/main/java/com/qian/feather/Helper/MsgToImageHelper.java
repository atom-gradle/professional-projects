package com.qian.feather.Helper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Typeface;

import com.qian.feather.item.Msg;
import com.qian.feather.Utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class MsgToImageHelper {
    public static Bitmap convertMsgListToImage(List<Msg> msgList) {
        int width = Utils.screenWidth; // 设置图片宽度
        int lineHeight = 60; // 每行高度
        int padding = 20; // 边距
        int height = 200+(msgList.size() << 8);//估算高度
        // 创建Bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        // 设置背景色
        canvas.drawColor(Color.parseColor("#E3F2FD"));
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(32);
        // 标题
        Msg msg0 = msgList.get(0);
        String whenCreated = LocalDate.now().toString()+LocalTime.now().toString().substring(0,8);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText(msg0.getFrom().trim()+" 和 "+msg0.getTo().trim()+" 的聊天记录，导出时间："+whenCreated, padding, padding + 32, paint);
        paint.setTypeface(Typeface.DEFAULT);
        int y = padding + 60;
        y += lineHeight;

        paint.setColor(Color.BLACK);
        canvas.drawText("---start---", padding, y, paint);
        y += lineHeight;
        // 绘制每条消息
        int size = msgList.size();
        for (int i = 0;i < size;i++) {
            Msg msg = msgList.get(i);
            //绘制from和to
            paint.setColor(Color.BLACK);
            canvas.drawText(msg.getFrom().trim()+" -> "+msg.getTo().trim(), padding, y, paint);
            y += lineHeight;
            //绘制when
            canvas.drawText("时间: " + msg.getWhen(), padding, y, paint);
            y += lineHeight;
            //绘制content 处理长文本换行
            String content = msg.getContent();
            int maxLineWidth = width - 2 * padding;
            List<String> lines = splitTextToLines(paint, content, maxLineWidth);
            for (String line : lines) {
                canvas.drawText(line, padding, y, paint);
                y += lineHeight;
            }
            // 添加分隔线
            paint.setColor(Color.LTGRAY);
            canvas.drawLine(padding, y, width - padding, y, paint);
            y += lineHeight;
        }
        paint.setColor(Color.BLACK);
        canvas.drawText("---end---", padding, y, paint);
        return bitmap;
    }

    private static List<String> splitTextToLines(Paint paint, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }

        int start = 0;
        int end = 1;
        while (end <= text.length()) {
            String substring = text.substring(start, end);
            float width = paint.measureText(substring);
            if (width > maxWidth) {
                lines.add(text.substring(start, end - 1));
                start = end - 1;
            }
            end++;
        }
        // 添加最后一行
        if (start < text.length()) {
            lines.add(text.substring(start));
        }

        return lines;
    }
}