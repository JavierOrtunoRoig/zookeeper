package BackUp;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.CreateMode;

public class Profesor {
    public static void main(String[] args) throws Exception {

        CuratorFramework client = ConexionZooKeeper.conexion();

        while (true) {
            byte[] datosLeidos, worker;
            if (client.checkExists().forPath("/zTask") != null) {
                datosLeidos = client.getData().forPath("/zTask");
                worker = client.getData().forPath("/zWorkers");
                System.out.println("Maestro lee: " + new String(client.getData().forPath("/zTask")));
                System.out.println("Maestro ve: " + new String(client.getData().forPath("/zWorkers")));

                client.delete().forPath("/zWorkers");
                //client.delete().forPath("/zTask");

                client.create().orSetData().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/zAssign/" + new String(worker) + "/" + new String (datosLeidos), datosLeidos);
            }
            Thread.sleep(5000);


        }
    }
}
