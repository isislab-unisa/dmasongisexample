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
package it.isislab.campania.mason;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.grid.IntGrid2D;
import sim.util.Double2D;
import sim.util.geo.MasonGeometry;
import com.vividsolutions.jts.geom.Coordinate;
import it.isislab.campania.gis.DataLocationISTAT;
import it.isislab.campania.gis.GISCampaniaData;
import it.isislab.campania.mason.agents.Agent;
import it.isislab.dmason.nonuniform.QuadTree;

public class CampaniaModel extends SimState
{
	/*SIR MODEL*/
	public double infetion_probability=0.1;
	public final double population_rate=0.5;
	public double neighborhood = 1;
	public int max_exposed_time=10;
	public int max_infected_time=20;

	public double initial_infected_ratio=0.1;
	public int initial_infected_period=10;

	public int region_density=10;

	public double DIAMETER=1;

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



	public double getDIAMETER() {
		return DIAMETER;
	}
	public void setDIAMETER(double dIAMETER) {
		DIAMETER = dIAMETER;
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



	public static final int WIDTH = 1921; 
	public static final int HEIGHT = 1079; 
	public static final int NUM_AGENTS = 100; 

	private static final long serialVersionUID = -4554882816749973618L;

	public Continuous2D population;
	public GISCampaniaData campaniadata;

	public IntGrid2D nonuniformgrid=new IntGrid2D(WIDTH, HEIGHT);

	public CampaniaModel(long seed)
	{
		super(seed);
	}
	public void start()
	{
		population= new Continuous2D(neighborhood/1.5,WIDTH,HEIGHT);

		try {
			campaniadata=GISCampaniaData.getInstance(WIDTH, HEIGHT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.print("Creating population, for "+campaniadata.getIstat_data_map().size()+" countries..");

		addAgentPerBasicDataRegionDensity(campaniadata.getIstat_data_map());
//		addTestAgentsUniform();
		System.out.println();


		System.out.println("Population size: "+population.size());

		generateQuadTree();

	}
	int P=16;
	private void generateQuadTree() {


		QuadTree q=new QuadTree(population.size()/(P), 0, 0, WIDTH, HEIGHT,2.0);
		for(Object s:population.allObjects)
		{
			Agent a = (Agent)s;
			Double2D apos=null;
			if(a.location!=null)
				apos=new Double2D(campaniadata.worldToScreenPointTransform(a.location.getGeometry().getCentroid().getX(), a.location.getGeometry().getCentroid().getY()));
			else apos=a.fakepos;

			if(!q.insert(a, apos.getX(), apos.getY()))
			{
				System.err.println("Error ");
				System.exit(-1);
			}
		}
		//		QuadTree.printTree(q);
		//		System.out.println("Number of leafs: "+QuadTree.printLeafs(q));
		//		double computed_area=drawTree(q,0);
		//		
		//		System.out.println("Sanity check area "+(computed_area== (WIDTH*HEIGHT)));


		QuadTree.printQuadTree(q,0);
		System.out.println("=================================================");
		QuadTree.partition(P, q,true);
		System.out.println("=================================================");
		QuadTree.printQuadTree(q,0);

		nonuniformgrid.field = new int[WIDTH][HEIGHT];

		double computed_area=drawTree(q);

		System.out.println("Sanity check area (after part) "+(computed_area== (WIDTH*HEIGHT)));

		//		QuadTree.printTree(q);
		System.out.println("Number of leafs: "+QuadTree.printLeafs(q));
		
		System.out.println(QuadTree.checkObjects(q));
		
//		System.out.println(QuadTree.reportPartitioning(QuadTree.getPartitioning(q)));

		//System.out.println(QuadTree.reportStatsPartitioning(QuadTree.getPartitioning(q),0));


	}

	private double drawTree(QuadTree node)
	{
		QuadTree[] neighbors=node.getNeighbors();
		if(neighbors[0]==null && neighbors[1]==null && neighbors[2]==null &&  neighbors[3]==null)
		{
			for(int x=(int)node.getX1(); x < (int)(node.getX2());x++)
				nonuniformgrid.set(x,Math.max(0,(int) node.getY1()-1),1);
			for(int y=(int)node.getY1(); y < (int)(node.getY2());y++)
				nonuniformgrid.set(Math.max(0,(int)node.getX2()-1),y,1);
			for(int y=(int)node.getY1(); y < (int)(node.getY2());y++)
				nonuniformgrid.set(Math.max(0,(int)node.getX1()-1),y,1);
			for(int x=(int)node.getX1(); x < (int)(node.getX2());x++)
				nonuniformgrid.set(x,Math.max(0,(int)node.getY2()-1),1);

			return (node.getY2()-(node.getY1()))*(node.getX2()-(node.getX1()));
		}

		double parea=(neighbors[0]!=null)?drawTree(neighbors[0]):0;
		parea+=(neighbors[1]!=null)?drawTree(neighbors[1]):0;
		parea+=(neighbors[2]!=null)?drawTree(neighbors[2]):0;
		parea+=(neighbors[3]!=null)?drawTree(neighbors[3]):0;


		return parea;

	}
	void createNonUniformSpaceSplit(int x1, int y1, int x2, int y2,int level){

		//		if(level> 3) return;
		for (int i = 0; i < level; i++) {
			System.out.print(" ");
		}
		System.out.println(x1+" "+y1+" "+x2+" "+y2+ " "+level);

		//if(level%2==0 )
		if( (x2-x1) < (y2-y1))
		{
			for (int i = x1; i < x2-1; i++) {

				nonuniformgrid.set(y1+((y2-y1)/2)-1,i,1);
			}
			createNonUniformSpaceSplit(x1, y1, x2, y1+((y2-y1)/2),level+1);
			createNonUniformSpaceSplit(x1, y1+((y2-y1)/2), x2, y2,level+1);
		}else
		{
			for (int i = y1; i < y2-1; i++) {

				nonuniformgrid.set(i, x1+((x2-x1)/2)-1,1);
			}
			createNonUniformSpaceSplit(x1, y1, x1+((x2-x1)/2), y2,level+1);
			createNonUniformSpaceSplit(x1+((x2-x1)/2), y1, x2, y2,level+1);
		}

	}
	void addTestAgentsUniform()
	{
		for (int i = 0; i < 100; i++) {
			Agent a = new Agent(this,null,DIAMETER,null);
			//			Coordinate c=a.getGeometry().getGeometry().getCoordinate();
			//			a.setISTATData(campaniadata.codISTATdataPendolo.get(pdata.getCod_istat()).get(i),pdata.getLocation_name(),campaniadata.codISTATNames);
			//			Point2D p=campaniadata.worldToScreenPointTransform(this.random.nextDouble()*WIDTH,this.random.nextDouble()*HEIGHT);
			a.fakepos=new Double2D(this.random.nextDouble()*WIDTH,this.random.nextDouble()*HEIGHT);
			population.setObjectLocation(a,a.fakepos);
			schedule.scheduleRepeating(a);

		}
	}
	void addAgentPerBasicDataRegionDensity(HashMap<Integer, ArrayList<DataLocationISTAT>> map_istat_data)
	{
		//		for(ArrayList<MasonGeometry> places:campaniadata.codISTAT2Geometry.values())
		//		{
		//			for(MasonGeometry place:places)
		//			{
		//				String place_istat_code = place.getIntegerAttribute("COD_ISTAT")+"";
		//				if(!place_istat_code.equalsIgnoreCase("15064008")) continue;
		//				
		//				ArrayList<DataLocationISTAT> istat_data_palce=map_istat_data.get(Integer.parseInt(place_istat_code));
		//				if(istat_data_palce==null)
		//				{
		//					System.err.println("Error location "+place_istat_code+" not found!");
		//					continue;
		//				}
		//				for(DataLocationISTAT pdata:istat_data_palce)
		//				{
		//					if(Integer.parseInt(place_istat_code)!=pdata.getCod_istat())
		//					{
		//						System.err.println("error "+place_istat_code+" "+pdata.getCod_istat());
		//					}
		//					if(campaniadata.codISTATdataPendolo.get(pdata.getCod_istat())!=null)
		//					for (int i = 0; i < region_density; i++) {
		//						Agent a = new Agent(this,place.getGeometry().getCentroid().getCoordinate(),DIAMETER);
		//						Coordinate c=a.getGeometry().getGeometry().getCoordinate();
		//						a.setISTATData(campaniadata.codISTATdataPendolo.get(pdata.getCod_istat()).get(i),campaniadata.codISTATNames);
		//						Point2D p=campaniadata.worldToScreenPointTransform(c.x,c.y);
		//						population.setObjectLocation(a,new Double2D(p));
		//						schedule.scheduleRepeating(a);
		//						
		//						if(pdata.getCod_istat()==15064008)
		//						{
		//							System.out.println(p);
		//							System.out.println(c);
		//							System.out.println(campaniadata.codISTATNames.get(15064008));
		//						}
		//					}
		//				}
		//				
		//				
		//			}
		//		}
		for(Integer istat_geometry:campaniadata.codISTAT2Geometry.keySet())
		{
			//			if(istat_geometry !=15064008) continue;
			ArrayList<MasonGeometry> places =campaniadata.codISTAT2Geometry.get(istat_geometry);
			for(MasonGeometry place:places)
			{
				for(DataLocationISTAT pdata:map_istat_data.get(istat_geometry))
				{
					if(!istat_geometry.equals(pdata.getCod_istat()))
					{
						System.err.println("error"+istat_geometry+" "+pdata.getCod_istat() +""+(istat_geometry!=pdata.getCod_istat()));
						continue;
					}
					if(campaniadata.codISTATdataPendolo.get(pdata.getCod_istat())!=null)
						for (int i = 0; i < region_density; i++) {
							Agent a = new Agent(this,place.getGeometry().getCentroid().getCoordinate(),DIAMETER,istat_geometry);
							Coordinate c=a.getGeometry().getGeometry().getCoordinate();
							a.setISTATData(campaniadata.codISTATdataPendolo.get(pdata.getCod_istat()).get(i),pdata.getLocation_name(),campaniadata.codISTATNames);
							Point2D p=campaniadata.worldToScreenPointTransform(c.x,c.y);
							population.setObjectLocation(a,new Double2D(p));
							schedule.scheduleRepeating(a);

							//						if(pdata.getCod_istat()==15064008)
							//						{
							//							System.out.println(p);
							//							System.out.println(c);
							//							System.out.println(campaniadata.codISTATNames.get(15064008));
							//						}
						}
				}
			}

		}
	}

	//	void addAgentPerBasicDataRegionDensity(HashMap<Integer, ArrayList<DataLocationISTAT>> map_istat_data)
	//	{
	////		for(ArrayList<MasonGeometry> places:campaniadata.codISTAT2Geometry.values())
	////		{
	////			for(MasonGeometry place:places)
	////			{
	////				String place_istat_code = place.getIntegerAttribute("COD_ISTAT")+"";
	////				if(!place_istat_code.equalsIgnoreCase("15064008")) continue;
	////				
	////				ArrayList<DataLocationISTAT> istat_data_palce=map_istat_data.get(Integer.parseInt(place_istat_code));
	////				if(istat_data_palce==null)
	////				{
	////					System.err.println("Error location "+place_istat_code+" not found!");
	////					continue;
	////				}
	////				for(DataLocationISTAT pdata:istat_data_palce)
	////				{
	////					if(Integer.parseInt(place_istat_code)!=pdata.getCod_istat())
	////					{
	////						System.err.println("error "+place_istat_code+" "+pdata.getCod_istat());
	////					}
	////					if(campaniadata.codISTATdataPendolo.get(pdata.getCod_istat())!=null)
	////					for (int i = 0; i < region_density; i++) {
	////						Agent a = new Agent(this,place.getGeometry().getCentroid().getCoordinate(),DIAMETER);
	////						Coordinate c=a.getGeometry().getGeometry().getCoordinate();
	////						a.setISTATData(campaniadata.codISTATdataPendolo.get(pdata.getCod_istat()).get(i),campaniadata.codISTATNames);
	////						Point2D p=campaniadata.worldToScreenPointTransform(c.x,c.y);
	////						population.setObjectLocation(a,new Double2D(p));
	////						schedule.scheduleRepeating(a);
	////						
	////						if(pdata.getCod_istat()==15064008)
	////						{
	////							System.out.println(p);
	////							System.out.println(c);
	////							System.out.println(campaniadata.codISTATNames.get(15064008));
	////						}
	////					}
	////				}
	////				
	////				
	////			}
	////		}
	//}
	//	void addAgentPerFixedRegionDensity()
	//	{
	//		for(ArrayList<MasonGeometry> places:campaniadata.codISTAT2Geometry.values())
	//		{
	//			for(MasonGeometry place:places)
	//			{
	//				for (int i = 0; i < region_density; i++) {
	//					Agent a = new Agent(this,place.getGeometry().getCentroid().getCoordinate(),DIAMETER);
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
	//	void addAgentPerBasicDataRegion(HashMap<Integer, ArrayList<DataLocationISTAT>> map_istat_data)
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
	//						Agent a = new Agent(this,place.getGeometry().getCentroid().getCoordinate(),DIAMETER);
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

	public static void main(String[] args) {
		doLoop(CampaniaModel.class, args);
		System.exit(0);
	}
}
