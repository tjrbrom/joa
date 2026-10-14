package com.x.hhqs.joa.alarm.model.login;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.x.hhqs.joa.common.core.util.JwtUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 *
 *
 * @author aip
 */
public class LoginUser implements Serializable
{
    private static final long serialVersionUID = 7810522817658874345L;

    private String token;

    private Long userid;

    private String username;

    private Long loginTime;

    private Long expireTime;

    private String ipaddr;

    private Set<String> permissions;

    private Set<String> roles;

    private SysUser sysUser;

    public String getjSessionId() {
        return jSessionId;
    }

    public void setjSessionId(String jSessionId) {
        this.jSessionId = jSessionId;
    }

    private String jSessionId;

    public Long getUserExpireTimeSetting() {
        return userExpireTimeSetting;
    }

    public void setUserExpireTimeSetting(Long userExpireTimeSetting) {
        this.userExpireTimeSetting = userExpireTimeSetting;
    }

    private Long userExpireTimeSetting;

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    private String userKey;

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public Long getUserid()
    {
        return userid;
    }

    public void setUserid(Long userid)
    {
        this.userid = userid;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public Long getLoginTime()
    {
        return loginTime;
    }

    public void setLoginTime(Long loginTime)
    {
        this.loginTime = loginTime;
    }

    public Long getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Long expireTime)
    {
        this.expireTime = expireTime;
    }

    public String getIpaddr()
    {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr)
    {
        this.ipaddr = ipaddr;
    }

    public Set<String> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(Set<String> permissions)
    {
        this.permissions = permissions;
    }

    public Set<String> getRoles()
    {
        return roles;
    }

    public void setRoles(Set<String> roles)
    {
        this.roles = roles;
    }

    public SysUser getSysUser()
    {
        return sysUser;
    }

    public void setSysUser(SysUser sysUser)
    {
        this.sysUser = sysUser;
    }

    @Override
    public String toString() {
        return "LoginUser{" +
                "token='" + token + '\'' +
                ", userid=" + userid +
                ", username='" + username + '\'' +
                ", loginTime=" + loginTime +
                ", expireTime=" + expireTime +
                ", ipaddr='" + ipaddr + '\'' +
                ", permissions=" + permissions +
                ", roles=" + roles +
                ", sysUser=" + sysUser +
                '}';
    }

    public static LoginUser processClaimsMap2LoginUser(String token) {
        LoginUser loginUser=new LoginUser();

        Map<String, Object> ClaimsMap= JwtUtils.parseTokenNoSecret(token);

        loginUser.setToken(token);
//        loginUser.setUserKey(TokenConstants.AUTHENTICATION);

        if(ClaimsMap.containsKey("auth_time")) {
            loginUser.setLoginTime(Long.parseLong(ClaimsMap.get("auth_time").toString()));
        }
        if(ClaimsMap.containsKey("exp")) {
            loginUser.setExpireTime(Long.parseLong(ClaimsMap.get("exp").toString()));
        }

        if(ClaimsMap.containsKey("preferred_username")) {
            loginUser.setUsername((String) ClaimsMap.get("preferred_username"));
        }
        if(ClaimsMap.containsKey("username")) {
            loginUser.setUsername((String) ClaimsMap.get("username"));
        }
        if(ClaimsMap.containsKey("sub")) {
            loginUser.setUserKey((String) ClaimsMap.get("sub"));
        }
        //authorization ->permissions[]->scopes []
        final Set<String> roles=new HashSet();
        try {
            if(ClaimsMap.containsKey("roles")) {
                if (Optional.ofNullable(ClaimsMap.get("roles")).isPresent()){
                    List<String> tempRoles = (List<String>) ClaimsMap.get("roles");
                    roles.addAll(tempRoles);
                }
            }
            if(ClaimsMap.containsKey("realm_access")){
                JSONObject realmAccess = (JSONObject) ClaimsMap.get("realm_access");
                if (Optional.ofNullable(realmAccess.get("roles")).isPresent()){
                    List<String> rolest = (List<String>) realmAccess.get("roles");
                    roles.addAll(rolest);
                }
            }
            if(ClaimsMap.containsKey("authorization")) {
                JSONObject authorization = (JSONObject) ClaimsMap.get("authorization");
                if(Optional.ofNullable(authorization.get("permissions")).isPresent()){
                    JSONArray permissions= (JSONArray) authorization.get("permissions");
                    permissions.forEach(permission->{
                        JSONObject permissionjs= (JSONObject) permission;
                        JSONArray scopes= (JSONArray)  permissionjs.get("scopes");
                        scopes.forEach(role->{
                            roles.add(String.valueOf(role));
                        });
                    });
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        loginUser.setRoles(roles);
        return loginUser;
    }
}
