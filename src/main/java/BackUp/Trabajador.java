package BackUp;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

import java.util.Random;

public class Trabajador {
    public static void main(String[] args) throws Exception {

        CuratorFramework client = ConexionZooKeeper.conexion();

        //Creo mu Id de Worker
        Random rnd = new Random();
        String worker = "Worker - ";
        worker = worker.concat(String.valueOf(rnd.nextInt(Integer.MAX_VALUE)));

        while(true) {

            //Me pongo como trabajador libre
            boolean tareaCompletada = false;
            createFreeWorker(client, worker);

            while(!tareaCompletada) {
                if (iHaveTask(client, worker)) { //Si alguien ha puesto una tarea para mi
                    printMyTask(client, worker);
                    String tarea = new String(client.getData().forPath("/zTask"));
                    deleteTaskfromZNode(client);
                    tareaCompletada = true;

                    Thread.sleep(3000);
                    System.out.println("\nTAREA ACABADA!!");
                    deleteFromAsign(client, worker, tarea);
                    writeInZDone(client, tarea);
                    System.out.println();
                }
            }
        }
    }

    public static void createFreeWorker(CuratorFramework client, String worker) throws Exception {

        System.out.println("Se va a crear el nodo " + worker + "en zWorkers");
        client.create().orSetData().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/zWorkers", worker.getBytes());
        System.out.println("Se ha creado el nodo " + worker + "en zWorkers");
    }

    public static boolean iHaveTask(CuratorFramework client, String worker) throws Exception {
        return client.checkExists().forPath("/zAssign/" + worker) != null;
    }

    private static void printMyTask(CuratorFramework client, String worker) throws Exception {
        System.out.println("Yo, " + worker + ", voy a trbajar con la tarea " +
                new String(client.getData().forPath("/zAssign/" + worker + "/" + new String(client.getData().forPath("/zTask")))));
    }

    private static void deleteTaskfromZNode(CuratorFramework client) throws Exception {
        client.delete().forPath("/zTask");
    }

    private static void deleteFromAsign(CuratorFramework client, String worker, String tarea) throws Exception {
        client.delete().forPath("/zAssign/" + worker + "/" + tarea);
        client.delete().forPath("/zAssign/" + worker);
    }

    private static void writeInZDone(CuratorFramework client, String tarea) throws Exception {
        System.out.println("Se va a crear el nodo " + tarea + "en zDone");
        client.create().orSetData().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/zDone", tarea.getBytes());
        System.out.println("Se ha creado el nodo " + tarea + "en zDone \n\n");
    }
}
