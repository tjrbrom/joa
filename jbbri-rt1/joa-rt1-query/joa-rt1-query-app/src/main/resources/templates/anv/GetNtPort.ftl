<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1">
  <get>
    <filter type="subtree">
      <anv:device-manager xmlns:anv="http://www.x.com/management-solutions/anv">
        <adh:device xmlns:adh="http://www.x.com/management-solutions/anv-device-holders">
          <adh:device-id>${deviceId}.IHUB</adh:device-id>
          <adh:device-specific-data>
            <state:state xmlns:state="urn:x.com:sros:ns:yang:sr:state">
              <state:port>
                <state:port-id></state:port-id>
                <state:oper-state/>
                    <state:port-name/>
                <state:port-state/>
              </state:port>
            </state:state>
          </adh:device-specific-data>
        </adh:device>
      </anv:device-manager>
    </filter>
  </get>
</rpc>

