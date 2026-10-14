<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1336053761">
  <get>
    <filter type="subtree">
      <ibn xmlns="http://www.x.com/management-solutions/ibn">
        <intent>
          <intent-specific-data>
            <standard-voip-sip-user xmlns="http://www.x.com/management-solutions/standard-voip-sip-user">
              <user-device-name>${ontTarget}</user-device-name>
              <uni-id></uni-id>
              <sip-address-of-record/>
              <sip-display-name/>
              <sip-user-name/>
              <sip-password/>
            </standard-voip-sip-user>
          </intent-specific-data>
        </intent>
      </ibn>
    </filter>
  </get>
</rpc>