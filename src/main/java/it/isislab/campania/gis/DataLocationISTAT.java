package it.isislab.campania.gis;

import java.util.ArrayList;

public class DataLocationISTAT {
	
	private Integer cod_istat;
	private String location_name;
	private ArrayList<Long> basic_population_data;//P1 P2 P3
	private ArrayList<Long> advanced_population_data;//P14-P44
	private ArrayList<Long> advanced_foreign_population_data;//S1-S8
	
	
	public DataLocationISTAT(Integer cod_istat, String location_name,
			ArrayList<Long> basic_population_data,
			ArrayList<Long> advanced_population_data,
			ArrayList<Long> advanced_foreign_population_data) {
		super();
		this.cod_istat = cod_istat;
		this.location_name = location_name;
		this.basic_population_data = basic_population_data;
		this.advanced_population_data = advanced_population_data;
		this.advanced_foreign_population_data = advanced_foreign_population_data;
	}
	@Override
	public String toString() {
		return cod_istat+";"+location_name+";"+basic_population_data+";"+advanced_population_data+";"+advanced_foreign_population_data;
	}
	public Integer getCod_istat() {
		return cod_istat;
	}
	public void setCod_istat(Integer cod_istat) {
		this.cod_istat = cod_istat;
	}
	public String getLocation_name() {
		return location_name;
	}
	public void setLocation_name(String location_name) {
		this.location_name = location_name;
	}
	public ArrayList<Long> getBasic_population_data() {
		return basic_population_data;
	}
	public void setBasic_population_data(ArrayList<Long> basic_population_data) {
		this.basic_population_data = basic_population_data;
	}
	public ArrayList<Long> getAdvanced_population_data() {
		return advanced_population_data;
	}
	public void setAdvanced_population_data(ArrayList<Long> advanced_population_data) {
		this.advanced_population_data = advanced_population_data;
	}
	public ArrayList<Long> getAdvanced_foreign_population_data() {
		return advanced_foreign_population_data;
	}
	public void setAdvanced_foreign_population_data(
			ArrayList<Long> advanced_foreign_population_data) {
		this.advanced_foreign_population_data = advanced_foreign_population_data;
	}
	

	
	
}
