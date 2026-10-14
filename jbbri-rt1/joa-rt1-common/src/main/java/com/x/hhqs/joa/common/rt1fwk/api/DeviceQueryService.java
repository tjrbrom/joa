package com.x.hhqs.joa.common.rt1fwk.api;

import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.common.rt1fwk.pojo.DeviceInfo;

import java.util.List;

/**
 * find OLT/ONU by attribute
 */
public interface DeviceQueryService {
    /**
     * @param oltId:oltIp or oltName
     * @return DeviceInfo
     */
    DeviceInfo queryOlt(String oltId) throws OssCommandException;

    /**
     * @param oltId:oltIp or oltName
     * @param ponId:like 1-1-1-1
     * @return DeviceInfo
     */
    DeviceInfo queryPon(String oltId, String ponId) throws OssCommandException;

    /**
     *
     * @param oltId:oltIp or oltName
     * @param ponId:like 1-1-1-1
     * @param onuIdType: ONU_NUMBER,ONU_NAME,ONU_IP,(MAC/LOID/PASSWORD)
     * @param onuId: onu_number,onu_name or Sernum/SLID/LOID
     * @return DeviceInfo
     */
    DeviceInfo queryOnu(String oltId, String ponId, String onuIdType, String onuId) throws OssCommandException;

    /**
     *
     * @param onuId : onu ip address or onu name
     * @return DeviceInfo
     * @throws OssCommandException
     */
    DeviceInfo queryOnu(String  onuId) throws OssCommandException;


    /**
     * @param  onuIdType: Sernum/SLID/LOID
     * @param onuId: Sernum/SLID/LOID
     * @return List<DeviceInfo>
     */
    List<DeviceInfo> queryOnuList(String onuIdType, String onuId);


}
