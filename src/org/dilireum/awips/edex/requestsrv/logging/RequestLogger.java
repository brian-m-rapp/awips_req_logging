package org.dilireum.awips.edex.requestsrv.logging;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import javax.xml.bind.Unmarshaller;

public class RequestLogger implements ILocalizationPathObserver {

	private static final int DEFAULT_MAX_STRING_LENGTH = 80;

    private static final IUFStatusHandler 
    		requestLog = UFStatus.getNamedHandler("ThriftSrvRequestLogger");

    private static final String REQ_LOG_CONFIG_DIR = 
    		LocalizationUtil.join("requestsrv", "logging");

    public static final String REQ_LOG_FILENAME = 
    		LocalizationUtil.join(REQ_LOG_CONFIG_DIR, "request_logging.xml");

    private static final RequestLogger instance = new RequestLogger();

	private final ObjectMapper mapper = new ObjectMapper();

	private Map<String, RequestFilter> filterMap = new HashMap<>();

	private int maxStringLength = DEFAULT_MAX_STRING_LENGTH;

	private boolean loggingEnabled = false;

	private RequestLogger() {
		readConfigs();
		PathManagerFactory.getPathManager().addLocalizationPathObserver(REQ_LOG_CONFIG_DIR, this);
	}

	public static RequestLogger getInstance() {
		return instance;
	}

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

	private synchronized void readConfigs() {
		IPathManager pathMgr = PathManagerFactory.getPathManager();
        LocalizationContext[] searchOrder = pathMgr.getLocalSearchHierarchy(LocalizationType.COMMON_STATIC);
        Unmarshaller unmarshaller;

        try {
        	unmarshaller = JAXBContext.newInstance(RequestFilters.class).createUnmarshaller();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

        List<LocalizationContext> reverseOrder = Arrays.asList(Arrays.copyOf(searchOrder, searchOrder.length));
        Collections.reverse(reverseOrder);
        for (LocalizationContext ctx : reverseOrder) {
        	File file = pathMgr.getFile(ctx, REQ_LOG_FILENAME);
        	if (file != null && file.exists()) {
        		try {
        			RequestFilters filters = (RequestFilters) unmarshaller.unmarshal(file);
        			filters.requestFiltersToMap(filterMap);
        	        maxStringLength = filters.getMaxFieldStringLength();
        	        loggingEnabled = filters.isLoggingEnabled();
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        	}
        }
	}

	@SuppressWarnings("unchecked")
	public void applyFilters(Map<String, Object> requestWrapperMap) {
		Map<String, Object> requestMap = (Map<String, Object>) requestWrapperMap.get("request");
		String reqClass = (String) requestWrapperMap.get("reqClass");
		if (filterMap.containsKey(reqClass)) {
			RequestFilter reqFilter = (RequestFilter) filterMap.get(requestWrapperMap.get("reqClass"));
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

	public void truncateLongStrings(Map<String, Object> jsonMap) {
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
			requestLog.info(String.format("Request: %s", mapper.writeValueAsString(requestWrapperMap)));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void fileChanged(ILocalizationFile file) {
		filterMap.clear();
		readConfigs();
	}
}
