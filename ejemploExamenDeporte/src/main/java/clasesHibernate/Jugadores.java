package clasesHibernate;
// Generated 4 feb 2025 10:27:30 by Hibernate Tools 6.5.1.Final

/**
 * Jugadores generated by hbm2java
 */
public class Jugadores implements java.io.Serializable {

	private int id;
	private Equipos equipos;
	private String nombre;
	private Integer edad;
	private String posicion;
	private String nacionalidad;

	public Jugadores() {
	}

	public Jugadores(int id, String nombre) {
		this.id = id;
		this.nombre = nombre;
	}

	public Jugadores(Equipos equipos, String nombre, Integer edad, String posicion, String nacionalidad) {
		this.equipos = equipos;
		this.nombre = nombre;
		this.edad = edad;
		this.posicion = posicion;
		this.nacionalidad = nacionalidad;
	}

	public Jugadores(int id, Equipos equipos, String nombre, Integer edad, String posicion, String nacionalidad) {
		this.id = id;
		this.equipos = equipos;
		this.nombre = nombre;
		this.edad = edad;
		this.posicion = posicion;
		this.nacionalidad = nacionalidad;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Equipos getEquipos() {
		return this.equipos;
	}

	public void setEquipos(Equipos equipos) {
		this.equipos = equipos;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getEdad() {
		return this.edad;
	}

	public void setEdad(Integer edad) {
		this.edad = edad;
	}

	public String getPosicion() {
		return this.posicion;
	}

	public void setPosicion(String posicion) {
		this.posicion = posicion;
	}

	public String getNacionalidad() {
		return this.nacionalidad;
	}

	public void setNacionalidad(String nacionalidad) {
		this.nacionalidad = nacionalidad;
	}

	@Override
	public String toString() {
		return "Jugadores [id=" + id + ", equipos=" + equipos + ", nombre=" + nombre + ", edad=" + edad + ", posicion="
				+ posicion + ", nacionalidad=" + nacionalidad + "]";
	}

}
