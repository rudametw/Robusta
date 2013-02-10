package fr.adele.robusta.agent.manipulator;

import java.util.HashSet;
import java.util.Set;

public class Utils {
	
	public static void print(Set<String> dependencies){
		for (String s : dependencies) {
			System.out.println(Utils.getFriendlyName(s));
		}
	}
	
	public static String getFriendlyName(String canonicalClassName){
		return canonicalClassName.replace("/", ".").substring(1, canonicalClassName.length()-1);
	}
	
	public static boolean specialCharacter(char i) {
		if ((i == ';') || (i == '<'))
			return true;
		else
			return false;
	}
	
	public static boolean isUppercase(char i) {
		if ((i >= 'A') && (i <= 'Z'))
			return true;
		else
			return false;
	}
	
	public static Set<String> getClassNamesFromFieldDesc(String desc, Set<String> ignoredClasses){
		Set<String> names = new HashSet<String>();
		
		String[] descParts = desc.split("<");
		for (String descPart : descParts) {
			int lastIndex = 0;
			while (descPart.indexOf("L", lastIndex) != -1) {
				String className = "";				
				int index = descPart.indexOf("L", lastIndex);
				boolean addingToClassName = true;
				while (addingToClassName) {
					if (!Utils.specialCharacter(descPart.charAt(index))) {
						className += descPart.charAt(index);
						index = index + 1;
					}
					else { 
						if (descPart.charAt(index) == ';') {
							// class name is over
							addingToClassName = false;
							lastIndex = index;
						}
					}
					if (index >= descPart.length()) {
						addingToClassName = false;
						lastIndex = index;
					}
				}
				className += ';';
				if (!ignoredClasses.contains(className)) {
					names.add(className);
				}					
			}
		}
		
		return names;
	}
	
	public static Set<String> getClassNamesFromMethodDesc(String desc, Set<String> ignoredClasses){
		Set<String> names = new HashSet<String>();

		// A method description is like this: (<inputtypes>)<outputtypes>
		String[] parameters = desc.substring(1).split("\\)");
		for (String parameter : parameters) {
			// We'll only care about the L<type>; types
			String[] splittedDesc = parameter.split("<");
			for (String descPart : splittedDesc) {
				// I'll follow the conventions to name classes: Only the class name is written with capital letter.
				int lastIndex = 0;
				while (descPart.indexOf("L", lastIndex) != -1) {
					String className = "";
					int index = descPart.indexOf("L", lastIndex);
					boolean addingToClassName = true;
					while (addingToClassName) {
						if (!Utils.specialCharacter(descPart.charAt(index))) {
							className += descPart.charAt(index);
							index = index + 1;
						}
						else { 
							if (descPart.charAt(index) == ';') {
								// class name is over
								addingToClassName = false;
								lastIndex = index;
							}
						}
						if (index >= descPart.length()) {
							addingToClassName = false;
							lastIndex = index;
						}
					}
					className += ';';
					if (!ignoredClasses.contains(className)) {
						names.add(className);
					}					
				}
			}	
		}

		return names;
	}
	
}
