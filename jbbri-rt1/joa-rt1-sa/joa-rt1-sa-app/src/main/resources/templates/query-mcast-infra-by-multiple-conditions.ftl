<#import "base.ftl" as base>
<@base.queryIntent>
     <mcast-infra xmlns="http://www.x.com/management-solutions/mcast-infra">
        <#if l2InfraName??>
        <l2-infra-services>
          <l2-infra>${l2InfraName}</l2-infra>
        </l2-infra-services>
        </#if>
     </mcast-infra>
</@base.queryIntent>