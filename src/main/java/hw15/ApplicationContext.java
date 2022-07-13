package hw15;

import hw15.exceptions.NoSuchBeanException;
import hw15.exceptions.NoUniqueBeanException;

import java.util.Map;

public interface ApplicationContext {
    <T> T getBean(Class<T> beanType) throws NoSuchBeanException, NoUniqueBeanException;

    <T> T getBean(String name, Class<T> beanType) throws NoSuchBeanException;

    <T> Map<String, T> getAllBeans(Class<T> beanType);

}
