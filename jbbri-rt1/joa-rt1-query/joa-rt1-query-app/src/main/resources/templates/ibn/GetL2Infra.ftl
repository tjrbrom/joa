<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1336053761">
  <get>
	<filter type="subtree">
	  <ibn xmlns="http://www.x.com/management-solutions/ibn">
		<#list l2InfraTargetList as l2InfraTarget>
		  <intent>
			<target>${l2InfraTarget}</target>
			<intent-type>l2-infra</intent-type>
			<intent-specific-data>
			  <l2-infra	xmlns="http://www.x.com/management-solutions/l2-infra" />
			</intent-specific-data>
		  </intent>
		</#list>
	  </ibn>
	</filter>
  </get>
</rpc>