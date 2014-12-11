package myTinerary.entities;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * Entity implementation class for Entity: TripBase
 *
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name="TripBase", uniqueConstraints = @UniqueConstraint(columnNames={"friendlyName"}))
@DiscriminatorColumn(name="TRIP_TYPE", discriminatorType=DiscriminatorType.STRING, length=20)
@NamedQueries(value = { @NamedQuery( 
 		name = "findAllTrips", 
 		query = "SELECT tb FROM TripBase tb") })
public abstract class TripBase implements Serializable {

	// Primary key field
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	// References trips owned by this user
	@OneToMany(mappedBy="containingTrip", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER) 
	@CascadeOnDelete
	private List<TripDetail> tripDetails;		
	
	@Column(name = "destination", nullable=false)
	private String destination;

	@Column(name = "friendlyName", nullable=false)
	private String friendlyName;
	
	@Column(name = "startDate", nullable=false)
	@Temporal(TemporalType.DATE)
	private Date startDate;
	
	@Column(name = "endDate")
	@Temporal(TemporalType.DATE)
	private Date endDate;	
	
	private static final long serialVersionUID = 1L;

	public TripBase() {
		super();
		// Default two fields in case they aren't specified explicitly
		Date localNow = new Date(); 
		this.setStartDate(localNow);
		this.setFriendlyName("Trip plan on " + localNow);
	}

	// Property getters and setters	
	public Integer getId() {
		return this.id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getFriendlyName() {
		return friendlyName;
	}
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(String startDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("m/d/yy");
		Date date = new Date();
		try {		
			if (startDate != null)
			{
				date = formatter.parse(startDate);
			}
			this.setStartDate(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}					
	}	
	public void setStartDate(Date startDate) {		
		
		// Strip off any time - we only care about the date.
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);	
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		if ((startDate != null) && (this.endDate != null) && (cal.getTime().getTime() > this.endDate.getTime()))
		{
			throw new IllegalArgumentException("endTime value must be greater than or equal to startTime");
		}		
		this.startDate = cal.getTime();
	}

	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("m/d/yy");
		try {
			Date date = null;
			if ((endDate != null) && (!endDate.startsWith("m")))
			{
				date = formatter.parse(endDate);
			}
			this.setEndDate(date);	
	 
		} catch (ParseException e) {
			e.printStackTrace();
		}		
	}	
	public void setEndDate(Date endDate) {
		if (endDate != null)
		{
		// Strip off any time - we only care about the date.
		Calendar cal = Calendar.getInstance();
		cal.setTime(endDate);	
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		if ((endDate != null) && (this.startDate != null) && (this.startDate.getTime() > cal.getTime().getTime()))
		{
			throw new IllegalArgumentException("endTime value must be greater than or equal to startTime");
		}			
		this.endDate = cal.getTime();
		}
		else {
			this.endDate = null;
		}
	}
	
	public List<TripDetail> getTripDetails() {
		return tripDetails;
	}
	public void setTripDetails(List<TripDetail> tripDetails) {
		this.tripDetails = tripDetails;
	}	
}
