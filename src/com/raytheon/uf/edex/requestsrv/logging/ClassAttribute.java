package com.raytheon.uf.edex.requestsrv.logging;

import javax.xml.bind.annotation.*;

/**
 * Class used by JAXB to transform request attribute logging configuration from XML to POJOs.
 * This class contains a single attribute filter definition.
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
@XmlRootElement(name="attribute")
@XmlAccessorType(XmlAccessType.NONE)
public class ClassAttribute {
	@XmlAttribute 
	private String name;

	@XmlAttribute
	private boolean enabled = true;

	@XmlAttribute(name="maxlength")
	private int maxLength = -1;

	/**
	 * Getter for attribute name
	 * @return String
	 * 	name of attribute
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter for attribute logging enabled flag
	 * @return boolean
	 * 	true if enabled; false if disabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Getter for maximum string length
	 * @return int
	 * 	maximum length of the attribute value to write to log
	 */
	public int getMaxLength() {
		return maxLength;
	}
}
