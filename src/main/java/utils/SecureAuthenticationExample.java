package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Secure Authentication Example
 *
 * This class demonstrates secure authentication practices in Java,
 * addressing the vulnerabilities found in the insecure JavaScript login function.
 *
 * Key improvements:
 * - No hardcoded credentials
 * - Password hashing with salt
 * - Rate limiting and account lockout
 * - Constant-time comparison to prevent timing attacks
 * - Secure random number generation
 *
 * Note: For production use, consider using Spring Security or similar frameworks
 * and a proper password hashing library like BCrypt.
 */
public class SecureAuthenticationExample {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000; // 15 minutes
    private static final int SALT_LENGTH = 16;
    private static final int HASH_ITERATIONS = 10000;

    // In production, load from environment variables or secure configuration
    private final String storedUsername;
    private final String storedPasswordHash;
    private final String storedSalt;

    // Track failed login attempts (in production, use a distributed cache like Redis)
    private final Map<String, FailedAttempt> failedAttempts = new ConcurrentHashMap<>();

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Constructor that loads credentials from environment variables or system properties.
     * This is more secure than hardcoding credentials in the source code.
     * System properties are checked to allow for easier testing.
     */
    public SecureAuthenticationExample() {
        // Check environment variables first, then fall back to system properties for testing
        this.storedUsername = getConfigValue("ADMIN_USERNAME");
        this.storedPasswordHash = getConfigValue("ADMIN_PASSWORD_HASH");
        this.storedSalt = getConfigValue("ADMIN_PASSWORD_SALT");

        if (storedUsername == null || storedPasswordHash == null || storedSalt == null) {
            throw new IllegalStateException("Authentication configuration missing. Please set ADMIN_USERNAME, "
                    + "ADMIN_PASSWORD_HASH, and ADMIN_PASSWORD_SALT environment variables or system properties.");
        }
    }

    /**
     * Get configuration value from environment variable or system property.
     * Environment variables take precedence for security.
     */
    private String getConfigValue(String key) {
        String value = System.getenv(key);
        if (value == null) {
            value = System.getProperty(key);
        }
        return value;
    }

    /**
     * Secure login method that addresses multiple security vulnerabilities.
     *
     * @param username The username to authenticate
     * @param password The password to verify
     * @return true if authentication succeeds, false otherwise
     * @throws AccountLockedException if the account is temporarily locked due to failed attempts
     */
    public boolean login(String username, String password) throws AccountLockedException {
        // Check for account lockout
        FailedAttempt attempt = failedAttempts.get(username);
        if (attempt != null && attempt.isLockedOut()) {
            long remainingMs = attempt.getLockoutTimeRemaining();
            throw new AccountLockedException(
                    String.format("Account locked. Try again in %d seconds.", remainingMs / 1000));
        }

        // Validate input
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            recordFailedAttempt(username);
            return false;
        }

        // Use constant-time comparison for username to prevent timing attacks
        boolean usernameMatches = constantTimeEquals(
                username.getBytes(StandardCharsets.UTF_8), storedUsername.getBytes(StandardCharsets.UTF_8));

        // Hash the provided password with the stored salt
        String hashedPassword;
        try {
            hashedPassword = hashPassword(password, storedSalt);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing algorithm not available", e);
        }

        // Use constant-time comparison for password hash
        boolean passwordMatches = constantTimeEquals(
                hashedPassword.getBytes(StandardCharsets.UTF_8), storedPasswordHash.getBytes(StandardCharsets.UTF_8));

        // Combine checks - both must be true for successful authentication
        boolean authenticated = usernameMatches && passwordMatches;

        if (!authenticated) {
            recordFailedAttempt(username);
        } else {
            clearFailedAttempts(username);
        }

        return authenticated;
    }

    /**
     * Constant-time comparison to prevent timing attacks.
     *
     * This method always compares all bytes, regardless of whether
     * differences are found early in the comparison. This prevents
     * attackers from learning about the stored value through timing analysis.
     *
     * @param a First byte array
     * @param b Second byte array
     * @return true if arrays are equal, false otherwise
     */
    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null) {
            return a == b;
        }

        // If lengths differ, still perform comparison to maintain constant time
        int length = Math.min(a.length, b.length);
        int result = a.length ^ b.length;

        for (int i = 0; i < length; i++) {
            result |= a[i] ^ b[i];
        }

        return result == 0;
    }

    /**
     * Hash a password with a salt using PBKDF2.
     *
     * In production, consider using BCrypt, SCrypt, or Argon2 instead,
     * as they are specifically designed for password hashing and include
     * built-in salting and adaptive cost factors.
     *
     * @param password The plain text password
     * @param salt The salt (as base64 string)
     * @return The hashed password as a base64 string
     * @throws NoSuchAlgorithmException if SHA-256 is not available
     */
    private String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        byte[] saltBytes = Base64.getDecoder().decode(salt);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(saltBytes);

        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

        // Multiple iterations to slow down brute force attacks
        for (int i = 1; i < HASH_ITERATIONS; i++) {
            digest.reset();
            hash = digest.digest(hash);
        }

        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Generate a new salt for password hashing.
     * This method is provided as a utility for setting up new accounts.
     *
     * @return A base64-encoded random salt
     */
    public String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hash a password with a newly generated salt.
     * This method is provided as a utility for setting up new accounts.
     *
     * @param password The plain text password
     * @return A PasswordHashResult containing both the hash and salt
     * @throws NoSuchAlgorithmException if SHA-256 is not available
     */
    public PasswordHashResult hashPasswordWithNewSalt(String password) throws NoSuchAlgorithmException {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        return new PasswordHashResult(hash, salt);
    }

    /**
     * Record a failed login attempt for rate limiting.
     */
    private void recordFailedAttempt(String username) {
        if (username == null || username.isEmpty()) {
            return;
        }

        FailedAttempt attempt = failedAttempts.computeIfAbsent(username, k -> new FailedAttempt());
        attempt.increment();
    }

    /**
     * Clear failed login attempts after successful authentication.
     */
    private void clearFailedAttempts(String username) {
        if (username != null) {
            failedAttempts.remove(username);
        }
    }

    /**
     * Tracks failed login attempts for a user.
     */
    private static class FailedAttempt {
        private int count = 0;
        private long lockoutUntil = 0;

        public synchronized void increment() {
            count++;
            if (count >= MAX_FAILED_ATTEMPTS) {
                lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS;
            }
        }

        public synchronized boolean isLockedOut() {
            if (lockoutUntil > System.currentTimeMillis()) {
                return true;
            }
            if (lockoutUntil > 0) {
                // Lockout expired, reset counters
                count = 0;
                lockoutUntil = 0;
            }
            return false;
        }

        public synchronized long getLockoutTimeRemaining() {
            if (lockoutUntil > System.currentTimeMillis()) {
                return lockoutUntil - System.currentTimeMillis();
            }
            return 0;
        }
    }

    /**
     * Custom exception for account lockout.
     */
    public static class AccountLockedException extends Exception {
        public AccountLockedException(String message) {
            super(message);
        }
    }

    /**
     * Result object containing password hash and salt.
     */
    public static class PasswordHashResult {
        private final String hash;
        private final String salt;

        public PasswordHashResult(String hash, String salt) {
            this.hash = hash;
            this.salt = salt;
        }

        public String getHash() {
            return hash;
        }

        public String getSalt() {
            return salt;
        }
    }

    /**
     * Utility method to demonstrate password hashing setup.
     * Run this once to generate hash and salt for a new admin account.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java SecureAuthenticationExample <password>");
            System.out.println("This will generate a hash and salt for the given password.");
            return;
        }

        // This is for demonstration only - environment variables should be used in production
        System.setProperty("ADMIN_USERNAME", "admin");
        System.setProperty("ADMIN_PASSWORD_HASH", "placeholder");
        System.setProperty("ADMIN_PASSWORD_SALT", "placeholder");

        try {
            SecureAuthenticationExample auth = new SecureAuthenticationExample();
            PasswordHashResult result = auth.hashPasswordWithNewSalt(args[0]);

            System.out.println("\nGenerated credentials for password: " + args[0]);
            System.out.println("\nAdd these to your environment variables:");
            System.out.println("export ADMIN_PASSWORD_HASH=\"" + result.getHash() + "\"");
            System.out.println("export ADMIN_PASSWORD_SALT=\"" + result.getSalt() + "\"");
            System.out.println("\nNever commit these values to source control!");
        } catch (Exception e) {
            System.err.println("Error generating hash: " + e.getMessage());
        }
    }
}
