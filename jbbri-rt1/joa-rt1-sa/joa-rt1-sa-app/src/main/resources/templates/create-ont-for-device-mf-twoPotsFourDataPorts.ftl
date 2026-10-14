<#import "base.ftl" as base>
<@base.createIntent target="${ontTarget}" type="ont">
    <ont
        xmlns='http://www.x.com/management-solutions/ont'>
        <uni-service-configuration>
            <uni-id>LAN1</uni-id>
            <service-profile>${lanPortServiceProfile}</service-profile>
        </uni-service-configuration>
        <uni-service-configuration>
            <uni-id>LAN2</uni-id>
            <service-profile>${lanPortServiceProfile}</service-profile>
        </uni-service-configuration>
        <uni-service-configuration>
            <uni-id>LAN3</uni-id>
            <service-profile>${lanPortServiceProfile}</service-profile>
        </uni-service-configuration>
        <uni-service-configuration>
            <uni-id>LAN4</uni-id>
            <service-profile>${lanPortServiceProfile}</service-profile>
        </uni-service-configuration>
        <uni-service-configuration>
            <uni-id>TEL1</uni-id>
            <service-profile>${potsPortServiceProfile}</service-profile>
        </uni-service-configuration>
        <uni-service-configuration>
            <uni-id>TEL2</uni-id>
            <service-profile>${potsPortServiceProfile}</service-profile>
        </uni-service-configuration>
        <@base.onuIdType/>
        <from-device-mapping></from-device-mapping>
        <fiber-name>${fiberName}</fiber-name>
        <pon-type>${ponType}</pon-type>
        <ont-type>twoPotsFourDataPorts</ont-type>
        <@base.auth/>
        <onu-service-profile>${onuServiceProfile}</onu-service-profile>
        <#if onuSpeedProfile??>
        <ont-speed-profile>${onuSpeedProfile}</ont-speed-profile>
        </#if>
        <pots-service-profile>${potsServiceProfile}</pots-service-profile>
        <#if ontName??>
        <ont-name>${ontName}</ont-name>
        </#if>
        <#if ontIP??>
        <ont-ip>${ontIP}</ont-ip>
        </#if>
        <creation-user>${creationUser}</creation-user>
        <creation-date>${.now?string("yyyy-MM-dd HH:mm:ss")}</creation-date>
    </ont>
</@base.createIntent>