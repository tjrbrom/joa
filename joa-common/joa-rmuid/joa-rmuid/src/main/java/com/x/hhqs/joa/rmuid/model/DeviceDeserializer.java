package com.x.hhqs.joa.rmuid.model;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.x.hhqs.joa.rmuid.exception.JoaRmuidException;

public class DeviceDeserializer extends JsonDeserializer<Device> {

    @Override
    public Device deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
        JsonNode configurationNode = rootNode.path("configuration");

        if (configurationNode.isMissingNode()) {
            throw new JoaRmuidException("Missing 'configuration' node in JSON");
        }

        JsonNode ipAddressNode = configurationNode.path("ip-address");

        if (ipAddressNode.isMissingNode()) {
            throw new JoaRmuidException("Missing 'ip-address' node in 'configuration'");
        }

        if (!ipAddressNode.isArray()) {
            throw new JoaRmuidException("'ip-address' should be an array");
        }

        Device device = new Device();
        if (ipAddressNode.size() > 0) {
            device.setIpAddress(ipAddressNode.get(0).asText());
        } else {
            throw new JoaRmuidException("'ip-address' array is empty");
        }
        return device;
    }
}
