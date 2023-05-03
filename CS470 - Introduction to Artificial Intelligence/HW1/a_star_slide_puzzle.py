# A General A* Function and its Application to Slide Puzzles
# CS 470/670 at UMass Boston

import numpy as np
from collections import deque
import bisect

example_1_start = np.array([[2, 8, 3],
                           [1, 6, 4],
                           [7, 0, 5]])

example_1_goal = np.array([[1, 2, 3],
                           [8, 0, 4],
                           [7, 6, 5]])

example_2_start = np.array([[ 2,  6,  4,  8],
                            [ 5, 11,  3, 12],
                            [ 7,  0,  1, 15],
                            [10,  9, 13, 14]])

example_2_goal = np.array([[ 1,  2,  3,  4],
                           [ 5,  6,  7,  8],
                           [ 9, 10, 11, 12],
                           [13, 14, 15,  0]])

# For a given current state, move, and goal, compute the new state and its h'-score and return them as a pair. 
def make_node(state, row_from, col_from, row_to, col_to, goal):
    # Create the new state that results from playing the current move. 
    (height, width) = state.shape
    new_state = np.copy(state)
    new_state[row_to, col_to] = new_state[row_from, col_from]
    new_state[row_from, col_from] = 0
    
    # Count the mismatched numbers and use this value as the h'-score (estimated number of moves needed to reach the goal).
    mismatch_count = 0
    for i in range(height):
        for j in range(width):
            if new_state[i, j] > 0 and new_state[i, j] != goal[i, j]:
                mismatch_count += 1
   
    return (new_state, mismatch_count)

# For given current state and goal state, create all states that can be reached from the current state
# (i.e., expand the current node in the search tree) and return a list that contains a pair (state, h'-score)
# for each of these states.   
def slide_expand(state, goal):
    node_list = []
    (height, width) = state.shape
    (empty_row, empty_col) = np.argwhere(state == 0)[0]     # Find the position of the empty tile
    
    # Based on the position of the empty tile, find all possible moves and add a pair (new_state, h'-score)
    # for each of them.
    if empty_row > 0:
        node_list.append(make_node(state, empty_row - 1, empty_col, empty_row, empty_col, goal))
    if empty_row < height - 1:
        node_list.append(make_node(state, empty_row + 1, empty_col, empty_row, empty_col, goal))
    if empty_col > 0:
        node_list.append(make_node(state, empty_row, empty_col - 1, empty_row, empty_col, goal))
    if empty_col < width - 1:
        node_list.append(make_node(state, empty_row, empty_col + 1, empty_row, empty_col, goal))
    
    return node_list

# For a given current state, move, and goal, compute the new state and its h'-score and return them as a pair. 
def make_node_improved(state, row_from, col_from, row_to, col_to, goal):
    # Create the new state that results from playing the current move. 
    (height, width) = state.shape
    new_state = np.copy(state)
    new_state[row_to, col_to] = new_state[row_from, col_from]
    new_state[row_from, col_from] = 0
    
    # Count the mismatched numbers and use this value as the h'-score (estimated number of moves needed to reach the goal).
    # mismatch_count = 0
    # for i in range(height):
    #     for j in range(width):
    #         if new_state[i, j] > 0 and new_state[i, j] != goal[i, j]:
    #             mismatch_count += 1
    
    # Calculate the minimum distance from each number's position to where it should be in the goal and use the sum of these 
    # values as the h-score. This is done by iterating  over 'state', finding the appropriate location for each number in 
    # 'state' in 'goal' by iterating over 'goal', and summing the absolute values of the distances between the actual row 
    # and column and the target row and column.
    def calc_dist(state, goal, i, j, height, width):
        k = 0
        while k < height:
            l = 0
            while l < width:
                if goal[k][l] == state[i][j]:
                    return abs(k-i) + abs(l-j)
                else:
                    l += 1
            k += 1
    
    manhattan_distance = 0
    
    i = 0
    while i < height:
        j = 0
        while j < width:
            manhattan_distance += calc_dist(new_state, goal, i, j, height, width)
            j += 1
        i += 1
    
    return (new_state, manhattan_distance)

# For given current state and goal state, create all states that can be reached from the current state
# (i.e., expand the current node in the search tree) and return a list that contains a pair (state, h'-score)
# for each of these states.   
def slide_expand_improved(state, goal):
    node_list = []
    (height, width) = state.shape
    (empty_row, empty_col) = np.argwhere(state == 0)[0]     # Find the position of the empty tile
    
    # Based on the position of the empty tile, find all possible moves and add a pair (new_state, h'-score)
    # for each of them.
    if empty_row > 0:
        node_list.append(make_node_improved(state, empty_row - 1, empty_col, empty_row, empty_col, goal))
    if empty_row < height - 1:
        node_list.append(make_node_improved(state, empty_row + 1, empty_col, empty_row, empty_col, goal))
    if empty_col > 0:
        node_list.append(make_node_improved(state, empty_row, empty_col - 1, empty_row, empty_col, goal))
    if empty_col < width - 1:
        node_list.append(make_node_improved(state, empty_row, empty_col + 1, empty_row, empty_col, goal))
    
    return node_list
  
# Return either the solution as a list of states from start to goal or [] if there is no solution.               
def a_star(start, goal, expand):
    # numpy arrays cannot natively be used as the key to a dictionary, as they both use 
    # the same syntax to refer to indices and python cannot tell which is desired.
    # This function just turns an array representation of a board into a unique
    # string, to use as the key instead. Also used for comparing boards.
    def convert(nparray):
        rep = ""
        (height, width) = nparray.shape
        for row in range(height):
            for col in range(width):
                rep += str(nparray[row][col]) + "."
        return rep[:-1]
    
    # The tree of possible moves is represented internally as a dictionary, where each key is a board
    # and its value is 2-tuple: the g-value for that node (how many moves are required to 
    # get to that node) and the list of moves required to get from start to this node.
    gr = {convert(start):(0, [start])}
    cgoal = convert(goal)
    
    # The open list, boards that have not yet been processed, is a 2-tuple: the boards themselves, 
    # and the f-value for those states.
    open = [(start, np.Infinity)]
    
    # The closed list is the list of boards that have already been processed, in converted form.
    closed = []
    solution = []
    max_len = 0
    # While there are unexplored nodes:
    while len(open) != 0:
        
        
        # Setup code to process the next node
        node = open[0][0]
        cnode = convert(node)
        g = gr[cnode][0] + 1
        path = gr[cnode][1]
        closed.append(cnode)
        open = open[1:]
        
        # If the current node is the goal node, set the solution and break.
        if cnode == cgoal:
            solution = path
            break
        
        # Otherwise:
        # - Expand the next node to derive its children
        # - Add unprocessed children to the tree and open list, calculating f values
        # - Sort open list by calculated f values
        else:
            children = expand(node, goal)
            for board in children:
                cboard = convert(board[0])
                if cboard not in closed:
                    gr[cboard] = [g, path + [board[0]]]
                    open.append((board[0], board[1] + g))
                    if len(open) > max_len:
                        max_len = len(open)
            open.sort(key=lambda state: state[1])
    print(max_len)
    return solution
    
# Find and print a solution for a given slide puzzle, i.e., the states we need to go through 
# in order to get from the start state to the goal state.
def slide_puzzle_solver(start, goal):
    solution = a_star(start, goal, slide_expand_improved)
    if len(solution) == 0:
        print('This puzzle has no solution. Please stop trying to fool me.')
        return
        
    (height, width) = start.shape
    if height * width >= 10:            # If numbers can have two digits, more space is needed for printing
        digits = 2
    else:
        digits = 1
    horizLine = ('+' + '-' * (digits + 2)) * width + '+'
    for step in range(len(solution)):
        state = solution[step]
        for row in range(height):
            print(horizLine)
            for col in range(width):
                print('| %*d'%(digits, state[row, col]), end=' ')
            print('|')
        print(horizLine)
        if step < len(solution) - 1:
            space = ' ' * (width * (digits + 3) // 2)
            print(space + '|')
            print(space + 'V')

slide_puzzle_solver(example_1_start, example_1_goal)       # Find solution to example_1
