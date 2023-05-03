from cgi import test
import random
import math

class Node:

    name =""
    parentNames = []
    cpt = []

    def __init__(self, nodeInfo):
        """
        :param nodeInfo: in the format as [name, parents, cpt]
        """
        # name, parents, cpt

        self.name = nodeInfo[0]
        self.parentNames = nodeInfo[1].copy()
        self.cpt = nodeInfo[2].copy()


    def format_cpt(self):
        s_cpt = '\t'.join(self.parentNames) + '\n'
        for i in range(len(self.cpt)):
            s_cpt += bin(i).replace("0b", "").zfill(len(self.parentNames)).replace('0', 'T\t').replace('1', 'F\t')
            s_cpt += str(self.cpt[i]) + '\n'
        return s_cpt


    def print(self):
        print("name: {}\nparents:{}\ncpt:\n{}".format(self.name, self.parentNames, self.format_cpt()))


class BayesNet:
    nodes = []

    def __init__(self, nodeList):
        for n in nodeList:
            self.nodes.append(Node(n))

    def print(self):
        for n in self.nodes:
            n.print()
    
    def sample(self, p):
        """
        :param p: a probability
        :return: a boolean, if the next normally distributed value was a success with probability p
        """
    
        return random.random() < p
    
    def probGivenParents(self, var, x, flag, niMap):
        """
        :param index: the index of the variable being looked up
        :param x: the current state of the bayes net
        :param flag: boolean variable, representing whether the sampling should be
            direct or complement
        :param niMap: a mapping from variable names to their corresponding indices
        :return: the probability of the given variable being true, given its parents
        """
        j = 0 # index in cpt for var given x
        if self.nodes[var].parentNames != []:
            k = 2 ** (len(self.nodes[var].parentNames) - 1)
            for parent in self.nodes[var].parentNames:
                if not x[niMap[parent]]:
                    j += k
                k //= 2
        if flag:
            return self.nodes[var].cpt[j]
        else: 
            return 1 - self.nodes[var].cpt[j]
    
    def normalize(self, dist):
        """
        :param dist: probability distribution, as a list
        :return: equivalent normalized probability distribution
        """
        
        sum = dist[0] + dist[1]
        dist[0] /= sum
        dist[1] /= sum
        return dist
    
    def consistent(self, x, evidence):
        """
        :param x: a state of the bayes net
        :param evidence: evidence variables and their values in a dictionary
        :return: boolean value, if the sample is consistent with the evidence
        """
        
        for i in range(len(self.nodes)):
            val = evidence.get(self.nodes[i].name)
            if val != None and val != x[i]:
                return False
        return True

    def rejectionSampling(self, qVar, evidence, N):
        """
        :param qVar: query variable
        :param evidence: evidence variables and their values in a dictionary
        :param N: maximum number of iterations
        E.g. ['WetGrass',{'Sprinkler':True, 'Rain':False}, 10000]
        :return: probability distribution for the query
        """
        
        TFMAP = {True:0, False:1} # mapping, used to convert booleans to indices
        countDist = [0, 0] # the counts used for the final probability distribution
        niMap = {} # mapping of variable names to their corresponding index
        x = [None] * len(self.nodes) # the state of the system
        
        # build niMap and get the index of qVar
        for i in range(len(self.nodes)):
            niMap[self.nodes[i].name] = i
            if self.nodes[i].name == qVar:
                qIndex = i
        
        # repeat N times:
        # - get a new random sample
        # - determine if it is consistent with the evidence variables
        #   - if so, increment count at the index corresponding to the query variable
        for _ in range(N):
            for i in range(len(self.nodes)):
                x[i] = self.sample(self.probGivenParents(i, x, True, niMap))
            if self.consistent(x, evidence):
                countDist[TFMAP[x[qIndex]]] += 1                
        
        # normalize and return the probability distribution
        return self.normalize(countDist)
    
    def markovSample(self, var, x, flag, niMap):
        """
        :param var: index of variable to change with sampling
        :param x: current state of the bayes net
        :param flag: boolean variable, representing whether the sampling should be
            direct or complement
        :param niMap: a mapping from variable names to their corresponding indices
        :return: a number, the probability of var given var's markov blanket
        """
        
        children = []
        for i in range(len(self.nodes)):
            if self.nodes[var].name in self.nodes[i].parentNames:
                children.append(i)
        prob = self.probGivenParents(var, x, flag, niMap)
        for child in children:
            prob *= self.probGivenParents(child, x, x[child], niMap)
        return prob

    def  gibbsSampling(self, qVar, evidence, N):
        """
        :param qVar: query variable
        :param evidence: evidence variables and their values in a dictionary
        :param N: maximum number of iterations
        E.g. ['WetGrass',{'Sprinkler':True, 'Rain':False}, 10000]
        :return: probability distribution for the query
        """
        
        TFMAP = {True:0, False:1} # mapping, used to convert booleans to indices
        countDist = [0, 0] # the counts used for the final probability distribution
        probDist = [0, 0] # the probabilities for the true case and the false case for a given variable
        niMap = {} # mapping of variable names to their corresponding index
        z = [] # list of indices of non-evidence variables
        x = [None] * len(self.nodes) # the state of the system
        
        # build niMap, get the index of qVar, build z, and determine the initial state of the system
        for i in range(len(self.nodes)):
            niMap[self.nodes[i].name] = i
            if self.nodes[i].name not in evidence.keys():
                z.append(i)
                x[i] = random.choice([True, False])
            else:
                x[i] = evidence[self.nodes[i].name]
            if self.nodes[i].name == qVar:
                qIndex = i
            
        # repeat N times:
        # - select random non-evidence variable
        # - set that variable to true, then false, and get the markov probability of each state
        # - normalize those probabilities and sample from that distribution
        # - increment count at the index corresponding to the query variable
        for _ in range(N):
            var = random.choice(z)
            for b in [True, False]:
                x[var] = b
                probDist[TFMAP[b]] = self.markovSample(var, x, b, niMap)
            x[var] = self.sample(self.normalize(probDist)[0])
            countDist[TFMAP[x[qIndex]]] += 1
            
        # normalize and return the probability distribution
        return self.normalize(countDist)


# Sample Bayes net
nodes = [["Cloudy", [], [0.5]],
 ["Sprinkler", ["Cloudy"], [0.1, 0.5]],
 ["Rain", ["Cloudy"], [0.8, 0.2]],
 ["WetGrass", ["Sprinkler", "Rain"], [0.99, 0.9, 0.9, 0.0]]]
b = BayesNet(nodes)
# b.print()

# Sample queries to test your code
print(b.gibbsSampling("Rain", {"Sprinkler":True, "WetGrass" : False}, 100000))
print(b.rejectionSampling("Rain", {"Sprinkler":True}, 1000))
