<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <get>
    <filter type="pruned-subtree">
      <ibn xmlns="http://www.x.com/management-solutions/ibn">
        <intent>
          <target>${ontTarget}</target>
          <intent-type>ont</intent-type>
          <intent-specific-data>
            <ont xmlns="http://www.x.com/management-solutions/ont">
              <fiber-name/>
              <onu-id/>
              <ont-type/>
              <expected-serial-number/>
              <expected-registration-id/>
              <expected-loid/>
              <ont-speed-profile/>
              <active-software/>
            </ont>
          </intent-specific-data>
        </intent>
      </ibn>
    </filter>
  </get>
</rpc>