package org.dilireum.awips.edex.requestsrv.logging;

import com.raytheon.edex.utility.EDEXLocalizationAdapter;
import com.raytheon.uf.common.dataquery.requests.DbQueryRequest;
import com.raytheon.uf.common.dataquery.requests.DbQueryRequestSet;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint.ConstraintType;
import com.raytheon.uf.common.localization.PathManagerFactory;

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
		System.setProperty("edex.home", "/awips2/edex");
		System.setProperty("aw.site.identifier", "OAX");
		String edexHome = System.getProperty("edex.home");
		String siteId = System.getProperty("aw.site.identifier");
		System.out.format("edex.home: %s, aw.site.identifier: %s\n", edexHome, siteId);
		PathManagerFactory.setAdapter(new EDEXLocalizationAdapter());
		String wsid = "16777343:awips:CAVE:8213:1";
		DbQueryRequestSet reqSet = new DbQueryRequestSet();
		DbQueryRequest[] requests = new DbQueryRequest[2];

		RequestLogger logger = RequestLogger.getInstance();

		requests[0] = createDbRequest("East");
		requests[1] = createDbRequest("West");

		logger.logRequest(wsid, requests[0]);

		reqSet.setQueries(requests);
		logger.logRequest(wsid, reqSet);
	}
}
