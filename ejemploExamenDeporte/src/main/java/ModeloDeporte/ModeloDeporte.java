package ModeloDeporte;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;

import org.bson.Document;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import clasesHibernate.Deportes;
import clasesHibernate.Equipos;
import clasesHibernate.Jugadores;
import jakarta.persistence.TypedQuery;
import net.xqj.exist.ExistXQDataSource;

public class ModeloDeporte {
	private static final String USERNAME = "admin";
	private static final String PASSWORD = "toor";

	// HIBERNATE
	private static final Configuration cfg = new Configuration().configure();
	private static final SessionFactory sf = cfg.buildSessionFactory();
	private static Session sesion;

	// MONGO
	private static MongoClient cliente;
	private static MongoDatabase db;
	private static String connectionString = "mongodb://localhost:27017/";

	// ExistDB
	private static XQConnection conexionXQJ;
	private static Collection col;

	private static ArrayList<Deportes> listDeportes = new ArrayList<Deportes>();
	private static ArrayList<Equipos> listEquipos = new ArrayList<Equipos>();
	private static ArrayList<Jugadores> listJugadores = new ArrayList<Jugadores>();

	private static final List<String> deportesValidos = Arrays.asList(new String[] { "Fútbol", "Baloncesto" });
	private static final List<String> equiposValidos = Arrays.asList(new String[] { "España", "Inglaterra" });
	private static final List<String> jugadoresValidos = Arrays.asList(new String[] { "Delantero", "Centrocampista" });

	public static void main(String[] args) {
		conectarCompass();
		conectarseAXQJ(USERNAME, PASSWORD);
		col = conectarseAXMLDB(USERNAME, PASSWORD, "/db/coleccionCreadaAhora");
		menu();
		desconectarCompass();
	}

	public static void conectarseAXQJ(String username, String password) {
		XQDataSource dataSource = new ExistXQDataSource();
		try {
			dataSource.setProperty("serverName", "localhost");
			dataSource.setProperty("port", "8080");
			dataSource.setProperty("user", username);
			dataSource.setProperty("password", password);
			conexionXQJ = dataSource.getConnection();
		} catch (XQException e) {
			e.printStackTrace();
		}
	}

	public static Collection conectarseAXMLDB(String username, String password, String URICollection) {
		try {
			// Cargar la clase de la base de datos de eXist-DB
			Class<?> cl = Class.forName("org.exist.xmldb.DatabaseImpl");
			Database database = (Database) cl.getDeclaredConstructor().newInstance();

			// Registrar la base de datos en el DatabaseManager
			DatabaseManager.registerDatabase(database);

			// Conectar a la colección
			String connectionURI = "xmldb:exist://localhost:8080/exist/xmlrpc" + URICollection;
			Collection col = DatabaseManager.getCollection(connectionURI, username, password);
			return col;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void crearBorrarColeccion(Collection collection, String nombreColeccion, boolean crear) {
		if (collection == null) {
			System.out.println("La colección padre no existe");
			return;
		}

		try {
			CollectionManagementService serviCollectionManagement = (CollectionManagementService) collection
					.getService("CollectionManagementService", "1.0");
			if (crear) {
				serviCollectionManagement.createCollection(nombreColeccion);
				System.out.println("Colección creada correctamente");
			} else {
				serviCollectionManagement.removeCollection(nombreColeccion);
				System.out.println("Colección eliminada correctamente");
			}

		} catch (XMLDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void menu() {
		String eleccion = "";
		while (!eleccion.equalsIgnoreCase("X")) {
			Scanner sc = new Scanner(System.in);
			System.out.println("Elige la opción que quieras:" + "\n1-Pasar datos de mongoDB a MySQL"
					+ "\n2-Pasar de MySQL a existDB" + "\n3-Pasar de mongoDB a existBD" + "\nX-Salir");
			eleccion = sc.next().toLowerCase();
			switch (eleccion) {
			case "1":
				if (pasarDeMongoAMySQL()) {
					System.out.println("Traspaso realizado correctamente de mongo a mysql");
				} else {
					System.out.println("Error realizando el traspaso de mongo a mysql");
				}
				break;
			case "2":
				if (pasarDeMysQLAExistDB(col, "deportes.xml", "equipos.xml", "jugadores.xml")) {
					System.out.println("Traspaso realizado correctamente de mysql a existDB");
				} else {
					System.out.println("Error realizando el traspaso de mysql a existDB");
				}

				break;
			case "3":
				if (pasarDeMysQLAExistDB(col, "deportes.xml", "equipos.xml", "jugadores.xml")) {
					System.out.println("Traspaso realizado correctamente de mongodb a existDB");
				} else {
					System.out.println("Error realizando el traspaso de mongodb a existDB");
				}
				break;

			case "x":
				if (desconectarCompass()) {
					System.out.println("Desconectado correctamente de la base de datos");
				}
				System.out.println("Saliendo del programa");

				break;
			default:
				break;
			}
		}
	}

	public static boolean conectarCompass() {
		try {
			cliente = MongoClients.create(connectionString);
			db = cliente.getDatabase("deporte");
			return true;
		} catch (Exception e) {
			return false;

		}

	}

	public static boolean desconectarCompass() {
		try {
			cliente.close();
			return true;
		} catch (Exception e) {
			return false;

		}

	}

	private static boolean pasarDeMongoAMySQL() {
		try {
			listarDeportes();
			anadirDeportesAMySQL();
			listarEquipos();
			anadirEquiposAMySQL();
			listarJugadores();
			anadirJugadoresAMySQL();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	private static void listarDeportes() {

		listDeportes.clear();

		FindIterable<Document> listaDocs = db.getCollection("deporte").find();
		// Llenamos lista deportes
		for (Document doc : listaDocs) {
			if (deportesValidos.contains(doc.getString("deporte"))) {
				Deportes tempDept = new Deportes(doc.getString("deporte"), new HashSet());
				listDeportes.add(tempDept);
			}
		}

	}

	private static void anadirDeportesAMySQL() {
		for (Deportes d : listDeportes) {
			anadirDeporte(d);

		}
		listarDeportesMySQL();
		for (Deportes d : listDeportes) {
			System.out.println(d.toString());
		}

	}

	private static void anadirEquiposAMySQL() {
		for (Equipos e : listEquipos) {
			anadirEquipo(e);
		}
		listarEquiposMySQL();
		for (Equipos e : listEquipos) {
			System.out.println(e.toString());
			;
		}

	}

	private static void anadirJugadoresAMySQL() {
		for (Jugadores j : listJugadores) {
			anadirJugador(j);
		}
		listarJugadoresMySQL();
		for (Jugadores j : listJugadores) {
			System.out.println(j.toString());
		}
	}

	private static void listarEquipos() {
		listEquipos.clear();

		FindIterable<Document> listaDocs = db.getCollection("deporte").find();
		// Llenamos lista deportes
		for (Document doc : listaDocs) {
			if (deportesValidos.contains(doc.getString("deporte"))) {
				List<Document> equipos = (List<Document>) doc.get("equipos");
				for (Document equipo : equipos) {
					if (equiposValidos.contains(equipo.getString("pais"))) {
						for (Deportes d : listDeportes) {
							if (d.getNombre().equals(doc.getString("deporte"))) {
								Equipos tempEq = new Equipos(d, equipo.getString("nombre"), equipo.getString("pais"),
										new HashSet());
								listEquipos.add(tempEq);

							}
						}
					}
				}
			}
		}
	}

	private static void listarJugadores() {
		listJugadores.clear();

		try {

			FindIterable<Document> listaDocs = db.getCollection("deporte").find();
			// Llenamos lista deportes
			for (Document doc : listaDocs) {
				if (deportesValidos.contains(doc.getString("deporte"))) {
					List<Document> equipos = (List<Document>) doc.get("equipos");
					for (Document equipo : equipos) {
						if (equiposValidos.contains(equipo.getString("pais"))) {
							List<Document> jugadores = (List<Document>) equipo.get("jugadores");
							for (Document jugador : jugadores) {
								if (jugadoresValidos.contains(jugador.getString("posicion"))
										&& jugador.getInteger("edad") > 25) {
									for (Equipos e : listEquipos) {
										if (e.getNombre().equals(equipo.getString("nombre"))) {
											Jugadores jugTemp = new Jugadores(e, jugador.getString("nombre"),
													jugador.getInteger("edad"), jugador.getString("posicion"),
													jugador.getString("nacionalidad"));
											listJugadores.add(jugTemp);
										}

									}

								}

							}

						}
					}

				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void listarDeportesMySQL() {
		listDeportes.clear();
		try {
			String hqlQuery = "from Deportes";
			sesion = sf.openSession();
			TypedQuery<Deportes> tqDep = sesion.createQuery(hqlQuery, Deportes.class);
			listDeportes = (ArrayList<Deportes>) tqDep.getResultList();
			if (listDeportes.size() > 0) {
				System.out.println("List deportes rellenada");
			} else {
				System.out.println("List deportes no rellenada");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("List deportes no rellenada");
		}

	}

	private static void listarEquiposMySQL() {
		listEquipos.clear();
		try {
			String hqlQuery = "from Equipos";
			sesion = sf.openSession();
			TypedQuery<Equipos> tqDep = sesion.createQuery(hqlQuery, Equipos.class);
			listEquipos = (ArrayList<Equipos>) tqDep.getResultList();
			if (listEquipos.size() > 0) {
				System.out.println("List equipos rellenada");
			} else {
				System.out.println("List equipos no rellenada");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("List equipos no rellenada");
		}

	}

	private static void listarJugadoresMySQL() {
		listJugadores.clear();
		try {
			String hqlQuery = "from Jugadores";
			sesion = sf.openSession();
			TypedQuery<Jugadores> tqDep = sesion.createQuery(hqlQuery, Jugadores.class);
			listJugadores = (ArrayList<Jugadores>) tqDep.getResultList();
			if (listJugadores.size() > 0) {
				System.out.println("List equipos rellenada");
			} else {
				System.out.println("List equipos no rellenada");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("List equipos no rellenada");
		}

	}

	private static void anadirDeporte(Deportes d) {
		if (!comprobarExistenciaDeporte(d)) {
			sesion = sf.openSession();
			Transaction t = sesion.beginTransaction();
			sesion.persist(d);
			t.commit();
			if (comprobarExistenciaDeporte(d)) {
				System.out.println("Deporte añadido correctamente");
			} else {
				System.out.println("Error añadiendo el deporte");
			}

		}

	}

	private static boolean comprobarExistenciaDeporte(Deportes d) {
		sesion = sf.openSession();
		String hql = "from Deportes where nombre='" + d.getNombre() + "'";
		TypedQuery<?> tqDeportes = sesion.createQuery(hql, Deportes.class);
		// resultado de la query
		ArrayList<Deportes> tqDpto = (ArrayList<Deportes>) tqDeportes.getResultList();
		if (tqDpto.size() >= 1) {
			return true;
		}

		return false;
	}

	private static void anadirEquipo(Equipos e) {
		if (!comprobarExistenciaEquipo(e)) {
			sesion = sf.openSession();
			Transaction t = sesion.beginTransaction();
			sesion.persist(e);
			t.commit();
			if (comprobarExistenciaEquipo(e)) {
				System.out.println("Equipo añadido correctamente");
			} else {
				System.out.println("Error añadiendo el equipo");
			}
		}

	}

	private static boolean comprobarExistenciaEquipo(Equipos e) {
		sesion = sf.openSession();
		String hql = "from Equipos where nombre='" + e.getNombre() + "' and pais='" + e.getPais() + "'";
		TypedQuery<?> tqEquipos = sesion.createQuery(hql, Equipos.class);
		// resultado de la query
		ArrayList<Equipos> tqDpto = (ArrayList<Equipos>) tqEquipos.getResultList();
		if (tqDpto.size() >= 1) {
			return true;
		}

		return false;
	}

	private static void anadirJugador(Jugadores j) {
		if (!comprobarExistenciaJugador(j)) {
			sesion = sf.openSession();
			Transaction t = sesion.beginTransaction();
			sesion.persist(j);
			t.commit();
			if (comprobarExistenciaJugador(j)) {
				System.out.println("Equipo añadido correctamente");
			} else {
				System.out.println("Error añadiendo el equipo");
			}
		}

	}

	private static boolean comprobarExistenciaJugador(Jugadores j) {
		sesion = sf.openSession();
		String hql = "from Jugadores where nombre='" + j.getNombre() + "' and edad='" + j.getEdad()
				+ "' and posicion ='" + j.getPosicion() + "' and nacionalidad ='" + j.getNacionalidad() + "'";
		TypedQuery<?> tqJugadores = sesion.createQuery(hql, Equipos.class);
		// resultado de la query
		ArrayList<Jugadores> tqDpto = (ArrayList<Jugadores>) tqJugadores.getResultList();
		if (tqDpto.size() >= 1) {
			return true;
		}

		return false;
	}

	private static boolean pasarDeMysQLAExistDB(Collection collection, String nombreArchivoDeportes,
			String nombreArchivoEquipos, String nombreArchivoJugadores) {
		if (collection == null) {
			System.out.println("La colección padre no existe");
			return false;
		}

		try {
			if (importarDeportesDesdeListAExistDB(collection, nombreArchivoDeportes)) {
				if (importarEquiposDesdeListAExistDB(collection, nombreArchivoEquipos)) {
					if (importarJugadoresDesdeListAExistDB(collection, nombreArchivoJugadores)) {
						return true;
					}

				}
			}
			return false;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	private static boolean importarJugadoresDesdeListAExistDB(Collection collection, String nombreArchivoJugadores) {
		if (collection == null) {
			System.out.println("La colección padre no existe");
			return false;
		}

		try {
			Resource recursoExiste = collection.getResource(nombreArchivoJugadores);
			if (recursoExiste != null) {
				System.out.println("El archivo ya existe, añadiendo datos nuevos");
				return anadirModuloJugador();

			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		String contenido = "";

		for (Jugadores j : listJugadores) {
			contenido += "<jugador><nombre id_equipo='" + j.getEquipos().getId() + "'>" + j.getNombre() + "</nombre>"
					+ "<edad>" + j.getEdad() + "</edad>" + "<posicion>" + j.getPosicion() + "</posicion> </jugador>";
		}
		contenido = "<jugadores>" + contenido + "</jugadores>";
		try {
			Resource recurso = collection.createResource(nombreArchivoJugadores, XMLResource.RESOURCE_TYPE);
			recurso.setContent(contenido);
			collection.storeResource(recurso);
			return true;

		} catch (XMLDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	private static boolean anadirModuloJugador() {
		for (Jugadores j : listJugadores) {
			if (!buscarJugadorExistDB(j)) {
				String query = "update insert <jugador><nombre id_equipo='" + j.getEquipos().getId() + "'>"
						+ j.getNombre() + "</nombre><edad>" + j.getEdad() + "</edad>" + "<posicion>" + j.getPosicion()
						+ "</posicion> </jugador> into doc('/db/coleccionCreadaAhora/jugadores.xml')/empleados";
				XQExpression xqe;
				try {
					xqe = conexionXQJ.createExpression();
					xqe.executeCommand(query);
					return true;
				} catch (XQException ex) {
					ex.printStackTrace();
					return false;
				}
			}
		}
		return false;

	}

	private static boolean buscarJugadorExistDB(Jugadores j) {
		try {
			String query = "for $e in doc('/db/coleccionCreadaAhora/jugadores.xml')/jugadores/jugador\n"
					+ "where $e/nombre = '" + j.getNombre() + "' and $e/edad = '" + j.getEdad() + "'"
					+ " and $e/posicion = '" + j.getPosicion() + "' and $e/nacionalidad = '" + j.getNacionalidad()
					+ "' and $e/nombre/@id_quipo = '" + j.getEquipos().getId() + "'\nreturn $e";
			XQExpression xqe = conexionXQJ.createExpression();
			XQResultSequence xqresultado = xqe.executeQuery(query);
			return xqresultado.next();

		} catch (XQException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

		return false;
	}

	private static boolean importarEquiposDesdeListAExistDB(Collection collection, String nombreArchivoEquipos) {
		if (collection == null) {
			System.out.println("La colección padre no existe");
			return false;
		}

		try {
			Resource recursoExiste = collection.getResource(nombreArchivoEquipos);
			if (recursoExiste != null) {
				System.out.println("El archivo ya existe, añadiendo datos nuevos");
				return anadirModuloEquipo();

			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		String contenido = "";

		for (Equipos eq : listEquipos) {
			contenido += "<equipo><nombre id_deporte='" + eq.getDeportes().getId() + "'>" + eq.getNombre() + "</nombre>"
					+ "<pais>" + eq.getPais() + "</pais></equipo>";
		}
		contenido = "<equipos>" + contenido + "</equipos>";
		try {
			Resource recurso = collection.createResource(nombreArchivoEquipos, XMLResource.RESOURCE_TYPE);
			recurso.setContent(contenido);
			collection.storeResource(recurso);
			return true;

		} catch (XMLDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	private static boolean anadirModuloEquipo() {
		for (Equipos eq : listEquipos) {
			if (!buscarEquipoExistDB(eq)) {
				String query = "update insert <equipo><nombre id_deporte='" + eq.getDeportes().getId() + "'>"
						+ eq.getNombre() + "</nombre><pais>" + eq.getPais() + "</pais>"
						+ "</equipo> into doc('/db/coleccionCreadaAhora/equipos.xml')/equipos";
				XQExpression xqe;
				try {
					xqe = conexionXQJ.createExpression();
					xqe.executeCommand(query);
					return true;
				} catch (XQException ex) {
					ex.printStackTrace();
					return false;
				}
			}
		}
		return false;

	}

	private static boolean buscarEquipoExistDB(Equipos eq) {
		try {
			String query = "for $e in doc('/db/coleccionCreadaAhora/equipos.xml')/equipos/equipo\n"
					+ "where $e/nombre = '" + eq.getNombre() + "' and $e/pais = '" + eq.getPais() + "'"
					+ " and $e/nombre/@id_deporte = '" + eq.getDeportes().getId() + "'\nreturn $e";
			XQExpression xqe = conexionXQJ.createExpression();
			XQResultSequence xqresultado = xqe.executeQuery(query);
			return xqresultado.next();

		} catch (XQException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

		return false;
	}

	private static boolean importarDeportesDesdeListAExistDB(Collection collection, String nombreArchivoDeportes) {
		if (collection == null) {
			System.out.println("La colección padre no existe");
			return false;
		}

		try {
			Resource recursoExiste = collection.getResource(nombreArchivoDeportes);
			if (recursoExiste != null) {
				System.out.println("El archivo ya existe, añadiendo datos nuevos");
				return anadirModuloDeportes();

			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		String contenido = "";

		for (Deportes d : listDeportes) {
			contenido += "<deporte><nombre id='" + d.getId() + "'>" + d.getNombre() + "</nombre></deporte>";
		}
		contenido = "<deportes>" + contenido + "</deportes>";
		try {
			Resource recurso = collection.createResource(nombreArchivoDeportes, XMLResource.RESOURCE_TYPE);
			recurso.setContent(contenido);
			collection.storeResource(recurso);
			return true;

		} catch (XMLDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	private static boolean anadirModuloDeportes() {
		for (Deportes d : listDeportes) {
			if (!buscarDeporteExistDB(d)) {
				String query = "update insert <deporte><nombre id'" + d.getId() + "'>" + d.getNombre()
						+ "</nombre> </deporte> into doc('/db/coleccionCreadaAhora/equipos.xml')/equipos";
				XQExpression xqe;
				try {
					xqe = conexionXQJ.createExpression();
					xqe.executeCommand(query);
					return true;
				} catch (XQException ex) {
					ex.printStackTrace();
					return false;
				}
			}
		}
		return false;

	}

	private static boolean buscarDeporteExistDB(Deportes d) {
		try {
			String query = "for $e in doc('/db/coleccionCreadaAhora/deportes.xml')/deportes/deporte\n"
					+ "where $e/nombre = '" + d.getNombre() + "' and $e/nombre/@id = '" + d.getId() + "'\nreturn $e";
			XQExpression xqe = conexionXQJ.createExpression();
			XQResultSequence xqresultado = xqe.executeQuery(query);
			return xqresultado.next();

		} catch (XQException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

		return false;
	}
}
