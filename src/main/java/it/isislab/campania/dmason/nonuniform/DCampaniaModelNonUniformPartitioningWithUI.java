package it.isislab.campania.dmason.nonuniform;

import java.awt.Color;

import javax.swing.JFrame;

import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;

public class DCampaniaModelNonUniformPartitioningWithUI extends GUIState{

	public static String name;

	public static String topicPrefix = "";

	public DCampaniaModelNonUniformPartitioningWithUI(GeneralParam args,String topic) 
	{ 
		super(new DCampaniaModelNonUniformPartitioning(args,topic));
		topicPrefix=topic;
		name=String.valueOf(args.getI())+""+(String.valueOf(args.getJ()));
	}

	public static String getName() { return "Peer: <"+name+">"; }
	private Display2D display;
	private JFrame displayFrame;
	private ContinuousPortrayal2D popPortrayal = new ContinuousPortrayal2D();
	private GeomVectorFieldPortrayal campaniaPortrayal = new GeomVectorFieldPortrayal();
	private GeomVectorFieldPortrayal campania_stradePortrayal = new GeomVectorFieldPortrayal();

	public Object getSimulationInspectedObject() { return state; }  // non-volatile
	@Override
	public void init(Controller controller)
	{
		super.init(controller);

		display = new Display2D(DCampaniaModelNonUniformPartitioning.WIDTH, DCampaniaModelNonUniformPartitioning.HEIGHT, this);

		display.attach(campaniaPortrayal, "Countries", true);
		display.attach(campania_stradePortrayal, "Roads", true);
		display.attach(popPortrayal, "Population", true);

		displayFrame = display.createFrame();
		controller.registerFrame(displayFrame);
		displayFrame.setVisible(true);
	}
	@Override
	public void start()
	{
		super.start();

		setupPortrayals();
	}
	private void setupPortrayals()
	{
		DCampaniaModelNonUniformPartitioning world = (DCampaniaModelNonUniformPartitioning)state;

		campaniaPortrayal.setField(world.campaniadata.getCountry());
		popPortrayal.setField(world.population);
		campania_stradePortrayal.setField(world.campaniadata.getRoads());

		campaniaPortrayal.setPortrayalForAll(new GeomPortrayal(Color.BLACK,false));
		campania_stradePortrayal.setPortrayalForAll(new GeomPortrayal(Color.GRAY,true));
		display.reset();
		display.setBackdrop(Color.WHITE);
		display.repaint();
	}
	//	public static void main(String[] args)
	//	{
	//		DCampaniaModelWithUI worldGUI = null;
	//
	//		try
	//		{
	//			worldGUI = new DCampaniaModelWithUI();
	//		}
	//		catch (Exception ex)
	//		{
	//			Logger.getLogger(DCampaniaModelWithUI.class.getName()).log(Level.SEVERE, null, ex);
	//		}
	//
	//		Console console = new Console(worldGUI);
	//		console.setVisible(true);
	//	}
}
