package com.monolith.dsxp.demo.cardreader;

import com.monolith.dsxp.driver.conn.ConnectionException;

public interface CardReaderWrapper {
    /**
     * 打开读卡器
     * @throws ConnectionException
     */
    void open() throws ConnectionException;

    /**
     * 关闭读卡器
     * @throws ConnectionException
     */
    void close() throws ConnectionException;

    /**
     * 是否已打开
     * @return
     */
    boolean isOpened();

    /**
     * 开始读卡
     */
    void startRead();

    /**
     * 设置监控器
     * @param listener
     */
    void setListener(CardDataListener listener);
}
