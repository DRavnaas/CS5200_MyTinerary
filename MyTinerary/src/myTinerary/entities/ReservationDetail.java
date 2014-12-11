package myTinerary.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name="ReservationDetail")
@DiscriminatorValue("Reservation")
@NamedQueries(value = { 
		@NamedQuery( 
 		name = "findAllReservations", 
 		query = "SELECT rsv FROM ReservationDetail rsv"),
 		@NamedQuery(
 				name="findReservationsByTrip",
 				query = "SELECT r FROM ReservationDetail r WHERE r.containingTrip = :trip")})
public class ReservationDetail extends TripDetail implements Serializable {
	
	// Rather than be hardcore about date/time formatting - the user
	// can enter free form text for the start and duration fields.
	@Column(name = "start", length=50)
	private String start;

	@Column(name = "duration", length = 50)
	private String duration;		
	
	@Column(name = "notes")
	private String notes;
	
	private static final long serialVersionUID = 1L;
	
	public ReservationDetail() {
		super();
	}

	public String getStart() {
		return start;
	}
	public void setStart(String start) {
		this.start = start;
	}

	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
}
