<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1">
  <get-config>
    <source>
      <running/>
    </source>
    <filter type="subtree">
      <anv:device-manager xmlns:anv="http://www.x.com/management-solutions/anv">
        <adh:device xmlns:adh="http://www.x.com/management-solutions/anv-device-holders">
          <adh:device-id>${deviceId}.IHUB</adh:device-id>
          <adh:device-specific-data>
            <conf:configure xmlns:conf="urn:x.com:sros:ns:yang:sr:conf">
              <#list ntIdList as ntId>
              <conf:port>
                <conf:port-id>${ntId}</conf:port-id>
                <conf:admin-state/>
                <conf:description/>
                <conf:ethernet>
                  <conf:speed/>
                  <conf:autonegotiate/>
                </conf:ethernet>
              </conf:port>
              </#list>
            </conf:configure>
          </adh:device-specific-data>
        </adh:device>
      </anv:device-manager>
    </filter>
  </get-config>
</rpc>
