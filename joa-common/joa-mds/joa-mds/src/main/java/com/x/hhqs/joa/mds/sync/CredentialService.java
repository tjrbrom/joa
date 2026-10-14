package com.x.hhqs.joa.mds.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.x.hhqs.joa.mds.config.AmsProperties;
import com.x.hhqs.joa.mds.crypto.CryptoService;
import com.x.hhqs.joa.mds.crypto.EmptyCryptoService;
import com.x.hhqs.joa.mds.model.Manager;
import com.x.hhqs.joa.mds.model.ManagerType;

public class CredentialService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialService.class);

    private AmsProperties amsProperties;

    private CryptoService cryptoService;

    public CredentialService(CryptoService cryptoService, AmsProperties amsProperties) {
        this.cryptoService = cryptoService;
        this.amsProperties = amsProperties;
    }

    public String getUsername(Manager manager) {
        if (manager.getType() == ManagerType.AMS && amsProperties.isUseConfiguredCredentials()) {
            return amsProperties.getUsername();
        }

        return manager.getUsername();
    }

    public String getPassword(Manager manager) {
        if (manager.getType() == ManagerType.AMS && amsProperties.isUseConfiguredCredentials()) {
            return amsProperties.getPassword();
        }

        if (cryptoService instanceof EmptyCryptoService) {
            LOGGER.warn("Resource 'keyfile.plain' is not exists, check if 'secretkey.path' is configured correctly.");
        }
        return cryptoService.decrypt(manager.getPassword());
    }

}
