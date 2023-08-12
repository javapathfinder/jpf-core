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
package gov.nasa.jpf.search;

import gov.nasa.jpf.JPFListener;

/**
 * The {@code SearchListener} interface defines the methods available to register for notifications by the {@code Search} object.
 * 
 * <p>Any desired methods will require implementation logic in the child class, but unwanted methods can be instantiated without logic.
 * {@code SearchListenerAdapter} instantiates all of the methods as an abstract class and does not require child classes to instantiate all methods.
 * If the child class is not another {@code Listener} type interface, then {@code SearchListenerAdapter} would be a more appropriate parent class.
 * 
 *  <p>This interface is to be used alongside classes that extend {@code Search} class functionality. {@code SearchListener} is capable of gauging 
 *  {@code Search} attributes through the implemented methods and can receive information such as depth, configured properties, and other important {@code Search}
 *  properties.
 */

public interface SearchListener extends JPFListener {

	/**
	 * Notified when the state in a {@code Search} child class is advanced forward 
	 * one state in the search tree. The {@code Search} object that is passed as 
	 * a parameter contains attributes describing the next state in the search 
	 * loop as well as attributes describing the specifications of the search loop 
	 * itself  (e.g. current depth, depth limit, current error, etc.)
	 * 
	 * <p>{@code stateAdvanced(Search search)} will be notified and called before any potential 
	 * property violations (in {@code propertyViolated(Search search)}, thus the currentError 
	 * will already be set and may not reflect upcoming property violations.
	 * 
	 * @param search a {@code Search} object that denotes current attributes in the next state
	 */
	void stateAdvanced (Search search);

	/**
	 * Notified when the search loop has fully explored the current state and is ready to move
	 * to a new state in the search tree. The {@code Search} object that is passed as a parameter
	 * contains attributes describing the current state in the search loop as well as attributes
	 * describing the specifications of the search loop itself (e.g. current depth, depth limit, 
	 * current error, etc.)
	 * 
	 * @param search a {@code Search} object that denotes current attributes in the current state
	 */
	void stateProcessed (Search search);

	/**
	 * Notified when the state in a {@code Search} child class is moved backwards 
	 * one state in the search tree. The {@code Search} object that is passed as 
	 * a parameter contains attributes describing the next state (which is also
	 * the previous state) in the search tree attributes as well as attributes
	 * describing the specifications of the search loop itself  (e.g. current depth, 
	 * depth limit, current error, etc.)
	 * 
	 * @param search a {@code Search} object that denotes current attributes 
	 * of the next (previous) state
	 */
	void stateBacktracked (Search search);

	/**
	 * Notified when a state is removed from the search tree and will not be appearing
	 * in the future. The {@code Search} object passed through contains details 
	 * that describe the state and the current properties of the search loop
	 * (e.g. current depth, depth limit, current error, etc.)
	 * 
	 * @param search a {@code Search} object that denotes current attributes of the purged state
	 */
	void statePurged (Search search);

	/**
	 * Notified when an explored state from the search state is stored to be accessed later. The
	 * {@code Search} object passed through contains details that describe the state and current 
	 * properties of the search loop (e.g. current depth, depth limit, current error, etc.)
	 * 
	 * <p>The stored state may be restored later through {@code stateRestored(Search search)}
	 * 
	 * @param search a {@code Search} object that denotes current attributes of the stored state
	 */
	void stateStored (Search search);

	/**
	 * Notified when a previously stored search state is restored for use in the search algorithm.
	 * The {@code Search} object passed through contains details that describe the restored state
	 * and current properties of the search loop (e.g. current depth, depth limit, current
	 * error, etc.)
	 * 
	 * <p>The restored state is guaranteed to have been visited and explored in the past. 
	 * 
	 * <p>The restored state may be from a separate path than the previous state.
	 * 
	 * @param search a {@code Search} object that denotes current attributes of the restored state
	 */
	void stateRestored (Search search);

	/**
	 * Notified when a probe request occurs (e.g. from a periodical timer). The {@code Search} object
	 * passed through contains details that describe the probe request, as well as the properties of 
	 * the search loop (e.g. current depth, depth limit, current error, etc.)
	 * 
	 * While probe requests in {@code Search} may be implemented and called asynchronously, the notification
	 * sent to {@code searchProbed(Search search)} will always be from the main JPF loop, and thus be synchronously
	 * called (i.e. after instruction execution).
	 * 
	 * @param search a {@code Search} object that denotes current attributes of the probe request 
	 */
	void searchProbed (Search search);

	/**
	 * Notified when JPF encounters a property violation of the application during the search loop. The 
	 * {@code Search} object passed through contains details describing the properties of the search loop
	 * (e.g. current depth, depth limit, current error, etc.)
	 * 
	 * <p>Notification of {@code stateAdvanced(Search search)} will always precede notifications of 
	 * {@code propertyViolated(Search search)}
	 * 
	 * <p>The JPF search loop will notify {@code SearchListener} of the property violation before resetting the violation
	 * 
	 * <p>Properties refers to jpf.Property and not java.util.Property
	 * 
	 * @param search a {@code Search} object that denotes current attributes at the time of the property violation
	 */
	void propertyViolated (Search search);

	/**
	 * Notified when a search is started and the search loop is entered. The {@code Search} object passed through
	 * contains details describing the properties of the search loop (e.g. current depth, depth limit, current
	 * error, etc.)
	 * 
	 * <p>{@code searchStarted(Search search)} will always be called before the first {@code Search.forward()} call.
	 * 
	 * @param search a {@code Search} object that denotes current attributes of search loop
	 */
	void searchStarted (Search search);

	/**
	 * Notified when a constraint is reached during the search loop. The {@code Search} object passed through contains details describing 
	 * the properties of the search loop at the time of notification (e.g. current depth, depth limit, current error, etc.)
	 * 
	 * <p>The constraint being reached may have also been turned into a property but is usually an attribute of the search, not the application.
	 * 
	 * <p>Examples of constraints are depth limit or the amount of memory that the search loop is allowed to use being exceeded. These general constraints 
	 * are usually specified in a properties file.
	 * 
	 * @param search a {@code Search} object that denotes current attributes of search loop at the time of the constraint being hit
	 */
	void searchConstraintHit (Search search);

	/**
	 * Notified when a search ends and the search loop is exited. The {@code Search} object passed through
	 * contains details describing the properties of the search loop and final state (e.g. current depth, 
	 * depth limit, current error, etc.)
	 * 
	 * <p>If the search was finished prematurely due to some unexpected reason, a preceding error may antecede
	 * the {@code searchFinished(Search search)} call.
	 * 
	 * @param search a {@code Search} object that denotes attributes of finalized search loop
	 */
	void searchFinished (Search search);
}

