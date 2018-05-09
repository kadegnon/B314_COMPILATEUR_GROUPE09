package be.unamur.info.b314.compiler.main;

public class ArrayType extends Type {

    private ScalarType scalarType;
    private int[] size;

    /**
     * Constructor for type class
     *
     * @param name of the new type
     */
    public ArrayType(String name, ScalarType scalarType, int[] size) {
        super(name);
        this.scalarType = scalarType;
        this.size = size;
    }

    /**
     * @return the base type of the array
     */
    public ScalarType getScalarType() {
        return this.scalarType;
    }

    /**
     * @return the size of the array object
     */

    public int[] getSize() {
        return size;
    }
}
