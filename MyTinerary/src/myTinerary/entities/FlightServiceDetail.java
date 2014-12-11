package myTinerary.entities;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import myTinerary.services.AirlineCode;
import myTinerary.services.AirportCode;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.eclipse.persistence.annotations.CascadeOnDelete;

@Entity
@Table(name="FlightServiceDetail", uniqueConstraints = @UniqueConstraint(columnNames={"airlineCode", "flightNumber", "departureCity", "departureTime", "arrivalCity"}))
@DiscriminatorValue("FlightServiceDetail")
@NamedQueries(value = { @NamedQuery( 
 		name = "findAllFlightServiceDetails", 
 		query = "SELECT fd FROM FlightServiceDetail fd") })
public class FlightServiceDetail implements Serializable {

	// Primary key field = airline + flight number and departure date
	@Id
	@Column(name = "id")
	private String id;		
	
	@Column(name = "airlineCode", length=5, nullable=false)
	@Enumerated(EnumType.STRING)
	private AirlineCode airlineCode;
	
	@Column(name = "flightNumber", nullable=false)
	private Integer flightNumber;
	
	@Column(name = "departureTime", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date departureTime;		

	@Column(name = "departureCity", nullable=false)
	@Enumerated(EnumType.STRING)
	private AirportCode departureCity;	
	
	@Column(name = "arrivalCity", nullable=false)
	@Enumerated(EnumType.STRING)
	private AirportCode arrivalCity;	
	
	@Column(name = "arrivalTime", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date arrivalTime;	
	
	// This property is only set by the flight service (except for a few tests)
	@Column(name = "lastRefreshed")
	@Temporal(TemporalType.DATE)
	private Date lastRefreshed;	
		
	// If a flight is deleted, the related flight details are removed
	@OneToMany(mappedBy="relatedFlight", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER)
	@CascadeOnDelete
	@JsonIgnore
	private List<FlightDetail> tripFlights;	
	
	private static final long serialVersionUID = 1L;
	
	public FlightServiceDetail() {
		super();
	}

	
	// Minimum required fields for a flight.
	public FlightServiceDetail(AirlineCode airlineCode, Integer flightNumber, Date departureTimestamp) 
	{
		super();
		setId(airlineCode, flightNumber, departureTimestamp);
		this.airlineCode = airlineCode;
		this.flightNumber = flightNumber;
		this.departureTime = departureTimestamp;
	}


    // Property getters and setters	
	public String getId() {
		return this.id;
	}
	
	// The unique id of a flight is the airline + flight # + departure date
	public void setId(AirlineCode ac, Integer flightNo, Date departureTimestamp) {
		
		// Strip off any time - we only care about the date.
		// That way we don't have to care about the exact second of flight departure
		Calendar cal = Calendar.getInstance();
		cal.setTime(departureTimestamp);	
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date departureDay = cal.getTime();
		
		this.id = String.format("%s%d %s", ac.toString(), flightNo, departureDay.toString());
	}

	// Protect setters on the key field parts - can't changes those after instantiation.

	protected void setAirlineCode(AirlineCode airlineCode) {
		this.airlineCode = airlineCode;
	}
	protected void setDepartureTime(Date departureTime) {
		this.departureTime = departureTime;
	}	
	protected void setFlightNumber(Integer flightNumber) {
		this.flightNumber = flightNumber;
	}
	
	public Date getDepartureTime() {
		return departureTime;
	}
	public AirlineCode getAirlineCode() {
		return airlineCode;
	}
	public Integer getFlightNumber() {
		return flightNumber;
	}


	public AirportCode getDepartureCity() {
		return departureCity;
	}
	public void setDepartureCity(AirportCode departureCity) {
		this.departureCity = departureCity;
	}

	public AirportCode getArrivalCity() {
		return arrivalCity;
	}
	public void setArrivalCity(AirportCode arrivalCity) {
		this.arrivalCity = arrivalCity;
	}
	
	public Date getArrivalTime() {
		return arrivalTime;
	}
	public void setArrivalTime(Date arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public Date getLastRefreshed() {
		return lastRefreshed;
	}
	public void setLastRefreshed(Date lastRefreshed) {
		this.lastRefreshed = lastRefreshed;
	}
	
	public List<FlightDetail> getTripFlights() {
		return tripFlights;
	}
	public void setTripFlights(List<FlightDetail> tripFlights) {
		this.tripFlights = tripFlights;
	}	
}
