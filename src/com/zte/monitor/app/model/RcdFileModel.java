package com.zte.monitor.app.model;

import java.io.Serializable;

/**
 * Created by Sylar on 14-9-17.
 */
public class RcdFileModel  implements Serializable {

    public String fileName;
    public int status;
    public boolean isSelected;

    public static interface STATUS {
        int PLAY = 0;
        int PAUSE = 1;
        int STOP = 3;
    }
}
