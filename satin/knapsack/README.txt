The goal is to find a set of items, each with a weight w and a value v such
that the total value is maximal, while not exceeding a fixed weight limit.
The problem for n items is recursively divided into two subproblems for
n-1 items, one with the missimg item placed into the knapsack, and one
without it.
The program takes one parameter: the number of items. The weights and
values are chosen randomly.
