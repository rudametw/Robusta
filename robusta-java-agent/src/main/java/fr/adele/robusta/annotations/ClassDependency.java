package fr.adele.robusta.annotations;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassDependency {

    public String className();

    public String modifier();

    public String origin();

    public Class<?> clazz();


}
