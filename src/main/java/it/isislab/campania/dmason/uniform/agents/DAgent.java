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
package it.isislab.campania.dmason.uniform.agents;



import java.awt.geom.Point2D;
import sim.engine.SimState;
import sim.util.Double2D;
import sim.util.geo.GeomPlanarGraphDirectedEdge;
import sim.util.geo.GeomPlanarGraphEdge;
import sim.util.geo.MasonGeometry;
import sim.util.geo.PointMoveTo;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.planargraph.DirectedEdgeStar;
import com.vividsolutions.jts.planargraph.Node;

import it.isislab.campania.dmason.uniform.DCampaniaModel;
import it.isislab.dmason.exception.DMasonException;



/**
 * 
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public class DAgent extends DAgentInfectionState 
{

	private static final long serialVersionUID = -1113018274619047013L;
	// point that denotes agent's position
	private MasonGeometry location;
	public Coordinate born_place;
	// The base speed of the agent.
	private double basemoveRate = 10.0;
	// How much to move the agent by in each step(); may become negative if
	// agent is moving from the end to the start of current line.
	private double moveRate = basemoveRate;
	// Used by agent to walk along line segment; assigned in setNewRoute()
	private LengthIndexedLine segment = null;
	double startIndex = 0.0; // start position of current line
	double endIndex = 0.0; // end position of current line
	double currentIndex = 0.0; // current location along line
	PointMoveTo pointMoveTo = new PointMoveTo();
	

	public DAgent(DCampaniaModel state, Coordinate born_place,LineString born_road)
	{
		super(state,born_place);

		location = new MasonGeometry(state.campaniadata.factoryGeometry.createPoint(born_place)); 
		this.born_place = born_place;
		//    	location = state.campaniadata.convertDouble2D2Geometry(new Double2D(500,500));
		location.isMovable = true;

		// Find the first line segment and set our position over the start coordinate.
//		
//		int walkway = state.random.nextInt(state.campaniadata.getRoads().getGeometries().numObjs);
//		MasonGeometry mg = (MasonGeometry) state.campaniadata.getRoads().getGeometries().objs[walkway];
//		setNewRoute((LineString) mg.getGeometry(), true,state);
		
//		Bag nearroads=state.campaniadata.getRoads().queryField(new Envelope(born_place.x-1,born_place.y-1,born_place.x+1,born_place.y+1));
//		
//	    int walkway = state.random.nextInt(nearroads.numObjs);
//		MasonGeometry mg = (MasonGeometry) nearroads.objs[walkway];
		setNewRoute(born_road, true,state);
		
		// Not everyone walks at the same speed
		basemoveRate = basemoveRate*Math.abs(state.random.nextGaussian());

	}

	/**
	 * @return geometry representing agent location
	 */
	public MasonGeometry getGeometry()
	{
		return location;
	}



	/** true if the agent has arrived at the target intersection
	 */
	private boolean arrived()
	{
		// If we have a negative move rate the agent is moving from the end to
		// the start, else the agent is moving in the opposite direction.
		if ((moveRate > 0 && currentIndex >= endIndex)
				|| (moveRate < 0 && currentIndex <= startIndex))
		{
			return true;
		}

		return false;
	}



	/** randomly selects an adjacent route to traverse
	 */
	private void findNewPath(DCampaniaModel DCampaniaModel)
	{
		
		// find all the adjacent junctions
		Node currentJunction = 
				DCampaniaModel.campaniadata.getNetwork().findNode(location.getGeometry().getCoordinate());
		
		if (currentJunction != null)
		{
			System.out.println("ok");
			DirectedEdgeStar directedEdgeStar = currentJunction.getOutEdges();
			Object[] edges = directedEdgeStar.getEdges().toArray();

			if (edges.length > 0)
			{
				// pick one randomly
				int i = DCampaniaModel.random.nextInt(edges.length);
				GeomPlanarGraphDirectedEdge directedEdge = (GeomPlanarGraphDirectedEdge) edges[i];
				GeomPlanarGraphEdge edge = (GeomPlanarGraphEdge) directedEdge.getEdge();

				// and start moving along it
				LineString newRoute = edge.getLine();
				Point startPoint = newRoute.getStartPoint();
				Point endPoint = newRoute.getEndPoint();

				if (startPoint.equals(location.geometry))
				{
					setNewRoute(newRoute, true,DCampaniaModel);
				} else
				{
					if (endPoint.equals(location.geometry))
					{
						setNewRoute(newRoute, false,DCampaniaModel);
					} else
					{
						System.err.println("Where am I?");
					}
				}
			}
		}
	}



	/**
	 * have the agent move along new route
	 *
	 * @param line defining new route
	 * @param start true if agent at start of line else agent placed at end
	 */
	private void setNewRoute(LineString line, boolean start,DCampaniaModel model)
	{
		segment = new LengthIndexedLine(line);
		startIndex = segment.getStartIndex();
		endIndex = segment.getEndIndex();

		Coordinate startCoord = null;

		if (start)
		{
			startCoord = segment.extractPoint(startIndex);
			currentIndex = startIndex;
			moveRate = basemoveRate; // ensure we move forward along segment
		} else
		{
			startCoord = segment.extractPoint(endIndex);
			currentIndex = endIndex;
			moveRate = -basemoveRate; // ensure we move backward along segment
		}

		moveTo(startCoord,model);
	}

	// move the agent to the given coordinates


	public void moveTo(Coordinate c,DCampaniaModel model)
	{
		pointMoveTo.setCoordinate(c);
		location.getGeometry().apply(pointMoveTo);
		getGeometry().geometry.geometryChanged();

		//        Double2D loc=model.campaniadata.convertGeometry2Double2D(new MasonGeometry(model.campaniadata.factoryGeometry.createPoint(c)));
		Point2D p=model.campaniadata.worldToScreenPointTransform(c.x,c.y);
		if(model.population.verifyPosition(new Double2D(p)))
			try {
				model.population.setDistributedObjectLocation(new Double2D(p),this,model);
			} catch (DMasonException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public void step(SimState state)
	{
		DCampaniaModel campState = (DCampaniaModel) state;
		
		
		
		Double2D mypos=new Double2D(campState.campaniadata.worldToScreenPointTransform(location.getGeometry().getCentroid().getX(), location.getGeometry().getCentroid().getY()));
		updateInfectionState();
		if(!switchToExposed(campState,campState.population.getNeighborsWithinDistance(mypos,campState.neighborhood)))
			if(!switchToInfected(campState))
				switchToRecovered(campState);
		
		move(campState);
	}

	/**
	 * moves the agent along the grid
	 *
	 * @param geoTest handle on the base SimState
	 *
	 * The agent will randomly select an adjacent junction and then move along
	 * the line segment to it. Then it will repeat.
	 */
	private void move(DCampaniaModel DCampaniaModel)
	{
		// if we're not at a junction move along the current segment
		if (!arrived())
		{
			moveAlongPath(DCampaniaModel);
		} else
		{
			findNewPath(DCampaniaModel);
		}
	}



	// move agent along current line segment
	private void moveAlongPath(DCampaniaModel model)
	{
		currentIndex += moveRate;

		// Truncate movement to end of line segment
		if (moveRate < 0)
		{ // moving from endIndex to startIndex
			if (currentIndex < startIndex)
			{
				currentIndex = startIndex;
			}
		} else
		{ // moving from startIndex to endIndex
			if (currentIndex > endIndex)
			{
				currentIndex = endIndex;
			}
		}

		Coordinate currentPos = segment.extractPoint(currentIndex);


		moveTo(currentPos,model);
	}


	


}
