package Practica;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.CreateMode;


public class Maestro {
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
        if (Maestro.task != null && Maestro.worker != null) {
            client.create().orSetData().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/zAssign/" + Maestro.worker + "/" + Maestro.task);
            Maestro.task = null;
            Maestro.worker = null;

            System.out.println("Tarea asignada\n\n\n");
        }
    }

    public static void CuratorCacheTask(CuratorFramework client) {

        CuratorCache cacheTask = CuratorCache.build(client, "/zTask");
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forCreates(node ->
                {
                    if (!node.getPath().equals("/zTask")) {
                        System.out.println("Maestro lee la nueva ruta: "+ node.getPath());
                        String datos = new String(node.getData());
                        task = datos;
                        try {
                            createTaskWithWorker(client);
                            client.delete().forPath(node.getPath());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
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
                    if (!node.getPath().equals("/zWorkers")) {
                        //String datos = new String(node.getData());
                        System.out.println("Maestro lee en " + node.getPath());
                        Maestro.worker = node.getPath().substring(node.getPath().lastIndexOf("/") + 1);
                        //Maestro.worker = datos;
                        try {
                            client.delete().forPath(node.getPath());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            createTaskWithWorker(client);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .build();

        // Registrar listener
        cacheWorker.listenable().addListener(listener);
        cacheWorker.start();
    }
}
