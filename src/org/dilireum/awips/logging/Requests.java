package org.dilireum.awips.logging;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.xml.bind.annotation.*;

@XmlRootElement(name="requests")
@XmlAccessorType(XmlAccessType.NONE)
public class Requests {
	@XmlElement(name="request")
	private List<Request> requests = new ArrayList<>();

	private Map<String, Request> requestMap = new HashMap<>();

	public List<Request> getRequests() {
		return requests;
	}

	public Map<String, Request> getRequestMap() {
		return requestMap;
	}

	public void setRequestMap(Map<String, Request> requestMap) {
		this.requestMap = requestMap;
	}

	public void requestsToMap() {
		requestMap = new HashMap<String, Request>();
		for (Request req : requests) {
			requestMap.put(req.getClassName(), req);
		}
	}

	public void requestMapToList() {
		requests = new ArrayList<Request>();
		for (String name : requestMap.keySet()) {
			requests.add(requestMap.get(name));
		}
	}
}
