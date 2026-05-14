import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class Main {

    // Método para limpiar la pantalla (Secuencia ANSI)
    public static void limpiarPantalla() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // Método para pausar la ejecución
    public static void presionarEnter(Scanner entrada) {
        System.out.println("\nPresione ENTER para volver al menú...");
        entrada.nextLine();
    }

    public static void main(String[] args) {
        // Datos de conexión
        String url = "jdbc:mysql://localhost:3306/BDNotas";
        String usuario = "root";
        String contraseña = "jleo037"; 

        Scanner entrada = new Scanner(System.in);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conexion = DriverManager.getConnection(url, usuario, contraseña);
            
            int opcion;

            do {
                limpiarPantalla();
                System.out.println("========= CONTROL DE NOTAS - UMG =========");
                System.out.println("1. Ingreso de Alumnos");
                System.out.println("2. Ingreso/Actualización de Notas");
                System.out.println("3. Eliminar Alumnos");
                System.out.println("4. Actualizar datos y notas de alumnos");
                System.out.println("5. Buscar alumnos por Carnet o por Nombre");
                System.out.println("6. Obtener Promedios por Sección");
                System.out.println("7. Listar Alumnos");
                System.out.println("8. Salir");
                System.out.print("\nSeleccione una opción: ");
                
                opcion = entrada.nextInt();
                entrada.nextLine(); // Limpiar buffer

                switch (opcion) {
                    case 1: 
                        limpiarPantalla();
                        System.out.println("=== INGRESO DE ALUMNOS ===");
                        System.out.print("Carnet: ");
                        String carnet = entrada.nextLine();
                        System.out.print("Nombres: ");
                        String nombres = entrada.nextLine();
                        System.out.print("Apellidos: ");
                        String apellidos = entrada.nextLine();
                        System.out.print("Sección (A/B): ");
                        String seccion = entrada.nextLine();

                        try {
                            String sql = "INSERT INTO alumnos(carnet, nombres, apellidos, seccion) VALUES (?, ?, ?, ?)";
                            PreparedStatement ps = conexion.prepareStatement(sql);
                            ps.setString(1, carnet);
                            ps.setString(2, nombres);
                            ps.setString(3, apellidos);
                            ps.setString(4, seccion);
                            ps.executeUpdate();
                            System.out.println("\n¡Alumno registrado con éxito!");
                        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                            System.out.println("\nERROR: El carnet ya existe.");
                        }
                        presionarEnter(entrada);
                        break;

                    case 2: 
                        limpiarPantalla();
                        System.out.println("=== INGRESO DE NOTAS ===");
                        System.out.print("Carnet del alumno: ");
                        String cNota = entrada.nextLine();
                        
                        // Verificar si ya tiene nota para decidir entre INSERT o UPDATE
                        PreparedStatement psCh = conexion.prepareStatement("SELECT nota FROM notas WHERE carnet = ?");
                        psCh.setString(1, cNota);
                        ResultSet rsCh = psCh.executeQuery();

                        System.out.print("Nota a ingresar: ");
                        double nNota = entrada.nextDouble();
                        entrada.nextLine();

                        if (rsCh.next()) {
                            PreparedStatement psUp = conexion.prepareStatement("UPDATE notas SET nota = ? WHERE carnet = ?");
                            psUp.setDouble(1, nNota);
                            psUp.setString(2, cNota);
                            psUp.executeUpdate();
                            System.out.println("\nNota actualizada correctamente.");
                        } else {
                            try {
                                PreparedStatement psIn = conexion.prepareStatement("INSERT INTO notas(carnet, nota) VALUES (?, ?)");
                                psIn.setString(1, cNota);
                                psIn.setDouble(2, nNota);
                                psIn.executeUpdate();
                                System.out.println("\nNota guardada.");
                            } catch (Exception e) {
                                System.out.println("\nERROR: El alumno no existe.");
                            }
                        }
                        presionarEnter(entrada);
                        break;

                    case 3: 
                        limpiarPantalla();
                        System.out.println("=== ELIMINAR ALUMNO ===");
                        System.out.print("Carnet del alumno a eliminar: ");
                        String cEli = entrada.nextLine();
                        System.out.print("¿Está seguro de eliminar este registro? (si/no): ");
                        if (entrada.nextLine().equalsIgnoreCase("si")) {
                            conexion.prepareStatement("DELETE FROM notas WHERE carnet = '" + cEli + "'").executeUpdate();
                            if (conexion.prepareStatement("DELETE FROM alumnos WHERE carnet = '" + cEli + "'").executeUpdate() > 0)
                                System.out.println("Registro eliminado.");
                            else System.out.println("Alumno no encontrado.");
                        }
                        presionarEnter(entrada);
                        break;

                    case 4: 
                        limpiarPantalla();
                        System.out.println("=== ACTUALIZAR DATOS ===");
                        System.out.print("Carnet del alumno: ");
                        String cA = entrada.nextLine();
                        System.out.println("\n1. Datos Personales\n2. Nota\nSeleccione: ");
                        int sub = entrada.nextInt(); entrada.nextLine();
                        if(sub == 1) {
                            System.out.print("Nombres: "); String n = entrada.nextLine();
                            System.out.print("Apellidos: "); String a = entrada.nextLine();
                            PreparedStatement ps = conexion.prepareStatement("UPDATE alumnos SET nombres=?, apellidos=? WHERE carnet=?");
                            ps.setString(1, n); ps.setString(2, a); ps.setString(3, cA);
                            ps.executeUpdate();
                        } else {
                            System.out.print("Nueva Nota: "); double nt = entrada.nextDouble(); entrada.nextLine();
                            PreparedStatement ps = conexion.prepareStatement("UPDATE notas SET nota=? WHERE carnet=?");
                            ps.setDouble(1, nt); ps.setString(2, cA);
                            ps.executeUpdate();
                        }
                        System.out.println("Información actualizada.");
                        presionarEnter(entrada);
                        break;

                    case 5: 
                        limpiarPantalla();
                        System.out.println("=== BUSCAR ALUMNO ===");
                        System.out.print("1. Por Carnet\n2. Por Nombre\nSeleccione: ");
                        int m = entrada.nextInt(); entrada.nextLine();
                        String qB = "SELECT a.carnet, a.nombres, a.apellidos, a.seccion, n.nota FROM alumnos a LEFT JOIN notas n ON a.carnet = n.carnet WHERE ";
                        qB += (m == 1) ? "a.carnet = ?" : "a.nombres LIKE ?";
                        
                        System.out.print("Texto a buscar: ");
                        String valorBusqueda = entrada.nextLine();
                        
                        PreparedStatement psB = conexion.prepareStatement(qB);
                        psB.setString(1, (m == 1) ? valorBusqueda : "%" + valorBusqueda + "%");
                        ResultSet rsB = psB.executeQuery();
                        
                        while (rsB.next()) {
                            System.out.println("\n[" + rsB.getString("carnet") + "] " + rsB.getString("nombres") + " " + rsB.getString("apellidos"));
                            System.out.println("Sección: " + rsB.getString("seccion") + " | Nota: " + rsB.getDouble("nota"));
                        }
                        presionarEnter(entrada);
                        break;

                    case 6: 
                        limpiarPantalla();
                        System.out.println("=== PROMEDIOS POR SECCIÓN ===");
                        ResultSet rsP = conexion.createStatement().executeQuery("SELECT a.seccion, AVG(n.nota) FROM alumnos a JOIN notas n ON a.carnet = n.carnet GROUP BY a.seccion");
                        while (rsP.next()) System.out.println("Sección " + rsP.getString(1) + ": " + String.format("%.2f", rsP.getDouble(2)));
                        presionarEnter(entrada);
                        break;

                    case 7: 
                        limpiarPantalla();
                        System.out.println("=== LISTADO DE ALUMNOS ===");
                        System.out.print("Sección (A/B): ");
                        String sL = entrada.nextLine();
                        System.out.println("Ordenar por: 1.Carnet 2.Nombre 3.Nota");
                        int o = entrada.nextInt(); entrada.nextLine();
                        String col = switch(o) { case 2 -> "a.nombres ASC"; case 3 -> "n.nota DESC"; default -> "a.carnet ASC"; };
                        
                        String sQL = "SELECT a.carnet, a.nombres, a.apellidos, n.nota FROM alumnos a LEFT JOIN notas n ON a.carnet = n.carnet WHERE a.seccion = ? ORDER BY " + col;
                        PreparedStatement psL = conexion.prepareStatement(sQL);
                        psL.setString(1, sL);
                        ResultSet rsL = psL.executeQuery();
                        System.out.printf("\n%-12s %-25s %-5s\n", "CARNET", "NOMBRE", "NOTA");
                        System.out.println("--------------------------------------------");
                        while (rsL.next()) System.out.printf("%-12s %-25s %-5.2f\n", rsL.getString(1), rsL.getString(2) + " " + rsL.getString(3), rsL.getDouble(4));
                        presionarEnter(entrada);
                        break;
                }
            } while (opcion != 8);
            conexion.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}