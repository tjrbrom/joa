<#import "base.ftl" as base>
<@base.createIntent target="${templateName}#${customerName}" type="l2-user">
    <l2-user
        xmlns='http://www.x.com/management-solutions/l2-user'>
        <user-device-name>${userDeviceName}</user-device-name>
        <uni-id>${uniId}</uni-id>
        <l2-infra>${serviceName}</l2-infra>
        <#if cVlanId??>
        <c-vlan-id>${cVlanId}</c-vlan-id>
        </#if>
        <#if sVlanId??>
        <s-vlan-id>${sVlanId}</s-vlan-id>
        </#if>
        <#if qVlanId??>
        <q-vlan-id>${qVlanId}</q-vlan-id>
        </#if>
        <#if transparentQVlanId??>
        <transparent-q-vlan-id>${transparentQVlanId}</transparent-q-vlan-id>
        </#if>
        <#if nVlanId??>
        <n-vlan-id>${nVlanId}</n-vlan-id>
        </#if>
        <#if multipleVlanId??>
        <multiple-vlan-id>${multipleVlanId}</multiple-vlan-id>
        </#if>
        <service-profile>${serviceProfile}</service-profile>
    </l2-user>
</@base.createIntent>