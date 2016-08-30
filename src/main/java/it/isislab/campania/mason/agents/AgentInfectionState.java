package it.isislab.campania.mason.agents;

import java.awt.Color;
import java.awt.Graphics2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Bag;
import com.vividsolutions.jts.geom.Coordinate;
import it.isislab.campania.mason.CampaniaModel;


public class AgentInfectionState extends OvalPortrayal2D{

	private static final long serialVersionUID = 1L;

	private static double DIAMETER=2;

	private int EXPOSED_PERIOD=0;
	private int INFECTED_PERIOD=0;

	private int EXPOSED_PERDIOD_EXP_TIME=0;

	protected Color STATE = STATE_susceptible;

	protected static final Color STATE_susceptible = Color.YELLOW;
	protected static final Color STATE_exposed = Color.ORANGE;
	protected static final Color STATE_infected = Color.RED;
	protected static final Color STATE_recovered = Color.GREEN;

	public void updateInfectionState()
	{
		if(STATE==STATE_exposed)
			EXPOSED_PERIOD++;
		else
			if(STATE==STATE_infected)
				INFECTED_PERIOD++;
	}
	public boolean switchToInfected(CampaniaModel state)
	{
		if(STATE==STATE_exposed && EXPOSED_PERIOD >= EXPOSED_PERDIOD_EXP_TIME )
		{
			STATE=STATE_infected;
			EXPOSED_PERIOD=0;
			INFECTED_PERIOD=1;
			return true;
		}else

			return false;

	}

	public boolean switchToExposed(CampaniaModel state,Bag neighbors)
	{
		if(STATE==STATE_susceptible)
		{
			
			for(Object obj:neighbors)
			{
				AgentInfectionState ng=(AgentInfectionState)obj;
				if(ng.getSTATE()==STATE_infected)
				{
					if(state.random.nextDouble() < state.infetion_probability)
					{
						return switchToExposed(state);
						
					}
				}
			}
		}
		return false;
	}
	private boolean switchToExposed(CampaniaModel state)
	{
		STATE=STATE_exposed;
		EXPOSED_PERIOD=1;
		EXPOSED_PERDIOD_EXP_TIME=(int) (state.random.nextDouble()*state.max_exposed_time)+1;
		INFECTED_PERIOD=0;
		return true;
	}

	public boolean switchToRecovered(CampaniaModel state)
	{
		if(STATE==STATE_infected && state.random.nextDouble()< (INFECTED_PERIOD<state.max_infected_time?(1/(INFECTED_PERIOD+1)):1) )
		{
			STATE=STATE_recovered;
			EXPOSED_PERIOD=0;
			INFECTED_PERIOD=0;
			return true;
		}else 
			return false;
	}

	public AgentInfectionState(CampaniaModel state, Coordinate born_place,double diameter)
	{
		super();
		DIAMETER=diameter;
		if(state.random.nextDouble() < state.initial_infected_ratio)
		{
			STATE=STATE_infected;
			INFECTED_PERIOD=state.random.nextInt(state.initial_infected_period);
		}
	}

	private Color getSTATE() {
		return STATE;
	}
	private void setSTATE(Color sTATE) {
		STATE = sTATE;
	}
	public final void draw(Object object, Graphics2D graphics, DrawInfo2D info)
	{
		double diamx = info.draw.width*DIAMETER;
		double diamy = info.draw.height*DIAMETER;

		graphics.setColor(STATE);
		graphics.fillOval((int)(info.draw.x-diamx/2),(int)(info.draw.y-diamy/2),(int)(diamx),(int)(diamy));
	}
}
