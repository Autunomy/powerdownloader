package com.hty.core;

import com.hty.constant.Constant;
import com.hty.util.HttpUtils;
import com.hty.util.LogUtils;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * @author hty
 * @date 2023-10-24 14:22
 * @email 1156388927@qq.com
 * @description
 */

//分块下载
public class DownloaderTask implements Callable<Boolean> {

    //下载链接
    private String url;
    //起始位置
    private long startPos;
    //结束位置
    private long endPos;
    //块号 作为临时文件的文件名
    private int part;

    private CountDownLatch countDownLatch;

    public DownloaderTask(String url, long startPos, long endPos, int part,CountDownLatch countDownLatch) {
        this.url = url;
        this.startPos = startPos;
        this.endPos = endPos;
        this.part = part;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public Boolean call() throws Exception {
        //获取文件名
        String httpFileName = HttpUtils.getHttpFileName(url);
        //分块的文件名
        httpFileName = httpFileName + ".temp" + part;
        //下载路径
        httpFileName = Constant.PATH + httpFileName;

        //获取分块下载链接
        HttpURLConnection httpURLConnection = HttpUtils.getHttpURLConnection(url, startPos, endPos);

        try (
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                RandomAccessFile randomAccessFile = new RandomAccessFile(httpFileName,"rw");
                ){
            byte[] buffer = new byte[Constant.BYTE_SIZE];
            int len = -1;

            while((len = bis.read(buffer)) != -1){
                DownloadInfoThread.downSize.add(len);
                randomAccessFile.write(buffer,0,len);
            }
        }catch (FileNotFoundException e){
            LogUtils.error("文件不存在{}",url);
            return false;
        }catch (Exception e){
            LogUtils.error("下载失败");
            return false;
        }finally {
            httpURLConnection.disconnect();
            countDownLatch.countDown();
        }
        return true;
    }
}
