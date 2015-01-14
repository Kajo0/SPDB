package elka.pw.edu.pl.spdb;

/**
 * Created by Maciej on 2015-01-14.
 */
public class ServerResponse {
    private final String status;
    private final Route route;
    private final String departureTime;
    private final String arrivalTime;

    public ServerResponse(String status, Route route, String departureTime, String arrivalTime) {
        this.status = status;
        this.route = route;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public String getStatus() {
        return status;
    }

    public Route getRoute() {
        return route;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }
}
