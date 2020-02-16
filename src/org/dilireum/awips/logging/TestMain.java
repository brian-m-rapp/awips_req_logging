package org.dilireum.awips.logging;

import com.raytheon.uf.common.dataquery.requests.DbQueryRequest;
import com.raytheon.uf.common.serialization.comm.IServerRequest;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint.ConstraintType;

import org.dilireum.awips.logging.LogStringFormatter;

public class TestMain {

	public static DbQueryRequest createDbRequest() {
		DbQueryRequest request = new DbQueryRequest();

		request.setEntityClass(request.getClass());
		request.setDistinct(true);
		request.addRequestField("dataTime", false);
		request.addRequestField("coverage.gid", false);
		request.setOrderByField("dataTime");

		request.addConstraint("sectorID", new RequestConstraint("East CONUS", ConstraintType.EQUALS));
		request.addConstraint("pluginName", new RequestConstraint("satellite", ConstraintType.EQUALS));
		request.addConstraint("physicalElement", new RequestConstraint("satDif11u13uIR", ConstraintType.IN));
		request.addConstraint("creatingEntity", new RequestConstraint("GOES%", ConstraintType.LIKE));
		return request;
	}

	public static void main(String[] args) {
		LogStringFormatter formatter = LogStringFormatter.getInstance();

		IServerRequest request = createDbRequest();

		String logString = formatter.getLogString(request);
		System.out.println(logString);
	}

}
