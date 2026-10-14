<rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1">
  <get>
    <filter type="pruned-subtree">
      <anv:device-manager xmlns:anv="http://www.x.com/management-solutions/anv">
        <adh:device xmlns:adh="http://www.x.com/management-solutions/anv-device-holders">
          <adh:device-id>${deviceID}</adh:device-id>
          <adh:device-specific-data>
            <if:interfaces-state xmlns:if="urn:ietf:params:xml:ns:yang:ietf-interfaces">
            <#list onuTargetList as onuTarget>
              <if:interface>
                <if:name>${onuTarget}</if:name>
                <if:oper-status/>
                <if:last-change/>
                <bbf-xponvani:v-ani xmlns:bbf-xponvani="urn:bbf:yang:bbf-xponvani">
                  <onu-last-down-reason xmlns="http://www.x.com/Fixed-Networks/BBA/yang/x-sdan-if-xponvani-aug"/>
                  <onu-present-on-this-olt xmlns="urn:bbf:yang:bbf-xponvani">
                    <onu-fiber-distance/>
                  </onu-present-on-this-olt>
                </bbf-xponvani:v-ani>
              </if:interface>
            </#list>
            </if:interfaces-state>
          </adh:device-specific-data>
        </adh:device>
      </anv:device-manager>
    </filter>
  </get>
</rpc>