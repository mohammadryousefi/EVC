package vcreature.mainSimulation;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.texture.Texture;
import vcreature.Genome.Genotype;
import vcreature.Genome.Population;
import vcreature.phenotype.Block;
import vcreature.phenotype.PhysicsConstants;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class MainSim extends SimpleApplication implements ActionListener
{
  private static InformationFrame infoFrame;
  private static int numCreatures = 0;
  private static int populationSize = 0;
  private static boolean overWriteStats;
  private static boolean genGraph;
  public PriorityQueue<Genotype> orgGenotypes;
  public PriorityQueue<Genotype> fitGenotypes;// = new PriorityQueue<>();
  public ArrayList<MakeCreature> fitCreatures = new ArrayList<>();
  public ArrayList<Float> fitnesses = new ArrayList<>();
  public int deletedCreatures = 0;
  private float maxFitness;
  private BulletAppState bulletAppState;
  private PhysicsSpace physicsSpace;
  private float cameraAngle = (float) (Math.PI / 2.0);
  private float elapsedSimulationTime = 0.0f;
  private float creatureSimulationTime = 0.0f;
  private Vector3f tmpVec3;
  private boolean isCameraRotating = true;
  private boolean hillClimb = false;
  private Population population;
  private Genotype genotype;
  private StatsThread outStats;
  private static String fileName;
  private static Population myPopulation;


  public static void main(String[] args)
  {
    //Main jumping creature view
    AppSettings settings = new AppSettings(true);
    settings.setResolution(800, 600);
    settings
        .setSamples(4);

    MainSim mainApp = new MainSim();
    settings.setVSync(true);
    settings.setFrequency((int) mainApp.speed * 60);//Frames per second
    settings.setTitle("Solicitous Super Fragile Acidic Pixel Creature");

    mainApp.setShowSettings(false);
    mainApp.setPauseOnLostFocus(false);
    mainApp.setSettings(settings);

    /* Sets up user arguments */
    boolean headless = false;
    overWriteStats = false;
    genGraph = false;

    if (args.length > 0)
    {
      for (int i = 0; i < args.length; i++)
      {
        headless = headless | args[i].toLowerCase().equals("-headless");
        overWriteStats =
            overWriteStats | args[i].toLowerCase().equals("-nokeep");
        genGraph = genGraph | args[i].toLowerCase().equals("-graph");


      }
    }

    if(headless)
    {
      mainApp.start(JmeContext.Type.Headless);
    }
    else
    {
      mainApp.start();
      CreatureInfoWindow();
    }
  }


  /**
   * Window that shows current stats on creature
   */
  private static void CreatureInfoWindow()
  {
    infoFrame = new InformationFrame();
  }


  /**
   * Updates labels on GUI every simpleUpdate
   */
  private void updateLabels()
  {
    if (infoFrame == null) return;
    if (!fitCreatures.isEmpty())
    {
      float fitness = fitCreatures.get(0).getFitness();
      if (fitness > maxFitness) maxFitness = fitness;
      infoFrame.setFitness(fitness);
      infoFrame.setChangePerMinute(fitness / elapsedSimulationTime);
      infoFrame.setTotalFitness(fitness);
      infoFrame.setTotalPopulation(populationSize);
      infoFrame.setCurrentCreature(numCreatures);
      infoFrame.setGeneration(population.getGeneration());
    }
  }

  @Override
  public void simpleInitApp()
  {
    gainFocus();
    /**
     * Set up Physics
     */
    bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState);
    physicsSpace = bulletAppState.getPhysicsSpace();
    //bulletAppState.setDebugEnabled(true);

    this.speed = 5;
    physicsSpace.setGravity(PhysicsConstants.GRAVITY);
    physicsSpace.setAccuracy(PhysicsConstants.PHYSICS_UPDATE_RATE);
    physicsSpace.setMaxSubSteps((int) speed * 4);


    //Set up inmovable floor
    Box floor = new Box(50f, 0.1f, 50f);
    Material floor_mat =
        new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    Texture floorTexture = assetManager.loadTexture("Textures/FloorTile.png");

    floorTexture.setWrap(Texture.WrapMode.Repeat);
    floor_mat.setTexture("ColorMap", floorTexture);

    floor.scaleTextureCoordinates(new Vector2f(50, 50));
    Geometry floor_geo = new Geometry("Floor", floor);
    floor_geo.setMaterial(floor_mat);
    floor_geo.setShadowMode(ShadowMode.Receive);
    floor_geo.setLocalTranslation(0, -0.11f, 0);
    rootNode.attachChild(floor_geo);

    /* Make the floor physical with mass 0.0f */
    RigidBodyControl floor_phy = new RigidBodyControl(0.0f);
    floor_geo.addControl(floor_phy);
    physicsSpace.add(floor_phy);
    floor_phy.setFriction(PhysicsConstants.GROUND_SLIDING_FRICTION);
    floor_phy.setRestitution(PhysicsConstants.GROUND_BOUNCINESS);
    floor_phy.setDamping(PhysicsConstants.GROUND_LINEAR_DAMPINING,
        PhysicsConstants.GROUND_ANGULAR_DAMPINING);


    Block.initStaticMaterials(assetManager);


    /* Begins creating population and creature making */
    population = new Population(500); //Makes PQ of genotypes

    fitGenotypes = population.getModifiedCreatures();
    populationSize = population.getPopulation().size();

    System.out.println("population: " + populationSize);

    genotype = population.getPopulation().poll(); //Gets first genotype

    MakeCreature creature = new MakeCreature(physicsSpace, rootNode, genotype);
    numCreatures++;
    creature.placeOnGround();
    fitCreatures.add(creature);

    /* Begins threads for saving data */
    outStats = new StatsThread();
    outStats.start();

    initLighting();
    initKeys();

    flyCam.setDragToRotate(true);

  }

  /**
   * Called after everything in the population has been tested.
   */
  private void reinitPopulation()
  {
    populationSize = population.getPopulation().size();
    deletedCreatures = 0;
    creatureSimulationTime = 0;

    System.out.println("population: " + populationSize);

    genotype = population.getPopulation().poll();

    //System.out.println(genotype.getRoot().getGeneVector());

    MakeCreature creature = new MakeCreature(physicsSpace, rootNode, genotype);
    numCreatures = 1;
    creature.placeOnGround();
    fitCreatures.clear();
    fitCreatures.add(creature);

  }


  private void initLighting()
  {
    //  ust add a light to make the lit object visible!
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(0, -10, -2).normalizeLocal());
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun);

    //Without ambient light, the seen looks like outerspace with razer sharp
    // black shadows.
    AmbientLight ambient = new AmbientLight();
    ambient.setColor(ColorRGBA.White.mult(0.3f));
    rootNode.addLight(ambient);

    // SHADOW
    // the second parameter is the resolution. Experiment with it! (Must be a
    // power of 2)
    DirectionalLightShadowRenderer dlsr =
        new DirectionalLightShadowRenderer(assetManager, 2048, 2);
    dlsr.setLight(sun);
    viewPort.addProcessor(dlsr);
  }

  private void initKeys()
  {
    inputManager.addMapping("Quit", new KeyTrigger(KeyInput.KEY_Q));
    inputManager
        .addMapping("Toggle Camera Rotation", new KeyTrigger(KeyInput.KEY_P));
    inputManager.addMapping("Change Creature", new KeyTrigger(KeyInput.KEY_C));

    // Add the names to the action listener.
    inputManager.addListener(this, "Quit");
    inputManager.addListener(this, "Toggle Camera Rotation");
    inputManager.addListener(this, "Change Creature");
  }

  public void onAction(String name, boolean isPressed, float timePerFrame)
  {
    if (isPressed && name.equals("Toggle Camera Rotation"))
    {
      isCameraRotating = !isCameraRotating;
    }

    else if(isPressed && name.equals("Quit"))
    {
      System.out.format(
          "Creature Fitness (Maximium height of lowest point) = %.3f meters]\n",
          fitCreatures.get(0).getFitness());
      System.exit(0);
    }
  }

  /* Use the main event loop to trigger repeating actions. */
  @Override
  public void simpleUpdate(float deltaSeconds)
  {
    gainFocus();
    elapsedSimulationTime += deltaSeconds;
    creatureSimulationTime += deltaSeconds;

    //Makes sure the amount of creatures already made isn't more than actual population
    if (deletedCreatures < populationSize)
    {
      updateLabels();

      if (!fitCreatures.isEmpty())
      {
        //Wait 7 seconds to update brain and fire neurons
        if (creatureSimulationTime >= 7.0f)
        {
          fitCreatures.get(0).updateBrain(creatureSimulationTime);
        }

        //Wait 15 seconds to get the fitness and move on to next creature
        if (creatureSimulationTime >= 15f) /*  done testing a creature */
        {
          float fitness = fitCreatures.get(0).getFitness();
          fitCreatures.get(0).genotype.setFitness(fitness);
          fitGenotypes.add(genotype);
          fitnesses.add(fitness);
          System.out.println("  fitness: " + fitness % 2f + "\n");

          fitCreatures.get(0).remove(); //removes from physics
          fitCreatures.remove(0); //removes from array
          deletedCreatures++;

          creatureSimulationTime = 0;
        }
      }

      //If physics space is now available, make new creature
      if (fitCreatures.isEmpty() && deletedCreatures < populationSize)
      {
        genotype = population.getPopulation().poll();
        MakeCreature creature =
            new MakeCreature(physicsSpace, rootNode, genotype);
        numCreatures++;
        creature.placeOnGround();
        fitCreatures.add(creature);

      }
    }
    else //All creatures in population have been made
    {
      System.out.println("Tested all current genotypes");
      System.out.println(fitnesses);
      System.out.println("total creatures: " + numCreatures*population.getGeneration());

      //Once generation has reached a certain number, quit the simulation
      if(population.getGeneration() == 6)
      {
        outStats.updateStats();
        if (genGraph)
        {
          ProcessBuilder pb =
              new ProcessBuilder("nohup", "matlab", "-nosplash", "-nodisplay",
                  "<", "plotStats.m", ">", "output.txt");
          try
          {
            Process proc = pb.start();
          }
          catch (IOException e)
          {
            e.printStackTrace();
          }
        }
        System.exit(0);
      }
        population.returnPopulation(fitGenotypes);
        reinitPopulation();

    }


    if (isCameraRotating)
    {
      //Move camera continously in circle of radius 25 meters centered 10 meters
      //  above the origin.
      cameraAngle +=
          deltaSeconds * 2.0 * Math.PI / 60.0; //rotate full circle every minute
      float x = (float) (25.0 * Math.cos(cameraAngle));
      float z = (float) (25.0 * Math.sin(cameraAngle));

      tmpVec3 = new Vector3f(x, 10.0f, z);
      cam.setLocation(tmpVec3);
      cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }
  }

  private void print(String msg, float x)
  {
    String className = this.getClass().getSimpleName();
    System.out.format("%s.%s %.3f\n", className, msg, x);
  }

  private void print(String msg, Vector3f vector)
  {
    String className = this.getClass().getSimpleName();
    System.out.format("%s.%s [%.3f, %.3f, %.3f]\n", className, msg, vector.x,
        vector.y, vector.z);
  }

  /**
   * Private inner class is used to save the data and fitnesses over a certain
   * amount of elapsed time.
   */
  private class StatsThread extends Thread
  {
    private int i;
    private int sizeFit;
    private float avgFit;
    private float curFit;
    private float maxFit;
    private FileWriter statsOut;

    private long timeSinceOutput;
    private long lastSave;
    private long deltaTime;

    public StatsThread()
    {
      timeSinceOutput = System.nanoTime();
      if (overWriteStats)
      {
        try
        {
          statsOut = new FileWriter("statsOut.txt", false);
        } catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    }

    public void updateStats()
    {
      avgFit = 0f;
      maxFit = 0f;
      sizeFit = fitnesses.size();
      for (i = 0; i < sizeFit; i++)
      {
        curFit = fitnesses.get(i);
        avgFit += curFit;
        maxFit = (maxFit > curFit ? maxFit : curFit);
      }
      avgFit = avgFit / sizeFit;

      try
      {
        statsOut = new FileWriter("statsOut.txt", true);
        statsOut.write(String
            .format("%f\t%f\t%f\t%d\n", elapsedSimulationTime, maxFit, avgFit, population.getGeneration()));
        statsOut.flush();
        statsOut.close();
      } catch (IOException e)
      {
        e.printStackTrace();
      }
    }

    public void run()
    {
      while (true)
      {
        deltaTime = System.nanoTime() - timeSinceOutput;
        if (deltaTime > 12e9)
        {
          timeSinceOutput = System.nanoTime();
          updateStats();
          //save simulation condition
        }
        else
        {
          try
          {
            sleep(16);
          } catch (InterruptedException e)
          {
            e.printStackTrace();
          }
        }
      }
    }
  }
}
