<rpc xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'>
    <get-config>
	    <source>
            <running/>
        </source>
        <filter type='subtree'>
            <ibn xmlns='http://www.x.com/management-solutions/ibn'>
                <intent>
                  	<intent-specific-data xmlns="http://www.x.com/management-solutions/ibn">
                      	<${deviceType} xmlns="http://www.x.com/management-solutions/${deviceType}">
				            <ip-address>${ipAddress}</ip-address>
                        </${deviceType}>
                    </intent-specific-data>
             </intent>
            </ibn>
        </filter>
    </get-config>
</rpc>
