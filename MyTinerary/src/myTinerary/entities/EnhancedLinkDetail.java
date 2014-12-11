package myTinerary.entities;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

@Entity
@Table(name="EnhancedLinkDetail")
@DiscriminatorValue("EnhancedLinkDetail")
@NamedQueries(value = { @NamedQuery( 
 		name = "findAllEnhancedLinkDetails", 
 		query = "SELECT el FROM EnhancedLinkDetail el") })
public class EnhancedLinkDetail implements Serializable {

	// Primary key field = the link to the details
	@Id
	@Column(name = "providerUrlKey")
	private String providerUrl;	

	// Note that if this is deleted, we do NOT delete the related activity links
	// An activity link doesn't have to have enhanced details
	@OneToMany(mappedBy="enhancedDetail") 
	private List<ActivityLinkDetail> relatedLinks;		

	@Column(name = "name", nullable=false)
	private String name;
	
	@Column(name = "thumbnailUrl")
	private String thumbnailUrl;
	
	@Column(name = "location")
	private String location;
	
	private static final long serialVersionUID = 1L;
	
	public EnhancedLinkDetail() {
		super();
	}

	// The yelp URL is both a field and the natural key
	public String getId() {
		return getProviderUrl();
	}
	public void setId(String id) {
		// If we had to do any normalization/validation, we could do it here
		setProviderUrl(id);
	}
	
	public String getProviderUrl() {
		return providerUrl;
	}
	public void setProviderUrl(String providerUrl) {
		this.providerUrl = providerUrl;
	}

	public List<ActivityLinkDetail> getRelatedLinks() {
		return relatedLinks;
	}
	public void setRelatedLinks(List<ActivityLinkDetail> relatedLinks) {
		this.relatedLinks = relatedLinks;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
}
