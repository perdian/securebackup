package de.perdian.apps.securebackup.modules.collector;

public interface CollectorJobProgressListener {

    void onProgress(Double progress, String message, Throwable exception);

}
