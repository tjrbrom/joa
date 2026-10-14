<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1">
  <get>
    <filter type="pruned-subtree">
      <anv:device-manager xmlns:anv="http://www.x.com/management-solutions/anv">
        <adh:device xmlns:adh="http://www.x.com/management-solutions/anv-device-holders">
          <adh:device-id>${deviceId}</adh:device-id>
          <adh:device-specific-data>
            <onu:onus xmlns:onu="urn:bbf:params:xml:ns:yang:bbf-fiber-onu-emulated-mount">
            <#list voipUniIdList as voipUniId>
              <onu:onu>
                <onu:name>${ontTarget}</onu:name>
                <onu:root>
                  <interfaces-state xmlns="urn:ietf:params:xml:ns:yang:ietf-interfaces-mounted">
                    <interface>
                      <name>${voipUniId}</name>
                      <oper-status/>
                      <admin-status/>
                    </interface>
                  </interfaces-state>
                  <sip-signaling-state xmlns="urn:bbf:yang:bbf-sip-voip-mounted">
                    <sip-terminations>
                      <sip-termination>
                        <sip-user-name>${voipUniId}</sip-user-name>
                        <sip-termination-call-state/>
                        <sip-termination-registration-state/>
                      </sip-termination>
                    </sip-terminations>
                  </sip-signaling-state>
                </onu:root>
              </onu:onu>
              </#list>
            </onu:onus>
          </adh:device-specific-data>
        </adh:device>
      </anv:device-manager>
    </filter>
  </get>
</rpc>
