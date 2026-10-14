<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="145501432">  
  <get> 
    <filter type="pruned-subtree"> 
      <ibn xmlns="http://www.x.com/management-solutions/ibn">
        <intent> 
          <intent-specific-data> 
            <fiber xmlns="http://www.x.com/management-solutions/fiber">
              <pon-port> 
                <device-name>${deviceId}</device-name>  
                <pon-id></pon-id> 
              </pon-port> 
            </fiber> 
          </intent-specific-data> 
        </intent> 
      </ibn> 
    </filter> 
  </get> 
</rpc>