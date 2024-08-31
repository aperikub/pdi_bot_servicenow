package fr.aperikub.botSnow;

import com.warrenstrange.googleauth.GoogleAuthenticator;

public class GoogleAuthProvider {
  private String key;


  public GoogleAuthProvider(String _key) {
    key=_key;
  }
  public int getCode(){
    GoogleAuthenticator gAuth = new GoogleAuthenticator();
    return gAuth.getTotpPassword(key);
  }
}
