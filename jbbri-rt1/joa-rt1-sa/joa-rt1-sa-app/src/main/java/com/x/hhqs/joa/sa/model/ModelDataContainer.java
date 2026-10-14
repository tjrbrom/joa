package com.x.hhqs.joa.sa.model;

import java.util.HashMap;
import java.util.Map;

public class ModelDataContainer {

    private static final ThreadLocal<SharedData> threadLocal = ThreadLocal.withInitial(SharedData::new);

    public static SharedData getSharedData() {
        return threadLocal.get();
    }

    public static void clearSharedData() {
        threadLocal.remove();
    }

    public static class SharedData {
        private Map<String, String> nbiMeta = new HashMap<>();
        private Map<String, String> sbiMeta = new HashMap<>();
        private Map<String, String> context = new HashMap<>();
        private Map<String, String> onuInfo = new HashMap<>();
        private Map<String, String> userInfo = new HashMap<>();

        public Map<String, String> getNbiMeta() {
            return nbiMeta;
        }

        public void setNbiMeta(Map<String, String> nbiMeta) {
            this.nbiMeta = nbiMeta;
        }

        public Map<String, String> getSbiMeta() {
            return sbiMeta;
        }

        public void setSbiMeta(Map<String, String> sbiMeta) {
            this.sbiMeta = sbiMeta;
        }

        public Map<String, String> getContext() {
            return context;
        }

        public void setContext(Map<String, String> context) {
            this.context = context;
        }

        public Map<String, String> getOnuInfo() {
            return onuInfo;
        }

        public void setOnuInfo(Map<String, String> onuInfo) {
            this.onuInfo = onuInfo;
        }

        public Map<String, String> getUserInfo() {
            return userInfo;
        }

        public void setUserInfo(Map<String, String> userInfo) {
            this.userInfo = userInfo;
        }
    }

    private ModelDataContainer() {
    }
}
