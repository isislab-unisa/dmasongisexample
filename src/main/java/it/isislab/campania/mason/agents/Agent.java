package it.isislab.campania.mason.agents;

import java.awt.geom.Point2D;
import java.util.HashMap;
import sim.engine.SimState;
import sim.engine.Steppable;
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
import it.isislab.campania.gis.GISCampaniaPendolo;
import it.isislab.campania.mason.CampaniaModel;


public class Agent extends AgentInfectionState implements Steppable
{

	private static final long serialVersionUID = -1113018274619047013L;
	// point that denotes agent's position
	public MasonGeometry location;
	public Coordinate born_place;
	// The base speed of the agent.
	private double basemoveRate = 10.0;
	// How much to move the agent by in each step(); may become negative if
	// agent is moving from the end to the start of current line.
	private double moveRate = basemoveRate;
	// Used by agent to walk along line segment; assigned in setNewRoute()
	private LengthIndexedLine segment = null;
	private double startIndex = 0.0; // start position of current line
	private double endIndex = 0.0; // end position of current line
	private double currentIndex = 0.0; // current location along line
	private PointMoveTo pointMoveTo = new PointMoveTo();
	

	
	public Double2D fakepos;
	

	public Agent(CampaniaModel state, Coordinate born_place,double diameter,Integer istat_palce)
	{
		super(state,born_place,diameter);
		if(born_place==null)return;

		location = new MasonGeometry(state.campaniadata.factoryGeometry.createPoint(born_place)); // magic numbers
		born_place = this.born_place;
		//    	location = state.campaniadata.convertDouble2D2Geometry(new Double2D(500,500));
		location.isMovable = true;

		// Find the first line segment and set our position over the start coordinate.
		int walkway = state.random.nextInt(state.campaniadata.getRoads().getGeometries().numObjs);
		
//		MasonGeometry mg = (MasonGeometry) state.campaniadata.getRoads().getGeometries().objs[walkway];
		MasonGeometry mg=null;
		if(state.campaniadata.nearRoad2codISTAT.get(istat_palce).size()!=0)
		mg = state.campaniadata.nearRoad2codISTAT.get(istat_palce).get(state.random.nextInt(state.campaniadata.nearRoad2codISTAT.get(istat_palce).size()));
		
		if(mg==null)
			mg = (MasonGeometry) state.campaniadata.getRoads().getGeometries().objs[walkway];
		setNewRoute((LineString) mg.getGeometry(), true,state);


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
	private void findNewPath(CampaniaModel campaniamodel)
	{
		// find all the adjacent junctions
		Node currentJunction = 
				campaniamodel.campaniadata.getNetwork().findNode(location.getGeometry().getCoordinate());

		if (currentJunction != null)
		{
			DirectedEdgeStar directedEdgeStar = currentJunction.getOutEdges();
			Object[] edges = directedEdgeStar.getEdges().toArray();

			if (edges.length > 0)
			{
				// pick one randomly
				int i = campaniamodel.random.nextInt(edges.length);
				GeomPlanarGraphDirectedEdge directedEdge = (GeomPlanarGraphDirectedEdge) edges[i];
				GeomPlanarGraphEdge edge = (GeomPlanarGraphEdge) directedEdge.getEdge();

				// and start moving along it
				LineString newRoute = edge.getLine();
				Point startPoint = newRoute.getStartPoint();
				Point endPoint = newRoute.getEndPoint();

				if (startPoint.equals(location.geometry))
				{
					setNewRoute(newRoute, true,campaniamodel);
				} else
				{
					if (endPoint.equals(location.geometry))
					{
						setNewRoute(newRoute, false,campaniamodel);
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
	private void setNewRoute(LineString line, boolean start,CampaniaModel model)
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


	public void moveTo(Coordinate c,CampaniaModel model)
	{
		pointMoveTo.setCoordinate(c);
		location.getGeometry().apply(pointMoveTo);
		getGeometry().geometry.geometryChanged();

		//        Double2D loc=model.campaniadata.convertGeometry2Double2D(new MasonGeometry(model.campaniadata.factoryGeometry.createPoint(c)));
		Point2D p=model.campaniadata.worldToScreenPointTransform(c.x,c.y);


		model.population.setObjectLocation(this, new Double2D(p));

	}

	public void step(SimState state)
	{
		CampaniaModel campState = (CampaniaModel) state;
		
		
		
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
	private void move(CampaniaModel campaniamodel)
	{
		// if we're not at a junction move along the current segment
		if (!arrived())
		{
			moveAlongPath(campaniamodel);
		} else
		{
			findNewPath(campaniamodel);
		}
	}



	// move agent along current line segment
	private void moveAlongPath(CampaniaModel model)
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
	 String start_location;
	 String goal_location;
	 Integer sex; //1,2
	 String movement_reason;//1,2
	 String meansTransport;//01treno,02tram,03,....,12
	 String work_day_start;//1,2,3,4
	 String travel_time;//1,2,3,4
	public void setISTATData(GISCampaniaPendolo pdata,String namelocation,  HashMap<Integer,String> codISTATNames) {
		// TODO Auto-generated method stub
//		this.data_istat=pdata;
	
		sex=pdata.getSesso();
		movement_reason=pdata.getMotivoSpostamento();
		meansTransport=pdata.getMezzo();
		work_day_start=pdata.getOrario_uscita();
		travel_time=pdata.getTempo();
		
		start_location=namelocation;//codISTATNames.get(Integer.parseInt(pdata.getCodISTAT()));
		goal_location=pdata.getDest_loc();//codISTATNames.get(Integer.parseInt(pdata.getCodIstatdest()));
		
	}

	public String getStart_location() {
		return start_location;
	}

	public void setStart_location(String start_location) {
		this.start_location = start_location;
	}

	public String getGoal_location() {
		return goal_location;
	}

	public void setGoal_location(String goal_location) {
		this.goal_location = goal_location;
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public String getMovement_reason() {
		return movement_reason;
	}

	public void setMovement_reason(String movement_reason) {
		this.movement_reason = movement_reason;
	}

	public String getMeansTransport() {
		return meansTransport;
	}

	public void setMeansTransport(String meansTransport) {
		this.meansTransport = meansTransport;
	}

	public String getWork_day_start() {
		return work_day_start;
	}

	public void setWork_day_start(String work_day_start) {
		this.work_day_start = work_day_start;
	}

	public String getTravel_time() {
		return travel_time;
	}

	public void setTravel_time(String travel_time) {
		this.travel_time = travel_time;
	}

	


	


}
