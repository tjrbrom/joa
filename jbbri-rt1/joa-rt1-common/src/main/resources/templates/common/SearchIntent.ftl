<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="145501432">
    <get-config>
        <source>
            <running/>
        </source>
        <filter type="subtree">
            <ibn xmlns="http://www.x.com/management-solutions/ibn">
                <intent>
                    <intent-type>${intentType}</intent-type>
                    <target>${targetName}</target>
                    <required-network-state></required-network-state>
                    <intent-specific-data></intent-specific-data>
                </intent>
            </ibn>
        </filter>
    </get-config>
</rpc>