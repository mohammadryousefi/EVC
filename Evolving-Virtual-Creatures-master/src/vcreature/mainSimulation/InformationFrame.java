package vcreature.mainSimulation;

import javafx.scene.control.CheckBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static javax.swing.Box.createHorizontalGlue;

/**
 * Makes gui component to view creature information and statistics.
 */
public class InformationFrame extends JFrame
{
  private static InformationFrame activeFrame;
  private float currentValue;
  private float changePerMinuteValue;
  private float totalValue;
  private int generationValue;
  private final JLabel fitness = new JLabel();
  private final JLabel fitnessChange = new JLabel();
  private final JLabel totalFitness = new JLabel();
  private final JLabel populationTotal = new JLabel();
  private final JLabel currentCreature = new JLabel();
  private final JLabel currentGeneration = new JLabel();
  private final JLabel mutate_crossover = new JLabel("Testing creatures...");
  private final Font font = new Font("Arial", Font.BOLD, 16);
  public static boolean hillClimb = false;
  private int totalPopulation;
  private int creatureIndex;
  public static boolean startCrossover = false;



  public InformationFrame()
  {
    super("Information");
activeFrame = this;
    //Set all label values to zero initially
    setTotalPopulation(0);
    setCurrentCreature(0);
    setGeneration(1);
    setFitness(0);
    setChangePerMinute(0);
    setTotalFitness(0);
    setMutate_crossover(hillClimb,startCrossover);


    final JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    final JPanel leftPanelContainer = new JPanel();
    final JPanel leftPanel = new JPanel();
    final JPanel rightPanelContainer = new JPanel();
    final JPanel rightPanel = new JPanel();

    leftPanelContainer
        .setLayout(new BoxLayout(leftPanelContainer, BoxLayout.X_AXIS));
    leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
    rightPanelContainer
        .setLayout(new BoxLayout(rightPanelContainer, BoxLayout.X_AXIS));
    rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

    //Add labels to gui
    leftPanel.add(Box.createRigidArea(new Dimension(0,20)));
    leftPanel.add(populationTotal);
    leftPanel.add(Box.createRigidArea(new Dimension(0,20)));
    leftPanel.add(currentCreature);
    leftPanel.add(Box.createRigidArea(new Dimension(0,20)));
    leftPanel.add(currentGeneration);
    leftPanel.add(Box.createRigidArea(new Dimension(0,20)));
    leftPanel.add(fitness);
    leftPanel.add(Box.createRigidArea(new Dimension(0,20)));
    leftPanel.add(fitnessChange);
    leftPanel.add(Box.createRigidArea(new Dimension(0,20)));
    leftPanel.add(totalFitness);
    leftPanel.add(Box.createRigidArea(new Dimension(0,20)));
    leftPanel.add(mutate_crossover);


    leftPanel.add(Box.createRigidArea(new Dimension(0,20)));

    leftPanelContainer.add(createHorizontalGlue());
    leftPanelContainer.add(leftPanel);
    leftPanelContainer.add(createHorizontalGlue());


    rightPanelContainer.add(createHorizontalGlue());
    rightPanelContainer.add(rightPanel);
    rightPanelContainer.add(createHorizontalGlue());

    content.add(leftPanelContainer);
    content.add(rightPanelContainer);

    add(content);
    setPreferredSize(new Dimension(300, 320));
    pack();
    setResizable(false);
    setVisible(true);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


  }


  public void setFitness(float value)
  {
    this.currentValue = value;
    fitness.setText(String.format("Current Fitness: %.2f", currentValue));
  }

  public void setChangePerMinute(float value)
  {
    this.changePerMinuteValue = value;
    fitnessChange
        .setText(String.format("Fitness/Min: %.2f", changePerMinuteValue));
  }

  public void setTotalFitness(float value)
  {
    this.totalValue = value;
    totalFitness.setText(String.format("Total Fitness: %.2f", totalValue));
  }

  public void setGeneration(int value)
  {
    this.generationValue = value;
    currentGeneration.setText(String.format("Current Generation: %d", generationValue));
  }

  public static void main(String[] args)
  {
    InformationFrame x = new InformationFrame();
    float i = 0;
    while (true)
    {
      i++;
      x.setFitness(i);
      System.out.println(i);
    }
  }


  public void setTotalPopulation(int totalPopulation)
  {
    this.totalPopulation = totalPopulation;
    populationTotal.setText("Population: " + totalPopulation);
  }

  public void setCurrentCreature(int creatureIndex)
  {
    this.creatureIndex = creatureIndex;
    currentCreature.setText("Creature: " + creatureIndex);
  }

  public void setMutate_crossover(boolean hillClimb, boolean startCrossover)
  {
    if(hillClimb)
    {
      mutate_crossover.setText("Mutating generation...");
    }
    else if(startCrossover)
    {
      mutate_crossover.setText("Doing crossover...");
    }
    repaint();
  }

  public static InformationFrame getActiveFrame()
  {
    return activeFrame;
  }
}
