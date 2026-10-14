package com.x.hhqs.joa.sa.netconf;

import com.x.hhqs.joa.sa.api.RequestType;
import com.x.hhqs.joa.sa.api.SbiRequest;

import java.util.List;

/**
 * netconf request entity
 */
public class NetconfRequest implements SbiRequest {

    private boolean history;
    private ContentType contentType = ContentType.APPLICATION_XML;
    private String body;

    private String template;

    private String condition;

    private List<Data> models;

    private List<Selector> selectors;

    public NetconfRequest() {
    }

    public NetconfRequest(String body) {
        this.body = body;
    }

    public NetconfRequest(boolean history, String body) {
        this.history = history;
        this.body = body;
    }

    public NetconfRequest(boolean history, ContentType contentType, String body) {
        this.history = history;
        this.contentType = contentType;
        this.body = body;
    }

    public boolean isHistory() {
        return history;
    }

    public void setHistory(boolean history) {
        this.history = history;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public List<Data> getModels() {
        return models;
    }

    public void setModels(List<Data> models) {
        this.models = models;
    }

    public List<Selector> getSelectors() {
        return selectors;
    }

    public void setSelectors(List<Selector> selectors) {
        this.selectors = selectors;
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.NETCONF;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "NetconfRequest{" +
                "history=" + history +
                ", contentType=" + contentType +
                ", body='" + body + '\'' +
                ", template='" + template + '\'' +
                ", condition='" + condition + '\'' +
                ", models=" + models +
                ", selectors=" + selectors +
                '}';
    }

    public static class Selector {
        private String key;
        private String type;
        private String expr;
        private Boolean optional;

        public Selector(String key, String type, String expr) {
            this(key, type, expr, false);
        }

        public Selector(String key, String type, String expr, Boolean optional) {
            this.key = key;
            this.type = type;
            this.expr = expr;
            this.optional = optional;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getExpr() {
            return expr;
        }

        public void setExpr(String expr) {
            this.expr = expr;
        }

        public Boolean getOptional() {
            return optional;
        }

        public void setOptional(Boolean optional) {
            this.optional = optional;
        }

        @Override
        public String toString() {
            return "Selector{" +
                    "key='" + key + '\'' +
                    ", type='" + type + '\'' +
                    ", expr='" + expr + '\'' +
                    ", optional=" + optional +
                    '}';
        }
    }

    public static class Data {
        private String name;
        private String value;
        private String type;
        private Boolean optional;
        private Boolean toContext;

        public Data(String name, String value, String type) {
            this(name, value, type, false);
        }

        public Data(String name, String value, String type, Boolean optional) {
            this(name, value, type, optional, false);
        }

        public Data(String name, String value, String type, Boolean optional, Boolean toContext) {
            this.name = name;
            this.value = value;
            this.type = type;
            this.optional = optional;
            this.toContext = toContext;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Boolean getOptional() {
            return optional;
        }

        public void setOptional(Boolean optional) {
            this.optional = optional;
        }

        public Boolean getToContext() {
            return toContext;
        }

        public void setToContext(Boolean toContext) {
            this.toContext = toContext;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "name='" + name + '\'' +
                    ", value='" + value + '\'' +
                    ", type='" + type + '\'' +
                    ", optional=" + optional +
                    ", toContext=" + toContext +
                    '}';
        }
    }
}
