<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1336053761">
  <get>
    <filter type="subtree">
      <ibn xmlns="http://www.x.com/management-solutions/ibn">
        <intent>
          <target>${ontTarget}</target>
          <intent-type>standard-voip-sip-agent</intent-type>
          <intent-specific-data>
            <standard-voip-sip-agent xmlns="http://www.x.com/management-solutions/standard-voip-sip-agent">
              <voip-l2-infra-name/>
              <ip-address/>
              <netmask/>
              <service-profile/>
            </standard-voip-sip-agent>
          </intent-specific-data>
        </intent>
      </ibn>
    </filter>
  </get>
</rpc>
