package myTinerary.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name="ActivityLinkDetail")
@DiscriminatorValue("ActivityLink")
@NamedQueries(value = { @NamedQuery( 
 		name = "findAllActivities", 
 		query = "SELECT al FROM ActivityLinkDetail al"),
 		@NamedQuery(
 				name="findActivitiesByTrip",
 				query = "SELECT al FROM ActivityLinkDetail al WHERE al.containingTrip = :trip") })
public class ActivityLinkDetail extends TripDetail implements Serializable {

	// (Id is inherited from TripDetail)
	
	// Note that the url is an "uncleansed" link
	// The link for the enhanced details (if one exists) is a verified link to a business
	@Column(name = "url", nullable=false, length=2048)	
	private String Url;
	
	// Note that the enhanced link detail might not exist
	@ManyToOne
	@JoinColumn(name="enhancedDetailId")
	private EnhancedLinkDetail enhancedDetail = null;	

	private static final long serialVersionUID = 1L;
	
	public ActivityLinkDetail() {
		super();
	}

	public String getUrl() {
		return Url;
	}
	public void setUrl(String url) {
		Url = url;
	}
	
	public EnhancedLinkDetail getEnhancedDetail() {
		return enhancedDetail;
	}
	public void setEnhancedDetail(EnhancedLinkDetail enhancedDetail) {
		this.enhancedDetail = enhancedDetail;
	}	
}

