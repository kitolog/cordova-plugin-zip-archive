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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ZipArchivePlugin extends CordovaPlugin {

    Map<String, ZipArchiveAdapter> zipArchiveAdapters = new HashMap<String, ZipArchiveAdapter>();

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("zip")) {
            this.zip(args, callbackContext);
        } else if (action.equals("stop")) {
            this.stop(args, callbackContext);
        } else {
            callbackContext.error(String.format("ZipArchivePlugin - invalid action:", action));
            return false;
        }
        return true;
    }

    private void zip(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        String adapterKey = args.getString(0);
        String zipFile = args.getString(1);
        JSONArray jsonArray = args.getJSONArray(2);
        ArrayList<String> filesArray = new ArrayList<String>();
        for (int i = 0, count = jsonArray.length(); i < count; i++) {
            try {
                String pathString = jsonArray.getString(i);
                filesArray.add(pathString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        ZipArchiveAdapter zipArchiveAdapter = new ZipArchiveAdapterImpl();
        //zipArchiveAdapter.setStopEventHandler(new StopEventHandler(adapterKey));
        //zipArchiveAdapter.setTickHandler(new TickConsumer(adapterKey));
        zipArchiveAdapter.setErrorEventHandler(new ErrorEventHandler(adapterKey));
        zipArchiveAdapter.setZipEventHandler(new ZipEventHandler(adapterKey, zipArchiveAdapter, callbackContext));
        try {
            zipArchiveAdapter.zip(zipFile, filesArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stop(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        String adapterKey = args.getString(0);
        ZipArchiveAdapter zipArchiveAdapter = this.getZipArchiveAdapter(adapterKey);
        zipArchiveAdapter.stop();
        callbackContext.success();
    }

    private ZipArchiveAdapter getZipArchiveAdapter(String adapterKey) {
        if (!this.zipArchiveAdapters.containsKey(adapterKey)) {
            throw new IllegalStateException("ZipArchive isn't connected.");
        }
        return this.zipArchiveAdapters.get(adapterKey);
    }

    private void dispatchEvent(JSONObject jsonEventObject) {
        this.webView.sendJavascript(String.format("window.zipArchive.dispatchEvent(%s);", jsonEventObject.toString()));
    }

    private class StopEventHandler implements Consumer<Boolean> {
        private String adapterKey;

        public StopEventHandler(String adapterKey) {
            this.adapterKey = adapterKey;
        }

        @Override
        public void accept(Boolean hasError) {
            zipArchiveAdapters.remove(this.adapterKey);

            try {
                JSONObject event = new JSONObject();
                event.put("type", "OnStop");
                event.put("hasError", hasError.booleanValue());
                event.put("adapterKey", this.adapterKey);

                dispatchEvent(event);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class TickConsumer implements Consumer<Integer> {
        private String adapterKey;

        public TickConsumer(String adapterKey) {
            this.adapterKey = adapterKey;
        }

        @Override
        public void accept(Integer tick) {
            try {
                JSONObject event = new JSONObject();
                event.put("type", "OnTick");
                event.put("data", tick);
                event.put("adapterKey", adapterKey);

                dispatchEvent(event);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ErrorEventHandler implements Consumer<String> {
        private String adapterKey;

        public ErrorEventHandler(String adapterKey) {
            this.adapterKey = adapterKey;
        }

        @Override
        public void accept(String errorMessage) {
            try {
                JSONObject event = new JSONObject();
                event.put("type", "OnError");
                event.put("errorMessage", errorMessage);
                event.put("adapterKey", adapterKey);

                dispatchEvent(event);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ZipEventHandler implements Consumer<String> {
        private String adapterKey;
        private ZipArchiveAdapter zipArchiveAdapter;
        private CallbackContext openCallbackContext;

        public ZipEventHandler(String adapterKey, ZipArchiveAdapter zipArchiveAdapter, CallbackContext openCallbackContext) {
            this.adapterKey = adapterKey;
            this.zipArchiveAdapter = zipArchiveAdapter;
            this.openCallbackContext = openCallbackContext;
        }

        @Override
        public void accept(String path) {
            zipArchiveAdapters.put(adapterKey, zipArchiveAdapter);
            this.openCallbackContext.success(path);
        }
    }
}
