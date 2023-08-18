package example.security;

import java.security.Principal;

/**
 * Represents a user.
 */
public class User implements Principal {
    private final String name;

    public User(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
