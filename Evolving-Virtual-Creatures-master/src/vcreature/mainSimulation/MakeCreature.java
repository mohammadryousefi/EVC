// changed

package vcreature.mainSimulation;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import javafx.geometry.Point3D;
import vcreature.Genome.Gene;
import vcreature.Genome.Genotype;
import vcreature.phenotype.Block;
import vcreature.phenotype.Creature;
import vcreature.phenotype.EnumNeuronInput;
import vcreature.phenotype.Neuron;

import javax.vecmath.Point3d;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;


public class MakeCreature extends Creature
{

  private final float jumpTime = 7.0f;
  ArrayList<Block> blockList = new ArrayList<>();
  LinkedList<Gene> genes;
  Random rand = new Random();
  LinkedList<Point3D> hinges;

  private LinkedList<Gene> children = new LinkedList<>();
  private ConcurrentLinkedDeque<Gene> parents = new ConcurrentLinkedDeque<>();
  private LinkedList<Point3D> child_hinges = new LinkedList<>();
  private Point3D parent_hinge;

  public Genotype genotype;
  public Gene gene;


  float[] eulerAngles = {0, 0, 0};

  /**
   * Class constructor.
   * Initialize and start making creature from genotype input.
   *
   * @param physicsSpace reference to JME Physics, required
   * @param rootNode     reference to JME Root, required
   * @param genotype     input genotype
   */
  public MakeCreature(PhysicsSpace physicsSpace, Node rootNode, Genotype genotype)
  {
    super(physicsSpace, rootNode);


    this.genotype = genotype;
    int blocks_made = 0;

    try
    {
      //Get root block
      Gene root = genotype.getRoot();

      genes = genotype.getGeneList();

    /* Makes root block as torso*/
      Vector3f rootCenter = new Vector3f(0f, 20f, 0f);

      Vector3f rootSize = root.getGeneVector();

      //Constructs a root block for creature
      Block torso = addRoot(rootCenter, rootSize, eulerAngles);
      torso.setMaterial(Block.MATERIAL_BLUE);
      blockList.add(torso);
      blocks_made++;


      int numberOfChildren = root.getNumberOfChildren();


      System.out.println("Number of Genes " + genotype.getNumberOfGenes());
      for (int i = 0; i < numberOfChildren;
           i++) //Each hinge consists of two pivots
      {
        parents.add(root.getChild(i));
        children.add(root.getChild(i));
        child_hinges.add(root.getChildHinge(i));
      }
      for (int j = 0; j < numberOfChildren; j++)
      {
        parent_hinge = root.getChild(j).getParentHinge();
        buildCreature(torso, blockList, children.poll().getGeneVector(),
            child_hinges.poll(), parent_hinge);
        blocks_made++;
      }

      ArrayList<Block> tempBlocks = new ArrayList<>();
      for (Block child : blockList)
      {
        if (parents.peek() != null) root = parents.poll();
        else break;
        numberOfChildren = root.getNumberOfChildren();
        for (int i = 0; i < numberOfChildren;
             i++) //Each hinge consists of two pivots
        { //sets up each child block and connects to parent block
          parents.add(root.getChild(i));
          children.add(root.getChild(i));
          child_hinges.add(root.getChildHinge(i));
        }
        for (int j = 0; j < numberOfChildren; j++) //Makes children blocks
        {
          parent_hinge = root.getChild(j).getParentHinge();
          buildCreature(child, tempBlocks, children.poll().getGeneVector(),
              child_hinges.poll(), parent_hinge);
          blocks_made++;
        }
      }

      //Add all children blocks to full list of blocks
      blockList.addAll(tempBlocks);

      System.out.println("BLOCKS PLACED " + blocks_made);

      while (getNumberOfBodyBlocks() > blockList.size())
      {
        rootNode.detachChildAt(getNumberOfBodyBlocks() - 1);
      }
    }catch(NullPointerException e)
    {
      return;
    }

  }


  /**
   * Takes parent block and builds hinges and child blocks from it. Then adds
   * new child blocks to specified array. (Avoids concurrent modification errors)
   *
   * @param parent    Block being added to.
   * @param blockList ArrayList saving the blocks.
   * @param child_dimension Size of child block as a vector3f
   * @param child_hinge Hinge connecting parent to child
   * @param parent_hinge Hinge connecting child to parent
   */
  private void buildCreature(Block parent, ArrayList<Block> blockList, Vector3f child_dimension,
                             Point3D child_hinge, Point3D parent_hinge)
  {
    Vector3f pivot1 = new Vector3f((float)child_hinge.getX()*(parent.getSizeX()/2),
            (float)child_hinge.getY()*(parent.getSizeY()/2),(float)child_hinge.getZ()*(parent.getSize()/2));
    Vector3f child = child_dimension;
    Vector3f pivot2 = new Vector3f((float)parent_hinge.getX()*(child.getX()),
            (float)parent_hinge.getY()*(child.getY()),(float)parent_hinge.getZ()*(child.getZ()));
    Block limb = addChildtoBody(child, parent, pivot1, pivot2);
    blockList.add(limb);


    for (Block block : blockList) //Copy temp array back to used array
    {
      createNeurons(block);
    }

  }

  /**
   * Adds child block to creature.
   *
   * @param childHalfSize Half sizes of block.
   * @param torso         Parent block being attached to.
   * @param pivot1        Hinge on parent block.
   * @param pivot2        Hinge on child block.
   * @return New block.
   */
  private Block addChildtoBody(Vector3f childHalfSize, Block torso, Vector3f pivot1,
                               Vector3f pivot2)
  {
    Block child = addBlock(eulerAngles, childHalfSize, torso, pivot1, pivot2,
            Vector3f.UNIT_Z);
    child.setMaterial(Block.MATERIAL_RED);
    return child;
  }

  /**
   * Should take every hinge and add a Neuron rule to it, then specify which
   * block
   * the rules are being added to.
   *
   * @param block Block with hinge
   */
  public void createNeurons(Block block)
  {
    Neuron n1 = new Neuron(EnumNeuronInput.TIME, null,
            EnumNeuronInput.CONSTANT, EnumNeuronInput.CONSTANT, null);


    n1.setInputValue(Neuron.C, jumpTime);

    n1.setInputValue(Neuron.D, Float.MAX_VALUE);


    Neuron n2 = new Neuron(EnumNeuronInput.TIME, null,
            EnumNeuronInput.CONSTANT, EnumNeuronInput.CONSTANT, null);

    n2.setInputValue(Neuron.C, jumpTime - 1);
    n2.setInputValue(Neuron.D, -Float.MAX_VALUE);


    block.addNeuron(n1);
    block.addNeuron(n2);
  }



}