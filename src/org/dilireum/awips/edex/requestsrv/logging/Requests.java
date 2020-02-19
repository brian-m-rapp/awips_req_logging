package org.dilireum.awips.edex.requestsrv.logging;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.xml.bind.annotation.*;

@XmlRootElement(name="requests")
@XmlAccessorType(XmlAccessType.NONE)
public class Requests {
	/* 
	 * Request classes are loaded from the XML file into this ArrayList.  
	 * Call requestsToMap() to copy the requests to requestMap HashMap.
	 * The HashMap provides efficient access by request class name.
	 */
	@XmlAttribute
	private int maxFieldStringLength;

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
			if (!requestMap.containsKey(req.getClassName())) {
				req.attributesToMap();
				requestMap.put(req.getClassName(), req);
			}
		}
	}

	public void requestMapToList() {
		requests = new ArrayList<Request>();
		for (String name : requestMap.keySet()) {
			requests.add(requestMap.get(name));
		}
	}
}
