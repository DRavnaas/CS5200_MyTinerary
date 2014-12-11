package myTinerary.entities;

import java.io.Serializable;

import javax.persistence.*;

import org.codehaus.jackson.annotate.JsonIgnore;


/**
 * Entity implementation class for Entity: ShareableTrip
 *
 */
@Entity
@Table(name="SubTrip")
@DiscriminatorValue("SubTrip")
@NamedQueries(value = { @NamedQuery( 
 		name = "findAllSubTrip", 
 		query = "SELECT sub FROM SubTrip sub") })
public class SubTrip extends TripBase implements Serializable {
	
	 // The subtrip detail that lists this trip
	@OneToOne(optional=false)
	@JsonIgnore
	private SubtripDetail subtripDetail;

	private static final long serialVersionUID = 1L;

	public SubTrip() {
		super();
	}
   
	// Property getters and setters
	public SubtripDetail getSubtripDetail() {
		return subtripDetail;
	}
	public void setSubtripDetail(SubtripDetail subtripDetail) {
		this.subtripDetail = subtripDetail;
	}	
}
