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
    public void zip(String zipFile, ArrayList<String> filesList) throws IOException {

        FileOutputStream fileOutputStream = null;
        ZipOutputStream zipOutputStream = null;
        FileInputStream fileInputStream = null;
        if (filesList.isEmpty())
            invokeExceptionHandler("No files found");
        try {

            zipFile = zipFile.replace("file:///", "/");

            File file = new File(zipFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file, false);
            zipOutputStream = new ZipOutputStream(fileOutputStream);
            for (String filePath : filesList) {
                filePath = filePath.replace("file:///", "/");
                fileInputStream = new FileInputStream(filePath);
                ZipEntry zipEntry = new ZipEntry(filePath.substring(filePath.lastIndexOf("/") + 1));
                zipOutputStream.putNextEntry(zipEntry);
                byte[] tmp = new byte[BUFFER_SIZE];
                int size = 0;
                while ((size = fileInputStream.read(tmp)) != -1) {
                    zipOutputStream.write(tmp, 0, size);
                }
                zipOutputStream.flush();
                fileInputStream.close();
            }
            zipOutputStream.close();
            fileOutputStream.close();
            invokeZipEventHandler(zipFile);
        } catch (FileNotFoundException e) {
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
