package com.x.hhqs.joa.rmuid.model;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.x.hhqs.joa.rmuid.exception.JoaRmuidException;

public class OntDeserializer extends JsonDeserializer<Ont> {

    @Override
    public Ont deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);

        JsonNode stateNode = rootNode.path("state");
        if (stateNode.isMissingNode()) {
            throw new JoaRmuidException("Missing 'state' node in JSON");
        }

        JsonNode idNode = stateNode.path("onu-id");
        if (idNode.isMissingNode()) {
            throw new JoaRmuidException("Missing 'onu-id' node in 'state'");
        }

        if (!idNode.isArray()) {
            throw new JoaRmuidException("'onu-id' should be an array");
        }

        Ont ont = new Ont();
        if (idNode.size() > 0) {
            ont.setId(idNode.get(0).asInt());
        } else {
            throw new JoaRmuidException("'onu-id' array is empty");
        }

        return ont;
    }
}
