package com.hty.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author hty
 * @date 2023-10-24 11:38
 * @email 1156388927@qq.com
 * @description
 */


public class HttpUtils {

    //与服务器建立连接
    public static HttpURLConnection getHttpURLConnection(String url) throws IOException {
        URL httpUrl = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) httpUrl.openConnection();

        //向服务器发送浏览器的标识信息 User-Agent的信息可以随意找一个网页并打开控制台，选取一个请求在请求头中就会有
        httpURLConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36");

        return httpURLConnection;
    }

    //获取下载文件的文件名
    public static String getHttpFileName(String url){
        return url.substring(url.lastIndexOf("/") + 1);
    }

    //分片下载时获取连接
    public static HttpURLConnection getHttpURLConnection(String url,long startPos,long endPos) throws IOException {
        HttpURLConnection httpURLConnection = getHttpURLConnection(url);
        LogUtils.info("下载的区间是:{}-{}",startPos,endPos);


        if(endPos != 0){//非最后一块 需要起始和结束位置
            httpURLConnection.setRequestProperty("RANGE","bytes="+startPos + "-" + endPos);
        }else{//当前是最后一块只需要起始位置
            httpURLConnection.setRequestProperty("RANGE","bytes="+startPos+"-");
        }

        return httpURLConnection;
    }


    //获取下载文件大小
    public static long getHttpFileContentLength(String url) throws IOException {
        int contentLength;
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = getHttpURLConnection(url);
            contentLength = httpURLConnection.getContentLength();
        }finally {
            if(httpURLConnection != null){
                httpURLConnection.disconnect();
            }
        }

        return contentLength;
    }
}
