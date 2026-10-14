<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1">
  <get-config>
    <source>
      <running/>
    </source>
    <filter type="pruned-subtree">
      <anv:device-manager xmlns:anv="http://www.x.com/management-solutions/anv">
        <adh:device xmlns:adh="http://www.x.com/management-solutions/anv-device-holders">
          <adh:device-id>${deviceId}</adh:device-id>
          <adh:device-specific-data>
            <onu:onus xmlns:onu="urn:bbf:params:xml:ns:yang:bbf-fiber-onu-emulated-mount">
              <onu:onu>
                <onu:name>${ontTarget}</onu:name>
                <onus-from-template:template-parameters xmlns:onus-from-template="http://www.x.com/Fixed-Networks/BBA/yang/onus-from-template">
                  <onus-from-template:voip>
                    <onus-from-template:sip-termination>
                      <onus-from-template:name>${voipUniId}</onus-from-template:name>
                      <onus-from-template:auth-user-name/>
                      <onus-from-template:digest-password/>
                      <onus-from-template:sip-uni-display-name/>
                      <onus-from-template:sip-uni-uri/>
                    </onus-from-template:sip-termination>
                  </onus-from-template:voip>
                </onus-from-template:template-parameters>
              </onu:onu>
            </onu:onus>
          </adh:device-specific-data>
        </adh:device>
      </anv:device-manager>
    </filter>
  </get-config>
</rpc>
