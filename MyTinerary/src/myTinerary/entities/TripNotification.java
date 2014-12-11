package myTinerary.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

import org.codehaus.jackson.annotate.JsonIgnore;

@Entity
@Table(name="TripNotification")
@NamedQueries(value = { @NamedQuery( 
 		name = "findAllNotifications", 
 		query = "SELECT nt FROM TripNotification nt"),
 		@NamedQuery(
 				name="findNotificationsByTripList",
 				query = "SELECT note FROM TripNotification note WHERE note.notificationList = :list")})
public class TripNotification implements Serializable {

	// Primary key field
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;		
		
	@Column(name = "addedDate")
	@Temporal(TemporalType.DATE)
	private Date addedDate;	

	@Column(name = "text", nullable=false)
	private String text;
	
	@ManyToOne(optional=false)
	@JoinColumn(name="listId", nullable=false)
	@JsonIgnore
	private TripNotificationList notificationList;
	
	private static final long serialVersionUID = 1L;
	
	public TripNotification() {
		super();
	}

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

	public Date getAddedDate() {
		return addedDate;
	}
	public void setAddedDate(Date addedDate) {
		this.addedDate = addedDate;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	public TripNotificationList getNotificationList() {
		return notificationList;
	}
	public void setNotificationList(TripNotificationList notificationList) {
		this.notificationList = notificationList;
	}

}
