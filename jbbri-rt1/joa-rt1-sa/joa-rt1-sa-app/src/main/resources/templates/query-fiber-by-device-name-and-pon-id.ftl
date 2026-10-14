<rpc xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'>
    <get-config>
        <source>
            <running/>
        </source>
        <filter type='subtree'>
            <ibn xmlns='http://www.x.com/management-solutions/ibn'>
                <intent>
                    <intent-specific-data xmlns="http://www.x.com/management-solutions/ibn">
                        <fiber xmlns="http://www.x.com/management-solutions/fiber">
                            <pon-port>
                                <device-name>${deviceName}</device-name>
                                <pon-id>${ponId}</pon-id>
                            </pon-port>
                        </fiber>
                    </intent-specific-data>
                </intent>
            </ibn>
        </filter>
    </get-config>
</rpc>