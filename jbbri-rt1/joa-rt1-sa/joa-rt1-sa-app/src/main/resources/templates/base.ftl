<#macro createIntent target type>
<rpc
    xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'>
    <edit-config>
        <target>
            <running></running>
        </target>
        <test-option>set</test-option>
        <error-option>rollback-on-error</error-option>
        <config>
            <ibn
                xmlns='http://www.x.com/management-solutions/ibn'>
                <intent
                    xmlns:yang='urn:ietf:params:xml:ns:yang:1'
                    xmlns:xc='urn:ietf:params:xml:ns:netconf:base:1.0' xc:operation='create'>
                    <target>${target}</target>
                    <intent-type>${type}</intent-type>
                    <required-network-state>active</required-network-state>
                    <intent-specific-data>
                        <#nested>
                    </intent-specific-data>
                </intent>
            </ibn>
        </config>
    </edit-config>
</rpc>
</#macro>

<#macro modifyIntent target type>
<rpc
    xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'>
    <edit-config>
        <target>
            <running></running>
        </target>
        <test-option>set</test-option>
        <error-option>rollback-on-error</error-option>
        <config>
            <ibn
                xmlns='http://www.x.com/management-solutions/ibn'>
                <intent
                    xmlns:yang='urn:ietf:params:xml:ns:yang:1'
                    xmlns:xc='urn:ietf:params:xml:ns:netconf:base:1.0' xc:operation='merge'>
                    <target>${target}</target>
                    <intent-type>${type}</intent-type>
                    <required-network-state>active</required-network-state>
                    <intent-specific-data>
                        <#nested>
                    </intent-specific-data>
                </intent>
            </ibn>
        </config>
    </edit-config>
</rpc>
</#macro>

<#macro queryIntent>
<rpc xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'>
    <get-config>
        <source>
            <running/>
        </source>
        <filter type='subtree'>
            <ibn xmlns='http://www.x.com/management-solutions/ibn'>
                <intent>
                    <#if target??>
                    <target>${target}</target>
                    </#if>
                    <#if type??>
                    <intent-type>${type}</intent-type>
                    </#if>
                    <intent-specific-data>
                        <#nested>
                    </intent-specific-data>
                </intent>
            </ibn>
        </filter>
    </get-config>
</rpc>
</#macro>

<#macro action target type>
<rpc
    xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'>
    <action
        xmlns='urn:ietf:params:xml:ns:yang:1'>
        <ibn
            xmlns='http://www.x.com/management-solutions/ibn'>
            <intent>
                <target>${target}</target>
                <intent-type>${type}</intent-type>
                <#nested>
            </intent>
        </ibn>
    </action>
</rpc>
</#macro>

<#macro onuIdType>
        <#if onuNo??>
        <onu-id>${onuNo}</onu-id>
        <#else>
        <auto></auto>
        </#if>
</#macro>

<#macro auth>
        <#if authType??>
        <#if authType == "MAC">
        <expected-serial-number>${onuId}</expected-serial-number>
        <#elseif authType == "PASSWORD">
        <expected-registration-id>${onuId}</expected-registration-id>
        <#elseif authType == "LOID">
        <expected-loid>${onuId}</expected-loid>
        </#if>
        <#else>
        <expected-registration-id>${onuId}</expected-registration-id>
        </#if>
</#macro>
