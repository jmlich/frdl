package FRDL;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * creates a password hash and will check for a match with an original plain-text string
 * @author rmh
 */
    public class PasswordHasher extends App {
        String algorithm = "MD5";

        public PasswordHasher() {
        }

        public PasswordHasher(String algorithm) {
            this.algorithm = algorithm;
        }

        /*
         * Creates a hash of a plain text string
         * Returns it base64 encoded so it will save to
         * xml ok
        */
        public String hashPassword(String plainPassword)  {
            String st = null;
            try {
                byte[] passwordBytes = plainPassword.getBytes("UTF-8");
                MessageDigest digest = MessageDigest.getInstance("MD5");
                byte[] hashBytes = digest.digest(passwordBytes);
                String hashString = Base64.encodeBytes(hashBytes); //.encode(hashBytes);
                st = hashString;
            } catch (NoSuchAlgorithmException ex) {
                //Logger.getLogger(FRDLchampionshipSettings.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                //Logger.getLogger(FRDLchampionshipSettings.class.getName()).log(Level.SEVERE, null, ex);
            }
            return st;
        }

        /*
         * Checks a hash for a match with an original plain-text string
         *
        */
        public boolean verifyPassword()  {
            String plainPw = App.thisChampionship.champData.readValue("password.master");
            String hashedPw = App.thisChampionship.champData.readValue("password.hashed");
            //System.out.println("plain pw received in verifyPassword=" + plainPw);
            //System.out.println("hashed pw received in verifyPassword=" + hashedPw);
            //System.out.println("plain pw then hashed in verifyPassword=" + hashPassword(plainPw));
            return hashPassword(plainPw).equals(hashedPw);

        }

        public boolean verifyPassword (String plainPw, String hashedPw) {
            return hashPassword(plainPw).equals(hashedPw);
        }
    }
