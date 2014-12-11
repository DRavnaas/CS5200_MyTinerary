package myTinerary.entities;

import java.io.Serializable;

import javax.persistence.*;

@Entity
@Table(name="FlightDetail")
@DiscriminatorValue("FlightDetail")
@NamedQueries(value = { @NamedQuery( 
 		name = "findAllFlightDetails", 
 		query = "SELECT fd FROM FlightDetail fd"),
 		@NamedQuery(
 				name="findFlightsByTrip",
 				query = "SELECT flt FROM FlightDetail flt WHERE flt.containingTrip = :trip")})
public class FlightDetail extends TripDetail implements Serializable {

	@Column(name = "confirmationCode")
	private String confirmationCode;

	// A flight must have the associated details supplied by the service
	@ManyToOne(optional=false)
	@JoinColumn(name="flightSvcDetailId", nullable=false)
	private FlightServiceDetail relatedFlight;
	
	private static final long serialVersionUID = 1L;
	
	public FlightDetail() {
		super();
	}

	public FlightServiceDetail getRelatedFlight() {
		return relatedFlight;
	}
	public void setRelatedFlight(FlightServiceDetail relatedFlight) {
		this.relatedFlight = relatedFlight;
	}
	
	public String getConfirmationCode() {
		return confirmationCode;
	}
	public void setConfirmationCode(String confirmationCode) {
		this.confirmationCode = confirmationCode;
	}

}


