# IR-Homework2
Page Rank Project

# Things to change if using custom graph.txt file:
- On line 32 of the app.java file, you must change the location to that of your own file.
- On line 38 of the app.java file, you can change the THRESHOLD value to whatever you want.
  - This value determines when the Random Surfer computation halts. Each iteration, the difference for each element for the previous rank vector and new rank vector is summed. Once this sum is at or below the threshold value, the program will finish.
  - For example if the last rank vector is [0.25, 0.2, 0.3] and the new rank vector is [0.2, 0.15, 0.3] then the difference will be calculated as |0.25 - 0.2| + |0.2 - 0.15| + |0.3 - 0.3| = 0.1 and the program will continue onto the next iteration until the difference is at or below the set THRESHOLD value.
