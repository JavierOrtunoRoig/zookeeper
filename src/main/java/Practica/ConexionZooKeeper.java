package Practica;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class ConexionZooKeeper {

    static CuratorFramework conexion() {
        CuratorFramework client;

        String zkConnString = "127.0.0.1:2181";
        client = CuratorFrameworkFactory.newClient(zkConnString,
                new ExponentialBackoffRetry(1000, 3));
        client.start();

        return client;
    }
}
