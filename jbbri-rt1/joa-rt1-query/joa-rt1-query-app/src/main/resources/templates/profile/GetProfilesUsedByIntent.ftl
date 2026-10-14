<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1679160136576">
  <get-profiles-used-by-intent xmlns="http://www.x.com/management-solutions/profile-mgr">
    <filter>
      <intent-type>${intentType}</intent-type>
      <target>${target}</target>
      <profile-type-ui-name>${profileTypeUiName}</profile-type-ui-name>
    </filter>
  </get-profiles-used-by-intent>
</rpc>
