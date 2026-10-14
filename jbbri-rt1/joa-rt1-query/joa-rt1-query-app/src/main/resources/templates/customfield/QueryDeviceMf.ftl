<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <get-config>
    <source>
      <running/>
    </source>
    <filter type="pruned-subtree">
      <ibn xmlns="http://www.x.com/management-solutions/ibn">
      <#list deviceTarget as target>
        <intent>
          <target>${target}</target>
          <intent-type>device-mf</intent-type>
          <intent-specific-data>
            <device-mf xmlns="http://www.x.com/management-solutions/device-mf">
              <ne-name/>
              <ne-location/>
            </device-mf>
          </intent-specific-data>
        </intent>
      </#list>
      </ibn>
    </filter>
  </get-config>
</rpc>