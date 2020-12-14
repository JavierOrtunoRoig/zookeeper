package BackUp;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

import java.util.Random;

public class Cliente {
    public static void main(String[] args) throws Exception {
        CuratorFramework client = ConexionZooKeeper.conexion();

        Random rnd = new Random();
        String tarea = "NODE - ";
        tarea = tarea.concat(String.valueOf(rnd.nextInt(Integer.MAX_VALUE)));

        while (true) {
            boolean tareaCompletada = false;

            createData(client, tarea);

            while (!tareaCompletada) {
                if (client.checkExists().forPath("/zDone") != null) {
                    byte[] tareasCompletadas = client.getData().forPath("/zDone");
                    System.out.println("La tarea: " + new String(tareasCompletadas) + " ha sido completada.\n");
                    client.delete().forPath("/zDone");
                    tareaCompletada = true;
                }

                /*if (client.checkExists().forPath("/zDone") != null) {
                    byte[] tareasCompletadas = client.getData().forPath("/zDone");

                    int i = 0;
                    while (i < tareasCompletadas.length && !tareaCompletada) {
                        if (tarea.equals(String.valueOf(tareasCompletadas[i]))) {
                                tareaCompletada = true;
                        }
                        i++;
                    }
                }*/
            }
            Thread.sleep(1000);
        }
    }

    public static void createData(CuratorFramework client, String tarea) throws Exception {
        System.out.println("Se va a crear el nodo " + tarea + " en zTask");
        client.create().orSetData().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/zTask", tarea.getBytes());
        System.out.println("Se ha creado el nodo " + tarea + " en zTask");
    }
}
