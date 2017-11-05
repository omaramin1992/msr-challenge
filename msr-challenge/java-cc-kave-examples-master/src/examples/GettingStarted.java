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

import cc.kave.commons.model.events.*;

import cc.kave.commons.model.events.CommandEvent;
//There's also "Education"...
//import cc.kave.commons.model.events.userprofiles.Positions;
import cc.kave.commons.model.events.IDEEvent;
import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.completionevents.TerminationState;
import cc.kave.commons.model.events.testrunevents.TestRunEvent;
import cc.kave.commons.model.events.userprofiles.Positions;
import cc.kave.commons.model.events.userprofiles.UserProfileEvent;
import cc.kave.commons.model.events.versioncontrolevents.VersionControlAction;
import cc.kave.commons.model.events.versioncontrolevents.VersionControlActionType;
import cc.kave.commons.model.events.versioncontrolevents.VersionControlEvent;
import cc.kave.commons.model.events.visualstudio.BuildEvent;
import cc.kave.commons.model.events.visualstudio.DebuggerEvent;
import cc.kave.commons.model.events.visualstudio.DocumentAction;
import cc.kave.commons.model.events.visualstudio.DocumentEvent;
import cc.kave.commons.model.events.visualstudio.FindEvent;
import cc.kave.commons.model.events.visualstudio.SolutionAction;
import cc.kave.commons.model.events.visualstudio.SolutionEvent;
import cc.kave.commons.model.events.visualstudio.WindowAction;
import cc.kave.commons.model.events.visualstudio.WindowEvent;
import cc.kave.commons.model.ssts.ISST;
import cc.kave.commons.utils.io.IReadingArchive;
import cc.kave.commons.utils.io.ReadingArchive;

import java.util.TreeSet;

import java.nio.file.Files;

/**
 * Simple example that shows how the interaction dataset can be opened, all
 * users identified, and all contained events deserialized.
 */
public class GettingStarted {

	private String eventsDir;
/**	
	private static TreeSet<String> commandIDSet = new TreeSet<String>();
	private static TreeSet<String> buildScopeSet = new TreeSet<String>();
	private static TreeSet<String> buildActionSet = new TreeSet<String>();

	private static TreeSet<String> dbgReasonSet = new TreeSet<String>();
	private static TreeSet<String> dbgActionSet = new TreeSet<String>();
*/	
	//Assuming the execution action default are the dbg reasons strings, and all of the other weird exceptions are either
	//exception action or null... think we'll just ignore exception actions unless they're from the dbgreasons strings[]
	public static final String[] dbgActionStrings = {"dbgExceptionActionDefault", "dbgExecutionActionDefault"};
	//There are many dbg Reasons, I'm only putting the ones that looked like they could make sense...
	public static final String[] dbgReasonStrings = {"dbgEventReasonAttachProgram", "dbgEventReasonBreakpoint",
			"dbgEventReasonEndProgram", "dbgEventReasonExceptionNotHandled", "dbgEventReasonExceptionThrown",
			"dbgEventReasonGo", "dbgEventReasonLaunchProgram", "dbgEventReasonNone", "dbgEventReasonStep",
			"dbgEventReasonStopDebugging", "dbgEventReasonUserBreak"};
	
	/**Unfortunately, I think it will be best if we ignore the commandEvents. It's too "wild"!!!*/
	
	public static final String[] buildActionStrings = {"vsBuildActionBuild", "vsBuildActionClean", 
			"vsBuildActionDeploy", "vsBuildActionRebuildAll"};
	
	public static final String[] buildScopeStrings = {"0", "vsBuildScopeBatch", "vsBuildScopeProject", "vsBuildScopeSolution"};
	
	public static EnumMap<MSRCommandType, EnumMap<Positions, BigInteger>> outputTable = new EnumMap<MSRCommandType, EnumMap<Positions, BigInteger>>(MSRCommandType.class);
	
	public static EnumMap<MSRCommandType, EnumMap<Positions, BigInteger>> currentUserTable = new EnumMap<MSRCommandType, EnumMap<Positions, BigInteger>>(MSRCommandType.class);
	
	public static Positions currentUserPosition = Positions.Unknown;
	
	public GettingStarted(String eventsDir) {
		this.eventsDir = eventsDir;
	}

	public void run() {
		
		for(MSRCommandType c : MSRCommandType.values()) {
			EnumMap<Positions, BigInteger> temp = new EnumMap<Positions, BigInteger>(Positions.class);
			for(Positions p : Positions.values()) {
				temp.put(p, new BigInteger("0"));
			}
			outputTable.put(c, temp);
		}

		System.out.printf("looking (recursively) for events in folder %s\n", new File(eventsDir).getAbsolutePath());

		/*
		 * Each .zip that is contained in the eventsDir represents all events that we
		 * have collected for a specific user, the folder represents the first day when
		 * the user uploaded data.
		 */
		Set<String> userZips = IoHelper.findAllZips(eventsDir);

		for (String userZip : userZips) {
			currentUserPosition = Positions.Unknown;
			currentUserTable = new EnumMap<MSRCommandType, EnumMap<Positions, BigInteger>>(MSRCommandType.class);
			for(MSRCommandType c : MSRCommandType.values()) {
				EnumMap<Positions, BigInteger> temp = new EnumMap<Positions, BigInteger>(Positions.class);
				for(Positions p : Positions.values()) {
					temp.put(p, new BigInteger("0"));
				}
				currentUserTable.put(c, temp);
			}			
			System.out.printf("\n#### processing user zip: %s #####\n", userZip);
			processUserZip(userZip);
			copyCurrentUserTableToOutput();
		}
		/**
		try {
			Files.write(Paths.get("commandIDS-3.txt"), commandIDSet);
			Files.write(Paths.get("buildScopes-3.txt"), buildScopeSet);
			Files.write(Paths.get("buildActions-3.txt"), buildActionSet);
			Files.write(Paths.get("dbgReasons-3.txt"), dbgReasonSet);
			Files.write(Paths.get("dbgActions-3.txt"), dbgActionSet);		
		} catch (Exception e) {
			System.err.println("Error trying to output the sets to files.\nException " + e);
		}
		*/
		try {
			Files.write(Paths.get("output.txt"), outputTable.toString().getBytes());
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
			int i = 0;
			while (ra.hasNext()) {
				/*
				 * within the userZip, each stored event is contained as a single file that
				 * contains the Json representation of a subclass of IDEEvent.
				 */
				try {
					IDEEvent e = ra.getNext(IDEEvent.class);
					/**NOTE
					 *  "Not all event bindings are very stable already, reading the JSON helps debugging possible bugs in the bindings"
					 *  We may need to add the extra steps of reading plain and processing JSON, as in EventExamples.java -> readPlainEvents()
					 */
					// the events can then be processed individually
					processEvent(e);
					++i;
//					System.out.println(i);
				}
				catch (Exception e) {
					System.err.println("Couldn't process userzip " + userZip + " event " + i + "\nException " + e);					
				}
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
/*		if (e instanceof CommandEvent) {
			process((CommandEvent) e);
		} else*/ if (e instanceof BuildEvent) {
			process((BuildEvent) e);
		} else if (e instanceof DebuggerEvent) {
			process((DebuggerEvent) e);
		} else if (e instanceof UserProfileEvent) {
			process((UserProfileEvent) e);
		} else if (e instanceof CompletionEvent) {
			process((CompletionEvent) e);
		} else if (e instanceof DocumentEvent) {
			process((DocumentEvent) e);
		} else if (e instanceof FindEvent) {
			process((FindEvent) e);
		} else if (e instanceof SolutionEvent) {
			process((SolutionEvent) e);
		} else if (e instanceof WindowEvent) {
			process((WindowEvent) e);
		} else if (e instanceof VersionControlEvent) {
			process((VersionControlEvent) e);
		} else if (e instanceof NavigationEvent) {
			process((NavigationEvent) e);
		} else if (e instanceof TestRunEvent) {
			process((TestRunEvent) e);
		}

	}

	private void process(UserProfileEvent e) {
		if (null != e.Position) {
			if (e.Position != currentUserPosition) {
				Positions old = currentUserPosition;
				currentUserPosition = e.Position;
				remakeCurrentUserOutputMap(old);
			}
		}
	}
	
	/**
	 * Unfortunately, it is not the case that the first event we see in a user's directory is its profile.
	 * So, when we finally do see a profile, we need to copy everything we've done into the proper heading.
	 * WARNING: There are multiple profile events per user, but we assume they all have the same Position.
	 *          If the position changes, we lump everything into the new category.
	 * @param oldPos
	 */
	private void remakeCurrentUserOutputMap(Positions oldPos){
		
		for(MSRCommandType c : MSRCommandType.values()) {
			EnumMap<Positions, BigInteger> temp = new EnumMap<Positions, BigInteger>(Positions.class);
			for(Positions p : Positions.values()) {
				temp.put(p, new BigInteger("0"));
			}
			temp.put(currentUserPosition, currentUserTable.get(c).get(oldPos));
			currentUserTable.put(c, temp);
		}		
	}
	
	private void copyCurrentUserTableToOutput() {
		for(MSRCommandType c : MSRCommandType.values()) {
			for(Positions p : Positions.values()) {
				outputTable.get(c).get(p).add( currentUserTable.get(c).get(p) );
			}
		}		
	}
	
/*	private void process(CommandEvent ce) {
		if(null == ce.getCommandId()) {
//			commandIDSet.add("null");
		} else {
//			commandIDSet.add(ce.getCommandId());
		}
		//System.out.printf("found a CommandEvent (id: %s)\n", ce.getCommandId());
	}*/
	
	private void process(BuildEvent e) {
		
		MSRCommandType temp = MSRCommandType.unknown;
		
		if ((null != e.Scope) && (null != e.Action)) {
			//buildScopeStrings[0] is garbage
			if (e.Scope.equalsIgnoreCase(buildScopeStrings[1])) {
				//Batch
				if (e.Action.equalsIgnoreCase(buildActionStrings[0])) {
					//Build
					temp = MSRCommandType.BatchBuild;
				} else if (e.Action.equalsIgnoreCase(buildActionStrings[1])) {
					//Clean
					temp = MSRCommandType.BatchClean;
				} else if (e.Action.equalsIgnoreCase(buildActionStrings[2])) {
					//Deploy
					temp = MSRCommandType.BatchDeploy;
				} else if (e.Action.equalsIgnoreCase(buildActionStrings[3])) {
					//Deploy
					temp = MSRCommandType.BatchRebuildAll;
				}
			} else if (e.Scope.equalsIgnoreCase(buildScopeStrings[2])) {
				//Project
				if (e.Action.equalsIgnoreCase(buildActionStrings[0])) {
					//Build
					temp = MSRCommandType.ProjectBuild;
				} else if (e.Action.equalsIgnoreCase(buildActionStrings[1])) {
					//Clean
					temp = MSRCommandType.ProjectClean;
				} else if (e.Action.equalsIgnoreCase(buildActionStrings[2])) {
					//Deploy
					temp = MSRCommandType.ProjectDeploy;
				} else if (e.Action.equalsIgnoreCase(buildActionStrings[3])) {
					//Deploy
					temp = MSRCommandType.ProjectRebuildAll;
				}			
			} else if (e.Scope.equalsIgnoreCase(buildScopeStrings[3])) {
				//Solution
				if (e.Action.equalsIgnoreCase(buildActionStrings[0])) {
					//Build
					temp = MSRCommandType.SolutionBuild;
				} else if (e.Action.equalsIgnoreCase(buildActionStrings[1])) {
					//Clean
					temp = MSRCommandType.SolutionClean;
				} else if (e.Action.equalsIgnoreCase(buildActionStrings[2])) {
					//Deploy
					temp = MSRCommandType.SolutionDeploy;
				} else if (e.Action.equalsIgnoreCase(buildActionStrings[3])) {
					//Deploy
					temp = MSRCommandType.SolutionRebuildAll;
				}			
			}
		}
		currentUserTable.get(temp).get(currentUserPosition).add(new BigInteger("1"));
	}
	
	private void process(DebuggerEvent e) {
		MSRCommandType temp = MSRCommandType.unknown;
		
		if ((null != e.Action) && (null != e.Reason)) {		
			if (e.Action.equalsIgnoreCase(dbgActionStrings[0])) {
				if (e.Reason.equalsIgnoreCase(dbgReasonStrings[3])) {
					temp = MSRCommandType.ExceptionActExceptionNotHandled;
				} else if (e.Reason.equalsIgnoreCase(dbgReasonStrings[4])) {
					temp = MSRCommandType.ExceptionActExceptionThrown;
				}
			} else if (e.Action.equalsIgnoreCase(dbgActionStrings[1])) {
				if (e.Reason.equalsIgnoreCase(dbgReasonStrings[0])) {
					temp = MSRCommandType.ExecActAttachProgram;
				} else if (e.Reason.equalsIgnoreCase(dbgReasonStrings[1])) {
					temp = MSRCommandType.ExecActBreakpoint;
				} else if (e.Reason.equalsIgnoreCase(dbgReasonStrings[2])) {
					temp = MSRCommandType.ExecActExceptionNotHandled;
				} else if (e.Reason.equalsIgnoreCase(dbgReasonStrings[3])) {
					temp = MSRCommandType.ExecActEndProgram;
				} else if (e.Reason.equalsIgnoreCase(dbgReasonStrings[4])) {
					temp = MSRCommandType.ExecActExceptionThrown;
				} else if (e.Reason.equalsIgnoreCase(dbgReasonStrings[5])) {
					temp = MSRCommandType.ExecActGo;
				} else if (e.Reason.equalsIgnoreCase(dbgReasonStrings[6])) {
					temp = MSRCommandType.ExecActLaunchProgram;
				} else if (e.Reason.equalsIgnoreCase(dbgReasonStrings[7])) {
					temp = MSRCommandType.ExecActNone;
				} else if (e.Reason.equalsIgnoreCase(dbgReasonStrings[8])) {
					temp = MSRCommandType.ExecActStep;
				} else if (e.Reason.equalsIgnoreCase(dbgReasonStrings[9])) {
					temp = MSRCommandType.ExecActStopDebugging;
				} else if (e.Reason.equalsIgnoreCase(dbgReasonStrings[10])) {
					temp = MSRCommandType.ExecActUserBreak;
				}		
			} 
		}
		
		currentUserTable.get(temp).get(currentUserPosition).add(new BigInteger("1"));
	}

	private void process(CompletionEvent e) {
		MSRCommandType temp = MSRCommandType.unknown;

		if (null != e.terminatedState) {
			if (e.terminatedState == TerminationState.Applied) {
				temp = MSRCommandType.CompletionApplied;
			} else if (e.terminatedState == TerminationState.Cancelled) {
				temp = MSRCommandType.CompletionCancelled;
			} else if (e.terminatedState == TerminationState.Filtered) {
				temp = MSRCommandType.CompletionFiltered;
			} else if (e.terminatedState == TerminationState.Unknown) {
				temp = MSRCommandType.CompletionUnkown;
			}
		}
		currentUserTable.get(temp).get(currentUserPosition).add(new BigInteger("1"));
	}
	
	private void process(DocumentEvent e) {
		MSRCommandType temp = MSRCommandType.unknown;

		if (null != e.Action) {
			if (e.Action == DocumentAction.Opened) {
				temp = MSRCommandType.DocumentOpened;
			} else if (e.Action == DocumentAction.Saved) {
				temp = MSRCommandType.DocumentSaved;
			} else if (e.Action == DocumentAction.Closing) {
				temp = MSRCommandType.DocumentClosed;
			}
		}
		currentUserTable.get(temp).get(currentUserPosition).add(new BigInteger("1"));
	}	
	
	private void process(FindEvent e) {
		MSRCommandType temp = MSRCommandType.unknown;

		if (e.Cancelled) {
			temp = MSRCommandType.FindCancelled;
		} else {
			temp = MSRCommandType.FindCompleted;
		}
		
		currentUserTable.get(temp).get(currentUserPosition).add(new BigInteger("1"));
	}
	
	private void process(SolutionEvent e) {
		MSRCommandType temp = MSRCommandType.unknown;

		if (null != e.Action) {
			if (e.Action == SolutionAction.OpenSolution) {
				temp = MSRCommandType.SolutionOpened;
			} else if (e.Action == SolutionAction.RenameSolution) {
				temp = MSRCommandType.SolutionRenamed;
			} else if (e.Action == SolutionAction.CloseSolution) {
				temp = MSRCommandType.SolutionClosed;
			} else if (e.Action == SolutionAction.AddSolutionItem) {
				temp = MSRCommandType.SolutionItemAdded;
			} else if (e.Action == SolutionAction.RenameSolutionItem) {
				temp = MSRCommandType.SolutionItemRenamed;
			} else if (e.Action == SolutionAction.RemoveSolutionItem) {
				temp = MSRCommandType.SolutionItemRemoved;
			} else if (e.Action == SolutionAction.AddProject) {
				temp = MSRCommandType.SolutionProjectAdded;
			} else if (e.Action == SolutionAction.RenameProject) {
				temp = MSRCommandType.SolutionProjectRenamed;
			} else if (e.Action == SolutionAction.RemoveProject) {
				temp = MSRCommandType.SolutionProjectRemoved;
			} else if (e.Action == SolutionAction.AddProjectItem) {
				temp = MSRCommandType.SolutionProjectItemAdded;
			} else if (e.Action == SolutionAction.RenameProjectItem) {
				temp = MSRCommandType.SolutionProjectItemRenamed;
			} else if (e.Action == SolutionAction.RemoveProjectItem) {
				temp = MSRCommandType.SolutionProjectItemRemoved;
			}
		}
		currentUserTable.get(temp).get(currentUserPosition).add(new BigInteger("1"));
	}
	
	private void process(WindowEvent e) {
		MSRCommandType temp = MSRCommandType.unknown;

		if (null != e.Action) {
			if (e.Action == WindowAction.Create) {
				temp = MSRCommandType.WindowCreated;
			} else if (e.Action == WindowAction.Activate) {
				temp = MSRCommandType.WindowActivated;
			} else if (e.Action == WindowAction.Move) {
				temp = MSRCommandType.WindowMoved;
			} else if (e.Action == WindowAction.Close) {
				temp = MSRCommandType.WindowClosed;
			} else if (e.Action == WindowAction.Deactivate) {
				temp = MSRCommandType.WindowDeactivated;
			}
		}
		
		currentUserTable.get(temp).get(currentUserPosition).add(new BigInteger("1"));
	}
	
	private void process(VersionControlEvent e) {

		if (null != e.Actions) {
			for (VersionControlAction a : e.Actions) {
				MSRCommandType temp = MSRCommandType.unknown;
				
				if (null != a.ActionType) {
					if (a.ActionType == VersionControlActionType.Unknown) {
						temp = MSRCommandType.VersionControlUnknown;
					} else if (a.ActionType == VersionControlActionType.Branch) {
						temp = MSRCommandType.VersionControlBranch;
					} else if (a.ActionType == VersionControlActionType.Checkout) {
						temp = MSRCommandType.VersionControlCheckout;
					} else if (a.ActionType == VersionControlActionType.Clone) {
						temp = MSRCommandType.VersionControlClone;
					} else if (a.ActionType == VersionControlActionType.Commit) {
						temp = MSRCommandType.VersionControlCommit;
					} else if (a.ActionType == VersionControlActionType.CommitAmend) {
						temp = MSRCommandType.VersionControlCommitAmend;
					} else if (a.ActionType == VersionControlActionType.CommitInitial) {
						temp = MSRCommandType.VersionControlCommitInitial;
					} else if (a.ActionType == VersionControlActionType.Merge) {
						temp = MSRCommandType.VersionControlMerge;
					} else if (a.ActionType == VersionControlActionType.Pull) {
						temp = MSRCommandType.VersionControlPull;
					} else if (a.ActionType == VersionControlActionType.Rebase) {
						temp = MSRCommandType.VersionControlRebase;
					} else if (a.ActionType == VersionControlActionType.RebaseFinished) {
						temp = MSRCommandType.VersionControlRebaseFinished;
					} else if (a.ActionType == VersionControlActionType.Reset) {
						temp = MSRCommandType.VersionControlReset;
					}
				}
				
				currentUserTable.get(temp).get(currentUserPosition).add(new BigInteger("1"));			
			}
		}
		
	}	
	
	private void process(NavigationEvent e) {
		MSRCommandType temp = MSRCommandType.unknown;

		if (null != e.TypeOfNavigation) {
			if (e.TypeOfNavigation == NavigationType.Unknown) {
				temp = MSRCommandType.NavigationUnknown;
			} else if (e.TypeOfNavigation == NavigationType.CtrlClick) {
				temp = MSRCommandType.NavigationCtrlClick;
			} else if (e.TypeOfNavigation == NavigationType.Click) {
				temp = MSRCommandType.NavigationClick;
			} else if (e.TypeOfNavigation == NavigationType.Keyboard) {
				temp = MSRCommandType.NavigationKeyboard;
			}
		}
		
		currentUserTable.get(temp).get(currentUserPosition).add(new BigInteger("1"));
	}
	
	private void process(TestRunEvent e) {
		MSRCommandType temp = MSRCommandType.unknown;

		if (e.WasAborted) {
			temp = MSRCommandType.TestRunAborted;
		} else {
			temp = MSRCommandType.TestRunCompleted;
		}
		
		currentUserTable.get(temp).get(currentUserPosition).add(new BigInteger("1"));
	}	

/*	private void processBasic(IDEEvent e) {
		String eventType = e.getClass().getSimpleName();
		ZonedDateTime triggerTime = e.getTriggeredAt();

		System.out.printf("unrecognized event: " + e + "\n found an %s that has been triggered at: %s)\n", eventType, triggerTime);
	}*/
}