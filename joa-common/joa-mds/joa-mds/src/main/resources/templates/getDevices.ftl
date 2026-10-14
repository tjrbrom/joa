<rpc message-id="101"
    xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'>
    <get-config>
        <source>
            <running />
        </source>
        <filter type='subtree'>
            <ibn xmlns='http://www.x.com/management-solutions/ibn'>
                <#list intentTypes as intentType>
                <intent>
                    <intent-type>${intentType}</intent-type>
                    <intent-specific-data xmlns="http://www.x.com/management-solutions/ibn">
                        <${intentType} xmlns='http://www.x.com/management-solutions/${intentType}'>
                            <device-manager>${deviceManager}</device-manager>
                            <ip-address />
                            <device-version />
                            <hardware-type />
                        </${intentType}>
                    </intent-specific-data>
                </intent>
                </#list>
            </ibn>
        </filter>
    </get-config>
</rpc>