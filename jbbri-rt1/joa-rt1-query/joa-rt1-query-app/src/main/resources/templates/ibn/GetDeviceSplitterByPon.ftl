<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="145501432">  
  <get> 
    <filter type="pruned-subtree"> 
      <ibn xmlns="http://www.x.com/management-solutions/ibn">
        <intent> 
          <intent-specific-data> 
            <device-splitter xmlns="http://www.x.com/management-solutions/device-splitter">
              <fiber-name>${fiberTarget}</fiber-name>
              <up-splitter/>
              <ratio/>
              <topPosPort/>
            </device-splitter> 
          </intent-specific-data> 
        </intent> 
      </ibn> 
    </filter> 
  </get> 
</rpc>
