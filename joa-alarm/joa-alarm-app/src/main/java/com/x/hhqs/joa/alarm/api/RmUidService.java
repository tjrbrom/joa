package com.x.hhqs.joa.alarm.api;

import java.util.Map;

//import com.y.axs.app.jbbri.rmuid.RmType;

public interface RmUidService {
	
	public String getNeRmUidSourceById(Long neId);
	
	public String getNeRmUidSourceByIp(String neIpAddress);
	
//	public String generateRmUidByMobjectId(String neIpAddress, String mobjectId, RmType rmType) throws Exception;
//
//	public String generateRmUidByFriendlyName(String neIpAddress, String friendlyName, RmType rmType) throws Exception;
//
//    public String generateRmUidByMobjectId(Long neId, String mobjectId, RmType rmType) throws Exception;
//
//	public String generateRmUidByFriendlyName(Long neId, String friendlyName, RmType rmType) throws Exception;
	
	public Map<String, String> getAllRmUidSources();
	
}
