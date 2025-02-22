package clasesHibernate;
// Generated 4 feb 2025 10:27:30 by Hibernate Tools 6.5.1.Final

import java.util.HashSet;
import java.util.Set;

/**
 * Equipos generated by hbm2java
 */
public class Equipos implements java.io.Serializable {

	private int id;
	private Deportes deportes;
	private String nombre;
	private String pais;
	private Set jugadoreses = new HashSet(0);

	public Equipos() {
	}

	public Equipos(int id, String nombre, String pais) {
		this.id = id;
		this.nombre = nombre;
		this.pais = pais;
	}

	public Equipos(Deportes deportes, String nombre, String pais, Set jugadoreses) {
		this.deportes = deportes;
		this.nombre = nombre;
		this.pais = pais;
		this.jugadoreses = jugadoreses;
	}

	public Equipos(int id, Deportes deportes, String nombre, String pais, Set jugadoreses) {
		this.id = id;
		this.deportes = deportes;
		this.nombre = nombre;
		this.pais = pais;
		this.jugadoreses = jugadoreses;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Deportes getDeportes() {
		return this.deportes;
	}

	public void setDeportes(Deportes deportes) {
		this.deportes = deportes;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getPais() {
		return this.pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	public Set getJugadoreses() {
		return this.jugadoreses;
	}

	public void setJugadoreses(Set jugadoreses) {
		this.jugadoreses = jugadoreses;
	}

	@Override
	public String toString() {
		return "Equipos [id=" + id + ", deportes=" + deportes + ", nombre=" + nombre + ", pais=" + pais + "]";
	}

}
