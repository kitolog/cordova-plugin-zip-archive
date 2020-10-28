/**
 * Copyright (c) 2019, kitolog
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by kitolog. The name of the
 * kitolog may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.hqsoftwarelab.ziparchive;

import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

public class ZipArchiveAdapterImpl implements ZipArchiveAdapter {

    private Consumer<String> zipEventHandler;
    private Consumer<Integer> progressHandler;
    private Consumer<Boolean> stopEventHandler;
    private Consumer<String> errorEventHandler;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        public void run() {
            invokeProgressHandler(2);
        }
    };

    public ZipArchiveAdapterImpl() {
    }

    private static int BUFFER_SIZE = 6 * 1024;

    @Override
    public void zip(String zipFilePath, ArrayList<String> filesList, float maxSize) throws IOException {

        if (filesList.isEmpty())
            invokeExceptionHandler("No files found");
        try {

            zipFilePath = zipFilePath.replace("file:///", "/");
            File file = new File(zipFilePath);
            if (file.exists() && file.isFile()) {
                if (file.delete()) {
                    System.out.println("Zip file DELETED :" + zipFilePath);
                } else {
                    System.out.println("Zip file not deleted :" + zipFilePath);
                }
            } else {
                System.out.println("Zip file not exists :" + zipFilePath);
            }

            File parentDir = file.getParentFile();
            if (parentDir.exists() && parentDir.isDirectory()) {
                File[] dirFiles = parentDir.listFiles();
                if ((dirFiles != null) && (dirFiles.length > 0)) {
                    System.out.println("Zip file parent dir files count :" + dirFiles.length);
                    for (int i = 0; i < dirFiles.length; i++) {
                        String fileName = dirFiles[i].getName();
                        if (dirFiles[i].isFile() && fileName.matches(".*\\.z.*")) {
                            System.out.println("Zip delete old chunks : " + fileName);
                            if (dirFiles[i].delete()) {
                                System.out.println("Zip file chunk DELETED :" + fileName);
                            } else {
                                System.out.println("Zip file chunk not deleted :" + fileName);
                            }
                        }
                    }

                }


            }

            ArrayList<File> preparedFilesList = new ArrayList<File>();
            for (String filePath : filesList) {
                filePath = filePath.replace("file:///", "/");
                preparedFilesList.add(new File(filePath));
            }

            int fileSize = Math.round(maxSize * 1024 * 1024);
            ZipFile zipFile = new ZipFile(file);
            zipFile.createSplitZipFile(preparedFilesList, new ZipParameters(), true, fileSize); // using 10MB in this example
            invokeZipEventHandler(zipFilePath);
        } catch (ZipException e) {
            e.printStackTrace();
            invokeExceptionHandler(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            invokeExceptionHandler(e.getMessage());
        }
    }

    @Override
    public void stop() {
        Log.d("ZipArchive", "stop");
    }

    @Override
    public void setZipEventHandler(Consumer<String> zipEventHandler) {
        this.zipEventHandler = zipEventHandler;
    }

    @Override
    public void setProgressHandler(Consumer<Integer> progressHandler) {
        this.progressHandler = progressHandler;
    }

    @Override
    public void setStopEventHandler(Consumer<Boolean> stopEventHandler) {
        this.stopEventHandler = stopEventHandler;
    }

    @Override
    public void setErrorEventHandler(Consumer<String> errorEventHandler) {
        this.errorEventHandler = errorEventHandler;
    }

    private void invokeZipEventHandler(String path) {
        if (this.zipEventHandler != null) {
            this.zipEventHandler.accept(path);
        }
    }

    private void invokeProgressHandler(int data) {
        if (this.progressHandler != null) {
            this.progressHandler.accept(data);
        }
    }

    private void invokeStopEventHandler(boolean hasError) {
        if (this.stopEventHandler != null) {
            this.stopEventHandler.accept(hasError);
        }
    }

    private void invokeExceptionHandler(String errorMessage) {
        if (this.errorEventHandler != null) {
            this.errorEventHandler.accept(errorMessage);
        }
    }
}
