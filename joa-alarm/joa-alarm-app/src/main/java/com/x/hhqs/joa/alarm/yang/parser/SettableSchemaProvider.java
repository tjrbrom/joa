package com.x.hhqs.joa.alarm.yang.parser;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;

public class SettableSchemaProvider<T extends SchemaSourceRepresentation> implements SchemaSourceProvider<T> {

    private final SettableFuture<T> m_future = SettableFuture.create();
    private final T m_schemaSourceRepresentation;
    private final PotentialSchemaSource<T> m_potentialSchemaSource;

    SettableSchemaProvider(final T schemaSourceRepresentation, final SourceIdentifier sourceIdentifier, final Class<T> representation,
            final int cost) {
        this.m_schemaSourceRepresentation = schemaSourceRepresentation;
        this.m_potentialSchemaSource = PotentialSchemaSource.create(sourceIdentifier, representation, cost);
    }

    public static <T extends SchemaSourceRepresentation> SettableSchemaProvider<T> createImmediate(final T schemaSourceRepresentation,
            final Class<T> representation) {
        return new SettableSchemaProvider<>(schemaSourceRepresentation, schemaSourceRepresentation.getIdentifier(), representation,
                PotentialSchemaSource.Costs.IMMEDIATE.getValue());
    }

    @Override
    public ListenableFuture<T> getSource(final SourceIdentifier sourceIdentifier) {
        return m_future;
    }

    public T getSchemaSourceRepresentation() {
        return m_schemaSourceRepresentation;
    }

    public SourceIdentifier getId() {
        return m_schemaSourceRepresentation.getIdentifier();
    }

    public void setResult() {
        m_future.set(m_schemaSourceRepresentation);
    }

    public void setException(final Throwable ex) {
        m_future.setException(ex);
    }

    public void register(final SchemaSourceRegistry repo) {
        repo.registerSchemaSource(this, m_potentialSchemaSource);
    }

}