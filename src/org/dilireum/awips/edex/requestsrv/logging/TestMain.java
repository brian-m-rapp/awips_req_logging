package org.dilireum.awips.edex.requestsrv.logging;

import com.raytheon.uf.common.dataquery.requests.DbQueryRequest;
import com.raytheon.uf.common.dataquery.requests.DbQueryRequestSet;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint.ConstraintType;
import org.dilireum.awips.edex.requestsrv.logging.RequestLogger;

public class TestMain {

	public static DbQueryRequest createDbRequest(String satellite) {
		DbQueryRequest request = new DbQueryRequest();

		request.setEntityClass(request.getClass());
		request.setDistinct(true);
		request.addRequestField("dataTime", false);
		request.addRequestField("coverage.gid", false);
		request.setOrderByField("dataTime");

		request.addConstraint("sectorID", new RequestConstraint(String.format("%s CONUS", satellite), ConstraintType.EQUALS));
		request.addConstraint("pluginName", new RequestConstraint("satellite", ConstraintType.EQUALS));
		request.addConstraint("physicalElement", new RequestConstraint("satDif11u13uIR", ConstraintType.IN));
		request.addConstraint("creatingEntity", new RequestConstraint("GOES%", ConstraintType.LIKE));
		return request;
	}

	public static void main(String[] args) {
		String wsid = "16777343:awips:CAVE:8213:1";
		DbQueryRequestSet reqSet = new DbQueryRequestSet();
		DbQueryRequest[] requests = new DbQueryRequest[2];

		RequestLogger formatter = RequestLogger.getInstance();

		requests[0] = createDbRequest("East");
		requests[1] = createDbRequest("West");

		formatter.logRequest(wsid, requests[0]);

		reqSet.setQueries(requests);
		formatter.logRequest(wsid, reqSet);
	}
}
