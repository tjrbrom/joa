<#import "base.ftl" as base>
<@base.createIntent target="${ontTarget}" type="ont">
    <ont
        xmlns='http://www.x.com/management-solutions/ont'>
        <uni-service-configuration>
            <uni-id>VEIP</uni-id>
            <service-profile>${uniServiceProfile}</service-profile>
        </uni-service-configuration>
        <@base.onuIdType/>
        <from-device-mapping></from-device-mapping>
        <fiber-name>${fiberName}</fiber-name>
        <pon-type>${ponType}</pon-type>
        <ont-type>INTEGRATED</ont-type>
        <@base.auth/>
        <onu-service-profile>${onuServiceProfile}</onu-service-profile>
        <#if onuSpeedProfile??>
        <ont-speed-profile>${onuSpeedProfile}</ont-speed-profile>
        </#if>
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