<rpc
    xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'>
    <ibn:delete-intent-from-network-and-controller
        xmlns:ibn='http://www.x.com/management-solutions/ibn'>
        <ibn:target>${intentTarget}</ibn:target>
        <ibn:intent-type>${intentType}</ibn:intent-type>
    </ibn:delete-intent-from-network-and-controller>
</rpc>