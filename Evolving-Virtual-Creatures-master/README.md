# Evolving-Virtual-Creatures
Project 2
Ally Goodman-Janow, Keira Haskins, Rashid Yousefi, Thien-Cam Nguyen

The purpose of this application is to use a Genetic Algorithm along with Hill Climbing to procedurally, and somewhat randomly, create a 3D creature made out of blocks that uses such algorithms to learn how to jump. 

Every creature uses the same characteristics such as size, how many children are connected, how many hinges are connecting children, and neurons that fire to make the creature "jump" by using a down and then up force when in the physics space. 

In order to give the creature these characteristics, we build a Genotype that sets up all characteristics and creates a list of Genes for each block in the body of the creature. This Genotype is responsible for outputing an x_Length, y_Length and z_Length as a Vector3f to determine the size of a specific block, the number_of_children that block will have connected to it, a parent hinge that says which block is the current block's parent, a set of child_hinges made up of Point3D that determine the relative position of a hinge onto a block, and of course, the overall fitness of that creature. 

To then determine the fitness of that creature, and more scientifically, that Genotype combination, we create the creature in the physics space with the given attributes, wait a certain amount of time for the neurons to fire, and then save the fitness of that creature. (Here, the fitness means the lowest point of the creature from the ground once it has "jumped".)

Our GUI component displays the population's information as the simulation runs as well as options for using Hill Climbing and/or Crossover.
The Hill Climbing checkbox, once checked, begins by taking a genotype who's fitness has already been set, and mutates that genotype in one of these three ways: removes a block from the body, adds a block to the body, or changes the size of some block in the body. A comparison is done between the old genotype and the newly mutated one, and if the fitness has increased then the probability of using that mutation now increases as well. If it does not increase the fitness, the opposite is done. 

The Crossover checkbox, once checked, begins by taking two or more genotypes who's fitness has been set and starts swapping their genes/attributes. Those creatures are then put back into the population to be tested. 

While all this is happening, a graph of all the fitnesses over the elapsed time is being recorded and made into a text file format that can then be read in using Matlab. This graph should display how much better the fitnesses have gotten over time by using the hill climbing algorithm and by using crossover. 

When running the simulation, there are a few parameters that the user can type in such as:
-headless
-nokeep
-graph
-noclimb
-nocrossover

Using -headless removes the gui components but runs the simulation normally.
Using -nokeep removes the data from the previous use and writes over it.
Using -graph takes all data after the simulation is done running through all the creatures and graphs it in Matlab.
Using -noclimb removes the hill climbing algorithm, so only the crossover is being done.
Using -nocrossover removes the crossover algorithm, so only the hill climbing is being done. 
