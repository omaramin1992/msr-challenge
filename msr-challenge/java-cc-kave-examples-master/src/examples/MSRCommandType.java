
//NOTE I think we're going to ignore "CommandEvent"s, they aren't well-defined...
public enum MSRCommandType {
	unknown,
	
	//Build events. We should have commands like ProjectBuild, SolutionClean, etc, combining scope w action
	BatchBuild, BatchClean, BatchDeploy, BatchRebuildAll,
	ProjectBuild, ProjectClean, ProjectDeploy, ProjectRebuildAll,
	SolutionBuild, SolutionClean, SolutionDeploy, SolutionRebuildAll,
	
	//It's unclear how the debug events work, I'm going to guess
	ExecActAttachProgram, ExecActBreakpoint, ExecActEndProgram, ExecActExceptionNotHandled, ExceptionActExceptionNotHandled,
	ExecActExceptionThrown, ExceptionActExceptionThrown,
	ExecActGo, ExecActLaunchProgram, ExecActNone, ExecActStep, ExecActStopDebugging, ExecActUserBreak,
	
	CompletionApplied, CompletionCancelled, CompletionFiltered, CompletionUnkown, //termination state for completion events/intellisense
	DocumentOpened, DocumentSaved, DocumentClosed, //DocumentEvent actions
//	Edit //This may only be interesting when preceded by an applied completion... Then again, it may be useless in that case...
	FindCompleted, FindCancelled, //Find events have one property to tell us whether or not the search was cancelled
	//Looks like only RUNTIME IDEState events could be useful for us, though they don't seem to tell you if things were opened, rearranged, etc...
	//These IDEState events aren't descriptive enough to tell us what they're for... ignore them? Otherwise we need to keep deltas
	//between consecutive events to figure out what happened...
	SolutionOpened, SolutionRenamed, SolutionClosed, SolutionItemAdded, SolutionItemRenamed, SolutionItemRemoved, 
	SolutionProjectAdded, SolutionProjectRenamed, SolutionProjectRemoved, 
	SolutionProjectItemAdded, SolutionProjectItemRenamed, SolutionProjectItemRemoved, //From solution event, solution actions
	//WindowEvent, window actions, maybe only moved and closed are interesting... creation is invisible, activation seems to tie into navigations... 
	WindowCreated, WindowActivated, WindowMoved, WindowClosed, WindowDeactivated,
	//Version control events, vc action types
	//Maybe the commits can be merged, as well as rebase and rebasefinished... maybe merge merging w rebasing...
	VersionControlUnknown, VersionControlBranch, VersionControlCheckout, VersionControlClone, VersionControlCommit, 
	VersionControlCommitAmend, VersionControlCommitInitial, VersionControlMerge, VersionControlPull, 
	VersionControlRebase, VersionControlRebaseFinished, VersionControlReset,
	//Navigation type... 
	NavigationUnknown, NavigationCtrlClick, NavigationClick, NavigationKeyboard,
	//Don't think we care about system events like locking or sleeping...
	//For test run events, we probably only care that it was run or aborted...
	TestRunCompleted, TestRunAborted
	//I think we can ignore the remaining events
}
