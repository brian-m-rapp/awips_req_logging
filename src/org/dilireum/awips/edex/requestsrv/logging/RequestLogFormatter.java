package org.dilireum.awips.edex.requestsrv.logging;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raytheon.uf.common.serialization.comm.IServerRequest;
import java.io.File;
import java.util.Iterator;
import java.util.Map; 
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestLogFormatter {

	final private int defaultMaxStringLength = 160;

    private static final Logger logger = LoggerFactory
            .getLogger(RequestLogFormatter.class);

    private static final RequestLogFormatter instance = new RequestLogFormatter();

	private RequestLogFormatter() {
		RequestFilters requestFilters;
		try {
			requestFilters = (RequestFilters) JAXBContext.newInstance(RequestFilters.class)
									.createUnmarshaller()
									.unmarshal(new File("requests1.xml"));
		} catch (JAXBException e) {
			e.printStackTrace();
			filterMap = new HashMap<String, RequestFilter>();
			maxStringLength = defaultMaxStringLength;
			return;
		}

        requestFilters.requestFiltersToMap();
        filterMap = requestFilters.getRequestFiltersMap();
        maxStringLength = requestFilters.getMaxFieldStringLength();
	}

	private final ObjectMapper mapper = new ObjectMapper();

	public static RequestLogFormatter getInstance() {
		return instance;
	}

	Map<String, RequestFilter> filterMap;

	private int maxStringLength;

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
							//requestMap.put(fieldKey, fieldValue.substring(0, attr.getMaxLength()) + "...");
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
				if (((String) obj).length() > maxStringLength) {
					String nstr = (String) obj;
					jsonMap.put(key, nstr.substring(0, maxStringLength) + "...");
				}
			} else if (obj instanceof LinkedHashMap) {
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
							if (strArrayList.get(i).length() > maxStringLength) {
								strArrayList.set(i, strArrayList.get(i).substring(0, maxStringLength) + "...");
							}
						}
					} else if (objs.get(0) instanceof LinkedHashMap) {
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
		String clsStr = request.getClass().getName();
		if (filterMap.containsKey(clsStr) && !filterMap.get(clsStr).isEnabled()) {
			logger.debug(String.format("Filtered request %s", clsStr));
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
			logger.info(mapper.writeValueAsString(requestWrapperMap));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}
