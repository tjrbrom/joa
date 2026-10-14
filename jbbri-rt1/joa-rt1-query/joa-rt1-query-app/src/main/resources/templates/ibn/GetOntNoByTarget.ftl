<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <get>
    <filter type="pruned-subtree">
      <ibn xmlns="http://www.x.com/management-solutions/ibn">
        <intent>
          <target>${ontTarget}</target>
          <intent-type>ont</intent-type>
          <intent-specific-data>
            <ont-state xmlns="http://www.x.com/management-solutions/ont">
              <onu-id/>
            </ont-state>
          </intent-specific-data>
        </intent>
      </ibn>
    </filter>
  </get>
</rpc>