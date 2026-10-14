<#import "base.ftl" as base>
<@base.modifyIntent target="${ontTarget}" type="ont">
    <ont
        xmlns='http://www.x.com/management-solutions/ont'>
        <#if authType??>
        <#if authType == "MAC">
        <expected-serial-number>${authInfo}</expected-serial-number>
        <#if expectedRegistrationId??>
        <expected-registration-id xc:operation='delete'>${expectedRegistrationId}</expected-registration-id>
        </#if>
        <#if expectedLoid??>
        <expected-loid xc:operation='delete'>${expectedLoid}</expected-loid>
        </#if>
        <#elseif authType == "PASSWORD">
        <expected-registration-id>${authInfo}</expected-registration-id>
        <#if expectedSerialNumber??>
        <expected-serial-number xc:operation='delete'>${expectedSerialNumber}</expected-serial-number>
        </#if>
        <#if expectedLoid??>
        <expected-loid xc:operation='delete'>${expectedLoid}</expected-loid>
        </#if>
        <#elseif authType == "LOID">
        <expected-loid>${authInfo}</expected-loid>
        <#if expectedSerialNumber??>
        <expected-serial-number xc:operation='delete'>${expectedSerialNumber}</expected-serial-number>
        </#if>
        <#if expectedRegistrationId??>
        <expected-registration-id xc:operation='delete'>${expectedRegistrationId}</expected-registration-id>
        </#if>
        </#if>
        </#if>
        <#if ontSpeedProfile??>
        <ont-speed-profile>${ontSpeedProfile}</ont-speed-profile>
        </#if>
        <#if onuPort??>
        <#list onuPort?split("|") as uniId>
        <uni-service-configuration>
            <uni-id>LAN${uniId?keep_after_last("-")}</uni-id>
            <uni-speed-profile>${uniSpeedProfile}</uni-speed-profile>
        </uni-service-configuration>
        </#list>
        </#if>
    </ont>
</@base.modifyIntent>