package com.app.util;

import java.io.File;
import java.nio.file.Paths;

public class FileUtil {
    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String UPLOAD_DIR = "uploads";

    public static String getUploadPath() {
        return Paths.get(PROJECT_ROOT, UPLOAD_DIR).toString();
    }

    public static String getPath(String subFolder) {
        return Paths.get(PROJECT_ROOT, UPLOAD_DIR, subFolder).toString();
    }

    public static void prepareDirectories() {
        String[] folders = {"thumbnails", "videos", "materials"};
        for (String folder : folders) {
            File dir = new File(getPath(folder));
            if (!dir.exists()) dir.mkdirs();
        }
    }
}