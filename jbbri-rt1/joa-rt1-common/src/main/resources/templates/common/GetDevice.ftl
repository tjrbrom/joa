<rpc message-id="101" xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'>
    <get-config>
        <source>
          <running/>
        </source>
        <filter type='subtree'>
            <ibn xmlns='http://www.x.com/management-solutions/ibn'>
                <intent>
                    <intent-specific-data xmlns="http://www.x.com/management-solutions/ibn">
                        <device-mf xmlns="http://www.x.com/management-solutions/device-mf">
                            <#if oltIdType=='NAME'>
                                <ne-name>${oltId}</ne-name>
                            <#else>
                                <ip-address>${oltId}</ip-address>
                            </#if>
                        </device-mf>
                    </intent-specific-data>
                </intent>
            </ibn>
        </filter>
    </get-config>
</rpc>