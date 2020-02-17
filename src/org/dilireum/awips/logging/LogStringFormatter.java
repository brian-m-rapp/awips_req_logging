package org.dilireum.awips.logging;

import java.util.HashMap; 
import java.util.Map; 
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;

import com.raytheon.uf.common.util.ReflectionUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import com.raytheon.uf.common.serialization.comm.IServerRequest;

public class LogStringFormatter {

	private static final LogStringFormatter instance = new LogStringFormatter();

	private Map<String, HashMap<String, Field>> requestClasses = new HashMap<>();

	private LogStringFormatter() {
		// Instantiate the request output structure from the XML config file
	}

	private final ObjectMapper mapper = new ObjectMapper();

	public static LogStringFormatter getInstance() {
		return instance;
	}

	private HashMap<String, Field> makeClassFieldMap(Class<?> clazz) {
		HashMap<String, Field> classFields = new HashMap<>();
		List<Field> fields = ReflectionUtil.getAllFields(clazz);
		for (Field field : fields) {
			classFields.put(field.getName(), field);
		}

		return classFields;
	}

	private class RequestWrapper {
		private String reqClass;
		private IServerRequest request;

		public String getReqClass() {
			return reqClass;
		}

		public void setReqClass(String reqClass) {
			this.reqClass = reqClass;
		}

		public IServerRequest getRequest() {
			return request;
		}

		public void setRequest(IServerRequest request) {
			this.request = request;
		}

		public RequestWrapper(IServerRequest request) {
			this.reqClass = request.getClass().getName();
			this.request = request;
		}
	}

	public String getLogString(IServerRequest request) {
		Class<?> reqClass = request.getClass();
		String className = reqClass.getName();
		if (!requestClasses.containsKey(className)) {
			requestClasses.put(className,  makeClassFieldMap(reqClass));
		}

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

		System.out.println();

		try {
			return mapper.writeValueAsString(new RequestWrapper(request));
			//return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(new RequestWrapper(request));
			//return String.format("{\"reqClass\":\"%s\", \"request\":%s}", 
			//		className, mapper.writeValueAsString(request));
			//return String.format("{\"reqClass\":\"%s\", \"request\":%s}", 
			//		className, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
			//System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.format("Request class: %s", reqClass.getName());
		return "";
	}
}
