package com.hty.util;

import java.io.File;

/**
 * @author hty
 * @date 2023-10-24 12:40
 * @email 1156388927@qq.com
 * @description
 */

public class FileUtils {
    public static long getFileContentLength(String path){
        File file = new File(path);
        return file.exists() && file.isFile() ? file.length() : 0;
    }
}
