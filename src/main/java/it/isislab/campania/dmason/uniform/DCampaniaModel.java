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
package it.isislab.campania.dmason.uniform;

import it.isislab.campania.dmason.uniform.agents.DAgent;
import it.isislab.campania.gis.GISCampaniaData;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DFactory;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import sim.engine.SimState;
import sim.util.Double2D;
import sim.util.geo.MasonGeometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;


public class DCampaniaModel extends DistributedState<Double2D>
{
	/*SEIR MODEL*/
	public double infetion_probability=0.1;
	public final double population_rate=0.5;
	public double neighborhood = 1;
	public int max_exposed_time=10;
	public int max_infected_time=20;

	public double initial_infected_ratio=0.1;
	public int initial_infected_period=10;

	public int region_density=10;
	//NOW ARE NOT GLOBAL VAR
	/**
	public int getMax_exposed_time() {
		return max_exposed_time;
	}
	public void setMax_exposed_time(int max_exposed_time) {
		this.max_exposed_time = max_exposed_time;
	}
	public double getNeighborhood() {
		return neighborhood;
	}
	public void setNeighborhood(double neighborhood) {
		this.neighborhood = neighborhood;
	}
	public double getInfetion_probability() {
		return infetion_probability;
	}
	public int getRegion_density() {
		return region_density;
	}
	public void setRegion_density(int region_density) {
		this.region_density = region_density;
	}
	public int getMax_infected_time() {
		return max_infected_time;
	}
	public void setMax_infected_time(int max_infected_time) {
		this.max_infected_time = max_infected_time;
	}
	public int getInitial_infected_period() {
		return initial_infected_period;
	}
	public void setInitial_infected_period(int initial_infected_period) {
		this.initial_infected_period = initial_infected_period;
	}
	public double getInitial_infected_ratio() {
		return initial_infected_ratio;
	}
	public void setInitial_infected_ratio(double initial_infected_ratio) {
		this.initial_infected_ratio = initial_infected_ratio;
	}
	public void setInfetion_probability(double infetion_probability) {
		this.infetion_probability = infetion_probability;
	}

	 **/

	public static  int WIDTH = 1600; 
	public static  int HEIGHT = 1200; 
	//public static final int NUM_AGENTS = 100; 

	private static final long serialVersionUID = -4554882816749973618L;

	public DContinuousGrid2D population;
	public GISCampaniaData campaniadata;

	public static String topicPrefix = "";

	public DCampaniaModel(GeneralParam params,String topic)
	{
		super(params,new DistributedMultiSchedule<Double2D>(), topic,params.getConnectionType());

		this.topicPrefix=topic;
		this.MODE=params.getMode();
		WIDTH=params.getWidth();
		HEIGHT=params.getHeight();
	}
	public void start()
	{
		//necessario per effettuare la simulazione senza costruttore
//		((DistributedStateConnectionFake)super.getDistributedStateConnectionJMS()).setupfakeconnection(this);
		super.start();
		try 
		{
			population = DContinuousGrid2DFactory.createDContinuous2D(neighborhood/1.5,WIDTH, HEIGHT,this,super.AOI,TYPE.pos_i,TYPE.pos_j,super.rows,super.columns,MODE,"population", topicPrefix,true);
			init_connection();
			
		} catch (DMasonException e) { e.printStackTrace(); }
		try {
			System.out.println(this.TYPE+"  Loading campania data..");
			campaniadata=GISCampaniaData.getInstance(WIDTH, HEIGHT);
			System.out.println(this.TYPE+"  End loading campania data.");
		}  catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		addAgentPerBasicDataRegionInDistributedCell();
		System.out.println("Population size: "+population.size());

	}
	void addAgentPerBasicDataRegionInDistributedCell()
	{

		for(Integer istat_code:campaniadata.nearRoad2codISTAT.keySet())
		{
			ArrayList<MasonGeometry> locations=campaniadata.codISTAT2Geometry.get(istat_code);
			
			for(MasonGeometry location :locations)
			{
				Point2D p=campaniadata.worldToScreenPointTransform(location.getGeometry().getCentroid().getCoordinate().x,location.getGeometry().getCentroid().getCoordinate().y);
				if(population.verifyPosition(new Double2D(p.getX(),p.getY())))
				{
					ArrayList<MasonGeometry> near_roads=campaniadata.nearRoad2codISTAT.get(istat_code);
					int random_road=this.random.nextInt(near_roads.size());
					Coordinate born_road=near_roads.get(random_road).getGeometry().getCoordinate();
					if(population.verifyPosition(new Double2D(born_road.x,born_road.y)))
					{
						DAgent a = new DAgent(this,near_roads.get(random_road).getGeometry().getCoordinate(),(LineString) near_roads.get(random_road).getGeometry());
						try {
							population.setDistributedObjectLocation(new Double2D(p.getX(),p.getY()), a, this);
						} catch (DMasonException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						schedule.scheduleOnce(a);
						
						break;
					}
					
				}
			}
		}
		
	}
//	void addAgentPerBasicDataRegion(HashMap<String, ArrayList<DataLocationISTAT>> map_istat_data)
//	{
//		for(ArrayList<MasonGeometry> places:campaniadata.codISTAT2Geometry.values())
//		{
//			for(MasonGeometry place:places)
//			{
//				String place_istat_code = place.getIntegerAttribute("COD_ISTAT")+"";
//				ArrayList<DataLocationISTAT> istat_data_palce=map_istat_data.get(place_istat_code);
//				if(istat_data_palce==null)
//				{
//					System.err.println("Error location "+place_istat_code+" not found!");
//					continue;
//				}
//				for(DataLocationISTAT pdata:istat_data_palce)
//				{
//					long pop_totale=pdata.getBasic_population_data().get(0);
//					for (long i = 0; i < pop_totale; i++) {
//						DAgent a = new DAgent(this,place.getGeometry().getCentroid().getCoordinate());
//						Coordinate c=a.getGeometry().getGeometry().getCoordinate();
//
//						Point2D p=campaniadata.worldToScreenPointTransform(c.x,c.y);
//						population.setObjectLocation(a,new Double2D(p));
//						schedule.scheduleRepeating(a);
//					}
//				}
//
//
//			}
//		}
//	}
//	void addAgentPerBasicDataRegionOne(HashMap<String, ArrayList<DataLocationISTAT>> map_istat_data)
//	{
//		for(ArrayList<MasonGeometry> places:campaniadata.codISTAT2Geometry.values())
//		{
//			for(MasonGeometry place:places)
//			{
//				String place_istat_code = place.getIntegerAttribute("COD_ISTAT")+"";
//				ArrayList<DataLocationISTAT> istat_data_palce=map_istat_data.get(place_istat_code);
//				if(istat_data_palce==null)
//				{
//					System.err.println("Error location "+place_istat_code+" not found!");
//					continue;
//				}
//				for(DataLocationISTAT pdata:istat_data_palce)
//				{
//					long pop_totale=pdata.getBasic_population_data().get(0);
//					for (long i = 0; i < 1; i++) {
//						DAgent a = new DAgent(this,place.getGeometry().getCentroid().getCoordinate());
//						Coordinate c=a.getGeometry().getGeometry().getCoordinate();
//
//						Point2D p=campaniadata.worldToScreenPointTransform(c.x,c.y);
//						population.setObjectLocation(a,new Double2D(p));
//						schedule.scheduleRepeating(a);
//					}
//				}
//
//
//			}
//		}
//	}
//	void addOneAgentPerRegion()
//	{
//		for(ArrayList<MasonGeometry> places:campaniadata.codISTAT2Geometry.values())
//		{
//			for(MasonGeometry place:places)
//			{
//				for (int i = 0; i < region_density; i++) {
//					DAgent a = new DAgent(this,place.getGeometry().getCentroid().getCoordinate());
//					Coordinate c=a.getGeometry().getGeometry().getCoordinate();
//
//					Point2D p=campaniadata.worldToScreenPointTransform(c.x,c.y);
//					population.setObjectLocation(a,new Double2D(p));
//					schedule.scheduleRepeating(a);
//				}
//
//			}
//		}
//	}


	public static void main(String[] args) {
		doLoop(DCampaniaModel.class, args);
		System.exit(0);
	}
	@Override
	public DistributedField<Double2D> getField() {
		// TODO Auto-generated method stub
		return population;
	}
	@Override
	public void addToField(RemotePositionedAgent rm, Double2D loc) 
	{
		population.setObjectLocation(rm,loc);
		setPortrayalForObject(rm);

	}
	@Override
	public SimState getState() 
	{
		return this;
	}
	public boolean setPortrayalForObject(Object o) {
		// TODO Auto-generated method stub
		return true;
	}
}
