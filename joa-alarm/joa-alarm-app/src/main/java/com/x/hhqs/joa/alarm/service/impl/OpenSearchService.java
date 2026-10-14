package com.x.hhqs.joa.alarm.service.impl;

import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class OpenSearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenSearchService.class);

    private final OpenSearchClient openSearchClient;

    public OpenSearchService(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    public Optional<JsonData> queryJsonDataWithIndexPatternAndId(String indexPattern, String id) throws IOException {
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexPattern)
                .query(q -> q
                        .term(t -> t
                                .field("_id")
                                .value(FieldValue.of(id))
                        )
                )
                .size(1)
        );
        SearchResponse<JsonData> searchResponse = openSearchClient.search(searchRequest, JsonData.class);
        if (searchResponse.hits().hits().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(searchResponse.hits().hits().get(0).source());
    }
}
