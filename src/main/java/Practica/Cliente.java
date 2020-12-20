package Practica;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.CreateMode;

import java.util.Random;

public class Cliente {
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
        System.out.println("Se va a crear el nodo " + tarea + " en zTask/" + tarea);
        client.create().orSetData().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/zTask/" + tarea, tarea.getBytes());
        System.out.println("Se ha creado el nodo " + tarea + " en zTask/" + tarea);
    }

    public static CuratorCache iniciarCacheDone(CuratorFramework client) {
        System.out.println("Cache escuchando en " + "/zDone/" + tarea);
        CuratorCache cacheDone = CuratorCache.build(client, "/zDone/" + tarea);
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forCreates(node ->
                {
                    try {

                        System.out.println("La tarea: ha sido completada.\n");
                        client.delete().forPath("/zDone/" + tarea);
                        createData(client, tarea);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .build();

        cacheDone.listenable().addListener(listener);
        cacheDone.start();

        return cacheDone;
    }
}
