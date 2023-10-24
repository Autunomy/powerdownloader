package com.hty.core;

import com.hty.constant.Constant;
import com.hty.util.FileUtils;
import com.hty.util.HttpUtils;
import com.hty.util.LogUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.sql.Time;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * @author hty
 * @date 2023-10-24 11:47
 * @email 1156388927@qq.com
 * @description
 */

//单线程下载器
public class Downloader {

    //这个是用来打印下载信息的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    //这个是分片下载时需要的下载线程池
    public ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
            Constant.THREAD_NUM,
            Constant.THREAD_NUM,
            0,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(Constant.THREAD_NUM));
    private CountDownLatch countDownLatch = new CountDownLatch(Constant.THREAD_NUM);


    public void download(String url){
        //获取文件名
        String fileName = HttpUtils.getHttpFileName(url);

        //目的地址 + 文件名形成最终下载路径
        fileName = Constant.PATH + fileName;

        //获取本地文件大小
        long localFileLength = FileUtils.getFileContentLength(fileName);

        //获取连接
        HttpURLConnection httpURLConnection = null;

        DownloadInfoThread downloadInfoThread = null;

        try {
            httpURLConnection = HttpUtils.getHttpURLConnection(url);
            //下载的文件大小
            int contentLength = httpURLConnection.getContentLength();

            if(contentLength <= localFileLength){
                LogUtils.info("{}已下载完毕，无需重新下载",fileName);
                return;
            }

            //创建获取下载信息的任务对象
            downloadInfoThread = new DownloadInfoThread(contentLength);

            //交给线程池执行，每隔一秒执行一次
            scheduledExecutorService.scheduleWithFixedDelay(downloadInfoThread,1,1, TimeUnit.SECONDS);
//------------------------------------------------------------------------------------------------------------------
            ArrayList<Future> list = new ArrayList<>();
            //分块
            split(url,list);

            //需要等待全部文件下载完毕
            countDownLatch.await();
            if(merge(fileName)){
                //清空临时文件
                clearTemp(fileName);
            }

//------------------------------------------------------------------------------------------------------------------


//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        try(
//                //这些都不需要关闭 会自动关闭
//                InputStream inputStream = httpURLConnection.getInputStream();
//                BufferedInputStream bis = new BufferedInputStream(inputStream);
//                FileOutputStream fos = new FileOutputStream(fileName);
//                BufferedOutputStream bos = new BufferedOutputStream(fos);
//        ) {
//            int len = -1;
//            byte[] buffer = new byte[Constant.BYTE_SIZE];
//            while( (len = bis.read(buffer)) != -1){
//                downloadInfoThread.downSize += len;
//                bos.write(buffer,0,len);
//            }
//        } catch (FileNotFoundException e) {
//            LogUtils.error("下载的文件不存在{}",url);
        } catch (Exception e){
            LogUtils.error("下载失败");
        } finally {
            System.out.print("\r");
            System.out.print("下载完成");
            //关闭连接
            if(httpURLConnection != null){
                httpURLConnection.disconnect();
            }
            scheduledExecutorService.shutdownNow();
            poolExecutor.shutdown();
        }

    }

    //文件切分
    public void split(String url, ArrayList<Future> futureList){
        //获取下载文件大小
        try {
            long contentLength = HttpUtils.getHttpFileContentLength(url);
            //计算切分后的文件大小
            long size = contentLength / Constant.THREAD_NUM;


            for(int i=0;i<Constant.THREAD_NUM;++i){
                //下载起始位置
                long startPos = i * size;
                //下载结束位置
                long endPos;
                if(i == Constant.THREAD_NUM - 1){
                    endPos = 0;
                }else{
                    endPos = startPos + size;
                }

                //如果不是第一块 起始位置+1
                if(i != 0){
                    startPos ++;
                }

                DownloaderTask downloaderTask = new DownloaderTask(url, startPos, endPos, i,countDownLatch);
                Future<Boolean> future = poolExecutor.submit(downloaderTask);
                futureList.add(future);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //文件合并
    public boolean merge(String fileName){
        LogUtils.info("开始合并文件{}",fileName);
        byte[] buffer = new byte[Constant.BYTE_SIZE];
        int len = -1;

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(fileName,"rw");){
            for(int i=0;i<Constant.THREAD_NUM;++i){
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName + ".temp" + i));){
                    while((len = bis.read(buffer)) != -1){
                        randomAccessFile.write(buffer,0,len);
                    }
                }
            }
            LogUtils.info("文件合并完毕{}",fileName);
        } catch (IOException e) {
            LogUtils.error("文件合并失败");
            return false;
        }

        return true;
    }

    //删除临时文件
    public boolean clearTemp(String fileName){
        for(int i=0;i<Constant.THREAD_NUM;++i){
            File file = new File(fileName + ".temp" + i);
            file.delete();
        }
        return true;
    }
}
