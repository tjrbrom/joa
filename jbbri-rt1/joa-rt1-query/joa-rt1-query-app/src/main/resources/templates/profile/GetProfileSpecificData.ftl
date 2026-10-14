<rpc xmlns='urn:ietf:params:xml:ns:netconf:base:1.0' message-id='1678176636948'>
    <get>
        <filter type='subtree'>
            <profile-mgr:profile-mgr
                xmlns:profile-mgr='http://www.x.com/management-solutions/profile-mgr'>
                <profile-mgr:profile>
                    <profile-mgr:profile-type>${profileType}</profile-mgr:profile-type>
                    <profile-mgr:subtype>${subType}</profile-mgr:subtype>
                    <profile-mgr:name>${name}</profile-mgr:name>
                    <profile-mgr:version>${version}</profile-mgr:version>
                    <profile-mgr:base-release>${baseRelease}</profile-mgr:base-release>
                    <profile-mgr:profile-specific-data/>
                </profile-mgr:profile>
            </profile-mgr:profile-mgr>
        </filter>
    </get>
</rpc>
