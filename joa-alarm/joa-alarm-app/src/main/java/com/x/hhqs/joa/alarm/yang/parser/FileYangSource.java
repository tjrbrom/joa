package com.x.hhqs.joa.alarm.yang.parser;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class FileYangSource extends YangTextSchemaSource {

    private final File m_yangFile;

    public FileYangSource(File yangFile) {
        super(identifierFromFilename(yangFile.getName()));
        m_yangFile = yangFile;
    }

    @Override
    protected ToStringHelper addToStringAttributes(ToStringHelper toStringHelper) {
        return toStringHelper.add("file", m_yangFile.getAbsolutePath());
    }

    @Override
    public InputStream openStream() throws IOException {
        return new FileInputStream(m_yangFile);
    }
    
    @Override
    public Optional<String> getSymbolicName() {
        return Optional.of(m_yangFile.getAbsolutePath());
    }

}