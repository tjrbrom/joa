<#import "base.ftl" as base>
<@base.queryIntent>
    <ont
        xmlns="http://www.x.com/management-solutions/ont">
        <#if fiberName??>
        <fiber-name>${fiberName}</fiber-name>
        </#if>
        <#if expectedSerialNumber??>
        <expected-serial-number>${expectedSerialNumber}</expected-serial-number>
        </#if>
        <#if expectedRegistrationId??>
        <expected-registration-id>${expectedRegistrationId}</expected-registration-id>
        </#if>
        <#if expectedLoid??>
        <expected-loid>${expectedLoid}</expected-loid>
        </#if>
    </ont>
</@base.queryIntent>