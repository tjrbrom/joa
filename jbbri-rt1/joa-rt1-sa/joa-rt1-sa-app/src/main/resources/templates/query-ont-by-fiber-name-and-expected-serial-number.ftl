<rpc xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'>
    <get-config>
        <source>
            <running/>
        </source>
        <filter type='subtree'>
            <ibn xmlns='http://www.x.com/management-solutions/ibn'>
                <intent>
                    <intent-specific-data xmlns="http://www.x.com/management-solutions/ibn">
                        <ont xmlns="http://www.x.com/management-solutions/ont">
                          	<fiber-name>${fiberName}</fiber-name>
                            <expected-serial-number>${expectedSerialNumber}</expected-serial-number>
                        </ont>
                    </intent-specific-data>
                </intent>
            </ibn>
        </filter>
    </get-config>
</rpc>