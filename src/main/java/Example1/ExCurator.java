package Example1;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class ExCurator {

	public static void main(String[] args) {
		try {

			CuratorFramework client;

			String zkConnString = "127.0.0.1:2181";
			client = CuratorFrameworkFactory.newClient(zkConnString,
					new ExponentialBackoffRetry(1000, 3));
			client.start();		

			// Escritura 
			String datos="hola";
			client.create().orSetData().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/ejemplo/escritura", datos.getBytes());
			// ls /ejemplo/escritura
			// get /ejemplo/escritura retorna "hola" (Cuidado si el nodo es efímero)
			
			// Lectura
			byte []datos2;
			datos2=client.getData().forPath("/ejemplo/escritura");
			System.out.println(new String(datos2));

			// Comprobar si una ruta existe
			if (client.checkExists().forPath("/ejemplo/noexiste")==null) {
				System.out.println("Ruta no existe");
			}

			
			CuratorCache cache = CuratorCache.build(client, "/ejemplo/cache");
	        CuratorCacheListener listener = CuratorCacheListener.builder()
	            .forCreates(node -> System.out.println(String.format("Node created. Old value: [%s]", node)))
	            .forChanges((oldNode, node) -> System.out.println(String.format("Node changed. Old: [%s] New: [%s]", oldNode, node)))
	            .forDeletes(oldNode -> System.out.println(String.format("Node deleted. Old value: [%s]", oldNode)))
	            .forInitialized(() -> System.out.println("Cache initialized"))
	            .build();

	        // Registrar listener
	        cache.listenable().addListener(listener);

	        // Iniciar cache
	        // Si hay ya nodos creados, se recibirán en .forCreates
	        // Si los nodos tienen varios niveles de /, se reciben cada / en un .forCreates
	        cache.start();
	        
	        Thread.sleep(3000);

			client.create().orSetData().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/ejemplo/cache", datos.getBytes());

			Thread.sleep(1000);
	        
	        // Modificado
			datos="modificado";
			client.create().orSetData().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/ejemplo/cache", datos.getBytes());
			
			Thread.sleep(1000);
	        
	        // Borrado	        
			client.delete().forPath("/ejemplo/cache");
			
			// Hijos (nodos efímeros no pueden tener hijos)
			// Si ya existen no da error (orSetData hace que se establezcan datos)
			client.create().orSetData().creatingParentsIfNeeded().forPath("/ejemplo2/escritura", new String("padre").getBytes());
			client.create().orSetData().creatingParentsIfNeeded().forPath("/ejemplo2/escritura/hijo1", new String("hijo1").getBytes());
			client.create().orSetData().creatingParentsIfNeeded().forPath("/ejemplo2/escritura/hijo2", new String("hijo2").getBytes());
          	
			GetChildrenBuilder childrenBuilder = client.getChildren();
			List<String> children = childrenBuilder.forPath("/ejemplo2/escritura");
			
			if (children != null) {
				for (String child : children) {  // El nodo padre no sale
					System.out.println(child);
				}
			}

			// Stream y conversión a lista
			client.create().orSetData().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/ejemplo/cache/cache1", datos.getBytes());
			client.create().orSetData().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/ejemplo/cache/cache2", datos.getBytes());
			client.create().orSetData().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/ejemplo/cache/cache3", datos.getBytes());
			Thread.sleep(3000);
	
			System.out.println("LISTANDO");  // /ejemplo/cache/cache2 no sale por filtro. /ejemplo/cache sí sale
			List<ChildData> list = cache.stream().filter(n -> !n.getPath().equals("/ejemplo/cache/cache2")).collect(Collectors.toList());
			for (ChildData child2: list)
				System.out.println(child2);
		
	        
			while (true) Thread.sleep(1000);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        

	}

}
