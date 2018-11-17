package pl.f4.regatta;

import java.time.ZonedDateTime;;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

enum EventStatus {
    REGISTRATION, PENDING, RACE, FINISH
}

/**
 * A DTO for the Event entity.
 */
public class EventDTO implements Serializable {

    private Long id;


    private String name;

    private EventStatus status;

    private String description;


    private ZonedDateTime registerEndTime;

    private String createdBy;


    private ZonedDateTime raceStartTime;


    private Double lat;


    private Double lng;

    private Double zoom;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ZonedDateTime getRegisterEndTime() {
        return registerEndTime;
    }

    public void setRegisterEndTime(ZonedDateTime registerEndTime) {
        this.registerEndTime = registerEndTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getRaceStartTime() {
        return raceStartTime;
    }

    public void setRaceStartTime(ZonedDateTime raceStartTime) {
        this.raceStartTime = raceStartTime;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getZoom() {
        return zoom;
    }

    public void setZoom(Double zoom) {
        this.zoom = zoom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EventDTO eventDTO = (EventDTO) o;
        if(eventDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), eventDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "EventDTO{" +
                "id=" + getId() +
                ", name='" + getName() + "'" +
                ", status='" + getStatus() + "'" +
                ", description='" + getDescription() + "'" +
                ", registerEndTime='" + getRegisterEndTime() + "'" +
                ", createdBy=" + getCreatedBy() +
                ", raceStartTime='" + getRaceStartTime() + "'" +
                ", lat=" + getLat() +
                ", lng=" + getLng() +
                ", zoom=" + getZoom() +
                "}";
    }
}