package com.mivideo.mifm.data.models.jsondata.download;

import com.mivideo.mifm.data.models.AudioInfo;
import com.mivideo.mifm.data.models.jsondata.common.CommonVideo;

import java.io.Serializable;

public class DownloadInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    public boolean canTryStartDownload = false;
    public boolean pushIntoDownloadQueue = false;
    public boolean alreadyInQueue = false;
    public String key = null;
    public String url = null;
    public String dir = null;
    public String path = null;

    public String msg = null;
    public String hint = null;
    public String title = null;

    public int definitionCode = 0;

    public CommonVideo commonVideo = null;

    public AudioInfo audioInfo = null;

    public boolean jumpTheQueue = false; // 是否插队
}