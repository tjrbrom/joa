package com.x.hhqs.joa.alarm.yang.utils;

import com.x.hhqs.joa.common.restclient.Pair;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public class IdentityUtil {

    private final SchemaContext schemaContext;
    private final Map<QName, IdentitySchemaNode> identityMap = new HashMap<>();
    private final Set<IdentitySchemaNode> identifiers = new HashSet<IdentitySchemaNode>();

    public IdentityUtil(SchemaContext schemaContext) {
        this.schemaContext = schemaContext;
        for (Module module : schemaContext.getModules()) {
            identifiers.addAll(module.getIdentities());
            for (IdentitySchemaNode identity : module.getIdentities()) {
                identityMap.put(identity.getQName(), identity);
            }
        }
    }

    public Set<IdentitySchemaNode> getAllIdentities() {
        return identifiers;
    }

    public List<Pair<String, Set<IdentitySchemaNode>>> getModuleNameIdentitiesPairs() {
        List<Pair<String, Set<IdentitySchemaNode>>> pairs = new ArrayList<>();
        for (Module module : schemaContext.getModules()) {
            pairs.add(new Pair<String, Set<IdentitySchemaNode>>(module.getName(), module.getIdentities()));
        }
        return pairs;
    }

    /**
     * Recursively find the closest base identity whose local name ends with "alarm-types".
     */
    public Optional<QName> findClosestAlarmTypesBase(QName identityQName) {
        IdentitySchemaNode startNode = identityMap.get(identityQName);
        if (startNode == null) {
            return Optional.empty();
        }
        return findInBaseChain(startNode);
    }

    /**
     * Recursively walk up the base identity chain and return the first that ends with "alarm-types".
     */
    private Optional<QName> findInBaseChain(IdentitySchemaNode node) {
        Set<IdentitySchemaNode> baseIdentities = node.getBaseIdentities();
        if (baseIdentities == null || baseIdentities.isEmpty()) {
            return Optional.empty();
        }

        for (IdentitySchemaNode base : baseIdentities) {
            String namespace = base.getQName().getNamespace().toString();
            if (namespace.endsWith("alarm-types")) {
                return Optional.of(base.getQName());
            }
            Optional<QName> result = findInBaseChain(base);
            if (result.isPresent()) {
                return result;
            }
        }

        return Optional.empty();
    }

    /**
     * Recursively resolve the base identity in the given target namespace.
     */
    public Optional<QName> resolveBaseInNamespace(QName startIdentity, String targetNamespace) {
        Set<QName> visited = new HashSet<>();
        QName current = startIdentity;

        while (current != null && !visited.contains(current)) {
            visited.add(current);
            IdentitySchemaNode node = identityMap.get(current);
            if (node == null) break;

            if (current.getNamespace().toString().equals(targetNamespace)) {
                return Optional.of(current);
            }

            // getBaseIdentities returns IdentitySchemaNode instances (could be multiple)
            Set<IdentitySchemaNode> bases = node.getBaseIdentities();
            if (bases.isEmpty()) break;

            // Just take the first base to continue upward (if you only care about single-chain)
            IdentitySchemaNode nextBase = bases.iterator().next();
            current = nextBase.getQName();
        }

        return Optional.empty();
    }
}
