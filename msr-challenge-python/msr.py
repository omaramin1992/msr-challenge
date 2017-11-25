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
for command in data:
    commands.append(command)
print(len(commands))
print(commands)

user_types = []
for user in data[commands[1]]:
    user_types.append(user)
print(len(user_types))
print(user_types)

# commands_per_user = [0, 0, 0, 0, 0, 0]
# for count in data[command[1]]:
#     print()

# message = f.read()
# print(message)
# f.close()

# create data frame
raw_data = {'first_name': ['Unknown', 'HobbyProgrammer', 'Student', 'ResAcademic', 'ResIndustry',
                           'SWEngineer'],
            'pre_score': [4, 24, 31, 2, 3, 5],
            'mid_score': [25, 94, 57, 62, 70, 6],
            'post_score': [5, 43, 23, 23, 51, 7]}
df = pd.DataFrame(raw_data, columns=['first_name', 'pre_score', 'mid_score', 'post_score'])
df

# Setting the positions and width for the bars
pos = list(range(len(df['pre_score'])))
width = 0.15

# Plotting the bars
fig, ax = plt.subplots(figsize=(10, 5))

# Create a bar with pre_score data,
# in position pos,
plt.bar(pos,
        # using df['pre_score'] data,
        df['pre_score'],
        # of width
        width,
        # with alpha 0.5
        alpha=0.5,
        # with color
        color='#EE3224',
        # with label the first value in first_name
        label=df['first_name'][0])

# Create a bar with mid_score data,
# in position pos + some width buffer,
plt.bar([p + width for p in pos],
        # using df['mid_score'] data,
        df['mid_score'],
        # of width
        width,
        # with alpha 0.5
        alpha=0.5,
        # with color
        color='#F78F1E',
        # with label the second value in first_name
        label=df['first_name'][1])

# Create a bar with post_score data,
# in position pos + some width buffer,
plt.bar([p + width * 2 for p in pos],
        # using df['post_score'] data,
        df['post_score'],
        # of width
        width,
        # with alpha 0.5
        alpha=0.5,
        # with color
        color='#FFC222',
        # with label the third value in first_name
        label=df['first_name'][2])

# Set the y axis label
ax.set_ylabel('Score')

# Set the chart's title
ax.set_title('Test Subject Scores')

# Set the position of the x ticks
ax.set_xticks([p + 1.5 * width for p in pos])

# Set the labels for the x ticks
ax.set_xticklabels(df['first_name'])

# Setting the x-axis and y-axis limits
plt.xlim(min(pos) - width, max(pos) + width * 4)
plt.ylim([0, max(df['pre_score'] + df['mid_score'] + df['post_score'])])

# Adding the legend and showing the plot
plt.legend(['Pre Score', 'Mid Score', 'Post Score'], loc='upper left')
plt.grid()
plt.show()
