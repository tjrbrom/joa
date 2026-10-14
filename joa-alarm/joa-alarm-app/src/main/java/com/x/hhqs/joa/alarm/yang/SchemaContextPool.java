package com.x.hhqs.joa.alarm.yang;

import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.impl.YangParserFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class SchemaContextPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaContextPool.class);

    private SchemaContext schemaContext;

    public SchemaContext getOrLoad(List<YangTextSchemaSource> sources) {
        if (schemaContext == null) {
            var parserFactory = new YangParserFactoryImpl();
            var parser = parserFactory.createParser();
            try {
                schemaContext = parser.addSources(sources).buildSchemaContext();
            } catch (YangParserException e) {
                LOGGER.error("Fail to parse yang files", e);
            } catch (IOException e) {
                LOGGER.error("Fail to load yang files", e);
            }
        }
        return schemaContext;
    }

    public void clear() {
        schemaContext = null;
    }
}
