package com.x.hhqs.joa.rmuid.config;

import java.util.Map;
import java.util.TreeMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.generator.OmcUid;
import com.x.hhqs.joa.rmuid.generator.RmUidGenerator;
import com.x.hhqs.joa.rmuid.generator.RmUidGeneratorManager;
import com.x.hhqs.joa.rmuid.generator.impl.BoardUid;
import com.x.hhqs.joa.rmuid.generator.impl.HolderUid;
import com.x.hhqs.joa.rmuid.generator.impl.LagUid;
import com.x.hhqs.joa.rmuid.generator.impl.LldpUid;
import com.x.hhqs.joa.rmuid.generator.impl.MgwUid;
import com.x.hhqs.joa.rmuid.generator.impl.OltUid;
import com.x.hhqs.joa.rmuid.generator.impl.OnuUid;
import com.x.hhqs.joa.rmuid.generator.impl.PonVlanUid;
import com.x.hhqs.joa.rmuid.generator.impl.PortUid;
import com.x.hhqs.joa.rmuid.generator.impl.PosUid;
import com.x.hhqs.joa.rmuid.generator.impl.ProtectGroupUid;
import com.x.hhqs.joa.rmuid.generator.impl.SrfUid;
import com.x.hhqs.joa.rmuid.generator.impl.TopologyUid;
import com.x.hhqs.joa.rmuid.generator.impl.VlanPortUid;
import com.x.hhqs.joa.rmuid.service.RmUidService;


@Configuration
@ConditionalOnProperty(name = "joa.rmuid.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({OmcConfig.class})
public class JoaRmuidConfiguration {

    @Bean
    public OmcUid omcUid(OmcConfig omcConfig) {
        return new OmcUid(omcConfig);
    }

    @Bean
    public RmUidGeneratorManager uidGeneratorManager(OmcUid omcUid, @Nullable QueryService queryService) {
        Map<String, RmUidGenerator> generators = createDefaultGenerators(omcUid, queryService);
        return new RmUidGeneratorManager(generators);
    }

    @Bean
    public RmUidService rmUidService(RmUidGeneratorManager uidGeneratorManager, OmcUid omcUid) {
        return new RmUidService(uidGeneratorManager, omcUid);
    }

    public Map<String, RmUidGenerator> createDefaultGenerators(OmcUid omcUid, QueryService queryService) {
        Map<String, RmUidGenerator> generators = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        // OMC
        generators.put(omcUid.getRmType().getValue(), omcUid);

        // OLT
        OltUid oltUid = new OltUid(omcUid, queryService);
        generators.put(oltUid.getRmType().getValue(), oltUid);

        // ONU
        OnuUid onuUid = new OnuUid(omcUid, queryService);
        generators.put(onuUid.getRmType().getValue(), onuUid);

        // POS
        PosUid posUid = new PosUid(omcUid, queryService);
        generators.put(posUid.getRmType().getValue(), posUid);

        // EQH
        HolderUid holderUid = new HolderUid(omcUid, queryService);
        generators.put(holderUid.getRmType().getValue(), holderUid);

        // CRD
        BoardUid boardUid = new BoardUid(omcUid, queryService);
        boardUid.setOnuUid(onuUid);
        generators.put(boardUid.getRmType().getValue(), boardUid);

        // PRT
        PortUid portUid = new PortUid(omcUid, queryService);
        generators.put(portUid.getRmType().getValue(), portUid);

        // TPL
        TopologyUid topologyUid = new TopologyUid(omcUid, queryService);
        topologyUid.setOnuUid(onuUid);
        topologyUid.setPosUid(posUid);
        generators.put(topologyUid.getRmType().getValue(), topologyUid);

        // MGW
        MgwUid mgwUid = new MgwUid(omcUid, queryService);
        generators.put(mgwUid.getRmType().getValue(), mgwUid);

        // PRG
        ProtectGroupUid protectGroupUid = new ProtectGroupUid(omcUid, queryService);
        generators.put(protectGroupUid.getRmType().getValue(), protectGroupUid);

        // PTV
        VlanPortUid vlanPortUid = new VlanPortUid(omcUid, queryService);
        generators.put(vlanPortUid.getRmType().getValue(), vlanPortUid);

        // VLN
        PonVlanUid ponVlanUid =  new PonVlanUid(omcUid, queryService);
        generators.put(ponVlanUid.getRmType().getValue(), ponVlanUid);

        // LAG
        LagUid lagUid = new LagUid(omcUid, queryService);
        generators.put(lagUid.getRmType().getValue(), lagUid);

        // SFR
        SrfUid srfUid = new SrfUid(omcUid, queryService);
        generators.put(srfUid.getRmType().getValue(), srfUid);
        
        // LLDP
        LldpUid lldpUid = new LldpUid(omcUid, queryService);
        generators.put(lldpUid.getRmType().getValue(), lldpUid);

        return generators;
    }


}
