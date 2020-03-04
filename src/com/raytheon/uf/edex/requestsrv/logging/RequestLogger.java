package com.raytheon.uf.edex.requestsrv.logging;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raytheon.uf.common.serialization.comm.IServerRequest;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class RequestLogger {

	private static final String CONFIG_BASE_PATH = "/awips2/edex/data/utility/common_static/base/requestsrv/logging";

	private static final String CONFIG_FILE = "request_logging.xml";

	private static final int DEFAULT_MAX_STRING_LENGTH = 80;

    private static final IUFStatusHandler requestLog = UFStatus.getNamedHandler("ThriftSrvRequestLogger");

    private static final RequestLogger instance = new RequestLogger();

	private final ObjectMapper mapper = new ObjectMapper();

	private Map<String, RequestFilter> filterMap = new HashMap<>();

	private int maxStringLength = DEFAULT_MAX_STRING_LENGTH;

	private RequestLogger() {
		/*
		 * See com.raytheon.uf.common.site.SiteMap.readFiles() to see how to accomplish localization override.
		 */
		RequestFilters requestFilters;
		try {
			requestFilters = (RequestFilters) JAXBContext.newInstance(RequestFilters.class)
							.createUnmarshaller()
							.unmarshal(new File(String.format("%s/%s", CONFIG_BASE_PATH, CONFIG_FILE)));

		} catch (JAXBException e) {
			e.printStackTrace();
			return;
		}

		try {
			requestFilters.requestFiltersToMap();
	        filterMap = requestFilters.getRequestFiltersMap();
	        maxStringLength = requestFilters.getMaxFieldStringLength();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
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
		try {
			String clsStr = request.getClass().getName();
			if (filterMap.containsKey(clsStr) && !filterMap.get(clsStr).isEnabled()) {
				requestLog.debug(String.format("Filtered request %s", clsStr));
				return;
			}

			// Create JSON string from RequestWrapper, then convert to a map of objects to simplify parsing
			String jstring = mapper.writeValueAsString(new RequestWrapper(wsid, request));
			Map<String, Object> requestWrapperMap = mapper.readValue(jstring, new TypeReference<Map<String, Object>>(){});

			applyFilters(requestWrapperMap);

			requestLog.info(String.format("Request: %s", mapper.writeValueAsString(requestWrapperMap)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
