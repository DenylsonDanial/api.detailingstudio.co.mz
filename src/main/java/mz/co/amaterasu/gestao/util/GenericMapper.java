package mz.co.amaterasu.gestao.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenericMapper {

    private GenericMapper() {
    }

    public static <T, R> R map(T source, Class<R> targetClass) {
        R target;
        try {
            target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
        } catch (Exception e) {
            throw new RuntimeException("Error mapping object", e);
        }
        return target;
    }

    public static <T, R> List<R> mapAll(List<T> sourceList, Class<R> targetClass) {
        if (sourceList == null) {
            return new ArrayList<>(); // return an empty list
        }

        List<R> targetList = new ArrayList<>();
        for (T source : sourceList) {
            try {
                R target = map(source, targetClass);
                targetList.add(target);
            } catch (Exception e) {
                // Log the error if necessary, but don't interrupt the process
                System.err.println("Error mapping object: " + e.getMessage());
            }
        }
        return targetList;
    }

    public static <T> void synchronize(T source, T destination) {
        BeanUtils.copyProperties(source, destination, getNullPropertyNames(source));
    }

    private static <T> String[] getNullPropertyNames(T source) {
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(source);
        return Arrays.stream(beanWrapper.getPropertyDescriptors())
                .map(PropertyDescriptor::getName)
                .filter(propertyName -> beanWrapper.getPropertyValue(propertyName) == null)
                .toArray(String[]::new);
    }

    public static <T, R> List<R> map(List<T> sourceList, Class<R> targetClass) {
        List<R> targetList = new ArrayList<>();
        for (T source : sourceList) {
            R target;
            try {
                target = targetClass.getDeclaredConstructor().newInstance();
                BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
            } catch (Exception e) {
                throw new RuntimeException("Error mapping object", e);
            }
            targetList.add(target);
        }
        return targetList;
    }
}
