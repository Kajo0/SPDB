package elka.pw.edu.pl.spdb;

import java.util.Map;

public class Util {

    private static final String PARAM_ORIGIN = "origin";
    private static final String PARAM_DESTINATION = "destination";
    private static final String PARAM_ARRIVAL_TIME = "arrival_time";
    private static final String PARAM_DEPARTURE_TIME = "departure_time";

    private static final String SERVER_MAIN_ADDRESS = "http://mmarkiew.no-ip.info/spdb";
    private static final String SERVLET_ROUTE = SERVER_MAIN_ADDRESS + "/driving/";
    private static final String SERVLET_TRANSTI = SERVER_MAIN_ADDRESS + "/transit/";


    public static String requestRoute(boolean driving, String from, String to, Long time, boolean arrivalTime) {
        Map<String, Object> map = CustomHttpClient.createParameterMap();
        map.put(PARAM_ORIGIN, from);
        map.put(PARAM_DESTINATION, to);
        if (time != null) {
            if (arrivalTime) {
                map.put(PARAM_ARRIVAL_TIME, time);
            }
            else {
                map.put(PARAM_DEPARTURE_TIME, time);
            }
        }
        if (driving) {
            return CustomHttpClient.doGetRequest(SERVLET_ROUTE, map);
        }
        return CustomHttpClient.doGetRequest(SERVLET_TRANSTI, map);
    }
}
