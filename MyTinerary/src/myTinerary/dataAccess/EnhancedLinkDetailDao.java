package myTinerary.dataAccess;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.persistence.*;

import myTinerary.entities.EnhancedLinkDetail;
import myTinerary.services.YelpAPI;

// Annotated class providing CRUD resource handling for EnhancedLinkDetail
// This class is "behind the scenes", not exposed as a public web service endpoint
public class EnhancedLinkDetailDao {

	private static String findAllQueryName = "findAllEnhancedLinkDetails";

	private EntityManagerFactory factory = Persistence.createEntityManagerFactory("MyTinerary");

	private static String EnhancedLinkPrefix = "http://www.yelp.com/biz/";
	

	// Return a specific entity by querying for the given id.
	// If the entity doesn't exist, then null is returned.
	public EnhancedLinkDetail findEnhancedLinkDetail(String enhancedLinkId) {
		EnhancedLinkDetail enhancedLink = null;
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			enhancedLink = em.find(EnhancedLinkDetail.class, enhancedLinkId);

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
		return enhancedLink;
	}


	// Return the current set of persisted entities
	public List<EnhancedLinkDetail> findAllEnhancedLinkDetails() {
		List<EnhancedLinkDetail> enhancedLinks = new ArrayList<EnhancedLinkDetail>();
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			TypedQuery<EnhancedLinkDetail> query = em.createNamedQuery(findAllQueryName, EnhancedLinkDetail.class);
			enhancedLinks = query.getResultList();

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

		return enhancedLinks;
	}


	// Insert or update an entity (aka: upsert)
	//  Note - the specified enhancedLink id must correspond to a persisted enhancedLink, or 
	//  this method will result in an error.
	protected List<EnhancedLinkDetail> upsertEnhancedLinkDetail(String enhancedLinkId, EnhancedLinkDetail enhancedLink) {
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			enhancedLink.setId(enhancedLinkId);
			em.merge(enhancedLink);

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

		return findAllEnhancedLinkDetails();
	}

	// Delete the persisted entity for the given id, and return the current set of persisted entities
	// If the entity doesn't exist, then no error occurs.
	public List<EnhancedLinkDetail> removeEnhancedLinkDetail(String enhancedLinkId) {
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			EnhancedLinkDetail enhancedLink = null;

			em.getTransaction().begin();

			enhancedLink = em.find(EnhancedLinkDetail.class, enhancedLinkId);
			if (enhancedLink != null)
			{
				em.remove(enhancedLink);
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

		return findAllEnhancedLinkDetails();
	}

	// Query for and (if found) persist the enhanced details for a given yelp link.
	// Check for enhanced details from yelp, and store them if found for the given link. 
	// Returns true if enhanced details are stored for the input link.
	// If an error occurs (bad link, no results, yelp is down, whatever)
	// then no enhanced details will be stored.
	// The return code indicates if details are stored or not.
	public static Boolean fillInEnhancedDetailsForLink(String activityLink)
	{		
		EnhancedLinkDetail details = null;
		
		// Does this look like a link we can get more info on via Yelp?
		// (Not all links in yelp are direct links to businesses - 
		// we want a url that we know how to get the yelp id out of)
		if (activityLink.toLowerCase().trim().startsWith(EnhancedLinkPrefix))
		{
			String yelpId = null;
			try {
				URI uri = new URI(activityLink);

				String[] segments = uri.getPath().split("/");
				yelpId = segments[segments.length-1];
			} catch (URISyntaxException e) {
				System.out.println("Couldn't parse yelp url, skipping query for details");
				e.printStackTrace();
				// yelpId will be empty/null is this case.
			}
			
			if (yelpId != null && !yelpId.isEmpty())
			{
				// Get the details from yelp.
				details = YelpAPI.queryBusinessAPI(yelpId);		
				
				// Were we able to fill in our entity from the yelp response?
				if (details != null)
				{
					// Upsert the details into our database.
					EnhancedLinkDetailDao detailMgr = new EnhancedLinkDetailDao();
					try
					{
						// We found a link with details - persist the details we'll show in our app.
						detailMgr.upsertEnhancedLinkDetail(activityLink, details);
					}
					catch (Exception ex)
					{
						System.out.println("Error storing enhanced details, ignoring");
						ex.printStackTrace();
						details = null;
					}
				}				
			}
		}
		
		if (details == null)
		{
			return false;
		}
		else {
			return true;
		}				
	}
	
	// Main for testing purposes - test the DAO methods.
	// (Fiddler used to test the web service REST calls.)
	// This test assumes two enhancedLinks are in the db at the start 
	public static void main(String[] args) {		

		EnhancedLinkDetailDao enhancedLinkMgr = new EnhancedLinkDetailDao();

		// Testing DAO operations - START
		EnhancedLinkDetail el = null;

		List<EnhancedLinkDetail> enhancedLinks = enhancedLinkMgr.findAllEnhancedLinkDetails();
		String bastilleLink = "http://www.yelp.com/biz/bastille-cafe-and-bar-seattle-2";
		String duckLink = "http://www.yelp.com/biz/ride-the-ducks-of-seattle-seattle";
		
		int expectedNumber = enhancedLinks.size();
		if (expectedNumber < 1) {
			
			EnhancedLinkDetailDao.fillInEnhancedDetailsForLink(bastilleLink);		
			expectedNumber += 1;
		}

		enhancedLinks = enhancedLinkMgr.findAllEnhancedLinkDetails(); 
		if (enhancedLinks.size() != expectedNumber) {
			System.out.println("Wrong number of enhancedLinks found?");
			return;
		}		

		// Add an enhancedLink just like the first one
		EnhancedLinkDetailDao.fillInEnhancedDetailsForLink(bastilleLink);		

		// This should still be the same one (query params and casing don't matter)
		EnhancedLinkDetailDao.fillInEnhancedDetailsForLink("http://www.YELP.com/biz/bastille-cafe-and-bar-seattle-2?osq=bistro");		
		
		enhancedLinks = enhancedLinkMgr.findAllEnhancedLinkDetails(); 
		if (enhancedLinks.size() != expectedNumber) {
			System.out.println("Wrong number of enhancedLinks found?");
			return;
		}	
		
		el = enhancedLinkMgr.findEnhancedLinkDetail(bastilleLink);
		if (el == null)
		{
			System.out.println("Querying by yelp url did not find inserted link");
		}
		
		// This will likely be new
		EnhancedLinkDetailDao.fillInEnhancedDetailsForLink("http://www.yelp.com/biz/ride-the-ducks-of-seattle-seattle?osq=duck+tour");		
		el = enhancedLinkMgr.findEnhancedLinkDetail(duckLink);
		if (el == null)
		{
			System.out.println("Querying by yelp url did not find inserted link");
		}	

		// We should have one more now
		enhancedLinks = enhancedLinkMgr.findAllEnhancedLinkDetails(); 
		if (enhancedLinks.size() != expectedNumber + 1) {
			System.out.println("Wrong number of enhancedLinks found?");
			return;
		}	

		// Remove the enhancedLink we just added
		enhancedLinks = enhancedLinkMgr.removeEnhancedLinkDetail(duckLink);
		if (enhancedLinks.size() != expectedNumber) {
			System.out.println("Wrong number of enhancedLinks found - rerun add and clean?");
		}	

		// Remove the entity again (should still have 2 entities left, nothing removed)
		enhancedLinks = enhancedLinkMgr.removeEnhancedLinkDetail(duckLink);
		if (enhancedLinks.size() != expectedNumber) {
			System.out.println("Wrong number of enhancedLinks found - rerun add and clean?");
		}	

		System.out.println("Done with find/upsert/remove method test");
	}	
}

