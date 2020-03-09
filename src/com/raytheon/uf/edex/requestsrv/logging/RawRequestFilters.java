package com.raytheon.uf.edex.requestsrv.logging;

import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.annotation.*;

/**
 * Class used by JAXB to transform request logging configuration from XML to POJOs.
 * This class contains the top-level list of all request filters and global attributes.
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

	/**
	 * Getter for request logging enabled flag
	 * @return boolean
	 * 	true if request logging is enabled; false if disabled
	 */
	public boolean isLoggingEnabled() {
		return loggingEnabled;
	}

	/**
	 * Getter for maximum length of logged request attribute strings
	 * @return int
	 * 	maximum length of string attributes
	 */
	public int getMaxFieldStringLength() {
		return maxFieldStringLength;
	}

	/**
	 * Getter for request filters
	 * @return List
	 * 	List of {@link RawRequestFilter}s
	 */
	public List<RawRequestFilter> getFilters() {
		return rawFilters;
	}
}
