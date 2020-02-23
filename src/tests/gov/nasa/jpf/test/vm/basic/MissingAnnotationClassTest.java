package gov.nasa.jpf.test.vm.basic;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

import gov.nasa.jpf.jvm.JVMSystemClassLoaderInfo;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.AnnotationInfo;
import gov.nasa.jpf.vm.ClassInfoException;
import gov.nasa.jpf.vm.VM;

public class MissingAnnotationClassTest extends TestJPF {
  public static class SkippingSystemClassLoader extends JVMSystemClassLoaderInfo {
    public SkippingSystemClassLoader(VM vm, int appId) {
      super(vm, appId);
    }
    
    @Override
    public AnnotationInfo getResolvedAnnotationInfo(String typeName) throws ClassInfoException {
      if(typeName.equals("gov.nasa.jpf.test.vm.basic.MissingAnnotationClassTest$Nullable")) {
        throw new ClassInfoException("class not found: " + typeName, this, "java.lang.ClassNotFoundException", typeName);
      }
      return super.getResolvedAnnotationInfo(typeName);
    }
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Nullable {
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Present {
  }
  
  @Present
  @Nullable
  public static class AnnotatedClass {
    
  }
  
  @Test
  public void testMissingAnnotationOk() {
    if(verifyNoPropertyViolation("+vm.classloader.class=gov.nasa.jpf.test.vm.basic.MissingAnnotationClassTest$SkippingSystemClassLoader")) {
      assertEquals(1, AnnotatedClass.class.getAnnotations().length);
    }
  }
}
