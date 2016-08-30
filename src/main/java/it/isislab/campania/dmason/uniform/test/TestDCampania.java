package it.isislab.campania.dmason.uniform.test;
/**
 * Copyright 2012 Universita' degli Studi di Salerno


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
/*
 * THIS CLASS HAS BEEN USED FOR TESTING PURPOSES IN THE BEGINNINGS,
 */

import it.isislab.campania.dmason.uniform.DCampaniaModel;
import it.isislab.campania.dmason.uniform.DCampaniaModelWithUI;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.util.connection.ConnectionType;
import java.util.ArrayList;


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
public class TestDCampania {

	private static CellType vtype=new CellType(0,0);
	private static int numSteps = 100000; //only graphicsOn=false
	private static int rows = 3; //number of rows
	private static int columns = 3; //number of columns
	private static int MAX_DISTANCE=10; //max distance

	private static int MODE = DistributedField2D.UNIFORM_PARTITIONING_MODE;//(rows==1 || columns==1)? DistributedField2D.HORIZONTAL_DISTRIBUTION_MODE : DistributedField2D.SQUARE_DISTRIBUTION_MODE; 

	public static void main(String[] args) throws InterruptedException {

				
		class Worker extends Thread
		{

			private DCampaniaModel ds;
			public Worker(DCampaniaModel ds) {
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
		DCampaniaModelWithUI viewer=null;

		ArrayList<Worker> myWorker = new ArrayList<Worker>();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				GeneralParam genParam = new GeneralParam(DCampaniaModel.WIDTH, DCampaniaModel.HEIGHT, MAX_DISTANCE, rows,columns,0, MODE,ConnectionType.pureActiveMQ); 
				genParam.setI(i);
				genParam.setJ(j);
				genParam.setIp("127.0.0.1");//fake value
				genParam.setPort("61616");//fake Value
//				
//				if(vtype.pos_i!=i && vtype.pos_j!=j)
//				{
					
					DCampaniaModel sim = new DCampaniaModel(genParam,"topic"); 
					Worker a = new Worker(sim);
					myWorker.add(a);
//				}
//	
				
			}
		}

		for (Worker w : myWorker) {
			w.start();
		}
//		System.out.println("Start simulations");
//		GeneralParam genParam = new GeneralParam(DCampaniaModel.WIDTH, DCampaniaModel.HEIGHT, MAX_DISTANCE, rows,columns,0, MODE,ConnectionType.fakeUnitTestJMS); 
//		genParam.setI(vtype.pos_i);
//		genParam.setJ(vtype.pos_j);
//		genParam.setIp("127.0.0.1");//fake value
//		genParam.setPort("8080");//fake Value
//		viewer=new DCampaniaModelWithUI(genParam);
//
//		Console console = new Console(viewer);
//		console.setVisible(true);
		
		for (Worker w : myWorker) {
			w.join();
		}

	}
}