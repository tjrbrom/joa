<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
  <ibn:search-intents xmlns:ibn="http://www.x.com/management-solutions/ibn">
    <ibn:filter>
      <ibn:intent-type-list>
        <ibn:intent-type>${intentType}</ibn:intent-type>
      </ibn:intent-type-list>
      <ibn:device-name>${deviceId}</ibn:device-name>
      <ibn:predicate>MATCHES</ibn:predicate>
      <ibn:order-by>asc.intent-type</ibn:order-by>
      <ibn:config-required>true</ibn:config-required>
      <ibn:include-subsystems>true</ibn:include-subsystems>
    </ibn:filter>
    <ibn:page-number>0</ibn:page-number>
    <ibn:page-size>0</ibn:page-size>
  </ibn:search-intents>
</rpc>