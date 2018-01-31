package com.example.mylibrary;

public class UsbManagerNotInitedException extends RuntimeException {
    private static final long serialVersionUID = 10203040506070L;

    public UsbManagerNotInitedException(String detailMessage) {
        super(detailMessage);
    }

    public UsbManagerNotInitedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UsbManagerNotInitedException(Throwable throwable) {
        super(throwable);
    }
}
