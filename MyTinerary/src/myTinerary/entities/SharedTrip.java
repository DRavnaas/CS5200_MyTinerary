package myTinerary.entities;

import java.io.Serializable;

import javax.persistence.*;

import org.codehaus.jackson.annotate.JsonIgnore;

//import org.codehaus.jackson.annotate.JsonBackReference;

/**
 * Entity implementation class for Entity: SharedTrip
 *
 */
@Entity
@Table(name="SharedTrip", uniqueConstraints = @UniqueConstraint(columnNames={"trip", "sharedUser"}))
@DiscriminatorValue("SharedTrip")
@NamedQueries(
		value = { 
		@NamedQuery( 
 		name = "findAllSharedTrips", 
 		query = "SELECT sht FROM SharedTrip sht"),
 		@NamedQuery(
 				name="findAllTripsSharedWithUser",
 				query = "SELECT st FROM SharedTrip st WHERE st.sharedUser = :user"),
 		@NamedQuery(
 				name="findAllUsersTripSharedWith",
 				query = "SELECT st.sharedUser FROM SharedTrip st WHERE st.trip = :trip")
 		})
public class SharedTrip implements Serializable {
	
	// Primary key field
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;	
	
	// The actual trip that was shared to the user
	@ManyToOne(optional=false, fetch=FetchType.EAGER)
	@JoinColumn(name="trip", nullable=false)
	@JsonIgnore
	private ShareableTrip trip;	

	// The user that the trip was shared with
	@ManyToOne(optional=false, fetch=FetchType.EAGER)
	@JoinColumn(name="sharedUser", nullable=false)
	@JsonIgnore
	private AuthenticatedUser sharedUser;
	
	private static final long serialVersionUID = 1L;

	public SharedTrip() {
		super();
	}
   
	// Property getters and setters	
	public Integer getId() {
		return this.id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	public ShareableTrip getTrip() {
		return trip;
	}
	public void setTrip(ShareableTrip trip) {
		this.trip = trip;
	}

	public AuthenticatedUser getSharedUser() {
		return sharedUser;
	}
	public void setSharedUser(AuthenticatedUser sharedUser) {
		this.sharedUser = sharedUser;
	}	
}
