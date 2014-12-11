package myTinerary.dataAccess;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.persistence.*;

import myTinerary.entities.*;

// Annotated class providing CRUD resource handling for ShareableTrip
@Path("/shareabletrip")
public class ShareableTripDao {

	private static String findAllQueryName = "findAllShareableTrips";
	private static String findByUser = "findAllShareableTripsForUser";

	private EntityManagerFactory factory = Persistence.createEntityManagerFactory("MyTinerary");

	// GET on http://<server>:<port>/<vdir>/<servlet mapping>/shareableTrip/<shareableTripId>
	// for example:  http://localhost:8080/MyTinerary/api/shareableTrip/1
	// findShareableTrip - find a shareableTrip with the given id (specified via url path parameter)
	//  Returns either the JSON representing the shareableTrip or null if the shareableTrip wasn't found
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ShareableTrip findShareableTrip(@PathParam("id") int shareableTripId) {
		EntityManager em = null;
		ShareableTrip shareableTrip = null;

		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			shareableTrip = em.find(ShareableTrip.class, shareableTripId);

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

		return shareableTrip;
	}

	// GET on http://<server>:<port>/<vdir>/<servlet mapping>/shareableTrip/
	// for example:  http://localhost:8080/MyTinerary/api/shareableTrip/
	// findAllShareableTrips - return all shareableTrips
	//  Returns either the JSON representing all currently persisted shareableTrips,
	//  or JSON representing an empty list if no shareableTrips are defined
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ShareableTrip> findAllShareableTrips() {
		EntityManager em = null;
		List<ShareableTrip> shareableTrips = new ArrayList<ShareableTrip>();
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			TypedQuery<ShareableTrip> query = em.createNamedQuery(findAllQueryName, ShareableTrip.class);
			shareableTrips = query.getResultList();

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
		return shareableTrips;
	}

	// Find trips for a particular user
	public List<ShareableTrip> findAllShareableTripsForUser(AuthenticatedUser user)
	{
		List<ShareableTrip> trips = new ArrayList<ShareableTrip>();

		if (user != null) {
			EntityManager em = null;
			try	{
				em = factory.createEntityManager();

				em.getTransaction().begin();		
				TypedQuery<ShareableTrip> query = em.createNamedQuery(findByUser, ShareableTrip.class);
				query.setParameter("user", user);
				trips = query.getResultList();

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

		return trips;
	}

	// PUT on http://<server>:<port>/<vdir>/<servlet mapping>/shareableTrip/<shareableTripId>
	// for example:  http://localhost:8080/MyTinerary/api/shareableTrip/1
	// updateShareableTrip - update a shareableTrip
	//  Updates the shareableTrip at the given id with the properties of the specified shareableTrip.
	//  Note that the shareableTripId is used to find the shareableTrip - not whatever id is in the
	//  specified shareableTrip object.
	//  Returns the JSON representing all currently persisted shareableTrips 
	//  (including the newly updated shareableTrip).
	//  Note - the specified shareableTrip id must correspond to a persisted shareableTrip, or 
	//  this method will result in an error.
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)	
	public List<ShareableTrip> updateShareableTrip(@PathParam("id") int shareableTripId, ShareableTrip shareableTrip) {
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			shareableTrip.setId(shareableTripId);
			em.merge(shareableTrip);

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

		return findAllShareableTrips();
	}

	// DELETE on http://<server>:<port>/<vdir>/<servlet mapping>/shareableTrip/<shareableTripId>
	// for example:  http://localhost:8080/MyTinerary/api/shareableTrip/1
	// removeShareableTrip - remove a shareableTrip
	//  Removes the shareableTrip at the given id.
	//  Returns the JSON representing all currently persisted shareableTrips 
	//  (with the just deleted shareableTrip removed).
	//  This method can be called more than once on the same id; if
	//  the shareableTrip doesn't exist, then there is no change made.
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ShareableTrip> removeShareableTrip(@PathParam("id") int shareableTripId) {
		EntityManager em = null;
		
		ShareableTrip shareableTrip = findShareableTrip(shareableTripId);
		TripNotificationListDao tripNotificationListMgr = new TripNotificationListDao();
		
		// Get rid of the contained trip notification list
		// (I tried cascade delete annotation, but it didn't work?)
		tripNotificationListMgr.removeTripNotificationList(shareableTrip.getNotificationList().getId());	
		
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			shareableTrip = em.find(ShareableTrip.class, shareableTripId);
			if (shareableTrip != null)
			{
				em.remove(shareableTrip);
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

		return findAllShareableTrips();
	}

	// POST on http://<server>:<port>/<vdir>/<servlet mapping>/shareableTrip/
	// for example:  http://localhost:8080/MyTinerary/api/shareableTrip/
	// createShareableTrip - create a shareableTrip
	//  Creates a shareableTrip with a new id set with the properties of the specified shareableTrip.
	//  Note that the given shareableTrip object's id is ignored and a new one is assigned.
	//  Returns the JSON representing all currently persisted shareableTrips 
	//  (including the newly created shareableTrip).
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<ShareableTrip> createShareableTrip(ShareableTrip shareableTrip) {
		EntityManager em = null;
		EntityTransaction tx = null;

		try	{
			em = factory.createEntityManager();

			tx = em.getTransaction();
			tx.begin();

			em.persist(shareableTrip);

			tx.commit();
		}
		catch (Exception ex) {
			if ((tx != null) && (tx.isActive()))
			{
				tx.rollback();
			}
			throw ex;
		}
		finally {
			if (em != null)
			{
				em.close();
			}
		}		

		return findAllShareableTrips();

	}	

	// Main for testing purposes - test the DAO methods.
	// (Fiddler used to test the web service REST calls.)
	// This test assumes two shareableTrips are in the db at the start 
	public static void main(String[] args) {		

		ShareableTripDao shareableTripMgr = new ShareableTripDao();
		AuthenticatedUserDao authUserMgr = new AuthenticatedUserDao();

		// Testing DAO operations - START
		// We assume we have at least two shareable trips in the DB

		List<ShareableTrip> shareableTrips = shareableTripMgr.findAllShareableTrips(); 
		int expectedNumber = shareableTrips.size();
		if (shareableTrips.size() < 2) {
			// We need two shareableTrips for the testing.
			System.out.println("Wrong number of shareableTrips found - rerun add and clean?");
			return;
		}		

		// Add a shareableTrip similar to the first one, planned by the same user

		ShareableTrip trip = new ShareableTrip();
		trip.setStartDate(new Date());
		trip.setFriendlyName("a new trip");
		trip.setDestination(shareableTrips.get(0).getDestination());
		trip.setTripOwner(shareableTrips.get(0).getTripOwner());
		AuthenticatedUser tripPlanner = trip.getTripOwner();
		Integer numPlannedTrips = tripPlanner.getPlannedTrips().size();


		shareableTrips = shareableTripMgr.createShareableTrip(trip);
		if (shareableTrips.size() != expectedNumber + 1) {
			System.out.println("Wrong number of shareableTrips found - rerun add and clean?");
		}		

		tripPlanner = authUserMgr.findAuthenticatedUser(tripPlanner.getId());
		if (tripPlanner.getPlannedTrips().size() != numPlannedTrips + 1 )
		{
			System.out.println("Trip planner should now have one more shareable trip?");			
		}


		// Find the trip we added (no guarantees on array ordering)
		Integer testIndex = expectedNumber + 1;
		do 
		{
			testIndex --;
		}
		while ((!shareableTrips.get(testIndex).getFriendlyName().equalsIgnoreCase(trip.getFriendlyName())) ||
				(shareableTrips.get(testIndex).getTripOwner().getId() != trip.getTripOwner().getId()));	

		// Get the shareableTrip we just added.
		trip = shareableTripMgr.findShareableTrip(shareableTrips.get(testIndex).getId());
		if ((!shareableTrips.get(testIndex).getFriendlyName().equalsIgnoreCase(trip.getFriendlyName())) ||
				(shareableTrips.get(testIndex).getTripOwner().getId() != trip.getTripOwner().getId()) ||
				(shareableTrips.get(testIndex).getSharedTrips().size() != 0) ||
				(shareableTrips.get(testIndex).getTripDetails().size() != 0))
		{
			System.out.println("Updated shareableTrip properties mismatch");
		}

		// Update the shareableTrip's properties
		trip.setFriendlyName("updated " + Integer.toString(trip.getId()));	
		trip.setDestination("Chiang Mai");		

		shareableTrips = shareableTripMgr.updateShareableTrip(trip.getId(), trip);
		// Have to find the trip again, array order not guaranteed
		testIndex = expectedNumber + 1;
		do 
		{
			testIndex --;
		}
		while ((shareableTrips.get(testIndex).getId() != trip.getId()));

		if ((!shareableTrips.get(testIndex).getFriendlyName().equalsIgnoreCase(trip.getFriendlyName())) ||
				(shareableTrips.get(testIndex).getTripOwner().getId() != trip.getTripOwner().getId()) ||
				(shareableTrips.get(testIndex).getSharedTrips().size() != 0) ||
				(shareableTrips.get(testIndex).getTripDetails().size() != 0))
		{
			System.out.println("Updated shareableTrip properties mismatch");
		}		

		// Remove the shareableTrip we just added
		Integer removeId = shareableTrips.get(testIndex).getId();	
		shareableTrips = shareableTripMgr.removeShareableTrip(removeId);
		if (shareableTrips.size() != expectedNumber) {
			System.out.println("Wrong number of shareableTrips found - rerun add and clean?");
		}	

		// Remove the entity again (should still have 2 entities left, nothing removed)
		shareableTrips = shareableTripMgr.removeShareableTrip(removeId);
		if (shareableTrips.size() != expectedNumber) {
			System.out.println("Wrong number of shareableTrips found - rerun add and clean?");
		}

		// Trip planner should be back to original number of planned trips
		if (authUserMgr.findAuthenticatedUser(tripPlanner.getId()).getPlannedTrips().size() != numPlannedTrips)
		{
			System.out.println("Trip planner should now be back to original shareable trips?");			
		}		

		System.out.println("Done with CRUD method test");		
	}	
}
