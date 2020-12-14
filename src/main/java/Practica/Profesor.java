package Practica;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.CreateMode;


public class Profesor {
    public static String task = null;
    public static String worker = null;

    public static void main(String[] args) throws Exception {

        CuratorFramework client = ConexionZooKeeper.conexion();

        CuratorCacheWorkers(client);
        CuratorCacheTask(client);

        while(true) {
        }
    }

    public static void createTaskWithWorker(CuratorFramework client) throws Exception {
        if (Profesor.task != null && Profesor.worker != null) {
            client.create().orSetData().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/zAssign/" + Profesor.worker + "/" + Profesor.task);
            Profesor.task = null;
            Profesor.worker = null;

            System.out.println("Tarea asignada\n\n\n");
        }
    }

    public static void CuratorCacheTask(CuratorFramework client) {

        CuratorCache cacheTask = CuratorCache.build(client, "/zTask");
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forCreates(node ->
                {
                    String datos = new String(node.getData());
                    System.out.println("Maestro lee: " + datos + " en zTask");
                    task = datos;
                    try {
                        createTaskWithWorker(client);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .forInitialized(() -> System.out.println("Cache initialized"))
                .build();

        // Registrar listener
        cacheTask.listenable().addListener(listener);
        cacheTask.start();
    }

    public static void CuratorCacheWorkers(CuratorFramework client) {

        CuratorCache cacheWorker = CuratorCache.build(client, "/zWorkers");
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forCreates(node ->
                {
                    String datos = new String(node.getData());
                    System.out.println("Maestro lee: " + datos + " en zWorker");
                    Profesor.worker = datos;
                    try {
                        client.delete().forPath("/zWorkers");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        createTaskWithWorker(client);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .forInitialized(() -> System.out.println("Cache initialized"))
                .build();

        // Registrar listener
        cacheWorker.listenable().addListener(listener);
        cacheWorker.start();
    }
}
