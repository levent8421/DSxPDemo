package com.monolith.dsxp.demo.serialport;


import android.os.Build;

import androidx.annotation.RequiresApi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Create By Levent8421
 * Create Time: 2020/12/17 20:11
 * Class Name: SerialDeviceUtils
 * Author: Levent8421
 * Description:
 * 串口设备工具类
 *
 * @author Levent8421
 */
public class SerialDeviceUtils {
    private static final Logger log = LoggerFactory.getLogger(SerialDeviceUtils.class);

    private static final File DEVICE_ID_PATH_FILE = new File("/dev/serial/by-id");

    private static boolean hasUsbTtyDevice() {
        return DEVICE_ID_PATH_FILE.exists();
    }

    /**
     * 获取USB ID连接文件
     *
     * @param deviceFile 设备文件
     * @return 连接文件
     * @throws IOException IOE
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static File getUsbDeviceIdPath(String deviceFile) throws IOException {
        if (!hasUsbTtyDevice()) {
            return null;
        }
        final File[] ids = DEVICE_ID_PATH_FILE.listFiles();
        if (ids == null || ids.length <= 0) {
            return null;
        }
        final File device = new File(deviceFile);
        if (!device.exists()) {
            return null;
        }
        final String filename = device.getName();
        for (File linkFile : ids) {
            final Path readPath = linkFile.toPath().toRealPath();
            final String sourceName = readPath.toFile().getName();
            if (filename.equalsIgnoreCase(sourceName)) {
                return linkFile;
            }
        }
        return null;
    }

    /**
     * 获取USB串口设备ID
     *
     * @param deviceFile device file
     * @return id
     * @throws IOException ioe
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getUsbTtyDeviceId(String deviceFile) throws IOException {
        final File usbLinkFile = getUsbDeviceIdPath(deviceFile);
        if (usbLinkFile == null) {
            return null;
        }
        return usbLinkFile.getName();
    }

    /**
     * 将USBid转换为USB设备连接的路径
     *
     * @param usbId usb id
     * @return path
     */
    public static String asUsbDeviceIdTarget(String usbId) {
        return new File(DEVICE_ID_PATH_FILE, usbId).getAbsolutePath();
    }

    /**
     * 尝试获取文件的真实名称
     *
     * @param deviceName 设备名称
     * @return 真实 文件
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static File tryObtainRealDeviceFile(String deviceName) {
        final File linkFile = new File(deviceName);
        if (!linkFile.exists()) {
            return null;
        }
        try {
            return linkFile.toPath().toRealPath().toFile();
        } catch (IOException e) {
            log.debug("Can not find real path for [{}]", deviceName);
            return null;
        }
    }
}
