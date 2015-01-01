package elka.pw.edu.pl.spdb;

import java.util.Map;

public class Util {

    private static final String PARAM_ORIGIN = "origin";
    private static final String PARAM_DESTINATION = "destination";
    private static final String PARAM_ARRIVAL_TIME = "arrival_time";

    private static final String SERVER_MAIN_ADDRESS = "http://mmarkiew.no-ip.info";
    private static final String SERVLET_ROUTE = SERVER_MAIN_ADDRESS + "/find-route/";
    private static final String SERVLET_TRANSTI = SERVER_MAIN_ADDRESS + "/find-transport/";

    public static String requestRoute(String from, String to) {
        Map<String, Object> map = CustomHttpClient.createParameterMap();
        map.put(PARAM_ORIGIN, from);
        map.put(PARAM_DESTINATION, to);

        return CustomHttpClient.doGetRequest(SERVLET_ROUTE, map);
    }

    public static String requestTransit(String from, String to, Long startTime) {
        Map<String, Object> map = CustomHttpClient.createParameterMap();
        map.put(PARAM_ORIGIN, from);
        map.put(PARAM_DESTINATION, to);
        map.put(PARAM_ARRIVAL_TIME, startTime);

        return CustomHttpClient.doGetRequest(SERVLET_TRANSTI, map);
    }

}
