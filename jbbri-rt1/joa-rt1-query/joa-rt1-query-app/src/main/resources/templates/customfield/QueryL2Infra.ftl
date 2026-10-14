<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <get-config>
    <source>
      <running/>
    </source>
    <filter type="pruned-subtree">
      <ibn xmlns="http://www.x.com/management-solutions/ibn">
      <#list l2InfraTarget as target>
        <intent>
          <target>${target}</target>
          <intent-type>l2-infra</intent-type>
          <intent-specific-data>
            <l2-infra xmlns="http://www.x.com/management-solutions/l2-infra">
              <service-description/>
              <mvlan-flag/>
              <mvlan-pri/>
              <service-type/>
            </l2-infra>
          </intent-specific-data>
        </intent>
      </#list>
      </ibn>
    </filter>
  </get-config>
</rpc>