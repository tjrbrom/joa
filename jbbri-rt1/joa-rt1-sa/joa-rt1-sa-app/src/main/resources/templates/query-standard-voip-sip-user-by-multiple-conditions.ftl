<#import "base.ftl" as base>
<@base.queryIntent>
    <standard-voip-sip-user xmlns="http://www.x.com/management-solutions/standard-voip-sip-user">
        <user-device-name>${userDeviceName}</user-device-name>
        <uni-id>${uniId}</uni-id>
    </standard-voip-sip-user>
</@base.queryIntent>