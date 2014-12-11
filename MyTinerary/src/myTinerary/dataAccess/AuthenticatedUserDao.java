package myTinerary.dataAccess;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.persistence.*;

import myTinerary.entities.AuthenticatedUser;
import myTinerary.entities.ShareableTrip;
import myTinerary.entities.SharedTrip;
import myTinerary.services.AuthService;

// Annotated class providing CRUD resource handling for AuthenticatedUser
// One web service endpoint = verify user by auth token 
@Path("/user")
public class AuthenticatedUserDao {

	private static String findAllQueryName = "findAllUsers";
	private static String findByAuthId = "findUserByAuthId";

	private EntityManagerFactory factory = Persistence.createEntityManagerFactory("MyTinerary");

	// GET on http://<server>:<port>/<vdir>/<servlet mapping>/user/<authId>?authToken=<token>
	// for example:  http://localhost:8080/MyTinerary/api/user/1
	// verifyAuthenticatedUser - verify a user given an authentication token.
	//  Checks if the user is currently logged in (ie: valid unexpired auth token)
	//  If the auth token is expired - queries the auth service to verify/generate a new token
	//  If the auth token is unknown - queries the auth service for the user.
	//  If the returned id from the auth service is in our system, then updates the auth token
	//  If the returned id from the auth service is not in our system, then creates a user with the auth token.
	//  If the auth service rejects the token, then the user is not authenticated and the user needs to
	//  re-login on the client to get a new auth token
	//  Returns either the JSON representing the authUser or null if the authUser wasn't found/verified.
	@GET
	@Path("/{authId}/{authToken}")
	@Produces(MediaType.APPLICATION_JSON)
	public AuthenticatedUser verifyAuthenticatedUser(
			@PathParam("authId") String authId, 
			@PathParam("authToken") String authToken) {
	
		AuthenticatedUser user = null;
		// Don't do anything unless we have values for both inputs
		if ((authId != null) && (!authId.isEmpty()) && (authToken != null) && (!authToken.isEmpty()))
		{
			// Check if we've seen this user before....
			user = findAuthenticatedUserByAuthServiceId(authId);
			
			// If the user was not found, or it's been too long since we verified, then do a call to authenticate them
			Date now = new Date();
			Date userNextCheck = now; // Default to check it now.
			if (user != null)
			{
				// If the user has logged in before, check it again at last time + 1 hour.
				userNextCheck = new Date(user.getLastAuthCheck().getTime() +  1L * 60L * 60L * 1000L);
			}
			
			System.out.println("Next user auth check is at " + userNextCheck.toString());
			if ((user == null) || (now.getTime() > userNextCheck.getTime()))
			{	
				// Get the id of this user in the auth service, plus first/last name if available
				List<String> userProps = AuthService.verifyUserAuthToken(authId, authToken);

				// Were we able to find a user via the auth service + auth token?
				if ((userProps != null) && (userProps.size() > 0) && (!userProps.get(0).isEmpty()))
				{			
					System.out.println("auth token verified by auth service");

					// If the user query was null, but userProps isn't null, then store a new user
					// If user isn't null, we found a user for the authId - ensure the authToken is updated
					if (user == null)
					{
						System.out.println("creating new user with auth token");

						// If this is a new user, add the user to the system.
						user = new AuthenticatedUser(authId);
						user.setAuthToken(authToken);
						user.setLastAuthCheck(now);
						user.setAuthId(userProps.get(0));
						user.setAuthToken(userProps.get(1));

						if (userProps.size() > 2)
						{
							user.setFirstName(userProps.get(2));						
						}					

						if (userProps.size() > 3)
						{
							user.setLastName(userProps.get(3));						
						}					

						user = createAuthenticatedUser(user);	
					}			
					else {
						// We have this user in the system. Update the user to store the new auth token
						user.setLastAuthCheck(now);
						user.setAuthId(userProps.get(0));
						user.setAuthToken(userProps.get(1));

						System.out.println("updating user for last auth check update");
						updateAuthenticatedUser(user.getId(), user);
					}				
				}
			}		
		}
		return user;		
	}	

	// Find a user in the system with a given authentication service id
	// Return null if the user is not in the system (yet?)
	public AuthenticatedUser findAuthenticatedUserByAuthServiceId(String authId)
	{
		AuthenticatedUser user = null;

		if ((authId != null) && (!authId.isEmpty())) {
			EntityManager em = null;
			try	{
				em = factory.createEntityManager();

				em.getTransaction().begin();

				TypedQuery<AuthenticatedUser> query = em.createNamedQuery(findByAuthId, AuthenticatedUser.class);
				query.setParameter("authId", authId);
				List<AuthenticatedUser> authUsers = query.getResultList();
				if ((authUsers != null) && (authUsers.size() > 0))
				{
					user = authUsers.get(0);
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
		}
		
		return user;
	}

	// Find a user by our user id
	public AuthenticatedUser findAuthenticatedUser(String userId) {
		AuthenticatedUser authUser = null;

		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			authUser = em.find(AuthenticatedUser.class, userId);

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

		return authUser;
	}


	// Find all users
	public List<AuthenticatedUser> findAllAuthenticatedUsers() {
		EntityManager em = null;
		List<AuthenticatedUser> authUsers = new ArrayList<AuthenticatedUser>();
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			TypedQuery<AuthenticatedUser> query = em.createNamedQuery(findAllQueryName, AuthenticatedUser.class);
			authUsers = query.getResultList();

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
		return authUsers;
	}


	// Update a user
	public List<AuthenticatedUser> updateAuthenticatedUser(String authUserId, AuthenticatedUser authUser) {
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			authUser.setId(authUserId);
			em.merge(authUser);

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

		return findAllAuthenticatedUsers();
	}


	// Remove a user
	public List<AuthenticatedUser> removeAuthenticatedUser(String authUserId) {
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			AuthenticatedUser authUser = null;

			em.getTransaction().begin();

			authUser = em.find(AuthenticatedUser.class, authUserId);
			if (authUser != null)
			{
				em.remove(authUser);
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

		return findAllAuthenticatedUsers();
	}

	// Create a new user
	public AuthenticatedUser createAuthenticatedUser(AuthenticatedUser authUser) {
		EntityManager em = null;
		EntityTransaction tx = null;

		try	{
			em = factory.createEntityManager();

			tx = em.getTransaction();
			tx.begin();

			em.persist(authUser);

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

		return findAuthenticatedUserByAuthServiceId(authUser.getAuthId());

	}	

	// Main for testing purposes - test the DAO methods.
	// This test assumes two authUsers are in the db at the start 
	public static void main(String[] args) {		

		AuthenticatedUserDao authUserMgr = new AuthenticatedUserDao();

		// Testing DAO operations - START

		List<AuthenticatedUser> authUsers = authUserMgr.findAllAuthenticatedUsers(); 
		int expectedNumber = authUsers.size();
		if (authUsers.size() < 2) {
			// We need two authUsers for the testing.
			System.out.println("Wrong number of authUsers found - rerun add and clean?");
			return;
		}
		AuthenticatedUser existingUser = authUsers.get(0);

		// Try to add a user using a bogus auth token
		AuthenticatedUser newUser = authUserMgr.verifyAuthenticatedUser("Nobody  ", "bogusToken");
		if (newUser != null)
		{
			System.out.println("user found using bogus token?");
		}

		// Add a new user using a "valid" auth token (will create user if necessary)
		newUser = authUserMgr.verifyAuthenticatedUser("hravnaas", "HansRavnaasAuth");
		if (newUser == null)
		{
			System.out.println("user not found using good token?");
			return;
		}	

		authUsers = authUserMgr.findAllAuthenticatedUsers(); 
		if (authUsers.size() != expectedNumber + 1) {
			System.out.println("Wrong number of authUsers found - where's our new user?");
			return;
		}		

		ShareableTrip tripToShare = null;
		if (existingUser.getPlannedTrips().size() > 0)
		{
			tripToShare = existingUser.getPlannedTrips().get(0);
		}

		// Find the authUser we just added (test find by id)
		AuthenticatedUser tempUser = authUserMgr.findAuthenticatedUser(newUser.getId());
		if ((!tempUser.getFirstName().equalsIgnoreCase(newUser.getFirstName())) ||
				(!tempUser.getLastName().equalsIgnoreCase(newUser.getLastName()))   )
		{
			System.out.println("Updated authUser properties mismatch");
		}

		// Update the authUser's properties
		newUser.setFirstName("updated" + newUser.getId());	
		newUser.setLastName("user");

		// Share a trip with them
		SharedTrip sharedTrip = new SharedTrip();
		sharedTrip.setSharedUser(newUser);
		sharedTrip.setTrip(tripToShare);	
		List<SharedTrip> sharedTrips = new ArrayList<SharedTrip>();
		sharedTrips.add(sharedTrip);
		newUser.setSharedTrips(sharedTrips);

		authUsers = authUserMgr.updateAuthenticatedUser(newUser.getId(), newUser);
		// Have to find the user again, array order not guaranteed
		Integer testIndex = expectedNumber + 1;
		do 
		{
			testIndex --;
		}
		while ((authUsers.get(testIndex).getId() != newUser.getId()));

		if ((!authUsers.get(testIndex).getFirstName().equalsIgnoreCase(newUser.getFirstName())) ||
				(!authUsers.get(testIndex).getLastName().equalsIgnoreCase(newUser.getLastName()))   )
		{
			System.out.println("Updated authUser properties mismatch");
		}		


		// Remove the authUser we just added
		String removeId = newUser.getId();	
		authUsers = authUserMgr.removeAuthenticatedUser(removeId);
		if (authUsers.size() != expectedNumber) {
			System.out.println("Wrong number of authUsers found - rerun add and clean?");
		}	

		// Remove the entity again (should still have 2 entities left, nothing removed)
		authUsers = authUserMgr.removeAuthenticatedUser(removeId);
		if (authUsers.size() != expectedNumber) {
			System.out.println("Wrong number of authUsers found - rerun add and clean?");
		}

		// Make sure the shared trip went away with the user
		existingUser = authUserMgr.findAuthenticatedUser(existingUser.getId());
		if (existingUser.getSharedTrips().size() != 0)
		{
			System.out.println("deleted shared trip, but still in orig user as shared");
		}

		System.out.println("Done with CRUD method test");		
	}	
}
