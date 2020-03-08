package com.raytheon.uf.edex.requestsrv.logging;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
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
 * <p>Class for logging request ({@link IServerRequest}) details.  Requests are logged
 * to edex-request-thriftSrv-<date>.log as JSON-encoded strings to allow easy parsing 
 * by external applications.  By default, all request attributes are logged.  Requests
 * are instances of classes that implement the {@link IServerRequest} interface.
 * Request logging is configured in common_static:requestsrv/logging/request_logging.xml.
 * <p>
 * Request logging can be disabled in 2 ways:
 * <ol><li>delete request_logging.xml from all locations, or</li>
 * <li>set loggingEnabled="false" in the <code>requests</code> element of the most 
 * specific instance of request_logging.xml</li></ol>
 * <p>Two types of attribute filtering are provided: enable/disable, and truncation 
 * of String attributes to a maximum length.  By default, all attributes are logged.  
 * A maximum default attribute String length is hardcoded as 
 * {@link #DEFAULT_MAX_STRING_LENGTH}.  This maximum can be overridden by defining 
 * the XML attribute <code>maxFieldStringLength</code> in the <code>requests</code> 
 * tag.
 * <p>To disable logging of a request class, use a <code>request</code> element
 * with a <code>class</code> attribute naming the class and set the <code>enabled</code>
 * attribute to false.  For example:
 * <pre>{@code<request class="com.raytheon.uf.common.localization.msgs.UtilityRequestMessage" enabled="false"/>}</pre>
 * <p>Attributes are configured for each request class using <code>attribute</code>
 * tags within an outer <code>attributes</code>tag.
 * For example:
 * <pre>{@code    <request class="com.raytheon.uf.common.dataquery.requests.QlServerRequest">
        <attributes>
            <attribute name="query" maxlength="80"/>
            <attribute name="lang" enabled="false"/>
        </attributes>
    </request>}</pre>
 * 
 * 
 * @author Brian Rapp
 * @version 1.0
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
    private static final IUFStatusHandler 
    		requestLog = UFStatus.getNamedHandler("ThriftSrvRequestLogger");

    /**
     * Subdirectory for request configuration
     */
    private static final String REQ_LOG_CONFIG_DIR = 
    		LocalizationUtil.join("requestsrv", "logging");

    /**
     * Name of request configuration file
     */
    private static final String REQ_LOG_FILENAME = 
    		LocalizationUtil.join(REQ_LOG_CONFIG_DIR, "request_logging.xml");

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
	 * Object for transforming XML configuration into POJOs.
	 */
	private Unmarshaller unmarshaller;

	/**
	 * Private constructor for initializing the RequestLogger singleton instance.  Instantiates
	 * the unmarshaller, reads the configuration files, and sets up a localization path observer.
	 */
	private RequestLogger() {
        try {
        	unmarshaller = JAXBContext.newInstance(RawRequestFilters.class).createUnmarshaller();
        } catch (JAXBException e) {
        	requestLog.error("Error creating context for RequestLogger", e);
            throw new ExceptionInInitializerError("Error creating context for RequestLogger");
        }

        readConfigs();
		PathManagerFactory.getPathManager().addLocalizationPathObserver(REQ_LOG_CONFIG_DIR, this);
	}

	/**
	 * @return The RequestLogger instance.
	 */
	public static RequestLogger getInstance() {
		return instance;
	}

	/**
	 * Internal class for stringifying request objects to JSON.
	 */
	@JsonPropertyOrder({"wsid", "reqClass", "request"})
	private class RequestWrapper {
		private String wsid;
		private String reqClass;
		private IServerRequest request;

		public RequestWrapper(String wsid, IServerRequest request) {
			this.wsid = wsid;
			this.reqClass = request.getClass().getName();
			this.request = request;
		}

		@SuppressWarnings("unused")
		public String getWsid() {
			return wsid;
		}

		@SuppressWarnings("unused")
		public String getReqClass() {
			return reqClass;
		}

		@SuppressWarnings("unused")
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
        				} else {
        					// This is a new filter
        					filterMap.put(req.getClassName(), new RequestFilter(req));
        				}
        			}
        	        maxStringLength = rawFilters.getMaxFieldStringLength();
        	        loggingEnabled = rawFilters.isLoggingEnabled();
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
	 * Recursively traverses an object map to truncate all String longer
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
	 * @param wsid
	 * 	String containing the workstation ID.
	 * @param request
	 * 	Request object to be logged.
	 */
	public void logRequest(String wsid, IServerRequest request) {
		if (!loggingEnabled) {
			return;
		}

		String clsStr = request.getClass().getName();
		if (filterMap.containsKey(clsStr) && !filterMap.get(clsStr).isEnabled()) {
			requestLog.debug(String.format("Filtered request %s", clsStr));
			return;
		}

		String jstring;
		try {
			jstring = mapper.writeValueAsString(new RequestWrapper(wsid, request));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return;
		}

		Map<String, Object> requestWrapperMap;
		try {
			requestWrapperMap = mapper.readValue(jstring, new TypeReference<Map<String, Object>>(){});
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		applyFilters(requestWrapperMap);

		try {
			requestLog.info(String.format("Request::: %s", mapper.writeValueAsString(requestWrapperMap)));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Callback function triggered when a request configuration file has been modified.
	 * @param file
	 * 	ILocalizationFile object representation of the file that changed.
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
