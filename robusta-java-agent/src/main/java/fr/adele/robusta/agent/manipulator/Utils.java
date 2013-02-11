package fr.adele.robusta.agent.manipulator;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Opcodes;

public class Utils {
	
	public static void print(Set<Dependency> dependencies){
		for (Dependency d : dependencies) {
			System.out.println(Utils.getFriendlyName(d.getClassName()) + " - " + d.getModifier() + " " + d.getOrigin());
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
	
	public static String getModifier(int access) {
		String modifier = "";
		boolean none = true;
		if ((access & Opcodes.ACC_PUBLIC) > 0) {
			modifier += Dependency.publicModifier;
			none = false;
		}
		if ((access & Opcodes.ACC_PRIVATE) > 0) {
			modifier += Dependency.privateModifier;
			none = false;
		}
		if ((access & Opcodes.ACC_PROTECTED) > 0) {
			modifier += Dependency.protectedModifier;
			none = false;
		}
		/* if ((access & Opcodes.ACC_STATIC) > 0) {
			modifier += Dependency.staticModifier;
			none = false;
		}
		if ((access & Opcodes.ACC_FINAL) > 0) {
			modifier += Dependency.finalModifier;
			none = false;
		} */
		if (none)
			modifier += Dependency.noModifier;
		return modifier;
	}
	
	public static Set<Dependency> getClassNamesFromFieldDesc(String desc, Set<String> ignoredClasses, String modifier, String depType){
		Set<Dependency> names = new HashSet<Dependency>();
		
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
					names.add(new Dependency(className, modifier, depType));
				}					
			}
		}
		
		return names;
	}
	
	public static Set<Dependency> getClassNamesFromMethodDesc(String desc, Set<String> ignoredClasses, String modifier, String depType){
		Set<Dependency> names = new HashSet<Dependency>();

		int returnIndex = desc.indexOf(")");
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
						names.add(new Dependency(className, modifier, depType));
					}					
				}
			}	
		}

		return names;
	}
	
}
