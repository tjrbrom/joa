package com.x.hhqs.joa.rmuid.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.exception.QueryServiceException;
import com.x.hhqs.joa.rmuid.model.Device;
import com.x.hhqs.joa.rmuid.model.HamObjectId;
import com.x.hhqs.joa.rmuid.model.Ont;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

public class IsQueryService implements QueryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsQueryService.class);

    private final ElasticsearchClient esClient;

    public IsQueryService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    @Override
    public Optional<String> getDeviceIp(HamObjectId hamObjectId) {
        try {
            String device = hamObjectId.getValue("device");
            Query termQuery = TermQuery.of(t -> t
                .field("target.device-name.keyword")
                .value(device))._toQuery();

            Query existsQuery = Query.of(q -> q
                .exists(e -> e.field("configuration.ip-address")));

            BoolQuery boolQuery = BoolQuery.of(b -> b
                .filter(termQuery)
                .filter(existsQuery));

            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("intents-device-*")
                .size(1)
                .source(src -> src
                    .filter(f -> f
                        .includes("configuration.ip-address")))
                .query(q -> q.bool(boolQuery)));

            LOGGER.debug("searchRequest: {}", searchRequest);

            SearchResponse<Device> response = esClient.search(searchRequest, Device.class);

            List<Hit<Device>> hits = response.hits().hits();

            if (hits.size() > 0) {
                return Optional.of(hits.get(0).source().getIpAddress());
            }

            return Optional.empty();
        } catch (IOException e) {
            throw new QueryServiceException(e);
        }
    }

    @Override
    public Optional<Integer> getOnuId(HamObjectId hamObjectId) {
        try {
            String ont = hamObjectId.getValue("ont");

            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("intents-ont-*")
                .size(1)
                .source(src -> src
                    .filter(f -> f
                        .includes("state.onu-id")))
                .query(q -> q
                    .bool(b -> b
                        .filter(m -> m
                            .term(t -> t
                                .field("target.ont-name.keyword")
                                .value(ont))))));

            LOGGER.debug("searchRequest: {}", searchRequest);

            SearchResponse<Ont> response = esClient.search(searchRequest, Ont.class);

            List<Hit<Ont>> hits = response.hits().hits();
            if (hits.size() > 0) {
                return Optional.of(hits.get(0).source().getId());
            }

            return Optional.empty();
        } catch (IOException e) {
            throw new QueryServiceException(e);
        }
    }

}
