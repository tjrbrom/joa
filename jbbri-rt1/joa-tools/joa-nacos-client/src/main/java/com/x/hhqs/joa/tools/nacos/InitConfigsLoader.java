package com.x.hhqs.joa.tools.nacos;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InitConfigsLoader {

    private Map<String, String> valueMap = new HashMap<>();

    public List<InitConfig> loadNacosConfigs(Path path) throws Exception {
        var mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(path.toFile());

        List<InitConfig> result = new ArrayList<>();
        if (Files.size(path) == 0) {
            return result;
        }

        if (jsonNode.isObject()) {
            InitConfig config = mapper.convertValue(jsonNode, InitConfig.class);
            result.add(config);
        } else if (jsonNode.isArray()) {
            result = Arrays.asList(mapper.readValue(Files.readString(path), InitConfig[].class));
        } else {
            throw new Exception("Invalid json file: " + path);
        }

        var substitutor = new StringSubstitutor(getValueMap());
        for (var config : result) {
            config.getData().forEach((key, value) -> {
                config.getData().put(key, substitutor.replace(value));
            });
        }

        return result;
    }

    public Map<String, String> getValueMap() {
        return valueMap;
    }

    public void setValueMap(Map<String, String> valueMap) {
        this.valueMap = valueMap;
    }
}
