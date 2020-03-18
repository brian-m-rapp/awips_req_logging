package com.raytheon.uf.edex.requestsrv.logging;

import com.raytheon.edex.utility.EDEXLocalizationAdapter;
import com.raytheon.uf.common.dataquery.requests.DbQueryRequest;
import com.raytheon.uf.common.dataquery.requests.DbQueryRequestSet;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint.ConstraintType;
import com.raytheon.uf.common.localization.PathManagerFactory;

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
        PathManagerFactory.setAdapter(new EDEXLocalizationAdapter());

        DbQueryRequestSet reqSet = new DbQueryRequestSet();
        DbQueryRequest[] requests = new DbQueryRequest[2];

        RequestLogger reqLogger = RequestLogger.getInstance();

        requests[0] = createDbRequest("East");
        requests[1] = createDbRequest("West");

        String wsid = "16777343:awips:CAVE:8213:1";
        long startTime;
        long endTime;
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 500; i++) {
            reqLogger.logRequest(wsid, requests[0]);
        }

        endTime = System.currentTimeMillis();
        System.out.format("Execution time %d ms\n", endTime-startTime);

        reqSet.setQueries(requests);
        startTime = System.currentTimeMillis();
        reqLogger.logRequest(wsid, reqSet);
        endTime = System.currentTimeMillis();
        System.out.format("Execution time %d ms\n", endTime-startTime);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            
        }
    }
}
