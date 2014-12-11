package myTinerary.dataAccess;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.persistence.*;

import myTinerary.entities.*;

// Annotated class providing CRUD resource handling for SharedTrip
@Path("/sharedtrip")
public class SharedTripDao {

	private static String findAllQueryName = "findAllSharedTrips";
	private static String findSharedWithUser = "findAllTripsSharedWithUser";
	private static String findUsersTripSharedWith = "findAllUsersTripSharedWith";

	private EntityManagerFactory factory = Persistence.createEntityManagerFactory("MyTinerary");

	// GET on http://<server>:<port>/<vdir>/<servlet mapping>/sharedTrip/<sharedTripId>
	// for example:  http://localhost:8080/MyTinerary/api/sharedTrip/1
	// findSharedTrip - find a sharedTrip with the given id (specified via url path parameter)
	//  Returns either the JSON representing the sharedTrip or null if the sharedTrip wasn't found
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public SharedTrip findSharedTrip(@PathParam("id") int sharedTripId) {
		EntityManager em = null;
		SharedTrip sharedTrip = null;
		
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			sharedTrip = em.find(SharedTrip.class, sharedTripId);

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
		
		return sharedTrip;
	}

	// GET on http://<server>:<port>/<vdir>/<servlet mapping>/sharedTrip/
	// for example:  http://localhost:8080/MyTinerary/api/sharedTrip/
	// findAllSharedTrips - return all sharedTrips
	//  Returns either the JSON representing all currently persisted sharedTrips,
	//  or JSON representing an empty list if no sharedTrips are defined
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SharedTrip> findAllSharedTrips() {
		EntityManager em = null;
		List<SharedTrip> sharedTrips = new ArrayList<SharedTrip>();
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			TypedQuery<SharedTrip> query = em.createNamedQuery(findAllQueryName, SharedTrip.class);
			sharedTrips = query.getResultList();

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
		return sharedTrips;
	}

	
	// Find users that a trip is shared with (does not include trip owner/planner)
	public List<AuthenticatedUser> findUsersTripSharedWith(ShareableTrip trip)
	{
		List<AuthenticatedUser> viewers = new ArrayList<AuthenticatedUser>();

		if (trip != null) {
			EntityManager em = null;
			try	{
				em = factory.createEntityManager();

				em.getTransaction().begin();		
				TypedQuery<AuthenticatedUser> query = em.createNamedQuery(findUsersTripSharedWith, AuthenticatedUser.class);
				query.setParameter("trip", trip);
				viewers = query.getResultList();

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

		return viewers;
	}	
	
	// Find trips shared with a particular user
	public List<SharedTrip> findAllTripsSharedWithUser(AuthenticatedUser user)
	{
		List<SharedTrip> trips = new ArrayList<SharedTrip>();

		if (user != null) {
			EntityManager em = null;
			try	{
				em = factory.createEntityManager();

				em.getTransaction().begin();		
				TypedQuery<SharedTrip> query = em.createNamedQuery(findSharedWithUser, SharedTrip.class);
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
	
	// ToDo: is update even needed for shared trip?  It's a mapping of two ids, so we likely don't update the records
	//       after insertion.
	// PUT on http://<server>:<port>/<vdir>/<servlet mapping>/sharedTrip/<sharedTripId>
	// for example:  http://localhost:8080/MyTinerary/api/sharedTrip/1
	// updateSharedTrip - update a sharedTrip
	//  Updates the sharedTrip at the given id with the properties of the specified sharedTrip.
	//  Note that the sharedTripId is used to find the sharedTrip - not whatever id is in the
	//  specified sharedTrip object.
	//  Returns the JSON representing all currently persisted sharedTrips 
	//  (including the newly updated sharedTrip).
	//  Note - the specified sharedTrip id must correspond to a persisted sharedTrip, or 
	//  this method will result in an error.
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)	
	public List<SharedTrip> updateSharedTrip(@PathParam("id") int sharedTripId, SharedTrip sharedTrip) {
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			sharedTrip.setId(sharedTripId);
			em.merge(sharedTrip);

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
		
		return findAllSharedTrips();
	}

	// DELETE on http://<server>:<port>/<vdir>/<servlet mapping>/sharedTrip/<sharedTripId>
	// for example:  http://localhost:8080/MyTinerary/api/sharedTrip/1
	// removeSharedTrip - remove a sharedTrip
	//  Removes the sharedTrip at the given id.
	//  Returns the JSON representing all currently persisted sharedTrips 
	//  (with the just deleted sharedTrip removed).
	//  This method can be called more than once on the same id; if
	//  the sharedTrip doesn't exist, then there is no change made.
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SharedTrip> removeSharedTrip(@PathParam("id") int sharedTripId) {
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			SharedTrip sharedTrip = null;

			em.getTransaction().begin();

			sharedTrip = em.find(SharedTrip.class, sharedTripId);
			if (sharedTrip != null)
			{
				em.remove(sharedTrip);
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
				
		return findAllSharedTrips();
	}

	// POST on http://<server>:<port>/<vdir>/<servlet mapping>/sharedTrip/
	// for example:  http://localhost:8080/MyTinerary/api/sharedTrip/
	// createSharedTrip - create a sharedTrip
	//  Creates a sharedTrip with a new id set with the properties of the specified sharedTrip.
	//  Note that the given sharedTrip object's id is ignored and a new one is assigned.
	//  Returns the JSON representing all currently persisted sharedTrips 
	//  (including the newly created sharedTrip).
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<SharedTrip> createSharedTrip(SharedTrip sharedTrip) {
		EntityManager em = null;
		EntityTransaction tx = null;
		
		try	{
			em = factory.createEntityManager();

			tx = em.getTransaction();
			tx.begin();

			em.persist(sharedTrip);

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
		
		ShareableTrip trip = sharedTrip.getTrip();
		new TripNotificationListDao().createTripNotificationInTrip(trip, "Trip shared with user " + sharedTrip.getSharedUser().getId());

		return findAllSharedTrips();

	}	

	// Main for testing purposes - test the DAO methods.
	// (Fiddler used to test the web service REST calls.)
	// This test assumes two sharedTrips are in the db at the start 
	public static void main(String[] args) {		

		SharedTripDao sharedTripMgr = new SharedTripDao();
		ShareableTripDao shareableTripMgr = new ShareableTripDao();
		AuthenticatedUserDao authUserMgr = new AuthenticatedUserDao();

		// Testing DAO operations - START
		// We assume we have at least two shareable trips in the DB
		List<ShareableTrip> shareableTrips = shareableTripMgr.findAllShareableTrips();
		if (shareableTrips.size() < 2) {
			// We need two sharedTrips for the testing.
			System.out.println("Wrong number of sharedTrips found - rerun add and clean?");
			return;
		}		
		
		// We will create forcibly create a new user (ie: not via auth service)
		// and share existing trips with them
		AuthenticatedUser tempUser = new AuthenticatedUser("newuser");
		tempUser.setFirstName("newbie");
		tempUser.setLastName("whatever");
		tempUser.setAuthId("newuser");
		tempUser.setAuthToken("whateverToken");
		tempUser.setLastAuthCheck(new Date());
		AuthenticatedUser newUser = authUserMgr.createAuthenticatedUser(tempUser);
		if ((newUser.getPlannedTrips().size() != 0) ||
				(newUser.getSharedTrips().size() != 0) ||
				(!newUser.getFirstName().equalsIgnoreCase(tempUser.getFirstName())) ||
				(!newUser.getLastName().equalsIgnoreCase(tempUser.getLastName())))
		{
			System.out.println("User properties mismatch after create");
		}
		
		
		String newUserId = newUser.getId();
		ShareableTrip origTrip = shareableTrips.get(0);
		SharedTrip sharedTrip = new SharedTrip();
		
		sharedTrip.setSharedUser(newUser);
		sharedTrip.setTrip(origTrip);

		// Add the "link" back from the user to the trip shared with them.
		List<SharedTrip> sharedTrips = new ArrayList<SharedTrip>();
		sharedTrips.add(sharedTrip);
		newUser.setSharedTrips(sharedTrips);

		sharedTrips = sharedTripMgr.createSharedTrip(sharedTrip);
		Integer expectedNumber = sharedTrips.size();
		Integer i = expectedNumber;
		SharedTrip foundtrip = null;
		do
		{
			i--;
			foundtrip = sharedTrips.get(i);
		}
		while ((foundtrip.getTrip().getId() != sharedTrip.getTrip().getId()) ||
			   (foundtrip.getSharedUser().getId() != sharedTrip.getSharedUser().getId()));
		
		
		// Get the sharedTrip we just added using the id, and check properties again.
		sharedTrip = sharedTripMgr.findSharedTrip(foundtrip.getId());
		if ((foundtrip.getTrip().getId() != sharedTrip.getTrip().getId()) ||
				   (foundtrip.getSharedUser().getId() != sharedTrip.getSharedUser().getId()))
		{
			System.out.println("Updated sharedTrip properties mismatch");
		}	

		List<SharedTrip> updateSharedTrips = newUser.getSharedTrips();
		if (updateSharedTrips.size() == 0)
		{
			updateSharedTrips.add(sharedTrip);
		}
		//authUserMgr.updateAuthenticatedUser(newUser.getId(), newUser);
		
		newUser = authUserMgr.findAuthenticatedUser(newUserId);
		if (newUser.getSharedTrips().size() != 1)
		{
			System.out.println("New user should have one trip shared with them");
		}		
		
		if (foundtrip.getSharedUser().getId() != newUser.getId())
		{
			System.out.println("Trip should show as shared with ");
			
		}
	
		if (foundtrip.getTrip().getTripOwner().getId() != origTrip.getTripOwner().getId())
		{
			System.out.println("Trip owner of shared trip should be the same as the original shareable trip");			
		}
		
		// Remove the sharedTrip we just added
		Integer removeId = sharedTrips.get(i).getId();	
		sharedTrips = sharedTripMgr.removeSharedTrip(removeId);
		if (sharedTrips.size() != expectedNumber-1) {
			System.out.println("Wrong number of sharedTrips found - rerun add and clean?");
		}	
		
		// Remove the entity again (should still have 2 entities left, nothing removed)
		sharedTrips = sharedTripMgr.removeSharedTrip(removeId);
		if (sharedTrips.size() != expectedNumber-1) {
			System.out.println("Wrong number of sharedTrips found - rerun add and clean?");
		}
		
		updateSharedTrips = newUser.getSharedTrips();
		if (updateSharedTrips.size() == 1)
		{
			// ToDo: handle in remove for user and shared trips remove methods
			newUser.setSharedTrips(new ArrayList<SharedTrip>());
		}
		authUserMgr.updateAuthenticatedUser(newUser.getId(), newUser);

		newUser = authUserMgr.findAuthenticatedUser(newUserId);
		if (newUser.getSharedTrips().size() != 0)
		{
			System.out.println("New user should have no trips shared with them");
		}		
		
		authUserMgr.removeAuthenticatedUser(newUser.getId());
		
		System.out.println("Done with CRUD method test");		
	}	
}
