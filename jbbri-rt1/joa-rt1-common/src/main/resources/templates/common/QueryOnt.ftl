<rpc xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'>
    <get-config>
        <source>
            <running/>
        </source>
        <filter type='subtree'>
            <ibn xmlns='http://www.x.com/management-solutions/ibn'>
                <intent>
                    <intent-specific-data xmlns="http://www.x.com/management-solutions/ibn">
                        <ont xmlns="http://www.x.com/management-solutions/ont">

                            <#if onuIdType=='NAME'>
                                <ont-name>${onuId}</ont-name>
                            <#else>
                                <ont-ip>${onuId}</ont-ip>
                            </#if>
                        </ont>
                    </intent-specific-data>
                </intent>
            </ibn>
        </filter>
    </get-config>
</rpc>