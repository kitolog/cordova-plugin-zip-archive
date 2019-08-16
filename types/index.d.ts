interface Window {
    nativeTimer: {
        start(
            delay: number,
            interval: number,
            successCallback: (status: number) => void,
            errorCallback?: (fileError: NativeTimerError) => void): void;
        stop(
            successCallback: (status: number) => void,
            errorCallback?: (fileError: NativeTimerError) => void): void;
        checkState(
            state: string,
            errorCallback?: (fileError: NativeTimerError) => void): boolean;
        onTick(): void;
        onStop(): void;
        onError(): void;
        on(eventName: string,
           callback?: (data: any) => void): void;
    }
}

interface NativeTimerError {
    /** Error code */
    code: number;
}