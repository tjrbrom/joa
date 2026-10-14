<rpc message-id="101"
  xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'>
  <get-config>
    <source>
      <running />
    </source>
    <filter type='subtree'>
      <pap-management xmlns='http://www.x.com/management-solutions/x-sdnc-pap-management'/>
    </filter>
  </get-config>
</rpc>
