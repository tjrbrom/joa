<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1776365039">  
  <get>
    <filter type="subtree">
      <ibn xmlns="http://www.x.com/management-solutions/ibn">
        <#list fiberTargetList as fiberTarget>
        <intent>
          <intent-specific-data>
            <ont xmlns="http://www.x.com/management-solutions/ont">
                <fiber-name>${fiberTarget}</fiber-name>
                <ont-type/>
                <onu-id/>
                <ont-description/>
                <expected-serial-number/>
                <expected-registration-id/>
            </ont>
            <ont-state xmlns="http://www.x.com/management-solutions/ont">
                <detected-fiber-name>${fiberTarget}</detected-fiber-name>
                <onu-id></onu-id>
                <detected-serial-number/>
                <detected-registration-id/>
                <actual-active-software-on-device/>
            </ont-state>
          </intent-specific-data>
        </intent>
        </#list>
      </ibn>
    </filter>
  </get>
</rpc>