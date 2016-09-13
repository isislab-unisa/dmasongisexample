/**
 * Copyright 2016 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package it.isislab.campania.dmason.nonuniform.agents;

import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import sim.portrayal.DrawInfo2D;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.Serializable;
/**
 * 
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public abstract class DRemoteAgentNonUniformPartitioning<E> implements Serializable, RemotePositionedAgent<E> {

	private static final long serialVersionUID = 1L;
	public E pos;     // Location of agents  
	public String id; //id remote agent.An id uniquely identifies the agent in the distributed-field 
	
	public DRemoteAgentNonUniformPartitioning() {}

	/**
     * Constructor of Remote Agent
	 * @param state the Distributed State of simulation
	 */
    public DRemoteAgentNonUniformPartitioning(DistributedState<E> state){
			int i=state.nextId();
			this.id=state.getType().toString()+"-"+i;		
	}

    //getters and setters
	public E getPos() { return pos; }
	public void setPos(E pos) { this.pos = pos; }
	public String getId() {return id;	}
	public void setId(String id) {this.id = id;}	
    
    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DRemoteAgentNonUniformPartitioning other = (DRemoteAgentNonUniformPartitioning) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (pos == null) {
			if (other.pos != null)
				return false;
		} else if (!pos.equals(other.pos))
			return false;
		return true;
	}	
    
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
   	{
   		double diamx = info.draw.width*1;
   		double diamy = info.draw.height*1;

   		graphics.setColor(Color.RED);
   		graphics.fillOval((int)(info.draw.x-diamx/2),(int)(info.draw.y-diamy/2),(int)(diamx),(int)(diamy));
   	}
    @Override
    public String toString() {
    	return "ID= "+id+" Pos: "+pos;
    }
}