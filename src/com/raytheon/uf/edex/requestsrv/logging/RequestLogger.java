package com.raytheon.uf.edex.requestsrv.logging;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raytheon.uf.common.localization.ILocalizationFile;
import com.raytheon.uf.common.localization.ILocalizationPathObserver;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationUtil;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.serialization.comm.IServerRequest;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import java.io.File;
import java.util.Iterator;
import java.util.Map; 
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Class for logging request ({@link IServerRequest}) details.  Implemented using
 * the singleton design pattern.  Requests are logged to edex-request-thriftSrv 
 * as JSON-encoded strings to allow easy parsing by external applications.  Request
 * logging is configured as a white list: only classes explicitly identified and 
 * enabled are logged.  By default, all request attributes are logged for enabled 
 * classes.  Requests are instances of {@link IServerRequest}.  Request logging is 
 * configured in common_static:requestsrv/logging/request_logging.xml.  Supports 
 * localization override so a site or region can override the base default 
 * configuration.
 * <p>
 * Request logging can be disabled in one of two ways:
 * <ol><li>delete request_logging.xml from all configuration locations, or</li>
 * <li>set loggingEnabled="false" in the <code>requests</code> element of the most 
 * specific instance of request_logging.xml</li></ol>
 * <p>
 * A special "discovery mode" exists for developing configuration files.  In 
 * discovery mode, all requests are logged unless explicitly disabled.  It is 
 * enabled by setting the XML attribute <code>discoveryMode="true"</code> in the 
 * requests tag.  Then run requestsrv for some period of time while exercising 
 * CAVE, GFE, etc.  Review the edex-request-thriftsrv log to identify the desired 
 * request classes and define them as enabled in a config file.  To assist with 
 * this process, you can explicitly disable classes you aren't interested in 
 * logging by setting <code>enabled="false"</code>.  When done with discovery, 
 * either remove the discoveryMode attribute from the requests tag, or set it to 
 * "false".
 * <p>
 * Request filtering uses the value for a class from the most specific configuration
 * file.  If the base file has a class enabled, you can disable it at the site level
 * by defining a request element for it in the site-level file and set enabled="false".
 * <p>
 * Two types of attribute filtering are supported: enable/disable, and truncation 
 * of String attributes to a maximum length.  By default, all attributes are logged.  
 * A maximum default attribute String length is hardcoded as 
 * {@link #DEFAULT_MAX_STRING_LENGTH}.  This maximum can be overridden by defining 
 * the XML attribute <code>maxFieldStringLength</code> in the <code>requests</code> 
 * tag.  Attribute configuration is additive, with the most specific configuration
 * taking effect in the event of conflicts.
 * <p>
 * To disable logging of a request class, either remove it from all configuration
 * files, or use a <code>request</code> element with a <code>class</code> attribute 
 * naming the class and set the <code>enabled</code> attribute to false.  For example:
 * <pre>{@code<request class="com.raytheon.uf.common.localization.msgs.UtilityRequestMessage" enabled="false"/>}</pre>
 * <p>
 * Attributes are configured for each request class using <code>attribute</code>
 * tags within an outer <code>attributes</code>tag.
 * For example:
 * <pre>{@code    <request class="com.raytheon.uf.common.dataquery.requests.QlServerRequest">
        <attributes>
            <attribute name="query" maxLength="80"/>
            <attribute name="lang" enabled="false"/>
        </attributes>
    </request>}</pre>
 * 
 * Sample configuration file:
 * <pre>{@code
<?xml version='1.0' encoding='UTF-8'?> 
<requests maxFieldStringLength="150">
    <request class="com.raytheon.uf.common.dataquery.requests.DbQueryRequest"/>
    <request class="com.raytheon.uf.common.dataquery.requests.DbQueryRequestSet"/>
    <request class="com.raytheon.uf.common.dataquery.requests.TimeQueryRequestSet"/>
    <request class="com.raytheon.uf.common.pointdata.PointDataServerRequest"/>
</requests>

}</pre>
 * 
 * @author Brian Rapp
 *
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date          Ticket#    Engineer    Description
 * ------------- ---------- ----------- --------------------------------------------
 * Mar 8, 2020   DCS 21885  brapp       Initial creation
 * </pre>
 */
public class RequestLogger implements ILocalizationPathObserver {

    /**
     * Default maximum length of String attributes, if not specified
     * in request_logging.xml.
     */
    private static final int DEFAULT_MAX_STRING_LENGTH = 80;

    /**
     * Instance of thrift server request logging (edex-request-thriftsrv-<date>).
     */
    private static IUFStatusHandler requestLog;

    /**
     * Edex run mode for determining config file path
     */
    private static final String edexRunMode = System.getProperty("edex.run.mode");

    /**
     * Subdirectory for request configuration
     */
    private static final String REQ_LOG_CONFIG_DIR = LocalizationUtil.join("request", "logging");

    /**
     * Name of request configuration file.  Determined by EDEX run mode.
     */
    private static String REQ_LOG_FILENAME = null;

    /**
     * Singleton instance of the RequestLogger.
     */
    private static final RequestLogger instance = new RequestLogger();

    /**
     * ObjectMapper provides functionality for reading and writing JSON,
     * either to and from basic POJOs (Plain Old Java Objects), or to and from
     * a general-purpose JSON Tree Model ({@link JsonNode}).
     */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Map of {@link RequestFilter}s from the request logging configuration file(s)
     * keyed by request class name.
     */
    private Map<String, RequestFilter> filterMap = new HashMap<>();

    /**
     * Configured maximum String attribute logging length
     */
    private int maxStringLength = DEFAULT_MAX_STRING_LENGTH;

    /**
     * If true, request logging is enabled; if false, request details are not logged.
     */
    private boolean loggingEnabled = false;  // If there is no configuration file, request details will not be logged

    /**
     * If true, log all request classes, otherwise use the configuration files to determine
     * which request classes to log.
     */
    private boolean inDiscoveryMode = false;

    /**
     * Object for transforming XML configuration into POJOs.
     */
    private Unmarshaller unmarshaller;

    /**
     * Private constructor for initializing the RequestLogger singleton instance.  Instantiates
     * the unmarshaller, reads the configuration files, and sets up a localization path observer.
     */
    private RequestLogger() {
        /*
         * Config file name is determined by which instance of EDEX this is.
         */
        switch (edexRunMode) {
            case "request":        // Request server
                REQ_LOG_FILENAME = LocalizationUtil.join(REQ_LOG_CONFIG_DIR, "request.xml");
                requestLog = UFStatus.getNamedHandler("ThriftSrvRequestLogger");
                break;

            case "registry":    // Data Delivery and Hazard Services
                REQ_LOG_FILENAME = LocalizationUtil.join(REQ_LOG_CONFIG_DIR, "registry.xml");
                requestLog = null;
                break;

            case "centralRegistry":
                REQ_LOG_FILENAME = LocalizationUtil.join(REQ_LOG_CONFIG_DIR, "centralRegistry.xml");
                requestLog = null;
                break;

            case "bmh":
                REQ_LOG_FILENAME = LocalizationUtil.join(REQ_LOG_CONFIG_DIR, "bmh.xml");
                requestLog = null;
                break;

            default:
                REQ_LOG_FILENAME = null;
                requestLog = null;
                break;
        }

        /* Don't try to read config files for run modes we don't care about.  
         * Logging is disabled by default. */
        if (REQ_LOG_FILENAME != null) {
            try {
                unmarshaller = JAXBContext.newInstance(RawRequestFilters.class).createUnmarshaller();
            } catch (JAXBException e) {
                requestLog.error("Error creating context for RequestLogger", e);
                throw new ExceptionInInitializerError("Error creating context for RequestLogger");
            }

            readConfigs();
            PathManagerFactory.getPathManager().addLocalizationPathObserver(REQ_LOG_CONFIG_DIR, this);
        }
    }

    /**
     * @return RequestLogger instance.
     */
    public static RequestLogger getInstance() {
        return instance;
    }

    /**
     * Internal class for stringifying request objects to JSON.
     * Contains 3 attributes: workstation ID (wsid) as a string, the
     * request class as a string, and the raw deserialized request. 
     */
    @JsonPropertyOrder({"wsid", "reqClass", "request"})
    @SuppressWarnings("unused")
    private class RequestWrapper {
        private String wsid;
        private String reqClass;
        private IServerRequest request;

        public RequestWrapper(String wsid, IServerRequest request) {
            this.wsid = wsid;
            this.reqClass = request.getClass().getName();
            this.request = request;
        }

        public String getWsid() {
            return wsid;
        }

        public String getReqClass() {
            return reqClass;
        }

        public IServerRequest getRequest() {
            return request;
        }
    }

    /**
     * Reads all configuration files.  Support localization override.
     */
    private synchronized void readConfigs() {
        IPathManager pathMgr = PathManagerFactory.getPathManager();
        LocalizationContext[] searchOrder = pathMgr.getLocalSearchHierarchy(LocalizationType.COMMON_STATIC);

        List<LocalizationContext> reverseOrder = Arrays.asList(Arrays.copyOf(searchOrder, searchOrder.length));
        Collections.reverse(reverseOrder);
        for (LocalizationContext ctx : reverseOrder) {
            File file = pathMgr.getFile(ctx, REQ_LOG_FILENAME);
            if (file != null && file.exists()) {
                try {
                    RawRequestFilters rawFilters = (RawRequestFilters) unmarshaller.unmarshal(file);
                    for (RawRequestFilter req : rawFilters.getFilters()) {
                        if (filterMap.containsKey(req.getClassName())) {
                            // This is an update to an existing filter
                            // Put each attribute from the raw filter into the request filter
                            Map<String, ClassAttribute> attrs = filterMap.get(req.getClassName())
                                                                    .getAttributeMap();
                            for (ClassAttribute attr : req.getAttributes()) {
                                attrs.put(attr.getName(), attr);
                            }
                        }
                        filterMap.put(req.getClassName(), new RequestFilter(req));
                    }
                    maxStringLength = rawFilters.getMaxFieldStringLength();
                    loggingEnabled = rawFilters.isLoggingEnabled();
                    inDiscoveryMode = rawFilters.isDiscoveryMode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Applies configured filters to a request object map.
     */
    @SuppressWarnings("unchecked")
    private void applyFilters(Map<String, Object> requestWrapperMap) {
        Map<String, Object> requestMap = (Map<String, Object>) requestWrapperMap.get("request");
        String reqClass = (String) requestWrapperMap.get("reqClass");

        if (filterMap.containsKey(reqClass)) {
            RequestFilter reqFilter = filterMap.get(requestWrapperMap.get("reqClass"));
            Map<String, ClassAttribute> attrFilters = reqFilter.getAttributeMap();
            Iterator<Map.Entry<String, Object>> iterator = requestMap.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, Object> field = iterator.next();
                String fieldKey = field.getKey();

                if (attrFilters.containsKey(fieldKey)) {
                    ClassAttribute attr = attrFilters.get(fieldKey);
                    if (!attr.isEnabled()) {
                        iterator.remove();
                        continue;
                    }

                    if (attr.getMaxLength() > 0) {
                        String fieldValue = (String) field.getValue();

                        if (fieldValue.length() > attr.getMaxLength()) {
                            field.setValue(fieldValue.substring(0, attr.getMaxLength()) + "...");
                        }
                    }
                }
            }
        }

        truncateLongStrings(requestMap);
    }

    /**
     * Recursively traverses an object map to truncate all Strings longer
     * than the configured maximum length.  Overrides attribute-specific settings.
     */
    private void truncateLongStrings(Map<String, Object> jsonMap) {
        for (String key : jsonMap.keySet()) {
            Object obj = jsonMap.get(key);

            if (obj instanceof String) {
                if ((maxStringLength > 0) && ((String) obj).length() > maxStringLength) {
                    String nstr = (String) obj;

                    jsonMap.put(key, nstr.substring(0, maxStringLength) + "...");
                }
            } else if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapObj = (Map<String, Object>) obj;

                truncateLongStrings(mapObj);
            } else if (obj instanceof ArrayList) {
                List<?> objs = (ArrayList<?>) obj;

                if (objs.size() > 0) {
                    if (objs.get(0) instanceof String) {
                        @SuppressWarnings("unchecked")
                        List<String> strArrayList = (ArrayList<String>) objs;

                        for (int i = 0; i < strArrayList.size(); i++) {
                            if ((maxStringLength > 0) && (strArrayList.get(i).length() > maxStringLength)) {
                                strArrayList.set(i, strArrayList.get(i).substring(0, maxStringLength) + "...");
                            }
                        }
                    } else if (objs.get(0) instanceof Map) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> mapArrayList = (ArrayList<Map<String, Object>>) obj;

                        for (int i = 0; i < mapArrayList.size(); i++) {
                            truncateLongStrings(mapArrayList.get(i));
                        }
                    }
                }
            }
        }
    }

    /**
     * Logs request to the request log after applying configured filters. 
     * Request filtering and logging is done in a separate thread to allow
     * request processing to continue unimpeded by logging.
     * @param wsid
     *     String containing the workstation ID.
     * @param request
     *     Request object to be logged.
     */
    public void logRequest(String wsid, IServerRequest request) {
        if (!loggingEnabled) {
            return;
        }

        new Thread(new Runnable() {
            private String wsid;
            private IServerRequest request;

            public Runnable init(String wsid, IServerRequest request) {
                this.wsid = wsid;
                this.request = request;
                return this;
            }

            @Override
            public void run() {
                try {
                    String clsStr = request.getClass().getName();
                    /*
                     * Log the request if it's explicitly enabled, -OR-
                     * if discovery Mode is set and the request class isn't in a config file.
                     * This allows discovery mode to ignore requests that are explicitly disabled.
                     */
                    if ((filterMap.containsKey(clsStr) && filterMap.get(clsStr).isEnabled())
                        || (inDiscoveryMode && !filterMap.containsKey(clsStr))) {
                        Map<String, Object> requestWrapperMap = mapper.readValue(
                            mapper.writeValueAsString(new RequestWrapper(wsid, request)), 
                            new TypeReference<Map<String, Object>>(){}
                        );

                        applyFilters(requestWrapperMap);

                        requestLog.info(String.format("Request::: %s", mapper.writeValueAsString(requestWrapperMap)));
                    } else {
                        requestLog.debug(String.format("Filtered::: %s", clsStr));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }.init(wsid, request)).start();
    }

    /**
     * Callback function triggered when a request configuration file has been modified.
     * @param file
     *     ILocalizationFile object representation of the file that changed.
     */
    @Override
    public synchronized void fileChanged(ILocalizationFile file) {
        filterMap.clear();
        readConfigs();
    }

    /**
     * @return map of request filter objects
     */
    public Map<String, RequestFilter> getFilterMap() {
        return filterMap;
    }
}
