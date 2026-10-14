<rpc message-id="7" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <get>
    <filter type="subtree">
      <mds:manager-directory xmlns:mds="http://www.x.com/management-solutions/manager-directory-service">
        <mds:manager-info>
          <mds:name/>
          <mds:ip/>
          <mds:manager-type>AMS</mds:manager-type>
        </mds:manager-info>
      </mds:manager-directory>
    </filter>
  </get>
</rpc>