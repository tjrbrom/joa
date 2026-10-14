<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1336053761">
  <get>
	<filter type="subtree">
	  <ibn xmlns="http://www.x.com/management-solutions/ibn">
		<#list mcastUserTargetList as mcastUserTarget>
		  <intent>
			<target>${mcastUserTarget}</target>
			<intent-type>mcast-user</intent-type>
			<intent-specific-data>
			  <mcast-user xmlns="http://www.x.com/management-solutions/mcast-user">
                <mcast-infra-name></mcast-infra-name>
                <service-profile></service-profile>
              </mcast-user>
			</intent-specific-data>
		  </intent>
		</#list>
	  </ibn>
	</filter>
  </get>
</rpc>