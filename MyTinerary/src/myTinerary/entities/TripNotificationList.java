package myTinerary.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.eclipse.persistence.annotations.CascadeOnDelete;

// Represents a to-do list in a trip.  A to-do list is a container for to-do list items.
@Entity
@Table(name="TripNotificationList", uniqueConstraints = @UniqueConstraint(columnNames={"containingTrip"}))
@NamedQueries(value = { @NamedQuery( 
 		name = "findAllTripNotificationLists", 
 		query = "SELECT notices FROM TripNotificationList notices") })
public class TripNotificationList implements Serializable {

	// Primary key field
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;		

	@OneToOne
	@JsonIgnore	
	@JoinColumn(name = "containingTrip")
	private ShareableTrip containingTrip;

	// References notifications contained in this list
	@OneToMany(mappedBy="notificationList", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER) 
	@CascadeOnDelete
	private List<TripNotification> notifications;


	private static final long serialVersionUID = 1L;
	
	
	public TripNotificationList() {
		super();
	}

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}	
	public ShareableTrip getContainingTrip() {
		return containingTrip;
	}

	public void setContainingTrip(ShareableTrip containingTrip) {
		this.containingTrip = containingTrip;
	}	
	
	public List<TripNotification> getNotifications() {
		return notifications;
	}
	public void setNotifications(List<TripNotification> notifications) {
		this.notifications = notifications;
	}
}
