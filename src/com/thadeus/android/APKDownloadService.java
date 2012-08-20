package com.thadeus.android;

import com.google.android.vending.expansion.downloader.impl.DownloaderService;

public class APKDownloadService extends DownloaderService{

 // You must use the public key belonging to your publisher account
    public static final String BASE64_PUBLIC_KEY = "YourLVLKey";
    // You should also modify this salt
    public static final byte[] SALT = new byte[] { 4, 2, -18, 21, -94, 98,
            -90, -2, 3, 72, -87, -54, 15, 57, -6, -128, -33, 45, -1, 84
    };

    @Override
    public String getPublicKey() {
        return BASE64_PUBLIC_KEY;
    }

    @Override
    public byte[] getSALT() {
        return SALT;
    }

    @Override
    public String getAlarmReceiverClassName() {
        return APKDownloadAlarmReceiver.class.getName();
    }

}
