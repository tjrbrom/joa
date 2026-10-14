package com.x.hhqs.joa.common.restclient.provider;

import java.util.List;
import java.util.Map;

public interface StaticParamsProvider {
    List<Map.Entry<String, String>> getParams();
}
