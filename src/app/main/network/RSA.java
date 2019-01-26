package app.main.network;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSA {
	
	/**
	 * String representing the RSA algorithm for Java purposes.
	 */
	private static final String RSA = "RSA";

	/**
	 * Generates a key pair (public and private key) using the RSA algorithm.
	 * @param keysize The size of the keys in bytes.
	 * @return [{@link KeyPair}] The generated key pair.
	 */
	public static KeyPair generateKeyPair(int keysize) {
		
		try {
			
			// RSA key generator.
			KeyPairGenerator keygen = KeyPairGenerator.getInstance(RSA);
			
			// Init the keygen with the specified size.
			keygen.initialize(keysize);
			
			// Generate the key pair.
			return keygen.generateKeyPair();
			
		} catch (NoSuchAlgorithmException e) {
			
			// Return a null reference in case of an exception.
			return null;
		
		}
		
	}
	
	/**
	 * Encrypts a byte array of data using the provided public key.
	 * @param key The public key to use for encryption.
	 * @param data The data to encrypt.
	 * @return [<b>byte[]</b>] The encrypted data.
	 */
	public static byte[] encrypt(PublicKey key, byte[] data) {
		
		try {
			
			// Cipher instance.
			Cipher cipher = Cipher.getInstance(RSA);
			
			// Initialize the cipher in decrypt mode.
			cipher.init(Cipher.ENCRYPT_MODE, key);
			
			// Decrypt the data.
			return cipher.doFinal(data);
			
		}catch(Exception e) {return null;}
		
	}
	
	/**
	 * Decrypts a byte array of data using the provided private key.
	 * @param key The private key to use for decryption.
	 * @param data The data to decrypt.
	 * @return [<b>byte[]</b>] The decrypted data.
	 */
	public static byte[] decrypt(PrivateKey key, byte[] data) {
		
		try {
			
			// Cipher instance.
			Cipher cipher = Cipher.getInstance(RSA);
			
			// Initialize the cipher in decrypt mode.
			cipher.init(Cipher.DECRYPT_MODE, key);
			
			// Decrypt the data.
			return cipher.doFinal(data);
			
		}catch(Exception e) {return null;}
		
	}
	
	/**
	 * Creates an RSA public key from its encoded byte array format.
	 * @return {@link PublicKey} The public key.
	 */
	public static PublicKey toPublicKey(byte[] bytes) {
		try {
			return KeyFactory.getInstance(RSA).generatePublic(new X509EncodedKeySpec(bytes));
		} catch (Exception e) {return null;}
	}
	
	/**
	 * Creates an RSA public key from its encoded byte array format.
	 * @return {@link PublicKey} The public key.
	 */
	public static PrivateKey toPrivateKey(byte[] bytes) {
		try {
			return KeyFactory.getInstance(RSA).generatePrivate(new PKCS8EncodedKeySpec(bytes));
		} catch (Exception e) {return null;}
	}
}
