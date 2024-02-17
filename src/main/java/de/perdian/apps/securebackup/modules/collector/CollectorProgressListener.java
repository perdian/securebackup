package de.perdian.apps.securebackup.modules.collector;

public interface CollectorProgressListener {

    void onProgress(String message, Throwable exception);

}
