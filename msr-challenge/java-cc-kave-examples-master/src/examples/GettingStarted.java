/**
 * Copyright 2016 University of Zurich
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


import java.io.File;
import java.time.ZonedDateTime;
import java.util.Set;

//I don't know how many events are in this DB... There's probably an upper-bound less than long.max_value...
//In any case, we can use these Bigintegers to make sure we don't overflow, or we can use math.addExact to
//increase our event counts. It would throw an exception on overflow, allowing us to continue with a new long counter.
import java.math.BigInteger;
import java.nio.file.Paths;
//This might be the best container for our counters, since a lot of the things are enummed types.
//Can use Collections.synchronizedMap(new EnumMap<EnumKey, V>(...)); if we need concurrency
import java.util.EnumMap;

import cc.kave.commons.model.events.CommandEvent;
//There's also "Education"...
//import cc.kave.commons.model.events.userprofiles.Positions;
import cc.kave.commons.model.events.IDEEvent;
import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.visualstudio.BuildEvent;
import cc.kave.commons.model.events.visualstudio.DebuggerEvent;
import cc.kave.commons.model.ssts.ISST;
import cc.kave.commons.utils.io.IReadingArchive;
import cc.kave.commons.utils.io.ReadingArchive;

import cc.kave.commons.model.events.*;

import java.util.TreeSet;

import java.nio.file.Files;

/**
 * Simple example that shows how the interaction dataset can be opened, all
 * users identified, and all contained events deserialized.
 */
public class GettingStarted {

	private String eventsDir;
	
	private static TreeSet<String> commandIDSet = new TreeSet<String>();
	private static TreeSet<String> buildScopeSet = new TreeSet<String>();
	private static TreeSet<String> buildActionSet = new TreeSet<String>();

	private static TreeSet<String> dbgReasonSet = new TreeSet<String>();
	private static TreeSet<String> dbgActionSet = new TreeSet<String>();
	
	public GettingStarted(String eventsDir) {
		this.eventsDir = eventsDir;
	}

	public void run() {

		System.out.printf("looking (recursively) for events in folder %s\n", new File(eventsDir).getAbsolutePath());

		/*
		 * Each .zip that is contained in the eventsDir represents all events that we
		 * have collected for a specific user, the folder represents the first day when
		 * the user uploaded data.
		 */
		Set<String> userZips = IoHelper.findAllZips(eventsDir);

		for (String userZip : userZips) {
			System.out.printf("\n#### processing user zip: %s #####\n", userZip);
			processUserZip(userZip);
		}
		
		try {
			Files.write(Paths.get("commandIDS.txt"), commandIDSet);
			Files.write(Paths.get("buildScopes.txt"), buildScopeSet);
			Files.write(Paths.get("buildActions.txt"), buildActionSet);
			Files.write(Paths.get("dbgReasons.txt"), dbgReasonSet);
			Files.write(Paths.get("dbgActions.txt"), dbgActionSet);		
		} catch (Exception e) {
			System.err.println("Error trying to output the sets to files.\nException " + e);
		}
	}

	private void processUserZip(String userZip) {
//		int numProcessedEvents = 0;
		// open the .zip file ...
		try (IReadingArchive ra = new ReadingArchive(new File(eventsDir, userZip))) {
			// ... and iterate over content.
			// the iteration will stop after 200 events to speed things up.
//			while (ra.hasNext() && (numProcessedEvents++ < 200)) {
//			int i = 0;
			while (ra.hasNext()) {
				/*
				 * within the userZip, each stored event is contained as a single file that
				 * contains the Json representation of a subclass of IDEEvent.
				 */
				IDEEvent e = ra.getNext(IDEEvent.class);
				/**NOTE
				 *  "Not all event bindings are very stable already, reading the JSON helps debugging possible bugs in the bindings"
				 *  We may need to add the extra steps of reading plain and processing JSON, as in EventExamples.java -> readPlainEvents()
				 */
				// the events can then be processed individually
				processEvent(e);
//				System.out.println(++i);
			}
			ra.close();
		} catch (Exception e) {
			System.err.println("Couldn't process userzip " + userZip + "\nException " + e);
		}
	}

	/*
	 * if you review the type hierarchy of IDEEvent, you will realize that several
	 * subclasses exist that provide access to context information that is specific
	 * to the event type.
	 * 
	 * To access the context, you should check for the runtime type of the event and
	 * cast it accordingly.
	 * 
	 * As soon as I have some more time, I will implement the visitor pattern to get
	 * rid of the casting. For now, this is recommended way to access the contents.
	 */
	private void processEvent(IDEEvent e) {

		/**
		 * We just need to count the distinct commands and keep track of the type of user, e.g. student.
		 * The best thing may be a dynamic 3-D array of some kind of list type (a list of lists of lists of long/BigInt).
		 * Maybe maps.
		 * Actually, the hierarchy of this thing (the dimension per topic) kind of depends on the event.
		 * Some events will have more than one level.
		 * The first index will be associated with a command we want to count (probably via an enum).
		 * The second index will be associated with a user type, e.g. student. 
		 * This has the advantage of having our counts already separated, and easy to sum up for the absolute total.
		 * The third index will probably always be 0. 
		 * The idea is that if increasing our count would overflow the counter, 
		 * then we instead increase the counter in the __next__ element. 
		 */
//		if (e instanceof CommandEvent) {
//			process((CommandEvent) e);
//		} else if (e instanceof CompletionEvent) {
//			process((CompletionEvent) e);
//		} else {
//			/*
//			 * CommandEvent and Completion event are just two examples, please explore the
//			 * type hierarchy of IDEEvent to find other types and review their API to
//			 * understand what kind of context data is available.
//			 * 
//			 * We include this "fall back" case, to show which basic information is always
//			 * available.
//			 */
//			processBasic(e);
//		}
		if (e instanceof CommandEvent) {
			process((CommandEvent) e);
		} else if (e instanceof BuildEvent) {
			process((BuildEvent) e);
		} else if (e instanceof DebuggerEvent) {
			process((DebuggerEvent) e);
		}

	}

	private void process(CommandEvent ce) {
		if(null == ce.getCommandId()) {
			commandIDSet.add("null");
		} else {
			commandIDSet.add(ce.getCommandId());
		}
		//System.out.printf("found a CommandEvent (id: %s)\n", ce.getCommandId());
	}
	
	private void process(BuildEvent e) {
		if(null == e.Scope) {
			buildScopeSet.add("null");
		} else {
			buildScopeSet.add(e.Scope);
		}
		if (null == e.Action) {
			buildActionSet.add("null");
		} else {
			buildActionSet.add(e.Action);
		}
		//System.out.printf("found a CommandEvent (id: %s)\n", ce.getCommandId());
	}
	
	private void process(DebuggerEvent e) {
		if(null == e.Reason) {
			dbgReasonSet.add("null");
		} else {
			dbgReasonSet.add(e.Reason);
		}
		if(null == e.Action) {
			dbgActionSet.add("null");
		} else {
			dbgActionSet.add(e.Action);
		}
		//System.out.printf("found a CommandEvent (id: %s)\n", ce.getCommandId());
	}

	private void process(CompletionEvent e) {
		ISST snapshotOfEnclosingType = e.context.getSST();
		String enclosingTypeName = snapshotOfEnclosingType.getEnclosingType().getFullName();

		System.out.printf("found a CompletionEvent (was triggered in: %s)\n", enclosingTypeName);
	}

	private void processBasic(IDEEvent e) {
		String eventType = e.getClass().getSimpleName();
		ZonedDateTime triggerTime = e.getTriggeredAt();

		System.out.printf("unrecognized event: " + e + "\n found an %s that has been triggered at: %s)\n", eventType, triggerTime);
	}
}