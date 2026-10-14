package com.x.hhqs.joa.in.handler;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.service.JmxClientService;
import com.x.hhqs.joa.in.service.NetconfRpcBuilder;
import com.x.hhqs.joa.in.service.NetconfService;
import com.x.hhqs.joa.in.util.JmxClientServiceFactory;
import com.x.hhqs.joa.in.util.OssCmdMetaParser;
import com.x.hhqs.joa.in.vo.Device;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

@Component("LST-DEVICE")
public class LstDeviceHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LstDeviceHandler.class);
    private static final String TITLE = "List of Device";

    @Autowired
    private NetconfService netconfService;

    @Autowired
    private NetconfRpcBuilder netconfRpcBuilder;

    @Autowired
    private JmxClientServiceFactory jmxClientServiceFactory;

    @Override
    protected OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception {
        // Core device
        List<Device> deviceList = addDeviceMfToDeviceList(ossCmdMeta);
        if (OssCmdMetaParser.isEntireNetworkCmd(ossCmdMeta)) {
            filterByPaps(deviceList, userinfo.getPaps());
        }
        String[][] rtrvArray = createRtrvArray(deviceList, Device.class);

        // AMS device
        if (OssCmdMetaParser.isEntireNetworkCmd(ossCmdMeta)) {
            List<Device> amsDeviceList = createAmsDeviceList();
            rtrvArray = addToRtrvArray(rtrvArray, amsDeviceList, Device.class);
        }
        return createOssCmdResult(TITLE, rtrvArray);
    }

    private List<Device> createAmsDeviceList() throws Exception {
        String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_MDS_FTL);
        String amsIp = netconfService.sendRequestAndParseFirstValue(rpcRequest, mdsIpXPath(), RpcConsts.MDS_NS);
        List<Device> deviceList = new ArrayList<Device>();

        if(StringUtils.hasText(amsIp)){
            Map<String, String> topoMap = new HashMap<String, String>();
            Map<String, Map<String, String>> allDeviceMap = jmxClientServiceFactory.create(amsIp).getAllDevice(topoMap);
            for (String deviceMoId : allDeviceMap.keySet()) {
                Map<String, String> deviceAttRstMap = allDeviceMap.get(deviceMoId);
                String name = deviceAttRstMap.get("name");
                String ipAddress = deviceAttRstMap.get("ipAddress");
                String proRealease = deviceAttRstMap.get("productAndRelease");
                String nodeModel = deviceAttRstMap.get("nodeModel");
                String actVer = deviceAttRstMap.get("activeSoftwareVersion");
                String parentId = deviceAttRstMap.get("parent_id");
                String connectivityState = deviceAttRstMap.get("connectivityState");
                String aliasName = deviceAttRstMap.get("aliasName");
                String dt = parseProRelease(proRealease);
                String deVer = "R" + nodeModel.substring(nodeModel.indexOf('.') + 1) + "(" + actVer + ")";
                String topoLoc = topoMap.get(parentId);
                String dState = "CONNECTED".equals(connectivityState) ? "Connecting" : "Disconnecting";

                Device device = new Device(name, ipAddress, dt, deVer, topoLoc, dState, aliasName);
                deviceList.add(device);
            }
        }
        return deviceList;
    }

    private List<Device> addDeviceMfToDeviceList(OssCmdMeta ossCmdMeta)
            throws IOException, DocumentException, Rt1QueryException {
        String deviceFxTarget = getOltTarget(ossCmdMeta);
        Map<String, Object> templateArgs = new HashMap<String, Object>();
        templateArgs.put("deviceId", deviceFxTarget);

        String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_DEVICE_FTL, templateArgs);
        List<Element> intentEleList = netconfService.sendRequestAndParseResults(rpcRequest, intentXPath());

        List<Device> deviceList = new ArrayList<Device>();
        intentEleList.forEach(intentEle -> {
            Device device = new Device();
            String intentEleXml = intentEle.asXML();
            device.setTarget(getFirstValFromIbn(intentEleXml, targetXPath()));
            device.setDevName(device.getTarget());
            device.setDevIp(getFirstValFromIbn(intentEleXml, deviceXPath("ip-address")));
            device.setDt(getFirstValFromIbn(intentEleXml, deviceXPath("hardware-type")));
            device.setDeVer(getFirstValFromIbn(intentEleXml, deviceXPath("device-version")));
            device.setPap(getFirstValFromIbn(intentEleXml, deviceXPath("partition-access-profile")));
            device.setDevStat(parseDevState(getFirstValFromIbn(intentEleXml, deviceStateXPath("reachable"))));
            deviceList.add(device);
        });
        return deviceList;
    }

    private void filterByPaps(List<Device> deviceList, Set<String> userinfo) {
        LOG.info("filterByPaps:" + userinfo);
        if (userinfo.equals(Collections.singleton("*"))) { // ALLPAPS
            return;
        } else if (userinfo.isEmpty()) { // no PAP
            deviceList.clear();
        } else { // specific pap
            deviceList.removeIf(device -> !userinfo.contains(device.getPap()));
        }
    }

    private String getOltTarget(OssCmdMeta ossCmdMeta) {
        String oltTarget = "";
        if (OssCmdMetaParser.isOltCmd(ossCmdMeta)) {
            oltTarget = OssCmdMetaParser.oltTarget(ossCmdMeta);
        }
        return oltTarget;
    }

    private String parseDevState(String reachable) {
        String devState = "";
        if ("true".equals(reachable)) {
            devState = "Connecting";
        } else if ("false".equals(reachable)) {
            devState = "Disconnecting";
        }
        return devState;
    }

    private String parseProRelease(String pRealease) {
        if (pRealease.startsWith("FX-")) {
            pRealease = "7360 ISAM FX";
        } else if (pRealease.startsWith("GPON")) {
            pRealease = "7342 ISAM FTTU";
        } else if (pRealease.startsWith("EPON")) {
            pRealease = "7342 ISAM FTTU EPON";
        } else if (pRealease.startsWith("iSAM-I.5.1")) {
            pRealease = "7302 ISAM IHUB";
        }
        return pRealease;
    }


    private String mdsIpXPath() {
        return "//mds:manager-info/mds:ip";
    }

    private String intentXPath() {
        return "//ibn:ibn/ibn:intent";
    }

    private String targetXPath() {
        return "//ibn:intent/ibn:target";
    }

    private String deviceXPath(String leaf) {
        return "//dmf:device-mf/dmf:" + leaf;
    }

    private String deviceStateXPath(String leaf) {
        return "//device-mf:device-mf-state/device-mf:" + leaf;
    }
}
