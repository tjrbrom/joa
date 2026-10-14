<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="145501432">  
  <get-config>
    <source>
      <running/>
    </source>
    <filter type="pruned-subtree">
      <ibn xmlns="http://www.x.com/management-solutions/ibn">
        <intent> 
          <intent-specific-data> 
            <fiber xmlns="http://www.x.com/management-solutions/fiber">
              <pon-port> 
                <device-name>${deviceName}</device-name>
                <pon-id>${ponId}</pon-id>
              </pon-port>
              <xpon-type></xpon-type>
            </fiber>
          </intent-specific-data> 
        </intent> 
      </ibn> 
    </filter> 
  </get-config>
</rpc>