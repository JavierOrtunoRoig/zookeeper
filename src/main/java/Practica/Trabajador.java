package Practica;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.CreateMode;

import java.util.Random;

public class Trabajador {

    public static String worker = null;
    public static void main(String[] args) throws Exception {

        CuratorFramework client = ConexionZooKeeper.conexion();

        //Creo mu Id de Worker
        Random rnd = new Random();
        worker = "Worker - ";
        worker = worker.concat(String.valueOf(rnd.nextInt(Integer.MAX_VALUE)));
        createFreeWorker(client, worker);

        CuratorCacheWorker(client);
        while (true) {}
    }

    public static void CuratorCacheWorker(CuratorFramework client) {
        //TODO: Poner con nodos intermedios

        CuratorCache cacheTask = CuratorCache.build(client, "/zAssign/" + worker );
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forCreates(node ->
                {
                    try {
                        String tarea = node.getPath().substring(node.getPath().lastIndexOf("/"));
                        if (tarea.charAt(1) == 'N') {
                            System.out.println("Yo, " + worker + ", voy a trabajar con la tarea " + tarea);
                            deleteTaskfromZNode(client, tarea);

                            Thread.sleep(5000);
                            System.out.println("\nTAREA ACABADA!!");
                            deleteFromAsign(client, worker, tarea);
                            writeInZDone(client, tarea);
                            createFreeWorker(client, worker);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                })
                .forChanges((oldNode, newNode) -> System.out.println("Cache changed"))
                .build();

        // Registrar listener
        cacheTask.listenable().addListener(listener);
        cacheTask.start();
    }

    public static void createFreeWorker(CuratorFramework client, String worker) throws Exception {
        System.out.println("Se va a escribir en /zWorkers/" + worker);
        client.create().orSetData().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/zWorkers/" + worker, worker.getBytes());
        System.out.println("Se ha escrito en /zWorkers/" + worker);
    }

    public static boolean iHaveTask(CuratorFramework client, String worker) throws Exception {
        return client.checkExists().forPath("/zAssign/" + worker) != null;
    }

    private static void printMyTask(CuratorFramework client, String worker) throws Exception {
        System.out.println("Yo, " + worker + ", voy a trbajar con la tarea " +
                new String(client.getData().forPath("/zAssign/" + worker + "/" + new String(client.getData().forPath("/zTask")))));
    }

    private static void deleteTaskfromZNode(CuratorFramework client, String tarea) throws Exception {
        if (client.checkExists().forPath("/zTask" + tarea) != null) { //&& new String(client.getData().forPath("/zTask"+ tarea)).equals(tarea.substring(1))) {
            client.delete().forPath("/zTask" + tarea);
        }
    }

    private static void deleteFromAsign(CuratorFramework client, String worker, String tarea) throws Exception {
        client.delete().forPath("/zAssign/" + worker + "/" + tarea.substring(1));
        client.delete().forPath("/zAssign/" + worker);
    }

    private static void writeInZDone(CuratorFramework client, String tarea) throws Exception {
        System.out.println("Se va a crear el nodo " + tarea.substring(1) + " en /zDone/"+ tarea.substring(1));
        client.create().orSetData().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/zDone/" + tarea.substring(1), "a".getBytes());
        System.out.println("Se ha creado el nodo " + tarea.substring(1) + " en /zDone/" + tarea.substring(1) + "\n\n\n\n");
    }
}
