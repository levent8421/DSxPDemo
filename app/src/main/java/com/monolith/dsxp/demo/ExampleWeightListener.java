package com.monolith.dsxp.demo;

import com.monolith.dsxp.driver.cluster.weight.WeightClusterStateListener;
import com.monolith.dsxp.driver.cluster.weight.WeightDeviceCluster;
import com.monolith.dsxp.driver.cluster.weight.WeightDeviceClusterState;
import com.monolith.dsxp.driver.device.esl.WeightEslDevice;
import com.monolith.dsxp.driver.device.weight.WeightSensorDevice;

import java.math.BigDecimal;

/**
 * Create By Levent8421
 * Create Time: 2023/3/2 20:41
 * Class Name: ExampleWeightListener
 * Author: Levent8421
 * Description:
 * State Listener
 *
 * @author Levent8421
 */
public class ExampleWeightListener implements WeightClusterStateListener {
    @Override
    public void onWeightValueChange(WeightDeviceCluster cluster) throws Exception {
        // 重量变化
        WeightDeviceClusterState clusterState = cluster.getClusterState();
        BigDecimal weight = clusterState.getWeight();
        int weightState = clusterState.getWeightState();
        String cId = cluster.getIdentification().asIdentificationKey();
        System.out.println(cId + ":" + weight + "/" + weightState);
    }

    @Override
    public void onWeightCountChange(WeightDeviceCluster cluster) throws Exception {
        // 计数变化
    }

    @Override
    public void onSensorConnectionStateChange(WeightDeviceCluster cluster, WeightSensorDevice sensorDevice) throws Exception {
        // 传感器连接状态变化
    }

    @Override
    public void onEslConnectionStateChange(WeightDeviceCluster cluster, WeightEslDevice eslDevice) throws Exception {
        // 电子标签连接状态变化
    }

    @Override
    public void onMasterEslKeyLongPress(WeightDeviceCluster cluster, WeightEslDevice eslDevice) throws Exception {
        // 主标签长按事件
    }

    @Override
    public void onZombieEslKeyLongPress(WeightDeviceCluster cluster, WeightEslDevice eslDevice) throws Exception {
        // 副标签长按事件
    }

    @Override
    public void onEnableChange(WeightDeviceCluster cluster) throws Exception {
        // 启用状态变化，发生在主标签短按后
    }
}
