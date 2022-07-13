package hw15;

import hw15.exceptions.NoSuchBeanException;
import hw15.exceptions.NoUniqueBeanException;
import lombok.SneakyThrows;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ApplicationContextImpl implements ApplicationContext {

    private Map<String,Object> beansMap = new HashMap<>();


    public ApplicationContextImpl(String packageName) {
        var reflections = new Reflections(packageName);
        var foundBeans = reflections.getTypesAnnotatedWith(Bean.class);
        foundBeans.forEach(bean -> beansMap.put(getBeanName(bean), createInstanceOfClass(bean)));
    }

    private String getBeanName(Class<?> clazz){
        var annotationValue = clazz.getAnnotation(Bean.class).value();
        return annotationValue.isBlank() ?
                clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1)
                : annotationValue;
    }

    @SneakyThrows
    private Object createInstanceOfClass(Class<?> clazz) {
        return clazz.cast(clazz.getConstructor().newInstance());
    }

    @Override
    public <T> T getBean(Class<T> beanType) throws NoSuchBeanException, NoUniqueBeanException {
        var beans = beansMap.values().stream()
                .filter(bean -> bean.getClass().equals(beanType))
                .map(beanType::cast).toList();
        if (beans.isEmpty()) throw new NoSuchBeanException(beanType.getSimpleName());
        if (beans.size() > 1) throw new NoUniqueBeanException(beanType.getSimpleName());
        return beans.get(0);
    }

    @Override
    public <T> T getBean(String name, Class<T> beanType) throws NoSuchBeanException {
        var bean = beansMap.get(name);
        if (bean == null) {
            throw new NoSuchBeanException(name);
        } else {
            return beanType.cast(bean);
        }
    }

    @Override
    public  <T> Map<String, T> getAllBeans(Class<T> beanType) {
        return beansMap.entrySet().stream()
                .filter(b -> b.getValue().getClass().isAssignableFrom(beanType))
                .collect(Collectors.toMap(Map.Entry::getKey, b -> beanType.cast(b.getValue())));
    }
}
