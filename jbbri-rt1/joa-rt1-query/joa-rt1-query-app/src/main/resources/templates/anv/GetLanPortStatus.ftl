<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1">
        <get>
        <filter type="pruned-subtree">
            <anv:device-manager xmlns:anv="http://www.x.com/management-solutions/anv">
                <adh:device xmlns:adh="http://www.x.com/management-solutions/anv-device-holders">
                    <adh:device-id>${deviceID}</adh:device-id>
                    <adh:device-specific-data>
                       <onu:onus xmlns:onu="urn:bbf:params:xml:ns:yang:bbf-fiber-onu-emulated-mount">
                            <onu:onu>
                                <onu:name>${ontTarget}</onu:name>
                                <onu:root>
                                    <if-mounted:interfaces-state xmlns:if-mounted="urn:ietf:params:xml:ns:yang:ietf-interfaces-mounted">
                                      <#list uniIdList as uniId>
                                        <if-mounted:interface>
                                            <if-mounted:name>${uniId}</if-mounted:name>
                                            <if-mounted:admin-status/>
                                            <if-mounted:oper-status/>
                                        </if-mounted:interface>
                                      </#list>
                                    </if-mounted:interfaces-state>
                                   </onu:root>
                            </onu:onu>
                        </onu:onus>
                    </adh:device-specific-data>
                </adh:device>
            </anv:device-manager>
        </filter>
    </get>
</rpc>
