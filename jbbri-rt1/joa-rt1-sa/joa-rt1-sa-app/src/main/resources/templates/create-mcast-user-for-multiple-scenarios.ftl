<#import "base.ftl" as base>
<@base.createIntent target="${l2InfraName}#${l2UserName}" type="mcast-user">
    <mcast-user
        xmlns='http://www.x.com/management-solutions/mcast-user'>
        <mcast-infra-name>${mcastInfraName}</mcast-infra-name>
        <service-profile>${serviceProfile}</service-profile>
        <#if permittedPackages??>
        <permitted-packages>${permittedPackages}</permitted-packages>
        </#if>
        <#if allowedPreviewPackages??>
        <allowed-preview-packages>${allowedPreviewPackages}</allowed-preview-packages>
        </#if>
    </mcast-user>
</@base.createIntent>