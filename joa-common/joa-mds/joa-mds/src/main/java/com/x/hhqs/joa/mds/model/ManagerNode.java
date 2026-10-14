package com.x.hhqs.joa.mds.model;

import java.util.ArrayList;
import java.util.List;

public class ManagerNode {

    private Manager manager;

    private List<Device> devices = new ArrayList<>();

    public ManagerNode(Manager manager) {
        super();
        this.manager = manager;
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public void clearDevices() {
        if (devices != null) {
            devices.clear();
        }
    }

}
