package it.isislab.campania.gis;

import java.io.Serializable;

public class GISCampaniaPendolo implements Serializable{

	private String tipo_record;//S,L
	private Integer tipo_residenza;//1,2
	private String codISTAT;
	private Integer sesso; //1,2
	private String motivoSpostamento;//1,2
	private String tipoLuogo;//1,2,3
	private String provincia_studio_lavoro;
	private String comune_studio_laboro;
	private String paese_estero;
	private String mezzo;//01treno,02tram,03,....,12
	private String orario_uscita;//1,2,3,4
	private String tempo;//1,2,3,4
	private Double stima_numero_individui;
	private Integer numero_individui;
	private String part_loc;//1,2,3,4
	private String dest_loc;//1,2,3,4
	private String codIstatdest;
	public GISCampaniaPendolo(String tipo_record, 
			Integer tipo_residenza,
			String codISTAT,
			Integer sesso, String motivoSpostamento,
			String tipoLuogo, String provincia_studio_lavoro,
			String comune_studio_laboro, String paese_estero, String mezzo,
			String orario_uscita, String tempo,
			Double stima_numero_individui,
			Integer numero_individui,
			String part_loc,
			String dest_loc) {
		super();
		this.tipo_record = tipo_record;
		this.tipo_residenza = tipo_residenza;
		this.codISTAT = codISTAT;
		this.sesso = sesso;
		this.motivoSpostamento = motivoSpostamento;
		this.tipoLuogo = tipoLuogo;
		this.provincia_studio_lavoro = provincia_studio_lavoro;
		this.comune_studio_laboro = comune_studio_laboro;
		this.codIstatdest="15"+provincia_studio_lavoro+comune_studio_laboro;
		this.paese_estero = paese_estero;
		this.mezzo = mezzo;
		this.orario_uscita = orario_uscita;
		this.tempo = tempo;
		this.stima_numero_individui = stima_numero_individui;
		this.numero_individui = numero_individui;
		this.part_loc=part_loc;
		this.dest_loc=dest_loc;
	}
	
	public String getPart_loc() {
		return part_loc;
	}

	public void setPart_loc(String part_loc) {
		this.part_loc = part_loc;
	}

	public String getDest_loc() {
		return dest_loc;
	}

	public void setDest_loc(String dest_loc) {
		this.dest_loc = dest_loc;
	}

	public String getCodIstatdest() {
		return codIstatdest;
	}

	public void setCodIstatdest(String codIstatdest) {
		this.codIstatdest = codIstatdest;
	}

	public String getTipo_record() {
		return tipo_record;
	}
	public void setTipo_record(String tipo_record) {
		this.tipo_record = tipo_record;
	}
	public Integer getTipo_residenza() {
		return tipo_residenza;
	}
	public void setTipo_residenza(Integer tipo_residenza) {
		this.tipo_residenza = tipo_residenza;
	}
	public String getCodISTAT() {
		return codISTAT;
	}
	public void setCodISTAT(String codISTAT) {
		this.codISTAT = codISTAT;
	}
	public Integer getSesso() {
		return sesso;
	}
	public void setSesso(Integer sesso) {
		this.sesso = sesso;
	}
	public String getMotivoSpostamento() {
		return motivoSpostamento;
	}
	public void setMotivoSpostamento(String motivoSpostamento) {
		this.motivoSpostamento = motivoSpostamento;
	}
	public String getTipoLuogo() {
		return tipoLuogo;
	}
	public void setTipoLuogo(String tipoLuogo) {
		this.tipoLuogo = tipoLuogo;
	}
	public String getProvincia_studio_lavoro() {
		return provincia_studio_lavoro;
	}
	public void setProvincia_studio_lavoro(String provincia_studio_lavoro) {
		this.provincia_studio_lavoro = provincia_studio_lavoro;
	}
	public String getComune_studio_laboro() {
		return comune_studio_laboro;
	}
	public void setComune_studio_laboro(String comune_studio_laboro) {
		this.comune_studio_laboro = comune_studio_laboro;
	}
	public String getPaese_estero() {
		return paese_estero;
	}
	public void setPaese_estero(String paese_estero) {
		this.paese_estero = paese_estero;
	}
	public String getMezzo() {
		return mezzo;
	}
	public void setMezzo(String mezzo) {
		this.mezzo = mezzo;
	}
	public String getOrario_uscita() {
		return orario_uscita;
	}
	public void setOrario_uscita(String orario_uscita) {
		this.orario_uscita = orario_uscita;
	}
	public String getTempo() {
		return tempo;
	}
	public void setTempo(String tempo) {
		this.tempo = tempo;
	}
	public Double getStima_numero_individui() {
		return stima_numero_individui;
	}
	public void setStima_numero_individui(Double stima_numero_individui) {
		this.stima_numero_individui = stima_numero_individui;
	}
	public Integer getNumero_individui() {
		return numero_individui;
	}
	public void setNumero_individui(Integer numero_individui) {
		this.numero_individui = numero_individui;
	}
	@Override
	public String toString() {
		return "GISCampaniaPendolo "+part_loc+"->"+dest_loc+" [tipo_record=" + tipo_record
				+ ", tipo_residenza=" + tipo_residenza + ", codISTAT="
				+ codISTAT + ", sesso=" + sesso + ", motivoSpostamento="
				+ motivoSpostamento + ", tipoLuogo=" + tipoLuogo
				+ ", provincia_studio_lavoro=" + provincia_studio_lavoro
				+ ", comune_studio_laboro=" + comune_studio_laboro
				+ ", paese_estero=" + paese_estero + ", mezzo=" + mezzo
				+ ", orario_uscita=" + orario_uscita + ", tempo=" + tempo
				+ ", stima_numero_individui=" + stima_numero_individui
				+ ", numero_individui=" + numero_individui + ", codIstatdest="
				+ codIstatdest + "]";
	}
	
	
}
