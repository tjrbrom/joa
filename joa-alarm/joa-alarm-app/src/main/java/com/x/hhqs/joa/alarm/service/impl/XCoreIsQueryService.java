package com.x.hhqs.joa.alarm.service.impl;

import com.x.hhqs.joa.alarm.exception.DependencyNotReadyException;
import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.model.Device;
import com.x.hhqs.joa.rmuid.model.HamObjectId;
import com.x.hhqs.joa.rmuid.model.Ont;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.opensearch.client.opensearch.core.search.InnerHitsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class XCoreIsQueryService implements QueryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(XCoreIsQueryService.class);

    private final OpenSearchClient openSearchClient;
    private final OpenSearchService openSearchService;

    public XCoreIsQueryService(OpenSearchClient openSearchClient, OpenSearchService openSearchService) {
        this.openSearchClient = openSearchClient;
        this.openSearchService = openSearchService;
    }

    @Override
//    @Cacheable(cacheNames = "getDeviceIp", key = "#hamObjectId.toString()")
    public Optional<String> getDeviceIp(HamObjectId hamObjectId) {
        String device = hamObjectId.getValue("device");

        BoolQuery boolQuery = BoolQuery.of(b -> b
                .filter(f -> f.term(t -> t
                        .field("target.device-name.keyword")
                        .value(FieldValue.of(device))))
                .filter(f -> f.exists(e -> e
                        .field("configuration.ip-address")))
        );

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("intents-device-*")
                .size(1)
                .source(src -> src
                        .filter(f -> f
                                .includes("configuration.ip-address")))
                .query(q -> q.bool(boolQuery)));

        LOGGER.debug("searchRequest: {}", searchRequest);

        SearchResponse<Device> response = null;
        try {
            response = openSearchClient.search(searchRequest, Device.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .map(Device::getIpAddress)
                .filter(Objects::nonNull)
                .findFirst();
    }

    @Override
//    @Cacheable(cacheNames = "getOnuId", key = "#hamObjectId.toString()")
    public Optional<Integer> getOnuId(HamObjectId hamObjectId)  {
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
                                                .value(FieldValue.of(ont)))))));

        LOGGER.debug("searchRequest: {}", searchRequest);

        SearchResponse<Ont> response = null;
        try {
            response = openSearchClient.search(searchRequest, Ont.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .map(Ont::getId)
                .filter(Objects::nonNull)
                .findFirst();
    }

//    @Cacheable(cacheNames = "retrieveXJsonAlarmInfo", key = "#alarmId")
    public Optional<JsonObject> retrieveXJsonAlarmInfo(String alarmId) {
        try {
            Optional<JsonData> optionalJsonData = openSearchService.queryJsonDataWithIndexPatternAndId("alarms-*", alarmId);
            if (optionalJsonData.isEmpty()) {
                return Optional.empty();
            }
            JsonData jsonData = optionalJsonData.get();
            return Optional.ofNullable(jsonData.toJson().asJsonObject());
        } catch (IOException e) {
            throw new DependencyNotReadyException("", e);
        }
    }

//    @Cacheable(cacheNames = "queryDeviceInfo", key = "#device")
    public Optional<JsonObject> queryDeviceInfo(String device) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("intents-device-*")
                    .query(q -> q
                            .bool(b -> b
                                    .filter(m -> m
                                            .term(t -> t
                                                    .field("target.device-name.keyword")
                                                    .value(FieldValue.of(device))))))
                    .source(src -> src
                            .filter(f -> f
                                    .includes(List.of(
                                            "configuration.ip-address",
                                            "configuration.hardware-type",
                                            "configuration.planned-type",
                                            "configuration.ne-name",
                                            "configuration.ne-location"
                                    ))))
                    .size(1));

            LOGGER.debug("searchRequest: {}", searchRequest);

            SearchResponse<JsonData> response = openSearchClient.search(searchRequest, JsonData.class);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .map(JsonData::toJson)
                    .map(JsonValue::asJsonObject)
                    .findFirst();
        } catch (IOException e) {
            throw new DependencyNotReadyException("", e);
        }
    }

//    @Cacheable(cacheNames = "querySlotPlannedType", key = "#device + '_' + #slotName")
    public Optional<JsonObject> querySlotPlannedType(String device, List<String> slotNames) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("intents-device-*")
                    .source(src -> src.fetch(false))
                    .query(q -> q
                            .bool(b -> b
                                    .must(
                                            Query.of(m1 -> m1.term(t -> t
                                                    .field("target.device-name.keyword")
                                                    .value(FieldValue.of(device))
                                            )),
                                            Query.of( m2 -> m2.nested(n -> n
                                                    .path("nested-lists")
                                                    .query(nq -> nq.terms(t -> t
                                                            .field("nested-lists.slot-name.keyword")
                                                            .terms(ts -> ts
                                                                    .value(slotNames.stream()
                                                                            .map(FieldValue::of)
                                                                            .toList())
                                                            )
                                                    ))
                                                    .innerHits(ih -> ih
                                                            .name("matching_slots")
                                                            .source(src -> src
                                                                    .filter(f -> f.includes(List.of(
                                                                            "nested-lists.planned-type"
                                                                    )))
                                                            )
                                                    )
                                            ))
                                    )
                            )
                    )
                    .size(1)
            );
            LOGGER.debug("searchRequest: {}", searchRequest);

            SearchResponse<JsonData> response = openSearchClient.search(searchRequest, JsonData.class);

            return response.hits().hits().stream()
                    .findFirst()
                    .map(Hit::innerHits)
                    .map(innerHitsMap -> innerHitsMap.get("matching_slots"))
                    .filter(Objects::nonNull)
                    .map(InnerHitsResult::hits)
                    .map(HitsMetadata::hits)
                    .flatMap(hits -> hits.stream().findFirst())
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .map(JsonData::toJson)
                    .map(JsonValue::asJsonObject);
        } catch (IOException e) {
            throw new DependencyNotReadyException("", e);
        }
    }

    public Optional<JsonObject> retrieveHamInfo(String hamObjectId) {
        try {
            Optional<JsonData> optionalJsonData = openSearchService.queryJsonDataWithIndexPatternAndId("ham-*", hamObjectId);
            if (optionalJsonData.isEmpty()) {
                return Optional.empty();
            }
            JsonData jsonData = optionalJsonData.get();
            return Optional.ofNullable(jsonData.toJson().asJsonObject());
        } catch (IOException e) {
            throw new DependencyNotReadyException("", e);
        }
    }
}
