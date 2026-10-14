<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1336053761">
  <get>
    <filter type="subtree">
      <ibn xmlns="http://www.x.com/management-solutions/ibn">
      <#list l2UserTargetList as l2UserTarget>
        <intent>
          <target>${l2UserTarget}</target>
          <intent-type>l2-user</intent-type>
          <intent-specific-data>
            <l2-user xmlns="http://www.x.com/management-solutions/l2-user">
              <user-device-name/>
              <uni-id></uni-id>
              <l2-infra/>
              <q-vlan-id/>
              <transparent-q-vlan-id/>
              <n-vlan-id/>
              <multiple-q-vlan-id/>
            </l2-user>
          </intent-specific-data>
        </intent>
        </#list>
      </ibn>
    </filter>
  </get>
</rpc>