package com.mivideo.mifm.download.support;

public enum DownloadState {
    WAITING(10),
    START(12),
    SUCCESS(14),
    FAILURE(18);

    public int code;

    DownloadState(int code) {
        this.code = code;
    }

    public static DownloadState parse(int code) {
        for (DownloadState ds : DownloadState.values()) {
            if (ds.code == code) {
                return ds;
            }
        }
        return null;
    }
}
