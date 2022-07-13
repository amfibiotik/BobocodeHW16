package hw15.exceptions;

public class NoSuchBeanException extends Exception {
    public NoSuchBeanException(String typeOrName) {
        super("Bean with provided type or name [" + typeOrName + "] not found!");
    }
}
