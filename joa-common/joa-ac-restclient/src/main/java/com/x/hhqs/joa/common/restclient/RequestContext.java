package com.x.hhqs.joa.common.restclient;

import java.util.Map;
import java.util.Set;

public class RequestContext {
    private static ThreadLocal<RequestCategory> m_requestCategoryTL = new ThreadLocal<>();
    private static ThreadLocal<String> c_applicationTL = new ThreadLocal<>();
    private static ThreadLocal<UserContext> c_loggedInUserCtxtTL = new ThreadLocal<>();
    private static ThreadLocal<UserContext> c_additionalUserCtxt = new ThreadLocal<>();
    private static ThreadLocal<Map<String, Set<String>>> c_additionalAttrCtxtTL = new ThreadLocal<>();
    private static ThreadLocal<AggregateContext> c_aggregateContextTL = new ThreadLocal<>();
    private static ThreadLocal<Boolean> c_skipDependentSyncTL = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private static ThreadLocal<Boolean> c_isByPass = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static ThreadLocal<Set<String>> c_byPassPermissions = new ThreadLocal<>(); // keeping all permissions of ADMIN role at the moment

    public static void setRequestCategoryTL(RequestCategory category) {
        m_requestCategoryTL.set(category);
    }

    public static RequestCategory getRequestCategoryTL() {
        return m_requestCategoryTL.get();
    }

    public static void setApplicationTL(String application) {
        c_applicationTL.set(application);
    }

    public static String getApplicationTL() {
        return c_applicationTL.get();
    }

    public static AggregateContext getAggregateContextTL() {
        return c_aggregateContextTL.get();
    }

    public static void setAggregateContextTL(AggregateContext aggregateContext) {
        c_aggregateContextTL.set(aggregateContext);
    }

    public static boolean isSkipDependentSyncTL() {
        return c_skipDependentSyncTL.get();
    }

    public static void setSkipDependentSyncTL(boolean isSkipSync) {
        c_skipDependentSyncTL.set(isSkipSync);
    }

    public static void enableIsByPass() {
        c_isByPass.set(true);
    }

    public static void setByPassPermissions(Set<String> permissions) {
        c_byPassPermissions.set(permissions);
    }

    public static Map<String, Set<String>> getAdditionalAttrCtxt() {
        return c_additionalAttrCtxtTL.get();
    }

    public static void setAdditionalAttrCtxt(Map<String, Set<String>> additionalAttrCtxt) {
        c_additionalAttrCtxtTL.set(additionalAttrCtxt);
    }

    public static void reset() {
        m_requestCategoryTL.remove();
        c_applicationTL.remove();
        c_aggregateContextTL.remove();
        clearUserCtxtTLs();
        c_isByPass.remove();
        c_byPassPermissions.remove();
        c_skipDependentSyncTL.remove();
        c_additionalAttrCtxtTL.remove();
    }

    public static boolean isByPassed() {
        return c_isByPass.get();
    }

    public static Set<String> getByPassPermissions() {
        return c_byPassPermissions.get();
    }

    public static UserContext getLoggedInUserCtxtTL() {
        return c_loggedInUserCtxtTL.get();
    }

    public static UserContext getAdditionalUserCtxtTL() {
        return c_additionalUserCtxt.get();
    }

    public static void setLoggedInUserCtxtTL(UserContext loggedInUser) {
        c_loggedInUserCtxtTL.set(loggedInUser);
    }

    public static void setAdditionalUserCtxtTL(UserContext additionalUserContext) {
        c_additionalUserCtxt.set(additionalUserContext);
    }

    public static void clearUserCtxtTLs() {
        c_loggedInUserCtxtTL.remove();
        c_additionalUserCtxt.remove();
    }
    public static <RT> RT withCtxt(RCTemplate<RT> template) throws RCTemplate.RequestContextExecutionException {
        return template.executeWithReset();
    }

    public static abstract class RCTemplate<RT> {
        protected abstract RT execute() throws RequestContextExecutionException;
        final RT executeWithReset() throws RequestContextExecutionException {
            RequestContext.reset();
            try{
                return execute();
            }finally {
                RequestContext.reset();
            }
        };

        public static class RequestContextExecutionException extends RuntimeException {
            public RequestContextExecutionException(Exception e) {
                super(e);
            }
        }
    }

    private RequestCategory m_requestCategory;
    private String m_application;
    private UserContext m_loggedInUserCtx;
    private UserContext m_additionalUserCtxt;
    private AggregateContext m_aggregateContext;
    private Boolean m_skipDependentSync;

    public RequestContext(RequestCategory requestCategory) {
        this.m_requestCategory = requestCategory;
    }
    public RequestCategory getRequestCategory() {
        return m_requestCategory;
    }

    public void setLoggedInUserCtxt(UserContext userContext) {
        m_loggedInUserCtx = userContext;
    }

    public UserContext getLoggedInUserCtxt() {
        return m_loggedInUserCtx;
    }
    public void setAdditionalUserCtxt(UserContext additionalUserContext) {
        m_additionalUserCtxt = additionalUserContext;
    }

    public UserContext getAdditionalUserCtxt() {
        return m_additionalUserCtxt;
    }

    public void setRequestCategory(RequestCategory requestCategory) {
        this.m_requestCategory = requestCategory;
    }

    public String getApplication() {
        return m_application;
    }

    public void setApplication(String application) {
        this.m_application = application;
    }

    public AggregateContext getAggregateContext() {
        return m_aggregateContext;
    }

    public void setAggregateContext(AggregateContext aggregateContext) {
        this.m_aggregateContext = aggregateContext;
    }

    public Boolean isSkipDependentSync() {
        return m_skipDependentSync;
    }

    public void setSkipDependentSync(Boolean skipDependentSync) {
        this.m_skipDependentSync = skipDependentSync;
    }

}
