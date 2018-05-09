package be.unamur.info.b314.compiler.main;

/**
 * Class for the type
 */
public class Type {

    private String name;

    /**
     * Constructor for type class
     * @param name of the new type
     */

    public Type (String name) {
        this.name = name;
    }

    /**
     * @return name of the calling type
     */

    public String getName() {
        return this.name;
    }
}
