package com.x.hhqs.joa.mds.crypto;

public class EmptyCryptoService implements CryptoService {
    @Override
    public String encrypt(String data) {
        return data;
    }

    @Override
    public String decrypt(String data) {
        return data;
    }
}
