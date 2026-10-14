package com.x.hhqs.joa.sa.api;

import com.x.hhqs.joa.sa.exception.QueryServiceException;

public interface QueryService {
    String queryDeviceNameByIpAddress(String ipAddress) throws QueryServiceException;
    String queryFiberNameByDeviceNameAndPonId(String deviceName, String ponId) throws QueryServiceException;

    String queryOntNameByFiberNameAndOnuId(String fiberName, String onuId) throws QueryServiceException;
    String queryOntTypeByOntName(String ontName) throws QueryServiceException;
    String queryOntNameByFiberNameAndOnuIdAndOnuIdType(String fiberName, String onuId, String onuIdType) throws QueryServiceException;
    String queryL2InfraNameBySvlanIdAndCvlanId(String svlanId, String cvlanId) throws QueryServiceException;
    String queryL2UserNameByOntIntentNameAndUserVlan(String ontIntentName, String userVlan) throws QueryServiceException;
}
