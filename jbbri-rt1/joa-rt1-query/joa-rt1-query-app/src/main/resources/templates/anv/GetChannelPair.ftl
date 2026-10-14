<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1">
    <get-config>
        <source>
            <running/>
        </source>
        <filter type="pruned-subtree">
            <anv:device-manager xmlns:anv="http://www.x.com/management-solutions/anv">
                <adh:device xmlns:adh="http://www.x.com/management-solutions/anv-device-holders">
                    <adh:device-id>${deviceID}</adh:device-id>
                    <adh:device-specific-data>
                        <if:interfaces xmlns:if="urn:ietf:params:xml:ns:yang:ietf-interfaces">
                            <if:interface>
                                <if:type xmlns:bbf-xponift="urn:bbf:yang:bbf-xpon-if-type">bbf-xponift:channel-pair</if:type>
                                <bbf-xpon:channel-pair xmlns:bbf-xpon="urn:bbf:yang:bbf-xpon">
                                    <bbf-xpon:channel-group-ref>${channelGroup}</bbf-xpon:channel-group-ref>
                                </bbf-xpon:channel-pair>
                            </if:interface>
                        </if:interfaces>
                    </adh:device-specific-data>
                </adh:device>
            </anv:device-manager>
        </filter>
    </get-config>
</rpc>