package myTinerary.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * Entity implementation class for Entity: ShareableTrip
 *
 */
@Entity
@Table(name="ShareableTrip")
@DiscriminatorValue("ShareableTrip")
@NamedQueries(
		value = { @NamedQuery( 
 		name = "findAllShareableTrips", 
 		query = "SELECT st FROM ShareableTrip st"),
 		@NamedQuery(
 				name="findAllShareableTripsForUser",
 				query = "SELECT st FROM ShareableTrip st WHERE st.tripOwner = :user")
		})
public class ShareableTrip extends TripBase implements Serializable {
	
	// References the owner of this trip
	// Must reference an existing user
	@ManyToOne(optional=false)
	@JoinColumn(name="tripOwner", nullable=false)
	@JsonIgnore
	private AuthenticatedUser tripOwner;	
	
	// This trip can be shared with others
	@OneToMany(mappedBy="trip", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER) 
	@CascadeOnDelete	
	private List<SharedTrip> sharedTrips;		
	
	@OneToOne(mappedBy="containingTrip", cascade=CascadeType.ALL, optional=false, fetch=FetchType.EAGER)
	@CascadeOnDelete
	private TripNotificationList notificationList;
	
	private static final long serialVersionUID = 1L;

	public ShareableTrip() {
		super();
	}
   
	// Property getters and setters
	public List<SharedTrip> getSharedTrips() {
		return sharedTrips;
	}
	public void setSharedTrips(List<SharedTrip> sharedTrips) {
		this.sharedTrips = sharedTrips;
	}
	
	public AuthenticatedUser getTripOwner() {
		return this.tripOwner;
	}
	public void setTripOwner(AuthenticatedUser owner) {
		this.tripOwner = owner;
	}			
	public TripNotificationList getNotificationList() {
		return notificationList;
	}
	public void setNotificationList(TripNotificationList notificationList) {
		this.notificationList = notificationList;
	}
}
