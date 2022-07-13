package hw15.exceptions;

public class NoUniqueBeanException extends Exception {
    public NoUniqueBeanException(String type) {
        super("More than one bean with provided type [" + "] was found!");
    }
}
