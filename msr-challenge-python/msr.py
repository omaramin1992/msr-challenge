# importing libraries
from pprint import pprint
import json

import pandas as pd
import matplotlib.pyplot as plt
import numpy as np


# TODO: Commands overall graph with users combined
# TODO: for each of high level commands we explore users (graph)
# TODO: breaking down Navigation, Auto completion
# TODO: Comparing the importance of tests between different users


def data_parser(text, dic):
    for i, j in dic.items():
        text = text.replace(i, j)
        # print(text)
    return '"' + text


# reading data from file
# input_file = open('output.txt', 'r')
# output_file = open('output.json', 'w')
# my_text = input_file.readlines()
reps = {'=': '": ', '{': '{"', ', ': ', "'}
# output_file.writelines('{')
# for line in my_text:
#     output_file.writelines(data_parser(line, reps))
# output_file.writelines('}')

with open('output.json', 'r') as data_file:
    data = json.load(data_file)
# pprint(data)
commands = []

total_count = 0

unknown_commands_total = 0
unknown_commands_percentage = 0
unknown_command_users_count = [0, 0, 0, 0, 0, 0]
unknown_commands = ['unknown']

build_commands_total = 0
build_commands_percentage = 0
build_command_users_count = [0, 0, 0, 0, 0, 0]
build_commands = ['BatchBuild', 'BatchClean', 'BatchDeploy', 'BatchRebuildAll',
                  'ProjectBuild', 'ProjectClean', 'ProjectDeploy', 'ProjectRebuildAll',
                  'SolutionBuild', 'SolutionClean', 'SolutionDeploy', 'SolutionRebuildAll']

debug_commands_total = 0
debug_commands_percentage = 0
debug_command_users_count = [0, 0, 0, 0, 0, 0]
debug_commands = ['ExecActAttachProgram', 'ExecActBreakpoint', 'ExecActEndProgram', 'ExecActExceptionNotHandled',
                  'ExceptionActExceptionNotHandled', 'ExecActExceptionThrown', 'ExceptionActExceptionThrown',
                  'ExecActGo', 'ExecActLaunchProgram', 'ExecActNone', 'ExecActStep', 'ExecActStopDebugging',
                  'ExecActUserBreak']

completion_commands_total = 0
completion_commands_percentage = 0
completion_command_users_count = [0, 0, 0, 0, 0, 0]
completion_commands = ['CompletionApplied', 'CompletionCancelled', 'CompletionFiltered', 'CompletionUnkown']

documents_commands_total = 0
documents_commands_percentage = 0
documents_command_users_count = [0, 0, 0, 0, 0, 0]
documents_commands = ['DocumentOpened', 'DocumentSaved', 'DocumentClosed']

find_commands_total = 0
find_commands_percentage = 0
find_command_users_count = [0, 0, 0, 0, 0, 0]
find_commands = ['FindCompleted', 'FindCancelled']

solution_commands_total = 0
solution_commands_percentage = 0
solution_command_users_count = [0, 0, 0, 0, 0, 0]
solution_commands = ['SolutionOpened', 'SolutionRenamed', 'SolutionClosed', 'SolutionItemAdded', 'SolutionItemRenamed',
                     'SolutionItemRemoved',
                     'SolutionProjectAdded', 'SolutionProjectRenamed', 'SolutionProjectRemoved',
                     'SolutionProjectItemAdded', 'SolutionProjectItemRenamed', 'SolutionProjectItemRemoved']

window_commands_total = 0
window_commands_percentage = 0
window_command_users_count = [0, 0, 0, 0, 0, 0]
window_commands = ['WindowCreated', 'WindowActivated', 'WindowMoved', 'WindowClosed', 'WindowDeactivated']

vc_commands_total = 0
vc_commands_percentage = 0
vc_command_users_count = [0, 0, 0, 0, 0, 0]
version_control_commands = ['VersionControlUnknown', 'VersionControlBranch', 'VersionControlCheckout',
                            'VersionControlClone', 'VersionControlCommit',
                            'VersionControlCommitAmend', 'VersionControlCommitInitial', 'VersionControlMerge',
                            'VersionControlPull',
                            'VersionControlRebase', 'VersionControlRebaseFinished', 'VersionControlReset']

nav_commands_total = 0
nav_commands_percentage = 0
nav_command_users_count = [0, 0, 0, 0, 0, 0]
navigation_commands = ['NavigationUnknown', 'NavigationCtrlClick', 'NavigationClick', 'NavigationKeyboard']

test_commands_total = 0
test_commands_percentage = 0
test_command_users_count = [0, 0, 0, 0, 0, 0]
test_commands = ['TestRunCompleted', 'TestRunAborted']

nonzero_commands = []

for command in data:
    commands.append(command)
for command in data:
    if command == 'unknown':
        print()
    else:
        nonzero_commands.append(command)
print(len(commands))
print(commands)

user_types = []
for user in data[commands[1]]:
    user_types.append(user)
print(len(user_types))
print(user_types)

for user in user_types:
    print(user)
# commands_per_user = [0, 0, 0, 0, 0, 0]
print()
total_count_list = []
total_count_list_nonzero = []

# counting the total number of commands
for command in commands:
    print()
    total_count_per_command = 0
    print(command)
    for key, value in data[command].items():
        total_count_per_command += value
        total_count += value
        print("\t", key, value)
    print("\tTotal count: ", total_count_per_command)
    total_count_list.append(total_count_per_command)
    if 50 > total_count_per_command > 0:
        nonzero_commands.append(command)
        total_count_list_nonzero.append(total_count_per_command)

print()
print("Length of Total count list: ", len(total_count_list))
print(total_count_list)
print()
print("Length of nonzero commands list: ", len(nonzero_commands))
print(nonzero_commands)
print()
print("Length of Total count nonzero list: ", len(nonzero_commands))
print(total_count_list_nonzero)
print()
print("Total count of commands: ", total_count)
print()

# counting number of users in each category and calculating the percentages
for unknown_command in unknown_commands:
    for key, value in data[unknown_command].items():
        unknown_commands_total += value
        if key == 'Unknown':
            unknown_command_users_count[0] += value
        elif key == 'HobbyProgrammer':
            unknown_command_users_count[1] += value
        elif key == 'Student':
            unknown_command_users_count[2] += value
        elif key == 'ResearcherAcademic':
            unknown_command_users_count[3] += value
        elif key == 'ResearcherIndustry':
            unknown_command_users_count[4] += value
        elif key == 'SoftwareEngineer':
            unknown_command_users_count[5] += value
unknown_commands_percentage = (unknown_commands_total / total_count) * 100
print("Unknown Command Users count: ", unknown_command_users_count)
print("Unknown command total count: ", unknown_commands_total)
print("Unknown Command percentage: ", unknown_commands_percentage)
print()

for build_command in build_commands:
    for key, value in data[build_command].items():
        build_commands_total += value
        if key == 'Unknown':
            build_command_users_count[0] += value
        elif key == 'HobbyProgrammer':
            build_command_users_count[1] += value
        elif key == 'Student':
            build_command_users_count[2] += value
        elif key == 'ResearcherAcademic':
            build_command_users_count[3] += value
        elif key == 'ResearcherIndustry':
            build_command_users_count[4] += value
        elif key == 'SoftwareEngineer':
            build_command_users_count[5] += value
build_commands_percentage = (build_commands_total / total_count) * 100
print("Build Command Users count: ", build_command_users_count)
print("Build command total count: ", build_commands_total)
print("Build Command percentage: ", build_commands_percentage)
print()

for debug_command in debug_commands:
    for key, value in data[debug_command].items():
        debug_commands_total += value
        if key == 'Unknown':
            debug_command_users_count[0] += value
        elif key == 'HobbyProgrammer':
            debug_command_users_count[1] += value
        elif key == 'Student':
            debug_command_users_count[2] += value
        elif key == 'ResearcherAcademic':
            debug_command_users_count[3] += value
        elif key == 'ResearcherIndustry':
            debug_command_users_count[4] += value
        elif key == 'SoftwareEngineer':
            debug_command_users_count[5] += value
debug_commands_percentage = (debug_commands_total / total_count) * 100
print("Debug Command Users count: ", debug_command_users_count)
print("Debug command total count: ", debug_commands_total)
print("Debug Command percentage: ", debug_commands_percentage)
print()

for completion_command in completion_commands:
    for key, value in data[completion_command].items():
        completion_commands_total += value
        if key == 'Unknown':
            completion_command_users_count[0] += value
        elif key == 'HobbyProgrammer':
            completion_command_users_count[1] += value
        elif key == 'Student':
            completion_command_users_count[2] += value
        elif key == 'ResearcherAcademic':
            completion_command_users_count[3] += value
        elif key == 'ResearcherIndustry':
            completion_command_users_count[4] += value
        elif key == 'SoftwareEngineer':
            completion_command_users_count[5] += value
completion_commands_percentage = (completion_commands_total / total_count) * 100
print("Completion Command Users count: ", completion_command_users_count)
print("Completion command total count: ", completion_commands_total)
print("Completion Command percentage: ", completion_commands_percentage)
print()

for documents_command in documents_commands:
    for key, value in data[documents_command].items():
        documents_commands_total += value
        if key == 'Unknown':
            documents_command_users_count[0] += value
        elif key == 'HobbyProgrammer':
            documents_command_users_count[1] += value
        elif key == 'Student':
            documents_command_users_count[2] += value
        elif key == 'ResearcherAcademic':
            documents_command_users_count[3] += value
        elif key == 'ResearcherIndustry':
            documents_command_users_count[4] += value
        elif key == 'SoftwareEngineer':
            documents_command_users_count[5] += value
documents_commands_percentage = (documents_commands_total / total_count) * 100
print("Documents Command Users count: ", documents_command_users_count)
print("Documents command total count: ", documents_commands_total)
print("Documents Command percentage: ", documents_commands_percentage)
print()

for find_command in find_commands:
    for key, value in data[find_command].items():
        find_commands_total += value
        if key == 'Unknown':
            find_command_users_count[0] += value
        elif key == 'HobbyProgrammer':
            find_command_users_count[1] += value
        elif key == 'Student':
            find_command_users_count[2] += value
        elif key == 'ResearcherAcademic':
            find_command_users_count[3] += value
        elif key == 'ResearcherIndustry':
            find_command_users_count[4] += value
        elif key == 'SoftwareEngineer':
            find_command_users_count[5] += value
find_commands_percentage = (find_commands_total / total_count) * 100
print("Find Command Users count: ", find_command_users_count)
print("Find command total count: ", find_commands_total)
print("Find Command percentage: ", find_commands_percentage)
print()

for solution_command in solution_commands:
    for key, value in data[solution_command].items():
        solution_commands_total += value
        if key == 'Unknown':
            solution_command_users_count[0] += value
        elif key == 'HobbyProgrammer':
            solution_command_users_count[1] += value
        elif key == 'Student':
            solution_command_users_count[2] += value
        elif key == 'ResearcherAcademic':
            solution_command_users_count[3] += value
        elif key == 'ResearcherIndustry':
            solution_command_users_count[4] += value
        elif key == 'SoftwareEngineer':
            solution_command_users_count[5] += value
solution_commands_percentage = (solution_commands_total / total_count) * 100
print("Solution Command Users count: ", solution_command_users_count)
print("Solution command total count: ", solution_commands_total)
print("Solution Command percentage: ", solution_commands_percentage)
print()

for window_command in window_commands:
    for key, value in data[window_command].items():
        window_commands_total += value
        if key == 'Unknown':
            window_command_users_count[0] += value
        elif key == 'HobbyProgrammer':
            window_command_users_count[1] += value
        elif key == 'Student':
            window_command_users_count[2] += value
        elif key == 'ResearcherAcademic':
            window_command_users_count[3] += value
        elif key == 'ResearcherIndustry':
            window_command_users_count[4] += value
        elif key == 'SoftwareEngineer':
            window_command_users_count[5] += value
window_commands_percentage = (window_commands_total / total_count) * 100
print("Window Command Users count: ", window_command_users_count)
print("Window command total count: ", window_commands_total)
print("Window Command percentage: ", window_commands_percentage)
print()

for vc_command in version_control_commands:
    for key, value in data[vc_command].items():
        vc_commands_total += value
        if key == 'Unknown':
            vc_command_users_count[0] += value
        elif key == 'HobbyProgrammer':
            vc_command_users_count[1] += value
        elif key == 'Student':
            vc_command_users_count[2] += value
        elif key == 'ResearcherAcademic':
            vc_command_users_count[3] += value
        elif key == 'ResearcherIndustry':
            vc_command_users_count[4] += value
        elif key == 'SoftwareEngineer':
            vc_command_users_count[5] += value
vc_commands_percentage = (vc_commands_total / total_count) * 100
print("VC Command Users count: ", vc_command_users_count)
print("VC command total count: ", vc_commands_total)
print("VC Command percentage: ", vc_commands_percentage)
print()

for nav_command in navigation_commands:
    for key, value in data[nav_command].items():
        nav_commands_total += value
        if key == 'Unknown':
            nav_command_users_count[0] += value
        elif key == 'HobbyProgrammer':
            nav_command_users_count[1] += value
        elif key == 'Student':
            nav_command_users_count[2] += value
        elif key == 'ResearcherAcademic':
            nav_command_users_count[3] += value
        elif key == 'ResearcherIndustry':
            nav_command_users_count[4] += value
        elif key == 'SoftwareEngineer':
            nav_command_users_count[5] += value
nav_commands_percentage = (nav_commands_total / total_count) * 100
print("Navigation Command Users count: ", nav_command_users_count)
print("Navigation command total count: ", nav_commands_total)
print("Navigation Command percentage: ", nav_commands_percentage)
print()

for test_command in test_commands:
    for key, value in data[test_command].items():
        test_commands_total += value
        if key == 'Unknown':
            test_command_users_count[0] += value
        elif key == 'HobbyProgrammer':
            test_command_users_count[1] += value
        elif key == 'Student':
            test_command_users_count[2] += value
        elif key == 'ResearcherAcademic':
            test_command_users_count[3] += value
        elif key == 'ResearcherIndustry':
            test_command_users_count[4] += value
        elif key == 'SoftwareEngineer':
            test_command_users_count[5] += value
test_commands_percentage = (test_commands_total / total_count) * 100
print("Test Command Users count: ", test_command_users_count)
print("Test command total count: ", test_commands_total)
print("Test Command percentage: ", test_commands_percentage)
print()

percentages = [unknown_commands_percentage, build_commands_percentage, debug_commands_percentage,
               completion_commands_percentage, documents_commands_percentage, find_commands_percentage,
               solution_commands_percentage, window_commands_percentage, vc_commands_percentage,
               nav_commands_percentage, test_commands_percentage]

categories = ['Build', 'Debug', 'Completion', 'Documents', 'Find', 'Solution', 'Window', 'VC', 'Nav', 'Test']
categories_total_counts = [build_commands_total, debug_commands_total,
                           completion_commands_total, documents_commands_total, find_commands_total,
                           solution_commands_total, window_commands_total, vc_commands_total,
                           nav_commands_total, test_commands_total]

total_percentage = 0
for percentage in percentages:
    total_percentage += percentage
    new_percentage = round(percentage, 2)
    print(new_percentage)
print(total_percentage)
print(categories_total_counts)
data_file.close()

# create data frame
raw_data = {'command': categories,
            'categories_total_count': categories_total_counts}
df = pd.DataFrame(raw_data, columns=['command', 'categories_total_count'])
df

# Setting the positions and width for the bars
pos = list(range(len(df['categories_total_count'])))
width = 0.5

# Plotting the bars
fig, ax = plt.subplots(figsize=(10, 5))

# Create a bar with total_count data,
# in position pos,
plt.bar([p + 1.5 * width for p in pos],
        # using df['pre_score'] data,
        df['categories_total_count'],
        # of width
        width,
        # with alpha 0.5
        alpha=0.5,
        # with color
        color='#EE3224',
        # with label the first value in commands
        label=categories,
        align='center')

# Set the y axis label
ax.set_ylabel('Counts')

# Set the chart's title
ax.set_title('Total Counts of Commands per Category')

# Set the position of the x ticks
ax.set_xticks([p + 1.5 * width for p in pos])

# Set the labels for the x ticks
ax.set_xticklabels(df['command'])
plt.setp(ax.get_xticklabels(), rotation=20, horizontalalignment='right')

# Setting the x-axis and y-axis limits
plt.xlim(min(pos) - width, max(pos) + width * 4)
plt.ylim([0, max(df['categories_total_count'])])

# Adding the legend and showing the plot
plt.legend(['Total Count'], loc='upper left')
plt.grid()
plt.show()
