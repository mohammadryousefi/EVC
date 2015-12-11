package vcreature.Genome;

import com.jme3.math.Vector3f;
import javafx.geometry.Point3D;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Keira on 10/13/15.
 * <p>
 * Class gets called by Population.
 * There may be more hinges than Genes
 * Generated hinges can lie at center of face,
 * center of edge, endpoint of edge (26 options)
 * specified by -1, 0, 1 in each of xyz-direction (0,0,0) is invalid.
 */
public class Genotype implements Comparable
{
  private final float LOWER_BOUND = 0.5f;
  private final float UPPER_BOUND = 1.5f;

  private double chance_of_child_spawn = 0.9; //90% initially
  private int number_of_genes;
  private double creature_fitness = -1.0; //Fitness hasn't been calc yet
  private LinkedList<Gene> creature = new LinkedList<>();
  private Gene root; //The root of the "creature tree".
private Random rng = new Random();
  private Point3D parent_hinge;
  private LinkedList<Point3D> child_hinges = new LinkedList<>();
  private Queue<Gene> needs_processing = new ConcurrentLinkedQueue<>();
  private int genes_created = 0;
  private int number_of_children;
  private boolean standard = true;
  private Random random = new Random();

  private Mutation mut;

  Vector3f gene_dimension;

  /**
   * Class constructor. Initializes parameters and calls generateGenotype to fill in details.
   * @param number_of_genes  desired number of genes to create
   */
  public Genotype(int number_of_genes)
  {
    this.number_of_genes = number_of_genes;
    parent_hinge = null;
    generateGenotype();

    mut = new Mutation(this);
    mut.init();
  }

  /**
   * Construct genotype from linked list of genes by unpacking a "blueprint"
   * @param creature  input list of genes
   */
  public Genotype(LinkedList<Gene> creature)
  {
    this.creature = creature;
    buildBlueprint(creature);
  }

  /* private empty constructor for internal use */
  private Genotype()
  {
  }

  /**
   * Fill in Genotype fields by extracting information from an input list of genes
   * @param creature  linked list of genes
   */
  private void buildBlueprint(LinkedList<Gene> creature)
  {
    root = creature.poll();
    Gene parent = root;
    needs_processing.add(parent);
    while (needs_processing.peek()!=null)
    {
      int number_of_children = parent.getNumberOfChildren();
      for (int i = 0; i < number_of_children; i++)
      {
        Gene child;
        if (creature.peek()!=null) child = creature.poll();
        else break;
        parent.addChild(child);
        needs_processing.add(child);
      }
      if (creature.peek()!=null) parent = creature.poll();
      else break;
    }
  }



  /**
   * Function which generates three random dimensions or lengths for a "gene block".
   * These are then used to create the current gene_dimension Vector3F.
   *
   * @param lower_bound
   * @param upper_bound
   */
  private void createGene(float lower_bound, float upper_bound)
  {
    float x_len = 0.0f;
    float y_len = 0.0f;
    float z_len = 0.0f;
    float[] values = new float[3];
    int rand = random.nextInt(3);
    for (int i = 0; i < 3; i++)
    {
      values[i] = lower_bound + random.nextFloat() * upper_bound;
      lower_bound = modifyLowerBound(values[i], lower_bound);
      upper_bound = UPPER_BOUND - lower_bound + LOWER_BOUND;
    }
    x_len = values[(0 + rand) % 3];
    y_len = values[(1 + rand) % 3];
    z_len = values[(2 + rand) % 3];
    gene_dimension = new Vector3f(x_len, y_len, z_len);
  }

  /**
   * Function which generates the root node from scratch. (Does not include child hinges,
   * they are generated at the time at which the child block is created).
   */
  private void createRoot()
  {
    createGene(LOWER_BOUND, UPPER_BOUND);
    root = new Gene(gene_dimension, parent_hinge);
    creature.add(new Gene(gene_dimension, parent_hinge));
  }

  /**
   * Primary function used to build the creature. This involves creating the root, and by then randomly
   * generating the children, child hinges and adding them too each successive parent. Each child
   * is separately processed and treated as a potential parent before being removing from the
   * processing deque.
   */
  private void generateGenotype()
  {
    genes_created = 0;
    int childMax = 2;
    createRoot();
    genes_created++;
    needs_processing.add(root);
    while (needs_processing.peek() != null)
    {
      int created_on_parent = 0;
      Gene parent = needs_processing.poll(); //Starts with the root.
      while (true) //Selects number of children and determines validity.
      {
        if (genes_created >= number_of_genes)
        {
          number_of_children = 0;
          break;
        }
        else
        {
          number_of_children = random.nextInt(childMax) + 1;
          if (number_of_genes >= (genes_created + number_of_children)) break;
          else continue;
        }
      }
      child_hinges.clear();
      for (int i = 0; i < number_of_children; i++)
      {
        Point3D next_hinge = generateHingeQuadrant();
        if (i > 0)
        {
          for (Point3D previous : child_hinges)
          {
            while (true)
            {
              if (previous.equals(next_hinge)) next_hinge = generateHingeQuadrant();
              else break;
            }
          }
        }
        child_hinges.add(next_hinge);
      }
      while (created_on_parent < number_of_children)
      {
        for (Point3D hinge : child_hinges)
        {
          parent_hinge = generateHingeQuadrant();
          while (true)
          {
            if (hinge.equals(parent_hinge)) parent_hinge = generateHingeQuadrant();
            else break;
          }
        }
        createGene(LOWER_BOUND, UPPER_BOUND); //create the next block dimensions.
        Gene child = new Gene(gene_dimension, parent_hinge);
        child.setParentRef(parent);
        needs_processing.add(child); //this child will now be used as a parent.
        parent.addChild(child);
        creature.add(child);
        parent.addChildHinge(child_hinges.poll());
        parent.incrementChildren();
        created_on_parent++;
        genes_created++;
      }
    }
    //System.out.println("TEST COMPLETED : Genes Required "+number_of_genes+" : Genes actually created "+genes_created);
  }

  /**
   * Modifies the lower bound from createGene() if the upper bound goes
   * over 10;
   *
   * @param length
   * @param lower_bound
   * @return lower_bound
   */
  private float modifyLowerBound(float length, float lower_bound)
  {
    length /= 10;

    if (lower_bound < length && length >= 0.5)
    {
      return length;
    }
    return lower_bound;
  }

  /**
   *
   * @return root the gene
   */
  public Gene getRoot()
  {
    return root;
  }

  /**
   * @return Point3D the point in -1,0,1 used to represent the 3D quadrant
   */
  private Point3D generateHingeQuadrant()
  {
    int x_sign;
    int y_sign;
    int z_sign;
    boolean sign = random.nextBoolean();
    if (sign) x_sign = 1;
    else x_sign = -1;
    sign = random.nextBoolean();
    if (sign) y_sign = 1;
    else y_sign = -1;
    sign = random.nextBoolean();
    if (sign) z_sign = 1;
    else z_sign = -1;

    return new Point3D((double) x_sign, (double) y_sign, (double) z_sign);
  }

  /**
   * Override to implement Comparable
   *
   * @param other object
   * @return (-1, 0, 1) for (<, =, >)
   */
  @Override
  public int compareTo(Object other)
  {
    Genotype other_genotype = (Genotype) other;
    double other_fitness = other_genotype.getFitness();
    if (!standard)
    {
      if (creature_fitness < other_fitness) return -1;
      else if (creature_fitness > other_fitness) return 1;
      else return 0;
    }
    else
    {
      if (creature_fitness > other_fitness) return -1;
      else if (creature_fitness < other_fitness) return 1;
      else return 0;
    }
  }

  /**
   * Setter for gene Mutation
   *
   * @param mut
   */
  public void setMutation(Mutation mut)
  {
    this.mut = mut;
  }

  /**
   * Getter for gene list
   *
   * @return reference to gene list
   */
  public LinkedList<Gene> getGeneList()
  {
    LinkedList<Gene> copy = root.getTreeAsList();
    return copy;
  }

  /**
   * Getter for number of genes
   *
   * @return number of genes
   */
  public int getNumberOfGenes()
  {
    number_of_genes = root.getTreeAsList().size();
    return number_of_genes;
  }

  /**
   * Setter for mode flag.
   *
   * @param standard true = standard mode
   */
  public void setMode(boolean standard)
  {
    this.standard = standard;
  }

  /**
   * Getter for genotype fitness
   *
   * @return fitness
   */
  public double getFitness()
  {
    return creature_fitness;
  }

  /**
   * Setter for genotype fitness
   *
   * @param fitness
   */
  public void setFitness(double fitness)
  {
    creature_fitness = fitness;
  }

  /**
   * Clones relevant fields for use by Mutation and Crossover
   * @return
   */
  public Genotype cloneForMutation()
  {
    Genotype clone = new Genotype();

    clone.number_of_genes = number_of_genes;
    clone.creature_fitness = -1.0; /* has not been tested */
    clone.chance_of_child_spawn = chance_of_child_spawn;
    if(number_of_genes > 1)
    {
      clone.parent_hinge = new Point3D(parent_hinge.getX(), parent_hinge.getY(),
          parent_hinge.getZ());
    }
    else{
      clone.parent_hinge = null;
    }
    clone.genes_created = genes_created;
    clone.number_of_children = number_of_children;
    clone.standard = standard;
    clone.root = root;

    for (Gene gene : creature) clone.creature.add(gene.deepClone());
    for (Point3D p : child_hinges) clone.child_hinges.add(new Point3D(p.getX(), p.getY(), p.getZ()));

    return clone;
  }

  /**
   * This method makes a "deep clone" of any Java object it is given.
   *
   * @return New instance of the object with the same data.
   * @see <a href="http://alvinalexander.com/java/java-deep-clone-example-source-code">Source</a>
   */
  public Genotype deepClone()
  {
    try
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(this);
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      return (Genotype) ois.readObject();
    } catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Add a child block to the desired block specified by index
   * @param mutationGeneIndex  index of block to add child to
   */
  public void addChildBlock(int mutationGeneIndex)
  {
    createGene(1, 2);
    parent_hinge = generateHingeQuadrant();
    Gene newGene = new Gene(gene_dimension,parent_hinge);
    creature = root.getTreeAsList();
    Gene surrogate = creature.get(mutationGeneIndex);
    newGene.setParentRef(surrogate);
    surrogate.addChild(newGene);
    surrogate.addChildHinge(generateHingeQuadrant());
    surrogate.incrementChildren();
    number_of_genes++;
    creature = root.getTreeAsList();
  }

  /**
   * Removes gene from tree-like structure by linear index
   * @param mutationGeneIndex  index of gene to remove
   */
  public void removeGeneFromTreeByIndex(int mutationGeneIndex)
  {
    System.out.println("Remove gene by index "+mutationGeneIndex);
    creature = root.getTreeAsList();
    Gene delete = creature.get(mutationGeneIndex);
    removeGeneFromTree(delete);
    creature = root.getTreeAsList();
    number_of_genes = creature.size();
  }

  /**
   * Remove gene from tree-like structure by Gene reference
   * @param delete  reference to gene being deleted
   */
  private void removeGeneFromTree(Gene delete)
  {
    needs_processing.clear();
    needs_processing.add(root);
    while(needs_processing.peek()!=null)
    {
      System.out.println("Need processing");
      Gene check = needs_processing.poll();
      System.out.println("Checking Objects");
      System.out.println("Checking address: " + check + ", " + delete);
      System.out.println("Checking size: " + check.getGeneVector() + ", " + delete.getGeneVector());
      if (check.equals(delete))
      {
        System.out.println("Delete check");
        check.getParent().removeChild(check);
        return;
      }
      int num_child = check.getNumberOfChildren();
      for (int i = 0; i < num_child; i++)
      {
        Gene child = check.getChild(i);
        if (child.equals(delete))
        {
          System.out.println("Delete child");
          check.removeChild(child);
          return;
        }
        needs_processing.add(child);
      }
    }
  }

  /**
   * Removes gene by linear index and doesn't think or care the Genotype is a Tree
   * @param mutationGeneIndex  index of gene to delete
   */
  public void removeGeneByIndex(int mutationGeneIndex)
  {
    removeGeneFromTreeByIndex(mutationGeneIndex);
  }

  /**
   * Modifies gene at linear index by adding to its size
   * @param mutationGeneIndex  index of gene to modify
   * @param mutationVector     size vector to add
   */
  public void mutateGeneByIndex(int mutationGeneIndex, Vector3f mutationVector)
  {
    Vector3f geneVec = creature.get(mutationGeneIndex).getGeneVector();
    geneVec.x += mutationVector.x;
    geneVec.y += mutationVector.y;
    geneVec.z += mutationVector.z;
    if (geneVec.x < 0.5f) geneVec.x = 0.5f;
    if (geneVec.y < 0.5f) geneVec.y = 0.5f;
    if (geneVec.z < 0.5f) geneVec.z = 0.5f;
  }

  /**
   * Genetic mutation entry point. See the wonders of Artificial Intelligence unfold before your very own eyes!
   * @return  Magic
   */
  public Genotype mutate()
  {
    return mut.mutate();
  }

  public Genotype crossOver(Genotype otherCreature)
  {
    System.out.println("CROSSOVER");
    Genotype newInstance = this.cloneForMutation();
    Genotype otherInstance = otherCreature.cloneForMutation();

    List<Gene> creatureList = newInstance.root.getTreeAsList();
    Gene nodeToRemove = creatureList.get(1 + rng.nextInt(creatureList.size() - 1));
    Gene parentNode = nodeToRemove.getParent();
    parentNode.removeChild(nodeToRemove);
    creatureList = otherInstance.root.getTreeAsList();
    Gene nodeToAdd = creatureList.get(rng.nextInt(creatureList.size()));
    parentNode.addChild(nodeToAdd);
    return newInstance;
  }
}
