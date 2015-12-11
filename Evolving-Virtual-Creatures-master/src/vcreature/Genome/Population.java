package vcreature.Genome;

import vcreature.mainSimulation.InformationFrame;

import java.util.PriorityQueue;

/**
 * Class which generates and modifies the larger population of Genotypes. Implements
 * a priority queue to determine the order based upon the overall fitness of
 * each creature or genotype.
 *
 * Created by Keira on 10/13/15.
 */
public class Population
{
  private final int UPPER_NUMBER_OF_GENES = 2;
  private final int LOWER_NUMBER_OF_GENES = 4;
  private int generation = 1;
  private boolean standard_mode = true;


  private PriorityQueue<Genotype> realPopulation = new PriorityQueue<>
      ((o1, o2) -> -o1.compareTo(o2)); /* descending, probably */

  private PriorityQueue<Genotype> population = new PriorityQueue<>();
  private PriorityQueue<Genotype> modifiedCreatures = new PriorityQueue<>();

  Genotype genotype;

  /* hill climbing genotypes */
  private Genotype source;
  private Genotype mutation;
  private boolean needSource = true;


  /**
   * Class constructor. Create and initialize Population
   *
   * @param number_of_creatures number of Creatures to generate in Population
   */
  public Population(int number_of_creatures) //call from main
  {
    createPopulation(number_of_creatures);
  }

  /**
   * Creates Genotype for each Creature in Population
   *
   * @param number_of_creatures
   */
  private void createPopulation(int number_of_creatures)
  {
    for (int i = 0; i < number_of_creatures; i++)
    {
      int number_of_genes =
          (int) (Math.random() * UPPER_NUMBER_OF_GENES) + LOWER_NUMBER_OF_GENES;
      genotype = new Genotype(number_of_genes);

      // genotype = new Genotype(number_of_creatures);
      genotype.setMode(standard_mode);
      population.add(genotype);
    }
  }

  /**
   * Replace population with input then transfer modified Genotypes to
   * standard population
   *
   * @param population input population
   */
  public void returnPopulation(PriorityQueue<Genotype> population)
  {
    standard_mode = true;
    this.modifiedCreatures = population;
    movePopulation();
  }

  /**
   * Getter for first Genotype in queue
   *
   * @return first Genotype in queue (highest priority (fitness, as defined
   * in Genotype))
   */
//  public Genotype getNextCreature()
//  {
//    if (InformationFrame.hillClimb)
//    {
//      if (needSource)
//      {
//        needSource = false;
//        realPopulation.add(source);
//        return (source = population.poll());
//      }
//      else /* need mutation */
//      {
//        needSource = true;
//        realPopulation.add(mutation);
//        return (mutation = source.mutate());
//      }
//    }
//    else
//    {
//      Genotype copy = population.poll();
//      return copy;
//    }
//
//  }

  /**
   * Getter for Genotype Queue
   *
   * @return reference to Genotype queue
   */
  public PriorityQueue<Genotype> getPopulation()
  {
    return population;
  }

  public PriorityQueue<Genotype> getModifiedCreatures()
  {
    return modifiedCreatures;
  }

  public int getGeneration()
  {
    return generation;
  }

  /**
   * Replace population with modified population. Non-standard mode.
   */
  private void movePopulation()
  {
    if (population.isEmpty())
    {
      standard_mode = false;
      Genotype[] array = new Genotype[modifiedCreatures.size() + 1];
      array[array.length-1] = modifiedCreatures.peek();
      int size = modifiedCreatures.size();
      for (int i = 0; i < size; i++)
      {
        array[i] = modifiedCreatures.peek();
        modifiedCreatures.remove();
      }

      for (int i = 0; i < array.length - 1; i++)
      {
        Genotype newCreature = array[i];
        if(getGeneration() % 3 == 2)
        {
           InformationFrame.getActiveFrame().setMutate_crossover(false, true);
          newCreature = array[i].crossOver(array[i+1]);
        }
        else
        {
          InformationFrame.getActiveFrame().setMutate_crossover(true, false);
          newCreature = array[i].mutate();
        }

        newCreature.setFitness(-1);
        population.add(newCreature);
      }
      generation++;
      modifiedCreatures.clear();
    }
  }

  public void notifyGenotypeTested(Genotype genotype)
  {
    if (genotype.equals(mutation) && source.getFitness() < mutation.getFitness())
    {
      System.out.println("REMOVING OLD CREATURE. ADDING NEW ONE.");
      realPopulation.remove(source);
      population.add(mutation);
    }
  }
}
