package it.isislab.campania.dmason.activemq;

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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.SystemUsage;
import org.apache.activemq.usage.TempUsage;
import org.apache.activemq.usage.UsageCapacity;


/**
 * Embedded ActivemQ Starter  
 * set config.properties to change ActivemQ ip:port  
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class ActiveMQStarter {

	
	private  BrokerService broker=null;
	private  Properties startProperties = null;
	private  String IP_ACTIVEMQ;
	private  String PORT_ACTIVEMQ;

	//ActivemQ settings file, default 127.0.0.1:61616 otherwise you have to change config.properties file
	private static final String PROPERTIES_FILE_PATH="activemq.properties";
	
	/**
	 * Embedded starter for ActivemQ
	 */
	public ActiveMQStarter(){
		startProperties = new Properties();
		broker = new BrokerService();
		InputStream input=null;
		//load params from properties file 
		try {
			input=new FileInputStream(PROPERTIES_FILE_PATH);	
			startProperties.load(input);
			IP_ACTIVEMQ=startProperties.getProperty("ipmaster");
			PORT_ACTIVEMQ=startProperties.getProperty("portmaster");
			System.out.println(IP_ACTIVEMQ +" "+PORT_ACTIVEMQ);
			
			

		} catch (IOException e2) {
			System.err.println(e2.getMessage());
		}finally{
			try {input.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				}
			}
	}
	

	/**
	 * Start ActivemQ service
	 */
	public void startActivemq(){
		
		String address="tcp://"+IP_ACTIVEMQ+":"+PORT_ACTIVEMQ;
		try {
			/*code to set ActivemQ configuration 
			for tempUsage property a big value can cause error 
			for node with low disk space*/ 

			String os=System.getProperty("os.name").toLowerCase();
			File rootFileSystem=null;;

			Long val=new Long(1000000000);

			if(os.contains("linux")){
				rootFileSystem=new File("/");
				val=new Long(rootFileSystem.getFreeSpace()/2); 
			}else if(os.contains("windows")){
				//
				System.out.println("windows system using 1Gb for tempUsage");
			}

			TempUsage usage=new TempUsage();
			UsageCapacity c=broker.getSystemUsage().getTempUsage().getLimiter();
			c.setLimit(val);
			usage.setLimiter(c);
			SystemUsage su = broker.getSystemUsage();
			su.setTempUsage(usage);
			broker.setSystemUsage(su);
			/*     end code for tempUsage setting    */
			broker.addConnector(address);
			broker.start();
			
			
		} catch (Exception e1) {e1.printStackTrace();}
	}
	// start ActivemQ
	public static void main(String[] args) {
		ActiveMQStarter activemq=new ActiveMQStarter();
		activemq.startActivemq();
	}
}
