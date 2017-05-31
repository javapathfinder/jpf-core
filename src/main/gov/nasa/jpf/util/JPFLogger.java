/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package gov.nasa.jpf.util;

import java.util.ResourceBundle;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * this is a decorator for java.util.logging.JPFLogger
 *
 * We use this to avoid explicit Logger.isLoggable() checks in the code.
 * The goal is to avoid time & memory overhead if logging is not enabled.
 *
 * We provide a fat interface to avoid Object[] creation for ellipsis method
 * or auto boxing for Object arguments
 *
 */
public class JPFLogger extends Logger {

  protected Logger logger;

  public JPFLogger (Logger logger) {
    super(logger.getName(), logger.getResourceBundleName());

    this.logger = logger;
  }

  @Override
  public ResourceBundle getResourceBundle() {
    return logger.getResourceBundle();
  }

  @Override
  public String getResourceBundleName() {
    return logger.getResourceBundleName();
  }

  @Override
  public void setFilter(Filter newFilter) throws SecurityException {
    logger.setFilter(newFilter);
  }
  
  @Override
  public Filter getFilter() {
    return logger.getFilter();
  }
   
  @Override
  public void log(LogRecord record) {
    logger.log(record);
  }
  
  @Override
  public void log(Level level, String msg) {
    logger.log(level, msg);
  }
  
  @Override
  public void log(Level level, String msg, Object param1) {
    logger.log(level, msg, param1);
  }
  
  @Override
  public void log(Level level, String msg, Object params[]) {
    logger.log(level, msg, params);
  }
  
  @Override
  public void log(Level level, String msg, Throwable thrown) {
    logger.log(level, msg, thrown);
  }
  
  @Override
  public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
    logger.logp(level, sourceClass, sourceMethod, msg);
  }
  
  @Override
  public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object param1) {
    logger.logp(level, sourceClass, sourceMethod, msg, param1);
  }
  
  @Override
  public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object params[]) {
    logger.logp(level, sourceClass, sourceMethod, msg, params);
  }
  
  @Override
  public void logp(Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
    logger.logp(level, sourceClass, sourceMethod, msg, thrown);
  }
  
  @Override
@Deprecated
  public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg) {
    logger.logrb(level, sourceClass, sourceMethod, bundleName, msg);
  }
  
  @Override
@Deprecated
  public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Object param1) {
    logger.logrb(level, sourceClass, sourceMethod, bundleName, msg, param1);
  }
  
  @Override
@Deprecated
  public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Object params[]) {
    logger.logrb(level, sourceClass, sourceMethod, bundleName, msg, params);
  }
  
  @Override
@Deprecated
  public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Throwable thrown) {
    logger.logrb(level, sourceClass, sourceMethod, bundleName, msg, thrown);
  }
  
  @Override
  public void entering(String sourceClass, String sourceMethod) {
    logger.entering(sourceClass, sourceMethod);
  }

  @Override
  public void entering(String sourceClass, String sourceMethod, Object param1) {
    logger.entering(sourceClass, sourceMethod, param1);
  }
  
  @Override
  public void entering(String sourceClass, String sourceMethod, Object params[]) {
    logger.entering(sourceClass, sourceMethod, params);
  }
  
  @Override
  public void exiting(String sourceClass, String sourceMethod) {
    logger.exiting(sourceClass, sourceMethod);
  }
  
  @Override
  public void exiting(String sourceClass, String sourceMethod, Object result) {
    logger.exiting(sourceClass, sourceMethod, result);
  }
  
  @Override
  public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
    logger.throwing(sourceClass, sourceMethod, thrown);
  }
  
  @Override
  public void severe(String msg) {
    logger.severe(msg);
  }
  
  @Override
  public void warning(String msg) {
    logger.warning(msg);
  }
  
  @Override
  public void info(String msg) {
    logger.info(msg);
  }
  
  @Override
  public void config(String msg) {
    logger.config(msg);
  }
  
  @Override
  public void fine(String msg) {
    logger.fine(msg);
  }
  
  @Override
  public void finer(String msg) {
    logger.finer(msg);
  }
  
  @Override
  public void finest(String msg) {
    logger.finest(msg);
  }
  
  @Override
  public void setLevel(Level newLevel) throws SecurityException {
    logger.setLevel(newLevel);
  }
  
  @Override
  public Level getLevel() {
    return logger.getLevel();
  }
  
  @Override
  public boolean isLoggable(Level level) {
    return logger.isLoggable(level);
  }
  
  public boolean isInfoLogged(){
    return isLoggable(Level.INFO);
  }
  
  public boolean isFineLogged(){
    return isLoggable(Level.FINE);
  }  

  public boolean isFinerLogged(){
    return isLoggable(Level.FINER);
  }
  
  @Override
  public String getName() {
    return logger.getName();
  }
  
  @Override
  public void addHandler(Handler handler) throws SecurityException {
    logger.addHandler(handler);
  }
  
  @Override
  public void removeHandler(Handler handler) throws SecurityException {
    logger.removeHandler(handler);
  }
  
  @Override
  public Handler[] getHandlers() {
    return logger.getHandlers();
  }
  
  @Override
  public void setUseParentHandlers(boolean useParentHandlers) {
    logger.setUseParentHandlers(useParentHandlers);
  }
  
  @Override
  public boolean getUseParentHandlers() {
    return logger.getUseParentHandlers();
  }
  
  @Override
  public Logger getParent() {
    return logger.getParent();
  }
  
  @Override
  public void setParent(Logger parent) {
    logger.setParent(parent);
  }
  
  private void log (Level level, Object... args) {
    StringBuilder sb = new StringBuilder(256);
    int length = args.length;
    for (int i = 0; i < length; i++) {
      sb.append(args[i]);
    }
    logger.log(level, sb.toString());
  }

  //--- the SEVERE
  public void severe (Object s1, Object s2) {
    if (isLoggable(Level.SEVERE)) {
      logger.log(Level.SEVERE, s1.toString() + s2.toString());
    }
  }
  // this is here to avoid auto boxing
  public void severe (Object s1, int s2){
    if (isLoggable(Level.SEVERE)) {
         logger.log(Level.SEVERE, s1.toString() + s2);
    }
  }
  public void severe (Object s1, Object s2, Object s3){
    if (isLoggable(Level.SEVERE)) {
      logger.log(Level.SEVERE, s1.toString() + s2.toString() + s3.toString());
    }
  }
  public void severe (Object s1, Object s2, Object s3, Object s4){
    if (isLoggable(Level.SEVERE)) {
      logger.log(Level.SEVERE, s1.toString() + s2.toString() + s3.toString() + s4.toString());
    }
  }
  public void severe (Object s1, int s2, Object s3, int s4){
    if (isLoggable(Level.SEVERE)) {
      logger.log(Level.SEVERE, s1.toString() + s2 + s3.toString() + s4);
    }
  }
  public void severe (Object... args){
    if (isLoggable(Level.SEVERE)) {
      log(Level.SEVERE, args);
    }
  }
  // note this still wraps args into a String array - overhead
  public void fsevere (String format, Object... args){
    if (isLoggable(Level.SEVERE)) {
      logger.log(Level.SEVERE, String.format(format, args));
    }
  }

  //--- the WARNING
  public void warning (Object s1, Object s2) {
    if (isLoggable(Level.WARNING)) {
      logger.log(Level.WARNING, s1.toString() + s2.toString());
    }
  }
  // this is here to avoid auto boxing
  public void warning (Object s1, int s2){
    if (isLoggable(Level.WARNING)) {
         logger.log(Level.WARNING, s1.toString() + s2);
    }
  }
  public void warning (Object s1, Object s2, Object s3){
    if (isLoggable(Level.WARNING)) {
      logger.log(Level.WARNING, s1.toString() + s2.toString() + s3.toString());
    }
  }
  public void warning (Object s1, Object s2, Object s3, Object s4){
    if (isLoggable(Level.WARNING)) {
      logger.log(Level.WARNING, s1.toString() + s2.toString() + s3.toString() + s4.toString());
    }
  }
  public void warning (Object s1, int s2, Object s3, int s4){
    if (isLoggable(Level.WARNING)) {
      logger.log(Level.WARNING, s1.toString() + s2 + s3.toString() + s4);
    }
  }
  public void warning (Object... args){
    if (isLoggable(Level.WARNING)) {
      log(Level.WARNING, args);
    }
  }
  // note this still wraps args into a String array - overhead
  public void fwarning (String format, Object... args){
    if (isLoggable(Level.WARNING)) {
      logger.log(Level.WARNING, String.format(format, args));
    }
  }

  //--- the INFO
  public void info (Object s1, Object s2){
    if (isLoggable(Level.INFO)) {
      logger.log(Level.INFO, s1.toString() + s2.toString());
    }
  }
  public void info (Object s1, int s2){
    if (isLoggable(Level.INFO)) {
      logger.log(Level.INFO, s1.toString() + s2);
    }
  }
  public void info (Object s1, Object s2, Object s3){
    if (isLoggable(Level.INFO)) {
      logger.log(Level.INFO, s1.toString() + s2.toString() + s3.toString());
    }
  }
  public void info (Object s1, Object s2, Object s3, Object s4){
    if (isLoggable(Level.INFO)) {
      logger.log(Level.INFO, s1.toString() + s2.toString() + s3.toString() + s4.toString());
    }
  }
  public void info (Object s1, int s2, Object s3, int s4){
    if (isLoggable(Level.INFO)) {
      logger.log(Level.INFO, s1.toString() + s2 + s3.toString() + s4);
    }
  }
  public void info (Object... args){
    if (isLoggable(Level.INFO)) {
      log(Level.INFO, args);
    }
  }
  // note this still wraps args into a String array - overhead
  public void finfo (String format, Object... args){
    if (isLoggable(Level.INFO)) {
      logger.log(Level.INFO, String.format(format, args));
    }
  }

  //--- the CONFIG
  public void config (Object s1, Object s2){
    if (isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, s1.toString() + s2.toString());
    }
  }
  public void config (Object s1, int s2){
    if (isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, s1.toString() + s2);
    }
  }
  public void config (Object s1, Object s2, Object s3){
    if (isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, s1.toString() + s2.toString() + s3.toString());
    }
  }
  public void config (Object s1, Object s2, Object s3, Object s4){
    if (isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, s1.toString() + s2.toString() + s3.toString() + s4.toString());
    }
  }
  public void config (Object s1, int s2, Object s3, int s4){
    if (isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, s1.toString() + s2 + s3.toString() + s4);
    }
  }
  public void config (Object... args){
    if (isLoggable(Level.CONFIG)) {
      log(Level.CONFIG, args);
    }
  }
  // note this still wraps args into a String array - overhead
  public void fconfig (String format, String... args){
    if (isLoggable(Level.CONFIG)) {
      logger.log(Level.CONFIG, String.format(format, (Object)args));
    }
  }

  //--- the FINE
  public void fine (Object s1, Object s2){
    if (isLoggable(Level.FINE)) {
      logger.log(Level.FINE, s1.toString() + s2.toString());
    }
  }
  public void fine (Object s1, int s2){
    if (isLoggable(Level.FINE)) {
      logger.log(Level.FINE, s1.toString() + s2);
    }
  }
  public void fine (Object s1, Object s2, Object s3){
    if (isLoggable(Level.FINE)) {
      logger.log(Level.FINE, s1.toString() + s2.toString() + s3.toString());
    }
  }
  public void fine (Object s1, Object s2, Object s3, Object s4){
    if (isLoggable(Level.FINE)) {
      logger.log(Level.FINE, s1.toString() + s2.toString() + s3.toString() + s4.toString());
    }
  }
  public void fine (Object s1, int s2, Object s3, int s4){
    if (isLoggable(Level.FINE)) {
      logger.log(Level.FINE, s1.toString() + s2 + s3.toString() + s4);
    }
  }
  public void fine (Object... args){
    if (isLoggable(Level.FINE)) {
      log(Level.FINE, args);
    }
  }
  // note this still wraps args into a String array - overhead
  public void ffine (String format, Object... args){
    if (isLoggable(Level.FINE)) {
      logger.log(Level.FINE, String.format(format, args));
    }
  }

  //--- the FINER
  public void finer (Object s1, Object s2){
    if (isLoggable(Level.FINER)) {
      logger.log(Level.FINER, s1.toString() + s2.toString());
    }
  }
  public void finer (Object s1, int s2){
    if (isLoggable(Level.FINER)) {
      logger.log(Level.FINER, s1.toString() + s2);
    }
  }
  public void finer (Object s1, Object s2, Object s3){
    if (isLoggable(Level.FINER)) {
      logger.log(Level.FINER, s1.toString() + s2.toString() + s3.toString());
    }
  }
  public void finer (Object s1, Object s2, Object s3, Object s4){
    if (isLoggable(Level.FINER)) {
      logger.log(Level.FINER, s1.toString() + s2.toString() + s3.toString() + s4.toString());
    }
  }
  public void finer (Object s1, int s2, Object s3, int s4){
    if (isLoggable(Level.FINER)) {
      logger.log(Level.FINER, s1.toString() + s2 + s3.toString() + s4);
    }
  }
  public void finer (Object... args){
    if (isLoggable(Level.FINER)) {
      log(Level.FINER, args);
    }
  }
  // note this still wraps args into a String array - overhead
  public void ffiner (String format, Object... args){
    if (isLoggable(Level.FINER)) {
      logger.log(Level.FINER, String.format(format, args));
    }
  }

  //--- the FINEST
  public void finest (Object s1, Object s2){
    if (isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, s1.toString() + s2.toString());
    }
  }
  public void finest (Object s1, int s2){
    if (isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, s1.toString() + s2);
    }
  }
  public void finest (Object s1, Object s2, Object s3){
    if (isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, s1.toString() + s2.toString() + s3.toString());
    }
  }
  public void finest (Object s1, Object s2, Object s3, Object s4){
    if (isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, s1.toString() + s2.toString() + s3.toString() + s4.toString());
    }
  }
  public void finest (Object s1, int s2, Object s3, int s4){
    if (isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, s1.toString() + s2 + s3.toString() + s4);
    }
  }
  public void finest (Object... args){
    if (isLoggable(Level.FINEST)) {
      log(Level.FINEST, args);
    }
  }
  // note this still wraps args into a String array - overhead
  public void ffinest (String format, Object... args){
    if (isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, String.format(format, args));
    }
  }
}
