package com.raytheon.uf.edex.requestsrv.logging;

import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.annotation.*;

/**
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
@XmlRootElement(name="requests")
@XmlAccessorType(XmlAccessType.NONE)
public class RawRequestFilters {
	final private int defaultMaxStringLength = 160;

	@XmlAttribute
	private boolean loggingEnabled = true;

	@XmlAttribute
	private int maxFieldStringLength = defaultMaxStringLength;

	@XmlElement(name="request")
	private List<RawRequestFilter> rawFilters = new ArrayList<>();

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

	public List<RawRequestFilter> getFilters() {
		return rawFilters;
	}
}
