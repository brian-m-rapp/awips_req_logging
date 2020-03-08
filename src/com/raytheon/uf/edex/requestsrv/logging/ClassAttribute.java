package com.raytheon.uf.edex.requestsrv.logging;

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
@XmlRootElement(name="attribute")
@XmlAccessorType(XmlAccessType.NONE)
public class ClassAttribute {
	@XmlAttribute 
	private String name;

	@XmlAttribute
	private boolean enabled = true;

	@XmlAttribute(name="maxlength")
	private int maxLength = -1;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
}
