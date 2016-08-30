package it.isislab.campania.mason;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import com.vividsolutions.jts.io.ParseException;

public class CampaniaModelWithUI extends GUIState
{

	public CampaniaModelWithUI(SimState state)
	{
		super(state);
	}

	public CampaniaModelWithUI() throws ParseException
	{
		super(new CampaniaModel(System.currentTimeMillis()));
	}
	private Display2D display;
	private JFrame displayFrame;
	private ContinuousPortrayal2D popPortrayal = new ContinuousPortrayal2D();
	private GeomVectorFieldPortrayal campaniaPortrayal = new GeomVectorFieldPortrayal();
	private GeomVectorFieldPortrayal campania_stradePortrayal = new GeomVectorFieldPortrayal();
	 FastValueGridPortrayal2D homePheromonePortrayal = new FastValueGridPortrayal2D("Non uniform split");
	public Object getSimulationInspectedObject() { return state; }  // non-volatile
	@Override
	public void init(Controller controller)
	{
		super.init(controller);

		display = new Display2D(CampaniaModel.WIDTH, CampaniaModel.HEIGHT, this);

		display.attach(campaniaPortrayal, "Countries", true);
		display.attach(campania_stradePortrayal, "Roads", true);
		display.attach(popPortrayal, "Population", true);
		display.attach(homePheromonePortrayal, "Split", true);
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
		CampaniaModel world = (CampaniaModel)state;

		campaniaPortrayal.setField(world.campaniadata.getCountry());
		popPortrayal.setField(world.population);	
		campania_stradePortrayal.setField(world.campaniadata.getRoads());
		campaniaPortrayal.setPortrayalForAll(new GeomPortrayal(Color.BLACK,false));
		campania_stradePortrayal.setPortrayalForAll(new GeomPortrayal(Color.GRAY,true));
		
		  homePheromonePortrayal.setField(world.nonuniformgrid);
		  
//		  Color[] colors = new Color[128];  
//	        colors[0] = new Color(0,0,0,0);
//	        for(int i = 1 ; i < colors.length; i++)
//	            { colors[i] = new Color(guirandom.nextInt(255), guirandom.nextInt(255), guirandom.nextInt(255)); }
//	                
//	        homePheromonePortrayal.setMap(new sim.util.gui.SimpleColorMap(colors));
	        homePheromonePortrayal.setMap(new sim.util.gui.SimpleColorMap(
	                0,
	                1,
	                // home pheromones are beneath all, just make them opaque
	                new Color(1f,0f,0f,0f ),
	                new Color(0,255,0,255) )
	            { public double filterLevel(double level) { return Math.sqrt(Math.sqrt(level)); } } );  // map with custom level filtering
		
		display.reset();
		display.setBackdrop(Color.WHITE);
		display.repaint();
	}
	public static void main(String[] args)
	{
		CampaniaModelWithUI worldGUI = null;

		try
		{
			worldGUI = new CampaniaModelWithUI();
		}
		catch (Exception ex)
		{
			Logger.getLogger(CampaniaModelWithUI.class.getName()).log(Level.SEVERE, null, ex);
		}

		Console console = new Console(worldGUI);
		console.setVisible(true);
	}
}
