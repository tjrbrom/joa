package com.x.hhqs.joa.sa.utils;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestUtil {

    private RequestUtil() {
    }

    /**
     * Used to render the request body in XML format
     * placeholder: ${Param}
     * @param rpcTemplate rpc request template
     * @param params parameters in placeholders
     * @return request body
     */
    public static String renderRpcRequest(String rpcTemplate, Map<String, String> params) {
        Pattern pattern = Pattern.compile("\\$\\{(.+?)}");
        Matcher matcher = pattern.matcher(rpcTemplate);
        List<String> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        String reqBody = rpcTemplate;
        for (String match : matches) {
            String key = match.substring(2, match.length() - 1);
            if (params.containsKey(key)) {
                reqBody = reqBody.replace(match, params.get(key));
            }
        }
        return reqBody;
    }
}
