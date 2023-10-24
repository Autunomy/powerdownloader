package com.hty.core;

import com.hty.constant.Constant;
import com.hty.util.LogUtils;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author hty
 * @date 2023-10-24 12:18
 * @email 1156388927@qq.com
 * @description
 */

//展示下载过程中的信息
public class DownloadInfoThread implements Runnable{

    //文件总大小
    private long httpFileContentLength;

    //已经下载完成的文件大小(这个数据是在程序执行之前就已经确定了，不会变)
    public static LongAdder finishedSize = new LongAdder();

    //当前累计下载的大小
    public static volatile LongAdder downSize = new LongAdder();

    //前一次下载的大小
    public double prevSize;

    public DownloadInfoThread(long httpFileContentLength) {
        this.httpFileContentLength = httpFileContentLength;
    }

    //线程每秒执行一次
    @Override
    public void run() {
        //计算文件总大小 单位是MB
        String httpFileSize = String.format("%.2f", httpFileContentLength / Constant.MB);

        //计算每秒下载速度 kb
        int speed = (int)((downSize.doubleValue() - prevSize) / 1024d);
        prevSize = downSize.doubleValue();
        //剩余文件大小
        double remainSize = httpFileContentLength - finishedSize.doubleValue() - downSize.doubleValue();

        //计算剩余时间
        String remainTime = String.format("%.1f",remainSize / 1024 / speed);

        if("Infinity".equalsIgnoreCase(remainTime)){
            remainTime = "-";
        }

        //已下载大小
        String currentFileSize = String.format("%.2f", (downSize.doubleValue() - finishedSize.doubleValue()) / Constant.MB);

        String downInfo = String.format("已下载 %sMB/%sMB,速度 %skb/s,剩余时间 %ss",
                currentFileSize, httpFileSize, speed, remainTime);

        System.out.print("\r");
        System.out.print(downInfo);
    }
}
