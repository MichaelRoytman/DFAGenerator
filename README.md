# DFAGenerator
##Generator that creates a deterministic finite automaton (DFA) from a whitelist and minimizes it with the table-filling algorithm

Node.java is an implementation of a node/state class. It is used to represent states in an automaton.

DFAGenerator.java contains code that transforms a whitelist into a DFA that accepts all the strings in that whitelist. It also minimizes the DFA to remove redundant or non-reachable state and to reach a minimal-state DFA.