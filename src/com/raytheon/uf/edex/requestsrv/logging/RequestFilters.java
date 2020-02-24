package com.raytheon.uf.edex.requestsrv.logging;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.xml.bind.annotation.*;

@XmlRootElement(name="requests")
@XmlAccessorType(XmlAccessType.NONE)
public class RequestFilters {
	final private int defaultMaxStringLength = 160;

	@XmlAttribute
	private int maxFieldStringLength = defaultMaxStringLength;

	/* 
	 * RequestFilter classes are loaded from the XML file into this ArrayList.  
	 * Call requestsToMap() to copy the requestFilters to requestFiltersMap HashMap.
	 * The HashMap provides efficient access by request class name.
	 */
	@XmlElement(name="request")
	private List<RequestFilter> requestFilters = new ArrayList<>();

	private Map<String, RequestFilter> requestFiltersMap = new HashMap<>();

	public int getMaxFieldStringLength() {
		return maxFieldStringLength;
	}

	public void setMaxFieldStringLength(int maxFieldStringLength) {
		this.maxFieldStringLength = maxFieldStringLength;
	}

	public List<RequestFilter> getRequests() {
		return requestFilters;
	}

	public Map<String, RequestFilter> getRequestFiltersMap() {
		return requestFiltersMap;
	}

	public void setRequestFiltersMap(Map<String, RequestFilter> requestMap) {
		this.requestFiltersMap = requestMap;
	}

	public void requestFiltersToMap() {
		requestFiltersMap = new HashMap<String, RequestFilter>();
		for (RequestFilter req : requestFilters) {
			req.attributesToMap();
			requestFiltersMap.put(req.getClassName(), req);
		}
	}

	public void requestFiltersMapToList() {
		requestFilters = new ArrayList<RequestFilter>();
		for (String name : requestFiltersMap.keySet()) {
			requestFilters.add(requestFiltersMap.get(name));
		}
	}
}
