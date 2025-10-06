import java.util.Scanner;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class sumasrapidas {

    // Clase nodo para la lista doblemente enlazada
    static class JugadorNode {
        String nombre;
        int puntaje;
        JugadorNode prev;
        JugadorNode next;

        public JugadorNode(String nombre, int puntaje) {
            this.nombre = nombre;
            this.puntaje = puntaje;
            this.prev = null;
            this.next = null;
        }
    }

    // Lista doblemente enlazada para el ranking
    static class Ranking {
        private JugadorNode head;
        private JugadorNode tail;
        private int size;

        public void addJugador(String nombre, int puntaje) {
            JugadorNode nuevo = new JugadorNode(nombre, puntaje);
            if (head == null) {
                head = tail = nuevo;
            } else {
                // Insertar ordenado por puntaje (descendente)
                JugadorNode current = head;
                while (current != null && current.puntaje > puntaje) {
                    current = current.next;
                }
                if (current == null) {
                    // Insertar al final
                    tail.next = nuevo;
                    nuevo.prev = tail;
                    tail = nuevo;
                } else if (current == head) {
                    // Insertar al inicio
                    nuevo.next = head;
                    head.prev = nuevo;
                    head = nuevo;
                } else {
                    // Insertar en medio
                    nuevo.prev = current.prev;
                    nuevo.next = current;
                    current.prev.next = nuevo;
                    current.prev = nuevo;
                }
            }
            size++;

            // Mantener solo los 5 mejores
            if (size > 5) {
                tail = tail.prev;
                tail.next = null;
                size--;
            }
        }

        public void mostrarRanking() {
            System.out.println("\n=== RANKING DE LOS MEJORES JUGADORES ===");
            JugadorNode current = head;
            int pos = 1;
            while (current != null) {
                System.out.printf("%d. %s - %d puntos%n", pos++, current.nombre, current.puntaje);
                current = current.next;
            }
        }
    }

    private static Scanner scanner = new Scanner(System.in);
    private static Random random = new Random();
    private static Ranking ranking = new Ranking();

    public static void main(String[] args) {
        System.out.println("🎉 Bienvenido a Sumas Rápidas! 🎉");

        boolean jugarOtraVez = true;
        while (jugarOtraVez) {
            jugarPartida();
            System.out.print("\n¿Quieres jugar otra partida? (s/n): ");
            String respuesta = scanner.nextLine().trim().toLowerCase();
            jugarOtraVez = respuesta.startsWith("s");
        }

        System.out.println("¡Gracias por jugar! 👋");
    }

    private static void jugarPartida() {
        System.out.print("Ingresa tu nombre de usuario: ");
        String nombre = scanner.nextLine().trim();

        int puntaje = 0;
        int nivel = 1;
        int tiempoInicial = 10; // segundos

        System.out.println("\n🎮 ¡Comienza el juego! Tienes " + tiempoInicial + " segundos por operación.");

        while (true) {
            System.out.printf("\nNivel %d: Resuelve 5 sumas.\n", nivel);
            int aciertos = 0;

            for (int i = 0; i < 5; i++) {
                int a = random.nextInt(50) + 1;
                int b = random.nextInt(50) + 1;
                int resultadoCorrecto = a + b;

                System.out.printf("¿Cuánto es %d + %d? ", a, b);

                try {
                    int respuesta = obtenerRespuestaConTimeout(tiempoInicial);
                    if (respuesta == resultadoCorrecto) {
                        System.out.println("✅ Correcto!");
                        aciertos++;
                        puntaje += 100;
                    } else {
                        System.out.println("❌ Incorrecto. La respuesta era: " + resultadoCorrecto);
                        break;
                    }
                } catch (TimeoutException e) {
                    System.out.println("⏰ Se acabó el tiempo!");
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("❌ Valor inválido.");
                    break;
                }
            }

            if (aciertos < 5) {
                break; // Fin del juego si no completó el nivel
            }

            System.out.println("🎉 ¡Completaste el nivel " + nivel + "!");

            nivel++;
            tiempoInicial -= 2;
            if (tiempoInicial <= 0) {
                tiempoInicial = 1; // mínimo 1 segundo
            }
            System.out.printf("⏱️  Nuevo tiempo por operación: %d segundos\n", tiempoInicial);
        }

        System.out.printf("\n🏆 Puntaje final: %d puntos\n", puntaje);
        ranking.addJugador(nombre, puntaje);
        ranking.mostrarRanking();

        System.out.println("\nPresiona Enter para continuar...");
        scanner.nextLine();
    }

    // Método para obtener respuesta con timeout
    private static int obtenerRespuestaConTimeout(int segundos) throws TimeoutException, NumberFormatException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Integer> future = executor.submit(() -> {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        });

        try {
            int resultado = future.get(segundos, TimeUnit.SECONDS);
            executor.shutdown();
            return resultado;
        } catch (Exception e) {
            future.cancel(true);
            executor.shutdownNow();
            throw new TimeoutException();
        }
    }
}