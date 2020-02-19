package org.dilireum.awips.edex.requestsrv.logging;

import java.util.Map; 
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;

import com.raytheon.uf.common.util.ReflectionUtil;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

public class RequestLogFormatter {

	private static final RequestLogFormatter instance = new RequestLogFormatter();

	private Map<String, HashMap<String, Field>> requestClasses = new HashMap<>();

	private RequestLogFormatter() {
		// Instantiate the request output structure from the XML config file
	}

	private final ObjectMapper mapper = new ObjectMapper();

	public static RequestLogFormatter getInstance() {
		return instance;
	}

	final private int defaultMaxStringLength = 160;
	private int maxStringLength = defaultMaxStringLength;

	private HashMap<String, Field> makeClassFieldMap(Class<?> clazz) {
		HashMap<String, Field> classFields = new HashMap<>();
		List<Field> fields = ReflectionUtil.getAllFields(clazz);
		for (Field field : fields) {
			classFields.put(field.getName(), field);
		}

		return classFields;
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

	public String getLogString(String wsid, IServerRequest request) {
		Class<?> reqClass = request.getClass();
		String className = reqClass.getName();
		if (!requestClasses.containsKey(className)) {
			requestClasses.put(className,  makeClassFieldMap(reqClass));
			/*
			for (String cls : requestClasses.keySet()) {
				System.out.format("Request class: %s\n", cls);
				HashMap<String, Field> fldMap = requestClasses.get(cls);
				for (String fld : fldMap.keySet()) {
					Field f = fldMap.get(fld);
					if ((f.getModifiers() & Modifier.TRANSIENT) == 0) {
						try {
							Object obj = ReflectionUtil.getFieldValue(request, f);
							if (obj != null)
								System.out.format("\tField name: %s  type: %s  value: %s\n", 
									f.getName(), f.getType().getName(), obj.toString());
						} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
							System.out.format("Exception %s calling getFieldValue for %s\n", e, f.getName());
						}
					}
				}
				System.out.println("---------------------\n");
			}
			*/
		}

		System.out.println();

		Map<String, Field> classFieldMap = requestClasses.get(className);

		try {
			String jstring = mapper.writeValueAsString(new RequestWrapper(wsid, request));
			Map<String, Object> jsonMap = mapper.readValue(jstring, new TypeReference<Map<String, Object>>(){});
			// Traverse map and truncate any strings longer than the maximum desired length
			@SuppressWarnings("unchecked")
			Map<String, Object> requestMap = (Map<String, Object>) jsonMap.get("request");
			truncateLongStrings(requestMap);
			jstring = mapper.writeValueAsString(jsonMap);
			return jstring;
			//return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(new RequestWrapper(request));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.format("Request class: %s", reqClass.getName());
		return "{}";
	}
}
