package it.isislab.campania.dmason.uniform.agents;

import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.Serializable;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;

public abstract class DRemoteAgent<E>  extends SimplePortrayal2D implements Serializable, RemotePositionedAgent<E>
{
	private static final long serialVersionUID = 1L;
	public E pos;     // Location of agents  
	public String id; //id remote agent.An id uniquely identifies the agent in the distributed-field 
	
	public DRemoteAgent() {}

	/**
     * Constructor of Remote Agent
	 * @param state the Distributed State of simulation
	 */
    public DRemoteAgent(DistributedState<E> state){
			int i=state.nextId();
			this.id=state.getType().toString()+"-"+i;		
	}

    @Override
    public String toString() {
    	// TODO Auto-generated method stub
    	return "ID= "+id+" Pos: "+pos;
    }
    //getters and setters
	public E getPos() { return pos; }
	public void setPos(E pos) { this.pos = pos; }
	public String getId() {return id;	}
	public void setId(String id) {this.id = id;}	
    
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
	{
		double diamx = info.draw.width*1;
		double diamy = info.draw.height*1;

		graphics.setColor(Color.RED);
		graphics.fillOval((int)(info.draw.x-diamx/2),(int)(info.draw.y-diamy/2),(int)(diamx),(int)(diamy));
	}
}
