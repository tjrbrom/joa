<#import "base.ftl" as base>
<@base.modifyIntent target="${mcastUserTarget}" type="mcast-user">
    <mcast-user
        xmlns='http://www.x.com/management-solutions/mcast-user'>
        <#if maxGroupNumber??>
        <#if maxGroupNumber == "20">
        <service-profile>IPTV_MAXGRP_20</service-profile>
        <#elseif maxGroupNumber == "10">
        <service-profile>IPTV_MAXGRP_10</service-profile>
        </#if>
        </#if>
    </mcast-user>
</@base.modifyIntent>