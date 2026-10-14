<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <get>
    <filter type="pruned-subtree">
      <ibn xmlns="http://www.x.com/management-solutions/ibn">
        <intent>
          <target>${ontTarget}</target>
          <intent-type>ont</intent-type>
          <intent-specific-data>
            <ont xmlns="http://www.x.com/management-solutions/ont">
              <uni-service-configuration>
              <uni-id>${uniId?default("")}</uni-id>
              <service-profile/>
              <uni-speed-profile/>
              </uni-service-configuration>
            </ont>
          </intent-specific-data>
        </intent>
      </ibn>
    </filter>
  </get>
</rpc>