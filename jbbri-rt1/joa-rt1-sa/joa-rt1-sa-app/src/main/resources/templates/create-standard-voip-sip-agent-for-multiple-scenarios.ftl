<#import "base.ftl" as base>
<@base.createIntent target="${sipAgentTarget}" type="standard-voip-sip-agent">
    <standard-voip-sip-agent
        xmlns='http://www.x.com/management-solutions/standard-voip-sip-agent'>
        <voip-l2-infra-name>${voipL2InfraName}</voip-l2-infra-name>
        <#if ipMode??>
        <#if ipMode == "DHCP">
        <service-profile>default-voip-sip</service-profile>
        <#elseif ipMode == "STATIC">
        <service-profile>default-voip-sip-static</service-profile>
        <ip-address>${ipAddress}</ip-address>
        <netmask>${netmask}</netmask>
        </#if>
        </#if>
    </standard-voip-sip-agent>
</@base.createIntent>