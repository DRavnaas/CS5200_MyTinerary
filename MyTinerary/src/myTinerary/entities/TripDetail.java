package myTinerary.entities;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Entity implementation class for Entity: TripDetail
 *
 */
@Entity
@Table(name="TripDetail", uniqueConstraints = @UniqueConstraint(columnNames={"friendlyName", "DETAIL_TYPE", "tripId"}))
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="DETAIL_TYPE", discriminatorType=DiscriminatorType.STRING,length=20)
@NamedQueries(value = { @NamedQuery( 
 		name = "findAllTripDetails", 
 		query = "SELECT td FROM TripDetail td") })
public abstract class TripDetail implements Serializable {

	// Primary key field
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;	

	@Column(name = "friendlyName", nullable=false)
	private String friendlyName;

	@ManyToOne(optional=false)
	@JoinColumn(name="tripId", nullable=false)
	@JsonIgnore
	private TripBase containingTrip;	

	private static final long serialVersionUID = 1L;

	public TripDetail() {
		super();
		Date localNow = new Date(); 
		this.setFriendlyName("Trip detail added on " + localNow);
	}
	
	// Property getters and setters	
	public Integer getId() {
		return this.id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

	public String getFriendlyName() {
		return friendlyName;
	}
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}
	
	public TripBase getContainingTrip() {
		return containingTrip;
	}
	public void setContainingTrip(TripBase containingTrip) {
		this.containingTrip = containingTrip;
	}	
}
