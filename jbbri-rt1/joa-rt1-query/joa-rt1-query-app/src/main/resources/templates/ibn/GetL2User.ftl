<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1336053761">
  <get>
    <filter type="subtree">
      <ibn xmlns="http://www.x.com/management-solutions/ibn">
        <intent>
          <intent-specific-data>
            <l2-user xmlns="http://www.x.com/management-solutions/l2-user">
              <user-device-name>${ontTarget}</user-device-name>
              <uni-id>${uniId}</uni-id>
              <l2-infra/>
              <q-vlan-id/>
              <transparent-q-vlan-id/>
              <n-vlan-id/>
              <multiple-q-vlan-id/>
            </l2-user>
          </intent-specific-data>
        </intent>
      </ibn>
    </filter>
  </get>
</rpc>