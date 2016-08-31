package it.isislab.campania.gis;


import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.GeomPlanarGraph;
import sim.util.geo.MasonGeometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.planargraph.Node;

public class GISCampaniaData {

	private String strade_path="data"+File.separator+"stradegis"+File.separator+"campania-strade-WSG-32N.shp";
	private String countries_path="data"+File.separator+"basiterritoriali"+File.separator+"R15_WGS84.shp";
	private String istat_data_path="data"+File.separator+"popolazione"+File.separator+"R15_Dati_CPA_2011_definitivi.csv";
	private String istat_data_pendolo_first="data"+File.separator+"censimento"+File.separator+"matrix_pendo2011_10112014_1.txt";
	private String istat_data_pendolo_second="data"+File.separator+"censimento"+File.separator+"matrix_pendo2011_10112014_2.txt";
	private String istat_data_pendolo_third="data"+File.separator+"censimento"+File.separator+"matrix_pendo2011_10112014_3.txt";
	private String istat_data_pendolo_fourth="data"+File.separator+"censimento"+File.separator+"matrix_pendo2011_10112014_4.txt";

	private String mpa_istat_name="data"+File.separator+"popolazione"+File.separator+"CODISTAT-NAME-MAP.txt";
	
	private String currentDirectoryPath;
	public HashMap<Integer,ArrayList<GISCampaniaPendolo>> codISTATdataPendolo;


	private static GISCampaniaData GIS;
	private HashMap<Integer,ArrayList<DataLocationISTAT>> istat_data_map;
	private GeomVectorField country;
	private GeomVectorField roads;
	private GeomPlanarGraph network;
	private GeomVectorField junctions;

	private final int WIDTH; 
	private final int HEIGHT; 
	private Envelope MBR;
	public GeometryFactory factoryGeometry = new GeometryFactory();

	public HashMap<Integer, ArrayList<MasonGeometry>> codISTAT2Geometry=new HashMap<Integer, ArrayList<MasonGeometry>>();
	public HashMap<Coordinate, Integer> coordinate2Codpro=new HashMap<Coordinate,Integer>();
	public HashMap<Integer, ArrayList<MasonGeometry>> nearRoad2codISTAT=new HashMap<Integer, ArrayList<MasonGeometry>>();


	public HashMap<Integer,String> codISTATNames=new HashMap<Integer, String>();

	public void mapISTATtoNames()
	{
		BufferedReader br = null;
		String line = "";

		try {

			br = new BufferedReader(new FileReader(
					System.getProperty("user.dir")+File.separator+mpa_istat_name));
			if(br.readLine()==null) return;
			while ((line = br.readLine()) != null) {
				String[] istanames=line.split(",");
				if(codISTATNames.get(Integer.parseInt(istanames[0]))==null)
				codISTATNames.put(Integer.parseInt(istanames[0]),istanames[1]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	public GISCampaniaData(int width,int height) throws IOException
	{
		currentDirectoryPath= new File(new File(".").getAbsolutePath()).getCanonicalPath();
		System.out.println("Loaded working path:"+currentDirectoryPath);
		System.out.print("Loading roads and regions..");
		country = new GeomVectorField(width,height);
		roads = new GeomVectorField(width,height);
		network = new GeomPlanarGraph();
		junctions = new GeomVectorField(width,height); 
		this.WIDTH=width;
		this.HEIGHT=height;
		Bag desiredAttributes = new Bag();
		desiredAttributes.add("COD_ISTAT");
		desiredAttributes.add("PRO_COM");

		URL roadsdata =new URL("file://"+System.getProperty("user.dir")+File.separator+strade_path);

		ShapeFileImporter.read(roadsdata, roads);

		MBR = roads.getMBR();
		URL countrydata =new URL("file://"+System.getProperty("user.dir")+File.separator+countries_path);
		ShapeFileImporter.read(countrydata, country,desiredAttributes);

		MBR.expandToInclude(country.getMBR());

		roads.setMBR(MBR);
		country.setMBR(MBR);

		network.createFromGeomField(roads);

		addIntersectionNodes(network.nodeIterator(), junctions);

		for(Object og:country.getGeometries())
		{
			MasonGeometry country=(MasonGeometry) og;
			int cod_ISTAT=country.getIntegerAttribute("COD_ISTAT");
			coordinate2Codpro.put(country.getGeometry().getCoordinate(), cod_ISTAT);
			if(codISTAT2Geometry.get(cod_ISTAT)==null) codISTAT2Geometry.put(cod_ISTAT, new ArrayList<MasonGeometry>());
			codISTAT2Geometry.get(cod_ISTAT).add(country);
			//NEAR ROAD TO COD ISTAT
			Coordinate country_center=country.getGeometry().getCoordinate();
			if(nearRoad2codISTAT.get(cod_ISTAT)==null) nearRoad2codISTAT.put(cod_ISTAT,new ArrayList<MasonGeometry>());
			Bag near_road=roads.queryField(new Envelope(country_center));
			if(near_road.size()==0)
				near_road=roads.queryField(new Envelope(country_center.x,country_center.x+10000,country_center.y,country_center.y+10000));
			if(near_road.size()==0)
				System.out.println("Error ");
			nearRoad2codISTAT.get(cod_ISTAT).addAll(near_road);
		}
//		for(Integer toc:nearRoad2codISTAT.keySet())
//			if(nearRoad2codISTAT.get(toc).size()==0)
//				System.out.println(" "+toc);
//		
//		
		System.out.println("done");
		System.out.print("Loading ISTAT data..");
		istat_data_map=loadPopulationISTATdata(istat_data_path);

		codISTATdataPendolo=loadDataPendoloByCountry(System.getProperty("user.dir")+File.separator+istat_data_pendolo_first,
				System.getProperty("user.dir")+File.separator+istat_data_pendolo_second,
				System.getProperty("user.dir")+File.separator+istat_data_pendolo_third,
				System.getProperty("user.dir")+File.separator+istat_data_pendolo_fourth, istat_data_map, codISTAT2Geometry);
		mapISTATtoNames();

		System.out.println("done ("+istat_data_map.size()+" ISTAT places loaded)");
	}
	
	
	private static HashMap<Integer,ArrayList<GISCampaniaPendolo>> loadDataPendoloByCountry(final String file_path, final String file_path2,final String file_path3,final String file_path4,
			HashMap<Integer, ArrayList<DataLocationISTAT>> istat_data_map2, HashMap<Integer, ArrayList<MasonGeometry>> codISTAT2Geometry)
			{
		BufferedReader br = null;
		BufferedReader br2 = null;
		BufferedReader br3 = null;
		BufferedReader br4 = null;
		String line = "";

		HashMap<Integer,ArrayList<GISCampaniaPendolo>> datas=null;
		try {

			br = new BufferedReader(new FileReader(file_path));
			
			
			if(br.readLine()==null) return null;
			datas=new HashMap<Integer, ArrayList<GISCampaniaPendolo>>();
			while ((line = br.readLine()) != null) {

				String[] d=line.split("\\s+");	
				String istat="15"+d[2]+d[3];

				//				if(!nearRoad2codISTAT.containsKey(Integer.parseInt(istat))) continue;
				if(!codISTAT2Geometry.containsKey(Integer.parseInt(istat))) continue;
				GISCampaniaPendolo data=new GISCampaniaPendolo(
						d[0],
						Integer.parseInt(d[1]),
						"15"+d[2]+d[3],
						Integer.parseInt(d[4]),
						d[5],
						d[6],
						d[7],
						d[8],
						d[9],
						d[10],
						d[11],
						d[12],
						d[13].equals("ND")?-1.0:Double.parseDouble(d[13]),
								d[14].equals("ND")?-1: Integer.parseInt(d[14]),
										istat_data_map2.get(Integer.parseInt("15"+d[2]+d[3]))==null?"UNKNOWN":istat_data_map2.get(Integer.parseInt("15"+d[2]+d[3])).get(0).getLocation_name()
												,istat_data_map2.get(Integer.parseInt("15"+d[7]+d[8]))==null?"UNKNOWN":istat_data_map2.get(Integer.parseInt("15"+d[7]+d[8])).get(0).getLocation_name()
						);

				if(datas.get(Integer.parseInt(istat))==null)datas.put(Integer.parseInt(istat),new ArrayList<GISCampaniaPendolo>());
				datas.get(Integer.parseInt(istat)).add(data);

			}
			
		    line="";
			br2 = new BufferedReader(new FileReader(file_path2));
			if(br2.readLine()==null) return null;
			datas=new HashMap<Integer, ArrayList<GISCampaniaPendolo>>();
			while ((line = br2.readLine()) != null) {

				String[] d=line.split("\\s+");	
				String istat="15"+d[2]+d[3];

				//				if(!nearRoad2codISTAT.containsKey(Integer.parseInt(istat))) continue;
				if(!codISTAT2Geometry.containsKey(Integer.parseInt(istat))) continue;
				GISCampaniaPendolo data=new GISCampaniaPendolo(
						d[0],
						Integer.parseInt(d[1]),
						"15"+d[2]+d[3],
						Integer.parseInt(d[4]),
						d[5],
						d[6],
						d[7],
						d[8],
						d[9],
						d[10],
						d[11],
						d[12],
						d[13].equals("ND")?-1.0:Double.parseDouble(d[13]),
								d[14].equals("ND")?-1: Integer.parseInt(d[14]),
										istat_data_map2.get(Integer.parseInt("15"+d[2]+d[3]))==null?"UNKNOWN":istat_data_map2.get(Integer.parseInt("15"+d[2]+d[3])).get(0).getLocation_name()
												,istat_data_map2.get(Integer.parseInt("15"+d[7]+d[8]))==null?"UNKNOWN":istat_data_map2.get(Integer.parseInt("15"+d[7]+d[8])).get(0).getLocation_name()
						);

				if(datas.get(Integer.parseInt(istat))==null)datas.put(Integer.parseInt(istat),new ArrayList<GISCampaniaPendolo>());
				datas.get(Integer.parseInt(istat)).add(data);

			}
			
			
			    line="";
				br3 = new BufferedReader(new FileReader(file_path2));
				if(br3.readLine()==null) return null;
				datas=new HashMap<Integer, ArrayList<GISCampaniaPendolo>>();
				while ((line = br3.readLine()) != null) {

					String[] d=line.split("\\s+");	
					String istat="15"+d[2]+d[3];

					//				if(!nearRoad2codISTAT.containsKey(Integer.parseInt(istat))) continue;
					if(!codISTAT2Geometry.containsKey(Integer.parseInt(istat))) continue;
					GISCampaniaPendolo data=new GISCampaniaPendolo(
							d[0],
							Integer.parseInt(d[1]),
							"15"+d[2]+d[3],
							Integer.parseInt(d[4]),
							d[5],
							d[6],
							d[7],
							d[8],
							d[9],
							d[10],
							d[11],
							d[12],
							d[13].equals("ND")?-1.0:Double.parseDouble(d[13]),
									d[14].equals("ND")?-1: Integer.parseInt(d[14]),
											istat_data_map2.get(Integer.parseInt("15"+d[2]+d[3]))==null?"UNKNOWN":istat_data_map2.get(Integer.parseInt("15"+d[2]+d[3])).get(0).getLocation_name()
													,istat_data_map2.get(Integer.parseInt("15"+d[7]+d[8]))==null?"UNKNOWN":istat_data_map2.get(Integer.parseInt("15"+d[7]+d[8])).get(0).getLocation_name()
							);

					if(datas.get(Integer.parseInt(istat))==null)datas.put(Integer.parseInt(istat),new ArrayList<GISCampaniaPendolo>());
					datas.get(Integer.parseInt(istat)).add(data);

				}
				
				  line="";
					br4 = new BufferedReader(new FileReader(file_path2));
					if(br4.readLine()==null) return null;
					datas=new HashMap<Integer, ArrayList<GISCampaniaPendolo>>();
					while ((line = br4.readLine()) != null) {

						String[] d=line.split("\\s+");	
						String istat="15"+d[2]+d[3];

						//				if(!nearRoad2codISTAT.containsKey(Integer.parseInt(istat))) continue;
						if(!codISTAT2Geometry.containsKey(Integer.parseInt(istat))) continue;
						GISCampaniaPendolo data=new GISCampaniaPendolo(
								d[0],
								Integer.parseInt(d[1]),
								"15"+d[2]+d[3],
								Integer.parseInt(d[4]),
								d[5],
								d[6],
								d[7],
								d[8],
								d[9],
								d[10],
								d[11],
								d[12],
								d[13].equals("ND")?-1.0:Double.parseDouble(d[13]),
										d[14].equals("ND")?-1: Integer.parseInt(d[14]),
												istat_data_map2.get(Integer.parseInt("15"+d[2]+d[3]))==null?"UNKNOWN":istat_data_map2.get(Integer.parseInt("15"+d[2]+d[3])).get(0).getLocation_name()
														,istat_data_map2.get(Integer.parseInt("15"+d[7]+d[8]))==null?"UNKNOWN":istat_data_map2.get(Integer.parseInt("15"+d[7]+d[8])).get(0).getLocation_name()
								);

						if(datas.get(Integer.parseInt(istat))==null)datas.put(Integer.parseInt(istat),new ArrayList<GISCampaniaPendolo>());
						datas.get(Integer.parseInt(istat)).add(data);

					}
			
			
			

		} catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return datas;

			}
	public GISCampaniaData(int width,int height,String strade_path, String countries_path, String istat_data_path) throws IOException
	{
		this(width,height);
		this.strade_path=strade_path;
		this.countries_path=countries_path;
		this.istat_data_path=istat_data_path;

	}
	private static HashMap<Integer,ArrayList<DataLocationISTAT>> loadPopulationISTATdata(final String csvFile)
	{
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";
		HashMap<Integer,ArrayList<DataLocationISTAT>> datas;
		try {

			br = new BufferedReader(new FileReader(csvFile));
			if(br.readLine()==null) return null;
			datas=new HashMap<Integer, ArrayList<DataLocationISTAT>>();
			while ((line = br.readLine()) != null) {

				String[] country = line.split(cvsSplitBy);
				Integer ISTATcode=Integer.parseInt(country[0]);
				String ISTATloc_name=country[1];
				ArrayList<Long> pBasic=new ArrayList<Long>();
				ArrayList<Long> pAdv=new ArrayList<Long>();
				ArrayList<Long> pForadv=new ArrayList<Long>();
				for (int i = 2; i <=4 ; i++) {
					pBasic.add(Long.parseLong(country[i]));
				}
				for (int i = 5; i <= 36 ; i++) {
					pAdv.add(Long.parseLong(country[i]));
				}
				for (int i = 37; i <= 44 ; i++) {
					pForadv.add(Long.parseLong(country[i]));
				}
				DataLocationISTAT data=new DataLocationISTAT(ISTATcode, ISTATloc_name, pBasic, pAdv, pForadv);
				if(datas.get(ISTATcode)==null) datas.put(ISTATcode,new ArrayList<DataLocationISTAT>());
				datas.get(ISTATcode).add(data);


			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return datas;
	}
	private void addIntersectionNodes(@SuppressWarnings("rawtypes") Iterator nodeIterator, GeomVectorField intersections)
	{
		GeometryFactory fact = new GeometryFactory();
		Coordinate coord = null;
		Point point = null;
		while (nodeIterator.hasNext())
		{
			Node node = (Node) nodeIterator.next();
			coord = node.getCoordinate();
			point = fact.createPoint(coord);

			junctions.addGeometry(new MasonGeometry(point));

		}
	}
	public static GISCampaniaData getInstance(int width,int height) throws IOException
	{
		if(GIS==null)
		{
			GIS=new GISCampaniaData( width, height);

		}
		return GIS;
	}
	public GeomVectorField getCountry() {
		return country;
	}
	public GeomVectorField getRoads() {
		return roads;
	}

	public GeomPlanarGraph getNetwork() {
		return network;
	}

	public GeomVectorField getJunctions() {
		return junctions;
	}
	public Envelope getMBR() {
		return MBR;
	}
	public int getWIDTH() {
		return WIDTH;
	}
	public int getHEIGHT() {
		return HEIGHT;
	}
	public HashMap<Integer, ArrayList<DataLocationISTAT>> getIstat_data_map() {
		return istat_data_map;
	}

	public  Point2D worldToScreenPointTransform( double x, double y)
	{
		final AffineTransform worldToScreen = worldToScreenTransform(MBR,new Rectangle2D.Double(0,0,WIDTH,HEIGHT));

		Point2D p = new Point2D.Double();
		worldToScreen.transform(new Point2D.Double(x, y), p);
		return p;
	}
	public  Point2D screenToWorldPointTransform( double x, double y)
	{
		final AffineTransform worldToScreen = worldToScreenTransform(MBR,new Rectangle2D.Double(0,0,WIDTH,HEIGHT));
		AffineTransform screenToWorld = null;
		try
		{
			screenToWorld = worldToScreen.createInverse();
		} catch (Exception e)
		{
			System.out.println(e);
			System.exit(-1);
		}

		Point2D p = new Point2D.Double();
		screenToWorld.transform(new Point2D.Double(x, y), p);
		return p;
	}
	public static AffineTransform worldToScreenTransform(final Envelope mapExtent, final java.awt.geom.Rectangle2D.Double viewport)
	{
		double scaleX = viewport.width / mapExtent.getWidth();
		double scaleY = viewport.height / mapExtent.getHeight();

		double tx = -mapExtent.getMinX() * scaleX;
		double ty = (mapExtent.getMinY() * scaleY) + viewport.height;

		AffineTransform at = new AffineTransform(scaleX, 0.0d, 0.0d, -scaleY, tx, ty);
		AffineTransform originTranslation = AffineTransform.getTranslateInstance(viewport.x, viewport.y);
		originTranslation.concatenate(at);

		return originTranslation != null ? originTranslation : at;

	}

}
