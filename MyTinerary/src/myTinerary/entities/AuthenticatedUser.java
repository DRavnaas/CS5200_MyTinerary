package myTinerary.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * Entity implementation class for Entity: AuthenticatedUser
 *
 */
@Entity
@Table(name="AuthenticatedUser")
@NamedQueries(value = { 
		@NamedQuery( 
 		name = "findAllUsers", 
 		query = "SELECT u FROM AuthenticatedUser u"),
 		@NamedQuery(
 				name="findUserByAuthToken",
 				query = "SELECT u FROM AuthenticatedUser u WHERE u.authToken = :authToken"),
 		@NamedQuery(
 				name="findUserByAuthId",
 				query = "SELECT u FROM AuthenticatedUser u WHERE u.authId = :authId")
})
public class AuthenticatedUser implements Serializable {

	// Primary key field
	// behind the scenes this is equal to the auth service id
	// (Split into two fields in case we can't depend on that being unique)
	@Id
	@Column(name = "id", nullable=false)
	private String id;	

	@Column(name = "firstName", length=50)	
	private String firstName;
	
	@Column(name = "lastName", length=50)	
	private String lastName;	

	@Column(name = "authId", nullable=false)	
	private String authId;
	
	@Column(name = "authToken", nullable=false, length=255)	
	private String authToken;
	
	@Column(name = "lastAuthCheck", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastAuthCheck;

	// References trips owned by this user
	@OneToMany(mappedBy="tripOwner", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER) 
	@CascadeOnDelete
	private List<ShareableTrip> plannedTrips;	
	
	// References trips owned by this user
	@OneToMany(mappedBy="sharedUser", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER) 
	@CascadeOnDelete
	private List<SharedTrip> sharedTrips;		
	
	private static final long serialVersionUID = 1L;

	public AuthenticatedUser() {
		super();
	}
	
	public AuthenticatedUser(String authId) {
		super();
		this.setId(authId);
		this.setAuthId(authId);
	}
   
	// Property getters and setters	
	public String getId() {
		return this.id;
	}
	public void setId(String id) {
		this.id = id;
		this.authId = id;
	}	
	
	public String getFirstName() {
		return this.firstName;
	}
	public void setFirstName(String fName) {
		this.firstName = fName;
	}	
	
	public String getLastName() {
		return this.lastName;
	}
	public void setLastName(String lName) {
		this.lastName = lName;
	}	

	public String getAuthId() {
		return authId;
	}
	public void setAuthId(String authId) {
		this.authId = authId;
		this.id = authId;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public Date getLastAuthCheck() {
		return lastAuthCheck;
	}

	public void setLastAuthCheck(Date lastAuthCheck) {
		this.lastAuthCheck = lastAuthCheck;
	}	
	
	public List<ShareableTrip> getPlannedTrips() {
		return plannedTrips;
	}
	public void setPlannedTrips(List<ShareableTrip> plannedTrips) {
		this.plannedTrips = plannedTrips;
	}

	public List<SharedTrip> getSharedTrips() {
		return sharedTrips;
	}
	public void setSharedTrips(List<SharedTrip> sharedTrips) {
		this.sharedTrips = sharedTrips;
	}	
}
