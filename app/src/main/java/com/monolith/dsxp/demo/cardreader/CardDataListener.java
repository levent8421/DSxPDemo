package com.monolith.dsxp.demo.cardreader;


public interface CardDataListener {
    /**
     * 通知读到的原始数据
     * @param dataType 数据类型
     * @param data 字节数据
     */
    void onOriginalData(int dataType, byte[] data);

    /**
     * 通知读到的字符串数据
     * @param dataType 数据类型
     * @param data 字符串数据
     */
    void onStringData(int dataType, String data);

    /**
     * 通知错误信息
     * @param msg 错误信息
     */
    void onError(String msg);
}
