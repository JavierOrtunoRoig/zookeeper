package Practica;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.CreateMode;

import java.util.Random;

public class Cliente {
    public static boolean tareaCompletada = false;
    public static String tarea;

    public static void main(String[] args) throws Exception {
        CuratorFramework client = ConexionZooKeeper.conexion();

        Random rnd = new Random();
        tarea = "NODE - ";
        tarea = tarea.concat(String.valueOf(rnd.nextInt(Integer.MAX_VALUE)));

        CuratorCache cacheDone = iniciarCacheDone(client);
        createData(client, tarea);

        while (true) {}
    }

    public static void createData(CuratorFramework client, String tarea) throws Exception {
        System.out.println("Se va a crear el nodo " + tarea + " en zTask");
        client.create().orSetData().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/zTask", tarea.getBytes());
        System.out.println("Se ha creado el nodo " + tarea + " en zTask");
    }

    public static CuratorCache iniciarCacheDone(CuratorFramework client) {
        CuratorCache cacheDone = CuratorCache.build(client, "/zDone");
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forCreates(node ->
                {
                    try {
                        byte[] datos = node.getData();

                        if (new String(datos).equals(tarea)) {
                            System.out.println("La tarea: " + new String(datos) + " ha sido completada.\n");
                            client.delete().forPath("/zDone");
                            createData(client, tarea);
                            //tareaCompletada = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .forInitialized(() -> System.out.println("Cache initialized"))
                .build();

        cacheDone.listenable().addListener(listener);
        cacheDone.start();

        return cacheDone;
    }
}
