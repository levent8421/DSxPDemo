package com.monolith.dsxp.demo;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.monolith.dsxp.demo.serial.Ds2pSerialFactory;
import com.monolith.dsxp.driver.DeviceManager;
import com.monolith.dsxp.driver.building.ds3p.Ds3pDeviceFactory;
import com.monolith.dsxp.driver.cluster.locker.LockerDeviceCluster;
import com.monolith.dsxp.driver.cluster.weight.DefaultCountingStrategy;
import com.monolith.dsxp.driver.cluster.weight.WeightDeviceCluster;
import com.monolith.dsxp.driver.group.DeviceGroup;
import com.monolith.dsxp.driver.identification.ds3p.Ds3pClusterUrl;
import com.monolith.dsxp.driver.identification.ds3p.Ds3pDeviceUrl;
import com.monolith.dsxp.driver.impl.DefaultDeviceManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Create By Levent8421
 * Create Time: 2023/3/2 20:19
 * Class Name: MainActivity
 * Author: Levent8421
 * Description:
 * Demo Main Activity
 *
 * @author Levent8421
 */
public class MainActivity extends AppCompatActivity {
    /**
     * 重力设备1编号
     */
    public static final String CLUSTER_1 = "ds3p:uart://%2Fdev%2FttyS1:115200/WT/L1-1-1";
    /**
     * 重力设备2编号
     */
    public static final String CLUSTER_2 = "ds3p:uart://%2Fdev%2FttyS1:115200/WT/L1-1-2";
    /**
     * 锁设备编号
     */
    public static final String CLUSTER_3 = "ds3p:uart://%2Fdev%2FttyS1:115200/LK/L2-1-1";

    public static final String[] DEVICE_URIS = {
            CLUSTER_1 + "/1?device=weight_sensor",
            CLUSTER_2 + "/2?device=weight_sensor",
            CLUSTER_3 + "/230-X1Y1?device=locker_hold_off",
    };
    /**
     * Device Manager是一个重量级对象，应避免系统内频繁创建
     */
    private DeviceManager deviceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(v -> this.startDsxpService());
        findViewById(R.id.btn_read_weight).setOnClickListener(v -> this.readWeight());
        findViewById(R.id.btn_locker_on).setOnClickListener(v -> this.lockerOn());
        findViewById(R.id.btn_locker_off).setOnClickListener(v -> this.lockerOff());
    }

    private void startDsxpService() {
        try {
            // 创建Factory对象，并将URI填入Factory
            Ds3pDeviceFactory factory = new Ds3pDeviceFactory(new Ds2pSerialFactory());
            for (String uri : DEVICE_URIS) {
                factory.add(Ds3pDeviceUrl.parse(uri));
            }
            // 使用工厂构建设备对象
            List<DeviceGroup> deviceGroups = factory.buildGroups();
            // 创建设备管理器，并将设备对象放入设备管理器
            deviceManager = new DefaultDeviceManager();
            for (DeviceGroup group : deviceGroups) {
                deviceManager.addGroup(group);
            }
            // 启动硬件驱动线程
            deviceManager.startSchedule();
            // 停止线程
//            deviceManager.shutdown(1000);

            //监听重量变化,由于此处能确定Cluster1、Cluster2为重力采集点，因此可直接强制转换
            WeightDeviceCluster cluster1 = (WeightDeviceCluster) deviceManager.findCluster(Ds3pClusterUrl.parse(CLUSTER_1));
            WeightDeviceCluster cluster2 = (WeightDeviceCluster) deviceManager.findCluster(Ds3pClusterUrl.parse(CLUSTER_2));
            ExampleWeightListener listener = new ExampleWeightListener();
            cluster1.setStateListener(listener);
            cluster2.setStateListener(listener);
            // 设置单重等计数信息
            DefaultCountingStrategy strategy = new DefaultCountingStrategy();
            strategy.setApw(BigDecimal.ONE); // 单重=1kg
            strategy.setMaxError(BigDecimal.ONE.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP)); // 最大误差=0.5kg
            strategy.setMaxError(BigDecimal.ONE.divide(BigDecimal.TEN, RoundingMode.HALF_UP)); // 平均误差=0.11kg
            cluster1.setCountingStrategy(strategy);
            cluster2.setCountingStrategy(strategy);

            // 监听锁状态
            LockerDeviceCluster lockerCluster = (LockerDeviceCluster) deviceManager.findCluster(Ds3pClusterUrl.parse(CLUSTER_3));
            lockerCluster.setLockerStateListener(new ExampleLockerListener());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解锁
     */
    private void lockerOff() {
        try {
            LockerDeviceCluster lockerCluster = (LockerDeviceCluster) deviceManager.findCluster(Ds3pClusterUrl.parse(CLUSTER_3));
            lockerCluster.unlock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 上锁
     */
    private void lockerOn() {
        try {
            LockerDeviceCluster lockerCluster = (LockerDeviceCluster) deviceManager.findCluster(Ds3pClusterUrl.parse(CLUSTER_3));
            lockerCluster.lock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 主动读取重量信息
     */
    private void readWeight() {
        try {
            WeightDeviceCluster cluster1 = (WeightDeviceCluster) deviceManager.findCluster(Ds3pClusterUrl.parse(CLUSTER_1));
            WeightDeviceCluster cluster2 = (WeightDeviceCluster) deviceManager.findCluster(Ds3pClusterUrl.parse(CLUSTER_2));
            BigDecimal weight1 = cluster1.getClusterState().getWeight();
            BigDecimal weight2 = cluster2.getClusterState().getWeight();
            Toast.makeText(this, "Weight1=" + weight1 + "/weight2=" + weight2, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}