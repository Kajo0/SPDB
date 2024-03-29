package pl.edu.pw.elka.spdb.route;

import java.util.List;

public class Route {
    private List<RoutePart> parts;
    private Double length;
    private Double time;
    private String description;
    
    public Route() {
        super();
    }
    public List<RoutePart> getParts() {
        return parts;
    }
    public void setParts(List<RoutePart> parts) {
        this.parts = parts;
    }
    public Double getLength() {
        return length;
    }
    public void setLength(Double length) {
        this.length = length;
    }
    public Double getTime() {
        return time;
    }
    public void setTime(Double time) {
        this.time = time;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
