package com.monolith.dsxp.demo.cardreader.iccard;

import com.monolith.dsxp.demo.cardreader.CardDataListener;
import com.monolith.dsxp.demo.cardreader.CardReaderWrapper;
import com.monolith.dsxp.demo.serial.Ds2pSerialPortWrapper;
import com.monolith.dsxp.driver.conn.ConnectionException;
import com.monolith.dsxp.driver.conn.serial.SerialWrapper;
import com.monolith.dsxp.driver.util.ByteUtils;

import java.lang.reflect.Array;
import java.util.Date;

public class JTICCardReaderWrapper implements CardReaderWrapper {
    SerialWrapper serialPort;
    boolean opened;
    boolean reading;
    Thread readThread;
    CardDataListener listener;
    int readTimeout = 1000; // ms

    public static final int STEP_WAIT_HEAD1 = 0;
    public static final int STEP_WAIT_HEAD2 = 1;
    public static final int STEP_WAIT_CMD = 2;
    public static final int STEP_WAIT_SEQ = 3;
    public static final int STEP_WAIT_LENGTH_L = 4;
    public static final int STEP_WAIT_LENGTH_H = 5;
    public static final int STEP_WAIT_DATA = 6;
    public static final int STEP_WAIT_CHECKSUM = 7;

    public static final int DATA_TYPE_EPC = 0;

    //////////////////////////////////////////
    // head     cmd   seq   len    deviceSn  data                             checksum
    // 2        1     1     2       2        n                                 1
    // 43 4D    02    03    0D 00   6D 00    0B 08 00 E0 04 01 50 EF 6C 35 C4  A9
    // 43 4D    02    03    09 00   6D 00    0B 04 00 FE 0A 0E AB              33
    //////////////////////////////////////////
    //    type   epcLen  instructions     epc
    //     1        1        1            x
    //     0B       08       00           E0 04 01 50 EF 6C 35 C4
    //     0B       04       00           FE 0A 0E AB
    //////////////////////////////////////////

    public JTICCardReaderWrapper(SerialWrapper serialPort) {
        this.serialPort = serialPort;
    }

    @Override
    public void open() throws ConnectionException {
        if (!opened) {
            serialPort.open();
            opened = true;
        }
    }

    @Override
    public void close() throws ConnectionException {
        try {
            reading = false;
            if (opened) {
                serialPort.close();
            }
        } finally {
            opened = false;
        }
    }

    @Override
    public boolean isOpened() {
        return opened;
    }

    @Override
    public void startRead() {
        if (reading) return;

        if (serialPort != null) {
            readThread = new Thread(() ->
            {
                try {
                    byte[] buf = new byte[1];
                    byte[] data = new byte[128];
                    int len = 0;
                    int receivedLen = 0;
                    byte bt;
                    int cs;
                    int step = 0;
                    long startTime = System.currentTimeMillis();
                    while (opened && reading) {
                        serialPort.read(buf, 0, 1);
                        bt = buf[0];
                        if (System.currentTimeMillis() - startTime >= readTimeout)
                            step = STEP_WAIT_HEAD1;
                        startTime = System.currentTimeMillis();
                        switch (step) {
                            default:
                            case STEP_WAIT_HEAD1: {
                                if (bt == 0x43)
                                    step = STEP_WAIT_HEAD2;
                                break;
                            }
                            case STEP_WAIT_HEAD2: {
                                if (bt == 0x4d)
                                    step = STEP_WAIT_CMD;
                                else
                                    step = STEP_WAIT_HEAD1;
                                break;
                            }
                            case STEP_WAIT_CMD: {
                                step = STEP_WAIT_SEQ;
                                // ignore
                                break;
                            }
                            case STEP_WAIT_SEQ: {
                                step = STEP_WAIT_LENGTH_L;
                                // ignore
                                break;
                            }
                            case STEP_WAIT_LENGTH_L: {
                                len = bt;
                                step = STEP_WAIT_LENGTH_H;
                                break;
                            }
                            case STEP_WAIT_LENGTH_H: {
                                len += bt << 8;
                                if (len > data.length) {
                                    // length too long
                                    step = STEP_WAIT_HEAD1;
                                } else {
                                    receivedLen = 0;
                                    step = STEP_WAIT_DATA;
                                }
                                break;
                            }
                            case STEP_WAIT_DATA: {
                                data[receivedLen++] = bt;
                                if (receivedLen >= len) {
                                    step = STEP_WAIT_CHECKSUM;
                                }
                                break;
                            }
                            case STEP_WAIT_CHECKSUM: {
                                cs = bt & 0xFF;
                                step = STEP_WAIT_HEAD1;
                                // verify checksum
                                if (verifyChecksum(data, len, cs)) {
                                    // correct data
                                    if (listener != null) {
                                        // listener is set
                                        int epcStart = 5;
                                        byte[] epcData = new byte[len - epcStart];
                                        System.arraycopy(data, epcStart, epcData, 0, epcData.length);
                                        try {
                                            listener.onOriginalData(DATA_TYPE_EPC, epcData);
                                            listener.onStringData(DATA_TYPE_EPC, bytesToHexString(epcData));
                                        } catch (Exception e) {
                                            // ignore error from listener
                                        }
                                    }
                                } else {
                                    // checksum error
                                    notifyError("Checksum error");
                                }
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    // ignore errors
                    notifyError(e.getMessage());
                } finally {
                    reading = false;
                }
            });
            reading = true;
            readThread.start();
        }
    }

    boolean verifyChecksum(byte[] data, int len, int cs) {
        int x = data[0] & 0xFF;
        for (int n = 1; n < len; n++) {
            x ^= (data[n] & 0xFF);
        }
        return x == cs;
    }

    String bytesToHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte bt : data) {
            sb.append(ByteUtils.byteHex(bt));
        }
        return sb.toString();
    }

    @Override
    public void setListener(CardDataListener listener) {
        this.listener = listener;
    }

    public void notifyError(String msg) {
        if (listener != null) {
            try {
                listener.onError(msg);
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
