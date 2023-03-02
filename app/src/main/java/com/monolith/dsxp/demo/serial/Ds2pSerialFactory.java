package com.monolith.dsxp.demo.serial;

import com.monolith.dsxp.driver.building.SerialPortFactory;
import com.monolith.dsxp.driver.conn.serial.SerialWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create By Levent8421
 * Create Time: 2022/8/10 15:14
 * Class Name: Ds2pSerialFactory
 * Author: Levent8421
 * Description:
 * Serial Port Factory
 *
 * @author Levent8421
 */
public class Ds2pSerialFactory implements SerialPortFactory {
    private static final Logger log = LoggerFactory.getLogger(Ds2pSerialFactory.class);
    public static final Ds2pSerialFactory INSTANCE = new Ds2pSerialFactory();

    @Override
    public SerialWrapper create(String deviceName, Integer baudRate) {
        try {
            return new Ds2pSerialPortWrapper(deviceName, baudRate);
        } catch (Exception e) {
            log.error("Error on create serial port with[{}/{}]", deviceName, baudRate, e);
            return null;
        }
    }
}
