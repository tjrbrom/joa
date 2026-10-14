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
              <ont-description/>
              <expected-serial-number/>
              <expected-registration-id/>
              <expected-loid/>
              <ont-speed-profile/>
              <active-software/>
            </ont>
            <ont-state xmlns="http://www.x.com/management-solutions/ont">
              <detected-fiber-name></detected-fiber-name>  
              <onu-id/>
              <detected-serial-number/>
              <detected-registration-id/>
              <detected-loid/>
              <actual-active-software-on-device/>
              <onu-state/>
            </ont-state>
          </intent-specific-data>
        </intent>
      </ibn>
    </filter>
  </get>
</rpc>