package myTinerary.entities;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.CascadeOnDelete;

@Entity
@Table(name="SubtripDetail")
@DiscriminatorValue("Subtrip")
@NamedQueries(value = { @NamedQuery( 
 		name = "findAllSubtripDetails", 
 		query = "SELECT st FROM SubtripDetail st") })
public class SubtripDetail extends TripDetail implements Serializable {

	// The subtrip this detail represents
	@OneToOne(mappedBy="subtripDetail", cascade=CascadeType.ALL, optional=false, fetch=FetchType.EAGER)
	@CascadeOnDelete
	private SubTrip subtrip;

	private static final long serialVersionUID = 1L;
	
	public SubtripDetail() {
		super();
	}
	
	public SubTrip getSubtrip() {
		return subtrip;
	}
	public void setSubtrip(SubTrip subtrip) {
		this.subtrip = subtrip;
	}	
}
