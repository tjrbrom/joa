package com.x.hhqs.joa.in.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.vo.Profile;

@Service
public class ProfileManagerService {

	@Autowired
	private NetconfService netconfService;

	@Autowired
	private NetconfRpcBuilder netconfRpcBuilder;

	public Profile getProfilesUsedByIntent(String intentType, String target, String uiName,
			Map<String, String> xPathMap) throws Rt1QueryException, JSONException, IOException, DocumentException {
		Map<String, Object> templateArgs = profilesUsedByIntentTemplateArgs(intentType, target, uiName);
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_PROFILES_USED_BY_INTENT, templateArgs);

		Element profileEle = netconfService.sendRequestAndParseFirstResult(rpcRequest, profileXPath(),
				RpcConsts.PROFILE_NS);
		Profile profileVo = new Profile();
		if (profileEle != null) {
			profileVo.setName(profileEle.elementText("name"));
			profileVo.setVersion(profileEle.elementText("version"));
			profileVo.setBaseRelease(profileEle.elementText("base-release"));
			profileVo.setType(profileEle.elementText("profile-type"));
			profileVo.setSubType(profileEle.elementText("subtype"));
			addSpecificDataToProfileVo(profileVo, xPathMap);
		}
		return profileVo;
	}

	public Profile getProfilesUsedByIntent(String profileName, String intentType, String target, String uiName,
			Map<String, String> xPathMap) throws Rt1QueryException, JSONException, IOException, DocumentException {
		Map<String, Object> templateArgs = profilesUsedByIntentTemplateArgs(intentType, target, uiName);
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_PROFILES_USED_BY_INTENT, templateArgs);

		Element profileEle = netconfService.sendRequestAndParseFirstResult(rpcRequest, profileNameXPath(profileName),
				RpcConsts.PROFILE_NS);
		Profile profileVo = new Profile();
		if (profileEle != null) {
			profileVo.setName(profileName);
			profileVo.setVersion(profileEle.elementText("version"));
			profileVo.setBaseRelease(profileEle.elementText("base-release"));
			profileVo.setType(profileEle.elementText("profile-type"));
			profileVo.setSubType(profileEle.elementText("subtype"));
			addSpecificDataToProfileVo(profileVo, xPathMap);
		}
		return profileVo;
	}

	private void addSpecificDataToProfileVo(Profile profileVo, Map<String, String> xPathMap)
			throws Rt1QueryException, JSONException, IOException, DocumentException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("profileType", profileVo.getType());
		templateArgs.put("subType", profileVo.getSubType());
		templateArgs.put("name", profileVo.getName());
		templateArgs.put("version", profileVo.getVersion());
		templateArgs.put("baseRelease", profileVo.getBaseRelease());
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_PROFILE_SPECIFIC_DATA, templateArgs);

		Map<String, String> specificData = netconfService.sendRequestAndParseFirstValue(rpcRequest, xPathMap,
				RpcConsts.PROFILE_NS);
		profileVo.setSpecificData(specificData);
	}

	private Map<String, Object> profilesUsedByIntentTemplateArgs(String intentType, String target, String uiName) {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("target", target);
		templateArgs.put("intentType", intentType);
		templateArgs.put("profileTypeUiName", uiName);
		return templateArgs;
	}

	private String profileXPath() {
		return "//profile-mgr:profiles/profile-mgr:profile";
	}

	private String profileNameXPath(String name) {
		return "//profile-mgr:profiles/profile-mgr:profile[profile-mgr:name='" + name + "']";
	}
}
