package pl.edu.pw.elka.spdb.route;

import java.sql.Timestamp;

public class RouteResponse {

    public enum Status {OK, ERROR};
    
    private Status status;
    private Route route;
    private String description;
    private Timestamp departureTime;
    private Timestamp arrivalTime;
    
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public Route getRoute() {
        return route;
    }
    public void setRoute(Route route) {
        this.route = route;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Timestamp getDepartureTime() {
        return departureTime;
    }
    public void setDepartureTime(Timestamp departureTime) {
        this.departureTime = departureTime;
    }
    public Timestamp getArrivalTime() {
        return arrivalTime;
    }
    public void setArrivalTime(Timestamp arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
    @Override
    public String toString() {
        return "RouteResponse [status=" + status + ", route=" + route
                + ", description=" + description + ", departureTime="
                + departureTime + ", arrivalTime=" + arrivalTime + "]";
    }
}
