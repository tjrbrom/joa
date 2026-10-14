<#import "base.ftl" as base>
<@base.createIntent target="${oltTarget}#${serviceName}" type="l2-infra">
    <l2-infra
            xmlns='http://www.x.com/management-solutions/l2-infra'>
        <vlan-mode>${vlanMode}</vlan-mode>
        <#if portList??>
        <#assign nniMap = {
            "NA-NA-NA-LAG1": "LAG-1",
            "NA-NA-NA-LAG10": "LAG-10",
            "NA-NA-NTIO1-11": "ntio-1:qsfp:1-1",
            "NA-NA-NTIO1-12": "ntio-1:qsfp:1-2",
            "NA-NA-NTIO1-13": "ntio-1:qsfp:1-3",
            "NA-NA-NTIO1-14": "ntio-1:qsfp:1-4",
            "NA-NA-NTIO1-21": "ntio-1:qsfp:2-1",
            "NA-NA-NTIO1-22": "ntio-1:qsfp:2-2",
            "NA-NA-NTIO1-23": "ntio-1:qsfp:2-3",
            "NA-NA-NTIO1-24": "ntio-1:qsfp:2-4",
            "NA-NA-NTIO2-11": "ntio-2:qsfp:1-1",
            "NA-NA-NTIO2-12": "ntio-2:qsfp:1-2",
            "NA-NA-NTIO2-13": "ntio-2:qsfp:1-3",
            "NA-NA-NTIO2-14": "ntio-2:qsfp:1-4",
            "NA-NA-NTIO2-21": "ntio-2:qsfp:2-1",
            "NA-NA-NTIO2-22": "ntio-2:qsfp:2-2",
            "NA-NA-NTIO2-23": "ntio-2:qsfp:2-3",
            "NA-NA-NTIO2-24": "ntio-2:qsfp:2-4"
        }>
        <#list portList?split("|") as port>
        <#if nniMap[port]?has_content>
        <nni-id>${nniMap[port]}</nni-id>
        <#else>
        <nni-id>${port}</nni-id>
        </#if>
        </#list>
        </#if>
        <none></none>
        <#if sVlanId??>
        <s-vlan-id>${sVlanId}</s-vlan-id>
        </#if>
        <#if cVlanId??>
        <c-vlan-id>${cVlanId}</c-vlan-id>
        </#if>
        <forwarder-profile>${forwarderProfile}</forwarder-profile>
        <service-profile>${serviceProfile}</service-profile>
        <#if serviceDesc??>
        <service-description>${serviceDesc}</service-description>
        </#if>
        <#if mVlanFlag??>
        <mvlan-flag>${mVlanFlag}</mvlan-flag>
        </#if>
        <#if mVlanPri??>
        <mvlan-pri>${mVlanPri}</mvlan-pri>
        </#if>
        <#if serviceType??>
        <service-type>${serviceType}</service-type>
        </#if>
    </l2-infra>
</@base.createIntent>