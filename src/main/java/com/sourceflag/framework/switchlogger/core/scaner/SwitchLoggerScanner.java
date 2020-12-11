package com.sourceflag.framework.switchlogger.core.scaner;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public interface SwitchLoggerScanner {

    default void scan(String projectPath, String[] compilePaths) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        if (url != null) {
            if ("file".equals(url.getProtocol())) {
                doScan(new File(projectPath + "/"), compilePaths);
            } else if ("jar".equals(url.getProtocol())) {
                try {
                    JarURLConnection connection = (JarURLConnection) url.openConnection();
                    JarFile jarFile = connection.getJarFile();
                    Enumeration<JarEntry> jarEntries = jarFile.entries();
                    while (jarEntries.hasMoreElements()) {
                        JarEntry jar = jarEntries.nextElement();
                        if (jar.isDirectory() || !jar.getName().endsWith(".class")) {
                            continue;
                        }
                        String jarName = jar.getName();
                        String classPath = jarName.replaceAll("/", ".");
                        String className = classPath.substring(0, classPath.lastIndexOf("."));
                        processClassFile(className);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    default void doScan(File file, String[] compilePaths) {
        if (file.isDirectory()) {
            for (File _file : Objects.requireNonNull(file.listFiles())) {
                doScan(_file, compilePaths);
            }
        } else {
            String filePath = file.getPath();
            int index = filePath.lastIndexOf(".");
            if (index != -1 && ".class".equals(filePath.substring(index))) {
                filePath = extractLegalFilePath(filePath, compilePaths);
                if (filePath != null) {
                    String classPath = filePath.replaceAll("\\\\", ".");
                    String className = classPath.substring(0, classPath.lastIndexOf("."));
                    processClassFile(className);
                }
            }
        }
    }

    default String extractLegalFilePath(String filePath, String[] compilePaths) {
        for (String compilePath : compilePaths) {
            int index = filePath.indexOf(compilePath);
            if (index != -1) {
                return filePath.substring(index + compilePath.length() + 1);
            }
        }
        return null;
    }

    void processClassFile(String className);


}
