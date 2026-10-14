<#import "base.ftl" as base>
<@base.createIntent target="${serviceName}#${customerName}" type="standard-voip-sip-user">
    <standard-voip-sip-user
        xmlns='http://www.x.com/management-solutions/standard-voip-sip-user'>
        <user-device-name>${userDeviceName}</user-device-name>
        <uni-id>${uniId}</uni-id>
        <sip-address-of-record>${sipAddressOfRecord}</sip-address-of-record>
        <sip-display-name>${sipDisplayName}</sip-display-name>
        <sip-user-name>${sipUserName}</sip-user-name>
        <sip-password>${sipPassword}</sip-password>
        <#if serviceProfile??>
        <service-profile>${serviceProfile}</service-profile>
        </#if>
    </standard-voip-sip-user>
</@base.createIntent>