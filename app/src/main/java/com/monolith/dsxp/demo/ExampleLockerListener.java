package com.monolith.dsxp.demo;

import com.monolith.dsxp.driver.cluster.locker.LockerDeviceCluster;
import com.monolith.dsxp.driver.cluster.locker.LockerStateListener;

/**
 * Create By Levent8421
 * Create Time: 2023/3/2 20:43
 * Class Name: ExampleLockerListener
 * Author: Levent8421
 * Description:
 * Example Locker State Listener
 *
 * @author Levent8421
 */
public class ExampleLockerListener implements LockerStateListener {
    @Override
    public void onLockerStateChange(LockerDeviceCluster lockerDeviceCluster) {
        // 锁状态变化
        boolean locked = lockerDeviceCluster.isLocked();
        String cid = lockerDeviceCluster.getIdentification().asIdentificationKey();
        System.out.println("Locker:" + cid + "/lockerState=" + locked);
    }
}
