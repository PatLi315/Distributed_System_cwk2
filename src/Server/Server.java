package Server;

import Common.Player;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
public class Server {
    public static int id = 0;
    public static ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>();
    public static CopyOnWriteArrayList<Player> waitingPool = new CopyOnWriteArrayList<>();
    public static ConcurrentHashMap<Integer, GameInfo> games = new ConcurrentHashMap<>();
    public synchronized static int generateGameId() {
        id += 1;
        return id;
    }
}
