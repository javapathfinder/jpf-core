package gov.nasa.jpf.vm;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.util.JPFLogger;

/**
 * abstract base for Loggable peers
 */
public abstract class LoggablePeer extends NativePeer {

  final JPFLogger delegatee;

  protected LoggablePeer (String loggerId) {
    delegatee = JPF.getLogger(loggerId);
  }


  @MJI
  public void severe__Ljava_lang_String_2__V (MJIEnv env, int objRef, int sRef) {
    delegatee.severe(env.getStringObject(sRef));
  }
  @MJI public void severe__Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref) {
    delegatee.severe(env.getStringObject(s1Ref), env.getStringObject(s2Ref));
  }
  @MJI public void severe__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref) {
    delegatee.severe(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref));
  }
  @MJI public void severe__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref, int s4Ref) {
    delegatee.severe(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref), env.getStringObject(s4Ref));
  }
  @MJI public void severe__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref, int s4Ref, int s5Ref) {
    delegatee.severe(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref), env.getStringObject(s4Ref), env.getStringObject(s5Ref));
  }
  @MJI public void severe___3Ljava_lang_String_2__V (MJIEnv env, int objRef, int aRef) {
    delegatee.severe((Object[]) env.getStringArrayObject(aRef));
  }

  @MJI public void warning__Ljava_lang_String_2__V (MJIEnv env, int objRef, int sRef) {
    delegatee.warning(env.getStringObject(sRef));
  }
  @MJI public void warning__Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref) {
    delegatee.warning(env.getStringObject(s1Ref), env.getStringObject(s2Ref));
  }
  @MJI public void warning__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref) {
    delegatee.warning(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref));
  }
  @MJI public void warning__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref, int s4Ref) {
    delegatee.warning(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref), env.getStringObject(s4Ref));
  }
  @MJI public void warning__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref, int s4Ref, int s5Ref) {
    delegatee.warning(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref), env.getStringObject(s4Ref), env.getStringObject(s5Ref));
  }
  @MJI public void warning___3Ljava_lang_String_2__V (MJIEnv env, int objRef, int aRef) {
    delegatee.warning((Object[]) env.getStringArrayObject(aRef));
  }

  @MJI public void info__Ljava_lang_String_2__V (MJIEnv env, int objRef, int sRef) {
    delegatee.info(env.getStringObject(sRef));
  }
  @MJI public void info__Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref) {
    delegatee.info(env.getStringObject(s1Ref), env.getStringObject(s2Ref));
  }
  @MJI public void info__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref) {
    delegatee.info(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref));
  }
  @MJI public void info__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref, int s4Ref) {
    delegatee.info(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref), env.getStringObject(s4Ref));
  }
  @MJI public void info__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref, int s4Ref, int s5Ref) {
    delegatee.info(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref), env.getStringObject(s4Ref), env.getStringObject(s5Ref));
  }
  @MJI public void info___3Ljava_lang_String_2__V (MJIEnv env, int objRef, int aRef) {
    delegatee.info((Object[]) env.getStringArrayObject(aRef));
  }

  @MJI public void fine__Ljava_lang_String_2__V (MJIEnv env, int objRef, int sRef) {
    delegatee.fine(env.getStringObject(sRef));
  }
  @MJI public void fine__Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref) {
    delegatee.fine(env.getStringObject(s1Ref), env.getStringObject(s2Ref));
  }
  @MJI public void fine__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref) {
    delegatee.fine(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref));
  }
  @MJI public void fine__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref, int s4Ref) {
    delegatee.fine(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref), env.getStringObject(s4Ref));
  }
  @MJI public void fine__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref, int s4Ref, int s5Ref) {
    delegatee.fine(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref), env.getStringObject(s4Ref), env.getStringObject(s5Ref));
  }
  @MJI public void fine___3Ljava_lang_String_2__V (MJIEnv env, int objRef, int aRef) {
    delegatee.fine((Object[]) env.getStringArrayObject(aRef));
  }

  @MJI public void finer__Ljava_lang_String_2__V (MJIEnv env, int objRef, int sRef) {
    delegatee.finer(env.getStringObject(sRef));
  }
  @MJI public void finer__Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref) {
    delegatee.finer(env.getStringObject(s1Ref), env.getStringObject(s2Ref));
  }
  @MJI public void finer__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref) {
    delegatee.finer(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref));
  }
  @MJI public void finer__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref, int s4Ref) {
    delegatee.finer(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref), env.getStringObject(s4Ref));
  }
  @MJI public void finer__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref, int s4Ref, int s5Ref) {
    delegatee.finer(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref), env.getStringObject(s4Ref), env.getStringObject(s5Ref));
  }
  @MJI public void finer___3Ljava_lang_String_2__V (MJIEnv env, int objRef, int aRef) {
    delegatee.finer((Object[]) env.getStringArrayObject(aRef));
  }

  @MJI public void finest__Ljava_lang_String_2__V (MJIEnv env, int objRef, int sRef) {
    delegatee.finest(env.getStringObject(sRef));
  }
  @MJI public void finest__Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref) {
    delegatee.finest(env.getStringObject(s1Ref), env.getStringObject(s2Ref));
  }
  @MJI public void finest__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref) {
    delegatee.finest(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref));
  }
  @MJI public void finest__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref, int s4Ref) {
    delegatee.finest(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref), env.getStringObject(s4Ref));
  }
  @MJI public void finest__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2__V (MJIEnv env, int objRef, int s1Ref, int s2Ref, int s3Ref, int s4Ref, int s5Ref) {
    delegatee.finest(env.getStringObject(s1Ref), env.getStringObject(s2Ref), env.getStringObject(s3Ref), env.getStringObject(s4Ref), env.getStringObject(s5Ref));
  }
  @MJI public void finest___3Ljava_lang_String_2__V (MJIEnv env, int objRef, int aRef) {
    delegatee.finest((Object[]) env.getStringArrayObject(aRef));
  }

}
