package com.x.hhqs.joa.alarm.yang.parser;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class FileDelegateYangSource extends YangTextSchemaSource {

    private final String m_name;
    private final YangTextSchemaSource m_delegate;

    public FileDelegateYangSource(String name, YangTextSchemaSource delegate) {
        super(identifierFromFilename(name));
        m_name = name;
        m_delegate = delegate;
    }

    @Override
    protected ToStringHelper addToStringAttributes(ToStringHelper toStringHelper) {
        return toStringHelper.add("file", m_name);
    }

    @Override
    public InputStream openStream() throws IOException {
        return m_delegate.openStream();
    }
    
    @Override
    public Optional<String> getSymbolicName() {
        return Optional.of(m_name);
    }

}