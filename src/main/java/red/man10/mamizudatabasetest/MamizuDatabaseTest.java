package red.man10.mamizudatabasetest;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class MamizuDatabaseTest extends JavaPlugin {

    MySQLManager mysql;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        mysql = new MySQLManager(this, "MamizuDatabaseTest");
        getCommand("mamizutest").setExecutor(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player p = (Player) sender;
        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("save")) {
                double health = p.getHealth();
                int food = p.getFoodLevel();
                Save(p,health,food);
            } else if(args[0].equalsIgnoreCase("load")) {
                load(p);
            }
        }
        return true;
    }

    public void Save(Player p,double health,int food){
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            String sql = "SELECT * FROM users WHERE uuid = '" + p.getUniqueId().toString() + "';";
            ResultSet rs = mysql.query(sql);
            if (rs != null) {
                try {
                    if(rs.next()) {
                        mysql.close();
                        String sqls = "UPDATE users SET health = "+health+" , food = "+food+" WHERE uuid = '" + p.getUniqueId().toString() + "';";
                        mysql.execute(sqls);
                        return;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }
            }
            mysql.close();
            String sqls = "INSERT INTO users (name,uuid,health,food) VALUES ('"+p.getName()+"','" + p.getUniqueId().toString() + "',"+health+","+food+");";
            p.sendMessage("§a§lセーブ完了");
            mysql.execute(sqls);
        });
    }

    public void load(Player p){
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            String sql = "SELECT * FROM users WHERE uuid = '" + p.getUniqueId().toString() + "';";
            ResultSet rs = mysql.query(sql);
            if (rs == null) {
                p.sendMessage("§c§lデータが存在しません");
                mysql.close();
                return;
            }
            try {
                if (rs.next()) {
                    double health = rs.getDouble("health");
                    int food = rs.getInt("food");
                    p.setHealth(health);
                    p.setFoodLevel(food);
                    p.sendMessage("§a§lデータをロードしました");
                }else{
                    p.sendMessage("§c§lデータが存在しません");
                }
                rs.close();
            } catch (NullPointerException | SQLException e1) {
                e1.printStackTrace();
                return;
            }
            mysql.close();
        });
    }
}
