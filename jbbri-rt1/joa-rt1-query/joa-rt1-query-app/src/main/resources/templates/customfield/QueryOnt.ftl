<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <get-config>
    <source>
      <running/>
    </source>
    <filter type="pruned-subtree">
      <ibn xmlns="http://www.x.com/management-solutions/ibn">
      <#list ontTarget as target>
        <intent>
          <target>${target}</target>
          <intent-type>ont</intent-type>
          <intent-specific-data>
            <ont xmlns="http://www.x.com/management-solutions/ont">
              <ont-name/>
              <ont-ip/>
              <fttx-mode/>
              <vip-tag/>
            </ont>
          </intent-specific-data>
        </intent>
      </#list>
      </ibn>
    </filter>
  </get-config>
</rpc>