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

/**
 * Bob Jenkins One-at-a-Time Hash (http://www.burtleburtle.net/bob/hash/doobs.html),
 * a simple yet sufficiently avalanching hash that doesn't require a-priori knowledge
 * of the key length and is much faster than lookup3 
 */
public class OATHash {

  //--- the hash primitives
  
  public static int hashMixin (int h, int key){
    int k = key & 0xff;
    h += k; h += (h <<10); h ^= (h >>> 6);
    
    key >>= 8;
    k = key & 0xff;
    h += k; h += (h <<10); h ^= (h >>> 6);

    key >>= 8;
    k = key & 0xff;
    h += k; h += (h <<10); h ^= (h >>> 6);

    key >>= 8;
    k = key & 0xff;
    h += k; h += (h <<10); h ^= (h >>> 6);
    
    return h;
  }
  
  public static int hashMixin (int h, long key) {
    h = hashMixin( h, (int)key);
    h = hashMixin( h, (int)(key >> 32));
    return h;
  }
  
  public static int hashFinalize (int h){
    h += (h << 3);
    h ^= (h >>> 11);
    h += (h << 15);
    
    return h;
  }

  //--- the one step public hashers
  
  public static int hash (int key){
    return hashFinalize( hashMixin(0,key));
  }
  
  public static int hash (int key1, int key2){
    int h = hashMixin(0,key1);
    h = hashMixin(h, key2);
    return hashFinalize(h);
  }
  
  public static int hash (long key) {
    int h = hashMixin(0, (int)key);
    h = hashMixin( h, (int)(key>>32));
    return hashFinalize(h);
  }
  
  public static int hash (int key1, int key2, int key3) {
    int h = hashMixin( 0, key1);
    h = hashMixin( h, key2);
    h = hashMixin( h, key3);
    
    return hashFinalize(h);
  }
  
  public static int hash (int[] keys){
    int h = 0;
    for (int i=0; i<keys.length; i++){
      h = hashMixin( h, keys[i]);
    }
    return hashFinalize(h);
  }
}
