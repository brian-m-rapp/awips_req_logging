package org.dilireum.awips.edex.requestsrv.logging;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.xml.bind.annotation.*;

@XmlRootElement(name="requests")
@XmlAccessorType(XmlAccessType.NONE)
public class RequestFilters {
	final private int defaultMaxStringLength = 160;

	@XmlAttribute
	private boolean loggingEnabled = true;

	@XmlAttribute
	private int maxFieldStringLength = defaultMaxStringLength;

	@XmlElement(name="request")
	private List<RequestFilter> requestFilters = new ArrayList<>();

	public boolean isLoggingEnabled() {
		return loggingEnabled;
	}

	public void setLoggingEnabled(boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}

	public int getMaxFieldStringLength() {
		return maxFieldStringLength;
	}

	public void setMaxFieldStringLength(int maxFieldStringLength) {
		this.maxFieldStringLength = maxFieldStringLength;
	}

	public List<RequestFilter> getRequests() {
		return requestFilters;
	}

	public void requestFiltersToMap(Map<String, RequestFilter> map) {
		for (RequestFilter req : requestFilters) {
			req.attributesToMap();
			map.put(req.getClassName(), req);
		}
	}
}
