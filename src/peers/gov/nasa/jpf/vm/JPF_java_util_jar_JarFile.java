package gov.nasa.jpf.vm;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.jpf.annotation.MJI;

public class JPF_java_util_jar_JarFile extends NativePeer {
	
	@MJI
	public int getMetaInfEntryNames_____3Ljava_lang_String_2(MJIEnv env, int thisRef) {
		int entryNames = env.getReferenceField(thisRef, "entryNames");
		List<String> l = new ArrayList<>();
		int len = env.getArrayLength(entryNames);
		for(int i = 0; i < len; i++) {
			int nameRef = env.getReferenceArrayElement(entryNames, i);
			if(nameRef == MJIEnv.NULL) {
				continue;
			}
			String name = env.getStringObject(nameRef);
			if(name.startsWith("META-INF/")) {
				l.add(name);
			}
		}
		return env.newStringArray(l.toArray(new String[l.size()]));
	}
}
