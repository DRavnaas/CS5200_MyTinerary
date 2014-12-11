package myTinerary.services;

import java.util.ArrayList;
import java.util.List;

// Represents auth provider, for server-side verification of user
public class AuthService {

	// Input = a user id in the auth system and an authToken to be verified (required)
	// Returns an empty list if the user was not recognized or the authToken was invalid
	// If user was recognized and the authToken was valid, then returns a list of properties
	// for the user - the authId and a possibly updated authToken at minimum.  If the 
	// user first and last name are known, returns those as well.
	public static List<String> verifyUserAuthToken(String authId, String authToken)
	{
		ArrayList<String> userProps = new ArrayList<String>();
		
		// ToDo: fill in facebook server-side call here (see "generate long lived token" in docs)
		// send id and auth token, get first and last name (and updated token?)
		
		if (authId.equalsIgnoreCase("dravnaas"))
		{	
			userProps.add(authId);
			userProps.add(authToken);
			userProps.add("Doyle");
			userProps.add("Ravnaas");
		}
		else if (authId.equalsIgnoreCase("hravnaas"))
		{
			userProps.add(authId);
			userProps.add(authToken);
			userProps.add("Hans");
			userProps.add("Ravnaas");
		}		
		else
		{
			userProps.add(authId);
			userProps.add(authToken);
		}				
		
		return userProps;
	}

}
