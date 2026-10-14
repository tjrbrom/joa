<rpc xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'>
    <get-config>
		<source>
            <running/>
        </source>
        <filter type='subtree'>
            <ibn xmlns='http://www.x.com/management-solutions/ibn'>
                <intent>
                  <intent-specific-data xmlns="http://www.x.com/management-solutions/ibn">
                     <l2-infra xmlns="http://www.x.com/management-solutions/l2-infra">
                        <s-vlan-id>${sVlanId}</s-vlan-id>
                        <c-vlan-id>${cVlanId}</c-vlan-id>
                     </l2-infra>
                  </intent-specific-data>
             </intent>
            </ibn>
        </filter>
    </get-config>
</rpc>