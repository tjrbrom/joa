<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1">
    <get>
        <filter type="pruned-subtree">
            <anv:device-manager xmlns:anv="http://www.x.com/management-solutions/anv">
                <adh:device xmlns:adh="http://www.x.com/management-solutions/anv-device-holders">
                    <adh:device-id>${deviceID}</adh:device-id>
                    <adh:device-specific-data>
                        <if:interfaces-state xmlns:if="urn:ietf:params:xml:ns:yang:ietf-interfaces">
                           <if:interface>
                                <if:name>${interfaceName}</if:name>
                                <if:type xmlns:bbf-xponift="urn:bbf:yang:bbf-xpon-if-type">${interfaceType}</if:type>
                                <if:admin-status/>
                                <if:oper-status/>
                            </if:interface>
                        </if:interfaces-state>
                    </adh:device-specific-data>
                </adh:device>
            </anv:device-manager>
        </filter>
    </get>
</rpc>