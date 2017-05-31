/*
 * Copyright (C) 2015, United States Government, as represented by the
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

import java.util.logging.Level;

/**
 * convenience interface that mixes in JPFLogger interface methods
 */
public interface Loggable {

  // the primitive method used by the defaults
  JPFLogger getLogger();

  default void setLogLevel (Level newLevel){
    getLogger().setLevel(newLevel);
  }

  default void severe (String msg){
    getLogger().severe(msg);
  }
  default void severe (String a1, String a2){
    getLogger().severe(a1, a2);
  }
  default void severe (String a1, String a2, String a3){
    getLogger().severe(a1, a2, a3);
  }
  default void severe (String a1, String a2, String a3, String a4){
    getLogger().severe(a1, a2, a3, a4);
  }
  default void severe (String a1, String a2, String a3, String a4, String a5){
    getLogger().severe(a1, a2, a3, a4, a5);
  }
  default void severe (String... a) {
    getLogger().severe((Object[])a);
  }

  default void severe (Object msg){
    getLogger().severe(msg);
  }
  default void severe (Object a1, Object a2){
    getLogger().severe(a1, a2);
  }
  default void severe (Object a1, Object a2, Object a3){
    getLogger().severe(a1, a2, a3);
  }
  default void severe (Object a1, Object a2, Object a3, Object a4){
    getLogger().severe(a1, a2, a3, a4);
  }
  default void severe (Object a1, Object a2, Object a3, Object a4, Object a5){
    getLogger().severe(a1, a2, a3, a4, a5);
  }
  default void severe (Object... a) {
    getLogger().severe((Object[])a);
  }
  default void fsevere (String format, Object... a) {
    getLogger().fsevere(format, a);
  }


  default void warning (String msg){
    getLogger().warning(msg);
  }
  default void warning (String a1, String a2){
    getLogger().warning(a1, a2);
  }
  default void warning (String a1, String a2, String a3){
    getLogger().warning(a1, a2, a3);
  }
  default void warning (String a1, String a2, String a3, String a4){
    getLogger().warning(a1, a2, a3, a4);
  }
  default void warning (String a1, String a2, String a3, String a4, String a5){
    getLogger().warning(a1, a2, a3, a4, a5);
  }
  default void warning (String... a) {
    getLogger().warning((Object[]) a);
  }

  default void warning (Object msg){
    getLogger().warning(msg);
  }
  default void warning (Object a1, Object a2){
    getLogger().warning(a1, a2);
  }
  default void warning (Object a1, Object a2, Object a3){
    getLogger().warning(a1, a2, a3);
  }
  default void warning (Object a1, Object a2, Object a3, Object a4){
    getLogger().warning(a1, a2, a3, a4);
  }
  default void warning (Object a1, Object a2, Object a3, Object a4, Object a5){
    getLogger().warning(a1, a2, a3, a4, a5);
  }
  default void warning (Object... a) {
    getLogger().warning((Object[]) a);
  }
  default void fwarning (String format, Object... a) {
    getLogger().fwarning(format, a);
  }


  default void info (String msg){
    getLogger().info(msg);
  }
  default void info (String a1, String a2){
    getLogger().info(a1, a2);
  }
  default void info (String a1, String a2, String a3){
    getLogger().info(a1, a2, a3);
  }
  default void info (String a1, String a2, String a3, String a4){
    getLogger().info(a1, a2, a3, a4);
  }
  default void info (String a1, String a2, String a3, String a4, String a5){
    getLogger().info(a1, a2, a3, a4, a5);
  }
  default void info (String... a) {
    getLogger().info((Object[]) a);
  }

  default void info (Object msg){
    getLogger().info(msg);
  }
  default void info (Object a1, Object a2){
    getLogger().info(a1, a2);
  }
  default void info (Object a1, Object a2, Object a3){
    getLogger().info(a1, a2, a3);
  }
  default void info (Object a1, Object a2, Object a3, Object a4){
    getLogger().info(a1, a2, a3, a4);
  }
  default void info (Object a1, Object a2, Object a3, Object a4, Object a5){
    getLogger().info(a1, a2, a3, a4, a5);
  }
  default void info (Object... a) {
    getLogger().info((Object[]) a);
  }
  default void finfo (String format, Object... a) {
    getLogger().finfo(format, a);
  }


  default void fine (String msg){
    getLogger().fine(msg);
  }
  default void fine (String a1, String a2){
    getLogger().fine(a1, a2);
  }
  default void fine (String a1, String a2, String a3){
    getLogger().fine(a1, a2, a3);
  }
  default void fine (String a1, String a2, String a3, String a4){
    getLogger().fine(a1, a2, a3, a4);
  }
  default void fine (String a1, String a2, String a3, String a4, String a5){
    getLogger().fine(a1, a2, a3, a4, a5);
  }
  default void fine (String... a) {
    getLogger().fine((Object[]) a);
  }

  default void fine (Object msg){
    getLogger().fine(msg);
  }
  default void fine (Object a1, Object a2){
    getLogger().fine(a1, a2);
  }
  default void fine (Object a1, Object a2, Object a3){
    getLogger().fine(a1, a2, a3);
  }
  default void fine (Object a1, Object a2, Object a3, Object a4){
    getLogger().fine(a1, a2, a3, a4);
  }
  default void fine (Object a1, Object a2, Object a3, Object a4, Object a5){
    getLogger().fine(a1, a2, a3, a4, a5);
  }
  default void fine (Object... a) {
    getLogger().fine((Object[]) a);
  }
  default void ffine (String format, Object... a) {
    getLogger().ffine(format, a);
  }


  default void finer (String msg){
    getLogger().finer(msg);
  }
  default void finer (String a1, String a2){
    getLogger().finer(a1, a2);
  }
  default void finer (String a1, String a2, String a3){
    getLogger().finer(a1, a2, a3);
  }
  default void finer (String a1, String a2, String a3, String a4){
    getLogger().finer(a1, a2, a3, a4);
  }
  default void finer (String a1, String a2, String a3, String a4, String a5){
    getLogger().finer(a1, a2, a3, a4, a5);
  }
  default void finer (String... a) {
    getLogger().finer((Object[]) a);
  }

  default void finer (Object msg){
    getLogger().finer(msg);
  }
  default void finer (Object a1, Object a2){
    getLogger().finer(a1, a2);
  }
  default void finer (Object a1, Object a2, Object a3){
    getLogger().finer(a1, a2, a3);
  }
  default void finer (Object a1, Object a2, Object a3, Object a4){
    getLogger().finer(a1, a2, a3, a4);
  }
  default void finer (Object a1, Object a2, Object a3, Object a4, Object a5){
    getLogger().finer(a1, a2, a3, a4, a5);
  }
  default void finer (Object... a) {
    getLogger().finer((Object[]) a);
  }
  default void ffiner (String format, Object... a) {
    getLogger().ffiner(format, a);
  }


  default void finest (String msg){
    getLogger().finest(msg);
  }
  default void finest (String a1, String a2){
    getLogger().finest(a1, a2);
  }
  default void finest (String a1, String a2, String a3){
    getLogger().finest(a1, a2, a3);
  }
  default void finest (String a1, String a2, String a3, String a4){
    getLogger().finest(a1, a2, a3, a4);
  }
  default void finest (String a1, String a2, String a3, String a4, String a5){
    getLogger().finest(a1, a2, a3, a4, a5);
  }
  default void finest (String... a) {
    getLogger().finest((Object[]) a);
  }

  default void finest (Object msg){
    getLogger().finest(msg);
  }
  default void finest (Object a1, Object a2){
    getLogger().finest(a1, a2);
  }
  default void finest (Object a1, Object a2, Object a3){
    getLogger().finest(a1, a2, a3);
  }
  default void finest (Object a1, Object a2, Object a3, Object a4){
    getLogger().finest(a1, a2, a3, a4);
  }
  default void finest (Object a1, Object a2, Object a3, Object a4, Object a5){
    getLogger().finest(a1, a2, a3, a4, a5);
  }
  default void finest (Object... a) {
    getLogger().finest((Object[]) a);
  }
  default void ffinest (String format, Object... a) {
    getLogger().ffinest(format, a);
  }

}
