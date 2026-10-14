package com.x.hhqs.joa.alarm.yang.parser;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.opendaylight.yangtools.yang.model.repo.api.YinTextSchemaSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class FileYinSource extends YinTextSchemaSource {

    private final File m_yinFile;

    public FileYinSource(File yangFile) {
        super(identifierFromFilename(yangFile.getName()));
        m_yinFile = yangFile;
    }

    @Override
    protected ToStringHelper addToStringAttributes(ToStringHelper toStringHelper) {
        return toStringHelper.add("file", m_yinFile.getAbsolutePath());
    }

    @Override
    public InputStream openStream() throws IOException {
        return new FileInputStream(m_yinFile);
    }
    
    @Override
    public Optional<String> getSymbolicName() {
        return Optional.of(m_yinFile.getAbsolutePath());
    }
}