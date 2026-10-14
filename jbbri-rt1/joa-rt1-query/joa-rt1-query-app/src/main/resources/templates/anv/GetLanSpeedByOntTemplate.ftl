<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1">
        <get-config>
        <source>
            <running/>
        </source>
        <filter type="subtree">
            <anv:device-manager xmlns:anv="http://www.x.com/management-solutions/anv">
                <adh:device xmlns:adh="http://www.x.com/management-solutions/anv-device-holders">
                    <adh:device-id>${deviceID}</adh:device-id>
                    <adh:device-specific-data>
                        <onu:onus xmlns:onu="urn:bbf:params:xml:ns:yang:bbf-fiber-onu-emulated-mount">
                            <onu:onu>
                                <onu:name>${ontTemplateName}</onu:name>
                                <onu:root>
                                    <if-mounted:interfaces xmlns:if-mounted="urn:ietf:params:xml:ns:yang:ietf-interfaces-mounted">
                                     <#list uniIdList as uniId>
                                        <if-mounted:interface>
                                            <if-mounted:name>${uniId}</if-mounted:name>
                                            <eth-mounted:ethernet xmlns:eth-mounted="urn:ieee:params:xml:ns:yang:ethernet-mounted">
                                                <eth-mounted:auto-negotiation>
                                                    <eth-mounted:speed/>
                                                    <eth-mounted:duplex/>
                                                </eth-mounted:auto-negotiation>
                                            </eth-mounted:ethernet>
                                        </if-mounted:interface>
                                      </#list>
                                    </if-mounted:interfaces>
                                </onu:root>
                            </onu:onu>
                        </onu:onus>
                    </adh:device-specific-data>
                </adh:device>
            </anv:device-manager>
        </filter>
    </get-config>
</rpc>