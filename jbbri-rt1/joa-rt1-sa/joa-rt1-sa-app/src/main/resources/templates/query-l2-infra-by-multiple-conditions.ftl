<#import "base.ftl" as base>
<@base.queryIntent>
     <l2-infra xmlns="http://www.x.com/management-solutions/l2-infra">
        <#if sVlanId??>
        <s-vlan-id>${sVlanId}</s-vlan-id>
        </#if>
        <#if cVlanId??>
        <c-vlan-id>${cVlanId}</c-vlan-id>
        </#if>
     </l2-infra>
</@base.queryIntent>