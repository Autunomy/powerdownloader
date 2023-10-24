package com.hty;

import com.hty.core.Downloader;
import com.hty.util.LogUtils;

import java.util.Scanner;

/**
 * @author hty
 * @date 2023-10-24 11:32
 * @email 1156388927@qq.com
 * @description
 */

public class Main {
    public static void main(String[] args) {
        String url = null;

        //如果没有在使用java命令的过程中传入就需要用户重新输入
        if(args == null || args.length == 0){
            while(true){
                LogUtils.info("请输入下载链接");
                Scanner sc = new Scanner(System.in);
                url = sc.next();
                if(url != null){
                    break;
                }
            }
        }else{
            url = args[0];
        }

        Downloader downloader = new Downloader();
        downloader.download(url);
    }
}
