package com.zte.monitor.app.model;

import java.io.Serializable;

/**
 * Created by Sylar on 8/26/14.
 */
public class KeyValuePair implements Serializable{

    public String key;
    public String value;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyValuePair)) return false;

        KeyValuePair that = (KeyValuePair) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return value;
    }
}
