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
package it.isislab.campania.dmason.nonuniform.test;
import java.util.ArrayList;

import it.isislab.campania.dmason.nonuniform.DCampaniaModelNonUniformPartitioning;
import it.isislab.campania.dmason.nonuniform.DCampaniaModelNonUniformPartitioningWithUI;
import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
/*
 * THIS CLASS HAS BEEN USED FOR TESTING PURPOSES IN THE BEGINNINGS,
 */
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.util.connection.ConnectionType;
import sim.display.Console;

/**
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public class TestCampaniaModelNonUniformPartitioning {

	private static boolean graphicsOn=false; //with or without graphics?
	private static int numSteps = Integer.MAX_VALUE; //only graphicsOn=false
	private static int P=9;
	private static int AOI=10; //max distance
	private static int NUM_AGENTS=2000; //number of agents
	private static int WIDTH=DCampaniaModelNonUniformPartitioning.WIDTH; //field width
	private static int HEIGHT=DCampaniaModelNonUniformPartitioning.HEIGHT; //field height
	private static int CONNECTION_TYPE=ConnectionType.pureActiveMQ;
	private static String ip="127.0.0.1"; //ip of activemq
	private static String port="61616"; //port of activemq
	private static String topicPrefix="flocknoun"; //unique string to identify topics for this simulation
	
	//don't modify this...

	private static int MODE =  DistributedField2D.NON_UNIFORM_PARTITIONING_MODE;
	
	public static void main(String[] args) 
	{		
		class worker extends Thread
		{
			private DistributedState ds;
			public worker(DistributedState ds) {
				this.ds=ds;
				ds.start();
			}
			@Override
			public void run() {
				int i=0;
				while(i!=numSteps)
				{
					System.out.println(i);
					ds.schedule.step(ds);
					i++;
				}
			}
		}

		ArrayList<worker> myWorker = new ArrayList<worker>();
		
		for (int i = 0; i < P; i++) {
	
				GeneralParam genParam = new GeneralParam(WIDTH, HEIGHT, AOI, P, NUM_AGENTS, MODE, CONNECTION_TYPE); 
				genParam.setI(0);
				genParam.setJ(i);
				genParam.setIp(ip);
				genParam.setPort(port);
				ArrayList<EntryParam<String, Object>> simParams=new ArrayList<EntryParam<String, Object>>();
				if(graphicsOn || i==0)
				{
					DCampaniaModelNonUniformPartitioningWithUI sim =new DCampaniaModelNonUniformPartitioningWithUI(genParam,topicPrefix);
					((Console)sim.createController()).pressPause();
				}
				else
				{
					DCampaniaModelNonUniformPartitioning sim = new DCampaniaModelNonUniformPartitioning(genParam,topicPrefix); 
					worker a = new worker(sim);
					myWorker.add(a);
				}
			
		}
		if(!graphicsOn)
			for (worker w : myWorker) {
				w.start();
			}
	}
}