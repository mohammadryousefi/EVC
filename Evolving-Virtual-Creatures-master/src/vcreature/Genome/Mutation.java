package vcreature.Genome;

import com.jme3.math.Vector3f;

import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Mutation class is also known as the Hill Climbing for our creatures.
 * Responsible for choosing mutation type and updating probability for that
 * mutation.
 */
public class Mutation
{

  public static final double STEP_ADJUST_RATE = .9;
  private static Random rand = new Random();

  /**
   * Mutation types
   */
  public enum Type
  {
    REMOVE_BLOCK,
    ADD_BLOCK,
    MODIFY_BLOCK
  }

  /* Mutation.Type --> Integer */
  private Map<Type, Integer> typeProbMap = new TreeMap<>();

  private int typeProbSum;
  /* Gene index --> Integer */
  private Map<Integer, Integer> geneProbMap = new TreeMap<>();
  private int geneProbSum;

  private float maxStepSize = 1;
  private float xMean, yMean, zMean;

  private final Genotype genotype;

  /**
   * Makes a new mutation
   * @param genotype genotype to mutate
   */
  public Mutation(Genotype genotype)
  {
    this.genotype = genotype;
  }

  public void init()
  {
    initTypeMap();
    initGeneMap();
  }

  /**
   * Initializes map of genotypes to probabilities
   */
  private void initGeneMap()
  {
    int size = genotype.getGeneList().size();
    for (int i = 0; i < size; i++)
    {
      geneProbMap.put(i, 1);
    }
    geneProbSum = size;
  }

  /**
   * Initializes map of types to probabilities
   */
  private void initTypeMap()
  {
    /* all equally likely */
    typeProbMap.put(Type.ADD_BLOCK, 1);
    typeProbMap.put(Type.REMOVE_BLOCK, 1);
    typeProbMap.put(Type.MODIFY_BLOCK, 1);
    typeProbSum = 3;
  }

  /**
   * Clones the genotype passed in and mutates it in one of three ways.
   * The step size as well as the probabilities increase.
   * @return Returns mutated genotype.
   */
  public Genotype mutate()
  {
    int newVal;
    Genotype clone = genotype.cloneForMutation();

    Mutation cloneMut = new Mutation(clone);

    //Initializes trees
    cloneMut.initWith(this);


    Type mutationType = randType(); //chooses random mutation type
    //int mutationGeneIndex = randGeneIndex(); //chooses random index
    int mutationGeneIndex = rand.nextInt(clone.getGeneList().size());
    float stepSize = stepSize();
    Vector3f mutationVector = randVector().normalize().mult(stepSize);

    switch (mutationType)
    {
      case REMOVE_BLOCK:
        //Filter illegal removals
        //  if only 2 or less genes left
        //  if trying to remove the root gene
        if (clone.getNumberOfGenes() > 2 && mutationGeneIndex != 0)
        {
          System.out.println("REMOVE_BLOCK");
          System.out.println("mutationGeneIndex: " + mutationGeneIndex);
          clone.removeGeneByIndex(mutationGeneIndex);

          //Creates probability that this is chosen again
          newVal = cloneMut.typeProbMap.get(Type.REMOVE_BLOCK) + 1;
          cloneMut.typeProbMap.put(Type.REMOVE_BLOCK, newVal);
        }
        break;
      case ADD_BLOCK:
        System.out.println("ADD_BLOCK");
        clone.addChildBlock(mutationGeneIndex);

        //updates probability that this is chosen again
        newVal = cloneMut.typeProbMap.get(Type.ADD_BLOCK) + 1;
        cloneMut.typeProbMap.put(Type.ADD_BLOCK, newVal);
        break;
      case MODIFY_BLOCK:
        System.out.println("MODIFY_BLOCK");
        clone.mutateGeneByIndex(mutationGeneIndex, mutationVector );

        //updates probability that this is chosen again
        newVal = cloneMut.typeProbMap.get(Type.MODIFY_BLOCK) + 1;
        cloneMut.typeProbMap.put(Type.MODIFY_BLOCK, newVal);

        //Uses definition of step size in spec
        if(stepSize < maxStepSize/2)
        {
          cloneMut.maxStepSize *= STEP_ADJUST_RATE; //arbitrary value
        }
        else
        {
          cloneMut.maxStepSize /= STEP_ADJUST_RATE;
        }

        //Decides whether modifying the block will make it bigger or smaller
        cloneMut.xMean += (mutationVector.x - xMean)/2;
        cloneMut.yMean += (mutationVector.y - yMean)/2;
        cloneMut.zMean += (mutationVector.z - zMean)/2;


        break;
    }

    clone.setMutation(cloneMut);
    return clone;
  }

  /**
   * Initializes all trees to work with the clone's attributes.
   * @param mutation Mutation (clone) with specific attributes.
   */
  private void initWith(Mutation mutation)
  {
    typeProbMap = new TreeMap<>(mutation.typeProbMap);
    typeProbSum = mutation.typeProbSum;

    geneProbMap = new TreeMap<>();
    initGeneMap();

    xMean = mutation.xMean;
    yMean = mutation.yMean;
    zMean = mutation.zMean;
    maxStepSize = mutation.maxStepSize;
  }

  /**
   * Uses gaussian curve to make a new vector with existing averages.
   * @return new Vector3f
   */
  private Vector3f randVector()
  {
    return new Vector3f(gaussianFloat(xMean),
        gaussianFloat(yMean),
        gaussianFloat(zMean));
  }

  /**
   * Chooses a step size from one to maxStepSize initially.
   * @return A step size.
   */
  private float stepSize()
  {
    return rand.nextFloat() * maxStepSize;
  }

  /**
   * Helper method that uses gaussian curve to get standard deviation of mean.
   * @param mean Average.
   * @return A random float.
   */
  private static float gaussianFloat(float mean)
  {
    return (float)rand.nextGaussian() + mean;
  }

  /**
   * @return Random index in the gene probability map.
   */
  private Integer randGeneIndex()
  {
    return randInProbMap(geneProbSum, geneProbMap);
  }

  /**
   * @return Random mutation type based on probability.
   */
  private Type randType()
  {
    return randInProbMap(typeProbSum, typeProbMap);
  }

  /**
   * @param mapSum Probability passed in.
   * @param map Specific map to look at.
   * @param <K> Key in the map.
   * @return Key (Probability)
   */
  private <K> K randInProbMap(int mapSum,
                          Map<K, Integer> map)
  {
    int choice = rand.nextInt(mapSum);
    int val = 0;
    for(K key : map.keySet())
    {
      val += map.get(key);
      if(choice < val) return key;
    }
    /* never reached */
    return null;

  }


}
