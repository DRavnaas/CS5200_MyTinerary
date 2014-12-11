package myTinerary.services;
//Base code from https://raw.githubusercontent.com/Yelp/yelp-api/master/v2/java/YelpAPI.java
//Modified for use in MyTinerary project

import org.scribe.builder.api.Api;
import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;

/**
 * Generic service provider for two-step OAuth10a.
 */
public class TwoStepOAuth extends DefaultApi10a implements Api {

  @Override
  public String getAccessTokenEndpoint() {
    return null;
  }

  @Override
  public String getAuthorizationUrl(Token arg0) {
    return null;
  }

  @Override
  public String getRequestTokenEndpoint() {
    return null;
  }
}