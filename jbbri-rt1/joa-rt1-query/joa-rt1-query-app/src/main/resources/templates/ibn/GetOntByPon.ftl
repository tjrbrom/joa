<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1776365039">  
  <get>
    <filter type="pruned-subtree">
      <ibn xmlns="http://www.x.com/management-solutions/ibn">
        <intent>
          <intent-specific-data>
            <ont xmlns="http://www.x.com/management-solutions/ont">
                <fiber-name>${fiberTarget}</fiber-name>
                <ont-type/>
                <onu-id/>
                <ont-description/>
                <expected-serial-number/>
                <expected-registration-id/>
                <expected-loid/>
            </ont>
            <ont-state xmlns="http://www.x.com/management-solutions/ont">
                <detected-fiber-name>${fiberTarget}</detected-fiber-name>
                <onu-id></onu-id>
            </ont-state>
          </intent-specific-data>
        </intent>
      </ibn>
    </filter>
  </get>
</rpc>