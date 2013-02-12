package fr.adele.robusta.annotations.example;

import fr.adele.robusta.annotations.ClassDependency;
import fr.adele.robusta.annotations.Robusta;

@Robusta({
    @ClassDependency(className = "Toto", modifier = "Public", origin = "Field", clazz = Toto.class),
    @ClassDependency(className = "Tata", modifier = "Public", origin = "Field",clazz = Tata.class),
    @ClassDependency(className = "SimpleAnnotatedExample", modifier = "Public", origin = "Field", clazz = SimpleAnnotatedExample.class)})
public class SimpleAnnotatedExample {

}
