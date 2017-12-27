package com.wildrune.runegear.widgets.util;

/**
 * @author Mark van der Wal
 * @web www.markvanderwal.nl
 * @since 08/08/17
 */
public interface WindowWorker {
    enum WindowState {
        ICONIFIED, RESTORED, MAXIMIZED
    }
    void minimize();
    void maximize();
    void restore();
    void setPosition(int x, int y);
    void center();
    int getPositionX();
    int getPositionY();
    WindowState getWindowState();
}
