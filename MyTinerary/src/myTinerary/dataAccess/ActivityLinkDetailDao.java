package myTinerary.dataAccess;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.persistence.*;

import myTinerary.entities.ActivityLinkDetail;
import myTinerary.entities.ShareableTrip;
import myTinerary.entities.SharedTrip;
import myTinerary.entities.TripBase;

// Annotated class providing CRUD resource handling for ActivityLinkDetail
@Path("/activityLink")
public class ActivityLinkDetailDao {

	private static String findAllQueryName = "findAllActivities";
	private static String findByTrip = "findActivitiesByTrip";

	private EntityManagerFactory factory = Persistence.createEntityManagerFactory("MyTinerary");


	// GET on http://<server>:<port>/<vdir>/<servlet mapping>/activityLink/<activityLinkId>
	// for example:  http://localhost:8080/testjpa/api/activityLink/1
	// findActivityLinkDetail - find an activityLink with the given id (specified via url path parameter)
	//  Returns either the JSON representing the activityLink or null if the activityLink wasn't found
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ActivityLinkDetail findActivityLinkDetail(@PathParam("id") int activityLinkId) {
		ActivityLinkDetail activityLink = null;

		EntityManager em = null;
		try {
			em = factory.createEntityManager();

			em.getTransaction().begin();

			activityLink = em.find(ActivityLinkDetail.class, activityLinkId);

			em.getTransaction().commit();
		}
		catch (Exception ex) {
			if ((em != null) && (em.getTransaction() != null) && (em.getTransaction().isActive()))
			{
				em.getTransaction().rollback();
			}
			throw ex;
		}
		finally {
			if (em != null)
			{
				em.close();
			}
		}		
		
		return activityLink;
	}

	// GET on http://<server>:<port>/<vdir>/<servlet mapping>/activityLink/
	// for example:  http://localhost:8080/testjpa/api/activityLink/
	// findAllActivityLinkDetails - return all activityLinks
	//  Returns either the JSON representing all currently persisted activityLinks,
	//  or JSON representing an empty list if no activityLinks are defined
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ActivityLinkDetail> findAllActivityLinkDetails() {
		List<ActivityLinkDetail> activityLinks = new ArrayList<ActivityLinkDetail>();
		EntityManager em = null;
		try {		em = factory.createEntityManager();

		em.getTransaction().begin();

		TypedQuery<ActivityLinkDetail> query = em.createNamedQuery(findAllQueryName, ActivityLinkDetail.class);
		activityLinks = query.getResultList();

		em.getTransaction().commit();
		}
		catch (Exception ex) {
			if ((em != null) && (em.getTransaction() != null) && (em.getTransaction().isActive()))
			{
				em.getTransaction().rollback();
			}
			throw ex;
		}
		finally {
			if (em != null)
			{
				em.close();
			}
		}
		return activityLinks;
	}
	
	// Get the activites for a shared trip
	public List<ActivityLinkDetail> findActivitiesForTrip(SharedTrip t)
	{
		return findActivitiesForTrip(t.getTrip());
	}
	
	// Get the activity links for a trip (shared or subtrip)
	public List<ActivityLinkDetail> findActivitiesForTrip(TripBase t)
	{
		List<ActivityLinkDetail> activityLinks = new ArrayList<ActivityLinkDetail>();
		
		if (t != null) {
			EntityManager em = null;
			try	{
				em = factory.createEntityManager();

				em.getTransaction().begin();		
				TypedQuery<ActivityLinkDetail> query = em.createNamedQuery(findByTrip, ActivityLinkDetail.class);
				query.setParameter("trip", t);
				activityLinks = query.getResultList();

				em.getTransaction().commit();
			}
			catch (Exception ex) {
				if ((em != null) && (em.getTransaction() != null) && (em.getTransaction().isActive()))
				{
					em.getTransaction().rollback();
				}
				throw ex;
			}
			finally {
				if (em != null)
				{
					em.close();
				}
			}
		}		
		
		return activityLinks;
	}


	// PUT on http://<server>:<port>/<vdir>/<servlet mapping>/activityLink/<activityLinkId>
	// for example:  http://localhost:8080/testjpa/api/activityLink/1
	// updateActivityLinkDetail - update an activityLink
	//  Updates the activityLink at the given id with the properties of the specified activityLink.
	//  Note that the activityLinkId is used to find the activityLink - not whatever id is in the
	//  specified activityLink object.
	//  Returns the JSON representing all currently persisted activityLinks 
	//  (including the newly updated activityLink).
	//  Note - the specified activityLink id must correspond to a persisted activityLink, or 
	//  this method will result in an error.
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)	
	public List<ActivityLinkDetail> updateActivityLinkDetail(@PathParam("id") int activityLinkId, ActivityLinkDetail activityLink) {
		EntityManager em = null;
		
		try {
			em = factory.createEntityManager();

			em.getTransaction().begin();

			activityLink.setId(activityLinkId);
			em.merge(activityLink);

			em.getTransaction().commit();
		}
		catch (Exception ex) {
			if ((em != null) && (em.getTransaction() != null) && (em.getTransaction().isActive()))
			{
				em.getTransaction().rollback();
			}
			throw ex;
		}
		finally {
			if (em != null)
			{
				em.close();
			}
		}
		
		// If there are enhanced details we know how to get for this link, do that now.
		EnhancedLinkDetailDao.fillInEnhancedDetailsForLink(activityLink.getUrl());
	
		return findAllActivityLinkDetails();
	}

	// DELETE on http://<server>:<port>/<vdir>/<servlet mapping>/activityLink/<activityLinkId>
	// for example:  http://localhost:8080/testjpa/api/activityLink/1
	// removeActivityLinkDetail - remove an activityLink
	//  Removes the activityLink at the given id.
	//  Returns the JSON representing all currently persisted activityLinks 
	//  (with the just deleted activityLink removed).
	//  This method can be called more than once on the same id; if
	//  the activityLink doesn't exist, then there is no change made.
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ActivityLinkDetail> removeActivityLinkDetail(@PathParam("id") int activityLinkId) {
		EntityManager em = null;
		try {
			em = factory.createEntityManager();

			ActivityLinkDetail activityLink = null;

			em.getTransaction().begin();

			activityLink = em.find(ActivityLinkDetail.class, activityLinkId);
			if (activityLink != null)
			{
				em.remove(activityLink);
			}

			em.getTransaction().commit();
		}
		catch (Exception ex) {
			if ((em != null) && (em.getTransaction() != null) && (em.getTransaction().isActive()))
			{
				em.getTransaction().rollback();
			}
			throw ex;
		}
		finally {
			if (em != null)
			{
				em.close();
			}
		}

		return findAllActivityLinkDetails();
	}

	// POST on http://<server>:<port>/<vdir>/<servlet mapping>/activityLink/
	// for example:  http://localhost:8080/testjpa/api/activityLink/
	// createActivityLinkDetail - create an activityLink
	//  Creates an activityLink with a new id set with the properties of the specified activityLink.
	//  Note that the given activityLink object's id is ignored and a new one is assigned.
	//  Returns the JSON representing all currently persisted activityLinks 
	//  (including the newly created activityLink).
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<ActivityLinkDetail> createActivityLinkDetail(ActivityLinkDetail activityLink) {
		EntityManager em = null;
		try {	
			em = factory.createEntityManager();

			em.getTransaction().begin();

			em.persist(activityLink);

			em.getTransaction().commit();
		}
		catch (Exception ex) {
			if ((em != null) && (em.getTransaction() != null) && (em.getTransaction().isActive()))
			{
				em.getTransaction().rollback();
			}
			throw ex;
		}
		finally {
			if (em != null)
			{
				em.close();
			}
		}		

		// If there are enhanced details we know how to get for this link, do that now.
		EnhancedLinkDetailDao.fillInEnhancedDetailsForLink(activityLink.getUrl());
		
		return findAllActivityLinkDetails();

	}	

	// Main for testing purposes - test the DAO methods.
	// (Fiddler used to test the web service REST calls.)
	public static void main(String[] args) {		

		ActivityLinkDetailDao activityLinkMgr = new ActivityLinkDetailDao();

		// Find a trip to add a detail to.
		ShareableTripDao tripMgr = new ShareableTripDao();
		List<ShareableTrip> trips = tripMgr.findAllShareableTrips();
		if (trips.size() == 0)
		{
			System.out.println("Expecting at least one shareable trip defined - rerun add and clean");
			return;
		}
		ShareableTrip trip = trips.get(0);

		// Testing DAO operations - START
		ActivityLinkDetail al = null;

		List<ActivityLinkDetail> activityLinks = activityLinkMgr.findAllActivityLinkDetails();

		int expectedNumber = activityLinks.size();
		while (expectedNumber < 2) {
			// We need two activityLinks for the testing - add two links.
			// We will not modify links that are already created (so we can test with data in the system)
			al = new ActivityLinkDetail();
			al.setFriendlyName("link " + Integer.toString(activityLinks.size()));
			al.setUrl("http://link" + Integer.toString(activityLinks.size()));
			al.setContainingTrip(trip);	

			activityLinks = activityLinkMgr.createActivityLinkDetail(al);			
			expectedNumber += 1;
		}

		activityLinks = activityLinkMgr.findAllActivityLinkDetails(); 
		if (activityLinks.size() != expectedNumber) {
			System.out.println("Wrong number of activityLinks found?");
			return;
		}		

		// Add an activityLink just like the first one.
		al = activityLinks.get(0);

		al.setFriendlyName("added activityLink" + Integer.toString(al.getId()));
		activityLinks = activityLinkMgr.createActivityLinkDetail(al);
		if (activityLinks.size() != expectedNumber + 1) {
			System.out.println("Wrong number of activityLinks found - rerun add and clean?");
		}		

		// Find our new item...
		Integer testIndex = expectedNumber + 1;
		do 
		{
			testIndex --;
		}
		while (!activityLinks.get(testIndex).getFriendlyName().equalsIgnoreCase(al.getFriendlyName()));	


		// Get the activityLink we just added.
		al = activityLinkMgr.findActivityLinkDetail(activityLinks.get(testIndex).getId());
		if ((activityLinks.get(testIndex).getUrl() != al.getUrl()) ||
				(!activityLinks.get(testIndex).getFriendlyName().equalsIgnoreCase(al.getFriendlyName())))
		{
			System.out.println("Updated activityLink properties mismatch");
		}

		// Update the activityLink's properties
		al.setFriendlyName("updated activityLink" + Integer.toString(al.getId()));	    
		al.setUrl("http://yelp.com/whatever");
		activityLinks = activityLinkMgr.updateActivityLinkDetail(al.getId(), al);
		// Need to find our item again, the array can be in a different order for different calls
		testIndex = expectedNumber + 1;
		do 
		{
			testIndex --;
		}
		while (!activityLinks.get(testIndex).getFriendlyName().equalsIgnoreCase(al.getFriendlyName()));

		if ((activityLinks.get(testIndex).getUrl() != al.getUrl()) ||
				(!activityLinks.get(testIndex).getFriendlyName().equalsIgnoreCase(al.getFriendlyName())))
		{
			System.out.println("Updated activityLink properties mismatch");
		}		

		// Remove the activityLink we just added
		Integer removeId = activityLinks.get(testIndex).getId();	
		activityLinks = activityLinkMgr.removeActivityLinkDetail(removeId);
		if (activityLinks.size() != expectedNumber) {
			System.out.println("Wrong number of activityLinks found - rerun add and clean?");
		}	

		// Remove the entity again (should still have 2 entities left, nothing removed)
		activityLinks = activityLinkMgr.removeActivityLinkDetail(removeId);
		if (activityLinks.size() != expectedNumber) {
			System.out.println("Wrong number of activityLinks found - rerun add and clean?");
		}	

		System.out.println("Done with CRUD method test");
	}	
}

