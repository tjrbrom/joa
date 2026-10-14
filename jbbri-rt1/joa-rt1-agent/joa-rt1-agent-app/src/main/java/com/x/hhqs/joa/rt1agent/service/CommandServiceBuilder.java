package com.x.hhqs.joa.rt1agent.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.x.hhqs.joa.rt1agent.command.EaddCode;
import com.x.hhqs.joa.rt1agent.command.FailResultException;
import com.x.hhqs.joa.rt1agent.loadbalancer.LoadBalancer;
import com.x.hhqs.joa.rt1agent.router.Router;
import com.x.hhqs.joa.rt1agent.util.RT1AgentConstants;

public class CommandServiceBuilder implements InvocationHandler {
    private CommandServiceManager serviceManager;
    private Router router;
    private LoadBalancer loadBalancer;
    private RestTemplate restClient;
    private Class<?> proxy;

    public CommandServiceBuilder() {}

    public CommandServiceBuilder(CommandServiceManager serviceManager, Router router, LoadBalancer loadBalancer,
        RestTemplate restClient, Class<?> proxy) {
        this.serviceManager = serviceManager;
        this.router = router;
        this.loadBalancer = loadBalancer;
        this.restClient = restClient;
        this.proxy = proxy;
    }

    public CommandServiceManager getServiceManager() {
        return serviceManager;
    }

    public void setServiceManager(CommandServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    public Router getRouter() {
        return router;
    }

    public void setRouter(Router router) {
        this.router = router;
    }

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public RestTemplate getRestClient() {
        return restClient;
    }

    public void setRestClient(RestTemplate restClient) {
        this.restClient = restClient;
    }

    public Class<?> getProxy() {
        return proxy;
    }

    public void setProxy(Class<?> proxy) {
        this.proxy = proxy;
    }

    public static CommandServiceBuilder.Builder builder() {
        return new CommandServiceBuilder.Builder();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RequestMetadata requestMetadata = extractRequestMetadata(method, args);
        return handleMethodInvocation(requestMetadata);
    }

    private Object handleMethodInvocation(RequestMetadata requestMetadata) throws FailResultException {
        RequestMethod requestMethod = requestMetadata.requestMethod;
        Class<?> returnType = requestMetadata.returnType;
        String baseUrl = getBaseUrl(requestMetadata);
        Optional<Instance> instance = findCommandServiceInstance(requestMetadata.commandCode);
        if (instance.isEmpty()) {
            throw new FailResultException(EaddCode.SERVICE_NOT_FOUND, new Object[]{requestMetadata.commandCode});
        }
        StringBuilder wholeUrl = new StringBuilder();
        wholeUrl.append("http://").append(instance.get().getIp()).append(":").append(instance.get().getPort()).append(baseUrl);
        if (RequestMethod.GET.equals(requestMethod)) {
            return restClient.getForObject(wholeUrl.toString(), returnType);
        } else if (RequestMethod.POST.equals(requestMethod)) {
            return restClient.postForObject(wholeUrl.toString(), createEntity(requestMetadata), returnType);
        } else if (RequestMethod.PUT.equals(requestMethod)) {
            restClient.put(wholeUrl.toString(), null);
            return "";
        } else if (RequestMethod.DELETE.equals(requestMethod)) {
            restClient.delete(wholeUrl.toString());
            return "";
        } else {
            throw new IllegalArgumentException("Unsupported method type");
        }
    }

    protected HttpEntity<?> createEntity(RequestMetadata requestMetadata) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestEntity = new HttpEntity<>(requestMetadata.payload, headers);
        return requestEntity;
    }

    private Optional<Instance> findCommandServiceInstance(String commandCode) {
        List<Instance> allInstances = serviceManager.getAllInstances();

        List<Instance> selectedInstances = allInstances.stream()
            .filter(instance -> instance.containsMetadata(RT1AgentConstants.COMMAND_CODE) &&
                instance.getMetadata().get(RT1AgentConstants.COMMAND_CODE).contains(commandCode))
            .collect(Collectors.toList());

        List<Instance> routedInstances =
            router != null ? router.route(selectedInstances, commandCode).orElse(selectedInstances) : selectedInstances;

        return loadBalancer.chooseInstance(routedInstances);
    }

    private static String getBaseUrl(RequestMetadata requestMetadata) {
        String baseUrl = requestMetadata.baseUrl;
        Map<String, String> pathParams = requestMetadata.pathParams;
        Map<String, String> formParams = requestMetadata.formParams;
        if (!pathParams.isEmpty()) {
            for (Map.Entry<String, String> entry : pathParams.entrySet()) {
                baseUrl = baseUrl.replaceAll(Pattern.quote("{" + entry.getKey() + "}"), entry.getValue());
            }
        }
        if (!formParams.isEmpty()) {
            StringJoiner params = new StringJoiner("&");
            for (Map.Entry<String, String> entry : formParams.entrySet()) {
                params.add(entry.getKey() + "="
                    + URLEncoder.encode(entry.getValue(), Charset.defaultCharset()).replace("\\+", "%20"));
            }
            baseUrl = baseUrl + "?" + params;
        }
        return baseUrl;
    }

    private RequestMetadata extractRequestMetadata(Method method, Object[] args) {
        RequestMetadata requestMetadata = new RequestMetadata();
        if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping annotation = method.getAnnotation(RequestMapping.class);
            requestMetadata.baseUrl = annotation.value()[0];
            requestMetadata.requestMethod = annotation.method()[0];
        } else if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping annotation = method.getAnnotation(GetMapping.class);
            requestMetadata.baseUrl = annotation.value()[0];
            requestMetadata.requestMethod = RequestMethod.GET;
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping annotation = method.getAnnotation(PostMapping.class);
            requestMetadata.baseUrl = annotation.value()[0];
            requestMetadata.requestMethod = RequestMethod.POST;
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping annotation = method.getAnnotation(PutMapping.class);
            requestMetadata.baseUrl = annotation.value()[0];
            requestMetadata.requestMethod = RequestMethod.PUT;
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping annotation = method.getAnnotation(DeleteMapping.class);
            requestMetadata.baseUrl = annotation.value()[0];
            requestMetadata.requestMethod = RequestMethod.DELETE;
        }
        requestMetadata.returnType = method.getReturnType();
        if (args[0] == null) {
            throw new IllegalArgumentException("command is a mandatory parameter.");
        }
        String commandCode = (String) args[0];
        requestMetadata.commandCode = commandCode;
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            if (parameterAnnotations.length > 0 && i < parameterAnnotations.length) {
                Annotation[] annotations = parameterAnnotations[i];
                if (annotations != null) {
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof PathVariable) {
                            requestMetadata.pathParams.put(((PathVariable) annotation).value(), String.valueOf(arg));
                        } else if (annotation instanceof RequestParam) {
                            requestMetadata.formParams.put(((RequestParam) annotation).value(), String.valueOf(arg));
                        } else if (annotation instanceof RequestBody) {
                            requestMetadata.payload = arg;
                        } else if (annotation instanceof RequestHeader) {
                            requestMetadata.headers.put(((RequestHeader) annotation).value(), String.valueOf(arg));
                        }
                    }
                }
            }
        }
        return requestMetadata;
    }

    private static class RequestMetadata {
        RequestMethod requestMethod;
        Class<?> returnType;
        String baseUrl;
        Map<String, String> pathParams = new HashMap<>();
        Map<String, String> formParams = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        Object payload;
        String commandCode;
    }

    public static class Builder {
        private CommandServiceManager serviceManager;
        private Router router;
        private LoadBalancer loadBalancer;
        private RestTemplate restClient;
        private Class<?> proxy;

        public Builder() {}

        public CommandServiceBuilder.Builder serviceManager(CommandServiceManager serviceManager) {
            this.serviceManager = serviceManager;
            return this;
        }

        public CommandServiceBuilder.Builder router(Router router) {
            this.router = router;
            return this;
        }

        public CommandServiceBuilder.Builder loadBalancer(LoadBalancer loadBalancer) {
            this.loadBalancer = loadBalancer;
            return this;
        }

        public CommandServiceBuilder.Builder restClient(RestTemplate restClient) {
            this.restClient = restClient;
            return this;
        }

        public CommandServiceBuilder.Builder proxy(Class<?> proxy) {
            this.proxy = proxy;
            return this;
        }

        public <T> T build() {
            return (T) Proxy.newProxyInstance(proxy.getClassLoader(),
                new Class[] {proxy},
                new CommandServiceBuilder(this.serviceManager, this.router, this.loadBalancer, this.restClient,
                    this.proxy));
        }
    }
}
