package com.x.hhqs.joa.alarm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.x.hhqs.joa.alarm.config.JoaAlarmConfig;
import com.x.hhqs.joa.alarm.config.XAlarmProperties;
import com.x.hhqs.joa.alarm.dto.*;
import com.x.hhqs.joa.alarm.entity.JoaAlarm;
import com.x.hhqs.joa.alarm.exception.UnsupportedAlarmException;
import com.x.hhqs.joa.alarm.listener.kafka.processor.XCoreAlarmMessageProcessor;
import com.x.hhqs.joa.alarm.model.OmcRequest;
import com.x.hhqs.joa.alarm.model.PagingInput;
import com.x.hhqs.joa.alarm.service.impl.XCoreIsQueryService;
import com.x.hhqs.joa.alarm.utils.ChannelIndexUtil;
import com.x.hhqs.joa.alarm.utils.DateTimeUtils;
import com.x.hhqs.joa.alarm.utils.SyncAlarmFile;

import com.x.hhqs.joa.alarm.utils.SyncAlarmFileUtil;
import com.x.hhqs.joa.alarm.vo.AlarmDefinitionInfoVO;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.checkerframework.checker.units.qual.A;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.RangeQuery;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.x.hhqs.joa.alarm.dto.XAlarmContextFields.HAM_OBJECT_ID;
import static com.x.hhqs.joa.alarm.dto.XAlarmContextFields.MAIN_DEVICE_FIELD;

@Component
@Slf4j
public class SyncActiveAlarmFileService {

    private static final String tempDir = System.getProperty("java.io.tmpdir");

    private final int PAGE_SIZE = 5000;

    @Resource
    private JoaAlarmConfig joaAlarmConfig;

    @Autowired
    private FileUploadService fileUploadService;

    @Resource
    private FileDownLoadService fileDownLoadService;

    @Resource
    OpenSearchClient openSearchClient;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private  AlarmDefinitionService alarmDefinitionService;

    @Autowired
    @Qualifier("xCoreAlarmMessageProcessor")
    private XCoreAlarmMessageProcessor processor;

    @Resource
    private  XAlarmProperties xAlarmProperties;

    @Resource
    private XCoreIsQueryService xCoreIsQueryService;

    public void writeActiveAlarms(List<String> amsFilePath,
                                  FileWriteContext writeContext,
                                  String remoteIp,
                                  int channelIndex,
                                  List<JoaAlarm> joaAlarms) throws Exception {
        try {
            log.info("channelIndex[{}] writeActiveAlarms, core alarm size[{}]", channelIndex, joaAlarms.size());
            AtomicLong alarmSeq = new AtomicLong(0);
            readAmsAlarmFiles(amsFilePath, alarmSeq, writeContext, remoteIp, channelIndex);
            joaAlarms.forEach(it -> {
                long incrementAndGet = alarmSeq.incrementAndGet();
                it.getJsonValue().setAlarmSeq(incrementAndGet);
                it.alarmSeq(incrementAndGet);
            });
            writeAlarmsToFile(joaAlarms, writeContext);
        } catch (Exception e) {
            cleanupResources(writeContext);
            log.error("", e);
            throw e;
        }
    }

    public List<JoaAlarm> queryESActiveAlarms(LocalDateTime sTime, LocalDateTime eTime, int channelIndex) throws Exception {
        int page = 0;
        List<AlarmDefinitionInfoVO> allAlarmDefinitions = alarmDefinitionService.getAllAlarmDefinitions();
        List<JoaAlarm> batchAlarms = new ArrayList<>();
        Map<String, AlarmDefinitionInfoVO> alarmDefinitionMap = allAlarmDefinitions.stream()
                .collect(Collectors.toMap(AlarmDefinitionInfoVO::getAlarmTypeId, Function.identity()));
        while (true) {
            SearchRequest searchRequest = buildSearchRequest(sTime, eTime, page);
            SearchResponse<JsonData> response = openSearchClient.search(searchRequest, JsonData.class);
            log.info("total-{}, query es alarms current={}", response.hits().total().value(), page);
            batchAlarms.addAll(processAlarmBatch(channelIndex, response, alarmDefinitionMap));
            if (batchAlarms.size() < PAGE_SIZE) break;
            page++;
        }
        return batchAlarms;
    }

    private SearchRequest buildSearchRequest(LocalDateTime sTime, LocalDateTime eTime, int page) {
        PagingInput paging = new PagingInput(page * PAGE_SIZE, PAGE_SIZE);
        return alarmSearchBuilder(sTime, eTime, paging);
    }

    private SearchRequest alarmSearchBuilder(LocalDateTime sTime, LocalDateTime eTime, PagingInput pagingInput) {
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        boolBuilder.must(m -> m.terms(t -> t
                .field("alarmNotificationOrigin")
                .terms(ts -> ts.value(xAlarmProperties.getAllowedOrigins().stream()
                        .map(FieldValue::of)
                        .toList()))));
        if (sTime != null) {
            addRangeCondition(boolBuilder, "gt", sTime);
        }
        addRangeCondition(boolBuilder, "lt", eTime);

        BoolQuery boolQuery = boolBuilder.build();

        Query query = boolQuery.must().isEmpty()
                ? Query.of(q -> q.matchAll(m -> m))
                : Query.of(q -> q.bool(boolQuery));

        return SearchRequest.of(s -> s
                .index("alarms-active")
                .query(query)
                .sort(sort -> sort.field(f -> f.field("raisedTime").order(SortOrder.Asc)))
                .from(pagingInput.getFirstResult())
                .size(pagingInput.getMaxResults()));
    }

    private void addRangeCondition(BoolQuery.Builder boolBuilder, String operator, LocalDateTime time) {
        RangeQuery.Builder rangeBuilder = new RangeQuery.Builder().field("raisedTime");
        String formatterUTCDate = DateTimeUtils.formatterUTCDate(time);
        switch (operator) {
            case "gt":
                rangeBuilder.gt(JsonData.of(formatterUTCDate));
                break;
            case "lt":
                rangeBuilder.lt(JsonData.of(formatterUTCDate));
                break;
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
        boolBuilder.must(m -> m.range(rangeBuilder.build()));
    }
    public void readAmsAlarmFiles(List<String> files, AtomicLong alarmSeq, FileWriteContext writeContext,String remoteIp, int channelIndex){
        files.forEach(fileName -> {
            processSingleFile(fileName, alarmSeq, writeContext, remoteIp, channelIndex);
        });
    }

    private void processSingleFile(String fileName, AtomicLong alarmSeq, FileWriteContext writeContext, String remoteIp, int channelIndex) {
        long currentLine = 0;
        long channelIndexLine = 0;
        List<JoaAlarm> joaAlarms = new ArrayList<>(PAGE_SIZE);
        String downloadFile = fileDownLoadService.downloadFile(fileName, remoteIp);

        if (downloadFile == null) {
            return;
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(downloadFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory() || !entry.getName().endsWith(".txt")) {
                    zis.closeEntry();
                    continue;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(new NoCloseInputStream(zis), StandardCharsets.UTF_8));
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        JoaAlarm joaAlarm = processLine(line, alarmSeq, channelIndex);
                        currentLine++;
                        if (joaAlarm ==null) continue;
                        joaAlarms.add(joaAlarm);
                        channelIndexLine++;
                        if (currentLine % PAGE_SIZE == 0) {
                            writeAlarmsToFile(joaAlarms, writeContext);
                            joaAlarms.clear();
                        }
                    }
                } finally {
                    reader.close();
                    zis.closeEntry();
                }
            }
            if (!joaAlarms.isEmpty()) {
                writeAlarmsToFile(joaAlarms, writeContext);
            }
            log.info("read fileName-{}, total line-{}, channelIndex-{} total-{}", downloadFile, currentLine, channelIndex, channelIndexLine);
        } catch (Exception e) {
            log.error("read file[{}] error!", downloadFile, e);
            throw new RuntimeException(e.getMessage());
        } finally {
            log.info("delete download file [{}]", downloadFile);
            cleanupTempFiles(List.of(downloadFile));
        }
    }

    private JoaAlarm processLine(String line, AtomicLong alarmSeq, int channelIndex) throws JsonProcessingException {
        if (line.isEmpty()) return null;
        Map<String, Object> amsAlarmMap = objectMapper.readValue(line, new TypeReference<>() {});
        String neIp = (String) amsAlarmMap.get("neIP");
        if (channelIndex != ChannelIndexUtil.getChannelIndex(neIp, joaAlarmConfig.getServer().getChannelNumber())) {
            return null;
        }
        amsAlarmMap.remove("neIP");
        JoaAlarmInfo joaAlarmInfo = objectMapper.convertValue(amsAlarmMap, JoaAlarmInfo.class);
        joaAlarmInfo.setAlarmSeq(alarmSeq.incrementAndGet());
        JoaAlarm joaAlarm = new JoaAlarm();
        joaAlarm.jsonValue(joaAlarmInfo);

        return joaAlarm;
    }

    private void writeAlarmsToFile(List<JoaAlarm> alarms, FileWriteContext ctx) throws Exception {
        if (alarms.isEmpty()) return;

        if (ctx.getWriter() == null) {
            throw new IllegalStateException("No active writer after rotation");
        }
        String lineSeparator = System.lineSeparator();
        final int lineSeparatorLength = lineSeparator.length();
        try {
            for (JoaAlarm alarm : alarms) {
                String jsonStr = objectMapper.writeValueAsString(alarm.getJsonValue());
                long alarmSize = (long) jsonStr.length() + lineSeparatorLength;
                if (ctx.getCurrentSize() + alarmSize > joaAlarmConfig.getMaxAlarmFileSize()) {
                    rotateFile(ctx);
                }
                ctx.getWriter().write(jsonStr);
                ctx.getWriter().write(lineSeparator);
                ctx.setCurrentSize(ctx.getCurrentSize() + alarmSize);
            }
            ctx.getWriter().flush();
        } catch (Exception e) {
            log.error("Write failed, closing resources", e);
            throw e;
        }
    }

    private List<JoaAlarm> processAlarmBatch(int channelIndex, SearchResponse<JsonData> response, Map<String, AlarmDefinitionInfoVO> alarmDefinitionMap) {

        return response.hits().hits().stream()
                .filter(f -> f.source() != null)
                .map(hit->{
                    ActiveAlarmVO activeAlarm = hit.source().to(ActiveAlarmVO.class);
                    XAlarmVO xAlarmVO = convertToXAlarmVO(activeAlarm, hit.id());
                    return convertHitToAlarm(channelIndex, xAlarmVO, hit.source().toJson().asJsonObject(),
                            alarmDefinitionMap.get(activeAlarm.getAlarmTypeId()), activeAlarm.getMainDeviceRefId());
                    /*if (joaAlarmConfig.getServer().getChannelNumber() == 1) {
                        return convertHitToAlarm(channelIndex, xAlarmVO, hit.source().toJson().asJsonObject(),
                                alarmDefinitionMap.get(activeAlarm.getAlarmTypeId()), activeAlarm.getMainDeviceRefId());
                    }
                    AtomicReference<String> neIp = new AtomicReference<>();
                    Optional<JsonObject> optionalJsonObject = xCoreIsQueryService.queryDeviceInfo(activeAlarm.getMainDeviceRefId());
                    optionalJsonObject.map(obj -> obj.getJsonObject("configuration")).ifPresent(confNode -> {
                        if (confNode.containsKey(XAlarmContextFields.NE_IP)) {
                            JsonArray arr = confNode.getJsonArray(XAlarmContextFields.NE_IP);
                            if (!arr.isEmpty() && arr.get(0).getValueType() == JsonValue.ValueType.STRING) {
                                neIp.set(((JsonString) arr.get(0)).getString());
                            }
                        }
                    });
                    int neIpChannelIndex = ChannelIndexUtil.getChannelIndex(neIp.get(), joaAlarmConfig.getServer().getChannelNumber());
                    log.info("neIpChannelIndex-{}---------------------{}------------------{}---------", neIpChannelIndex, channelIndex, neIp.get());
                    return neIpChannelIndex != channelIndex ? null : convertHitToAlarm(channelIndex, xAlarmVO, hit.source().toJson().asJsonObject(),
                            alarmDefinitionMap.get(activeAlarm.getAlarmTypeId()), activeAlarm.getMainDeviceRefId());*/
                }).filter(Objects::nonNull)
                .toList();

    }

    private JoaAlarm convertHitToAlarm(int channelIndex, XAlarmVO xAlarmVO, JsonObject jsonObject, AlarmDefinitionInfoVO alarmDefinitionInfoVO, String mainDeviceRefId) {
        try {
            JoaAlarm joaAlarm = processXAlarmVO(xAlarmVO, jsonObject, alarmDefinitionInfoVO, mainDeviceRefId);
            if (joaAlarm == null || joaAlarm.getChannelIndex() != channelIndex) return null;
            JoaAlarmInfo joaAlarmInfo = joaAlarm.getJsonValue();
            joaAlarmInfo.setEventTime(DateTimeUtils.dateTimeConverter(xAlarmVO.getRaisedTime()));
            joaAlarm.jsonValue(joaAlarmInfo);
            return joaAlarm;
        } catch (Exception e) {
            log.error("{}", xAlarmVO.getAlarmId(), e);
            throw e;
        }
    }


    private JoaAlarm processXAlarmVO(XAlarmVO xAlarmVO, JsonObject jsonObject, AlarmDefinitionInfoVO alarmDefinition, String mainDeviceRefId) {
        String alarmTypeId = xAlarmVO.getAlarmTypeId();
        if (alarmDefinition != null) {
            try {
                XAlarmContext xAlarmContext = XAlarmContext.of(xAlarmVO, alarmDefinition, jsonObject);
                processor.extractXAlarmContext(xAlarmContext, alarmDefinition, xAlarmVO.getAlarmId());
                return processor.constructJoaAlarm(xAlarmContext);
            } catch (UnsupportedAlarmException e) {
                log.warn("reqSyncAlarmFile UnsupportedAlarmException! errorMessage {}", e.getMessage());
                return null;
            }
        } else {
            log.warn("AlarmDefinition is null for alarmId: {}", alarmTypeId);
        }
        return null;
    }

    private XAlarmVO convertToXAlarmVO(ActiveAlarmVO activeAlarmVO, String alarmId) {
        XAlarmVO xAlarmVO = new XAlarmVO();
        xAlarmVO.setAlarmId(alarmId);
        xAlarmVO.setAlarmPap(activeAlarmVO.getPAP());
        xAlarmVO.setAlarmNotificationOrigin(activeAlarmVO.getAlarmNotificationOrigin());
        xAlarmVO.setAlarmResource(activeAlarmVO.getAlarmResource());
        xAlarmVO.setAlarmResourceUiName(activeAlarmVO.getAlarmResourceUiName());
        xAlarmVO.setAlarmSeverity(activeAlarmVO.getAlarmSeverity());
        xAlarmVO.setAlarmStatus(activeAlarmVO.getAlarmStatus());

        xAlarmVO.setAlarmType(activeAlarmVO.getAlarmType());

        xAlarmVO.setAlarmText(activeAlarmVO.getAlarmText());
        xAlarmVO.setAlarmTypeId(activeAlarmVO.getAlarmTypeId());
        xAlarmVO.setLastStatusChangeTime(activeAlarmVO.getLastStatusChangeTime());
        xAlarmVO.setObjectId(activeAlarmVO.getObjectId());
        xAlarmVO.setProposedRepairAction(activeAlarmVO.getProposedRepairAction());
        xAlarmVO.setRaisedTime(activeAlarmVO.getRaisedTime());
        return xAlarmVO;
    }

    public FileWriteContext initializeFileWriter(String reqId) throws Exception {
        FileWriteContext ctx = new FileWriteContext();
        ctx.setRemotePath(ctx.getCurrentDate());
        ctx.setTargetFile(new SyncAlarmFile(reqId, joaAlarmConfig.getVersion()));

        File tmpFile = createTempFile(ctx.getRemotePath(), ctx.getTargetFile());

        ctx.setWriter(createBufferedWriter(tmpFile));
        ctx.setCurrentSize(0L);
        ctx.setTmpFile(tmpFile);
        return ctx;
    }


    private void rotateFile(FileWriteContext ctx) throws Exception {
        closeWriter(ctx);
        uploadAndRotateFile(ctx.getRemotePath(), ctx.getTmpFile(), ctx.getTargetFile(), ctx.getTmpFiles());

        SyncAlarmFile newFile = ctx.getTargetFile().getNext();
        File newTmpFile = createTempFile(ctx.getRemotePath(), newFile);
        ctx.setTargetFile(newFile);
        ctx.setWriter(createBufferedWriter(newTmpFile));
        ctx.setCurrentSize(0L);
        ctx.setTmpFile(newTmpFile);
    }

    private void closeWriter(FileWriteContext ctx) throws IOException {
        if (ctx.getWriter() != null) {
            ctx.getWriter().close();
            ctx.setWriter(null);
        }
    }

    public List<String> finalizeWriting(FileWriteContext ctx) throws Exception {
        if (ctx.getWriter() != null) {
            closeWriter(ctx);
            uploadAndRotateFile(ctx.getRemotePath(), ctx.getTmpFile(), ctx.getTargetFile(), ctx.getTmpFiles());
        }
        List<String> files = renameTempFiles(ctx.getRemotePath(), ctx.getTmpFiles(), new ArrayList<>());
        log.debug("Finalized {} files: {}", files.size(), files);
        return files;
    }

    private void cleanupResources(FileWriteContext ctx) {
        try {
            if (ctx.getWriter() != null) {
                ctx.getWriter().close();
            }
        } catch (IOException e) {
            log.error(" close writer error!", e);
        }
    }

    private File createTempFile(String remotePath, SyncAlarmFile syncFile) throws Exception {
        Path path = Paths.get(tempDir, syncFile.getTmpName());
        File tmpFile = Files.write(path, new byte[0], StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).toFile();
        fileUploadService.uploadFile(remotePath, tmpFile.getAbsolutePath());
        return tmpFile;
    }

    private BufferedWriter createBufferedWriter(File file) throws Exception {
        return new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true),
                        joaAlarmConfig.getCharset()));
    }


    private void uploadAndRotateFile(String remotePath, File tmpFile, SyncAlarmFile meta, List<SyncAlarmFile> tmpFiles) throws Exception {
        fileUploadService.uploadFile(remotePath, tmpFile.getAbsolutePath());
        if (meta.getIndex() == 0) {
            meta.setIndex(1);
            Path src = Paths.get(tempDir, tmpFile.getName());
            Path dst = Paths.get(tempDir, meta.getName() + ".tmp");
            Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            fileUploadService.rename(remotePath, tmpFile.getName(), dst.toFile().getName());
        }
        tmpFiles.add(meta);
    }

    private List<String> renameTempFiles(String remotePath, List<SyncAlarmFile> tmpFiles, List<String> result) throws Exception {
        for (SyncAlarmFile meta : tmpFiles) {
            Path src = Paths.get(tempDir, meta.getTmpName());
            Path dst = Paths.get(tempDir, meta.getName());
            Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            fileUploadService.rename(remotePath, src.toFile().getName(), dst.toFile().getName());
            result.add(dst.toFile().getCanonicalPath());
        }
        return result;
    }

    public void cleanupTempFiles(List<String> tmpFiles) {
        tmpFiles.forEach(it -> {
            try {
                File file = new File(it);
                if (file.exists()) {
                    boolean result = file.delete();
                    log.warn("Delete file:{}, result={}", it, result);
                }
            }catch (Exception e) {
                log.error("delete local tmp file {} error", it, e);
            }
        });
    }

    public List<String> zipFiles(List<String> filePaths) throws Exception {
        return SyncAlarmFileUtil.zipFiles(filePaths);
    }

    private static class NoCloseInputStream extends FilterInputStream {
        protected NoCloseInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
        }
    }


    @Getter
    @Setter
    public static class FileWriteContext {

        private boolean result = false;

        private String remotePath;

        private SyncAlarmFile targetFile;

        private List<SyncAlarmFile> tmpFiles = new ArrayList<>();

        private List<String> finalFiles = new ArrayList<>();

        private BufferedWriter writer;

        private Long currentSize;

        private File tmpFile;

        private String currentDate;

        public FileWriteContext() {
            currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        }
    }
}
