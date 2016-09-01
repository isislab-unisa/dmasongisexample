package it.isislab.campania.dmason.nonuniform.agents;

import java.awt.Color;
import java.awt.Graphics2D;
import com.vividsolutions.jts.geom.Coordinate;
import it.isislab.campania.dmason.nonuniform.DCampaniaModelNonUniformPartitioning;
import sim.portrayal.DrawInfo2D;
import sim.util.Bag;
import sim.util.Double2D;

public abstract class DAgentInfectionStateNonUniformPartitioning extends DRemoteAgentNonUniformPartitioning<Double2D>{
	
	private static final long serialVersionUID = 1L;

	private static double DIAMETER=1;

	private int EXPOSED_PERIOD=0;
	private int INFECTED_PERIOD=0;

	private int EXPOSED_PERDIOD_EXP_TIME=0;

	protected Color STATE = STATE_susceptible;

	protected static final Color STATE_susceptible = Color.YELLOW;
	protected static final Color STATE_exposed = Color.ORANGE;
	protected static final Color STATE_infected = Color.RED;
	protected static final Color STATE_recovered = Color.GREEN;

	
	
	public DAgentInfectionStateNonUniformPartitioning(DCampaniaModelNonUniformPartitioning state, Coordinate born_place)
	{
		super(state);
		if(state.random.nextDouble() < state.initial_infected_ratio)
		{
			STATE=STATE_infected;
			INFECTED_PERIOD=state.random.nextInt(state.initial_infected_period);
		}
	}
	
	public void updateInfectionState()
	{
		if(STATE==STATE_exposed)
			EXPOSED_PERIOD++;
		else
			if(STATE==STATE_infected)
				INFECTED_PERIOD++;
	}
	public boolean switchToInfected(DCampaniaModelNonUniformPartitioning state)
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

	public boolean switchToExposed(DCampaniaModelNonUniformPartitioning state,Bag neighbors)
	{
		if(STATE==STATE_susceptible)
		{
			
			for(Object obj:neighbors)
			{
				DAgentInfectionStateNonUniformPartitioning ng=(DAgentInfectionStateNonUniformPartitioning)obj;
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
	private boolean switchToExposed(DCampaniaModelNonUniformPartitioning state)
	{
		STATE=STATE_exposed;
		EXPOSED_PERIOD=1;
		EXPOSED_PERDIOD_EXP_TIME=(int) (state.random.nextDouble()*state.max_exposed_time)+1;
		INFECTED_PERIOD=0;
		return true;
	}

	public boolean switchToRecovered(DCampaniaModelNonUniformPartitioning state)
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



	public Color getSTATE() {
		return STATE;
	}
	public void setSTATE(Color sTATE) {
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
