package vcreature.Genome;

import com.jme3.math.Vector3f;
import javafx.geometry.Point3D;

import java.util.LinkedList;

/**
 * Created by keira on 10/13/15.
 * 
 *
 * Class gets called by Genotype. Each Block in the Creature is represented by one Gene
 */
public class Gene
{
  private Vector3f gene = new Vector3f(0,0,0);
  private int number_of_children;
  private double gene_fitness = -1.0;
  private Point3D parent_hinge = new Point3D(0,0,0);
  private LinkedList<Point3D> child_hinges = new LinkedList<>();
  private Gene parent_gene;
  private LinkedList<Gene> children = new LinkedList<>();

  /**
   * @param gene                gene dimension
   * @param parent              location of hinge in current local coordinates
   *
   */
  public Gene(Vector3f gene, Point3D parent)
  {
    this.gene = gene;
    parent_hinge = parent;
  }

  /**
   * Copy constructor
   * @param gene_size     size of gene
   * @param fitness       fitness of gene
   * @param num_children  number of children
   * @param parent_hinge  parent hinge location in local coordinates
   * @param child_hinges  list of child hinge locations in local coordinates
   * @param parent        reference to parent gene
   * @param children      list of references to child genes
   */
  public Gene(Vector3f gene_size, double fitness, int num_children, Point3D parent_hinge, LinkedList<Point3D> child_hinges, Gene parent, LinkedList<Gene> children)
  {
    this.gene = new Vector3f(gene_size.x, gene_size.y, gene_size.z);
    this.gene_fitness = fitness;
    this.number_of_children = num_children;
    this.parent_hinge = new Point3D(parent_hinge.getX(), parent_hinge.getY(), parent_hinge.getZ());
    this.child_hinges = new LinkedList<>();
    for (Point3D hinge : child_hinges)
    {
      this.child_hinges.add(new Point3D(hinge.getX(), hinge.getY(), hinge.getZ()));
    }
    this.parent_gene = parent;
    this.children = new LinkedList<>();
    for (Gene child : children)
    {
      Gene childClone = deepClone(child);
      childClone.setParentRef(this);
      this.children.add(childClone);
    }
  }

  /**
   * Deep clone of source gene
   * @param source  source gene
   * @return  Deep clone of source gene
   */
  public Gene deepClone(Gene source)
  {
    return new Gene(source.gene, source.gene_fitness, source.number_of_children, source.parent_hinge, source.child_hinges, source.parent_gene, source.children);
  }

  /**
   * Gets gene tree as linked list
   * @return  LinkedList representation of gene tree
   */
  public LinkedList<Gene> getTreeAsList()
  {
    LinkedList<Gene> result = new LinkedList<>();
    result.add(this);
    getSubtreeAsList(result, this);
    return result;
  }

  /**
   * Gets subtree starting at node as linked list
   * @param subtree  possibly partially populated list
   * @param node  starting node for recursive traversal
   */
  private void getSubtreeAsList(LinkedList<Gene> subtree, Gene node)
  {
    subtree.addAll(node.children);
    for (Gene child : node.children)
    {
      getSubtreeAsList(subtree, child);
    }
  }

  /**
   * Getter for the block size defined by this gene
   * @return  Gene's block size (half extent)
   */
  public Vector3f getGeneVector()
  {
    return gene;
  }

  /**
   * Getter for the number of children
   * @return  number of children
   */
  public int getNumberOfChildren()
  {
    return number_of_children;
  }

  /**
   * Increments Children
   */
  public void incrementChildren()
  {
    number_of_children++;
  }

  /**
   * Getter for location of hinge to parent
   * @return  location of hinge to parent in current local coordinates
   */
  public void addChildHinge(Point3D child_hinge)
  {
    child_hinges.add(child_hinge);
  }

  /**
   * Getter for location of hinge to parent
   * @return  location of hinge to parent in current local coordinates
   */
  public Point3D getParentHinge()
  {
    return new Point3D(parent_hinge.getX(), parent_hinge.getY(), parent_hinge.getZ());
  }

  /**
   * Setter for gene fitness. A tested Creature will bestow the same fitness to all of its genes prior to crossover/hill-climbing.
   * @param index             index of hinge
   */
  public Point3D getChildHinge(int index)
  {
    if (child_hinges.get(index)!=null) return child_hinges.get(index);
    return null;
  }

  /**
   * Adds child to list of children
   * @param child  child to add
   */
  public void addChild(Gene child)
  {
    children.add(child);
  }

  /**
   * Removes child from list of children
   * @param child  child to remove
   */
  public void removeChild(Gene child)
  {
    children.remove(child);
    number_of_children--;
  }

  /**
   * Gets child gene by index
   * @param index  index of child to get
   * @return  child
   */
  public Gene getChild(int index)
  {
    if (children.get(index)!=null) return children.get(index);
    return null;
  }

  /**
   * Sets reference to parent gene
   * @param parent  reference to new parent gene
   */
  public void setParentRef(Gene parent)
  {
    this.parent_gene = parent;
  }

  /**
   * Gets reference to parent gene
   * @return  reference to parent gene
   */
  public Gene getParent()
  {
    return parent_gene;
  }

  /**
   * @return A deep clone (i.e. a value replica) of this Gene
   */
  public Gene deepClone()
  {
    Point3D newParent = parent_hinge == null? null :
        new Point3D(parent_hinge.getX(), parent_hinge.getY(), parent_hinge.getZ());
    //Point3D newParent =
    //      new Point3D(parent.getX(), parent.getY(), parent.getZ());

    LinkedList<Point3D> newChildHinges = new LinkedList<>();

    for (Point3D p : child_hinges) newChildHinges.add(new Point3D(p.getX(), p.getY(), p.getZ()));

    return new Gene(gene, newParent);
  }

  /**
   * Genes are equal if they have the same size and parent hinge
   * @param other
   * @return
   */
  @Override
  public boolean equals (Object other)
  {
    if (this == other) return true;
    Gene otherGene = (Gene) other;
    return gene.equals(otherGene.getGeneVector()) && number_of_children == otherGene.number_of_children && parent_hinge.equals(otherGene.parent_hinge);
  }
}
