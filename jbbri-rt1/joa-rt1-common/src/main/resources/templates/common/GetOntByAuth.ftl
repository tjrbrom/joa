<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1776365039">
<#if onuIdType=='ONU_NUMBER'>
  <get>
    <filter type="pruned-subtree">
<#else>
  <get-config>
    <source>
      <running/>
    </source>
    <filter type="subtree">
</#if>

      <ibn xmlns="http://www.x.com/management-solutions/ibn">
        <intent>
          <intent-specific-data>
            <ont xmlns="http://www.x.com/management-solutions/ont">
              <fiber-name>${fiberName}</fiber-name>

              <#if onuIdType=='MAC'>
                <expected-serial-number>${onuId}</expected-serial-number>
              <#elseif onuIdType=='PASSWORD'>
                <expected-registration-id>${onuId}</expected-registration-id>
              <#elseif onuIdType=='LOID'>
                <expected-loid>${onuId}</expected-loid>
              <#elseif onuIdType=='ONU_NUMBER'>
              </#if>
            </ont>
            <#if onuIdType=='ONU_NUMBER'>
               <ont-state xmlns="http://www.x.com/management-solutions/ont">
                    <detected-fiber-name>${fiberName}</detected-fiber-name>
                    <onu-id>${onuId}</onu-id>
               </ont-state>
            </#if>
          </intent-specific-data>
        </intent>
      </ibn>
    </filter>
<#if onuIdType=='ONU_NUMBER'>
  </get>
<#else>
  </get-config>
</#if>
</rpc>