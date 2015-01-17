package pl.edu.pw.elka.spdb.route;

import java.sql.Timestamp;

public class RouteResponse {

    public enum Status {OK, ERROR};
    
    private Status status;
    private String description;
    private Route route;
    private Timestamp departureTime;
    private Timestamp arrivalTime;
    
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
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
    public Route getRoute() {
        return route;
    }
    public void setRoute(Route route) {
        this.route = route;
    }
}
