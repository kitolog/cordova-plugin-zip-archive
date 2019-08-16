/**
 * Copyright (c) 2018, kitolog
 * All rights reserved.
 *
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

var exec = require('cordova/exec');
var PLUGIN_EVENT = "ZIP_ARCHIVE_EVENT";
var CORDOVA_SERVICE_NAME = "ZipArchive";

ZipArchive.State = {};
ZipArchive.State[ZipArchive.State.STARTED = 1] = "STARTED";
ZipArchive.State[ZipArchive.State.STARTING = 2] = "STARTING";
ZipArchive.State[ZipArchive.State.STOPPING = 3] = "STOPPING";
ZipArchive.State[ZipArchive.State.STOPPED = 0] = "STOPPED";

ZipArchive.ErrorType = {};
ZipArchive.ErrorType[ZipArchive.ErrorType.GENERAL = 0] = "general";

function ZipArchive() {
    this._state = ZipArchive.State.STOPPED;
    this.timerKey = guid();
    this.onProgress = function () {
        console.log('On zip progress');
    };
    this.onFinish = function () {
        console.log('On zip finish');
    };
    this.onError = function (e) {
        console.error('On zip error');
        console.error(e);
    };

    this.on = function (eventName, callback) {
        if ((typeof eventName === 'string') && (typeof callback === 'function')) {
            switch (eventName) {
                case 'progress':
                    this.onProgress = function (e) {
                        callback(e);
                    };
                    break;
                case 'finish':
                    this.onFinish = function (e) {
                        callback(e);
                    };
                    break;
                case 'error':
                    this.onError = function (e) {
                        callback(e);
                    };
                    break;
            }
        }

        return this;
    };
}

ZipArchive.prototype.zip = function (path, files, success, error) {
    success = success || function () {
    };
    error = error || function () {
    };

    if ((typeof path === 'undefined') || path === null) {
        console.warn('[ZIP] PATH IS EMPTY');
        return;
    }

    if ((typeof files === 'undefined') || files === null) {
        console.warn('[ZIP] FILES ARE EMPTY');
        return;
    }

    // if (!this.checkState(ZipArchive.State.STOPPED, error)) {
    //     console.warn('STATE ');
    //     return;
    // }

    var _that = this;

    function timerEventHandler(event) {
        var payload = event.payload;
        if (payload.timerKey !== _that.timerKey) {
            return;
        }

        switch (payload.type) {
            case "OnFinish":
                _that._state = ZipArchive.State.STOPPED;
                window.document.removeEventListener(PLUGIN_EVENT, timerEventHandler);
                _that.onFinish(payload.hasError);
                break;
            case "OnProgress":
                _that.onProgress(payload.data);
                break;
            case "OnError":
                _that.onError(payload);
                break;
            default:
                console.error("ZipArchive: Unknown event type " + payload.type + ", timer key: " + payload.timerKey);
                break;
        }
    }

    _that._state = ZipArchive.State.STARTING;

    exec(
        function () {
            _that._state = ZipArchive.State.STARTED;
            window.document.addEventListener(PLUGIN_EVENT, timerEventHandler);
            success();
        },
        function (errorMessage) {
            _that._state = ZipArchive.State.STOPPED;
            error(errorMessage);
        },
        CORDOVA_SERVICE_NAME,
        "zip",
        [
            this.timerKey,
            path,
            files
        ]);
};

ZipArchive.prototype.stop = function (success, error) {

    success = success || function () {
    };
    error = error || function () {
    };

    if (!this.checkState(ZipArchive.State.STARTED, error)) {
        return;
    }

    this._state = ZipArchive.State.STOPPING;

    exec(
        success,
        error,
        CORDOVA_SERVICE_NAME,
        "stop",
        [this.timerKey]);
};

Object.defineProperty(ZipArchive.prototype, "state", {
    get: function () {
        return this._state;
    },
    enumerable: true,
    configurable: true
});

ZipArchive.prototype.checkState = function (requiredState, errorCallback) {
    var state = this._state;
    if (state != requiredState) {
        errorCallback = errorCallback || function () {
        };
        window.setTimeout(function () {
            errorCallback("Invalid operation for this timer state: " + ZipArchive.State[state]);
        });
        return false;
    }
    else {
        return true;
    }
};

ZipArchive.dispatchEvent = function (event) {
    var eventReceive = document.createEvent('Events');
    eventReceive.initEvent(PLUGIN_EVENT, true, true);
    eventReceive.payload = event;

    document.dispatchEvent(eventReceive);
};

var guid = (function () {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }

    return function () {
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
    };
})();

module.exports = ZipArchive;