<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <get>
    <filter type="pruned-subtree">
      <ibn xmlns="http://www.x.com/management-solutions/ibn">
        <intent>
          <target>${deviceId}</target>
          <intent-type>device-mf</intent-type>
          <intent-specific-data>
            <device-mf xmlns="http://www.x.com/management-solutions/device-mf">
              <ip-address/>
              <ne-name/>
              <ne-location/>
              <hardware-type/>
              <device-version/>
              <partition-access-profile/>
            </device-mf>
            <device-mf-state xmlns="http://www.x.com/management-solutions/device-mf">
              <reachable/>
              <actual-active-software-on-device/>
            </device-mf-state>
          </intent-specific-data>
        </intent>
      </ibn>
    </filter>
  </get>
</rpc>