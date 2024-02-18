package de.perdian.apps.securebackup.modules.collector;

public enum CollectorJobStatus {

    NEW(null, "New"),
    RUNNING(null, "Running"),
    CANCELLED("#FFF59D", "Cancelled"),                  // GREEN 200),
    FINISHED_SUCCESS("#A5D6A7", "Finished // Success"), // YELLOW
    FINISHED_ERROR("#EF9A9A", "Finished // Error");     // RED 200

    private String color = null;
    private String title = null;

    CollectorJobStatus(String color, String title) {
        this.setColor(color);
        this.setTitle(title);
    }

    public String getColor() {
        return this.color;
    }
    private void setColor(String color) {
        this.color = color;
    }

    public String getTitle() {
        return this.title;
    }
    private void setTitle(String title) {
        this.title = title;
    }

}
