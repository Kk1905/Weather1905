package com.cocolee.weather1905.util;

/**
 * Created by Administrator on 2016/5/29.
 */

public interface HttpCallbackListener {
    public abstract void onFinish(String response);
    public abstract void onError(Exception e);
}
