from time import time

class nQueens:

    def __init__(self, n):
        self.n = n
        self.range_n = range(n) # prevents creating duplicate range objects
        self.backtrack_counter = 0
        
        # index corresponds to column, value to row: -1 := unassigned
        self.assignment = [-1] * n 
        
        # available rows for each column: True := available
        self.domain = [[True] * n for _ in self.range_n]
        
        # Domain has been modified so that it is no longer mutated as part of
        # the algorithms, replacing mutation of the lists with checks to the 
        # stored boolean values. Also, the additional data structure for 
        # unassigned columns has been removed: that information is already part 
        # of assignment. Both changes are because mutating lists is very slow;
        # this makes solving significantly faster.
        
        self.solve_and_print()

    def is_set(self, var):
        """
        Check if the passed variable has already been assigned
        :param: integer, representing column
        :return: true if that column is already assigned, false otherwise
        """
        
        return self.assignment[var] != -1
    
    def is_arc_consistent(self, pair1, pair2):
        """
        checks if the two passed variable/value pairs are consistent
        :return: true if the passed arc is consistent, false otherwise
        """
        
        return pair1[1] != pair2[1] and \
                abs(pair1[0] - pair2[0]) != abs(pair1[1] - pair2[1])
    
    def is_consistent(self, col, val):
        """
        Check if assigning val to col would be consistent with other assigments
        :param col, val: integers, indicating position to check
        :return: true if this assignment is fully consistent, false otherwise
        """
        
        # Function modified to use the is_arc_consistent function for 
        # calculations, to make the code more clear.
        for i in self.range_n:
            if self.is_set(i) and not self.is_arc_consistent(\
                    (i, self.assignment[i]),(col, val)):
                return False
        return True

    def select_next_variable(self):
        """
        Finds and returns the variable with the fewest legal values
        :return: column with the fewest legal values
        """
        
        mrv = [self.n for _ in self.range_n]
        for i in self.range_n:
            if not self.is_set(i):
                for j in self.range_n:
                    if not self.domain[i][j] or not self.is_consistent(i, j):
                        mrv[i] -= 1
        return mrv.index(min(mrv))
    
    def forward_checking(self, var):
        """
        Updates the domain of values and returns whether or not this state is
            solveable
        :param var: column that was just assigned
        :return: true if the current state can be solved, false otherwise
        """
        
        for i in self.range_n:
            if not self.is_set(i):
                for j in self.range_n:
                    self.domain[i][j] &= self.is_arc_consistent(\
                            (var, self.assignment[var]), (i, j))
                if sum(self.domain[i]) == 0:
                    return False
        return True

    def ac3(self, var):
        """
        Updates the domain of values and returns whether or not this state is
            solveable
        :param var: column that was just assigned
        :return: true if the current state can be solved, false otherwise
        """
        
        def revise(xi, xj):
            """
            Checks an arc for consistency, removing inconsistent domain values 
                from the first column
            :param xi, xj: columns to check
            :return: true if the domain was revised, false otherwise
            """
        
            revised = False
            for i in self.range_n:
                if self.domain[xi][i]:
                    
                    # Special, faster handling for the initial arcs
                    if self.is_set(xj):
                        consistent = self.is_arc_consistent(\
                                (xi, i), (xj, self.assignment[xj]))
                    
                    # Iterate through possible values, breaking if a consistent
                    # value is found.
                    else:
                        consistent = False
                        for j in self.range_n:
                            if self.domain[xj][j] and self.is_arc_consistent(\
                                    (xi, i), (xj, j)):
                                consistent = True
                                break
                    
                    # If a value was inconsistent, remove it and update revised      
                    if not consistent:
                        self.domain[xi][i] = False
                        revised = True
            return revised
        
        # Initial queue is all unassigned columns paired with the column
        # that was just assigned
        queue = [(x, var) for x in self.range_n if not self.is_set(x)]
        
        while queue:
            (xi, xj) = queue.pop(0)
            
            # If a column's domain was revised, check to see if it is now empty:
            # - if so return false
            # - if not, append to the queue all other unassigned columns paired
            #   with that column
            if revise(xi, xj):
                if sum(self.domain[xi]) == 0:
                    return False
                queue += [(x, xi) for x in self.range_n \
                        if not self.is_set(x) and x != xi and x != xj and (x, xi) not in queue]
        return True
    
    def backtrack(self):
        """
        Recursive backtracking function
        :return: a solution (final problem state) if there is one, otherwise []
        """
        
        self.backtrack_counter += 1

        # Because a valid assignment is an arrangement of the integers 0 to 
        # (n-1), completion can be verified by simply summing the current 
        # assignment and comparing that to the sum of 0 through (n-1)
        if sum(self.assignment) == self.n * (self.n - 1) // 2:
            return self.assignment

        # select the next unassigned column using the MRV heuristic and iterate
        var = self.select_next_variable()
        for val in self.range_n:
            if self.is_consistent(var, val):
                self.assignment[var] = val
                result = self.backtrack()
                if result:
                    return result
                self.assignment[var] = -1
        return []
        
    def backtrack_improved(self, inference):
        """
        Recursive backtracking function
        :param inference: an inference method such as forward chaining or ac3
        :return: a solution (final problem state) if there is one, otherwise []
        """

        self.backtrack_counter += 1

        # Because a valid assignment is an arrangement of the integers 0 to 
        # (n-1), completion can be verified by simply summing the current 
        # assignment and comparing that to the sum of 0 through (n-1)
        if sum(self.assignment) == self.n * (self.n - 1) // 2:
            return self.assignment

        # select the next unassigned column using the MRV heuristic
        var = self.select_next_variable()
    
        # Create a copy of the current domain to enable backtracking
        bt_domain = [x[:] for x in self.domain]
        
        # Iterate through values for var, inferring the domain after assignment
        for val in self.range_n:
            if self.domain[var][val]:
                self.assignment[var] = val
                if inference(var):
                    result = self.backtrack_improved(inference)
                    if result:
                        return result
                self.assignment[var] = -1
            
                # Reset domain to check next value
                for i in self.range_n:
                    for j in self.range_n:
                        self.domain[i][j] = bt_domain[i][j]                   
        return []

    def solve_and_print(self):
        """
        Solve the n-queens puzzle and print the solution
        """
        
        #solution = self.backtrack()
        #solution = self.backtrack_improved(self.forward_checking)
        solution = self.backtrack_improved(self.ac3)

        if solution.count(-1) == len(solution) or not solution:
            print(f"There is no solution to the {self.n}-queens problem.")
        else:
            print('Solution:', solution)
            for x in self.range_n:
                for y in self.range_n:
                    
                    # The board was printing rotated 90 degrees: the first
                    # number is supposed to be the column, and the second
                    # number the row.
                    if solution[y] == x:
                        print('Q', end=' ')
                    else:
                        print('-', end=' ')
                print('')
        print(f"Backtracking is called {self.backtrack_counter} times.")
        return
    
    def reset(self, n):
        """
        Perform __init__ without solving the puzzle. For timing
        """
        
        self.n = n
        self.range_n = range(n)
        self.backtrack_counter = 0
        self.assignment = [-1] * n 
        self.domain = [[True] * n for _ in self.range_n]
        return

    def time_solutions(self):
        """
        Time each type of solution and print out the time and backtrack counter
        """
        
        print(f"\nn = {self.n}\n")
        
        start = time()
        self.backtrack()
        stop = time()
        print(f"MRV acktracking took:\t{(stop-start):.4f}s")
        print(f"\t\t\t{self.backtrack_counter} recursive calls")
        self.reset(self.n)
        
        start = time()
        self.backtrack_improved(self.forward_checking)
        stop = time()
        print(f"Forward checking took:\t{(stop-start):.4f}s")
        print(f"\t\t\t{self.backtrack_counter} recursive calls")
        self.reset(self.n)
        
        start = time()
        self.backtrack_improved(self.ac3)
        stop = time()
        print(f"Arc consistency took:\t{(stop-start):.4f}s")
        print(f"\t\t\t{self.backtrack_counter} recursive calls")
        self.reset(self.n)

solver = nQueens(8)

def timing_loop(solver: nQueens):
    """
    Small function to time solving using different algorithms on puzzles of 
    different sizes.
    """
    
    n_vals = [2**i for i in range(2,7)]
    for i in n_vals:
        solver.reset(i)
        solver.time_solutions()

#timing_loop(solver)
