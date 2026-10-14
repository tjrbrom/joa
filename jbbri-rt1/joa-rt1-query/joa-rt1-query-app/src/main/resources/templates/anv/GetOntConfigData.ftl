<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1">
    <get-config>
      <source>
          <running/>
      </source>
      <filter type="pruned-subtree">
          <anv:device-manager xmlns:anv="http://www.x.com/management-solutions/anv">
              <adh:device xmlns:adh="http://www.x.com/management-solutions/anv-device-holders">
                  <adh:device-id>${deviceId}</adh:device-id>
                  <adh:device-specific-data>
                      <if:interfaces xmlns:if="urn:ietf:params:xml:ns:yang:ietf-interfaces">
                          <if:interface>
                              <if:name>${ontTarget}</if:name>
                              <if:enabled/>
                              <if:type xmlns:bbf-xponift="urn:bbf:yang:bbf-xpon-if-type">bbf-xponift:v-ani</if:type>
                              <bbf-xponvani:v-ani xmlns:bbf-xponvani="urn:bbf:yang:bbf-xponvani">
                                <bbf-xponvani:onu-id/>
                                <bbf-xponvani:expected-serial-number/>
                                <bbf-xponvani:expected-registration-id/>
                                <bbf-xponvani-onu-auth:expected-loid xmlns:bbf-xponvani-onu-auth="urn:bbf:yang:bbf-xponvani-onu-authentication"/>
                              </bbf-xponvani:v-ani>
                          </if:interface>
                       </if:interfaces>        
                  </adh:device-specific-data>
              </adh:device>
          </anv:device-manager>
      </filter>
  </get-config>
</rpc>

